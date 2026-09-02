package principal.maquinaestado.estados.menu.herramientas;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

import principal.controles.Raton;
import principal.controles.Tecla;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Casilla interactiva para reasignar una tecla física (Zero-GC / m5x7).
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class CajaTeclaPixel extends ComponenteMenu {

	private static final Color COLOR_FONDO_NORMAL = new Color(20, 24, 32, 240);
	private static final Color COLOR_FONDO_ESCUCHANDO = new Color(45, 55, 75, 255);
	private static final Color COLOR_BORDE_NORMAL = new Color(55, 60, 75);
	private static final Color COLOR_BORDE_ACTIVO = new Color(220, 180, 50); // Oro
	private static final Color COLOR_TEXTO_MODIFICADO = new Color(255, 205, 50);

	private final Tecla tecla;
	private int codigoAsignado;
	private boolean escuchando = false;

	public CajaTeclaPixel(final Rectangle area, final Tecla tecla) {
		super(area);
		this.tecla = tecla;
		this.codigoAsignado = (tecla != null) ? tecla.getCodigoTecla() : KeyEvent.VK_UNDEFINED;
	}

	@Override
	public void actualizar(final Raton raton) {
		if (!this.visible) {
			return;
		}

		if (raton != null && raton.presionadoClickIzqUnicaAct()) {
			final Point pMouse = raton.getPuntoPosicionEscalado();
			if (this.area.contains(pMouse)) {
				this.escuchando = true;
				GestorSonido.reproducir(IDSonido.GOLPE_1);
			} else if (this.escuchando) {
				this.escuchando = false;
			}
		}

		// Si está escuchando, captura la primera tecla presionada
		if (this.escuchando) {
			for (int i = 0; i < Globales.TECLADO.teclas.length; i++) {
				if (Globales.TECLADO.isTeclaPresionadaUnaVez(i)) {
					if (i == KeyEvent.VK_ESCAPE) {
						this.escuchando = false;
					} else {
						this.codigoAsignado = i;
						this.escuchando = false;
						GestorSonido.reproducir(IDSonido.GOLPE_1);
					}
					break;
				}
			}
		}
	}

	public void actualizarConScroll(final Raton raton, final int scrollY) {
		if (!this.visible) {
			return;
		}

		final Rectangle areaAparente = new Rectangle(this.area.x, this.area.y - scrollY, this.area.width, this.area.height);

		if (raton != null && raton.presionadoClickIzqUnicaAct()) {
			final Point pMouse = raton.getPuntoPosicionEscalado();
			if (areaAparente.contains(pMouse)) {
				this.escuchando = true;
				GestorSonido.reproducir(IDSonido.GOLPE_1);
			} else if (this.escuchando) {
				this.escuchando = false;
			}
		}

		if (this.escuchando) {
			for (int i = 0; i < Globales.TECLADO.teclas.length; i++) {
				if (Globales.TECLADO.isTeclaPresionadaUnaVez(i)) {
					if (i == KeyEvent.VK_ESCAPE) {
						this.escuchando = false;
					} else {
						this.codigoAsignado = i;
						this.escuchando = false;
						GestorSonido.reproducir(IDSonido.GOLPE_1);
					}
					break;
				}
			}
		}
	}

	@Override
	public void pintar(final Graphics2D g) {
		this.pintarConScroll(g, 0);
	}

	public void pintarConScroll(final Graphics2D g, final int scrollY) {
		if (!this.visible) {
			return;
		}

		final int x = this.area.x;
		final int y = this.area.y - scrollY;
		final int w = this.area.width;
		final int h = this.area.height;

		// 1. Fondo
		Render2D.dibujarRectanguloRelleno(g, x, y, w, h, this.escuchando ? COLOR_FONDO_ESCUCHANDO : COLOR_FONDO_NORMAL);

		// 2. Borde (Oro si está escuchando o modificada)
		final Color colorBorde = this.escuchando ? COLOR_BORDE_ACTIVO
				: (this.isModificada() ? COLOR_TEXTO_MODIFICADO : COLOR_BORDE_NORMAL);
		Render2D.dibujarRectanguloContorno(g, x, y, w, h, colorBorde);

		// 3. Texto de la tecla en m5x7 (16f)
		final Font fontPrevia = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 16f));

		final String texto = this.escuchando ? "[Presiona...]" : KeyEvent.getKeyText(this.codigoAsignado);
		final int anchoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, texto);
		final int xTexto = x + ((w - anchoTexto) / 2);
		final int yTexto = (y + h) - 4;

		final Color colorTexto = this.escuchando ? Color.WHITE : (this.isModificada() ? COLOR_TEXTO_MODIFICADO : Color.LIGHT_GRAY);
		Render2D.dibujarStringConSombra(g, texto, xTexto, yTexto, colorTexto, Color.BLACK);

		g.setFont(fontPrevia);
	}

	public void aplicarCambios() {
		if (this.tecla != null) {
			this.tecla.establecerCodigoTecla(this.codigoAsignado);
		}
	}

	public boolean isModificada() {
		return this.tecla != null && this.codigoAsignado != this.tecla.getCodigoTecla();
	}

	public Tecla getTecla() {
		return this.tecla;
	}
}