package principal.entes.objetos.items.materiales;

import java.util.ArrayList;

import org.json.simple.JSONObject;

import principal.entes.criaturas.Criatura;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.Consumible;
import principal.recursos.TexturaItem;

public class RecursoMaterial extends Consumible {

	private static final long serialVersionUID = 1L;

	public static final String COD_MADERA = "Madera";
	public static final String COD_PIEDRA = "Piedra";

	public RecursoMaterial(final int x, final int y, final int cantidad, final String codModelo) {
		super(x, y, cantidad, codModelo, codModelo, resolverTexturaInv(codModelo), resolverTexturaMapa(codModelo), 999);
		this.rellenarInfo(this.LISTA_INFO);
	}

	public RecursoMaterial(final int cantidad, final String codModelo) {
		this(0, 0, cantidad, codModelo);
	}

	public static RecursoMaterial crearMadera(final int x, final int y, final int cantidad) {
		return new RecursoMaterial(x, y, cantidad, COD_MADERA);
	}

	public static RecursoMaterial crearPiedra(final int x, final int y, final int cantidad) {
		return new RecursoMaterial(x, y, cantidad, COD_PIEDRA);
	}

	@Override
	public void consumir(final Criatura c) {
		// Los materiales no se consumen directamente; se usan para crafteo y
		// construcción
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
		json.put("x", Integer.valueOf(this.getPosicionXInt()));
		json.put("y", Integer.valueOf(this.getPosicionYInt()));
		json.put("codModelo", this.getCodigoModelo());
		json.put("cant", Integer.valueOf(this.getCantidad()));
		return json;
	}

	public static RecursoMaterial crearDesdeJson(final JSONObject json) {
		if (json == null) {
			return null;
		}
		final int x = (json.get("x") != null) ? ((Number) json.get("x")).intValue() : 0;
		final int y = (json.get("y") != null) ? ((Number) json.get("y")).intValue() : 0;
		final String codModelo = (json.get("codModelo") != null) ? json.get("codModelo").toString() : COD_MADERA;
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

	private static TexturaItem resolverTexturaInv(final String cod) {
		switch (cod) {
		case RecursoMaterial.COD_MADERA:
			return TexturaItem.MADERA_INV;
		case RecursoMaterial.COD_PIEDRA:
			return TexturaItem.PIEDRA_INV;
		default:
			return TexturaItem.PARTICULA_SANGRE_8;
		}
	}

	private static TexturaItem resolverTexturaMapa(final String cod) {
		switch (cod) {
		case RecursoMaterial.COD_MADERA:
			return TexturaItem.MADERA_MAPA;
		case RecursoMaterial.COD_PIEDRA:
			return TexturaItem.PIEDRA_MAPA;
		default:
			return TexturaItem.PARTICULA_SANGRE_8;
		}
	}
}