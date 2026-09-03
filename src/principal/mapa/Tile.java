package principal.mapa;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import org.json.simple.JSONObject;

import principal.recursos.SetTerreno;
import principal.recursos.TipoTerreno;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

public class Tile implements Serializable {

	private static final long serialVersionUID = -445324235886L;

	protected final int LADO;
	protected final int X;
	protected final int Y;
	protected final Rectangle AREA;

	protected TipoTerreno tipoTerreno;
	protected TipoTerreno tipoFondo = null;

	protected byte mascaraBit = 0;
	protected byte variacionPropia = 0;

	public Tile(final int x, final int y, final int lado, final TipoTerreno tipoTerreno) {
		this.X = x;
		this.Y = y;
		this.LADO = lado;
		this.tipoTerreno = (tipoTerreno != null) ? tipoTerreno : TipoTerreno.TIERRA;
		this.AREA = new Rectangle(x, y, lado, lado);
	}

	private void pintarCapas(final Graphics2D g) {
		if (this.tipoFondo != null) {
			final SetTerreno setFondo = Globales.GESTOR_TEXTURAS.getSetTerreno(this.tipoFondo);
			if (setFondo != null) {
				final BufferedImage texFondo = setFondo.getSprite(this.mascaraBit, this.variacionPropia, 0);
				Render2D.dibujarImagenRefCamara(g, texFondo, this.X, this.Y);
			}
		}

		final SetTerreno set = Globales.GESTOR_TEXTURAS.getSetTerreno(this.tipoTerreno);
		if (set != null) {
			final BufferedImage texPrincipal = set.getSprite(this.mascaraBit, this.variacionPropia, 0);
			Render2D.dibujarImagenRefCamara(g, texPrincipal, this.X, this.Y);
		}
	}

	public void pintar(final Graphics2D g) {
		if (!Globales.TECLADO.TECLA_OCULTAR_TERRENO.presionado()) {
			this.pintarCapas(g);
		}

		if (Globales.TECLADO.TECLA_DEBUG_TILE.presionado() && Globales.estadoJuego) {
			Render2D.dibujarImagenRefCamara(g, Globales.GESTOR_TEXTURAS.getTexturaContornoTile(), this.X, this.Y);
		}
	}

	public void pintarEditor(final Graphics2D g) {
		this.pintarCapas(g);
		if (Globales.TECLADO.TECLA_DEBUG_TILE.presionado()) {
			Render2D.dibujarImagenRefCamara(g, Globales.GESTOR_TEXTURAS.getTexturaContornoTile(), this.X, this.Y);
		}
	}

	public void pintarPaleta(final Graphics2D g) {
		Render2D.dibujarImagen(g, this.getTexturaImagen(), this.X, this.Y);
		Render2D.dibujarImagen(g, Globales.GESTOR_TEXTURAS.getTexturaContornoTile(), this.X, this.Y);
	}

	public void pintarContorno(final Graphics2D g, final Color color) {
		Render2D.dibujarRectanguloContornoRefCamara(g, this.X, this.Y, this.LADO, this.LADO, color);
	}

	public boolean esSolido() {
		return this.tipoTerreno.isSolido();
	}

	public boolean esSolidoDijkstra() {
		return this.esSolido();
	}

	public double getAlteracionVelocidad() {
		return this.tipoTerreno.getAlteracionVelocidad();
	}

	public boolean intersecta(final Rectangle area) {
		return (area != null) && area.intersects(this.AREA);
	}

	public void setMascaraBit(final byte mascara) {
		this.mascaraBit = mascara;
	}

	public byte getMascaraBit() {
		return this.mascaraBit;
	}

	public void setVariacionPropia(final byte variacion) {
		this.variacionPropia = variacion;
	}

	public byte getVariacionPropia() {
		return this.variacionPropia;
	}

	public void setTipoFondo(final TipoTerreno tipoFondo) {
		this.tipoFondo = tipoFondo;
	}

	public TipoTerreno getTipoFondo() {
		return this.tipoFondo;
	}

	public void setTipoTerreno(final TipoTerreno tipoTerreno) {
		if (tipoTerreno != null) {
			this.tipoTerreno = tipoTerreno;
		}
	}

	public TipoTerreno getTipoTerreno() {
		return this.tipoTerreno;
	}

	public BufferedImage getTexturaImagen() {
		final SetTerreno set = Globales.GESTOR_TEXTURAS.getSetTerreno(this.tipoTerreno);
		return (set != null) ? set.getSpriteBase() : Globales.GESTOR_TEXTURAS.getTexturaError();
	}

	public Rectangle getArea() {
		return this.AREA;
	}

	public int getPosicionX() {
		return this.X;
	}

	public int getPosicionY() {
		return this.Y;
	}

	public int getLado() {
		return this.LADO;
	}

	public Point getPosicion() {
		return new Point(this.X, this.Y);
	}

	public Point getPosicionTile() {
		return new Point(Math.floorDiv(this.X, this.LADO), Math.floorDiv(this.Y, this.LADO));
	}

	@SuppressWarnings("unchecked")
	public JSONObject exportarParaJSON() {
		final JSONObject json = new JSONObject();
		json.put("x", Integer.valueOf(this.X));
		json.put("y", Integer.valueOf(this.Y));
		json.put("tipo", this.tipoTerreno.name());
		if (this.tipoFondo != null) {
			json.put("fondo", this.tipoFondo.name());
		}
		return json;
	}

	public static Tile crearDesdeJson(final JSONObject json) {
		final int x = ((Number) json.get("x")).intValue();
		final int y = ((Number) json.get("y")).intValue();

		TipoTerreno tipo = TipoTerreno.TIERRA;
		if (json.get("tipo") != null) {
			try {
				tipo = TipoTerreno.valueOf(json.get("tipo").toString());
			} catch (final Exception ignored) {
			}
		}

		final Tile tile = new Tile(x, y, Constantes.LADO_TILE, tipo);

		if (json.get("fondo") != null) {
			try {
				tile.setTipoFondo(TipoTerreno.valueOf(json.get("fondo").toString()));
			} catch (final Exception ignored) {
			}
		}

		return tile;
	}

	@Override
	public String toString() {
		return "Tile [AREA= x: " + this.AREA.x + ", y: " + this.AREA.y + ", TIPO=" + this.tipoTerreno.name() + "]";
	}
}