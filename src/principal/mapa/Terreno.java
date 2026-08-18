package principal.mapa;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import principal.entes.modelos.tile.ListaModeloTile;
import principal.mapa.renderEntidades.ZoneBox;
import principal.utilidades.Globales;

/**
 * Representa el terreno lógico y gráfico del mapa del juego.
 * <p>
 * Implementa una matriz bidimensional ({@code GroupTile[][]}) para
 * almacenamiento en memoria contigua con acceso $O(1)$ de máximo rendimiento.
 * </p>
 */
public class Terreno implements Serializable {
	private static final long serialVersionUID = -230565732234345L;

	protected final int ANCHO;
	protected final int ALTO;
	protected final int CANTIDAD_ANCHO_GROUPTILE;
	protected final int CANTIDAD_ALTO_GROUPTILE;
	protected final int LADO_GRUPO_TILE;
	protected final int LADO_TILE;
	protected final long CANT_TILES;

	/**
	 * Matriz bidimensional de bloques de tiles. Acceso directo por índice:
	 * GRUPOS_TILES[gridX][gridY]
	 */
	protected final GroupTile[][] GRUPOS_TILES;

	/**
	 * Crea un nuevo terreno llenándolo por defecto con el modelo de tierra base.
	 *
	 * @param cantTilesAncho Cantidad de tiles a lo ancho.
	 * @param cantTilesAlto  Cantidad de tiles a lo alto.
	 * @param ladoTile       Tamaño en píxeles de cada tile.
	 */
	public Terreno(final int cantTilesAncho, final int cantTilesAlto, final int ladoTile) {
		this(cantTilesAncho, cantTilesAlto, ladoTile, ListaModeloTile.COD_TIERRA);
	}

	/**
	 * Crea un nuevo terreno llenándolo con un modelo de tile específico.
	 *
	 * @param cantTilesAncho Cantidad de tiles a lo ancho.
	 * @param cantTilesAlto  Cantidad de tiles a lo alto.
	 * @param ladoTile       Tamaño en píxeles de cada tile.
	 * @param idModeloTile   ID del modelo de tile por defecto.
	 */
	public Terreno(final int cantTilesAncho, final int cantTilesAlto, final int ladoTile, final int idModeloTile) {
		Globales.CONSTANTES.LADO_TILE = ladoTile;
		this.LADO_TILE = ladoTile;
		this.LADO_GRUPO_TILE = ladoTile * 2;
		this.CANTIDAD_ANCHO_GROUPTILE = cantTilesAncho / 2;
		this.CANTIDAD_ALTO_GROUPTILE = cantTilesAlto / 2;
		this.ANCHO = ladoTile * cantTilesAncho;
		this.ALTO = ladoTile * cantTilesAlto;

		// Inicialización de la matriz 2D
		this.GRUPOS_TILES = new GroupTile[this.CANTIDAD_ANCHO_GROUPTILE][this.CANTIDAD_ALTO_GROUPTILE];
		this.llenarVacioTerreno(idModeloTile);
		this.CANT_TILES = (long) cantTilesAncho * cantTilesAlto;
	}

	/**
	 * Reconstruye una instancia de {@link Terreno} desde un objeto JSON.
	 *
	 * @param jso Objeto {@link JSONObject} serializado.
	 */
	public Terreno(final JSONObject jso) {
		this.CANTIDAD_ANCHO_GROUPTILE = ((Number) jso.get("cantGTancho")).intValue();
		this.CANTIDAD_ALTO_GROUPTILE = ((Number) jso.get("cantGTalto")).intValue();
		this.LADO_TILE = Globales.CONSTANTES.LADO_TILE;
		this.LADO_GRUPO_TILE = this.LADO_TILE * 2;
		this.ANCHO = ((Number) jso.get("ancho")).intValue();
		this.ALTO = ((Number) jso.get("alto")).intValue();
		this.CANT_TILES = ((Number) jso.get("cantTile")).longValue();

		// Inicialización de la matriz 2D
		this.GRUPOS_TILES = new GroupTile[this.CANTIDAD_ANCHO_GROUPTILE][this.CANTIDAD_ALTO_GROUPTILE];

		JSONArray listaGT = null;
		final Object gtObj = jso.get("GT");

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
					final GroupTile gt = GroupTile.crearDesdeJson((JSONObject) o);
					final int gx = gt.getPosicionX() / this.LADO_GRUPO_TILE;
					final int gy = gt.getPosicionY() / this.LADO_GRUPO_TILE;

					if ((gx >= 0) && (gx < this.CANTIDAD_ANCHO_GROUPTILE) && (gy >= 0)
							&& (gy < this.CANTIDAD_ALTO_GROUPTILE)) {
						this.GRUPOS_TILES[gx][gy] = gt;
					}
				}
			}
		}
	}

	/**
	 * Exporta los datos del terreno a un formato estructurado en JSON.
	 *
	 * @return Objeto {@link JSONObject} con la estructura serializada.
	 */
	@SuppressWarnings("unchecked")
	public JSONObject getTilesJson() {
		final JSONObject terreno = new JSONObject();
		terreno.put("cantGTancho", this.CANTIDAD_ANCHO_GROUPTILE);
		terreno.put("cantGTalto", this.CANTIDAD_ALTO_GROUPTILE);
		terreno.put("cantTile", this.CANT_TILES);
		terreno.put("ladoTile", this.ladoTile());
		terreno.put("ancho", this.ANCHO);
		terreno.put("alto", this.ALTO);

		final JSONArray gtLista = new JSONArray();
		for (int x = 0; x < this.CANTIDAD_ANCHO_GROUPTILE; x++) {
			for (int y = 0; y < this.CANTIDAD_ALTO_GROUPTILE; y++) {
				final GroupTile gt = this.GRUPOS_TILES[x][y];
				if (gt != null) {
					gtLista.add(gt.exportarParaJSON());
				}
			}
		}
		terreno.put("GT", gtLista);
		return terreno;
	}

	/**
	 * Retorna la matriz bidimensional interna de {@link GroupTile}.
	 */
	public GroupTile[][] getGroupTILES() {
		return this.GRUPOS_TILES;
	}

	public ArrayList<Tile> getTILES() {
		final ArrayList<Tile> lista = new ArrayList<Tile>();
		for (int x = 0; x < this.CANTIDAD_ANCHO_GROUPTILE; x++) {
			for (int y = 0; y < this.CANTIDAD_ALTO_GROUPTILE; y++) {
				final GroupTile gt = this.GRUPOS_TILES[x][y];
				if (gt != null) {
					lista.addAll(gt.getTiles()); // <--- ¡Cambiado aquí!
				}
			}
		}
		return lista;
	}

	public GroupTile getGrupoTileReferenciado(final Point punto) {
		if (punto == null) {
			return null;
		}
		return this.getGrupoTileReferenciado(punto.x, punto.y);
	}

	/**
	 * Obtiene el {@link GroupTile} correspondiente a las coordenadas del mundo
	 * (píxeles). Acceso $O(1)$ directo a la matriz.
	 */
	public GroupTile getGrupoTileReferenciado(final int x, final int y) {
		final int gtX = Math.floorDiv(x, this.LADO_GRUPO_TILE);
		final int gtY = Math.floorDiv(y, this.LADO_GRUPO_TILE);

		if ((gtX < 0) || (gtX >= this.CANTIDAD_ANCHO_GROUPTILE) || (gtY < 0) || (gtY >= this.CANTIDAD_ALTO_GROUPTILE)) {
			return null;
		}

		return this.GRUPOS_TILES[gtX][gtY];
	}

	public Tile getTileReferenciado(final int x, final int y) {
		final GroupTile gt = this.getGrupoTileReferenciado(x, y);
		if (gt != null) {
			return gt.getTileReferenciado(new Point(x, y));
		}
		return null;
	}

	public Tile getTileReferenciado(final Point p) {
		if (p == null) {
			return null;
		}
		return this.getTileReferenciado(p.x, p.y);
	}

	public void llenarVacioTerreno(final int idModeloTile) {
		for (int y = 0; y < this.CANTIDAD_ALTO_GROUPTILE; y++) {
			for (int x = 0; x < this.CANTIDAD_ANCHO_GROUPTILE; x++) {
				this.GRUPOS_TILES[x][y] = new GroupTile(x * this.LADO_GRUPO_TILE, y * this.LADO_GRUPO_TILE,
						this.LADO_GRUPO_TILE, idModeloTile);
			}
		}
	}

	public void establecerTileReferenciado(final Point punto, final Tile tile) {
		if ((punto == null) || (tile == null)) {
			return;
		}
		final GroupTile gt = this.getGrupoTileReferenciado(punto.x, punto.y);
		if (gt != null) {
			final Point puntoTile = new Point(punto.x / this.LADO_TILE, punto.y / this.LADO_TILE);
			gt.establecerTileEspecifico(puntoTile, tile);
		}
	}

	/**
	 * Renderiza en pantalla únicamente los bloques {@link GroupTile} visibles por
	 * la cámara.
	 * <p>
	 * Clampa los índices directamente a los límites de la matriz 2D para garantizar
	 * cero evaluaciones innecesarias.
	 * </p>
	 *
	 * @param g Contexto gráfico {@link Graphics2D}.
	 */
	public void pintar(final Graphics2D g) {
		final int minX = Globales.CAMARA.getPosicionXInt() - Globales.CONSTANTES.CENTROX
				- (3 * this.LADO_TILE);
		final int maxX = Globales.CAMARA.getPosicionXInt() + Globales.CONSTANTES.CENTROX
				+ (3 * this.LADO_TILE);

		final int minY = Globales.CAMARA.getPosicionYInt() - Globales.CONSTANTES.CENTROY
				- (3 * this.LADO_TILE);
		final int maxY = Globales.CAMARA.getPosicionYInt() + Globales.CONSTANTES.CENTROY
				+ (3 * this.LADO_TILE);

		// Delimitación acotada directamente dentro de los rangos de la matriz
		final int startGtX = Math.max(0, Math.floorDiv(minX, this.LADO_GRUPO_TILE));
		final int endGtX = Math.min(this.CANTIDAD_ANCHO_GROUPTILE - 1, Math.floorDiv(maxX, this.LADO_GRUPO_TILE));

		final int startGtY = Math.max(0, Math.floorDiv(minY, this.LADO_GRUPO_TILE));
		final int endGtY = Math.min(this.CANTIDAD_ALTO_GROUPTILE - 1, Math.floorDiv(maxY, this.LADO_GRUPO_TILE));

		GroupTile gt = null;

		for (int gtY = startGtY; gtY <= endGtY; gtY++) {
			for (int gtX = startGtX; gtX <= endGtX; gtX++) {
				gt = this.GRUPOS_TILES[gtX][gtY];
				if (gt != null) {
					gt.pintar(g);
				}
			}
		}
	}

	/**
	 * Renderiza únicamente las celdas espaciales ({@link ZoneBox}) visibles en la
	 * cámara.
	 *
	 * @param g        Contexto gráfico {@link Graphics2D}.
	 * @param zonas    Mapa de celdas espaciales activas indexadas por coordenadas
	 *                 de grilla.
	 * @param ladoZona Dimensión en píxeles del lado de cada celda espacial.
	 */
	public void pintarZonas(final Graphics2D g, final HashMap<Point, ZoneBox> zonas, final int ladoZona) {
		if ((zonas == null) || zonas.isEmpty()) {
			return;
		}

		final int minX = Globales.CAMARA.getPosicionXInt() - Globales.CONSTANTES.CENTROX
				- (3 * this.LADO_TILE);
		final int maxX = Globales.CAMARA.getPosicionXInt() + Globales.CONSTANTES.CENTROX
				+ (3 * this.LADO_TILE);

		final int minY = Globales.CAMARA.getPosicionYInt() - Globales.CONSTANTES.CENTROY
				- (3 * this.LADO_TILE);
		final int maxY = Globales.CAMARA.getPosicionYInt() + Globales.CONSTANTES.CENTROY
				+ (3 * this.LADO_TILE);

		final int inicioGridX = Math.floorDiv(minX, ladoZona);
		final int finGridX = Math.floorDiv(maxX, ladoZona);

		final int inicioGridY = Math.floorDiv(minY, ladoZona);
		final int finGridY = Math.floorDiv(maxY, ladoZona);

		final Point claveBusqueda = new Point();
		ZoneBox zbAux = null;

		for (int gridY = inicioGridY; gridY <= finGridY; gridY++) {
			for (int gridX = inicioGridX; gridX <= finGridX; gridX++) {
				claveBusqueda.setLocation(gridX, gridY);
				zbAux = zonas.get(claveBusqueda);

				if (zbAux != null) {
					zbAux.pintar(g);
				}
			}
		}
	}

	/**
	 * Ejecuta la actualización lógica de las celdas espaciales visibles en
	 * pantalla.
	 *
	 * @param zonas    Mapa de celdas espaciales activas.
	 * @param ladoZona Dimensión en píxeles de la zona.
	 */
	public void actualizarZonas(final HashMap<Point, ZoneBox> zonas, final int ladoZona) {
		if ((zonas == null) || zonas.isEmpty()) {
			return;
		}

		final int minX = Globales.CAMARA.getPosicionXInt() - Globales.CONSTANTES.CENTROX
				- (3 * this.LADO_TILE);
		final int maxX = Globales.CAMARA.getPosicionXInt() + Globales.CONSTANTES.CENTROX
				+ (3 * this.LADO_TILE);

		final int minY = Globales.CAMARA.getPosicionYInt() - Globales.CONSTANTES.CENTROY
				- (3 * this.LADO_TILE);
		final int maxY = Globales.CAMARA.getPosicionYInt() + Globales.CONSTANTES.CENTROY
				+ (3 * this.LADO_TILE);

		final int inicioGridX = Math.floorDiv(minX, ladoZona);
		final int finGridX = Math.floorDiv(maxX, ladoZona);

		final int inicioGridY = Math.floorDiv(minY, ladoZona);
		final int finGridY = Math.floorDiv(maxY, ladoZona);

		final Point claveBusqueda = new Point();
		ZoneBox zbAux = null;

		for (int gridY = inicioGridY; gridY <= finGridY; gridY++) {
			for (int gridX = inicioGridX; gridX <= finGridX; gridX++) {
				claveBusqueda.setLocation(gridX, gridY);
				zbAux = zonas.get(claveBusqueda);

				if (zbAux != null) {
					zbAux.actualizar();
				}
			}
		}
	}

	public boolean contienePuntoGrupoTileReferenciado(final int x, final int y) {
		return this.getGrupoTileReferenciado(x, y) != null;
	}

	public boolean contienePuntoGrupoTileReferenciado(final Point p) {
		return (p != null) && this.contienePuntoGrupoTileReferenciado(p.x, p.y);
	}

	public boolean contienePuntoTileReferenciado(final int x, final int y) {
		return this.getTileReferenciado(x, y) != null;
	}

	public boolean contienePuntoTileReferenciado(final Point p) {
		return (p != null) && this.contienePuntoTileReferenciado(p.x, p.y);
	}

	/**
	 * Obtiene la lista de tiles que se intersectan con una determinada forma
	 * geométrica.
	 *
	 * @param s Forma geométrica a comprobar.
	 * @return Lista de tiles intersectados.
	 */
	public ArrayList<Tile> getTilesIntersectados(final Shape s) {
		final ArrayList<Tile> tilesIntersectados = new ArrayList<Tile>();
		if (s == null) {
			return tilesIntersectados;
		}

		final Rectangle bounds = s.getBounds();

		final int minTileX = Math.floorDiv(bounds.x, this.LADO_TILE);
		final int maxTileX = Math.floorDiv(bounds.x + bounds.width, this.LADO_TILE);

		final int minTileY = Math.floorDiv(bounds.y, this.LADO_TILE);
		final int maxTileY = Math.floorDiv(bounds.y + bounds.height, this.LADO_TILE);

		Tile tile = null;

		for (int x = minTileX; x <= maxTileX; x++) {
			for (int y = minTileY; y <= maxTileY; y++) {
				tile = this.getTileReferenciado(x * this.LADO_TILE, y * this.LADO_TILE);

				if ((tile != null) && s.intersects(tile.getArea())) {
					tilesIntersectados.add(tile);
				}
			}
		}

		return tilesIntersectados;
	}

	/**
	 * Evalúa si un área rectangular intersecta con alguna celda de la grilla.
	 *
	 * @param r Rectángulo de colisión a verificar.
	 * @return {@code true} si existe intersección; {@code false} en caso contrario.
	 */
	public boolean intersecta(final Rectangle r) {
		if ((r == null) || r.isEmpty()) {
			return false;
		}

		final int minTileX = Math.floorDiv(r.x, this.LADO_TILE);
		final int maxTileX = Math.floorDiv((r.x + r.width) - 1, this.LADO_TILE);

		final int minTileY = Math.floorDiv(r.y, this.LADO_TILE);
		final int maxTileY = Math.floorDiv((r.y + r.height) - 1, this.LADO_TILE);

		for (int x = minTileX; x <= maxTileX; x++) {
			for (int y = minTileY; y <= maxTileY; y++) {
				if (this.getTileReferenciado(x * this.LADO_TILE, y * this.LADO_TILE) != null) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Evalúa si un área intersecta con un tile considerado sólido para el algoritmo
	 * de Dijkstra/Pathfinding.
	 */
	public boolean intersectaSolidoDijkstra(final Shape area) {
		if (area == null) {
			return false;
		}

		final Rectangle bounds = area.getBounds();
		final int minTileX = Math.floorDiv(bounds.x, this.LADO_TILE);
		final int maxTileX = Math.floorDiv(bounds.x + bounds.width, this.LADO_TILE);
		final int minTileY = Math.floorDiv(bounds.y, this.LADO_TILE);
		final int maxTileY = Math.floorDiv(bounds.y + bounds.height, this.LADO_TILE);

		for (int x = minTileX; x <= maxTileX; x++) {
			for (int y = minTileY; y <= maxTileY; y++) {
				final Tile t = this.getTileReferenciado(x * this.LADO_TILE, y * this.LADO_TILE);
				if ((t != null) && t.esSolidoDijkstra() && area.intersects(t.getArea())) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Evalúa si un área colisiona con algún objeto o tile sólido.
	 */
	public boolean intersectaAlgoSolido(final Shape area) {
		if (area == null) {
			return false;
		}

		final Rectangle bounds = area.getBounds();
		final int minTileX = Math.floorDiv(bounds.x, this.LADO_TILE);
		final int maxTileX = Math.floorDiv(bounds.x + bounds.width, this.LADO_TILE);
		final int minTileY = Math.floorDiv(bounds.y, this.LADO_TILE);
		final int maxTileY = Math.floorDiv(bounds.y + bounds.height, this.LADO_TILE);

		for (int x = minTileX; x <= maxTileX; x++) {
			for (int y = minTileY; y <= maxTileY; y++) {
				final Tile t = this.getTileReferenciado(x * this.LADO_TILE, y * this.LADO_TILE);
				if ((t != null) && t.hayColisionConAlgoSolido(area)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Evalúa si un área intersecta con un tile sólido.
	 */
	public boolean intersectaTileSolido(final Shape area) {
		if (area == null) {
			return false;
		}

		final Rectangle bounds = area.getBounds();
		final int minTileX = Math.floorDiv(bounds.x, this.LADO_TILE);
		final int maxTileX = Math.floorDiv(bounds.x + bounds.width, this.LADO_TILE);
		final int minTileY = Math.floorDiv(bounds.y, this.LADO_TILE);
		final int maxTileY = Math.floorDiv(bounds.y + bounds.height, this.LADO_TILE);

		for (int x = minTileX; x <= maxTileX; x++) {
			for (int y = minTileY; y <= maxTileY; y++) {
				final Tile t = this.getTileReferenciado(x * this.LADO_TILE, y * this.LADO_TILE);
				if ((t != null) && t.esSolido() && area.intersects(t.getArea())) {
					return true;
				}
			}
		}

		return false;
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
		return this.LADO_GRUPO_TILE;
	}

	public boolean AreaDentroDelTerreno(final Rectangle r) {
		if (r == null) {
			return false;
		}
		return !((r.x < 0) || (r.y < 0) || ((r.x + r.width) > this.ANCHO) || ((r.y + r.height) > this.ALTO));
	}

	public boolean areaEnSectorNoSolido(final Rectangle r) {
		if (!this.AreaDentroDelTerreno(r)) {
			System.out.println("se ha detectado una colocacion en area no valida. Fuera del terreno! " + r);
			return false;
		}

		final Tile tile = this.getTileReferenciado(r.x, r.y);
		if ((tile != null) && tile.esSolidoDijkstra()) {
			System.out.println("se ha detectado una colocacion en area no valida. TileSolido " + r);
			return false;
		}
		return true;
	}

	public long getCantidadTiles() {
		return this.CANT_TILES;
	}
}