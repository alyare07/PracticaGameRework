package principal.entes.objetos;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;

import org.json.simple.JSONObject;

import principal.entes.modelos.complemento.ListaModeloComplemento;
import principal.entes.modelos.complemento.ModeloComplementoT1;
import principal.entes.modelos.complemento.ModeloComplementoT2;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;

public class Complemento extends Objeto{
    private static final long serialVersionUID = -2759528530038714828L;
    private final int COD_MODELO_COMPLEMENTO;

    public Complemento(final int x, final int y, final int codModeloComplemento) {
	super(x, y);
	this.COD_MODELO_COMPLEMENTO = codModeloComplemento;
    }

    public void pintarAreaInterseccion(final Graphics2D g) {
	if (ListaModeloComplemento.getModeloComplemento(this.COD_MODELO_COMPLEMENTO) instanceof ModeloComplementoT1) {
	    DibujoDebug.dibujarRectanguloContornoRefCamara(g,
		    this.getAreaInterseccionEnBaseMargen(((ModeloComplementoT1) ListaModeloComplemento.getModeloComplemento(this.COD_MODELO_COMPLEMENTO)).getMargenesInterseccion()), Color.orange);
	} else if (ListaModeloComplemento.getModeloComplemento(this.COD_MODELO_COMPLEMENTO) instanceof ModeloComplementoT2) {
	    for (final Rectangle margen : ((ModeloComplementoT2) ListaModeloComplemento.getModeloComplemento(this.COD_MODELO_COMPLEMENTO)).getMargenesInterseccion()) {
		DibujoDebug.dibujarRectanguloContornoRefCamara(g, this.getAreaInterseccionEnBaseMargen(margen), Color.orange);
	    }
	}
    }

    public int getCodigoModelo() {
	return this.COD_MODELO_COMPLEMENTO;
    }

    public boolean compararModelos(final Complemento c) {
	return c.COD_MODELO_COMPLEMENTO == this.COD_MODELO_COMPLEMENTO;
    }

    public Rectangle getAreaInterseccionEnBaseMargen(final Rectangle margen) {
	return new Rectangle(this.getPosicionXInt() + margen.x, this.getPosicionYInt() + margen.y, this.getAncho() - margen.width - margen.x, (this.getAlto() - margen.height - margen.y));
    }

    @Override
    public void pintar(final Graphics2D g) {
	if (!Constantes.TECLADO.TECLA_OCULTAR_COMPLEMENTOS.presionado()) {
	    if (ListaModeloComplemento.getModeloComplemento(this.COD_MODELO_COMPLEMENTO).animar()) {
		ListaModeloComplemento.getModeloComplemento(this.COD_MODELO_COMPLEMENTO).getAnimacion().pintar(g, this.x, this.y);
	    } else {
		DibujoDebug.dibujarImagenRefCamara(g, this.getTextura(), this.x, this.y);
	    }
	}

	if (Constantes.TECLADO.TECLA_VER_COLISIONES.presionado()) {
	    this.pintarAreaInterseccion(g);
	    DibujoDebug.dibujarRectanguloContornoRefCamara(g, this.getArea(), Color.black);
	}
    }

    public JSONObject exportarParaJSON() {
	final JSONObject json = new JSONObject();
	json.put("x", this.x);
	json.put("y", this.y);
	json.put("codModelo", this.getCodigoModelo());
	return json;
    }

    public boolean intersectaAreaNoSolida(final Shape area) {
	if (area.intersects(this.getArea()) && ListaModeloComplemento.getModeloComplemento(this.COD_MODELO_COMPLEMENTO).contieneZonaNoSolida()) {
	    return !this.intersecta(area);
	}
	return false;
    }

    public static Complemento crearDesdeJson(final JSONObject json) {
	final int x = Integer.parseInt(json.get("x").toString());
	final int y = Integer.parseInt(json.get("y").toString());
	final int codModelo = Integer.parseInt(json.get("codModelo").toString());
	return new Complemento(x, y, codModelo);
    }

    @Override
    public boolean intersecta(final Shape s) {
	return ListaModeloComplemento.getModeloComplemento(this.COD_MODELO_COMPLEMENTO).intersecta(s, this);
    }

    @Override
    public boolean esSolido() {
	return ListaModeloComplemento.getModeloComplemento(this.COD_MODELO_COMPLEMENTO).esSolido();
    }

    @Override
    public Objeto copiar() {
	return new Complemento(this.x, this.y, this.COD_MODELO_COMPLEMENTO);
    }

    @Override
    public BufferedImage getTextura() {
	return ListaModeloComplemento.getModeloComplemento(this.COD_MODELO_COMPLEMENTO).getTextura();
    }

    @Override
    public void eliminar() {
	this.eliminado = true;
    }

    @Override
    public double getPosicionX() {
	return this.x;
    }

    @Override
    public double getPosicionY() {
	return this.y;
    }

    @Override
    public void modificarPosicionX(final double desplazamientoX) {
	this.x += desplazamientoX;

    }

    @Override
    public void modificarPosicionY(final double desplazamientoY) {
	this.y += desplazamientoY;
    }

    @Override
    public boolean estaEliminado() {
	return this.eliminado;
    }

    @Override
    public int getAncho() {
	return ListaModeloComplemento.getModeloComplemento(this.COD_MODELO_COMPLEMENTO).getAncho();
    }

    @Override
    public int getAlto() {
	return ListaModeloComplemento.getModeloComplemento(this.COD_MODELO_COMPLEMENTO).getAlto();
    }

    @Override
    public Rectangle getArea() {
	return new Rectangle(this.getPosicionXInt(), this.getPosicionYInt(), this.getAncho(), this.getAlto());
    }

}
