package principal.entes.objetos.cofres;

import java.awt.image.BufferedImage;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.Item;
import principal.utilidades.Constantes;

public class CofreMediano extends Cofre {

	private static final long serialVersionUID = -7676048119724996866L;
	private final int ANCHO = 16;
	private final int ALTO = 16;

	public CofreMediano(int x, int y) {
		super(x, y, 6, 4, "Cofre Mediano");
	}
	
	@Override
	public BufferedImage getTextura() {
		if(this.getEstado() == EstadoCofre.CERRADO) {
			return Constantes.LISTA_HOJAS_SPRITES.COFRES.getCofreCerrado();
		}else {
			return Constantes.LISTA_HOJAS_SPRITES.COFRES.getCofreAbierto();
		}
	}
	
	@Override
	public int getAncho() {
		return this.ANCHO;
	}

	@Override
	public int getAlto() {
		return this.ALTO;
	}

	@Override
	public Objeto copiar() {
		return new CofreMediano(x, y);// COPIAR TAMBIEN EL INVENTARIO
	}

	@Override
	public boolean esSolido() {
		return true;
	}

	@Override
	protected String getTipoCofre() {
		return Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(CofreMediano.class);
	}
	
	public static CofreMediano crearDesdeJson(final JSONObject json) {
		final int x = Integer.parseInt(json.get("x").toString());
		final int y = Integer.parseInt(json.get("y").toString());
		JSONParser parse = new JSONParser();
		JSONArray listaItemsJson = null;
		try {
			listaItemsJson = (JSONArray) parse.parse(json.get(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Item.class)).toString());
		} catch (ParseException e) {
			listaItemsJson = new JSONArray();
		}
		CofreMediano cofre = new CofreMediano(x, y);
		Item i = null;
		for(Object obj : listaItemsJson) {
			if(obj instanceof JSONObject) {
				i = Item.crearItemDesdeJson((JSONObject)obj);
				if(i == null) continue;
				cofre.meterItem(i);
			}
		}
		return cofre;
	}


}
