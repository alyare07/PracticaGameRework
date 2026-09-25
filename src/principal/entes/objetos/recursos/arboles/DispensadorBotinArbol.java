package principal.entes.objetos.recursos.arboles;

import principal.entes.objetos.recursos.ArbolCosechable;
import principal.mapa.Mundo;

/**
 * Estrategia de entrega de recursos al talar un árbol o arrancar su tocón.
 * Desacopla el botín de la persistencia (no se serializa en JSON).
 */
@FunctionalInterface
public interface DispensadorBotinArbol {

	/**
	 * Genera y entrega los ítems en el mundo al talar la copa o el tocón.
	 *
	 * @param arbol Instancia viva del árbol que fue destruido o talado.
	 * @param mundo Mundo activo donde se arrojarán los ítems.
	 * @param esTocon true si se acaba de destruir el tocón; false si cayó la copa.
	 */
	void soltar(ArbolCosechable arbol, Mundo mundo, boolean esTocon);
}