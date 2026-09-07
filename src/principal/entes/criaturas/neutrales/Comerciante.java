package principal.entes.criaturas.neutrales;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import principal.animaciones.criaturas.AnimacionesComerciante;
import principal.dialogos.MensajeDialogo;
import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Jugador;
import principal.entes.facciones.GestorFacciones;
import principal.entes.objetos.items.Item;
import principal.interaccion.Interactuable;
import principal.inventario.Contenedor;
import principal.inventario.tienda.InventarioTienda;
import principal.inventario.vault.InventarioVault;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;

/**
 * NPC Mercader con soporte para stock infinito o finito, catálogo plantilla y
 * renovación periódica de mercancía según el calendario in-game.
 * 
 * @version 3.0 (Vanilla Java 8 - Dynamic Catalog & Restock Support)
 */
public class Comerciante extends Criatura implements Contenedor, Interactuable {

	private final String nombre;
	private final InventarioTienda INVENTARIO;
	private final AnimacionesComerciante ANIMACION;

	// Respaldo de mercancía original para renovaciones periódicas
	private final ArrayList<Item> catalogoPlantilla = new ArrayList<Item>();

	// Configuración de renovación automática por calendario
	private boolean renovacionAutomatica = false;
	private int intervaloDiasRenovacion = 3; // Cada 3 días por defecto
	private int ultimoDiaRenovado = 1;

	private final GestorTiempo GT_PAUSA_MIRADA = new GestorTiempo();
	private static final Random RANDOM = new Random();

	public Comerciante(final double x, final double y, final String nombre, final double vidaMaxima) {
		super(x, y, 12, 20, vidaMaxima, vidaMaxima, 0.4);

		this.nombre = (nombre != null) ? nombre : "Comerciante";
		this.setFaccion(GestorFacciones.FACCION_ALDEANOS);

		this.INVENTARIO = new InventarioTienda(this, 15, 5, "Tienda de " + this.nombre);
		this.ANIMACION = new AnimacionesComerciante();

		this.direccion = Direccion.SUR;
		this.setEstadoUnico(Estado.ESTANDAR);
		this.GT_PAUSA_MIRADA.establecerReferenciaTiempoActual();

		if ((Globales.GESTOR_LUZ != null) && (Globales.GESTOR_LUZ.getCiclo() != null)) {
			this.ultimoDiaRenovado = Globales.GESTOR_LUZ.getCiclo().getDiaActual();
		}
	}

	public Comerciante(final double x, final double y) {
		this(x, y, "Comerciante", 100.0);
	}

	@Override
	public void actualizar() {
		super.actualizar();

		this.INVENTARIO.actualizarEstadoCofre();

		// Comprobar renovación periódica de stock por días del calendario
		this.actualizarRenovacionStock();

		// Mirar a diferentes lados periódicamente si está ocioso
		if (!this.estaEstadoCaminando() && this.GT_PAUSA_MIRADA.transcurrioMiliSegundos(4000 + RANDOM.nextInt(3000))) {
			this.GT_PAUSA_MIRADA.establecerReferenciaTiempoActual();
			final Direccion[] dirs = Direccion.values();
			this.direccion = dirs[RANDOM.nextInt(dirs.length)];
		}

		final int tipoAnim = this.estaEstadoCaminando() ? AnimacionesComerciante.CAMINANDO
				: AnimacionesComerciante.ESTANDAR;
		this.ANIMACION.actualizar(this.direccion, tipoAnim);

		if (this.mundo != null) {
			this.atrasDeComplemento = this.mundo.colisionaConObjetoSolidoPeroEnZonaNoSolida(this.getArea());
		}
	}

	private void actualizarRenovacionStock() {
		if (this.renovacionAutomatica && (Globales.GESTOR_LUZ != null) && (Globales.GESTOR_LUZ.getCiclo() != null)) {
			final int diaActual = Globales.GESTOR_LUZ.getCiclo().getDiaActual();
			if ((diaActual - this.ultimoDiaRenovado) >= this.intervaloDiasRenovacion) {
				this.renovarMercancia();
				this.ultimoDiaRenovado = diaActual;
			}
		}
	}

	/**
	 * Añade un ítem a la venta y lo guarda en la plantilla de renovación.
	 */
	public void registrarMercanciaInicial(final Item item) {
		if (item != null) {
			this.INVENTARIO.agregarItem(item);
			this.catalogoPlantilla.add((Item) item.copiar());
		}
	}

	/**
	 * Restablece la tienda con el catálogo plantilla original.
	 */
	public void renovarMercancia() {
		this.INVENTARIO.vaciar();
		for (int i = 0; i < this.catalogoPlantilla.size(); i++) {
			this.INVENTARIO.agregarItem((Item) this.catalogoPlantilla.get(i).copiar());
		}
	}

	// =========================================================================
	// TOGGLES Y CONFIGURACIÓN DINÁMICA
	// =========================================================================

	public void setStockInfinito(final boolean infinito) {
		this.INVENTARIO.setStockInfinito(infinito);
	}

	public boolean isStockInfinito() {
		return this.INVENTARIO.isStockInfinito();
	}

	public boolean conmutarStockInfinito() {
		this.setStockInfinito(!this.isStockInfinito());
		return this.isStockInfinito();
	}

	public void setRenovacionAutomatica(final boolean renovar, final int intervaloDias) {
		this.renovacionAutomatica = renovar;
		this.intervaloDiasRenovacion = Math.max(1, intervaloDias);
	}

	public boolean isRenovacionAutomatica() {
		return this.renovacionAutomatica;
	}

	public boolean conmutarRenovacionAutomatica() {
		this.renovacionAutomatica = !this.renovacionAutomatica;
		return this.renovacionAutomatica;
	}

	public int getIntervaloDiasRenovacion() {
		return this.intervaloDiasRenovacion;
	}

	public void setIntervaloDiasRenovacion(final int dias) {
		this.intervaloDiasRenovacion = Math.max(1, dias);
	}

	@Override
	public String getTextoPrompt() {
		return "Hablar con " + this.nombre;
	}

	@Override
	public void interactuar(final Jugador jugador) {
		if (jugador == null) {
			return;
		}

		this.setDireccionMirandoCriatura(jugador);

		final MensajeDialogo dialogoComercio = new MensajeDialogo(this.nombre, new Color(255, 215, 80),
				"¡Saludos! Tengo provisiones y equipo de primera. Clic en mi tienda para comprar, o clic derecho en tus objetos para vender.",
				null);

		dialogoComercio.agregarOpcion("1. Ver mercancía a la venta", () -> {
			Comerciante.this.INVENTARIO.abrir();
		});

		dialogoComercio.agregarOpcion("2. Pedir información del lugar", () -> {
			final String rumor = Comerciante.this.obtenerRumorAleatorio();
			final MensajeDialogo dialogoRumor = new MensajeDialogo(Comerciante.this.nombre, rumor);
			Globales.GESTOR_DIALOGOS.iniciarDialogo(dialogoRumor);
		});

		dialogoComercio.agregarOpcion("3. Ahora no necesito nada, gracias.", () -> {
		});

		Globales.GESTOR_DIALOGOS.iniciarDialogo(dialogoComercio);
	}

	private String obtenerRumorAleatorio() {
		final String[] rumores = {
				"Los bandidos del norte custodian un cofre sellado... necesitarás una llave para abrirlo.",
				"Si vas a entrar a las cuevas oscuras, no olvides llevar antorchas o una linterna.",
				"La carne y los consumibles restauran tu salud mucho más rápido si descansas en una fogata.",
				"Ten cuidado al talar árboles; el ruido de los hachazos puede alertar a las patrullas cercanas." };
		return rumores[RANDOM.nextInt(rumores.length)];
	}

	@Override
	public void pintar(final Graphics2D g) {
		final int drawX = this.getPosicionXIntDibujado();
		final int drawY = this.getPosicionYIntDibujado();
		final boolean flash = this.estaEnFlashDanio();
		final int tipoAnim = this.estaEstadoCaminando() ? AnimacionesComerciante.CAMINANDO
				: AnimacionesComerciante.ESTANDAR;

		this.ANIMACION.pintar(g, drawX, drawY, this.direccion, tipoAnim, this.atrasDeComplemento, true, flash);

		super.pintar(g);
	}

	@Override
	public void establecerMargenesSprite() {
		this.margenXInicialSprite = 10;
		this.margenYInicialSprite = 6;
		this.margenXFinalSprite = 9;
		this.margenYFinalSprite = 3;
	}

	@Override
	public InventarioVault getInventario() {
		return this.INVENTARIO;
	}

	public InventarioTienda getInventarioTienda() {
		return this.INVENTARIO;
	}

	@Override
	public String getNombreContenedor() {
		return this.INVENTARIO.getNombre();
	}

	@Override
	public Ente getEntePropietario() {
		return this;
	}

	@Override
	public String getNombre() {
		return this.nombre;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JSONObject exportarParaJSON() {
		final JSONObject json = new JSONObject();
		json.put("x", Integer.valueOf(this.getPosicionXInt()));
		json.put("y", Integer.valueOf(this.getPosicionYInt()));
		json.put("nombre", this.nombre);
		json.put("vidaMaxima", Double.valueOf(this.vidaMaxima));
		json.put("stockInfinito", Boolean.valueOf(this.isStockInfinito()));
		json.put("renovacionAuto", Boolean.valueOf(this.renovacionAutomatica));
		json.put("intervaloRenovacion", Integer.valueOf(this.intervaloDiasRenovacion));

		final JSONArray itemsArray = new JSONArray();
		for (final Item i : this.INVENTARIO.getItems()) {
			itemsArray.add(i.getJsonItem());
		}
		json.put("stock", itemsArray);

		return json;
	}

	public static Comerciante crearDesdeJSON(final JSONObject json) {
		if (json == null) {
			return new Comerciante(0, 0);
		}

		final int x = (json.get("x") != null) ? ((Number) json.get("x")).intValue() : 0;
		final int y = (json.get("y") != null) ? ((Number) json.get("y")).intValue() : 0;
		final String nombre = (json.get("nombre") != null) ? json.get("nombre").toString() : "Comerciante";
		final double vidaMax = (json.get("vidaMaxima") != null) ? ((Number) json.get("vidaMaxima")).doubleValue()
				: 100.0;

		final Comerciante comerciante = new Comerciante(x, y, nombre, vidaMax);

		if (json.get("stockInfinito") != null) {
			comerciante.setStockInfinito(Boolean.parseBoolean(json.get("stockInfinito").toString()));
		}
		if (json.get("renovacionAuto") != null) {
			final boolean auto = Boolean.parseBoolean(json.get("renovacionAuto").toString());
			final int dias = (json.get("intervaloRenovacion") != null)
					? ((Number) json.get("intervaloRenovacion")).intValue()
					: 3;
			comerciante.setRenovacionAutomatica(auto, dias);
		}

		if (json.get("stock") instanceof JSONArray) {
			final JSONArray itemsArray = (JSONArray) json.get("stock");
			for (final Object obj : itemsArray) {
				if (obj instanceof JSONObject) {
					final Item i = Item.crearItemDesdeJson((JSONObject) obj);
					if (i != null) {
						comerciante.registrarMercanciaInicial(i);
					}
				}
			}
		}

		return comerciante;
	}

	@Override
	public String exportarTipoCriatura() {
		return "Comerciante";
	}
}