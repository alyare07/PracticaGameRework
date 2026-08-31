package principal.clima;

import java.awt.Color;

import principal.iluminacion.IntensidadNiebla;

/**
 * Catálogo central de estados y perfiles meteorológicos predefinidos para el
 * mundo del juego.
 * 
 * @version 4.0
 */
public enum TipoClima {

	// =========================================================================
	// === 1. CLIMAS CLÁSICOS Y NATURALES
	// =========================================================================

	DESPEJADO("Despejado", IntensidadNiebla.DESACTIVADA, new Color(200, 215, 230), 0, false, true, 0.32f, 1.0, 45.0),
	VENTOSO("Ventoso", IntensidadNiebla.DESACTIVADA, new Color(200, 215, 230), 140, false, true, 0.38f, 2.6, 35.0),
	LLUVIA_LEVE("Lluvia Leve", IntensidadNiebla.LEVE, new Color(170, 190, 215), 180, false, false, 0.0f, 1.4, 65.0),
	LLUVIA_TORMENTA("Tormenta Eléctrica", IntensidadNiebla.LEVE, new Color(130, 150, 180), 360, true, false, 0.0f, 2.4,
			75.0),
	NIEVE("Nieve", IntensidadNiebla.LEVE, new Color(220, 235, 255), 220, false, false, 0.0f, 0.8, 30.0),
	VENTISCA("Ventisca", IntensidadNiebla.MODERADA, new Color(200, 220, 250), 380, false, false, 0.0f, 2.8, 80.0),
	TORMENTA_ARENA("Tormenta de Arena", IntensidadNiebla.INTENSA, new Color(210, 165, 90), 320, false, false, 0.0f, 3.2,
			10.0),

	// =========================================================================
	// === 2. CLIMAS TEMÁTICOS Y FANTASÍA
	// =========================================================================

	CENIZA_VOLCANICA("Ceniza Volcánica", IntensidadNiebla.MODERADA, new Color(90, 80, 85), 180, false, false, 0.0f, 0.6,
			60.0),
	ESPORAS_MAGICAS("Esporas Mágicas", IntensidadNiebla.LEVE, new Color(150, 220, 240), 110, false, false, 0.0f, 0.4,
			270.0),
	NIEBLA_CERRADA("Niebla Cerrada", IntensidadNiebla.INTENSA, new Color(210, 220, 230), 0, false, false, 0.0f, 0.5,
			45.0),
	PETALOS_CEREZO("Pétalos de Cerezo", IntensidadNiebla.DESACTIVADA, new Color(200, 215, 230), 120, false, true, 0.32f,
			1.2, 40.0),
	LLUVIA_ACIDA("Lluvia Ácida", IntensidadNiebla.LEVE, new Color(130, 185, 95), 240, false, false, 0.0f, 1.6, 70.0),

	// =========================================================================
	// === 3. CLIMAS MÍSTICOS Y CÓSMICOS
	// =========================================================================

	/** Aurora boreal ondulante con cintas de luz esmeralda y violeta. */
	AURORA_BOREAL("Aurora Boreal", IntensidadNiebla.LEVE, new Color(40, 180, 160), 90, false, false, 0.0f, 0.6, 45.0),

	/** Eclipse solar total con atmósfera de penumbra carmesí. */
	ECLIPSE_SOLAR("Eclipse Solar", IntensidadNiebla.MODERADA, new Color(120, 25, 45), 0, false, false, 0.0f, 0.3, 0.0),

	/** Lluvia cósmica de estrellas fugaces y meteoros brillantes. */
	LLUVIA_ESTRELLAS("Lluvia de Estrellas", IntensidadNiebla.DESACTIVADA, new Color(200, 220, 255), 130, false, false,
			0.0f, 1.1, 40.0);

	// =========================================================================
	// === ATRIBUTOS
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