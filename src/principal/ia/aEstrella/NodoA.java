package principal.ia.aEstrella;

import java.awt.Dimension;

/**
 * Representa una celda individual dentro de la grilla de búsqueda del algoritmo
 * A*. Soporta Clearance (Holgura de tamaño de agente) en O(1).
 */
public class NodoA {

	public static final byte ESTADO_NINGUNO = 0;
	public static final byte ESTADO_ABIERTA = 1;
	public static final byte ESTADO_CERRADA = 2;

	private static final float SQRT_2_MINUS_ONE = (float) (Math.sqrt(2.0) - 1.0);

	private final int xNodo;
	private final int yNodo;
	private final int mundoX;
	private final int mundoY;
	private final int ancho;
	private final int alto;

	private boolean inmodificable;
	private float costoG;
	private float costoH;
	private float costoF;
	private int generacionBusqueda;
	private byte estado;
	private NodoA nodoProcedente;

	/** Holgura espacial de paso (1 = 16x16, 2 = 32x32, 4 = 64x64) */
	private byte clearance = 1;

	public NodoA(final int xNodo, final int yNodo, final Dimension dimension, final boolean inmodificable) {
		this.xNodo = xNodo;
		this.yNodo = yNodo;
		this.ancho = dimension.width;
		this.alto = dimension.height;
		this.mundoX = xNodo * this.ancho;
		this.mundoY = yNodo * this.alto;

		this.inmodificable = inmodificable;
		this.generacionBusqueda = 0;
		this.estado = ESTADO_NINGUNO;
		this.clearance = 1;
	}

	public void reiniciar(final int generacion) {
		this.generacionBusqueda = generacion;
		this.costoG = Float.MAX_VALUE;
		this.costoH = 0f;
		this.costoF = Float.MAX_VALUE;
		this.nodoProcedente = null;
		this.estado = ESTADO_NINGUNO;
	}

	public boolean visitado(final int generacion) {
		return this.generacionBusqueda == generacion;
	}

	public void evaluar(final NodoA padre, final NodoA objetivo, final float costoPaso) {
		this.nodoProcedente = padre;
		this.costoG = (padre == null) ? 0f : padre.costoG + costoPaso;

		final float dx = Math.abs(this.xNodo - objetivo.xNodo);
		final float dy = Math.abs(this.yNodo - objetivo.yNodo);

		this.costoH = Math.max(dx, dy) + (SQRT_2_MINUS_ONE * Math.min(dx, dy));
		this.costoF = this.costoG + this.costoH;
	}

	public boolean compararPosicionesMundo(final int xMundo, final int yMundo) {
		if ((this.ancho == 0) || (this.alto == 0)) {
			return false;
		}
		return (this.xNodo == Math.floorDiv(xMundo, this.ancho)) && (this.yNodo == Math.floorDiv(yMundo, this.alto));
	}

	public byte getClearance() {
		return this.clearance;
	}

	public void setClearance(final byte clearance) {
		this.clearance = clearance;
	}

	public int getXNodo() {
		return this.xNodo;
	}

	public int getYNodo() {
		return this.yNodo;
	}

	public int getXMundo() {
		return this.mundoX;
	}

	public int getYMundo() {
		return this.mundoY;
	}

	public int getAncho() {
		return this.ancho;
	}

	public int getAlto() {
		return this.alto;
	}

	public boolean isInmodificable() {
		return this.inmodificable;
	}

	public void setInmodificable(final boolean inmodificable) {
		this.inmodificable = inmodificable;
	}

	public float getCostoG() {
		return this.costoG;
	}

	public float getCostoH() {
		return this.costoH;
	}

	public float getCostoF() {
		return this.costoF;
	}

	public NodoA getNodoProcedente() {
		return this.nodoProcedente;
	}

	public byte getEstado() {
		return this.estado;
	}

	public void setEstado(final byte estado) {
		this.estado = estado;
	}

	public void resetearGeneracion() {
		this.generacionBusqueda = 0;
	}
}