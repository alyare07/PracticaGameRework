package principal.ia.arbol;

/**
 * Estados de retorno para los nodos de un Árbol de Comportamiento (Behavior Tree).
 * 
 * @version 1.0 (Vanilla Java 8 - Zero-GC Architecture)
 */
public enum EstadoBT {
	/** El nodo completó su lógica con éxito. */
	EXITO,

	/** El nodo no pudo cumplir su condición o su acción falló. */
	FRACASO,

	/** La acción requiere múltiples ticks para completarse (ej: desplazarse o canalizar). */
	EN_PROCESO
}