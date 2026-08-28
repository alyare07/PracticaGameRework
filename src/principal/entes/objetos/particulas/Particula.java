package principal.entes.objetos.particulas;

import java.awt.Rectangle;

import principal.entes.objetos.Objeto;
import principal.utilidades.GestorTiempo;

public abstract class Particula extends Objeto {

	private static final long serialVersionUID = -6870582749216223945L;
	protected boolean eliminado;
	protected final GestorTiempo GT_CREACION;
	protected final int tiempoVidaMs;

	public Particula(final int x, final int y, final int tiempoVidaMs) {
		super(x, y);
		this.GT_CREACION = new GestorTiempo();
		this.GT_CREACION.establecerReferenciaTiempoActual();
		this.tiempoVidaMs = tiempoVidaMs;
	}

	@Override
	public void actualizar() {
		if (this.GT_CREACION.transcurrioMiliSegundos(this.tiempoVidaMs)) {
			this.eliminado = true;
		}
	}

	@Override
	public Rectangle getArea() {
		return new Rectangle(this.getPosicionXInt(), this.getPosicionYInt(), this.getAncho(), this.getAlto());
	}

}
