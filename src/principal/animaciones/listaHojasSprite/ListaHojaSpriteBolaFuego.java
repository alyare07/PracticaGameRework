package principal.animaciones.listaHojasSprite;

import java.awt.image.BufferedImage;

import principal.utilidades.Globales;
import principal.utilidades.HojaSprite;

public class ListaHojaSpriteBolaFuego {
	private final BufferedImage TEXTURA_BOLAFUEGO;
	private final BufferedImage TEXTURA_EXPLOSION;

	public final HojaSprite ESTANDAR_DERECHA;
	public final HojaSprite ESTANDAR_IZQUIERDA;
	public final HojaSprite ESTANDAR_ARRIBA;
	public final HojaSprite ESTANDAR_ABAJO;
	public final HojaSprite EXPLOSION;

	public ListaHojaSpriteBolaFuego() {
		this.TEXTURA_BOLAFUEGO = Globales.FUNCIONES.TEXTURAS_TOOLS
				.redimensionar(Globales.FUNCIONES.CARGADOR_RECURSOS
						.cargarImagenCompatibleTranslucida("/imagenes/sprites/firebolt.png"), 16 * 3, 16);
		this.TEXTURA_EXPLOSION = Globales.FUNCIONES.CARGADOR_RECURSOS
				.cargarImagenCompatibleTranslucida("/imagenes/sprites/firebolt_explosion.png");
		this.ESTANDAR_DERECHA = new HojaSprite(this.TEXTURA_BOLAFUEGO, 16, false);
		this.ESTANDAR_IZQUIERDA = new HojaSprite(
				Globales.FUNCIONES.TEXTURAS_TOOLS.voltearImagenH(this.TEXTURA_BOLAFUEGO), 16, false);
		this.ESTANDAR_ARRIBA = new HojaSprite(
				Globales.FUNCIONES.TEXTURAS_TOOLS.voltearImagen90GradosIzquierda(this.TEXTURA_BOLAFUEGO), 16,
				false);
		this.ESTANDAR_ABAJO = new HojaSprite(
				Globales.FUNCIONES.TEXTURAS_TOOLS.voltearImagen90GradosDerecha(this.TEXTURA_BOLAFUEGO), 16,
				false);
		this.EXPLOSION = new HojaSprite(this.TEXTURA_EXPLOSION, 32, false);

	}
}
