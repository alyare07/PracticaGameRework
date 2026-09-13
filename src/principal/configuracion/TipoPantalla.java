package principal.configuracion;

/**
 * Modos de presentación de la ventana en el sistema operativo.
 */
public enum TipoPantalla {
	VENTANA("Modo Ventana"),
	SIN_BORDES("Pantalla Completa (Sin Bordes)"),
	EXCLUSIVA("Pantalla Completa (Exclusiva)");

	private final String nombreLegible;

	TipoPantalla(final String nombreLegible) {
		this.nombreLegible = nombreLegible;
	}

	public String getNombreLegible() {
		return this.nombreLegible;
	}

	public TipoPantalla siguiente() {
		final TipoPantalla[] valores = values();
		return valores[(this.ordinal() + 1) % valores.length];
	}

	public TipoPantalla anterior() {
		final TipoPantalla[] valores = values();
		return valores[(this.ordinal() - 1 + valores.length) % valores.length];
	}
}