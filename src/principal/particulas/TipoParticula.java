package principal.particulas;

import java.awt.Color;

/**
 * Catálogo central de tipos y perfiles físicos para el motor de partículas 2D.
 * <p>
 * <b>¿Por qué usar un Enum para los presets de partículas? (Explicación para
 * novatos):</b><br>
 * En lugar de tener que configurar manualmente la gravedad, la fricción del
 * aire, el tamaño y los colores cada vez que ocurre un golpe o una explosión,
 * este enum encapsula "recetas físicas" pre-calibradas. <br>
 * Además, al almacenar instancias fijas de {@link Color} directamente en el
 * enum, el motor puede alternar colores durante el vuelo <b>sin crear objetos
 * {@code new Color()} en tiempo de ejecución</b>, manteniendo la memoria en
 * $0\text{ GC}$.
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public enum TipoParticula {

	// =========================================================================
	// === 1. PRESETS FÍSICOS Y VISUALES
	// =========================================================================

	/**
	 * Chispas incandescentes de fuego y explosiones.
	 * <p>
	 * <b>Física:</b> Flotan hacia arriba (gravedad negativa de -50 px/s²) y se
	 * encogen de 7 px a 1 px.
	 * </p>
	 */
	FUEGO_CHISPA(new Color(255, 200, 0), new Color(255, 80, 0), 1.0, 7.0f, 1.0f, -50.0, 0.92),

	/**
	 * Nubes densas de humo para detonaciones, fogatas o chimeneas.
	 * <p>
	 * <b>Física:</b> Flotan lentamente hacia arriba (-30 px/s²) y se
	 * <b>expanden</b> de 4 px a 8 px antes de disiparse.
	 * </p>
	 */
	HUMO(new Color(210, 210, 210), new Color(90, 90, 90), 1.2, 4.0f, 8.0f, -30.0, 0.95),

	/**
	 * Gotas pesadas de sangre por impactos críticos o cortes de espada.
	 * <p>
	 * <b>Física:</b> Caída pesada hacia el suelo (+220 px/s²) con rápida pérdida de
	 * velocidad lateral.
	 * </p>
	 */
	SANGRE(new Color(255, 20, 30), new Color(160, 0, 10), 0.9, 6.0f, 2.0f, 220.0, 0.85),

	/**
	 * Destellos arcanos y chispazos mágicos (curaciones, conjuros o auras).
	 * <p>
	 * <b>Física:</b> Levitan suavemente en el aire (-20 px/s²) con baja fricción.
	 * </p>
	 */
	MAGIA(new Color(80, 240, 255), new Color(200, 255, 255), 1.1, 6.0f, 1.0f, -20.0, 0.90),

	/**
	 * Nubes de polvo y tierra bajo los pies al correr, caminar o hacer Dash.
	 * <p>
	 * <b>Física:</b> Se frenan rápidamente por alta fricción (0.82) y duran poco
	 * tiempo (0.7 s).
	 * </p>
	 */
	POLVO_TIERRA(new Color(210, 180, 140), new Color(140, 110, 70), 0.7, 5.0f, 1.0f, -15.0, 0.82);

	// =========================================================================
	// === PARÁMETROS FÍSICOS Y DE COLOR
	// =========================================================================

	/** Color inicial brillante al momento de spawnear (primera mitad de vida). */
	private final Color colorInicio;

	/** Color final más oscuro o enfriado al disiparse (segunda mitad de vida). */
	private final Color colorFin;

	/** Duración base de la partícula en segundos (a 60 APS). */
	private final double duracionBaseSeg;

	/** Tamaño inicial en píxeles al momento de nacer. */
	private final float tamanoInicial;

	/** Tamaño final en píxeles antes de desaparecer. */
	private final float tamanoFinal;

	/**
	 * Aceleración vertical en píxeles por segundo al cuadrado (px/s²).
	 * <ul>
	 * <li><b>Valor positivo (+G):</b> Cae hacia el suelo (gravedad terrestre).</li>
	 * <li><b>Valor negativo (-G):</b> Flota hacia arriba (flotabilidad térmica de
	 * humo/fuego).</li>
	 * </ul>
	 */
	private final double gravedad;

	/**
	 * Coeficiente de resistencia aerodinámica del aire (0.0 a 1.0). Valores más
	 * bajos frenan la partícula más rápido.
	 */
	private final double friccion;

	/**
	 * Constructor interno del enum para definir las propiedades físicas.
	 *
	 * @param colorInicio     Color brillante inicial.
	 * @param colorFin        Color oscuro final.
	 * @param duracionBaseSeg Tiempo de vida estándar en segundos.
	 * @param tamanoInicial   Tamaño de nacimiento (px).
	 * @param tamanoFinal     Tamaño de muerte (px).
	 * @param gravedad        Fuerza vertical (positiva = cae, negativa = flota).
	 * @param friccion        Resistencia del aire (0.80 a 0.98).
	 */
	TipoParticula(final Color colorInicio, final Color colorFin, final double duracionBaseSeg,
			final float tamanoInicial, final float tamanoFinal, final double gravedad, final double friccion) {
		this.colorInicio = colorInicio;
		this.colorFin = colorFin;
		this.duracionBaseSeg = duracionBaseSeg;
		this.tamanoInicial = tamanoInicial;
		this.tamanoFinal = tamanoFinal;
		this.gravedad = gravedad;
		this.friccion = friccion;
	}

	// =========================================================================
	// === GETTERS
	// =========================================================================

	public Color getColorInicio() {
		return this.colorInicio;
	}

	public Color getColorFin() {
		return this.colorFin;
	}

	public double getDuracionBaseSeg() {
		return this.duracionBaseSeg;
	}

	public float getTamanoInicial() {
		return this.tamanoInicial;
	}

	public float getTamanoFinal() {
		return this.tamanoFinal;
	}

	public double getGravedad() {
		return this.gravedad;
	}

	public double getFriccion() {
		return this.friccion;
	}
}