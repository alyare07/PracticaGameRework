package principal.ia.dijkstra;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

/**
 * Representa una celda o nodo individual dentro del mapa de distancias
 * Dijkstra.
 */
public class NodoD {

	/** Código de actualización correspondiente al pulso actual de BFS. */
	private byte codAct;

	/** Distancia acumulada desde el nodo objetivo (0.0 = Objetivo). */
	public double distancia;

	/** Coordenadas del nodo en la matriz bidimensional [x, y]. */
	public final Point POSICION;

	/** Área de colisión del nodo expresada en píxeles. */
	public final Rectangle AREA;

	/** Indica si el nodo es un obstáculo sólido e infranqueable del mapa. */
	public boolean inmodificable;

	/** Referencia al nodo previo en el camino óptimo hacia el objetivo. */
	public NodoD nodoProcedente;

	/**
	 * Constructor por defecto. Crea un nodo transitable con distancia infinita.
	 *
	 * @param posicion Posición en la matriz (índices X, Y).
	 * @param d        Dimensiones físicas del tile en píxeles (ancho x alto).
	 */
	public NodoD(final Point posicion, final Dimension d) {
		this.POSICION = posicion;
		this.AREA = new Rectangle(posicion.x * d.width, posicion.y * d.height, d.width, d.height);
		this.distancia = Double.MAX_VALUE;
		this.inmodificable = false;
	}

	/**
	 * Constructor extendido para definir propiedades de obstáculo permanente.
	 *
	 * @param posicion      Posición en la matriz (índices X, Y).
	 * @param d             Dimensiones físicas del tile en píxeles.
	 * @param inmodificable 'true' si es una celda sólida e incalcanzable.
	 */
	public NodoD(final Point posicion, final Dimension d, final boolean inmodificable) {
		this(posicion, d);
		this.inmodificable = inmodificable;
	}

	// --- Control de Código de Actualización (Pulso BFS) ---

	public void setCodAct(final byte codAct) {
		this.codAct = codAct;
	}

	public byte getCodAct() {
		return this.codAct;
	}

	/**
	 * Evalúa si este nodo ya fue procesado o no se puede atravesar en el pulso
	 * actual.
	 *
	 * @param codActActual Código de actualización en desarrollo.
	 * @return 'true' si ya fue visitado o si es una estructura inmodificable.
	 */
	public boolean visitado(final byte codActActual) {
		return this.inmodificable || ((this.codAct == codActActual) && (this.distancia != Double.MAX_VALUE));
	}

	/**
	 * Compara si unas coordenadas en píxeles pertenecen a las coordenadas de celda
	 * de este nodo.
	 */
	public boolean compararPosicionesMundo(final int x, final int y) {
		return (this.POSICION.x == (x / this.AREA.width)) && (this.POSICION.y == (y / this.AREA.height));
	}
}