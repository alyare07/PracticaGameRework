package principal.ia.dijkstra;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import principal.ia.Lista;
import principal.mapa.Mundo;

/**
 * Sistema de búsqueda de caminos (Pathfinding) masivo basado en el algoritmo de
 * Dijkstra.
 * 
 * ¿Por qué usar Dijkstra en lugar de A*? Mientras que A* calcula el camino de
 * UN personaje hacia UN destino, Dijkstra genera un "Mapa de Flujo" o "Campo de
 * Distancias" desde el objetivo (ej. el Jugador) hacia TODAS las casillas del
 * mapa. Esto permite que DECENAS o CIENTOS de enemigos persigan al jugador
 * usando una sola consulta de cálculo, optimizando enormemente el rendimiento.
 * 
 * Arquitectura Multihilo con Doble Búfer: El cálculo se realiza en un hilo
 * secundario (`ExecutorService`) para no congelar los fotogramas del juego.
 * Para evitar que el hilo del juego lea datos a medio calcular, se usará un
 * esquema de Doble Búfer: - El hilo secundario escribe los resultados en el
 * "Búfer de Escritura" (Write Buffer). - El hilo del juego lee los caminos del
 * "Búfer de Lectura" (Read Buffer). - Cuando el hilo secundario termina,
 * conmuta los búferes atómicamente sin pausar el juego.
 */
public class DijkstraRework {

	// Costos de movimiento (Movimiento recto = 1.0, Movimiento en diagonal = √2 ≈
	// 1.414)
	private static final double COSTO_ORTOGONAL = 1.0;
	private static final double COSTO_DIAGONAL = 1.4142135623730951;

	// Desplazamientos para consultar las 8 direcciones adyacentes de un nodo
	private static final int[] OFFSET_X = { -1, 0, 1, -1, 1, -1, 0, 1 };
	private static final int[] OFFSET_Y = { -1, -1, -1, 0, 0, 1, 1, 1 };

	// Costos precalculados en el mismo orden que los offsets para acelerar
	// búsquedas
	private static final double[] COSTOS = { COSTO_DIAGONAL, COSTO_ORTOGONAL, COSTO_DIAGONAL, COSTO_ORTOGONAL,
			COSTO_ORTOGONAL, COSTO_DIAGONAL, COSTO_ORTOGONAL, COSTO_DIAGONAL };

	private final Mundo mundo;
	private final Dimension dimensionNodo;

	// Límites máximos en la grilla del mapa
	private int xUltimoNodo;
	private int yUltimoNodo;

	// Contadores atómicos (Thread-Safe): Se pueden modificar desde varios hilos sin
	// riesgo de corrupción
	private final AtomicInteger cantNodoVisitados = new AtomicInteger(0);
	private final AtomicInteger entidadesAlPendiente = new AtomicInteger(0);

	// Control de frecuencia de actualización (Throttle): No hace falta recalcular
	// Dijkstra en cada FPS.
	// Se actualizará cada 30 ticks (aproximadamente cada medio segundo a 60 FPS).
	private static final int INTERVALO_TICKS_ACTUALIZACION = 30;
	private int contadorTicks = 0;

	/**
	 * Código/Número de la última generación de cálculo que ha sido completada con
	 * éxito. La palabra clave 'volatile' garantiza que cuando este entero cambie en
	 * el hilo secundario, el hilo principal del juego lo detecte inmediatamente en
	 * memoria.
	 */
	private volatile int codActCompleto = 0;

	/** Matriz de nodos que conforman el mapa. */
	private NodoD[][] nodos;

	/**
	 * Último nodo donde se posicionó el objetivo principal (ej. la posición del
	 * jugador).
	 */
	private volatile NodoD ultimoNodoPosObjetivo;

	/**
	 * Flag atómico para evitar lanzar múltiples tareas de procesamiento de Dijkstra
	 * en paralelo si la anterior no ha terminado.
	 */
	private final AtomicBoolean actualizando = new AtomicBoolean(false);

	/** Servicio para ejecutar tareas pesadas en un hilo secundario dedicado. */
	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	/**
	 * Estructura auxiliar interna utilizada dentro de la Cola de Prioridad del
	 * algoritmo Dijkstra.
	 */
	private static final class PathNode {
		private final NodoD nodo;
		private final double distancia;

		public PathNode(final NodoD nodo, final double distancia) {
			this.nodo = nodo;
			this.distancia = distancia;
		}
	}

	/**
	 * Constructor e inicializador de la grilla Dijkstra.
	 *
	 * @param mundo     Referencia al mundo del juego.
	 * @param dimension Tamaño físico en píxeles de cada nodo.
	 */
	public DijkstraRework(final Mundo mundo, final Dimension dimension) {
		this.mundo = mundo;
		this.dimensionNodo = dimension;
		this.generarNodos();
	}

	/**
	 * Inicializa la grilla de nodos analizando qué casillas son obstáculos
	 * permanentes desde el inicio.
	 */
	private void generarNodos() {
		this.xUltimoNodo = (this.mundo.getTerreno().getAncho() - this.dimensionNodo.width) / this.dimensionNodo.width;
		this.yUltimoNodo = (this.mundo.getTerreno().getAlto() - this.dimensionNodo.height) / this.dimensionNodo.height;

		this.nodos = new NodoD[this.xUltimoNodo + 1][this.yUltimoNodo + 1];

		for (int x = 0; x <= this.xUltimoNodo; x++) {
			for (int y = 0; y <= this.yUltimoNodo; y++) {
				final boolean esPermaSolido = this.verificarSiEsteNodoVaSerPermaSolido(x, y);
				final Point pos = new Point(x, y);
				this.nodos[x][y] = new NodoD(pos, this.dimensionNodo, esPermaSolido);
			}
		}
	}

	/**
	 * Solicita la actualización periódica del mapa de Dijkstra apuntando a la nueva
	 * posición del objetivo. Se ejecuta en el Hilo Principal del juego.
	 *
	 * @param posicionObjetivo Posición en píxeles hacia donde deben orientarse los
	 *                         caminos (ej. el jugador).
	 */
	public void actualizar(final Point posicionObjetivo) {
		if (posicionObjetivo == null) {
			return;
		}

		// Temporizador Ticks: Limitamos la frecuencia para ahorrar recursos del
		// procesador
		this.contadorTicks++;
		if (this.contadorTicks < INTERVALO_TICKS_ACTUALIZACION) {
			return;
		}
		this.contadorTicks = 0;

		// Si ya hay un cálculo de Dijkstra ejecutándose en el hilo secundario,
		// descartamos este tick.
		// `compareAndSet(false, true)` cambia de false a true de forma atómica.
		if (!this.actualizando.compareAndSet(false, true)) {
			return;
		}

		final int posRefX = posicionObjetivo.x / this.dimensionNodo.width;
		final int posRefY = posicionObjetivo.y / this.dimensionNodo.height;

		// Validamos que el objetivo esté dentro de los límites del mapa
		if ((posRefX < 0) || (posRefX > this.xUltimoNodo) || (posRefY < 0) || (posRefY > this.yUltimoNodo)) {
			this.actualizando.set(false);
			return;
		}

		NodoD nodoObjetivo = this.nodos[posRefX][posRefY];

		// Si la posición exacta del objetivo es un obstáculo, buscamos un nodo libre
		// cercano
		if ((nodoObjetivo == null) || nodoObjetivo.isInmodificable() || this.colisiona(nodoObjetivo)) {
			nodoObjetivo = this.getNodoCercano(posicionObjetivo.x, posicionObjetivo.y);
		}

		// Si sigue siendo inválido o el objetivo no ha cambiado de casilla, liberamos
		// el flag y salimos
		if ((nodoObjetivo == null) || nodoObjetivo.isInmodificable() || (nodoObjetivo == this.ultimoNodoPosObjetivo)) {
			this.actualizando.set(false);
			return;
		}

		final NodoD targetFinal = nodoObjetivo;

		// --- ENVIAMOS LA TAREA PESADA AL HILO SECUNDARIO ---
		this.executor.submit(() -> {
			try {
				final int nuevoCodAct = this.codActCompleto + 1;

				// MATEMÁTICA DEL DOBLE BÚFER:
				// Si `nuevoCodAct` es par (ej. 2), `2 % 2 = 0` -> Escribe en Búfer 0.
				// Si `nuevoCodAct` es impar (ej. 3), `3 % 2 = 1` -> Escribe en Búfer 1.
				final int writeBuf = Math.abs(nuevoCodAct % 2);

				// Seteamos la distancia inicial del objetivo en 0
				targetFinal.setDistancia(writeBuf, 0);
				targetFinal.setNodoProcedente(writeBuf, null);
				targetFinal.setCodAct(writeBuf, nuevoCodAct);

				// Ejecutamos la expansión de distancias de Dijkstra
				this.procesarDijkstra(targetFinal, nuevoCodAct, writeBuf);

				// Al finalizar todo el mapa:
				this.ultimoNodoPosObjetivo = targetFinal;

				// ¡CONMUTACIÓN ATÓMICA DE BÚFER!
				// Al actualizar `codActCompleto`, instantáneamente el hilo del juego leerá el
				// nuevo búfer sin pausar nada.
				this.codActCompleto = nuevoCodAct;
			} catch (final Exception e) {
				Thread.currentThread().interrupt();
			} finally {
				// Indicamos que el hilo secundario ha quedado libre para una nueva consulta
				this.actualizando.set(false);
			}
		});
	}

	/**
	 * Algoritmo Dijkstra en reversa: Empieza en el objetivo (distancia = 0) y va
	 * propagando la distancia acumulada hacia afuera. Escribe la nueva generación
	 * exclusivamente en el búfer asignado (`writeBuf`).
	 */
	private void procesarDijkstra(final NodoD objetivo, final int nuevoCodAct, final int writeBuf) {
		// Cola de prioridad que ordena los nodos por la menor distancia calculada
		final PriorityQueue<PathNode> pq = new PriorityQueue<>(Comparator.comparingDouble(p -> p.distancia));

		pq.add(new PathNode(objetivo, 0.0));
		int visitadosContador = 0;

		while (!pq.isEmpty()) {
			final PathNode actual = pq.poll();
			final NodoD n = actual.nodo;

			// Si encontramos un camino registrado previamente más corto que este, ignoramos
			// este camino obsoleto
			if (actual.distancia > n.getDistancia(writeBuf)) {
				continue;
			}

			final int xNodo = n.getPosicion().x;
			final int yNodo = n.getPosicion().y;

			// Evaluamos las 8 celdas vecinas
			for (int i = 0; i < OFFSET_X.length; i++) {
				final int nx = xNodo + OFFSET_X[i];
				final int ny = yNodo + OFFSET_Y[i];

				// Validamos límites del mapa
				if ((nx < 0) || (nx > this.xUltimoNodo) || (ny < 0) || (ny > this.yUltimoNodo)) {
					continue;
				}

				final NodoD nodoAct = this.nodos[nx][ny];

				// Omitimos obstáculo o pared
				if ((nodoAct == null) || nodoAct.isInmodificable() || this.colisiona(nodoAct)) {
					continue;
				}

				// Evitamos atravesar esquinas formadas por paredes
				if (this.esDiagonal(OFFSET_X[i], OFFSET_Y[i])
						&& this.hayBloqueoEnEsquina(xNodo, yNodo, OFFSET_X[i], OFFSET_Y[i])) {
					continue;
				}

				final double nuevaDistancia = n.getDistancia(writeBuf) + COSTOS[i];

				// Si el vecino pertenece a una búsqueda vieja o si encontramos una ruta hacia
				// él más corta:
				if ((nodoAct.getCodAct(writeBuf) != nuevoCodAct) || (nuevaDistancia < nodoAct.getDistancia(writeBuf))) {

					// Actualizamos los datos en el BÚFER DE ESCRITURA
					nodoAct.setDistancia(writeBuf, nuevaDistancia);

					// La flecha 'nodoProcedente' apunta de regreso HACIA el objetivo (para que las
					// IA sepan hacia dónde caminar)
					nodoAct.setNodoProcedente(writeBuf, n);
					nodoAct.setCodAct(writeBuf, nuevoCodAct);

					visitadosContador++;
					pq.add(new PathNode(nodoAct, nuevaDistancia));
				}
			}
		}
		// Guardamos la cantidad de nodos escaneados para métricas/depuración
		this.cantNodoVisitados.set(visitadosContador);
	}

	private boolean esDiagonal(final int dx, final int dy) {
		return (dx != 0) && (dy != 0);
	}

	/**
	 * Previene que las entidades atraviesen en diagonal dos esquinas de obstáculos
	 * sólidos.
	 */
	private boolean hayBloqueoEnEsquina(final int x, final int y, final int dx, final int dy) {
		final int xLat = x + dx;
		final int yLat = y + dy;

		boolean solidoX = false;
		if ((xLat >= 0) && (xLat <= this.xUltimoNodo)) {
			final NodoD nX = this.nodos[xLat][y];
			solidoX = (nX != null) && (nX.isInmodificable() || this.colisiona(nX));
		}

		boolean solidoY = false;
		if ((yLat >= 0) && (yLat <= this.yUltimoNodo)) {
			final NodoD nY = this.nodos[x][yLat];
			solidoY = (nY != null) && (nY.isInmodificable() || this.colisiona(nY));
		}

		return solidoX || solidoY;
	}

	/**
	 * Verifica si una celda es un obstáculo que jamás cambiará durante el juego.
	 */
	private boolean verificarSiEsteNodoVaSerPermaSolido(final int xMatriz, final int yMatriz) {
		final int xPx = xMatriz * this.dimensionNodo.width;
		final int yPx = yMatriz * this.dimensionNodo.height;

		final Rectangle areaNodo = new Rectangle(xPx, yPx, this.dimensionNodo.width, this.dimensionNodo.height);
		return this.mundo.getTerreno().intersectaSolidoDijkstra(areaNodo)
				|| this.mundo.colisionaConAlgoSolidoPermanente(areaNodo);
	}

	/**
	 * Verifica colisión en tiempo real (útil si hay puertas u objetos
	 * destructibles/dinámicos).
	 */
	private boolean colisiona(final NodoD n) {
		return this.mundo.getTerreno().intersectaSolidoDijkstra(n.getArea())
				|| this.mundo.colisionaConObjetoSolido(n.getArea());
	}

	/**
	 * Convierte coordenadas en píxeles a su respectivo nodo en la matriz.
	 */
	public NodoD getNodoReferenciado(final int x, final int y) {
		final int nx = x / this.dimensionNodo.width;
		final int ny = y / this.dimensionNodo.height;

		if ((nx < 0) || (nx > this.xUltimoNodo) || (ny < 0) || (ny > this.yUltimoNodo)) {
			return null;
		}
		return this.nodos[nx][ny];
	}

	/**
	 * Busca el nodo transitable más cercano a unas coordenadas de píxeles. Realiza
	 * la lectura garantizando el aislamiento del hilo gracias a `readBuf`.
	 */
	public NodoD getNodoCercano(final int x, final int y) {
		final int targetCodAct = this.codActCompleto;

		// Determinamos el índice del BÚFER DE LECTURA activo
		final int readBuf = Math.abs(targetCodAct % 2);

		final int xPosRefNodo = x / this.dimensionNodo.width;
		final int yPosRefNodo = y / this.dimensionNodo.height;

		if ((xPosRefNodo < 0) || (xPosRefNodo > this.xUltimoNodo) || (yPosRefNodo < 0)
				|| (yPosRefNodo > this.yUltimoNodo)) {
			return null;
		}

		final NodoD nodoActual = this.nodos[xPosRefNodo][yPosRefNodo];

		// Si el nodo actual está actualizado y es transitable
		if ((nodoActual != null) && (nodoActual.getCodAct(readBuf) == targetCodAct)
				&& (nodoActual.getDistancia(readBuf) != Double.MAX_VALUE)) {
			if (nodoActual.getDistancia(readBuf) == 0) {
				return nodoActual;
			}
			if (nodoActual.getNodoProcedente(readBuf) != null) {
				return nodoActual.getNodoProcedente(readBuf);
			}
		}

		NodoD nodoCercano = null;

		// Si la posición buscada era un obstáculo, escaneamos los 8 vecinos para dar el
		// nodo libre más cercano
		for (int i = 0; i < OFFSET_X.length; i++) {
			final int nx = xPosRefNodo + OFFSET_X[i];
			final int ny = yPosRefNodo + OFFSET_Y[i];

			if ((nx < 0) || (nx > this.xUltimoNodo) || (ny < 0) || (ny > this.yUltimoNodo)) {
				continue;
			}

			final NodoD nodoAux = this.nodos[nx][ny];
			if ((nodoAux != null) && (nodoAux.getDistancia(readBuf) != Double.MAX_VALUE)
					&& (nodoAux.getCodAct(readBuf) == targetCodAct)) {
				if ((nodoCercano == null) || (nodoAux.getDistancia(readBuf) < nodoCercano.getDistancia(readBuf))) {
					nodoCercano = nodoAux;
				}
			}
		}
		return nodoCercano;
	}

	/**
	 * Obtiene el camino completo a seguir desde unas coordenadas del mundo (en
	 * píxeles).
	 */
	public Lista<NodoD> getRecorrido(final int x, final int y) {
		final NodoD nodoProx = this.getNodoReferenciado(x, y);
		return this.getRecorrido(nodoProx);
	}

	/**
	 * Genera una lista de pasos partiendo de un nodo específico hacia el objetivo
	 * actual.
	 *
	 * @param nodoActual Nodo donde se encuentra la entidad (enemigo/NPC).
	 * @return Una lista ordenada de nodos a recorrer.
	 */
	public Lista<NodoD> getRecorrido(final NodoD nodoActual) {
		final Lista<NodoD> recorrido = new Lista<>();
		final int readBuf = Math.abs(this.codActCompleto % 2);

		if ((nodoActual != null) && !this.mundo.colisionaConObjetoSolido(nodoActual.getArea())) {
			this.generarRecorridoIterativo(recorrido, nodoActual, readBuf);
		}
		return recorrido;
	}

	/**
	 * Recorre los nodos enlazados mediante `nodoProcedente` desde la posición de la
	 * entidad hasta llegar al destino final.
	 */
	private void generarRecorridoIterativo(final List<NodoD> lista, final NodoD inicio, final int readBuf) {
		NodoD actual = inicio;
		final int limitePasos = this.xUltimoNodo * this.yUltimoNodo; // Máximo número de casillas del mapa
		int pasos = 0;

		while ((actual != null) && (pasos < limitePasos)) {
			lista.add(actual);
			if (actual == this.ultimoNodoPosObjetivo) {
				break;
			}
			actual = actual.getNodoProcedente(readBuf);
			pasos++;
		}
	}

	// --- MÉTODOS DE ESTADO Y CONTADORES DE CONCURRENCIA ---

	/**
	 * @return El índice (0 o 1) del búfer que el hilo del juego está leyendo
	 *         actualmente.
	 */
	public int getBufferLecturaIndex() {
		return Math.abs(this.codActCompleto % 2);
	}

	/**
	 * @return El número de la última generación de búsqueda completada.
	 */
	public int getCodActCompleto() {
		return this.codActCompleto;
	}

	public void aumentarEntidadesPendientes() {
		this.entidadesAlPendiente.incrementAndGet();
	}

	public void reducirEntidadesPendientes() {
		this.entidadesAlPendiente.updateAndGet(val -> Math.max(0, val - 1));
	}

	public boolean isActualizando() {
		return this.actualizando.get();
	}

	public boolean hayEntidadesAlPendiente() {
		return this.entidadesAlPendiente.get() > 0;
	}

	public int getCantNodoVisitados() {
		return this.cantNodoVisitados.get();
	}

	public Dimension getDimensionNodo() {
		return this.dimensionNodo;
	}

	public Mundo getMundo() {
		return this.mundo;
	}

	/**
	 * Apaga de manera limpia el servicio de hilos secundarios. Debe llamarse al
	 * cerrar el juego.
	 */
	public void destruir() {
		this.executor.shutdown();
	}
}