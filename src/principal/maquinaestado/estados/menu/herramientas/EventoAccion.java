package principal.maquinaestado.estados.menu.herramientas;

/**
 * Interfaz funcional para despachar acciones al presionar botones de la
 * interfaz.
 */
@FunctionalInterface
public interface EventoAccion {
	void ejecutar();
}