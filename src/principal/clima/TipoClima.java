package principal.clima;

import java.awt.Color;

import principal.iluminacion.IntensidadNiebla;

/**
 * Catálogo central de estados meteorológicos terrestres predefinidos
 * (precipitación, viento, niebla y partículas de suelo) calibrado para
 * resolución nativa 640x360.
 * 
 * @version 6.0 (Vanilla Java 8 - Purged Celestial Phenomena)
 */
public enum TipoClima {

	// =========================================================================
	// 1. CLIMAS CLÁSICOS Y NATURALES
	// =========================================================================
	DESPEJADO("Despejado", IntensidadNiebla.DESACTIVADA, new Color(200, 215, 230), 0, false, true, 0.32f, 1.0, 45.0),
	VENTOSO("Ventoso", IntensidadNiebla.DESACTIVADA, new Color(200, 215, 230), 55, false, true, 0.38f, 2.6, 35.0),
	LLUVIA_LEVE("Lluvia Leve", IntensidadNiebla.LEVE, new Color(170, 190, 215), 65, false, false, 0.0f, 1.4, 65.0),
	LLUVIA_TORMENTA("Tormenta Eléctrica", IntensidadNiebla.INTENSA, new Color(100, 120, 145), 120, true, false, 0.0f,
			3.8, 35.0),
	NIEVE("Nieve", IntensidadNiebla.LEVE, new Color(220, 235, 255), 75, false, false, 0.0f, 0.8, 30.0),
	VENTISCA("Ventisca", IntensidadNiebla.INTENSA, new Color(210, 230, 255), 125, false, false, 0.0f, 3.8, 15.0),
	TORMENTA_ARENA("Tormenta de Arena", IntensidadNiebla.INTENSA, new Color(210, 165, 90), 110, false, false, 0.0f, 3.2,
			10.0),

	// =========================================================================
	// 2. CLIMAS TEMÁTICOS Y SEVEROS
	// =========================================================================
	CENIZA_VOLCANICA("Ceniza Volcánica", IntensidadNiebla.MODERADA, new Color(90, 80, 85), 65, false, false, 0.0f, 0.6,
			60.0),
	ESPORAS_MAGICAS("Esporas Mágicas", IntensidadNiebla.LEVE, new Color(150, 220, 240), 40, false, false, 0.0f, 0.4,
			270.0),
	NIEBLA_CERRADA("Niebla Cerrada", IntensidadNiebla.INTENSA, new Color(210, 220, 230), 0, false, false, 0.0f, 0.5,
			45.0),
	PETALOS_CEREZO("Pétalos de Cerezo", IntensidadNiebla.DESACTIVADA, new Color(200, 215, 230), 45, false, true, 0.32f,
			1.2, 40.0),
	LLUVIA_ACIDA("Lluvia Ácida", IntensidadNiebla.LEVE, new Color(130, 185, 95), 85, false, false, 0.0f, 1.6, 70.0);

	// =========================================================================
	// ATRIBUTOS
	// =========================================================================
	private final String nombre;
	private final IntensidadNiebla nivelNiebla;
	private final Color colorNiebla;
	private final int cantidadParticulas;
	private final boolean tieneTormentaRayos;
	private final boolean tieneNubes;
	private final float opacidadNubes;
	private final double fuerzaViento;
	private final double anguloVientoGrados;

	TipoClima(final String nombre, final IntensidadNiebla nivelNiebla, final Color colorNiebla,
			final int cantidadParticulas, final boolean tieneTormentaRayos, final boolean tieneNubes,
			final float opacidadNubes, final double fuerzaViento, final double anguloVientoGrados) {
		this.nombre = nombre;
		this.nivelNiebla = nivelNiebla;
		this.colorNiebla = colorNiebla;
		this.cantidadParticulas = cantidadParticulas;
		this.tieneTormentaRayos = tieneTormentaRayos;
		this.tieneNubes = tieneNubes;
		this.opacidadNubes = opacidadNubes;
		this.fuerzaViento = fuerzaViento;
		this.anguloVientoGrados = anguloVientoGrados;
	}

	public String getNombre() {
		return this.nombre;
	}

	public IntensidadNiebla getNivelNiebla() {
		return this.nivelNiebla;
	}

	public Color getColorNiebla() {
		return this.colorNiebla;
	}

	public int getCantidadParticulas() {
		return this.cantidadParticulas;
	}

	public boolean isTieneTormentaRayos() {
		return this.tieneTormentaRayos;
	}

	public boolean isTieneNubes() {
		return this.tieneNubes;
	}

	public float getOpacidadNubes() {
		return this.opacidadNubes;
	}

	public double getFuerzaViento() {
		return this.fuerzaViento;
	}

	public double getAnguloVientoGrados() {
		return this.anguloVientoGrados;
	}
}