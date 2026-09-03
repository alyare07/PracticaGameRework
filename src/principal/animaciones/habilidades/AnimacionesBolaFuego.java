package principal.animaciones.habilidades;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import principal.animaciones.Animacion;
import principal.entes.criaturas.Criatura.Direccion;
import principal.entes.proyectil.explosivo.BolaFuego;
import principal.recursos.ClaveHoja;
import principal.utilidades.Globales;
import principal.utilidades.HojaSprite;

public class AnimacionesBolaFuego {

	public final Animacion ANIMACION_ESTANDAR_DERECHA;
	public final Animacion ANIMACION_ESTANDAR_IZQUIERDA;
	public final Animacion ANIMACION_ESTANDAR_ARRIBA;
	public final Animacion ANIMACION_ESTANDAR_ABAJO;
	public final Animacion ANIMACION_EXPLOSION;

	public AnimacionesBolaFuego() {
		final HojaSprite hojaBase = Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.BOLA_FUEGO);
		final HojaSprite hojaVolteada = Globales.GESTOR_TEXTURAS.getHojaVolteadaH(ClaveHoja.BOLA_FUEGO);
		final HojaSprite hojaExplosion = Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.EXPLOSION_BOLA_FUEGO);

		// Rotaciones direccionales en VRAM a partir del sprite base
		final int cant = hojaBase.getCantidadSprite();
		final BufferedImage[] spritesArriba = new BufferedImage[cant];
		final BufferedImage[] flashArriba = new BufferedImage[cant];
		final BufferedImage[] spritesAbajo = new BufferedImage[cant];
		final BufferedImage[] flashAbajo = new BufferedImage[cant];

		for (int i = 0; i < cant; i++) {
			spritesArriba[i] = Globales.FUNCIONES.TEXTURAS_TOOLS.voltearImagen90GradosIzquierda(hojaBase.getSprite(i));
			flashArriba[i] = Globales.FUNCIONES.TEXTURAS_TOOLS.crearMascaraBlanca(spritesArriba[i]);

			spritesAbajo[i] = Globales.FUNCIONES.TEXTURAS_TOOLS.voltearImagen90GradosDerecha(hojaBase.getSprite(i));
			flashAbajo[i] = Globales.FUNCIONES.TEXTURAS_TOOLS.crearMascaraBlanca(spritesAbajo[i]);
		}

		this.ANIMACION_ESTANDAR_DERECHA = new Animacion(hojaBase, true, 100);
		this.ANIMACION_ESTANDAR_IZQUIERDA = new Animacion(hojaVolteada, true, 100);
		this.ANIMACION_ESTANDAR_ARRIBA = new Animacion(new HojaSprite(spritesArriba, flashArriba, 16, 16), true, 100);
		this.ANIMACION_ESTANDAR_ABAJO = new Animacion(new HojaSprite(spritesAbajo, flashAbajo, 16, 16), true, 100);
		this.ANIMACION_EXPLOSION = new Animacion(hojaExplosion, false, 100);
	}

	public void pintar(final Graphics2D g, final int x, final int y, final BolaFuego bolaFuego) {
		final Direccion direccion = bolaFuego.getDireccion();
		final byte margen = 5;

		if (!bolaFuego.impactoRealizado()) {
			if (direccion == Direccion.OESTE) {
				this.ANIMACION_ESTANDAR_IZQUIERDA.pintar(g, x, y - margen, true);
			} else if (direccion == Direccion.ESTE) {
				this.ANIMACION_ESTANDAR_DERECHA.pintar(g, x, y - margen, true);
			} else if (direccion == Direccion.NORTE) {
				this.ANIMACION_ESTANDAR_ARRIBA.pintar(g, x - margen, y, true);
			} else if (direccion == Direccion.SUR) {
				this.ANIMACION_ESTANDAR_ABAJO.pintar(g, x - margen, y, true);
			}
		} else {
			this.ANIMACION_EXPLOSION.pintar(g, x - 16, y - 8, true);
		}
	}
}