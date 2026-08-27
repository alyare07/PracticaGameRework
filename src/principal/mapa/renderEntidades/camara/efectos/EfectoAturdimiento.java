package principal.mapa.renderEntidades.camara.efectos;

/**
 * Efecto cinemático de conmoción, aturdimiento y pérdida de equilibrio.
 * <p>
 * <b>Comportamiento y Mecánica:</b><br>
 * Simula el mareo severo tras recibir un golpe contundente en la cabeza, una
 * explosión de granada cegadora (Flashbang) o un estado de aturdimiento (Stun).
 * </p>
 * <p>
 * <b>Fórmula Matemática (Curva de Lissajous 1:2 en forma de 8):</b><br>
 * En lugar de mover la cámara en círculos simples, utiliza una curva
 * paramétrica de Lissajous donde el eje Y oscila exactamente al <b>doble de
 * velocidad</b> que el eje X:
 * 
 * <pre>
 *   offsetX        = sin(t) * amplitudX
 *   offsetY        = sin(2t) * amplitudY
 *   anguloRotacion = sin(1.25t) * rotacionMax (en radianes)
 *   offsetZoom     = cos(1.5t) * zoomMax
 * </pre>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public class EfectoAturdimiento extends EfectoCamara {

	// =========================================================================
	// === PARÁMETROS CONFIGURABLES
	// =========================================================================

	/** Amplitud máxima de bamboleo horizontal en píxeles. */
	private double amplitudXPx = 10.0;

	/** Amplitud máxima de balanceo vertical en píxeles. */
	private double amplitudYPx = 6.0;

	/** Inclinación máxima de la cabeza en grados. */
	private double rotacionMaxGrados = 2.0;

	/** Factor de velocidad de la oscilación de mareo. */
	private double frecuenciaVelocidad = 2.5;

	// =========================================================================
	// === CONSTRUCTOR
	// =========================================================================

	/**
	 * Instancia el efecto registrándolo en el catálogo con su tipo correspondiente.
	 */
	public EfectoAturdimiento() {
		super(TipoEfectoCamara.ATURDIMIENTO);
	}

	// =========================================================================
	// === CONFIGURACIÓN
	// =========================================================================

	/**
	 * Permite calibrar la intensidad del bamboleo y la velocidad de mareo.
	 *
	 * @param amplitudXPx         Desplazamiento horizontal (píxeles).
	 * @param amplitudYPx         Desplazamiento vertical (píxeles).
	 * @param rotacionMaxGrados   Inclinación angular (grados).
	 * @param frecuenciaVelocidad Velocidad de oscilación (1.0 = lenta, 2.5 =
	 *                            estándar).
	 */
	public void configurar(final double amplitudXPx, final double amplitudYPx, final double rotacionMaxGrados,
			final double frecuenciaVelocidad) {
		this.amplitudXPx = Math.max(0.0, amplitudXPx);
		this.amplitudYPx = Math.max(0.0, amplitudYPx);
		this.rotacionMaxGrados = Math.max(0.0, rotacionMaxGrados);
		this.frecuenciaVelocidad = Math.max(0.1, frecuenciaVelocidad);
	}

	// =========================================================================
	// === CÁLCULO MATEMÁTICO (CERO ASIGNACIONES EN HEAP)
	// =========================================================================

	/**
	 * Calcula la trayectoria en forma de 8 (Lissajous) y las modulaciones en cada
	 * frame.
	 *
	 * @param delta Tiempo transcurrido en segundos (1.0 / 60.0 en bucle fijo).
	 */
	@Override
	protected void calcularTransformaciones(final double delta) {
		/*
		 * =====================================================================
		 * EXPLICACIÓN DIDÁCTICA: ¿POR QUÉ UNA CURVA DE LISSAJOUS?
		 * --------------------------------------------------------------------- 1. LA
		 * FORMA EN OCHO (∞ / 8): Cuando mueves X con sin(t) y mueves Y con sin(2t), la
		 * posición traza en la pantalla una figura en forma de ocho infinito (∞). Este
		 * es el movimiento biomecánico real que hace la cabeza de un boxeador o una
		 * persona mareada al intentar mantener el equilibrio.
		 * 
		 * 2. EL TRUCO DEL DESFASAMIENTO DE FRECUENCIAS: Si la rotación y el zoom usaran
		 * la misma velocidad que X e Y, el movimiento se vería repetitivo como un reloj
		 * mecánico. - Rotación usa frecuencia (t * 1.25) - Zoom usa frecuencia (t *
		 * 1.5)
		 * 
		 * Al usar múltiplos no enteros, los movimientos nunca coinciden en el mismo
		 * instante exacto. El resultado es un mareo totalmente orgánico, caótico y
		 * creíble.
		 * =====================================================================
		 */
		final double t = this.tiempoTranscurrido * this.frecuenciaVelocidad;

		// 1. Desplazamiento espacial en forma de 8 (Curva de Lissajous con ratio 1:2)
		this.offsetX = Math.sin(t) * this.amplitudXPx * this.intensidad;
		this.offsetY = Math.sin(t * 2.0) * this.amplitudYPx * this.intensidad;

		// 2. Inclinación de cabeza oscilante desfasada (±2.0 grados en radianes)
		this.anguloRotacion = Math.sin(t * 1.25) * Math.toRadians(this.rotacionMaxGrados) * this.intensidad;

		// 3. Respiración de zoom sutil (±5% de zoom) en desfase armónico
		this.offsetZoom = Math.cos(t * 1.5) * 0.05 * this.intensidad;
	}

	// =========================================================================
	// === GETTERS
	// =========================================================================

	public double getAmplitudXPx() {
		return this.amplitudXPx;
	}

	public double getAmplitudYPx() {
		return this.amplitudYPx;
	}

	public double getRotacionMaxGrados() {
		return this.rotacionMaxGrados;
	}

	public double getFrecuenciaVelocidad() {
		return this.frecuenciaVelocidad;
	}
}