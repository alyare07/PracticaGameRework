package principal.utilidades.dijkstra;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

import principal.mapa.Tile;
import principal.utilidades.Constantes;

public class Dijkstra {
	public HashMap<Point, Nodo> NODOS = new HashMap<Point, Nodo>();
	public final int X_ULTIMO_NODO;
	public final int Y_ULTIMO_NODO;
	public final int LADO_NODO;
	public int cantNodoVisitados = 0;
	public int criaturasAlPendiente = 0;

	public Dijkstra(final ArrayList<Tile> listaTile, final int lado, final int xUltimoTile, final int yUltimoTile) {
		this.LADO_NODO = lado;
		for (Tile t : listaTile) {
			this.NODOS.put(t.getPosicionTile(), new Nodo(t));

		}
		this.X_ULTIMO_NODO = xUltimoTile / lado;
		this.Y_ULTIMO_NODO = yUltimoTile / lado;
//		System.out.println("nodos: " + listaTile.size());
//		System.out.println("xUltimo: " + X_ULTIMO_NODO);
//		System.out.println("yUltimo: " + Y_ULTIMO_NODO);
	}

	public Nodo getNodoReferenciado(final int x, final int y) {
		final Point posicionNodo = new Point(x / Constantes.LADO_TILE, y / Constantes.LADO_TILE);
		return this.NODOS.get(posicionNodo);
	}

	public void resetearVisitados() {
		for (Nodo n : this.NODOS.values()) {
			n.analizado = false;
			n.distancia = Double.MAX_VALUE;
		}
	}

	public void actualizar(final Point posicionTileObjetivo) {
		resetearVisitados();
//		final Point posiscionTileJugador = Constantes.JUGADOR.getPosicionTile();
		Nodo n = this.NODOS.get(posicionTileObjetivo);
		n.distancia = 0;
		n.analizado = true;
//		establecerDistanciasComienzo(posiscionTileJugador, n);
		establecerDistanciasComienzo4P(posicionTileObjetivo, n);
	}

	private void establecerDistanciasComienzo(final Point puntoEnTile, final Nodo objetivo) {
//		ArrayList<Nodo> vecinos = new ArrayList<Nodo>();
		HashMap<Point, Nodo> vecinos = new HashMap<Point, Nodo>();
		for (int y = puntoEnTile.y - 1; y <= puntoEnTile.y + 1; y++) {
			if (y < 0 || y > this.Y_ULTIMO_NODO) {
				continue;
			}
			for (int x = puntoEnTile.x - 1; x <= puntoEnTile.x + 1; x++) {
				if (x < 0 || x > this.X_ULTIMO_NODO) {
					continue;
				}
				if (x == puntoEnTile.x && y == puntoEnTile.y) {
					continue;
				}

				final Point p = new Point(x, y);
				final Nodo nodoAct = this.NODOS.get(p);
				if (nodoAct == null) {
					System.out.println("NODO NULL EN: " + p);
					continue;
				}
				if (nodoAct.analizado) {
					continue;
				}

				if (nodoAct.TILE.esSolidoDisktra()) {
					nodoAct.distancia = Double.MAX_VALUE;
					nodoAct.analizado = true;
					this.cantNodoVisitados++;
					continue;
				}
//				if (puntoEnTile.x != x & puntoEnTile.y != y) {
//					nodoAct.distancia = objetivo.distancia + 1.41;
//				} else {
//					nodoAct.distancia = objetivo.distancia + 1;
//				}
				nodoAct.distancia = (objetivo.distancia) + objetivo.POSICION_TILE.distance(p);
				vecinos.put(p, nodoAct);
				nodoAct.analizado = true;
				this.cantNodoVisitados++;
			}
		}

		if (vecinos.isEmpty()) {
			return;
		}

		analizarDistanciasReferidas(vecinos);

	}

	private void establecerDistanciasComienzo4P(final Point puntoEnTile, final Nodo objetivo) {
//		ArrayList<Nodo> vecinos = new ArrayList<Nodo>();
		HashMap<Point, Nodo> vecinos = new HashMap<Point, Nodo>();
		for (int y = puntoEnTile.y - 1; y <= puntoEnTile.y + 1; y++) {
			if (y < 0 || y > this.Y_ULTIMO_NODO) {
				continue;
			}
			if (y == puntoEnTile.y) {
				continue;
			}

			final Point p = new Point(puntoEnTile.x, y);
			final Nodo nodoAct = this.NODOS.get(p);
			if (nodoAct == null) {
				System.out.println("NODO NULL EN: " + p);
				continue;
			}
			if (nodoAct.analizado) {
				continue;
			}

			if (nodoAct.TILE.esSolidoDisktra()) {
				nodoAct.distancia = Double.MAX_VALUE;
				nodoAct.analizado = true;
				this.cantNodoVisitados++;
				continue;
			}
			nodoAct.distancia = (objetivo.distancia) + objetivo.POSICION_TILE.distance(p);
			vecinos.put(p, nodoAct);
			nodoAct.analizado = true;
			this.cantNodoVisitados++;
		}

		for (int x = puntoEnTile.x - 1; x <= puntoEnTile.x + 1; x++) {
			if (x < 0 || x > this.X_ULTIMO_NODO) {
				continue;
			}
			if (x == puntoEnTile.x) {
				continue;
			}

			final Point p = new Point(x, puntoEnTile.y);
			final Nodo nodoAct = this.NODOS.get(p);
			if (nodoAct == null) {
				System.out.println("NODO NULL EN: " + p);
				continue;
			}
			if (nodoAct.analizado) {
				continue;
			}

			if (nodoAct.TILE.esSolidoDisktra()) {
				nodoAct.distancia = Double.MAX_VALUE;
				nodoAct.analizado = true;
				this.cantNodoVisitados++;
				continue;
			}
			nodoAct.distancia = (objetivo.distancia) + objetivo.POSICION_TILE.distance(p);
			vecinos.put(p, nodoAct);
			nodoAct.analizado = true;
			this.cantNodoVisitados++;
		}

		if (vecinos.isEmpty()) {
			return;
		}

		analizarDistanciasReferidas4P(vecinos);

	}

	private void analizarDistanciasReferidas(final HashMap<Point, Nodo> lista) {
//		System.out.println("TAMANO SIGUIENTES REFERIDAS: " + lista.size());
		HashMap<Point, Nodo> vecinos = new HashMap<Point, Nodo>();
		for (Nodo n : lista.values()) {
			final int xNodo = n.POSICION_TILE.x;
			final int yNodo = n.POSICION_TILE.y;
			for (int y = yNodo - 1; y <= yNodo + 1; y++) {
				if (y < 0 || y > this.Y_ULTIMO_NODO) {
					continue;
				}
				for (int x = xNodo - 1; x <= xNodo + 1; x++) {
					if (x < 0 || x > this.X_ULTIMO_NODO) {
						continue;
					}
					if (x == xNodo && y == yNodo) {
						continue;
					}

					final Point p = new Point(x, y);
					final Nodo nodoAct = this.NODOS.get(p);
					if (nodoAct == null) {
						System.out.println("NODO NULL EN: " + p);
						continue;
					}
					if (nodoAct.analizado) {
						if (lista.containsKey(p)) {
							continue;
						}
						if (nodoAct.distancia != Double.MAX_VALUE && nodoAct.distancia > ((n.distancia) + n.POSICION_TILE.distance(p))) {
							nodoAct.distancia = (n.distancia) + n.POSICION_TILE.distance(p);
							continue;
						}
						continue;
					}

					if (nodoAct.TILE.esSolidoDisktra()) {
						nodoAct.distancia = Double.MAX_VALUE;
						nodoAct.analizado = true;
						this.cantNodoVisitados++;
						continue;
					}
//					if (xNodo != x & yNodo != y) {
//						nodoAct.distancia = n.distancia + 1.41;
//					} else {
//						nodoAct.distancia = n.distancia + 1;
//					}
					nodoAct.distancia = (n.distancia) + n.POSICION_TILE.distance(p);
					vecinos.put(p, nodoAct);
					nodoAct.analizado = true;
					this.cantNodoVisitados++;
				}
			}
		}
		if (vecinos.isEmpty()) {
			return;
		}
		analizarDistanciasReferidas(vecinos);

	}

	private void analizarDistanciasReferidas4P(final HashMap<Point, Nodo> lista) {
//		System.out.println("TAMANO SIGUIENTES REFERIDAS: " + lista.size());
		HashMap<Point, Nodo> vecinos = new HashMap<Point, Nodo>();
		for (Nodo n : lista.values()) {
			final int xNodo = n.POSICION_TILE.x;
			final int yNodo = n.POSICION_TILE.y;
			for (int y = yNodo - 1; y <= yNodo + 1; y++) {
				if (y < 0 || y > this.Y_ULTIMO_NODO) {
					continue;
				}
				if (yNodo == y) {
					continue;
				}

				final Point p = new Point(xNodo, y);
				final Nodo nodoAct = this.NODOS.get(p);
				if (nodoAct == null) {
					System.out.println("NODO NULL EN: " + p);
					continue;
				}
				if (nodoAct.analizado) {
					if (lista.containsKey(p)) {
						continue;
					}
					if (nodoAct.distancia != Double.MAX_VALUE && nodoAct.distancia > ((n.distancia) + n.POSICION_TILE.distance(p))) {
						nodoAct.distancia = (n.distancia) + n.POSICION_TILE.distance(p);
						continue;
					}
					continue;
				}

				if (nodoAct.TILE.esSolidoDisktra()) {
					nodoAct.distancia = Double.MAX_VALUE;
					nodoAct.analizado = true;
					this.cantNodoVisitados++;
					continue;
				}
				nodoAct.distancia = (n.distancia) + n.POSICION_TILE.distance(p);
				vecinos.put(p, nodoAct);
				nodoAct.analizado = true;
				this.cantNodoVisitados++;

			}

			for (int x = xNodo - 1; x <= xNodo + 1; x++) {
				if (x < 0 || x > this.X_ULTIMO_NODO) {
					continue;
				}
				if (x == xNodo) {
					continue;
				}

				final Point p = new Point(x, yNodo);
				final Nodo nodoAct = this.NODOS.get(p);
				if (nodoAct == null) {
					System.out.println("NODO NULL EN: " + p);
					continue;
				}
				if (nodoAct.analizado) {
					if (lista.containsKey(p)) {
						continue;
					}
					if (nodoAct.distancia != Double.MAX_VALUE && nodoAct.distancia > ((n.distancia) + n.POSICION_TILE.distance(p))) {
						nodoAct.distancia = (n.distancia) + n.POSICION_TILE.distance(p);
						continue;
					}
					continue;
				}

				if (nodoAct.TILE.esSolidoDisktra()) {
					nodoAct.distancia = Double.MAX_VALUE;
					nodoAct.analizado = true;
					this.cantNodoVisitados++;
					continue;
				}
				nodoAct.distancia = (n.distancia) + n.POSICION_TILE.distance(p);
				vecinos.put(p, nodoAct);
				nodoAct.analizado = true;
				this.cantNodoVisitados++;
			}
		}
		if (vecinos.isEmpty()) {
			return;
		}
		analizarDistanciasReferidas4P(vecinos);

	}

	public Nodo getNodoCercano(final int x, final int y) {
		final int xTile = x / LADO_NODO;
		final int yTile = y / LADO_NODO;
		Nodo nodoCercano = null;
		Nodo nodoAux = null;
		boolean primerNodo = true;
		for (int yNodo = yTile - 1; yNodo <= yTile + 1; yNodo++) {
			if (yNodo < 0 || yNodo > this.Y_ULTIMO_NODO) {
				continue;
			}
			for (int xNodo = xTile - 1; xNodo <= xTile + 1; xNodo++) {
				if (xNodo < 0 || xNodo > this.X_ULTIMO_NODO) {
					continue;
				}
				if (xTile == xNodo && yTile == yNodo) {
					continue;
				}
				if (primerNodo) {
					nodoCercano = this.NODOS.get(new Point(xNodo, yNodo));
					if (nodoCercano != null && nodoCercano.distancia != Double.MAX_VALUE && nodoCercano.analizado) {
						primerNodo = false;
					}
				} else {
					nodoAux = this.NODOS.get(new Point(xNodo, yNodo));
					if (nodoAux != null && nodoAux.distancia != Double.MAX_VALUE && nodoAux.analizado && nodoAux.distancia < nodoCercano.distancia) {
						nodoCercano = nodoAux;
					}
				}

			}
		}
		if (nodoCercano.distancia == Double.MAX_VALUE) {
			return null;
		}

		return nodoCercano;
	}

	public Nodo getNodoCercano4P(final int posX, final int posY) {
		final int xTile = posX / LADO_NODO;
		final int yTile = posY / LADO_NODO;
		Nodo nodoCercano = null;
		Nodo nodoAux = null;
		boolean primerNodo = true;
		for (int y = yTile - 1; y <= yTile + 1; y++) {
			if (y < 0 || y > this.Y_ULTIMO_NODO) {
				continue;
			}
			if (yTile == y) {
				continue;
			}
			if (primerNodo) {
				nodoCercano = this.NODOS.get(new Point(xTile, y));
				if (nodoCercano != null && nodoCercano.distancia != Double.MAX_VALUE && nodoCercano.analizado) {
					primerNodo = false;
				}
			} else {
				nodoAux = this.NODOS.get(new Point(xTile, y));
				if (nodoAux != null && nodoAux.distancia != Double.MAX_VALUE && nodoAux.analizado && nodoAux.distancia < nodoCercano.distancia) {
					nodoCercano = nodoAux;
				}
			}
		}

		for (int x = xTile - 1; x <= xTile + 1; x++) {
			if (x < 0 || x > this.X_ULTIMO_NODO) {
				continue;
			}
			if (x == xTile) {
				continue;
			}
			nodoAux = this.NODOS.get(new Point(x, yTile));
			if (nodoAux.distancia < nodoCercano.distancia) {
				nodoCercano = nodoAux;
			}
		}
		if (nodoCercano.distancia == Double.MAX_VALUE) {
			return null;
		}
		return nodoCercano;
	}

	public void aumentarCriaturasPendientes() {
		this.criaturasAlPendiente++;
	}

	public void reducirCriaturasPendientes() {
		if ((this.criaturasAlPendiente - 1) >= 0) {
			this.criaturasAlPendiente--;
		}
	}

	public boolean actualizarDijkstra() {
		return this.criaturasAlPendiente > 0;
	}

}
