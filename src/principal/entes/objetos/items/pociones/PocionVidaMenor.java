package principal.entes.objetos.items.pociones;

import java.util.ArrayList;

import org.json.simple.JSONObject;

import principal.entes.modelos.item.ListaModelosItem;
import principal.entes.objetos.Objeto;

public class PocionVidaMenor extends PocionVida {

	private static final long serialVersionUID = 604669784500287669L;
	protected static final double puntosRest = 20;
	
	protected static final String codModelo = ListaModelosItem.COD_CONSUMIBLE_POCION_VIDA_MENOR;
	
	public PocionVidaMenor(int cantidad) {
		super(cantidad, codModelo, puntosRest);
		this.rellenarInfo(LISTA_INFO);
	}
	
	public PocionVidaMenor(int x, int y, int cantidad) {
		super(x, y, cantidad, codModelo, puntosRest);
		this.rellenarInfo(LISTA_INFO);
	}

	@Override
	public Objeto copiar() {
		return new PocionVidaMenor(this.x, this.y, this.getCantidad());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected JSONObject exportarParaJSON() {
		JSONObject json = new JSONObject();
		json.put("x", x);
		json.put("y", y);
		json.put("codModelo", this.getCodigoModelo());
		json.put("cant", this.getCantidad());
		return json;
	}

	
	public static PocionVidaMenor crearDesdeJson(final JSONObject json) {
		int x = Integer.parseInt(json.get("x").toString());
		int y = Integer.parseInt(json.get("y").toString());
		int cant = Integer.parseInt(json.get("cant").toString());
		return new PocionVidaMenor(x, y, cant);
	}

	@Override
	protected void rellenarInfo(ArrayList<String> listaInfo) {
		listaInfo.add("Regenera "+puntosRest+"pts de vida.");
	}

	
}
