package principal.utilidades.dijkstra;

import java.awt.Point;

import principal.mapa.Tile;

public class Nodo {
	public final Tile TILE;
	public boolean analizado;
	public double distancia;
	public final Point POSICION_TILE;

	public Nodo(final Tile tile) {
		this.TILE = tile;
		this.POSICION_TILE = tile.getPosicionTile();
	}

	@Override
	public String toString() {
		return "Nodo [TILE=" + TILE + ", analizado=" + analizado + ", distancia=" + distancia + "]";
	}

}
