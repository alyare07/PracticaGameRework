package principal.controles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import principal.graficos.SuperficieDibujo;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.GestorTiempo;

public class Raton extends MouseAdapter{
    private final Point posicion;
    private boolean click;
    private boolean presionadoDerecho;
    private boolean presionadoIzquierdo;
    private Rectangle puntoPresionado;
    private final GestorTiempo GT = new GestorTiempo();
    private int tiempoMsEspera = 0;
    private boolean presionadoDerUnicaAct;
    private boolean presionadoIzqUnicaAct;
    private boolean disponibleParaPresionarIzqUnicaAct = true;
    private boolean disponibleParaPresionarDerUnicaAct = true;

    public Raton() {
	this.posicion = new Point();
	this.click = false;
    }

    private void actualizarPresionadosUnicaVez() {
	if (this.presionadoDerecho) {
	    if (this.presionadoDerUnicaAct) {
		this.presionadoDerUnicaAct = false;
		this.disponibleParaPresionarDerUnicaAct = false;
	    } else if (this.disponibleParaPresionarDerUnicaAct) {
		this.presionadoDerUnicaAct = true;
	    }
	} else if (!this.disponibleParaPresionarDerUnicaAct) {
	    this.presionadoDerUnicaAct = false;
	    this.disponibleParaPresionarDerUnicaAct = true;
	}

	if (this.presionadoIzquierdo) {
	    if (this.presionadoIzqUnicaAct) {
		this.presionadoIzqUnicaAct = false;
		this.disponibleParaPresionarIzqUnicaAct = false;
	    } else if (this.disponibleParaPresionarIzqUnicaAct) {
		this.presionadoIzqUnicaAct = true;
	    }
	} else if (!this.disponibleParaPresionarIzqUnicaAct) {
	    this.presionadoIzqUnicaAct = false;
	    this.disponibleParaPresionarIzqUnicaAct = true;
	}
    }

    public void actualizar(final SuperficieDibujo sd) {
	this.actualizarPosicion(sd);
	this.actualizarPresionadosUnicaVez();

    }

    public void dibujar(final Graphics2D g) {
	DibujoDebug.dibujarString(g, "RX: " + this.posicion.x, 20, 200, Color.RED);
	DibujoDebug.dibujarString(g, "RY: " + this.posicion.y, 20, 210, Color.red);
	DibujoDebug.dibujarRectanguloContorno(g, this.getRectanguloPosicionEscalado(), Color.BLUE);
    }

    private void actualizarPosicion(final SuperficieDibujo sd) {
	final Point posicionInicial = MouseInfo.getPointerInfo().getLocation();
	SwingUtilities.convertPointFromScreen(posicionInicial, sd);
	this.posicion.setLocation(posicionInicial);
    }

    /**
     * El punto donde se ubica el puntero. No tiene en cuenta el escalado de la
     * pantalla. NO se tiene en cuenta el desplazamiento de la camara!
     * 
     * @return El punto donde se ubica el puntero (SIN ESCALAR)
     */
    public Point getPuntoPosicionSinEscalar() {
	return this.posicion;
    }

    /**
     * El punto donde se ubica el puntero. Se tiene en cuenta el escalado de la
     * pantalla. NO se tiene en cuenta el desplazamiento de la camara!
     * 
     * @return El punto donde se ubica el puntero (ESCALADO)
     */
    public Point getPuntoPosicionEscalado() {
	return new Point((int) (this.posicion.x / Constantes.FACTOR_ESCALADO_X), (int) (this.posicion.y / Constantes.FACTOR_ESCALADO_Y));
    }

    /**
     * El area donde se ubica el puntero. Se tiene en cuenta el escalado de la
     * pantalla. NO se tiene en cuenta el desplazamiento de la camara!
     * 
     * @return El area donde se ubica el puntero (ESCALADO)
     */
    public Rectangle getRectanguloPosicionEscalado() {
	final Rectangle area = new Rectangle((int) (this.posicion.x / Constantes.FACTOR_ESCALADO_X), (int) (this.posicion.y / Constantes.FACTOR_ESCALADO_Y), 1, 1);
	return area;
    }

    /**
     * El area donde se ubica el puntero. Se tiene en cuenta el escalado de la
     * pantalla. Se tiene en cuenta el desplazamiento de la camara!
     * 
     * @return El area donde se ubica el puntero (ESCALADO Y CON DESPLAZAMIENTO
     *         CAMARA)
     */
    public Rectangle getRectanguloPosicionEscaladoConDesplazamientoCamara() {
	return new Rectangle((int) (this.posicion.x / Constantes.FACTOR_ESCALADO_X) + Constantes.CAMARA.getPosicionXInt() - Constantes.CAMARA.getMargenX(),
		(int) (this.posicion.y / Constantes.FACTOR_ESCALADO_Y + Constantes.CAMARA.getPosicionYInt() - Constantes.CAMARA.getMargenY()), 1, 1);
    }

    public Point getPuntoPosicionEscaladoConDesplazamientoCamara() {
	return new Point((int) (this.posicion.x / Constantes.FACTOR_ESCALADO_X) + Constantes.CAMARA.getPosicionXInt() - Constantes.CAMARA.getMargenX(),
		(int) (this.posicion.y / Constantes.FACTOR_ESCALADO_Y + Constantes.CAMARA.getPosicionYInt() - Constantes.CAMARA.getMargenY()));
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
	if (this.GT.transcurrioMiliSegundos(this.tiempoMsEspera)) {
	    this.GT.establecerReferenciaTiempoActual();
	    if (!(this.presionadoDerecho || this.presionadoIzquierdo)) {
		this.tiempoMsEspera = 0;
		if (SwingUtilities.isLeftMouseButton(e)) {
		    this.presionadoIzquierdo = true;
		} else if (SwingUtilities.isRightMouseButton(e)) {
		    this.presionadoDerecho = true;
		}
		this.puntoPresionado = this.getRectanguloPosicionEscalado();
	    }

	}

    }

    @Override
    public void mouseReleased(final MouseEvent e) {
	this.presionadoDerecho = false;
	this.presionadoIzquierdo = false;
	if (SwingUtilities.isLeftMouseButton(e)) {
	    this.disponibleParaPresionarIzqUnicaAct = true;
	}
	if (SwingUtilities.isRightMouseButton(e)) {
	    this.disponibleParaPresionarDerUnicaAct = true;
	}
    }

    public boolean getClick() {
	return this.click;
    }

    public void reiniciarClick() {
	if (this.click) {
	    this.click = false;
	}
    }

    /**
     * Verifica si el click izquierdo del mouse esta presionado en dicho momento.
     * 
     * @return TRUE si el click izquierdo esta presionado. FALSE si el click
     *         izquierdo no esta presionado
     */
    public boolean presionadoClickIzq() {
	return this.presionadoIzquierdo;
    }

    /**
     * Verifica si el click derecho del mouse esta presionado en dicho momento.
     * 
     * @return TRUE si el click derecho esta presionado. FALSE si el click derecho
     *         no esta presionado
     */
    public boolean presionadoClickDer() {
	return this.presionadoDerecho;
    }

    /**
     * Verifica si el click izquierdo del mouse esta presionado en dicha
     * actualizacion. Los click por actualizacion solo se tendran en cuenta en una
     * unica actualizacion. Para un siguiente click se debera dejar de presionar el
     * mouse y realizar nuevamente el clik.
     * 
     * @return TRUE si el click izquierdo esta presionado en dicha actualizacion.
     *         FALSE si el click izquierdo no esta presionado en dicha actualizacion
     */
    public boolean presionadoClickIzqUnicaAct() {
	return this.presionadoIzqUnicaAct;
    }

    /**
     * Verifica si el click derecho del mouse esta presionado en dicha
     * actualizacion. Los click por actualizacion solo se tendran en cuenta en una
     * unica actualizacion. Para un siguiente click se debera dejar de presionar el
     * mouse y realizar nuevamente el clik.
     * 
     * @return TRUE si el click derecho esta presionado en dicha actualizacion.
     *         FALSE si el click derecho no esta presionado en dicha actualizacion
     */
    public boolean presionadoClickDerUnicaAct() {
	return this.presionadoDerUnicaAct;
    }

    /**
     * Area expresado en Rectangulo donde esta presionado el mouse. Este metodo
     * tiene en cuenta el escalado de la pantalla! NO se tiene en cuenta el
     * desplazamiento de la camara!
     * 
     * @return El punto donde se presiono, pero expresado en Rectangle.
     */
    public Rectangle getPuntoPresionado() {
	return this.puntoPresionado;
    }

    /**
     * Duerme el detector de click y presionados durante el tiempo especificado.
     * 
     * @param ms El tiempo en milisegundos a dormir el mouse.
     */
    public void dormirMS(final int ms) {
	this.tiempoMsEspera = ms;
	this.GT.establecerReferenciaTiempoActual();
    }

    /**
     * Suelta todos los click que haya en el momento.
     */
    public void soltar() {
	this.presionadoIzquierdo = false;
	this.presionadoDerecho = false;
	this.presionadoIzqUnicaAct = false;
	this.presionadoDerUnicaAct = false;
	this.disponibleParaPresionarIzqUnicaAct = true;
	this.disponibleParaPresionarDerUnicaAct = true;
    }

}
