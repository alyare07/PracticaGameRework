package principal.entes.proyectil;

import java.awt.Rectangle;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Criatura.Direccion;
import principal.mapa.Mundo;
import principal.utilidades.Constantes;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Sonidos;

public class GolpeMele extends ProyectilGeneral {
	
	private static final long serialVersionUID = 5631243201941847598L;
	
	private final GestorTiempo GT_DIBUJADO = new GestorTiempo();
	private final int TIEMPO_DIBUJADO_MS = 500;
	
	private boolean golpeRealizado;
	
	public GolpeMele(double damage, boolean penetrante, Mundo escenario, double x,
			double y, int ancho, int alto, Direccion direccion, Ente causante) {
		super(damage, 0, penetrante, 0, escenario, x, y, ancho, alto, direccion, causante);
	}
	
	@Override
	public void actualizar() {
		if(!this.golpeRealizado) {
			this.GT_DIBUJADO.establecerReferenciaTiempoActual();
			this.verificarImpacto();
			this.golpeRealizado = true;
			Sonidos.SONIDO_GOLPE.reproducir();
		}else {
			if(this.GT_DIBUJADO.transcurrioMiliSegundos(this.TIEMPO_DIBUJADO_MS)) {
				this.eliminar();
			}
		}
		
	}
	
	@Override
	protected void verificarImpacto() {
		final Rectangle area = this.getArea();
		for(Criatura c : this.mundo.getCriaturasIntersectadasConEnte(this)) {
			if(area.intersects(c.getRectangulo())) {
				if(c == this.CAUSANTE) {
					continue;
				}
				this.impactar(c);
//				System.out.println("proyectil impacta con "+c);
				if(!this.PENETRANTE) {
					
					return;
				}
			}
		}
		
		if(this.eliminado) {
			return;
		}else if(Constantes.JUGADOR != this.CAUSANTE){
			if(area.intersects(Constantes.JUGADOR.getRectangulo())) {
				
				this.impactar(Constantes.JUGADOR);
				if(!this.PENETRANTE) {
					return;
				}
			}
		}
	}


}
