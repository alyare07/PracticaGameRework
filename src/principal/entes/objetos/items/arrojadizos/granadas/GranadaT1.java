package principal.entes.objetos.items.arrojadizos.granadas;

import java.util.ArrayList;

import org.json.simple.JSONObject;

import principal.entes.modelos.item.ListaModelosItem;
import principal.entes.objetos.Objeto;

public class GranadaT1 extends Granada {

	private static final long serialVersionUID = -6468671785650283188L;

	public GranadaT1(final int cantidad) {
		super(0, 0, cantidad, 50, 20.0, ListaModelosItem.COD_CONSUMIBLE_GRANADAT1);
		this.rellenarInfo(this.LISTA_INFO);
	}

	public GranadaT1(final int x, final int y, final int cantidad) {
		super(x, y, cantidad, 50, 20.0, ListaModelosItem.COD_CONSUMIBLE_GRANADAT1);
		this.rellenarInfo(this.LISTA_INFO);
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

	public static GranadaT1 crearDesdeJson(final JSONObject json) {
		if (json == null) {
			return new GranadaT1(1);
		}

		final int x = (json.get("x") != null) ? ((Number) json.get("x")).intValue() : 0;
		final int y = (json.get("y") != null) ? ((Number) json.get("y")).intValue() : 0;
		final int cant = (json.get("cant") != null) ? ((Number) json.get("cant")).intValue() : 1;

		return new GranadaT1(x, y, cant);
	}

	@Override
	public Objeto copiar() {
		return new GranadaT1(this.getPosicionXInt(), this.getPosicionYInt(), this.getCantidad());
	}

	@Override
	public String exportarTipoItem() {
		return "GranadaT1";
	}

	@Override
	protected void rellenarInfo(final ArrayList<String> listaInfo) {
		listaInfo.clear();
		listaInfo.add("Daño: " + (int) this.DAMAGE + " pts.");
		listaInfo.add("Radio de explosión: " + this.DIAMENTRO_DEL_AREA + " px.");
	}
}