package principal.controles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.SwingUtilities;

import principal.graficos.SuperficieDibujo;
import principal.utilidades.Constantes;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

/**
 * Gestor centralizado de entrada del ratón con sincronización Lock-Free
 * (Zero-GC).
 * 
 * @version 2.6 (Vanilla Java 8)
 */
public class Raton extends MouseAdapter {

	private final Point posicion;
	private volatile boolean click;
	private volatile boolean presionadoIzquierdo;
	private volatile boolean presionadoDerecho;
	private volatile boolean presionadoIzqUnicaAct;
	private volatile boolean presionadoDerUnicaAct;
	private volatile boolean latchIzq = false;
	private volatile boolean latchDer = false;

	// Acumulador concurrente de rueda del ratón
	private volatile int rotacionRuedaEDT = 0;
	private int rotacionRuedaConsumida = 0;

	private final GestorTiempo GT = new GestorTiempo();
	private int tiempoMsEspera = 0;

	private final Rectangle puntoPresionado = new Rectangle(0, 0, 1, 1);
	private final Point puntoPosicionEscalado = new Point();
	private final Rectangle rectanguloPosicionEscalado = new Rectangle(0, 0, 1, 1);
	private final Point puntoMundoCamara = new Point();
	private final Rectangle rectanguloMundoCamara = new Rectangle(0, 0, 1, 1);

	public Raton() {
		this.posicion = new Point();
		this.click = false;
	}

	public void actualizar(final SuperficieDibujo sd) {
		this.actualizarPresionadosUnicaVez();
		// Transferencia atómica de la rueda del ratón al tick lógico actual
		this.rotacionRuedaConsumida = this.rotacionRuedaEDT;
		this.rotacionRuedaEDT = 0;
	}

	private void actualizarPresionadosUnicaVez() {
		if (this.latchIzq) {
			this.presionadoIzqUnicaAct = true;
			this.latchIzq = false;
		} else {
			this.presionadoIzqUnicaAct = false;
		}

		if (this.latchDer) {
			this.presionadoDerUnicaAct = true;
			this.latchDer = false;
		} else {
			this.presionadoDerUnicaAct = false;
		}
	}

	public void dibujar(final Graphics2D g) {
		Render2D.dibujarString(g, "RX: " + this.posicion.x, 20, 200, Color.RED);
		Render2D.dibujarString(g, "RY: " + this.posicion.y, 20, 210, Color.RED);
		Render2D.dibujarRectanguloContorno(g, this.getRectanguloPosicionEscalado(), Color.BLUE);
	}

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
			this.latchIzq = true;
		} else if (SwingUtilities.isRightMouseButton(e)) {
			this.presionadoDerecho = true;
			this.latchDer = true;
		}

		final int escX = (int) (this.posicion.x / Globales.FACTOR_ESCALADO_X);
		final int escY = (int) (this.posicion.y / Globales.FACTOR_ESCALADO_Y);
		this.puntoPresionado.setBounds(escX, escY, 1, 1);
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

	@Override
	public void mouseWheelMoved(final MouseWheelEvent e) {
		this.rotacionRuedaEDT += e.getWheelRotation();
	}

	public Point getPuntoPosicionSinEscalar() {
		return this.posicion;
	}

	public Point getPuntoPosicionEscalado() {
		this.puntoPosicionEscalado.setLocation((int) (this.posicion.x / Globales.FACTOR_ESCALADO_X),
				(int) (this.posicion.y / Globales.FACTOR_ESCALADO_Y));
		return this.puntoPosicionEscalado;
	}

	public int getPosicionXEscalada() {
		return (int) (this.posicion.x / Globales.FACTOR_ESCALADO_X);
	}

	public int getPosicionYEscalada() {
		return (int) (this.posicion.y / Globales.FACTOR_ESCALADO_Y);
	}

	public Rectangle getRectanguloPosicionEscalado() {
		this.rectanguloPosicionEscalado.setBounds((int) (this.posicion.x / Globales.FACTOR_ESCALADO_X),
				(int) (this.posicion.y / Globales.FACTOR_ESCALADO_Y), 1, 1);
		return this.rectanguloPosicionEscalado;
	}

	public Rectangle getRectanguloPosicionEscaladoConDesplazamientoCamara() {
		final Point p = this.getPuntoPosicionEscaladoConDesplazamientoCamara();
		this.rectanguloMundoCamara.setBounds(p.x, p.y, 1, 1);
		return this.rectanguloMundoCamara;
	}

	public Point getPuntoPosicionEscaladoConDesplazamientoCamara() {
		final double z = (Globales.CAMARA != null) ? Globales.CAMARA.getZoomFinal() : 1.0;
		final double shakeX = (Globales.CAMARA != null) ? Globales.CAMARA.getGestorEfectos().getOffsetX() : 0.0;
		final double shakeY = (Globales.CAMARA != null) ? Globales.CAMARA.getGestorEfectos().getOffsetY() : 0.0;
		final double rot = (Globales.CAMARA != null) ? Globales.CAMARA.getGestorEfectos().getAnguloRotacion() : 0.0;

		final double xScreen = this.posicion.x / Globales.FACTOR_ESCALADO_X;
		final double yScreen = this.posicion.y / Globales.FACTOR_ESCALADO_Y;

		final double dx = xScreen - (Constantes.CENTROX + shakeX);
		final double dy = yScreen - (Constantes.CENTROY + shakeY);

		final double sx = dx / z;
		final double sy = dy / z;

		final double cos = Math.cos(rot);
		final double sin = Math.sin(rot);
		final double rx = (sx * cos) + (sy * sin);
		final double ry = (-sx * sin) + (sy * cos);

		final int xVirtual = Constantes.CENTROX + (int) Math.round(rx);
		final int yVirtual = Constantes.CENTROY + (int) Math.round(ry);

		final int camX = (Globales.CAMARA != null) ? Globales.CAMARA.getPosicionXInt() : 0;
		final int camY = (Globales.CAMARA != null) ? Globales.CAMARA.getPosicionYInt() : 0;
		final int margenX = (Globales.CAMARA != null) ? Globales.CAMARA.getMargenX() : Constantes.CENTROX;
		final int margenY = (Globales.CAMARA != null) ? Globales.CAMARA.getMargenY() : Constantes.CENTROY;

		final int worldX = (xVirtual - margenX) + camX;
		final int worldY = (yVirtual - margenY) + camY;

		this.puntoMundoCamara.setLocation(worldX, worldY);
		return this.puntoMundoCamara;
	}

	public boolean getClick() {
		return this.click;
	}

	public void reiniciarClick() {
		this.click = false;
	}

	public boolean presionadoClickIzq() {
		return this.presionadoIzquierdo;
	}

	public boolean presionadoClickDer() {
		return this.presionadoDerecho;
	}

	public boolean presionadoClickIzqUnicaAct() {
		return this.presionadoIzqUnicaAct;
	}

	public boolean presionadoClickDerUnicaAct() {
		return this.presionadoDerUnicaAct;
	}

	public Rectangle getPuntoPresionado() {
		return this.puntoPresionado;
	}

	public int getRotacionRueda() {
		return this.rotacionRuedaConsumida;
	}

	public void dormirMS(final int ms) {
		this.tiempoMsEspera = ms;
		this.GT.establecerReferenciaTiempoActual();
	}

	public void soltar() {
		this.presionadoIzquierdo = false;
		this.presionadoDerecho = false;
		this.presionadoIzqUnicaAct = false;
		this.presionadoDerUnicaAct = false;
		this.latchIzq = false;
		this.latchDer = false;
		this.rotacionRuedaEDT = 0;
		this.rotacionRuedaConsumida = 0;
	}
}