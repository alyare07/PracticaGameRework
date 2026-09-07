package principal.interaccion;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import principal.utilidades.Globales;
import principal.utilidades.Render2D;

/**
 * Componente visual que dibuja la insignia interactiva [E] sobre la entidad enfocada.
 * 
 * @version 1.0 (Vanilla Java 8 - Zero-GC)
 */
public class PromptInteraccion {

	private static final Color COLOR_FONDO = new Color(15, 18, 26, 230);
	private static final Color COLOR_BORDE = new Color(220, 180, 50); // Oro
	private static final Color COLOR_TECLA = new Color(255, 215, 80);
	private static final Color COLOR_TEXTO = Color.WHITE;

	private static final int PADDING_H = 4;
	private static final int ALTO_BADGE = 13;

	public void pintar(final Graphics2D g, final Interactuable objetivo) {
		if (objetivo == null) {
			return;
		}

		final String accion = objetivo.getTextoPrompt();
		if (accion == null || accion.isEmpty()) {
			return;
		}

		final Font fontPrevia = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 12f));

		final String textoCompleto = "[E] " + accion;
		final int anchoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, textoCompleto);
		final int anchoBadge = anchoTexto + (PADDING_H * 2);

		final int xMundo = objetivo.getCentroX() - (anchoBadge / 2);
		final int yMundo = objetivo.getPosicionYInt() - ALTO_BADGE - 4;

		// 1. Fondo y marco en relieve en coordenadas de cámara
		Render2D.dibujarRectanguloRellenoRefCamara(g, xMundo, yMundo, anchoBadge, ALTO_BADGE, COLOR_FONDO);
		Render2D.dibujarRectanguloContornoRefCamara(g, xMundo, yMundo, anchoBadge, ALTO_BADGE, COLOR_BORDE);

		// 2. Texto con [E] dorado
		Render2D.dibujarStringConSombraRefCamara(g, "[E] ", xMundo + PADDING_H, (yMundo + ALTO_BADGE) - 3, COLOR_TECLA, Color.BLACK);

		final int offsetE = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, "[E] ");
		Render2D.dibujarStringConSombraRefCamara(g, accion, xMundo + PADDING_H + offsetE, (yMundo + ALTO_BADGE) - 3, COLOR_TEXTO, Color.BLACK);

		g.setFont(fontPrevia);
	}
}