package principal.entes.estructuras;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import principal.entes.Ente;

public class Estructura extends Ente {
	protected final int ANCHO;
	protected final int ALTO;
	protected final int X;
	protected final int Y;

	public Estructura(final int x, final int y, int ancho, int alto) {
		ANCHO = ancho;
		ALTO = alto;
		this.X = x;
		this.Y = y;
	}

	public void pintar(Graphics2D g) {
	}

	public void actualizar() {

	}

	public int getAncho() {
		return ANCHO;
	}

	public int getAlto() {
		return ALTO;
	}

	@Override
	public void eliminar() {
		this.eliminado = true;
	}

	@Override
	public int getPosicionXInt() {
		return this.X;
	}

	@Override
	public int getPosicionYInt() {
		return this.Y;
	}

	@Override
	public double getPosicionX() {
		return (double) this.X;
	}

	@Override
	public double getPosicionY() {
		return (double) this.Y;
	}

	@Override
	public void modificarPosicionX(final double desplazamientoX) {

	}

	@Override
	public void modificarPosicionY(final double desplazamientoY) {

	}

	@Override
	public boolean estaEliminado() {
		return this.eliminado;
	}
	
	
	public Rectangle getArea() {
		return new Rectangle(getPosicionXInt(), getPosicionYInt(), getAncho(), getAlto());
	}

}
