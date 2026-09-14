package principal.ia.arbol;

import principal.entes.criaturas.Criatura;

/**
 * Contrato base para todos los nodos del Árbol de Comportamiento.
 * <p>
 * <b>REGLA DE RENDIMIENTO (Stateless):</b> Las implementaciones NO deben almacenar
 * estado mutable dentro de sus campos de clase. Toda la memoria transitoria debe
 * leerse y escribirse en el {@link BlackboardIA} provisto en la firma. Esto permite
 * que una única instancia del árbol sea compartida concurrentemente por cientos de
 * criaturas sin colisión de memoria ni asignaciones en el Heap.
 * </p>
 * 
 * @version 1.0 (Vanilla Java 8 - Zero-GC Architecture)
 */
public interface NodoBT {

	/**
	 * Evalúa y ejecuta la lógica del nodo en el tick actual.
	 * 
	 * @param criatura Criatura que ejecuta el comportamiento.
	 * @param bb       Memoria local (Blackboard) de la criatura.
	 * @param dt       Delta de tiempo del fotograma en segundos.
	 * @return Resultado de la evaluación (EXITO, FRACASO o EN_PROCESO).
	 */
	EstadoBT ejecutar(final Criatura criatura, final BlackboardIA bb, final double dt);
}