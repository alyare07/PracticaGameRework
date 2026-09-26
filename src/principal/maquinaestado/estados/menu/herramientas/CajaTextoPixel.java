package principal.maquinaestado.estados.menu.herramientas;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import principal.controles.Raton;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Campo de texto y números interactivo Pixel-Art. Soporta mayúsculas (Shift /
 * Caps Lock), espacios, guiones y números (Zero-GC).
 */
public class CajaTextoPixel extends ComponenteMenu {

	private static final Color COLOR_FONDO_NORMAL = new Color(20, 24, 32, 240);
	private static final Color COLOR_FONDO_ACTIVO = new Color(28, 35, 48, 255);
	private static final Color COLOR_BORDE_NORMAL = new Color(55, 60, 75);
	private static final Color COLOR_BORDE_ACTIVO = new Color(220, 180, 50);

	private String texto;
	private final int limiteCaracteres;
	private final boolean soloNumeros;
	private final boolean forzarMinusculas;

	private boolean activo = false;
	private final GestorTiempo gtCursor = new GestorTiempo();
	private boolean cursorVisible = true;
	private boolean permitirEspacios = true;
	private int ultimaTeclaPresionada = -1;
	private final GestorTiempo gtRepeticion = new GestorTiempo();

	public CajaTextoPixel(final Rectangle area, final String textoInicial, final int limiteCaracteres,
			final boolean soloNumeros) {
		this(area, textoInicial, limiteCaracteres, soloNumeros, false);
	}

	public CajaTextoPixel(final Rectangle area, final String textoInicial, final int limiteCaracteres,
			final boolean soloNumeros, final boolean forzarMinusculas) {
		super(area);
		this.limiteCaracteres = Math.max(1, limiteCaracteres);
		this.soloNumeros = soloNumeros;
		this.forzarMinusculas = forzarMinusculas;
		this.setTexto(textoInicial);
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

		// Liberar repetición si se soltó la tecla física
		if ((this.ultimaTeclaPresionada != -1) && !Globales.TECLADO.presionaTeclaEnLista(this.ultimaTeclaPresionada)) {
			this.ultimaTeclaPresionada = -1;
		}

		// 3. Borrado (Backspace)
		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_BACK_SPACE)) {
			if (this.puedeEscribirTecla(KeyEvent.VK_BACK_SPACE)) {

				if (!this.texto.isEmpty()) {
					this.texto = this.texto.substring(0, this.texto.length() - 1);
				}
				this.registrarPulsacion(KeyEvent.VK_BACK_SPACE);
			}
			return;
		}

		// 4. Captura de Números (0..9)
		for (int code = KeyEvent.VK_0; code <= KeyEvent.VK_9; code++) {
			if (Globales.TECLADO.isTeclaPresionadaUnaVez(code)) {
				if (this.puedeEscribirTecla(code)) {
					if (this.texto.length() < this.limiteCaracteres) {
						this.texto += (char) code;
					}
					this.registrarPulsacion(code);
				}
				return;
			}
		}

		// 5. Captura de Letras, Espacios y Puntuación
		if (!this.soloNumeros) {
			// Detección de Mayúsculas: Shift presionado O Bloq Mayús activo
			boolean mayusculas = false;
			if (!this.forzarMinusculas) {
				mayusculas = Globales.TECLADO.presionaTeclaEnLista(KeyEvent.VK_SHIFT);
				try {
					final boolean capsLock = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
					mayusculas = mayusculas ^ capsLock; // XOR: Si ambos están activos, se invierten a minúscula
				} catch (final Throwable ignored) {
				}
			}

			// Letras A-Z
			for (int code = KeyEvent.VK_A; code <= KeyEvent.VK_Z; code++) {
				if (Globales.TECLADO.isTeclaPresionadaUnaVez(code)) {
					if (this.puedeEscribirTecla(code)) {
						if (this.texto.length() < this.limiteCaracteres) {
							char c = (char) code;
							if (!mayusculas) {
								c = Character.toLowerCase(c);
							}
							this.texto += c;
						}
						this.registrarPulsacion(code);
					}
					return;
				}
			}

			// Espacio (Solo si no es numérico y la caja permite espacios)
			if (this.permitirEspacios && Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_SPACE)) {
				if (this.puedeEscribirTecla(KeyEvent.VK_SPACE)) {
					if (this.texto.length() < this.limiteCaracteres) {
						this.texto += " ";
					}
					this.registrarPulsacion(KeyEvent.VK_SPACE);
				}
				return;
			}

			// Guión normal (-) y Guión bajo (_)
			if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_MINUS)
					|| Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_UNDERSCORE)) {
				if (this.puedeEscribirTecla(KeyEvent.VK_MINUS)) {
					if (this.texto.length() < this.limiteCaracteres) {
						final boolean shift = Globales.TECLADO.presionaTeclaEnLista(KeyEvent.VK_SHIFT);
						this.texto += shift ? "_" : "-";
					}
					this.registrarPulsacion(KeyEvent.VK_MINUS);
				}
				return;
			}

			// Punto (.)
			if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_PERIOD)) {
				if (this.puedeEscribirTecla(KeyEvent.VK_PERIOD)) {
					if (this.texto.length() < this.limiteCaracteres) {
						this.texto += ".";
					}
					this.registrarPulsacion(KeyEvent.VK_PERIOD);
				}
			}
		}
	}

	private boolean puedeEscribirTecla(final int code) {
		if (this.ultimaTeclaPresionada != code) {
			return true;
		}
		return this.gtRepeticion.transcurrioMiliSegundos(350);
	}

	private void registrarPulsacion(final int code) {
		this.ultimaTeclaPresionada = code;
		this.gtRepeticion.establecerReferenciaTiempoActual();
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
		if (texto == null) {
			this.texto = "";
			return;
		}
		this.texto = this.forzarMinusculas ? texto.toLowerCase() : texto;
	}

	public boolean isPermitirEspacios() {
		return this.permitirEspacios;
	}

	public CajaTextoPixel setPermitirEspacios(final boolean permitirEspacios) {
		this.permitirEspacios = permitirEspacios;
		if (!permitirEspacios && (this.texto != null)) {
			this.texto = this.texto.replace(" ", "_");
		}
		return this;
	}

	public int getNumeroEntero(final int valorPorDefecto) {
		if (this.texto.isEmpty()) {
			return valorPorDefecto;
		}
		try {
			return Integer.parseInt(this.texto.trim());
		} catch (final NumberFormatException e) {
			return valorPorDefecto;
		}
	}
}