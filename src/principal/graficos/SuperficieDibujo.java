package principal.graficos;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferStrategy;

import principal.controles.Raton;
import principal.maquinaestado.GestorEstados;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

/**
 * Lienzo principal (Canvas) sobre el cual se renderizan los gráficos 2D del
 * juego. Implementa triple buffer y listeners de teclado, ratón y rueda de
 * scroll.
 */
public class SuperficieDibujo extends Canvas {

	private static final long serialVersionUID = -2303469561959410099L;
	private static SuperficieDibujo instancia;

	public final Raton RATON;

	private static final Font FUENTE_DEBUG = new Font(Font.SANS_SERIF, Font.PLAIN, 9);

	private SuperficieDibujo(final int ancho, final int alto) {
		this.RATON = Globales.RATON;

		this.setIgnoreRepaint(true);
		this.setPreferredSize(new Dimension(ancho, alto));
		this.addKeyListener(Globales.TECLADO);
		this.addMouseListener(this.RATON);
		this.addMouseMotionListener(this.RATON);
		this.addMouseWheelListener(this.RATON); // <-- Habilita la captura de eventos de la rueda del ratón

		this.setFocusable(true);
		this.requestFocus();
	}

	public static SuperficieDibujo obtenerSuperficieDibujo() {
		if (instancia == null) {
			instancia = new SuperficieDibujo(Globales.ANCHO_PANTALLA_COMPLETA, Globales.ALTO_PANTALLA_COMPLETA);
		}
		return instancia;
	}

	@Deprecated
	public static SuperficieDibujo obetenerSuperficieDibujo() {
		return obtenerSuperficieDibujo();
	}

	public void pintar(final GestorEstados ge) {
		final BufferStrategy buffer = this.getBufferStrategy();

		if (buffer == null) {
			this.createBufferStrategy(3);
			return;
		}

		Render2D.reiniciarContadorObjetos();
		final Graphics2D g = (Graphics2D) buffer.getDrawGraphics();

		try {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);

			g.setFont(FUENTE_DEBUG);

			if ((Globales.FACTOR_ESCALADO_X != 1.0) || (Globales.FACTOR_ESCALADO_Y != 1.0)) {
				g.scale(Globales.FACTOR_ESCALADO_X, Globales.FACTOR_ESCALADO_Y);
			}

			// 1. Limpieza de fondo
			Render2D.dibujarRectanguloRelleno(g, 0, 0, Constantes.ANCHO_JUEGO, Constantes.ALTO_JUEGO, Color.BLACK);

			// 2. Renderizado del estado actual
			if (ge != null) {
				ge.pintar(g);
			}

			// 3. Información Debug
			g.setColor(Color.GREEN);
			Render2D.dibujarString(g, "APS: " + Globales.aps, 20, 35);
			Render2D.dibujarString(g, "FPS: " + Globales.fps, 20, 50);
			Render2D.dibujarString(g, "OPF: " + (Render2D.getContadorObjetos() + 1), 20, 65);

		} finally {
			g.dispose();
		}

		if (!buffer.contentsLost()) {
			buffer.show();
			Toolkit.getDefaultToolkit().sync();
		}
	}
}