package principal.entes.proyectil.filtro;

import java.awt.Rectangle;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura.Direccion;
import principal.mapa.Mundo;
import principal.utilidades.Constantes;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Sonidos;

public class GolpeMeleContraJugador extends ProyectilContraJugador {
	
	private static final long serialVersionUID = 5631243201941847598L;
	
	private final GestorTiempo GT_DIBUJADO = new GestorTiempo();
	private final int TIEMPO_DIBUJADO_MS = 200;
	
	private boolean golpeRealizado;
	
	public GolpeMeleContraJugador(double damage, boolean penetrante, Mundo escenario, double x,
			double y, int ancho, int alto, Direccion direccion, Ente causante) {
		super(damage, 0, penetrante, 0, escenario, x, y, ancho, alto, direccion, causante);
	}
	
	public void actualizar() {
		if(!this.golpeRealizado) {
			this.GT_DIBUJADO.establecerReferenciaTiempoActual();
			this.verificarImpacto();
			this.golpeRealizado = true;
			Sonidos.SONIDO_GOLPE_2.reproducir();
		}else {
			if(this.GT_DIBUJADO.transcurrioMiliSegundos(this.TIEMPO_DIBUJADO_MS)) {
				this.eliminar();
			}
		}
		
	}
	
	protected void verificarImpacto() {
		final Rectangle area = this.getArea();
		if(area.intersects(Constantes.JUGADOR.getRectangulo())) {
				
			this.impactar(Constantes.JUGADOR);
		}
	}


}
