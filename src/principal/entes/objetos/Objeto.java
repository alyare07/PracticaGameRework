package principal.entes.objetos;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import principal.entes.Ente;
import principal.utilidades.DibujoDebug;
import principal.utilidades.Globales;

public abstract class Objeto extends Ente implements Serializable {
	private static final long serialVersionUID = -465657672324L;
	protected int x;
	protected int y;

	public Objeto(final int x, final int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public Rectangle getArea() {
		return new Rectangle(this.x, this.y, this.getAncho(), this.getAlto());
	}

	public boolean intersecta(final Shape s) {
		return s.intersects(this.getArea());
	}

	@Override
	public int getPosicionXInt() {
		return this.x;
	}

	@Override
	public int getPosicionYInt() {
		return this.y;
	}

	public void setPosicion(final int x, final int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public abstract int getAncho();

	@Override
	public abstract int getAlto();

	public abstract BufferedImage getTextura();

	public void establecerPosicionX(final int x) {
		this.x = x;
	}

	public void establecerPosicionY(final int y) {
		this.y = y;
	}

	@Override
	public void pintar(final Graphics2D g) {
		DibujoDebug.dibujarImagenRefCamara(g, this.getTextura(), this.x, this.y);
		if (Globales.TECLADO.TECLA_VER_COLISIONES.presionado() && Globales.estadoJuego) {
			DibujoDebug.dibujarRectanguloContornoRefCamara(g, this.getArea(), Color.ORANGE);
		}
	}

	public void pintarFijo(final Graphics2D g) {
		DibujoDebug.dibujarImagen(g, this.getTextura(), this.getPosicionXInt(), this.getPosicionYInt());
	}

	public abstract boolean esSolido();

	public abstract Objeto copiar();

}
