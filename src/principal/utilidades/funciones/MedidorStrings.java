package principal.utilidades.funciones;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
public class MedidorStrings {
	
	protected MedidorStrings() {
		
	}

	/**
	 * Mide el ancho en pixeles del {@link String} solicitado.
	 * Se calculara teniendo en cuenta el Font de la clase {@link Graphics2D}.
	 * @param g La clase {@link Graphics2D}
	 * @param s El {@link String} a calcular
	 * @return el valor del ancho en pixeles.
	 */
	public int medirAnchoPixeles(final Graphics2D g, final String s) {
		FontMetrics fm = g.getFontMetrics();
		return fm.stringWidth(s);
	}
	/**
	 * Mide el alto en pixeles del {@link String} solicitado.
	 * Se calculara teniendo en cuenta el Font de la clase {@link Graphics2D}.
	 * @param g La clase {@link Graphics2D}
	 * @param s El {@link String} a calcular
	 * @return el valor del alto en pixeles.
	 */
	public int medirAltoPixeles(final Graphics2D g, final String s) {
		FontMetrics fm = g.getFontMetrics();
		return (int) fm.getLineMetrics(s, g).getHeight();
	}
}
