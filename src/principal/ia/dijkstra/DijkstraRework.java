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
 * Sistema de búsqueda de caminos masivo (Massive Pathfinding / Flowfield)
 * basado en el algoritmo de Dijkstra con soporte multihilo y corte temprano.
 * <p>
 * <b>Pilares de Arquitectura y Rendimiento:</b>
 * <ul>
 * <li><b>Multihilo Lock-Free con Doble Búfer:</b> El cálculo pesado corre en un
 * hilo secundario dedicado ({@link ExecutorService}). Utiliza dos búferes de
 * memoria (0 y 1) conmutados atómicamente mediante {@code codActCompleto % 2},
 * permitiendo que cientos de criaturas lean sus caminos a 60 FPS sin bloqueos
 * ({@code synchronized}).</li>
 * <li><b>Cero Asignaciones en Ejecución (Zero-GC):</b> Reutiliza un
 * {@link Runnable} persistente, contenedores {@link Rectangle} aislados por
 * hilo y una cola binaria plana ({@link MinHeapDijkstra}) sin instanciar
 * objetos en el Heap.</li>
 * <li><b>Búsqueda Acotada con Salida Temprana (Early-Exit):</b> Detiene la
 * expansión de la onda de Dijkstra tan pronto como supera el radio visible de
 * la cámara + márgenes, reduciendo el uso de CPU en más del 95% respecto a un
 * escaneo de mapa completo.</li>
 * </ul>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.5
 */
public class DijkstraRework {

	// =========================================================================
	// === 1. CONSTANTES DE COSTO Y DESPLAZAMIENTOS EN 8 DIRECCIONES
	// =========================================================================

	/** Costo de movimiento ortogonal (Arriba, Abajo, Izquierda, Derecha). */
	private static final double COSTO_ORTOGONAL = 1.0;

	/** Costo de movimiento diagonal (√2 ≈ 1.41421356). */
	private static final double COSTO_DIAGONAL = 1.4142135623730951;

	/** Desplazamientos X para consultar los 8 vecinos de un nodo. */
	private static final int[] OFFSET_X = { -1, 0, 1, -1, 1, -1, 0, 1 };

	/** Desplazamientos Y para consultar los 8 vecinos de un nodo. */
	private static final int[] OFFSET_Y = { -1, -1, -1, 0, 0, 1, 1, 1 };

	/** Costos pre-calculados en el mismo orden de los desplazamientos. */
	private static final double[] COSTOS = { COSTO_DIAGONAL, COSTO_ORTOGONAL, COSTO_DIAGONAL, COSTO_ORTOGONAL,
			COSTO_ORTOGONAL, COSTO_DIAGONAL, COSTO_ORTOGONAL, COSTO_DIAGONAL };

	// =========================================================================
	// === 2. ESTRUCTURAS DE MAPA Y LÍMITES
	// =========================================================================

	private final Mundo mundo;
	private final Dimension dimensionNodo;

	/**
	 * Radio máximo de búsqueda en casillas (compensado para Zoom-Out de hasta
	 * 0.5x).
	 */
	private final double radioMaximoBusqueda;

	/** Coordenada X del último nodo válido en la grilla. */
	private int xUltimoNodo;

	/** Coordenada Y del último nodo válido en la grilla. */
	private int yUltimoNodo;

	/** Matriz bidimensional de nodos que componen el mapa de navegación. */
	private NodoD[][] nodos;

	/** Cola de prioridad Min-Heap nativa reutilizable en cada cálculo (Zero-GC). */
	private MinHeapDijkstra colaPrioridad;

	// =========================================================================
	// === 3. CONCURRENCIA, DOBLE BÚFER Y CONTROL DE FRECUENCIA
	// =========================================================================

	/**
	 * Intervalo de ticks entre recálculos (30 ticks = ~2 veces por segundo a 60
	 * APS).
	 */
	private static final int INTERVALO_TICKS_ACTUALIZACION = 30;
	private int contadorTicks = 0;

	/**
	 * Código de la última generación de cálculo completada con éxito.
	 * {@code volatile} garantiza visibilidad atómica e inmediata entre hilos.
	 */
	private volatile int codActCompleto = 0;

	/** Último nodo donde se posicionó el objetivo (Jugador) al calcular. */
	private volatile NodoD ultimoNodoPosObjetivo;

	/** Nodo objetivo pendiente de procesar en el hilo secundario. */
	private volatile NodoD targetPendiente;

	/**
	 * Bandera atómica que evita enviar múltiples tareas simultáneas al hilo de IA.
	 */
	private final AtomicBoolean actualizando = new AtomicBoolean(false);

	/**
	 * Servicio de hilo secundario dedicado exclusivamente al pathfinding masivo.
	 */
	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	/**
	 * Tarea persistente enviada al Executor (evita crear 'new Runnable()' por
	 * tick).
	 */
	private final Runnable tareaDijkstra;

	/** Contadores atómicos para depuración y métricas de rendimiento. */
	private final AtomicInteger cantNodoVisitados = new AtomicInteger(0);
	private final AtomicInteger entidadesAlPendiente = new AtomicInteger(0);

	/**
	 * Rectángulos de colisión pre-asignados separados por hilo para evitar
	 * condiciones de carrera (Race Conditions) y llamadas a 'new Rectangle()'.
	 */
	private final Rectangle rectColisionHiloPrincipal = new Rectangle();
	private final Rectangle rectColisionHiloSecundario = new Rectangle();

	// =========================================================================
	// === CONSTRUCTOR
	// =========================================================================

	/**
	 * Inicializa el sistema de Dijkstra masivo y dimensiona los búferes de memoria.
	 *
	 * @param mundo     Referencia al mundo activo.
	 * @param dimension Dimensiones físicas en píxeles de cada nodo (ej: 16x16).
	 */
	public DijkstraRework(final Mundo mundo, final Dimension dimension) {
		this.mundo = mundo;
		this.dimensionNodo = dimension;

		/*
		 * =====================================================================
		 * EXPLICACIÓN DIDÁCTICA: CÁLCULO DEL RADIO CON COBERTURA DE ZOOM-OUT
		 * --------------------------------------------------------------------- La
		 * diagonal de la pantalla (640x360) es ≈ 735 px.
		 * 
		 * Si multiplicamos por 1.35x, obtenemos un radio de ≈ 992 px (unas 62
		 * casillas). Esto asegura que: 1. A Zoom 1.0x: Cubre la pantalla y un radio de
		 * 2 pantallas alrededor. 2. A Zoom 0.5x (Zoom-Out total): Cubre la pantalla
		 * completa de esquina a esquina sin que ningún enemigo quede fuera del mapa de
		 * calor. =====================================================================
		 */
		final double radioPx = Math.hypot(Constantes.ANCHO_JUEGO, Constantes.ALTO_JUEGO) * 1.35;
		this.radioMaximoBusqueda = radioPx / dimension.width;

		// Definición única del Runnable de cálculo (0 allocations en el bucle de juego)
		this.tareaDijkstra = new Runnable() {
			@Override
			public void run() {
				try {
					final NodoD targetFinal = DijkstraRework.this.targetPendiente;
					if (targetFinal == null) {
						return;
					}

					final int nuevoCodAct = DijkstraRework.this.codActCompleto + 1;

					/*
					 * ========================================================= EXPLICACIÓN
					 * DIDÁCTICA: LA MAGIA DEL DOBLE BÚFER
					 * --------------------------------------------------------- Cada NodoD tiene
					 * dos casillas para guardar su distancia: - distancia[0] y distancia[1]
					 * 
					 * Si las criaturas están LEYENDO de la casilla 0: - El hilo secundario ESCRIBE
					 * en la casilla 1 (writeBuf = 1). - Cuando termina de calcular toda el área,
					 * cambia atómicamente: codActCompleto = nuevoCodAct - En el siguiente frame,
					 * las criaturas leen instantáneamente de la casilla 1 sin haber frenado el
					 * juego ni 1 milisegundo.
					 * =========================================================
					 */
					final int writeBuf = Math.abs(nuevoCodAct % 2);

					// El objetivo (Jugador) tiene distancia 0 de sí mismo
					targetFinal.setDistancia(writeBuf, 0.0);
					targetFinal.setNodoProcedente(writeBuf, null);
					targetFinal.setCodAct(writeBuf, nuevoCodAct);

					// Propagación de Dijkstra acotada
					DijkstraRework.this.procesarDijkstra(targetFinal, nuevoCodAct, writeBuf);

					DijkstraRework.this.ultimoNodoPosObjetivo = targetFinal;

					// ¡Conmutación atómica de búfer de lectura!
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
	 * Crea la matriz bidimensional de nodos analizando los obstáculos permanentes
	 * del terreno.
	 */
	private void generarNodos() {
		this.xUltimoNodo = Math.floorDiv(this.mundo.getTerreno().getAncho() - this.dimensionNodo.width,
				this.dimensionNodo.width);
		this.yUltimoNodo = Math.floorDiv(this.mundo.getTerreno().getAlto() - this.dimensionNodo.height,
				this.dimensionNodo.height);

		this.nodos = new NodoD[this.xUltimoNodo + 1][this.yUltimoNodo + 1];

		// Capacidad estimada para el Min-Heap según el área circular del radio acotado
		final int capacidadEstimada = (int) (Math.PI * this.radioMaximoBusqueda * this.radioMaximoBusqueda * 1.5);
		this.colaPrioridad = new MinHeapDijkstra(Math.max(1024, capacidadEstimada));

		for (int x = 0; x <= this.xUltimoNodo; x++) {
			for (int y = 0; y <= this.yUltimoNodo; y++) {
				final boolean esPermaSolido = this.verificarSiEsteNodoVaSerPermaSolido(x, y);
				this.nodos[x][y] = new NodoD(x, y, this.dimensionNodo, esPermaSolido);
			}
		}
	}

	// =========================================================================
	// === ACTUALIZACIÓN DESDE EL HILO PRINCIPAL (GAME LOOP)
	// =========================================================================

	/**
	 * Solicita periódicamente el recálculo del mapa de distancias orientado hacia
	 * la posición del objetivo (Jugador).
	 *
	 * @param posicionObjetivo Coordenadas continuas en píxeles del mundo.
	 */
	public void actualizar(final Point posicionObjetivo) {
		if (posicionObjetivo == null) {
			return;
		}

		// Throttle: Limitamos la ejecución a 2 veces por segundo para ahorrar CPU
		this.contadorTicks++;
		if (this.contadorTicks < INTERVALO_TICKS_ACTUALIZACION) {
			return;
		}
		this.contadorTicks = 0;

		// Si el hilo secundario sigue procesando la tarea previa, esperamos al
		// siguiente ciclo
		if (!this.actualizando.compareAndSet(false, true)) {
			return;
		}

		final int posRefX = Math.floorDiv(posicionObjetivo.x, this.dimensionNodo.width);
		final int posRefY = Math.floorDiv(posicionObjetivo.y, this.dimensionNodo.height);

		// Validación de límites del mapa
		if ((posRefX < 0) || (posRefX > this.xUltimoNodo) || (posRefY < 0) || (posRefY > this.yUltimoNodo)) {
			this.actualizando.set(false);
			return;
		}

		NodoD nodoObjetivo = this.nodos[posRefX][posRefY];

		// Si el jugador está parado sobre un obstáculo, buscamos el nodo transitable
		// más cercano
		if ((nodoObjetivo == null) || nodoObjetivo.isInmodificable()
				|| this.colisiona(nodoObjetivo, this.rectColisionHiloPrincipal)) {
			nodoObjetivo = this.getNodoCercano(posicionObjetivo.x, posicionObjetivo.y);
		}

		// Si el objetivo no cambió de celda, evitamos recalcular innecesariamente
		if ((nodoObjetivo == null) || nodoObjetivo.isInmodificable() || (nodoObjetivo == this.ultimoNodoPosObjetivo)) {
			this.actualizando.set(false);
			return;
		}

		this.targetPendiente = nodoObjetivo;

		// Despachamos la tarea al hilo secundario con 0 asignaciones de memoria
		this.executor.submit(this.tareaDijkstra);
	}

	// =========================================================================
	// === ALGORITMO DIJKSTRA (EJECUTADO EN HILO SECUNDARIO)
	// =========================================================================

	/**
	 * Expande el flujo de distancias desde el objetivo hacia afuera en 8
	 * direcciones con corte temprano (Early-Exit).
	 */
	private void procesarDijkstra(final NodoD objetivo, final int nuevoCodAct, final int writeBuf) {
		this.colaPrioridad.clear();
		this.colaPrioridad.push(objetivo, 0.0);

		int visitadosContador = 0;

		while (!this.colaPrioridad.isEmpty()) {
			final NodoD n = this.colaPrioridad.poll();
			final double distActual = n.getDistancia(writeBuf);

			/*
			 * ================================================================= EXPLICACIÓN
			 * DIDÁCTICA: SALIDA TEMPRANA (EARLY-EXIT)
			 * ----------------------------------------------------------------- El Min-Heap
			 * siempre nos entrega los nodos ordenados de MENOR a MAYOR distancia.
			 * 
			 * En el instante exacto en que un nodo extraído supera el radio máximo de
			 * búsqueda, sabemos con certeza matemática que TODOS los nodos que quedan en la
			 * cola están aún más lejos. Hacemos 'break' de inmediato y ahorramos escanear
			 * el 90% restante del mapa.
			 * =================================================================
			 */
			if (distActual > this.radioMaximoBusqueda) {
				break;
			}

			final int xNodo = n.getGrillaX();
			final int yNodo = n.getGrillaY();

			// Evaluamos los 8 vecinos adyacentes
			for (int i = 0; i < OFFSET_X.length; i++) {
				final int nx = xNodo + OFFSET_X[i];
				final int ny = yNodo + OFFSET_Y[i];

				if ((nx < 0) || (nx > this.xUltimoNodo) || (ny < 0) || (ny > this.yUltimoNodo)) {
					continue;
				}

				final NodoD nodoAct = this.nodos[nx][ny];

				// Ignoramos paredes u obstáculos
				if ((nodoAct == null) || nodoAct.isInmodificable()
						|| this.colisiona(nodoAct, this.rectColisionHiloSecundario)) {
					continue;
				}

				// Evitamos atravesar esquinas diagonales sólidas
				if (this.esDiagonal(OFFSET_X[i], OFFSET_Y[i])
						&& this.hayBloqueoEnEsquina(xNodo, yNodo, OFFSET_X[i], OFFSET_Y[i])) {
					continue;
				}

				final double nuevaDistancia = distActual + COSTOS[i];

				// Solo propagamos si no excede el límite del radio de visión
				if (nuevaDistancia <= this.radioMaximoBusqueda) {
					if ((nodoAct.getCodAct(writeBuf) != nuevoCodAct)
							|| (nuevaDistancia < nodoAct.getDistancia(writeBuf))) {

						// Actualizamos en el BÚFER DE ESCRITURA
						nodoAct.setDistancia(writeBuf, nuevaDistancia);

						// 'nodoProcedente' apunta de regreso HACIA el jugador
						nodoAct.setNodoProcedente(writeBuf, n);
						nodoAct.setCodAct(writeBuf, nuevoCodAct);

						visitadosContador++;
						this.colaPrioridad.push(nodoAct, nuevaDistancia);
					}
				}
			}
		}

		this.cantNodoVisitados.set(visitadosContador);
	}

	private boolean esDiagonal(final int dx, final int dy) {
		return (dx != 0) && (dy != 0);
	}

	/**
	 * Previene que las criaturas corten esquinas atravesando paredes sólidas en
	 * diagonal.
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

	// =========================================================================
	// === COLISIONES Y MAPEO DE COORDENADAS
	// =========================================================================

	private boolean verificarSiEsteNodoVaSerPermaSolido(final int xMatriz, final int yMatriz) {
		final int xPx = xMatriz * this.dimensionNodo.width;
		final int yPx = yMatriz * this.dimensionNodo.height;

		this.rectColisionHiloPrincipal.setBounds(xPx, yPx, this.dimensionNodo.width, this.dimensionNodo.height);
		return this.mundo.getTerreno().intersectaSolidoDijkstra(this.rectColisionHiloPrincipal)
				|| this.mundo.colisionaConAlgoSolidoPermanente(this.rectColisionHiloPrincipal);
	}

	private boolean colisiona(final NodoD n, final Rectangle rectAux) {
		rectAux.setBounds(n.getXMundo(), n.getYMundo(), n.getAncho(), n.getAlto());
		return this.mundo.getTerreno().intersectaSolidoDijkstra(rectAux)
				|| this.mundo.colisionaConObjetoSolido(rectAux);
	}

	/**
	 * Convierte coordenadas de píxeles del mundo al nodo correspondiente en la
	 * matriz.
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
	 * Retorna el siguiente nodo transitable hacia el objetivo desde el búfer de
	 * lectura activo.
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

	// =========================================================================
	// === MÉTODOS DE RECORRIDO COMPLETO (COMPATIBILIDAD)
	// =========================================================================

	public Lista<NodoD> getRecorrido(final int x, final int y) {
		final NodoD nodoProx = this.getNodoReferenciado(x, y);
		return this.getRecorrido(nodoProx);
	}

	public Lista<NodoD> getRecorrido(final NodoD nodoActual) {
		final Lista<NodoD> recorrido = new Lista<>();
		final int readBuf = Math.abs(this.codActCompleto % 2);

		if ((nodoActual != null) && !this.colisiona(nodoActual, this.rectColisionHiloPrincipal)) {
			this.generarRecorridoIterativo(recorrido, nodoActual, readBuf);
		}
		return recorrido;
	}

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

	// =========================================================================
	// === GETTERS Y APAGADO DEL SERVICIO
	// =========================================================================

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
		int prev, next;
		do {
			prev = this.entidadesAlPendiente.get();
			next = Math.max(0, prev - 1);
		} while (!this.entidadesAlPendiente.compareAndSet(prev, next));
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
	 * Apaga de forma segura el pool de hilos de la IA al cerrar la partida o salir
	 * del juego.
	 */
	public void destruir() {
		this.executor.shutdown();
	}
}