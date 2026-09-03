package principal.animaciones.criaturas;

import java.awt.Graphics2D;

import principal.animaciones.Animacion;
import principal.animaciones.AnimacionDireccionada;
import principal.entes.criaturas.Criatura.Direccion;
import principal.recursos.ClaveHoja;
import principal.utilidades.Globales;
import principal.utilidades.HojaSprite;

public class AnimacionesBandido {

	// =========================================================================
	// ÍNDICES PRIMITIVOS PARA ACCESO O(1)
	// =========================================================================
	public static final int ESTANDAR = 0;
	public static final int CAMINANDO = 1;
	public static final int GARROTE_ESTANDAR = 2;
	public static final int GARROTE_CAMINANDO = 3;
	public static final int GARROTE_ATACANDO = 4;
	public static final int PISTOLA_ESTANDAR = 5;
	public static final int PISTOLA_CAMINANDO = 6;
	public static final int TOTAL_ANIMACIONES = 7;

	private final AnimacionDireccionada[] animaciones = new AnimacionDireccionada[TOTAL_ANIMACIONES];
	private final int TIEMPO_MS_POR_FRAME = 150;

	public AnimacionesBandido() {
		final HojaSprite hojaNormal = Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.BANDIDO);
		final HojaSprite hojaVolteada = Globales.GESTOR_TEXTURAS.getHojaVolteadaH(ClaveHoja.BANDIDO);
		final int frames = 4;

		// 1. ESTÁNDAR (Filas 0, 1, 2)
		this.animaciones[ESTANDAR] = new AnimacionDireccionada(
				new Animacion(hojaNormal.recortarRango(4, frames), true, this.TIEMPO_MS_POR_FRAME), // Norte
				new Animacion(hojaNormal.recortarRango(0, frames), true, this.TIEMPO_MS_POR_FRAME), // Sur
				new Animacion(hojaNormal.recortarRango(8, frames), true, this.TIEMPO_MS_POR_FRAME), // Este
				new Animacion(hojaVolteada.recortarRango(8, frames), true, this.TIEMPO_MS_POR_FRAME) // Oeste
		);

		// 2. CAMINANDO (Filas 3, 4, 5)
		this.animaciones[CAMINANDO] = new AnimacionDireccionada(
				new Animacion(hojaNormal.recortarRango(16, frames), true, this.TIEMPO_MS_POR_FRAME - 50),
				new Animacion(hojaNormal.recortarRango(12, frames), true, this.TIEMPO_MS_POR_FRAME - 50),
				new Animacion(hojaNormal.recortarRango(20, frames), true, this.TIEMPO_MS_POR_FRAME - 50),
				new Animacion(hojaVolteada.recortarRango(20, frames), true, this.TIEMPO_MS_POR_FRAME - 50));

		// 3. PISTOLA ESTÁNDAR (Filas 6, 7, 8)
		this.animaciones[PISTOLA_ESTANDAR] = new AnimacionDireccionada(
				new Animacion(hojaNormal.recortarRango(28, frames), true, this.TIEMPO_MS_POR_FRAME),
				new Animacion(hojaNormal.recortarRango(24, frames), true, this.TIEMPO_MS_POR_FRAME),
				new Animacion(hojaNormal.recortarRango(32, frames), true, this.TIEMPO_MS_POR_FRAME),
				new Animacion(hojaVolteada.recortarRango(32, frames), true, this.TIEMPO_MS_POR_FRAME));

		// 4. PISTOLA CAMINANDO (Filas 9, 10, 11)
		this.animaciones[PISTOLA_CAMINANDO] = new AnimacionDireccionada(
				new Animacion(hojaNormal.recortarRango(40, frames), true, this.TIEMPO_MS_POR_FRAME),
				new Animacion(hojaNormal.recortarRango(36, frames), true, this.TIEMPO_MS_POR_FRAME),
				new Animacion(hojaNormal.recortarRango(44, frames), true, this.TIEMPO_MS_POR_FRAME),
				new Animacion(hojaVolteada.recortarRango(44, frames), true, this.TIEMPO_MS_POR_FRAME));

		// 5. GARROTE ESTÁNDAR (Filas 12, 13, 14)
		this.animaciones[GARROTE_ESTANDAR] = new AnimacionDireccionada(
				new Animacion(hojaNormal.recortarRango(52, frames), true, this.TIEMPO_MS_POR_FRAME),
				new Animacion(hojaNormal.recortarRango(48, frames), true, this.TIEMPO_MS_POR_FRAME),
				new Animacion(hojaNormal.recortarRango(56, frames), true, this.TIEMPO_MS_POR_FRAME),
				new Animacion(hojaVolteada.recortarRango(56, frames), true, this.TIEMPO_MS_POR_FRAME));

		// 6. GARROTE CAMINANDO (Filas 15, 16, 17)
		this.animaciones[GARROTE_CAMINANDO] = new AnimacionDireccionada(
				new Animacion(hojaNormal.recortarRango(64, frames), true, this.TIEMPO_MS_POR_FRAME),
				new Animacion(hojaNormal.recortarRango(60, frames), true, this.TIEMPO_MS_POR_FRAME),
				new Animacion(hojaNormal.recortarRango(68, frames), true, this.TIEMPO_MS_POR_FRAME),
				new Animacion(hojaVolteada.recortarRango(68, frames), true, this.TIEMPO_MS_POR_FRAME));

		// 7. GARROTE ATACANDO (Filas 18, 19, 20)
		this.animaciones[GARROTE_ATACANDO] = new AnimacionDireccionada(
				new Animacion(hojaNormal.recortarRango(76, frames), false, this.TIEMPO_MS_POR_FRAME - 75), // Norte
				new Animacion(hojaNormal.recortarRango(72, frames), false, this.TIEMPO_MS_POR_FRAME - 75), // Sur
				new Animacion(hojaNormal.recortarRango(80, frames), false, this.TIEMPO_MS_POR_FRAME - 75), // Este
				new Animacion(hojaVolteada.recortarRango(80, frames), false, this.TIEMPO_MS_POR_FRAME - 75) // Oeste
																											// (Orden
																											// normal 0
																											// -> 3)
		);
	}

	public void actualizar(final Direccion direccion, final int tipo) {
		if ((tipo >= 0) && (tipo < TOTAL_ANIMACIONES)) {
			final AnimacionDireccionada animDir = this.animaciones[tipo];
			if (animDir != null) {
				animDir.actualizar(direccion);
			}
		}
	}

	public void pintar(final Graphics2D g, final int x, final int y, final Direccion direccion, final int tipo,
			final boolean transparente, final boolean refCamara) {
		this.pintar(g, x, y, direccion, tipo, transparente, refCamara, false);
	}

	public void pintar(final Graphics2D g, final int x, final int y, final Direccion direccion, final int tipo,
			final boolean transparente, final boolean refCamara, final boolean flash) {
		if ((tipo >= 0) && (tipo < TOTAL_ANIMACIONES)) {
			final AnimacionDireccionada animDir = this.animaciones[tipo];
			if (animDir != null) {
				if (transparente) {
					animDir.pintarConTransparencia(g, x, y, refCamara, 0.5f, direccion, flash);
				} else {
					animDir.pintar(g, x, y, refCamara, direccion, flash);
				}
			}
		}
	}

	public Animacion getAnimacion(final int tipo, final Direccion direccion) {
		if ((tipo >= 0) && (tipo < TOTAL_ANIMACIONES)) {
			final AnimacionDireccionada animDir = this.animaciones[tipo];
			return (animDir != null) ? animDir.getAnimacion(direccion) : null;
		}
		return null;
	}
}