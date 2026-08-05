package principal.mapa.escenario;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.enemigos.Enemigo;
import principal.entes.criaturas.neutrales.CosaNeutral;
import principal.entes.objetos.Complemento;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.cofres.Cofre;
import principal.entes.objetos.items.Consumible;
import principal.entes.objetos.items.Item;
import principal.entes.objetos.items.armas.distancia.fuego.Pistola;
import principal.mapa.Mapa;
import principal.mapa.Mundo;
import principal.utilidades.Constantes;

public class Escenario implements Serializable{
	private static final long serialVersionUID = 3351562178857131172L;
	protected final Mapa MAPA;
	protected final String LISTA_CREACION_CRIATURAS_JSON;
	protected final String LISTA_CREACION_ITEMS_JSON;
	protected final String LISTA_CREACION_COMPLEMENTOS_JSON;
	protected final String LISTA_CREACION_OBJETOS_JSON;
	
	public Escenario(final Mapa mapa, final String criaturasJSON, final String itemsJSON, final String complementosJSON, final String objetosJSON) {
		this.MAPA = mapa;
		this.LISTA_CREACION_CRIATURAS_JSON = criaturasJSON;
		this.LISTA_CREACION_ITEMS_JSON = itemsJSON;
		this.LISTA_CREACION_COMPLEMENTOS_JSON = complementosJSON;
		this.LISTA_CREACION_OBJETOS_JSON = objetosJSON;
	}
	
	public ArrayList<Criatura> generarListaCriaturas(final Mundo mundo){
		ArrayList<Criatura> criaturas = new ArrayList<Criatura>();
		JSONParser parse = new JSONParser();
		JSONArray lista = null;
		try {
			lista = (JSONArray) parse.parse(LISTA_CREACION_CRIATURAS_JSON);
		} catch (ParseException e) {
			lista = new JSONArray();
		}
		
		JSONObject json = null;
		Criatura c = null;
		for(Object obj : lista) {
			if(obj instanceof JSONObject) {
				json = (JSONObject) obj;
				if(json.get("tipo").toString().equals(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(CosaNeutral.class))) {
					c = CosaNeutral.crearDesdeJSON((JSONObject)json.get("entiti"), MAPA);
					if(MAPA.areaEnSectorNoSolido(c.getRectangulo())) {
						criaturas.add(c);
					}
				}else if(json.get("tipo").toString().equals(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Enemigo.class))) {
					c = Enemigo.crearDesdeJSON((JSONObject)json.get("entiti"), mundo);
					if(MAPA.areaEnSectorNoSolido(c.getRectangulo())) {
						criaturas.add(c);
					}
				}
			}
		}
		
		return criaturas;
	}
	
	public void generarListaComplementos(final Mundo mundo){
		ArrayList<Complemento> complementos = new ArrayList<Complemento>();
		JSONParser parse = new JSONParser();
		JSONArray lista = null;
		try {
			lista = (JSONArray) parse.parse(LISTA_CREACION_COMPLEMENTOS_JSON);
		} catch (ParseException e) {
			lista = new JSONArray();
		}
		
		JSONObject json = null;
		Complemento c = null;
		for(Object obj : lista) {
			if(obj instanceof JSONObject) {
				json = (JSONObject) obj;
				
				
				c = Complemento.crearDesdeJson(json);
				complementos.add(c);
//				mundo.meterEntidad(c);
			}
		}
		
		complementos.sort((c1 , c2) -> {
			if((c1.getPosicionYInt()+c1.getAlto()) < (c2.getPosicionYInt() + c2.getAlto())) {
				return -1; //c1 menor
			}else if((c1.getPosicionYInt()+c1.getAlto()) > (c2.getPosicionYInt() + c2.getAlto())) {
				return 1; //c1 mayor
			}else return 0; // iguales
		});
		
		for(Complemento complemento : complementos) {
			mundo.meterEntidad(complemento);
		}
	}
	
	public ArrayList<Item> generarItemsEnMapa(){
		ArrayList<Item> items = new ArrayList<Item>();
		JSONParser parse = new JSONParser();
		JSONArray lista = null;
		try {
			lista = (JSONArray) parse.parse(LISTA_CREACION_ITEMS_JSON);
		} catch (ParseException e) {
			lista = new JSONArray();
		}
		JSONObject json = null;
		Item i = null;
		for(Object obj : lista) {
			if(obj instanceof JSONObject) {
				json = (JSONObject) obj;
				i = Item.crearItemDesdeJson(json);
				if(i == null) continue;
				if(MAPA.AreaDentroDelMapa(i.getArea())) {
					items.add(i);
				}
				i = null;
			}
		}
		return items;
	}
	
	public int generarObjetosEnMapa(final Mundo mundo){
		int cant = 0;
		JSONParser parse = new JSONParser();
		JSONArray lista = null;
		try {
			lista = (JSONArray) parse.parse(LISTA_CREACION_OBJETOS_JSON);
		} catch (ParseException e) {
			lista = new JSONArray();
		}
		JSONObject json = null;
		Objeto obj = null;
		for(Object object : lista) {
			if(object instanceof JSONObject) {
				json = (JSONObject) object;
				if(json.get("tipoObjeto").toString().equals(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Cofre.class))) {
					obj = Cofre.crearDesdeJSON((JSONObject)json.get("entiti"));
				}
				
				if(obj != null) {
					mundo.meterEntidad(obj);
					cant++;
					 obj = null;
				}
			}
		}
		return cant;
	}
	
	
	public Mapa getMapa() {
		return this.MAPA;
	}
	
	
	
	
	
	
	

}
