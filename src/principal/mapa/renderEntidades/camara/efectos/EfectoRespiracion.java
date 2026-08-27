package principal.mapa.renderEntidades.camara.efectos;

/**
 * Efecto cinemático ambiental de respiración y relajación (Breathing Calm).
 * <p>
 * <b>Comportamiento y Atmósfera (Game Feel):</b><br>
 * Simula el ritmo respiratorio humano calmado para momentos de introspección y
 * descanso:
 * <ul>
 * <li><b>Descanso en Fogatas y Posadas:</b> Genera una sensación acogedora de
 * tranquilidad y seguridad (estilo <i>Dark Souls</i> o <i>Celeste</i>).</li>
 * <li><b>Meditación y Regeneración de Maná:</b> Acompaña estados de
 * concentración espiritual o lectura de diarios y menús de lore.</li>
 * <li><b>Modo Sigilo en Espera:</b> Cuando el jugador se oculta agachado en la
 * hierba esperando el paso de una patrulla enemiga.</li>
 * </ul>
 * </p>
 * <p>
 * <b>Fórmula Matemática del Ciclo Respiratorio (~4.2 segundos):</b><br>
 * 
 * <pre>
 *   Periodo T  = 2π / frecuencia = 2π / 1.5 ≈ 4.19 segundos
 *   offsetZoom = sin(tiempo * frecuencia) * amplitudZoom * Intensidad
 * </pre>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public class EfectoRespiracion extends EfectoCamara {

	// =========================================================================
	// === PARÁMETROS CONFIGURABLES
	// =========================================================================

	/**
	 * Frecuencia angular de la respiración (1.5 rad/s equivale a un ciclo completo
	 * cada ~4.19 segundos).
	 */
	private double frecuencia = 1.5;

	/**
	 * Magnitud de acercamiento y alejamiento del zoom (0.075 = ±7.5% de zoom).
	 */
	private double amplitudZoom = 0.075;

	// =========================================================================
	// === CONSTRUCTOR
	// =========================================================================

	/**
	 * Instancia el efecto registrándolo en el catálogo con su tipo correspondiente.
	 */
	public EfectoRespiracion() {
		super(TipoEfectoCamara.RESPIRACION);
	}

	// =========================================================================
	// === CONFIGURACIÓN
	// =========================================================================

	/**
	 * Permite calibrar la lentitud del ciclo y la profundidad del zoom
	 * respiratorio.
	 *
	 * @param frecuencia   Velocidad del ciclo (ej: 1.0 para respiración muy
	 *                     profunda, 2.0 para respiración agitada).
	 * @param amplitudZoom Variación máxima de zoom (ej: 0.04 a 0.08).
	 */
	public void configurar(final double frecuencia, final double amplitudZoom) {
		this.frecuencia = Math.max(0.1, frecuencia);
		this.amplitudZoom = Math.max(0.0, amplitudZoom);
	}

	// =========================================================================
	// === CÁLCULO MATEMÁTICO (CERO ASIGNACIONES EN HEAP)
	// =========================================================================

	/**
	 * Calcula la dilatación y contracción armónica continua en cada frame.
	 *
	 * @param delta Tiempo transcurrido en segundos (1.0 / 60.0 en bucle fijo).
	 */
	@Override
	protected void calcularTransformaciones(final double delta) {
		/*
		 * =====================================================================
		 * EXPLICACIÓN DIDÁCTICA: ¿POR QUÉ DURA EXACTAMENTE ~4 SEGUNDOS?
		 * --------------------------------------------------------------------- 1. LA
		 * FÓRMULA DEL PERIODO (T = 2π / ω): - Una onda senoidal completa va de 0 a 2π
		 * radianes (≈ 6.2831). - Si multiplicamos el tiempo 't' por una frecuencia ω =
		 * 1.5, el tiempo que tarda en dar una vuelta completa es: T = 6.2831 / 1.5 =
		 * 4.188 segundos.
		 * 
		 * 2. LAS DOS FASES DE LA RESPIRACIÓN: - FASE 1 (Primeros 2.1 segundos, sin >
		 * 0): El valor es positivo. La cámara se acerca suavemente (+Zoom), simulando
		 * que el pecho se expande al INHALAR aire.
		 * 
		 * - FASE 2 (Siguientes 2.1 segundos, sin < 0): El valor es negativo. La cámara
		 * se aleja suavemente (-Zoom), simulando el alivio y relajación muscular al
		 * EXHALAR.
		 * =====================================================================
		 */
		final double t = this.tiempoTranscurrido * this.frecuencia;

		// 1. Oscilación sinusoidal armónica continua de respiración
		this.offsetZoom = Math.sin(t) * this.amplitudZoom * this.intensidad;

		// Sin desplazamiento espacial ni rotación (enfoque relajado centrado)
		this.offsetX = 0.0;
		this.offsetY = 0.0;
		this.anguloRotacion = 0.0;
	}

	// =========================================================================
	// === GETTERS
	// =========================================================================

	public double getFrecuencia() {
		return this.frecuencia;
	}

	public double getAmplitudZoom() {
		return this.amplitudZoom;
	}
}