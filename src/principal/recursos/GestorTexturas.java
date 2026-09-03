package principal.recursos;

import java.awt.Color;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.HojaSprite;

/**
 * Gestor maestro del subsistema gráfico y memoria VRAM (Zero-GC / O(1)).
 * Centraliza todo el acceso I/O del juego, garantizando que ningún archivo se
 * lea más de una vez del disco.
 * 
 * @version 1.1 (Vanilla Java 8 - Robust Pipeline)
 */
public class GestorTexturas {

	// =========================================================================
	// === 1. CACHÉ INTERNA DE ARCHIVOS MAESTROS (I/O DEDUPLICADO)
	// =========================================================================
	private final Map<String, BufferedImage> cacheImagenesRaw = new HashMap<String, BufferedImage>();

	// =========================================================================
	// === 2. ARREGLOS PLANOS PARA ACCESO O(1) POR ORDINAL
	// =========================================================================
	private final HojaSprite[] cacheHojas = new HojaSprite[ClaveHoja.values().length];
	private final HojaSprite[] cacheHojasVolteadasH = new HojaSprite[ClaveHoja.values().length];
	private final SetTerreno[] setsTerreno = new SetTerreno[TipoTerreno.values().length];
	private final BufferedImage[] cacheItems = new BufferedImage[TexturaItem.values().length];

	// =========================================================================
	// === 3. TEXTURAS ESPECIALES DE SISTEMA
	// =========================================================================
	private final BufferedImage texturaError;
	private final BufferedImage texturaTransparente;
	private final BufferedImage texturaContornoTile;
	private final BufferedImage texturaContornoGroupTile;

	public GestorTexturas() {
		this.texturaError = Globales.FUNCIONES.TEXTURAS_TOOLS.crearTexturaError(Constantes.LADO_TILE);
		this.texturaTransparente = Globales.FUNCIONES.TEXTURAS_TOOLS.crearImagenVRAM(1, 1, Transparency.TRANSLUCENT);
		this.texturaContornoTile = Globales.FUNCIONES.TEXTURAS_TOOLS
				.crearImagenRectanguloContornoEnVRAM(Constantes.LADO_TILE, Color.RED);
		this.texturaContornoGroupTile = Globales.FUNCIONES.TEXTURAS_TOOLS
				.crearImagenRectanguloContornoEnVRAM(Constantes.LADO_TILE * 2, Color.BLUE);

		this.cargarTodo();
	}

	private void cargarTodo() {
		this.precargarHojasSprites();
		this.precargarSetsTerreno();
		this.precargarTexturasItems();
	}

	// =========================================================================
	// === GESTIÓN DE I/O CENTRALIZADA (1 SOLA LECTURA POR RUTA)
	// =========================================================================

	public BufferedImage getImagenBase(final String ruta) {
		if ((ruta == null) || ruta.trim().isEmpty()) {
			return this.texturaError;
		}

		BufferedImage img = this.cacheImagenesRaw.get(ruta);
		if (img == null) {
			img = Globales.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida(ruta);
			if (img == null) {
				System.err.println("[GestorTexturas] Error al cargar recurso: " + ruta);
				return this.texturaError;
			}
			this.cacheImagenesRaw.put(ruta, img);
		}
		return img;
	}

	// =========================================================================
	// === PRECARGA DE HOJAS Y SPRITESHEETS
	// =========================================================================

	private void precargarHojasSprites() {
		for (final ClaveHoja clave : ClaveHoja.values()) {
			BufferedImage imagenBase = this.getImagenBase(clave.getRuta());

			if ((clave == ClaveHoja.DUNGEON_16) && (imagenBase != null)) {
				imagenBase = Globales.FUNCIONES.TEXTURAS_TOOLS.redimensionar(imagenBase, imagenBase.getWidth() / 2,
						imagenBase.getHeight() / 2);
			} else if ((clave == ClaveHoja.CASA_1) && (imagenBase != null)) {
				imagenBase = Globales.FUNCIONES.TEXTURAS_TOOLS.redimensionar(imagenBase, 64, 64);
			}

			final HojaSprite hoja = new HojaSprite(imagenBase, clave.getAnchoFrame(), clave.getAltoFrame(), false);

			this.cacheHojas[clave.ordinal()] = hoja;
			this.cacheHojasVolteadasH[clave.ordinal()] = hoja.crearVolteadaHorizontal();
		}
	}

	// =========================================================================
	// === PRECARGA DE TERRENOS Y AUTOTILES
	// =========================================================================

	private void precargarSetsTerreno() {
		final BufferedImage imgTerrenos = this.getImagenBase(ClaveHoja.TERRENOS_16.getRuta());
		final int lado = Constantes.LADO_TILE;
		final int columnasAutotile = 20;

		for (final TipoTerreno tipo : TipoTerreno.values()) {
			final int cantFrames = tipo.getCantFramesAnimacion();
			final BufferedImage[][] sprites = new BufferedImage[cantFrames][columnasAutotile];

			for (int f = 0; f < cantFrames; f++) {
				final int filaY = (tipo.getFilaSpritesheet() + f) * lado;

				for (int c = 0; c < columnasAutotile; c++) {
					final int colX = c * lado;
					final BufferedImage sub = imgTerrenos.getSubimage(colX, filaY, lado, lado);
					sprites[f][c] = Globales.FUNCIONES.TEXTURAS_TOOLS.convertirAVRAM(sub);
				}
			}

			this.setsTerreno[tipo.ordinal()] = new SetTerreno(tipo, sprites);
		}
	}

	// =========================================================================
	// === PRECARGA DE ÍTEMS Y PARTICULAS
	// =========================================================================

	private void precargarTexturasItems() {
		for (final TexturaItem item : TexturaItem.values()) {
			final HojaSprite hoja = this.getHoja(item.getHojaOrigen());
			BufferedImage sprite = (hoja != null) ? hoja.getSprite(item.getIndiceSprite()) : this.texturaError;

			if (item.name().endsWith("_MAPA") && (item.getHojaOrigen() == ClaveHoja.ARMAS_PACK_16)) {
				sprite = Globales.FUNCIONES.TEXTURAS_TOOLS.redimensionar(sprite, 8, 8);
			}

			this.cacheItems[item.ordinal()] = sprite;
		}
	}

	// =========================================================================
	// === ACCESORES PÚBLICOS O(1) PURO (ZERO-GC)
	// =========================================================================

	public BufferedImage get(final TexturaItem item) {
		return (item != null) ? this.cacheItems[item.ordinal()] : this.texturaError;
	}

	public HojaSprite getHoja(final ClaveHoja clave) {
		return (clave != null) ? this.cacheHojas[clave.ordinal()] : null;
	}

	public HojaSprite getHojaVolteadaH(final ClaveHoja clave) {
		return (clave != null) ? this.cacheHojasVolteadasH[clave.ordinal()] : null;
	}

	public SetTerreno getSetTerreno(final TipoTerreno tipo) {
		return (tipo != null) ? this.setsTerreno[tipo.ordinal()] : null;
	}

	public BufferedImage getTexturaError() {
		return this.texturaError;
	}

	public BufferedImage getTexturaTransparente() {
		return this.texturaTransparente;
	}

	public BufferedImage getTexturaContornoTile() {
		return this.texturaContornoTile;
	}

	public BufferedImage getTexturaContornoGroupTile() {
		return this.texturaContornoGroupTile;
	}

	public void liberarRecursos() {
		for (final BufferedImage img : this.cacheImagenesRaw.values()) {
			if (img != null) {
				img.flush();
			}
		}
		this.cacheImagenesRaw.clear();

		for (int i = 0; i < this.cacheItems.length; i++) {
			if (this.cacheItems[i] != null) {
				this.cacheItems[i].flush();
				this.cacheItems[i] = null;
			}
		}
	}
}