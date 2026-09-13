package principal.graficos;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import javax.swing.JFrame;

import principal.configuracion.ConfiguracionGrafica;
import principal.configuracion.TipoPantalla;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;

/**
 * Ventana principal contenedora del juego (JFrame) con soporte para Pantalla
 * Completa Sin Bordes, Exclusiva y Modo Ventana escalado.
 * 
 * @version 3.0 (Vanilla Java 8)
 */
public class Ventana extends JFrame {
	private static final long serialVersionUID = 5979421777239930009L;

	private final String titulo;
	private final SuperficieDibujo sd;
	private int escalaActual;
	private boolean pantallaCompleta;

	public Ventana(final String titulo, final SuperficieDibujo sd) {
		this.titulo = titulo;
		this.sd = sd;
		this.escalaActual = Constantes.ESCALA_1X_640x360;
		this.pantallaCompleta = false;

		this.setTitle(this.titulo);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.getContentPane().setBackground(Color.BLACK);
		this.setBackground(Color.BLACK);
		this.setLayout(new BorderLayout());
		this.add(this.sd, BorderLayout.CENTER);
		this.setResizable(false);
	}

	/**
	 * Aplica el modo de visualización seleccionado por el usuario en caliente.
	 *
	 * @param tipo          Modo de pantalla (VENTANA, SIN_BORDES, EXCLUSIVA).
	 * @param escalaVentana Multiplicador para modo ventana (1x, 2x, 3x, etc.).
	 */
	public void aplicarModoVisualizacion(final TipoPantalla tipo, final int escalaVentana) {
		final GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		final DisplayMode dm = gd.getDisplayMode();
		final int anchoMonitor = dm.getWidth();
		final int altoMonitor = dm.getHeight();

		if (gd.getFullScreenWindow() == this) {
			gd.setFullScreenWindow(null);
		}

		this.dispose();

		switch (tipo) {
		case SIN_BORDES:
			this.setUndecorated(true);
			this.setResizable(false);
			this.setBounds(0, 0, anchoMonitor, altoMonitor);
			this.sd.setPreferredSize(new Dimension(anchoMonitor, altoMonitor));
			this.sd.setSize(anchoMonitor, altoMonitor);
			this.pack();
			this.setLocation(0, 0);
			this.pantallaCompleta = true;
			ConfiguracionGrafica.recalcularEscaladoYOffsets();
			this.setVisible(true);
			this.sd.requestFocus();
			break;

		case EXCLUSIVA:
			if (gd.isFullScreenSupported()) {
				this.setUndecorated(true);
				this.setResizable(false);
				this.sd.setPreferredSize(new Dimension(anchoMonitor, altoMonitor));
				this.sd.setSize(anchoMonitor, altoMonitor);
				this.pack();
				gd.setFullScreenWindow(this);
				this.pantallaCompleta = true;
				ConfiguracionGrafica.recalcularEscaladoYOffsets();
				this.setVisible(true);
				this.sd.requestFocus();
			} else {
				this.aplicarModoVisualizacion(TipoPantalla.SIN_BORDES, escalaVentana);
			}
			break;

		case VENTANA:
		default:
			this.setUndecorated(false);
			this.setResizable(false);
			this.pantallaCompleta = false;

			final int escalaMaxima = Math.max(Constantes.ESCALA_MINIMA,
					Math.min(anchoMonitor / Constantes.ANCHO_JUEGO, altoMonitor / Constantes.ALTO_JUEGO));
			final int escalaFinal = Math.max(Constantes.ESCALA_MINIMA, Math.min(escalaVentana, escalaMaxima));
			this.escalaActual = escalaFinal;

			final int anchoFinal = Constantes.ANCHO_JUEGO * escalaFinal;
			final int altoFinal = Constantes.ALTO_JUEGO * escalaFinal;

			Globales.ANCHO_PANTALLA_COMPLETA = anchoFinal;
			Globales.ALTO_PANTALLA_COMPLETA = altoFinal;
			Globales.FACTOR_ESCALADO_X = escalaFinal;
			Globales.FACTOR_ESCALADO_Y = escalaFinal;
			Globales.DESPLAZAMIENTO_X = 0;
			Globales.DESPLAZAMIENTO_Y = 0;

			this.sd.setPreferredSize(new Dimension(anchoFinal, altoFinal));
			this.sd.setSize(anchoFinal, altoFinal);
			this.pack();
			this.setLocationRelativeTo(null);
			this.setVisible(true);
			this.sd.requestFocus();
			break;
		}
	}

	@Deprecated
	public void establecerResolucion(final int escala) {
		this.aplicarModoVisualizacion(TipoPantalla.VENTANA, escala);
	}

	@Deprecated
	public void establecerPantallaCompleta(final boolean activar) {
		this.aplicarModoVisualizacion(activar ? TipoPantalla.SIN_BORDES : TipoPantalla.VENTANA, this.escalaActual);
	}

	public int getEscalaMaximaSoportada() {
		final GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		final DisplayMode dm = gd.getDisplayMode();
		return Math.max(Constantes.ESCALA_MINIMA,
				Math.min(dm.getWidth() / Constantes.ANCHO_JUEGO, dm.getHeight() / Constantes.ALTO_JUEGO));
	}

	public int getEscalaActual() {
		return this.escalaActual;
	}

	public boolean isPantallaCompleta() {
		return this.pantallaCompleta;
	}
}