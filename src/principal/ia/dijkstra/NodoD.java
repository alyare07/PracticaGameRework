package principal.ia.dijkstra;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

/**
 * Representa una celda individual dentro de la grilla del algoritmo de
 * Dijkstra.
 * 
 * ¿Cómo funciona el Doble Búfer en este nodo? Los atributos `codAct`,
 * `distancia` y `nodoProcedente` no son variables simples, sino arreglos de
 * tamaño 2 (`new float[2]` o `new NodoD[2]`).
 * 
 * - El índice 0 representa el "Búfer A". - El índice 1 representa el "Búfer B".
 * 
 * Mientras el hilo secundario calcula el camino escribiendo en el índice B (1),
 * el hilo del juego puede seguir leyendo sin ningún tipo de interrupción ni
 * "congelamiento" los datos del índice A (0).
 */
public class NodoD {

	/**
	 * Arreglo de 2 posiciones para almacenar el código de generación en cada búfer.
	 */
	private final int[] codAct = new int[2];

	/**
	 * Arreglo de 2 posiciones para la distancia hacia el objetivo en cada búfer.
	 */
	private final double[] distancia = new double[2];

	/**
	 * Arreglo de 2 posiciones para el puntero hacia la siguiente casilla del camino
	 * en cada búfer.
	 */
	private final NodoD[] nodoProcedente = new NodoD[2];

	/** Coordenada (X, Y) dentro de la grilla del mapa (no en píxeles). */
	private final Point posicion;

	/**
	 * Área rectangular en píxeles que ocupa la casilla en el mundo (usado para
	 * colisiones).
	 */
	private final Rectangle area;

	/**
	 * Indica si el nodo es un obstáculo infranqueable permanente (paredes sólidas).
	 */
	private boolean inmodificable;

	/**
	 * Constructor simple para un nodo transitable por defecto.
	 *
	 * @param posicion  Coordenada en la matriz del mapa.
	 * @param dimension Ancho y alto de la casilla en píxeles.
	 */
	public NodoD(final Point posicion, final Dimension dimension) {
		this(posicion, dimension, false);
	}

	/**
	 * Constructor completo del nodo.
	 *
	 * @param posicion      Coordenada en la matriz del mapa.
	 * @param dimension     Ancho y alto de la casilla en píxeles.
	 * @param inmodificable 'true' si es una pared permanente.
	 */
	public NodoD(final Point posicion, final Dimension dimension, final boolean inmodificable) {
		this.posicion = posicion;
		this.area = new Rectangle(posicion.x * dimension.width, posicion.y * dimension.height, dimension.width,
				dimension.height);

		// Inicializamos ambos búferes con distancia infinita
		this.distancia[0] = Double.MAX_VALUE;
		this.distancia[1] = Double.MAX_VALUE;
		this.inmodificable = inmodificable;
	}

	// --- GETTERS Y SETTERS ESPECÍFICOS CON ÍNDICE DE BÚFER (0 o 1) ---

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

	// --- GETTERS GENERALES DE PROPIEDADES ---

	public Point getPosicion() {
		return this.posicion;
	}

	public Rectangle getArea() {
		return this.area;
	}

	public int getXMundo() {
		return this.area.x;
	}

	public int getYMundo() {
		return this.area.y;
	}

	public int getAncho() {
		return this.area.width;
	}

	public int getAlto() {
		return this.area.height;
	}

	public boolean isInmodificable() {
		return this.inmodificable;
	}

	public void setInmodificable(final boolean inmodificable) {
		this.inmodificable = inmodificable;
	}

	/**
	 * Verifica si este nodo ya fue visitado en el búfer indicado durante la
	 * generación actual.
	 *
	 * @param bufIdx       Índice del búfer (0 o 1).
	 * @param codActActual Código de actualización/generación a comparar.
	 * @return 'true' si es una pared o si ya fue evaluado en la generación
	 *         indicada.
	 */
	public boolean isVisitado(final int bufIdx, final int codActActual) {
		return this.inmodificable
				|| ((this.codAct[bufIdx] == codActActual) && (this.distancia[bufIdx] != Double.MAX_VALUE));
	}

	/**
	 * Comprueba si unas coordenadas dadas en píxeles corresponden a la posición de
	 * este nodo.
	 *
	 * @param x Posición X en píxeles.
	 * @param y Posición Y en píxeles.
	 * @return 'true' si el punto recae sobre este nodo.
	 */
	public boolean compararPosicionesMundo(final int x, final int y) {
		if ((this.area.width == 0) || (this.area.height == 0)) {
			return false;
		}
		return (this.posicion.x == (x / this.area.width)) && (this.posicion.y == (y / this.area.height));
	}
}