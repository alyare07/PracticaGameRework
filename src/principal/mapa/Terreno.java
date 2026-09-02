package principal.mapa;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.io.Serializable;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import principal.entes.modelos.tile.ListaModeloTile;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;

public class Terreno implements Serializable {

	private static final long serialVersionUID = -230565732234345L;

	public static final int LADO_CHUNK = 256; // 16 tiles x 16 px = 256 px por Chunk

	protected final int ANCHO;
	protected final int ALTO;
	protected final int CANTIDAD_TILES_X;
	protected final int CANTIDAD_TILES_Y;
	protected final int LADO_TILE;
	protected final long CANT_TILES;
	protected final Tile[] TILES;

	// Sistema de Chunks en VRAM
	protected transient ChunkTerreno[] chunks;
	protected transient int cantChunksX;
	protected transient int cantChunksY;

	public Terreno(final int cantTilesAncho, final int cantTilesAlto, final int ladoTile) {
		this(cantTilesAncho, cantTilesAlto, ladoTile, ListaModeloTile.COD_TIERRA);
	}

	public Terreno(final int cantTilesAncho, final int cantTilesAlto, final int ladoTile, final int idModeloTile) {
		this.LADO_TILE = ladoTile;
		this.CANTIDAD_TILES_X = cantTilesAncho;
		this.CANTIDAD_TILES_Y = cantTilesAlto;
		this.ANCHO = cantTilesAncho * ladoTile;
		this.ALTO = cantTilesAlto * ladoTile;
		this.CANT_TILES = (long) cantTilesAncho * cantTilesAlto;

		this.TILES = new Tile[cantTilesAncho * cantTilesAlto];
		this.llenarVacioTerreno(idModeloTile);
		this.calcularAutotiles();
		this.inicializarChunks();
	}

	public Terreno(final JSONObject jso) {
		this.LADO_TILE = Constantes.LADO_TILE;
		this.ANCHO = ((Number) jso.get("ancho")).intValue();
		this.ALTO = ((Number) jso.get("alto")).intValue();
		this.CANTIDAD_TILES_X = this.ANCHO / this.LADO_TILE;
		this.CANTIDAD_TILES_Y = this.ALTO / this.LADO_TILE;
		this.CANT_TILES = (long) this.CANTIDAD_TILES_X * this.CANTIDAD_TILES_Y;
		this.TILES = new Tile[this.CANTIDAD_TILES_X * this.CANTIDAD_TILES_Y];

		final Object tilesObj = jso.get("Tiles");
		if (tilesObj instanceof JSONArray) {
			for (final Object o : (JSONArray) tilesObj) {
				if (o instanceof JSONObject) {
					final Tile t = Tile.crearDesdeJson((JSONObject) o);
					final int tx = Math.floorDiv(t.getPosicionX(), this.LADO_TILE);
					final int ty = Math.floorDiv(t.getPosicionY(), this.LADO_TILE);
					if ((tx >= 0) && (tx < this.CANTIDAD_TILES_X) && (ty >= 0) && (ty < this.CANTIDAD_TILES_Y)) {
						this.TILES[(ty * this.CANTIDAD_TILES_X) + tx] = t;
					}
				}
			}
		} else {
			final Object gtObj = jso.get("GT");
			JSONArray listaGT = null;
			if (gtObj instanceof JSONArray) {
				listaGT = (JSONArray) gtObj;
			} else if (gtObj != null) {
				try {
					listaGT = (JSONArray) new JSONParser().parse(gtObj.toString());
				} catch (final ParseException e) {
					e.printStackTrace();
				}
			}

			if (listaGT != null) {
				for (final Object o : listaGT) {
					if (o instanceof JSONObject) {
						final JSONObject gtJson = (JSONObject) o;
						final Object subTiles = gtJson.get("Tiles");
						if (subTiles instanceof JSONArray) {
							for (final Object st : (JSONArray) subTiles) {
								final Tile t = Tile.crearDesdeJson((JSONObject) st);
								final int tx = Math.floorDiv(t.getPosicionX(), this.LADO_TILE);
								final int ty = Math.floorDiv(t.getPosicionY(), this.LADO_TILE);
								if ((tx >= 0) && (tx < this.CANTIDAD_TILES_X) && (ty >= 0)
										&& (ty < this.CANTIDAD_TILES_Y)) {
									this.TILES[(ty * this.CANTIDAD_TILES_X) + tx] = t;
								}
							}
						}
					}
				}
			}
		}

		for (int i = 0; i < this.TILES.length; i++) {
			if (this.TILES[i] == null) {
				final int tx = i % this.CANTIDAD_TILES_X;
				final int ty = i / this.CANTIDAD_TILES_X;
				this.TILES[i] = new Tile(tx * this.LADO_TILE, ty * this.LADO_TILE, this.LADO_TILE,
						ListaModeloTile.COD_TIERRA);
			}
		}

		this.calcularAutotiles();
		this.inicializarChunks();
	}

	// =========================================================================
	// === SISTEMA DE CHUNKS PRE-HORNEADOS (ZERO-GC)
	// =========================================================================

	public void inicializarChunks() {
		this.cantChunksX = Math.max(1, (int) Math.ceil((double) this.ANCHO / LADO_CHUNK));
		this.cantChunksY = Math.max(1, (int) Math.ceil((double) this.ALTO / LADO_CHUNK));

		this.chunks = new ChunkTerreno[this.cantChunksX * this.cantChunksY];

		for (int cy = 0; cy < this.cantChunksY; cy++) {
			for (int cx = 0; cx < this.cantChunksX; cx++) {
				this.chunks[(cy * this.cantChunksX) + cx] = new ChunkTerreno(cx, cy, LADO_CHUNK);
			}
		}
	}

	public void marcarChunkSucio(final int worldX, final int worldY) {
		if (this.chunks == null) {
			return;
		}
		final int cx = Math.max(0, Math.min(this.cantChunksX - 1, Math.floorDiv(worldX, LADO_CHUNK)));
		final int cy = Math.max(0, Math.min(this.cantChunksY - 1, Math.floorDiv(worldY, LADO_CHUNK)));

		final ChunkTerreno chunk = this.chunks[(cy * this.cantChunksX) + cx];
		if (chunk != null) {
			chunk.marcarSucio();
		}
	}

	public void marcarTodosLosChunksSucios() {
		if (this.chunks == null) {
			return;
		}
		for (int i = 0; i < this.chunks.length; i++) {
			if (this.chunks[i] != null) {
				this.chunks[i].marcarSucio();
			}
		}
	}

	// =========================================================================
	// === RENDERIZADO OPTIMIZADO POR CHUNKS (BAJO OPF)
	// =========================================================================

	public void pintar(final Graphics2D g) {
		if (this.chunks == null) {
			this.inicializarChunks();
		}

		final double zoomActivo = Math.max(0.2, Globales.CAMARA.getZoomFinal());
		final double rotAbs = Math.abs(Globales.CAMARA.getGestorEfectos().getAnguloRotacion());
		final double shakeX = Math.abs(Globales.CAMARA.getGestorEfectos().getOffsetX());
		final double shakeY = Math.abs(Globales.CAMARA.getGestorEfectos().getOffsetY());

		final double cos = Math.cos(rotAbs);
		final double sin = Math.sin(rotAbs);

		// Margen de seguridad con soporte para rotación y temblor
		final int radioVisibleX = (int) Math
				.ceil(((Constantes.CENTROX * cos) + (Constantes.CENTROY * sin)) / zoomActivo) + (int) shakeX
				+ LADO_CHUNK;
		final int radioVisibleY = (int) Math
				.ceil(((Constantes.CENTROX * sin) + (Constantes.CENTROY * cos)) / zoomActivo) + (int) shakeY
				+ LADO_CHUNK;

		final int camX = Globales.CAMARA.getPosicionXInt();
		final int camY = Globales.CAMARA.getPosicionYInt();

		// Rango de chunks visibles en el frustum de la cámara
		final int startChunkX = Math.max(0, Math.floorDiv(camX - radioVisibleX, LADO_CHUNK));
		final int endChunkX = Math.min(this.cantChunksX - 1, Math.floorDiv(camX + radioVisibleX, LADO_CHUNK));
		final int startChunkY = Math.max(0, Math.floorDiv(camY - radioVisibleY, LADO_CHUNK));
		final int endChunkY = Math.min(this.cantChunksY - 1, Math.floorDiv(camY + radioVisibleY, LADO_CHUNK));

		for (int cy = startChunkY; cy <= endChunkY; cy++) {
			final int offset = cy * this.cantChunksX;
			for (int cx = startChunkX; cx <= endChunkX; cx++) {
				final ChunkTerreno chunk = this.chunks[offset + cx];
				if (chunk != null) {
					chunk.pintar(g, this);
				}
			}
		}
	}

	// =========================================================================
	// === AUTOTILING Y LÓGICA ESPACIAL
	// =========================================================================

	@SuppressWarnings("unchecked")
	public JSONObject getTilesJson() {
		final JSONObject terreno = new JSONObject();
		terreno.put("cantTile", Long.valueOf(this.CANT_TILES));
		terreno.put("ladoTile", Integer.valueOf(this.LADO_TILE));
		terreno.put("ancho", Integer.valueOf(this.ANCHO));
		terreno.put("alto", Integer.valueOf(this.ALTO));

		final JSONArray tilesLista = new JSONArray();
		for (int i = 0; i < this.TILES.length; i++) {
			if (this.TILES[i] != null) {
				tilesLista.add(this.TILES[i].exportarParaJSON());
			}
		}
		terreno.put("Tiles", tilesLista);
		return terreno;
	}

	private byte calcularVariacionDeterminista(final int gridX, final int gridY, final int idModelo) {
		int h = (gridX * 374761393) ^ (gridY * 668265263) ^ (idModelo * 3571);
		h = (h ^ (h >>> 13)) * 1274126177;
		final int roll = (h & 0x7FFFFFFF) % 100;

		if (roll < 90) {
			return 0;
		}
		if (roll < 95) {
			return 1;
		}
		if (roll < 99) {
			return 2;
		}
		return 3;
	}

	public void calcularAutotiles() {
		for (int ty = 0; ty < this.CANTIDAD_TILES_Y; ty++) {
			final int fila = ty * this.CANTIDAD_TILES_X;
			for (int tx = 0; tx < this.CANTIDAD_TILES_X; tx++) {
				final Tile tileActual = this.TILES[fila + tx];
				if (tileActual == null) {
					continue;
				}

				final int modelo = tileActual.getCodModelo();
				byte mascara = 0;

				if ((ty > 0) && (this.TILES[((ty - 1) * this.CANTIDAD_TILES_X) + tx].getCodModelo() == modelo)) {
					mascara += 1;
				}
				if ((tx < (this.CANTIDAD_TILES_X - 1)) && (this.TILES[fila + tx + 1].getCodModelo() == modelo)) {
					mascara += 2;
				}
				if ((ty < (this.CANTIDAD_TILES_Y - 1))
						&& (this.TILES[((ty + 1) * this.CANTIDAD_TILES_X) + tx].getCodModelo() == modelo)) {
					mascara += 4;
				}
				if ((tx > 0) && (this.TILES[(fila + tx) - 1].getCodModelo() == modelo)) {
					mascara += 8;
				}

				tileActual.setMascaraBit(mascara);
				tileActual.setVariacionPropia(this.calcularVariacionDeterminista(tx, ty, modelo));
			}
		}
		this.marcarTodosLosChunksSucios();
	}

	public void actualizarAutotile(final int worldX, final int worldY) {
		final int tx = Math.floorDiv(worldX, this.LADO_TILE);
		final int ty = Math.floorDiv(worldY, this.LADO_TILE);
		if ((tx < 0) || (tx >= this.CANTIDAD_TILES_X) || (ty < 0) || (ty >= this.CANTIDAD_TILES_Y)) {
			return;
		}

		final Tile tileActual = this.TILES[(ty * this.CANTIDAD_TILES_X) + tx];
		if (tileActual == null) {
			return;
		}

		final int modelo = tileActual.getCodModelo();
		byte mascara = 0;

		final Tile tN = this.getTileGrid(tx, ty - 1);
		if ((tN != null) && (tN.getCodModelo() == modelo)) {
			mascara += 1;
		}
		final Tile tE = this.getTileGrid(tx + 1, ty);
		if ((tE != null) && (tE.getCodModelo() == modelo)) {
			mascara += 2;
		}
		final Tile tS = this.getTileGrid(tx, ty + 1);
		if ((tS != null) && (tS.getCodModelo() == modelo)) {
			mascara += 4;
		}
		final Tile tO = this.getTileGrid(tx - 1, ty);
		if ((tO != null) && (tO.getCodModelo() == modelo)) {
			mascara += 8;
		}

		tileActual.setMascaraBit(mascara);
		tileActual.setVariacionPropia(this.calcularVariacionDeterminista(tx, ty, modelo));
		this.marcarChunkSucio(worldX, worldY);
	}

	public void actualizarAutotileLocal(final int worldX, final int worldY) {
		this.actualizarAutotile(worldX, worldY);
		this.actualizarAutotile(worldX, worldY - this.LADO_TILE);
		this.actualizarAutotile(worldX + this.LADO_TILE, worldY);
		this.actualizarAutotile(worldX, worldY + this.LADO_TILE);
		this.actualizarAutotile(worldX - this.LADO_TILE, worldY);
	}

	public Tile getTileGrid(final int tx, final int ty) {
		if ((tx < 0) || (tx >= this.CANTIDAD_TILES_X) || (ty < 0) || (ty >= this.CANTIDAD_TILES_Y)) {
			return null;
		}
		return this.TILES[(ty * this.CANTIDAD_TILES_X) + tx];
	}

	public Tile getTileReferenciado(final int x, final int y) {
		return this.getTileGrid(Math.floorDiv(x, this.LADO_TILE), Math.floorDiv(y, this.LADO_TILE));
	}

	public Tile getTileReferenciado(final Point p) {
		return (p != null) ? this.getTileReferenciado(p.x, p.y) : null;
	}

	public void llenarVacioTerreno(final int idModeloTile) {
		for (int ty = 0; ty < this.CANTIDAD_TILES_Y; ty++) {
			final int fila = ty * this.CANTIDAD_TILES_X;
			for (int tx = 0; tx < this.CANTIDAD_TILES_X; tx++) {
				this.TILES[fila + tx] = new Tile(tx * this.LADO_TILE, ty * this.LADO_TILE, this.LADO_TILE,
						idModeloTile);
			}
		}
		this.marcarTodosLosChunksSucios();
	}

	public void establecerTileReferenciado(final int x, final int y, final Tile tile) {
		if (tile == null) {
			return;
		}
		final int tx = Math.floorDiv(x, this.LADO_TILE);
		final int ty = Math.floorDiv(y, this.LADO_TILE);
		if ((tx >= 0) && (tx < this.CANTIDAD_TILES_X) && (ty >= 0) && (ty < this.CANTIDAD_TILES_Y)) {
			this.TILES[(ty * this.CANTIDAD_TILES_X) + tx] = new Tile(tx * this.LADO_TILE, ty * this.LADO_TILE,
					this.LADO_TILE, tile.getCodModelo());
			this.actualizarAutotileLocal(x, y);
		}
	}

	public void establecerTileReferenciado(final Point punto, final Tile tile) {
		if (punto != null) {
			this.establecerTileReferenciado(punto.x, punto.y, tile);
		}
	}

	public boolean contienePuntoTileReferenciado(final int x, final int y) {
		return this.getTileReferenciado(x, y) != null;
	}

	public boolean contienePuntoTileReferenciado(final Point p) {
		return (p != null) && this.contienePuntoTileReferenciado(p.x, p.y);
	}

	public boolean hayLineaDeVisionLimpia(final double x0, final double y0, final double x1, final double y1) {
		int tx = Math.floorDiv((int) x0, this.LADO_TILE);
		int ty = Math.floorDiv((int) y0, this.LADO_TILE);
		final int targetTX = Math.floorDiv((int) x1, this.LADO_TILE);
		final int targetTY = Math.floorDiv((int) y1, this.LADO_TILE);

		if ((tx == targetTX) && (ty == targetTY)) {
			return true;
		}

		final double dx = x1 - x0;
		final double dy = y1 - y0;

		final int stepX = (dx > 0) ? 1 : ((dx < 0) ? -1 : 0);
		final int stepY = (dy > 0) ? 1 : ((dy < 0) ? -1 : 0);

		final double tDeltaX = (stepX != 0) ? Math.abs(this.LADO_TILE / dx) : Double.MAX_VALUE;
		final double tDeltaY = (stepY != 0) ? Math.abs(this.LADO_TILE / dy) : Double.MAX_VALUE;

		double tMaxX = (stepX > 0) ? (((tx + 1) * this.LADO_TILE) - x0) / dx
				: ((stepX < 0) ? ((tx * this.LADO_TILE) - x0) / dx : Double.MAX_VALUE);

		double tMaxY = (stepY > 0) ? (((ty + 1) * this.LADO_TILE) - y0) / dy
				: ((stepY < 0) ? ((ty * this.LADO_TILE) - y0) / dy : Double.MAX_VALUE);

		final int maxPasos = this.CANTIDAD_TILES_X + this.CANTIDAD_TILES_Y;
		int pasos = 0;

		while (((tx != targetTX) || (ty != targetTY)) && (pasos++ < maxPasos)) {
			if (tMaxX < tMaxY) {
				tx += stepX;
				tMaxX += tDeltaX;
			} else {
				ty += stepY;
				tMaxY += tDeltaY;
			}

			if ((tx == targetTX) && (ty == targetTY)) {
				break;
			}

			final Tile tile = this.getTileGrid(tx, ty);
			if ((tile == null) || tile.esSolido()) {
				return false;
			}
		}

		return true;
	}

	public ArrayList<Tile> getTilesIntersectados(final Shape s) {
		final ArrayList<Tile> lista = new ArrayList<>();
		if (s == null) {
			return lista;
		}

		final Rectangle b = s.getBounds();
		final int minTileX = Math.max(0, Math.floorDiv(b.x, this.LADO_TILE));
		final int maxTileX = Math.min(this.CANTIDAD_TILES_X - 1, Math.floorDiv((b.x + b.width) - 1, this.LADO_TILE));
		final int minTileY = Math.max(0, Math.floorDiv(b.y, this.LADO_TILE));
		final int maxTileY = Math.min(this.CANTIDAD_TILES_Y - 1, Math.floorDiv((b.y + b.height) - 1, this.LADO_TILE));

		for (int ty = minTileY; ty <= maxTileY; ty++) {
			final int fila = ty * this.CANTIDAD_TILES_X;
			for (int tx = minTileX; tx <= maxTileX; tx++) {
				final Tile t = this.TILES[fila + tx];
				if ((t != null) && s.intersects(t.getArea())) {
					lista.add(t);
				}
			}
		}
		return lista;
	}

	public boolean intersecta(final Rectangle r) {
		if ((r == null) || r.isEmpty()) {
			return false;
		}

		final int minTileX = Math.max(0, Math.floorDiv(r.x, this.LADO_TILE));
		final int maxTileX = Math.min(this.CANTIDAD_TILES_X - 1, Math.floorDiv((r.x + r.width) - 1, this.LADO_TILE));
		final int minTileY = Math.max(0, Math.floorDiv(r.y, this.LADO_TILE));
		final int maxTileY = Math.min(this.CANTIDAD_TILES_Y - 1, Math.floorDiv((r.y + r.height) - 1, this.LADO_TILE));

		for (int ty = minTileY; ty <= maxTileY; ty++) {
			final int fila = ty * this.CANTIDAD_TILES_X;
			for (int tx = minTileX; tx <= maxTileX; tx++) {
				if (this.TILES[fila + tx] != null) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean intersectaSolidoDijkstra(final Shape area) {
		return this.intersectaTileSolido(area);
	}

	public boolean intersectaAlgoSolido(final Shape area) {
		return this.intersectaTileSolido(area);
	}

	public boolean intersectaTileSolido(final Shape area) {
		if (area == null) {
			return false;
		}
		final Rectangle b = area.getBounds();

		final int minTileX = Math.max(0, Math.floorDiv(b.x, this.LADO_TILE));
		final int maxTileX = Math.min(this.CANTIDAD_TILES_X - 1, Math.floorDiv((b.x + b.width) - 1, this.LADO_TILE));
		final int minTileY = Math.max(0, Math.floorDiv(b.y, this.LADO_TILE));
		final int maxTileY = Math.min(this.CANTIDAD_TILES_Y - 1, Math.floorDiv((b.y + b.height) - 1, this.LADO_TILE));

		for (int ty = minTileY; ty <= maxTileY; ty++) {
			final int fila = ty * this.CANTIDAD_TILES_X;
			for (int tx = minTileX; tx <= maxTileX; tx++) {
				final Tile t = this.TILES[fila + tx];
				if ((t != null) && t.esSolido() && area.intersects(t.getArea())) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean areaDentroDelTerreno(final Rectangle r) {
		if (r == null) {
			return false;
		}
		return !((r.x < 0) || (r.y < 0) || ((r.x + r.width) > this.ANCHO) || ((r.y + r.height) > this.ALTO));
	}

	public boolean AreaDentroDelTerreno(final Rectangle r) {
		return this.areaDentroDelTerreno(r);
	}

	public boolean areaEnSectorNoSolido(final Rectangle r) {
		if (!this.areaDentroDelTerreno(r)) {
			return false;
		}
		final Tile tile = this.getTileReferenciado(r.x, r.y);
		return (tile == null) || !tile.esSolido();
	}

	public ArrayList<Tile> getTILES() {
		final ArrayList<Tile> lista = new ArrayList<>((int) this.CANT_TILES);
		for (int i = 0; i < this.TILES.length; i++) {
			if (this.TILES[i] != null) {
				lista.add(this.TILES[i]);
			}
		}
		return lista;
	}

	public int getAncho() {
		return this.ANCHO;
	}

	public int getAlto() {
		return this.ALTO;
	}

	public int ladoTile() {
		return this.LADO_TILE;
	}

	public int ladoGrupoTile() {
		return this.LADO_TILE * 2;
	}

	public long getCantidadTiles() {
		return this.CANT_TILES;
	}
}