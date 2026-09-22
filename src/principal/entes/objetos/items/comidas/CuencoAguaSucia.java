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
 * Agua no tratada recolectada de la intemperie. Riesgosa para consumo directo;
 * debe hervirse al fuego (Zero-GC / O(1)).
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class CuencoAguaSucia extends Comida {

	private static final long serialVersionUID = 1L;

	public static final String CODIGO = "Cuenco de Agua Turbia";
	public static final int LIMITE_PILA = 10;
	public static final double VIDA_UTIL_HORAS = 96.0;

	public CuencoAguaSucia(final int x, final int y, final int cantidad) {
		// Aporte: 0 Hambre, +35 Sed, 0 Salud. Podrida o cruda: -15 Sed y Veneno
		super(x, y, cantidad, CODIGO, CODIGO, TexturaItem.CUENCO_AGUA_SUCIA_INV, TexturaItem.CUENCO_AGUA_SUCIA_MAPA,
				LIMITE_PILA, 0.0, 35.0, 0.0, 0.0, -15.0, TipoEfectoEstado.VENENO, 5.0, 0.60);

		this.configurarPerecedero(VIDA_UTIL_HORAS);
		this.setPrecioBasePlata(1L);
	}

	public CuencoAguaSucia(final int cantidad) {
		this(0, 0, cantidad);
	}

	public CuencoAguaSucia(final int x, final int y, final int cantidad, final double horaCaducidad,
			final boolean podrido) {
		super(x, y, cantidad, CODIGO, CODIGO, TexturaItem.CUENCO_AGUA_SUCIA_INV, TexturaItem.CUENCO_AGUA_SUCIA_MAPA,
				LIMITE_PILA, 0.0, 35.0, 0.0, 0.0, -15.0, TipoEfectoEstado.VENENO, 5.0, 0.60);

		this.establecerCaducidadDirecta(horaCaducidad, podrido);
		this.setPrecioBasePlata(1L);
	}

	@Override
	public void consumir(final Criatura c) {
		super.consumir(c);

		// Si se bebe directa sin hervir, 60% de probabilidad de contraer parásitos
		if (!this.podrido && (Math.random() < 0.60)) {
			c.aplicarEfecto(TipoEfectoEstado.VENENO, 4.0, 0.5);
			Globales.GESTOR_TEXTOS.agregarTexto("¡Parasitos estomacales!", c.getCentroX(), c.getPosicionYInt() - 8,
					principal.igu.textos.TipoTextoFlotante.VENENO);
		}

		// Devuelve el cuenco vacío al beber
		if (c instanceof Jugador) {
			Globales.GESTOR_INVENTARIO.getInventarioJugador().agregarObjeto(new CuencoVacio(1));
		}
	}

	@Override
	protected void rellenarInfo(final ArrayList<String> listaInfo) {
		super.rellenarInfo(listaInfo);
		listaInfo.add("Agua estancada o de río. Conviene hervirla.");
	}

	@Override
	public Objeto copiar() {
		return new CuencoAguaSucia(this.getPosicionXInt(), this.getPosicionYInt(), this.getCantidad(),
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

	public static CuencoAguaSucia crearDesdeJson(final JSONObject json) {
		if (json == null) {
			return new CuencoAguaSucia(1);
		}
		final int cant = (json.get("cantidad") != null) ? ((Number) json.get("cantidad")).intValue() : 1;
		final double horaCad = (json.get("horaCaducidad") != null) ? ((Number) json.get("horaCaducidad")).doubleValue()
				: -1.0;
		final boolean esPodrido = (json.get("podrido") != null) ? Boolean.parseBoolean(json.get("podrido").toString())
				: false;

		return new CuencoAguaSucia(0, 0, cant, horaCad, esPodrido);
	}
}