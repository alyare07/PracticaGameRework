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

/**
 * Representa la superficie del mapa del juego (grilla continua de tiles).
 * <p>
 * <b>ARQUITECTURA DE ALTO RENDIMIENTO (Zero-GC & Cache Locality):</b> En lugar
 * de usar una matriz bidimensional {@code Tile[][]}, este motor almacena todos
 * los tiles en un único arreglo unidimensional {@code Tile[]}. <br>
 * <i>¿Por qué?</i> En Java, una matriz 2D {@code Tile[Y][X]} es en realidad un
 * arreglo de punteros a otros arreglos, dispersos por la memoria RAM. Un
 * arreglo plano {@code Tile[]} garantiza que los datos estén contiguos,
 * permitiendo que la CPU cargue bloques enteros en su memoria Caché L1/L2
 * (Cache Line Prefetching), acelerando drásticamente el render y las colisiones
 * a 60 FPS continuos.
 * </p>
 * 
 * @version 2.1 (Java 8 Compatible - DDA Raycasting Integrado)
 */
public class Terreno implements Serializable {

	private static final long serialVersionUID = -230565732234345L;

	/** Ancho total del terreno en píxeles. */
	protected final int ANCHO;

	/** Alto total del terreno en píxeles. */
	protected final int ALTO;

	/** Cantidad de columnas (tiles horizontales) en la grilla. */
	protected final int CANTIDAD_TILES_X;

	/** Cantidad de filas (tiles verticales) en la grilla. */
	protected final int CANTIDAD_TILES_Y;

	/** Tamaño en píxeles del lado de cada celda (generalmente 16 o 32 px). */
	protected final int LADO_TILE;

	/**
	 * Total absoluto de celdas en el terreno (CANTIDAD_TILES_X * CANTIDAD_TILES_Y).
	 */
	protected final long CANT_TILES;

	/**
	 * Arreglo unidimensional que contiene la totalidad de los tiles del mapa.
	 * Acceso matemático directo O(1):
	 * {@code indice = (ty * CANTIDAD_TILES_X) + tx}.
	 */
	protected final Tile[] TILES;

	/**
	 * Constructor para generar un terreno nuevo con un modelo de suelo por defecto
	 * (Tierra).
	 *
	 * @param cantTilesAncho Cantidad de celdas horizontales.
	 * @param cantTilesAlto  Cantidad de celdas verticales.
	 * @param ladoTile       Tamaño de cada celda en píxeles.
	 */
	public Terreno(final int cantTilesAncho, final int cantTilesAlto, final int ladoTile) {
		this(cantTilesAncho, cantTilesAlto, ladoTile, ListaModeloTile.COD_TIERRA);
	}

	/**
	 * Constructor principal para inicializar un terreno completamente vacío con un
	 * tipo de tile específico.
	 *
	 * @param cantTilesAncho Cantidad de celdas horizontales.
	 * @param cantTilesAlto  Cantidad de celdas verticales.
	 * @param ladoTile       Tamaño de cada celda en píxeles.
	 * @param idModeloTile   ID del modelo base con el que se rellenará el mapa.
	 */
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
	}

	/**
	 * Constructor para deserializar un terreno desde un archivo JSON. Incluye
	 * retrocompatibilidad con versiones heredadas (que usaban agrupaciones "GT" o
	 * "GroupTiles").
	 *
	 * @param jso Objeto {@link JSONObject} con la estructura guardada del terreno.
	 */
	public Terreno(final JSONObject jso) {
		this.LADO_TILE = Constantes.LADO_TILE;
		this.ANCHO = ((Number) jso.get("ancho")).intValue();
		this.ALTO = ((Number) jso.get("alto")).intValue();
		this.CANTIDAD_TILES_X = this.ANCHO / this.LADO_TILE;
		this.CANTIDAD_TILES_Y = this.ALTO / this.LADO_TILE;
		this.CANT_TILES = (long) this.CANTIDAD_TILES_X * this.CANTIDAD_TILES_Y;
		this.TILES = new Tile[this.CANTIDAD_TILES_X * this.CANTIDAD_TILES_Y];

		// =====================================================================
		// 1. CARGA FORMATO MODERNO: Lista plana de Tiles
		// =====================================================================
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
		}
		// =====================================================================
		// 2. RETROCOMPATIBILIDAD: Cargar formato antiguo con "GroupTiles" ("GT")
		// =====================================================================
		else {
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

		// Rellenar huecos con suelo por defecto en caso de celdas faltantes en el JSON
		for (int i = 0; i < this.TILES.length; i++) {
			if (this.TILES[i] == null) {
				final int tx = i % this.CANTIDAD_TILES_X;
				final int ty = i / this.CANTIDAD_TILES_X;
				this.TILES[i] = new Tile(tx * this.LADO_TILE, ty * this.LADO_TILE, this.LADO_TILE,
						ListaModeloTile.COD_TIERRA);
			}
		}

		// Recalcular autotiling para conectar todas las texturas leídas
		this.calcularAutotiles();
	}

	/**
	 * Exporta la totalidad de la estructura del terreno a un objeto JSON.
	 *
	 * @return Objeto {@link JSONObject} serializado.
	 */
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

	// =========================================================================
	// === AUTOTILING DETERMINISTA Y BITS
	// =========================================================================

	/**
	 * Genera una variación estética (0, 1, 2 o 3) de manera completamente
	 * matemática y determinista.
	 *
	 * @param gridX    Columna del tile en la grilla.
	 * @param gridY    Fila del tile en la grilla.
	 * @param idModelo Identificador del tipo de tile.
	 * @return Índice de variación cosmética.
	 */
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

	/**
	 * Recalcula la máscara de bits (Autotiling) y las variaciones de todos los
	 * tiles del mapa.
	 */
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

				// Norte (Fila superior)
				if ((ty > 0) && (this.TILES[((ty - 1) * this.CANTIDAD_TILES_X) + tx].getCodModelo() == modelo)) {
					mascara += 1;
				}
				// Este (Columna derecha)
				if ((tx < (this.CANTIDAD_TILES_X - 1)) && (this.TILES[fila + tx + 1].getCodModelo() == modelo)) {
					mascara += 2;
				}
				// Sur (Fila inferior)
				if ((ty < (this.CANTIDAD_TILES_Y - 1))
						&& (this.TILES[((ty + 1) * this.CANTIDAD_TILES_X) + tx].getCodModelo() == modelo)) {
					mascara += 4;
				}
				// Oeste (Columna izquierda)
				if ((tx > 0) && (this.TILES[(fila + tx) - 1].getCodModelo() == modelo)) {
					mascara += 8;
				}

				tileActual.setMascaraBit(mascara);
				tileActual.setVariacionPropia(this.calcularVariacionDeterminista(tx, ty, modelo));
			}
		}
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
	}

	public void actualizarAutotileLocal(final int worldX, final int worldY) {
		this.actualizarAutotile(worldX, worldY);
		this.actualizarAutotile(worldX, worldY - this.LADO_TILE);
		this.actualizarAutotile(worldX + this.LADO_TILE, worldY);
		this.actualizarAutotile(worldX, worldY + this.LADO_TILE);
		this.actualizarAutotile(worldX - this.LADO_TILE, worldY);
	}

	// =========================================================================
	// === ACCESO ESPACIAL O(1) DIRECTO
	// =========================================================================

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

	// =========================================================================
	// === RENDERIZADO Y FRUSTUM CULLING DINÁMICO
	// =========================================================================

	public void pintar(final Graphics2D g) {
		final double zoomActivo = Math.max(0.2, Globales.CAMARA.getZoomFinal());
		final double rotAbs = Math.abs(Globales.CAMARA.getGestorEfectos().getAnguloRotacion());
		final double shakeX = Math.abs(Globales.CAMARA.getGestorEfectos().getOffsetX());
		final double shakeY = Math.abs(Globales.CAMARA.getGestorEfectos().getOffsetY());

		final double cos = Math.cos(rotAbs);
		final double sin = Math.sin(rotAbs);

		final int radioVisibleX = (int) Math
				.ceil(((Constantes.CENTROX * cos) + (Constantes.CENTROY * sin)) / zoomActivo) + (int) shakeX
				+ this.LADO_TILE;
		final int radioVisibleY = (int) Math
				.ceil(((Constantes.CENTROX * sin) + (Constantes.CENTROY * cos)) / zoomActivo) + (int) shakeY
				+ this.LADO_TILE;

		final int camX = Globales.CAMARA.getPosicionXInt();
		final int camY = Globales.CAMARA.getPosicionYInt();

		final int startTileX = Math.max(0, Math.floorDiv(camX - radioVisibleX, this.LADO_TILE));
		final int endTileX = Math.min(this.CANTIDAD_TILES_X - 1, Math.floorDiv(camX + radioVisibleX, this.LADO_TILE));
		final int startTileY = Math.max(0, Math.floorDiv(camY - radioVisibleY, this.LADO_TILE));
		final int endTileY = Math.min(this.CANTIDAD_TILES_Y - 1, Math.floorDiv(camY + radioVisibleY, this.LADO_TILE));

		for (int ty = startTileY; ty <= endTileY; ty++) {
			final int fila = ty * this.CANTIDAD_TILES_X;
			for (int tx = startTileX; tx <= endTileX; tx++) {
				final Tile t = this.TILES[fila + tx];
				if (t != null) {
					t.pintar(g);
				}
			}
		}
	}

	// =========================================================================
	// === GESTIÓN DE COLISIONES Y RAYCASTING DDA 360° (ZERO-GC)
	// =========================================================================

	/**
	 * Comprueba mediante el algoritmo DDA (Digital Differential Analyzer) si existe
	 * una línea recta de visión o disparo 100% libre de obstáculos sólidos entre
	 * dos puntos del mundo en 360 grados.
	 * <p>
	 * <b>RENDIMIENTO (Zero-GC / O(K)):</b> Recorre matemáticamente solo las celdas
	 * de la grilla que el rayo atraviesa en su trayectoria sin generar objetos en
	 * el Heap ni calcular raíces cuadradas complejas.
	 * </p>
	 *
	 * @param x0 Coordenada X de origen en píxeles.
	 * @param y0 Coordenada Y de origen en píxeles.
	 * @param x1 Coordenada X de destino en píxeles.
	 * @param y1 Coordenada Y de destino en píxeles.
	 * @return {@code true} si la línea está completamente despejada; {@code false}
	 *         si choca con una pared u obstáculo.
	 */
	public boolean hayLineaDeVisionLimpia(final double x0, final double y0, final double x1, final double y1) {
		int tx = Math.floorDiv((int) x0, this.LADO_TILE);
		int ty = Math.floorDiv((int) y0, this.LADO_TILE);
		final int targetTX = Math.floorDiv((int) x1, this.LADO_TILE);
		final int targetTY = Math.floorDiv((int) y1, this.LADO_TILE);

		if ((tx == targetTX) && (ty == targetTY)) {
			return true; // Origen y destino en la misma celda
		}

		final double dx = x1 - x0;
		final double dy = y1 - y0;

		final int stepX = (dx > 0) ? 1 : ((dx < 0) ? -1 : 0);
		final int stepY = (dy > 0) ? 1 : ((dy < 0) ? -1 : 0);

		final double tDeltaX = (stepX != 0) ? Math.abs(this.LADO_TILE / dx) : Double.MAX_VALUE;
		final double tDeltaY = (stepY != 0) ? Math.abs(this.LADO_TILE / dy) : Double.MAX_VALUE;

		double tMaxX;
		if (stepX > 0) {
			tMaxX = (((tx + 1) * this.LADO_TILE) - x0) / dx;
		} else if (stepX < 0) {
			tMaxX = ((tx * this.LADO_TILE) - x0) / dx;
		} else {
			tMaxX = Double.MAX_VALUE;
		}

		double tMaxY;
		if (stepY > 0) {
			tMaxY = (((ty + 1) * this.LADO_TILE) - y0) / dy;
		} else if (stepY < 0) {
			tMaxY = ((ty * this.LADO_TILE) - y0) / dy;
		} else {
			tMaxY = Double.MAX_VALUE;
		}

		// Límite de seguridad para evitar bucles en coordenadas corruptas
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

			// Si alcanzamos la celda objetivo final, la línea está despejada
			if ((tx == targetTX) && (ty == targetTY)) {
				break;
			}

			// Si se sale del mapa o intersecta un tile sólido, la visión está obstruida
			final Tile tile = this.getTileGrid(tx, ty);
			if ((tile == null) || tile.esSolidoDijkstra()) {
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
				if ((t != null) && t.esSolidoDijkstra() && area.intersects(t.getArea())) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean intersectaAlgoSolido(final Shape area) {
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
				if ((t != null) && t.hayColisionConAlgoSolido(area)) {
					return true;
				}
			}
		}
		return false;
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
		return (tile == null) || !tile.esSolidoDijkstra();
	}

	// =========================================================================
	// === ACCESORES Y MÉTODOS DE COMPATIBILIDAD
	// =========================================================================

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