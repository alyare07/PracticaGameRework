package principal.entes.objetos;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import principal.entes.Ente;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;

public abstract class Objeto extends Ente implements Serializable {
	private static final long serialVersionUID = -465657672324L;
	protected int x;
	protected int y;
	private final Rectangle AREA;

	public Objeto(final int x, final int y) {
		this.x = x;
		this.y = y;
		this.AREA = new Rectangle();
	}

	public Rectangle getArea() {
		 this.AREA.setBounds(x, y, getAncho(), getAlto());
		 return this.AREA;
	}

	public boolean intersecta(final Shape s) {
		return s.intersects(this.getArea());
	}

	public int getPosicionXInt() {
		return x;
	}

	public int getPosicionYInt() {
		return y;
	}
	
	public void setPosicion(final int x, final int y) {
		this.x = x;
		this.y = y;
	}

	public abstract int getAncho();

	public abstract int getAlto();

	public abstract BufferedImage getTextura();

	public void establecerPosicionX(final int x) {
		this.x = x;
	}

	public void establecerPosicionY(final int y) {
		this.y = y;
	}

	public void pintar(final Graphics2D g) {
		DibujoDebug.dibujarImagenRefCamara(g, getTextura(), this.x, this.y);
		if (Constantes.TECLADO.TECLA_VER_COLISIONES.presionado() && Constantes.GLOBALES.estadoJuego) {
			DibujoDebug.dibujarRectanguloContornoRefCamara(g, this.getArea(), Color.ORANGE);
		}
	}

	public void pintarFijo(final Graphics2D g) {
		DibujoDebug.dibujarImagen(g, getTextura(), getPosicionXInt(), getPosicionYInt());
	}
	
	public abstract boolean esSolido();

	public abstract Objeto copiar();

}
