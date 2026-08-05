package principal.maquinaestado.estados;

import java.awt.Graphics2D;

import principal.maquinaestado.GestorEstados;
import principal.maquinaestado.estados.menu.MenuPartida;
import principal.maquinaestado.estados.pantallaCarga.GestorCargaJuego;
import principal.maquinaestado.estados.pantallaCarga.PantallaCarga;

public class GestorPartida implements EstadoJuego {  
	private final GestorEstados GE;
	private final GestorJuego GJ;
	private final MenuPartida MP;
	private EstadoJuego estadoActivo;
	private final  GestorCargaJuego GCJ = new GestorCargaJuego();
	public GestorPartida(final GestorEstados ge) {
		this.GE = ge;
		this.GJ = new GestorJuego(ge, this);
		this.GCJ.cargar(this.GJ, "escenario1.json", GCJ);
//		this.GJ.partidaNueva("escenario1.json");
		this.MP = new MenuPartida(ge, this);
		this.estadoActivo = new PantallaCarga(GCJ,"recursos/screens/pantallaCarga1.json");
	}
	
	public GestorPartida(final GestorEstados ge, final String mundo) {
		this.GE = ge;
		this.GJ = new GestorJuego(ge, this);
		this.GCJ.cargar(this.GJ, "escenario1.json", GCJ);
//		this.GJ.partidaNueva(mundo);
		this.MP = new MenuPartida(ge, this);
		this.estadoActivo = this.GJ;
	}

	@Override
	public void actualizar() {
		if(this.estadoActivo instanceof PantallaCarga) {
			if(this.GCJ.isCompleto()) {
				this.estadoActivo = GJ;
			}
		}
		this.estadoActivo.actualizar();
	}

	@Override
	public void pintar(final Graphics2D g) {
		this.estadoActivo.pintar(g);
	}

	public void establecerEstadoActivoJuego() {
		this.estadoActivo = this.GJ;
	}

	public void establecerEstadoActivoMenu() {
		this.estadoActivo = this.MP;
	}
	public EstadoJuego getEstadoActivo() {
		return this.estadoActivo;
	}
	
	public GestorJuego getGestorJuego() {
		return this.GJ;
	}
	
	public void cambiarMundo(final String ruta) {
		this.GCJ.cargar(this.GJ, ruta, GCJ);
		this.estadoActivo = new PantallaCarga(GCJ,"recursos/screens/pantallaCarga1.json");
	}
	
	

}
