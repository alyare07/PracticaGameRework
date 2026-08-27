package principal.mapa.renderEntidades.camara.efectos;

/**
 * Efecto cinemático de caída en agujeros, precipicios y vórtices dimensionales.
 * <p>
 * <b>Comportamiento y Cinemática:</b><br>
 * Simula la pérdida de sustentación y succión gravitacional cuando el personaje
 * cae en una trampa de foso sin fondo, es tragado por un remolino mágico o
 * muere al caer al abismo (estilo <i>The Legend of Zelda</i>).
 * </p>
 * <p>
 * <b>Fórmula Matemática (Aceleración Angular Cuadrática y Contracción):</b><br>
 * Utiliza una curva de aceleración no lineal (<i>Ease-In</i>) para los giros y
 * una reducción constante del campo de visión:
 * 
 * <pre>
 *   progreso       = tiempo / duracionTotal  (0.0 a 1.0)
 *   anguloRotacion = (progreso^2) * (2π * girosTotales) * Intensidad
 *   offsetZoom     = -contraccionZoomMax * progreso * Intensidad
 * </pre>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public class EfectoCaidaAbismo extends EfectoCamara {

	// =========================================================================
	// === PARÁMETROS CONFIGURABLES
	// =========================================================================

	/**
	 * Cantidad total de vueltas completas de 360° que dará la cámara durante la
	 * caída.
	 */
	private double girosTotales = 2.0;

	/** Reducción máxima de zoom hacia el fondo del abismo (0.75 = aleja un 75%). */
	private double contraccionZoomMax = 0.75;

	// =========================================================================
	// === CONSTRUCTOR
	// =========================================================================

	/**
	 * Instancia el efecto registrándolo en el catálogo con su tipo correspondiente.
	 */
	public EfectoCaidaAbismo() {
		super(TipoEfectoCamara.CAIDA_ABISMO);
	}

	// =========================================================================
	// === CONFIGURACIÓN
	// =========================================================================

	/**
	 * Permite calibrar la cantidad de vueltas de giro y qué tanto se encoge el
	 * mundo hacia la profundidad.
	 *
	 * @param girosTotales       Vueltas de 360° a dar (ej: 1.5 a 3.0 vueltas).
	 * @param contraccionZoomMax Porcentaje de alejamiento (ej: 0.60 a 0.85).
	 */
	public void configurar(final double girosTotales, final double contraccionZoomMax) {
		this.girosTotales = Math.max(0.5, girosTotales);
		this.contraccionZoomMax = Math.max(0.1, Math.min(0.95, contraccionZoomMax));
	}

	// =========================================================================
	// === CÁLCULO MATEMÁTICO (CERO ASIGNACIONES EN HEAP)
	// =========================================================================

	/**
	 * Calcula la espiral acelerada y el alejamiento hacia el vacío en cada frame.
	 *
	 * @param delta Tiempo transcurrido en segundos (1.0 / 60.0 en bucle fijo).
	 */
	@Override
	protected void calcularTransformaciones(final double delta) {
		/*
		 * =====================================================================
		 * EXPLICACIÓN DIDÁCTICA: ¿POR QUÉ LA ROTACIÓN SE MULTIPLICA AL CUADRADO?
		 * --------------------------------------------------------------------- 1. LA
		 * ACELERACIÓN ANGULAR (progreso * progreso): Si usáramos la velocidad lineal
		 * (progreso simple), la cámara giraría a la misma velocidad de principio a fin,
		 * viéndose como un disco plano.
		 * 
		 * Al elevar el progreso al cuadrado (Ease-In): - Al 10% del tiempo: 0.1 * 0.1 =
		 * 0.01 (Comienza girando muy lento) - Al 50% del tiempo: 0.5 * 0.5 = 0.25
		 * (Empieza a acelerar) - Al 90% del tiempo: 0.9 * 0.9 = 0.81 (Gira a máxima
		 * velocidad)
		 * 
		 * Esto imita perfectamente la física de un vórtice o agujero negro: a medida
		 * que caes hacia el centro, la fuerza centrípeta te hace girar cada vez más
		 * rápido.
		 * 
		 * 2. LA CONTRACCIÓN DE ZOOM (El efecto de profundidad 3D): - Formula:
		 * -contraccionZoomMax * progreso - Al restar zoom progresivamente, el mundo se
		 * hace cada vez más pequeño hacia el centro de la pantalla, simulando que el
		 * jugador se hunde hacia las profundidades de la tierra.
		 * =====================================================================
		 */
		final double progreso = Math.min(1.0, this.tiempoTranscurrido / this.duracionSegundos);

		// 1. Giro acelerado acumulativo en radianes (1 vuelta completa = 2*PI radianes
		// = 360°)
		final double radianesTotales = Math.PI * 2.0 * this.girosTotales;
		this.anguloRotacion = (progreso * progreso) * radianesTotales * this.intensidad;

		// 2. Alejamiento progresivo hacia el fondo del pozo (Zoom negativo)
		this.offsetZoom = -this.contraccionZoomMax * progreso * this.intensidad;

		// El giro y zoom están perfectamente anclados al centro (sin desvío en X o Y)
		this.offsetX = 0.0;
		this.offsetY = 0.0;
	}

	// =========================================================================
	// === GETTERS
	// =========================================================================

	public double getGirosTotales() {
		return this.girosTotales;
	}

	public double getContraccionZoomMax() {
		return this.contraccionZoomMax;
	}
}