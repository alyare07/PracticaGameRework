package principal.maquinaestado.estados.menu.herramientas;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

import principal.controles.Raton;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Campo de texto y números interactivo Pixel-Art con control de foco estricto
 * para prevenir fugas de teclado hacia la cámara o el mapa (Zero-GC).
 * 
 * @version 1.1 (Vanilla Java 8)
 */
public class CajaTextoPixel extends ComponenteMenu {

	private static final Color COLOR_FONDO_NORMAL = new Color(20, 24, 32, 240);
	private static final Color COLOR_FONDO_ACTIVO = new Color(28, 35, 48, 255);
	private static final Color COLOR_BORDE_NORMAL = new Color(55, 60, 75);
	private static final Color COLOR_BORDE_ACTIVO = new Color(220, 180, 50); // Oro

	private String texto;
	private final int limiteCaracteres;
	private final boolean soloNumeros;

	private boolean activo = false;
	private final GestorTiempo gtCursor = new GestorTiempo();
	private boolean cursorVisible = true;

	public CajaTextoPixel(final Rectangle area, final String textoInicial, final int limiteCaracteres,
			final boolean soloNumeros) {
		super(area);
		this.texto = (textoInicial != null) ? textoInicial : "";
		this.limiteCaracteres = Math.max(1, limiteCaracteres);
		this.soloNumeros = soloNumeros;
	}

	@Override
	public void actualizar(final Raton raton) {
		if (!this.visible) {
			this.activo = false;
			return;
		}

		// 1. Selección y foco con el ratón
		if ((raton != null) && raton.presionadoClickIzqUnicaAct()) {
			final Point pMouse = raton.getPuntoPosicionEscalado();
			if (this.area.contains(pMouse)) {
				this.activo = true;
				GestorSonido.reproducir(IDSonido.GOLPE_1);
			} else {
				this.activo = false;
			}
		}

		if (!this.activo) {
			return;
		}

		// 2. Parpadeo del cursor (cada 450 ms)
		if (this.gtCursor.transcurrioMiliSegundos(450)) {
			this.cursorVisible = !this.cursorVisible;
			this.gtCursor.establecerReferenciaTiempoActual();
		}

		// 3. Borrado (Backspace)
		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_BACK_SPACE)) {
			if (!this.texto.isEmpty()) {
				this.texto = this.texto.substring(0, this.texto.length() - 1);
			}
			return;
		}

		// 4. Captura de Números (0..9)
		for (int code = KeyEvent.VK_0; code <= KeyEvent.VK_9; code++) {
			if (Globales.TECLADO.isTeclaPresionadaUnaVez(code)) {
				if (this.texto.length() < this.limiteCaracteres) {
					this.texto += (char) code;
				}
				return;
			}
		}

		// 5. Captura de Letras y Guiones si no es solo numérico
		if (!this.soloNumeros) {
			for (int code = KeyEvent.VK_A; code <= KeyEvent.VK_Z; code++) {
				if (Globales.TECLADO.isTeclaPresionadaUnaVez(code)) {
					if (this.texto.length() < this.limiteCaracteres) {
						final boolean shift = Globales.TECLADO.presionaTeclaEnLista(KeyEvent.VK_SHIFT);
						char c = (char) code;
						if (!shift) {
							c = Character.toLowerCase(c);
						}
						this.texto += c;
					}
					return;
				}
			}

			if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_MINUS)
					|| Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_UNDERSCORE)) {
				if (this.texto.length() < this.limiteCaracteres) {
					this.texto += "_";
				}
			}
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

		Render2D.dibujarRectanguloRelleno(g, x, y, w, h, this.activo ? COLOR_FONDO_ACTIVO : COLOR_FONDO_NORMAL);
		Render2D.dibujarRectanguloContorno(g, x, y, w, h, this.activo ? COLOR_BORDE_ACTIVO : COLOR_BORDE_NORMAL);

		final Font fontPrevia = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 14f));

		final String textoDibujado = this.texto + ((this.activo && this.cursorVisible) ? "|" : "");
		Render2D.dibujarStringConSombra(g, textoDibujado, x + 4, (y + h) - 4, Color.WHITE, Color.BLACK);

		g.setFont(fontPrevia);
	}

	public boolean isActivo() {
		return this.activo && this.visible;
	}

	public void setActivo(final boolean activo) {
		this.activo = activo;
	}

	public String getTexto() {
		return this.texto;
	}

	public void setTexto(final String texto) {
		this.texto = (texto != null) ? texto : "";
	}

	public int getNumeroEntero(final int valorPorDefecto) {
		if (this.texto.isEmpty()) {
			return valorPorDefecto;
		}
		try {
			return Integer.parseInt(this.texto);
		} catch (final NumberFormatException e) {
			return valorPorDefecto;
		}
	}
}