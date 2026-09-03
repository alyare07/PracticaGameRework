package principal.ia.aEstrella;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayDeque;

import principal.mapa.Mundo;

/**
 * Sistema de búsqueda de caminos óptimos punto a punto mediante A* (A-Star).
 * 
 * Optimizaciones Aplicadas: 1. Zero-Allocation (0 bytes GC): Montículo binario
 * plano (`MinHeapAEstrella`) reutilizable sin instanciar objetos intermedios.
 * 2. Reconstrucción de camino en 1 pasada O(N) con `ArrayDeque.addFirst()`,
 * eliminando listas temporales intermedias. 3. Filtrado instantáneo de
 * obstáculos fijos (`inmodificable`) y distancias al cuadrado sin cálculo de
 * raíces cuadradas (`Math.hypot`). 4. Precisión espacial en coordenadas
 * negativas con `Math.floorDiv`.
 */
public class AEstrella {

	private static final float COSTO_DIAGONAL = 1.41421356f;
	private static final float COSTO_ORTOGONAL = 1.0f;

	// Desplazamientos de 8 direcciones adyacentes
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
		// Capacidad estimada inicial para cubrir búsquedas de tamaño medio/grande
		this.listaAbierta = new MinHeapAEstrella(
				Math.min(4096, Math.max(512, (this.anchoMatriz * this.altoMatriz) / 4)));
	}

	/**
	 * Calcula el camino óptimo desde una posición inicial en píxeles hasta el
	 * destino. Escribe la ruta en el contenedor `recorrido` pasado por referencia
	 * (0 asignaciones).
	 *
	 * @param xInicial  Posición X inicial en píxeles del mundo.
	 * @param yInicial  Posición Y inicial en píxeles del mundo.
	 * @param xObjetivo Posición X objetivo en píxeles del mundo.
	 * @param yObjetivo Posición Y objetivo en píxeles del mundo.
	 * @param recorrido Contenedor de salida donde se insertará la secuencia de
	 *                  nodos.
	 */
	public void getRecorrido(final int xInicial, final int yInicial, final int xObjetivo, final int yObjetivo,
			final ArrayDeque<NodoA> recorrido) {

		recorrido.clear();

		final NodoA nodoInicial = this.getNodoRef(xInicial, yInicial);
		NodoA nodoObjetivo = this.getNodoRef(xObjetivo, yObjetivo);

		if ((nodoInicial == null) || (nodoObjetivo == null) || (nodoInicial == nodoObjetivo)) {
			return;
		}

		// Si el destino exacto es sólido, buscamos la casilla libre contigua más
		// cercana
		if (this.colisiona(nodoObjetivo)) {
			nodoObjetivo = this.obtenerVecinoTransitableMasCercano(nodoObjetivo, nodoInicial);
			if ((nodoObjetivo == null) || (nodoObjetivo == nodoInicial)) {
				return;
			}
		}

		this.actualizarGeneracionBusqueda();
		this.listaAbierta.clear();

		nodoInicial.reiniciar(this.generacionBusqueda);
		nodoInicial.evaluar(null, nodoObjetivo, 0f);
		nodoInicial.setEstado(NodoA.ESTADO_ABIERTA);
		this.listaAbierta.push(nodoInicial, nodoInicial.getCostoF(), nodoInicial.getCostoH());

		while (!this.listaAbierta.isEmpty()) {
			final NodoA nodoAct = this.listaAbierta.poll();

			// Lazy Deletion: Ignoramos entradas obsoletas si el nodo ya fue cerrado
			if (nodoAct.getEstado() == NodoA.ESTADO_CERRADA) {
				continue;
			}

			if (nodoAct == nodoObjetivo) {
				this.reconstruirCamino(recorrido, nodoObjetivo);
				return;
			}

			nodoAct.setEstado(NodoA.ESTADO_CERRADA);

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
						|| this.colisiona(vecino)) {
					continue;
				}

				final boolean esDiagonal = (OFFSET_X[i] != 0) && (OFFSET_Y[i] != 0);

				if (esDiagonal && this.cortaEsquina(nodoAct, nx, ny)) {
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
	}

	/**
	 * Si la casilla destino está bloqueada, busca el vecino transitable más próximo
	 * al origen. Utiliza distancias al cuadrado para evitar el cálculo de raíces
	 * cuadradas (Math.hypot).
	 */
	private NodoA obtenerVecinoTransitableMasCercano(final NodoA objetivoSolido, final NodoA origen) {
		NodoA mejorVecino = null;
		int menorDistSq = Integer.MAX_VALUE;

		final int origenX = origen.getXNodo();
		final int origenY = origen.getYNodo();

		for (int i = 0; i < 8; i++) {
			final int nx = objetivoSolido.getXNodo() + OFFSET_X[i];
			final int ny = objetivoSolido.getYNodo() + OFFSET_Y[i];
			final NodoA v = this.getNodo(nx, ny);

			if ((v != null) && !v.isInmodificable() && !this.colisiona(v)) {
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

	private boolean cortaEsquina(final NodoA origen, final int vecinoX, final int vecinoY) {
		final NodoA ortogonal1 = this.getNodo(vecinoX, origen.getYNodo());
		final NodoA ortogonal2 = this.getNodo(origen.getXNodo(), vecinoY);

		return (ortogonal1 == null) || ortogonal1.isInmodificable() || this.colisiona(ortogonal1)
				|| (ortogonal2 == null) || ortogonal2.isInmodificable() || this.colisiona(ortogonal2);
	}

	/**
	 * Reconstruye el camino excluyendo el nodo de partida (nodoInicial) para que el
	 * primer destino sea directamente el primer paso hacia adelante.
	 */
	private void reconstruirCamino(final ArrayDeque<NodoA> destino, final NodoA nodoObjetivo) {
		NodoA actual = nodoObjetivo;
		// El nodo inicial tiene nodoProcedente == null, por lo que este bucle lo
		// excluye automáticamente
		while ((actual != null) && (actual.getNodoProcedente() != null)) {
			destino.addFirst(actual);
			actual = actual.getNodoProcedente();
		}
	}

	private boolean colisiona(final NodoA n) {
		if (n == null) {
			return true;
		}
		if (n.isInmodificable()) {
			return true;
		}

		this.cajaColisionAux.setBounds(n.getXMundo(), n.getYMundo(), n.getAncho(), n.getAlto());

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

	/**
	 * Recalcula en O(W x H) la holgura espacial (Clearance) de cada celda ante
	 * cambios dinámicos en el mapa (muros construidos o destruidos).
	 */
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

		this.cajaColisionAux.setBounds(xPx, yPx, this.dimensionNodo.width, this.dimensionNodo.height);
		return this.mundo.getTerreno().intersectaSolidoDijkstra(this.cajaColisionAux)
				|| this.mundo.colisionaConAlgoSolidoPermanente(this.cajaColisionAux);
	}

	public Dimension getDimensionNodoA() {
		return this.dimensionNodo;
	}
}