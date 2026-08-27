package principal.mapa.renderEntidades.camara.efectos;

/**
 * Efecto cinemático de golpe de zoom elástico (Zoom Punch / Ground Slam).
 * <p>
 * <b>Comportamiento y Física de Impacto (Game Feel):</b><br>
 * Simula el impacto vertical violento y el peso cinético en momentos de gran
 * contundencia:
 * <ul>
 * <li><b>Pisotón de Gigantes / Jefes:</b> Cuando un enemigo colosal da un paso
 * o golpea el suelo.</li>
 * <li><b>Caída de Alturas (Ground Pound):</b> Cuando el jugador cae desde una
 * plataforma o aterriza tras un salto potente.</li>
 * <li><b>Diferencia con Onda Expansiva:</b> Mientras que una explosión aleja la
 * cámara (Zoom-Out), el pisotón <b>acerca la cámara instantáneamente
 * (+Zoom)</b> simulando que el peso comprime el espacio hacia el suelo antes de
 * rebotar.</li>
 * </ul>
 * </p>
 * <p>
 * <b>Fórmula Matemática del Resorte Cosenoidal con Decaimiento:</b><br>
 * 
 * <pre>
 *   decaimiento = e^(-progreso * 5.0)
 *   oscilacion  = cos(progreso * π * 3.0)
 *   offsetZoom  = zoomMax * decaimiento * oscilacion * Intensidad
 * </pre>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public class EfectoPisoton extends EfectoCamara {

	// =========================================================================
	// === PARÁMETROS CONFIGURABLES
	// =========================================================================

	/**
	 * Magnitud máxima del acercamiento instantáneo de zoom en el momento del
	 * impacto (0.25 = +25% de zoom).
	 */
	private double zoomMax = 0.25;

	// =========================================================================
	// === CONSTRUCTOR
	// =========================================================================

	/**
	 * Instancia el efecto registrándolo en el catálogo con su tipo correspondiente.
	 */
	public EfectoPisoton() {
		super(TipoEfectoCamara.PISOTON);
	}

	// =========================================================================
	// === CONFIGURACIÓN
	// =========================================================================

	/**
	 * Permite calibrar la contundencia del golpe de zoom antes de disparar el
	 * efecto.
	 *
	 * @param zoomMax Magnitud del salto de zoom (ej: 0.15 para caídas leves, 0.40
	 *                para jefes colosales).
	 */
	public void configurar(final double zoomMax) {
		this.zoomMax = Math.max(0.0, zoomMax);
	}

	// =========================================================================
	// === CÁLCULO MATEMÁTICO (CERO ASIGNACIONES EN HEAP)
	// =========================================================================

	/**
	 * Calcula la compresión óptica y el retorno elástico amortiguado en cada frame.
	 *
	 * @param delta Tiempo transcurrido en segundos (1.0 / 60.0 en bucle fijo).
	 */
	@Override
	protected void calcularTransformaciones(final double delta) {
		/*
		 * =====================================================================
		 * EXPLICACIÓN DIDÁCTICA: ¿CÓMO FUNCIONA EL ZOOM PUNCH ELÁSTICO?
		 * --------------------------------------------------------------------- 1. MODO
		 * INFINITO (Sostenido): Si se activa como infinito, mantiene el zoom cerrado
		 * constante.
		 * 
		 * 2. EL PICO INSTANTÁNEO EN t = 0: - cos(0 * 3π) = cos(0) = 1.0 - e^(-0 * 5.0)
		 * = e^0 = 1.0 --> offsetZoom = zoomMax * 1.0 * 1.0 = zoomMax (+25% de golpe).
		 * 
		 * 3. EL REBOTE ARMÓNICO (cos(progreso * 3π)): Al multiplicar por 3π, la onda
		 * completa 1.5 ciclos de vaivén: - Salto hacia adentro (+25% de zoom en el
		 * impacto). - Rebote hacia afuera pasando ligeramente el zoom normal
		 * (contrafase). - Retorno suave al punto cero.
		 * 
		 * 4. EL FRENO EXPONENCIAL (e^-5x): Apaga las oscilaciones rápidamente para que
		 * el impacto se sienta seco, elástico y dure exactamente unos 180 a 250 ms.
		 * =====================================================================
		 */
		if (this.infinito) {
			this.offsetZoom = this.zoomMax * this.intensidad;
			this.offsetX = 0.0;
			this.offsetY = 0.0;
			this.anguloRotacion = 0.0;
			return;
		}

		final double progreso = this.tiempoTranscurrido / this.duracionSegundos;

		// 1. Curva exponencial decreciente de absorción de energía
		final double decaimiento = Math.exp(-progreso * 5.0);

		// 2. Oscilación cosenoidal armónica de rebote
		final double oscilacion = Math.cos(progreso * Math.PI * 3.0);

		// 3. Zoom elástico positivo (compresión de impacto)
		this.offsetZoom = (this.zoomMax * this.intensidad) * decaimiento * oscilacion;

		// Sin desplazamiento en X, Y ni rotación (impacto focal centrado)
		this.offsetX = 0.0;
		this.offsetY = 0.0;
		this.anguloRotacion = 0.0;
	}

	// =========================================================================
	// === GETTERS
	// =========================================================================

	public double getZoomMax() {
		return this.zoomMax;
	}
}