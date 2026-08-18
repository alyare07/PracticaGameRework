package principal.animaciones.criaturas;

import java.awt.Graphics2D;
import java.util.HashMap;

import principal.animaciones.Animacion;
import principal.animaciones.AnimacionDireccionada;
import principal.entes.criaturas.Criatura.Direccion;
import principal.utilidades.Globales;

public class AnimacionesBandido {
	private final HashMap<String, AnimacionDireccionada> ANIMACIONES;

	public static final String ESTANDAR = "Estandar";
	public static final String CAMINANDO = "Caminando";
	public static final String GARROTE_ESTANDAR = "Garrote Estandar";
	public static final String GARROTE_CAMINANDO = "Garrote Caminando";
	public static final String GARROTE_ATACANDO = "Garrote Atacando";
	public static final String PISTOLA_ESTANDAR = "Pistola Estandar";
	public static final String PISTOLA_CAMINANDO = "Pistola Caminando";

	private final int TIEMPO_MS_POR_FRAME = 150;

	public AnimacionesBandido() {
		this.ANIMACIONES = new HashMap<String, AnimacionDireccionada>();
		this.ANIMACIONES.put(ESTANDAR,
				new AnimacionDireccionada(
						new Animacion(Globales.LISTA_HOJAS_SPRITES.BANDIDO.ESTANDAR_ARRIBA, true,
								this.TIEMPO_MS_POR_FRAME),
						new Animacion(Globales.LISTA_HOJAS_SPRITES.BANDIDO.ESTANDAR_ABAJO, true,
								this.TIEMPO_MS_POR_FRAME),
						new Animacion(Globales.LISTA_HOJAS_SPRITES.BANDIDO.ESTANDAR_DERECHA, true,
								this.TIEMPO_MS_POR_FRAME),
						new Animacion(Globales.LISTA_HOJAS_SPRITES.BANDIDO.ESTANDAR_IZQUIERDA, true,
								this.TIEMPO_MS_POR_FRAME)));
		this.ANIMACIONES.put(CAMINANDO,
				new AnimacionDireccionada(
						new Animacion(Globales.LISTA_HOJAS_SPRITES.BANDIDO.CAMINANDO_ARRIBA, true,
								this.TIEMPO_MS_POR_FRAME - 50),
						new Animacion(Globales.LISTA_HOJAS_SPRITES.BANDIDO.CAMINANDO_ABAJO, true,
								this.TIEMPO_MS_POR_FRAME - 50),
						new Animacion(Globales.LISTA_HOJAS_SPRITES.BANDIDO.CAMINANDO_DERECHA, true,
								this.TIEMPO_MS_POR_FRAME - 50),
						new Animacion(Globales.LISTA_HOJAS_SPRITES.BANDIDO.CAMINANDO_IZQUIERDA, true,
								this.TIEMPO_MS_POR_FRAME)));
		this.ANIMACIONES.put(PISTOLA_ESTANDAR,
				new AnimacionDireccionada(
						new Animacion(Globales.LISTA_HOJAS_SPRITES.BANDIDO.PISTOLA_ESTANDAR_ARRIBA, true,
								this.TIEMPO_MS_POR_FRAME),
						new Animacion(Globales.LISTA_HOJAS_SPRITES.BANDIDO.PISTOLA_ESTANDAR_ABAJO, true,
								this.TIEMPO_MS_POR_FRAME),
						new Animacion(Globales.LISTA_HOJAS_SPRITES.BANDIDO.PISTOLA_ESTANDAR_DERECHA, true,
								this.TIEMPO_MS_POR_FRAME),
						new Animacion(Globales.LISTA_HOJAS_SPRITES.BANDIDO.PISTOLA_ESTANDAR_IZQUIERDA, true,
								this.TIEMPO_MS_POR_FRAME)));
		this.ANIMACIONES.put(PISTOLA_CAMINANDO,
				new AnimacionDireccionada(
						new Animacion(Globales.LISTA_HOJAS_SPRITES.BANDIDO.PISTOLA_CAMINANDO_ARRIBA, true,
								this.TIEMPO_MS_POR_FRAME),
						new Animacion(Globales.LISTA_HOJAS_SPRITES.BANDIDO.PISTOLA_CAMINANDO_ABAJO, true,
								this.TIEMPO_MS_POR_FRAME),
						new Animacion(Globales.LISTA_HOJAS_SPRITES.BANDIDO.PISTOLA_CAMINANDO_DERECHA, true,
								this.TIEMPO_MS_POR_FRAME),
						new Animacion(Globales.LISTA_HOJAS_SPRITES.BANDIDO.PISTOLA_CAMINANDO_IZQUIERDA, true,
								this.TIEMPO_MS_POR_FRAME)));
		this.ANIMACIONES.put(GARROTE_ESTANDAR,
				new AnimacionDireccionada(
						new Animacion(Globales.LISTA_HOJAS_SPRITES.BANDIDO.GARROTE_ESTANDAR_ARRIBA, true,
								this.TIEMPO_MS_POR_FRAME),
						new Animacion(Globales.LISTA_HOJAS_SPRITES.BANDIDO.GARROTE_ESTANDAR_ABAJO, true,
								this.TIEMPO_MS_POR_FRAME),
						new Animacion(Globales.LISTA_HOJAS_SPRITES.BANDIDO.GARROTE_ESTANDAR_DERECHA, true,
								this.TIEMPO_MS_POR_FRAME),
						new Animacion(Globales.LISTA_HOJAS_SPRITES.BANDIDO.GARROTE_ESTANDAR_IZQUIERDA, true,
								this.TIEMPO_MS_POR_FRAME)));
		this.ANIMACIONES.put(GARROTE_CAMINANDO,
				new AnimacionDireccionada(
						new Animacion(Globales.LISTA_HOJAS_SPRITES.BANDIDO.GARROTE_CAMINANDO_ARRIBA, true,
								this.TIEMPO_MS_POR_FRAME),
						new Animacion(Globales.LISTA_HOJAS_SPRITES.BANDIDO.GARROTE_CAMINANDO_ABAJO, true,
								this.TIEMPO_MS_POR_FRAME),
						new Animacion(Globales.LISTA_HOJAS_SPRITES.BANDIDO.GARROTE_CAMINANDO_DERECHA, true,
								this.TIEMPO_MS_POR_FRAME),
						new Animacion(Globales.LISTA_HOJAS_SPRITES.BANDIDO.GARROTE_CAMINANDO_IZQUIERDA, true,
								this.TIEMPO_MS_POR_FRAME)));
		this.ANIMACIONES.put(GARROTE_ATACANDO,
				new AnimacionDireccionada(
						new Animacion(Globales.LISTA_HOJAS_SPRITES.BANDIDO.GARROTE_ATACANDO_ARRIBA, false,
								this.TIEMPO_MS_POR_FRAME - 75),
						new Animacion(Globales.LISTA_HOJAS_SPRITES.BANDIDO.GARROTE_ATACANDO_ABAJO, false,
								this.TIEMPO_MS_POR_FRAME - 75),
						new Animacion(Globales.LISTA_HOJAS_SPRITES.BANDIDO.GARROTE_ATACANDO_DERECHA, false,
								this.TIEMPO_MS_POR_FRAME - 75),
						new Animacion(Globales.LISTA_HOJAS_SPRITES.BANDIDO.GARROTE_ATACANDO_IZQUIERDA, false,
								this.TIEMPO_MS_POR_FRAME - 75, true)));
	}

	public void pintar(final Graphics2D g, final int x, final int y, final Direccion direccion, final String tipo,
			final boolean transparente, final boolean refCamara) {
		final float alpha = 0.5f;
		if (transparente) {
			this.ANIMACIONES.get(tipo).pintarConTransparencia(g, x, y, refCamara, alpha, direccion);
		} else {
			this.ANIMACIONES.get(tipo).pintar(g, x, y, refCamara, direccion);
		}
	}

	public Animacion getAnimacion(final String tipo, final Direccion direccion) {
		return this.ANIMACIONES.get(tipo).getAnimacion(direccion);
	}
}
