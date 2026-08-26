package principal.ia.dijkstra;

import java.awt.Dimension;

/**
 * Representa una celda individual dentro de la grilla del algoritmo de
 * Dijkstra.
 * 
 * Optimización de Memoria y Caché L1/L2: Se han eliminado las dependencias con
 * objetos pesados de AWT (Point y Rectangle). Todas las coordenadas espaciales
 * se gestionan mediante tipos primitivos planos ('int'), reduciendo
 * drásticamente el consumo de memoria RAM y mejorando la localidad espacial en
 * CPU.
 * 
 * Arquitectura de Doble Búfer: Los atributos 'codAct', 'distancia' y
 * 'nodoProcedente' operan sobre arreglos de tamaño 2 para lectura y escritura
 * concurrentes sin bloqueos (Lock-Free).
 */
public class NodoD {

	/** Arreglo de 2 posiciones para el código de generación en cada búfer. */
	private final int[] codAct = new int[2];

	/**
	 * Arreglo de 2 posiciones para la distancia hacia el objetivo en cada búfer.
	 */
	private final double[] distancia = new double[2];

	/**
	 * Arreglo de 2 posiciones para el puntero hacia la siguiente casilla en cada
	 * búfer.
	 */
	private final NodoD[] nodoProcedente = new NodoD[2];

	/** Coordenadas (X, Y) discretas dentro de la matriz/grilla del mapa. */
	private final int grillaX;
	private final int grillaY;

	/** Coordenadas (X, Y) físicas en píxeles del mundo. */
	private final int mundoX;
	private final int mundoY;

	/** Dimensiones en píxeles de la celda. */
	private final int ancho;
	private final int alto;

	/**
	 * Indica si el nodo es un obstáculo infranqueable permanente (paredes sólidas).
	 */
	private boolean inmodificable;

	/**
	 * Constructor principal del nodo optimizado con tipos primitivos.
	 *
	 * @param grillaX       Coordenada X en la matriz de casillas.
	 * @param grillaY       Coordenada Y en la matriz de casillas.
	 * @param dimension     Dimensión en píxeles de cada celda.
	 * @param inmodificable 'true' si es una pared permanente.
	 */
	public NodoD(final int grillaX, final int grillaY, final Dimension dimension, final boolean inmodificable) {
		this.grillaX = grillaX;
		this.grillaY = grillaY;
		this.ancho = dimension.width;
		this.alto = dimension.height;
		this.mundoX = grillaX * this.ancho;
		this.mundoY = grillaY * this.alto;

		// Inicializamos ambos búferes con distancia infinita
		this.distancia[0] = Double.MAX_VALUE;
		this.distancia[1] = Double.MAX_VALUE;
		this.inmodificable = inmodificable;
	}

	// --- GETTERS Y SETTERS DE BÚFER (0 o 1) ---

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

	// --- COORDENADAS Y PROPIEDADES PRIMITIVAS ---

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

	/**
	 * Verifica si este nodo ya fue evaluado en el búfer indicado durante la
	 * generación actual.
	 */
	public boolean isVisitado(final int bufIdx, final int codActActual) {
		return this.inmodificable
				|| ((this.codAct[bufIdx] == codActActual) && (this.distancia[bufIdx] != Double.MAX_VALUE));
	}

	/**
	 * Comprueba si unas coordenadas dadas en píxeles del mundo corresponden a la
	 * posición de este nodo usando división entera segura para valores negativos.
	 *
	 * @param x Posición X en píxeles del mundo.
	 * @param y Posición Y en píxeles del mundo.
	 * @return 'true' si el punto recae sobre este nodo.
	 */
	public boolean compararPosicionesMundo(final int x, final int y) {
		if ((this.ancho == 0) || (this.alto == 0)) {
			return false;
		}
		return (this.grillaX == Math.floorDiv(x, this.ancho)) && (this.grillaY == Math.floorDiv(y, this.alto));
	}
}