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
 * Sistema de Flowfield Dijkstra Lock-Free y con Clearance. Elimina la
 * contención con ZoneBox en el hilo secundario.
 */
public class DijkstraRework {

	private static final double COSTO_ORTOGONAL = 1.0;
	private static final double COSTO_DIAGONAL = 1.4142135623730951;

	private static final int[] OFFSET_X = { -1, 0, 1, -1, 1, -1, 0, 1 };
	private static final int[] OFFSET_Y = { -1, -1, -1, 0, 0, 1, 1, 1 };

	private static final double[] COSTOS = { COSTO_DIAGONAL, COSTO_ORTOGONAL, COSTO_DIAGONAL, COSTO_ORTOGONAL,
			COSTO_ORTOGONAL, COSTO_DIAGONAL, COSTO_ORTOGONAL, COSTO_DIAGONAL };

	private final Mundo mundo;
	private final Dimension dimensionNodo;
	private final double radioMaximoBusqueda;

	private int xUltimoNodo;
	private int yUltimoNodo;
	private NodoD[][] nodos;
	private MinHeapDijkstra colaPrioridad;

	private static final int INTERVALO_TICKS_ACTUALIZACION = 30;
	private int contadorTicks = 0;

	private volatile int codActCompleto = 0;
	private volatile NodoD ultimoNodoPosObjetivo;
	private volatile NodoD targetPendiente;

	private final AtomicBoolean actualizando = new AtomicBoolean(false);
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private final Runnable tareaDijkstra;

	private final AtomicInteger cantNodoVisitados = new AtomicInteger(0);
	private final AtomicInteger entidadesAlPendiente = new AtomicInteger(0);

	private final Rectangle rectColisionHiloPrincipal = new Rectangle();

	public DijkstraRework(final Mundo mundo, final Dimension dimension) {
		this.mundo = mundo;
		// Alineacion estricta con el terreno
		this.dimensionNodo = new Dimension(Constantes.LADO_TILE, Constantes.LADO_TILE);

		final double radioPx = Math.hypot(Constantes.ANCHO_JUEGO, Constantes.ALTO_JUEGO) * 1.35;
		this.radioMaximoBusqueda = radioPx / this.dimensionNodo.width;

		this.tareaDijkstra = new Runnable() {
			@Override
			public void run() {
				try {
					final NodoD targetFinal = DijkstraRework.this.targetPendiente;
					if (targetFinal == null) {
						return;
					}

					final int nuevoCodAct = DijkstraRework.this.codActCompleto + 1;
					final int writeBuf = Math.abs(nuevoCodAct % 2);

					targetFinal.setDistancia(writeBuf, 0.0);
					targetFinal.setNodoProcedente(writeBuf, null);
					targetFinal.setCodAct(writeBuf, nuevoCodAct);

					DijkstraRework.this.procesarDijkstra(targetFinal, nuevoCodAct, writeBuf);
					DijkstraRework.this.ultimoNodoPosObjetivo = targetFinal;

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

	private void generarNodos() {
		this.xUltimoNodo = Math.floorDiv(this.mundo.getTerreno().getAncho() - this.dimensionNodo.width,
				this.dimensionNodo.width);
		this.yUltimoNodo = Math.floorDiv(this.mundo.getTerreno().getAlto() - this.dimensionNodo.height,
				this.dimensionNodo.height);

		this.nodos = new NodoD[this.xUltimoNodo + 1][this.yUltimoNodo + 1];

		final int capacidadEstimada = (int) (Math.PI * this.radioMaximoBusqueda * this.radioMaximoBusqueda * 1.5);
		this.colaPrioridad = new MinHeapDijkstra(Math.max(1024, capacidadEstimada));

		for (int x = 0; x <= this.xUltimoNodo; x++) {
			for (int y = 0; y <= this.yUltimoNodo; y++) {
				final boolean esPermaSolido = this.verificarSiEsteNodoVaSerPermaSolido(x, y);
				this.nodos[x][y] = new NodoD(x, y, this.dimensionNodo, esPermaSolido);
			}
		}

		this.calcularMatrizClearance();
	}

	public void calcularMatrizClearance() {
		for (int y = this.yUltimoNodo; y >= 0; y--) {
			for (int x = this.xUltimoNodo; x >= 0; x--) {
				final NodoD n = this.nodos[x][y];

				if (n.isInmodificable() || this.colisiona(n, this.rectColisionHiloPrincipal)) {
					n.setClearance((byte) 0);
				} else if ((x == this.xUltimoNodo) || (y == this.yUltimoNodo)) {
					n.setClearance((byte) 1);
				} else {
					final int der = this.nodos[x + 1][y].getClearance();
					final int aba = this.nodos[x][y + 1].getClearance();
					final int diag = this.nodos[x + 1][y + 1].getClearance();

					final int minVecinos = Math.min(der, Math.min(aba, diag));
					n.setClearance((byte) Math.min(15, minVecinos + 1));
				}
			}
		}
	}

	public void actualizar(final Point posicionObjetivo) {
		if (posicionObjetivo == null) {
			return;
		}

		this.contadorTicks++;
		if (this.contadorTicks < INTERVALO_TICKS_ACTUALIZACION) {
			return;
		}
		this.contadorTicks = 0;

		if (!this.actualizando.compareAndSet(false, true)) {
			return;
		}

		final int posRefX = Math.floorDiv(posicionObjetivo.x, this.dimensionNodo.width);
		final int posRefY = Math.floorDiv(posicionObjetivo.y, this.dimensionNodo.height);

		if ((posRefX < 0) || (posRefX > this.xUltimoNodo) || (posRefY < 0) || (posRefY > this.yUltimoNodo)) {
			this.actualizando.set(false);
			return;
		}

		NodoD nodoObjetivo = this.nodos[posRefX][posRefY];

		if ((nodoObjetivo == null) || (nodoObjetivo.getClearance() < 1)) {
			nodoObjetivo = this.getNodoCercano(posicionObjetivo.x, posicionObjetivo.y);
		}

		if ((nodoObjetivo == null) || (nodoObjetivo.getClearance() < 1)
				|| (nodoObjetivo == this.ultimoNodoPosObjetivo)) {
			this.actualizando.set(false);
			return;
		}

		this.targetPendiente = nodoObjetivo;
		this.executor.submit(this.tareaDijkstra);
	}

	private void procesarDijkstra(final NodoD objetivo, final int nuevoCodAct, final int writeBuf) {
		this.colaPrioridad.clear();
		this.colaPrioridad.push(objetivo, 0.0);

		int visitadosContador = 0;

		while (!this.colaPrioridad.isEmpty()) {
			final NodoD n = this.colaPrioridad.poll();
			final double distActual = n.getDistancia(writeBuf);

			if (distActual > this.radioMaximoBusqueda) {
				break;
			}

			final int xNodo = n.getGrillaX();
			final int yNodo = n.getGrillaY();

			for (int i = 0; i < OFFSET_X.length; i++) {
				final int nx = xNodo + OFFSET_X[i];
				final int ny = yNodo + OFFSET_Y[i];

				if ((nx < 0) || (nx > this.xUltimoNodo) || (ny < 0) || (ny > this.yUltimoNodo)) {
					continue;
				}

				final NodoD nodoAct = this.nodos[nx][ny];

				// LECTURA LOCK-FREE: No accede a ZoneBox ni a colecciones dinámicas
				if ((nodoAct == null) || (nodoAct.getClearance() < 1)) {
					continue;
				}

				if (this.esDiagonal(OFFSET_X[i], OFFSET_Y[i])
						&& this.hayBloqueoEnEsquina(xNodo, yNodo, OFFSET_X[i], OFFSET_Y[i])) {
					continue;
				}

				final double nuevaDistancia = distActual + COSTOS[i];

				if (nuevaDistancia <= this.radioMaximoBusqueda) {
					if ((nodoAct.getCodAct(writeBuf) != nuevoCodAct)
							|| (nuevaDistancia < nodoAct.getDistancia(writeBuf))) {

						nodoAct.setDistancia(writeBuf, nuevaDistancia);
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

	private boolean hayBloqueoEnEsquina(final int x, final int y, final int dx, final int dy) {
		final int xLat = x + dx;
		final int yLat = y + dy;

		boolean solidoX = false;
		if ((xLat >= 0) && (xLat <= this.xUltimoNodo)) {
			final NodoD nX = this.nodos[xLat][y];
			solidoX = (nX != null) && (nX.getClearance() < 1);
		}

		boolean solidoY = false;
		if ((yLat >= 0) && (yLat <= this.yUltimoNodo)) {
			final NodoD nY = this.nodos[x][yLat];
			solidoY = (nY != null) && (nY.getClearance() < 1);
		}

		return solidoX || solidoY;
	}

	private boolean verificarSiEsteNodoVaSerPermaSolido(final int xMatriz, final int yMatriz) {
		final int xPx = xMatriz * this.dimensionNodo.width;
		final int yPx = yMatriz * this.dimensionNodo.height;

		this.rectColisionHiloPrincipal.setBounds(xPx, yPx, this.dimensionNodo.width, this.dimensionNodo.height);
		return this.mundo.getTerreno().intersectaTileSolido(this.rectColisionHiloPrincipal)
				|| this.mundo.colisionaConAlgoSolidoPermanente(this.rectColisionHiloPrincipal);
	}

	private boolean colisiona(final NodoD n, final Rectangle rectAux) {
		rectAux.setBounds(n.getXMundo(), n.getYMundo(), n.getAncho(), n.getAlto());
		return this.mundo.getTerreno().intersectaTileSolido(rectAux) || this.mundo.colisionaConObjetoSolido(rectAux);
	}

	public NodoD getNodoReferenciado(final int x, final int y) {
		final int nx = Math.floorDiv(x, this.dimensionNodo.width);
		final int ny = Math.floorDiv(y, this.dimensionNodo.height);

		if ((nx < 0) || (nx > this.xUltimoNodo) || (ny < 0) || (ny > this.yUltimoNodo)) {
			return null;
		}
		return this.nodos[nx][ny];
	}

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

	public Lista<NodoD> getRecorrido(final int x, final int y) {
		final NodoD nodoProx = this.getNodoReferenciado(x, y);
		return this.getRecorrido(nodoProx);
	}

	public Lista<NodoD> getRecorrido(final NodoD nodoActual) {
		final Lista<NodoD> recorrido = new Lista<>();
		final int readBuf = Math.abs(this.codActCompleto % 2);

		if ((nodoActual != null) && (nodoActual.getClearance() >= 1)) {
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

	public void destruir() {
		this.executor.shutdown();
	}
}