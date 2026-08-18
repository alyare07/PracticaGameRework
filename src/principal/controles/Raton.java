package principal.controles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import principal.graficos.SuperficieDibujo;
import principal.utilidades.DibujoDebug;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;

/**
 * Gestor de entrada para el mouse.
 * <p>
 * Maneja eventos de posición, clics sostenidos y pulsaciones únicas por frame,
 * optimizado para minimizar el consumo de CPU y la presión sobre el Garbage
 * Collector.
 * </p>
 */
public class Raton extends MouseAdapter {

	private final Point posicion;
	private volatile boolean click;
	private volatile boolean presionadoDerecho;
	private volatile boolean presionadoIzquierdo;

	private volatile boolean presionadoDerUnicaAct;
	private volatile boolean presionadoIzqUnicaAct;
	private volatile boolean disponibleParaPresionarIzqUnicaAct = true;
	private volatile boolean disponibleParaPresionarDerUnicaAct = true;
	private volatile boolean latchIzq = false;
	private volatile boolean latchDer = false;
	private Rectangle puntoPresionado;
	private final GestorTiempo GT = new GestorTiempo();
	private int tiempoMsEspera = 0;

	public Raton() {
		this.posicion = new Point();
		this.click = false;
		this.puntoPresionado = new Rectangle(0, 0, 1, 1);
	}

	/**
	 * Consume las acciones de pulsación única después de haber sido leídas en el
	 * ciclo actual.
	 */
	private void actualizarPresionadosUnicaVez() {
		if (this.latchIzq) {
			this.presionadoIzqUnicaAct = true;
			this.latchIzq = false; // Consumimos la señal del evento
		} else {
			this.presionadoIzqUnicaAct = false;
		}

		if (this.latchDer) {
			this.presionadoDerUnicaAct = true;
			this.latchDer = false; // Consumimos la señal del evento
		} else {
			this.presionadoDerUnicaAct = false;
		}
	}

	public void actualizar(final SuperficieDibujo sd) {
		this.actualizarPresionadosUnicaVez();
	}

	public void dibujar(final Graphics2D g) {
		DibujoDebug.dibujarString(g, "RX: " + this.posicion.x, 20, 200, Color.RED);
		DibujoDebug.dibujarString(g, "RY: " + this.posicion.y, 20, 210, Color.RED);
		DibujoDebug.dibujarRectanguloContorno(g, this.getRectanguloPosicionEscalado(), Color.BLUE);
	}

	// -----------------------------------------------------------------------
	// EVENTOS DE NAVEGACIÓN Y MOVIMIENTO (Escuchados por Swing AWT)
	// Nota: Para que estos métodos funcionen, la SuperficieDibujo debe hacer:
	// "sd.addMouseListener(raton);" y "sd.addMouseMotionListener(raton);"
	// -----------------------------------------------------------------------

	@Override
	public void mouseMoved(final MouseEvent e) {
		this.posicion.setLocation(e.getPoint());
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		this.posicion.setLocation(e.getPoint());
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
		if (this.GT.transcurrioMiliSegundos(this.tiempoMsEspera)) {
			if (!this.click) {
				this.tiempoMsEspera = 0;
				this.click = true;
			}
		}
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		if (!this.GT.transcurrioMiliSegundos(this.tiempoMsEspera)) {
			return;
		}

		this.GT.establecerReferenciaTiempoActual();
		this.tiempoMsEspera = 0;

		if (SwingUtilities.isLeftMouseButton(e)) {
			this.presionadoIzquierdo = true;
			this.latchIzq = true; // Levantamos el pestillo para el próximo frame
		} else if (SwingUtilities.isRightMouseButton(e)) {
			this.presionadoDerecho = true;
			this.latchDer = true; // Levantamos el pestillo para el próximo frame
		}

		this.puntoPresionado = this.getRectanguloPosicionEscalado();
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			this.presionadoIzquierdo = false;
			this.latchIzq = false;
		}
		if (SwingUtilities.isRightMouseButton(e)) {
			this.presionadoDerecho = false;
			this.latchDer = false;
		}
	}

	// -----------------------------------------------------------------------
	// CONSULTAS DE POSICIÓN
	// -----------------------------------------------------------------------

	/**
	 * El punto donde se ubica el puntero. No tiene en cuenta el escalado de la
	 * pantalla. NO se tiene en cuenta el desplazamiento de la cámara.
	 * 
	 * @return El punto donde se ubica el puntero (SIN ESCALAR)
	 */
	public Point getPuntoPosicionSinEscalar() {
		return this.posicion;
	}

	/**
	 * El punto donde se ubica el puntero. Se tiene en cuenta el escalado de la
	 * pantalla. NO se tiene en cuenta el desplazamiento de la cámara.
	 * 
	 * @return El punto donde se ubica el puntero (ESCALADO)
	 */
	public Point getPuntoPosicionEscalado() {
		return new Point((int) (this.posicion.x / Globales.FACTOR_ESCALADO_X),
				(int) (this.posicion.y / Globales.FACTOR_ESCALADO_Y));
	}

	/**
	 * El área donde se ubica el puntero. Se tiene en cuenta el escalado de la
	 * pantalla. NO se tiene en cuenta el desplazamiento de la cámara.
	 * 
	 * @return El área donde se ubica el puntero (ESCALADO)
	 */
	public Rectangle getRectanguloPosicionEscalado() {
		return new Rectangle((int) (this.posicion.x / Globales.FACTOR_ESCALADO_X),
				(int) (this.posicion.y / Globales.FACTOR_ESCALADO_Y), 1, 1);
	}

	/**
	 * El área donde se ubica el puntero. Se tiene en cuenta el escalado de la
	 * pantalla y el desplazamiento de la cámara.
	 * 
	 * @return El área donde se ubica el puntero (ESCALADO Y CON DESPLAZAMIENTO
	 *         CÁMARA)
	 */
	public Rectangle getRectanguloPosicionEscaladoConDesplazamientoCamara() {
		return new Rectangle(
				((int) (this.posicion.x / Globales.FACTOR_ESCALADO_X)
						+ Globales.CAMARA.getPosicionXInt()) - Globales.CAMARA.getMargenX(),
				((int) (this.posicion.y / Globales.FACTOR_ESCALADO_Y)
						+ Globales.CAMARA.getPosicionYInt()) - Globales.CAMARA.getMargenY(),
				1, 1);
	}

	public Point getPuntoPosicionEscaladoConDesplazamientoCamara() {
		return new Point(
				((int) (this.posicion.x / Globales.FACTOR_ESCALADO_X)
						+ Globales.CAMARA.getPosicionXInt()) - Globales.CAMARA.getMargenX(),
				((int) (this.posicion.y / Globales.FACTOR_ESCALADO_Y)
						+ Globales.CAMARA.getPosicionYInt()) - Globales.CAMARA.getMargenY());
	}

	// -----------------------------------------------------------------------
	// ESTADOS Y GETTERS
	// -----------------------------------------------------------------------

	public boolean getClick() {
		return this.click;
	}

	public void reiniciarClick() {
		this.click = false;
	}

	/**
	 * Verifica si el click izquierdo del mouse está presionado en dicho momento.
	 * 
	 * @return TRUE si el clic izquierdo está presionado.
	 */
	public boolean presionadoClickIzq() {
		return this.presionadoIzquierdo;
	}

	/**
	 * Verifica si el click derecho del mouse está presionado en dicho momento.
	 * 
	 * @return TRUE si el clic derecho está presionado.
	 */
	public boolean presionadoClickDer() {
		return this.presionadoDerecho;
	}

	/**
	 * Verifica si el click izquierdo del mouse se presionó en esta actualización.
	 * Solo retorna TRUE durante un único ciclo por cada pulsación.
	 * 
	 * @return TRUE si el clic izquierdo se activó en esta actualización.
	 */
	public boolean presionadoClickIzqUnicaAct() {
		return this.presionadoIzqUnicaAct;
	}

	/**
	 * Verifica si el click derecho del mouse se presionó en esta actualización.
	 * Solo retorna TRUE durante un único ciclo por cada pulsación.
	 * 
	 * @return TRUE si el clic derecho se activó en esta actualización.
	 */
	public boolean presionadoClickDerUnicaAct() {
		return this.presionadoDerUnicaAct;
	}

	/**
	 * Área expresada en Rectángulo donde está presionado el mouse (escalado).
	 * 
	 * @return El punto presionado expresado en Rectangle.
	 */
	public Rectangle getPuntoPresionado() {
		return this.puntoPresionado;
	}

	/**
	 * Duerme la detección de clics y presionados durante el tiempo especificado.
	 * 
	 * @param ms El tiempo en milisegundos a dormir el mouse.
	 */
	public void dormirMS(final int ms) {
		this.tiempoMsEspera = ms;
		this.GT.establecerReferenciaTiempoActual();
	}

	/**
	 * Suelta todos los clics y reinicia los estados de pulsación.
	 */
	public void soltar() {
		this.presionadoIzquierdo = false;
		this.presionadoDerecho = false;
		this.presionadoIzqUnicaAct = false;
		this.presionadoDerUnicaAct = false;
		this.latchIzq = false;
		this.latchDer = false;
	}
}