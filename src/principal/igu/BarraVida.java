package principal.igu;

import java.awt.Color;
import java.awt.Rectangle;

import principal.entes.criaturas.Jugador;
import principal.utilidades.Globales;

/**
 * Barra de vida principal del jugador ubicada de forma fija en la interfaz
 * (HUD).
 * <p>
 * <b>Integración con la Barra Fantasma:</b><br>
 * Sobreescribe {@link #getCantidadLag()} para vincularse directamente a
 * {@link Jugador#getVidaLag()}, mostrando el rastro amarillo de daño durante 1
 * segundo.
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public class BarraVida extends Barra {

	private final Jugador JUGADOR;

	public BarraVida(final Rectangle area) {
		// area, borde (Negro), fondo (Gris oscuro/Blanco), relleno (Rojo), texto
		// (Blanco/Negro)
		super(area, Color.BLACK, Color.WHITE, Color.RED, Color.BLACK);
		this.JUGADOR = Globales.JUGADOR;
	}

	@Override
	protected double getLimite() {
		return this.JUGADOR.getVidaMaxima();
	}

	@Override
	protected double getCantidadActual() {
		return this.JUGADOR.getVida();
	}

	/**
	 * Conecta el rastro amortiguado con el cálculo de vida-lag del Jugador.
	 */
	@Override
	protected double getCantidadLag() {
		return this.JUGADOR.getVidaLag();
	}
}