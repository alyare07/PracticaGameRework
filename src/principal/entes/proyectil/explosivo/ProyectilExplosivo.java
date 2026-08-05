package principal.entes.proyectil.explosivo;

import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Criatura.Direccion;
import principal.entes.proyectil.ProyectilGeneral;
import principal.mapa.Mundo;
import principal.mapa.Tile;
import principal.utilidades.Constantes;
import principal.utilidades.GestorTiempo;

public abstract class ProyectilExplosivo extends ProyectilGeneral {

	private static final long serialVersionUID = -6101794986241939867L;
	protected final double DIAMETRO_EXPLOSION;
	protected final int TIEMPO_MS_ANIMACION_IMPACTO = 400;
	protected final GestorTiempo GT_ANIMACION_IMPACTO = new GestorTiempo();
	protected boolean impactoRealizado;
	public ProyectilExplosivo(double damage, double velocidad, double alcance,final boolean penetraObstaculos, Mundo escenario,
			double x, double y, int ancho, int alto, final double radioExplosion, Direccion direccion, Ente causante) {
		super(damage, velocidad, penetraObstaculos, alcance, escenario, x, y, ancho, alto, direccion, causante);
		this.DIAMETRO_EXPLOSION = radioExplosion;
	}
	
	
	protected void verificarImpacto() {
		final Rectangle area = this.getArea();
		for(Criatura c : this.mundo.getCriaturasIntersectadasConEnte(this)) {
			if(area.intersects(c.getRectangulo())) {
				if(c == this.CAUSANTE) {
					continue;
				}
				this.impactar(c);
				this.realizarImpacto();
				break;
			}
		}
		
		if(this.impactoRealizado) {
			return;
		}else if(Constantes.JUGADOR != this.CAUSANTE){
			if(area.intersects(Constantes.JUGADOR.getRectangulo())) {
				
				this.impactar(Constantes.JUGADOR);
				this.realizarImpacto();
				return;
			}
		}
		
		final Tile tilePosicionado = this.mundo.getMapa().getTileReferenciado(area.x, area.y);
		if(tilePosicionado == null) { // se encuentra fuera del mapa
			this.eliminar();
			return;
		}
		if(!this.PENETRANTE) {
			if (this.mundo.getMapa().intersecta(area)) {
				this.eliminar();
			}
		}
	}
	
	
	protected void realizarImpacto() {
		this.impactoRealizado = true;
		this.GT_ANIMACION_IMPACTO.establecerReferenciaTiempoActual();
	}
	
	public boolean impactoRealizado() {
		return this.impactoRealizado;
	}
	
	protected Ellipse2D getAreaExplosion() {
		return new Ellipse2D.Double(this.getPosicionXInt() - this.DIAMETRO_EXPLOSION/2, this.getPosicionYInt() + this.alto/2 - this.DIAMETRO_EXPLOSION/2,this.DIAMETRO_EXPLOSION,this.DIAMETRO_EXPLOSION);
	}
	
}
