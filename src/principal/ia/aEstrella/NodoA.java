package principal.ia.aEstrella;

import java.awt.Dimension;

/**
 * Representa una celda individual dentro de la grilla de búsqueda del algoritmo
 * A*.
 * 
 * Optimizado para Java 8: - Se elimina el Enum 'EstadoNodo' usando constantes
 * byte (1 byte vs puntero de 8 bytes). - Se almacena la posición física en
 * píxeles del mundo para evitar multiplicaciones reiteradas. - Soporta
 * obstáculos fijos permanentes mediante 'inmodificable'.
 */
public class NodoA {

	/** Constantes de estado del nodo (reemplazo de Enum) */
	public static final byte ESTADO_NINGUNO = 0;
	public static final byte ESTADO_ABIERTA = 1;
	public static final byte ESTADO_CERRADA = 2;

	/**
	 * Constante optimizada para la heurística Octile: Math.sqrt(2) - 1 ≈
	 * 0.41421354f
	 */
	private static final float SQRT_2_MINUS_ONE = (float) (Math.sqrt(2.0) - 1.0);

	// Coordenadas en la grilla discreta
	private final int xNodo;
	private final int yNodo;

	// Coordenadas físicas en píxeles del mundo
	private final int mundoX;
	private final int mundoY;
	private final int ancho;
	private final int alto;

	/**
	 * Indica si el nodo es un obstáculo fijo e infranqueable (muros, acantilados)
	 */
	private boolean inmodificable;

	/** Costo G: Distancia acumulada real desde el nodo de partida */
	private float costoG;

	/** Costo H: Heurística Octile estimada hasta el destino */
	private float costoH;

	/** Costo F Total: G + H */
	private float costoF;

	/** Generación de búsqueda en la que este nodo fue evaluado por última vez */
	private int generacionBusqueda;

	/** Estado actual del nodo en la iteración activa (NINGUNO, ABIERTA, CERRADA) */
	private byte estado;

	/** Puntero al nodo padre para reconstruir el camino óptimo */
	private NodoA nodoProcedente;

	/**
	 * Constructor principal de la casilla A*.
	 *
	 * @param xNodo         Coordenada X en la matriz.
	 * @param yNodo         Coordenada Y en la matriz.
	 * @param dimension     Tamaño físico en píxeles de cada celda.
	 * @param inmodificable 'true' si es una pared permanente.
	 */
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
	}

	/**
	 * Reinicia los valores del nodo en O(1) cuando es alcanzado por una nueva
	 * generación.
	 *
	 * @param generacion Identificador de la búsqueda actual.
	 */
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

	/**
	 * Asigna los costos F, G y H del nodo actual mediante la Heurística Octile.
	 *
	 * @param padre     Nodo predecesor.
	 * @param objetivo  Nodo destino de la búsqueda.
	 * @param costoPaso Costo de paso (1.0f ortogonal o 1.414f diagonal).
	 */
	public void evaluar(final NodoA padre, final NodoA objetivo, final float costoPaso) {
		this.nodoProcedente = padre;
		this.costoG = (padre == null) ? 0f : padre.costoG + costoPaso;

		final float dx = Math.abs(this.xNodo - objetivo.xNodo);
		final float dy = Math.abs(this.yNodo - objetivo.yNodo);

		// Heurística Octile óptima para 8 direcciones
		this.costoH = Math.max(dx, dy) + (SQRT_2_MINUS_ONE * Math.min(dx, dy));
		this.costoF = this.costoG + this.costoH;
	}

	/**
	 * Comprueba si unas coordenadas del mundo corresponden a este nodo usando
	 * división segura.
	 */
	public boolean compararPosicionesMundo(final int xMundo, final int yMundo) {
		if ((this.ancho == 0) || (this.alto == 0)) {
			return false;
		}
		return (this.xNodo == Math.floorDiv(xMundo, this.ancho)) && (this.yNodo == Math.floorDiv(yMundo, this.alto));
	}

	// --- GETTERS Y SETTERS ---

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