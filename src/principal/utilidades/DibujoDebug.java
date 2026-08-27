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
 * Biblioteca centralizada de dibujo 2D, depuración gráfica y métricas de
 * renderizado.
 * <p>
 * <b>Arquitectura de Coordenadas y Rendimiento:</b>
 * <ul>
 * <li><b>Separación Estricta de Capas:</b>
 * <ul>
 * <li><i>Métodos Directos:</i> Dibujan en coordenadas absolutas de pantalla
 * (Espacio HUD 1:1).</li>
 * <li><i>Métodos RefCamara:</i> Convierten automáticamente coordenadas del
 * mundo al espacio visible restando la posición de la cámara mediante
 * {@link Globales#getXDesplazamientoCamara(int)}.</li>
 * </ul>
 * </li>
 * <li><b>Cero Asignaciones en el Heap (Zero-GC):</b> Reemplaza clases
 * geométricas pesadas (como {@link java.awt.geom.Ellipse2D}) por primitivas
 * nativas de {@link Graphics2D} ({@code drawOval}, {@code fillRect}).</li>
 * <li><b>Métricas de Rendimiento en Tiempo Real (OPF):</b> Monitorea la
 * cantidad exacta de objetos y primitivas dibujadas en cada fotograma mediante
 * {@link #objetosDibujados}.</li>
 * </ul>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.5
 */
public final class DibujoDebug {

	/**
	 * Contador acumulativo de objetos y primitivas dibujadas en el frame actual
	 * (OPF).
	 */
	private static int objetosDibujados = 0;

	private DibujoDebug() {
		// Constructor privado para clase estática de utilidades
	}

	// =========================================================================
	// === 1. CONTROL DE MÉTRICAS (OBJETOS POR FRAME / OPF)
	// =========================================================================

	/**
	 * Retorna la cantidad total de llamadas de dibujo realizadas en el último
	 * frame.
	 *
	 * @return Contador de objetos dibujados (OPF).
	 */
	public static int getContadorObjetos() {
		return objetosDibujados;
	}

	/**
	 * Restablece el contador de métricas a cero. Invocado al inicio de cada frame
	 * en {@code SuperficieDibujo}.
	 */
	public static void reiniciarContadorObjetos() {
		objetosDibujados = 0;
	}

	// =========================================================================
	// === 2. DIBUJO DIRECTO / ESPACIO DE PANTALLA (HUD E INTERFAZ 1:1)
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
		g.drawOval(x, y, ancho, alto);
	}

	public static void dibujarImagen(final Graphics2D g, final Image img, final int x, final int y) {
		if (img == null) {
			return;
		}
		objetosDibujados++;
		g.drawImage(img, x, y, null);
	}

	public static void dibujarImagen(final Graphics2D g, final BufferedImage img, final int x, final int y) {
		if (img == null) {
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
		if (img == null) {
			return;
		}
		objetosDibujados++;
		final Composite comOriginal = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0.0f, Math.min(1.0f, alpha))));
		g.drawImage(img, x, y, null);
		g.setComposite(comOriginal);
	}

	public static void dibujarString(final Graphics2D g, final String s, final int x, final int y) {
		if (s == null) {
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
		if (s == null) {
			return;
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

	/**
	 * Dibuja un texto con sombra de alto contraste para máxima legibilidad en
	 * interfaces.
	 */
	public static void dibujarStringConSombra(final Graphics2D g, final String s, final int x, final int y,
			final Color c, final Color sombra) {
		if (s == null) {
			return;
		}
		dibujarString(g, s, x + 1, y + 1, sombra);
		dibujarString(g, s, x, y, c);
	}

	public static void dibujarRectanguloRelleno(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto) {
		objetosDibujados++;
		g.fillRect(x, y, ancho, alto);
	}

	public static void dibujarRectanguloRelleno(final Graphics2D g, final Rectangle r) {
		if (r == null) {
			return;
		}
		dibujarRectanguloRelleno(g, r.x, r.y, r.width, r.height);
	}

	public static void dibujarRectanguloRelleno(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto, final Color c) {
		objetosDibujados++;
		g.setColor(c);
		g.fillRect(x, y, ancho, alto);
	}

	public static void dibujarRectanguloRelleno(final Graphics2D g, final Rectangle r, final Color c) {
		if (r == null) {
			return;
		}
		dibujarRectanguloRelleno(g, r.x, r.y, r.width, r.height, c);
	}

	public static void dibujarRectanguloContorno(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto) {
		objetosDibujados++;
		g.drawRect(x, y, ancho, alto);
	}

	public static void dibujarRectanguloContorno(final Graphics2D g, final Rectangle r) {
		if (r == null) {
			return;
		}
		dibujarRectanguloContorno(g, r.x, r.y, r.width, r.height);
	}

	public static void dibujarRectanguloContorno(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto, final Color c) {
		objetosDibujados++;
		g.setColor(c);
		g.drawRect(x, y, ancho, alto);
	}

	public static void dibujarRectanguloContorno(final Graphics2D g, final Rectangle r, final Color c) {
		if (r == null) {
			return;
		}
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
	// === 3. DIBUJO CON REFERENCIA A CÁMARA / ESPACIO DE MUNDO (RELATIVO)
	// =========================================================================

	public static void dibujarFiguraEllipseRefCamara(final Graphics2D g, final Rectangle area, final Color color) {
		if (area == null) {
			return;
		}
		dibujarFiguraEllipseRefCamara(g, area.x, area.y, area.width, area.height, color);
	}

	public static void dibujarFiguraEllipseRefCamara(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto, final Color color) {
		objetosDibujados++;
		g.setColor(color);
		g.drawOval(Globales.getXDesplazamientoCamara(x), Globales.getYDesplazamientoCamara(y), ancho, alto);
	}

	public static void dibujarImagenRefCamara(final Graphics2D g, final Image img, final int x, final int y) {
		if (img == null) {
			return;
		}
		objetosDibujados++;
		g.drawImage(img, Globales.getXDesplazamientoCamara(x), Globales.getYDesplazamientoCamara(y), null);
	}

	public static void dibujarImagenRefCamara(final Graphics2D g, final BufferedImage img, final int x, final int y) {
		if (img == null) {
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
		if (img == null) {
			return;
		}
		objetosDibujados++;
		final Composite comOriginal = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0.0f, Math.min(1.0f, alpha))));
		g.drawImage(img, Globales.getXDesplazamientoCamara(x), Globales.getYDesplazamientoCamara(y), null);
		g.setComposite(comOriginal);
	}

	public static void dibujarStringRefCamara(final Graphics2D g, final String s, final int x, final int y) {
		if (s == null) {
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
		if (s == null) {
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

	/**
	 * Dibuja un texto en coordenadas del mundo con sombra de contraste proyectada
	 * con la cámara. Ideal para nombres de criaturas, textos de daño flotante y
	 * etiquetas en el mundo.
	 */
	public static void dibujarStringConSombraRefCamara(final Graphics2D g, final String s, final int x, final int y,
			final Color c, final Color sombra) {
		if (s == null) {
			return;
		}
		final int renderX = Globales.getXDesplazamientoCamara(x);
		final int renderY = Globales.getYDesplazamientoCamara(y);
		dibujarString(g, s, renderX + 1, renderY + 1, sombra);
		dibujarString(g, s, renderX, renderY, c);
	}

	public static void dibujarRectanguloRellenoRefCamara(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto) {
		objetosDibujados++;
		g.fillRect(Globales.getXDesplazamientoCamara(x), Globales.getYDesplazamientoCamara(y), ancho, alto);
	}

	public static void dibujarRectanguloRellenoRefCamara(final Graphics2D g, final Rectangle r) {
		if (r == null) {
			return;
		}
		dibujarRectanguloRellenoRefCamara(g, r.x, r.y, r.width, r.height);
	}

	public static void dibujarRectanguloRellenoRefCamara(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto, final Color c) {
		objetosDibujados++;
		g.setColor(c);
		g.fillRect(Globales.getXDesplazamientoCamara(x), Globales.getYDesplazamientoCamara(y), ancho, alto);
	}

	public static void dibujarRectanguloRellenoRefCamara(final Graphics2D g, final Rectangle r, final Color c) {
		if (r == null) {
			return;
		}
		dibujarRectanguloRellenoRefCamara(g, r.x, r.y, r.width, r.height, c);
	}

	public static void dibujarRectanguloContornoRefCamara(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto) {
		objetosDibujados++;
		g.drawRect(Globales.getXDesplazamientoCamara(x), Globales.getYDesplazamientoCamara(y), ancho, alto);
	}

	public static void dibujarRectanguloContornoRefCamara(final Graphics2D g, final Rectangle r) {
		if (r == null) {
			return;
		}
		dibujarRectanguloContornoRefCamara(g, r.x, r.y, r.width, r.height);
	}

	public static void dibujarRectanguloContornoRefCamara(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto, final Color c) {
		objetosDibujados++;
		g.setColor(c);
		g.drawRect(Globales.getXDesplazamientoCamara(x), Globales.getYDesplazamientoCamara(y), ancho, alto);
	}

	public static void dibujarRectanguloContornoRefCamara(final Graphics2D g, final Rectangle r, final Color c) {
		if (r == null) {
			return;
		}
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