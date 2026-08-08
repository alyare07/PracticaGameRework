package principal.maquinaestado.estados.menu.herramientas;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.Textura;

public class Boton extends Componente {
	protected final Rectangle AREA;
	protected  BufferedImage fondo;
	protected boolean apuntado;
	protected String texto;
	protected EventoAccion accion;
	protected Color colorForeground = Color.black;
	protected Color colorBordes = Color.orange;
	protected float tamanoLetra = 8f;

	public Boton(final String texto, final Color color, final Rectangle r) {
		this.AREA = r;
		this.fondo = Constantes.FUNCIONES.TEXTURAS_TOOLS.crearTextura(color, r.width, r.height);
		this.texto = texto;
	}

	public Boton(final String texto, final BufferedImage image, final Rectangle r) {
		this.AREA = r;
		this.fondo = image;
		this.texto = texto;
	}

	@Override
	public void pintar(final Graphics2D g) {
		DibujoDebug.dibujarImagen(g, fondo, this.AREA.x, this.AREA.y);
		this.pintarTexto(g);
		if (this.apuntado) {
			DibujoDebug.dibujarRectanguloContorno(g, AREA, this.colorBordes);
		}
	}
	
	@Override
	public void pintar(final Graphics2D g, final int desplazamientoY) {
		DibujoDebug.dibujarImagen(g, fondo, this.AREA.x, this.AREA.y);
		this.pintarTexto(g);
		if (this.apuntado) {
			DibujoDebug.dibujarRectanguloContorno(g, AREA, this.colorBordes);
		}
	}

	private void pintarTexto(final Graphics2D g) {
		g.setFont(g.getFont().deriveFont(this.tamanoLetra));
		g.setColor(this.colorForeground);
		final int ancho = Constantes.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, texto);
		final int alto = Constantes.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, texto);
		final int puntoX = this.AREA.x + (this.AREA.width / 2) - (ancho / 2);
		final int puntoY = this.AREA.y + (this.AREA.height / 2) + (alto / 2);
		DibujoDebug.dibujarString(g, texto, puntoX, puntoY);
	}

	@Override
	public void actualizar() {
		if (Constantes.RATON.getRectanguloPosicionEscalado().intersects(AREA)) {
			if (Constantes.RATON.presionadoClickIzq()) {
				if (this.accion != null) {
					this.accion.ejecutar();
				}
			}
			if (!this.apuntado) {
				this.apuntado = true;
			}
		} else {
			if (this.apuntado) {
				this.apuntado = false;
			}
		}
	}

	public void establecerAccion(final EventoAccion e) {
		this.accion = e;
	}

	public void establecerTexto(final String texto) {
		this.texto = texto;
	}
	
	public void establecerColorBackGround(final Color cbg) {
		this.fondo = Constantes.FUNCIONES.TEXTURAS_TOOLS.crearTextura(cbg, this.AREA.width, this.AREA.height);
	}

	public void establecerColorForeGround(final Color cfg) {
		this.colorForeground = cfg;
	}
	
	public void establecerColorBordes(final Color cb) {
		this.colorBordes = cb;
	}
	
	public void establecerTamanoLetra(final float t) {
		this.tamanoLetra = t;
	}
}
