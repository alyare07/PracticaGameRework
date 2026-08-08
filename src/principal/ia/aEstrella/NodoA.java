package principal.ia.aEstrella;

import java.awt.Dimension;
import java.awt.Rectangle;

public class NodoA implements Comparable<NodoA> {

	private final int xNodo;
	private final int yNodo;
	private final Rectangle areaEnMundo;

	private double costoG;
	private double costoH;
	private double costoF;

	private int codAct;
	private NodoA nodoProcedente;

	public NodoA(final int xNodo, final int yNodo, final Dimension d) {
		this.xNodo = xNodo;
		this.yNodo = yNodo;
		this.areaEnMundo = new Rectangle(xNodo * d.width, yNodo * d.height, d.width, d.height);
		this.codAct = Integer.MIN_VALUE;
	}

	public void reiniciar(final int codAct) {
		this.codAct = codAct;
		this.costoG = Double.MAX_VALUE;
		this.costoH = 0;
		this.costoF = Double.MAX_VALUE;
		this.nodoProcedente = null;
	}

	public boolean visitado(final int codAct) {
		return this.codAct == codAct;
	}

	public void evaluar(final NodoA padre, final NodoA objetivo, final double costoPaso) {
		this.nodoProcedente = padre;
		this.costoG = (padre == null) ? 0 : padre.costoG + costoPaso;

		// Distancia Manhattan pura para la heurística H
		this.costoH = Math.abs(this.xNodo - objetivo.xNodo) + Math.abs(this.yNodo - objetivo.yNodo);
		this.costoF = this.costoG + this.costoH;
	}

	@Override
	public int compareTo(final NodoA otro) {
		return Double.compare(this.costoF, otro.costoF);
	}

	public boolean compararPosicionesMundo(final int x, final int y) {
		return (this.xNodo == (x / this.areaEnMundo.width)) && (this.yNodo == (y / this.areaEnMundo.height));
	}

	// --- Getters y Setters ---
	public int getXNodo() {
		return this.xNodo;
	}

	public int getYNodo() {
		return this.yNodo;
	}

	public Rectangle getAreaEnMundo() {
		return this.areaEnMundo;
	}

	public double getCostoG() {
		return this.costoG;
	}

	public double getCostoF() {
		return this.costoF;
	}

	public NodoA getNodoProcedente() {
		return this.nodoProcedente;
	}

}