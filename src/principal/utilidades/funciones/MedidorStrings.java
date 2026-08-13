package principal.utilidades.funciones;

import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 * Clase de utilidad para la medición de dimensiones en píxeles de textos
 * ({@link String}) según la fuente ({@link java.awt.Font}) activa en el
 * contexto gráfico {@link Graphics2D}.
 */
public final class MedidorStrings {

	public MedidorStrings() {
	}

	/**
	 * Mide el ancho en píxeles del {@link String} solicitado. Se calcula teniendo
	 * en cuenta la fuente activa en el contexto {@link Graphics2D}.
	 *
	 * @param g Contexto gráfico {@link Graphics2D}.
	 * @param s El {@link String} a medir.
	 * @return Ancho en píxeles, o 0 si la cadena/contexto es nulo o vacío.
	 */
	public int medirAnchoPixeles(final Graphics2D g, final String s) {
		if ((g == null) || (s == null) || s.isEmpty()) {
			return 0;
		}
		final FontMetrics fm = g.getFontMetrics();
		return fm.stringWidth(s);
	}

	/**
	 * Mide el alto en píxeles de la fuente activa para el {@link String}
	 * solicitado. Acceso instantáneo $O(1)$ sin asignación de memoria.
	 *
	 * @param g Contexto gráfico {@link Graphics2D}.
	 * @param s El {@link String} a medir (opcional, para validación).
	 * @return Alto en píxeles de la línea de texto, o 0 si la cadena/contexto es
	 *         nulo o vacío.
	 */
	public int medirAltoPixeles(final Graphics2D g, final String s) {
		if ((g == null) || (s == null) || s.isEmpty()) {
			return 0;
		}
		final FontMetrics fm = g.getFontMetrics();
		return fm.getHeight();
	}

	/**
	 * Mide el alto en píxeles de la línea de texto según la fuente activa en el
	 * contexto gráfico.
	 *
	 * @param g Contexto gráfico {@link Graphics2D}.
	 * @return Alto en píxeles de la fuente actual.
	 */
	public int medirAltoPixeles(final Graphics2D g) {
		if (g == null) {
			return 0;
		}
		return g.getFontMetrics().getHeight();
	}
}