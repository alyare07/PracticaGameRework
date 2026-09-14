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
import principal.ia.arbol.FabricaArbolesIA;
import principal.iluminacion.CicloDiaNoche;
import principal.iluminacion.Estacion;
import principal.interaccion.Interactuable;
import principal.inventario.Contenedor;
import principal.inventario.tienda.InventarioTienda;
import principal.inventario.vault.InventarioVault;
import principal.utilidades.Globales;

/**
 * NPC Mercader con soporte de autopreservación, retorno a su tienda y catálogo
 * estacional (Zero-GC).
 * 
 * @version 6.0 (Vanilla Java 8 - Threat Evasion & Shop Return Anchor
 *          Integration)
 */
public class Comerciante extends Criatura implements Contenedor, Interactuable {

	private final String nombre;
	private final InventarioTienda INVENTARIO;
	private final AnimacionesComerciante ANIMACION;

	private final ArrayList<Item> catalogoPlantilla = new ArrayList<Item>();

	@SuppressWarnings("unchecked")
	private final ArrayList<Item>[] catalogosEstacionales = new ArrayList[4];

	private boolean renovacionAutomatica = false;
	private ModoRenovacion modoRenovacion = ModoRenovacion.POR_INTERVALO_DIAS;
	private int intervaloDiasRenovacion = 3;
	private int ultimoDiaRenovado = 1;

	private static final Random RANDOM = new Random();

	public Comerciante(final double x, final double y, final String nombre, final double vidaMaxima) {
		super(x, y, 12, 20, vidaMaxima, vidaMaxima, 0.4);

		this.nombre = (nombre != null) ? nombre : "Comerciante";
		this.setFaccion(GestorFacciones.FACCION_ALDEANOS);

		this.INVENTARIO = new InventarioTienda(this, 15, 5, "Tienda de " + this.nombre);
		this.ANIMACION = new AnimacionesComerciante();

		for (int i = 0; i < 4; i++) {
			this.catalogosEstacionales[i] = new ArrayList<Item>();
		}

		this.direccion = Direccion.SUR;
		this.setEstadoUnico(Estado.ESTANDAR);

		this.configurarFootprint(10, 8, 0);
		this.arbolComportamiento = FabricaArbolesIA.ARBOL_NPC_COMERCIANTE;

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
		this.actualizarRenovacionStock();

		final int tipoAnim = this.estaEnMovimientoFisico() ? AnimacionesComerciante.CAMINANDO
				: AnimacionesComerciante.ESTANDAR;
		this.ANIMACION.actualizar(this.direccion, tipoAnim);

		if (this.mundo != null) {
			this.atrasDeComplemento = this.mundo.colisionaConObjetoSolidoPeroEnZonaNoSolida(this.getArea());
		}
	}

	private void actualizarRenovacionStock() {
		if (!this.renovacionAutomatica || (Globales.GESTOR_LUZ == null) || (Globales.GESTOR_LUZ.getCiclo() == null)) {
			return;
		}

		final CicloDiaNoche ciclo = Globales.GESTOR_LUZ.getCiclo();
		final int diaActual = ciclo.getDiaActual();

		boolean debeRenovar = false;

		switch (this.modoRenovacion) {
		case CADA_LUNES:
			debeRenovar = (ciclo.getIndiceDiaSemana() == 0) && (diaActual != this.ultimoDiaRenovado);
			break;
		case CADA_ESTACION:
			debeRenovar = (ciclo.getDiaDeLaEstacion() == 1) && (diaActual != this.ultimoDiaRenovado);
			break;
		case POR_INTERVALO_DIAS:
		default:
			debeRenovar = (diaActual - this.ultimoDiaRenovado) >= this.intervaloDiasRenovacion;
			break;
		}

		if (debeRenovar) {
			this.renovarMercancia();
			this.ultimoDiaRenovado = diaActual;
		}
	}

	public void registrarMercanciaInicial(final Item item) {
		if (item != null) {
			this.INVENTARIO.agregarItem(item);
			this.catalogoPlantilla.add((Item) item.copiar());
		}
	}

	public void registrarMercanciaEstacional(final Estacion estacion, final Item item) {
		if ((estacion != null) && (item != null)) {
			this.catalogosEstacionales[estacion.ordinal()].add((Item) item.copiar());

			if ((Globales.GESTOR_LUZ != null) && (Globales.GESTOR_LUZ.getCiclo() != null)) {
				if (Globales.GESTOR_LUZ.getCiclo().getEstacionActual() == estacion) {
					this.INVENTARIO.agregarItem((Item) item.copiar());
				}
			}
		}
	}

	public void renovarMercancia() {
		this.INVENTARIO.vaciar();

		for (int i = 0; i < this.catalogoPlantilla.size(); i++) {
			this.INVENTARIO.agregarItem((Item) this.catalogoPlantilla.get(i).copiar());
		}

		if ((Globales.GESTOR_LUZ != null) && (Globales.GESTOR_LUZ.getCiclo() != null)) {
			final Estacion est = Globales.GESTOR_LUZ.getCiclo().getEstacionActual();
			final ArrayList<Item> listaEstacional = this.catalogosEstacionales[est.ordinal()];
			for (int i = 0; i < listaEstacional.size(); i++) {
				this.INVENTARIO.agregarItem((Item) listaEstacional.get(i).copiar());
			}
		}
	}

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
		this.modoRenovacion = ModoRenovacion.POR_INTERVALO_DIAS;
	}

	public void setRenovacionAutomatica(final boolean renovar, final ModoRenovacion modo) {
		this.renovacionAutomatica = renovar;
		this.modoRenovacion = (modo != null) ? modo : ModoRenovacion.POR_INTERVALO_DIAS;
	}

	public boolean isRenovacionAutomatica() {
		return this.renovacionAutomatica;
	}

	public ModoRenovacion getModoRenovacion() {
		return this.modoRenovacion;
	}

	public void setModoRenovacion(final ModoRenovacion modo) {
		if (modo != null) {
			this.modoRenovacion = modo;
		}
	}

	public int getIntervaloDiasRenovacion() {
		return this.intervaloDiasRenovacion;
	}

	public void setIntervaloDiasRenovacion(final int dias) {
		this.intervaloDiasRenovacion = Math.max(1, dias);
	}

	public int getUltimoDiaRenovado() {
		return this.ultimoDiaRenovado;
	}

	public void setUltimoDiaRenovado(final int dia) {
		this.ultimoDiaRenovado = dia;
	}

	@Override
	public String getTextoPrompt() {
		if (this.tieneEstado(Estado.HUYENDO) || this.blackboard.isEnPanico()) {
			return "¡" + this.nombre + " está huyendo!";
		}
		return "Hablar con " + this.nombre;
	}

	@Override
	public void interactuar(final Jugador jugador) {
		if (jugador == null) {
			return;
		}

		// Si está en peligro o huyendo, no abre la tienda y pide auxilio
		if (this.tieneEstado(Estado.HUYENDO) || this.blackboard.isEnPanico()) {
			this.setDireccionMirandoCriatura(jugador);
			final MensajeDialogo dialogoPanico = new MensajeDialogo(this.nombre, new Color(255, 70, 70),
					"¡Por los cielos, me están atacando! ¡No puedo comerciar ahora, ayúdame!", null);
			Globales.GESTOR_DIALOGOS.iniciarDialogo(dialogoPanico);
			return;
		}

		this.setDireccionMirandoCriatura(jugador);
		final String saludo = this.obtenerSaludoContextual();

		final MensajeDialogo dialogoComercio = new MensajeDialogo(this.nombre, new Color(255, 215, 80), saludo, null);

		dialogoComercio.agregarOpcion("1. Ver mercancía a la venta", () -> {
			Comerciante.this.INVENTARIO.abrir();
		});

		dialogoComercio.agregarOpcion("2. Pedir información o rumores", () -> {
			final String rumor = Comerciante.this.obtenerRumorAleatorio();
			final MensajeDialogo dialogoRumor = new MensajeDialogo(Comerciante.this.nombre, rumor);
			Globales.GESTOR_DIALOGOS.iniciarDialogo(dialogoRumor);
		});

		dialogoComercio.agregarOpcion("3. Ahora no necesito nada, gracias.", () -> {
		});

		Globales.GESTOR_DIALOGOS.iniciarDialogo(dialogoComercio);
	}

	private String obtenerSaludoContextual() {
		if ((Globales.GESTOR_LUZ == null) || (Globales.GESTOR_LUZ.getCiclo() == null)) {
			return "¡Saludos! Tengo provisiones y equipo de primera. Echa un vistazo a mi catálogo.";
		}

		final Estacion est = Globales.GESTOR_LUZ.getCiclo().getEstacionActual();
		final boolean tormenta = (Globales.GESTOR_CLIMA != null) && Globales.GESTOR_CLIMA.isTormentaActiva();

		if (tormenta) {
			return "¡Menudo temporal! Pasa y sacúdete la lluvia. No conviene estar afuera con estos rayos.";
		}

		switch (est) {
		case INVIERNO:
			return "¡Cierra rápido la puerta, que entra la escarcha! Si vas a viajar con este frío, asegúrate de llevar abrigo y fuego.";
		case VERANO:
			return "¡Uf, qué bochorno! El sol no da tregua hoy. Tengo provisiones frescas si te preparas para una caminata larga.";
		case OTONO:
			return "Las hojas caen y las noches empiezan a enfriar. Es buen momento para surtirse antes de que llegue la nieve.";
		case PRIMAVERA:
		default:
			return "¡Buen día! La primavera trae buena caza y caminos despejados. ¿Qué mercancía buscas hoy?";
		}
	}

	private String obtenerRumorAleatorio() {
		final String[] rumores = {
				"Los bandidos del norte custodian un cofre sellado... necesitarás una llave para abrirlo.",
				"Si vas a entrar a las cuevas oscuras, no olvides llevar antorchas o una linterna.",
				"La carne y los consumibles restauran tu salud mucho más rápido si descansas en una fogata.",
				"Ten cuidado al talar árboles; el ruido de los hachazos puede alertar a las patrullas cercanas.",
				"Dicen que en invierno las ventiscas pueden congelar a un viajero en cuestión de minutos si no lleva abrigo." };
		return rumores[RANDOM.nextInt(rumores.length)];
	}

	@Override
	public void pintar(final Graphics2D g) {
		final int drawX = this.getPosicionXIntDibujado();
		final int drawY = this.getPosicionYIntDibujado();
		final boolean flash = this.estaEnFlashDanio();
		final int tipoAnim = this.estaEnMovimientoFisico() ? AnimacionesComerciante.CAMINANDO
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
		json.put("modoRenovacion", this.modoRenovacion.name());
		json.put("intervaloRenovacion", Integer.valueOf(this.intervaloDiasRenovacion));
		json.put("ultimoDiaRenovado", Integer.valueOf(this.ultimoDiaRenovado));

		final JSONArray itemsArray = new JSONArray();
		for (final Item i : this.INVENTARIO.getItems()) {
			itemsArray.add(i.getJsonItem());
		}
		json.put("stock", itemsArray);

		final JSONObject jsonEstacional = new JSONObject();
		for (int est = 0; est < 4; est++) {
			final ArrayList<Item> lista = this.catalogosEstacionales[est];
			if (!lista.isEmpty()) {
				final JSONArray arr = new JSONArray();
				for (int i = 0; i < lista.size(); i++) {
					arr.add(lista.get(i).getJsonItem());
				}
				jsonEstacional.put(Estacion.VALORES[est].name(), arr);
			}
		}
		json.put("stockEstacional", jsonEstacional);

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
		if (json.get("modoRenovacion") != null) {
			try {
				comerciante.setModoRenovacion(ModoRenovacion.valueOf(json.get("modoRenovacion").toString()));
			} catch (final Exception ignored) {
			}
		}
		if (json.get("renovacionAuto") != null) {
			final boolean auto = Boolean.parseBoolean(json.get("renovacionAuto").toString());
			final int dias = (json.get("intervaloRenovacion") != null)
					? ((Number) json.get("intervaloRenovacion")).intValue()
					: 3;
			comerciante.setRenovacionAutomatica(auto, dias);
		}
		if (json.get("ultimoDiaRenovado") != null) {
			comerciante.setUltimoDiaRenovado(((Number) json.get("ultimoDiaRenovado")).intValue());
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

		if (json.get("stockEstacional") instanceof JSONObject) {
			final JSONObject jEst = (JSONObject) json.get("stockEstacional");
			for (final Estacion est : Estacion.VALORES) {
				if (jEst.get(est.name()) instanceof JSONArray) {
					final JSONArray arr = (JSONArray) jEst.get(est.name());
					for (final Object objItem : arr) {
						if (objItem instanceof JSONObject) {
							final Item item = Item.crearItemDesdeJson((JSONObject) objItem);
							if (item != null) {
								comerciante.registrarMercanciaEstacional(est, item);
							}
						}
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