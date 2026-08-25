package principal.controles;

import org.json.simple.JSONObject;

/**
 * Representa una acción lógica mapeada a una tecla física del teclado.
 * <p>
 * <b>Características:</b>
 * <ul>
 * <li><b>Protección contra Repetición del SO:</b> Ignora las señales continuas
 * enviadas por la repetición de teclado del sistema operativo (*Typematic
 * Delay*).</li>
 * <li><b>Acciones Conmutables (Toggle):</b> Soporta teclas que alternan su
 * estado (true/false) en cada pulsación (ideal para modos de depuración).</li>
 * <li><b>Pulsación Única por Tick:</b> Proporciona la bandera
 * {@link #presionadoUnicaActualizacion()} para acciones que solo deben
 * dispararse una vez por pulsación física.</li>
 * </ul>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public class Tecla {

	/** Código de tecla nativo de AWT (ej: {@link java.awt.event.KeyEvent#VK_W}). */
	protected int codigoTecla;

	/** Estado activo de la acción lógica (presionada o activa). */
	protected boolean presionada;

	/**
	 * Si es true, cada pulsación física alterna (invierte) el valor booleano de
	 * {@link #presionada}.
	 */
	protected boolean accionarInvertible;

	/** Nombre descriptivo e identificador único para la serialización en JSON. */
	protected final String nombre;

	/**
	 * Activo únicamente durante el primer tick de juego en que la tecla es
	 * presionada.
	 */
	protected boolean presionadaUnaSolaVez;

	/** Registro del estado de contacto físico continuo del teclado. */
	protected boolean teclaFisicaPresionada;

	/**
	 * Pestillo interno para sincronizar la pulsación única entre el evento AWT y el
	 * Game Loop.
	 */
	protected boolean latchUnicaAct;

	// =========================================================================
	// === CONSTRUCTORES
	// =========================================================================

	public Tecla(final int codigo, final String nombre) {
		this(codigo, false, nombre);
	}

	public Tecla(final int codigo, final boolean accionarInvertible, final String nombre) {
		this.codigoTecla = codigo;
		this.accionarInvertible = accionarInvertible;
		this.nombre = nombre;
	}

	// =========================================================================
	// === LÓGICA DE PULSACIÓN Y CICLO DE VIDA
	// =========================================================================

	/*
	 * =========================================================================
	 * EXPLICACIÓN TÉCNICA: SUPRESIÓN DE REPETICIÓN DEL SO
	 * ------------------------------------------------------------------------- Al
	 * mantener presionada una tecla, el sistema operativo genera múltiples eventos
	 * 'keyPressed' repetitivos a partir del retardo de repetición.
	 * 
	 * 'teclaFisicaPresionada' actúa como un cerrojo: solo la PRIMERA señal física
	 * conmuta el estado o activa 'latchUnicaAct'. Todas las señales repetitivas
	 * posteriores se ignoran hasta que el usuario suelta físicamente la tecla.
	 * =========================================================================
	 */

	/**
	 * Se ejecuta al recibir el evento nativo de pulsación.
	 */
	public void presionar() {
		if (this.accionarInvertible) {
			if (!this.teclaFisicaPresionada) {
				this.presionada = !this.presionada;
				this.teclaFisicaPresionada = true;
				this.latchUnicaAct = true;
			}
		} else {
			this.presionada = true;
			if (!this.teclaFisicaPresionada) {
				this.teclaFisicaPresionada = true;
				this.latchUnicaAct = true;
			}
		}
	}

	/**
	 * Actualiza el estado de pulsación única al inicio del ciclo de juego.
	 */
	public void actualizar() {
		if (this.latchUnicaAct) {
			this.presionadaUnaSolaVez = true;
			this.latchUnicaAct = false;
		} else {
			this.presionadaUnaSolaVez = false;
		}
	}

	/**
	 * Se ejecuta al soltar la tecla física en el teclado.
	 */
	public void soltar() {
		this.teclaFisicaPresionada = false;
		if (!this.accionarInvertible) {
			this.presionada = false;
		}
	}

	// =========================================================================
	// === ACCESORES Y PERSISTENCIA
	// =========================================================================

	public int getCodigoTecla() {
		return this.codigoTecla;
	}

	public void establecerCodigoTecla(final int codigoTecla) {
		this.codigoTecla = codigoTecla;
	}

	public boolean presionado() {
		return this.presionada;
	}

	public boolean presionadoUnicaActualizacion() {
		return this.presionadaUnaSolaVez;
	}

	public String getNombre() {
		return this.nombre;
	}

	public boolean accionarInvertible() {
		return this.accionarInvertible;
	}

	public void establecerAccionarInvertible(final boolean b) {
		this.accionarInvertible = b;
	}

	@SuppressWarnings("unchecked")
	public void agregarEnJSON(final JSONObject jo) {
		jo.put(this.nombre, this.codigoTecla);
	}
}