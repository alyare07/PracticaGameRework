package principal.entes;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import principal.mapa.Mundo;

public abstract class Ente {
	protected boolean eliminado;
	protected long codRender;
	protected Mundo mundo;
	protected final Rectangle AREA_ENTE_RETORNO = new Rectangle();

	public void actualizar() {

	}

	public void pintar(final Graphics2D g) {

	}

	public abstract void eliminar();

	public abstract int getPosicionXInt();

	public abstract int getPosicionYInt();

	public abstract double getPosicionX();

	public abstract double getPosicionY();

	public abstract void modificarPosicionX(final double desplazamientoX);

	public abstract void modificarPosicionY(final double desplazamientoY);

	public abstract boolean estaEliminado();

	public long getCodRender() {
		return this.codRender;
	}

	public void setCodRender(final long cod) {
		this.codRender = cod;
	}

	public void setMundo(final Mundo mundo) {
		this.mundo = mundo;
	}

	public Mundo getMundo() {
		return this.mundo;
	}

	public Rectangle getArea() {
		this.AREA_ENTE_RETORNO.setBounds(this.getPosicionXInt(), this.getPosicionYInt(), this.getAncho(),
				this.getAlto());
		return this.AREA_ENTE_RETORNO;
	}

	public abstract int getAncho();

	public abstract int getAlto();

}
