package principal.entes.objetos.particulas;

import java.awt.image.BufferedImage;

import principal.entes.objetos.Objeto;
import principal.utilidades.Textura;

public final class Sangre extends Particula {
	private static final long serialVersionUID = 7303537108026422687L;
	public static final int TIEMPO_VIDA_MS = 4500;

	public Sangre(final int x, final int y) {
		super(x, y, TIEMPO_VIDA_MS);
	}

	@Override
	public int getAncho() {
		return 8;
	}

	@Override
	public int getAlto() {
		// TODO Auto-generated method stub
		return 8;
	}

	@Override
	public BufferedImage getTextura() {
		return Textura.getTextura(Textura.TEXTURA_X8_PARTICULA_SANGRE);
	}

	@Override
	public boolean esSolido() {
		return false;
	}

	@Override
	public Objeto copiar() {
		return new Sangre(x, y);
	}

	@Override
	public void eliminar() {
		this.eliminado = true;
	}

	@Override
	public double getPosicionX() {
		return x;
	}

	@Override
	public double getPosicionY() {
		return y;
	}

	@Override
	public void modificarPosicionX(double desplazamientoX) {
		this.x += desplazamientoX;
	}

	@Override
	public void modificarPosicionY(double desplazamientoY) {
		this.y += desplazamientoY;
	}

	@Override
	public boolean estaEliminado() {
		return this.eliminado;
	}

}
