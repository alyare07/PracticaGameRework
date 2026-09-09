package principal.animaciones.criaturas;

import java.awt.Graphics2D;

import principal.animaciones.Animacion;
import principal.animaciones.AnimacionDireccionada;
import principal.entes.criaturas.Criatura.Direccion;
import principal.recursos.ClaveHoja;
import principal.utilidades.Globales;
import principal.utilidades.HojaSprite;

/**
 * Gestor de animaciones para NPCs y Comerciantes humanos (Zero-GC / O(1)).
 * Basado en la hoja de sprites de 32x32 de ClaveHoja.CHARACTER_2.
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class AnimacionesComerciante {

	public static final int ESTANDAR = 0;
	public static final int CAMINANDO = 1;
	public static final int TOTAL_ANIMACIONES = 2;

	private final AnimacionDireccionada[] animaciones = new AnimacionDireccionada[TOTAL_ANIMACIONES];
	private final int TIEMPO_MS_POR_FRAME = 150;

	public AnimacionesComerciante() {
		final HojaSprite hojaNormal = Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.CHARACTER_2);
		final HojaSprite hojaVolteada = Globales.GESTOR_TEXTURAS.getHojaVolteadaH(ClaveHoja.CHARACTER_2);
		final int framesPorFila = 4;

		// 1. ESTÁNDAR / REPOSO (Filas 0, 1, 2)
		this.animaciones[ESTANDAR] = new AnimacionDireccionada(
				new Animacion(hojaNormal.recortarRango(4, framesPorFila), true, this.TIEMPO_MS_POR_FRAME), // Norte
				new Animacion(hojaNormal.recortarRango(0, framesPorFila), true, this.TIEMPO_MS_POR_FRAME), // Sur
				new Animacion(hojaNormal.recortarRango(8, framesPorFila), true, this.TIEMPO_MS_POR_FRAME), // Este
				new Animacion(hojaVolteada.recortarRango(8, framesPorFila), true, this.TIEMPO_MS_POR_FRAME) // Oeste
																											// (Volteado)
		);

		// 2. CAMINANDO (Filas 3, 4, 5)
		this.animaciones[CAMINANDO] = new AnimacionDireccionada(
				new Animacion(hojaNormal.recortarRango(16, framesPorFila), true, this.TIEMPO_MS_POR_FRAME - 50),
				new Animacion(hojaNormal.recortarRango(12, framesPorFila), true, this.TIEMPO_MS_POR_FRAME - 50),
				new Animacion(hojaNormal.recortarRango(20, framesPorFila), true, this.TIEMPO_MS_POR_FRAME - 50),
				new Animacion(hojaVolteada.recortarRango(20, framesPorFila), true, this.TIEMPO_MS_POR_FRAME - 50));
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
}