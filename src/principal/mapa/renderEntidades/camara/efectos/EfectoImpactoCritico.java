package principal.mapa.renderEntidades.camara.efectos;

import java.util.Random;

/**
 * Efecto cinemático de impacto crítico, contraataque perfecto y remate (Hitstop
 * Tremor).
 * <p>
 * <b>Comportamiento y Psicología de Combate (Game Feel):</b><br>
 * Simula la técnica clásica de <i>Hitstop</i> o congelación de impacto
 * utilizada en juegos de acción (como <i>Monster Hunter</i>, <i>Hollow
 * Knight</i> o <i>Street Fighter</i>):
 * <ul>
 * <li><b>Sensación de Peso y Masa:</b> Cuando una espada pesada corta una
 * armadura gruesa, el golpe no pasa de largo instantáneamente; la cámara "se
 * clava" y vibra durante una fracción microscópica de segundo ($60 \text{ a }
 * 120\text{ ms}$).</li>
 * <li><b>Micro-Zoom y Jitter de Alta Frecuencia:</b> Un acercamiento
 * instantáneo (+15%) acompañado de un temblor rápido de 2 px que decae
 * velozmente a cero.</li>
 * </ul>
 * </p>
 * <p>
 * <b>Fórmula Matemática del Decaimiento Lineal Rápido:</b><br>
 * 
 * <pre>
 *   factor     = 1.0 - progreso  (1.0 en t=0 hasta 0.0 en t=duracion)
 *   offsetZoom = microZoom * factor * Intensidad
 *   Ruido      = (Random[0.0, 1.0) * 2.0) - 1.0  --> [-1.0, +1.0)
 *   offsetX    = Ruido * vibracionPx * factor * Intensidad
 *   offsetY    = Ruido * vibracionPx * factor * Intensidad
 * </pre>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public class EfectoImpactoCritico extends EfectoCamara {

	// =========================================================================
	// === ESTRUCTURAS Y PARÁMETROS CONFIGURABLES
	// =========================================================================

	/**
	 * Generador pseudo-aleatorio pre-asignado. Se instancia UNA sola vez para
	 * evitar recolección de basura (Zero-GC).
	 */
	private final Random random = new Random();

	/**
	 * Magnitud del micro-zoom instantáneo en el momento del golpe (0.15 = +15%).
	 */
	private double microZoom = 0.15;

	/** Amplitud máxima de vibración por impacto en píxeles. */
	private double vibracionPx = 2.0;

	// =========================================================================
	// === CONSTRUCTOR
	// =========================================================================

	/**
	 * Instancia el efecto registrándolo en el catálogo con su tipo correspondiente.
	 */
	public EfectoImpactoCritico() {
		super(TipoEfectoCamara.IMPACTO_CRITICO);
	}

	// =========================================================================
	// === CONFIGURACIÓN
	// =========================================================================

	/**
	 * Permite calibrar la fuerza del golpe crítico antes de disparar el efecto.
	 *
	 * @param microZoom   Aumento súbito de zoom (ej: 0.10 para dagas, 0.25 para
	 *                    mazas pesadas).
	 * @param vibracionPx Amplitud de vibración en píxeles (ej: 1.5 a 3.0 px).
	 */
	public void configurar(final double microZoom, final double vibracionPx) {
		this.microZoom = Math.max(0.0, microZoom);
		this.vibracionPx = Math.max(0.0, vibracionPx);
	}

	// =========================================================================
	// === CÁLCULO MATEMÁTICO (CERO ASIGNACIONES EN HEAP)
	// =========================================================================

	/**
	 * Calcula el micro-zoom decreciente y el temblor de alta frecuencia en cada
	 * frame.
	 *
	 * @param delta Tiempo transcurrido en segundos (1.0 / 60.0 en bucle fijo).
	 */
	@Override
	protected void calcularTransformaciones(final double delta) {
		/*
		 * =====================================================================
		 * EXPLICACIÓN DIDÁCTICA: ¿POR QUÉ UN DECAIMIENTO LINEAL (1.0 - PROGRESO)?
		 * --------------------------------------------------------------------- 1. EL
		 * FACTOR INVERSO: - Al inicio exacto del golpe (progreso = 0.0): factor = 1.0 -
		 * 0.0 = 1.0 --> Máxima vibración y zoom instantáneo.
		 * 
		 * - A la mitad del golpe (progreso = 0.5): factor = 1.0 - 0.5 = 0.5 --> La
		 * vibración se reduce al 50%.
		 * 
		 * - Al finalizar (progreso = 1.0): factor = 1.0 - 1.0 = 0.0 --> Se apaga
		 * suavemente sin cortes abruptos.
		 * 
		 * 2. DURACIONES ULTRA-CORTAS: Este efecto debe durar entre 60 ms (unos 3
		 * frames) y 120 ms (unos 7 frames). Si durara más tiempo, el jugador sentiría
		 * que el juego "se trabó"; pero al durar menos de una décima de segundo, el
		 * cerebro lo interpreta como la resistencia física del arma chocando contra el
		 * enemigo.
		 * =====================================================================
		 */
		final double progreso = this.tiempoTranscurrido / this.duracionSegundos;
		final double factor = Math.max(0.0, 1.0 - progreso);

		// 1. Salto de zoom decreciente que enfoca el punto de contacto del golpe
		this.offsetZoom = this.microZoom * factor * this.intensidad;

		// 2. Micro-vibración nerviosa de alta frecuencia centrada en el eje (Jitter)
		this.offsetX = ((this.random.nextDouble() * 2.0) - 1.0) * this.vibracionPx * factor * this.intensidad;
		this.offsetY = ((this.random.nextDouble() * 2.0) - 1.0) * this.vibracionPx * factor * this.intensidad;

		// Sin rotación angular (mantiene el plano de corte recto)
		this.anguloRotacion = 0.0;
	}

	// =========================================================================
	// === GETTERS
	// =========================================================================

	public double getMicroZoom() {
		return this.microZoom;
	}

	public double getVibracionPx() {
		return this.vibracionPx;
	}
}