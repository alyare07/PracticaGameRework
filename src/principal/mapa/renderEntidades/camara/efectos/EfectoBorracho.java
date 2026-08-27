package principal.mapa.renderEntidades.camara.efectos;

/**
 * Efecto cinemático de intoxicación, embriaguez y alucinación.
 * <p>
 * <b>Comportamiento y Mecánica:</b><br>
 * Simula la pérdida progresiva de equilibrio y distorsión de la percepción tras
 * consumir alcohol (cerveza, vino), pociones experimentales o ser afectado por
 * toxinas alucinógenas.
 * </p>
 * <p>
 * <b>Fórmula Matemática de los 4 Ejes Desfasados:</b><br>
 * Combina simultáneamente 4 oscilaciones trigonométricas independientes con
 * frecuencias no enteras (inconmensurables):
 * 
 * <pre>
 *   anguloRotacion = sin(t * 1.8) * rotacionMax  (Balanceo pendular angular)
 *   offsetZoom     = cos(t * 1.1) * zoomMax      (Respiración óptica en contrafase)
 *   offsetX        = sin(t * 2.2) * desvíoX      (Tambaleo lateral de piernas)
 *   offsetY        = cos(t * 1.5) * desvíoY      (Cabeceo vertical)
 * </pre>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public class EfectoBorracho extends EfectoCamara {

	// =========================================================================
	// === PARÁMETROS CONFIGURABLES
	// =========================================================================

	/** Inclinación angular máxima de la cámara en grados. */
	private double rotacionMaxGrados = 3.5;

	/** Magnitud de dilatación y contracción de zoom (0.06 = ±6% de zoom). */
	private double zoomMax = 0.06;

	/** Desplazamiento máximo horizontal por tambaleo en píxeles. */
	private double desvioXPx = 5.0;

	/** Desplazamiento máximo vertical por cabeceo en píxeles. */
	private double desvioYPx = 3.0;

	/** Factor de velocidad del estado de embriaguez (1.0 = estándar). */
	private double velocidadBorrachera = 1.0;

	// =========================================================================
	// === CONSTRUCTOR
	// =========================================================================

	/**
	 * Instancia el efecto registrándolo en el catálogo con su tipo correspondiente.
	 */
	public EfectoBorracho() {
		super(TipoEfectoCamara.BORRACHO);
	}

	// =========================================================================
	// === CONFIGURACIÓN
	// =========================================================================

	/**
	 * Permite calibrar la severidad de la intoxicación (útil para graduar entre un
	 * mareo leve y una borrachera extrema).
	 *
	 * @param rotacionMaxGrados   Inclinación máxima en grados (ej: 1.5 a 4.5°).
	 * @param zoomMax             Amplitud de respiración de zoom (ej: 0.03 a 0.08).
	 * @param desvioXPx           Tambaleo horizontal en píxeles.
	 * @param desvioYPx           Tambaleo vertical en píxeles.
	 * @param velocidadBorrachera Rapidez de las oscilaciones.
	 */
	public void configurar(final double rotacionMaxGrados, final double zoomMax, final double desvioXPx,
			final double desvioYPx, final double velocidadBorrachera) {
		this.rotacionMaxGrados = Math.max(0.0, rotacionMaxGrados);
		this.zoomMax = Math.max(0.0, zoomMax);
		this.desvioXPx = Math.max(0.0, desvioXPx);
		this.desvioYPx = Math.max(0.0, desvioYPx);
		this.velocidadBorrachera = Math.max(0.1, velocidadBorrachera);
	}

	// =========================================================================
	// === CÁLCULO MATEMÁTICO (CERO ASIGNACIONES EN HEAP)
	// =========================================================================

	/**
	 * Calcula las transformaciones de los 4 ejes combinados en cada frame.
	 *
	 * @param delta Tiempo transcurrido en segundos (1.0 / 60.0 en bucle fijo).
	 */
	@Override
	protected void calcularTransformaciones(final double delta) {
		/*
		 * =====================================================================
		 * EXPLICACIÓN DIDÁCTICA: EL SECRETO DE LAS FRECUENCIAS INCONMENSURABLES
		 * --------------------------------------------------------------------- ¿Por
		 * qué usamos números decimales raros como 1.8, 1.1, 2.2 y 1.5?
		 * 
		 * Si usáramos números enteros iguales (ej: todo multiplicado por 2.0), la
		 * rotación, el zoom y la posición coincidirían en el mismo punto exacto cada 1
		 * segundo, viéndose como un péndulo de reloj aburrido y robótico.
		 * 
		 * Al usar frecuencias decimales que no son múltiplos directos entre sí: 1. La
		 * rotación gira hacia la izquierda a un ritmo (1.8). 2. El zoom se acerca a un
		 * ritmo más lento (1.1). 3. Las piernas se tambalean a un ritmo más rápido
		 * (2.2). 4. La cabeza cabecea a otro ritmo distinto (1.5).
		 * 
		 * Estas 4 ondas tardan más de 60 segundos en volver a alinearse igual, creando
		 * una sensación de descontrol físico y náusea completamente natural.
		 * =====================================================================
		 */
		final double t = this.tiempoTranscurrido * this.velocidadBorrachera;

		// 1. Balanceo pendular suave (±3.5 grados convertidos a radianes)
		this.anguloRotacion = Math.sin(t * 1.8) * Math.toRadians(this.rotacionMaxGrados) * this.intensidad;

		// 2. Respiración de zoom lenta en contrafase (dilatación y contracción óptica)
		this.offsetZoom = Math.cos(t * 1.1) * this.zoomMax * this.intensidad;

		// 3. Tambaleo horizontal de piernas (desplazamiento lateral X)
		this.offsetX = Math.sin(t * 2.2) * this.desvioXPx * this.intensidad;

		// 4. Cabeceo vertical (desplazamiento en Y)
		this.offsetY = Math.cos(t * 1.5) * this.desvioYPx * this.intensidad;
	}

	// =========================================================================
	// === GETTERS
	// =========================================================================

	public double getRotacionMaxGrados() {
		return this.rotacionMaxGrados;
	}

	public double getZoomMax() {
		return this.zoomMax;
	}

	public double getDesvioXPx() {
		return this.desvioXPx;
	}

	public double getDesvioYPx() {
		return this.desvioYPx;
	}

	public double getVelocidadBorrachera() {
		return this.velocidadBorrachera;
	}
}