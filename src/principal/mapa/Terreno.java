package principal.mapa;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import principal.entes.modelos.tile.ListaModeloTile;
import principal.mapa.renderEntidades.ZoneBox;
import principal.utilidades.Constantes;

public class Terreno implements Serializable {
	private static final long serialVersionUID = -230565732234345L;
	protected final int ANCHO;
	protected final int ALTO;
	protected final int CANTIDAD_ANCHO_GROUPTILE;
	protected final int CANTIDAD_ALTO_GROUPTILE;
	protected final int LADO_GRUPO_TILE;
	protected final int LADO_TILE;
	protected final long CANT_TILES;
	protected final HashMap<Point, GroupTile> GRUPOS_TILES = new HashMap<Point, GroupTile>();

	public Terreno(final int cantTilesAncho, final int cantTilesAlto, final int ladoTile) {
		Constantes.GLOBALES.ladoTile = ladoTile;
		this.LADO_GRUPO_TILE = ladoTile * 2;
		this.LADO_TILE = ladoTile;
		this.CANTIDAD_ANCHO_GROUPTILE = cantTilesAncho / 2;// Esto solo nos limitaria a cantidad en tiles pares
		this.CANTIDAD_ALTO_GROUPTILE = cantTilesAlto / 2;
		this.ANCHO = ladoTile * cantTilesAncho;
		this.ALTO = ladoTile * cantTilesAlto;
		this.CANT_TILES = (this.GRUPOS_TILES.size()
				* ((this.LADO_GRUPO_TILE / this.LADO_TILE) * (this.LADO_GRUPO_TILE / this.LADO_TILE)));
		this.llenarVacioTerreno(ListaModeloTile.COD_TIERRA);
	}

	public Terreno(final int cantTilesAncho, final int cantTilesAlto, final int ladoTile, final int idModeloTile) {
		Constantes.GLOBALES.ladoTile = ladoTile;
		this.LADO_GRUPO_TILE = ladoTile * 2;
		this.LADO_TILE = ladoTile;
		this.CANTIDAD_ANCHO_GROUPTILE = cantTilesAncho / 2;// Esto solo nos limitaria a cantidad en tiles pares
		this.CANTIDAD_ALTO_GROUPTILE = cantTilesAlto / 2;
		this.ANCHO = ladoTile * cantTilesAncho;
		this.ALTO = ladoTile * cantTilesAlto;
		this.CANT_TILES = (this.GRUPOS_TILES.size()
				* ((this.LADO_GRUPO_TILE / this.LADO_TILE) * (this.LADO_GRUPO_TILE / this.LADO_TILE)));
		this.llenarVacioTerreno(idModeloTile);
	}

	public Terreno(final JSONObject jso) {
		this.CANTIDAD_ANCHO_GROUPTILE = Integer.parseInt(jso.get("cantGTancho").toString());
		this.CANTIDAD_ALTO_GROUPTILE = Integer.parseInt(jso.get("cantGTalto").toString());
//		this.LADO_TILE = Integer.parseInt(jso.get("ladoTile").toString());
		this.LADO_TILE = Constantes.LADO_TILE;
		this.LADO_GRUPO_TILE = this.LADO_TILE * 2;
		this.ANCHO = Integer.parseInt(jso.get("ancho").toString());
		this.ALTO = Integer.parseInt(jso.get("alto").toString());
		this.CANT_TILES = Integer.parseInt(jso.get("cantTile").toString());

		JSONArray listaGT = null;
		try {
			listaGT = (JSONArray) new JSONParser().parse(jso.get("GT").toString());
			GroupTile gt = null;
			for (final Object o : listaGT) {
				if (o instanceof JSONObject) {
					gt = GroupTile.crearDesdeJson((JSONObject) o);
					this.GRUPOS_TILES.put(new Point(gt.getPosicionX() / this.LADO_GRUPO_TILE,
							gt.getPosicionY() / this.LADO_GRUPO_TILE), gt);
				}
			}
		} catch (final ParseException e) {
			e.printStackTrace();
		}

	}

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
		for (final GroupTile gt : this.GRUPOS_TILES.values()) {
			gtLista.add(gt.exportarParaJSON());
		}
		terreno.put("GT", gtLista);
		return terreno;
	}

	public HashMap<Point, GroupTile> getGroupTILES() {
		return this.GRUPOS_TILES;
	}

	public ArrayList<Tile> getTILES() {
		final ArrayList<Tile> lista = new ArrayList<Tile>();
		for (final GroupTile gt : this.GRUPOS_TILES.values()) {
			for (final Tile t : gt.TILES.values()) {
				lista.add(t);
			}
		}
		return lista;
	}

	public GroupTile getGrupoTileReferenciado(final Point punto) {
		return this.GRUPOS_TILES.get(new Point(punto.x / this.LADO_GRUPO_TILE, punto.y / this.LADO_GRUPO_TILE));
	}

	public GroupTile getGrupoTileReferenciado(final int x, final int y) {
		if ((x < 0) || (y < 0)) {
			return null;
		}
		return this.GRUPOS_TILES.get(new Point(x / this.LADO_GRUPO_TILE, y / this.LADO_GRUPO_TILE));
	}

	public Tile getTileReferenciado(final int x, final int y) {

		Tile t = null;
		if (this.contienePuntoGrupoTileReferenciado(x, y)) {
			t = this.getGrupoTileReferenciado(x, y).getTileReferenciado(new Point(x, y));
		}

		return t;
	}

	public Tile getTileReferenciado(final Point p) {
		Tile t = null;
		if (this.contienePuntoGrupoTileReferenciado(p.x, p.y)) {
			t = this.getGrupoTileReferenciado(p).getTileReferenciado(p);
		}
		return t;
	}

	public void llenarVacioTerreno(final int idModeloTile) {
		this.GRUPOS_TILES.clear();
		for (int y = 0; y < this.CANTIDAD_ALTO_GROUPTILE; y++) {
			for (int x = 0; x < this.CANTIDAD_ANCHO_GROUPTILE; x++) {
				this.GRUPOS_TILES.put(new Point(x, y), new GroupTile(x * this.LADO_GRUPO_TILE, y * this.LADO_GRUPO_TILE,
						this.LADO_GRUPO_TILE, idModeloTile));
			}
		}
	}

	public void establecerTileReferenciado(final Point punto, final Tile tile) {
		final Point puntoGroupTile = new Point(punto.x / this.LADO_GRUPO_TILE, punto.y / this.LADO_GRUPO_TILE);
		final Point puntoTile = new Point(punto.x / this.LADO_TILE, punto.y / this.LADO_TILE);
		final GroupTile gt = this.GRUPOS_TILES.get(puntoGroupTile);
		if (gt != null) {
			final Tile t = gt.getTileReferenciado(punto);
			if (t != null) {

				gt.establecerTileEspecifico(puntoTile, tile);
			}
		}

	}

	/**
	 * Renderiza en pantalla únicamente los grupos de tiles ({@link GroupTile})
	 * dentro de la franja visible.
	 * <p>
	 * <b>Optimización de Frustum Culling:</b> Proyecta los límites de la cámara con
	 * un margen de seguridad (padding) de 3 tiles alrededor de la pantalla para
	 * evitar pop-in visual. Alinea los índices directamente a las coordenadas
	 * discretas de la grilla de {@code GroupTile} para garantizar saltos de paso
	 * exactos ($O(N)$ en celdas visibles) sin iterar píxel por píxel.
	 * </p>
	 *
	 * @param g Contexto gráfico {@link Graphics2D} sobre el cual pintar el terreno.
	 */
	public void pintar(final Graphics2D g) {
		// 1. Calcula el área visible de la cámara sumando un padding de seguridad (3
		// tiles)
		final int minX = Constantes.CAMARA.getPosicionXInt() - Constantes.CENTROX - (3 * this.LADO_TILE);
		final int maxX = Constantes.CAMARA.getPosicionXInt() + Constantes.CENTROX + (3 * this.LADO_TILE);

		final int minY = Constantes.CAMARA.getPosicionYInt() - Constantes.CENTROY - (3 * this.LADO_TILE);
		final int maxY = Constantes.CAMARA.getPosicionYInt() + Constantes.CENTROY + (3 * this.LADO_TILE);

		// 2. Proyecta las coordenadas de mundo a la grilla discreta de GroupTiles
		// usando floorDiv
		final int inicioX = Math.floorDiv(minX, this.LADO_GRUPO_TILE) * this.LADO_GRUPO_TILE;
		final int finX = Math.floorDiv(maxX, this.LADO_GRUPO_TILE) * this.LADO_GRUPO_TILE;

		final int inicioY = Math.floorDiv(minY, this.LADO_GRUPO_TILE) * this.LADO_GRUPO_TILE;
		final int finY = Math.floorDiv(maxY, this.LADO_GRUPO_TILE) * this.LADO_GRUPO_TILE;

		GroupTile gt = null;

		// 3. Iteración alineada por bloque espacial completo (sin reevaluaciones pixel
		// por pixel)
		for (int y = inicioY; y <= finY; y += this.LADO_GRUPO_TILE) {
			for (int x = inicioX; x <= finX; x += this.LADO_GRUPO_TILE) {
				gt = this.getGrupoTileReferenciado(x, y);
				if (gt != null) {
					gt.pintar(g);
				}
			}
		}
	}

	/**
	 * Renderiza únicamente las celdas espaciales ({@link ZoneBox}) visibles en la
	 * pantalla actual.
	 * <p>
	 * <b>Optimización de Rendimiento y Memoria:</b><br>
	 * 1. Elimina la creación de instancias efímeras {@code new Point(...)} dentro
	 * del bucle de renderizado.<br>
	 * 2. Proyecta las coordenadas de la cámara directamente a los índices discretos
	 * de la grilla mediante {@link Math#floorDiv}.<br>
	 * 3. Realiza saltos exactos de paso ({@code ladoZona}) evitando iteraciones
	 * píxel por píxel.
	 * </p>
	 * 
	 * @param g        Contexto gráfico {@link Graphics2D} donde se dibujarán las
	 *                 zonas.
	 * @param zonas    Mapa de celdas espaciales activas indexadas por coordenadas
	 *                 de grilla.
	 * @param ladoZona Dimensión en píxeles del lado de cada celda espacial
	 *                 ({@code ZoneBox}).
	 */
	public void pintarZonas(final Graphics2D g, final HashMap<Point, ZoneBox> zonas, final int ladoZona) {
		// 1. Delimita la franja visible con margen de seguridad (padding de 3 tiles)
		final int minX = Constantes.CAMARA.getPosicionXInt() - Constantes.CENTROX - (3 * this.LADO_TILE);
		final int maxX = Constantes.CAMARA.getPosicionXInt() + Constantes.CENTROX + (3 * this.LADO_TILE);

		final int minY = Constantes.CAMARA.getPosicionYInt() - Constantes.CENTROY - (3 * this.LADO_TILE);
		final int maxY = Constantes.CAMARA.getPosicionYInt() + Constantes.CENTROY + (3 * this.LADO_TILE);

		// 2. Proyección exacta a índices de la grilla discreta (resiste coordenadas
		// negativas)
		final int inicioGridX = Math.floorDiv(minX, ladoZona);
		final int finGridX = Math.floorDiv(maxX, ladoZona);

		final int inicioGridY = Math.floorDiv(minY, ladoZona);
		final int finGridY = Math.floorDiv(maxY, ladoZona);

		// Clave de búsqueda reutilizable para evitar instanciación de objetos Point en
		// el Heap
		final Point claveBusqueda = new Point();
		ZoneBox zbAux = null;

		// 3. Iteración directa sobre la matriz de celdas visibles
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
	 * Ejecuta el ciclo de actualización lógica (update) únicamente en las celdas
	 * espaciales ({@link ZoneBox}) contenidas dentro del área visible de la cámara.
	 * <p>
	 * <b>Optimización de Rendimiento y Memoria:</b><br>
	 * 1. Reutiliza un objeto {@link Point} de búsqueda para eliminar la
	 * instanciación de objetos efímeros en el Heap.<br>
	 * 2. Mapea directamente los límites de la cámara a índices discretos de la
	 * grilla usando {@link Math#floorDiv}.<br>
	 * 3. Realiza saltos exactos de paso por celda ({@code ladoZona}), reduciendo la
	 * ejecución a $O(\text{Celdas Visibles})$.
	 * </p>
	 *
	 * @param zonas    Mapa de celdas espaciales activas indexadas por coordenadas
	 *                 de grilla.
	 * @param ladoZona Dimensión en píxeles del lado de cada celda espacial
	 *                 ({@code ZoneBox}).
	 */
	public void actualizarZonas(final HashMap<Point, ZoneBox> zonas, final int ladoZona) {
		// 1. Delimita el franja visible con margen de seguridad (padding de 3 tiles)
		final int minX = Constantes.CAMARA.getPosicionXInt() - Constantes.CENTROX - (3 * this.LADO_TILE);
		final int maxX = Constantes.CAMARA.getPosicionXInt() + Constantes.CENTROX + (3 * this.LADO_TILE);

		final int minY = Constantes.CAMARA.getPosicionYInt() - Constantes.CENTROY - (3 * this.LADO_TILE);
		final int maxY = Constantes.CAMARA.getPosicionYInt() + Constantes.CENTROY + (3 * this.LADO_TILE);

		// 2. Proyección exacta a índices de grilla discreta (resiste coordenadas
		// negativas)
		final int inicioGridX = Math.floorDiv(minX, ladoZona);
		final int finGridX = Math.floorDiv(maxX, ladoZona);

		final int inicioGridY = Math.floorDiv(minY, ladoZona);
		final int finGridY = Math.floorDiv(maxY, ladoZona);

		// Clave de búsqueda reutilizable para prevenir presión sobre el Garbage
		// Collector
		final Point claveBusqueda = new Point();
		ZoneBox zbAux = null;

		// 3. Iteración directa sobre la matriz de celdas visibles
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
		if ((x < 0) || (y < 0)) {
			return false;
		}
		final boolean contiene = false;
		final Point p = new Point(x / this.LADO_GRUPO_TILE, y / this.LADO_GRUPO_TILE);
		if (this.GRUPOS_TILES.get(p) != null) {
			return true;

		}
		return contiene;
	}

	public boolean contienePuntoGrupoTileReferenciado(final Point p) {
		final boolean contiene = false;
		final Point punto = new Point(p.x / this.LADO_GRUPO_TILE, p.y / this.LADO_GRUPO_TILE);
		if (this.GRUPOS_TILES.get(punto) != null) {
			return true;

		}
		return contiene;
	}

	public boolean contienePuntoTileReferenciado(final int x, final int y) {
		final boolean contiene = false;
		final Point p = new Point(x / this.LADO_GRUPO_TILE, y / this.LADO_GRUPO_TILE);
		if (this.GRUPOS_TILES.get(p) != null) {
			if (this.GRUPOS_TILES.get(p)
					.getTileReferenciado(new Point(x / this.LADO_TILE, y / this.LADO_TILE)) != null) {
				return true;
			}

		}
		// revisar codigo dudoso en este sector;
		return contiene;
	}

	public boolean contienePuntoTileReferenciado(final Point p) {
		final boolean contiene = false;
		final Point punto = new Point(p.x / this.LADO_GRUPO_TILE, p.y / this.LADO_GRUPO_TILE);
		if (this.GRUPOS_TILES.get(punto) != null) {
			if (this.GRUPOS_TILES.get(punto)
					.getTileReferenciado(new Point(p.x / this.LADO_TILE, p.y / this.LADO_TILE)) != null) {
				return true;
			}

		}

		return contiene;
	}

	/**
	 * Obtiene la lista de celdas ({@link Tile}) que colisionan o se intersectan con
	 * una forma geométrica dada.
	 * <p>
	 * <b>Optimizaciones de Rendimiento:</b><br>
	 * 1. Extrae el rectángulo delimitador una sola vez para evitar instanciaciones
	 * efímeras.<br>
	 * 2. Usa {@link Math#floorDiv} para garantizar índices de grilla precisos en
	 * coordenadas negativas.<br>
	 * 3. Inserta directamente en el {@link ArrayList} de retorno evitando la
	 * sobrecarga de un {@link HashSet} intermedio.
	 * </p>
	 *
	 * @param s Forma geométrica ({@link Shape}) a comprobar contra la grilla.
	 * @return Lista de tiles que intersectan con la forma.
	 */
	public ArrayList<Tile> getTilesIntersectados(final Shape s) {
		final ArrayList<Tile> tilesIntersectados = new ArrayList<>();
		if (s == null) {
			return tilesIntersectados;
		}

		// 1. Obtiene el AABB (Axis-Aligned Bounding Box) una sola vez
		final Rectangle bounds = s.getBounds();

		// 2. Proyección exacta a índices de grilla discreta (resiste coordenadas
		// negativas)
		final int minTileX = Math.floorDiv(bounds.x, this.LADO_TILE);
		final int maxTileX = Math.floorDiv(bounds.x + bounds.width, this.LADO_TILE);

		final int minTileY = Math.floorDiv(bounds.y, this.LADO_TILE);
		final int maxTileY = Math.floorDiv(bounds.y + bounds.height, this.LADO_TILE);

		Tile tile = null;

		// 3. Iteración directa sobre las celdas contenidas en el AABB
		for (int x = minTileX; x <= maxTileX; x++) {
			for (int y = minTileY; y <= maxTileY; y++) {

				tile = this.getTileReferenciado(x * this.LADO_TILE, y * this.LADO_TILE);

				// Comprobación de intersección precisa con la forma geométrica
				if ((tile != null) && s.intersects(tile.getArea())) {
					tilesIntersectados.add(tile);
				}
			}
		}

		return tilesIntersectados;
	}

	/**
	 * Evalúa si un área rectangular ({@link Rectangle}) intersecta con alguna celda
	 * sólida o activa de la grilla.
	 * <p>
	 * <b>Optimizaciones de Rendimiento:</b><br>
	 * 1. Cortocircuito inmediato (<i>Early Exit</i>): Retorna {@code true} en la
	 * primera colisión encontrada.<br>
	 * 2. Usa {@link Math#floorDiv} para evitar fallos de alineación en coordenadas
	 * de mapa negativas.<br>
	 * 3. Elimina llamadas pesadas a {@code Rectangle.intersects} aprovechando la
	 * naturaleza discreta de la grilla.
	 * </p>
	 *
	 * @param r Rectángulo de colisión a verificar.
	 * @return {@code true} si existe al menos una celda que colisione con el
	 *         rectángulo; {@code false} en caso contrario.
	 */
	public boolean intersecta(final Rectangle r) {
		if ((r == null) || r.isEmpty()) {
			return false;
		}

		// 1. Proyección exacta a índices de grilla discreta (resiste coordenadas
		// negativas)
		final int minTileX = Math.floorDiv(r.x, this.LADO_TILE);
		final int maxTileX = Math.floorDiv((r.x + r.width) - 1, this.LADO_TILE);

		final int minTileY = Math.floorDiv(r.y, this.LADO_TILE);
		final int maxTileY = Math.floorDiv((r.y + r.height) - 1, this.LADO_TILE);

		Tile tile = null;

		// 2. Iteración acotada con Early Exit
		for (int x = minTileX; x <= maxTileX; x++) {
			for (int y = minTileY; y <= maxTileY; y++) {

				tile = this.getTileReferenciado(x * this.LADO_TILE, y * this.LADO_TILE);

				// Si la celda existe (y/o es sólida), la intersección dentro del AABB es
				// garantizada
				if (tile != null) {
					return true;
				}
			}
		}

		return false;
	}

	public boolean intersectaSolidoDijkstra(final Shape area) {

		for (final Tile t : this.getTilesIntersectados(area)) {

			if (area.intersects(t.getArea()) && t.esSolidoDijkstra()) {
				if ((t.getArea().x == 16) && (t.getArea().y == 176)) {
					System.out.println("tile encontrado");
				}
				return true;
			}
		}

		return false;
	}

	public boolean intersectaAlgoSolido(final Shape area) {

		for (final Tile t : this.getTilesIntersectados(area)) {

			if (t.hayColisionConAlgoSolido(area)) {
				return true;
			}
		}

		return false;
	}

	public boolean intersectaTileSolido(final Shape area) {

		for (final Tile t : this.getTilesIntersectados(area)) {

			if (area.intersects(t.getArea()) && t.esSolido()) {
				return true;
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
		return !((r.x < 0) || (r.y < 0) || ((r.x + r.width) > this.ANCHO) || ((r.y + r.height) > this.ALTO));
	}

	public boolean areaEnSectorNoSolido(final Rectangle r) {
		if (!this.AreaDentroDelTerreno(r)) {
			System.out.println("se ha detectado una colocacion en area no valida. Fuera del terreno! " + r);
			return false;
		}
		if (this.getTileReferenciado(r.x, r.y).esSolidoDijkstra()) {
			System.out.println("se ha detectado una colocacion en area no valida. TileSolido " + r);
			return false;
		}
		return true;
	}

	public long getCantidadTiles() {
		return this.CANT_TILES;
	}

}
