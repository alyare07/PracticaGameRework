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

/**
 * Lienzo principal (Canvas) sobre el cual se renderizan los gráficos 2D del
 * juego. Implementa una estrategia de triple buffer (BufferStrategy) para
 * evitar parpadeos.
 */
public class SuperficieDibujo extends Canvas {

	private static final long serialVersionUID = -2303469561959410099L;
	private static SuperficieDibujo instancia;

	public final Raton RATON;

	private SuperficieDibujo(final int ancho, final int alto) {
		this.RATON = Constantes.RATON;

		this.setIgnoreRepaint(true);
		this.setPreferredSize(new Dimension(ancho, alto));
		this.addKeyListener(Constantes.TECLADO);
		this.addMouseListener(this.RATON);
		this.addMouseMotionListener(this.RATON); // <--- Imprescindible para mouseMoved / mouseDragged

		this.setFocusable(true);
		this.requestFocus();
	}

	/**
	 * Singleton para obtener la única instancia activa del Canvas de dibujo.
	 */
	public static SuperficieDibujo obetenerSuperficieDibujo() {
		if (instancia == null) {
			instancia = new SuperficieDibujo(Constantes.ANCHO_PANTALLA_COMPLETA, Constantes.ALTO_PANTALLA_COMPLETA);
		}
		return instancia;
	}

	/**
	 * Ejecuta el ciclo de renderizado de un frame en la ventana.
	 *
	 * @param ge Gestor de estados actual del juego.
	 */
	public void pintar(final GestorEstados ge) {
		final BufferStrategy buffer = this.getBufferStrategy();

		// Si el BufferStrategy no existe aún, se inicializa con Triple Buffer
		if (buffer == null) {
			this.createBufferStrategy(3);
			return;
		}

		DibujoDebug.reiniciarContadorObjetos();
		final Graphics2D g = (Graphics2D) buffer.getDrawGraphics();

		// --- Configuración de Rendering Hints (Rendimiento 2D / Pixel Art) ---
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);

		g.setFont(g.getFont().deriveFont(9f));

		// Aplicar escalado si la resolución configurada difiere del tamaño real de
		// pantalla
		if ((Constantes.FACTOR_ESCALADO_X != 1.0) || (Constantes.FACTOR_ESCALADO_Y != 1.0)) {
			g.scale(Constantes.FACTOR_ESCALADO_X, Constantes.FACTOR_ESCALADO_Y);
		}

		// --- INICIO DEL DIBUJADO ---

		// 1. Limpieza de fondo (Vaciado de pantalla)
		DibujoDebug.dibujarRectanguloRelleno(g, 0, 0, Constantes.ANCHO_JUEGO, Constantes.ALTO_JUEGO, Color.BLACK);

		// 2. Renderizado del estado actual del juego
		ge.pintar(g);

		// 3. Renderizado de información Debug (APS, FPS, Objetos dibujados)
		g.setColor(Color.GREEN);
		DibujoDebug.dibujarString(g, "APS: " + Constantes.GLOBALES.aps, 20, 35);
		DibujoDebug.dibujarString(g, "FPS: " + Constantes.GLOBALES.fps, 20, 50);
		DibujoDebug.dibujarString(g, "OPF: " + (DibujoDebug.getContadorObjetos() + 1), 20, 65);

		// --- FIN DEL DIBUJADO ---

		// Sincronizar buffer con la memoria de la pantalla del SO
		Toolkit.getDefaultToolkit().sync();

		// Liberar recursos gráficos del contexto actual
		g.dispose();
		buffer.show();
	}
}