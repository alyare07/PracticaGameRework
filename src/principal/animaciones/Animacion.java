package principal.animaciones;

import java.awt.Graphics2D;

import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.GestorTIempoActualizacion;
import principal.utilidades.HojaSprite;

public class Animacion {
	
protected final HojaSprite hojasprite;
protected boolean repetitiva;
protected final int TIEMPO_MS_POR_FRAMES;
protected final GestorTIempoActualizacion GT_DURACION_ANIMACION;
protected boolean animando;
protected int spritePosicion;
protected final int MAX_SPRITE_POSICION;
protected boolean pausado;
private int codPintado;

	
	public Animacion(final HojaSprite hojasprite,final boolean repetitiva, final int tiempoMSFrames) {
		this.hojasprite = hojasprite;
		this.repetitiva = repetitiva;
		this.TIEMPO_MS_POR_FRAMES = tiempoMSFrames;
		this.MAX_SPRITE_POSICION = hojasprite.getCantidadSprite()-1;
		this.GT_DURACION_ANIMACION = new GestorTIempoActualizacion();
		this.animando = true;
	}
	
	public void pintar(final Graphics2D g, final double x, final double y, final boolean refJugador) {
		if(!this.animando) {
			return;
		}
		if(!repetitiva && (this.spritePosicion == this.MAX_SPRITE_POSICION) && this.GT_DURACION_ANIMACION.transcurrioMS(TIEMPO_MS_POR_FRAMES)) {
			return;
		}
		if(refJugador) {
			DibujoDebug.dibujarImagenRefCamara(g, this.hojasprite.getSprite(this.spritePosicion), (int)x, (int)y);
		}else {
			DibujoDebug.dibujarImagen(g, this.hojasprite.getSprite(this.spritePosicion), (int)x, (int)y);
		}
		if(Constantes.GLOBALES.pausa) {
			if(!this.pausado)	this.pausado = true;
		}else {
			if(this.pausado) this.pausado = false;
		}
		if(this.codPintado != Constantes.getCodActualizacion()) {
			if(!pausado) {
				this.codPintado = Constantes.getCodActualizacion();
				this.GT_DURACION_ANIMACION.actualizar();
				if(this.GT_DURACION_ANIMACION.transcurrioMS(this.TIEMPO_MS_POR_FRAMES)) {
					if(!repetitiva && (this.spritePosicion == this.MAX_SPRITE_POSICION) && this.GT_DURACION_ANIMACION.transcurrioMS(TIEMPO_MS_POR_FRAMES)) {
						return;
					}
					this.GT_DURACION_ANIMACION.reiniciarTiempo();
					this.siguienteSprite();
				}
			}
		}
		
		
		
		
		
	}
	
	public void pintarConTransparencia(final Graphics2D g, final double x, final double y, final boolean refJugador, final float alpha) {
		if(!this.animando) {
			return;
		}
		if(!repetitiva && (this.spritePosicion == this.MAX_SPRITE_POSICION) && this.GT_DURACION_ANIMACION.transcurrioMS(TIEMPO_MS_POR_FRAMES)) {
			return;
		}
		
		if(refJugador) {
			DibujoDebug.dibujarImagenConTransparenciaRefCamara(g, this.hojasprite.getSprite(this.spritePosicion), (int)x, (int)y, alpha);
		}else {
			DibujoDebug.dibujarImagenConTransparencia (g, this.hojasprite.getSprite(this.spritePosicion), (int)x, (int)y, alpha);
		}
		if(Constantes.GLOBALES.pausa) {
			if(!this.pausado)	this.pausado = true;
		}else {
			if(this.pausado) this.pausado = false;
		}
		if(this.codPintado != Constantes.getCodActualizacion()) {
			if(!pausado) {
				this.codPintado = Constantes.getCodActualizacion();
				this.GT_DURACION_ANIMACION.actualizar();
				if(this.GT_DURACION_ANIMACION.transcurrioMS(this.TIEMPO_MS_POR_FRAMES)) {
					if(!repetitiva && (this.spritePosicion == this.MAX_SPRITE_POSICION) && this.GT_DURACION_ANIMACION.transcurrioMS(TIEMPO_MS_POR_FRAMES)) {
						return;
					}
					this.GT_DURACION_ANIMACION.reiniciarTiempo();
					this.siguienteSprite();
				}
			}
		}
	}
	
	public void reanudarAnimacion() {
		this.animando = true;
	}
	
	public void pausarAnimacion() {
		this.animando = false;
	}
	
	public void reiniciarAnimacion() {
		this.spritePosicion = 0;
		this.GT_DURACION_ANIMACION.reiniciarTiempo();
	}
	
	public boolean animando() {
		return this.animando;
	}
	
	
	protected void siguienteSprite() {
		this.spritePosicion++;
		if(this.spritePosicion>this.MAX_SPRITE_POSICION) {
			this.spritePosicion = 0;
		}
	}
	
	public boolean animacionFinalizada() {
//		System.out.println("Repetitiva: " + this.repetitiva +" , posicion final: "+(this.spritePosicion == this.MAX_SPRITE_POSICION)+ " tiempo transcurrido: "+this.GT_DURACION_ANIMACION.transcurrioMS(TIEMPO_MS_POR_FRAMES));
		return (!repetitiva && (this.spritePosicion == this.MAX_SPRITE_POSICION) && this.GT_DURACION_ANIMACION.transcurrioMS(TIEMPO_MS_POR_FRAMES));
	}
	

}
