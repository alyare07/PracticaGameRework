package principal.entes.objetos.items.pociones;

import java.util.ArrayList;

import org.json.simple.JSONObject;

import principal.entes.objetos.Objeto;
import principal.recursos.TexturaItem;

public class PocionVidaMenor extends PocionVida {

	private static final long serialVersionUID = 604669784500287669L;
	protected static final double PUNTOS_REST = 20.0;
	public static final String COD_MODELO = "Pocion Vida Menor";

	public PocionVidaMenor(final int cantidad) {
		super(cantidad, COD_MODELO, "Poción de Vida Menor", TexturaItem.POCION_ROJA_INV, TexturaItem.POCION_ROJA_MAPA,
				99, PUNTOS_REST);
		this.precioBasePlata = 15L; // 15 Plata
		this.rellenarInfo(this.LISTA_INFO);
	}

	public PocionVidaMenor(final int x, final int y, final int cantidad) {
		super(x, y, cantidad, COD_MODELO, "Poción de Vida Menor", TexturaItem.POCION_ROJA_INV,
				TexturaItem.POCION_ROJA_MAPA, 99, PUNTOS_REST);
		this.precioBasePlata = 15L;
		this.rellenarInfo(this.LISTA_INFO);
	}

	@Override
	public Objeto copiar() {
		final PocionVidaMenor p = new PocionVidaMenor(this.getPosicionXInt(), this.getPosicionYInt(),
				this.getCantidad());
		p.setPrecioBasePlata(this.precioBasePlata);
		return p;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JSONObject exportarParaJSON() {
		final JSONObject json = new JSONObject();
		json.put("x", Integer.valueOf(this.getPosicionXInt()));
		json.put("y", Integer.valueOf(this.getPosicionYInt()));
		json.put("codModelo", this.getCodigoModelo());
		json.put("cant", Integer.valueOf(this.getCantidad()));
		json.put("precio", Long.valueOf(this.precioBasePlata));
		return json;
	}

	public static PocionVidaMenor crearDesdeJson(final JSONObject json) {
		final int x = Integer.parseInt(json.get("x").toString());
		final int y = Integer.parseInt(json.get("y").toString());
		final int cant = Integer.parseInt(json.get("cant").toString());
		final PocionVidaMenor p = new PocionVidaMenor(x, y, cant);
		if (json.get("precio") != null) {
			p.setPrecioBasePlata(((Number) json.get("precio")).longValue());
		}
		return p;
	}

	@Override
	protected void rellenarInfo(final ArrayList<String> listaInfo) {
		listaInfo.clear();
		listaInfo.add("Regenera " + (int) PUNTOS_REST + " pts de vida.");
	}
}