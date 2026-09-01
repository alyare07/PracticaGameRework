package principal.animaciones;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import principal.utilidades.GestorTIempoActualizacion;
import principal.utilidades.Globales;
import principal.utilidades.HojaSprite;
import principal.utilidades.Render2D;

public class Animacion {

	protected final HojaSprite hojasprite;
	protected boolean repetitiva;
	protected final int TIEMPO_MS_POR_FRAMES;
	protected final GestorTIempoActualizacion GT_DURACION_ANIMACION;
	protected boolean animando;
	protected int spritePosicion;
	protected final int MAX_SPRITE_POSICION;
	protected boolean pausado;
	private int codActualizacion;
	private final boolean inversa;

	public Animacion(final HojaSprite hojasprite, final boolean repetitiva, final int tiempoMSFrames) {
		this(hojasprite, repetitiva, tiempoMSFrames, false);
	}

	public Animacion(final HojaSprite hojasprite, final boolean repetitiva, final int tiempoMSFrames,
			final boolean inversa) {
		this.hojasprite = hojasprite;
		this.repetitiva = repetitiva;
		this.TIEMPO_MS_POR_FRAMES = tiempoMSFrames;
		this.MAX_SPRITE_POSICION = (hojasprite != null) ? hojasprite.getCantidadSprite() - 1 : 0;
		this.GT_DURACION_ANIMACION = new GestorTIempoActualizacion();
		this.animando = true;
		this.inversa = inversa;
		this.codActualizacion = Integer.MIN_VALUE;

		if (inversa) {
			this.spritePosicion = this.MAX_SPRITE_POSICION;
		}
	}

	// =========================================================================
	// === ACTUALIZACIÓN LÓGICA (60 APS / TICK LÓGICO)
	// =========================================================================

	public void actualizar() {
		if (!this.animando || this.pausado || Globales.pausa) {
			return;
		}

		final int codGlobal = Globales.getCodActualizacion();
		if (this.codActualizacion == codGlobal) {
			return; // Ya fue actualizada en este tick lógico
		}
		this.codActualizacion = codGlobal;

		this.GT_DURACION_ANIMACION.actualizar();

		if (this.GT_DURACION_ANIMACION.transcurrioMS(this.TIEMPO_MS_POR_FRAMES)) {
			if (!this.repetitiva && ((this.inversa && (this.spritePosicion == 0))
					|| (!this.inversa && (this.spritePosicion == this.MAX_SPRITE_POSICION)))) {
				return;
			}

			this.GT_DURACION_ANIMACION.reiniciarTiempo();
			this.siguienteSprite();
		}
	}

	// =========================================================================
	// === RENDERIZADO IDEMPOTENTE (SOLO LECTURA)
	// =========================================================================

	public void pintar(final Graphics2D g, final double x, final double y, final boolean refJugador) {
		this.pintar(g, x, y, refJugador, false);
	}

	public void pintar(final Graphics2D g, final double x, final double y, final boolean refJugador,
			final boolean flash) {
		if (!this.animando || (this.hojasprite == null)) {
			return;
		}

		final BufferedImage spriteActual = flash ? this.hojasprite.getSpriteFlash(this.spritePosicion)
				: this.hojasprite.getSprite(this.spritePosicion);

		if (refJugador) {
			Render2D.dibujarImagenRefCamara(g, spriteActual, (int) x, (int) y);
		} else {
			Render2D.dibujarImagen(g, spriteActual, (int) x, (int) y);
		}
	}

	public void pintarConTransparencia(final Graphics2D g, final double x, final double y, final boolean refJugador,
			final float alpha) {
		this.pintarConTransparencia(g, x, y, refJugador, alpha, false);
	}

	public void pintarConTransparencia(final Graphics2D g, final double x, final double y, final boolean refJugador,
			final float alpha, final boolean flash) {
		if (!this.animando || (this.hojasprite == null)) {
			return;
		}

		final BufferedImage spriteActual = flash ? this.hojasprite.getSpriteFlash(this.spritePosicion)
				: this.hojasprite.getSprite(this.spritePosicion);

		if (refJugador) {
			Render2D.dibujarImagenConTransparenciaRefCamara(g, spriteActual, (int) x, (int) y, alpha);
		} else {
			Render2D.dibujarImagenConTransparencia(g, spriteActual, (int) x, (int) y, alpha);
		}
	}

	// =========================================================================
	// === CONTROL DE ESTADO
	// =========================================================================

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

	public int getSpritePosicion() {
		return this.spritePosicion;
	}

	public void setSpritePosicion(final int pos) {
		this.spritePosicion = Math.max(0, Math.min(this.MAX_SPRITE_POSICION, pos));
	}

	public boolean animacionFinalizada() {
		return (!this.repetitiva
				&& ((this.inversa && (this.spritePosicion == 0))
						|| (!this.inversa && (this.spritePosicion == this.MAX_SPRITE_POSICION)))
				&& this.GT_DURACION_ANIMACION.transcurrioMS(this.TIEMPO_MS_POR_FRAMES));
	}
}