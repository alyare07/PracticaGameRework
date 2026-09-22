package principal.entes.objetos.items.comidas;

import java.util.ArrayList;

import principal.entes.criaturas.Criatura;
import principal.entes.efectos.TipoEfectoEstado;
import principal.entes.objetos.items.Consumible;
import principal.igu.textos.TipoTextoFlotante;
import principal.recursos.TexturaItem;
import principal.utilidades.Globales;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Base abstracta universal para todos los víveres, alimentos cocinados, carnes
 * y bebidas del juego (Zero-GC / O(1)).
 * 
 * @version 1.0 (Vanilla Java 8 - Template Method Architecture)
 */
public abstract class Comida extends Consumible {

	private static final long serialVersionUID = 1L;

	// === VALORES NUTRICIONALES EN ESTADO FRESCO ===
	protected final double aporteHambre;
	protected final double aporteSed;
	protected final double aporteSalud;

	// === PENALIZACIONES EN ESTADO DESCOMPUESTO ===
	protected final double aporteHambrePodrido;
	protected final double aporteSedPodrido;
	protected final TipoEfectoEstado efectoPodrido;
	protected final double duracionEfectoPodrido;
	protected final double potenciaEfectoPodrido;

	public Comida(final int x, final int y, final int cantidad, final String codModelo, final String nombre,
			final TexturaItem texturaInv, final TexturaItem texturaMapa, final int limite, final double aporteHambre,
			final double aporteSed, final double aporteSalud, final double aporteHambrePodrido,
			final double aporteSedPodrido, final TipoEfectoEstado efectoPodrido, final double duracionVeneno,
			final double potenciaVeneno) {

		super(x, y, cantidad, codModelo, nombre, texturaInv, texturaMapa, limite);

		this.aporteHambre = aporteHambre;
		this.aporteSed = aporteSed;
		this.aporteSalud = aporteSalud;

		this.aporteHambrePodrido = aporteHambrePodrido;
		this.aporteSedPodrido = aporteSedPodrido;
		this.efectoPodrido = efectoPodrido;
		this.duracionEfectoPodrido = duracionVeneno;
		this.potenciaEfectoPodrido = potenciaVeneno;
	}

	/**
	 * Constructor estándar para alimentos comunes con penalización de putrefacción
	 * por defecto
	 */
	public Comida(final int x, final int y, final int cantidad, final String codModelo, final String nombre,
			final TexturaItem texturaInv, final TexturaItem texturaMapa, final int limite, final double aporteHambre,
			final double aporteSed, final double aporteSalud) {

		this(x, y, cantidad, codModelo, nombre, texturaInv, texturaMapa, limite, aporteHambre, aporteSed, aporteSalud,
				Math.max(1.0, aporteHambre * 0.25), -10.0, TipoEfectoEstado.VENENO, 4.0, 0.5);
	}

	@Override
	public void consumir(final Criatura c) {
		this.verificarCaducidad();

		if (c == null) {
			return;
		}

		if ((Globales.CAMARA != null) && (Globales.CAMARA.getEntidadEnfocada() != null)) {
			GestorSonido.reproducir(IDSonido.GOLPE_1);
		}

		final boolean esJugador = (c == Globales.JUGADOR);

		// =====================================================================
		// CASO A: ALIMENTO EN DESCOMPOSICIÓN (PODRIDO)
		// =====================================================================
		if (this.podrido) {
			if (esJugador && (Globales.GESTOR_METABOLISMO != null)) {
				Globales.GESTOR_METABOLISMO.ingerir(this.aporteHambrePodrido, this.aporteSedPodrido);
			}

			if (this.efectoPodrido != null) {
				c.aplicarEfecto(this.efectoPodrido, this.duracionEfectoPodrido, this.potenciaEfectoPodrido);
			}

			Globales.GESTOR_TEXTOS.agregarTexto("¡Comida descompuesta!", c.getCentroX(), c.getPosicionYInt() - 8,
					TipoTextoFlotante.VENENO);
		}
		// =====================================================================
		// CASO B: ALIMENTO FRESCO
		// =====================================================================
		else {
			if (esJugador && (Globales.GESTOR_METABOLISMO != null)) {
				Globales.GESTOR_METABOLISMO.ingerir(this.aporteHambre, this.aporteSed);
			}

			if (this.aporteSalud > 0.0) {
				c.curar(this.aporteSalud);
			}

			// Mensaje flotante formateado automáticamente según los aportes del alimento
			final String feedbackTexto = this.construirTextoFeedbackIngesta();
			Globales.GESTOR_TEXTOS.agregarTexto(feedbackTexto, c.getCentroX(), c.getPosicionYInt() - 8,
					(this.aporteSed > this.aporteHambre) ? TipoTextoFlotante.AGUA_SED
							: TipoTextoFlotante.COMIDA_HAMBRE);
		}

		this.reducirCantidad(1);
	}

	private String construirTextoFeedbackIngesta() {
		final int h = (int) Math.round(this.aporteHambre);
		final int s = (int) Math.round(this.aporteSed);

		if ((h > 0) && (s > 0)) {
			return "+" + h + " Hambre | +" + s + " Sed";
		}
		if (h > 0) {
			return "+" + h + " Hambre";
		}
		if (s > 0) {
			return "+" + s + " Sed";
		}
		return "+Consumido";
	}

	@Override
	protected void rellenarInfo(final ArrayList<String> listaInfo) {
		final int h = (int) Math.round(this.aporteHambre);
		final int s = (int) Math.round(this.aporteSed);
		final int hp = (int) Math.round(this.aporteSalud);

		if ((h > 0) || (s > 0)) {
			listaInfo.add("Sustento: +" + h + " Hambre | +" + s + " Sed");
		}
		if (hp > 0) {
			listaInfo.add("Vitalidad: +" + hp + " Salud");
		}
	}

	public double getAporteHambre() {
		return this.aporteHambre;
	}

	public double getAporteSed() {
		return this.aporteSed;
	}

	public double getAporteSalud() {
		return this.aporteSalud;
	}
}