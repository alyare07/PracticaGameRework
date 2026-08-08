package principal.eventos;

import principal.mapa.Mundo;
import principal.maquinaestado.estados.GestorPartida;

public abstract class EventoGeneral extends Evento {
	protected final GestorPartida GP;
	
	/**
	 * Esta clase contiene el gestor de partidas
	 * lo que permite que permite acceder al mundo 
	 * que este en cualquier momento, sin importar que 
	 * este cambie.
	 * 
	 * @param gp El Gestor de Partidas
	 */
	
	public EventoGeneral(final GestorPartida gp) {
		super(gp.getGestorJuego());
		this.GP = gp;
	}

	@Override
	protected Mundo getMundo() {
		return this.GP.getGestorJuego().getMundo();
	}


}
