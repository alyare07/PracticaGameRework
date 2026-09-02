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
 * Botón interactivo Pixel-Art con foco exclusivo unificado, feedback táctil de
 * 1 px y estética grafito/oro (Zero-GC).
 * 
 * @version 1.2 (Vanilla Java 8 - Unified Selection Fix)
 */
public class BotonPixel extends ComponenteMenu {

	private static final Color COLOR_FONDO_NORMAL = new Color(20, 24, 32, 240);
	private static final Color COLOR_FONDO_HOVER = new Color(36, 44, 58, 255);
	private static final Color COLOR_FONDO_CLICK = new Color(14, 16, 22, 255);

	private static final Color COLOR_BORDE_NORMAL = new Color(55, 60, 75, 255);
	private static final Color COLOR_BORDE_HOVER = new Color(220, 180, 50, 255); // Oro
	private static final Color COLOR_SOMBRA_BORDE = new Color(8, 10, 14, 255);

	private static final Color COLOR_TEXTO_NORMAL = new Color(190, 195, 205);
	private static final Color COLOR_TEXTO_HOVER = new Color(255, 240, 180); // Oro claro

	private final String texto;
	private final EventoAccion accion;
	private boolean presionado = false;

	public BotonPixel(final String texto, final Rectangle area, final EventoAccion accion) {
		super(area);
		this.texto = (texto != null) ? texto : "";
		this.accion = accion;
	}

	@Override
	public void actualizar(final Raton raton) {
		if (!this.visible || (raton == null)) {
			this.presionado = false;
			return;
		}

		final Point pMouse = raton.getPuntoPosicionEscalado();
		final boolean mouseEncima = this.area.contains(pMouse);

		this.presionado = (mouseEncima && raton.presionadoClickIzq());

		// Disparo por clic directo del ratón
		if (mouseEncima && raton.presionadoClickIzqUnicaAct()) {
			this.accionar();
		}
	}

	public void accionar() {
		GestorSonido.reproducir(IDSonido.GOLPE_1);
		if (this.accion != null) {
			this.accion.ejecutar();
		}
	}

	@Override
	public void pintar(final Graphics2D g) {
		if (!this.visible) {
			return;
		}

		// El botón se resalta EXCLUSIVAMENTE si el gestor de menú le otorgó el foco
		final boolean resaltado = this.enfocado;

		final int x = this.area.x;
		final int y = this.area.y + (this.presionado ? 1 : 0);
		final int w = this.area.width;
		final int h = this.area.height;

		// 1. Fondo según estado
		final Color colorFondo = this.presionado ? COLOR_FONDO_CLICK
				: (resaltado ? COLOR_FONDO_HOVER : COLOR_FONDO_NORMAL);
		Render2D.dibujarRectanguloRelleno(g, x, y, w, h, colorFondo);

		// 2. Bordes
		Render2D.dibujarRectanguloContorno(g, x - 1, y - 1, w + 2, h + 2, COLOR_SOMBRA_BORDE);
		final Color colorBorde = resaltado ? COLOR_BORDE_HOVER : COLOR_BORDE_NORMAL;
		Render2D.dibujarRectanguloContorno(g, x, y, w, h, colorBorde);

		// 3. Flechas indicadoras de foco activo (► ◄)
		if (resaltado) {
			final Font fontPrevia = g.getFont();
			g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 16f));
			Render2D.dibujarStringConSombra(g, ">", x + 6, (y + h) - 4, COLOR_BORDE_HOVER, Color.BLACK);
			Render2D.dibujarStringConSombra(g, "<", (x + w) - 12, (y + h) - 4, COLOR_BORDE_HOVER, Color.BLACK);
			g.setFont(fontPrevia);
		}

		// 4. Texto del botón centrado en m5x7 (16f)
		final Font fontPrevia = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 16f));

		final int anchoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, this.texto);
		final int xTexto = x + ((w - anchoTexto) / 2);
		final int yTexto = (y + h) - 4;

		final Color colorTexto = resaltado ? COLOR_TEXTO_HOVER : COLOR_TEXTO_NORMAL;
		Render2D.dibujarStringConSombra(g, this.texto, xTexto, yTexto, colorTexto, Color.BLACK);

		g.setFont(fontPrevia);
	}

	public String getTexto() {
		return this.texto;
	}
}