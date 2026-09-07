package principal.utilidades.progreso;

/**
 * Catálogo fuertemente tipado de todas las banderas (flags) de eventos,
 * jefes derrotados, palancas activadas y progreso de misiones.
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public enum FlagProgreso {

	// =========================================================================
	// 1. JEFES Y ENEMIGOS CLAVE
	// =========================================================================
	JEFE_BANDIDO_DERROTADO("Líder Bandido eliminado"),
	JEFE_GOLEM_DERROTADO("Guardián de Piedra derrotado"),

	// =========================================================================
	// 2. MECANISMOS, PUERTAS Y PALANCAS
	// =========================================================================
	PALANCA_CUEVA_ACTIVADA("Palanca de la cueva accionada"),
	PUERTA_CRIPTICA_DESBLOQUEADA("Sello de la cripta roto"),
	PUENTE_REPARADO("Puente hacia el norte transitable"),

	// =========================================================================
	// 3. MISIONES Y DIÁLOGOS
	// =========================================================================
	HABLO_CON_COMERCIANTE("Primer contacto con el comerciante"),
	MISION_PROLOGO_COMPLETADA("Prólogo completado");

	private final String descripcion;

	FlagProgreso(final String descripcion) {
		this.descripcion = descripcion;
	}

	public String getDescripcion() {
		return this.descripcion;
	}
}