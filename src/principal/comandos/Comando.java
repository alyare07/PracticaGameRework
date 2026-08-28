package principal.comandos;

/**
 * Clase base para todos los comandos de depuración y consola del motor.
 * 
 * @version 1.0
 */
public abstract class Comando {

	private final String nombre;
	private final String sintaxis;
	private final String descripcion;

	/**
	 * @param nombre      Palabra clave principal del comando (ej: "curar", "tp").
	 * @param sintaxis    Formato de uso (ej: "curar [cantidad]").
	 * @param descripcion Explicación de lo que hace para el comando de ayuda.
	 */
	public Comando(final String nombre, final String sintaxis, final String descripcion) {
		this.nombre = nombre.toLowerCase().trim();
		this.sintaxis = sintaxis;
		this.descripcion = descripcion;
	}

	/**
	 * Lógica que se ejecutará en el hilo principal a 60 APS.
	 * 
	 * @param args Arreglo con los parámetros ingresados (sin incluir el nombre del comando).
	 */
	public abstract void ejecutar(final String[] args);

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
	// === MÉTODOS DE PARSEO SEGURO (HELPERS)
	// =========================================================================

	protected int parsearEntero(final String str, final int valorPorDefecto) {
		try {
			return Integer.parseInt(str);
		} catch (final NumberFormatException e) {
			System.err.println("[Consola] Parametro entero invalido ('" + str + "'). Usando valor por defecto: " + valorPorDefecto);
			return valorPorDefecto;
		}
	}

	protected double parsearDouble(final String str, final double valorPorDefecto) {
		try {
			return Double.parseDouble(str);
		} catch (final NumberFormatException e) {
			System.err.println("[Consola] Parametro decimal invalido ('" + str + "'). Usando valor por defecto: " + valorPorDefecto);
			return valorPorDefecto;
		}
	}
}