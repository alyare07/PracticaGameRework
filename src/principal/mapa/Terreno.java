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

	public void pintar(final Graphics2D g) {

		final int puntoX = Constantes.CAMARA.getPosicionXInt() - Constantes.CENTROX - (3 * this.LADO_TILE);
		final int limiteX = Constantes.CAMARA.getPosicionXInt() + Constantes.CENTROX + (3 * this.LADO_TILE);

		final int puntoY = Constantes.CAMARA.getPosicionYInt() - Constantes.CENTROY - (3 * this.LADO_TILE);
		final int limiteY = Constantes.CAMARA.getPosicionYInt() + Constantes.CENTROY + (3 * this.LADO_TILE);
		boolean contieneEnY = false;
		GroupTile gt = null;
		int px = puntoX;
		int py = puntoY;
		if ((puntoX < 0) && (limiteX > 0)) {
			px = 0;
		}
		if ((puntoY < 0) && (limiteY > 0)) {
			py = 0;
		}

		for (int y = py; y < limiteY;) {

			for (int x = px; x < limiteX;) {

				if ((gt = this.getGrupoTileReferenciado(x, y)) != null) {
					gt.pintar(g);
					gt = null;
					x += this.LADO_GRUPO_TILE;
					if (!contieneEnY) {
						contieneEnY = true;
					}

				} else {
					x++;
				}
			}
			if (contieneEnY) {
				y += this.LADO_GRUPO_TILE;
			} else {
				y++;
			}
		}

	}

	public void pintarZonas(final Graphics2D g, final HashMap<Point, ZoneBox> zonas, final int ladoZona) {

		final int puntoX = Constantes.CAMARA.getPosicionXInt() - Constantes.CENTROX - (3 * this.LADO_TILE);
		final int limiteX = Constantes.CAMARA.getPosicionXInt() + Constantes.CENTROX + (3 * this.LADO_TILE);

		final int puntoY = Constantes.CAMARA.getPosicionYInt() - Constantes.CENTROY - (3 * this.LADO_TILE);
		final int limiteY = Constantes.CAMARA.getPosicionYInt() + Constantes.CENTROY + (3 * this.LADO_TILE);
		final boolean contieneEnY = false;
		ZoneBox zbAux = null;
		int px = puntoX;
		int py = puntoY;
		if ((puntoX < 0) && (limiteX > 0)) {
			px = 0;
		}
		if ((puntoY < 0) && (limiteY > 0)) {
			py = 0;
		}

		for (int y = py; y < limiteY;) {

			for (int x = px; x < limiteX;) {

				zbAux = zonas.get(new Point(x / ladoZona, y / ladoZona));
				if (zbAux != null) {
					zbAux.pintar(g);
//					zbAux.pintar(g);
					x += ladoZona;
				} else {
					x++;
				}
			}
			if (contieneEnY) {
				y += ladoZona;
			} else {
				y++;
			}
		}
	}

	public void actualizarZonas(final HashMap<Point, ZoneBox> zonas, final int ladoZona) {

		final int puntoX = Constantes.CAMARA.getPosicionXInt() - Constantes.CENTROX - (3 * this.LADO_TILE);
		final int limiteX = Constantes.CAMARA.getPosicionXInt() + Constantes.CENTROX + (3 * this.LADO_TILE);

		final int puntoY = Constantes.CAMARA.getPosicionYInt() - Constantes.CENTROY - (3 * this.LADO_TILE);
		final int limiteY = Constantes.CAMARA.getPosicionYInt() + Constantes.CENTROY + (3 * this.LADO_TILE);
		final boolean contieneEnY = false;
		ZoneBox zbAux = null;
		int px = puntoX;
		int py = puntoY;
		if ((puntoX < 0) && (limiteX > 0)) {
			px = 0;
		}
		if ((puntoY < 0) && (limiteY > 0)) {
			py = 0;
		}

		for (int y = py; y < limiteY;) {

			for (int x = px; x < limiteX;) {

				zbAux = zonas.get(new Point(x / ladoZona, y / ladoZona));
				if (zbAux != null) {
					zbAux.actualizar();
//					zbAux.pintar(g);
					x += ladoZona;
				} else {
					x++;
				}
			}
			if (contieneEnY) {
				y += ladoZona;
			} else {
				y++;
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

	public ArrayList<Tile> getTilesIntersectados(final Shape s) {
		final int x = s.getBounds().x;
		final int y = s.getBounds().y;
		final int w = s.getBounds().width;
		final int h = s.getBounds().height;
		final HashSet<Tile> lista = new HashSet<Tile>();
		Tile tile = null;
		final int xTile = x / this.LADO_TILE;
		final int limiteXTile = (x + w) / this.LADO_TILE;
		final int yTile = y / this.LADO_TILE;
		final int limiteYTile = (y + h) / this.LADO_TILE;

		for (int x2 = xTile; x2 <= limiteXTile; x2++) {
			for (int y2 = yTile; y2 <= limiteYTile; y2++) {
				tile = this.getTileReferenciado(x2 * this.LADO_TILE, y2 * this.LADO_TILE);
				if ((tile != null) && s.intersects(tile.getArea())) {
					lista.add(tile);
				}
			}
		}
		return new ArrayList<Tile>(lista);
	}

	public boolean intersecta(final Rectangle r) {

		final int x = r.x;
		final int y = r.y;
		final int w = r.width;
		final int h = r.height;
		Tile tile = null;
		final int xTile = x / this.LADO_TILE;
		final int limiteXTile = (x + w) / this.LADO_TILE;
		final int yTile = y / this.LADO_TILE;
		final int limiteYTile = (y + h) / this.LADO_TILE;

		for (int x2 = xTile; x2 <= limiteXTile; x2++) {
			for (int y2 = yTile; y2 <= limiteYTile; y2++) {
				tile = this.getTileReferenciado(x2 * this.LADO_TILE, y2 * this.LADO_TILE);
				if ((tile != null) && r.intersects(tile.getArea())) {
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

	public boolean intersectaSolido(final Shape area) {

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
