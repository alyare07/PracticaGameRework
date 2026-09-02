package principal.igu;

import java.awt.Color;
import java.awt.Rectangle;

import principal.entes.criaturas.Jugador;
import principal.utilidades.Globales;

public class BarraEstamina extends Barra {

	private final Jugador JUGADOR;

	public BarraEstamina(final Rectangle area) {
		super(area, Color.BLACK, new Color(15, 18, 24), new Color(45, 209, 231), Color.WHITE);
		this.JUGADOR = Globales.JUGADOR;
	}

	@Override
	protected double getLimite() {
		return (this.JUGADOR != null) ? this.JUGADOR.getLimiteEstamina() : 30.0;
	}

	@Override
	protected double getCantidadActual() {
		return (this.JUGADOR != null) ? this.JUGADOR.getEstamina() : 30.0;
	}
}