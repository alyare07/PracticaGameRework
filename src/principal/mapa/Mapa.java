package principal.mapa;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import principal.entes.modelos.tile.ListaModeloTile;
import principal.mapa.renderEntidades.ZoneBox;
import principal.utilidades.Constantes;

public class Mapa implements Serializable {
	private static final long serialVersionUID = -230565732234345L;
	protected final int ANCHO;
	protected final int ALTO;
	protected final int CANTIDAD_ANCHO_GROUPTILE;
	protected final int CANTIDAD_ALTO_GROUPTILE;
	protected final int LADO_GRUPO_TILE;
	protected final int LADO_TILE;
	protected final long CANT_TILES;
	protected final HashMap<Point, GroupTile> GRUPOS_TILES = new HashMap<Point, GroupTile>();
	public Mapa(final int cantTilesAncho, final int cantTilesAlto, final int ladoTile) {
		Constantes.GLOBALES.ladoTile = ladoTile;
		this.LADO_GRUPO_TILE = ladoTile * 2;
		this.LADO_TILE = ladoTile;
		this.CANTIDAD_ANCHO_GROUPTILE = cantTilesAncho / 2;// Esto solo nos limitaria a cantidad en tiles pares
		this.CANTIDAD_ALTO_GROUPTILE = cantTilesAlto / 2;
		this.ANCHO = ladoTile * cantTilesAncho;
		this.ALTO = ladoTile * cantTilesAlto;
		this.CANT_TILES = (this.GRUPOS_TILES.size() * ((LADO_GRUPO_TILE / LADO_TILE) * (LADO_GRUPO_TILE / LADO_TILE)));
		llenarVacioMapa(ListaModeloTile.COD_TIERRA);
	}

	public Mapa(final int cantTilesAncho, final int cantTilesAlto, final int ladoTile, final int idModeloTile) {
		Constantes.GLOBALES.ladoTile = ladoTile;
		this.LADO_GRUPO_TILE = ladoTile * 2;
		this.LADO_TILE = ladoTile;
		this.CANTIDAD_ANCHO_GROUPTILE = cantTilesAncho / 2;// Esto solo nos limitaria a cantidad en tiles pares
		this.CANTIDAD_ALTO_GROUPTILE = cantTilesAlto / 2;
		this.ANCHO = ladoTile * cantTilesAncho;
		this.ALTO = ladoTile * cantTilesAlto;
		this.CANT_TILES = (this.GRUPOS_TILES.size() * ((LADO_GRUPO_TILE / LADO_TILE) * (LADO_GRUPO_TILE / LADO_TILE)));
		llenarVacioMapa(idModeloTile);
	}
	
	
	public Mapa(final JSONObject jso) {
		this.CANTIDAD_ANCHO_GROUPTILE = Integer.parseInt(jso.get("cantGTancho").toString());
		this.CANTIDAD_ALTO_GROUPTILE = Integer.parseInt(jso.get("cantGTalto").toString());
//		this.LADO_TILE = Integer.parseInt(jso.get("ladoTile").toString());
		this.LADO_TILE = Constantes.LADO_TILE;
		this.LADO_GRUPO_TILE = this.LADO_TILE*2;
		this.ANCHO = Integer.parseInt(jso.get("ancho").toString());
		this.ALTO = Integer.parseInt(jso.get("alto").toString());
		this.CANT_TILES = Integer.parseInt(jso.get("cantTile").toString());
		
		JSONArray listaGT = null;
		try {
			listaGT = (JSONArray)new JSONParser().parse(jso.get("GT").toString());
			GroupTile gt = null;
			for(Object o : listaGT) {
				if(o instanceof JSONObject) {
					gt = GroupTile.crearDesdeJson((JSONObject)o);
					this.GRUPOS_TILES.put(new Point(gt.getPosicionX()/LADO_GRUPO_TILE, gt.getPosicionY()/LADO_GRUPO_TILE), gt);
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	@SuppressWarnings("unchecked")
	public JSONObject getTilesJson() {
		JSONObject terreno = new JSONObject();
		terreno.put("cantGTancho", this.CANTIDAD_ANCHO_GROUPTILE);
		terreno.put("cantGTalto", this.CANTIDAD_ALTO_GROUPTILE);
		terreno.put("cantTile", this.CANT_TILES);
		terreno.put("ladoTile", this.ladoTile());
		terreno.put("ancho", this.ANCHO);
		terreno.put("alto", this.ALTO);
		
		JSONArray gtLista = new JSONArray();
		for(GroupTile gt : this.GRUPOS_TILES.values()) {
			gtLista.add(gt.exportarParaJSON());
		}
		terreno.put("GT", gtLista);
		return terreno;
	}
	

	public HashMap<Point, GroupTile> getGroupTILES() {
		return GRUPOS_TILES;
	}

	public ArrayList<Tile> getTILES() {
		final ArrayList<Tile> lista = new ArrayList<Tile>();
		for (GroupTile gt : this.GRUPOS_TILES.values()) {
			for (Tile t : gt.TILES.values()) {
				lista.add(t);
			}
		}
		return lista;
	}

	public GroupTile getGrupoTileReferenciado(final Point punto) {
		return GRUPOS_TILES.get(new Point(punto.x / this.LADO_GRUPO_TILE, punto.y / this.LADO_GRUPO_TILE));
	}

	public GroupTile getGrupoTileReferenciado(final int x, final int y) {
		if (x < 0 || y < 0) {
			return null;
		}
		return GRUPOS_TILES.get(new Point(x / this.LADO_GRUPO_TILE, y / this.LADO_GRUPO_TILE));
	}

	public Tile getTileReferenciado(final int x, final int y) {

		Tile t = null;
		if (contienePuntoGrupoTileReferenciado(x, y)) {
			t = getGrupoTileReferenciado(x, y).getTileReferenciado(new Point(x, y));
		}

		return t;
	}

	public Tile getTileReferenciado(final Point p) {
		Tile t = null;
		if (contienePuntoGrupoTileReferenciado(p.x, p.y)) {
			t = getGrupoTileReferenciado(p).getTileReferenciado(p);
		}
		return t;
	}

	public void llenarVacioMapa(final int idModeloTile) {
		GRUPOS_TILES.clear();
		for (int y = 0; y < CANTIDAD_ALTO_GROUPTILE; y++) {
			for (int x = 0; x < CANTIDAD_ANCHO_GROUPTILE; x++) {
				GRUPOS_TILES.put(new Point(x, y), new GroupTile(x * this.LADO_GRUPO_TILE, y * this.LADO_GRUPO_TILE, LADO_GRUPO_TILE, idModeloTile));
			}
		}
	}

	public void establecerTileReferenciado(final Point punto, Tile tile) {
		final Point puntoGroupTile = new Point(punto.x / this.LADO_GRUPO_TILE, punto.y / this.LADO_GRUPO_TILE);
		final Point puntoTile = new Point(punto.x / LADO_TILE, punto.y / LADO_TILE);
		final GroupTile gt = this.GRUPOS_TILES.get(puntoGroupTile);
		if (gt != null) {
			final Tile t = gt.getTileReferenciado(punto);
			if (t != null) {

				gt.establecerTileEspecifico(puntoTile, tile);
			}
		}

	}

	
	
	public void pintar(Graphics2D g) {

		final int puntoX = Constantes.CAMARA.getPosicionXInt() - Constantes.CENTROX - (3 * LADO_TILE);
		final int limiteX = Constantes.CAMARA.getPosicionXInt() + Constantes.CENTROX + (3 * LADO_TILE);

		final int puntoY = Constantes.CAMARA.getPosicionYInt() - Constantes.CENTROY - (3 * LADO_TILE);
		final int limiteY = Constantes.CAMARA.getPosicionYInt() + Constantes.CENTROY + (3 * LADO_TILE);
		boolean contieneEnY = false;
		GroupTile gt = null;
		int px = puntoX;
		int py = puntoY;
		if (puntoX < 0 && limiteX > 0) {
			px = 0;
		}
		if (puntoY < 0 && limiteY > 0) {
			py = 0;
		}

		for (int y = py; y < limiteY;) {

			for (int x = px; x < limiteX;) {

				if ((gt = this.getGrupoTileReferenciado(x, y)) != null) {
					gt.pintar(g);
					gt = null;
					x += LADO_GRUPO_TILE;
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
	
	public void pintarZonas(Graphics2D g,final HashMap<Point, ZoneBox> zonas, final int ladoZona) {

		final int puntoX = Constantes.CAMARA.getPosicionXInt() - Constantes.CENTROX - (3 * LADO_TILE);
		final int limiteX = Constantes.CAMARA.getPosicionXInt() + Constantes.CENTROX + (3 * LADO_TILE);

		final int puntoY = Constantes.CAMARA.getPosicionYInt() - Constantes.CENTROY - (3 * LADO_TILE);
		final int limiteY = Constantes.CAMARA.getPosicionYInt() + Constantes.CENTROY + (3 * LADO_TILE);
		boolean contieneEnY = false;
		ZoneBox zbAux = null;
		int px = puntoX;
		int py = puntoY;
		if (puntoX < 0 && limiteX > 0) {
			px = 0;
		}
		if (puntoY < 0 && limiteY > 0) {
			py = 0;
		}

		for (int y = py; y < limiteY;) {

			for (int x = px; x < limiteX;) {
				
				zbAux = zonas.get(new Point(x/ladoZona, y/ladoZona));
				if (zbAux != null) {
					zbAux.pintar(g);
//					zbAux.pintar(g);
					x += ladoZona;
				}else {
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

		final int puntoX = Constantes.CAMARA.getPosicionXInt() - Constantes.CENTROX - (3 * LADO_TILE);
		final int limiteX = Constantes.CAMARA.getPosicionXInt() + Constantes.CENTROX + (3 * LADO_TILE);

		final int puntoY = Constantes.CAMARA.getPosicionYInt() - Constantes.CENTROY - (3 * LADO_TILE);
		final int limiteY = Constantes.CAMARA.getPosicionYInt() + Constantes.CENTROY + (3 * LADO_TILE);
		boolean contieneEnY = false;
		ZoneBox zbAux = null;
		int px = puntoX;
		int py = puntoY;
		if (puntoX < 0 && limiteX > 0) {
			px = 0;
		}
		if (puntoY < 0 && limiteY > 0) {
			py = 0;
		}

		for (int y = py; y < limiteY;) {

			for (int x = px; x < limiteX;) {
				
				
				zbAux = zonas.get(new Point(x/ladoZona, y/ladoZona));
				if (zbAux != null) {
					zbAux.actualizar();
//					zbAux.pintar(g);
					x += ladoZona;
				}else {
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
		if (x < 0 || y < 0) {
			return false;
		}
		boolean contiene = false;
		final Point p = new Point(x / LADO_GRUPO_TILE, y / LADO_GRUPO_TILE);
		if (this.GRUPOS_TILES.get(p) != null) {
			return true;

		}
		return contiene;
	}

	public boolean contienePuntoGrupoTileReferenciado(final Point p) {
		boolean contiene = false;
		final Point punto = new Point(p.x / LADO_GRUPO_TILE, p.y / LADO_GRUPO_TILE);
		if (this.GRUPOS_TILES.get(punto) != null) {
			return true;

		}
		return contiene;
	}

	public boolean contienePuntoTileReferenciado(final int x, final int y) {
		boolean contiene = false;
		final Point p = new Point(x / LADO_GRUPO_TILE, y / LADO_GRUPO_TILE);
		if (this.GRUPOS_TILES.get(p) != null) {
			if (this.GRUPOS_TILES.get(p).getTileReferenciado(new Point(x / LADO_TILE, y / LADO_TILE)) != null) {
				return true;
			}

		}
		// revisar codigo dudoso en este sector;
		return contiene;
	}

	public boolean contienePuntoTileReferenciado(final Point p) {
		boolean contiene = false;
		final Point punto = new Point(p.x / LADO_GRUPO_TILE, p.y / LADO_GRUPO_TILE);
		if (this.GRUPOS_TILES.get(punto) != null) {
			if (this.GRUPOS_TILES.get(punto).getTileReferenciado(new Point(p.x / LADO_TILE, p.y / LADO_TILE)) != null) {
				return true;
			}

		}

		return contiene;
	}

	public boolean intersecta(final Rectangle r) {
		boolean intersecta = false;
		int x = r.x;
		int y = r.y;

		// Verificamos interseccion punto extremo superior izquierdo
		if (contienePuntoGrupoTileReferenciado(x, y)) {

			intersecta = getGrupoTileReferenciado(x, y).intersecta(r);
			if (intersecta) {
				return true;
			}
		}
		// si lo anterior es falso, Verificamos interseccion punto extremo inferior
		// izquierdo
		y += r.height;
		if (contienePuntoGrupoTileReferenciado(x, y)) {
			intersecta = getGrupoTileReferenciado(x, y).intersecta(r);
			if (intersecta) {
				return true;
			}
		}
		// si lo anterior es falso, Verificamos interseccion punto extremo superior
		// derecho
		y = r.y;
		x = r.x + r.width;
		if (contienePuntoGrupoTileReferenciado(x, y)) {
			intersecta = getGrupoTileReferenciado(x, y).intersecta(r);
			if (intersecta) {
				return true;
			}
		}
		// si lo anterior es falso, Verificamos interseccion punto extremo inferior
		// derecho
		y += r.height;
		if (contienePuntoGrupoTileReferenciado(x, y)) {
			intersecta = getGrupoTileReferenciado(x, y).intersecta(r);
			if (intersecta) {
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

	
	public boolean AreaDentroDelMapa(final Rectangle r) {
		if(r.x< 0 || r.y < 0 || (r.x + r.width) > ANCHO || (r.y + r.height) > ALTO) {
			return false;
		}else {
			return true;
		}
	}
	
	public boolean areaEnSectorNoSolido(final Rectangle r) {
		if(!this.AreaDentroDelMapa(r)) {
			System.out.println("se ha detectado una colocacion en area no valida. Fuera del mapa! "+ r);
			return false;
		}else if(getTileReferenciado(r.x, r.y).esSolidoDisktra()) {
			System.out.println("se ha detectado una colocacion en area no valida. TileSolido " + r);
			return false;
		}else {
			return true;
		}
	}
	
	public long getCantidadTiles() {
		return this.CANT_TILES;
	}
	
	

	

}
