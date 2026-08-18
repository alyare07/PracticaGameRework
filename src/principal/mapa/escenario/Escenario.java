package principal.mapa.escenario;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import principal.entes.criaturas.Criatura;
import principal.entes.objetos.Complemento;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.cofres.Cofre;
import principal.entes.objetos.items.Item;
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
		this.LISTA_CREACION_CRIATURAS_JSON = criaturasJSON;
		this.LISTA_CREACION_ITEMS_JSON = itemsJSON;
		this.LISTA_CREACION_COMPLEMENTOS_JSON = complementosJSON;
		this.LISTA_CREACION_OBJETOS_JSON = objetosJSON;
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

		JSONObject json = null;
		final Criatura c = null;
		for (final Object obj : lista) {
			if (obj instanceof JSONObject) {
				json = (JSONObject) obj;
//		if (json.get("tipo").toString().equals(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(CosaNeutral.class))) {
//		    c = CosaNeutral.crearDesdeJSON((JSONObject) json.get("entiti"), mundo);
//		    if (this.TERRENO.areaEnSectorNoSolido(c.getRectangulo())) {
//			criaturas.add(c);
//		    }
//		}
//		else if (json.get("tipo").toString().equals(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Enemigo.class))) {
//		    c = Enemigo.crearDesdeJSON((JSONObject) json.get("entiti"), mundo);
//		    if (this.TERRENO.areaEnSectorNoSolido(c.getRectangulo())) {
//			criaturas.add(c);
//		    }
//		}
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

		JSONObject json = null;
		Complemento c = null;
		for (final Object obj : lista) {
			if (obj instanceof JSONObject) {
				json = (JSONObject) obj;

				c = Complemento.crearDesdeJson(json);
				complementos.add(c);
//				mundo.meterEntidad(c);
			}
		}

		complementos.sort((c1, c2) -> {
			if ((c1.getPosicionYInt() + c1.getAlto()) < (c2.getPosicionYInt() + c2.getAlto())) {
				return -1; // c1 menor
			}
			if ((c1.getPosicionYInt() + c1.getAlto()) > (c2.getPosicionYInt() + c2.getAlto())) {
				return 1; // c1 mayor
			}
			return 0; // iguales
		});

		for (final Complemento complemento : complementos) {
			mundo.meterEntidad(complemento);
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
		JSONObject json = null;
		Item i = null;
		for (final Object obj : lista) {
			if (obj instanceof JSONObject) {
				json = (JSONObject) obj;
				i = Item.crearItemDesdeJson(json);
				if (i == null) {
					continue;
				}
				if (this.TERRENO.AreaDentroDelTerreno(i.getArea())) {
					items.add(i);
				}
				i = null;
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
		JSONObject json = null;
		Objeto obj = null;
		for (final Object object : lista) {
			if (object instanceof JSONObject) {
				json = (JSONObject) object;
				if (json.get("tipoObjeto").toString()
						.equals(Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Cofre.class))) {
					obj = Cofre.crearDesdeJSON((JSONObject) json.get("entiti"));
				}

				if (obj != null) {
					mundo.meterEntidad(obj);
					cant++;
					obj = null;
				}
			}
		}
		return cant;
	}

	public Terreno getTerreno() {
		return this.TERRENO;
	}

}
