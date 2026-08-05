package principal.eventos;

import principal.mapa.Mundo;
import principal.maquinaestado.estados.GestorJuego;

public abstract class Evento {
	protected boolean repetir;	
	protected boolean eliminado;
	protected final GestorJuego GJ;
	
	protected Evento(final GestorJuego gj) {
		this.GJ = gj;
	}
	
	public void actualizar() {
		if(this.getMundo() != this.GJ.getMundo()) this.eliminado = true;
		if(!this.eliminado && this.getCondicionEvento().cumpleCondicionEvento()) {
			if(!this.repetir) this.eliminado = true;
			this.getEjecucionEvento().ejecutarEvento();
		}
	}
	
	public boolean estaEliminado() {
		return this.eliminado;
	}
	
	public void eliminar() {
		this.eliminado = true;
	}
	
	protected abstract Mundo getMundo();
	
	/**
	 * Codigo que se ejecutaria al cumplirse la condicion del evento.
	 */
	protected abstract EjecucionEvento getEjecucionEvento();
	
	/**
	 * Condicion necesaria para que el evento pueda ejecutarse.
	 */
	protected abstract CondicionEvento getCondicionEvento();
	
}
