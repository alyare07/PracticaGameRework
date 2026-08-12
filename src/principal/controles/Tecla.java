package principal.controles;

import org.json.simple.JSONObject;

/**
 * Representa una tecla individual con soporte para acciones sostenidas,
 * conmutables (toggle) y de pulsación única por frame.
 */
public class Tecla {

	protected int codigoTecla;
	protected boolean presionada;
	protected boolean accionarInvertible;
	protected final String nombre;

	protected boolean presionadaUnaSolaVez;
	protected boolean teclaFisicaPresionada;
	protected boolean latchUnicaAct;

	public Tecla(final int codigo, final String nombre) {
		this(codigo, false, nombre);
	}

	public Tecla(final int codigo, final boolean accionarInvertible, final String nombre) {
		this.codigoTecla = codigo;
		this.accionarInvertible = accionarInvertible;
		this.nombre = nombre;
	}

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

	/**
	 * Se ejecuta al presionar la tecla. Protegido contra la repetición automática
	 * del SO.
	 */
	public void presionar() {
		if (this.accionarInvertible) {
			// Solo conmuta en la pulsación física inicial, ignorando la repetición del SO
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
	 * Actualiza el estado de pulsación única durante un único frame del juego.
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
	 * Se ejecuta al soltar la tecla física.
	 */
	public void soltar() {
		this.teclaFisicaPresionada = false;
		if (!this.accionarInvertible) {
			this.presionada = false;
		}
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