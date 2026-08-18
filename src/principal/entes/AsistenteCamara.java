package principal.entes;

import java.awt.Rectangle;

/**
 * Entidad auxiliar invisible utilizada como objetivo simulado para la cámara.
 * Útil para cinemáticas, desplazamientos suaves, pantallas de carga y enfoques
 * temporales.
 */
public class AsistenteCamara extends Ente {

	private static final long serialVersionUID = 1L;

	private boolean eliminado;
	private final Rectangle AREA;

	public AsistenteCamara(final int x, final int y, final int w, final int h) {
		this.AREA = new Rectangle(x, y, Math.max(1, w), Math.max(1, h));
	}

	public void setPosicion(final int x, final int y) {
		this.AREA.x = x;
		this.AREA.y = y;
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
	public void modificarPosicionX(final double desplazamientoX) {
		this.AREA.x += (int) Math.round(desplazamientoX);
	}

	@Override
	public void modificarPosicionY(final double desplazamientoY) {
		this.AREA.y += (int) Math.round(desplazamientoY);
	}

	@Override
	public boolean estaEliminado() {
		return this.eliminado;
	}

	@Override
	public int getAncho() {
		return this.AREA.width;
	}

	@Override
	public int getAlto() {
		return this.AREA.height;
	}
}