package principal.crafteo;

import principal.entes.criaturas.jugador.Jugador;
import principal.mapa.Mundo;

/**
 * Contrato funcional para requisitos ambientales, celestes, climáticos o de historia
 * necesarios para desbloquear o fabricar una receta (Zero-GC / O(1)).
 * 
 * @version 1.0 (Vanilla Java 8 - Functional Specification Pattern)
 */
@FunctionalInterface
public interface CondicionEspecialReceta {

	/**
	 * Evalúa si las condiciones del mundo o del jugador permiten la fabricación.
	 * 
	 * @param jugador Referencia al jugador que intenta fabricar.
	 * @param mundo   Mundo activo donde se realiza la acción.
	 * @return {@code true} si la condición mística o ambiental se cumple; {@code false} en caso contrario.
	 */
	boolean seCumple(final Jugador jugador, final Mundo mundo);
}