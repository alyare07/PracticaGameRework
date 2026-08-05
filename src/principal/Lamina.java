package principal;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

import principal.utilidades.Constantes;

public class Lamina extends Canvas {

	private static final long serialVersionUID = -1226193701839500515L;
	private BufferedImage buffer;
	private int anchoBuffer;
	private int altoBuffer;
	private Graphics2D g2;
	private Graphics g;
	private BufferedImage rectangulo;
	private Shape circulo = new Ellipse2D.Double(100, 100, 100, 100);
	private Rectangle r = new Rectangle(170, 190, 50, 50);

	public Lamina() {
		this.setBounds(0, 0, Constantes.ANCHO_PANTALLA_COMPLETA, Constantes.ALTO_PANTALLA_COMPLETA);
		this.setBackground(Color.black);
		this.rectangulo = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
		{
			final Graphics g = this.rectangulo.getGraphics();
			g.setColor(Color.orange);
			g.drawRect(0, 0, this.rectangulo.getWidth() - 1, this.rectangulo.getHeight() - 1);
			g.dispose();

		}

	}

	@Override
	public void update(final Graphics g) {
		this.paint(g);
	}

	@Override
	public void paint(final Graphics graphics) {
		super.paint(graphics);
		if (this.buffer == null || this.anchoBuffer != this.getWidth() || this.altoBuffer != this.getHeight()) {
			this.anchoBuffer = this.getWidth();
			this.altoBuffer = this.getHeight();
			this.buffer = new BufferedImage(anchoBuffer, altoBuffer, BufferedImage.TYPE_INT_ARGB);
		}
		g2 = (Graphics2D) this.buffer.getGraphics();
		g2.clearRect(0, 0, anchoBuffer, altoBuffer);
		g = this.buffer.getGraphics();
		g2.setColor(Color.white);
		g2.draw3DRect(100, 100, 100, 100, true);
		final Graphics gx = g2.create();
		gx.setColor(Color.BLUE);
		gx.fillRect(200, 200, 50, 50);
		g2.drawLine(100, 500, 150, 700);

		long ti = System.nanoTime();
		g.setColor(Color.red);
		g.drawRect(500, 500, 50, 50);
		long tf = System.nanoTime();
		long ti2 = System.nanoTime();
		g.drawImage(this.rectangulo, 600, 500, null);
		long tf2 = System.nanoTime();
		g.setColor(Color.white);
		g.drawString("t: " + (tf - ti), 450, 450);
		g.drawString("t: " + (tf2 - ti2), 550, 450);
		g2.draw(circulo);
		if (circulo.intersects(r.x, r.y, r.width, r.height)) {
			gx.setColor(Color.red);
			gx.drawRect(r.x, r.y, r.width, r.height);
		} else {
			gx.drawRect(r.x, r.y, r.width, r.height);
		}

		g.dispose();
		graphics.drawImage(buffer, 0, 0, this);
	}

}
