package principal.entes.objetos.items.pociones;

import java.util.ArrayList;

import org.json.simple.JSONObject;

import principal.entes.modelos.item.ListaModelosItem;
import principal.entes.objetos.Objeto;

public class PocionVidaMenor extends PocionVida {

	private static final long serialVersionUID = 604669784500287669L;
	protected static final double puntosRest = 20;

	protected static final String codModelo = ListaModelosItem.COD_CONSUMIBLE_POCION_VIDA_MENOR;

	public PocionVidaMenor(final int cantidad) {
		super(cantidad, codModelo, puntosRest);
		this.rellenarInfo(this.LISTA_INFO);
	}

	public PocionVidaMenor(final int x, final int y, final int cantidad) {
		super(x, y, cantidad, codModelo, puntosRest);
		this.rellenarInfo(this.LISTA_INFO);
	}

	@Override
	public Objeto copiar() {
		return new PocionVidaMenor(this.getPosicionXInt(), this.getPosicionYInt(), this.getCantidad());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JSONObject exportarParaJSON() {
		final JSONObject json = new JSONObject();
		json.put("x", this.getPosicionXInt());
		json.put("y", this.getPosicionYInt());
		json.put("codModelo", this.getCodigoModelo());
		json.put("cant", this.getCantidad());
		return json;
	}

	public static PocionVidaMenor crearDesdeJson(final JSONObject json) {
		final int x = Integer.parseInt(json.get("x").toString());
		final int y = Integer.parseInt(json.get("y").toString());
		final int cant = Integer.parseInt(json.get("cant").toString());
		return new PocionVidaMenor(x, y, cant);
	}

	@Override
	protected void rellenarInfo(final ArrayList<String> listaInfo) {
		listaInfo.add("Regenera " + puntosRest + "pts de vida.");
	}

}
