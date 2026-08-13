package principal.ia.aEstrella;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.PriorityQueue;

import principal.ia.aEstrella.NodoA.EstadoNodo;
import principal.mapa.Mundo;

/**
 * Clase encargada de calcular el camino más corto entre dos puntos en un mapa o
 * grilla utilizando el algoritmo de búsqueda A* (A-Star).
 */
public class AEstrella {

	private static final float COSTO_DIAGONAL = (float) Math.sqrt(2);
	private static final float COSTO_ORTOGONAL = 1.0f;

	private static final int[] OFFSET_X = { -1, 0, 1, -1, 1, -1, 0, 1 };
	private static final int[] OFFSET_Y = { -1, -1, -1, 0, 0, 1, 1, 1 };

	private final PriorityQueue<NodoA> listaAbierta;
	private final Mundo mundo;
	private final Dimension dimensionNodo;
	private final Rectangle cajaColisionAux;
	private final ArrayList<NodoA> caminoTemporal;

	private NodoA[][] nodos;
	private int anchoMatriz;
	private int altoMatriz;
	private int generacionBusqueda = 1;

	public AEstrella(final Mundo mundo, final Dimension dimensionNodo) {
		this.mundo = mundo;
		this.dimensionNodo = dimensionNodo;
		this.listaAbierta = new PriorityQueue<>();
		this.cajaColisionAux = new Rectangle();
		this.caminoTemporal = new ArrayList<>();

		this.generarNodos();
	}

	/**
	 * Calcula el recorrido óptimo desde una posición inicial en píxeles hasta una
	 * de destino.
	 */
	public void getRecorrido(final int xInicial, final int yInicial, final int xObjetivo, final int yObjetivo,
			final ArrayDeque<NodoA> recorrido) {

		recorrido.clear();

		final NodoA nodoInicial = this.getNodoRef(xInicial, yInicial);
		NodoA nodoObjetivo = this.getNodoRef(xObjetivo, yObjetivo);

		if ((nodoInicial == null) || (nodoObjetivo == null) || (nodoInicial == nodoObjetivo)) {
			return;
		}

		// Si el nodo objetivo exacto es sólido (ej. el jugador está pegado a una
		// pared/objeto),
		// buscamos el nodo transitable más cercano para no congelar la IA.
		if (this.colisiona(nodoObjetivo)) {
			nodoObjetivo = this.obtenerVecinoTransitableMasCercano(nodoObjetivo, nodoInicial);
			if ((nodoObjetivo == null) || (nodoObjetivo == nodoInicial)) {
				return;
			}
		}

		this.actualizarGeneracionBusqueda();
		this.listaAbierta.clear();

		nodoInicial.reiniciar(this.generacionBusqueda);
		nodoInicial.evaluar(null, nodoObjetivo, 0);
		nodoInicial.setEstado(EstadoNodo.ABIERTA);
		this.listaAbierta.add(nodoInicial);

		while (!this.listaAbierta.isEmpty()) {
			final NodoA nodoAct = this.listaAbierta.poll();

			if (nodoAct.getEstado() == EstadoNodo.CERRADA) {
				continue;
			}

			if (nodoAct == nodoObjetivo) {
				this.reconstruirCamino(recorrido, nodoObjetivo);
				return;
			}

			nodoAct.setEstado(EstadoNodo.CERRADA);

			for (int i = 0; i < 8; i++) {
				final int nx = nodoAct.getXNodo() + OFFSET_X[i];
				final int ny = nodoAct.getYNodo() + OFFSET_Y[i];

				final NodoA vecino = this.getNodo(nx, ny);

				if (vecino == null) {
					continue;
				}

				if (!vecino.visitado(this.generacionBusqueda)) {
					vecino.reiniciar(this.generacionBusqueda);
				}

				if ((vecino.getEstado() == EstadoNodo.CERRADA) || this.colisiona(vecino)) {
					continue;
				}

				final boolean esDiagonal = (OFFSET_X[i] != 0) && (OFFSET_Y[i] != 0);

				if (esDiagonal && this.cortaEsquina(nodoAct, nx, ny)) {
					continue;
				}

				final float costoPaso = esDiagonal ? COSTO_DIAGONAL : COSTO_ORTOGONAL;
				final float nuevoCostoG = nodoAct.getCostoG() + costoPaso;

				if ((vecino.getEstado() == EstadoNodo.NINGUNO) || (nuevoCostoG < vecino.getCostoG())) {
					vecino.evaluar(nodoAct, nodoObjetivo, costoPaso);
					vecino.setEstado(EstadoNodo.ABIERTA);
					this.listaAbierta.add(vecino);
				}
			}
		}
	}

	/**
	 * Si la casilla objetivo es sólida, busca la casilla libre contigua más cercana
	 * al origen.
	 */
	private NodoA obtenerVecinoTransitableMasCercano(final NodoA objetivoSolido, final NodoA origen) {
		NodoA mejorVecino = null;
		double menorDistancia = Double.MAX_VALUE;

		for (int i = 0; i < 8; i++) {
			final int nx = objetivoSolido.getXNodo() + OFFSET_X[i];
			final int ny = objetivoSolido.getYNodo() + OFFSET_Y[i];
			final NodoA v = this.getNodo(nx, ny);

			if ((v != null) && !this.colisiona(v)) {
				final double dist = Math.hypot(v.getXNodo() - origen.getXNodo(), v.getYNodo() - origen.getYNodo());
				if (dist < menorDistancia) {
					menorDistancia = dist;
					mejorVecino = v;
				}
			}
		}
		return mejorVecino;
	}

	private boolean cortaEsquina(final NodoA origen, final int vecinoX, final int vecinoY) {
		final NodoA ortogonal1 = this.getNodo(vecinoX, origen.getYNodo());
		final NodoA ortogonal2 = this.getNodo(origen.getXNodo(), vecinoY);

		return this.colisiona(ortogonal1) || this.colisiona(ortogonal2);
	}

	private void reconstruirCamino(final ArrayDeque<NodoA> destino, final NodoA nodoObjetivo) {
		this.caminoTemporal.clear();
		NodoA actual = nodoObjetivo;

		while (actual != null) {
			this.caminoTemporal.add(actual);
			actual = actual.getNodoProcedente();
		}

		for (int i = this.caminoTemporal.size() - 1; i >= 0; i--) {
			destino.add(this.caminoTemporal.get(i));
		}
	}

	private boolean colisiona(final NodoA n) {
		if (n == null) {
			return true;
		}

		this.cajaColisionAux.setBounds(n.getXNodo() * this.dimensionNodo.width,
				n.getYNodo() * this.dimensionNodo.height, this.dimensionNodo.width, this.dimensionNodo.height);

		return this.mundo.getTerreno().intersectaSolidoDijkstra(this.cajaColisionAux)
				|| this.mundo.colisionaConObjetoSolido(this.cajaColisionAux);
	}

	private void actualizarGeneracionBusqueda() {
		if (this.generacionBusqueda == Integer.MAX_VALUE) {
			for (int x = 0; x < this.anchoMatriz; x++) {
				for (int y = 0; y < this.altoMatriz; y++) {
					this.nodos[x][y].resetearGeneracion();
				}
			}
			this.generacionBusqueda = 1;
		} else {
			this.generacionBusqueda++;
		}
	}

	public void recalcularGrilla() {
		this.generarNodos();
	}

	public NodoA getNodoRef(final int xRef, final int yRef) {
		final int x = xRef / this.dimensionNodo.width;
		final int y = yRef / this.dimensionNodo.height;
		return this.getNodo(x, y);
	}

	private NodoA getNodo(final int x, final int y) {
		if ((x < 0) || (x >= this.anchoMatriz) || (y < 0) || (y >= this.altoMatriz)) {
			return null;
		}
		return this.nodos[x][y];
	}

	private void generarNodos() {
		this.anchoMatriz = (this.mundo.getTerreno().getAncho()) / this.dimensionNodo.width;
		this.altoMatriz = (this.mundo.getTerreno().getAlto()) / this.dimensionNodo.height;

		this.nodos = new NodoA[this.anchoMatriz][this.altoMatriz];

		for (int x = 0; x < this.anchoMatriz; x++) {
			for (int y = 0; y < this.altoMatriz; y++) {
				this.nodos[x][y] = new NodoA(x, y);
			}
		}
	}

	public Dimension getDimensionNodoA() {
		return this.dimensionNodo;
	}
}