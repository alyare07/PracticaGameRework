package principal.controles;

import org.json.simple.JSONObject;

/**
 * Representa una acción lógica individual dentro del juego vinculada a una
 * tecla física.
 * <p>
 * <b>¿Por qué existe esta clase intermedia?</b><br>
 * Desacopla la acción lógica (ejemplo: "Saltar", "Atacar", "Abrir Menú") del
 * código físico del teclado (ejemplo: {@code KeyEvent.VK_SPACE}). Esto permite
 * que el jugador pueda reconfigurar los controles libremente sin tener que
 * modificar la lógica de las entidades.
 * </p>
 * <p>
 * <b>Manejo de Concurrencia (Hilo EDT vs Hilo Game Loop):</b><br>
 * Utiliza un pestillo booleano volátil ({@link #latchUnicaAct}) como puente de
 * comunicación ultra-rápido entre hilos, garantizando que una pulsación sea
 * detectada por el bucle de juego exactamente en un solo tick lógico sin
 * provocar bloqueos ni pausas de sincronización.
 * </p>
 * 
 * @version 2.1 (Java 8 Pure)
 */
public class Tecla {

	// =========================================================================
	// === ATRIBUTOS DE IDENTIFICACIÓN Y CONFIGURACIÓN
	// =========================================================================

	/**
	 * Código virtual de la tecla física asignada (ej.
	 * {@code java.awt.event.KeyEvent.VK_W}).
	 */
	protected int codigoTecla;

	/**
	 * Nombre descriptivo e identificador único de la acción (usado para
	 * serialización JSON).
	 */
	protected final String nombre;

	/**
	 * Define el comportamiento de la tecla:
	 * <ul>
	 * <li>{@code false} (Modo Continuo): La acción está activa <b>mientras</b> la
	 * tecla física se mantenga presionada (ej. Correr, Moverse).</li>
	 * <li>{@code true} (Modo Interruptor / Toggle): Cada pulsación alterna el
	 * estado entre encendido y apagado, similar a la tecla Bloq Mayús (ej. Mostrar
	 * FPS, Modo Debug).</li>
	 * </ul>
	 */
	protected boolean accionarInvertible;

	// =========================================================================
	// === ESTADOS LÓGICOS DEL MOTOR (LEÍDOS POR EL GAME LOOP)
	// =========================================================================

	/** Estado sostenido de la tecla (si está activa en este momento). */
	protected boolean presionada;

	/**
	 * Indicador de pulsación de fotograma único. Vale {@code true} <b>únicamente
	 * durante el tick</b> en el que la tecla fue accionada.
	 */
	protected boolean presionadaUnaSolaVez;

	/**
	 * Evita el molesto "Key Repeat" del Sistema Operativo.
	 * <p>
	 * Cuando mantienes una tecla presionada en el teclado, el sistema operativo
	 * empieza a enviar eventos repetitivos continuos (como cuando dejas pulsada una
	 * letra al escribir). Esta bandera recuerda si la tecla ya bajó físicamente
	 * para ignorar los eventos repetidos.
	 * </p>
	 */
	protected boolean teclaFisicaPresionada;

	/**
	 * Pestillo volátil de sincronización entre hilos (*Volatile Latch*).
	 * <p>
	 * <b>Explicación para principiantes:</b><br>
	 * En computadoras modernas con procesadores multi-núcleo, cada núcleo tiene su
	 * propia memoria caché ultra-rápida. Si el Hilo de Entrada (AWT EDT) cambia un
	 * valor, el Hilo del Juego podría no enterarse de inmediato porque está leyendo
	 * su propia caché local.
	 * </p>
	 * <p>
	 * La palabra clave {@code volatile} le indica a la máquina virtual de Java
	 * (JVM) que nunca guarde esta variable en caché, forzando a que ambos hilos
	 * lean y escriban siempre en la memoria RAM principal. Esto garantiza
	 * visibilidad instantánea con costo de CPU casi nulo.
	 * </p>
	 */
	protected volatile boolean latchUnicaAct;

	// =========================================================================
	// === CONSTRUCTORES
	// =========================================================================

	/**
	 * Crea una tecla en modo continuo estándar (la acción dura mientras se mantenga
	 * presionada).
	 * 
	 * @param codigo Código de tecla según {@link java.awt.event.KeyEvent}.
	 * @param nombre Nombre descriptivo de la acción.
	 */
	public Tecla(final int codigo, final String nombre) {
		this(codigo, false, nombre);
	}

	/**
	 * Crea una tecla configurando explícitamente si se comporta como interruptor.
	 * 
	 * @param codigo             Código de tecla según
	 *                           {@link java.awt.event.KeyEvent}.
	 * @param accionarInvertible {@code true} para modo Interruptor/Toggle;
	 *                           {@code false} para modo Continuo.
	 * @param nombre             Nombre descriptivo de la acción.
	 */
	public Tecla(final int codigo, final boolean accionarInvertible, final String nombre) {
		this.codigoTecla = codigo;
		this.accionarInvertible = accionarInvertible;
		this.nombre = nombre;
	}

	// =========================================================================
	// === GESTIÓN DE EVENTOS FÍSICOS (EJECUTADOS POR EL HILO EDT DE AWT)
	// =========================================================================

	/**
	 * Se ejecuta cuando el hardware detecta que la tecla fue pulsada.
	 * <p>
	 * Filtra los eventos automáticos del sistema operativo para registrar
	 * únicamente la primera bajada física de la tecla.
	 * </p>
	 */
	public void presionar() {
		if (this.accionarInvertible) {
			// MODO TOGGLE (ej. F1 para Debug):
			// Solo cambiamos el estado cuando el dedo recién baja sobre la tecla
			if (!this.teclaFisicaPresionada) {
				this.presionada = !this.presionada; // Invierte el estado: True -> False / False -> True
				this.teclaFisicaPresionada = true;
				this.latchUnicaAct = true; // Levanta el pestillo para el siguiente tick lógico
			}
		} else {
			// MODO CONTINUO (ej. Barra espaciadora para Atacar / W para Caminar):
			this.presionada = true;
			if (!this.teclaFisicaPresionada) {
				this.teclaFisicaPresionada = true;
				this.latchUnicaAct = true; // Levanta el pestillo
			}
		}
	}

	/**
	 * Se ejecuta cuando el hardware detecta que el usuario levantó el dedo de la
	 * tecla.
	 */
	public void soltar() {
		this.teclaFisicaPresionada = false;

		// Si es una tecla normal (no toggle), se apaga de inmediato al soltarla
		if (!this.accionarInvertible) {
			this.presionada = false;
		}
	}

	// =========================================================================
	// === CICLO LÓGICO DEL JUEGO (EJECUTADO POR EL GAME LOOP - 60 TICKS/SEG)
	// =========================================================================

	/**
	 * Consume el pestillo de eventos y actualiza las banderas de un solo fotograma.
	 * <p>
	 * <b>Consumo de eventos (Latch Consuming):</b><br>
	 * Si la tecla fue presionada desde el último tick,
	 * {@link #presionadaUnaSolaVez} se vuelve {@code true} durante este tick y de
	 * inmediato bajamos el {@link #latchUnicaAct} a {@code false}. Así, en el tick
	 * siguiente, la pulsación ya habrá expirado y no se duplicará.
	 * </p>
	 */
	public void actualizar() {
		if (this.latchUnicaAct) {
			this.presionadaUnaSolaVez = true;
			this.latchUnicaAct = false; // Consumimos el evento
		} else {
			this.presionadaUnaSolaVez = false;
		}
	}

	// =========================================================================
	// === MÉTODOS DE ACCESO Y CONSULTA (GETTERS / SETTERS)
	// =========================================================================

	/**
	 * Obtiene el código físico actual asignado a esta tecla.
	 */
	public int getCodigoTecla() {
		return this.codigoTecla;
	}

	/**
	 * Reasigna el código físico de la tecla (útil para el menú de opciones/cambio
	 * de controles).
	 * 
	 * @param codigoTecla Nuevo código virtual según
	 *                    {@link java.awt.event.KeyEvent}.
	 */
	public void establecerCodigoTecla(final int codigoTecla) {
		this.codigoTecla = codigoTecla;
	}

	/**
	 * Consulta si la acción está activa de forma continua en este momento.
	 * 
	 * @return {@code true} si está presionada o activada; {@code false} en caso
	 *         contrario.
	 */
	public boolean presionado() {
		return this.presionada;
	}

	/**
	 * Consulta si la acción acaba de ser disparada en este tick exacto. Ideal para
	 * disparos de un solo golpe, interacciones de menú, salto, etc.
	 * 
	 * @return {@code true} solo en el primer tick en el que se pulsó la tecla.
	 */
	public boolean presionadoUnicaActualizacion() {
		return this.presionadaUnaSolaVez;
	}

	/**
	 * Obtiene el nombre descriptivo de la acción.
	 */
	public String getNombre() {
		return this.nombre;
	}

	/**
	 * Comprueba si la tecla está operando bajo el modo interruptor (Toggle).
	 */
	public boolean accionarInvertible() {
		return this.accionarInvertible;
	}

	/**
	 * Modifica el modo de operación de la tecla (continuo o interruptor).
	 */
	public void establecerAccionarInvertible(final boolean b) {
		this.accionarInvertible = b;
	}

	// =========================================================================
	// === PERSISTENCIA JSON
	// =========================================================================

	/**
	 * Agrega la configuración de esta tecla al objeto JSON de guardado.
	 * 
	 * @param jo Objeto {@link JSONObject} donde se insertará el par clave-valor.
	 */
	@SuppressWarnings("unchecked")
	public void agregarEnJSON(final JSONObject jo) {
		if (jo != null) {
			jo.put(this.nombre, this.codigoTecla);
		}
	}
}