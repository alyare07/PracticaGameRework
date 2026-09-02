package principal.maquinaestado.estados.menu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import principal.controles.Raton;
import principal.maquinaestado.GestorEstados;
import principal.maquinaestado.estados.EstadoJuego;
import principal.maquinaestado.estados.menu.herramientas.BotonPixel;
import principal.maquinaestado.estados.menu.herramientas.ComponenteMenu;
import principal.maquinaestado.estados.menu.herramientas.EventoAccion;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Clase base para todos los menús con Auto-Layout y selección unificada
 * (Zero-GC).
 * 
 * @version 3.3 (Vanilla Java 8 - Unified Focus Manager)
 */
public abstract class Menu implements EstadoJuego {

	protected static final int ANCHO_BOTON_DEFECTO = 160;
	protected static final int ALTO_BOTON_DEFECTO = 18;
	protected static final int ESPACIADO_BOTONES = 6;

	protected final GestorEstados GE;
	protected final ArrayList<ComponenteMenu> componentes = new ArrayList<ComponenteMenu>();
	protected final ArrayList<BotonPixel> botones = new ArrayList<BotonPixel>();

	protected String tituloMenu = "";
	protected String subtituloMenu = "";
	protected int indiceBotonEnfocado = 0;

	// Rastreador de movimiento de ratón para alternancia limpia
	private int ultimoMouseX = -999;
	private int ultimoMouseY = -999;

	protected Color colorFondo = new Color(12, 15, 20, 255);

	public Menu(final GestorEstados ge, final String titulo) {
		this.GE = ge;
		this.tituloMenu = (titulo != null) ? titulo : "";
	}

	protected abstract void inicializarMenu();

	protected BotonPixel agregarBoton(final String texto, final EventoAccion accion) {
		final BotonPixel boton = new BotonPixel(texto, new Rectangle(0, 0, ANCHO_BOTON_DEFECTO, ALTO_BOTON_DEFECTO),
				accion);
		this.botones.add(boton);
		this.componentes.add(boton);
		this.recalcularLayoutBotones();
		return boton;
	}

	protected void recalcularLayoutBotones() {
		final int totalBotones = this.botones.size();
		if (totalBotones == 0) {
			return;
		}

		final int altoTotal = (totalBotones * ALTO_BOTON_DEFECTO) + ((totalBotones - 1) * ESPACIADO_BOTONES);
		final int yInicio = (Constantes.CENTROY - (altoTotal / 2)) + 20;

		for (int i = 0; i < totalBotones; i++) {
			final BotonPixel b = this.botones.get(i);
			final int x = Constantes.CENTROX - (b.getArea().width / 2);
			final int y = yInicio + (i * (ALTO_BOTON_DEFECTO + ESPACIADO_BOTONES));
			b.getArea().setLocation(x, y);
		}
	}

	@Override
	public void actualizar() {
		final Raton raton = Globales.RATON;

		// 1. Detección de movimiento físico del ratón
		final int mx = raton.getPosicionXEscalada();
		final int my = raton.getPosicionYEscalada();
		final boolean mouseSeMovio = (mx != this.ultimoMouseX) || (my != this.ultimoMouseY);

		if (mouseSeMovio) {
			this.ultimoMouseX = mx;
			this.ultimoMouseY = my;

			final Point pMouse = raton.getPuntoPosicionEscalado();
			for (int i = 0; i < this.botones.size(); i++) {
				if (this.botones.get(i).getArea().contains(pMouse)) {
					if (this.indiceBotonEnfocado != i) {
						this.establecerIndiceEnfocado(i);
					}
					break;
				}
			}
		}

		// 2. Navegación por Teclado (Tiene prioridad si el jugador toca una tecla)
		this.actualizarNavegacionTeclado();

		// 3. Actualización de componentes
		for (int i = 0; i < this.componentes.size(); i++) {
			final ComponenteMenu c = this.componentes.get(i);
			if (c.isVisible()) {
				c.actualizar(raton);
			}
		}
	}

	protected void actualizarNavegacionTeclado() {
		if (this.botones.isEmpty()) {
			return;
		}

		// W o Flecha Arriba
		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_UP)
				|| Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_W)) {
			final int nuevoIdx = (this.indiceBotonEnfocado <= 0) ? this.botones.size() - 1
					: this.indiceBotonEnfocado - 1;
			this.establecerIndiceEnfocado(nuevoIdx);
			GestorSonido.reproducir(IDSonido.GOLPE_1);
		}

		// S o Flecha Abajo
		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_DOWN)
				|| Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_S)) {
			final int nuevoIdx = (this.indiceBotonEnfocado >= (this.botones.size() - 1)) ? 0
					: this.indiceBotonEnfocado + 1;
			this.establecerIndiceEnfocado(nuevoIdx);
			GestorSonido.reproducir(IDSonido.GOLPE_1);
		}

		// Confirmar con Enter o Espacio
		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_ENTER)
				|| Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_SPACE)) {
			if ((this.indiceBotonEnfocado >= 0) && (this.indiceBotonEnfocado < this.botones.size())) {
				final BotonPixel botonActivo = this.botones.get(this.indiceBotonEnfocado);
				if ((botonActivo != null) && botonActivo.isVisible()) {
					botonActivo.accionar();
				}
			}
		}

		// Cancelar / Volver con Escape
		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_ESCAPE)) {
			this.alPresionarEscape();
		}
	}

	protected void establecerIndiceEnfocado(final int nuevoIndice) {
		this.indiceBotonEnfocado = nuevoIndice;
		for (int i = 0; i < this.botones.size(); i++) {
			this.botones.get(i).setEnfocado(i == this.indiceBotonEnfocado);
		}
	}

	protected void limpiarFoco() {
		this.indiceBotonEnfocado = -1;
		for (int i = 0; i < this.botones.size(); i++) {
			this.botones.get(i).setEnfocado(false);
		}
	}

	protected void alPresionarEscape() {
	}

	@Override
	public void pintar(final Graphics2D g) {
		this.pintarFondo(g);
		this.pintarCabecera(g);

		for (int i = 0; i < this.componentes.size(); i++) {
			final ComponenteMenu c = this.componentes.get(i);
			if (c.isVisible()) {
				c.pintar(g);
			}
		}

		this.pintarGuiaControles(g);
	}

	protected void pintarFondo(final Graphics2D g) {
		Render2D.dibujarRectanguloRelleno(g, 0, 0, Constantes.ANCHO_JUEGO, Constantes.ALTO_JUEGO, this.colorFondo);
	}

	protected void pintarCabecera(final Graphics2D g) {
		if (this.tituloMenu.isEmpty()) {
			return;
		}

		final Font fontPrevia = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 32f));

		final int anchoTitulo = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, this.tituloMenu);
		final int xTitulo = Constantes.CENTROX - (anchoTitulo / 2);
		final int yTitulo = 65;

		Render2D.dibujarStringConSombra(g, this.tituloMenu, xTitulo, yTitulo, new Color(255, 235, 180), Color.BLACK);

		if (!this.subtituloMenu.isEmpty()) {
			g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, 16f));
			final int anchoSub = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, this.subtituloMenu);
			final int xSub = Constantes.CENTROX - (anchoSub / 2);
			Render2D.dibujarStringConSombra(g, this.subtituloMenu, xSub, yTitulo + 16, new Color(150, 160, 175),
					Color.BLACK);
		}

		g.setFont(fontPrevia);
	}

	protected void pintarGuiaControles(final Graphics2D g) {
		final Font fontPrevia = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, 16f));

		final String guia = "[W/S / ^v] Navegar  |  [ENTER] Seleccionar  |  [ESC] Volver";
		final int anchoGuia = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, guia);
		final int xGuia = Constantes.CENTROX - (anchoGuia / 2);

		Render2D.dibujarStringConSombra(g, guia, xGuia, Constantes.ALTO_JUEGO - 12, new Color(110, 120, 135),
				Color.BLACK);
		g.setFont(fontPrevia);
	}
}