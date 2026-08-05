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

public class Raton extends MouseAdapter {
	private Point posicion;
	private boolean click;
	private boolean presionadoDerecho;
	private boolean presionadoIzquierdo;
	private Rectangle puntoPresionado;
	private final GestorTiempo GT = new GestorTiempo();
	private int tiempoMsEspera = 0;
	private int codActPresionadoClickDer;
	private int codActPresionadoClickIzq;
	private boolean presionadoDerUnicaAct;
	private boolean presionadoIzqUnicaAct;
	private boolean disponibleParaPresionarIzqUnicaAct = true;
	private boolean disponibleParaPresionarDerUnicaAct = true;
	private boolean actualizarCodPresIzqUnicaAct;
	private boolean actualizarCodPresDerUnicaAct;

	public Raton() {
		this.posicion = new Point();
		this.click = false;
	}
	
	public void actualizarPresionadosUnicaVez() {
		if (presionadoIzquierdo){
			if (!actualizarCodPresIzqUnicaAct && this.disponibleParaPresionarIzqUnicaAct) {
				this.codActPresionadoClickIzq = Constantes.getCodActualizacion();
				this.actualizarCodPresIzqUnicaAct = true;
				this.disponibleParaPresionarIzqUnicaAct = false;
			}else  if(actualizarCodPresIzqUnicaAct && Constantes.getCodActualizacion() >= (this.codActPresionadoClickIzq) ) {
				this.actualizarCodPresIzqUnicaAct = false;
				this.presionadoIzqUnicaAct = true;
//				System.out.println("click izq "+Constantes.getCodActualizacion());
			}else if(this.presionadoIzqUnicaAct) {
				this.presionadoIzqUnicaAct = false;
//				System.out.println("izq soltado");
			}
	    }
		if(this.presionadoDerecho){
	    	if (!actualizarCodPresDerUnicaAct && this.disponibleParaPresionarDerUnicaAct) {
	    		this.codActPresionadoClickDer = Constantes.getCodActualizacion();
	    		this.actualizarCodPresDerUnicaAct = true;
	    		this.disponibleParaPresionarDerUnicaAct = false;
	    	}else  if(actualizarCodPresDerUnicaAct && Constantes.getCodActualizacion() >= (this.codActPresionadoClickDer)) {
				this.presionadoDerUnicaAct = true;
				this.actualizarCodPresDerUnicaAct = false;
//				System.out.println("click der "+Constantes.getCodActualizacion());
			}else if(this.presionadoDerUnicaAct) {
				this.presionadoDerUnicaAct = false;
//				System.out.println("der soltado");
			}
	    	
	    }
	}

	public void actualizar(final SuperficieDibujo sd) {
		actualizarPosicion(sd);
		this.actualizarPresionadosUnicaVez();

	}

	public void dibujar(final Graphics2D g) {
		DibujoDebug.dibujarString(g, "RX: " + posicion.x, 20, 200, Color.RED);
		DibujoDebug.dibujarString(g, "RY: " + posicion.y, 20, 210, Color.red);
		DibujoDebug.dibujarRectanguloContorno(g, getRectanguloPosicionEscalado(), Color.BLUE);
	}

	private void actualizarPosicion(final SuperficieDibujo sd) {
		final Point posicionInicial = MouseInfo.getPointerInfo().getLocation();
		SwingUtilities.convertPointFromScreen(posicionInicial, sd);
		this.posicion.setLocation(posicionInicial);
	}

	/**
	 * El punto donde se ubica el puntero. No tiene en cuenta
	 * el escalado de la pantalla.
	 * NO se tiene en cuenta el desplazamiento de la camara!
	 * 
	 * @return El punto donde se ubica el puntero (SIN ESCALAR)
	 */
	public Point getPuntoPosicionSinEscalar() {
		return posicion;
	}
	/**
	 * El punto donde se ubica el puntero. Se tiene en cuenta
	 * el escalado de la pantalla.
	 * NO se tiene en cuenta el desplazamiento de la camara!
	 * 
	 * @return El punto donde se ubica el puntero (ESCALADO)
	 */
	public Point getPuntoPosicionEscalado() {
		return new Point((int) (posicion.x / Constantes.FACTOR_ESCALADO_X), (int) (posicion.y / Constantes.FACTOR_ESCALADO_Y));
	}
	/**
	 * El area donde se ubica el puntero. Se tiene en cuenta
	 * el escalado de la pantalla.
	 * NO se tiene en cuenta el desplazamiento de la camara!
	 * 
	 * @return El area donde se ubica el puntero (ESCALADO)
	 */
	public Rectangle getRectanguloPosicionEscalado() {
		final Rectangle area = new Rectangle((int) (posicion.x / Constantes.FACTOR_ESCALADO_X), (int) (posicion.y / Constantes.FACTOR_ESCALADO_Y), 1, 1);
		return area;
	}
	/**
	 * El area donde se ubica el puntero. Se tiene en cuenta
	 * el escalado de la pantalla.
	 * Se tiene en cuenta el desplazamiento de la camara!
	 * 
	 * @return El area donde se ubica el puntero (ESCALADO Y CON DESPLAZAMIENTO CAMARA)
	 */
	public Rectangle getRectanguloPosicionEscaladoConDesplazamientoCamara() {
		return new Rectangle((int) (posicion.x / Constantes.FACTOR_ESCALADO_X) + Constantes.CAMARA.getPosicionXInt() - Constantes.CAMARA.getMargenX(), (int) (posicion.y / Constantes.FACTOR_ESCALADO_Y + Constantes.CAMARA.getPosicionYInt() - Constantes.CAMARA.getMargenY()), 1, 1);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (this.GT.transcurrioMiliSegundos(tiempoMsEspera)) {
			if (!click) {
				this.tiempoMsEspera = 0;
				click = true;
			}
		}

	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (this.GT.transcurrioMiliSegundos(tiempoMsEspera)) {
			this.GT.establecerReferenciaTiempoActual();
			if (!(presionadoDerecho || presionadoIzquierdo)) {
				this.tiempoMsEspera = 0;
				if (SwingUtilities.isLeftMouseButton(e)){
			        this.presionadoIzquierdo = true;
			    }else if (SwingUtilities.isRightMouseButton(e)) {
			    	this.presionadoDerecho = true;
			    }
				this.puntoPresionado = getRectanguloPosicionEscalado();
			}
			
		}

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		this.presionadoDerecho = false;
		this.presionadoIzquierdo = false;
		if(SwingUtilities.isLeftMouseButton(e)) {
			this.disponibleParaPresionarIzqUnicaAct = true;
		}
		if(SwingUtilities.isRightMouseButton(e)) {
			this.disponibleParaPresionarDerUnicaAct = true;
		}
	}

	public boolean getClick() {
		return click;
	}

	public void reiniciarClick() {
		if (click) {
			click = false;
		}
	}

	/**
	 * Verifica si el click izquierdo del mouse 
	 * esta presionado en dicho momento.
	 * 
	 * @return TRUE si el click izquierdo esta presionado.
	 * 	FALSE si el click izquierdo no esta presionado
	 */
	public boolean presionadoClickIzq() {
		return this.presionadoIzquierdo;
	}
	/**
	 * Verifica si el click derecho del mouse 
	 * esta presionado en dicho momento.
	 * 
	 * @return TRUE si el click derecho esta presionado.
	 * 	FALSE si el click derecho no esta presionado
	 */
	public boolean presionadoClickDer() {
		return this.presionadoDerecho ;
	}
	/**
	 * Verifica si el click izquierdo del mouse 
	 * esta presionado en dicha actualizacion.
	 * Los click por actualizacion solo se tendran en cuenta
	 * en una unica actualizacion. Para un siguiente click se debera
	 * dejar de presionar el mouse y realizar nuevamente el clik.
	 * 
	 * @return TRUE si el click izquierdo esta presionado en dicha actualizacion.
	 * 	FALSE si el click izquierdo no esta presionado en dicha actualizacion
	 */
	public boolean presionadoClickIzqUnicaAct() {
		return this.presionadoIzqUnicaAct;
	}
	/**
	 * Verifica si el click derecho del mouse 
	 * esta presionado en dicha actualizacion.
	 * Los click por actualizacion solo se tendran en cuenta
	 * en una unica actualizacion. Para un siguiente click se debera
	 * dejar de presionar el mouse y realizar nuevamente el clik.
	 * 
	 * @return TRUE si el click derecho esta presionado en dicha actualizacion.
	 * 	FALSE si el click derecho no esta presionado en dicha actualizacion
	 */
	public boolean presionadoClickDerUnicaAct() {
		return this.presionadoDerUnicaAct;
	}
	/**
	 * Area expresado en Rectangulo donde esta presionado el mouse.
	 * Este metodo tiene en cuenta el escalado de la pantalla!
	 * NO se tiene en cuenta el desplazamiento de la camara!
	 * @return El punto donde se presiono, pero expresado en Rectangle.
	 */
	public Rectangle getPuntoPresionado() {
		return puntoPresionado;
	}

	/**
	 * Duerme el detector de click y presionados durante el tiempo especificado.
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
		this.actualizarCodPresIzqUnicaAct = false;
		this.actualizarCodPresDerUnicaAct = false;
	}

}
