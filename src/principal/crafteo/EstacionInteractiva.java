package principal.crafteo;

/**
 * Contrato arquitectónico para cualquier objeto físico del mundo
 * que actúe como estación de trabajo / crafteo (Mesa, Horno, Yunque, Fogata).
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public interface EstacionInteractiva {

	/**
	 * Retorna el tipo de estación de crafteo que este objeto desbloquea.
	 */
	EstacionCrafteo getTipoEstacion();
}