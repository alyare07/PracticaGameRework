package principal.mapa.escenario;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.enemigos.bandido.BandidoGarrote;
import principal.entes.criaturas.enemigos.bandido.BandidoGranadero;
import principal.entes.criaturas.enemigos.bandido.BandidoPistolero;
import principal.entes.objetos.ArbolCofre;
import principal.entes.objetos.Complemento;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.cofres.Cofre;
import principal.entes.objetos.items.Item;
import principal.entes.objetos.recursos.ArbolCosechable;
import principal.entes.objetos.recursos.RocaCosechable;
import principal.iluminacion.IntensidadNiebla;
import principal.iluminacion.TipoLuz;
import principal.iluminacion.ZonaAmbiente;
import principal.mapa.Mundo;
import principal.mapa.Terreno;
import principal.mapa.escenario.tps.PuertaArea;
import principal.mapa.escenario.tps.PuertaMapa;
import principal.mapa.escenario.tps.ZonaTP;
import principal.mapa.mapas.Spawn;
import principal.maquinaestado.estados.editor.metadatos.MetadatosEscenario;
import principal.utilidades.Globales;

/**
 * Representa la definición serializada completa de un mapa del juego. Carga de
 * forma autónoma Terreno, Entidades, Spawns, Triggers, Zonas de Ambiente, Luces
 * Estáticas y Metadatos climáticos/musicales.
 * 
 * @version 3.0 (Vanilla Java 8 - Data-Driven World)
 */
public class Escenario implements Serializable {

	private static final long serialVersionUID = 3351562178857131172L;

	protected final Terreno TERRENO;
	protected final String LISTA_CREACION_CRIATURAS_JSON;
	protected final String LISTA_CREACION_ITEMS_JSON;
	protected final String LISTA_CREACION_COMPLEMENTOS_JSON;
	protected final String LISTA_CREACION_OBJETOS_JSON;
	protected final String LISTA_CREACION_SPAWNS_JSON;
	protected final String LISTA_CREACION_TRIGGERS_JSON;
	protected final String LISTA_CREACION_ZONAS_AMBIENTE_JSON;
	protected final String LISTA_CREACION_LUCES_JSON;
	protected final MetadatosEscenario METADATOS;

	public Escenario(final Terreno mapa, final String criaturasJSON, final String itemsJSON,
			final String complementosJSON, final String objetosJSON, final String spawnsJSON, final String triggersJSON,
			final String zonasAmbienteJSON, final String lucesJSON, final MetadatosEscenario metadatos) {
		this.TERRENO = mapa;
		this.LISTA_CREACION_CRIATURAS_JSON = (criaturasJSON != null) ? criaturasJSON : "[]";
		this.LISTA_CREACION_ITEMS_JSON = (itemsJSON != null) ? itemsJSON : "[]";
		this.LISTA_CREACION_COMPLEMENTOS_JSON = (complementosJSON != null) ? complementosJSON : "[]";
		this.LISTA_CREACION_OBJETOS_JSON = (objetosJSON != null) ? objetosJSON : "[]";
		this.LISTA_CREACION_SPAWNS_JSON = (spawnsJSON != null) ? spawnsJSON : "[]";
		this.LISTA_CREACION_TRIGGERS_JSON = (triggersJSON != null) ? triggersJSON : "[]";
		this.LISTA_CREACION_ZONAS_AMBIENTE_JSON = (zonasAmbienteJSON != null) ? zonasAmbienteJSON : "[]";
		this.LISTA_CREACION_LUCES_JSON = (lucesJSON != null) ? lucesJSON : "[]";
		this.METADATOS = (metadatos != null) ? metadatos : new MetadatosEscenario();
	}

	// Sobrecargas de retrocompatibilidad
	public Escenario(final Terreno mapa, final String criaturasJSON, final String itemsJSON,
			final String complementosJSON, final String objetosJSON, final String spawnsJSON) {
		this(mapa, criaturasJSON, itemsJSON, complementosJSON, objetosJSON, spawnsJSON, "[]", "[]", "[]",
				new MetadatosEscenario());
	}

	public Escenario(final Terreno mapa, final String criaturasJSON, final String itemsJSON,
			final String complementosJSON, final String objetosJSON) {
		this(mapa, criaturasJSON, itemsJSON, complementosJSON, objetosJSON, "[]", "[]", "[]", "[]",
				new MetadatosEscenario());
	}

	public ArrayList<Spawn> generarSpawns() {
		final ArrayList<Spawn> spawns = new ArrayList<Spawn>();
		final JSONParser parse = new JSONParser();
		JSONArray lista = null;
		try {
			lista = (JSONArray) parse.parse(this.LISTA_CREACION_SPAWNS_JSON);
		} catch (final ParseException e) {
			lista = new JSONArray();
		}

		for (final Object obj : lista) {
			if (obj instanceof JSONObject) {
				final Spawn s = Spawn.crearDesdeJson((JSONObject) obj);
				if (s != null) {
					spawns.add(s);
				}
			}
		}
		return spawns;
	}

	public ArrayList<Criatura> generarListaCriaturas(final Mundo mundo) {
		final ArrayList<Criatura> criaturas = new ArrayList<Criatura>();
		final JSONParser parse = new JSONParser();
		JSONArray lista = null;
		try {
			lista = (JSONArray) parse.parse(this.LISTA_CREACION_CRIATURAS_JSON);
		} catch (final ParseException e) {
			lista = new JSONArray();
		}

		for (final Object obj : lista) {
			if (obj instanceof JSONObject) {
				final JSONObject json = (JSONObject) obj;
				final String tipo = (json.get("tipo") != null) ? json.get("tipo").toString() : "";
				final JSONObject entiti = (JSONObject) json.get("entiti");

				if (tipo.equals("Bandido") && (entiti != null)) {
					final int x = ((Number) entiti.get("x")).intValue();
					final int y = ((Number) entiti.get("y")).intValue();
					final double vida = (entiti.get("vida") != null) ? ((Number) entiti.get("vida")).doubleValue()
							: 50.0;
					final double vidaMax = (entiti.get("vidaMaxima") != null)
							? ((Number) entiti.get("vidaMaxima")).doubleValue()
							: 50.0;
					final String subtipo = (entiti.get("subtipo") != null) ? entiti.get("subtipo").toString()
							: "Pistolero";

					Criatura bandido = null;
					if (subtipo.equals("Pistolero")) {
						bandido = new BandidoPistolero(x, y, vida, vidaMax, mundo);
					} else if (subtipo.equals("Garrote")) {
						bandido = new BandidoGarrote(x, y, vida, vidaMax, mundo);
					} else if (subtipo.equals("Granadero")) {
						bandido = new BandidoGranadero(x, y, vida, vidaMax, mundo);
					}

					if (bandido != null) {
						criaturas.add(bandido);
					}
				}
			}
		}
		return criaturas;
	}

	public void generarListaComplementos(final Mundo mundo) {
		final ArrayList<Complemento> complementos = new ArrayList<Complemento>();
		final JSONParser parse = new JSONParser();
		JSONArray lista = null;
		try {
			lista = (JSONArray) parse.parse(this.LISTA_CREACION_COMPLEMENTOS_JSON);
		} catch (final ParseException e) {
			lista = new JSONArray();
		}

		for (final Object obj : lista) {
			if (obj instanceof JSONObject) {
				final JSONObject json = (JSONObject) obj;
				Complemento c = null;

				if ((json.get("esEspecial") != null) && Boolean.parseBoolean(json.get("esEspecial").toString())) {
					c = principal.entes.objetos.especial.CuadradoInvisible.crearDesdeJson(json);
				} else {
					c = Complemento.crearDesdeJson(json);
				}

				if (c != null) {
					complementos.add(c);
				}
			}
		}

		complementos.sort((c1, c2) -> Integer.compare(c1.getPosicionYBase(), c2.getPosicionYBase()));

		for (int i = 0; i < complementos.size(); i++) {
			mundo.meterEntidad(complementos.get(i));
		}
	}

	public ArrayList<Item> generarItemsEnTerreno() {
		final ArrayList<Item> items = new ArrayList<Item>();
		final JSONParser parse = new JSONParser();
		JSONArray lista = null;
		try {
			lista = (JSONArray) parse.parse(this.LISTA_CREACION_ITEMS_JSON);
		} catch (final ParseException e) {
			lista = new JSONArray();
		}

		for (final Object obj : lista) {
			if (obj instanceof JSONObject) {
				final Item i = Item.crearItemDesdeJson((JSONObject) obj);
				if ((i != null) && this.TERRENO.areaDentroDelTerreno(i.getArea())) {
					items.add(i);
				}
			}
		}
		return items;
	}

	public int generarObjetosEnTerreno(final Mundo mundo) {
		int cant = 0;
		final JSONParser parse = new JSONParser();
		JSONArray lista = null;
		try {
			lista = (JSONArray) parse.parse(this.LISTA_CREACION_OBJETOS_JSON);
		} catch (final ParseException e) {
			lista = new JSONArray();
		}

		for (final Object object : lista) {
			if (object instanceof JSONObject) {
				final JSONObject json = (JSONObject) object;
				final String tipo = (json.get("tipoObjeto") != null) ? json.get("tipoObjeto").toString()
						: (json.get("tipo") != null ? json.get("tipo").toString() : "");
				final JSONObject entiti = (json.get("entiti") instanceof JSONObject) ? (JSONObject) json.get("entiti")
						: json;

				Objeto obj = null;

				if (tipo.equals(Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Cofre.class))
						|| tipo.equals("Cofre")) {
					obj = Cofre.crearDesdeJSON(entiti);
				} else if (tipo.equals("ArbolCofre")) {
					obj = ArbolCofre.crearDesdeJson(entiti);
				} else if (tipo.equals("ArbolCosechable")) {
					obj = ArbolCosechable.crearDesdeJson(entiti);
				} else if (tipo.equals("RocaCosechable")) {
					obj = RocaCosechable.crearDesdeJson(entiti);
				}

				if (obj != null) {
					mundo.meterEntidad(obj);
					cant++;
				}
			}
		}
		return cant;
	}

	public void generarTriggers(final Mundo mundo) {
		if (mundo == null) {
			return;
		}

		final JSONParser parse = new JSONParser();
		JSONArray lista = null;
		try {
			lista = (JSONArray) parse.parse(this.LISTA_CREACION_TRIGGERS_JSON);
		} catch (final ParseException e) {
			lista = new JSONArray();
		}

		for (final Object obj : lista) {
			if (obj instanceof JSONObject) {
				final JSONObject json = (JSONObject) obj;
				final int x = ((Number) json.get("x")).intValue();
				final int y = ((Number) json.get("y")).intValue();
				final int w = ((Number) json.get("w")).intValue();
				final int h = ((Number) json.get("h")).intValue();
				final String tipoPuerta = json.get("tipo").toString();

				final Rectangle areaTP = new Rectangle(x, y, w, h);
				ZonaTP zonaTP = null;

				if (tipoPuerta.equals("PuertaMapa")) {
					final String rutaMapa = json.get("mapa").toString();
					final String spawn = json.get("spawn").toString();
					zonaTP = new ZonaTP(areaTP, new PuertaMapa(rutaMapa, spawn, false, null));
				} else if (tipoPuerta.equals("PuertaArea")) {
					final int dx = ((Number) json.get("destX")).intValue();
					final int dy = ((Number) json.get("destY")).intValue();
					zonaTP = new ZonaTP(areaTP, new PuertaArea(new Rectangle(dx, dy, 16, 16)));
				}

				if (zonaTP != null) {
					mundo.meterEntidad(zonaTP);
				}
			}
		}
	}

	public void generarZonasAmbiente() {
		if (Globales.GESTOR_ZONAS_AMBIENTE == null) {
			return;
		}

		final JSONParser parse = new JSONParser();
		JSONArray lista = null;
		try {
			lista = (JSONArray) parse.parse(this.LISTA_CREACION_ZONAS_AMBIENTE_JSON);
		} catch (final ParseException e) {
			lista = new JSONArray();
		}

		for (final Object obj : lista) {
			if (obj instanceof JSONObject) {
				final JSONObject json = (JSONObject) obj;
				final int x = ((Number) json.get("x")).intValue();
				final int y = ((Number) json.get("y")).intValue();
				final int w = ((Number) json.get("w")).intValue();
				final int h = ((Number) json.get("h")).intValue();
				final String nombre = json.get("nombre").toString();
				final boolean esInterior = Boolean.parseBoolean(json.get("interior").toString());

				final int r = ((Number) json.get("r")).intValue();
				final int g = ((Number) json.get("g")).intValue();
				final int b = ((Number) json.get("b")).intValue();
				final int a = ((Number) json.get("a")).intValue();

				IntensidadNiebla niebla = IntensidadNiebla.DESACTIVADA;
				if (json.get("niebla") != null) {
					try {
						niebla = IntensidadNiebla.valueOf(json.get("niebla").toString());
					} catch (final Exception ignored) {
					}
				}

				Globales.GESTOR_ZONAS_AMBIENTE
						.registrarZona(new ZonaAmbiente(x, y, w, h, new Color(r, g, b, a), niebla, nombre, esInterior));
			}
		}
	}

	public void generarLucesEstaticas() {
		if (Globales.GESTOR_LUZ == null) {
			return;
		}

		final JSONParser parse = new JSONParser();
		JSONArray lista = null;
		try {
			lista = (JSONArray) parse.parse(this.LISTA_CREACION_LUCES_JSON);
		} catch (final ParseException e) {
			lista = new JSONArray();
		}

		for (final Object obj : lista) {
			if (obj instanceof JSONObject) {
				final JSONObject json = (JSONObject) obj;
				final double x = ((Number) json.get("x")).doubleValue();
				final double y = ((Number) json.get("y")).doubleValue();
				final double radio = ((Number) json.get("radio")).doubleValue();
				final String tipoStr = json.get("tipo").toString();

				TipoLuz tipo = TipoLuz.ANTORCHA;
				try {
					tipo = TipoLuz.valueOf(tipoStr);
				} catch (final Exception ignored) {
				}

				Globales.GESTOR_LUZ.agregarLuzEstatica(x, y, tipo, radio);
			}
		}
	}

	public Terreno getTerreno() {
		return this.TERRENO;
	}

	public MetadatosEscenario getMetadatos() {
		return this.METADATOS;
	}

	public String getListaCreacionSpawnsJson() {
		return this.LISTA_CREACION_SPAWNS_JSON;
	}

	public String getListaCreacionTriggersJson() {
		return this.LISTA_CREACION_TRIGGERS_JSON;
	}

	public String getListaCreacionZonasAmbienteJson() {
		return this.LISTA_CREACION_ZONAS_AMBIENTE_JSON;
	}

	public String getListaCreacionLucesJson() {
		return this.LISTA_CREACION_LUCES_JSON;
	}
}