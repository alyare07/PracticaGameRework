package principal.iluminacion;

import java.awt.Color;

/**
 * Catálogo central de presets ópticos con soporte para halos radiales y conos
 * direccionales.
 * 
 * @version 6.0
 */
public enum TipoLuz {

	// =========================================================================
	// === 1. HALOS RADIALES CIRCULARES (360°)
	// =========================================================================

	ANTORCHA(80, new Color(255, 160, 40), 0.85f, true, 4.0, false, 360.0),
	FOGATA(140, new Color(255, 120, 20), 0.90f, true, 7.0, false, 360.0),
	VELA_TENUE(45, new Color(255, 200, 100), 0.70f, true, 2.5, false, 360.0),

	/**
	 * Aura biológica del jugador: penumbra nocturna tenue azulada/oscura (sin luz
	 * blanca).
	 */
	AURA_JUGADOR(55, new Color(25, 45, 85), 0.35f, false, 0.0, false, 360.0),

	BOLA_FUEGO(90, new Color(255, 100, 10), 0.95f, true, 5.5, false, 360.0),
	MAGIA_ARCANO(70, new Color(70, 210, 255), 0.90f, true, 3.0, false, 360.0),
	DESTELLO_EXPLOSION(160, new Color(255, 235, 170), 1.0f, false, 0.0, false, 360.0),

	// =========================================================================
	// === 2. CONOS DE VISIÓN DIRECCIONALES
	// =========================================================================

	LINTERNA_CONICA(140, new Color(255, 245, 215), 0.90f, false, 0.0, true, 85.0),
	REFLECTOR(175, new Color(255, 255, 230), 0.95f, false, 0.0, true, 120.0);

	private final int radioBase;
	private final Color colorLuz;
	private final float intensidad;
	private final boolean parpadea;
	private final double amplitudParpadeo;
	private final boolean esCono;
	private final double anguloAperturaGrados;

	TipoLuz(final int radioBase, final Color colorLuz, final float intensidad, final boolean parpadea,
			final double amplitudParpadeo, final boolean esCono, final double anguloAperturaGrados) {
		this.radioBase = radioBase;
		this.colorLuz = colorLuz;
		this.intensidad = intensidad;
		this.parpadea = parpadea;
		this.amplitudParpadeo = amplitudParpadeo;
		this.esCono = esCono;
		this.anguloAperturaGrados = anguloAperturaGrados;
	}

	public int getRadioBase() {
		return this.radioBase;
	}

	public Color getColorLuz() {
		return this.colorLuz;
	}

	public float getIntensidad() {
		return this.intensidad;
	}

	public boolean isParpadea() {
		return this.parpadea;
	}

	public double getAmplitudParpadeo() {
		return this.amplitudParpadeo;
	}

	public boolean isEsCono() {
		return this.esCono;
	}

	public double getAnguloAperturaGrados() {
		return this.anguloAperturaGrados;
	}
}