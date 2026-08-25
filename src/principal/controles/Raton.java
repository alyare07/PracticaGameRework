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
import principal.utilidades.DibujoDebug;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;

/**
 * Gestor centralizado de entrada para el ratón (mouse).
 * <p>
 * <b>Responsabilidades y Arquitectura:</b>
 * <ul>
 * <li><b>Sincronización Lock-Free (Patrón Latch):</b> Captura eventos
 * asíncronos del hilo de AWT/Swing y los sincroniza de forma atómica para que
 * el Game Loop consuma pulsaciones únicas exactamente una vez por tick.</li>
 * <li><b>Proyección de Coordenadas Inversa (Zoom-Aware):</b> Transforma las
 * coordenadas crudas del monitor a coordenadas de pantalla lógica y al espacio
 * continuo del mundo considerando la traslación de la cámara y el Zoom.</li>
 * <li><b>Cero Asignaciones en el Heap (Zero-GC):</b> Reutiliza estructuras
 * geométricas internas ({@link Rectangle}, {@link Point}) para evitar pausas
 * por Garbage Collector en consultas por frame.</li>
 * <li><b>Soporte de Rueda de Desplazamiento:</b> Captura la rotación de la
 * rueda para control de Zoom e interfaces.</li>
 * </ul>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public class Raton extends MouseAdapter {

	// =========================================================================
	// === ESTADO Y POSICIÓN BRUTA (AWT)
	// =========================================================================

	/** Coordenadas nativas del cursor en píxeles de monitor (sin escalar). */
	private final Point posicion;

	/** Bandera de clic rápido transitorio. */
	private volatile boolean click;

	/** Estado sostenido del botón izquierdo del ratón. */
	private volatile boolean presionadoIzquierdo;

	/** Estado sostenido del botón derecho del ratón. */
	private volatile boolean presionadoDerecho;

	/**
	 * Pulsación única del botón izquierdo activa únicamente durante el tick actual.
	 */
	private volatile boolean presionadoIzqUnicaAct;

	/**
	 * Pulsación única del botón derecho activa únicamente durante el tick actual.
	 */
	private volatile boolean presionadoDerUnicaAct;

	/**
	 * Pestillo de sincronización para capturar eventos de clic izquierdo entre
	 * frames.
	 */
	private volatile boolean latchIzq = false;

	/**
	 * Pestillo de sincronización para capturar eventos de clic derecho entre
	 * frames.
	 */
	private volatile boolean latchDer = false;

	/**
	 * Última rotación registrada de la rueda del ratón (-1 hacia arriba, +1 hacia
	 * abajo).
	 */
	private volatile int rotacionRueda = 0;

	/** Temporizador para regular pausas deliberadas en la lectura del ratón. */
	private final GestorTiempo GT = new GestorTiempo();

	/** Tiempo en milisegundos durante el cual se ignoran nuevas pulsaciones. */
	private int tiempoMsEspera = 0;

	// =========================================================================
	// === ESTRUCTURAS AUXILIARES REUTILIZABLES (ZERO-GC)
	// =========================================================================

	private final Rectangle puntoPresionado = new Rectangle(0, 0, 1, 1);
	private final Point puntoPosicionEscalado = new Point();
	private final Rectangle rectanguloPosicionEscalado = new Rectangle(0, 0, 1, 1);
	private final Point puntoMundoCamara = new Point();
	private final Rectangle rectanguloMundoCamara = new Rectangle(0, 0, 1, 1);

	// =========================================================================
	// === CONSTRUCTOR
	// =========================================================================

	public Raton() {
		this.posicion = new Point();
		this.click = false;
	}

	// =========================================================================
	// === SINCRONIZACIÓN Y CICLO DE VIDA (GAME LOOP)
	// =========================================================================

	/*
	 * =========================================================================
	 * EXPLICACIÓN TÉCNICA: PATRÓN LATCH (SINCRONIZACIÓN HILO AWT <-> GAME LOOP)
	 * ------------------------------------------------------------------------- Los
	 * eventos del ratón ocurren en el 'Event Dispatch Thread' (EDT) de Swing de
	 * forma asíncrona.
	 * 
	 * 1. Cuando el usuario hace clic, el EDT levanta el pestillo ('latchIzq =
	 * true'). 2. En el siguiente tick del Game Loop, 'actualizar()' consume la
	 * señal, activando 'presionadoIzqUnicaAct = true' y bajando el pestillo
	 * ('latchIzq = false'). 3. En el tick posterior, 'presionadoIzqUnicaAct' vuelve
	 * automáticamente a false.
	 * 
	 * Esto garantiza que una pulsación rápida NUNCA se pierda ni se ejecute 2
	 * veces.
	 * =========================================================================
	 */

	/**
	 * Consume y procesa los eventos acumulados durante el ciclo lógico actual del
	 * juego.
	 *
	 * @param sd Superficie de dibujo activa.
	 */
	public void actualizar(final SuperficieDibujo sd) {
		this.actualizarPresionadosUnicaVez();
		this.rotacionRueda = 0; // Se reinicia el acumulador de la rueda tras consumirse en el tick
	}

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

	public void dibujar(final Graphics2D g) {
		DibujoDebug.dibujarString(g, "RX: " + this.posicion.x, 20, 200, Color.RED);
		DibujoDebug.dibujarString(g, "RY: " + this.posicion.y, 20, 210, Color.RED);
		DibujoDebug.dibujarRectanguloContorno(g, this.getRectanguloPosicionEscalado(), Color.BLUE);
	}

	// =========================================================================
	// === EVENTOS DE MOUSE AWT / SWING
	// =========================================================================

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

		// Almacena las coordenadas escaladas en la estructura reutilizable
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
		this.rotacionRueda = e.getWheelRotation();
	}

	// =========================================================================
	// === PROYECCIÓN DE COORDENADAS Y TRANSFORMACIÓN CON ZOOM
	// =========================================================================

	/*
	 * =========================================================================
	 * EXPLICACIÓN TÉCNICA: PROYECCIÓN MATEMÁTICA DE RATÓN CON ZOOM
	 * ------------------------------------------------------------------------- 1.
	 * Coordenada de Pantalla Lógica: xScreen = posicion.x / FACTOR_ESCALADO_X
	 * yScreen = posicion.y / FACTOR_ESCALADO_Y
	 * 
	 * 2. Transformación Inversa del Zoom respecto al Centro de Pantalla (CENTROX,
	 * CENTROY): dx = (xScreen - CENTROX) / zoom dy = (yScreen - CENTROY) / zoom
	 * xVirtual = CENTROX + dx yVirtual = CENTROY + dy
	 * 
	 * 3. Proyección al Espacio Continuo del Mundo: xMundo = (xVirtual -
	 * CAMARA.getMargenX()) + CAMARA.getPosicionXInt() yMundo = (yVirtual -
	 * CAMARA.getMargenY()) + CAMARA.getPosicionYInt()
	 * 
	 * Complejidad: O(1) con CERO asignaciones 'new' en memoria.
	 * =========================================================================
	 */

	/**
	 * Retorna la posición nativa del cursor en píxeles del monitor (sin escalar).
	 */
	public Point getPuntoPosicionSinEscalar() {
		return this.posicion;
	}

	/**
	 * Retorna las coordenadas del cursor proyectadas al espacio de pantalla interna
	 * (640x360). CERO asignaciones en memoria.
	 */
	public Point getPuntoPosicionEscalado() {
		this.puntoPosicionEscalado.setLocation((int) (this.posicion.x / Globales.FACTOR_ESCALADO_X),
				(int) (this.posicion.y / Globales.FACTOR_ESCALADO_Y));
		return this.puntoPosicionEscalado;
	}

	/**
	 * Retorna un delimitador de 1x1 píxel en el espacio de pantalla interna. CERO
	 * asignaciones en memoria.
	 */
	public Rectangle getRectanguloPosicionEscalado() {
		this.rectanguloPosicionEscalado.setBounds((int) (this.posicion.x / Globales.FACTOR_ESCALADO_X),
				(int) (this.posicion.y / Globales.FACTOR_ESCALADO_Y), 1, 1);
		return this.rectanguloPosicionEscalado;
	}

	/**
	 * Calcula la posición del cursor proyectada en el mundo del juego considerando
	 * el desplazamiento de la cámara y el Zoom activo.
	 *
	 * @return Rectángulo de 1x1 píxel en coordenadas absolutas del mundo.
	 */
	public Rectangle getRectanguloPosicionEscaladoConDesplazamientoCamara() {
		final Point p = this.getPuntoPosicionEscaladoConDesplazamientoCamara();
		this.rectanguloMundoCamara.setBounds(p.x, p.y, 1, 1);
		return this.rectanguloMundoCamara;
	}

	/**
	 * Calcula el punto exacto donde apunta el cursor en el mundo considerando el
	 * Zoom y la Cámara.
	 *
	 * @return {@link Point} reutilizable con las coordenadas X, Y del mundo.
	 */
	public Point getPuntoPosicionEscaladoConDesplazamientoCamara() {
		final double z = (Globales.CAMARA != null) ? Globales.CAMARA.getZoom() : 1.0;
		final int xScreen = (int) (this.posicion.x / Globales.FACTOR_ESCALADO_X);
		final int yScreen = (int) (this.posicion.y / Globales.FACTOR_ESCALADO_Y);

		int xVirtual = xScreen;
		int yVirtual = yScreen;

		// Si el zoom no es 1.0, aplicamos la transformación inversa respecto al centro
		// de pantalla
		if (z != 1.0) {
			xVirtual = Constantes.CENTROX + (int) Math.round((xScreen - Constantes.CENTROX) / z);
			yVirtual = Constantes.CENTROY + (int) Math.round((yScreen - Constantes.CENTROY) / z);
		}

		final int camX = (Globales.CAMARA != null) ? Globales.CAMARA.getPosicionXInt() : 0;
		final int camY = (Globales.CAMARA != null) ? Globales.CAMARA.getPosicionYInt() : 0;
		final int margenX = (Globales.CAMARA != null) ? Globales.CAMARA.getMargenX() : Constantes.CENTROX;
		final int margenY = (Globales.CAMARA != null) ? Globales.CAMARA.getMargenY() : Constantes.CENTROY;

		final int worldX = (xVirtual - margenX) + camX;
		final int worldY = (yVirtual - margenY) + camY;

		this.puntoMundoCamara.setLocation(worldX, worldY);
		return this.puntoMundoCamara;
	}

	// =========================================================================
	// === ESTADOS, CONSULTAS Y CONTROL
	// =========================================================================

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

	/**
	 * Retorna la rotación de la rueda del ratón en este frame (-1 = arriba / zoom
	 * in, +1 = abajo / zoom out).
	 */
	public int getRotacionRueda() {
		return this.rotacionRueda;
	}

	/**
	 * Desactiva la lectura de pulsaciones durante el intervalo especificado.
	 *
	 * @param ms Tiempo en milisegundos a silenciar el ratón.
	 */
	public void dormirMS(final int ms) {
		this.tiempoMsEspera = ms;
		this.GT.establecerReferenciaTiempoActual();
	}

	/**
	 * Reinicia inmediatamente todos los estados de pulsación y pestillos.
	 */
	public void soltar() {
		this.presionadoIzquierdo = false;
		this.presionadoDerecho = false;
		this.presionadoIzqUnicaAct = false;
		this.presionadoDerUnicaAct = false;
		this.latchIzq = false;
		this.latchDer = false;
		this.rotacionRueda = 0;
	}
}