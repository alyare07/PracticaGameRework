package principal.maquinaestado.estados.menu.herramientas;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import principal.utilidades.Render2D;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;

public class CajaTexto extends Componente {
	protected final Rectangle AREA;
	protected final BufferedImage FONDO;
	protected final Color COLOR_TEXTO;
	protected String texto;
	protected boolean seleccionado;
	protected int contadorPuntero;
	protected final GestorTiempo GT_TECLEO = new GestorTiempo();
	protected final GestorTiempo GT_PUNTERO = new GestorTiempo();
	protected final int MS_ESPERA_TECLEO = 150;
	protected final int MS_ESPERA_PUNTERO = 450;
	protected boolean caracterTecla;
	protected boolean numerico;
	protected boolean alfabetico;
	protected boolean alfanumerico = true;
	protected BufferedImage imgTexto;
	protected boolean punteroMostrar;
	protected int limiteCaracteres = 0;

	public CajaTexto(final Rectangle area, final BufferedImage fondo, final Color colorTexto) {
		this.AREA = area;
		this.COLOR_TEXTO = colorTexto;
		this.FONDO = fondo;
		this.texto = "";
	}

	public CajaTexto(final Rectangle area, final Color colorFondo, final Color colorTexto) {
		this.AREA = area;
		this.COLOR_TEXTO = colorTexto;
		this.FONDO = Globales.FUNCIONES.TEXTURAS_TOOLS.crearTextura(colorFondo, area.width, area.height);
		this.texto = "";
	}

	public CajaTexto(final Rectangle area, final Color colorFondo, final Color colorBordes, final Color colorTexto) {
		this.AREA = area;
		this.COLOR_TEXTO = colorTexto;
		this.FONDO = Globales.FUNCIONES.TEXTURAS_TOOLS.crearTextura(colorFondo, area.width, area.height);
		final Graphics2D g = (Graphics2D) this.FONDO.getGraphics();
		g.setColor(colorBordes);
		g.drawRect(0, 0, area.width - 1, area.height - 1);
		g.dispose();
		this.texto = "";
	}

	@Override
	public void pintar(final Graphics2D g) {
		Render2D.dibujarImagen(g, this.FONDO, this.AREA.x, this.AREA.y);
		this.pintarTexto(g);
//		if (seleccionado) {
//			DibujoDebug.dibujarLinea(g, this.AREA.x, this.AREA.y, this.AREA.x + this.AREA.width, this.AREA.y, Color.yellow);
//		}
	}

	@Override
	public void pintar(final Graphics2D g, final int desplazamientoY) {
		Render2D.dibujarImagen(g, this.FONDO, this.AREA.x, this.AREA.y - desplazamientoY);
		this.pintarTexto(g, desplazamientoY);
	}

	private void pintarTexto(final Graphics2D g) {
		final int ancho = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, this.texto);
//		final int alto = MedidorStrings.medirAltoPixeles(g, texto);
		this.imgTexto = new BufferedImage(this.AREA.width - 6, this.AREA.height, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D imgG = (Graphics2D) this.imgTexto.getGraphics();
		imgG.setFont(g.getFont());
		int desplazamientoIzquierdo = 0;
		int desplazamientoPuntero = 0;
		if (ancho > this.imgTexto.getWidth()) {
			desplazamientoIzquierdo = (this.imgTexto.getWidth() - ancho);
			desplazamientoPuntero = this.imgTexto.getWidth();
			Render2D.dibujarString(imgG, this.texto, desplazamientoIzquierdo, this.imgTexto.getHeight() - 2,
					this.COLOR_TEXTO);
		} else {
			desplazamientoPuntero = ancho;
			Render2D.dibujarString(imgG, this.texto, desplazamientoIzquierdo, this.imgTexto.getHeight() - 2,
					this.COLOR_TEXTO);
		}
		imgG.dispose();
		Render2D.dibujarImagen(g, this.imgTexto, this.AREA.x + 2, this.AREA.y);
		this.pintarPuntero(g, desplazamientoPuntero);

//		System.out.println(
//				"Caja=  w:" + this.AREA.width + " , Img=  w:" + this.imgTexto.getWidth() + " , Letras= w:" + ancho + " , ComiText = x: " + ((imgTexto.getWidth() - ancho)));
	}

	private void pintarTexto(final Graphics2D g, final int desplazamientoY) {
		final int ancho = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, this.texto);
//		final int alto = MedidorStrings.medirAltoPixeles(g, texto);
		this.imgTexto = new BufferedImage(this.AREA.width - 6, this.AREA.height, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D imgG = (Graphics2D) this.imgTexto.getGraphics();
		imgG.setFont(g.getFont());
		int desplazamientoIzquierdo = 0;
		int desplazamientoPuntero = 0;
		if (ancho > this.imgTexto.getWidth()) {
			desplazamientoIzquierdo = (this.imgTexto.getWidth() - ancho);
			desplazamientoPuntero = this.imgTexto.getWidth();
			Render2D.dibujarString(imgG, this.texto, desplazamientoIzquierdo,
					this.imgTexto.getHeight() - 2 - desplazamientoY, this.COLOR_TEXTO);
		} else {
			desplazamientoPuntero = ancho;
			Render2D.dibujarString(imgG, this.texto, desplazamientoIzquierdo,
					this.imgTexto.getHeight() - 2 - desplazamientoY, this.COLOR_TEXTO);
		}
		imgG.dispose();
		Render2D.dibujarImagen(g, this.imgTexto, this.AREA.x + 2, this.AREA.y - desplazamientoY);
		this.pintarPuntero(g, desplazamientoPuntero, desplazamientoY);

//		System.out.println(
//				"Caja=  w:" + this.AREA.width + " , Img=  w:" + this.imgTexto.getWidth() + " , Letras= w:" + ancho + " , ComiText = x: " + ((imgTexto.getWidth() - ancho)));
	}

	@Override
	public void actualizar() {
		this.actualizarContadorPuntero();
		this.actualizarSeleccion();
		if (this.GT_TECLEO.transcurrioMiliSegundos(this.MS_ESPERA_TECLEO)) {
			this.actualizarTexto();
		}

	}

	private void actualizarSeleccion() {
		if (Globales.RATON.presionadoClickIzq()
				&& Globales.RATON.getRectanguloPosicionEscalado().intersects(this.AREA)) {
			this.seleccionado = true;
		} else if (this.seleccionado && Globales.RATON.presionadoClickIzq()) {
			this.seleccionado = false;
		}
	}

	private void actualizarContadorPuntero() {
		this.contadorPuntero++;
		if (this.contadorPuntero > 60) {
			this.contadorPuntero = 0;
		}
	}

	private void actualizarTexto() {
		if (this.seleccionado) {
			if (this.alfanumerico) {
				this.analizarTecleoNumeros();
				this.analizarTecleoLetras();
			} else if (this.numerico) {
				this.analizarTecleoNumeros();
			} else if (this.alfabetico) {
				this.analizarTecleoLetras();
			} else if (this.caracterTecla) {
				this.analizarTecleoTeclaCaracter();
			}
			this.verificarTeclaDelYAccionar();

		}
	}

	private boolean agregarCaracter(final int codigo) {
		boolean quedaEspacio = true;
		if (this.limiteCaracteres > 0) {
			if (this.texto.length() < this.limiteCaracteres) {
				this.texto += (char) codigo;
			} else {
				quedaEspacio = false;
			}
		} else {
			this.texto += (char) codigo;
		}
		return quedaEspacio;
	}

	private boolean agregarCaracterName(final int codigo) {
		boolean quedaEspacio = true;
		if (this.limiteCaracteres > 0) {
			if (this.texto.length() < this.limiteCaracteres) {
				this.texto += KeyEvent.getKeyText(codigo);
			} else {
				quedaEspacio = false;
			}
		} else {
			this.texto += (char) codigo;
		}
		return quedaEspacio;
	}

	private void analizarTecleoTeclaCaracter() {
		for (int t = 0; t < Globales.TECLADO.teclas.length; t++) {
			if (Globales.TECLADO.presionaTeclaEnLista(t)) {
				if (!this.agregarCaracterName(t)) {
					break;
				}
				this.GT_TECLEO.establecerReferenciaTiempoActual();
			}
		}
	}

	private void analizarTecleoNumeros() {
		for (int i = KeyEvent.VK_0; i <= KeyEvent.VK_9; i++) {
			if (Globales.TECLADO.presionaTeclaEnLista(i)) {
				if (!this.agregarCaracter(i)) {
					break;
				}
				this.GT_TECLEO.establecerReferenciaTiempoActual();
			}
		}
	}

	private void analizarTecleoLetras() {
		for (int i = KeyEvent.VK_A; i <= KeyEvent.VK_Z; i++) {
			if (Globales.TECLADO.presionaTeclaEnLista(i)) {
				if (!this.agregarCaracter(i)) {
					break;
				}
				this.GT_TECLEO.establecerReferenciaTiempoActual();

			}
		}
	}

	private void verificarTeclaDelYAccionar() {
		if (Globales.TECLADO.presionaTeclaEnLista(KeyEvent.VK_BACK_SPACE)) {
			if (this.texto.length() > 1) {
				this.texto = this.texto.substring(0, this.texto.length() - 1);
			} else if (this.texto.length() == 1) {
				this.texto = "";
			}
			this.GT_TECLEO.establecerReferenciaTiempoActual();
		}
	}

	private void pintarPuntero(final Graphics2D g, final int desplazamientoPuntero) {
		if (this.seleccionado && this.punteroMostrar) {
			Render2D.dibujarString(g, "|", this.AREA.x + desplazamientoPuntero, (this.AREA.y + this.AREA.height) - 2,
					this.COLOR_TEXTO);
			if (this.GT_PUNTERO.transcurrioMiliSegundos(this.MS_ESPERA_PUNTERO)) {
				this.punteroMostrar = false;
				this.GT_PUNTERO.establecerReferenciaTiempoActual();

			}
		} else if (this.seleccionado && !this.punteroMostrar) {
			if (this.GT_PUNTERO.transcurrioMiliSegundos(this.MS_ESPERA_PUNTERO)) {
				this.punteroMostrar = true;
				this.GT_PUNTERO.establecerReferenciaTiempoActual();
			}
		}
	}

	private void pintarPuntero(final Graphics2D g, final int desplazamientoPuntero, final int desplazamientoY) {
		if (this.seleccionado && this.punteroMostrar) {
			Render2D.dibujarString(g, "|", this.AREA.x + desplazamientoPuntero,
					(this.AREA.y + this.AREA.height) - 2 - desplazamientoY, this.COLOR_TEXTO);
			if (this.GT_PUNTERO.transcurrioMiliSegundos(this.MS_ESPERA_PUNTERO)) {
				this.punteroMostrar = false;
				this.GT_PUNTERO.establecerReferenciaTiempoActual();

			}
		} else if (this.seleccionado && !this.punteroMostrar) {
			if (this.GT_PUNTERO.transcurrioMiliSegundos(this.MS_ESPERA_PUNTERO)) {
				this.punteroMostrar = true;
				this.GT_PUNTERO.establecerReferenciaTiempoActual();
			}
		}
	}

	public String getTexto() {
		return this.texto;
	}

	public void establecerTexto(final String text) {
		this.texto = text;
	}

	public void establecerSoloNumerico() {
		this.numerico = true;
		this.alfanumerico = false;
		this.alfabetico = false;
		this.caracterTecla = false;
	}

	public void establecerSoloAlfabetico() {
		this.numerico = false;
		this.alfanumerico = false;
		this.alfabetico = true;
		this.caracterTecla = false;
	}

	public void establecerAlfaNumerico() {
		this.numerico = false;
		this.alfanumerico = true;
		this.alfabetico = false;
		this.caracterTecla = false;
	}

	public void establecerCaracterTecla() {
		this.numerico = false;
		this.alfanumerico = false;
		this.alfabetico = false;
		this.caracterTecla = true;
	}

	public void establecerLimiteCaracteres(final int lim) {
		if (lim <= 0) {
			this.limiteCaracteres = 0;
		} else {
			this.limiteCaracteres = lim;
		}
	}

	public int getLimiteCaracteres() {
		return this.limiteCaracteres;
	}

	public int getNumeroParseado() {
		int num = 0;
		if (!this.texto.isEmpty() && this.numerico) {
			num = Integer.parseInt(this.texto);
		}
		return num;
	}

	public Rectangle getArea() {
		return this.AREA;
	}

}
