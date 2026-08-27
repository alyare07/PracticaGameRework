package principal.mapa.renderEntidades.camara.efectos;

/**
 * Efecto cinemático de onda de choque expansiva por detonación (Shockwave
 * Pulse).
 * <p>
 * <b>Comportamiento y Física de Explosiones (Game Feel):</b><br>
 * Simula la onda de sobrepresión atmosférica producida por explosiones masivas:
 * <ul>
 * <li><b>Zoom-Out Instantáneo:</b> A diferencia de un pisotón (que acerca la
 * vista), una explosión empuja el aire y la cámara violentamente hacia atrás en
 * el frame inicial ($t=0$), expandiendo el campo de visión para mostrar la
 * magnitud del área afectada.</li>
 * <li><b>Rebote Elástico Armónico:</b> Tras el retroceso inicial, la cámara
 * rebota elásticamente hacia adelante y se estabiliza en reposo en unos $250
 * \text{ a } 350\text{ ms}$.</li>
 * </ul>
 * </p>
 * <p>
 * <b>Fórmula Matemática del Oscilador Armónico Sub-amortiguado:</b><br>
 * 
 * <pre>
 *   oscilacion = e^(-progreso * 4.5) * cos(progreso * π * 2.5)
 *   offsetZoom = -zoomOutMax * oscilacion * Intensidad
 * </pre>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public class EfectoOndaExpansiva extends EfectoCamara {

	// =========================================================================
	// === PARÁMETROS CONFIGURABLES
	// =========================================================================

	/**
	 * Magnitud máxima de alejamiento de cámara en el instante del estallido (0.20 =
	 * aleja un 20% el campo visual).
	 */
	private double zoomOutMax = 0.20;

	// =========================================================================
	// === CONSTRUCTOR
	// =========================================================================

	/**
	 * Instancia el efecto registrándolo en el catálogo con su tipo correspondiente.
	 */
	public EfectoOndaExpansiva() {
		super(TipoEfectoCamara.ONDA_EXPANSIVA);
	}

	// =========================================================================
	// === CONFIGURACIÓN
	// =========================================================================

	/**
	 * Permite calibrar la potencia del estallido antes de disparar el efecto.
	 *
	 * @param zoomOutMax Expansión máxima hacia afuera (ej: 0.15 para granadas, 0.30
	 *                   para bombas colosales).
	 */
	public void configurar(final double zoomOutMax) {
		this.zoomOutMax = Math.max(0.0, zoomOutMax);
	}

	// =========================================================================
	// === CÁLCULO MATEMÁTICO (CERO ASIGNACIONES EN HEAP)
	// =========================================================================

	/**
	 * Calcula el empuje de la onda de choque y su amortiguamiento en cada frame.
	 *
	 * @param delta Tiempo transcurrido en segundos (1.0 / 60.0 en bucle fijo).
	 */
	@Override
	protected void calcularTransformaciones(final double delta) {
		/*
		 * =====================================================================
		 * EXPLICACIÓN DIDÁCTICA: ¿POR QUÉ USAMOS COSENO (cos) Y NO SENO (sin)?
		 * --------------------------------------------------------------------- 1. EL
		 * IMPACTO EN EL FRAME CERO (t = 0): - sin(0) = 0.0 --> Empieza suave desde cero
		 * (malo para explosiones). - cos(0) = 1.0 --> Empieza al MÁXIMO
		 * instantáneamente.
		 * 
		 * Cuando una bomba estalla, la energía se libera en el milisegundo 0. Al usar
		 * cos(progreso * ...), en el primer fotograma la cámara ya está en su punto
		 * máximo de retroceso.
		 * 
		 * 2. EL REBOTE OSCILATORIO (cos * 2.5π): La onda pasa de positiva a negativa,
		 * haciendo que la cámara se aleje, luego se acerque un poco más de lo normal
		 * por inercia (rebote), y finalmente se estabilice.
		 * 
		 * 3. EL FRENO EXPONENCIAL (e^-4.5x): Absorbe la energía rápidamente para que el
		 * temblor no dure eternamente.
		 * 
		 * 4. EL SIGNO NEGATIVO (-zoomOutMax): Resta zoom para ALEJAR la vista
		 * (Zoom-Out) en lugar de acercarla.
		 * =====================================================================
		 */
		final double progreso = this.tiempoTranscurrido / this.duracionSegundos;

		// Resorte sub-amortiguado con fuerza máxima en t = 0
		final double oscilacion = Math.exp(-progreso * 4.5) * Math.cos(progreso * Math.PI * 2.5);

		// Zoom negativo (expansión visual de choque)
		this.offsetZoom = -this.zoomOutMax * oscilacion * this.intensidad;

		// Sin desplazamiento espacial ni rotación (la onda se expande radialmente en
		// todas direcciones)
		this.offsetX = 0.0;
		this.offsetY = 0.0;
		this.anguloRotacion = 0.0;
	}

	// =========================================================================
	// === GETTERS
	// =========================================================================

	public double getZoomOutMax() {
		return this.zoomOutMax;
	}
}