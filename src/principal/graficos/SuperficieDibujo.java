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
import principal.utilidades.DibujoDebug;
import principal.utilidades.Globales;

/**
 * Lienzo principal (Canvas) sobre el cual se renderizan los gráficos 2D del
 * juego. Implementa una estrategia de triple buffer (BufferStrategy) para
 * evitar parpadeos.
 */
public class SuperficieDibujo extends Canvas {

	private static final long serialVersionUID = -2303469561959410099L;
	private static SuperficieDibujo instancia;

	public final Raton RATON;

	/**
	 * Fuente reutilizable para texto de Debug. Evita instanciar objetos Font por
	 * frame
	 */
	private static final Font FUENTE_DEBUG = new Font(Font.SANS_SERIF, Font.PLAIN, 9);

	private SuperficieDibujo(final int ancho, final int alto) {
		this.RATON = Globales.RATON;

		this.setIgnoreRepaint(true);
		this.setPreferredSize(new Dimension(ancho, alto));
		this.addKeyListener(Globales.TECLADO);
		this.addMouseListener(this.RATON);
		this.addMouseMotionListener(this.RATON); // Imprescindible para mouseMoved / mouseDragged

		this.setFocusable(true);
		this.requestFocus();
	}

	/**
	 * Singleton para obtener la única instancia activa del Canvas de dibujo.
	 */
	public static SuperficieDibujo obtenerSuperficieDibujo() {
		if (instancia == null) {
			instancia = new SuperficieDibujo(Globales.CONSTANTES.ANCHO_PANTALLA_COMPLETA,
					Globales.CONSTANTES.ALTO_PANTALLA_COMPLETA);
		}
		return instancia;
	}

	/**
	 * Alias por retrocompatibilidad.
	 * 
	 * @deprecated Usar {@link #obtenerSuperficieDibujo()}
	 */
	@Deprecated
	public static SuperficieDibujo obetenerSuperficieDibujo() {
		return obtenerSuperficieDibujo();
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

		try {
			// --- Configuración de Rendering Hints (Rendimiento 2D / Pixel Art) ---
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);

			g.setFont(FUENTE_DEBUG);

			// Aplicar escalado si la resolución configurada difiere del tamaño real de
			// pantalla
			if ((Globales.FACTOR_ESCALADO_X != 1.0) || (Globales.FACTOR_ESCALADO_Y != 1.0)) {
				g.scale(Globales.FACTOR_ESCALADO_X, Globales.FACTOR_ESCALADO_Y);
			}

			// --- INICIO DEL DIBUJADO ---

			// 1. Limpieza de fondo (Vaciado de pantalla)
			DibujoDebug.dibujarRectanguloRelleno(g, 0, 0, Globales.CONSTANTES.ANCHO_JUEGO,
					Globales.CONSTANTES.ALTO_JUEGO, Color.BLACK);

			// 2. Renderizado del estado actual del juego
			if (ge != null) {
				ge.pintar(g);
			}

			// 3. Renderizado de información Debug (APS, FPS, Objetos dibujados)
			g.setColor(Color.GREEN);
			DibujoDebug.dibujarString(g, "APS: " + Globales.aps, 20, 35);
			DibujoDebug.dibujarString(g, "FPS: " + Globales.fps, 20, 50);
			DibujoDebug.dibujarString(g, "OPF: " + (DibujoDebug.getContadorObjetos() + 1), 20, 65);

			// --- FIN DEL DIBUJADO ---

		} finally {
			// Liberar recursos gráficos del contexto actual del buffer de forma segura
			g.dispose();
		}

		// Mostrar el buffer dibujado en pantalla si la superficie sigue siendo válida
		if (!buffer.contentsLost()) {
			buffer.show();
			Toolkit.getDefaultToolkit().sync();
		}
	}
}