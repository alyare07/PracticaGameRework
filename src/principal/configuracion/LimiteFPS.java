package principal.configuracion;

/**
 * Opciones de sincronización y tasa de cuadros por segundo.
 */
public enum LimiteFPS {
	FPS_30("30 FPS", 30, 1_000_000_000.0 / 30.0),
	FPS_60("60 FPS", 60, 1_000_000_000.0 / 60.0),
	ILIMITADO("Ilimitado", 0, 0.0);

	private final String nombreLegible;
	private final int fps;
	private final double nsPorFrame;

	LimiteFPS(final String nombreLegible, final int fps, final double nsPorFrame) {
		this.nombreLegible = nombreLegible;
		this.fps = fps;
		this.nsPorFrame = nsPorFrame;
	}

	public String getNombreLegible() {
		return this.nombreLegible;
	}

	public int getFps() {
		return this.fps;
	}

	public double getNsPorFrame() {
		return this.nsPorFrame;
	}

	public LimiteFPS siguiente() {
		final LimiteFPS[] valores = values();
		return valores[(this.ordinal() + 1) % valores.length];
	}

	public LimiteFPS anterior() {
		final LimiteFPS[] valores = values();
		return valores[(this.ordinal() - 1 + valores.length) % valores.length];
	}
}