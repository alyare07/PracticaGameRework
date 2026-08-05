package principal.entes.proyectil;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.HashMap;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Criatura.Direccion;
import principal.mapa.Mundo;
import principal.mapa.Tile;
import principal.utilidades.Constantes;

public class ProyectilGeneral extends Proyectil implements Serializable{
	
	public ProyectilGeneral(double damage, double velocidad, boolean penetrante, double alcance, Mundo mundo,
			double x, double y, int ancho, int alto, Direccion direccion, Ente causante) {
		super(damage, velocidad, penetrante, alcance, mundo, x, y, ancho, alto, direccion, causante);
	}

	private static final long serialVersionUID = -3596461015684122157L;
	protected final HashMap<Criatura, Criatura> perforados = new HashMap<Criatura, Criatura>();
	
	
	public void actualizar() {
//		System.out.println("Proyectil : "+ this.area);
		if(!eliminado) {
			if(this.distanciaRecorrida>= this.ALCANCE) {
				this.eliminar();
				return;
			}
			this.mover();
			this.verificarImpacto();
			
		}
		
	}
	@Override
	public void pintar(final Graphics2D g) {
		super.pintar(g);
	}
	
	
	protected void verificarImpacto() {
		final Rectangle area = this.getArea();
		for(Criatura c : this.mundo.getCriaturasIntersectadasConEnte(this)) {
			if(area.intersects(c.getRectangulo())) {
				if(c == this.CAUSANTE) {
					continue;
				}
				this.impactar(c);
				if(!this.PENETRANTE) {
					this.eliminar();
					break;
				}
			}
		}
		
		if(this.eliminado) {
			return;
		}else if(Constantes.JUGADOR != this.CAUSANTE){
			if(area.intersects(Constantes.JUGADOR.getRectangulo())) {
				
				this.impactar(Constantes.JUGADOR);
				if(!this.PENETRANTE) {
					this.eliminar();
					return;
				}
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
	
	
	@Override
	protected void impactar(final Criatura c) {
		if(this.perforados.containsKey(c)) {
			return;
		}
		this.perforados.put(c,c);
		c.recibirAtaque(this.DAMAGE, CAUSANTE);
	}

	@Override
	public void eliminar() {
		this.eliminado = true;
	}

	@Override
	public int getPosicionXInt() {
		return (int)this.x;
	}

	@Override
	public int getPosicionYInt() {

		return (int)this.y;
	}

	@Override
	public double getPosicionX() {
		return this.x;
	}

	@Override
	public double getPosicionY() {
		return this.y;
	}

	@Override
	public void modificarPosicionX(double desplazamientoX) {
		
	}

	@Override
	public void modificarPosicionY(double desplazamientoY) {
		
	}

	@Override
	public boolean estaEliminado() {
		return this.eliminado;
	}

	@Override
	public void pintarAnimacionImpacto(Graphics2D g) {
		// TODO Auto-generated method stub
		
	}
	


}
