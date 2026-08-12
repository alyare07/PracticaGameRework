package principal.ia.aEstrella;

import java.awt.Dimension;

/**
 * Representa un punto o casilla (celda) dentro de la grilla de búsqueda del
 * algoritmo A*. Implementa `Comparable<NodoA>` para que la `PriorityQueue`
 * pueda ordenar los nodos automáticamente según su costo final $F = G + H$.
 */
public class NodoA implements Comparable<NodoA> {

	/**
	 * Constante optimizada para el cálculo de la Distancia Octile. Equivale a
	 * Math.sqrt(2) - 1 ≈ 0.41421354f.
	 */
	private static final float SQRT_2_MINUS_ONE = (float) (Math.sqrt(2) - 1.0);

	/**
	 * Posibles estados de un nodo durante la búsqueda A*: - NINGUNO: No ha sido
	 * explorado en la búsqueda actual. - ABIERTA: Está en la cola de prioridad
	 * pendiente por explorar. - CERRADA: Ya fue explorado y sus vecinos fueron
	 * procesados.
	 */
	public enum EstadoNodo {
		NINGUNO, ABIERTA, CERRADA
	}

	// Posición X e Y del nodo dentro del mapa de la grilla (no en píxeles)
	private final int xNodo;
	private final int yNodo;

	/**
	 * Costo G: Distancia recorrida real acumulada desde el nodo inicial hasta este
	 * nodo.
	 */
	private float costoG;

	/**
	 * Costo H (Heurística): Distancia estimada restante desde este nodo hasta el
	 * nodo objetivo.
	 */
	private float costoH;

	/**
	 * Costo F Total: Suma de (costoG + costoH). Es la puntuación usada para decidir
	 * qué nodo evaluar primero.
	 */
	private float costoF;

	/**
	 * Identificador de la generación de búsqueda en la que este nodo fue reseteado
	 * por última vez.
	 */
	private int generacionBusqueda;

	/** Estado actual del nodo en la iteración. */
	private EstadoNodo estado;

	/**
	 * Referencia al nodo anterior en el camino óptimo (para poder reconstruir el
	 * camino al final).
	 */
	private NodoA nodoProcedente;

	/**
	 * Crea un nuevo nodo en una posición fija de la matriz.
	 *
	 * @param xNodo Coordenada X en la grilla.
	 * @param yNodo Coordenada Y en la grilla.
	 */
	public NodoA(final int xNodo, final int yNodo) {
		this.xNodo = xNodo;
		this.yNodo = yNodo;
		this.generacionBusqueda = 0;
		this.estado = EstadoNodo.NINGUNO;
	}

	/**
	 * Reinicia los valores del nodo únicamente cuando es alcanzado por una nueva
	 * búsqueda.
	 *
	 * @param generacionBusqueda Identificador de la búsqueda actual.
	 */
	public void reiniciar(final int generacionBusqueda) {
		this.generacionBusqueda = generacionBusqueda;
		this.costoG = Float.MAX_VALUE; // Inicialmente asignamos un costo infinito
		this.costoH = 0f;
		this.costoF = Float.MAX_VALUE;
		this.nodoProcedente = null;
		this.estado = EstadoNodo.NINGUNO;
	}

	/**
	 * Comprueba si este nodo ya ha sido alcanzado en la generación de búsqueda
	 * actual.
	 *
	 * @param generacionBusqueda Identificador de la búsqueda actual.
	 * @return 'true' si el nodo ya fue reiniciado/visitado en esta búsqueda;
	 *         'false' en caso contrario.
	 */
	public boolean visitado(final int generacionBusqueda) {
		return this.generacionBusqueda == generacionBusqueda;
	}

	/**
	 * Asigna los costos F, G y H del nodo actual.
	 * 
	 * ¿Qué es la Distancia Octile? Es la fórmula matemática ideal para calcular
	 * heurísticas en mapas con movimiento en 8 direcciones. Combina pasos rectos
	 * (costo 1) y pasos diagonales (costo √2).
	 *
	 * @param padre     Nodo desde el cual llegamos a este nodo.
	 * @param objetivo  Nodo destino final de la búsqueda.
	 * @param costoPaso Costo de moverse del padre a este nodo (1.0f u Ortogonal /
	 *                  1.414f o Diagonal).
	 */
	public void evaluar(final NodoA padre, final NodoA objetivo, final float costoPaso) {
		this.nodoProcedente = padre;
		this.costoG = (padre == null) ? 0f : padre.costoG + costoPaso;

		// Distancia absoluta en ejes X e Y hacia el destino
		final float dx = Math.abs(this.xNodo - objetivo.xNodo);
		final float dy = Math.abs(this.yNodo - objetivo.yNodo);

		// Fórmula simplificada de Distancia Octile:
		// max(dx, dy) + (√2 - 1) * min(dx, dy)
		this.costoH = Math.max(dx, dy) + (SQRT_2_MINUS_ONE * Math.min(dx, dy));

		// Calculamos el costo F total
		this.costoF = this.costoG + this.costoH;
	}

	/**
	 * Compara dos nodos para ordenar la Cola de Prioridad (`PriorityQueue`). Los
	 * nodos con menor `costoF` tendrán mayor prioridad.
	 *
	 * @param otro El otro nodo con el que se va a comparar.
	 * @return Un entero negativo si este nodo es preferible, positivo si el otro lo
	 *         es, o 0 si son iguales.
	 */
	@Override
	public int compareTo(final NodoA otro) {
		final int comparacionF = Float.compare(this.costoF, otro.costoF);

		// --- TIE-BREAKING (Desempate) ---
		// Si dos nodos tienen exactamente el mismo costo F, priorizamos el que tenga
		// menor costo H.
		// Un costo H menor significa que el nodo está visualmente más cerca del
		// objetivo final.
		if (comparacionF == 0) {
			return Float.compare(this.costoH, otro.costoH);
		}
		return comparacionF;
	}

	/**
	 * Comprueba si una coordenada dada en píxeles del mundo coincide con la
	 * posición de este nodo.
	 *
	 * @param xMundo        Coordenada X en píxeles.
	 * @param yMundo        Coordenada Y en píxeles.
	 * @param dimensionNodo Tamaño en píxeles de cada celda/nodo.
	 * @return 'true' si la posición coincide; 'false' si pertenece a otra casilla.
	 */
	public boolean compararPosicionesMundo(final int xMundo, final int yMundo, final Dimension dimensionNodo) {
		return (this.xNodo == (xMundo / dimensionNodo.width)) && (this.yNodo == (yMundo / dimensionNodo.height));
	}

	// --- Métodos de Acceso (Getters y Setters) ---

	public int getXNodo() {
		return this.xNodo;
	}

	public int getYNodo() {
		return this.yNodo;
	}

	public float getCostoG() {
		return this.costoG;
	}

	public float getCostoF() {
		return this.costoF;
	}

	public float getCostoH() {
		return this.costoH;
	}

	public NodoA getNodoProcedente() {
		return this.nodoProcedente;
	}

	public EstadoNodo getEstado() {
		return this.estado;
	}

	public void setEstado(final EstadoNodo estado) {
		this.estado = estado;
	}

	/**
	 * Restablece el identificador de la generación a 0 (solo se usa cuando ocurre
	 * un overflow de enteros).
	 */
	public void resetearGeneracion() {
		this.generacionBusqueda = 0;
	}
}