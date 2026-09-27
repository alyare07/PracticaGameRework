package principal.mapa;

import java.util.Base64;

import principal.configuracion.Dificultad;
import principal.entes.criaturas.jugador.Jugador;
import principal.entes.objetos.items.equipablesmano.ItemMapa;
import principal.iluminacion.FuenteLuz;
import principal.iluminacion.TipoLuz;
import principal.inventario.equipamiento.SlotManager;
import principal.maquinaestado.estados.editor.metadatos.MetadatosEscenario;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;

/**
 * Matriz plana en RAM de Niebla de Guerra de 3 estados (Zero-GC / O(1)).
 * Empaquetado bitwise de 4 celdas por byte para serialización ultraligera en
 * Base64.
 * 
 * @version 1.0 (Vanilla Java 8 - High-Performance Fog of War)
 */
public final class NieblaGuerra {

	public static final byte TERRA_INCOGNITA = 0; // Negro absoluto
	public static final byte EN_MEMORIA = 1; // Terreno desaturado / visitado
	public static final byte VISION_ACTIVA = 2; // Visible en tiempo real

	private static final int RADIO_VISION_DIURNA_TILES = 12;
	private static final int RADIO_VISION_PENUMBRA_TILES = 3;

	private final int anchoTiles;
	private final int altoTiles;
	private final byte[] celdas;

	// Bounding Box de la última visión activa para degradación en O(R^2)
	private int prevMinX = -1;
	private int prevMaxX = -1;
	private int prevMinY = -1;
	private int prevMaxY = -1;

	private int lastPlayerTileX = Integer.MIN_VALUE;
	private int lastPlayerTileY = Integer.MIN_VALUE;
	private boolean lastPermisoRevelar = false;
	private long versionVision = 0;

	public NieblaGuerra(final int anchoTiles, final int altoTiles) {
		this.anchoTiles = Math.max(1, anchoTiles);
		this.altoTiles = Math.max(1, altoTiles);
		this.celdas = new byte[this.anchoTiles * this.altoTiles];
	}

	/**
	 * Actualización perezosa vinculada al cambio de tile del jugador, de luz o de
	 * equipamiento (Zero-GC).
	 */
	public void actualizar(final Jugador jugador, final Mundo mundo) {
		if ((jugador == null) || (mundo == null)) {
			return;
		}

		final int pTileX = Math.floorDiv(jugador.getCentroX(), Constantes.LADO_TILE);
		final int pTileY = Math.floorDiv(jugador.getCentroY(), Constantes.LADO_TILE);

		final Dificultad dif = Globales.dificultad;
		final boolean tienePermisoActual = this.puedeRevelar(jugador, dif);

		// Si no ha cambiado de tile, no cambió de posición Y el permiso de mapa sigue
		// igual, omitimos cálculo
		if ((pTileX == this.lastPlayerTileX) && (pTileY == this.lastPlayerTileY) && !jugador.haCambiadoPosicion()
				&& (tienePermisoActual == this.lastPermisoRevelar)) {
			return;
		}

		this.lastPlayerTileX = pTileX;
		this.lastPlayerTileY = pTileY;
		this.lastPermisoRevelar = tienePermisoActual;

		// 1. Si no tiene permiso (o guardó el mapa en Hardcore), degrada la visión
		// activa a memoria y aborta
		if (!tienePermisoActual) {
			this.degradarVisionPreviaAMemoria();
			return;
		}

		// 2. Determinar radio de visión dinámico acoplado al SSOT lumínico
		final int radioTiles = this.calcularRadioVision(jugador, mundo);

		// 3. Aplicar degradación y nuevo círculo activo
		this.aplicarVisionActiva(pTileX, pTileY, radioTiles);
	}

	// =========================================================================
	// DETECCIÓN POLIMÓRFICA ROBUSTA (MANOS + HOTBAR + ALMACÉN)
	// =========================================================================
	private boolean puedeRevelar(final Jugador jugador, final Dificultad dif) {
		// En Fácil y Normal: Memoria Biológica activa
		if (dif.tieneMemoriaBiologicaCartografica()) {
			return true;
		}

		if ((Globales.GESTOR_INVENTARIO == null) || (Globales.GESTOR_INVENTARIO.getInventarioJugador() == null)) {
			return false;
		}

		final SlotManager sm = Globales.GESTOR_INVENTARIO.getInventarioJugador().getSlotManager();

		// En Hardcore: Obligatorio tener el mapa empuñado en una mano
		if (dif.requiereMapaEnMano()) {
			final boolean enPrincipal = (sm.getSlotArma() != null) && (sm.getSlotArma().getItem() instanceof ItemMapa);
			final boolean enSecundaria = (sm.getSlotManoSecundaria() != null)
					&& (sm.getSlotManoSecundaria().getItem() instanceof ItemMapa);
			return enPrincipal || enSecundaria;
		}

		// En Difícil: Basta con poseerlo en inventario, hotbar o mano
		final boolean enPrincipal = (sm.getSlotArma() != null) && (sm.getSlotArma().getItem() instanceof ItemMapa);
		final boolean enSecundaria = (sm.getSlotManoSecundaria() != null)
				&& (sm.getSlotManoSecundaria().getItem() instanceof ItemMapa);
		if (enPrincipal || enSecundaria) {
			return true;
		}

		for (int i = 0; i < sm.getSlotsPrincipales().size(); i++) {
			if (sm.getSlotsPrincipales().get(i).getItem() instanceof ItemMapa) {
				return true;
			}
		}
		for (int i = 0; i < sm.getSlotsAlmacen().size(); i++) {
			if (sm.getSlotsAlmacen().get(i).getItem() instanceof ItemMapa) {
				return true;
			}
		}

		return false;
	}

	private int calcularRadioVision(final Jugador jugador, final Mundo mundo) {
		final MetadatosEscenario meta = (mundo.getEscenario() != null) ? mundo.getEscenario().getMetadatos() : null;

		final boolean esCueva = (meta != null) && meta.esCueva();
		final boolean esInterior = (meta != null) && meta.esInterior();

		// Detección soberana mediante el canal Alpha de oscuridad celeste o modo
		// blackout
		final boolean hayOscuridadCeleste = (Globales.GESTOR_ASTRONOMICO != null)
				&& (Globales.GESTOR_ASTRONOMICO.isModoOscuridadTotal() || (Globales.GESTOR_ASTRONOMICO.getLuzA() > 80));

		if (esCueva || esInterior || hayOscuridadCeleste) {
			final FuenteLuz luz = jugador.getLuzAsignada();
			if ((luz != null) && luz.isActiva()) {
				if (luz.getTipo() == TipoLuz.ANTORCHA) {
					return Math.max(6, (int) Math.round(luz.getRadioBase() / Constantes.LADO_TILE));
				}
				if (luz.getTipo() == TipoLuz.LINTERNA_CONICA) {
					return Math.max(8, (int) Math.round(luz.getRadioBase() / Constantes.LADO_TILE));
				}
			}
			// Sin antorcha activa en penumbra u oscuridad subterránea
			return RADIO_VISION_PENUMBRA_TILES;
		}

		return RADIO_VISION_DIURNA_TILES;
	}

	protected void degradarVisionPreviaAMemoria() {
		if (this.prevMinX < 0) {
			return;
		}
		for (int y = this.prevMinY; y <= this.prevMaxY; y++) {
			final int filaOffset = y * this.anchoTiles;
			for (int x = this.prevMinX; x <= this.prevMaxX; x++) {
				final int idx = filaOffset + x;
				if (this.celdas[idx] == VISION_ACTIVA) {
					this.celdas[idx] = EN_MEMORIA;
				}
			}
		}
		this.prevMinX = -1;
	}

	private void aplicarVisionActiva(final int centroX, final int centroY, final int radio) {
		// Degradamos únicamente las celdas del paso previo
		this.degradarVisionPreviaAMemoria();

		final int minX = Math.max(0, centroX - radio);
		final int maxX = Math.min(this.anchoTiles - 1, centroX + radio);
		final int minY = Math.max(0, centroY - radio);
		final int maxY = Math.min(this.altoTiles - 1, centroY + radio);

		final int radioSq = radio * radio;

		for (int y = minY; y <= maxY; y++) {
			final int dy = y - centroY;
			final int dySq = dy * dy;
			final int filaOffset = y * this.anchoTiles;

			for (int x = minX; x <= maxX; x++) {
				final int dx = x - centroX;
				if (((dx * dx) + dySq) <= radioSq) {
					this.celdas[filaOffset + x] = VISION_ACTIVA;
				}
			}
		}

		// Guardamos el Bounding Box actual para el siguiente tick
		this.prevMinX = minX;
		this.prevMaxX = maxX;
		this.prevMinY = minY;
		this.prevMaxY = maxY;
		this.versionVision++;
	}

	public long getVersionVision() {
		return this.versionVision;
	}

	public byte getEstado(final int tileX, final int tileY) {
		if ((tileX < 0) || (tileX >= this.anchoTiles) || (tileY < 0) || (tileY >= this.altoTiles)) {
			return TERRA_INCOGNITA;
		}
		return this.celdas[(tileY * this.anchoTiles) + tileX];
	}

	public void revelarTodo() {
		for (int i = 0; i < this.celdas.length; i++) {
			this.celdas[i] = VISION_ACTIVA;
		}
	}

	public void resetear() {
		for (int i = 0; i < this.celdas.length; i++) {
			this.celdas[i] = TERRA_INCOGNITA;
		}
		this.prevMinX = -1;
	}

	// =========================================================================
	// PERSISTENCIA COMPACTA BITWISE EN BASE64 (4 CELDAS POR BYTE)
	// =========================================================================

	public String exportarBase64() {
		final int totalCeldas = this.celdas.length;
		final int totalBytes = (totalCeldas + 3) / 4;
		final byte[] empaquetado = new byte[totalBytes];

		for (int i = 0; i < totalCeldas; i++) {
			final int byteIdx = i / 4;
			final int bitShift = (i % 4) * 2;
			// Si la celda estaba en visión activa, al guardar se archiva en memoria
			// estática
			final byte valor = (this.celdas[i] == VISION_ACTIVA) ? EN_MEMORIA : this.celdas[i];
			empaquetado[byteIdx] |= (byte) ((valor & 0x03) << bitShift);
		}

		return Base64.getEncoder().encodeToString(empaquetado);
	}

	public void importarBase64(final String base64) {
		if ((base64 == null) || base64.isEmpty()) {
			return;
		}

		final byte[] empaquetado = Base64.getDecoder().decode(base64);
		final int totalCeldas = Math.min(this.celdas.length, empaquetado.length * 4);

		for (int i = 0; i < totalCeldas; i++) {
			final int byteIdx = i / 4;
			final int bitShift = (i % 4) * 2;
			this.celdas[i] = (byte) ((empaquetado[byteIdx] >> bitShift) & 0x03);
		}
		this.prevMinX = -1;
	}

	public int getAnchoTiles() {
		return this.anchoTiles;
	}

	public int getAltoTiles() {
		return this.altoTiles;
	}
}