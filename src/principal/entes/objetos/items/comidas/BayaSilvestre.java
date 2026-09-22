package principal.entes.objetos.items.comidas;

import java.util.ArrayList;

import org.json.simple.JSONObject;

import principal.entes.objetos.Objeto;
import principal.recursos.TexturaItem;

/**
 * Fruto silvestre perecedero recolectado de arbustos. Hereda del motor
 * metabólico universal de Comida (Zero-GC / O(1)).
 * 
 * @version 2.0 (Vanilla Java 8)
 */
public class BayaSilvestre extends Comida {

	private static final long serialVersionUID = 1L;

	public static final String CODIGO = "Baya Silvestre";
	public static final int LIMITE_PILA = 30;
	public static final double VIDA_UTIL_HORAS = 48.0;

	// === ATRIBUTOS METABÓLICOS CENTRALIZADOS ===
	public static final double NUTRICION_HAMBRE = 12.0;
	public static final double HIDRATACION_SED = 6.0;
	public static final double CURACION_SALUD = 2.0;

	/** Constructor para generación en suelo o recolección fresca */
	public BayaSilvestre(final int x, final int y, final int cantidad) {
		super(x, y, cantidad, CODIGO, CODIGO, TexturaItem.COMIDA_BAYAS_INV, TexturaItem.COMIDA_BAYAS_MAPA, LIMITE_PILA,
				NUTRICION_HAMBRE, HIDRATACION_SED, CURACION_SALUD);

		this.configurarPerecedero(VIDA_UTIL_HORAS);
		this.setPrecioBasePlata(2L);
	}

	public BayaSilvestre(final int cantidad) {
		this(0, 0, cantidad);
	}

	/** Constructor interno para deserialización JSON sin reiniciar caducidad */
	public BayaSilvestre(final int x, final int y, final int cantidad, final double horaCaducidad,
			final boolean podrido) {
		super(x, y, cantidad, CODIGO, CODIGO, TexturaItem.COMIDA_BAYAS_INV, TexturaItem.COMIDA_BAYAS_MAPA, LIMITE_PILA,
				NUTRICION_HAMBRE, HIDRATACION_SED, CURACION_SALUD);

		this.establecerCaducidadDirecta(horaCaducidad, podrido);
		this.setPrecioBasePlata(2L);
	}

	@Override
	protected void rellenarInfo(final ArrayList<String> listaInfo) {
		super.rellenarInfo(listaInfo); // Agrega automáticamente: Sustento +12 H | +6 S | +2 HP
		listaInfo.add("Fruto silvestre dulce y jugoso.");
	}

	@Override
	public Objeto copiar() {
		return new BayaSilvestre(this.getPosicionXInt(), this.getPosicionYInt(), this.getCantidad(), this.horaCaducidad,
				this.podrido);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JSONObject exportarParaJSON() {
		final JSONObject json = new JSONObject();
		json.put("codModelo", this.codigoModelo);
		json.put("cantidad", Integer.valueOf(this.getCantidad()));
		json.put("perecedero", Boolean.valueOf(this.perecedero));
		json.put("horaCaducidad", Double.valueOf(this.horaCaducidad));
		json.put("podrido", Boolean.valueOf(this.podrido));
		return json;
	}

	public static BayaSilvestre crearDesdeJson(final JSONObject json) {
		if (json == null) {
			return new BayaSilvestre(1);
		}

		final int cant = (json.get("cantidad") != null) ? ((Number) json.get("cantidad")).intValue() : 1;
		final double horaCad = (json.get("horaCaducidad") != null) ? ((Number) json.get("horaCaducidad")).doubleValue()
				: -1.0;
		final boolean esPodrido = (json.get("podrido") != null) ? Boolean.parseBoolean(json.get("podrido").toString())
				: false;

		return new BayaSilvestre(0, 0, cant, horaCad, esPodrido);
	}
}