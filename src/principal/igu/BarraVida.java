package principal.igu;

import java.awt.Color;
import java.awt.Rectangle;

import principal.entes.criaturas.Jugador;
import principal.utilidades.Globales;

public class BarraVida extends Barra {
	private final Jugador JUGADOR;

	public BarraVida(final Rectangle area) {
		super(area, Color.black, Color.white, Color.red, Color.black);
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

}
