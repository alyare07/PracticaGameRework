package principal.igu;

import java.awt.Color;
import java.awt.Rectangle;

import principal.entes.criaturas.Jugador;
import principal.utilidades.Constantes;

public class BarraEstamina extends Barra {
	private final Jugador JUGADOR;

	public BarraEstamina(Rectangle area) {
		super(area,Color.black, Color.white, new Color(45, 209, 231), Color.black);
		this.JUGADOR = Constantes.JUGADOR;
	}

	@Override
	protected double getLimite() {
		return JUGADOR.getLimiteEstamina();
	}

	@Override
	protected double getCantidadActual() {
		return JUGADOR.getEstamina();
	}

}
