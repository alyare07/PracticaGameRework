package principal.configuracion;

/**
 * Perfiles de optimización de renderizado según la capacidad de la CPU/GPU.
 */
public enum PerfilRendimiento {
	POTATO("Potato (Atom / Netbook)"),
	BASICO("Basico (GPU Integrada)"),
	MEDIO("Medio (Equilibrado)"),
	ALTO("Alto (Graficos Maximos)");

	private final String nombreLegible;

	PerfilRendimiento(final String nombreLegible) {
		this.nombreLegible = nombreLegible;
	}

	public String getNombreLegible() {
		return this.nombreLegible;
	}

	public PerfilRendimiento siguiente() {
		final PerfilRendimiento[] valores = values();
		return valores[(this.ordinal() + 1) % valores.length];
	}

	public PerfilRendimiento anterior() {
		final PerfilRendimiento[] valores = values();
		return valores[(this.ordinal() - 1 + valores.length) % valores.length];
	}
}