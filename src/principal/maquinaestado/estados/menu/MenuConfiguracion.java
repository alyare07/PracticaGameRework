package principal.maquinaestado.estados.menu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import principal.controles.Raton;
import principal.controles.Tecla;
import principal.maquinaestado.GestorEstados;
import principal.maquinaestado.estados.menu.herramientas.BotonPixel;
import principal.maquinaestado.estados.menu.herramientas.CajaTeclaPixel;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Menú de configuración de controles con foco unificado y scroll por rueda
 * (Zero-GC).
 * 
 * @version 3.3 (Vanilla Java 8)
 */
public class MenuConfiguracion extends Menu {

	private static final int FILA_ALTO = 22;
	private static final int VISTA_Y = 85;
	private static final int VISTA_ALTO = 210;
	private static final int PANEL_ANCHO = 360;

	private final ArrayList<CajaTeclaPixel> cajasTeclas = new ArrayList<CajaTeclaPixel>();
	private BotonPixel botonGuardar;
	private BotonPixel botonVolver;

	private int scrollY = 0;
	private int maxScrollY = 0;

	private int ultimoMouseX = -999;
	private int ultimoMouseY = -999;

	public MenuConfiguracion(final GestorEstados ge) {
		super(ge, "CONFIGURACION DE CONTROLES");
		this.subtituloMenu = "- REASIGNACION DE TECLAS -";
		this.inicializarMenu();
	}

	@Override
	protected void inicializarMenu() {
		this.cajasTeclas.clear();
		this.componentes.clear();
		this.botones.clear();

		final int panelX = Constantes.CENTROX - (PANEL_ANCHO / 2);
		int yItem = VISTA_Y + 4;

		for (final Tecla t : Globales.TECLADO.TECLAS_MODIFICABLES.values()) {
			final Rectangle areaCaja = new Rectangle((panelX + PANEL_ANCHO) - 100, yItem, 90, 16);
			final CajaTeclaPixel caja = new CajaTeclaPixel(areaCaja, t);
			this.cajasTeclas.add(caja);
			yItem += FILA_ALTO;
		}

		final int altoContenidoTotal = yItem - VISTA_Y;
		this.maxScrollY = Math.max(0, altoContenidoTotal - VISTA_ALTO);

		final int yBotones = Constantes.ALTO_JUEGO - 40;
		this.botonGuardar = new BotonPixel("Guardar", new Rectangle(Constantes.CENTROX - 110, yBotones, 100, 18),
				() -> {
					for (int i = 0; i < this.cajasTeclas.size(); i++) {
						this.cajasTeclas.get(i).aplicarCambios();
					}
					Globales.TECLADO.guardarConfig();
					Globales.GESTOR_TEXTOS.agregarTexto("Configuracion Guardada", Constantes.CENTROX,
							Constantes.CENTROY - 40, principal.igu.textos.TipoTextoFlotante.ORO_EXP);
				});

		this.botonVolver = new BotonPixel("Volver", new Rectangle(Constantes.CENTROX + 10, yBotones, 100, 18), () -> {
			this.alPresionarEscape();
		});

		this.componentes.add(this.botonGuardar);
		this.componentes.add(this.botonVolver);
		this.botones.add(this.botonGuardar);
		this.botones.add(this.botonVolver);

		this.establecerIndiceEnfocado(0);
	}

	@Override
	public void actualizar() {
		final Raton raton = Globales.RATON;

		// 1. Scroll por rueda del ratón
		final int rueda = raton.getRotacionRueda();
		if (rueda != 0) {
			this.scrollY = Math.max(0, Math.min(this.maxScrollY, this.scrollY + (rueda * 24)));
		}

		// 2. Actualizar cajas de teclas con scroll
		for (int i = 0; i < this.cajasTeclas.size(); i++) {
			this.cajasTeclas.get(i).actualizarConScroll(raton, this.scrollY);
		}

		// 3. Detección de movimiento del ratón para alternar foco entre Guardar y
		// Volver
		final int mx = raton.getPosicionXEscalada();
		final int my = raton.getPosicionYEscalada();
		final boolean mouseSeMovio = (mx != this.ultimoMouseX) || (my != this.ultimoMouseY);

		if (mouseSeMovio) {
			this.ultimoMouseX = mx;
			this.ultimoMouseY = my;
			final Point pMouse = raton.getPuntoPosicionEscalado();

			if (this.botonGuardar.getArea().contains(pMouse)) {
				this.establecerIndiceEnfocado(0);
			} else if (this.botonVolver.getArea().contains(pMouse)) {
				this.establecerIndiceEnfocado(1);
			}
		}

		// 4. Navegación por Teclado entre Guardar (0) y Volver (1)
		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_LEFT)
				|| Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_A)) {
			this.establecerIndiceEnfocado(0);
			GestorSonido.reproducir(IDSonido.GOLPE_1);
		}
		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_RIGHT)
				|| Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_D)) {
			this.establecerIndiceEnfocado(1);
			GestorSonido.reproducir(IDSonido.GOLPE_1);
		}

		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_ENTER)
				|| Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_SPACE)) {
			if (this.indiceBotonEnfocado == 0) {
				this.botonGuardar.accionar();
			} else if (this.indiceBotonEnfocado == 1) {
				this.botonVolver.accionar();
			}
		}

		this.botonGuardar.actualizar(raton);
		this.botonVolver.actualizar(raton);

		// 5. Atajo Escape
		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_ESCAPE)) {
			this.alPresionarEscape();
		}
	}

	@Override
	protected void alPresionarEscape() {
		this.GE.establecerEstadoActual(GestorEstados.NUMERO_ESTADO_MENU);
	}

	@Override
	public void pintar(final Graphics2D g) {
		this.pintarFondo(g);
		this.pintarCabecera(g);

		final int panelX = Constantes.CENTROX - (PANEL_ANCHO / 2);

		// 1. Panel de fondo
		Render2D.dibujarRectanguloRelleno(g, panelX, VISTA_Y, PANEL_ANCHO, VISTA_ALTO, new Color(16, 20, 26, 220));
		Render2D.dibujarRectanguloContorno(g, panelX, VISTA_Y, PANEL_ANCHO, VISTA_ALTO, new Color(55, 60, 75));

		// 2. Lista recortada con scroll
		final Graphics2D gClip = (Graphics2D) g.create();
		try {
			gClip.setClip(panelX + 2, VISTA_Y + 2, PANEL_ANCHO - 4, VISTA_ALTO - 4);

			final Font fontPrevia = gClip.getFont();
			gClip.setFont(Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, 16f));

			for (int i = 0; i < this.cajasTeclas.size(); i++) {
				final CajaTeclaPixel caja = this.cajasTeclas.get(i);
				final int itemY = caja.getArea().y - this.scrollY;

				if (((itemY + FILA_ALTO) >= VISTA_Y) && (itemY <= (VISTA_Y + VISTA_ALTO))) {
					final String nombreAccion = caja.getTecla().getNombre();
					Render2D.dibujarStringConSombra(gClip, nombreAccion, panelX + 12,
							(itemY + caja.getArea().height) - 4, Color.WHITE, Color.BLACK);

					caja.pintarConScroll(gClip, this.scrollY);
				}
			}
			gClip.setFont(fontPrevia);

		} finally {
			gClip.dispose();
		}

		// 3. Barra de scroll
		if (this.maxScrollY > 0) {
			final int trackX = (panelX + PANEL_ANCHO) - 6;
			final int trackY = VISTA_Y + 4;
			final int trackH = VISTA_ALTO - 8;
			Render2D.dibujarRectanguloRelleno(g, trackX, trackY, 3, trackH, new Color(30, 35, 45));

			final double ratio = (double) this.scrollY / this.maxScrollY;
			final int thumbH = Math.max(16, (int) (((double) VISTA_ALTO / (VISTA_ALTO + this.maxScrollY)) * trackH));
			final int thumbY = trackY + (int) (ratio * (trackH - thumbH));

			Render2D.dibujarRectanguloRelleno(g, trackX, thumbY, 3, thumbH, new Color(220, 180, 50));
		}

		// 4. Botones
		this.botonGuardar.pintar(g);
		this.botonVolver.pintar(g);

		this.pintarGuiaControles(g);
	}
}