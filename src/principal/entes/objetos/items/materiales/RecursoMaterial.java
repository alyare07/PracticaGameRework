package principal.entes.objetos.items.materiales;

import java.util.ArrayList;

import org.json.simple.JSONObject;

import principal.entes.criaturas.Criatura;
import principal.entes.modelos.item.ListaModelosItem;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.Consumible;

public class RecursoMaterial extends Consumible {

	private static final long serialVersionUID = 1L;

	public RecursoMaterial(final int x, final int y, final int cantidad, final String codModelo) {
		super(x, y, cantidad, codModelo);
		this.rellenarInfo(this.LISTA_INFO);
	}

	public RecursoMaterial(final int cantidad, final String codModelo) {
		super(cantidad, codModelo);
		this.rellenarInfo(this.LISTA_INFO);
	}

	public static RecursoMaterial crearMadera(final int x, final int y, final int cantidad) {
		return new RecursoMaterial(x, y, cantidad, ListaModelosItem.COD_RECURSO_MADERA);
	}

	public static RecursoMaterial crearPiedra(final int x, final int y, final int cantidad) {
		return new RecursoMaterial(x, y, cantidad, ListaModelosItem.COD_RECURSO_PIEDRA);
	}

	@Override
	public void consumir(final Criatura c) {
		// Los materiales no se consumen directamente; se usan para crafteo y
		// construccion
	}

	@Override
	public Objeto copiar() {
		return new RecursoMaterial(this.getPosicionXInt(), this.getPosicionYInt(), this.getCantidad(),
				this.getCodigoModelo());
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

	public static RecursoMaterial crearDesdeJson(final JSONObject json) {
		if (json == null) {
			return null;
		}
		final int x = (json.get("x") != null) ? ((Number) json.get("x")).intValue() : 0;
		final int y = (json.get("y") != null) ? ((Number) json.get("y")).intValue() : 0;
		final String codModelo = (json.get("codModelo") != null) ? json.get("codModelo").toString()
				: ListaModelosItem.COD_RECURSO_MADERA;
		final int cant = (json.get("cant") != null) ? ((Number) json.get("cant")).intValue() : 1;

		return new RecursoMaterial(x, y, cant, codModelo);
	}

	@Override
	protected void rellenarInfo(final ArrayList<String> listaInfo) {
		listaInfo.clear();
		listaInfo.add("Material básico recolectado.");
		listaInfo.add("Utilizado para crafteo y construcción.");
	}

	@Override
	public String exportarTipoItem() {
		return "RecursoMaterial";
	}
}