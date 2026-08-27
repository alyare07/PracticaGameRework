package principal.mapa.renderEntidades.camara.efectos;

/**
 * Efecto cinemático de inercia y arrastre elástico durante esquivas rápidas
 * (Dash Lag).
 * <p>
 * <b>Comportamiento y Física de Juego (Game Feel):</b><br>
 * Simula la 1ª Ley de Newton (Inercia): cuando un personaje se impulsa
 * bruscamente hacia adelante a gran velocidad, la cámara "pesa" y se retrasa
 * momentáneamente en la <b>dirección opuesta</b> al movimiento antes de
 * recuperarse elásticamente:
 * <ul>
 * <li><b>Sensación de Velocidad Explosiva:</b> Al tirar de la vista hacia atrás
 * mientras el sprite del jugador avanza, la distancia visual aparente se
 * duplica, haciendo que el dash se sienta potente y ágil.</li>
 * <li><b>Normalización 2D Omnidireccional:</b> Garantiza que el arrastre de la
 * cámara mida exactamente lo mismo en diagonales que en los 4 puntos
 * cardinales.</li>
 * </ul>
 * </p>
 * <p>
 * <b>Fórmula Matemática del Resorte Amortiguado Direccional:</b><br>
 * 
 * <pre>
 *   VectorUnitario = (dirX, dirY) / ||Vector||
 *   resorte        = sin(progreso * π) * e^(-progreso * 2.5)
 *   magnitud       = -distanciaLagPx * resorte * Intensidad
 *   offsetX        = VectorUnitario.x * magnitud
 *   offsetY        = VectorUnitario.y * magnitud
 * </pre>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public class EfectoInerciaDash extends EfectoCamara {

	// =========================================================================
	// === PARÁMETROS VECTORIALES CONFIGURABLES
	// =========================================================================

	/** Componente horizontal del vector director normalizado del dash. */
	private double dirX = 1.0;

	/** Componente vertical del vector director normalizado del dash. */
	private double dirY = 0.0;

	/** Distancia máxima en píxeles que la cámara se retrasa respecto al jugador. */
	private double distanciaLagPx = 18.0;

	// =========================================================================
	// === CONSTRUCTOR
	// =========================================================================

	/**
	 * Instancia el efecto registrándolo en el catálogo con su tipo correspondiente.
	 */
	public EfectoInerciaDash() {
		super(TipoEfectoCamara.INERCIA_DASH);
	}

	// =========================================================================
	// === CONFIGURACIÓN Y NORMALIZACIÓN VECTORIAL
	// =========================================================================

	/**
	 * Configura la dirección del desplazamiento y la fuerza del retraso inercial.
	 * <p>
	 * <b>¿Por qué normalizamos el vector? (Explicación para novatos):</b><br>
	 * Si el jugador esquiva en diagonal (ej: derecha + arriba, dirX=1, dirY=-1), la
	 * longitud de ese vector es $\sqrt{1^2 + (-1)^2} = 1.414$ ($41\%$ más largo).
	 * Al dividir por la hipotenusa (módulo), convertimos el vector en un <i>Vector
	 * Unitario</i> (longitud exacta = 1.0). De esta forma, esquivar en diagonal
	 * produce exactamente el mismo arrastre de píxeles que esquivar en línea recta.
	 * </p>
	 *
	 * @param dirX           Componente horizontal del movimiento (ej: -1 izquierda,
	 *                       +1 derecha).
	 * @param dirY           Componente vertical del movimiento (ej: -1 arriba, +1
	 *                       abajo).
	 * @param distanciaLagPx Píxeles de arrastre de la cámara (recomendado 12.0 a
	 *                       24.0 px).
	 */
	public void configurarDireccion(final double dirX, final double dirY, final double distanciaLagPx) {
		// Math.hypot calcula sqrt(x^2 + y^2) con precisión sin desbordamiento numérico
		final double hipotenusa = Math.hypot(dirX, dirY);

		if (hipotenusa > 0.0001) {
			this.dirX = dirX / hipotenusa;
			this.dirY = dirY / hipotenusa;
		} else {
			// Dirección por defecto hacia la derecha si el vector recibido es nulo (0,0)
			this.dirX = 1.0;
			this.dirY = 0.0;
		}

		this.distanciaLagPx = Math.max(0.0, distanciaLagPx);
	}

	// =========================================================================
	// === CÁLCULO MATEMÁTICO (CERO ASIGNACIONES EN HEAP)
	// =========================================================================

	/**
	 * Calcula el arrastre inercial y el retorno elástico en cada frame.
	 *
	 * @param delta Tiempo transcurrido en segundos (1.0 / 60.0 en bucle fijo).
	 */
	@Override
	protected void calcularTransformaciones(final double delta) {
		/*
		 * =====================================================================
		 * EXPLICACIÓN DIDÁCTICA: ¿POR QUÉ LA MAGNITUD ES NEGATIVA?
		 * --------------------------------------------------------------------- 1. EL
		 * SIGNO NEGATIVO: Si el jugador se mueve hacia la DERECHA (+dirX), queremos que
		 * la cámara se quede rezagada hacia la IZQUIERDA (-dirX). Al multiplicar por
		 * '-distanciaLagPx', invertimos la dirección del vector.
		 * 
		 * 2. LA FORMA DE RESORTE (sin * e^-x): - sin(progreso * π): Comienza en 0, sube
		 * suavemente a 1 en la mitad y baja a 0. - e^(-progreso * 2.5): Frena la
		 * segunda mitad del movimiento para que la cámara no se pase de largo y se
		 * acople suavemente al jugador cuando este frena al terminar la esquiva.
		 * =====================================================================
		 */
		final double progreso = this.tiempoTranscurrido / this.duracionSegundos;

		// Curva de resorte elástico sub-amortiguado
		final double resorte = Math.sin(progreso * Math.PI) * Math.exp(-progreso * 2.5);

		// Magnitud escalar invertida respecto a la dirección de movimiento
		final double magnitud = -this.distanciaLagPx * resorte * this.intensidad;

		// Proyección sobre los ejes X e Y de la pantalla
		this.offsetX = this.dirX * magnitud;
		this.offsetY = this.dirY * magnitud;

		// Sin alteración de zoom ni rotación durante el dash
		this.offsetZoom = 0.0;
		this.anguloRotacion = 0.0;
	}

	// =========================================================================
	// === GETTERS
	// =========================================================================

	public double getDirX() {
		return this.dirX;
	}

	public double getDirY() {
		return this.dirY;
	}

	public double getDistanciaLagPx() {
		return this.distanciaLagPx;
	}
}