package principal.igu.textos;

import java.awt.Color;

/**
 * Catálogo de estilos visuales predefinidos para los textos flotantes de combate.
 */
public enum TipoTextoFlotante {

	/** Daño estándar recibido o infligido (Blanco nítido). */
	DANIO_NORMAL(new Color(255, 255, 255), 9f, false, 900),

	/** Impacto crítico de alta potencia (Rojo vibrante y tamaño aumentado). */
	CRITICO(new Color(255, 60, 60), 13f, true, 1200),

	/** Regeneración de puntos de salud / Pociones (Verde esmeralda). */
	CURACION(new Color(60, 255, 90), 10f, false, 1000),

	/** Restauración de energía / Maná (Azul celeste). */
	MANA(new Color(60, 200, 255), 9f, false, 900),

	/** Estados alterados: "¡STUN!", "¡FALLO!", "¡VENENO!" (Amarillo anaranjado). */
	ESTADO(new Color(255, 190, 40), 9f, true, 1100),

	/** Recompensas de botín o experiencia (Dorado brillante). */
	ORO_EXP(new Color(255, 225, 30), 9f, false, 1000);

	private final Color color;
	private final float tamanoFuente;
	private final boolean esCritico;
	private final int duracionMs;

	TipoTextoFlotante(final Color color, final float tamanoFuente, final boolean esCritico, final int duracionMs) {
		this.color = color;
		this.tamanoFuente = tamanoFuente;
		this.esCritico = esCritico;
		this.duracionMs = duracionMs;
	}

	public Color getColor() {
		return this.color;
	}

	public float getTamanoFuente() {
		return this.tamanoFuente;
	}

	public boolean isCritico() {
		return this.esCritico;
	}

	public int getDuracionMs() {
		return this.duracionMs;
	}
}