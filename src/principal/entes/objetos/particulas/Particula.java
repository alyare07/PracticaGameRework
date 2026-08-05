package principal.entes.objetos.particulas;


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

	public void actualizar() {
		if (this.GT_CREACION.transcurrioMiliSegundos(tiempoVidaMs)) {
			this.eliminado = true;
		}
	}
	
	

}
