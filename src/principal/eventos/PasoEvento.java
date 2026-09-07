package principal.eventos;

/**
 * Representa una acción individual dentro de una secuencia o cinemática.
 */
public interface PasoEvento {
	void iniciar();
	void actualizar(double dt);
	boolean haTerminado();
}