package principal.entes.proyectil.filtro;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.Serializable;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Criatura.Direccion;
import principal.entes.proyectil.Proyectil;
import principal.mapa.Mundo;
import principal.mapa.Tile;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;

public class ProyectilContraJugador extends Proyectil implements Serializable{

	public ProyectilContraJugador(double damage, double velocidad, boolean penetrante, double alcance, Mundo escenario,
			double x, double y, int ancho, int alto, Direccion direccion, Ente causante) {
		super(damage, velocidad, penetrante, alcance, escenario, x, y, ancho, alto, direccion, causante);
	}

	private static final long serialVersionUID = 8936062246678950377L;

	
	
	
	public void actualizar() {
		if(!eliminado) {
			if(this.distanciaRecorrida>= this.ALCANCE) {
				this.eliminar();
				return;
			}
			this.mover();
			this.verificarImpacto();
			
		}
		
	}
	
	
	public void pintar(final Graphics2D g) {
		DibujoDebug.dibujarRectanguloRellenoRefCamara(g, this.getPosicionXInt(), this.getPosicionYInt(), ancho, alto, Color.red);
	}
	
	public Rectangle getArea() {
		return new Rectangle((int)this.x, (int)this.y , ancho, alto);
	}
	
	protected void verificarImpacto() {
		final Rectangle area = this.getArea();
		
		if(area.intersects(Constantes.JUGADOR.getRectangulo())) {
			this.impactar(Constantes.JUGADOR);
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
	
	protected void impactar(final Criatura c) {
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
