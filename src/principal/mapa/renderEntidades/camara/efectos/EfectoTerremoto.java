package principal.mapa.renderEntidades.camara.efectos;

import java.util.Random;

/**
 * Efecto cinemático de sacudida caótica por explosiones y sismos (Trauma Screen
 * Shake).
 * <p>
 * <b>Comportamiento y Física de Sismos (Game Feel):</b><br>
 * Implementa el estándar de la industria de desarrollo de videojuegos para
 * temblores de pantalla:
 * <ul>
 * <li><b>Decaimiento Cuadrático (Trauma^2):</b> Los temblores lineales se
 * sienten robóticos y terminan de forma brusca. Al elevar el tiempo restante al
 * cuadrado, el temblor comienza con una violencia explosiva y se disipa
 * suavemente en una micro-vibración natural.</li>
 * <li><b>Micro-Rotación Angular (±1.5°):</b> Un terremoto real no solo desplaza
 * la cámara a los lados; agregar un ligero cabeceo angular caótico multiplica
 * la sensación de caos y energía sísmica.</li>
 * </ul>
 * </p>
 * <p>
 * <b>Fórmula Matemática del Trauma de GDC:</b><br>
 * 
 * <pre>
 *   progreso       = tiempoRestante / duracionTotal  (1.0 a 0.0)
 *   Trauma         = (progreso^2) * Intensidad
 *   amplitudActual = amplitudMaxPx * Trauma
 *   Ruido          = (Random[0.0, 1.0) * 2.0) - 1.0  --> [-1.0, +1.0)
 *   offsetX        = Ruido * amplitudActual
 *   offsetY        = Ruido * amplitudActual
 *   anguloRotacion = Ruido * toRadians(rotacionMaxGrados) * Trauma
 * </pre>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public class EfectoTerremoto extends EfectoCamara {

	// =========================================================================
	// === ESTRUCTURAS Y PARÁMETROS CONFIGURABLES
	// =========================================================================

	/**
	 * Generador pseudo-aleatorio pre-asignado. Se instancia UNA sola vez en memoria
	 * para garantizar 0 GC en el Game Loop.
	 */
	private final Random random = new Random();

	/** Amplitud máxima de desplazamiento espacial en píxeles. */
	private double amplitudMaxPx = 12.0;

	/** Inclinación angular caótica máxima en grados (±1.5°). */
	private double rotacionMaxGrados = 1.5;

	// =========================================================================
	// === CONSTRUCTOR
	// =========================================================================

	/**
	 * Instancia el efecto registrándolo en el catálogo con su tipo correspondiente.
	 */
	public EfectoTerremoto() {
		super(TipoEfectoCamara.TERREMOTO);
	}

	// =========================================================================
	// === CONFIGURACIÓN
	// =========================================================================

	/**
	 * Permite calibrar la amplitud máxima del temblor antes de disparar el efecto.
	 *
	 * @param amplitudMaxPx Sacudida máxima en píxeles (ej: 4.0 para pasos, 12.0
	 *                      para explosiones, 25.0 para meteoritos).
	 */
	public void configurar(final double amplitudMaxPx) {
		this.configurar(amplitudMaxPx, 1.5);
	}

	/**
	 * Permite calibrar la amplitud espacial y la micro-rotación angular.
	 *
	 * @param amplitudMaxPx     Sacudida máxima en píxeles.
	 * @param rotacionMaxGrados Inclinación máxima en grados (ej: 0.5 a 2.5°).
	 */
	public void configurar(final double amplitudMaxPx, final double rotacionMaxGrados) {
		this.amplitudMaxPx = Math.max(0.0, amplitudMaxPx);
		this.rotacionMaxGrados = Math.max(0.0, rotacionMaxGrados);
	}

	// =========================================================================
	// === CÁLCULO MATEMÁTICO (CERO ASIGNACIONES EN HEAP)
	// =========================================================================

	/**
	 * Calcula el desplazamiento bidireccional caótico y la micro-rotación en cada
	 * frame.
	 *
	 * @param delta Tiempo transcurrido en segundos (1.0 / 60.0 en bucle fijo).
	 */
	@Override
	protected void calcularTransformaciones(final double delta) {
		/*
		 * =====================================================================
		 * EXPLICACIÓN DIDÁCTICA: ¿POR QUÉ EL TRAUMA ES CUADRÁTICO (t^2)?
		 * --------------------------------------------------------------------- 1. EL
		 * TIEMPO RESTANTE NORMALIZADO (1.0 a 0.0): - Al inicio del sismo: progreso =
		 * 1.0 (100% de fuerza restante). - A la mitad: progreso = 0.5 (50% restante). -
		 * Al finalizar: progreso = 0.0 (0% restante).
		 * 
		 * 2. LA NO-LINEALIDAD (progreso * progreso): Si la vibración decayera de forma
		 * lineal: A mitad de tiempo tendríamos el 50% de la sacudida.
		 * 
		 * Al elevarlo al cuadrado (Trauma = progreso^2): A mitad de tiempo nos queda
		 * 0.5 * 0.5 = 0.25 (25% de sacudida).
		 * 
		 * Esto hace que el impacto inicial sea masivo e imponente, pero la mayor parte
		 * de la duración del efecto se sienta como un temblor suave de acomodamiento en
		 * lugar de un movimiento rígido y molesto para los ojos.
		 * =====================================================================
		 */
		final double progreso;
		if (this.infinito) {
			progreso = 1.0;
		} else {
			final double tiempoRestante = this.duracionSegundos - this.tiempoTranscurrido;
			progreso = Math.max(0.0, tiempoRestante / this.duracionSegundos);
		}

		// Trauma cuadrático no lineal
		final double trauma = progreso * progreso * this.intensidad;
		final double amplitudActual = this.amplitudMaxPx * trauma;

		// 1. Desplazamiento caótico bidireccional en el plano X e Y (Rango [-1.0,
		// +1.0))
		this.offsetX = ((this.random.nextDouble() * 2.0) - 1.0) * amplitudActual;
		this.offsetY = ((this.random.nextDouble() * 2.0) - 1.0) * amplitudActual;

		// 2. Micro-rotación traumática asociada al impacto sísmico (convertida a
		// radianes)
		this.anguloRotacion = ((this.random.nextDouble() * 2.0) - 1.0) * Math.toRadians(this.rotacionMaxGrados)
				* trauma;

		// Sin alteración de zoom (la escala se preserva limpia)
		this.offsetZoom = 0.0;
	}

	// =========================================================================
	// === GETTERS
	// =========================================================================

	public double getAmplitudMaxPx() {
		return this.amplitudMaxPx;
	}

	public double getRotacionMaxGrados() {
		return this.rotacionMaxGrados;
	}
}