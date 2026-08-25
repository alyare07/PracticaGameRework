package principal.graficos;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import javax.swing.JFrame;

import principal.utilidades.Constantes;
import principal.utilidades.Globales;

/**
 * Ventana principal contenedora del juego (JFrame).
 * <p>
 * <b>Características de Renderizado y Resolución:</b>
 * <ul>
 * <li><b>Resoluciones Fijas Pixel-Perfect:</b> Aplica multiplicadores enteros
 * exactos ($1\times, 2\times, 3\times, \dots$) sobre la base de
 * {@code 640x360}.</li>
 * <li><b>Bloqueo de Redimensionado Manual:</b> Deshabilita el estiramiento
 * libre con el mouse ({@code setResizable(false)}) para evitar bandas negras
 * asimétricas y pixel jitter.</li>
 * <li><b>Validación de Límites del Monitor:</b> Impide seleccionar resoluciones
 * mayores a la capacidad nativa del monitor del usuario o inferiores al tamaño
 * base del juego.</li>
 * <li><b>Soporte Multiplataforma (Linux / Windows / macOS):</b> Gestiona la
 * transición limpia entre Modo Ventana y Pantalla Completa Exclusiva sin
 * excepciones de AWT.</li>
 * </ul>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public class Ventana extends JFrame {
	private static final long serialVersionUID = 5979421777239930009L;

	/** Título visible de la ventana en la barra de tareas y marco. */
	private final String titulo;

	/** Componente Canvas donde se realiza el renderizado activo del juego. */
	private final SuperficieDibujo sd;

	/** Multiplicador de escala entero activo actualmente (ej: 1, 2, 3). */
	private int escalaActual;

	/** Bandera que indica si el modo pantalla completa exclusiva está activado. */
	private boolean pantallaCompleta;

	/**
	 * Construye la ventana e inicializa su configuración gráfica.
	 *
	 * @param titulo Título de la ventana.
	 * @param sd     Superficie de dibujo (Canvas) del motor.
	 */
	public Ventana(final String titulo, final SuperficieDibujo sd) {
		this.titulo = titulo;
		this.sd = sd;
		this.escalaActual = Constantes.ESCALA_6X_3840x2160; // Escala por defecto al abrir en ventana
		this.pantallaCompleta = false;

		this.setTitle(this.titulo);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());
		this.add(this.sd, BorderLayout.CENTER);
		this.setResizable(false); // Prohíbe redimensionar manualmente con el ratón

		// Inicializar en modo ventana validado
		this.establecerResolucion(this.escalaActual);
	}

	// =========================================================================
	// === GESTIÓN DE RESOLUCIÓN Y VALIDACIONES
	// =========================================================================

	/*
	 * =========================================================================
	 * EXPLICACIÓN TÉCNICA: VALIDACIÓN DE RESOLUCIÓN RESPECTO AL MONITOR
	 * ------------------------------------------------------------------------- 1.
	 * Mínimo permitido: Escala 1x (640 x 360). Nunca permite valores menores. 2.
	 * Máximo permitido: Consulta la resolución física nativa del monitor usando
	 * 'DisplayMode'. 3. Si la resolución solicitada excede el ancho o alto del
	 * monitor: - Clampa automáticamente a la escala entera máxima que quepa en
	 * pantalla. 4. Ajusta 'sd.setPreferredSize()' y ejecuta 'pack()' para que los
	 * bordes de la ventana de Linux/Windows envuelvan el Canvas con precisión
	 * exacta.
	 * =========================================================================
	 */

	/**
	 * Cambia la resolución de la ventana a una escala fija especificada, validando
	 * que no exceda las dimensiones del monitor ni baje del mínimo base.
	 *
	 * @param escala Multiplicador de escala entero deseado (1, 2, 3, etc.).
	 */
	public void establecerResolucion(final int escala) {
		final GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		final DisplayMode dm = gd.getDisplayMode();
		final int anchoMonitor = dm.getWidth();
		final int altoMonitor = dm.getHeight();

		// 1. Calcular la escala máxima soportada por este monitor
		final int escalaMaxima = Math.max(Constantes.ESCALA_MINIMA,
				Math.min(anchoMonitor / Constantes.ANCHO_JUEGO, altoMonitor / Constantes.ALTO_JUEGO));

		// 2. Clampar la escala dentro de los límites seguros [1, escalaMaxima]
		int escalaValidada = Math.max(Constantes.ESCALA_MINIMA, escala);
		if (escalaValidada > escalaMaxima) {
			System.out.println("Advertencia: Escala " + escala + "x excede el monitor (" + anchoMonitor + "x"
					+ altoMonitor + "). Ajustando a " + escalaMaxima + "x.");
			escalaValidada = escalaMaxima;
		}

		// 3. Salir de pantalla completa si estaba activa
		if (this.pantallaCompleta) {
			this.salirDePantallaCompleta();
		}

		this.escalaActual = escalaValidada;
		final int anchoFinal = Constantes.ANCHO_JUEGO * this.escalaActual;
		final int altoFinal = Constantes.ALTO_JUEGO * this.escalaActual;

		// 4. Actualizar variables globales del motor
		Globales.ANCHO_PANTALLA_COMPLETA = anchoFinal;
		Globales.ALTO_PANTALLA_COMPLETA = altoFinal;
		Globales.FACTOR_ESCALADO_X = this.escalaActual;
		Globales.FACTOR_ESCALADO_Y = this.escalaActual;

		// 5. Ajustar Canvas y envolver ventana
		this.sd.setPreferredSize(new Dimension(anchoFinal, altoFinal));
		this.pack();
		this.setLocationRelativeTo(null); // Centra la ventana en el escritorio
		this.setVisible(true);

		System.out.println(
				"Resolución de ventana establecida: " + anchoFinal + "x" + altoFinal + " (" + this.escalaActual + "x)");
	}

	/**
	 * Activa o desactiva el modo de Pantalla Completa Exclusiva.
	 *
	 * @param activar {@code true} para activar pantalla completa; {@code false}
	 *                para volver a ventana.
	 */
	public void establecerPantallaCompleta(final boolean activar) {
		final GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

		if (activar && gd.isFullScreenSupported()) {
			if (this.pantallaCompleta) {
				return; // Ya está en pantalla completa
			}

			this.dispose(); // Requerido por Java AWT antes de modificar decoraciones
			this.setUndecorated(true);
			this.setResizable(false);
			this.pack();

			gd.setFullScreenWindow(this);
			this.pantallaCompleta = true;

			// Actualizar dimensiones reales de la pantalla completa
			final DisplayMode dm = gd.getDisplayMode();
			Globales.ANCHO_PANTALLA_COMPLETA = dm.getWidth();
			Globales.ALTO_PANTALLA_COMPLETA = dm.getHeight();
			Globales.actualizarFactorEscalado();

			this.setVisible(true);
			System.out.println("Pantalla Completa activada: " + dm.getWidth() + "x" + dm.getHeight() + " (Escala "
					+ (int) Globales.FACTOR_ESCALADO_X + "x)");
		} else {
			this.establecerResolucion(this.escalaActual);
		}
	}

	/**
	 * Restaura la ventana desde pantalla completa a modo ventana decorada.
	 */
	private void salirDePantallaCompleta() {
		final GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		if (gd.getFullScreenWindow() == this) {
			gd.setFullScreenWindow(null);
		}

		this.dispose();
		this.setUndecorated(false);
		this.setResizable(false);
		this.pantallaCompleta = false;
	}

	// =========================================================================
	// === ACCESORES Y CONSULTAS
	// =========================================================================

	/**
	 * Retorna la escala entera máxima soportada por el monitor conectado
	 * actualmente.
	 *
	 * @return Multiplicador máximo (ej: 3 para 1080p, 4 para 1440p, 6 para 4K).
	 */
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