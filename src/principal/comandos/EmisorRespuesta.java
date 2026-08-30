package principal.comandos;

/**
 * Interfaz que desacopla el destino de los mensajes de salida generados
 * por la ejecución de un comando (Consola local, Socket remoto, Log, etc.).
 */
public interface EmisorRespuesta {

	/**
	 * Envía un mensaje informativo estándar al origen del comando.
	 * 
	 * @param mensaje Texto a emitir.
	 */
	void enviarMensaje(String mensaje);

	/**
	 * Envía un mensaje de error o advertencia al origen del comando.
	 * 
	 * @param error Texto del error a emitir.
	 */
	void enviarError(String error);
}