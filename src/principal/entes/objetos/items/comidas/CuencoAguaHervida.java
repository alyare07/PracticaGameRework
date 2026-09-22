package principal.entes.objetos.items.comidas;

import java.util.ArrayList;

import org.json.simple.JSONObject;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.jugador.Jugador;
import principal.entes.efectos.TipoEfectoEstado;
import principal.entes.objetos.Objeto;
import principal.recursos.TexturaItem;
import principal.utilidades.Globales;

/**
 * Agua purificada al fuego en fogata. Segura, potable y revitalizante (Zero-GC / O(1)).
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class CuencoAguaHervida extends Comida {

	private static final long serialVersionUID = 1L;

	public static final String CODIGO = "Agua Purificada";
	public static final int LIMITE_PILA = 10;
	public static final double VIDA_UTIL_HORAS = 120.0; // 5 días de conservación

	public CuencoAguaHervida(final int x, final int y, final int cantidad) {
		// Aporte: 0 Hambre, +50 Sed, +4 HP salud. 0% veneno
		super(x, y, cantidad, CODIGO, CODIGO, TexturaItem.CUENCO_AGUA_HERVIDA_INV,
				TexturaItem.CUENCO_AGUA_HERVIDA_MAPA, LIMITE_PILA, 0.0, 50.0, 4.0, 0.0, -10.0,
				TipoEfectoEstado.VENENO, 3.0, 0.4);

		this.configurarPerecedero(VIDA_UTIL_HORAS);
		this.setPrecioBasePlata(3L);
	}

	public CuencoAguaHervida(final int cantidad) {
		this(0, 0, cantidad);
	}

	public CuencoAguaHervida(final int x, final int y, final int cantidad, final double horaCaducidad,
			final boolean podrido) {
		super(x, y, cantidad, CODIGO, CODIGO, TexturaItem.CUENCO_AGUA_HERVIDA_INV,
				TexturaItem.CUENCO_AGUA_HERVIDA_MAPA, LIMITE_PILA, 0.0, 50.0, 4.0, 0.0, -10.0,
				TipoEfectoEstado.VENENO, 3.0, 0.4);

		this.establecerCaducidadDirecta(horaCaducidad, podrido);
		this.setPrecioBasePlata(3L);
	}

	@Override
	public void consumir(final Criatura c) {
		super.consumir(c);

		// Devuelve el cuenco vacío al beber
		if (c instanceof Jugador) {
			Globales.GESTOR_INVENTARIO.getInventarioJugador().agregarObjeto(new CuencoVacio(1));
		}
	}

	@Override
	protected void rellenarInfo(final ArrayList<String> listaInfo) {
		super.rellenarInfo(listaInfo);
		listaInfo.add("Agua limpia esterilizada al calor del fuego.");
	}

	@Override
	public Objeto copiar() {
		return new CuencoAguaHervida(this.getPosicionXInt(), this.getPosicionYInt(), this.getCantidad(),
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

	public static CuencoAguaHervida crearDesdeJson(final JSONObject json) {
		if (json == null) {
			return new CuencoAguaHervida(1);
		}
		final int cant = (json.get("cantidad") != null) ? ((Number) json.get("cantidad")).intValue() : 1;
		final double horaCad = (json.get("horaCaducidad") != null)
				? ((Number) json.get("horaCaducidad")).doubleValue()
				: -1.0;
		final boolean esPodrido = (json.get("podrido") != null)
				? Boolean.parseBoolean(json.get("podrido").toString())
				: false;

		return new CuencoAguaHervida(0, 0, cant, horaCad, esPodrido);
	}
}