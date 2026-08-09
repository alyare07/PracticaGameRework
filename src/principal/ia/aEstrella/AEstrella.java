package principal.ia.aEstrella;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

import principal.ia.Lista;
import principal.mapa.Mundo;

/**
 * Gestor del algoritmo de búsqueda de caminos A* (A-Star). Diseñado para el
 * cálculo de rutas punto a punto independientes en criaturas/enemigos. Utiliza
 * una matriz unificada de nodos en memoria con acceso O(1) y reinicio mediante
 * ID de actualización.
 */
public class AEstrella {

	/** Lista de nodos por explorar, ordenada automáticamente por menor costo F. */
	private final PriorityQueue<NodoA> listaAbierta;

	/** Conjunto de nodos que ya han sido evaluados en la búsqueda actual. */
	private final Set<NodoA> listaCerrada;

	/**
	 * Referencia al mundo del juego para verificar colisiones físicas y del mapa.
	 */
	private final Mundo mundo;

	/** Dimensiones de cada tile/nodo en píxeles. */
	private final Dimension dimensionNodo;

	/** Matriz bidimensional de nodos que representa la grilla del mapa. */
	private NodoA[][] nodos;

	/** Ancho de la matriz de nodos en celdas. */
	private int anchoMatriz;

	/** Alto de la matriz de nodos en celdas. */
	private int altoMatriz;

	/**
	 * Código de actualización único para cada cálculo de ruta. Permite reutilizar
	 * las mismas instancias de NodoA sin instanciar nuevos objetos en cada
	 * búsqueda.
	 */
	private int codAct = Integer.MIN_VALUE;

	/**
	 * Constructor de la IA A*.
	 * 
	 * @param mundo         Referencia al mapa/mundo activo.
	 * @param dimensionNodo Tamaño de cada celda de navegación en píxeles.
	 */
	public AEstrella(final Mundo mundo, final Dimension dimensionNodo) {
		this.mundo = mundo;
		this.dimensionNodo = dimensionNodo;
		this.listaAbierta = new PriorityQueue<>();
		this.listaCerrada = new HashSet<>();
		this.generarNodos();
	}

	/**
	 * Calcula el camino más corto entre dos puntos del mapa utilizando el algoritmo
	 * A*.
	 * 
	 * @param xInicial  Posición X inicial en coordenadas del mundo (píxeles).
	 * @param yInicial  Posición Y inicial en coordenadas del mundo (píxeles).
	 * @param xObjetivo Posición X destino en coordenadas del mundo (píxeles).
	 * @param yObjetivo Posición Y destino en coordenadas del mundo (píxeles).
	 * @return Una 'Lista' personalizada con la secuencia ordenada de nodos desde el
	 *         origen hasta el destino.
	 */
	public Lista<NodoA> getRecorrido(final int xInicial, final int yInicial, final int xObjetivo, final int yObjetivo) {
		final Lista<NodoA> recorrido = new Lista<NodoA>();

		// Obtener las referencias de los nodos a partir de coordenadas en píxeles
		final NodoA nodoInicial = this.getNodoRef(xInicial, yInicial);
		final NodoA nodoObjetivo = this.getNodoRef(xObjetivo, yObjetivo);

		// Validaciones de seguridad previas
		if ((nodoInicial == null) || (nodoObjetivo == null) || (nodoInicial == nodoObjetivo)
				|| this.colisiona(nodoObjetivo)) {
			return recorrido;
		}

		// Preparar el nuevo ciclo de búsqueda
		this.actualizarCodAct();
		this.listaAbierta.clear();
		this.listaCerrada.clear();

		// Configurar y evaluar el nodo inicial (origen)
		nodoInicial.reiniciar(this.codAct);
		nodoInicial.evaluar(null, nodoObjetivo, 0);
		this.listaAbierta.add(nodoInicial);

		// Bucle principal de exploración
		while (!this.listaAbierta.isEmpty()) {
			// Extraer el nodo con el menor costo F acumulado
			final NodoA nodoAct = this.listaAbierta.poll();

			// Condición de victoria: Se alcanzó la meta
			if (nodoAct == nodoObjetivo) {
				this.reconstruirCamino(recorrido, nodoObjetivo);
				return recorrido;
			}

			this.listaCerrada.add(nodoAct);

			// Procesar cada uno de los nodos adyacentes (hasta 8 vecinos)
			for (final NodoA vecino : this.getNodosVecinos(nodoAct)) {
				// Descartar vecinos que sean sólidos o que ya hayan sido procesados
				if (this.colisiona(vecino) || this.listaCerrada.contains(vecino)) {
					continue;
				}

				// Si el vecino proviene de una búsqueda anterior, se reinician sus valores
				if (!vecino.visitado(this.codAct)) {
					vecino.reiniciar(this.codAct);
				}

				// Asignar costo de movimiento: 1.414 para diagonales, 1.0 para
				// horizontales/verticales
				final double costoPaso = ((vecino.getXNodo() != nodoAct.getXNodo())
						&& (vecino.getYNodo() != nodoAct.getYNodo())) ? 1.414 : 1.0;
				final double nuevoCostoG = nodoAct.getCostoG() + costoPaso;

				final boolean estaEnAbierta = this.listaAbierta.contains(vecino);

				// Si encontramos un camino más corto hacia este vecino, o si es la primera vez
				// que se descubre
				if (!estaEnAbierta || (nuevoCostoG < vecino.getCostoG())) {
					vecino.evaluar(nodoAct, nodoObjetivo, costoPaso);

					// Si ya estaba en la PriorityQueue, se quita y re-inserta para forzar el
					// reordenamiento
					if (estaEnAbierta) {
						this.listaAbierta.remove(vecino);
					}
					this.listaAbierta.add(vecino);
				}
			}
		}

		return recorrido;
	}

	/**
	 * Reconstruye la ruta final trazando la cadena de nodos procedentes desde el
	 * objetivo hasta el origen y la guarda en la Lista personalizada en el orden
	 * correcto.
	 */
	private void reconstruirCamino(final Lista<NodoA> destino, final NodoA nodoObjetivo) {
		final ArrayList<NodoA> temporal = new ArrayList<>();
		NodoA actual = nodoObjetivo;

		// Rastrear el camino hacia atrás usando las referencias de nodoProcedente
		while (actual != null) {
			temporal.add(actual);
			actual = actual.getNodoProcedente();
		}

		// Invertir el orden para que quede desde el Inicio -> Destino
		Collections.reverse(temporal);

		// Transferir el resultado a la estructura Lista del proyecto
		for (final NodoA n : temporal) {
			destino.add(n);
		}
	}

	/**
	 * Evalúa si un nodo específico está bloqueado por capas de colisión o elementos
	 * sólidos del mapa.
	 */
	private boolean colisiona(final NodoA n) {
		if (n == null) {
			return true;
		}
		return this.mundo.getTerreno().intersectaSolidoDijkstra(n.getAreaEnMundo())
				|| this.mundo.colisionaConObjetoSolido(n.getAreaEnMundo());
	}

	/**
	 * Obtiene los nodos adyacentes (hasta 8 direcciones: ortogonales y diagonales).
	 */
	private ArrayList<NodoA> getNodosVecinos(final NodoA nodoAct) {
		final ArrayList<NodoA> vecinos = new ArrayList<>(8);
		final int x = nodoAct.getXNodo();
		final int y = nodoAct.getYNodo();

		for (int i = x - 1; i <= (x + 1); i++) {
			for (int j = y - 1; j <= (y + 1); j++) {
				if ((i == x) && (j == y)) {
					continue; // Omitir el mismo nodo central
				}

				final NodoA vecino = this.getNodo(i, j);
				if (vecino != null) {
					vecinos.add(vecino);
				}
			}
		}
		return vecinos;
	}

	/**
	 * Incrementa el identificador de búsqueda. Si llega al límite máximo de entero,
	 * reinicia la cuenta para evitar desbordamiento.
	 */
	private void actualizarCodAct() {
		if (this.codAct == Integer.MAX_VALUE) {
			this.codAct = Integer.MIN_VALUE;
		} else {
			this.codAct++;
		}
	}

	/**
	 * Convierte coordenadas absolutas en píxeles del mundo a una referencia de
	 * NodoA en la matriz.
	 */
	public NodoA getNodoRef(final int xRef, final int yRef) {
		final int x = xRef / this.dimensionNodo.width;
		final int y = yRef / this.dimensionNodo.height;
		return this.getNodo(x, y);
	}

	/**
	 * Acceso seguro a la matriz bidimensional de nodos comprobando límites.
	 */
	private NodoA getNodo(final int x, final int y) {
		if ((x < 0) || (x >= this.anchoMatriz) || (y < 0) || (y >= this.altoMatriz)) {
			return null;
		}
		return this.nodos[x][y];
	}

	/**
	 * Construye e inicializa la matriz bidimensional de celdas según las
	 * dimensiones del terreno.
	 */
	private void generarNodos() {
		this.anchoMatriz = (this.mundo.getTerreno().getAncho()) / this.dimensionNodo.width;
		this.altoMatriz = (this.mundo.getTerreno().getAlto()) / this.dimensionNodo.height;

		this.nodos = new NodoA[this.anchoMatriz][this.altoMatriz];

		for (int x = 0; x < this.anchoMatriz; x++) {
			for (int y = 0; y < this.altoMatriz; y++) {
				this.nodos[x][y] = new NodoA(x, y, this.dimensionNodo);
			}
		}
	}
}