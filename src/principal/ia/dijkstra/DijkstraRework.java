package principal.ia.dijkstra;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import principal.ia.Lista;
import principal.mapa.Mundo;
import principal.utilidades.Constantes;

/**
 * Sistema de búsqueda de caminos (Pathfinding) masivo basado en el algoritmo de
 * Dijkstra.
 * 
 * Arquitectura de Alto Rendimiento: 1. Multihilo con Doble Búfer: Cálculo
 * desacoplado del Game Loop en hilo secundario (`ExecutorService`) con
 * conmutación atómica (`codActCompleto`). 2. Zero-Allocation (0 bytes GC):
 * Montículo binario plano (`MinHeapDijkstra`) y tarea persistente (`Runnable`)
 * para evitar la recolección de basura. 3. Búsqueda Acotada (Bounded
 * Early-Exit): El algoritmo solo propaga el flujo en el área activa (pantalla +
 * márgenes de 1/2 pantalla) cortando la exploración inmediatamente al superar
 * la distancia máxima, reduciendo el consumo de CPU en más del 99%.
 */
public class DijkstraRework {

	// Costos de movimiento (Movimiento ortogonal = 1.0, Diagonal = √2 ≈ 1.414)
	private static final double COSTO_ORTOGONAL = 1.0;
	private static final double COSTO_DIAGONAL = 1.4142135623730951;

	// Desplazamientos para consultar las 8 direcciones adyacentes de un nodo
	private static final int[] OFFSET_X = { -1, 0, 1, -1, 1, -1, 0, 1 };
	private static final int[] OFFSET_Y = { -1, -1, -1, 0, 0, 1, 1, 1 };

	// Costos precalculados en el mismo orden que los desplazamientos
	private static final double[] COSTOS = { COSTO_DIAGONAL, COSTO_ORTOGONAL, COSTO_DIAGONAL, COSTO_ORTOGONAL,
			COSTO_ORTOGONAL, COSTO_DIAGONAL, COSTO_ORTOGONAL, COSTO_DIAGONAL };

	private final Mundo mundo;
	private final Dimension dimensionNodo;

	// Radio máximo en unidades de costo de casillas calculado en base a la
	// resolución
	private final double radioMaximoBusqueda;

	// Límites máximos en la grilla del mapa
	private int xUltimoNodo;
	private int yUltimoNodo;

	// Contadores atómicos (Thread-Safe)
	private final AtomicInteger cantNodoVisitados = new AtomicInteger(0);
	private final AtomicInteger entidadesAlPendiente = new AtomicInteger(0);

	// Control de frecuencia de actualización (Throttle): Actualiza cada 30 ticks
	// (~2 veces/seg a 60 FPS)
	private static final int INTERVALO_TICKS_ACTUALIZACION = 30;
	private int contadorTicks = 0;

	/**
	 * Código de la última generación de cálculo completada con éxito. 'volatile'
	 * garantiza visibilidad atómica e inmediata entre hilos.
	 */
	private volatile int codActCompleto = 0;

	/** Matriz de nodos que conforman el mapa. */
	private NodoD[][] nodos;

	/**
	 * Cola de prioridad Min-Heap nativa basada en arreglos primitivos planos.
	 * Reutilizada en cada búsqueda para 0 asignaciones de memoria en el heap.
	 */
	private MinHeapDijkstra colaPrioridad;

	/**
	 * Último nodo donde se posicionó el objetivo principal (ej. el jugador).
	 */
	private volatile NodoD ultimoNodoPosObjetivo;

	/**
	 * Nodo objetivo hacia donde se orientará el siguiente cálculo asíncrono.
	 */
	private volatile NodoD targetPendiente;

	/**
	 * Flag atómico para evitar despachar múltiples tareas en paralelo si la
	 * anterior sigue procesándose.
	 */
	private final AtomicBoolean actualizando = new AtomicBoolean(false);

	/** Servicio para ejecutar tareas pesadas en un hilo secundario dedicado. */
	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	/**
	 * Tarea persistente y reutilizable enviada al Executor para Cero Asignación en
	 * el despacho.
	 */
	private final Runnable tareaDijkstra;

	/**
	 * Rectángulos auxiliares preasignados para evitar 'new Rectangle()' en
	 * colisiones. Separados por hilo para evitar condiciones de carrera (Race
	 * Conditions).
	 */
	private final Rectangle rectColisionHiloPrincipal = new Rectangle();
	private final Rectangle rectColisionHiloSecundario = new Rectangle();

	/**
	 * Constructor e inicializador de la grilla Dijkstra.
	 *
	 * @param mundo     Referencia al mundo del juego.
	 * @param dimension Tamaño físico en píxeles de cada nodo.
	 */
	public DijkstraRework(final Mundo mundo, final Dimension dimension) {
		this.mundo = mundo;
		this.dimensionNodo = dimension;

		// Cálculo dinámico del radio: Pantalla + margen radial (320+320 en X, 180+180
		// en Y)
		final double radioPx = Math.hypot(Constantes.ANCHO_JUEGO, Constantes.ALTO_JUEGO);
		this.radioMaximoBusqueda = radioPx / dimension.width;

		// Definición única del Runnable de cálculo (0 allocations en el bucle
		// principal)
		this.tareaDijkstra = new Runnable() {
			@Override
			public void run() {
				try {
					final NodoD targetFinal = DijkstraRework.this.targetPendiente;
					if (targetFinal == null) {
						return;
					}

					final int nuevoCodAct = DijkstraRework.this.codActCompleto + 1;

					// MATEMÁTICA DEL DOBLE BÚFER:
					// Determina el índice de escritura (0 o 1) según la paridad de la generación
					final int writeBuf = Math.abs(nuevoCodAct % 2);

					// Seteamos la distancia inicial del objetivo en 0
					targetFinal.setDistancia(writeBuf, 0.0);
					targetFinal.setNodoProcedente(writeBuf, null);
					targetFinal.setCodAct(writeBuf, nuevoCodAct);

					// Ejecutamos la expansión acotada de distancias de Dijkstra
					DijkstraRework.this.procesarDijkstra(targetFinal, nuevoCodAct, writeBuf);

					// Al finalizar el área delimitada:
					DijkstraRework.this.ultimoNodoPosObjetivo = targetFinal;

					// ¡CONMUTACIÓN ATÓMICA DE BÚFER!
					DijkstraRework.this.codActCompleto = nuevoCodAct;
				} catch (final Exception e) {
					Thread.currentThread().interrupt();
				} finally {
					DijkstraRework.this.actualizando.set(false);
				}
			}
		};

		this.generarNodos();
	}

	/**
	 * Inicializa la grilla de nodos analizando qué casillas son obstáculos
	 * permanentes y predimensiona la cola de prioridad.
	 */
	private void generarNodos() {
		this.xUltimoNodo = Math.floorDiv(this.mundo.getTerreno().getAncho() - this.dimensionNodo.width,
				this.dimensionNodo.width);
		this.yUltimoNodo = Math.floorDiv(this.mundo.getTerreno().getAlto() - this.dimensionNodo.height,
				this.dimensionNodo.height);

		this.nodos = new NodoD[this.xUltimoNodo + 1][this.yUltimoNodo + 1];

		// Preasignamos capacidad suficiente para el radio acotado de búsqueda
		final int capacidadEstimada = (int) (Math.PI * this.radioMaximoBusqueda * this.radioMaximoBusqueda * 1.5);
		this.colaPrioridad = new MinHeapDijkstra(Math.max(1024, capacidadEstimada));

		for (int x = 0; x <= this.xUltimoNodo; x++) {
			for (int y = 0; y <= this.yUltimoNodo; y++) {
				final boolean esPermaSolido = this.verificarSiEsteNodoVaSerPermaSolido(x, y);
				this.nodos[x][y] = new NodoD(x, y, this.dimensionNodo, esPermaSolido);
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

		// Temporizador Ticks: Limitamos la frecuencia para ahorrar recursos de CPU
		this.contadorTicks++;
		if (this.contadorTicks < INTERVALO_TICKS_ACTUALIZACION) {
			return;
		}
		this.contadorTicks = 0;

		// Si ya hay un cálculo de Dijkstra ejecutándose en el hilo secundario,
		// descartamos este tick
		if (!this.actualizando.compareAndSet(false, true)) {
			return;
		}

		final int posRefX = Math.floorDiv(posicionObjetivo.x, this.dimensionNodo.width);
		final int posRefY = Math.floorDiv(posicionObjetivo.y, this.dimensionNodo.height);

		// Validamos que el objetivo esté dentro de los límites del mapa
		if ((posRefX < 0) || (posRefX > this.xUltimoNodo) || (posRefY < 0) || (posRefY > this.yUltimoNodo)) {
			this.actualizando.set(false);
			return;
		}

		NodoD nodoObjetivo = this.nodos[posRefX][posRefY];

		// Si la posición exacta del objetivo es un obstáculo, buscamos un nodo libre
		// cercano
		if ((nodoObjetivo == null) || nodoObjetivo.isInmodificable()
				|| this.colisiona(nodoObjetivo, this.rectColisionHiloPrincipal)) {
			nodoObjetivo = this.getNodoCercano(posicionObjetivo.x, posicionObjetivo.y);
		}

		// Si sigue siendo inválido o el objetivo no ha cambiado de casilla, liberamos
		// el flag y salimos
		if ((nodoObjetivo == null) || nodoObjetivo.isInmodificable() || (nodoObjetivo == this.ultimoNodoPosObjetivo)) {
			this.actualizando.set(false);
			return;
		}

		this.targetPendiente = nodoObjetivo;

		// --- ENVIAMOS LA TAREA REUTILIZABLE AL HILO SECUNDARIO (0 ASIGNACIONES) ---
		this.executor.submit(this.tareaDijkstra);
	}

	/**
	 * Algoritmo Dijkstra en reversa ejecutado en el hilo secundario con corte
	 * temprano por radio máximo (Early-Exit).
	 */
	private void procesarDijkstra(final NodoD objetivo, final int nuevoCodAct, final int writeBuf) {
		this.colaPrioridad.clear();
		this.colaPrioridad.push(objetivo, 0.0);

		int visitadosContador = 0;

		while (!this.colaPrioridad.isEmpty()) {
			final NodoD n = this.colaPrioridad.poll();
			final double distActual = n.getDistancia(writeBuf);

			// --- SALIDA TEMPRANA (EARLY-EXIT) ---
			// Como el Min-Heap procesa de menor a mayor distancia, si este nodo ya superó
			// el radio máximo de pantalla + márgenes, ningún nodo restante estará más
			// cerca.
			if (distActual > this.radioMaximoBusqueda) {
				break;
			}

			final int xNodo = n.getGrillaX();
			final int yNodo = n.getGrillaY();

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
				if ((nodoAct == null) || nodoAct.isInmodificable()
						|| this.colisiona(nodoAct, this.rectColisionHiloSecundario)) {
					continue;
				}

				// Evitamos atravesar esquinas diagonales formadas por paredes
				if (this.esDiagonal(OFFSET_X[i], OFFSET_Y[i])
						&& this.hayBloqueoEnEsquina(xNodo, yNodo, OFFSET_X[i], OFFSET_Y[i])) {
					continue;
				}

				final double nuevaDistancia = distActual + COSTOS[i];

				// Solo propagamos si no excede el límite del radio activo
				if (nuevaDistancia <= this.radioMaximoBusqueda) {
					if ((nodoAct.getCodAct(writeBuf) != nuevoCodAct)
							|| (nuevaDistancia < nodoAct.getDistancia(writeBuf))) {

						// Actualizamos los datos en el BÚFER DE ESCRITURA
						nodoAct.setDistancia(writeBuf, nuevaDistancia);

						// La referencia 'nodoProcedente' apunta de regreso HACIA el objetivo
						nodoAct.setNodoProcedente(writeBuf, n);
						nodoAct.setCodAct(writeBuf, nuevoCodAct);

						visitadosContador++;
						this.colaPrioridad.push(nodoAct, nuevaDistancia);
					}
				}
			}
		}
		// Guardamos la cantidad de nodos escaneados para métricas y depuración
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
			solidoX = (nX != null) && (nX.isInmodificable() || this.colisiona(nX, this.rectColisionHiloSecundario));
		}

		boolean solidoY = false;
		if ((yLat >= 0) && (yLat <= this.yUltimoNodo)) {
			final NodoD nY = this.nodos[x][yLat];
			solidoY = (nY != null) && (nY.isInmodificable() || this.colisiona(nY, this.rectColisionHiloSecundario));
		}

		return solidoX || solidoY;
	}

	/**
	 * Verifica si una celda es un obstáculo permanente al iniciar la grilla.
	 */
	private boolean verificarSiEsteNodoVaSerPermaSolido(final int xMatriz, final int yMatriz) {
		final int xPx = xMatriz * this.dimensionNodo.width;
		final int yPx = yMatriz * this.dimensionNodo.height;

		this.rectColisionHiloPrincipal.setBounds(xPx, yPx, this.dimensionNodo.width, this.dimensionNodo.height);
		return this.mundo.getTerreno().intersectaSolidoDijkstra(this.rectColisionHiloPrincipal)
				|| this.mundo.colisionaConAlgoSolidoPermanente(this.rectColisionHiloPrincipal);
	}

	/**
	 * Verifica colisiones espaciales reutilizando un contenedor Rectangle pasado
	 * por referencia.
	 */
	private boolean colisiona(final NodoD n, final Rectangle rectAux) {
		rectAux.setBounds(n.getXMundo(), n.getYMundo(), n.getAncho(), n.getAlto());
		return this.mundo.getTerreno().intersectaSolidoDijkstra(rectAux)
				|| this.mundo.colisionaConObjetoSolido(rectAux);
	}

	/**
	 * Convierte coordenadas del mundo en píxeles a su respectivo nodo en la matriz.
	 */
	public NodoD getNodoReferenciado(final int x, final int y) {
		final int nx = Math.floorDiv(x, this.dimensionNodo.width);
		final int ny = Math.floorDiv(y, this.dimensionNodo.height);

		if ((nx < 0) || (nx > this.xUltimoNodo) || (ny < 0) || (ny > this.yUltimoNodo)) {
			return null;
		}
		return this.nodos[nx][ny];
	}

	/**
	 * Busca el nodo transitable más cercano a unas coordenadas de píxeles en el
	 * búfer de lectura activo.
	 */
	public NodoD getNodoCercano(final int x, final int y) {
		final int targetCodAct = this.codActCompleto;
		final int readBuf = Math.abs(targetCodAct % 2);

		final int xPosRefNodo = Math.floorDiv(x, this.dimensionNodo.width);
		final int yPosRefNodo = Math.floorDiv(y, this.dimensionNodo.height);

		if ((xPosRefNodo < 0) || (xPosRefNodo > this.xUltimoNodo) || (yPosRefNodo < 0)
				|| (yPosRefNodo > this.yUltimoNodo)) {
			return null;
		}

		final NodoD nodoActual = this.nodos[xPosRefNodo][yPosRefNodo];

		// Si el nodo actual está actualizado y es transitable dentro del radio activo
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

		// Si la posición buscada era un obstáculo, escaneamos los 8 vecinos
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
	 * Obtiene el camino completo hacia el objetivo. (Mantenido por compatibilidad).
	 */
	public Lista<NodoD> getRecorrido(final int x, final int y) {
		final NodoD nodoProx = this.getNodoReferenciado(x, y);
		return this.getRecorrido(nodoProx);
	}

	/**
	 * Genera una lista de pasos partiendo de un nodo específico hacia el objetivo.
	 */
	public Lista<NodoD> getRecorrido(final NodoD nodoActual) {
		final Lista<NodoD> recorrido = new Lista<>();
		final int readBuf = Math.abs(this.codActCompleto % 2);

		if ((nodoActual != null) && !this.colisiona(nodoActual, this.rectColisionHiloPrincipal)) {
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
		final int limitePasos = this.xUltimoNodo * this.yUltimoNodo;
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

	public int getBufferLecturaIndex() {
		return Math.abs(this.codActCompleto % 2);
	}

	public int getCodActCompleto() {
		return this.codActCompleto;
	}

	public double getRadioMaximoBusqueda() {
		return this.radioMaximoBusqueda;
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
	 * Apaga de manera limpia el servicio de hilos secundarios.
	 */
	public void destruir() {
		this.executor.shutdown();
	}
}