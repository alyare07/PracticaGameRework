package principal.mapa;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import principal.entes.modelos.tile.ListaModeloTile;
import principal.entes.modelos.tile.ModeloTile;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.Globales;
import principal.utilidades.Textura;

/**
 * Contenedor espacial que agrupa 4 {@link Tile}s en una subgrilla de 2x2.
 * <p>
 * Implementa únicamente una matriz bidimensional ({@code Tile[2][2]}) para cero
 * sobrecarga de memoria y acceso instantáneo $O(1)$.
 * </p>
 */
public final class GroupTile extends Tile {
	private static final long serialVersionUID = 1213432123L;

	/**
	 * Matriz 2x2: [0][0]=Arriba-Izq, [1][0]=Arriba-Der, [0][1]=Abajo-Izq,
	 * [1][1]=Abajo-Der
	 */
	protected final Tile[][] tilesMatriz = new Tile[2][2];
	protected final int LADO_TILES;

	public GroupTile(final int x, final int y, final int lado) {
		this(x, y, lado, ListaModeloTile.COD_TIERRA);
	}

	public GroupTile(final int x, final int y, final int lado, final int idModeloTile) {
		super(x, y, lado, 0);
		this.LADO_TILES = lado / 2;
		this.llenarTilesVacio(idModeloTile);
	}

	public GroupTile(final int x, final int y, final int lado, final Tile t1, final Tile t2, final Tile t3,
			final Tile t4) {
		super(x, y, lado, 0);
		this.LADO_TILES = lado / 2;
		this.meterTiles(t1, t2, t3, t4);
	}

	@Override
	public int getCodigoTextura() {
		return 0;
	}

	/**
	 * Posiciona 4 tiles dentro de la matriz 2x2 calculando su posición relativa.
	 */
	public void meterTiles(final Tile t1, final Tile t2, final Tile t3, final Tile t4) {
		final Tile[] entrada = { t1, t2, t3, t4 };

		for (final Tile t : entrada) {
			if (t == null) {
				continue;
			}

			final int relX = Math.floorDiv(t.getPosicionX() - this.X, this.LADO_TILES);
			final int relY = Math.floorDiv(t.getPosicionY() - this.Y, this.LADO_TILES);

			if ((relX >= 0) && (relX < 2) && (relY >= 0) && (relY < 2)) {
				this.tilesMatriz[relX][relY] = t;
			}
		}
	}

	public void llenarTilesVacio(final int codModeloTile) {
		this.tilesMatriz[0][0] = new Tile(this.X, this.Y, this.LADO_TILES, codModeloTile);
		this.tilesMatriz[1][0] = new Tile(this.X + this.LADO_TILES, this.Y, this.LADO_TILES, codModeloTile);
		this.tilesMatriz[0][1] = new Tile(this.X, this.Y + this.LADO_TILES, this.LADO_TILES, codModeloTile);
		this.tilesMatriz[1][1] = new Tile(this.X + this.LADO_TILES, this.Y + this.LADO_TILES, this.LADO_TILES,
				codModeloTile);
	}

	@Override
	public void pintar(final Graphics2D g) {
		for (int x = 0; x < 2; x++) {
			for (int y = 0; y < 2; y++) {
				if (this.tilesMatriz[x][y] != null) {
					this.tilesMatriz[x][y].pintar(g);
				}
			}
		}

		if (Globales.TECLADO.TECLA_DEBUG_GROUP_TILE.presionado() && Globales.estadoJuego) {
			DibujoDebug.dibujarImagenRefCamara(g, Textura.getTextura(Textura.idTexturaContornoGroupTile), this.X,
					this.Y);
		}
	}

	@Override
	public void pintarEditor(final Graphics2D g) {
		for (int x = 0; x < 2; x++) {
			for (int y = 0; y < 2; y++) {
				if (this.tilesMatriz[x][y] != null) {
					this.tilesMatriz[x][y].pintarEditor(g);
				}
			}
		}

		if (Globales.editorSelectGroupTile) {
			DibujoDebug.dibujarImagenRefCamara(g, Textura.getTextura(Textura.idTexturaContornoGroupTile), this.X,
					this.Y);
		}
	}

	public boolean establecerTileEspecifico(final Point puntoTile, final Tile tile) {
		if ((puntoTile == null) || (tile == null)) {
			return false;
		}

		// Convertimos el punto de la grilla de tiles a coordenadas relativas (0 o 1)
		final int worldX = puntoTile.x * Constantes.LADO_TILE;
		final int worldY = puntoTile.y * Constantes.LADO_TILE;

		final int relX = Math.floorDiv(worldX - this.X, this.LADO_TILES);
		final int relY = Math.floorDiv(worldY - this.Y, this.LADO_TILES);

		if ((relX >= 0) && (relX < 2) && (relY >= 0) && (relY < 2)) {
			this.tilesMatriz[relX][relY] = new Tile(worldX, worldY, this.LADO_TILES, tile.CODIGO_MODELO_TILE);
			return true;
		}
		return false;
	}

	@Override
	public boolean intersecta(final Rectangle area) {
		if (super.intersecta(area)) {
			for (int x = 0; x < 2; x++) {
				for (int y = 0; y < 2; y++) {
					final Tile t = this.tilesMatriz[x][y];
					if ((t != null) && (t.getEstado() == ModeloTile.ESTADO_OBSTACULO) && t.intersecta(area)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public Tile getTile1() {
		return this.tilesMatriz[0][0];
	}

	public Tile getTile2() {
		return this.tilesMatriz[1][0];
	}

	public Tile getTile3() {
		return this.tilesMatriz[0][1];
	}

	public Tile getTile4() {
		return this.tilesMatriz[1][1];
	}

	/**
	 * Retorna la lista de tiles contenidos en este bloque de 2x2.
	 */
	public Collection<Tile> getTiles() {
		final ArrayList<Tile> lista = new ArrayList<Tile>(4);
		if (this.tilesMatriz[0][0] != null) {
			lista.add(this.tilesMatriz[0][0]);
		}
		if (this.tilesMatriz[1][0] != null) {
			lista.add(this.tilesMatriz[1][0]);
		}
		if (this.tilesMatriz[0][1] != null) {
			lista.add(this.tilesMatriz[0][1]);
		}
		if (this.tilesMatriz[1][1] != null) {
			lista.add(this.tilesMatriz[1][1]);
		}
		return lista;
	}

	/**
	 * Obtiene el tile específico mediante coordenadas de mundo en píxeles.
	 */
	public Tile getTileReferenciado(final int x, final int y) {
		final int relX = Math.floorDiv(x - this.X, this.LADO_TILES);
		final int relY = Math.floorDiv(y - this.Y, this.LADO_TILES);

		if ((relX >= 0) && (relX < 2) && (relY >= 0) && (relY < 2)) {
			return this.tilesMatriz[relX][relY];
		}
		return null;
	}

	public Tile getTileReferenciado(final Point p) {
		if (p == null) {
			return null;
		}
		return this.getTileReferenciado(p.x, p.y);
	}

	@Override
	@SuppressWarnings("unchecked")
	public JSONObject exportarParaJSON() {
		final JSONObject json = new JSONObject();
		json.put("x", this.getPosicionX());
		json.put("y", this.getPosicionY());

		final JSONArray tiles = new JSONArray();
		for (int y = 0; y < 2; y++) {
			for (int x = 0; x < 2; x++) {
				if (this.tilesMatriz[x][y] != null) {
					tiles.add(this.tilesMatriz[x][y].exportarParaJSON());
				}
			}
		}
		json.put("Tiles", tiles);
		return json;
	}

	public static GroupTile crearDesdeJson(final JSONObject json) {
		final int x = ((Number) json.get("x")).intValue();
		final int y = ((Number) json.get("y")).intValue();

		final Object tilesObj = json.get("Tiles");
		JSONArray tiles = null;

		if (tilesObj instanceof JSONArray) {
			tiles = (JSONArray) tilesObj;
		} else if (tilesObj != null) {
			try {
				tiles = (JSONArray) new JSONParser().parse(tilesObj.toString());
			} catch (final ParseException e) {
				e.printStackTrace();
			}
		}

		Tile t1 = null, t2 = null, t3 = null, t4 = null;
		if ((tiles != null) && (tiles.size() >= 4)) {
			t1 = Tile.crearDesdeJson((JSONObject) tiles.get(0));
			t2 = Tile.crearDesdeJson((JSONObject) tiles.get(1));
			t3 = Tile.crearDesdeJson((JSONObject) tiles.get(2));
			t4 = Tile.crearDesdeJson((JSONObject) tiles.get(3));
		}

		return new GroupTile(x, y, Constantes.LADO_TILE * 2, t1, t2, t3, t4);
	}

	@Override
	public String toString() {
		return "GroupTile [AREA= x: " + this.AREA.x + " ,y: " + this.AREA.y + " , W: " + this.AREA.width + " ,H: "
				+ this.AREA.height + "]";
	}
}