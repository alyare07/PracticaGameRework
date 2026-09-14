package principal.ia.aEstrella;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayDeque;

import principal.mapa.Mundo;

/**
 * Motor A* con tolerancia geométrica de paso en pasillos estrechos y
 * autorrecuperación de origen para agentes de hasta 64x64 (Zero-GC).
 * 
 * @version 3.2 (Vanilla Java 8 - Bi-Directional Clearance Snapping)
 */
public class AEstrella {

	private static final float COSTO_DIAGONAL = 1.41421356f;
	private static final float COSTO_ORTOGONAL = 1.0f;
	private static final int MAX_NODOS_EXPANDIDOS = 750;

	private static final int[] OFFSET_X = { -1, 0, 1, -1, 1, -1, 0, 1 };
	private static final int[] OFFSET_Y = { -1, -1, -1, 0, 0, 1, 1, 1 };

	private final Mundo mundo;
	private final Dimension dimensionNodo;
	private final Rectangle cajaColisionAux;
	private final MinHeapAEstrella listaAbierta;

	private NodoA[][] nodos;
	private int anchoMatriz;
	private int altoMatriz;
	private int generacionBusqueda = 1;

	public AEstrella(final Mundo mundo, final Dimension dimensionNodo) {
		this.mundo = mundo;
		this.dimensionNodo = dimensionNodo;
		this.cajaColisionAux = new Rectangle();

		this.generarNodos();
		this.calcularMatrizClearance();

		this.listaAbierta = new MinHeapAEstrella(
				Math.min(4096, Math.max(512, (this.anchoMatriz * this.altoMatriz) / 4)));
	}

	public void getRecorrido(final int xInicial, final int yInicial, final int xObjetivo, final int yObjetivo,
			final ArrayDeque<NodoA> recorrido) {
		this.getRecorrido(xInicial, yInicial, xObjetivo, yObjetivo, (byte) 1, recorrido);
	}

	public void getRecorrido(final int xInicial, final int yInicial, final int xObjetivo, final int yObjetivo,
			final int clearanceRequerido, final ArrayDeque<NodoA> recorrido) {

		recorrido.clear();

		final int clearanceEfectivo = Math.max(1, clearanceRequerido);
		NodoA nodoInicial = this.getNodoRef(xInicial, yInicial);
		NodoA nodoObjetivo = this.getNodoRef(xObjetivo, yObjetivo);

		if ((nodoInicial == null) || (nodoObjetivo == null) || (nodoInicial == nodoObjetivo)) {
			return;
		}

		// 1. Autorrecuperación del Nodo Objetivo si este cae en un obstáculo o carece
		// de clearance
		if (!nodoObjetivo.admiteClearance(clearanceEfectivo)) {
			nodoObjetivo = this.obtenerVecinoTransitableConClearance(nodoObjetivo, nodoInicial, clearanceEfectivo);
			if ((nodoObjetivo == null) || (nodoObjetivo == nodoInicial)) {
				return;
			}
		}

		// 2. Autorrecuperación del Nodo Inicial si la criatura roza una pared o árbol
		if (!nodoInicial.admiteClearance(clearanceEfectivo)) {
			nodoInicial = this.obtenerVecinoTransitableConClearance(nodoInicial, nodoObjetivo, clearanceEfectivo);
			if ((nodoInicial == null) || (nodoInicial == nodoObjetivo)) {
				return;
			}
		}

		this.actualizarGeneracionBusqueda();
		this.listaAbierta.clear();

		nodoInicial.reiniciar(this.generacionBusqueda);
		nodoInicial.evaluar(null, nodoObjetivo, 0f);
		nodoInicial.setEstado(NodoA.ESTADO_ABIERTA);
		this.listaAbierta.push(nodoInicial, nodoInicial.getCostoF(), nodoInicial.getCostoH());

		int nodosProcesados = 0;
		NodoA nodoMasCercanoAlcanzado = nodoInicial;
		float menorCostoH = nodoInicial.getCostoH();

		while (!this.listaAbierta.isEmpty()) {
			final NodoA nodoAct = this.listaAbierta.poll();

			if (nodoAct.getEstado() == NodoA.ESTADO_CERRADA) {
				continue;
			}

			if (nodoAct == nodoObjetivo) {
				this.reconstruirCamino(recorrido, nodoObjetivo);
				return;
			}

			nodosProcesados++;
			if (nodosProcesados >= MAX_NODOS_EXPANDIDOS) {
				if ((nodoMasCercanoAlcanzado != null) && (nodoMasCercanoAlcanzado != nodoInicial)) {
					this.reconstruirCamino(recorrido, nodoMasCercanoAlcanzado);
				}
				return;
			}

			nodoAct.setEstado(NodoA.ESTADO_CERRADA);

			if (nodoAct.getCostoH() < menorCostoH) {
				menorCostoH = nodoAct.getCostoH();
				nodoMasCercanoAlcanzado = nodoAct;
			}

			final int xAct = nodoAct.getXNodo();
			final int yAct = nodoAct.getYNodo();

			for (int i = 0; i < 8; i++) {
				final int nx = xAct + OFFSET_X[i];
				final int ny = yAct + OFFSET_Y[i];

				final NodoA vecino = this.getNodo(nx, ny);

				if (vecino == null) {
					continue;
				}

				if (!vecino.visitado(this.generacionBusqueda)) {
					vecino.reiniciar(this.generacionBusqueda);
				}

				if ((vecino.getEstado() == NodoA.ESTADO_CERRADA) || vecino.isInmodificable()
						|| !vecino.admiteClearance(clearanceEfectivo)) {
					continue;
				}

				final boolean esDiagonal = (OFFSET_X[i] != 0) && (OFFSET_Y[i] != 0);

				if (esDiagonal && this.cortaEsquina(nodoAct, nx, ny, clearanceEfectivo)) {
					continue;
				}

				final float costoPaso = esDiagonal ? COSTO_DIAGONAL : COSTO_ORTOGONAL;
				final float nuevoCostoG = nodoAct.getCostoG() + costoPaso;

				if ((vecino.getEstado() == NodoA.ESTADO_NINGUNO) || (nuevoCostoG < vecino.getCostoG())) {
					vecino.evaluar(nodoAct, nodoObjetivo, costoPaso);
					vecino.setEstado(NodoA.ESTADO_ABIERTA);
					this.listaAbierta.push(vecino, vecino.getCostoF(), vecino.getCostoH());
				}
			}
		}

		if ((nodoMasCercanoAlcanzado != null) && (nodoMasCercanoAlcanzado != nodoInicial)) {
			this.reconstruirCamino(recorrido, nodoMasCercanoAlcanzado);
		}
	}

	private NodoA obtenerVecinoTransitableConClearance(final NodoA objetivoSolido, final NodoA origen,
			final int clearanceRequerido) {
		NodoA mejorVecino = null;
		int menorDistSq = Integer.MAX_VALUE;

		final int origenX = origen.getXNodo();
		final int origenY = origen.getYNodo();

		for (int i = 0; i < 8; i++) {
			final int nx = objetivoSolido.getXNodo() + OFFSET_X[i];
			final int ny = objetivoSolido.getYNodo() + OFFSET_Y[i];
			final NodoA v = this.getNodo(nx, ny);

			if ((v != null) && !v.isInmodificable() && v.admiteClearance(clearanceRequerido)) {
				final int dx = v.getXNodo() - origenX;
				final int dy = v.getYNodo() - origenY;
				final int distSq = (dx * dx) + (dy * dy);

				if (distSq < menorDistSq) {
					menorDistSq = distSq;
					mejorVecino = v;
				}
			}
		}
		return mejorVecino;
	}

	private boolean cortaEsquina(final NodoA origen, final int vecinoX, final int vecinoY,
			final int clearanceRequerido) {
		final NodoA ortogonal1 = this.getNodo(vecinoX, origen.getYNodo());
		final NodoA ortogonal2 = this.getNodo(origen.getXNodo(), vecinoY);

		final boolean lado1Bloqueado = (ortogonal1 == null) || ortogonal1.isInmodificable()
				|| !ortogonal1.admiteClearance(clearanceRequerido);
		final boolean lado2Bloqueado = (ortogonal2 == null) || ortogonal2.isInmodificable()
				|| !ortogonal2.admiteClearance(clearanceRequerido);

		return lado1Bloqueado && lado2Bloqueado;
	}

	private void reconstruirCamino(final ArrayDeque<NodoA> destino, final NodoA nodoObjetivo) {
		NodoA actual = nodoObjetivo;
		while ((actual != null) && (actual.getNodoProcedente() != null)) {
			destino.addFirst(actual);
			actual = actual.getNodoProcedente();
		}
	}

	public boolean colisiona(final NodoA n) {
		if (n == null) {
			return true;
		}
		if (n.isInmodificable()) {
			return true;
		}

		final int margen = 2;
		this.cajaColisionAux.setBounds(n.getXMundo() + margen, n.getYMundo() + margen, n.getAncho() - (margen * 2),
				n.getAlto() - (margen * 2));

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
		this.calcularMatrizClearance();
	}

	public NodoA getNodoRef(final int xRef, final int yRef) {
		final int x = Math.floorDiv(xRef, this.dimensionNodo.width);
		final int y = Math.floorDiv(yRef, this.dimensionNodo.height);
		return this.getNodo(x, y);
	}

	public NodoA getNodo(final int x, final int y) {
		if ((x < 0) || (x >= this.anchoMatriz) || (y < 0) || (y >= this.altoMatriz)) {
			return null;
		}
		return this.nodos[x][y];
	}

	private void generarNodos() {
		this.anchoMatriz = Math.floorDiv(this.mundo.getTerreno().getAncho(), this.dimensionNodo.width);
		this.altoMatriz = Math.floorDiv(this.mundo.getTerreno().getAlto(), this.dimensionNodo.height);

		this.nodos = new NodoA[this.anchoMatriz][this.altoMatriz];

		for (int x = 0; x < this.anchoMatriz; x++) {
			for (int y = 0; y < this.altoMatriz; y++) {
				final boolean esPermaSolido = this.verificarSiEsPermaSolido(x, y);
				this.nodos[x][y] = new NodoA(x, y, this.dimensionNodo, esPermaSolido);
			}
		}
	}

	public void calcularMatrizClearance() {
		for (int y = this.altoMatriz - 1; y >= 0; y--) {
			for (int x = this.anchoMatriz - 1; x >= 0; x--) {
				final NodoA n = this.nodos[x][y];

				if (n.isInmodificable() || this.colisiona(n)) {
					n.setClearance((byte) 0);
				} else if ((x == (this.anchoMatriz - 1)) || (y == (this.altoMatriz - 1))) {
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

	private boolean verificarSiEsPermaSolido(final int xMatriz, final int yMatriz) {
		final int xPx = xMatriz * this.dimensionNodo.width;
		final int yPx = yMatriz * this.dimensionNodo.height;

		this.cajaColisionAux.setBounds(xPx + 2, yPx + 2, this.dimensionNodo.width - 4, this.dimensionNodo.height - 4);
		return this.mundo.getTerreno().intersectaSolidoDijkstra(this.cajaColisionAux)
				|| this.mundo.colisionaConAlgoSolidoPermanente(this.cajaColisionAux);
	}

	public Dimension getDimensionNodoA() {
		return this.dimensionNodo;
	}
}