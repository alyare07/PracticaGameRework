package principal.mapa.renderEntidades.camara.efectos;

/**
 * Efecto cinemático de concentración, modo apuntado y tiempo bala
 * (Bullet-Time).
 * <p>
 * <b>Comportamiento y Cinematografía (Game Feel):</b><br>
 * Simula el estado de máxima concentración del personaje al realizar acciones
 * de alta precisión (como tensar la cuerda de un arco, apuntar una habilidad a
 * distancia, o activar una ralentización temporal tras una esquiva perfecta):
 * <ul>
 * <li><b>Zoom Táctico (+20%):</b> Acerca la visión para que el jugador pueda
 * calcular con precisión la trayectoria de disparo y la posición del
 * enemigo.</li>
 * <li><b>Plano Holandés (Dutch Angle de 1.5°):</b> Aplica una inclinación
 * angular sutil muy utilizada en cine de acción para transmitir tensión,
 * dramatismo y foco total en el objetivo.</li>
 * </ul>
 * </p>
 * <p>
 * <b>Fórmula Matemática:</b><br>
 * 
 * <pre>
 *   offsetZoom     = zoomEnfoque * Intensidad
 *   anguloRotacion = toRadians(inclinacionGrados) * Intensidad
 *   offsetX        = 0.0
 *   offsetY        = 0.0
 * </pre>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public class EfectoCamaraLentaEnfoque extends EfectoCamara {

	// =========================================================================
	// === PARÁMETROS CONFIGURABLES
	// =========================================================================

	/**
	 * Magnitud de acercamiento óptico durante el apuntado (0.20 = +20% de zoom).
	 */
	private double zoomEnfoque = 0.20;

	/**
	 * Inclinación angular cinematográfica expresada en grados (Dutch Angle).
	 */
	private double inclinacionGrados = 1.5;

	// =========================================================================
	// === CONSTRUCTOR
	// =========================================================================

	/**
	 * Instancia el efecto registrándolo en el catálogo con su tipo correspondiente.
	 */
	public EfectoCamaraLentaEnfoque() {
		super(TipoEfectoCamara.CAMARA_LENTA_ENFOQUE);
	}

	// =========================================================================
	// === CONFIGURACIÓN
	// =========================================================================

	/**
	 * Permite calibrar la profundidad del zoom y la inclinación angular de la
	 * lente.
	 *
	 * @param zoomEnfoque       Magnitud del zoom táctico (ej: 0.15 a 0.35).
	 * @param inclinacionGrados Inclinación angular en grados (ej: 1.0 a 2.5°).
	 */
	public void configurar(final double zoomEnfoque, final double inclinacionGrados) {
		this.zoomEnfoque = Math.max(0.0, zoomEnfoque);
		this.inclinacionGrados = inclinacionGrados;
	}

	// =========================================================================
	// === CÁLCULO MATEMÁTICO (CERO ASIGNACIONES EN HEAP)
	// =========================================================================

	/**
	 * Aplica el zoom de combate y la inclinación cinematográfica en cada frame.
	 *
	 * @param delta Tiempo transcurrido en segundos (1.0 / 60.0 en bucle fijo).
	 */
	@Override
	protected void calcularTransformaciones(final double delta) {
		/*
		 * =====================================================================
		 * EXPLICACIÓN DIDÁCTICA: EL PLANO HOLANDÉS Y LAS UNIDADES ANGULARES
		 * --------------------------------------------------------------------- 1. ¿POR
		 * QUÉ Math.toRadians()? Los seres humanos pensamos en GRADOS (ej: un giro de
		 * 90° o inclinar 1.5° la cabeza). Sin embargo, las funciones gráficas de Java
		 * 2D (como Graphics2D.rotate()) exigen RADIANES. - Fórmula: Radianes = Grados *
		 * (π / 180) - toRadians(1.5) nos da exactamente 0.02618 radianes.
		 * 
		 * 2. EL PLANO HOLANDÉS (DUTCH ANGLE): En el cine y los cómics, cuando un
		 * francotirador o un pistolero está a punto de disparar, la cámara nunca se
		 * queda plana: se inclina apenas 1 o 2 grados. Ese micro-giro altera sutilmente
		 * el horizonte y le comunica al cerebro del jugador que está en una postura de
		 * tiro.
		 * 
		 * 3. MODO CONTINUO (HOLD TO AIM): Este efecto está diseñado para mantenerse
		 * activo de forma infinita mientras el jugador mantenga presionado el botón
		 * derecho del ratón o la tecla de apuntar, y desactivarse al soltarlo.
		 * =====================================================================
		 */

		// 1. Zoom táctico sostenido sobre el área de combate
		this.offsetZoom = this.zoomEnfoque * this.intensidad;

		// 2. Inclinación angular cinematográfica fija convertida a radianes
		this.anguloRotacion = Math.toRadians(this.inclinacionGrados) * this.intensidad;

		// Sin desplazamiento en X o Y (la cámara se mantiene fija sobre el centro de
		// mira)
		this.offsetX = 0.0;
		this.offsetY = 0.0;
	}

	// =========================================================================
	// === GETTERS
	// =========================================================================

	public double getZoomEnfoque() {
		return this.zoomEnfoque;
	}

	public double getInclinacionGrados() {
		return this.inclinacionGrados;
	}
}