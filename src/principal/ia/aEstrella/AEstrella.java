package principal.ia.aEstrella;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

import principal.ia.Lista;
import principal.mapa.Mundo;

public class AEstrella {

	private final PriorityQueue<NodoA> listaAbierta;
	private final Set<NodoA> listaCerrada;
	private final Mundo mundo;
	private final Dimension dimensionNodo;

	private NodoA[][] nodos;
	private int anchoMatriz;
	private int altoMatriz;
	private int codAct = Integer.MIN_VALUE;

	public AEstrella(final Mundo mundo, final Dimension dimensionNodo) {
		this.mundo = mundo;
		this.dimensionNodo = dimensionNodo;
		this.listaAbierta = new PriorityQueue<>();
		this.listaCerrada = new HashSet<>();
		this.generarNodos();
	}

	public Lista<NodoA> getRecorrido(final int xInicial, final int yInicial, final int xObjetivo, final int yObjetivo) {
		final Lista<NodoA> recorrido = new Lista<NodoA>();

		final NodoA nodoInicial = this.getNodoRef(xInicial, yInicial);
		final NodoA nodoObjetivo = this.getNodoRef(xObjetivo, yObjetivo);

		if ((nodoInicial == null) || (nodoObjetivo == null) || (nodoInicial == nodoObjetivo)
				|| this.colisiona(nodoObjetivo)) {
			return recorrido;
		}

		this.actualizarCodAct();
		this.listaAbierta.clear();
		this.listaCerrada.clear();

		// Configurar nodo inicial
		nodoInicial.reiniciar(this.codAct);
		nodoInicial.evaluar(null, nodoObjetivo, 0);
		this.listaAbierta.add(nodoInicial);

		while (!this.listaAbierta.isEmpty()) {
			final NodoA nodoAct = this.listaAbierta.poll();

			// Si llegamos al destino, armamos el camino
			if (nodoAct == nodoObjetivo) {
				this.reconstruirCamino(recorrido, nodoObjetivo);
				return recorrido;
			}

			this.listaCerrada.add(nodoAct);

			for (final NodoA vecino : this.getNodosVecinos(nodoAct)) {
				if (this.colisiona(vecino) || this.listaCerrada.contains(vecino)) {
					continue;
				}

				// Si no se ha visitado en este ciclo de búsqueda, lo preparamos
				if (!vecino.visitado(this.codAct)) {
					vecino.reiniciar(this.codAct);
				}

				final double costoPaso = ((vecino.getXNodo() != nodoAct.getXNodo())
						&& (vecino.getYNodo() != nodoAct.getYNodo())) ? 1.414 : 1.0;
				final double nuevoCostoG = nodoAct.getCostoG() + costoPaso;

				final boolean estaEnAbierta = this.listaAbierta.contains(vecino);

				if (!estaEnAbierta || (nuevoCostoG < vecino.getCostoG())) {
					vecino.evaluar(nodoAct, nodoObjetivo, costoPaso);

					if (estaEnAbierta) {
						this.listaAbierta.remove(vecino); // Reordenar PriorityQueue
					}
					this.listaAbierta.add(vecino);
				}
			}
		}

		return recorrido;
	}

	private void reconstruirCamino(final Lista<NodoA> destino, final NodoA nodoObjetivo) {
		final ArrayList<NodoA> temporal = new ArrayList<>();
		NodoA actual = nodoObjetivo;

		while (actual != null) {
			temporal.add(actual);
			actual = actual.getNodoProcedente();
		}

		Collections.reverse(temporal);

		// Guardar en la Lista personalizada
		for (final NodoA n : temporal) {
			destino.add(n);
		}
	}

	private boolean colisiona(final NodoA n) {
		if (n == null) {
			return true;
		}
		return this.mundo.getTerreno().intersectaSolidoDijkstra(n.getAreaEnMundo())
				|| this.mundo.colisionaConObjetoSolido(n.getAreaEnMundo());
	}

	private ArrayList<NodoA> getNodosVecinos(final NodoA nodoAct) {
		final ArrayList<NodoA> vecinos = new ArrayList<>(8);
		final int x = nodoAct.getXNodo();
		final int y = nodoAct.getYNodo();

		for (int i = x - 1; i <= (x + 1); i++) {
			for (int j = y - 1; j <= (y + 1); j++) {
				if ((i == x) && (j == y)) {
					continue;
				}

				final NodoA vecino = this.getNodo(i, j);
				if (vecino != null) {
					vecinos.add(vecino);
				}
			}
		}
		return vecinos;
	}

	private void actualizarCodAct() {
		if (this.codAct == Integer.MAX_VALUE) {
			this.codAct = Integer.MIN_VALUE;
		} else {
			this.codAct++;
		}
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
				this.nodos[x][y] = new NodoA(x, y, this.dimensionNodo);
			}
		}
	}
}