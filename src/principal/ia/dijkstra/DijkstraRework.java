package principal.ia.dijkstra;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import principal.ia.Lista;
import principal.mapa.Mundo;

/**
 * Sistema de búsqueda de caminos (Pathfinding) optimizado mediante
 * Dijkstra/BFS. Utiliza una matriz 2D para accesos O(1) y cálculo asíncrono
 * para mantener el rendimiento.
 */
public class DijkstraRework {

	// --- Atributos de Configuración y Referencias ---
	public final Mundo MUNDO;
	private final Dimension DIMENSION_NODO;

	// --- Estado de la Matriz ---
	public int xUltimoNodo;
	public int yUltimoNodo;
	public int cantNodoVisitados = 0;
	public int entidadesAlPendiente = 0;

	/**
	 * Identificador de actualización del pulso actual (evita limpiar la matriz a
	 * cada frame).
	 */
	private byte codAct;

	/** Matriz bidimensional de nodos para búsquedas instantáneas O(1). */
	private NodoD[][] nodos;

	/** Último nodo objetivo procesado (generalmente la posición del jugador). */
	private NodoD ultimoNodoPosObjetivo;

	// --- Control Concurrente / Hilos ---
	protected final Lock lock = new ReentrantLock();
	private volatile boolean actualizando;

	/**
	 * Executor dedicado de un solo hilo para procesar el cálculo en segundo plano.
	 */
	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	public DijkstraRework(final Mundo mundo, final Dimension dimension) {
		this.DIMENSION_NODO = dimension;
		this.MUNDO = mundo;
		this.codAct = Byte.MIN_VALUE;
		this.generarNodos();
	}

	/**
	 * Inicializa la matriz bidimensional de nodos según las dimensiones del
	 * terreno. Evalúa obstáculos permanentes del mapa al momento de la creación.
	 */
	private void generarNodos() {
		this.xUltimoNodo = (this.MUNDO.getTerreno().getAncho() - this.DIMENSION_NODO.width) / this.DIMENSION_NODO.width;
		this.yUltimoNodo = (this.MUNDO.getTerreno().getAlto() - this.DIMENSION_NODO.height)
				/ this.DIMENSION_NODO.height;

		this.nodos = new NodoD[this.xUltimoNodo + 1][this.yUltimoNodo + 1];

		for (int x = 0; x <= this.xUltimoNodo; x++) {
			for (int y = 0; y <= this.yUltimoNodo; y++) {
				final boolean esPermaSolido = this.verificarSiEsteNodoVaSerPermaSolido(x, y);
				final Point pos = new Point(x, y);

				this.nodos[x][y] = new NodoD(pos, this.DIMENSION_NODO, esPermaSolido);
			}
		}
	}

	/**
	 * Dispara la actualización del mapa de distancias Dijkstra hacia una posición
	 * objetivo.
	 *
	 * @param posicionObjetivo Coordenadas en píxeles del objetivo (Ej. Posición del
	 *                         Jugador).
	 */
	public void actualizar(final Point posicionObjetivo) {
		if (this.actualizando) {
			return;
		}

		final int posRefX = posicionObjetivo.x / this.DIMENSION_NODO.width;
		final int posRefY = posicionObjetivo.y / this.DIMENSION_NODO.height;

		if ((posRefX < 0) || (posRefX > this.xUltimoNodo) || (posRefY < 0) || (posRefY > this.yUltimoNodo)) {
			return;
		}

		final NodoD n = this.nodos[posRefX][posRefY];
		if ((n == null) || (n == this.ultimoNodoPosObjetivo)) {
			return;
		}

		// Configuración sincrónica previa al procesamiento asíncrono
		this.actualizarCodAct();
		n.distancia = 0;
		n.nodoProcedente = null;
		n.setCodAct(this.codAct);
		this.ultimoNodoPosObjetivo = n;
		this.actualizando = true;

		// Delegación del cálculo intensivo al Executor en segundo plano
		this.executor.submit(() -> {
			try {
				this.lock.lock();
				this.procesarDijkstraBFS(posRefX, posRefY, n);
			} catch (final Exception e) {
				e.printStackTrace();
			} finally {
				this.actualizando = false;
				this.lock.unlock();
			}
		});
	}

	/**
	 * Algoritmo de inundación BFS que asigna las distancias más cortas hacia el
	 * objetivo.
	 */
	private void procesarDijkstraBFS(final int startX, final int startY, final NodoD objetivo) {
		final Queue<NodoD> cola = new ArrayDeque<>();
		cola.add(objetivo);

		while (!cola.isEmpty()) {
			final NodoD n = cola.poll();
			final int xNodo = n.POSICION.x;
			final int yNodo = n.POSICION.y;

			// Bucle para evaluar los 8 vecinos adyacentes (incluyendo diagonales)
			for (int y = yNodo - 1; y <= (yNodo + 1); y++) {
				if ((y < 0) || (y > this.yUltimoNodo)) {
					continue;
				}

				for (int x = xNodo - 1; x <= (xNodo + 1); x++) {
					if ((x < 0) || (x > this.xUltimoNodo) || ((x == xNodo) && (y == yNodo))) {
						continue;
					}

					final NodoD nodoAct = this.nodos[x][y];

					// Ignorar nodos inexistentes o con colisiones fijas del terreno
					if ((nodoAct == null) || nodoAct.inmodificable) {
						continue;
					}

					// Ignorar si el nodo ya se procesó en el pulso actual
					if (nodoAct.getCodAct() == this.codAct) {
						continue;
					}

					// Evaluar si existe colisión dinámica en el frame actual
					if (this.colisiona(nodoAct)) {
						nodoAct.distancia = Double.MAX_VALUE;
						nodoAct.nodoProcedente = null;
						nodoAct.setCodAct(this.codAct);
						continue;
					}

					// Calcular distancia acumulada y encolar
					final double distCalculada = n.distancia + n.POSICION.distance(nodoAct.POSICION);
					nodoAct.distancia = distCalculada;
					nodoAct.nodoProcedente = n;
					nodoAct.setCodAct(this.codAct);

					this.cantNodoVisitados++;
					cola.add(nodoAct);
				}
			}
		}
	}

	/**
	 * Verifica si una celda contiene un obstáculo permanente en el mundo al
	 * generarse el mapa.
	 */
	private boolean verificarSiEsteNodoVaSerPermaSolido(final int xMatriz, final int yMatriz) {
		final int xPx = xMatriz * this.DIMENSION_NODO.width;
		final int yPx = yMatriz * this.DIMENSION_NODO.height;

		final Rectangle areaNodo = new Rectangle(xPx, yPx, this.DIMENSION_NODO.width, this.DIMENSION_NODO.height);
		final boolean terrenoSolido = this.MUNDO.getTerreno().intersectaSolidoDijkstra(areaNodo);
		final boolean colisionaObjeto = this.MUNDO.colisionaConAlgoSolidoPermanente(areaNodo);

		return terrenoSolido || colisionaObjeto;
	}

	/**
	 * Verifica si un nodo colisiona con el terreno o con objetos sólidos dinámicos.
	 */
	private boolean colisiona(final NodoD n) {
		return this.MUNDO.getTerreno().intersectaSolidoDijkstra(n.AREA) || this.MUNDO.colisionaConObjetoSolido(n.AREA);
	}

	/**
	 * Obtiene el nodo exacto en la matriz basándose en coordenadas de píxeles.
	 */
	public NodoD getNodoReferenciado(final int x, final int y) {
		final int nx = x / this.DIMENSION_NODO.width;
		final int ny = y / this.DIMENSION_NODO.height;

		if ((nx < 0) || (nx > this.xUltimoNodo) || (ny < 0) || (ny > this.yUltimoNodo)) {
			return null;
		}
		return this.nodos[nx][ny];
	}

	/**
	 * Evalúa las 8 casillas vecinas alrededor de unas coordenadas y retorna la que
	 * posea menor distancia.
	 */
	public NodoD getNodoCercano(final int x, final int y) {
		final int xPosRefNodo = x / this.DIMENSION_NODO.width;
		final int yPosRefNodo = y / this.DIMENSION_NODO.height;

		NodoD nodoCercano = null;

		for (int yNodo = yPosRefNodo - 1; yNodo <= (yPosRefNodo + 1); yNodo++) {
			if ((yNodo < 0) || (yNodo > this.yUltimoNodo)) {
				continue;
			}

			for (int xNodo = xPosRefNodo - 1; xNodo <= (xPosRefNodo + 1); xNodo++) {
				if ((xNodo < 0) || (xNodo > this.xUltimoNodo) || ((xPosRefNodo == xNodo) && (yPosRefNodo == yNodo))) {
					continue;
				}

				final NodoD nodoAux = this.nodos[xNodo][yNodo];
				if ((nodoAux != null) && (nodoAux.distancia != Double.MAX_VALUE)
						&& (nodoAux.getCodAct() == this.codAct)) {
					if ((nodoCercano == null) || (nodoAux.distancia < nodoCercano.distancia)) {
						nodoCercano = nodoAux;
					}
				}
			}
		}
		return nodoCercano;
	}

	/**
	 * Retorna el recorrido completo desde una posición en píxeles hasta el
	 * objetivo.
	 */
	public Lista<NodoD> getRecorrido(final int x, final int y) {
		final Lista<NodoD> recorrido = new Lista<NodoD>();
		final NodoD nodoProx = this.getNodoReferenciado(x, y);

		if ((nodoProx != null) && !this.MUNDO.colisionaConObjetoSolido(nodoProx.AREA)) {
			this.generarRecorrido(recorrido, nodoProx);
		}
		return recorrido;
	}

	/**
	 * Retorna el recorrido completo desde un nodo específico hasta el objetivo.
	 */
	public Lista<NodoD> getRecorrido(final NodoD nodoActual) {
		final Lista<NodoD> recorrido = new Lista<NodoD>();

		if ((nodoActual != null) && !this.MUNDO.colisionaConObjetoSolido(nodoActual.AREA)) {
			this.generarRecorrido(recorrido, nodoActual);
		}
		return recorrido;
	}

	/**
	 * Construye recursivamente la lista del camino siguiendo las referencias de
	 * 'nodoProcedente'.
	 */
	private void generarRecorrido(final Lista<NodoD> lista, final NodoD nodo) {
		if ((nodo != null) && (nodo.nodoProcedente != null)) {
			lista.add(nodo);
			this.generarRecorrido(lista, nodo.nodoProcedente);
		} else if (nodo == this.ultimoNodoPosObjetivo) {
			lista.add(nodo);
		}
	}

	// --- Control del Código de Actualización ---
	private void actualizarCodAct() {
		if (this.codAct < Byte.MAX_VALUE) {
			this.codAct++;
		} else {
			this.codAct = Byte.MIN_VALUE;
		}
	}

	// --- Métodos de Estado y Contadores ---
	public void aumentarEntidadesPendientes() {
		this.entidadesAlPendiente++;
	}

	public void reducirEntidadesPendientes() {
		if ((this.entidadesAlPendiente - 1) >= 0) {
			this.entidadesAlPendiente--;
		}
	}

	public boolean actualizando() {
		return this.actualizando;
	}

	public boolean hayEntidadesAlPendiente() {
		return this.entidadesAlPendiente > 0;
	}

	public Dimension getDimensionNodo() {
		return this.DIMENSION_NODO;
	}

	public Mundo getMundo() {
		return this.MUNDO;
	}

	/**
	 * Detiene el executor del hilo de cálculo de forma segura al destruir o cambiar
	 * el Mundo.
	 */
	public void destruir() {
		this.executor.shutdown();
	}
}