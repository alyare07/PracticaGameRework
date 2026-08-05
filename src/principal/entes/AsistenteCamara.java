package principal.entes;

import java.awt.Rectangle;

public class AsistenteCamara extends Ente {
	private boolean eliminado;
	private final Rectangle AREA;
	
	public AsistenteCamara(final int x, final int y, final int w, final int h) {
		this.AREA = new Rectangle(x, y, w, h);
	}
	
	@Override
	public void eliminar() {
		this.eliminado = true;
	}

	@Override
	public int getPosicionXInt() {
		return this.AREA.x;
	}

	@Override
	public int getPosicionYInt() {
		return this.AREA.y;
	}

	@Override
	public double getPosicionX() {
		return this.AREA.x;
	}

	@Override
	public double getPosicionY() {
		return this.AREA.y;
	}

	@Override
	public void modificarPosicionX(double desplazamientoX) {

	}

	@Override
	public void modificarPosicionY(double desplazamientoY) {

	}

	@Override
	public boolean estaEliminado() {
		return this.eliminado;
	}

	@Override
	public Rectangle getArea() {
		return this.AREA;
	}

}
