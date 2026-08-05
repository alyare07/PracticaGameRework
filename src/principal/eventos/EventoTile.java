package principal.eventos;

import principal.mapa.Mundo;
import principal.mapa.Tile;
import principal.maquinaestado.estados.GestorJuego;

public abstract class EventoTile extends Evento {
	protected final Mundo MUNDO;
	protected final Tile TILE;
	
	/**
	 * Esta clase aferra el evento a un tile en especifico.
	 * @param tile El tile que se vinculara al evento.
	 * @param mundo El mundo que se tendra en cuenta en el evento.
	 */
	public EventoTile(final Tile tile, final Mundo mundo, final GestorJuego gj) {
		super(gj);
		this.TILE = tile;
		this.MUNDO = mundo;
	}

	@Override
	protected Mundo getMundo() {
		return this.MUNDO;
	}

}
