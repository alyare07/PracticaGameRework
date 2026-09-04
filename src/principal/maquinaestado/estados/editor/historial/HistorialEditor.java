package principal.maquinaestado.estados.editor.historial;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Gestor maestro de la pila de Deshacer (Undo) y Rehacer (Redo) del editor.
 * Mantiene un límite de pasos para controlar el consumo de memoria Heap.
 * 
 * @version 1.0 (Vanilla Java 8 - Zero-GC Stack)
 */
public class HistorialEditor {

	private static final int MAX_PASOS_HISTORIAL = 60;

	private final Deque<AccionHistorial> pilaDeshacer = new ArrayDeque<AccionHistorial>(MAX_PASOS_HISTORIAL);
	private final Deque<AccionHistorial> pilaRehacer = new ArrayDeque<AccionHistorial>(MAX_PASOS_HISTORIAL);

	public HistorialEditor() {
	}

	/**
	 * Registra una nueva acción ejecutada. Limpia la pila de rehacer.
	 */
	public void registrarAccion(final AccionHistorial accion) {
		if (accion == null) {
			return;
		}

		if (this.pilaDeshacer.size() >= MAX_PASOS_HISTORIAL) {
			this.pilaDeshacer.removeLast(); // Descarta el paso más antiguo
		}

		this.pilaDeshacer.push(accion);
		this.pilaRehacer.clear();
	}

	/**
	 * Ejecuta el último paso de deshacer si está disponible.
	 * 
	 * @return La acción revertida, o null si la pila está vacía.
	 */
	public AccionHistorial deshacer() {
		if (this.pilaDeshacer.isEmpty()) {
			return null;
		}

		final AccionHistorial accion = this.pilaDeshacer.pop();
		accion.deshacer();
		this.pilaRehacer.push(accion);
		return accion;
	}

	/**
	 * Vuelve a aplicar el último paso revertido si está disponible.
	 * 
	 * @return La acción rehecha, o null si la pila está vacía.
	 */
	public AccionHistorial rehacer() {
		if (this.pilaRehacer.isEmpty()) {
			return null;
		}

		final AccionHistorial accion = this.pilaRehacer.pop();
		accion.rehacer();
		this.pilaDeshacer.push(accion);
		return accion;
	}

	public boolean puedeDeshacer() {
		return !this.pilaDeshacer.isEmpty();
	}

	public boolean puedeRehacer() {
		return !this.pilaRehacer.isEmpty();
	}

	public void limpiar() {
		this.pilaDeshacer.clear();
		this.pilaRehacer.clear();
	}

	public int getCantidadDeshacer() {
		return this.pilaDeshacer.size();
	}

	public int getCantidadRehacer() {
		return this.pilaRehacer.size();
	}
}