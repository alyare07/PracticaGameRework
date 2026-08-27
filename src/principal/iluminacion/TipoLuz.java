package principal.iluminacion;

import java.awt.Color;

/**
 * Catálogo central de tipos y perfiles ópticos de fuentes de luz predefinidas.
 * <p>
 * <b>¿Por qué usar un Enum para los presets de luz? (Explicación para
 * novatos):</b><br>
 * En lugar de tener que configurar a mano el radio, el color y el parpadeo cada
 * vez que colocas una antorcha o disparas una bola de fuego, este enum
 * encapsula perfiles ya calibrados y equilibrados estéticamente. <br>
 * Además, su posición {@link Enum#ordinal()} permite que el motor
 * {@link GestorLuz} acceda en tiempo constante $O(1)$ a la textura de halo
 * pre-horneada en memoria, garantizando <b>cero asignaciones de memoria y
 * máxima velocidad de GPU</b>.
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public enum TipoLuz {

	// =========================================================================
	// === 1. LUCES DE AMBIENTE Y ESTRUCTURAS (FLICKERING / PARPADEO DE FUEGO)
	// =========================================================================

	/**
	 * Luz cálida anaranjada para antorchas de pared, faroles de poste o
	 * candelabros.
	 * <p>
	 * <b>Propiedades:</b> Radio base de 75 px con parpadeo medio de llama (±4.0
	 * px).
	 * </p>
	 */
	ANTORCHA(75, new Color(255, 170, 50), 0.90f, true, 4.0),

	/**
	 * Fogata de campamento, hogueras o piras funerarias.
	 * <p>
	 * <b>Propiedades:</b> Radio amplio de 130 px con oscilación de fuego profunda
	 * (±7.0 px).
	 * </p>
	 */
	FOGATA(130, new Color(255, 140, 30), 0.95f, true, 7.0),

	/**
	 * Resplandor íntimo y tenue para velas pequeñas, candiles o setas
	 * bioluminiscentes en cuevas.
	 * <p>
	 * <b>Propiedades:</b> Radio corto de 45 px con micro-parpadeo sutil (±2.5 px).
	 * </p>
	 */
	VELA_TENUE(45, new Color(255, 190, 80), 0.75f, true, 2.5),

	// =========================================================================
	// === 2. LUCES DEL JUGADOR Y PROYECTILES
	// =========================================================================

	/**
	 * Linterna frontal, lámpara de minero o aura base del Jugador.
	 * <p>
	 * <b>Propiedades:</b> Radio de 100 px constante y suave, sin parpadeo (0.0 px)
	 * para no cansar la vista.
	 * </p>
	 */
	LINTERNA_JUGADOR(100, new Color(255, 240, 200), 0.85f, false, 0.0),

	/**
	 * Proyectil de Bola de Fuego que ilumina el terreno a gran velocidad mientras
	 * vuela.
	 * <p>
	 * <b>Propiedades:</b> Radio de 85 px con estela de fuego viva (±5.0 px).
	 * </p>
	 */
	BOLA_FUEGO(85, new Color(255, 120, 20), 0.95f, true, 5.0),

	/**
	 * Destellos arcanos, curaciones divinas, cristales mágicos o portales de
	 * teletransporte.
	 * <p>
	 * <b>Propiedades:</b> Radio de 65 px cian/celeste eléctrico con pulsación
	 * mágica (±3.0 px).
	 * </p>
	 */
	MAGIA_ARCANO(65, new Color(80, 220, 255), 0.90f, true, 3.0);

	// =========================================================================
	// === PARÁMETROS ÓPTICOS Y FÍSICOS
	// =========================================================================

	/** Radio base de iluminación en píxeles de mundo antes del parpadeo. */
	private final int radioBase;

	/** Tinte de color del foco de luz (cálido, frío, arcano o neutro). */
	private final Color colorLuz;

	/** Nivel de claridad / opacidad en el centro del halo (0.0f a 1.0f). */
	private final float intensidad;

	/**
	 * Indica si el radio de la luz oscila con fórmulas senoidales simulando fuego.
	 */
	private final boolean parpadea;

	/**
	 * Variación máxima en píxeles que la llama puede expandirse o contraerse al
	 * parpadear.
	 */
	private final double amplitudParpadeo;

	/**
	 * Constructor interno para inicializar los parámetros físicos del preset.
	 *
	 * @param radioBase        Radio en píxeles.
	 * @param colorLuz         Color tonal del halo.
	 * @param intensidad       Claridad central.
	 * @param parpadea         {@code true} si la luz oscila como fuego.
	 * @param amplitudParpadeo Magnitud de la oscilación en píxeles.
	 */
	TipoLuz(final int radioBase, final Color colorLuz, final float intensidad, final boolean parpadea,
			final double amplitudParpadeo) {
		this.radioBase = radioBase;
		this.colorLuz = colorLuz;
		this.intensidad = intensidad;
		this.parpadea = parpadea;
		this.amplitudParpadeo = amplitudParpadeo;
	}

	// =========================================================================
	// === GETTERS
	// =========================================================================

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
}