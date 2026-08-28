package principal.clima;

import principal.utilidades.Constantes;

/**
 * Estructura de datos ligera para partículas atmosféricas simuladas en espacio
 * de pantalla (Screen-Space) con envoltura toroidal continua.
 * <p>
 * <b>Patrón de Diseño y Rendimiento:</b>
 * <ul>
 * <li><b>Data-Oriented / Struct-Like:</b> Utiliza campos primitivos públicos
 * directos para maximizar la velocidad de acceso de la CPU y eliminar el
 * overhead de llamadas por getters/setters en bucles de alta frecuencia (400
 * partículas $\times$ 60 FPS = 24.000 updates/seg).</li>
 * <li><b>Zero-GC Estricto:</b> No crea ningún objeto en el Heap durante la
 * ejecución. Las partículas se instancian una sola vez al inicio y se reciclan
 * indefinidamente.</li>
 * <li><b>Envoltura Espacial Toroidal:</b> Al salir por un borde de la pantalla,
 * la partícula reaparece instantáneamente por el extremo opuesto con un margen
 * de seguridad, simulando un volumen infinito de lluvia, nieve o viento.</li>
 * </ul>
 * </p>
 * 
 * @version 3.5
 */
public class ParticulaClima {

	// =========================================================================
	// === 1. CONSTANTES DE ENVOLTURA ESPACIAL
	// =========================================================================

	/*
	 * =========================================================================
	 * EXPLICACIÓN DIDÁCTICA: ¿POR QUÉ USAR UN MARGEN DE ENVOLTURA?
	 * ------------------------------------------------------------------------- Si
	 * reiniciáramos la posición de una gota de lluvia o un copo de nieve
	 * exactamente en 'x = 0' o 'x = 640' (los bordes exactos del monitor):
	 *
	 * El jugador vería cómo las partículas "desaparecen de golpe" o
	 * "nacen de la nada" justo en el filo visible de la pantalla, rompiendo la
	 * ilusión visual.
	 *
	 * Al agregar un margen de 40 píxeles fuera de los bordes (-40 a 680, -40 a
	 * 400): Las partículas entran y salen volando suavemente desde fuera del campo
	 * de visión, haciendo que el clima se sienta 100% natural y continuo.
	 * =========================================================================
	 */
	/**
	 * Margen perimetral fuera de pantalla para spawnear y envolver partículas de
	 * forma invisible.
	 */
	private static final int MARGEN_ENVOLTURA = 40;

	// =========================================================================
	// === 2. ESTADO FÍSICO Y COORDENADAS (ACCESO DIRECTO O(1))
	// =========================================================================

	/**
	 * Coordenada X actual de la partícula en espacio de pantalla (0 a 640 px +
	 * margen).
	 */
	public double x;

	/**
	 * Coordenada Y actual de la partícula en espacio de pantalla (0 a 360 px +
	 * margen).
	 */
	public double y;

	/**
	 * Multiplicador de velocidad individual (añade variedad: unas caen más rápido
	 * que otras).
	 */
	public double velocidadBase;

	/**
	 * Longitud en píxeles de la estela para gotas de lluvia o ráfagas de viento.
	 */
	public double longitudTrazo;

	/*
	 * =========================================================================
	 * EXPLICACIÓN DIDÁCTICA: DESFASAJE DE OSCILACIÓN INDIVIDUAL
	 * ------------------------------------------------------------------------- Si
	 * todos los copos de nieve o las hojas de viento usaran el mismo tiempo, todas
	 * las partículas en pantalla se moverían hacia la izquierda y derecha al mismo
	 * tiempo como soldaditos sincronizados.
	 *
	 * Al asignar a cada partícula un ángulo aleatorio inicial entre '0.0 y 2*PI',
	 * cada copo baila en un momento distinto de la onda senoidal, logrando un
	 * aleteo caótico, realista y visualmente orgánico.
	 * =========================================================================
	 */
	/** Acumulador de fase trigonométrica para el vaivén u oscilación lateral. */
	public double faseOscilacion;

	/** Dimensión escalar de la partícula (ancho/alto en píxeles). */
	public double tamano;

	// =========================================================================
	// === 3. INICIALIZACIÓN Y RECICLAJE (ZERO-GC)
	// =========================================================================

	/**
	 * Distribuye aleatoriamente la partícula dentro del área de la pantalla
	 * (incluyendo los márgenes invisibles) y asigna propiedades físicas variadas.
	 */
	public void inicializarAleatorio() {
		this.x = (Math.random() * (Constantes.ANCHO_JUEGO + (MARGEN_ENVOLTURA * 2))) - MARGEN_ENVOLTURA;
		this.y = (Math.random() * (Constantes.ALTO_JUEGO + (MARGEN_ENVOLTURA * 2))) - MARGEN_ENVOLTURA;
		this.velocidadBase = 0.8 + (Math.random() * 0.5);
		this.longitudTrazo = 6.0 + (Math.random() * 8.0);
		this.faseOscilacion = Math.random() * Math.PI * 2.0;
		this.tamano = 1.0 + (Math.random() * 2.0);
	}

	// =========================================================================
	// === 4. CICLO FÍSICO DE ACTUALIZACIÓN Y ENVOLTURA (60 APS)
	// =========================================================================

	/*
	 * =========================================================================
	 * EXPLICACIÓN DIDÁCTICA: ENVOLTURA TOROIDAL (SCREEN-SPACE WRAPPING)
	 * -------------------------------------------------------------------------
	 * Este algoritmo simula un espacio toroidal (como el clásico juego Asteroids o
	 * Pac-Man):
	 *
	 * 1. Sumamos la velocidad calculada por GestorClima multiplicada por 'dt'
	 * (Delta Time). 2. Si la partícula viaja hacia la derecha y supera el borde
	 * derecho (+40px), reaparece instantáneamente en el borde izquierdo (-40px). 3.
	 * Si cae hacia el fondo y supera el borde inferior (+40px), reaparece en el
	 * borde superior (-40px).
	 *
	 * Resultado: Con solo 400 partículas tenemos un suministro infinito de lluvia o
	 * nieve que cubre toda la pantalla del jugador sin importar hacia dónde camine.
	 * =========================================================================
	 */
	/**
	 * Desplaza la partícula según los vectores de velocidad calculados y aplica la
	 * envoltura toroidal si cruza los límites de la pantalla.
	 *
	 * @param vx Velocidad horizontal en píxeles por segundo (influenciada por
	 *           viento).
	 * @param vy Velocidad vertical en píxeles por segundo (influenciada por
	 *           gravedad).
	 * @param dt Delta de tiempo en segundos transcurrido desde el último frame
	 *           (1/60 s).
	 */
	public void actualizar(final double vx, final double vy, final double dt) {
		this.x += vx * dt;
		this.y += vy * dt;

		final int limiteMaxX = Constantes.ANCHO_JUEGO + MARGEN_ENVOLTURA;
		final int limiteMinX = -MARGEN_ENVOLTURA;
		final int limiteMaxY = Constantes.ALTO_JUEGO + MARGEN_ENVOLTURA;
		final int limiteMinY = -MARGEN_ENVOLTURA;

		// Envoltura horizontal (Eje X)
		if (this.x > limiteMaxX) {
			this.x = limiteMinX;
		} else if (this.x < limiteMinX) {
			this.x = limiteMaxX;
		}

		// Envoltura vertical (Eje Y)
		if (this.y > limiteMaxY) {
			this.y = limiteMinY;
		} else if (this.y < limiteMinY) {
			this.y = limiteMaxY;
		}
	}
}