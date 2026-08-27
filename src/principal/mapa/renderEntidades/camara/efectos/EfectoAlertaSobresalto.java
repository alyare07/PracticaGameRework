package principal.mapa.renderEntidades.camara.efectos;

/**
 * Efecto cinemático de sobresalto reflejo (Jump-Scare / Detección "!").
 * <p>
 * <b>Comportamiento y Mecánica:</b><br>
 * Simula el "respingo" involuntario que experimenta una persona ante una
 * sorpresa violenta (como una emboscada, pisar una trampa oculta o ser
 * detectado en modo sigilo estilo <i>Metal Gear</i>).
 * </p>
 * <p>
 * <b>Fórmula Matemática del Impulso Asimétrico:</b><br>
 * Utiliza una función de resorte sub-amortiguado rápido combinando una curva
 * senoidal con decaimiento exponencial:
 * 
 * <pre>
 *   Amortiguación = e^(-progreso * 7.0) * sin(progreso * π)
 *   offsetY       = -saltoPx * Amortiguación * Intensidad
 *   offsetZoom    = +0.08 * Amortiguación * Intensidad
 * </pre>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public class EfectoAlertaSobresalto extends EfectoCamara {

	// =========================================================================
	// === PARÁMETROS CONFIGURABLES
	// =========================================================================

	/**
	 * Magnitud máxima en píxeles que la cámara salta hacia arriba en el pico del
	 * sobresalto.
	 */
	private double saltoPx = 7.0;

	// =========================================================================
	// === CONSTRUCTOR
	// =========================================================================

	/**
	 * Instancia el efecto registrándolo en el catálogo con su tipo correspondiente.
	 */
	public EfectoAlertaSobresalto() {
		super(TipoEfectoCamara.ALERTA_SOBRESALTO);
	}

	// =========================================================================
	// === CONFIGURACIÓN
	// =========================================================================

	/**
	 * Permite calibrar la altura del salto vertical antes de disparar el efecto.
	 *
	 * @param saltoPx Altura máxima en píxeles (recomendado entre 5.0 y 12.0 px).
	 */
	public void configurar(final double saltoPx) {
		this.saltoPx = Math.max(0.0, saltoPx);
	}

	// =========================================================================
	// === CÁLCULO MATEMÁTICO (CERO ASIGNACIONES EN HEAP)
	// =========================================================================

	/**
	 * Calcula el desplazamiento vertical y el micro-zoom en cada frame.
	 *
	 * @param delta Tiempo transcurrido en segundos (1.0 / 60.0 en bucle fijo).
	 */
	@Override
	protected void calcularTransformaciones(final double delta) {
		/*
		 * =====================================================================
		 * EXPLICACIÓN DIDÁCTICA: ¿CÓMO FUNCIONA ESTA FÓRMULA?
		 * --------------------------------------------------------------------- 1.
		 * PROGRESO NORMALIZADO (0.0 a 1.0): Representa el porcentaje de tiempo que ha
		 * durado el efecto. Al inicio: progreso = 0.0. Al finalizar: progreso = 1.0.
		 * 
		 * 2. LA FORMA DE CAMPANA: sin(progreso * π) - En progreso = 0.0: sin(0) = 0 -
		 * En progreso = 0.5: sin(π/2) = 1.0 (Pico máximo) - En progreso = 1.0: sin(π) =
		 * 0
		 * 
		 * 3. EL DECAIMIENTO BRUSCO: e^(-progreso * 7.0) Al multiplicar por la
		 * exponencial negativa, el pico máximo no ocurre en la mitad, sino casi al
		 * instante (al 10% del tiempo), cayendo luego muy rápido. Esto crea el efecto
		 * de "latigazo seco" humano.
		 * 
		 * 4. EL SIGNO NEGATIVO EN EL EJE Y: En gráficos de computadora 2D, el origen
		 * (0,0) está arriba a la izquierda. - Coordenadas +Y van hacia ABAJO (suelo). -
		 * Coordenadas -Y van hacia ARRIBA (techo). Por eso usamos '-saltoPx', para que
		 * la cámara salte hacia ARRIBA.
		 * =====================================================================
		 */
		final double progreso = this.tiempoTranscurrido / this.duracionSegundos;
		final double amortiguacion = Math.exp(-progreso * 7.0) * Math.sin(progreso * Math.PI);

		// 1. Salto vertical reactivo hacia arriba (-Y)
		this.offsetY = -this.saltoPx * amortiguacion * this.intensidad;

		// 2. Micro-zoom seco que acompaña al sobresalto (+8% de zoom en el pico)
		this.offsetZoom = 0.08 * amortiguacion * this.intensidad;

		// Sin desplazamiento horizontal ni rotación en este efecto
		this.offsetX = 0.0;
		this.anguloRotacion = 0.0;
	}

	// =========================================================================
	// === GETTERS
	// =========================================================================

	public double getSaltoPx() {
		return this.saltoPx;
	}
}