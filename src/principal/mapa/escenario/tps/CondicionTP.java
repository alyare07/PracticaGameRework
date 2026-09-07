package principal.mapa.escenario.tps;

import principal.entes.criaturas.Jugador;

/**
 * Contrato funcional para evaluar requisitos antes de permitir un
 * teletransporte. Permite lambdas y validaciones complejas en O(1).
 * 
 * @version 1.0 (Vanilla Java 8)
 */
@FunctionalInterface
public interface CondicionTP {

	/**
	 * Evalúa si el jugador cumple con las condiciones para cruzar la puerta.
	 * 
	 * @param jugador Instancia del jugador activo.
	 * @return true si tiene permitido el paso; false en caso contrario.
	 */
	boolean seCumple(Jugador jugador);

	/**
	 * Mensaje de retroalimentación que aparecerá sobre la puerta si el jugador es
	 * rechazado.
	 */
	default String getMensajeRechazo() {
		return "¡Acceso bloqueado!";
	}

	/**
	 * Acción que se ejecuta al cruzar exitosamente (ej. consumir la llave del
	 * inventario).
	 */
	default void alCruzar(final Jugador jugador) {
		// Por defecto no realiza acciones adicionales
	}
}