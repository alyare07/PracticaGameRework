package principal.animaciones.habilidades;

import java.awt.Graphics2D;

import principal.animaciones.Animacion;
import principal.entes.criaturas.Criatura.Direccion;
import principal.entes.proyectil.explosivo.BolaFuego;
import principal.utilidades.Globales;

public class AnimacionesBolaFuego {
	public final Animacion ANIMACION_ESTANDAR_DERECHA = new Animacion(
			Globales.LISTA_HOJAS_SPRITES.BOLA_FUEGO.ESTANDAR_DERECHA, true, 100);
	public final Animacion ANIMACION_ESTANDAR_IZQUIERDA = new Animacion(
			Globales.LISTA_HOJAS_SPRITES.BOLA_FUEGO.ESTANDAR_IZQUIERDA, true, 100);
	public final Animacion ANIMACION_ESTANDAR_ARRIBA = new Animacion(
			Globales.LISTA_HOJAS_SPRITES.BOLA_FUEGO.ESTANDAR_ARRIBA, true, 100);
	public final Animacion ANIMACION_ESTANDAR_ABAJO = new Animacion(
			Globales.LISTA_HOJAS_SPRITES.BOLA_FUEGO.ESTANDAR_ABAJO, true, 100);

	public final Animacion ANIMACION_EXPLOSION = new Animacion(
			Globales.LISTA_HOJAS_SPRITES.BOLA_FUEGO.EXPLOSION, false, 100);

	public AnimacionesBolaFuego() {

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
