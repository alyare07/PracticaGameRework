package principal.iluminacion;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura.Direccion;

/**
 * Emisor de luz dinámico con soporte para conos de visión, orientación angular,
 * offsets de calibración, parpadeo armónico y sincronización de ciclo de vida
 * con {@link Ente}.
 * <p>
 * <b>Pilares de Arquitectura y Rendimiento:</b>
 * <ul>
 * <li><b>Patrón Object Pool (Zero-GC):</b> Las instancias se crean una sola vez
 * en {@link GestorLuz}. Activar o desactivar una luz solo muta tipos primitivos
 * sin generar recolección de basura en el Heap.</li>
 * <li><b>Sincronización Bidireccional:</b> Mantiene una referencia sincronizada
 * con {@link Ente}, permitiendo que si el ente muere o se destruye, la luz se
 * apague inmediatamente en tiempo constante $O(1)$.</li>
 * <li><b>Física de Fuego por Doble Armónico:</b> Modula el radio en tiempo real
 * superponiendo dos ondas senoidales desfasadas para simular el comportamiento
 * caótico y vivo de una llama.</li>
 * <li><b>Decaimiento Cuadrático (Flashes):</b> Las explosiones y destellos
 * reducen su radio mediante una curva de facilidad de salida ($progreso^2$),
 * logrando un impacto visual enérgico que se desvanece con suavidad.</li>
 * </ul>
 * </p>
 * 
 * @version 7.5
 */
public class FuenteLuz {

	// =========================================================================
	// === 1. IDENTIFICACIÓN Y TIPO DE LUZ
	// =========================================================================

	/**
	 * Posición fija de esta ranura dentro del arreglo del pool de
	 * {@link GestorLuz}. Permite devolver la luz a la pila libre en tiempo
	 * constante $O(1)$.
	 */
	private final int indicePool;

	/**
	 * Indica si esta fuente de luz está actualmente encendida y emitiendo en el
	 * mapa.
	 */
	private boolean activa;

	/**
	 * Perfil óptico que define color, intensidad, si parpadea y geometría (circular
	 * o cono).
	 */
	private TipoLuz tipo;

	// =========================================================================
	// === 2. POSICIONAMIENTO, ANCLAJE Y OFFSETS
	// =========================================================================

	/** Coordenada X central de emisión en píxeles absolutos de mundo. */
	private double posX;

	/** Coordenada Y central de emisión en píxeles absolutos de mundo. */
	private double posY;

	/**
	 * Entidad a la que sigue la luz (ej: Jugador, Bola de Fuego). Si es
	 * {@code null}, la luz permanece estática en el mapa (ej: antorcha de pared).
	 */
	private Ente enteAnclado;

	/*
	 * =========================================================================
	 * EXPLICACIÓN DIDÁCTICA: ¿PARA QUÉ SIRVEN LOS OFFSETS DE CALIBRACIÓN?
	 * ------------------------------------------------------------------------- En
	 * un motor 2D, la caja de colisiones de un personaje suele medir 16x16 px y
	 * ubicarse en los pies para detectar muros. Sin embargo, el sprite visual del
	 * cuerpo mide 32x32 px.
	 *
	 * Si anclamos la luz al centro de la colisión, la linterna nacería en las
	 * piernas o desfasada del dibujo. Con 'offsetX' y 'offsetY' podemos desplazar
	 * el foco unos píxeles hacia arriba o hacia la derecha para que coincida
	 * exactamente con la mano, el pecho o la cabeza del personaje.
	 * =========================================================================
	 */
	/**
	 * Desplazamiento manual en X en píxeles respecto al centro de la entidad
	 * anclada.
	 */
	private double offsetX = 0.0;

	/**
	 * Desplazamiento manual en Y en píxeles respecto al centro de la entidad
	 * anclada.
	 */
	private double offsetY = 0.0;

	/**
	 * Ángulo de rotación en radianes (utilizado para orientar el haz de los conos).
	 */
	private double anguloRotacion;

	// =========================================================================
	// === 3. RADIOS Y FÍSICA DE PARPADEO (FLICKERING)
	// =========================================================================

	/**
	 * Radio base establecido en píxeles antes de aplicar la modulación de fuego.
	 */
	private double radioBase;

	/** Radio final calculado en cada fotograma tras sumar las ondas de la llama. */
	private double radioActual;

	/*
	 * =========================================================================
	 * EXPLICACIÓN DIDÁCTICA: DESFASAJE DE FASE ALEATORIO
	 * ------------------------------------------------------------------------- Si
	 * colocas 5 antorchas en un pasillo y todas arrancan su tiempo en 0.0, las 5
	 * llamas crecerían y se achicarían al mismo milisegundo exacto, viéndose como
	 * un baile robótico y artificial.
	 *
	 * Al inicializar 'tiempoFase' con un valor aleatorio (Math.random() * 10.0),
	 * cada antorcha comienza en un punto distinto de la curva senoidal, logrando
	 * que titilen de forma totalmente independiente y orgánica.
	 * =========================================================================
	 */
	/**
	 * Acumulador de tiempo individual para las fórmulas trigonométricas de
	 * parpadeo.
	 */
	private double tiempoFase;

	// =========================================================================
	// === 4. CICLO DE VIDA TEMPORAL (FLASHES Y EXPLOSIONES)
	// =========================================================================

	/**
	 * Indica si la luz tiene tiempo de vida limitado (ej: impacto de granada o
	 * rayo).
	 */
	private boolean temporal;

	/** Duración total del destello en segundos. */
	private double duracionVidaTotal;

	/** Contador regresivo de tiempo de vida restante en segundos. */
	private double tiempoVidaRestante;

	/**
	 * Radio de partida en el momento de la detonación para calcular el decaimiento.
	 */
	private double radioInicialFlash;

	// =========================================================================
	// === CONSTRUCTOR
	// =========================================================================

	/**
	 * Inicializa la ranura en estado inactivo dentro del pool maestro de memoria.
	 *
	 * @param indicePool Posición fija asignada en el arreglo de {@link GestorLuz}.
	 */
	public FuenteLuz(final int indicePool) {
		this.indicePool = indicePool;
		this.activa = false;
	}

	// =========================================================================
	// === INICIALIZACIÓN / SPAWN (ZERO-GC)
	// =========================================================================

	/**
	 * Enciende una luz estática en una posición fija del mapa con un radio
	 * personalizado.
	 *
	 * @param x                  Coordenada X central en píxeles de mundo.
	 * @param y                  Coordenada Y central en píxeles de mundo.
	 * @param tipo               Preset óptico a aplicar.
	 * @param radioPersonalizado Radio base en píxeles (mínimo 10.0 px por
	 *                           seguridad).
	 */
	public void spawnFija(final double x, final double y, final TipoLuz tipo, final double radioPersonalizado) {
		this.posX = x;
		this.posY = y;
		this.enteAnclado = null;
		this.offsetX = 0.0;
		this.offsetY = 0.0;
		this.tipo = tipo;
		this.radioBase = Math.max(10.0, radioPersonalizado);
		this.radioActual = this.radioBase;
		this.tiempoFase = Math.random() * 10.0;
		this.anguloRotacion = 0.0;
		this.temporal = false;
		this.activa = true;
	}

	/*
	 * =========================================================================
	 * EXPLICACIÓN DIDÁCTICA: VINCULACIÓN BIDIRECCIONAL (ENTE <-> FUENTE_LUZ)
	 * ------------------------------------------------------------------------- 1.
	 * La luz guarda la referencia del Ente ('this.enteAnclado = ente') y calcula
	 * inmediatamente su posición para no nacer en (0,0).
	 *
	 * 2. El Ente guarda la referencia de la luz ('ente.asignarLuz(this)').
	 *
	 * Beneficio: Si el enemigo muere o la bola de fuego impacta y se elimina del
	 * juego, el Ente apaga la luz automáticamente en tiempo O(1) en su propio
	 * método 'eliminar()', evitando luces huérfanas o fugas de memoria.
	 * =========================================================================
	 */
	/**
	 * Enciende una luz vinculada al centro de una entidad móvil.
	 *
	 * @param ente               Entidad a seguir (Jugador, Criatura, Proyectil).
	 * @param tipo               Preset óptico a aplicar.
	 * @param radioPersonalizado Radio base en píxeles deseado.
	 */
	public void spawnAnclada(final Ente ente, final TipoLuz tipo, final double radioPersonalizado) {
		this.enteAnclado = ente;
		this.offsetX = 0.0;
		this.offsetY = 0.0;
		this.tipo = tipo;
		this.radioBase = Math.max(10.0, radioPersonalizado);
		this.radioActual = this.radioBase;
		this.tiempoFase = Math.random() * 10.0;
		this.anguloRotacion = 0.0;
		this.temporal = false;
		this.activa = true;

		if (ente != null) {
			this.actualizarPosicionEnte();
			ente.asignarLuz(this);
		}
	}

	/**
	 * Enciende un destello temporal de posición fija que reduce su radio y se apaga
	 * automáticamente.
	 *
	 * @param x                Coordenada X central de la explosión.
	 * @param y                Coordenada Y central de la explosión.
	 * @param tipo             Preset óptico (ej.
	 *                         {@link TipoLuz#DESTELLO_EXPLOSION}).
	 * @param radioInicial     Radio máximo en el instante de la detonación.
	 * @param duracionSegundos Tiempo total de vida antes de extinguirse (ej:
	 *                         0.35s).
	 */
	public void spawnTemporal(final double x, final double y, final TipoLuz tipo, final double radioInicial,
			final double duracionSegundos) {
		this.spawnFija(x, y, tipo, radioInicial);
		this.temporal = true;
		this.duracionVidaTotal = Math.max(0.01, duracionSegundos);
		this.tiempoVidaRestante = this.duracionVidaTotal;
		this.radioInicialFlash = this.radioBase;
	}

	/**
	 * Apaga la luz, resetea sus parámetros y desvincula la entidad anclada, dejando
	 * la ranura lista para ser reutilizada por el pool.
	 */
	public void apagar() {
		this.activa = false;
		this.temporal = false;
		if (this.enteAnclado != null) {
			if (this.enteAnclado.getLuzAsignada() == this) {
				this.enteAnclado.asignarLuz(null);
			}
			this.enteAnclado = null;
		}
		this.offsetX = 0.0;
		this.offsetY = 0.0;
	}

	// =========================================================================
	// === CICLO LÓGICO DE ACTUALIZACIÓN (60 APS)
	// =========================================================================

	/**
	 * Actualiza las coordenadas espaciales siguiendo al ente y procesa la física de
	 * parpadeo de llama o decaimiento temporal.
	 *
	 * @param dt Delta de tiempo en segundos transcurrido desde el último frame
	 *           (1/60 s).
	 */
	public void actualizar(final double dt) {
		if (!this.activa) {
			return;
		}

		/*
		 * ===================================================================== 1.
		 * DECAIMIENTO CUADRÁTICO DE FLASH TEMPORAL
		 * ---------------------------------------------------------------------
		 * 'progreso' va de 1.0 (nacimiento) a 0.0 (muerte). Al elevarlo al cuadrado
		 * '(progreso * progreso)': - Al inicio decae muy rápido simulando el estallido
		 * violento. - Al final se apaga lentamente simulando la disipación del
		 * humo/calor.
		 * =====================================================================
		 */
		if (this.temporal) {
			this.tiempoVidaRestante -= dt;
			if (this.tiempoVidaRestante <= 0.0) {
				this.apagar();
				return;
			}
			final double progreso = this.tiempoVidaRestante / this.duracionVidaTotal;
			this.radioBase = this.radioInicialFlash * (progreso * progreso);
		}

		// 2. Seguimiento de Ente Anclado
		this.actualizarPosicionEnte();

		/*
		 * ===================================================================== 3.
		 * FÍSICA DE LLAMA VIVA POR SUMA DE ARMÓNICOS
		 * --------------------------------------------------------------------- Una
		 * llama no oscila con una simple onda senoidal suave y aburrida.
		 *
		 * 1. ONDA PRINCIPAL: sin(t * 14.0) -> Frecuencia media (14 rad/s) que produce
		 * el balanceo general del fuego.
		 *
		 * 2. ONDA SECUNDARIA RÁPIDA: (0.5 * sin(t * 27.0)) -> Frecuencia rápida con
		 * menor fuerza que añade micro-vibraciones y chispazos irregulares.
		 *
		 * Al combinarlas, la luz baila de manera caótica, natural y realista.
		 * =========================================================================
		 */
		if (this.tipo.isParpadea()) {
			this.tiempoFase += dt;
			final double t = this.tiempoFase;
			final double ondaFuego = Math.sin(t * 14.0) + (0.5 * Math.sin(t * 27.0));
			this.radioActual = this.radioBase + (ondaFuego * this.tipo.getAmplitudParpadeo());
		} else {
			this.radioActual = this.radioBase;
		}
	}

	/**
	 * Actualiza exclusivamente las coordenadas de seguimiento del ente anclado (sin
	 * física de fuego). Permite que el motor mueva la luz aunque esté fuera de
	 * cámara.
	 */
	public void actualizarPosicionEnte() {
		if (this.enteAnclado != null) {
			if (this.enteAnclado.estaEliminado()) {
				this.apagar();
				return;
			}
			this.posX = this.enteAnclado.getCentroX() + this.offsetX;
			this.posY = this.enteAnclado.getCentroY() + this.offsetY;
		}
	}

	// =========================================================================
	// === MÉTODOS DE ORIENTACIÓN Y OFFSETS (API PÚBLICA)
	// =========================================================================

	/**
	 * Calibra el desplazamiento en píxeles respecto al centro geométrico del ente.
	 *
	 * @param offsetX Desplazamiento horizontal (+ hacia la derecha, - hacia la
	 *                izquierda).
	 * @param offsetY Desplazamiento vertical (+ hacia abajo, - hacia arriba).
	 */
	public void setOffset(final double offsetX, final double offsetY) {
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.actualizarPosicionEnte();
	}

	/*
	 * =========================================================================
	 * EXPLICACIÓN DIDÁCTICA: CONVERSIÓN DE PUNTOS CARDINALES A RADIANES
	 * ------------------------------------------------------------------------- En
	 * el sistema de coordenadas de pantalla 2D de Java (donde +Y va hacia abajo): -
	 * ESTE (Derecha) : 0° -> 0.0 radianes - SUR (Abajo) : 90° -> PI * 0.5 radianes
	 * - OESTE(Izquierda): 180° -> PI radianes - NORTE(Arriba) : 270° -> PI * 1.5
	 * radianes
	 * =========================================================================
	 */
	/**
	 * Orienta el haz del cono de luz según los 4 puntos cardinales de una criatura.
	 *
	 * @param direccion Dirección hacia la que mira el personaje.
	 */
	public void orientarSegunDireccion(final Direccion direccion) {
		if (direccion == null) {
			return;
		}
		switch (direccion) {
		case SUR:
			this.anguloRotacion = Math.PI * 0.5; // 90° (Hacia abajo)
			break;
		case OESTE:
			this.anguloRotacion = Math.PI; // 180° (Hacia la izquierda)
			break;
		case NORTE:
			this.anguloRotacion = Math.PI * 1.5; // 270° (Hacia arriba)
			break;
		case ESTE:
		default:
			this.anguloRotacion = 0.0; // 0° (Hacia la derecha)
			break;
		}
	}

	/*
	 * =========================================================================
	 * EXPLICACIÓN DIDÁCTICA: TRIGONOMETRÍA CON 'Math.atan2(dy, dx)'
	 * -------------------------------------------------------------------------
	 * Para que la linterna apunte suavemente hacia el cursor del ratón:
	 *
	 * 1. Calculamos la distancia en X (destinoX - posX) y en Y (destinoY - posY).
	 * 2. La función arcotangente 'Math.atan2(dy, dx)' nos devuelve el ángulo exacto
	 * en radianes (-PI a +PI) en un solo paso, manejando divisiones por cero
	 * automáticamente.
	 * =========================================================================
	 */
	/**
	 * Orienta el haz del cono de luz apuntando hacia una coordenada de mundo (ej:
	 * el ratón).
	 *
	 * @param destinoX Coordenada X del objetivo en píxeles de mundo.
	 * @param destinoY Coordenada Y del objetivo en píxeles de mundo.
	 */
	public void orientarHaciaPunto(final double destinoX, final double destinoY) {
		this.anguloRotacion = Math.atan2(destinoY - this.posY, destinoX - this.posX);
	}

	/**
	 * Establece directamente el ángulo de rotación del haz de luz en radianes.
	 *
	 * @param radianes Ángulo continuo en radianes.
	 */
	public void setAnguloRotacion(final double radianes) {
		this.anguloRotacion = radianes;
	}

	/**
	 * Multiplica el radio base predeterminado por un factor escalar dinámico.
	 * <p>
	 * <b>Casos de uso:</b> Pociones de visión nocturna ($\times 1.5$) o estados de
	 * ceguera/debilidad ($\times 0.6$).
	 * </p>
	 *
	 * @param factor Multiplicador escalar (ej: 1.5 para +50%, 0.8 para -20%).
	 */
	public void setMultiplicadorRadio(final double factor) {
		if (this.tipo != null) {
			this.radioBase = Math.max(10.0, this.tipo.getRadioBase() * factor);
		}
	}

	// =========================================================================
	// === GETTERS INMUTABLES (ZERO-GC)
	// =========================================================================

	public int getIndicePool() {
		return this.indicePool;
	}

	public boolean isActiva() {
		return this.activa;
	}

	public double getPosX() {
		return this.posX;
	}

	public double getPosY() {
		return this.posY;
	}

	public double getOffsetX() {
		return this.offsetX;
	}

	public double getOffsetY() {
		return this.offsetY;
	}

	public double getRadioActual() {
		return this.radioActual;
	}

	public TipoLuz getTipo() {
		return this.tipo;
	}

	public Ente getEnteAnclado() {
		return this.enteAnclado;
	}

	public double getAnguloRotacion() {
		return this.anguloRotacion;
	}
}