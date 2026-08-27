package principal.mapa.renderEntidades.camara.efectos;

/**
 * Efecto cinemático ambiental de ventisca, tormenta de arena y vendaval (Wind
 * Drift Sway).
 * <p>
 * <b>Comportamiento y Atmósfera Climatológica:</b><br>
 * Simula la fuerza del viento exterior e irregular en biomas abiertos:
 * <ul>
 * <li><b>Zonas Nevadas y Ventiscas:</b> La cámara es empujada por rachas de
 * aire frío.</li>
 * <li><b>Desiertos y Tormentas de Polvo:</b> Otorga peso ambiental al clima
 * adverso.</li>
 * <li><b>Cumbres Montañosas y Puentes Colgantes:</b> Acompaña el silbido del
 * viento en alturas peligrosas.</li>
 * </ul>
 * </p>
 * <p>
 * <b>Fórmula Matemática de Turbulencia Multi-Octava (Ruido 1D
 * Procedural):</b><br>
 * En lugar de usar una sola onda, superpone 3 frecuencias armónicas desfasadas
 * (octavas) con amplitudes decrecientes (1.0, 0.5, 0.25):
 * 
 * <pre>
 *   Ráfaga = sin(0.8t) + (0.5 * sin(2.3t)) + (0.25 * sin(5.7t))
 *   offsetX        = Ráfaga * fuerzaRafagaX * Intensidad
 *   offsetY        = cos(0.5t) * balanceoY * Intensidad
 *   anguloRotacion = Ráfaga * toRadians(inclinacionGrados) * Intensidad
 * </pre>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public class EfectoVientoTormenta extends EfectoCamara {

	// =========================================================================
	// === PARÁMETROS CONFIGURABLES
	// =========================================================================

	/** Fuerza máxima de empuje horizontal de las ráfagas en píxeles. */
	private double fuerzaRafagaXPx = 8.0;

	/** Balanceo vertical secundario por corrientes térmicas en píxeles. */
	private double balanceoYPx = 2.0;

	/** Inclinación angular máxima generada por la presión del viento (±0.75°). */
	private double inclinacionEolicaGrados = 0.75;

	/** Multiplicador de velocidad de las corrientes de aire (1.0 = estándar). */
	private double velocidadViento = 1.0;

	// =========================================================================
	// === CONSTRUCTOR
	// =========================================================================

	/**
	 * Instancia el efecto registrándolo en el catálogo con su tipo correspondiente.
	 */
	public EfectoVientoTormenta() {
		super(TipoEfectoCamara.VIENTO_TORMENTA);
	}

	// =========================================================================
	// === CONFIGURACIÓN
	// =========================================================================

	/**
	 * Permite calibrar la severidad del clima (útil para variar entre una brisa y
	 * un huracán).
	 *
	 * @param fuerzaRafagaXPx         Empuje lateral en píxeles (ej: 4.0 para brisa,
	 *                                15.0 para huracán).
	 * @param balanceoYPx             Balanceo vertical en píxeles.
	 * @param inclinacionEolicaGrados Inclinación angular por viento en grados.
	 * @param velocidadViento         Rapidez de las ráfagas (1.0 = estándar).
	 */
	public void configurar(final double fuerzaRafagaXPx, final double balanceoYPx, final double inclinacionEolicaGrados,
			final double velocidadViento) {
		this.fuerzaRafagaXPx = Math.max(0.0, fuerzaRafagaXPx);
		this.balanceoYPx = Math.max(0.0, balanceoYPx);
		this.inclinacionEolicaGrados = Math.max(0.0, inclinacionEolicaGrados);
		this.velocidadViento = Math.max(0.1, velocidadViento);
	}

	// =========================================================================
	// === CÁLCULO MATEMÁTICO (CERO ASIGNACIONES EN HEAP)
	// =========================================================================

	/**
	 * Calcula la turbulencia eólica multi-octava en cada frame sin generar objetos.
	 *
	 * @param delta Tiempo transcurrido en segundos (1.0 / 60.0 en bucle fijo).
	 */
	@Override
	protected void calcularTransformaciones(final double delta) {
		/*
		 * =====================================================================
		 * EXPLICACIÓN DIDÁCTICA: ¿CÓMO SE SIMULA LA TURBULENCIA REAL DEL AIRE?
		 * --------------------------------------------------------------------- En la
		 * naturaleza, el viento nunca sopla a velocidad fija ni de forma suave. Para
		 * recrear "ráfagas" impredecibles sin usar librerías pesadas:
		 * 
		 * 1. OCTAVA 1 (Viento base lento): - sin(t * 0.8) * 1.0 --> La corriente de
		 * fondo constante.
		 * 
		 * 2. OCTAVA 2 (Ráfaga intermedia): - sin(t * 2.3) * 0.5 --> La racha de viento
		 * repentina que golpea.
		 * 
		 * 3. OCTAVA 3 (Micro-turbulencia rápida): - sin(t * 5.7) * 0.25 --> El remolino
		 * rápido y caótico del aire.
		 * 
		 * Al sumar estas 3 ondas, los picos coinciden de vez en cuando creando una
		 * "super-ráfaga" que empuja con fuerza y luego se calma sola, exactamente igual
		 * que en una tormenta real.
		 * =====================================================================
		 */
		final double t = this.tiempoTranscurrido * this.velocidadViento;

		// Síntesis armónica de 3 octavas para turbulencia natural
		final double rafaga = Math.sin(t * 0.8) + (0.5 * Math.sin(t * 2.3)) + (0.25 * Math.sin(t * 5.7));

		// 1. Deriva lateral por empuje del viento (eje X)
		this.offsetX = (rafaga * this.fuerzaRafagaXPx) * this.intensidad;

		// 2. Corrientes térmicas verticales lentas (eje Y)
		this.offsetY = (Math.cos(t * 0.5) * this.balanceoYPx) * this.intensidad;

		// 3. Inclinación angular que acompaña la fuerza de la ráfaga (convertida a
		// radianes)
		this.anguloRotacion = (rafaga * Math.toRadians(this.inclinacionEolicaGrados)) * this.intensidad;

		// Sin alteración de zoom
		this.offsetZoom = 0.0;
	}

	// =========================================================================
	// === GETTERS
	// =========================================================================

	public double getFuerzaRafagaXPx() {
		return this.fuerzaRafagaXPx;
	}

	public double getBalanceoYPx() {
		return this.balanceoYPx;
	}

	public double getInclinacionEolicaGrados() {
		return this.inclinacionEolicaGrados;
	}

	public double getVelocidadViento() {
		return this.velocidadViento;
	}
}