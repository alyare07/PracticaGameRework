package principal.utilidades;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;

/**
 * Biblioteca centralizada de renderizado 2D, proyección de cámara, deformación
 * eólica de vegetación y telemetría gráfica de alto rendimiento (Zero-GC).
 * 
 * @version 4.1 (Vanilla Java 8 - Zero-GC Transform Pipeline)
 */
public final class Render2D {

	// =========================================================================
	// === 1. TELEMETRÍA GRÁFICA (OBJETOS POR FRAME / OPF)
	// =========================================================================

	private static int objetosDibujados = 0;

	private static final AlphaComposite[] COMPOSITES_OPACIDAD = new AlphaComposite[101];
	static {
		for (int i = 0; i <= 100; i++) {
			COMPOSITES_OPACIDAD[i] = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, i / 100.0f);
		}
	}

	private static AlphaComposite obtenerComposite(final float alpha) {
		final int indice = Math.max(0, Math.min(100, Math.round(alpha * 100.0f)));
		return COMPOSITES_OPACIDAD[indice];
	}

	private Render2D() {
	}

	public static int getContadorObjetos() {
		return objetosDibujados;
	}

	public static void reiniciarContadorObjetos() {
		objetosDibujados = 0;
	}

	public static void registrarLlamadas(final int cantidad) {
		objetosDibujados += cantidad;
	}

	// =========================================================================
	// === 2. DIBUJO CON DEFORMACIÓN EÓLICA (ZERO-GC TRANSFORM INVERSION)
	// =========================================================================

	/**
	 * Dibuja un sprite de vegetación en coordenadas de mundo deformando su copa con
	 * el viento sin instanciar objetos AffineTransform en cada frame.
	 */
	public static void dibujarImagenConBalanceoRefCamara(final Graphics2D g, final Image img, final int x, final int y,
			final double fuerzaBalanceo) {
		if ((g == null) || (img == null)) {
			return;
		}

		objetosDibujados++;

		final int rx = Globales.getXDesplazamientoCamara(x);
		final int ry = Globales.getYDesplazamientoCamara(y);
		final int w = img.getWidth(null);
		final int h = img.getHeight(null);

		final int pivotX = rx + (w / 2);
		final int pivotY = ry + h;

		// Transformación directa e inversión simétrica (CERO creación de objetos en
		// Heap)
		g.translate(pivotX, pivotY);
		g.shear(fuerzaBalanceo, 0.0);
		g.drawImage(img, -(w / 2), -h, null);
		g.shear(-fuerzaBalanceo, 0.0);
		g.translate(-pivotX, -pivotY);
	}

	public static void dibujarImagenConBalanceo(final Graphics2D g, final Image img, final int x, final int y,
			final double fuerzaBalanceo) {
		if ((g == null) || (img == null)) {
			return;
		}

		objetosDibujados++;

		final int w = img.getWidth(null);
		final int h = img.getHeight(null);
		final int pivotX = x + (w / 2);
		final int pivotY = y + h;

		g.translate(pivotX, pivotY);
		g.shear(fuerzaBalanceo, 0.0);
		g.drawImage(img, -(w / 2), -h, null);
		g.shear(-fuerzaBalanceo, 0.0);
		g.translate(-pivotX, -pivotY);
	}

	// =========================================================================
	// === 3. DIBUJO DIRECTO / ESPACIO DE PANTALLA (HUD 1:1)
	// =========================================================================

	public static void dibujarFigura(final Graphics2D g2D, final Shape figura, final Color color) {
		if ((g2D == null) || (figura == null)) {
			return;
		}
		objetosDibujados++;
		g2D.setColor(color);
		g2D.draw(figura);
	}

	public static void dibujarFiguraEllipse(final Graphics2D g, final Rectangle area, final Color color) {
		if (area == null) {
			return;
		}
		dibujarFiguraEllipse(g, area.x, area.y, area.width, area.height, color);
	}

	public static void dibujarFiguraEllipse(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto, final Color color) {
		if (g == null) {
			return;
		}
		objetosDibujados++;
		g.setColor(color);
		g.drawOval(x, y, ancho, alto);
	}

	public static void dibujarImagen(final Graphics2D g, final Image img, final int x, final int y) {
		if ((g == null) || (img == null)) {
			return;
		}
		objetosDibujados++;
		g.drawImage(img, x, y, null);
	}

	public static void dibujarImagen(final Graphics2D g, final BufferedImage img, final int x, final int y) {
		if ((g == null) || (img == null)) {
			return;
		}
		objetosDibujados++;
		g.drawImage(img, x, y, null);
	}

	public static void dibujarImagen(final Graphics2D g, final BufferedImage img, final Point p) {
		if ((img == null) || (p == null)) {
			return;
		}
		dibujarImagen(g, img, p.x, p.y);
	}

	public static void dibujarImagenConTransparencia(final Graphics2D g, final BufferedImage img, final int x,
			final int y, final float alpha) {
		if ((g == null) || (img == null)) {
			return;
		}
		objetosDibujados++;
		final Composite comOriginal = g.getComposite();
		g.setComposite(obtenerComposite(alpha));
		g.drawImage(img, x, y, null);
		g.setComposite(comOriginal);
	}

	public static void dibujarString(final Graphics2D g, final String s, final int x, final int y) {
		if ((g == null) || (s == null)) {
			return;
		}
		objetosDibujados++;
		g.drawString(s, x, y);
	}

	public static void dibujarString(final Graphics2D g, final String s, final Point p) {
		if ((s == null) || (p == null)) {
			return;
		}
		dibujarString(g, s, p.x, p.y);
	}

	public static void dibujarString(final Graphics2D g, final String s, final int x, final int y, final Color c) {
		if ((g == null) || (s == null)) {
			return;
		}
		objetosDibujados++;
		g.setColor(c);
		g.drawString(s, x, y);
	}

	public static void dibujarString(final Graphics2D g, final String s, final int x, final int y, final Color c,
			final float tamanofuente, final boolean bold) {
		if ((g == null) || (s == null)) {
			return;
		}

		if (bold) {
			g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, tamanofuente));
		} else {
			g.setFont(Globales.GESTOR_FUENTES.getFuente(tamanofuente));
		}
		objetosDibujados++;
		g.setColor(c);
		g.drawString(s, x, y);
	}

	public static void dibujarString(final Graphics2D g, final String s, final Point p, final Color c) {
		if ((s == null) || (p == null)) {
			return;
		}
		dibujarString(g, s, p.x, p.y, c);
	}

	public static void dibujarStringConSombra(final Graphics2D g, final String s, final int x, final int y,
			final Color c, final Color sombra) {
		if ((g == null) || (s == null)) {
			return;
		}
		dibujarString(g, s, x + 1, y + 1, sombra);
		dibujarString(g, s, x, y, c);
	}

	public static void dibujarStringConSombra(final Graphics2D g, final String s, final int x, final int y,
			final Color c, final Color sombra, final float tamanoFuente) {
		if ((g == null) || (s == null)) {
			return;
		}
		g.setFont(Globales.GESTOR_FUENTES.getFuente(tamanoFuente));
		dibujarString(g, s, x + 1, y + 1, sombra);
		dibujarString(g, s, x, y, c);
	}

	public static void dibujarStringConSombra(final Graphics2D g, final String s, final int x, final int y,
			final Color c, final Color sombra, final float tamanoFuente, final boolean bold) {
		if ((g == null) || (s == null)) {
			return;
		}
		if (bold) {
			g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, tamanoFuente));
		} else {
			g.setFont(Globales.GESTOR_FUENTES.getFuente(tamanoFuente));
		}
		dibujarString(g, s, x + 1, y + 1, sombra);
		dibujarString(g, s, x, y, c);
	}

	public static void dibujarRectanguloRelleno(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto) {
		if (g == null) {
			return;
		}
		objetosDibujados++;
		g.fillRect(x, y, ancho, alto);
	}

	public static void dibujarRectanguloRelleno(final Graphics2D g, final Rectangle r) {
		if ((g == null) || (r == null)) {
			return;
		}
		dibujarRectanguloRelleno(g, r.x, r.y, r.width, r.height);
	}

	public static void dibujarRectanguloRelleno(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto, final Color c) {
		if (g == null) {
			return;
		}
		objetosDibujados++;
		g.setColor(c);
		g.fillRect(x, y, ancho, alto);
	}

	public static void dibujarRectanguloRelleno(final Graphics2D g, final Rectangle r, final Color c) {
		if ((g == null) || (r == null)) {
			return;
		}
		dibujarRectanguloRelleno(g, r.x, r.y, r.width, r.height, c);
	}

	public static void dibujarRectanguloContorno(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto) {
		if (g == null) {
			return;
		}
		objetosDibujados++;
		g.drawRect(x, y, ancho, alto);
	}

	public static void dibujarRectanguloContorno(final Graphics2D g, final Rectangle r) {
		if ((g == null) || (r == null)) {
			return;
		}
		dibujarRectanguloContorno(g, r.x, r.y, r.width, r.height);
	}

	public static void dibujarRectanguloContorno(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto, final Color c) {
		if (g == null) {
			return;
		}
		objetosDibujados++;
		g.setColor(c);
		g.drawRect(x, y, ancho, alto);
	}

	public static void dibujarRectanguloContorno(final Graphics2D g, final Rectangle r, final Color c) {
		if ((g == null) || (r == null)) {
			return;
		}
		dibujarRectanguloContorno(g, r.x, r.y, r.width, r.height, c);
	}

	public static void dibujarLinea(final Graphics2D g, final int x1, final int y1, final int x2, final int y2,
			final Color c) {
		if (g == null) {
			return;
		}
		objetosDibujados++;
		g.setColor(c);
		g.drawLine(x1, y1, x2, y2);
	}

	public static void dibujarLinea(final Graphics2D g, final int x1, final int y1, final int x2, final int y2) {
		if (g == null) {
			return;
		}
		objetosDibujados++;
		g.drawLine(x1, y1, x2, y2);
	}

	public static void dibujarLinea(final Graphics2D g, final Point p1, final Point p2, final Color c) {
		if ((p1 == null) || (p2 == null)) {
			return;
		}
		dibujarLinea(g, p1.x, p1.y, p2.x, p2.y, c);
	}

	public static void dibujarLinea(final Graphics2D g, final Point p1, final Point p2) {
		if ((p1 == null) || (p2 == null)) {
			return;
		}
		dibujarLinea(g, p1.x, p1.y, p2.x, p2.y);
	}

	// =========================================================================
	// === 4. DIBUJO CON REFERENCIA A CÁMARA / ESPACIO DE MUNDO (RELATIVO)
	// =========================================================================

	public static void dibujarFiguraEllipseRefCamara(final Graphics2D g, final Rectangle area, final Color color) {
		if (area == null) {
			return;
		}
		dibujarFiguraEllipseRefCamara(g, area.x, area.y, area.width, area.height, color);
	}

	public static void dibujarFiguraEllipseRefCamara(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto, final Color color) {
		if (g == null) {
			return;
		}
		objetosDibujados++;
		g.setColor(color);
		g.drawOval(Globales.getXDesplazamientoCamara(x), Globales.getYDesplazamientoCamara(y), ancho, alto);
	}

	public static void dibujarImagenRefCamara(final Graphics2D g, final Image img, final int x, final int y) {
		if ((g == null) || (img == null)) {
			return;
		}
		objetosDibujados++;
		g.drawImage(img, Globales.getXDesplazamientoCamara(x), Globales.getYDesplazamientoCamara(y), null);
	}

	public static void dibujarImagenRefCamara(final Graphics2D g, final BufferedImage img, final int x, final int y) {
		if ((g == null) || (img == null)) {
			return;
		}
		objetosDibujados++;
		g.drawImage(img, Globales.getXDesplazamientoCamara(x), Globales.getYDesplazamientoCamara(y), null);
	}

	public static void dibujarImagenRefCamara(final Graphics2D g, final BufferedImage img, final Point p) {
		if ((img == null) || (p == null)) {
			return;
		}
		dibujarImagenRefCamara(g, img, p.x, p.y);
	}

	public static void dibujarImagenConTransparenciaRefCamara(final Graphics2D g, final BufferedImage img, final int x,
			final int y, final float alpha) {
		if ((g == null) || (img == null)) {
			return;
		}
		objetosDibujados++;
		final Composite comOriginal = g.getComposite();
		g.setComposite(obtenerComposite(alpha));
		g.drawImage(img, Globales.getXDesplazamientoCamara(x), Globales.getYDesplazamientoCamara(y), null);
		g.setComposite(comOriginal);
	}

	public static void dibujarStringRefCamara(final Graphics2D g, final String s, final int x, final int y) {
		if ((g == null) || (s == null)) {
			return;
		}
		objetosDibujados++;
		g.drawString(s, Globales.getXDesplazamientoCamara(x), Globales.getYDesplazamientoCamara(y));
	}

	public static void dibujarStringRefCamara(final Graphics2D g, final String s, final Point p) {
		if ((s == null) || (p == null)) {
			return;
		}
		dibujarStringRefCamara(g, s, p.x, p.y);
	}

	public static void dibujarStringRefCamara(final Graphics2D g, final String s, final int x, final int y,
			final Color c) {
		if ((g == null) || (s == null)) {
			return;
		}
		objetosDibujados++;
		g.setColor(c);
		g.drawString(s, Globales.getXDesplazamientoCamara(x), Globales.getYDesplazamientoCamara(y));
	}

	public static void dibujarStringRefCamara(final Graphics2D g, final String s, final Point p, final Color c) {
		if ((s == null) || (p == null)) {
			return;
		}
		dibujarStringRefCamara(g, s, p.x, p.y, c);
	}

	public static void dibujarStringRefCamara(final Graphics2D g, final String s, final int x, final int y,
			final Color c, final float tamanofuente, final boolean bold) {
		if (s == null) {
			return;
		}
		if (bold) {
			g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, tamanofuente));
		} else {
			g.setFont(Globales.GESTOR_FUENTES.getFuente(tamanofuente));
		}
		objetosDibujados++;
		g.setColor(c);
		g.drawString(s, Globales.getXDesplazamientoCamara(x), Globales.getYDesplazamientoCamara(y));
	}

	public static void dibujarStringConSombraRefCamara(final Graphics2D g, final String s, final int x, final int y,
			final Color c, final Color sombra) {
		if ((g == null) || (s == null)) {
			return;
		}
		final int renderX = Globales.getXDesplazamientoCamara(x);
		final int renderY = Globales.getYDesplazamientoCamara(y);
		dibujarString(g, s, renderX + 1, renderY + 1, sombra);
		dibujarString(g, s, renderX, renderY, c);
	}

	public static void dibujarStringConSombraRefCamara(final Graphics2D g, final String s, final int x, final int y,
			final Color c, final Color sombra, final float tamano, final boolean bold) {
		if ((g == null) || (s == null)) {
			return;
		}
		if (bold) {
			g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, tamano));
		} else {
			g.setFont(Globales.GESTOR_FUENTES.getFuente(tamano));
		}
		final int renderX = Globales.getXDesplazamientoCamara(x);
		final int renderY = Globales.getYDesplazamientoCamara(y);
		dibujarString(g, s, renderX + 1, renderY + 1, sombra);
		dibujarString(g, s, renderX, renderY, c);
	}

	public static void dibujarRectanguloRellenoRefCamara(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto) {
		if (g == null) {
			return;
		}
		objetosDibujados++;
		g.fillRect(Globales.getXDesplazamientoCamara(x), Globales.getYDesplazamientoCamara(y), ancho, alto);
	}

	public static void dibujarRectanguloRellenoRefCamara(final Graphics2D g, final Rectangle r) {
		if ((g == null) || (r == null)) {
			return;
		}
		dibujarRectanguloRellenoRefCamara(g, r.x, r.y, r.width, r.height);
	}

	public static void dibujarRectanguloRellenoRefCamara(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto, final Color c) {
		if (g == null) {
			return;
		}
		objetosDibujados++;
		g.setColor(c);
		g.fillRect(Globales.getXDesplazamientoCamara(x), Globales.getYDesplazamientoCamara(y), ancho, alto);
	}

	public static void dibujarRectanguloRellenoRefCamara(final Graphics2D g, final Rectangle r, final Color c) {
		if ((g == null) || (r == null)) {
			return;
		}
		dibujarRectanguloRellenoRefCamara(g, r.x, r.y, r.width, r.height, c);
	}

	public static void dibujarRectanguloContornoRefCamara(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto) {
		if (g == null) {
			return;
		}
		objetosDibujados++;
		g.drawRect(Globales.getXDesplazamientoCamara(x), Globales.getYDesplazamientoCamara(y), ancho, alto);
	}

	public static void dibujarRectanguloContornoRefCamara(final Graphics2D g, final Rectangle r) {
		if ((g == null) || (r == null)) {
			return;
		}
		dibujarRectanguloContornoRefCamara(g, r.x, r.y, r.width, r.height);
	}

	public static void dibujarRectanguloContornoRefCamara(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto, final Color c) {
		if (g == null) {
			return;
		}
		objetosDibujados++;
		g.setColor(c);
		g.drawRect(Globales.getXDesplazamientoCamara(x), Globales.getYDesplazamientoCamara(y), ancho, alto);
	}

	public static void dibujarRectanguloContornoRefCamara(final Graphics2D g, final Rectangle r, final Color c) {
		if ((g == null) || (r == null)) {
			return;
		}
		dibujarRectanguloContornoRefCamara(g, r.x, r.y, r.width, r.height, c);
	}

	public static void dibujarLineaRefCamara(final Graphics2D g, final int x1, final int y1, final int x2, final int y2,
			final Color c) {
		if (g == null) {
			return;
		}
		objetosDibujados++;
		g.setColor(c);
		g.drawLine(Globales.getXDesplazamientoCamara(x1), Globales.getYDesplazamientoCamara(y1),
				Globales.getXDesplazamientoCamara(x2), Globales.getYDesplazamientoCamara(y2));
	}

	public static void dibujarLineaRefCamara(final Graphics2D g, final int x1, final int y1, final int x2,
			final int y2) {
		if (g == null) {
			return;
		}
		objetosDibujados++;
		g.drawLine(Globales.getXDesplazamientoCamara(x1), Globales.getYDesplazamientoCamara(y1),
				Globales.getXDesplazamientoCamara(x2), Globales.getYDesplazamientoCamara(y2));
	}

	public static void dibujarLineaRefCamara(final Graphics2D g, final Point p1, final Point p2, final Color c) {
		if ((p1 == null) || (p2 == null)) {
			return;
		}
		dibujarLineaRefCamara(g, p1.x, p1.y, p2.x, p2.y, c);
	}

	public static void dibujarLineaRefCamara(final Graphics2D g, final Point p1, final Point p2) {
		if ((p1 == null) || (p2 == null)) {
			return;
		}
		dibujarLineaRefCamara(g, p1.x, p1.y, p2.x, p2.y);
	}
}