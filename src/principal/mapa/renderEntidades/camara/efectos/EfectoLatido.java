package principal.mapa.renderEntidades.camara.efectos;

/**
 * Efecto cinemático de pulso cardíaco rítmico (Heartbeat Pulse).
 * <p>
 * <b>Comportamiento y Fisiología de Juego (Game Feel):</b><br>
 * Simula el ritmo cardíaco biológico acelerado para momentos de máxima tensión:
 * <ul>
 * <li><b>Poca Vida (HP Crítico):</b> Cuando la salud cae por debajo del 20%, la
 * cámara late rítmicamente para advertir del peligro inminente.</li>
 * <li><b>Terror y Sigilo Tenso:</b> Al aproximarse a una criatura letal o
 * esconderse en la oscuridad.</li>
 * </ul>
 * </p>
 * <p>
 * <b>Fórmula Matemática del Doble Latido ("Lub-Dub"):</b><br>
 * En lugar de una onda senoidal simple (que se siente monótona y mecánica),
 * suma un segundo armónico al doble de frecuencia con rectificación de media
 * onda:
 * 
 * <pre>
 *   t          = tiempo * frecuencia
 *   PrimerPum  = max(0.0, sin(t))
 *   SegundoPum = 0.5 * max(0.0, sin(2t))
 *   offsetZoom = (PrimerPum + SegundoPum) * amplitudZoom * Intensidad
 * </pre>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public class EfectoLatido extends EfectoCamara {

	// =========================================================================
	// === PARÁMETROS CONFIGURABLES
	// =========================================================================

	/** Frecuencia de los latidos (velocidad del ritmo cardíaco en Hz). */
	private double frecuencia = 4.5;

	/** Magnitud del acercamiento de zoom en el pico del latido (+7.5% de zoom). */
	private double amplitudZoom = 0.075;

	// =========================================================================
	// === CONSTRUCTOR
	// =========================================================================

	/**
	 * Instancia el efecto registrándolo en el catálogo con su tipo correspondiente.
	 */
	public EfectoLatido() {
		super(TipoEfectoCamara.LATIDO);
	}

	// =========================================================================
	// === CONFIGURACIÓN
	// =========================================================================

	/**
	 * Permite calibrar la rapidez y la fuerza del pulso cardíaco.
	 *
	 * @param frecuencia   Rapidez del corazón (ej: 3.5 para pulso calmo, 6.0 para
	 *                     taquicardia extrema).
	 * @param amplitudZoom Aumento máximo de zoom (ej: 0.05 a 0.10).
	 */
	public void configurar(final double frecuencia, final double amplitudZoom) {
		this.frecuencia = Math.max(0.1, frecuencia);
		this.amplitudZoom = Math.max(0.0, amplitudZoom);
	}

	// =========================================================================
	// === CÁLCULO MATEMÁTICO (CERO ASIGNACIONES EN HEAP)
	// =========================================================================

	/**
	 * Calcula el doble pulso cardíaco en cada frame sin generar basura en memoria.
	 *
	 * @param delta Tiempo transcurrido en segundos (1.0 / 60.0 en bucle fijo).
	 */
	@Override
	protected void calcularTransformaciones(final double delta) {
		/*
		 * =====================================================================
		 * EXPLICACIÓN DIDÁCTICA: ¿CÓMO SE LOGRA EL SONIDO/MOVIMIENTO "LUB-DUB"?
		 * --------------------------------------------------------------------- 1. LA
		 * ONDA SIMPLE NO ALCANZA: Si usáramos solo sin(t), la pantalla se acercaría y
		 * alejaría como un columpio aburrido ("bum... bum... bum...").
		 * 
		 * 2. LA RECTIFICACIÓN DE MEDIA ONDA (Math.max(0.0, sin)): Una función seno pasa
		 * la mitad del tiempo en valores positivos y la otra mitad en negativos (-1 a
		 * +1). Al usar Math.max(0.0, sin), cortamos la parte negativa a 0. Esto crea
		 * los momentos de silencio y descanso entre latido y latido.
		 * 
		 * 3. EL SEGUNDO ARMÓNICO (sin(2t)): Al sumar 'sin(2t)' con la mitad de fuerza
		 * (0.5), agregamos un segundo golpe más pequeño inmediatamente después del
		 * primero: ¡PUM! (primer latido) -> ¡pum! (segundo latido) -> (pausa de
		 * descanso).
		 * 
		 * El resultado es exactamente la sístole y diástole del corazón humano.
		 * =====================================================================
		 */
		final double t = this.tiempoTranscurrido * this.frecuencia;

		// 1. Primer pulso principal (Sístole ventricular)
		final double pulsoPrincipal = Math.max(0.0, Math.sin(t));

		// 2. Segundo pulso secundario más rápido (Cierre de válvulas)
		final double pulsoSecundario = 0.5 * Math.max(0.0, Math.sin(t * 2.0));

		// 3. Modulación óptica combinada sobre el Zoom
		final double pulsoDoble = pulsoPrincipal + pulsoSecundario;
		this.offsetZoom = pulsoDoble * this.amplitudZoom * this.intensidad;

		// El corazón no desplaza las coordenadas ni rota el mundo
		this.offsetX = 0.0;
		this.offsetY = 0.0;
		this.anguloRotacion = 0.0;
	}

	// =========================================================================
	// === GETTERS
	// =========================================================================

	public double getFrecuencia() {
		return this.frecuencia;
	}

	public double getAmplitudZoom() {
		return this.amplitudZoom;
	}
}