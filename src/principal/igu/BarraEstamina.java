package principal.igu;

import java.awt.Color;
import java.awt.Rectangle;

import principal.entes.criaturas.Jugador;
import principal.utilidades.Globales;

public class BarraEstamina extends Barra {
	private final Jugador JUGADOR;

	public BarraEstamina(final Rectangle area) {
		super(area, Color.black, Color.white, new Color(45, 209, 231), Color.black);
		this.JUGADOR = Globales.JUGADOR;
	}

	@Override
	protected double getLimite() {
		return this.JUGADOR.getLimiteEstamina();
	}

	@Override
	protected double getCantidadActual() {
		return this.JUGADOR.getEstamina();
	}

}
