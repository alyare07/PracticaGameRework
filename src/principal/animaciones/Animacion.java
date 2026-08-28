package principal.animaciones;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import principal.utilidades.DibujoDebug;
import principal.utilidades.GestorTIempoActualizacion;
import principal.utilidades.Globales;
import principal.utilidades.HojaSprite;

/**
 * Controlador de fotogramas y temporización de animaciones con soporte para
 * renderizado estándar, transparencia y máscaras de impacto Hit-Flash.
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
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
	private final boolean inversa;

	public Animacion(final HojaSprite hojasprite, final boolean repetitiva, final int tiempoMSFrames) {
		this(hojasprite, repetitiva, tiempoMSFrames, false);
	}

	public Animacion(final HojaSprite hojasprite, final boolean repetitiva, final int tiempoMSFrames,
			final boolean inversa) {
		this.hojasprite = hojasprite;
		this.repetitiva = repetitiva;
		this.TIEMPO_MS_POR_FRAMES = tiempoMSFrames;
		this.MAX_SPRITE_POSICION = hojasprite.getCantidadSprite() - 1;
		this.GT_DURACION_ANIMACION = new GestorTIempoActualizacion();
		this.animando = true;
		this.inversa = inversa;
		if (inversa) {
			this.spritePosicion = this.MAX_SPRITE_POSICION;
		}
	}

	public void pintar(final Graphics2D g, final double x, final double y, final boolean refJugador) {
		this.pintar(g, x, y, refJugador, false);
	}

	/**
	 * Renderiza el frame actual alternando entre la textura normal y la máscara
	 * blanca de impacto.
	 *
	 * @param g          Contexto gráfico {@link Graphics2D}.
	 * @param x          Coordenada X.
	 * @param y          Coordenada Y.
	 * @param refJugador {@code true} para aplicar desplazamiento de cámara.
	 * @param flash      {@code true} para dibujar la silueta blanca de daño
	 *                   (Hit-Flash).
	 */
	public void pintar(final Graphics2D g, final double x, final double y, final boolean refJugador,
			final boolean flash) {
		if (!this.animando) {
			return;
		}
		if (!this.repetitiva
				&& ((this.inversa && (this.spritePosicion == 0))
						|| (!this.inversa && (this.spritePosicion == this.MAX_SPRITE_POSICION)))
				&& this.GT_DURACION_ANIMACION.transcurrioMS(this.TIEMPO_MS_POR_FRAMES)) {
			return;
		}

		final BufferedImage spriteActual = flash ? this.hojasprite.getSpriteFlash(this.spritePosicion)
				: this.hojasprite.getSprite(this.spritePosicion);

		if (refJugador) {
			DibujoDebug.dibujarImagenRefCamara(g, spriteActual, (int) x, (int) y);
		} else {
			DibujoDebug.dibujarImagen(g, spriteActual, (int) x, (int) y);
		}

		if (Globales.pausa) {
			if (!this.pausado) {
				this.pausado = true;
			}
		} else if (this.pausado) {
			this.pausado = false;
		}

		if (this.codPintado != Globales.getCodActualizacion()) {
			if (!this.pausado) {
				this.codPintado = Globales.getCodActualizacion();
				this.GT_DURACION_ANIMACION.actualizar();
				if (this.GT_DURACION_ANIMACION.transcurrioMS(this.TIEMPO_MS_POR_FRAMES)) {
					if (!this.repetitiva
							&& ((this.inversa && (this.spritePosicion == 0))
									|| (!this.inversa && (this.spritePosicion == this.MAX_SPRITE_POSICION)))
							&& this.GT_DURACION_ANIMACION.transcurrioMS(this.TIEMPO_MS_POR_FRAMES)) {
						return;
					}
					this.GT_DURACION_ANIMACION.reiniciarTiempo();
					this.siguienteSprite();
				}
			}
		}
	}

	public void pintarConTransparencia(final Graphics2D g, final double x, final double y, final boolean refJugador,
			final float alpha) {
		this.pintarConTransparencia(g, x, y, refJugador, alpha, false);
	}

	public void pintarConTransparencia(final Graphics2D g, final double x, final double y, final boolean refJugador,
			final float alpha, final boolean flash) {
		if (!this.animando) {
			return;
		}
		if (!this.repetitiva
				&& ((this.inversa && (this.spritePosicion == 0))
						|| (!this.inversa && (this.spritePosicion == this.MAX_SPRITE_POSICION)))
				&& this.GT_DURACION_ANIMACION.transcurrioMS(this.TIEMPO_MS_POR_FRAMES)) {
			return;
		}

		final BufferedImage spriteActual = flash ? this.hojasprite.getSpriteFlash(this.spritePosicion)
				: this.hojasprite.getSprite(this.spritePosicion);

		if (refJugador) {
			DibujoDebug.dibujarImagenConTransparenciaRefCamara(g, spriteActual, (int) x, (int) y, alpha);
		} else {
			DibujoDebug.dibujarImagenConTransparencia(g, spriteActual, (int) x, (int) y, alpha);
		}

		if (Globales.pausa) {
			if (!this.pausado) {
				this.pausado = true;
			}
		} else if (this.pausado) {
			this.pausado = false;
		}

		if (this.codPintado != Globales.getCodActualizacion()) {
			if (!this.pausado) {
				this.codPintado = Globales.getCodActualizacion();
				this.GT_DURACION_ANIMACION.actualizar();
				if (this.GT_DURACION_ANIMACION.transcurrioMS(this.TIEMPO_MS_POR_FRAMES)) {
					if (!this.repetitiva
							&& ((this.inversa && (this.spritePosicion == 0))
									|| (!this.inversa && (this.spritePosicion == this.MAX_SPRITE_POSICION)))
							&& this.GT_DURACION_ANIMACION.transcurrioMS(this.TIEMPO_MS_POR_FRAMES)) {
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
		if (this.inversa) {
			this.spritePosicion = this.MAX_SPRITE_POSICION;
		} else {
			this.spritePosicion = 0;
		}
		this.GT_DURACION_ANIMACION.reiniciarTiempo();
	}

	public boolean animando() {
		return this.animando;
	}

	protected void siguienteSprite() {
		if (!this.inversa) {
			this.spritePosicion++;
			if (this.spritePosicion > this.MAX_SPRITE_POSICION) {
				this.spritePosicion = 0;
			}
		} else {
			this.spritePosicion--;
			if (this.spritePosicion < 0) {
				this.spritePosicion = this.MAX_SPRITE_POSICION;
			}
		}
	}

	public int getSpritePosicion() {
		if (this.codPintado != Globales.getCodActualizacion()) {
			if (!this.pausado && !Globales.pausa) {
				this.codPintado = Globales.getCodActualizacion();
				this.GT_DURACION_ANIMACION.actualizar();
				if (this.GT_DURACION_ANIMACION.transcurrioMS(this.TIEMPO_MS_POR_FRAMES)) {
					this.GT_DURACION_ANIMACION.reiniciarTiempo();
					this.siguienteSprite();
				}
			}
		}
		return this.spritePosicion;
	}

	public boolean animacionFinalizada() {
		return (!this.repetitiva
				&& ((this.inversa && (this.spritePosicion == 0))
						|| (!this.inversa && (this.spritePosicion == this.MAX_SPRITE_POSICION)))
				&& this.GT_DURACION_ANIMACION.transcurrioMS(this.TIEMPO_MS_POR_FRAMES));
	}
}