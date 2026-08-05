package principal.eventos;

import principal.entes.Ente;
import principal.mapa.Mundo;
import principal.maquinaestado.estados.GestorJuego;

public abstract class EventoEnte extends Evento {
	protected final Ente ENTE;
	
	/**
	 * Esta clase aferra el evento a una entidad determinada y
	 * permite al evento acceder solo al mundo que posee esa entidad.
	 * 
	 * @param e La entidad que se vinculara al evento.
	 */
	public EventoEnte(final Ente e, final GestorJuego gj) {
		super(gj);
		this.ENTE = e;
	}
	
	public Ente getEnte() {
		return this.ENTE;
	}
	
	@Override
	protected Mundo getMundo() {
		return this.ENTE.getMundo();
	}
	
}
