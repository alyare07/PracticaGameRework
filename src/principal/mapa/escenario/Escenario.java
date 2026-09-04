package principal.mapa.escenario;

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
import principal.mapa.Mundo;
import principal.mapa.Terreno;
import principal.utilidades.Globales;

public class Escenario implements Serializable {

	private static final long serialVersionUID = 3351562178857131172L;
	protected final Terreno TERRENO;
	protected final String LISTA_CREACION_CRIATURAS_JSON;
	protected final String LISTA_CREACION_ITEMS_JSON;
	protected final String LISTA_CREACION_COMPLEMENTOS_JSON;
	protected final String LISTA_CREACION_OBJETOS_JSON;

	public Escenario(final Terreno mapa, final String criaturasJSON, final String itemsJSON,
			final String complementosJSON, final String objetosJSON) {
		this.TERRENO = mapa;
		this.LISTA_CREACION_CRIATURAS_JSON = (criaturasJSON != null) ? criaturasJSON : "[]";
		this.LISTA_CREACION_ITEMS_JSON = (itemsJSON != null) ? itemsJSON : "[]";
		this.LISTA_CREACION_COMPLEMENTOS_JSON = (complementosJSON != null) ? complementosJSON : "[]";
		this.LISTA_CREACION_OBJETOS_JSON = (objetosJSON != null) ? objetosJSON : "[]";
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
				final Complemento c = Complemento.crearDesdeJson((JSONObject) obj);
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

				// Deserialización Polimórfica de Objetos
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

	public Terreno getTerreno() {
		return this.TERRENO;
	}
}