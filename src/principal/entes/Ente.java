package principal.entes;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import principal.mapa.Mundo;

public abstract class Ente {
	protected boolean eliminado;
	protected long codRender;
	protected Mundo mundo;
	
	public void actualizar() {
		
	}

	public void pintar(Graphics2D g) {

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
		return codRender;
	}

	public void setCodRender(long cod) {
		this.codRender = cod;
	}
	
	public void setMundo(final Mundo mundo) {
		this.mundo = mundo;
	}
	public Mundo getMundo() {
		return this.mundo;
	}

	public abstract Rectangle getArea();
	
	
}
