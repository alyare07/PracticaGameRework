package principal.particulas;

import java.awt.Color;

/**
 * Catálogo de partículas con estética Pixel-Art definida, tamaños aumentados y
 * colores vibrantes.
 */
public enum TipoParticula {

	/**
	 * Chispas de fuego brillantes (Amarillo/Naranja, 6px a 1px, suben hacia
	 * arriba).
	 */
	FUEGO_CHISPA(new Color(255, 200, 0), new Color(255, 80, 0), 1.0, 7.0f, 1.0f, -50.0, 0.92),

	/** Humo denso de explosión (Gris claro a oscuro, 4px a 8px expandiéndose). */
	HUMO(new Color(210, 210, 210), new Color(90, 90, 90), 1.2, 4.0f, 8.0f, -30.0, 0.95),

	/**
	 * Gotas de sangre pesadas (Rojo carmesí puro, 5px con gravedad hacia el suelo).
	 */
	SANGRE(new Color(255, 20, 30), new Color(160, 0, 10), 0.9, 6.0f, 2.0f, 220.0, 0.85),

	/** Destellos mágicos (Cian eléctrico / Blanco brillante). */
	MAGIA(new Color(80, 240, 255), new Color(200, 255, 255), 1.1, 6.0f, 1.0f, -20.0, 0.90),

	/** Polvo de pisadas al correr o esquivar (Tierra visible de 5px). */
	POLVO_TIERRA(new Color(210, 180, 140), new Color(140, 110, 70), 0.7, 5.0f, 1.0f, -15.0, 0.82);

	private final Color colorInicio;
	private final Color colorFin;
	private final double duracionBaseSeg;
	private final float tamanoInicial;
	private final float tamanoFinal;
	private final double gravedad;
	private final double friccion;

	TipoParticula(final Color colorInicio, final Color colorFin, final double duracionBaseSeg,
			final float tamanoInicial, final float tamanoFinal, final double gravedad, final double friccion) {
		this.colorInicio = colorInicio;
		this.colorFin = colorFin;
		this.duracionBaseSeg = duracionBaseSeg;
		this.tamanoInicial = tamanoInicial;
		this.tamanoFinal = tamanoFinal;
		this.gravedad = gravedad;
		this.friccion = friccion;
	}

	public Color getColorInicio() {
		return this.colorInicio;
	}

	public Color getColorFin() {
		return this.colorFin;
	}

	public double getDuracionBaseSeg() {
		return this.duracionBaseSeg;
	}

	public float getTamanoInicial() {
		return this.tamanoInicial;
	}

	public float getTamanoFinal() {
		return this.tamanoFinal;
	}

	public double getGravedad() {
		return this.gravedad;
	}

	public double getFriccion() {
		return this.friccion;
	}
}