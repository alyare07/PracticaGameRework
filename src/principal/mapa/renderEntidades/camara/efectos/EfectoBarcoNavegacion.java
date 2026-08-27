package principal.mapa.renderEntidades.camara.efectos;

/**
 * Efecto cinemático ambiental de oleaje marítimo y navegación aérea.
 * <p>
 * <b>Comportamiento y Mecánica:</b><br>
 * Simula la física de flotación sobre el agua o corrientes de aire para cuando
 * el jugador viaja en balsa, barco en altamar, galeón o dirigible.
 * </p>
 * <p>
 * <b>Fórmula Matemática de Hidrodinámica 2D:</b><br>
 * Combina los 3 movimientos náuticos principales (<i>Roll</i>, <i>Heave</i> y
 * <i>Sway</i>) mediante ondas sinusoidales desfasadas:
 * 
 * <pre>
 *   anguloRotacion = sin(t * 1.3) * inclinacionMax  (Roll / Babor-Estribor)
 *   offsetY        = cos(t * 1.6) * alturaOleaje    (Heave / Cresta-Valle de ola)
 *   offsetX        = sin(t * 0.8) * derivaLateral   (Sway / Corriente de agua)
 * </pre>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public class EfectoBarcoNavegacion extends EfectoCamara {

	// =========================================================================
	// === PARÁMETROS CONFIGURABLES
	// =========================================================================

	/** Inclinación angular máxima del barco en grados (Roll). */
	private double inclinacionMaxGrados = 2.5;

	/** Altura máxima del sube y baja de las olas en píxeles (Heave). */
	private double alturaOleajePx = 4.5;

	/** Deriva horizontal por corrientes de marea en píxeles (Sway). */
	private double derivaLateralPx = 2.0;

	/** Multiplicador de velocidad de la marea (1.0 = estándar). */
	private double velocidadMarea = 1.0;

	// =========================================================================
	// === CONSTRUCTOR
	// =========================================================================

	/**
	 * Instancia el efecto registrándolo en el catálogo con su tipo correspondiente.
	 */
	public EfectoBarcoNavegacion() {
		super(TipoEfectoCamara.BARCO_NAVEGACION);
	}

	// =========================================================================
	// === CONFIGURACIÓN
	// =========================================================================

	/**
	 * Permite calibrar la intensidad del oleaje (útil para variar entre aguas
	 * calmas y tormentas).
	 *
	 * @param inclinacionMaxGrados Inclinación lateral en grados (ej: 1.5 en lago,
	 *                             4.0 en tormenta).
	 * @param alturaOleajePx       Altura vertical del oleaje en píxeles.
	 * @param derivaLateralPx      Empuje lateral de la corriente en píxeles.
	 * @param velocidadMarea       Rapidez del oleaje (1.0 = normal).
	 */
	public void configurar(final double inclinacionMaxGrados, final double alturaOleajePx, final double derivaLateralPx,
			final double velocidadMarea) {
		this.inclinacionMaxGrados = Math.max(0.0, inclinacionMaxGrados);
		this.alturaOleajePx = Math.max(0.0, alturaOleajePx);
		this.derivaLateralPx = Math.max(0.0, derivaLateralPx);
		this.velocidadMarea = Math.max(0.1, velocidadMarea);
	}

	// =========================================================================
	// === CÁLCULO MATEMÁTICO (CERO ASIGNACIONES EN HEAP)
	// =========================================================================

	/**
	 * Calcula las transformaciones náuticas desfasadas en cada frame.
	 *
	 * @param delta Tiempo transcurrido en segundos (1.0 / 60.0 en bucle fijo).
	 */
	@Override
	protected void calcularTransformaciones(final double delta) {
		/*
		 * =====================================================================
		 * EXPLICACIÓN DIDÁCTICA: FÍSICA NÁUTICA SIMULADA EN 2D
		 * --------------------------------------------------------------------- En la
		 * vida real, un barco en el agua no se mueve en una sola dirección, sino que
		 * experimenta 3 fuerzas combinadas:
		 * 
		 * 1. EL BALANCEO LATERAL (ROLL / Babor a Estribor): - Formula: sin(t * 1.3) *
		 * inclinacionMax - Inclina la cámara a la izquierda y derecha suavemente como
		 * si el casco cortara el agua.
		 * 
		 * 2. EL SUBE Y BAJA DE OLAS (HEAVE / Crestas y Valles): - Formula: cos(t * 1.6)
		 * * alturaOleaje - Usamos COSENO porque está desfasado 90° respecto al SENO:
		 * cuando el barco llega a su inclinación máxima, la ola comienza a bajar,
		 * imitando la física de flotabilidad real.
		 * 
		 * 3. LA DERIVA DE CORRIENTE (SWAY / Empuje lateral): - Formula: sin(t * 0.8) *
		 * derivaLateral - Una frecuencia más lenta (0.8) simula que las corrientes
		 * marinas mueven el barco de lado a lado con calma.
		 * =====================================================================
		 */
		final double t = this.tiempoTranscurrido * this.velocidadMarea;

		// 1. Inclinación pendular de babor a estribor (±2.5 grados convertidos a
		// radianes)
		this.anguloRotacion = Math.sin(t * 1.3) * Math.toRadians(this.inclinacionMaxGrados) * this.intensidad;

		// 2. Altura de las olas en el eje vertical (Heave en contrafase)
		this.offsetY = Math.cos(t * 1.6) * this.alturaOleajePx * this.intensidad;

		// 3. Deriva lateral lenta por corriente marina (Sway)
		this.offsetX = Math.sin(t * 0.8) * this.derivaLateralPx * this.intensidad;

		// Sin alteración de zoom en navegación (mantiene la escala 100% natural)
		this.offsetZoom = 0.0;
	}

	// =========================================================================
	// === GETTERS
	// =========================================================================

	public double getInclinacionMaxGrados() {
		return this.inclinacionMaxGrados;
	}

	public double getAlturaOleajePx() {
		return this.alturaOleajePx;
	}

	public double getDerivaLateralPx() {
		return this.derivaLateralPx;
	}

	public double getVelocidadMarea() {
		return this.velocidadMarea;
	}
}