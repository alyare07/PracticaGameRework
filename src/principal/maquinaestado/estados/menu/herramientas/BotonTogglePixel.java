package principal.maquinaestado.estados.menu.herramientas;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.function.BooleanSupplier;

import principal.controles.Raton;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Widget Pixel-Art interactivo para conmutadores (Toggles) de herramientas y
 * capas. Muestra retroalimentación visual de estado activo (Verde/Oro) e
 * inactivo (Grafito) en tiempo real (Zero-GC).
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class BotonTogglePixel extends ComponenteMenu {

	private static final Color COLOR_FONDO_ON = new Color(24, 45, 32, 255);
	private static final Color COLOR_FONDO_OFF = new Color(20, 24, 32, 240);
	private static final Color COLOR_FONDO_CLICK = new Color(14, 16, 22, 255);

	private static final Color COLOR_BORDE_ON = new Color(60, 240, 100); // Verde Neón
	private static final Color COLOR_BORDE_OFF = new Color(55, 60, 75);
	private static final Color COLOR_SOMBRA_BORDE = new Color(8, 10, 14, 255);

	private static final Color COLOR_TEXTO_ON = new Color(160, 255, 180);
	private static final Color COLOR_TEXTO_OFF = new Color(160, 165, 175);

	private final String texto;
	private final BooleanSupplier consultorEstado;
	private final EventoAccion accion;
	private boolean presionado = false;

	public BotonTogglePixel(final String texto, final Rectangle area, final BooleanSupplier consultorEstado,
			final EventoAccion accion) {
		super(area);
		this.texto = (texto != null) ? texto : "";
		this.consultorEstado = consultorEstado;
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

		final boolean estadoActivo = (this.consultorEstado != null) && this.consultorEstado.getAsBoolean();

		final int x = this.area.x;
		final int y = this.area.y + (this.presionado ? 1 : 0);
		final int w = this.area.width;
		final int h = this.area.height;

		// 1. Fondo según estado
		final Color colorFondo = this.presionado ? COLOR_FONDO_CLICK : (estadoActivo ? COLOR_FONDO_ON : COLOR_FONDO_OFF);
		Render2D.dibujarRectanguloRelleno(g, x, y, w, h, colorFondo);

		// 2. Bordes
		Render2D.dibujarRectanguloContorno(g, x - 1, y - 1, w + 2, h + 2, COLOR_SOMBRA_BORDE);
		Render2D.dibujarRectanguloContorno(g, x, y, w, h, estadoActivo ? COLOR_BORDE_ON : COLOR_BORDE_OFF);

		// 3. Indicador LED de estado
		final Color colorLed = estadoActivo ? COLOR_BORDE_ON : new Color(40, 45, 55);
		Render2D.dibujarRectanguloRelleno(g, x + 2, y + 2, 2, 2, colorLed);

		// 4. Texto centrado en m5x7 (12f)
		final Font fontPrevia = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 12f));

		final int anchoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, this.texto);
		final int xTexto = x + ((w - anchoTexto) / 2);
		final int yTexto = (y + h) - 3;

		final Color colorTexto = estadoActivo ? COLOR_TEXTO_ON : COLOR_TEXTO_OFF;
		Render2D.dibujarStringConSombra(g, this.texto, xTexto, yTexto, colorTexto, Color.BLACK);

		g.setFont(fontPrevia);
	}
}