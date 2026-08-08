package principal.maquinaestado.estados.menu.herramientas;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import principal.controles.Tecla;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Textura;

public class CajaTecla extends Componente {
	protected final Rectangle AREA;
	protected final BufferedImage FONDO;
	protected final Color COLOR_TEXTO;
	protected String texto;
	protected boolean seleccionado;
	protected final GestorTiempo GT_TECLEO = new GestorTiempo();
	protected final int MS_ESPERA_TECLEO = 150;
	protected final int MS_ESPERA_PUNTERO = 450;
	protected BufferedImage imgTexto;
	protected int codigoTecla = -1;
	protected float tamanoLetra = 8f;
	protected int desplazamientoY = 0;
	protected final Tecla tecla;
	protected boolean modificado;
	
	public CajaTecla(final Rectangle area, final BufferedImage fondo, final Color colorTexto, final Tecla tecla) {
		this.AREA = area;
		this.COLOR_TEXTO = colorTexto;
		this.FONDO = fondo;
		this.texto = "";
		this.tecla = tecla;
		this.establecerTecla(tecla.getCodigoTecla());
	}
	
	public CajaTecla(final Rectangle area, final Color colorFondo, final Color colorTexto, final Tecla tecla) {
		this.AREA = area;
		this.COLOR_TEXTO = colorTexto;
		this.FONDO = Constantes.FUNCIONES.TEXTURAS_TOOLS.crearTextura(colorFondo, area.width, area.height);
		this.texto = "";
		this.tecla = tecla;
		this.establecerTecla(tecla.getCodigoTecla());
	}
	
	public CajaTecla(final Rectangle area, final Color colorFondo, final Color colorBordes, final Color colorTexto, final Tecla tecla) {
		this.AREA = area;
		this.COLOR_TEXTO = colorTexto;
		this.FONDO = Constantes.FUNCIONES.TEXTURAS_TOOLS.crearTextura(colorFondo, area.width, area.height);
		final Graphics2D g = (Graphics2D) this.FONDO.getGraphics();
		g.setColor(colorBordes);
		g.drawRect(0, 0, area.width - 1, area.height - 1);
		g.dispose();
		this.texto = "";
		this.tecla = tecla;
		this.establecerTecla(tecla.getCodigoTecla());
	}

	@Override
	public void pintar(Graphics2D g) {
		g.setFont(g.getFont().deriveFont(this.tamanoLetra));
		DibujoDebug.dibujarImagen(g, FONDO, this.AREA.x, this.AREA.y);
		this.pintarTexto(g,0);
		if(this.seleccionado) {
			DibujoDebug.dibujarRectanguloContorno(g, AREA, Color.orange);
		}
	}

	@Override
	public void pintar(Graphics2D g, int desplazamientoY) {
		DibujoDebug.dibujarImagen(g, FONDO, this.AREA.x, this.AREA.y-desplazamientoY);
		this.pintarTexto(g, desplazamientoY);
		if(this.seleccionado) {
			DibujoDebug.dibujarRectanguloContorno(g, AREA.x, AREA.y - desplazamientoY,AREA.width, AREA.height, Color.orange);
		}
	}
	
	
	
	
	private void pintarTexto(final Graphics2D g, final int desplazamientoY) {
		final int ancho = Constantes.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, texto);
		this.imgTexto = new BufferedImage(this.AREA.width - 6, this.AREA.height, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D imgG = (Graphics2D) this.imgTexto.getGraphics();
		imgG.setFont(g.getFont());
		int desplazamientoIzquierdo = 0;
		if (ancho > imgTexto.getWidth()) {
			desplazamientoIzquierdo = (imgTexto.getWidth() - ancho);
			DibujoDebug.dibujarString(imgG, texto, desplazamientoIzquierdo, this.imgTexto.getHeight() - 2, COLOR_TEXTO);
		} else {
			DibujoDebug.dibujarString(imgG, texto, desplazamientoIzquierdo, this.imgTexto.getHeight() - 2, COLOR_TEXTO);
		}
		imgG.dispose();
		DibujoDebug.dibujarImagen(g, imgTexto, this.AREA.x + 2, this.AREA.y - desplazamientoY);

	}

	@Override
	public void actualizar() {
		this.actualizarSeleccion();
		if (this.GT_TECLEO.transcurrioMiliSegundos(MS_ESPERA_TECLEO)) {
			this.actualizarTexto();
		}
	}
	
	private void actualizarSeleccion() {
		final Rectangle areaDesplazada = Constantes.RATON.getRectanguloPosicionEscalado();
		areaDesplazada.y+=this.desplazamientoY;
		if (Constantes.RATON.presionadoClickIzq() && areaDesplazada.intersects(AREA)) {
			this.seleccionado = true;
		} else if (this.seleccionado && Constantes.RATON.presionadoClickIzq()) {
			this.seleccionado = false;
		}
	}
	
	
	private void actualizarTexto() {
		if (this.seleccionado) {
			this.analizarTecleo();
			this.verificarTeclaDelYAccionar();

		}
	}
	
	private void establecerTecla(final int codigo) {
		if(this.tecla.getCodigoTecla() == codigo && this.codigoTecla != -1) {
			if(this.codigoTecla != codigo) {
				this.establecerModificado(false);
				this.texto = KeyEvent.getKeyText(codigo);
				this.codigoTecla = codigo;
			}
			return;
		}
		if(this.codigoTecla != -1) {
			this.establecerModificado(true);
		}
		this.texto = KeyEvent.getKeyText(codigo);
		this.codigoTecla = codigo;
		
		
	}
	
	private void analizarTecleo() {
		for(int t = 0; t < Constantes.TECLADO.teclas.length; t++) {
			if(Constantes.TECLADO.presionaTeclaEnLista(t)) {
				this.establecerTecla(t);
				this.GT_TECLEO.establecerReferenciaTiempoActual();
				return;
			}
		}
	}
	
	private void verificarTeclaDelYAccionar() {
		if (Constantes.TECLADO.presionaTeclaEnLista(KeyEvent.VK_BACK_SPACE)) {
			this.texto = "";
			this.GT_TECLEO.establecerReferenciaTiempoActual();
		}
	}
	
	public int getCodigoTecla() {
		return this.codigoTecla;
	}
	
	public void establecerTamanoLetra(final float tamanoLetra) {
		this.tamanoLetra = tamanoLetra;
	}
	
	public void establecerDesplazamientoY(final int desplazamientoY) {
		this.desplazamientoY = desplazamientoY;
	}
	
	public Tecla getTeclaApuntada() {
		return this.tecla;
	}
	
	public void establecerCambiosEnTecla() {
		this.tecla.establecerCodigoTecla(this.codigoTecla);
		this.establecerModificado(false);
	}
	
	public void establecerModificado(final boolean modificado) {
		this.modificado = modificado;
	}
	
	public boolean modificado() {
		return this.modificado;
	}

}
