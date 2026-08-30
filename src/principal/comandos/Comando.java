package principal.comandos;

/**
 * Clase base para todos los comandos de depuración y consola del motor.
 * <p>
 * Compatible con Java 8. Permite respuestas directas al emisor local o remoto.
 * </p>
 */
public abstract class Comando {

	private final String nombre;
	private final String sintaxis;
	private final String descripcion;

	public Comando(final String nombre, final String sintaxis, final String descripcion) {
		this.nombre = nombre.toLowerCase().trim();
		this.sintaxis = sintaxis;
		this.descripcion = descripcion;
	}

	/**
	 * Lógica de ejecución estándar (retrocompatible con comandos ya creados).
	 * 
	 * @param args Arreglo con los argumentos parseados.
	 */
	public abstract void ejecutar(final String[] args);

	/**
	 * Sobrecarga que permite a los comandos responder directamente al emisor
	 * (consola o terminal remota). Por defecto delega en
	 * {@link #ejecutar(String[])}.
	 * 
	 * @param args   Arreglo con los argumentos.
	 * @param emisor Canal de respuesta (local o remoto).
	 */
	public void ejecutar(final String[] args, final EmisorRespuesta emisor) {
		this.ejecutar(args);
	}

	public String getNombre() {
		return this.nombre;
	}

	public String getSintaxis() {
		return this.sintaxis;
	}

	public String getDescripcion() {
		return this.descripcion;
	}

	// =========================================================================
	// === MÉTODOS DE UTILIDAD Y PARSEO SEGURO
	// =========================================================================

	protected void enviarInfo(final EmisorRespuesta emisor, final String mensaje) {
		if (emisor != null) {
			emisor.enviarMensaje("[Consola] " + mensaje);
		} else {
			System.out.println("[Consola] " + mensaje);
		}
	}

	protected void enviarError(final EmisorRespuesta emisor, final String error) {
		if (emisor != null) {
			emisor.enviarError("[Consola] " + error);
		} else {
			System.err.println("[Consola] " + error);
		}
	}

	protected int parsearEntero(final String str, final int valorPorDefecto) {
		try {
			return Integer.parseInt(str);
		} catch (final NumberFormatException e) {
			return valorPorDefecto;
		}
	}

	protected double parsearDouble(final String str, final double valorPorDefecto) {
		try {
			return Double.parseDouble(str);
		} catch (final NumberFormatException e) {
			return valorPorDefecto;
		}
	}
}