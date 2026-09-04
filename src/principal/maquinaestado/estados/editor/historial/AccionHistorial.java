package principal.maquinaestado.estados.editor.historial;

/**
 * Contrato arquitectónico para cualquier operación reversible dentro del editor
 * de mapas (Cambios de terreno, colocación de entidades, triggers y luces).
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public interface AccionHistorial {

	/**
	 * Revierte los cambios aplicados en esta acción.
	 */
	void deshacer();

	/**
	 * Vuelve a aplicar los cambios revertidos.
	 */
	void rehacer();

	/**
	 * Retorna una descripción breve para la telemetría del editor (ej: "Pintar 12 Tiles").
	 */
	String getDescripcion();
}