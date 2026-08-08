package principal.entes.objetos.cofres;

import java.awt.image.BufferedImage;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.Item;
import principal.utilidades.Constantes;

public class CofrePequeño extends Cofre {

	private static final long serialVersionUID = 4592661837024054777L;
	private final int ANCHO = 16;
	private final int ALTO = 16;
	
	
	public CofrePequeño(int x, int y) {
		super(x, y, 3, 3, "Cofre Pequeño");
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
		return new CofrePequeño(x, y); // COPIAR TAMBIEN EL INVENTARIO
	}
	
	@Override
	public boolean esSolido() {
		return true;
	}

	@Override
	protected String getTipoCofre() {
		return Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(CofrePequeño.class);
	}
	
	public static CofrePequeño crearDesdeJson(final JSONObject json) {
		final int x = Integer.parseInt(json.get("x").toString());
		final int y = Integer.parseInt(json.get("y").toString());
		JSONParser parse = new JSONParser();
		JSONArray listaItemsJson = null;
		try {
			listaItemsJson = (JSONArray) parse.parse(json.get(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Item.class)).toString());
		} catch (ParseException e) {
			listaItemsJson = new JSONArray();
		}
		CofrePequeño cofre = new CofrePequeño(x, y);
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
