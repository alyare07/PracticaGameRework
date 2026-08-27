package principal.mapa.renderEntidades.camara.efectos;

import java.util.Random;

/**
 * Efecto cinemático de modo frenético, rabia y sobrecarga de adrenalina
 * (Berserk Mode).
 * <p>
 * <b>Comportamiento y Psicología de Juego (Game Feel):</b><br>
 * Simula el estado alterado de furia ciega donde el personaje experimenta
 * "visión de túnel":
 * <ul>
 * <li><b>Zoom de Combate Cerrado:</b> La cámara se acerca (+12%) para aumentar
 * la tensión y enfocar la atención en el combate cuerpo a cuerpo
 * inmediato.</li>
 * <li><b>Micro-temblor Nervioso:</b> Una vibración aleatoria constante de 1.5
 * px que simula la tensión muscular y la adrenalina del cuerpo (a diferencia de
 * un terremoto externo).</li>
 * </ul>
 * </p>
 * <p>
 * <b>Fórmula Matemática del Ruido Centrado:</b><br>
 * 
 * <pre>
 *   offsetZoom = zoomFuria * Intensidad
 *   Ruido      = (Random[0.0, 1.0) * 2.0) - 1.0  --> Rango exacto [-1.0, +1.0)
 *   offsetX    = Ruido * temblorPx * Intensidad
 *   offsetY    = Ruido * temblorPx * Intensidad
 * </pre>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public class EfectoBerserk extends EfectoCamara {

	// =========================================================================
	// === ESTRUCTURAS Y PARÁMETROS CONFIGURABLES
	// =========================================================================

	/**
	 * Generador pseudo-aleatorio pre-asignado. Se instancia UNA sola vez en el
	 * constructor para garantizar 0 GC en el Game Loop.
	 */
	private final Random random = new Random();

	/** Magnitud de acercamiento de cámara en modo furia (0.12 = +12% de zoom). */
	private double zoomFuria = 0.12;

	/** Amplitud máxima del micro-temblor muscular en píxeles. */
	private double temblorNerviosoPx = 1.5;

	// =========================================================================
	// === CONSTRUCTOR
	// =========================================================================

	/**
	 * Instancia el efecto registrándolo en el catálogo con su tipo correspondiente.
	 */
	public EfectoBerserk() {
		super(TipoEfectoCamara.BERSERK);
	}

	// =========================================================================
	// === CONFIGURACIÓN
	// =========================================================================

	/**
	 * Permite calibrar la agresividad del zoom y la intensidad del temblor
	 * nervioso.
	 *
	 * @param zoomFuria         Aumento de zoom relativo (ej: 0.10 a 0.20).
	 * @param temblorNerviosoPx Amplitud de vibración en píxeles (ej: 1.0 a 2.5 px).
	 */
	public void configurar(final double zoomFuria, final double temblorNerviosoPx) {
		this.zoomFuria = Math.max(0.0, zoomFuria);
		this.temblorNerviosoPx = Math.max(0.0, temblorNerviosoPx);
	}

	// =========================================================================
	// === CÁLCULO MATEMÁTICO (CERO ASIGNACIONES EN HEAP)
	// =========================================================================

	/**
	 * Calcula el acercamiento focal sostenido y el ruido nervioso en cada frame.
	 *
	 * @param delta Tiempo transcurrido en segundos (1.0 / 60.0 en bucle fijo).
	 */
	@Override
	protected void calcularTransformaciones(final double delta) {
		/*
		 * =====================================================================
		 * EXPLICACIÓN DIDÁCTICA: ¿CÓMO CENTRAMOS EL RUIDO ALEATORIO?
		 * --------------------------------------------------------------------- 1.
		 * random.nextDouble() devuelve un número decimal entre 0.0 y 1.0 (siempre
		 * positivo). Si usáramos esto directamente, la cámara solo temblaría hacia la
		 * derecha y hacia abajo (+X, +Y), desplazando la pantalla de su lugar.
		 * 
		 * 2. Al multiplicar por 2.0 obtenemos un rango de [0.0 a 2.0).
		 * 
		 * 3. Al restar 1.0 obtenemos un rango perfectamente centrado de [-1.0 a +1.0).
		 * - Si sale 0.0 -> (0.0 * 2) - 1 = -1.0 (Sacude a la izquierda) - Si sale 0.5
		 * -> (0.5 * 2) - 1 = 0.0 (Sin desplazamiento) - Si sale 1.0 -> (1.0 * 2) - 1 =
		 * +1.0 (Sacude a la derecha)
		 * 
		 * De esta forma, la cámara vibra exactamente alrededor del jugador sin perder
		 * nunca el centrado de la pantalla.
		 * =====================================================================
		 */

		// 1. Zoom de combate sostenido (visión de túnel)
		this.offsetZoom = this.zoomFuria * this.intensidad;

		// 2. Ruido blanco pseudo-aleatorio de alta frecuencia centrado en cero
		this.offsetX = ((this.random.nextDouble() * 2.0) - 1.0) * this.temblorNerviosoPx * this.intensidad;
		this.offsetY = ((this.random.nextDouble() * 2.0) - 1.0) * this.temblorNerviosoPx * this.intensidad;

		// Sin rotación angular en modo Berserk
		this.anguloRotacion = 0.0;
	}

	// =========================================================================
	// === GETTERS
	// =========================================================================

	public double getZoomFuria() {
		return this.zoomFuria;
	}

	public double getTemblorNerviosoPx() {
		return this.temblorNerviosoPx;
	}
}