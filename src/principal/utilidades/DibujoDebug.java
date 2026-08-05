package principal.utilidades;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

public class DibujoDebug {

	private static int objetosDibujados = 0;
	
	public static void dibujarFigura(final Graphics2D g2D, final Shape figura, final Color color) {
		objetosDibujados++;
		g2D.setColor(color);
		g2D.draw(figura);
	}
	
	public static void dibujarFiguraEllipse(final Graphics2D g, final Rectangle area, final Color color) {
		objetosDibujados++;
		g.setColor(color);
		g.draw(new Ellipse2D.Double(area.x, area.y, area.width, area.height));
	}

	public static void dibujarImagen(final Graphics2D g, final Image img, final int x, final int y) {
		objetosDibujados++;
		g.drawImage(img, x, y, null);
	}

	public static void dibujarImagen(final Graphics2D g, final BufferedImage img, final int x, final int y) {
		objetosDibujados++;
		g.drawImage(img, x, y, null);
	}

	public static void dibujarImagenConTransparencia(final Graphics2D g, final BufferedImage img, final int x, final int y,
			final float alpha) {
		objetosDibujados++;
		Composite com = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha));
		g.drawImage(img,x,y, null);
		g.setComposite(com);
	}

	public static void dibujarImagen(final Graphics2D g, final BufferedImage img, final Point p) {
		objetosDibujados++;
		g.drawImage(img, p.x, p.y, null);
	}

	public static void dibujarString(final Graphics2D g, String s, final int x, final int y) {
		objetosDibujados++;
		g.drawString(s, x, y);
	}

	public static void dibujarString(final Graphics2D g, String s, final Point p) {
		objetosDibujados++;
		g.drawString(s, p.x, p.y);
	}

	public static void dibujarString(final Graphics2D g, String s, final int x, final int y, final Color c) {
		objetosDibujados++;
		g.setColor(c);
		g.drawString(s, x, y);
	}

	public static void dibujarString(final Graphics2D g, String s, final Point p, final Color c) {
		objetosDibujados++;
		g.setColor(c);
		g.drawString(s, p.x, p.y);
	}

	public static void dibujarRectanguloRelleno(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto) {
		objetosDibujados++;
		g.fillRect(x, y, ancho, alto);
	}

	public static void dibujarRectanguloRelleno(final Graphics2D g, final Rectangle r) {
		objetosDibujados++;
		g.fillRect(r.x, r.y, r.width, r.height);
	}

	public static void dibujarRectanguloRelleno(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto, final Color c) {
		objetosDibujados++;
		g.setColor(c);
		g.fillRect(x, y, ancho, alto);
	}

	public static void dibujarRectanguloRelleno(final Graphics2D g, final Rectangle r, final Color c) {
		objetosDibujados++;
		g.setColor(c);
		g.fillRect(r.x, r.y, r.width, r.height);
	}

	public static void dibujarRectanguloContorno(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto) {
		objetosDibujados++;
		g.drawRect(x, y, ancho, alto);
	}

	public static void dibujarRectanguloContorno(final Graphics2D g, final Rectangle r) {
		objetosDibujados++;
		g.drawRect(r.x, r.y, r.width, r.height);
	}

	public static void dibujarRectanguloContorno(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto, final Color c) {
		objetosDibujados++;
		g.setColor(c);
		g.drawRect(x, y, ancho, alto);
	}

	public static void dibujarRectanguloContorno(final Graphics2D g, final Rectangle r, final Color c) {
		objetosDibujados++;
		g.setColor(c);
		g.drawRect(r.x, r.y, r.width, r.height);
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
		objetosDibujados++;
		g.setColor(c);
		g.drawLine(p1.x, p1.y, p2.x, p2.y);
	}

	public static void dibujarLinea(final Graphics2D g, final Point p1, final Point p2) {
		objetosDibujados++;
		g.drawLine(p1.x, p1.y, p2.x, p2.y);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static void dibujarFiguraEllipseRefCamara(final Graphics2D g, final Rectangle area, final Color color) {
		objetosDibujados++;
		g.setColor(color);
		g.draw(new Ellipse2D.Double(Constantes.getXDesplazamientoCamara(area.x), Constantes.getYDesplazamientoCamara(area.y), area.width, area.height));
	}
	
	public static void dibujarFiguraEllipseRefCamara(final Graphics2D g, final int x, final int y, final int ancho, final int alto, final Color color) {
		objetosDibujados++;
		g.setColor(color);
		g.draw(new Ellipse2D.Double(Constantes.getXDesplazamientoCamara(x), Constantes.getYDesplazamientoCamara(y), ancho, alto));
	}
	
	public static void dibujarImagenRefCamara(final Graphics2D g, final Image img, final int x, final int y) {
		objetosDibujados++;
		g.drawImage(img, Constantes.getXDesplazamientoCamara(x), Constantes.getYDesplazamientoCamara(y), null);
	}

	public static void dibujarImagenRefCamara(final Graphics2D g, final BufferedImage img, final int x, final int y) {
		objetosDibujados++;
		g.drawImage(img, Constantes.getXDesplazamientoCamara(x), Constantes.getYDesplazamientoCamara(y), null);
	}

	public static void dibujarImagenConTransparenciaRefCamara(final Graphics2D g, final BufferedImage img, final int x, final int y,
			final float alpha) {
		objetosDibujados++;
		Composite com = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha));
		g.drawImage(img, Constantes.getXDesplazamientoCamara(x), Constantes.getYDesplazamientoCamara(y), null);
		g.setComposite(com);
	}

	public static void dibujarImagenRefCamara(final Graphics2D g, final BufferedImage img, final Point p) {
		objetosDibujados++;
		g.drawImage(img, Constantes.getXDesplazamientoCamara(p.x), Constantes.getYDesplazamientoCamara(p.y), null);
	}

	public static void dibujarStringRefCamara(final Graphics2D g, String s, final int x, final int y) {
		objetosDibujados++;
		g.drawString(s,Constantes.getXDesplazamientoCamara(x), Constantes.getYDesplazamientoCamara(y));
	}

	public static void dibujarStringRefCamara(final Graphics2D g, String s, final Point p) {
		objetosDibujados++;
		g.drawString(s, Constantes.getXDesplazamientoCamara(p.x), Constantes.getYDesplazamientoCamara(p.y));
	}

	public static void dibujarStringRefCamara(final Graphics2D g, String s, final int x, final int y, final Color c) {
		objetosDibujados++;
		g.setColor(c);
		g.drawString(s,Constantes.getXDesplazamientoCamara(x), Constantes.getYDesplazamientoCamara(y));
	}

	public static void dibujarStringRefCamara(final Graphics2D g, String s, final Point p, final Color c) {
		objetosDibujados++;
		g.setColor(c);
		g.drawString(s, Constantes.getXDesplazamientoCamara(p.x), Constantes.getYDesplazamientoCamara(p.y));
	}

	public static void dibujarRectanguloRellenoRefCamara(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto) {
		objetosDibujados++;
		g.fillRect(Constantes.getXDesplazamientoCamara(x), Constantes.getYDesplazamientoCamara(y), ancho, alto);
	}

	public static void dibujarRectanguloRellenoRefCamara(final Graphics2D g, final Rectangle r) {
		objetosDibujados++;
		g.fillRect(Constantes.getXDesplazamientoCamara(r.x), Constantes.getYDesplazamientoCamara(r.y), r.width, r.height);
	}

	public static void dibujarRectanguloRellenoRefCamara(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto, final Color c) {
		objetosDibujados++;
		g.setColor(c);
		g.fillRect(Constantes.getXDesplazamientoCamara(x), Constantes.getYDesplazamientoCamara(y), ancho, alto);
	}

	public static void dibujarRectanguloRellenoRefCamara(final Graphics2D g, final Rectangle r, final Color c) {
		objetosDibujados++;
		g.setColor(c);
		g.fillRect(Constantes.getXDesplazamientoCamara(r.x), Constantes.getYDesplazamientoCamara(r.y), r.width, r.height);
	}

	public static void dibujarRectanguloContornoRefCamara(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto) {
		objetosDibujados++;
		g.drawRect(Constantes.getXDesplazamientoCamara(x), Constantes.getYDesplazamientoCamara(y), ancho, alto);
	}

	public static void dibujarRectanguloContornoRefCamara(final Graphics2D g, final Rectangle r) {
		objetosDibujados++;
		g.drawRect(Constantes.getXDesplazamientoCamara(r.x), Constantes.getYDesplazamientoCamara(r.y), r.width, r.height);
	}

	public static void dibujarRectanguloContornoRefCamara(final Graphics2D g, final int x, final int y, final int ancho,
			final int alto, final Color c) {
		objetosDibujados++;
		if (g.getColor() != c && !c.equals(g.getColor())) {
	        g.setColor(c);
	    }
		g.drawRect(Constantes.getXDesplazamientoCamara(x), Constantes.getYDesplazamientoCamara(y), ancho, alto);
	}

	public static void dibujarRectanguloContornoRefCamara(final Graphics2D g, final Rectangle r, final Color c) {
		objetosDibujados++;
		g.setColor(c);
		g.drawRect(Constantes.getXDesplazamientoCamara(r.x), Constantes.getYDesplazamientoCamara(r.y), r.width, r.height);
	}

	public static void dibujarLineaRefCamara(final Graphics2D g, final int x1, final int y1, final int x2, final int y2,
			final Color c) {
		objetosDibujados++;
		g.setColor(c);
		g.drawLine(Constantes.getXDesplazamientoCamara(x1), Constantes.getYDesplazamientoCamara(y1), Constantes.getXDesplazamientoCamara(x2), Constantes.getYDesplazamientoCamara(y2));
	}

	public static void dibujarLineaRefCamara(final Graphics2D g, final int x1, final int y1, final int x2, final int y2) {
		objetosDibujados++;
		g.drawLine(Constantes.getXDesplazamientoCamara(x1), Constantes.getYDesplazamientoCamara(y1), Constantes.getXDesplazamientoCamara(x2), Constantes.getYDesplazamientoCamara(y2));
	}

	public static void dibujarLineaRefCamara(final Graphics2D g, final Point p1, final Point p2, final Color c) {
		objetosDibujados++;
		g.setColor(c);
		g.drawLine(Constantes.getXDesplazamientoCamara(p1.x), Constantes.getYDesplazamientoCamara(p1.y), Constantes.getXDesplazamientoCamara(p2.x), Constantes.getYDesplazamientoCamara(p2.y));
	}

	public static void dibujarLineaRefCamara(final Graphics2D g, final Point p1, final Point p2) {
		objetosDibujados++;
		g.drawLine(Constantes.getXDesplazamientoCamara(p1.x), Constantes.getYDesplazamientoCamara(p1.y), Constantes.getXDesplazamientoCamara(p2.x), Constantes.getYDesplazamientoCamara(p2.y));
	}
	
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static int getContadorObjetos() {
		return objetosDibujados;
	}

	public static void reiniciarContadorObjetos() {
		objetosDibujados = 0;
	}
	// wrapper - envoltorio

	// metodo abreviado que junte otros metodos

	// devolver objeto complejo construido

}
