package principal.maquinaestado.estados.menu.herramientas;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import principal.controles.Raton;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Selector interactivo Pixel-Art de opciones en carrusel [< Valor >] (Zero-GC).
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class SelectorOpcionPixel extends ComponenteMenu {

	private static final Color COLOR_FONDO_NORMAL = new Color(20, 24, 32, 240);
	private static final Color COLOR_FONDO_HOVER = new Color(36, 44, 58, 255);
	private static final Color COLOR_BORDE_NORMAL = new Color(55, 60, 75);
	private static final Color COLOR_BORDE_HOVER = new Color(220, 180, 50); // Oro
	private static final Color COLOR_SOMBRA_BORDE = new Color(8, 10, 14, 255);

	private static final Color COLOR_ETIQUETA = new Color(190, 195, 205);
	private static final Color COLOR_VALOR = new Color(255, 235, 150);

	private final String etiqueta;
	private final String[] opciones;
	private int indiceSeleccionado;
	private final EventoAccion alCambiar;

	private final Rectangle areaFlechaIzq;
	private final Rectangle areaFlechaDer;

	public SelectorOpcionPixel(final Rectangle area, final String etiqueta, final String[] opciones,
			final int indiceInicial, final EventoAccion alCambiar) {
		super(area);
		this.etiqueta = (etiqueta != null) ? etiqueta : "";
		this.opciones = (opciones != null) ? opciones : new String[] { "N/A" };
		this.indiceSeleccionado = Math.max(0, Math.min(this.opciones.length - 1, indiceInicial));
		this.alCambiar = alCambiar;

		final int alto = area.height;
		final int anchoFlecha = 18;
		this.areaFlechaIzq = new Rectangle((area.x + area.width) - (anchoFlecha * 2) - 100, area.y, anchoFlecha, alto);
		this.areaFlechaDer = new Rectangle((area.x + area.width) - anchoFlecha, area.y, anchoFlecha, alto);
	}

	@Override
	public void actualizar(final Raton raton) {
		if (!this.visible || (raton == null)) {
			return;
		}

		final Point pMouse = raton.getPuntoPosicionEscalado();

		if (raton.presionadoClickIzqUnicaAct()) {
			if (this.areaFlechaIzq.contains(pMouse)) {
				this.anterior();
			} else if (this.areaFlechaDer.contains(pMouse) || this.area.contains(pMouse)) {
				this.siguiente();
			}
		}
	}

	public void siguiente() {
		this.indiceSeleccionado = (this.indiceSeleccionado + 1) % this.opciones.length;
		GestorSonido.reproducir(IDSonido.SELECT_MENU);
		if (this.alCambiar != null) {
			this.alCambiar.ejecutar();
		}
	}

	public void anterior() {
		this.indiceSeleccionado = (this.indiceSeleccionado - 1 + this.opciones.length) % this.opciones.length;
		GestorSonido.reproducir(IDSonido.SELECT_MENU);
		if (this.alCambiar != null) {
			this.alCambiar.ejecutar();
		}
	}

	@Override
	public void pintar(final Graphics2D g) {
		if (!this.visible) {
			return;
		}

		final int x = this.area.x;
		final int y = this.area.y;
		final int w = this.area.width;
		final int h = this.area.height;

		// 1. Fondo y Bordes
		final Color colorFondo = this.enfocado ? COLOR_FONDO_HOVER : COLOR_FONDO_NORMAL;
		Render2D.dibujarRectanguloRelleno(g, x, y, w, h, colorFondo);
		Render2D.dibujarRectanguloContorno(g, x - 1, y - 1, w + 2, h + 2, COLOR_SOMBRA_BORDE);
		Render2D.dibujarRectanguloContorno(g, x, y, w, h, this.enfocado ? COLOR_BORDE_HOVER : COLOR_BORDE_NORMAL);

		final Font fontPrevia = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 16f));

		// 2. Etiqueta izquierda
		Render2D.dibujarStringConSombra(g, this.etiqueta, x + 8, (y + h) - 4, COLOR_ETIQUETA, Color.BLACK);

		// 3. Valor central con flechas [< Valor >]
		final String valorTexto = this.opciones[this.indiceSeleccionado];
		final String bloqueValor = "< " + valorTexto + " >";
		final int anchoBloque = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, bloqueValor);

		final int xValor = (x + w) - anchoBloque - 8;
		final Color colorValor = this.enfocado ? COLOR_VALOR : Color.LIGHT_GRAY;
		Render2D.dibujarStringConSombra(g, bloqueValor, xValor, (y + h) - 4, colorValor, Color.BLACK);

		g.setFont(fontPrevia);
	}

	public int getIndiceSeleccionado() {
		return this.indiceSeleccionado;
	}

	public void setIndiceSeleccionado(final int idx) {
		this.indiceSeleccionado = Math.max(0, Math.min(this.opciones.length - 1, idx));
	}

	public String getOpcionSeleccionada() {
		return this.opciones[this.indiceSeleccionado];
	}
}