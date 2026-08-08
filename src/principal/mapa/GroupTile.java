package principal.mapa;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import principal.entes.modelos.tile.ListaModeloTile;
import principal.entes.modelos.tile.ModeloTile;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.Textura;

public final class GroupTile extends Tile {
	protected final HashMap<Point, Tile> TILES = new HashMap<Point, Tile>();
	protected final int LADO_TILES;
	protected final Point PUNTO_TILE1;
	protected final Point PUNTO_TILE2;
	protected final Point PUNTO_TILE3;
	protected final Point PUNTO_TILE4;

	public GroupTile(int x, int y, int lado) {
		super(x, y, lado, 0);
		this.LADO_TILES = lado / 2;
		this.PUNTO_TILE1 = new Point(this.X / LADO_TILES, this.Y / LADO_TILES);
		this.PUNTO_TILE2 = new Point((this.X + this.LADO_TILES) / LADO_TILES, this.Y / LADO_TILES);
		this.PUNTO_TILE3 = new Point(this.X / LADO_TILES, (this.Y + this.LADO_TILES) / LADO_TILES);
		this.PUNTO_TILE4 = new Point((this.X + this.LADO_TILES) / LADO_TILES, (this.Y + this.LADO_TILES) / LADO_TILES);
		llenarTilesVacio(ListaModeloTile.COD_TIERRA);

	}

	public GroupTile(int x, int y, int lado, final int idModeloTile) {
		super(x, y, lado, 0);
		this.LADO_TILES = lado / 2;
		this.PUNTO_TILE1 = new Point(this.X / LADO_TILES, this.Y / LADO_TILES);
		this.PUNTO_TILE2 = new Point((this.X + this.LADO_TILES) / LADO_TILES, this.Y / LADO_TILES);
		this.PUNTO_TILE3 = new Point(this.X / LADO_TILES, (this.Y + this.LADO_TILES) / LADO_TILES);
		this.PUNTO_TILE4 = new Point((this.X + this.LADO_TILES) / LADO_TILES, (this.Y + this.LADO_TILES) / LADO_TILES);
		llenarTilesVacio(idModeloTile);

	}
	
	public GroupTile(int x, int y, int lado, final Tile t1, final Tile t2, final Tile t3, final Tile t4) {
		super(x, y, lado, 0);
		this.LADO_TILES = lado / 2;
		this.PUNTO_TILE1 = new Point(this.X / LADO_TILES, this.Y / LADO_TILES);
		this.PUNTO_TILE2 = new Point((this.X + this.LADO_TILES) / LADO_TILES, this.Y / LADO_TILES);
		this.PUNTO_TILE3 = new Point(this.X / LADO_TILES, (this.Y + this.LADO_TILES) / LADO_TILES);
		this.PUNTO_TILE4 = new Point((this.X + this.LADO_TILES) / LADO_TILES, (this.Y + this.LADO_TILES) / LADO_TILES);
		this.meterTiles(t1, t2, t3, t4);

	}

	private static final long serialVersionUID = 1213432123L;

	@Override
	public int getCodigoTextura() {
		return 0;
	}
	
	public void meterTiles(final Tile t1, final Tile t2, final Tile t3, final Tile t4) {
		this.TILES.clear();
		if(t1.getPosicionTile().equals(this.PUNTO_TILE1)) {
			this.TILES.put(PUNTO_TILE1, t1);
		}else if(t1.getPosicionTile().equals(this.PUNTO_TILE2)) {
			this.TILES.put(PUNTO_TILE2, t1);
		}else if(t1.getPosicionTile().equals(this.PUNTO_TILE3)) {
			this.TILES.put(PUNTO_TILE3, t1);
		}else if(t1.getPosicionTile().equals(this.PUNTO_TILE4)) {
			this.TILES.put(PUNTO_TILE4, t1);
		}
		
		if(t2.getPosicionTile().equals(this.PUNTO_TILE1)) {
			this.TILES.put(PUNTO_TILE1, t2);
		}else if(t2.getPosicionTile().equals(this.PUNTO_TILE2)) {
			this.TILES.put(PUNTO_TILE2, t2);
		}else if(t2.getPosicionTile().equals(this.PUNTO_TILE3)) {
			this.TILES.put(PUNTO_TILE3, t2);
		}else if(t2.getPosicionTile().equals(this.PUNTO_TILE4)) {
			this.TILES.put(PUNTO_TILE4, t2);
		}
		
		if(t3.getPosicionTile().equals(this.PUNTO_TILE1)) {
			this.TILES.put(PUNTO_TILE1, t3);
		}else if(t3.getPosicionTile().equals(this.PUNTO_TILE2)) {
			this.TILES.put(PUNTO_TILE2, t3);
		}else if(t3.getPosicionTile().equals(this.PUNTO_TILE3)) {
			this.TILES.put(PUNTO_TILE3, t3);
		}else if(t3.getPosicionTile().equals(this.PUNTO_TILE4)) {
			this.TILES.put(PUNTO_TILE4, t3);
		}
		
		if(t4.getPosicionTile().equals(this.PUNTO_TILE1)) {
			this.TILES.put(PUNTO_TILE1, t4);
		}else if(t4.getPosicionTile().equals(this.PUNTO_TILE2)) {
			this.TILES.put(PUNTO_TILE2, t4);
		}else if(t4.getPosicionTile().equals(this.PUNTO_TILE3)) {
			this.TILES.put(PUNTO_TILE3, t4);
		}else if(t4.getPosicionTile().equals(this.PUNTO_TILE4)) {
			this.TILES.put(PUNTO_TILE4, t4);
		}
	}

	public void llenarTilesVacio(final int codModeloTile) {
		this.TILES.clear();
		this.TILES.put(this.PUNTO_TILE1, new Tile(this.X, this.Y, this.LADO_TILES, codModeloTile));

		this.TILES.put(this.PUNTO_TILE2, new Tile(this.X + this.LADO_TILES, this.Y, this.LADO_TILES, codModeloTile));

		this.TILES.put(this.PUNTO_TILE3, new Tile(this.X, this.Y + this.LADO_TILES, this.LADO_TILES, codModeloTile));

		this.TILES.put(this.PUNTO_TILE4, new Tile(this.X + this.LADO_TILES, this.Y + this.LADO_TILES, this.LADO_TILES, codModeloTile));

	}

	
	
	@Override
	public void pintar(Graphics2D g) {
		for (Tile tile : this.TILES.values()) {
			tile.pintar(g);
		}
		if (Constantes.TECLADO.TECLA_DEBUG_GROUP_TILE.presionado() && Constantes.GLOBALES.estadoJuego) {
			DibujoDebug.dibujarImagenRefCamara(g, Textura.getTextura(Textura.idTexturaContornoGroupTile), this.X, this.Y);
		}
	}

	@Override
	public void pintarEditor(Graphics2D g) {

		for (Tile tile : this.TILES.values()) {
			tile.pintarEditor(g);
		}
		if(Constantes.GLOBALES.editorSelectGroupTile) {
			DibujoDebug.dibujarImagenRefCamara(g, Textura.getTextura(Textura.idTexturaContornoGroupTile), this.X, this.Y);
		}
	}


	public boolean establecerTileEspecifico(final Point puntoTile, Tile tile) {
		boolean establecido = false;
		Tile tileViejo = this.TILES.get(puntoTile);
		if (tileViejo != null) {
			final Tile nuevo = new Tile(tileViejo.X, tileViejo.Y, this.LADO_TILES, tile.CODIGO_MODELO_TILE);
//			nuevo.agregarObjetoZonaCentro(new Objeto(0, 0, 16, Textura.IMAGEN_ANILLO_ORO));
			this.TILES.put(puntoTile, nuevo);
		}
		return establecido;
	}

	@Override
	public boolean intersecta(final Rectangle area) {
		if (super.intersecta(area)) {
			
			for (Tile tile : this.TILES.values()) {
				if (tile.getEstado() == ModeloTile.ESTADO_OBSTACULO && tile.intersecta(area)) {
					return true;
				} 
			}
		}

		return false;
	}

	public Tile getTile1() {
		return this.TILES.get(this.PUNTO_TILE1);
	}

	public Tile getTile2() {
		return this.TILES.get(this.PUNTO_TILE2);
	}

	public Tile getTile3() {
		return this.TILES.get(this.PUNTO_TILE3);
	}

	public Tile getTile4() {
		return this.TILES.get(this.PUNTO_TILE4);
	}
	
	public Collection<Tile> getTiles(){
		return this.TILES.values();
	}

	public Tile getTileReferenciado(final Point p) {
		return this.TILES.get(new Point(p.x / this.LADO_TILES, p.y / this.LADO_TILES));
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject exportarParaJSON() {
		JSONObject json = new JSONObject();
		json.put("x", this.getPosicionX());
		json.put("y", this.getPosicionY());
		
		JSONArray tiles = new JSONArray();
		for(Tile t : this.TILES.values()) {
			tiles.add(t.exportarParaJSON());
		}
		json.put("Tiles", tiles);
		return json;
	}

	
	public static GroupTile crearDesdeJson(final JSONObject json) {
		int x = Integer.parseInt(json.get("x").toString());
		int y = Integer.parseInt(json.get("y").toString());
		JSONParser parse = new JSONParser();
		JSONArray tiles = null;
		try {
			tiles = (JSONArray) parse.parse(json.get("Tiles").toString());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Tile t1, t2, t3, t4 = null;

		Object[] lista = tiles.toArray();
		t1 = Tile.crearDesdeJson((JSONObject)lista[0]);
		t2 = Tile.crearDesdeJson((JSONObject)lista[1]);
		t3 = Tile.crearDesdeJson((JSONObject)lista[2]);
		t4 = Tile.crearDesdeJson((JSONObject)lista[3]);
		
		return new GroupTile(x, y, Constantes.LADO_TILE*2,t1,t2,t3,t4);
	}

	@Override
	public String toString() {
		return "GroupTile [AREA= x: " + AREA.x + " ,y:  " + AREA.y + " , W: " + AREA.width + " ,H: " + AREA.height + "]";
	}

}
