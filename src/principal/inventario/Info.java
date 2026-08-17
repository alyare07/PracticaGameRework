package principal.inventario;

/**
 * Objeto de Transferencia de Datos (DTO) ligero diseñado para representar un
 * par clave-valor (etiqueta y valor estadístico) en paneles informativos de la
 * interfaz.
 * 
 * <p>
 * <b>Estrategia de Memoria (Zero-GC In-Place Mutation):</b>
 * </p>
 * <ul>
 * <li><b>Etiqueta Inmutable ({@link #label}):</b> El nombre de la estadística
 * ("Ataque", "Munición", etc.) es fijo y se define una única vez durante la
 * construcción.</li>
 * <li><b>Valor Mutable ({@link #valor}):</b> El valor numérico o textual puede
 * actualizarse en caliente mediante {@link #establecerValor(String)} a 60 APS
 * sin tener que desechar e instanciar nuevos objetos {@code Info} o reconstruir
 * las estructuras de {@link java.util.HashMap}.</li>
 * </ul>
 * 
 * @author Copiloto Técnico / Arquitectura del Motor
 * @version 1.0 (Vanilla Java 8)
 * @see CajaInfo
 * @see principal.inventario.equipamiento.SlotArma
 */
public class Info {

	/***/
	/* ========================================================================= */
	/* 1. ATRIBUTOS DE DATO */
	/* ========================================================================= */
	/***/

	/** Nombre descriptivo estático del atributo o estadística */
	private final String label;

	/** Valor textual dinámico asociado a la etiqueta */
	private String valor;

	/**
	 * Construye un contenedor de información clave-valor.
	 * 
	 * @param label Nombre fijo de la estadística (ej: "Ataque", "Alcance").
	 * @param valor Valor inicial de la estadística en formato cadena (ej: "15",
	 *              "100").
	 */
	public Info(final String label, final String valor) {
		this.label = (label != null) ? label : "";
		this.valor = (valor != null) ? valor : "";
	}

	/***/
	/* ========================================================================= */
	/* 2. MUTADORES Y ACCESORES DE ESTADO */
	/* ========================================================================= */
	/***/

	/**
	 * Actualiza el valor dinámico de la estadística en memoria sin reasignar la
	 * instancia.
	 * 
	 * @param valor Nuevo valor textual a asignar.
	 */
	public void establecerValor(final String valor) {
		this.valor = (valor != null) ? valor : "";
	}

	/**
	 * Obtiene el valor dinámico actual de la estadística.
	 * 
	 * @return Cadena de texto con el valor actual.
	 */
	public String getValor() {
		return this.valor;
	}

	/**
	 * Obtiene la etiqueta o nombre estático de la estadística.
	 * 
	 * @return Cadena de texto con el nombre descriptivo.
	 */
	public String getTexto() {
		return this.label;
	}

	@Override
	public String toString() {
		return this.label + ": " + this.valor;
	}
}