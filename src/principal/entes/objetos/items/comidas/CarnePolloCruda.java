package principal.entes.objetos.items.comidas;

import java.util.ArrayList;

import org.json.simple.JSONObject;

import principal.entes.efectos.TipoEfectoEstado;
import principal.entes.objetos.Objeto;
import principal.recursos.TexturaItem;

/**
 * Alimento cárnico perecedero obtenido de aves de caza. Puede consumirse crudo
 * en emergencias o asarse en fogata para mayor valor nutricional (Zero-GC / O(1)).
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class CarnePolloCruda extends Comida {

	private static final long serialVersionUID = 1L;

	public static final String CODIGO = "Pata de Pollo Cruda";
	public static final int LIMITE_PILA = 20;
	public static final double VIDA_UTIL_HORAS = 24.0; // Se descompone en 1 día solar

	// === VALORES NUTRICIONALES CRUDA ===
	public static final double NUTRICION_HAMBRE = 22.0;
	public static final double HIDRATACION_SED = -6.0; // Carne cruda deshidrata
	public static final double CURACION_SALUD = 0.0;

	// === VALORES EN DESCOMPOSICIÓN ===
	public static final double HAMBRE_PODRIDA = 4.0;
	public static final double SED_PODRIDA = -15.0;

	public CarnePolloCruda(final int x, final int y, final int cantidad) {
		super(x, y, cantidad, CODIGO, CODIGO, TexturaItem.COMIDA_PATA_POLLO_CRUDA_INV,
				TexturaItem.COMIDA_PATA_POLLO_CRUDA_MAPA, LIMITE_PILA, NUTRICION_HAMBRE, HIDRATACION_SED,
				CURACION_SALUD, HAMBRE_PODRIDA, SED_PODRIDA, TipoEfectoEstado.VENENO, 5.0, 0.75);

		this.configurarPerecedero(VIDA_UTIL_HORAS);
		this.setPrecioBasePlata(4L);
	}

	public CarnePolloCruda(final int cantidad) {
		this(0, 0, cantidad);
	}

	public CarnePolloCruda(final int x, final int y, final int cantidad, final double horaCaducidad,
			final boolean podrido) {
		super(x, y, cantidad, CODIGO, CODIGO, TexturaItem.COMIDA_PATA_POLLO_CRUDA_INV,
				TexturaItem.COMIDA_PATA_POLLO_CRUDA_MAPA, LIMITE_PILA, NUTRICION_HAMBRE, HIDRATACION_SED,
				CURACION_SALUD, HAMBRE_PODRIDA, SED_PODRIDA, TipoEfectoEstado.VENENO, 5.0, 0.75);

		this.establecerCaducidadDirecta(horaCaducidad, podrido);
		this.setPrecioBasePlata(4L);
	}

	@Override
	protected void rellenarInfo(final ArrayList<String> listaInfo) {
		super.rellenarInfo(listaInfo);
		listaInfo.add("Carne cruda de ave. Conviene asarla al fuego.");
	}

	@Override
	public Objeto copiar() {
		return new CarnePolloCruda(this.getPosicionXInt(), this.getPosicionYInt(), this.getCantidad(),
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

	public static CarnePolloCruda crearDesdeJson(final JSONObject json) {
		if (json == null) {
			return new CarnePolloCruda(1);
		}

		final int cant = (json.get("cantidad") != null) ? ((Number) json.get("cantidad")).intValue() : 1;
		final double horaCad = (json.get("horaCaducidad") != null)
				? ((Number) json.get("horaCaducidad")).doubleValue()
				: -1.0;
		final boolean esPodrido = (json.get("podrido") != null)
				? Boolean.parseBoolean(json.get("podrido").toString())
				: false;

		return new CarnePolloCruda(0, 0, cant, horaCad, esPodrido);
	}
}