package principal.entes.proyectil.explosivo;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import principal.animaciones.habilidades.AnimacionesBolaFuego;
import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Criatura.Direccion;
import principal.mapa.Mundo;
import principal.mapa.Tile;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;

public class BolaFuego extends ProyectilExplosivo {

	private static final long serialVersionUID = -8819813981468122319L;
	
	private final AnimacionesBolaFuego ANIMACION;
	
	public BolaFuego(double damage, double velocidad, double alcance,final boolean penetraObstaculos, Mundo escenario, double x,
			double y, final double diametroExplosion, Direccion direccion, Ente causante) {
		super(damage, velocidad, alcance,penetraObstaculos, escenario, x, y, generarAncho(direccion), generarAlto(direccion),diametroExplosion, direccion, causante);
		this.ANIMACION = new AnimacionesBolaFuego();
	}
	
	
	public void actualizar() {
		if(!eliminado) {
			if(!this.impactoRealizado) {
				if(this.distanciaRecorrida>= this.ALCANCE && this.ALCANCE>0) {
					this.eliminar();
					return;
				}
				this.mover();
				this.verificarImpacto();
			}else {
				if(this.ANIMACION.ANIMACION_EXPLOSION.animacionFinalizada()) {
					this.eliminar();
				}
			}
			
		}
		
	}
	
	
	public void pintar(final Graphics2D g) {
		this.ANIMACION.pintar(g, this.getPosicionXInt(), this.getPosicionYInt(), this);
		if(this.impactoRealizado && Constantes.TECLADO.TECLA_DEBUG.presionado()) {
			DibujoDebug.dibujarFiguraEllipseRefCamara(g, this.getAreaExplosion().getBounds(), Color.red);
		}
		super.pintar(g);
	}
	
	
	protected void verificarImpacto() {
		final Rectangle area = this.getArea();
		
		if(this.mundo.intersectaAlgunaCriatura(area, Constantes.JUGADOR != this.CAUSANTE)) {
			this.realizarImpacto();
			for(Criatura c : this.mundo.getCriaturasIntersectadas(this.getAreaExplosion(), Constantes.JUGADOR != this.CAUSANTE)) {
				if(c == this.CAUSANTE) {
					continue;
				}
				this.impactar(c);
			}
		}
		
		if(this.impactoRealizado) {
			return;
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
	
	private static int generarAncho(final Direccion direccion) {
		int ancho = 0;
		if(direccion == Direccion.NORTE || direccion == Direccion.SUR) {
			ancho = 8;
		}else {
			ancho = 16;
		}
		return ancho;
	}
	
	private static int generarAlto(final Direccion direccion) {
		int alto = 0;
		if(direccion == Direccion.NORTE || direccion == Direccion.SUR) {
			alto = 16;
		}else {
			alto = 8;
		}
		return alto;
	}

}
