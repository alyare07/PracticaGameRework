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
	public static final String COD_COBRE = "Mena de Cobre";
	public static final String COD_HIERRO = "Mena de Hierro";
	public static final String COD_ORO = "Mena de Oro";
	public static final String COD_CARBON = "Carbón";
	public static final String COD_CRISTAL = "Cristal Arcano";

	public RecursoMaterial(final int x, final int y, final int cantidad, final String codModelo) {
		super(x, y, cantidad, codModelo, codModelo, resolverTexturaInv(codModelo), resolverTexturaMapa(codModelo), 999);
		this.asignarPrecioBase();
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

	private void asignarPrecioBase() {
		switch (this.codigoModelo) {
		case COD_MADERA:
			this.precioBasePlata = 2L;
			break;
		case COD_PIEDRA:
			this.precioBasePlata = 3L;
			break;
		case COD_CARBON:
			this.precioBasePlata = 5L;
			break;
		case COD_COBRE:
			this.precioBasePlata = 8L;
			break;
		case COD_HIERRO:
			this.precioBasePlata = 15L;
			break;
		case COD_ORO:
			this.precioBasePlata = 40L;
			break;
		case COD_CRISTAL:
			this.precioBasePlata = 75L;
			break;
		default:
			this.precioBasePlata = 3L;
			break;
		}
	}

	@Override
	public void consumir(final Criatura c) {
		// Material no consumible de forma directa
	}

	@Override
	public Objeto copiar() {
		final RecursoMaterial rm = new RecursoMaterial(this.getPosicionXInt(), this.getPosicionYInt(),
				this.getCantidad(), this.getCodigoModelo());
		rm.setPrecioBasePlata(this.precioBasePlata);
		return rm;
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

	public static RecursoMaterial crearDesdeJson(final JSONObject json) {
		if (json == null) {
			return null;
		}
		final int x = (json.get("x") != null) ? ((Number) json.get("x")).intValue() : 0;
		final int y = (json.get("y") != null) ? ((Number) json.get("y")).intValue() : 0;
		final String codModelo = (json.get("codModelo") != null) ? json.get("codModelo").toString() : COD_PIEDRA;
		final int cant = (json.get("cant") != null) ? ((Number) json.get("cant")).intValue() : 1;

		final RecursoMaterial rm = new RecursoMaterial(x, y, cant, codModelo);
		if (json.get("precio") != null) {
			rm.setPrecioBasePlata(((Number) json.get("precio")).longValue());
		}
		return rm;
	}

	@Override
	protected void rellenarInfo(final ArrayList<String> listaInfo) {
		listaInfo.clear();
		listaInfo.add("Materia prima recolectada.");
		listaInfo.add("Usado en forja, cocina, construcción o venta.");
	}

	@Override
	public String exportarTipoItem() {
		return "RecursoMaterial";
	}

	private static TexturaItem resolverTexturaInv(final String cod) {
		if (COD_MADERA.equals(cod)) {
			return TexturaItem.MADERA_INV;
		}
		if (COD_PIEDRA.equals(cod)) {
			return TexturaItem.PIEDRA_INV;
		}
		return TexturaItem.PIEDRA_INV;
	}

	private static TexturaItem resolverTexturaMapa(final String cod) {
		if (COD_MADERA.equals(cod)) {
			return TexturaItem.MADERA_MAPA;
		}
		if (COD_PIEDRA.equals(cod)) {
			return TexturaItem.PIEDRA_MAPA;
		}
		return TexturaItem.PIEDRA_MAPA;
	}
}