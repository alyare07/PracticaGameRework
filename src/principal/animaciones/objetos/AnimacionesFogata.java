package principal.animaciones.objetos;

import java.awt.Graphics2D;

import principal.animaciones.Animacion;
import principal.recursos.ClaveHoja;
import principal.utilidades.Globales;
import principal.utilidades.HojaSprite;

/**
 * Gestor de animaciones y estados visuales para la Fogata (Zero-GC / O(1)).
 * Administra los cuatro estados de la spritesheet de 16x16:
 * - Fila 0: Fuego normal (repetitiva)
 * - Fila 1: Fuego azul (repetitiva)
 * - Fila 2: Humo de extinción (no repetitiva)
 * - Fila 3: Base apagada en frío (estática, 1 frame)
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class AnimacionesFogata {

	// =========================================================================
	// === 1. CONSTANTES DE ESTADO VISUAL
	// =========================================================================
	public static final int ENCENDIDA = 0;
	public static final int FUEGO_AZUL = 1;
	public static final int APAGADA_HUMO = 2;
	public static final int BASE = 3;
	public static final int TOTAL_ANIMACIONES = 4;

	// =========================================================================
	// === 2. CADENCIA DE ANIMACIÓN (MILISEGUNDOS POR FRAME)
	// =========================================================================
	private final int TIEMPO_MS_FUEGO = 120;
	private final int TIEMPO_MS_HUMO = 150;
	private final int TIEMPO_MS_BASE = 1000;

	// =========================================================================
	// === 3. CONTENEDOR PLANO ZERO-GC
	// =========================================================================
	private final Animacion[] animaciones = new Animacion[TOTAL_ANIMACIONES];

	public AnimacionesFogata() {
		final HojaSprite hoja = Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.FOGATA);

		if (hoja != null) {
			// Fila 0: Fogata Encendida Normal (Frames 0, 1, 2, 3) - Cíclica
			this.animaciones[ENCENDIDA] = new Animacion(hoja.recortarRango(0, 4), true, this.TIEMPO_MS_FUEGO);

			// Fila 1: Fogata Fuego Azul (Frames 4, 5, 6, 7) - Cíclica
			this.animaciones[FUEGO_AZUL] = new Animacion(hoja.recortarRango(4, 4), true, this.TIEMPO_MS_FUEGO);

			// Fila 2: Humo al apagarse (Frames 8, 9, 10, 11) - No repetitiva (1 ciclo)
			this.animaciones[APAGADA_HUMO] = new Animacion(hoja.recortarRango(8, 4), false, this.TIEMPO_MS_HUMO);

			// Fila 3: Base Apagada en frío (Frame 12) - Estática
			this.animaciones[BASE] = new Animacion(hoja.recortarRango(12, 1), true, this.TIEMPO_MS_BASE);
		}
	}

	// =========================================================================
	// === ACTUALIZACIÓN LÓGICA (60 APS)
	// =========================================================================

	public void actualizar(final int estado) {
		if ((estado >= 0) && (estado < TOTAL_ANIMACIONES)) {
			final Animacion anim = this.animaciones[estado];
			if (anim != null) {
				anim.actualizar();
			}
		}
	}

	// =========================================================================
	// === RENDERIZADO (60 FPS)
	// =========================================================================

	public void pintar(final Graphics2D g, final int x, final int y, final int estado, final boolean refCamara) {
		this.pintar(g, x, y, estado, false, refCamara, false);
	}

	public void pintar(final Graphics2D g, final int x, final int y, final int estado, final boolean transparente,
			final boolean refCamara, final boolean flash) {
		if ((estado >= 0) && (estado < TOTAL_ANIMACIONES)) {
			final Animacion anim = this.animaciones[estado];
			if (anim != null) {
				if (transparente) {
					anim.pintarConTransparencia(g, x, y, refCamara, 0.5f, flash);
				} else {
					anim.pintar(g, x, y, refCamara, flash);
				}
			}
		}
	}

	// =========================================================================
	// === CONTROL Y CONSULTA DE ESTADOS
	// =========================================================================

	public void reiniciar(final int estado) {
		if ((estado >= 0) && (estado < TOTAL_ANIMACIONES)) {
			final Animacion anim = this.animaciones[estado];
			if (anim != null) {
				anim.reiniciarAnimacion();
			}
		}
	}

	public boolean animacionFinalizada(final int estado) {
		if ((estado >= 0) && (estado < TOTAL_ANIMACIONES)) {
			final Animacion anim = this.animaciones[estado];
			if (anim != null) {
				return anim.animacionFinalizada();
			}
		}
		return true;
	}

	public Animacion getAnimacion(final int estado) {
		if ((estado >= 0) && (estado < TOTAL_ANIMACIONES)) {
			return this.animaciones[estado];
		}
		return null;
	}
}