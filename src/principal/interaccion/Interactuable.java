package principal.interaccion;

import principal.entes.criaturas.Jugador;

/**
 * Contrato para cualquier objeto, NPC o elemento del mapa que reaccione
 * a la tecla de interacción [E].
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public interface Interactuable {

	/**
	 * Texto que aparecerá en el prompt flotante (ej: "Hablar", "Examinar", "Abrir").
	 */
	String getTextoPrompt();

	/**
	 * Acción a ejecutar cuando el jugador presiona [E] frente a este elemento.
	 */
	void interactuar(Jugador jugador);

	/**
	 * Condición opcional para saber si actualmente puede interactuarse con él.
	 */
	default boolean puedeInteractuar(Jugador jugador) {
		return true;
	}

	int getCentroX();
	int getPosicionYInt();
}