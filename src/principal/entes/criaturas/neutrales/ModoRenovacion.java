package principal.entes.criaturas.neutrales;

/**
 * Estrategias de reposición y rotación de mercancía para comerciantes según el
 * calendario canónico RPG.
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public enum ModoRenovacion {

	/** Reabastece cada X días acumulados (comportamiento clásico). */
	POR_INTERVALO_DIAS("Por Intervalo de Días"),

	/** Reabastece automáticamente cada Lunes (Día 1 de cada semana canónica). */
	CADA_LUNES("Cada Lunes (Inicio de Semana)"),

	/** Rota el catálogo completo el Día 1 de cada estación (Días 1, 29, 57, 85). */
	CADA_ESTACION("Cada Estación (Inicio de Temporada)");

	private final String nombreLegible;

	ModoRenovacion(final String nombreLegible) {
		this.nombreLegible = nombreLegible;
	}

	public String getNombreLegible() {
		return this.nombreLegible;
	}
}