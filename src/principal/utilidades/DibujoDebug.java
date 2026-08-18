package principal.utilidades;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;

/**
 * Clase de utilidad para operaciones de dibujo y depuración gráfica.
 * <p>
 * <b>Optimizaciones de Rendimiento:</b>
 * <ul>
 * <li>Sustituye {@link java.awt.geom.Ellipse2D} por {@link Graphics2D#drawOval}
 * para eliminar asignaciones en el Heap.</li>
 * <li>Aplica operaciones primitivas directas manteniendo contadores de métricas
 * de renderizado.</li>
 * </ul>
 * </p>
 */
public final class DibujoDebug {

	private static int objetosDibujados = 0;

	private DibujoDebug() {
		// Clase de utilidad no instanciable
	}

	// =========================================================================
	// DIBUJO DIRECTO (PANTALLA / ABSOLUTO)
	// =========================================================================

	public static void dibujarFigura(final Graphics2D g2D, final Shape figura, final Color color) {
		objetosDibujados++;
		g2D.setColor(color);
		g2D.draw(figura);
	}

	public static void dibujarFiguraEllipse(final Graphics2D g, final Rectangle area, final Color color) {
		dibujarFiguraEllipse(g, area.x, area.y, area.width, area.height, color);
	}

	public static void dibujarFiguraEllipse(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto, final Color color) {
		objetosDibujados++;
		g.setColor(color);
		// Usar drawOval previene crear objetos Ellipse2D.Double en el Heap
		g.drawOval(x, y, ancho, alto);
	}

	public static void dibujarImagen(final Graphics2D g, final Image img, final int x, final int y) {
		objetosDibujados++;
		g.drawImage(img, x, y, null);
	}

	public static void dibujarImagen(final Graphics2D g, final BufferedImage img, final int x, final int y) {
		objetosDibujados++;
		g.drawImage(img, x, y, null);
	}

	public static void dibujarImagen(final Graphics2D g, final BufferedImage img, final Point p) {
		dibujarImagen(g, img, p.x, p.y);
	}

	public static void dibujarImagenConTransparencia(final Graphics2D g, final BufferedImage img, final int x,
			final int y, final float alpha) {
		objetosDibujados++;
		final Composite comOriginal = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		g.drawImage(img, x, y, null);
		g.setComposite(comOriginal);
	}

	public static void dibujarString(final Graphics2D g, final String s, final int x, final int y) {
		objetosDibujados++;
		g.drawString(s, x, y);
	}

	public static void dibujarString(final Graphics2D g, final String s, final Point p) {
		dibujarString(g, s, p.x, p.y);
	}

	public static void dibujarString(final Graphics2D g, final String s, final int x, final int y, final Color c) {
		objetosDibujados++;
		g.setColor(c);
		g.drawString(s, x, y);
	}

	public static void dibujarString(final Graphics2D g, final String s, final Point p, final Color c) {
		dibujarString(g, s, p.x, p.y, c);
	}

	public static void dibujarRectanguloRelleno(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto) {
		objetosDibujados++;
		g.fillRect(x, y, ancho, alto);
	}

	public static void dibujarRectanguloRelleno(final Graphics2D g, final Rectangle r) {
		dibujarRectanguloRelleno(g, r.x, r.y, r.width, r.height);
	}

	public static void dibujarRectanguloRelleno(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto, final Color c) {
		objetosDibujados++;
		g.setColor(c);
		g.fillRect(x, y, ancho, alto);
	}

	public static void dibujarRectanguloRelleno(final Graphics2D g, final Rectangle r, final Color c) {
		dibujarRectanguloRelleno(g, r.x, r.y, r.width, r.height, c);
	}

	public static void dibujarRectanguloContorno(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto) {
		objetosDibujados++;
		g.drawRect(x, y, ancho, alto);
	}

	public static void dibujarRectanguloContorno(final Graphics2D g, final Rectangle r) {
		dibujarRectanguloContorno(g, r.x, r.y, r.width, r.height);
	}

	public static void dibujarRectanguloContorno(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto, final Color c) {
		objetosDibujados++;
		g.setColor(c);
		g.drawRect(x, y, ancho, alto);
	}

	public static void dibujarRectanguloContorno(final Graphics2D g, final Rectangle r, final Color c) {
		dibujarRectanguloContorno(g, r.x, r.y, r.width, r.height, c);
	}

	public static void dibujarLinea(final Graphics2D g, final int x1, final int y1, final int x2, final int y2,
			final Color c) {
		objetosDibujados++;
		g.setColor(c);
		g.drawLine(x1, y1, x2, y2);
	}

	public static void dibujarLinea(final Graphics2D g, final int x1, final int y1, final int x2, final int y2) {
		objetosDibujados++;
		g.drawLine(x1, y1, x2, y2);
	}

	public static void dibujarLinea(final Graphics2D g, final Point p1, final Point p2, final Color c) {
		dibujarLinea(g, p1.x, p1.y, p2.x, p2.y, c);
	}

	public static void dibujarLinea(final Graphics2D g, final Point p1, final Point p2) {
		dibujarLinea(g, p1.x, p1.y, p2.x, p2.y);
	}

	// =========================================================================
	// DIBUJO CON REFERENCIA A CÁMARA (MUNDO / RELATIVO)
	// =========================================================================

	public static void dibujarFiguraEllipseRefCamara(final Graphics2D g, final Rectangle area, final Color color) {
		dibujarFiguraEllipseRefCamara(g, area.x, area.y, area.width, area.height, color);
	}

	public static void dibujarFiguraEllipseRefCamara(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto, final Color color) {
		objetosDibujados++;
		g.setColor(color);
		g.drawOval(Globales.getXDesplazamientoCamara(x), Globales.getYDesplazamientoCamara(y), ancho,
				alto);
	}

	public static void dibujarImagenRefCamara(final Graphics2D g, final Image img, final int x, final int y) {
		objetosDibujados++;
		g.drawImage(img, Globales.getXDesplazamientoCamara(x), Globales.getYDesplazamientoCamara(y),
				null);
	}

	public static void dibujarImagenRefCamara(final Graphics2D g, final BufferedImage img, final int x, final int y) {
		objetosDibujados++;
		g.drawImage(img, Globales.getXDesplazamientoCamara(x), Globales.getYDesplazamientoCamara(y),
				null);
	}

	public static void dibujarImagenRefCamara(final Graphics2D g, final BufferedImage img, final Point p) {
		dibujarImagenRefCamara(g, img, p.x, p.y);
	}

	public static void dibujarImagenConTransparenciaRefCamara(final Graphics2D g, final BufferedImage img, final int x,
			final int y, final float alpha) {
		objetosDibujados++;
		final Composite comOriginal = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		g.drawImage(img, Globales.getXDesplazamientoCamara(x), Globales.getYDesplazamientoCamara(y),
				null);
		g.setComposite(comOriginal);
	}

	public static void dibujarStringRefCamara(final Graphics2D g, final String s, final int x, final int y) {
		objetosDibujados++;
		g.drawString(s, Globales.getXDesplazamientoCamara(x), Globales.getYDesplazamientoCamara(y));
	}

	public static void dibujarStringRefCamara(final Graphics2D g, final String s, final Point p) {
		dibujarStringRefCamara(g, s, p.x, p.y);
	}

	public static void dibujarStringRefCamara(final Graphics2D g, final String s, final int x, final int y,
			final Color c) {
		objetosDibujados++;
		g.setColor(c);
		g.drawString(s, Globales.getXDesplazamientoCamara(x), Globales.getYDesplazamientoCamara(y));
	}

	public static void dibujarStringRefCamara(final Graphics2D g, final String s, final Point p, final Color c) {
		dibujarStringRefCamara(g, s, p.x, p.y, c);
	}

	public static void dibujarRectanguloRellenoRefCamara(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto) {
		objetosDibujados++;
		g.fillRect(Globales.getXDesplazamientoCamara(x), Globales.getYDesplazamientoCamara(y), ancho,
				alto);
	}

	public static void dibujarRectanguloRellenoRefCamara(final Graphics2D g, final Rectangle r) {
		dibujarRectanguloRellenoRefCamara(g, r.x, r.y, r.width, r.height);
	}

	public static void dibujarRectanguloRellenoRefCamara(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto, final Color c) {
		objetosDibujados++;
		g.setColor(c);
		g.fillRect(Globales.getXDesplazamientoCamara(x), Globales.getYDesplazamientoCamara(y), ancho,
				alto);
	}

	public static void dibujarRectanguloRellenoRefCamara(final Graphics2D g, final Rectangle r, final Color c) {
		dibujarRectanguloRellenoRefCamara(g, r.x, r.y, r.width, r.height, c);
	}

	public static void dibujarRectanguloContornoRefCamara(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto) {
		objetosDibujados++;
		g.drawRect(Globales.getXDesplazamientoCamara(x), Globales.getYDesplazamientoCamara(y), ancho,
				alto);
	}

	public static void dibujarRectanguloContornoRefCamara(final Graphics2D g, final Rectangle r) {
		dibujarRectanguloContornoRefCamara(g, r.x, r.y, r.width, r.height);
	}

	public static void dibujarRectanguloContornoRefCamara(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto, final Color c) {
		objetosDibujados++;
		g.setColor(c);
		g.drawRect(Globales.getXDesplazamientoCamara(x), Globales.getYDesplazamientoCamara(y), ancho,
				alto);
	}

	public static void dibujarRectanguloContornoRefCamara(final Graphics2D g, final Rectangle r, final Color c) {
		dibujarRectanguloContornoRefCamara(g, r.x, r.y, r.width, r.height, c);
	}

	public static void dibujarLineaRefCamara(final Graphics2D g, final int x1, final int y1, final int x2, final int y2,
			final Color c) {
		objetosDibujados++;
		g.setColor(c);
		g.drawLine(Globales.getXDesplazamientoCamara(x1), Globales.getYDesplazamientoCamara(y1),
				Globales.getXDesplazamientoCamara(x2), Globales.getYDesplazamientoCamara(y2));
	}

	public static void dibujarLineaRefCamara(final Graphics2D g, final int x1, final int y1, final int x2,
			final int y2) {
		objetosDibujados++;
		g.drawLine(Globales.getXDesplazamientoCamara(x1), Globales.getYDesplazamientoCamara(y1),
				Globales.getXDesplazamientoCamara(x2), Globales.getYDesplazamientoCamara(y2));
	}

	public static void dibujarLineaRefCamara(final Graphics2D g, final Point p1, final Point p2, final Color c) {
		dibujarLineaRefCamara(g, p1.x, p1.y, p2.x, p2.y, c);
	}

	public static void dibujarLineaRefCamara(final Graphics2D g, final Point p1, final Point p2) {
		dibujarLineaRefCamara(g, p1.x, p1.y, p2.x, p2.y);
	}

	// =========================================================================
	// MÉTRICAS
	// =========================================================================

	public static int getContadorObjetos() {
		return objetosDibujados;
	}

	public static void reiniciarContadorObjetos() {
		objetosDibujados = 0;
	}
}