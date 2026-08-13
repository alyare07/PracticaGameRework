package principal.eventos;

import principal.mapa.Mundo;
import principal.maquinaestado.estados.GestorJuego;

/**
 * Clase base para disparadores de eventos lógicos en el juego.
 * <p>
 * Optimizado para evaluar condiciones y ejecuciones sin instanciar objetos por
 * frame.
 * </p>
 */
public abstract class Evento {

	protected boolean repetir;
	protected boolean eliminado;
	protected final GestorJuego GJ;

	protected Evento(final GestorJuego gj) {
		this.GJ = gj;
	}

	public void actualizar() {
		if (this.eliminado) {
			return;
		}

		// Si el evento pertenecía a un mundo previo que ya cambió, se elimina
		final Mundo mundoActual = (this.GJ != null) ? this.GJ.getMundo() : null;
		if (this.getMundo() != mundoActual) {
			this.eliminado = true;
			return;
		}

		// Evaluación directa $O(1)$ sin instanciar lambdas en memoria
		if (this.cumpleCondicion()) {
			if (!this.repetir) {
				this.eliminado = true;
			}
			this.ejecutar();
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
	 * Condición necesaria para que el evento pueda ejecutarse.
	 */
	protected abstract boolean cumpleCondicion();

	/**
	 * Código que se ejecutará al cumplirse la condición del evento.
	 */
	protected abstract void ejecutar();

	// Delegados por retrocompatibilidad si eran llamados externamente
	protected CondicionEvento getCondicionEvento() {
		return this::cumpleCondicion;
	}

	protected EjecucionEvento getEjecucionEvento() {
		return this::ejecutar;
	}
}