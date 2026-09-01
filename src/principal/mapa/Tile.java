package principal.mapa;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import org.json.simple.JSONObject;

import principal.entes.modelos.tile.ListaModeloTile;
import principal.entes.modelos.tile.ModeloTile;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.Textura;

public class Tile implements Serializable {

	private static final long serialVersionUID = -445324235886L;

	protected final int LADO;
	protected final int X;
	protected final int Y;
	protected final Rectangle AREA;
	protected final int CODIGO_MODELO_TILE;

	protected int codigoModeloFondo = 0;
	protected byte mascaraBit = 0;
	protected byte variacionPropia = 0;

	public Tile(final int x, final int y, final int lado, final int codigoModeloTile) {
		this.X = x;
		this.Y = y;
		this.LADO = lado;
		this.CODIGO_MODELO_TILE = codigoModeloTile;
		this.AREA = new Rectangle(x, y, lado, lado);
	}

	private void pintarCapas(final Graphics2D g) {
		if (this.codigoModeloFondo != 0) {
			final ModeloTile modeloFondo = ListaModeloTile.getModelo(this.codigoModeloFondo);
			if (modeloFondo != null) {
				final int texFondo = modeloFondo.getCodTextura(this.mascaraBit, this.variacionPropia);
				Render2D.dibujarImagenRefCamara(g, Textura.getTextura(texFondo), this.X, this.Y);
			}
		}

		final ModeloTile modelo = ListaModeloTile.getModelo(this.CODIGO_MODELO_TILE);
		if (modelo != null) {
			final int texturaFinal = modelo.getCodTextura(this.mascaraBit, this.variacionPropia);
			Render2D.dibujarImagenRefCamara(g, Textura.getTextura(texturaFinal), this.X, this.Y);
		}
	}

	public void pintar(final Graphics2D g) {
		if (!Globales.TECLADO.TECLA_OCULTAR_TERRENO.presionado()) {
			this.pintarCapas(g);
		}

		if (Globales.TECLADO.TECLA_DEBUG_TILE.presionado() && Globales.estadoJuego) {
			Render2D.dibujarImagenRefCamara(g, Textura.getTextura(Textura.idTexturaContornoTile), this.X, this.Y);
		}
	}

	public void pintarEditor(final Graphics2D g) {
		this.pintarCapas(g);
		if (Globales.TECLADO.TECLA_DEBUG_TILE.presionado()) {
			Render2D.dibujarImagenRefCamara(g, Textura.getTextura(Textura.idTexturaContornoTile), this.X, this.Y);
		}
	}

	public void pintarPaleta(final Graphics2D g) {
		Render2D.dibujarImagen(g, this.getTexturaImagen(), this.X, this.Y);
		Render2D.dibujarImagen(g, Textura.getTextura(Textura.idTexturaContornoTile), this.X, this.Y);
	}

	public void pintarContorno(final Graphics2D g, final Color color) {
		Render2D.dibujarRectanguloContornoRefCamara(g, this.X, this.Y, this.LADO, this.LADO, color);
	}

	public boolean esSolido() {
		return this.getEstado() == ModeloTile.ESTADO_OBSTACULO;
	}

	public boolean esSolidoDijkstra() {
		return this.esSolido();
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

	public void setCodigoModeloFondo(final int codigoFondo) {
		this.codigoModeloFondo = codigoFondo;
	}

	public int getCodigoModeloFondo() {
		return this.codigoModeloFondo;
	}

	public BufferedImage getTexturaImagen() {
		final ModeloTile m = ListaModeloTile.getModelo(this.CODIGO_MODELO_TILE);
		return (m != null) ? Textura.getTextura(m.getCodTextura(this.mascaraBit, this.variacionPropia)) : null;
	}

	public int getEstado() {
		final ModeloTile m = ListaModeloTile.getModelo(this.CODIGO_MODELO_TILE);
		return (m != null) ? m.getEstado() : 0;
	}

	public int getCodigoTextura() {
		final ModeloTile m = ListaModeloTile.getModelo(this.CODIGO_MODELO_TILE);
		return (m != null) ? m.getCodTextura() : 0;
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

	public int getCodModelo() {
		return this.CODIGO_MODELO_TILE;
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
		json.put("codModelo", Integer.valueOf(this.CODIGO_MODELO_TILE));
		return json;
	}

	public static Tile crearDesdeJson(final JSONObject json) {
		final int x = ((Number) json.get("x")).intValue();
		final int y = ((Number) json.get("y")).intValue();
		final int codModelo = ((Number) json.get("codModelo")).intValue();
		return new Tile(x, y, Constantes.LADO_TILE, codModelo);
	}

	@Override
	public String toString() {
		return "Tile [AREA= x: " + this.AREA.x + ", y: " + this.AREA.y + ", COD=" + this.CODIGO_MODELO_TILE + "]";
	}
}