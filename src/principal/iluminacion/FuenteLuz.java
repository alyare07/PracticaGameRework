package principal.iluminacion;

import principal.entes.Ente;

/**
 * Representa un punto emisor de luz dinámico en el mundo del juego con soporte
 * para radio personalizado en tiempo real y cero recolección de basura
 * (Zero-GC).
 * <p>
 * <b>Comportamientos y Capacidades:</b>
 * <ul>
 * <li><b>Modo Fijo vs. Modo Anclado:</b> Puede posicionarse de forma estática
 * en coordenadas del mapa (ej. antorchas en muros, faroles) o anclarse a un
 * {@link Ente} móvil (ej. la linterna del Jugador o un proyectil de Bola de
 * Fuego), actualizando automáticamente su posición en cada frame.</li>
 * <li><b>Simulación de Parpadeo de Fuego (Torch Flicker):</b> Modula el radio
 * de la luz en tiempo real utilizando la superposición de dos frecuencias
 * senoidales desfasadas, imitando el vaivén natural de una llama viva.</li>
 * <li><b>Escalabilidad Óptica Dinámica:</b> Permite alterar el radio base en
 * vivo para mecánicas como pociones de visión nocturna, antorchas que se van
 * gastando o mejoras de equipo.</li>
 * <li><b>Zero-GC:</b> Las instancias se crean una sola vez en el pool maestro
 * de {@link GestorLuz}. Encender o apagar una luz solo conmuta variables
 * primitivas.</li>
 * </ul>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public class FuenteLuz {

	// =========================================================================
	// === 1. ESTADO Y TIPO DE LUZ
	// =========================================================================

	/**
	 * Indica si este punto de luz está actualmente encendido y activo en el mundo.
	 */
	private boolean activa;

	/** Preset que define el comportamiento óptico, color y parpadeo de la luz. */
	private TipoLuz tipo;

	// =========================================================================
	// === 2. POSICIONAMIENTO Y ANCLAJE ESPACIAL
	// =========================================================================

	/** Coordenada X central de emisión en píxeles absolutos del mundo. */
	private double posX;

	/** Coordenada Y central de emisión en píxeles absolutos del mundo. */
	private double posY;

	/**
	 * Entidad a la que se encuentra vinculada la luz. Si es {@code null}, la luz se
	 * considera fija en un punto estático del mapa.
	 */
	private Ente enteAnclado;

	// =========================================================================
	// === 3. MODULACIÓN ÓPTICA Y RADIOS
	// =========================================================================

	/**
	 * Radio base establecido para la luz en píxeles (sin contar el parpadeo). Puede
	 * ser el predeterminado de {@link TipoLuz#getRadioBase()} o un valor
	 * personalizado.
	 */
	private double radioBase;

	/**
	 * Radio final calculado en cada frame tras aplicar las ondas de parpadeo de
	 * fuego. Este es el valor real que la GPU utiliza para dibujar el halo.
	 */
	private double radioActual;

	/**
	 * Acumulador de tiempo individual utilizado para las funciones trigonométricas.
	 * Se inicializa con un valor aleatorio para que dos antorchas cercanas no
	 * titilen en sincronía robótica.
	 */
	private double tiempoFase;

	// =========================================================================
	// === CONSTRUCTOR
	// =========================================================================

	/**
	 * Inicializa la fuente de luz en estado inactivo dentro del pool de memoria.
	 */
	public FuenteLuz() {
		this.activa = false;
	}

	// =========================================================================
	// === INICIALIZACIÓN / SPAWN (CERO ASIGNACIONES EN HEAP)
	// =========================================================================

	/**
	 * Enciende una luz estática en una posición fija del mapa usando el radio por
	 * defecto de su tipo.
	 *
	 * @param x    Coordenada X central en píxeles de mundo.
	 * @param y    Coordenada Y central en píxeles de mundo.
	 * @param tipo Preset de iluminación (ej. {@link TipoLuz#ANTORCHA}).
	 */
	public void spawnFija(final double x, final double y, final TipoLuz tipo) {
		this.spawnFija(x, y, tipo, tipo.getRadioBase());
	}

	/**
	 * Enciende una luz estática en una posición fija del mapa con un radio
	 * personalizado.
	 *
	 * @param x                  Coordenada X central en píxeles de mundo.
	 * @param y                  Coordenada Y central en píxeles de mundo.
	 * @param tipo               Preset de iluminación.
	 * @param radioPersonalizado Radio en píxeles (mínimo 10.0 px por seguridad).
	 */
	public void spawnFija(final double x, final double y, final TipoLuz tipo, final double radioPersonalizado) {
		this.posX = x;
		this.posY = y;
		this.enteAnclado = null;
		this.tipo = tipo;
		this.radioBase = Math.max(10.0, radioPersonalizado);
		this.radioActual = this.radioBase;

		/*
		 * =====================================================================
		 * EXPLICACIÓN DIDÁCTICA: DESFASAMIENTO DE FASE ALEATORIO
		 * --------------------------------------------------------------------- Si
		 * colocas dos antorchas en la misma pared y ambas inician su tiempo en 0.0, las
		 * dos llamas crecerían y se achicarían exactamente al mismo milisegundo,
		 * viéndose como un efecto artificial y poco natural.
		 * 
		 * Al asignar un 'tiempoFase' aleatorio inicial entre 0.0 y 10.0 segundos, cada
		 * antorcha comienza en un punto distinto de la onda, logrando que titilen de
		 * forma independiente y orgánica.
		 * =====================================================================
		 */
		this.tiempoFase = Math.random() * 10.0;
		this.activa = true;
	}

	/**
	 * Enciende una luz anclada a una entidad móvil con su radio predeterminado.
	 *
	 * @param ente Entidad a seguir (Jugador, Criatura, Proyectil).
	 * @param tipo Preset de iluminación.
	 */
	public void spawnAnclada(final Ente ente, final TipoLuz tipo) {
		this.spawnAnclada(ente, tipo, tipo.getRadioBase());
	}

	/**
	 * Enciende una luz anclada a una entidad móvil con un radio personalizado.
	 *
	 * @param ente               Entidad a seguir.
	 * @param tipo               Preset de iluminación.
	 * @param radioPersonalizado Radio en píxeles deseado.
	 */
	public void spawnAnclada(final Ente ente, final TipoLuz tipo, final double radioPersonalizado) {
		this.enteAnclado = ente;
		this.tipo = tipo;
		this.radioBase = Math.max(10.0, radioPersonalizado);
		this.radioActual = this.radioBase;
		this.tiempoFase = Math.random() * 10.0;
		this.activa = true;
	}

	/**
	 * Apaga la luz y desvincula la entidad anclada, devolviendo la ranura al pool.
	 */
	public void apagar() {
		this.activa = false;
		this.enteAnclado = null;
	}

	// =========================================================================
	// === MODIFICACIÓN DINÁMICA DE RADIO (GAMEPLAY & ESTADOS)
	// =========================================================================

	/**
	 * Modifica el radio base de la luz en tiempo real.
	 * <p>
	 * <b>Casos de uso:</b> Antorchas que se van consumiendo con el tiempo, subir de
	 * nivel la linterna en el herrero o hogueras al alimentarlas con madera.
	 * </p>
	 *
	 * @param nuevoRadio Nuevo radio base en píxeles.
	 */
	public void setRadioBase(final double nuevoRadio) {
		this.radioBase = Math.max(10.0, nuevoRadio);
	}

	/**
	 * Multiplica el radio base predeterminado por un factor escalar.
	 * <p>
	 * <b>Casos de uso:</b> Efectos de pociones (ej. Visión Nocturna $\times 1.5$) o
	 * estados de ceguera/debilidad ($\times 0.6$).
	 * </p>
	 *
	 * @param factor Multiplicador de escala (ej: 1.5 para +50%, 0.8 para -20%).
	 */
	public void setMultiplicadorRadio(final double factor) {
		if (this.tipo != null) {
			this.radioBase = Math.max(10.0, this.tipo.getRadioBase() * factor);
		}
	}

	// =========================================================================
	// === CICLO LÓGICO DE ACTUALIZACIÓN (60 APS)
	// =========================================================================

	/**
	 * Actualiza las coordenadas espaciales y procesa la física del parpadeo de
	 * fuego.
	 *
	 * @param dt Delta de tiempo en segundos transcurrido desde el último frame
	 *           (1/60 s).
	 */
	public void actualizar(final double dt) {
		if (!this.activa) {
			return;
		}

		/*
		 * 1. SEGUIMIENTO DE ENTIDAD ANCLADA: Si la entidad fue eliminada del mundo (ej.
		 * un enemigo que murió o una bola de fuego que impactó), apagamos la luz de
		 * inmediato. De lo contrario, centramos la luz en el punto medio exacto de su
		 * cuerpo.
		 */
		if (this.enteAnclado != null) {
			if (this.enteAnclado.estaEliminado()) {
				this.apagar();
				return;
			}
			this.posX = this.enteAnclado.getPosicionXInt() + (this.enteAnclado.getAncho() / 2.0);
			this.posY = this.enteAnclado.getPosicionYInt() + (this.enteAnclado.getAlto() / 2.0);
		}

		/*
		 * =====================================================================
		 * EXPLICACIÓN DIDÁCTICA: FÍSICA DE PARPADEO POR SUMA DE ARMÓNICOS
		 * --------------------------------------------------------------------- Una
		 * llama real no oscila de forma suave y monótona. Para simular el
		 * comportamiento caótico del fuego sin consumir CPU:
		 * 
		 * 1. ONDA PRINCIPAL: sin(t * 14.0) - Una frecuencia media (14 rad/s) que
		 * produce el vaivén general.
		 * 
		 * 2. ONDA SECUNDARIA RÁPIDA: (0.5 * sin(t * 27.0)) - Una frecuencia casi al
		 * doble de velocidad (27 rad/s) con la mitad de fuerza, que añade pequeñas
		 * micro-vibraciones y chispazos.
		 * 
		 * Al sumar ambas ondas, la luz baila de manera irregular, creíble y viva.
		 * =====================================================================
		 */
		if (this.tipo.isParpadea()) {
			this.tiempoFase += dt;
			final double t = this.tiempoFase;
			final double ondaFuego = Math.sin(t * 14.0) + (0.5 * Math.sin(t * 27.0));

			this.radioActual = this.radioBase + (ondaFuego * this.tipo.getAmplitudParpadeo());
		} else {
			// Luces estables (como la linterna o auras mágicas) mantienen su radio fijo
			this.radioActual = this.radioBase;
		}
	}

	// =========================================================================
	// === GETTERS
	// =========================================================================

	public boolean isActiva() {
		return this.activa;
	}

	public double getPosX() {
		return this.posX;
	}

	public double getPosY() {
		return this.posY;
	}

	public double getRadioBase() {
		return this.radioBase;
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
}