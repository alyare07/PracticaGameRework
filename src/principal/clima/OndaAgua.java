package principal.clima;

import java.awt.Color;
import java.awt.Graphics2D;

import principal.utilidades.Render2D;

/**
 * Representa un anillo u onda de agua elíptica en el suelo (Zero-GC).
 * <p>
 * Se proyecta en perspectiva 2.5D (elipse con relación 2:1) y se expande
 * progresivamente hasta desvanecerse.
 * </p>
 * 
 * @version 1.0
 */
public class OndaAgua {

	// =========================================================================
	// === 1. ESTADO FÍSICO Y GEOMETRÍA
	// =========================================================================

	private boolean activa;

	/** Coordenada X central en píxeles absolutos de mundo. */
	private double posX;

	/** Coordenada Y central en píxeles absolutos de mundo. */
	private double posY;

	private double radioActual;
	private double radioMaximo;

	private double duracionSegundos;
	private double vidaRestante;

	private Color colorOnda;

	// =========================================================================
	// === CONSTRUCTOR
	// =========================================================================

	public OndaAgua() {
		this.activa = false;
	}

	// =========================================================================
	// === SPAWN Y CICLO DE VIDA (ZERO-GC)
	// =========================================================================

	/**
	 * Activa y reinicia una onda de agua en el suelo.
	 *
	 * @param x           Coordenada X de mundo.
	 * @param y           Coordenada Y de mundo.
	 * @param radioMaximo Radio horizontal final al expandirse (en px).
	 * @param duracionSeg Tiempo en segundos que tarda en disiparse (ej: 0.45s).
	 * @param color       Tinte del agua/líquido.
	 */
	public void spawn(final double x, final double y, final double radioMaximo, final double duracionSeg,
			final Color color) {
		this.posX = x;
		this.posY = y;
		this.radioActual = 1.0;
		this.radioMaximo = Math.max(3.0, radioMaximo);
		this.duracionSegundos = Math.max(0.1, duracionSeg);
		this.vidaRestante = this.duracionSegundos;
		this.colorOnda = (color != null) ? color : Color.WHITE;
		this.activa = true;
	}

	public void actualizar(final double dt) {
		if (!this.activa) {
			return;
		}

		this.vidaRestante -= dt;
		if (this.vidaRestante <= 0.0) {
			this.activa = false;
			return;
		}

		// Progreso normalizado de expansión (0.0 = inicio, 1.0 = fin)
		final double progreso = 1.0 - (this.vidaRestante / this.duracionSegundos);

		// Expansión con curva suave cúbica
		final double factorExpansion = 1.0 - Math.pow(1.0 - progreso, 2.0);
		this.radioActual = 1.0 + ((this.radioMaximo - 1.0) * factorExpansion);
	}

	/**
	 * Dibuja el contorno de la onda proyectada con perspectiva 2.5D en el suelo.
	 */
	public void pintar(final Graphics2D g) {
		if (!this.activa) {
			return;
		}

		final int ancho = (int) Math.round(this.radioActual * 2.0);
		final int alto = Math.max(2, (int) Math.round(this.radioActual)); // Perspectiva 2:1 achatada

		final int renderX = (int) Math.round(this.posX) - (ancho / 2);
		final int renderY = (int) Math.round(this.posY) - (alto / 2);

		Render2D.dibujarFiguraEllipseRefCamara(g, renderX, renderY, ancho, alto, this.colorOnda);
	}

	// =========================================================================
	// === GETTERS Y SETTERS
	// =========================================================================

	public boolean isActiva() {
		return this.activa;
	}

	public void desactivar() {
		this.activa = false;
	}
}