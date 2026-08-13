package principal.eventos;

import principal.mapa.Mundo;
import principal.maquinaestado.estados.GestorPartida;

public abstract class EventoGeneral extends Evento {

	protected final GestorPartida GP;

	public EventoGeneral(final GestorPartida gp) {
		super(gp.getGestorJuego());
		this.GP = gp;
	}

	@Override
	protected Mundo getMundo() {
		return ((this.GP != null) && (this.GP.getGestorJuego() != null)) ? this.GP.getGestorJuego().getMundo() : null;
	}
}