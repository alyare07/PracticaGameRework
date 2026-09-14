package principal.entes.criaturas.grupo;

/**
 * Define la naturaleza del lazo entre una Criatura y el Jugador. Permite que
 * mascotas, familiares, soldados a sueldo y sirvientes compartan la misma
 * infraestructura de grupo bajo semánticas roleras claras.
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public enum TipoVinculo {

	/**
	 * Entidad salvaje, neutral o habitante común (no pertenece al círculo del
	 * jugador).
	 */
	NINGUNO("Sin Vínculo"),

	/** Animal domesticado o adoptado en el mundo. */
	MASCOTA("Mascota / Compañero"),

	/** Miembro general de la familia del jugador. */
	FAMILIA("Familia"),

	/** Cónyuge / Pareja matrimonial del jugador. */
	ESPOSA("Cónyuge"),

	/** Descendiente nacido o crecido en partida. */
	HIJO("Hijo / Descendiente"),

	/** Soldado o guardia contratado mediante pago monetario. */
	MERCENARIO("Mercenario a Sueldo"),

	/** Trabajador doméstico, agricultor o peón asignado a propiedades. */
	SIRVIENTE("Sirviente / Trabajador");

	private final String nombreLegible;

	private TipoVinculo(final String nombreLegible) {
		this.nombreLegible = nombreLegible;
	}

	public String getNombreLegible() {
		return this.nombreLegible;
	}

	public boolean esVinculado() {
		return this != NINGUNO;
	}

	public boolean esFamiliar() {
		return (this == FAMILIA) || (this == ESPOSA) || (this == HIJO);
	}
}