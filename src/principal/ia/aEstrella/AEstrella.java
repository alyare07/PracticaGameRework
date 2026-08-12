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
 * 
 * ¿Cómo funciona A*? Busca el camino óptimo evaluando casillas (nodos) según su
 * costo real desde el origen y un costo estimado hacia el destino (heurística).
 */
public class AEstrella {

	// Costo del movimiento en diagonal: La diagonal de un cuadrado de 1x1 es la
	// raíz cuadrada de 2 (~1.414).
	private static final float COSTO_DIAGONAL = (float) Math.sqrt(2);

	// Costo del movimiento recto (horizontal o vertical): 1 unidad.
	private static final float COSTO_ORTOGONAL = 1.0f;

	// Desplazamientos en la grilla para consultar los 8 vecinos de un nodo (Arriba,
	// Abajo, Izquierda, Derecha y 4 Diagonales).
	// La combinación de OFFSET_X[i] y OFFSET_Y[i] da la dirección relativa.
	private static final int[] OFFSET_X = { -1, 0, 1, -1, 1, -1, 0, 1 };
	private static final int[] OFFSET_Y = { -1, -1, -1, 0, 0, 1, 1, 1 };

	/**
	 * Lista Abierta: Almacena los nodos pendientes de evaluar, ordenados
	 * automáticamente por menor costo.
	 */
	private final PriorityQueue<NodoA> listaAbierta;

	/** Referencia al mundo del juego para comprobar obstáculos y dimensiones. */
	private final Mundo mundo;

	/** Tamaño en píxeles de cada celda/nodo de la grilla. */
	private final Dimension dimensionNodo;

	/**
	 * Objeto aux. de colisión para NO instanciar un 'Rectangle' nuevo en cada paso
	 * del algoritmo. Esto evita la recolección de basura (Garbage Collection)
	 * continua y mejora el rendimiento del juego.
	 */
	private final Rectangle cajaColisionAux;

	/**
	 * Lista temporal para almacenar los nodos del camino de fin a inicio antes de
	 * invertirlos.
	 */
	private final ArrayList<NodoA> caminoTemporal;

	/** Matriz bidimensional que representa el mapa dividido en nodos. */
	private NodoA[][] nodos;
	private int anchoMatriz;
	private int altoMatriz;

	/**
	 * Contador de 'Generación' para optimizar la limpieza de nodos. En lugar de
	 * borrar/reiniciar toda la matriz de nodos antes de cada búsqueda (operación
	 * muy lenta), simplemente incrementamos este número.
	 */
	private int generacionBusqueda = 1;

	/**
	 * Constructor principal para el sistema de búsqueda de caminos.
	 *
	 * @param mundo         El mundo del juego que contiene el mapa y los
	 *                      colisionables.
	 * @param dimensionNodo Tamaño en píxeles de cada nodo (ej. 32x32).
	 */
	public AEstrella(final Mundo mundo, final Dimension dimensionNodo) {
		this.mundo = mundo;
		this.dimensionNodo = dimensionNodo;
		this.listaAbierta = new PriorityQueue<>();
		this.cajaColisionAux = new Rectangle();
		this.caminoTemporal = new ArrayList<>();

		// Construimos la matriz de nodos al instanciar el algoritmo
		this.generarNodos();
	}

	/**
	 * Calcula el recorrido óptimo desde una posición inicial en píxeles hasta una
	 * de destino en píxeles.
	 *
	 * @param xInicial  Posición X inicial en el mundo (en píxeles).
	 * @param yInicial  Posición Y inicial en el mundo (en píxeles).
	 * @param xObjetivo Posición X destino en el mundo (en píxeles).
	 * @param yObjetivo Posición Y destino en el mundo (en píxeles).
	 * @param recorrido Cola donde se almacenarán los nodos del camino resultante
	 *                  (Origen -> Destino).
	 */
	public void getRecorrido(final int xInicial, final int yInicial, final int xObjetivo, final int yObjetivo,
			final ArrayDeque<NodoA> recorrido) {

		// Limpiamos el recorrido previo que venía en la cola
		recorrido.clear();

		// Convertimos coordenadas en píxeles a nodos de la grilla
		final NodoA nodoInicial = this.getNodoRef(xInicial, yInicial);
		final NodoA nodoObjetivo = this.getNodoRef(xObjetivo, yObjetivo);

		// Validaciones básicas: si los puntos no existen, son el mismo, o la meta es
		// una pared, no hay camino.
		if ((nodoInicial == null) || (nodoObjetivo == null) || (nodoInicial == nodoObjetivo)
				|| this.colisiona(nodoObjetivo)) {
			return;
		}

		// Preparamos una nueva búsqueda aumentando el identificador de generación
		this.actualizarGeneracionBusqueda();
		this.listaAbierta.clear();

		// Configuramos el nodo de partida
		nodoInicial.reiniciar(this.generacionBusqueda);
		nodoInicial.evaluar(null, nodoObjetivo, 0);
		nodoInicial.setEstado(EstadoNodo.ABIERTA);
		this.listaAbierta.add(nodoInicial);

		// --- BUCLE PRINCIPAL DE A* ---
		while (!this.listaAbierta.isEmpty()) {
			// Extraemos el nodo con menor costo acumulado (costo F) de la cola
			final NodoA nodoAct = this.listaAbierta.poll();

			// Si el nodo ya fue procesado previamente, lo ignoramos (evita duplicados
			// obsoletos de la PriorityQueue)
			if (nodoAct.getEstado() == EstadoNodo.CERRADA) {
				continue;
			}

			// ¡Llegamos al objetivo! Reconstruimos el camino y finalizamos
			if (nodoAct == nodoObjetivo) {
				this.reconstruirCamino(recorrido, nodoObjetivo);
				return;
			}

			// Marcamos el nodo actual como procesado (Lista Cerrada)
			nodoAct.setEstado(EstadoNodo.CERRADA);

			// Exploramos los 8 vecinos alrededor del nodo actual
			for (int i = 0; i < 8; i++) {
				final int nx = nodoAct.getXNodo() + OFFSET_X[i];
				final int ny = nodoAct.getYNodo() + OFFSET_Y[i];

				final NodoA vecino = this.getNodo(nx, ny);

				// Si el vecino está fuera de los límites de la grilla, lo saltamos
				if (vecino == null) {
					continue;
				}

				// Si el vecino no pertenecía a la búsqueda actual, lo reiniciamos bajo demanda
				// (Lazy Reset)
				if (!vecino.visitado(this.generacionBusqueda)) {
					vecino.reiniciar(this.generacionBusqueda);
				}

				// Ignoramos vecinos que ya se evaluaron o que son intransitables
				// (paredes/obstáculos)
				if ((vecino.getEstado() == EstadoNodo.CERRADA) || this.colisiona(vecino)) {
					continue;
				}

				// Comprobamos si el movimiento hacia el vecino es en diagonal
				final boolean esDiagonal = (OFFSET_X[i] != 0) && (OFFSET_Y[i] != 0);

				// Evitamos que los personajes atraviesen esquinas de paredes (es decir, atajar
				// atravesando paredes)
				if (esDiagonal && this.cortaEsquina(nodoAct, nx, ny)) {
					continue;
				}

				// Determinamos el costo de dar este paso (1.0 para recto, 1.414 para diagonal)
				final float costoPaso = esDiagonal ? COSTO_DIAGONAL : COSTO_ORTOGONAL;
				final float nuevoCostoG = nodoAct.getCostoG() + costoPaso;

				// Si encontramos un camino nuevo o uno más corto hacia este vecino:
				if ((vecino.getEstado() == EstadoNodo.NINGUNO) || (nuevoCostoG < vecino.getCostoG())) {
					vecino.evaluar(nodoAct, nodoObjetivo, costoPaso);
					vecino.setEstado(EstadoNodo.ABIERTA);

					// Lo añadimos a la cola de prioridad para explorarlo en los siguientes ciclos
					this.listaAbierta.add(vecino);
				}
			}
		}
	}

	/**
	 * Verifica si un movimiento diagonal intenta atravesar una esquina formada por
	 * obstáculos. Ejemplo: Para moverte en diagonal arriba-derecha, ambas casillas
	 * contiguas (arriba y derecha) deben estar libres.
	 *
	 * @param origen  Nodo de origen.
	 * @param vecinoX Coordenada X del vecino diagonal.
	 * @param vecinoY Coordenada Y del vecino diagonal.
	 * @return 'true' si el movimiento cortaría una esquina sólida; 'false' en caso
	 *         contrario.
	 */
	private boolean cortaEsquina(final NodoA origen, final int vecinoX, final int vecinoY) {
		final NodoA ortogonal1 = this.getNodo(vecinoX, origen.getYNodo());
		final NodoA ortogonal2 = this.getNodo(origen.getXNodo(), vecinoY);

		return this.colisiona(ortogonal1) || this.colisiona(ortogonal2);
	}

	/**
	 * Recorre los nodos desde el nodo objetivo hacia atrás siguiendo sus padres
	 * (nodoProcedente) para construir el camino ordenado desde el Inicio hasta el
	 * Destino.
	 *
	 * @param destino      Cola donde se volcará el camino final.
	 * @param nodoObjetivo Nodo final alcanzado por el algoritmo.
	 */
	private void reconstruirCamino(final ArrayDeque<NodoA> destino, final NodoA nodoObjetivo) {
		this.caminoTemporal.clear();
		NodoA actual = nodoObjetivo;

		// Rastrearemos hacia atrás desde el objetivo hasta el inicio
		while (actual != null) {
			this.caminoTemporal.add(actual);
			actual = actual.getNodoProcedente();
		}

		// Insertamos en sentido inverso para que la cola quede ordenada desde Origen ->
		// Destino
		for (int i = this.caminoTemporal.size() - 1; i >= 0; i--) {
			destino.add(this.caminoTemporal.get(i));
		}
	}

	/**
	 * Determina si un nodo colisiona con el terreno o con objetos sólidos del
	 * mundo.
	 *
	 * @param n Nodo a comprobar.
	 * @return 'true' si hay colisión o si el nodo es nulo; 'false' si es
	 *         transitable.
	 */
	private boolean colisiona(final NodoA n) {
		if (n == null) {
			return true;
		}

		// Reutilizamos el 'Rectangle' auxiliar asignándole la posición y tamaño del
		// nodo
		this.cajaColisionAux.setBounds(n.getXNodo() * this.dimensionNodo.width,
				n.getYNodo() * this.dimensionNodo.height, this.dimensionNodo.width, this.dimensionNodo.height);

		// Comprobamos colisiones con el mapa o los objetos sólidos
		return this.mundo.getTerreno().intersectaSolidoDijkstra(this.cajaColisionAux)
				|| this.mundo.colisionaConObjetoSolido(this.cajaColisionAux);
	}

	/**
	 * Incrementa el contador de generación para la búsqueda. Si alcanza el límite
	 * máximo de un entero (`Integer.MAX_VALUE`), resetea todos los nodos a 0 para
	 * prevenir desbordamientos numéricos (Overflow).
	 */
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

	/**
	 * Vuelve a construir la matriz de nodos de la grilla (útil si el tamaño del
	 * mapa cambió).
	 */
	public void recalcularGrilla() {
		this.generarNodos();
	}

	/**
	 * Obtiene el nodo correspondiente a partir de coordenadas en píxeles del mundo.
	 *
	 * @param xRef Posición X en píxeles.
	 * @param yRef Posición Y en píxeles.
	 * @return El nodo en esa coordenada o `null` si está fuera de rango.
	 */
	public NodoA getNodoRef(final int xRef, final int yRef) {
		final int x = xRef / this.dimensionNodo.width;
		final int y = yRef / this.dimensionNodo.height;
		return this.getNodo(x, y);
	}

	/**
	 * Obtiene un nodo de la matriz validando que los índices estén dentro de los
	 * límites.
	 *
	 * @param x Índice X en la matriz.
	 * @param y Índice Y en la matriz.
	 * @return El `NodoA` correspondiente o `null` si es una posición inválida.
	 */
	private NodoA getNodo(final int x, final int y) {
		if ((x < 0) || (x >= this.anchoMatriz) || (y < 0) || (y >= this.altoMatriz)) {
			return null;
		}
		return this.nodos[x][y];
	}

	/**
	 * Inicializa la matriz bidimensional de nodos basándose en las dimensiones del
	 * terreno.
	 */
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

	/**
	 * @return La dimensión (ancho y alto en píxeles) de cada nodo.
	 */
	public Dimension getDimensionNodoA() {
		return this.dimensionNodo;
	}
}