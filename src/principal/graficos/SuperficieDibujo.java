package principal.graficos;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferStrategy;
import principal.controles.Raton;
import principal.maquinaestado.GestorEstados;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;

public class SuperficieDibujo extends Canvas {

	private static SuperficieDibujo sd;

	private static final long serialVersionUID = -2303469561959410099L;
	public final Raton RATON;
	private RenderingHints rh = null;

	private SuperficieDibujo(final int ancho, final int alto) {
		this.RATON = Constantes.RATON;
		setIgnoreRepaint(true);
		setPreferredSize(new Dimension(ancho, alto));
		addKeyListener(Constantes.TECLADO);
		addMouseListener(RATON);
		setFocusable(true);
		requestFocus();
	}

	public static SuperficieDibujo obetenerSuperficieDibujo() {
		if (sd == null) {
			sd = new SuperficieDibujo(Constantes.ANCHO_PANTALLA_COMPLETA, Constantes.ALTO_PANTALLA_COMPLETA);
		}
		return sd;
	}

	public void actualizar() {

	}

	public void pintar(final GestorEstados ge) {
		final BufferStrategy buffer = getBufferStrategy();
		if (buffer == null) {
			createBufferStrategy(3);
			return;
		}
		DibujoDebug.reiniciarContadorObjetos();
		final Graphics2D g = (Graphics2D) buffer.getDrawGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
		
		
		g.setFont(g.getFont().deriveFont(9f));
		if (Constantes.FACTOR_ESCALADO_X != 1.0 || Constantes.FACTOR_ESCALADO_Y != 1.0) {
			g.scale(Constantes.FACTOR_ESCALADO_X, Constantes.FACTOR_ESCALADO_Y);
		}

		// ---------comienzo del dibujado-----------
		// vaciado de la ventana
		DibujoDebug.dibujarRectanguloRelleno(g, 0, 0, Constantes.ANCHO_JUEGO, Constantes.ALTO_JUEGO, Color.black);
		//LINEA SIGUIENTE A MODO PRUEBA
		
		// dibujado del estado actual
		ge.pintar(g);
		// dibujado del debug en caso de positivo
		g.setColor(Color.green);
//		if (Constantes.TECLADO.TECLA_DEBUG.presionado()) {
			g.setFont(g.getFont().deriveFont(9f));
			DibujoDebug.dibujarString(g, "APS: " + String.valueOf(Constantes.GLOBALES.aps), 20, 35);
			DibujoDebug.dibujarString(g, "OPF: " + String.valueOf((DibujoDebug.getContadorObjetos() + 1)), 20, 65);
			DibujoDebug.dibujarString(g, "FPS: " + String.valueOf(Constantes.GLOBALES.fps), 20, 50);
//		}
		
		
		// fin del dibujado
		Toolkit.getDefaultToolkit().sync();
		g.dispose();
		buffer.show();

	}

}
