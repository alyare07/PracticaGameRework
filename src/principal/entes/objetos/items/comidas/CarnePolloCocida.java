package principal.entes.objetos.items.comidas;

import java.util.ArrayList;

import org.json.simple.JSONObject;

import principal.entes.efectos.TipoEfectoEstado;
import principal.entes.objetos.Objeto;
import principal.recursos.TexturaItem;

/**
 * Alimento cocinado de alto valor nutricional asado en fogata (Zero-GC / O(1)).
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class CarnePolloCocida extends Comida {

	private static final long serialVersionUID = 1L;

	public static final String CODIGO = "Pata de Pollo Asada";
	public static final int LIMITE_PILA = 20;
	public static final double VIDA_UTIL_HORAS = 72.0; // 3 días canónicos conservada

	// === VALORES NUTRICIONALES ASADA ===
	public static final double NUTRICION_HAMBRE = 48.0;
	public static final double HIDRATACION_SED = 5.0;
	public static final double CURACION_SALUD = 10.0;

	// === VALORES EN DESCOMPOSICIÓN ===
	public static final double HAMBRE_PODRIDA = 8.0;
	public static final double SED_PODRIDA = -12.0;

	public CarnePolloCocida(final int x, final int y, final int cantidad) {
		super(x, y, cantidad, CODIGO, CODIGO, TexturaItem.COMIDA_PATA_POLLO_COCIDA_INV,
				TexturaItem.COMIDA_PATA_POLLO_COCIDA_MAPA, LIMITE_PILA, NUTRICION_HAMBRE, HIDRATACION_SED,
				CURACION_SALUD, HAMBRE_PODRIDA, SED_PODRIDA, TipoEfectoEstado.VENENO, 4.0, 0.5);

		this.configurarPerecedero(VIDA_UTIL_HORAS);
		this.setPrecioBasePlata(8L); // Duplica el valor de la carne cruda
	}

	public CarnePolloCocida(final int cantidad) {
		this(0, 0, cantidad);
	}

	public CarnePolloCocida(final int x, final int y, final int cantidad, final double horaCaducidad,
			final boolean podrido) {
		super(x, y, cantidad, CODIGO, CODIGO, TexturaItem.COMIDA_PATA_POLLO_COCIDA_INV,
				TexturaItem.COMIDA_PATA_POLLO_COCIDA_MAPA, LIMITE_PILA, NUTRICION_HAMBRE, HIDRATACION_SED,
				CURACION_SALUD, HAMBRE_PODRIDA, SED_PODRIDA, TipoEfectoEstado.VENENO, 4.0, 0.5);

		this.establecerCaducidadDirecta(horaCaducidad, podrido);
		this.setPrecioBasePlata(8L);
	}

	@Override
	protected void rellenarInfo(final ArrayList<String> listaInfo) {
		super.rellenarInfo(listaInfo);
		listaInfo.add("Carne dorada al fuego, tierna y nutritiva.");
	}

	@Override
	public Objeto copiar() {
		return new CarnePolloCocida(this.getPosicionXInt(), this.getPosicionYInt(), this.getCantidad(),
				this.horaCaducidad, this.podrido);
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

	public static CarnePolloCocida crearDesdeJson(final JSONObject json) {
		if (json == null) {
			return new CarnePolloCocida(1);
		}

		final int cant = (json.get("cantidad") != null) ? ((Number) json.get("cantidad")).intValue() : 1;
		final double horaCad = (json.get("horaCaducidad") != null)
				? ((Number) json.get("horaCaducidad")).doubleValue()
				: -1.0;
		final boolean esPodrido = (json.get("podrido") != null)
				? Boolean.parseBoolean(json.get("podrido").toString())
				: false;

		return new CarnePolloCocida(0, 0, cant, horaCad, esPodrido);
	}
}