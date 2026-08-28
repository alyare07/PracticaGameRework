package principal.entes.estructuras;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import principal.entes.Ente;

public class Estructura extends Ente {
	protected final int ANCHO;
	protected final int ALTO;
	protected final int X;
	protected final int Y;

	public Estructura(final int x, final int y, final int ancho, final int alto) {
		this.ANCHO = ancho;
		this.ALTO = alto;
		this.X = x;
		this.Y = y;
	}

	@Override
	public void pintar(final Graphics2D g) {
	}

	@Override
	public void actualizar() {

	}

	@Override
	public int getAncho() {
		return this.ANCHO;
	}

	@Override
	public int getAlto() {
		return this.ALTO;
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
		return this.X;
	}

	@Override
	public double getPosicionY() {
		return this.Y;
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

	@Override
	public Rectangle getArea() {
		return new Rectangle(this.getPosicionXInt(), this.getPosicionYInt(), this.getAncho(), this.getAlto());
	}

	@Override
	public void setPosicion(final double x, final double y) {

	}

}
