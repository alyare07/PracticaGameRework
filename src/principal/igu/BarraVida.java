package principal.igu;

import java.awt.Color;
import java.awt.Rectangle;
import principal.entes.criaturas.Jugador;
import principal.utilidades.Constantes;

public class BarraVida  extends Barra {
	private final Jugador JUGADOR;

	public BarraVida(final Rectangle area) {
		super(area,Color.black, Color.white, Color.red, Color.black);
		this.JUGADOR = Constantes.JUGADOR;
	}
	
	@Override
	protected double getLimite() {
		return JUGADOR.getVidaMaxima();
	}

	@Override
	protected double getCantidadActual() {
		return JUGADOR.getVida();
	}
	
	
}
