package principal.mapa;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.HashMap;

import org.json.simple.JSONObject;

import principal.entes.modelos.tile.ListaModeloTile;
import principal.entes.modelos.tile.ModeloTile;
import principal.entes.objetos.Complemento;
import principal.entes.objetos.Objeto;
import principal.maquinaestado.estados.editor.PaletaComplento;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.Textura;

public class Tile implements Serializable{
    private static final long serialVersionUID = -445324235886L;
    protected final int LADO;
    protected final int X;
    protected final int Y;
    protected final Rectangle AREA;
    protected final int CODIGO_MODELO_TILE;
    protected final HashMap<Objeto, Boolean> OBJETOS_SOLIDADOS = new HashMap<Objeto, Boolean>();

    public Tile(final int x, final int y, final int lado, final int codigoModeloTile) {
	this.X = x;
	this.Y = y;
	this.LADO = lado;
	this.CODIGO_MODELO_TILE = codigoModeloTile;
	this.AREA = new Rectangle(x, y, lado, lado);
    }

    // VER DESPLAZAMIENTOS!DIBUJODEBUG LO HACE SOLO!
    public void pintar(final Graphics2D g) {
	if (!Constantes.TECLADO.TECLA_OCULTAR_TERRENO.presionado()) {
	    if (!ListaModeloTile.getModelo(this.CODIGO_MODELO_TILE).contieneAnimacion()) {
		DibujoDebug.dibujarImagenRefCamara(g, this.getTexturaImagen(), this.X, this.Y);
	    } else {
		ListaModeloTile.getModelo(this.CODIGO_MODELO_TILE).getAnimacion().pintar(g, this.X, this.Y, true);
	    }
	}

	if (Constantes.TECLADO.TECLA_DEBUG_TILE.presionado() && Constantes.GLOBALES.estadoJuego) {
		DibujoDebug.dibujarImagenRefCamara(g, Textura.getTextura(Textura.idTexturaContornoTile), X, Y);
		
	}

    }

    public void pintarEditor(final Graphics2D g) {
	DibujoDebug.dibujarImagenRefCamara(g, this.getTexturaImagen(), this.X, this.Y);
	if (!Constantes.GLOBALES.editorSelectGroupTile) {
		DibujoDebug.dibujarImagenRefCamara(g, Textura.getTextura(Textura.idTexturaContornoTile), X, Y);
	}
    }

    public void pintarPaleta(final Graphics2D g) {
	DibujoDebug.dibujarImagen(g, this.getTexturaImagen(), this.X, this.Y);
	DibujoDebug.dibujarImagenRefCamara(g, Textura.getTextura(Textura.idTexturaContornoTile), X, Y);
    }

    public void meterObjetoSolido(final Objeto obj) {
	if (obj.getArea().intersects(this.AREA)) {
	    if (obj instanceof Complemento) {
		final Complemento c = (Complemento) obj;
		if (c.intersecta(this.AREA)) {
		    this.OBJETOS_SOLIDADOS.put(c, true);
		}
		return;
	    }
	    if (this.AREA.intersects(obj.getArea())) {
		this.OBJETOS_SOLIDADOS.put(obj, true);
	    }
	}
    }

    public void sacarObjetoSolido(final Objeto obj) {
	if (this.OBJETOS_SOLIDADOS.containsKey(obj)) {
	    this.OBJETOS_SOLIDADOS.remove(obj);
	}
    }

    public int getCantObjetosSolidos() {
	return this.OBJETOS_SOLIDADOS.size();
    }

    public void limpiarObjetosSolidos() {
	this.OBJETOS_SOLIDADOS.clear();
    }

    public boolean contieneObjetosSolidos() {
	return this.getCantObjetosSolidos() > 0;
    }

    /*
     * REVEER LA PARTE DE LAS COLOCACIONES SEGUN POSICIONAMIENTO
     */
    public Point getPosicionSegunZonaYArea(final int codigoZonaPosicion, final Objeto obj) {
	final Point punto = new Point();
	final int ancho = obj.getAncho();
	final int alto = obj.getAlto();
	switch (codigoZonaPosicion) {
	case PaletaComplento.POSICIONAMIENTO_CENTRO:
	    if (ancho == this.LADO && alto == this.LADO) {
		punto.x = this.X;
		punto.y = this.Y;
	    } else {
		punto.x = this.X + (this.LADO / 2) - (ancho / 2);
		punto.y = this.Y + (this.LADO / 2) - (alto / 2);
	    }
	    break;
	default:
	    punto.x = this.X;
	    punto.y = this.Y;
	}
	return punto;
    }

    public boolean intersecta(final Rectangle area) {
	return area.intersects(this.AREA);
    }

    public int getEstado() {
	return ListaModeloTile.getModelo(this.CODIGO_MODELO_TILE).getEstado();
    }

    public int getCodigoTextura() {
	return ListaModeloTile.getModelo(this.CODIGO_MODELO_TILE).getCodTextura();
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

    public BufferedImage getTexturaImagen() {
	return ListaModeloTile.getModelo(this.CODIGO_MODELO_TILE).getTextura();
    }

    public Point getPosicion() {
	return new Point(this.X, this.Y);
    }

    public Point getPosicionTile() {
	return new Point(this.X / this.LADO, this.Y / this.LADO);
    }

    public boolean esSolidoDijkstra() {
	if (this.getEstado() == ModeloTile.ESTADO_OBSTACULO) {
	    return true;
	}

	return this.contieneObjetosSolidos();
    }

    public boolean esSolido() {
	return this.getEstado() == ModeloTile.ESTADO_OBSTACULO;
    }

    public void pintarContorno(final Graphics2D g, final Color color) {
	DibujoDebug.dibujarRectanguloContornoRefCamara(g, this.X, this.Y, this.LADO, this.LADO, color);
    }

    @SuppressWarnings("unchecked")
    public JSONObject exportarParaJSON() {
	final JSONObject json = new JSONObject();
	json.put("x", this.getPosicionX());
	json.put("y", this.getPosicionY());
	json.put("codModelo", this.getCodModelo());
	return json;
    }

    public static Tile crearDesdeJson(final JSONObject json) {
	final int x = Integer.parseInt(json.get("x").toString());
	final int y = Integer.parseInt(json.get("y").toString());
	final int codModelo = Integer.parseInt(json.get("codModelo").toString());
	return new Tile(x, y, Constantes.LADO_TILE, codModelo);
    }

    @Override
    public String toString() {
	return "Tile [AREA= x: " + this.AREA.x + " ,y:  " + this.AREA.y + " , W: " + this.AREA.width + " ,H: " + this.AREA.height + ", MODELO_TILE="
		+ ListaModeloTile.getModelo(this.CODIGO_MODELO_TILE) + "]";
    }

}
