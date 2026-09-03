package principal.ia.dijkstra;

import java.awt.Dimension;

/**
 * Representa una celda individual dentro de la grilla del algoritmo de
 * Dijkstra. Arquitectura Lock-Free de doble buffer con soporte de Clearance.
 */
public class NodoD {

	private final int[] codAct = new int[2];
	private final double[] distancia = new double[2];
	private final NodoD[] nodoProcedente = new NodoD[2];

	private final int grillaX;
	private final int grillaY;
	private final int mundoX;
	private final int mundoY;
	private final int ancho;
	private final int alto;

	private boolean inmodificable;
	private byte clearance = 1;

	public NodoD(final int grillaX, final int grillaY, final Dimension dimension, final boolean inmodificable) {
		this.grillaX = grillaX;
		this.grillaY = grillaY;
		this.ancho = dimension.width;
		this.alto = dimension.height;
		this.mundoX = grillaX * this.ancho;
		this.mundoY = grillaY * this.alto;

		this.distancia[0] = Double.MAX_VALUE;
		this.distancia[1] = Double.MAX_VALUE;
		this.inmodificable = inmodificable;
		this.clearance = 1;
	}

	public byte getClearance() {
		return this.clearance;
	}

	public void setClearance(final byte clearance) {
		this.clearance = clearance;
	}

	public int getCodAct(final int bufIdx) {
		return this.codAct[bufIdx];
	}

	public void setCodAct(final int bufIdx, final int codAct) {
		this.codAct[bufIdx] = codAct;
	}

	public double getDistancia(final int bufIdx) {
		return this.distancia[bufIdx];
	}

	public void setDistancia(final int bufIdx, final double distancia) {
		this.distancia[bufIdx] = distancia;
	}

	public NodoD getNodoProcedente(final int bufIdx) {
		return this.nodoProcedente[bufIdx];
	}

	public void setNodoProcedente(final int bufIdx, final NodoD nodoProcedente) {
		this.nodoProcedente[bufIdx] = nodoProcedente;
	}

	public int getGrillaX() {
		return this.grillaX;
	}

	public int getGrillaY() {
		return this.grillaY;
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

	public boolean isVisitado(final int bufIdx, final int codActActual) {
		return this.inmodificable
				|| ((this.codAct[bufIdx] == codActActual) && (this.distancia[bufIdx] != Double.MAX_VALUE));
	}

	public boolean compararPosicionesMundo(final int x, final int y) {
		if ((this.ancho == 0) || (this.alto == 0)) {
			return false;
		}
		return (this.grillaX == Math.floorDiv(x, this.ancho)) && (this.grillaY == Math.floorDiv(y, this.alto));
	}
}