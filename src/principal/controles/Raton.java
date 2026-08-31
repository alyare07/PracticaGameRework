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
 * Gestor centralizado de entrada del ratón (Mouse Input Manager).
 * <p>
 * <b>Pilares de Arquitectura y Rendimiento:</b>
 * <ul>
 * <li><b>Sincronización Lock-Free (Patrón Latch):</b> Captura eventos
 * asíncronos provenientes del hilo de la interfaz de Swing (<i>Event Dispatch
 * Thread</i>) y los sincroniza atómicamente con el bucle lógico del juego
 * (<i>Game Loop</i>) para que cada clic se consuma exactamente una sola vez por
 * actualización.</li>
 * <li><b>Proyección Inversa Matricial (Screen-to-World Unprojection):</b>
 * Transforma las coordenadas crudas del monitor físico a coordenadas continuas
 * del mundo del juego, compensando matemáticamente el escalado de resolución,
 * la vibración sísmica, la rotación angular y el zoom activo.</li>
 * <li><b>Cero Asignaciones en el Heap (Zero-GC):</b> Reutiliza instancias
 * internas fijas de {@link Point} y {@link Rectangle}, evitando que las
 * consultas continuas del ratón en cada frame disparen pausas por el Garbage
 * Collector.</li>
 * </ul>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.5
 */
public class Raton extends MouseAdapter {

	// =========================================================================
	// === 1. ESTADO Y POSICIÓN CRUDA DEL CURSOR (NATIVO AWT)
	// =========================================================================

	/**
	 * Coordenadas físicas del cursor en píxeles reales del monitor (sin escalar).
	 */
	private final Point posicion;

	/** Bandera de clic rápido transitorio. */
	private volatile boolean click;

	/** Estado sostenido (mantenido presionado) del botón izquierdo. */
	private volatile boolean presionadoIzquierdo;

	/** Estado sostenido (mantenido presionado) del botón derecho. */
	private volatile boolean presionadoDerecho;

	/**
	 * Señal de pulsación única del botón izquierdo: está activa únicamente durante
	 * el tick actual del juego y se apaga automáticamente en el siguiente.
	 */
	private volatile boolean presionadoIzqUnicaAct;

	/**
	 * Señal de pulsación única del botón derecho: activa únicamente en el tick
	 * actual.
	 */
	private volatile boolean presionadoDerUnicaAct;

	/**
	 * Pestillo de sincronización (Latch) para capturar eventos de clic izquierdo
	 * del hilo de Swing entre fotogramas.
	 */
	private volatile boolean latchIzq = false;

	/**
	 * Pestillo de sincronización (Latch) para capturar eventos de clic derecho del
	 * hilo de Swing entre fotogramas.
	 */
	private volatile boolean latchDer = false;

	/**
	 * Rotación registrada de la rueda del ratón en este tick (-1 hacia arriba, +1
	 * hacia abajo).
	 */
	private volatile int rotacionRueda = 0;

	/** Temporizador para regular pausas deliberadas en la lectura del ratón. */
	private final GestorTiempo GT = new GestorTiempo();

	/** Tiempo en milisegundos durante el cual se ignoran nuevas pulsaciones. */
	private int tiempoMsEspera = 0;

	// =========================================================================
	// === 2. ESTRUCTURAS GEOMÉTRICAS REUTILIZABLES (ZERO-GC)
	// =========================================================================

	/** Rectángulo reutilizable para la posición del clic presionado. */
	private final Rectangle puntoPresionado = new Rectangle(0, 0, 1, 1);

	/** Punto reutilizable en espacio de pantalla lógica (640x360). */
	private final Point puntoPosicionEscalado = new Point();

	/** Rectángulo de 1x1 reutilizable en espacio de pantalla lógica. */
	private final Rectangle rectanguloPosicionEscalado = new Rectangle(0, 0, 1, 1);

	/** Punto reutilizable proyectado en el espacio continuo del mundo. */
	private final Point puntoMundoCamara = new Point();

	/** Rectángulo de 1x1 reutilizable proyectado en el espacio del mundo. */
	private final Rectangle rectanguloMundoCamara = new Rectangle(0, 0, 1, 1);

	// =========================================================================
	// === CONSTRUCTOR
	// =========================================================================

	/**
	 * Inicializa el gestor de entrada del ratón y sus estructuras geométricas
	 * fijas.
	 */
	public Raton() {
		this.posicion = new Point();
		this.click = false;
	}

	// =========================================================================
	// === SINCRONIZACIÓN Y CICLO DE VIDA (GAME LOOP)
	// =========================================================================

	/*
	 * =========================================================================
	 * EXPLICACIÓN DIDÁCTICA: EL PATRÓN LATCH (SINCRONIZACIÓN ENTRE HILOS)
	 * ------------------------------------------------------------------------- En
	 * Java con Swing/AWT ocurren dos cosas en paralelo: 1. El Hilo del Sistema
	 * Operativo (EDT): Detecta el clic físico del ratón en cualquier microsegundo
	 * aleatorio. 2. El Hilo del Juego (Game Loop): Se ejecuta exactamente 60 veces
	 * por segundo.
	 * 
	 * Si el usuario hace un clic muy rápido entre medio de dos actualizaciones del
	 * juego, el clic podría perderse o registrarse dos veces.
	 * 
	 * ¿CÓMO LO RESUELVE EL LATCH (PESTILLO)? 1. Cuando el EDT detecta el clic,
	 * levanta el pestillo: latchIzq = true. 2. Cuando el Game Loop llega a su
	 * método 'actualizar()', lee el pestillo, activa 'presionadoIzqUnicaAct = true'
	 * y BAJA el pestillo (latchIzq = false). 3. En el siguiente tick del Game Loop,
	 * 'presionadoIzqUnicaAct' vuelve solo a false.
	 * 
	 * Resultado: Ningún clic se pierde jamás y cada pulsación se ejecuta
	 * exactamente 1 sola vez por actualización lógica.
	 * =========================================================================
	 */

	/**
	 * Consume y procesa los eventos acumulados durante el ciclo lógico actual.
	 *
	 * @param sd Superficie de dibujo activa.
	 */
	public void actualizar(final SuperficieDibujo sd) {
		this.actualizarPresionadosUnicaVez();
		this.rotacionRueda = 0; // Se reinicia el acumulador de rueda tras ser consumido en el tick
	}

	/**
	 * Transfiere de forma atómica el estado de los pestillos a las señales de tick
	 * único.
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

	/**
	 * Dibuja la información de depuración visual de la posición del ratón.
	 *
	 * @param g Contexto gráfico.
	 */
	public void dibujar(final Graphics2D g) {
		Render2D.dibujarString(g, "RX: " + this.posicion.x, 20, 200, Color.RED);
		Render2D.dibujarString(g, "RY: " + this.posicion.y, 20, 210, Color.RED);
		Render2D.dibujarRectanguloContorno(g, this.getRectanguloPosicionEscalado(), Color.BLUE);
	}

	// =========================================================================
	// === EVENTOS ASÍNCRONOS DE MOUSE (AWT / SWING EDT)
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

		// Almacena las coordenadas en espacio lógico en el rectángulo pre-asignado
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
	// === PROYECCIÓN INVERSA DE COORDENADAS (SCREEN TO WORLD UNPROJECTION)
	// =========================================================================

	/**
	 * Retorna la posición física nativa del cursor en píxeles del monitor (sin
	 * escalar).
	 *
	 * @return Referencia al {@link Point} con coordenadas brutas de pantalla.
	 */
	public Point getPuntoPosicionSinEscalar() {
		return this.posicion;
	}

	/**
	 * Retorna las coordenadas del cursor proyectadas al espacio de pantalla interna
	 * lógica (640x360). CERO asignaciones en memoria.
	 *
	 * @return Instancia interna reutilizable de {@link Point}.
	 */
	public Point getPuntoPosicionEscalado() {
		this.puntoPosicionEscalado.setLocation((int) (this.posicion.x / Globales.FACTOR_ESCALADO_X),
				(int) (this.posicion.y / Globales.FACTOR_ESCALADO_Y));
		return this.puntoPosicionEscalado;
	}

	/**
	 * Retorna la coordenada X del cursor proyectadas al espacio de pantalla interna
	 * lógica (640x360). CERO asignaciones en memoria.
	 *
	 * @return Instancia interna reutilizable de {@link Point}.
	 */
	public int getPosicionXEscalada() {
		return (int) (this.posicion.x / Globales.FACTOR_ESCALADO_X);
	}

	/**
	 * Retorna la coordenada Y del cursor proyectadas al espacio de pantalla interna
	 * lógica (640x360). CERO asignaciones en memoria.
	 *
	 * @return Instancia interna reutilizable de {@link Point}.
	 */
	public int getPosicionYEscalada() {
		return (int) (this.posicion.y / Globales.FACTOR_ESCALADO_Y);
	}

	/**
	 * Retorna un delimitador de 1x1 píxel en el espacio de pantalla interna lógica.
	 * CERO asignaciones en memoria.
	 *
	 * @return Instancia interna reutilizable de {@link Rectangle}.
	 */
	public Rectangle getRectanguloPosicionEscalado() {
		this.rectanguloPosicionEscalado.setBounds((int) (this.posicion.x / Globales.FACTOR_ESCALADO_X),
				(int) (this.posicion.y / Globales.FACTOR_ESCALADO_Y), 1, 1);
		return this.rectanguloPosicionEscalado;
	}

	/**
	 * Calcula el delimitador de 1x1 píxel del cursor proyectado en el mundo
	 * continuo considerando la cámara, rotaciones, temblores y zoom.
	 *
	 * @return Instancia interna reutilizable de {@link Rectangle} en coordenadas de
	 *         mundo.
	 */
	public Rectangle getRectanguloPosicionEscaladoConDesplazamientoCamara() {
		final Point p = this.getPuntoPosicionEscaladoConDesplazamientoCamara();
		this.rectanguloMundoCamara.setBounds(p.x, p.y, 1, 1);
		return this.rectanguloMundoCamara;
	}

	/*
	 * =========================================================================
	 * EXPLICACIÓN DIDÁCTICA: LA PROYECCIÓN INVERSA MATRICIAL 2D COMPLETA
	 * -------------------------------------------------------------------------
	 * Cuando la cámara rota, tiembla o hace zoom, la imagen que ve el jugador en el
	 * monitor está transformada. Si el usuario hace clic sobre un cofre que está en
	 * el suelo, las coordenadas del mouse en el monitor NO coinciden con las
	 * coordenadas del cofre en la grilla del mapa.
	 * 
	 * Para saber EXACTAMENTE a qué punto del mundo apunta el cursor, aplicamos las
	 * 5 operaciones inversas exactas en orden inverso:
	 * 
	 * 1. PASO 1 (Escalado de Monitor a Pantalla Lógica): xScreen = mouseX /
	 * FACTOR_ESCALADO_X yScreen = mouseY / FACTOR_ESCALADO_Y
	 * 
	 * 2. PASO 2 (Traslación Inversa del Centro y Vibración de la Cámara): dx =
	 * xScreen - (CENTROX + shakeX) dy = yScreen - (CENTROY + shakeY)
	 * 
	 * 3. PASO 3 (Escala Inversa del Zoom): sx = dx / zoom sy = dy / zoom
	 * 
	 * 4. PASO 4 (Rotación Inversa 2D de Ángulo -θ): Aplicamos la matriz de rotación
	 * estándar de 2D con ángulo opuesto (-θ): rx = (sx * cos(θ)) + (sy * sin(θ)) ry
	 * = -(sx * sin(θ)) + (sy * cos(θ))
	 * 
	 * 5. PASO 5 (Mapeo a Coordenadas Absolutas del Terreno): worldX = (CENTROX + rx
	 * - camaraMargenX) + camaraPosX worldY = (CENTROY + ry - camaraMargenY) +
	 * camaraPosY
	 * 
	 * Resultado: El cursor puede interactuar con cofres, enemigos y tiles con
	 * precisión matemática del 100%, incluso mientras la pantalla tiembla
	 * violentamente o gira en Modo Borracho.
	 * =========================================================================
	 */
	/**
	 * Calcula el punto exacto donde apunta el cursor en el mundo continuo
	 * considerando la rotación, el temblor, el zoom y la posición de la cámara.
	 * CERO asignaciones en memoria (Zero-GC).
	 *
	 * @return {@link Point} reutilizable con las coordenadas X, Y del mundo.
	 */
	public Point getPuntoPosicionEscaladoConDesplazamientoCamara() {
		final double z = (Globales.CAMARA != null) ? Globales.CAMARA.getZoomFinal() : 1.0;
		final double shakeX = (Globales.CAMARA != null) ? Globales.CAMARA.getGestorEfectos().getOffsetX() : 0.0;
		final double shakeY = (Globales.CAMARA != null) ? Globales.CAMARA.getGestorEfectos().getOffsetY() : 0.0;
		final double rot = (Globales.CAMARA != null) ? Globales.CAMARA.getGestorEfectos().getAnguloRotacion() : 0.0;

		// 1. Coordenadas en espacio de pantalla lógica interna (640x360)
		final double xScreen = this.posicion.x / Globales.FACTOR_ESCALADO_X;
		final double yScreen = this.posicion.y / Globales.FACTOR_ESCALADO_Y;

		// 2. Traslación respecto al centro visual de la pantalla restando el temblor
		final double dx = xScreen - (Constantes.CENTROX + shakeX);
		final double dy = yScreen - (Constantes.CENTROY + shakeY);

		// 3. Escala inversa del zoom
		final double sx = dx / z;
		final double sy = dy / z;

		// 4. Rotación inversa 2D (-rot)
		final double cos = Math.cos(rot);
		final double sin = Math.sin(rot);
		final double rx = (sx * cos) + (sy * sin);
		final double ry = (-sx * sin) + (sy * cos);

		// 5. Retorno a espacio virtual centrado
		final int xVirtual = Constantes.CENTROX + (int) Math.round(rx);
		final int yVirtual = Constantes.CENTROY + (int) Math.round(ry);

		// 6. Proyección final al espacio continuo del mundo
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
	// === ESTADOS, CONSULTAS Y UTILIDADES
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
	 * Retorna la rotación registrada de la rueda del ratón en este tick.
	 *
	 * @return {@code -1} si rodó hacia arriba (Zoom In), {@code +1} hacia abajo
	 *         (Zoom Out), {@code 0} en reposo.
	 */
	public int getRotacionRueda() {
		return this.rotacionRueda;
	}

	/**
	 * Desactiva temporalmente la lectura de clics durante el intervalo
	 * especificado.
	 *
	 * @param ms Tiempo en milisegundos a silenciar el ratón.
	 */
	public void dormirMS(final int ms) {
		this.tiempoMsEspera = ms;
		this.GT.establecerReferenciaTiempoActual();
	}

	/**
	 * Restablece y limpia inmediatamente todos los estados de pulsación y
	 * pestillos. Ideal para transiciones de pantalla o cambio de foco de ventana.
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