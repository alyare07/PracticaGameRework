package principal.igu.textos;

import java.awt.Color;

/**
 * Catálogo central de estilos visuales, jerarquía tipográfica y presets de
 * color para los textos flotantes de combate (Floating Combat Text / Damage
 * Numbers).
 * <p>
 * <b>Psicología Visual y Feedback de Combate (Game Feel):</b>
 * <ul>
 * <li><b>Confirmación Inmediata de Impacto:</b> Los números flotantes comunican
 * al cerebro del jugador el resultado matemático exacto de sus acciones en
 * milisegundos.</li>
 * <li><b>Diferenciación Cromática por Significado:</b>
 * <ul>
 * <li><i>Blanco puro:</i> Daño físico/mágico ordinario predecible.</li>
 * <li><i>Rojo carmesí (13 pt):</i> Impacto crítico demoledor que resalta por
 * encima de los demás.</li>
 * <li><i>Verde esmeralda:</i> Alivio positivo de curación y recuperación de
 * salud.</li>
 * <li><i>Azul celeste:</i> Restauración de maná, recargas o absorción de
 * escudos.</li>
 * <li><i>Dorado / Amarillo:</i> Recompensas satisfactorias de experiencia y
 * monedas de oro.</li>
 * <li><i>Naranja de estado:</i> Control de masas e interrupciones (¡STUN!,
 * ¡FALLO!, ¡VENENO!).</li>
 * </ul>
 * </li>
 * <li><b>Duraciones Calibradas:</b> Un número normal dura 900 ms, mientras que
 * un golpe crítico permanece en pantalla durante 1.200 ms con un salto más alto
 * para que el jugador disfrute del impacto.</li>
 * </ul>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public enum TipoTextoFlotante {

	// =========================================================================
	// === 1. PRESETS DE COMBATE Y RETROALIMENTACIÓN VISUAL
	// =========================================================================

	/**
	 * Daño ordinario recibido o infligido.
	 * <p>
	 * <b>Propiedades:</b> Color blanco nítido, tamaño 9 pt, estilo estándar,
	 * duración de 900 ms.
	 * </p>
	 */
	DANIO_NORMAL(new Color(255, 255, 255), 9f, false, 900),

	/**
	 * Impacto crítico de alta potencia o remate definitivo.
	 * <p>
	 * <b>Propiedades:</b> Rojo vibrante, tamaño aumentado a 13 pt en negrita
	 * (Bold), salto alto y duración de 1.200 ms.
	 * </p>
	 */
	CRITICO(new Color(255, 60, 60), 13f, true, 1200),

	/**
	 * Regeneración de puntos de vida, pociones de salud o hechizos de sanación.
	 * <p>
	 * <b>Propiedades:</b> Verde esmeralda reconfortante, tamaño 10 pt, duración de
	 * 1.000 ms.
	 * </p>
	 */
	CURACION(new Color(60, 255, 90), 10f, false, 1000),

	/**
	 * Restauración de energía mágica, pociones de éter o absorción de barreras.
	 * <p>
	 * <b>Propiedades:</b> Azul celeste brillante, tamaño 9 pt, duración de 900 ms.
	 * </p>
	 */
	MANA(new Color(60, 200, 255), 9f, false, 900),

	/**
	 * Avisos de combate y estados alterados ("¡STUN!", "¡FALLO!", "¡BLOQUEO!",
	 * "¡VENENO!").
	 * <p>
	 * <b>Propiedades:</b> Amarillo anaranjado de alerta en negrita, tamaño 9 pt,
	 * duración de 1.100 ms.
	 * </p>
	 */
	ESTADO(new Color(255, 190, 40), 9f, true, 1100),

	/**
	 * Recolección de monedas de oro, gemas valiosas o ganancia de experiencia (XP).
	 * <p>
	 * <b>Propiedades:</b> Dorado metálico brillante, tamaño 9 pt, duración de 1.000
	 * ms.
	 * </p>
	 */
	ORO_EXP(new Color(255, 225, 30), 9f, false, 1000);

	// =========================================================================
	// === PARÁMETROS VISUALES Y TEMPORALES
	// =========================================================================

	/** Color tonal principal del texto (resaltado con sombra negra en pantalla). */
	private final Color color;

	/** Tamaño de la tipografía en puntos para la fuente de renderizado. */
	private final float tamanoFuente;

	/**
	 * Indica si el texto debe utilizar formato en negrita (Bold) y salto balístico
	 * amplificado.
	 */
	private final boolean esCritico;

	/** Tiempo total de vida en pantalla expresado en milisegundos. */
	private final int duracionMs;

	/**
	 * Constructor interno del enum para inicializar los atributos de cada estilo.
	 *
	 * @param color        Tono cromático de renderizado.
	 * @param tamanoFuente Escala de la fuente en puntos.
	 * @param esCritico    {@code true} si utiliza estilo de impacto crítico.
	 * @param duracionMs   Duración de la animación en milisegundos.
	 */
	TipoTextoFlotante(final Color color, final float tamanoFuente, final boolean esCritico, final int duracionMs) {
		this.color = color;
		this.tamanoFuente = tamanoFuente;
		this.esCritico = esCritico;
		this.duracionMs = duracionMs;
	}

	// =========================================================================
	// === GETTERS
	// =========================================================================

	public Color getColor() {
		return this.color;
	}

	public float getTamanoFuente() {
		return this.tamanoFuente;
	}

	public boolean isCritico() {
		return this.esCritico;
	}

	public int getDuracionMs() {
		return this.duracionMs;
	}
}