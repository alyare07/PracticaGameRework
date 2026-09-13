package principal.configuracion;

/**
 * Modos de adaptación de la resolución interna (640x360) a la pantalla física.
 */
public enum ModoEscalado {
	PIXEL_PERFECT_ENTERO("Entero (Pixel-Perfect)"),
	AJUSTE_PROPORCIONAL_16_9("Ajuste 16:9 (Aspect Fit)"),
	ESTIRAR_PANTALLA_COMPLETA("Estirar Pantalla");

	private final String nombreLegible;

	ModoEscalado(final String nombreLegible) {
		this.nombreLegible = nombreLegible;
	}

	public String getNombreLegible() {
		return this.nombreLegible;
	}

	public ModoEscalado siguiente() {
		final ModoEscalado[] valores = values();
		return valores[(this.ordinal() + 1) % valores.length];
	}

	public ModoEscalado anterior() {
		final ModoEscalado[] valores = values();
		return valores[(this.ordinal() - 1 + valores.length) % valores.length];
	}
}