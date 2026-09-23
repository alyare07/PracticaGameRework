package principal.maquinaestado.estados.menu;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import principal.configuracion.GestorConfiguracion;
import principal.controles.Raton;
import principal.igu.textos.TipoTextoFlotante;
import principal.maquinaestado.GestorEstados;
import principal.maquinaestado.estados.GestorPartida;
import principal.maquinaestado.estados.menu.herramientas.BotonPixel;
import principal.maquinaestado.estados.menu.herramientas.SelectorOpcionPixel;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Menú de Configuración General: Administra volúmenes de audio en tiempo real.
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class MenuConfiguracionGeneral extends Menu {

	private static final int PANEL_ANCHO = 370;
	private static final int PANEL_ALTO = 180;
	private static final int FILA_ALTO = 26;
	private static final int VISTA_Y = 95;

	private static final String[] NIVELES_VOLUMEN = { "0%", "10%", "20%", "30%", "40%", "50%", "60%", "70%", "80%",
			"90%", "100%" };

	private SelectorOpcionPixel selectorVolumenMaster;
	private SelectorOpcionPixel selectorVolumenMusica;
	private SelectorOpcionPixel selectorVolumenEfectos;

	private final ArrayList<SelectorOpcionPixel> selectores = new ArrayList<SelectorOpcionPixel>();
	private BotonPixel botonGuardar;
	private BotonPixel botonVolver;

	private int indiceFilaEnfocada = 0;
	private int ultimoMouseX = -999;
	private int ultimoMouseY = -999;
	private boolean esDesdePausa = false;

	public MenuConfiguracionGeneral(final GestorEstados ge) {
		super(ge, "CONFIGURACION GENERAL");
		this.subtituloMenu = "- AJUSTES DE SONIDO Y AUDIO -";
		this.inicializarMenu();
	}

	public void setEsDesdePausa(final boolean desdePausa) {
		this.esDesdePausa = desdePausa;
		this.colorFondo = desdePausa ? new Color(6, 8, 12, 220) : new Color(10, 12, 16, 255);
	}

	@Override
	protected void inicializarMenu() {
		this.componentes.clear();
		this.botones.clear();
		this.selectores.clear();

		final int panelX = Constantes.CENTROX - (PANEL_ANCHO / 2);
		final int anchoItem = PANEL_ANCHO - 20;
		final int xItem = panelX + 10;
		int yItem = VISTA_Y + 12;

		// 1. Selector Volumen General
		final int idxGen = Math.max(0, Math.min(10, (int) Math.round(GestorConfiguracion.getVolumenGeneral() * 10.0)));
		this.selectorVolumenMaster = new SelectorOpcionPixel(new Rectangle(xItem, yItem, anchoItem, 16),
				"Volumen General", NIVELES_VOLUMEN, idxGen, () -> {
					GestorConfiguracion.setVolumenGeneral(this.selectorVolumenMaster.getIndiceSeleccionado() / 10.0);
				});
		yItem += FILA_ALTO;

		// 2. Selector Volumen Música
		final int idxMus = Math.max(0, Math.min(10, (int) Math.round(GestorConfiguracion.getVolumenMusica() * 10.0)));
		this.selectorVolumenMusica = new SelectorOpcionPixel(new Rectangle(xItem, yItem, anchoItem, 16),
				"Volumen Musica", NIVELES_VOLUMEN, idxMus, () -> {
					GestorConfiguracion.setVolumenMusica(this.selectorVolumenMusica.getIndiceSeleccionado() / 10.0);
				});
		yItem += FILA_ALTO;

		// 3. Selector Volumen Efectos
		final int idxSfx = Math.max(0, Math.min(10, (int) Math.round(GestorConfiguracion.getVolumenEfectos() * 10.0)));
		this.selectorVolumenEfectos = new SelectorOpcionPixel(new Rectangle(xItem, yItem, anchoItem, 16),
				"Efectos de Sonido", NIVELES_VOLUMEN, idxSfx, () -> {
					GestorConfiguracion.setVolumenEfectos(this.selectorVolumenEfectos.getIndiceSeleccionado() / 10.0);
					GestorSonido.reproducir(IDSonido.GOLPE_1);
				});

		this.selectores.add(this.selectorVolumenMaster);
		this.selectores.add(this.selectorVolumenMusica);
		this.selectores.add(this.selectorVolumenEfectos);

		for (int i = 0; i < this.selectores.size(); i++) {
			this.componentes.add(this.selectores.get(i));
		}

		// 4. Botones de acción inferiores
		final int yBotones = Constantes.ALTO_JUEGO - 42;
		this.botonGuardar = new BotonPixel("Guardar", new Rectangle(Constantes.CENTROX - 110, yBotones, 100, 18),
				() -> {
					GestorConfiguracion.guardarConfiguracion();
					Globales.GESTOR_TEXTOS.agregarTextoFijo("Configuracion de Audio Guardada", Constantes.CENTROX,
							Constantes.CENTROY - 40, TipoTextoFlotante.AVISO_SISTEMA);
				});

		this.botonVolver = new BotonPixel("Volver", new Rectangle(Constantes.CENTROX + 10, yBotones, 100, 18), () -> {
			this.alPresionarEscape();
		});

		this.botones.add(this.botonGuardar);
		this.botones.add(this.botonVolver);
		this.componentes.add(this.botonGuardar);
		this.componentes.add(this.botonVolver);

		this.actualizarFocoVisual();
	}

	@Override
	public void actualizar() {
		final Raton raton = Globales.RATON;

		// Hover ratón
		final int mx = raton.getPosicionXEscalada();
		final int my = raton.getPosicionYEscalada();
		if ((mx != this.ultimoMouseX) || (my != this.ultimoMouseY)) {
			this.ultimoMouseX = mx;
			this.ultimoMouseY = my;
			final Point pMouse = raton.getPuntoPosicionEscalado();

			for (int i = 0; i < this.selectores.size(); i++) {
				if (this.selectores.get(i).getArea().contains(pMouse)) {
					this.indiceFilaEnfocada = i;
					this.actualizarFocoVisual();
					break;
				}
			}

			for (int i = 0; i < this.botones.size(); i++) {
				if (this.botones.get(i).getArea().contains(pMouse)) {
					this.indiceFilaEnfocada = this.selectores.size() + i;
					this.actualizarFocoVisual();
					break;
				}
			}
		}

		// Navegación teclado
		final int totalFilas = this.selectores.size() + this.botones.size();

		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_UP)
				|| Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_W)) {
			this.indiceFilaEnfocada = (this.indiceFilaEnfocada <= 0) ? totalFilas - 1 : this.indiceFilaEnfocada - 1;
			this.actualizarFocoVisual();
			GestorSonido.reproducir(IDSonido.SELECT_MENU);
		}

		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_DOWN)
				|| Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_S)) {
			this.indiceFilaEnfocada = (this.indiceFilaEnfocada >= (totalFilas - 1)) ? 0 : this.indiceFilaEnfocada + 1;
			this.actualizarFocoVisual();
			GestorSonido.reproducir(IDSonido.SELECT_MENU);
		}

		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_LEFT)
				|| Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_A)) {
			if (this.indiceFilaEnfocada < this.selectores.size()) {
				this.selectores.get(this.indiceFilaEnfocada).anterior();
			} else {
				final int idxBoton = this.indiceFilaEnfocada - this.selectores.size();
				final int nuevoIdx = (idxBoton <= 0) ? this.botones.size() - 1 : idxBoton - 1;
				this.indiceFilaEnfocada = this.selectores.size() + nuevoIdx;
				this.actualizarFocoVisual();
				GestorSonido.reproducir(IDSonido.SELECT_MENU);
			}
		}

		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_RIGHT)
				|| Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_D)) {
			if (this.indiceFilaEnfocada < this.selectores.size()) {
				this.selectores.get(this.indiceFilaEnfocada).siguiente();
			} else {
				final int idxBoton = this.indiceFilaEnfocada - this.selectores.size();
				final int nuevoIdx = (idxBoton >= (this.botones.size() - 1)) ? 0 : idxBoton + 1;
				this.indiceFilaEnfocada = this.selectores.size() + nuevoIdx;
				this.actualizarFocoVisual();
				GestorSonido.reproducir(IDSonido.SELECT_MENU);
			}
		}

		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_ENTER)
				|| Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_SPACE)) {
			if (this.indiceFilaEnfocada < this.selectores.size()) {
				this.selectores.get(this.indiceFilaEnfocada).siguiente();
			} else {
				final int idxBoton = this.indiceFilaEnfocada - this.selectores.size();
				this.botones.get(idxBoton).accionar();
			}
		}

		for (int i = 0; i < this.componentes.size(); i++) {
			this.componentes.get(i).actualizar(raton);
		}

		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_ESCAPE)) {
			this.alPresionarEscape();
		}
	}

	private void actualizarFocoVisual() {
		for (int i = 0; i < this.selectores.size(); i++) {
			this.selectores.get(i).setEnfocado(i == this.indiceFilaEnfocada);
		}
		for (int i = 0; i < this.botones.size(); i++) {
			this.botones.get(i).setEnfocado((this.selectores.size() + i) == this.indiceFilaEnfocada);
		}
	}

	@Override
	protected void alPresionarEscape() {
		if (this.esDesdePausa) {
			this.GE.abrirMenuConfiguracionesSeleccionEnPausa();
		} else {
			this.GE.establecerEstadoActual(GestorEstados.NUMERO_ESTADO_MENU_CONFIGURACIONES);
		}
	}

	@Override
	public void pintar(final Graphics2D g) {
		if (this.esDesdePausa && (this.GE.getEstadoActual() instanceof GestorPartida)) {
			final GestorPartida gp = (GestorPartida) this.GE.getEstadoActual();
			if (gp.getGestorJuego() != null) {
				gp.getGestorJuego().pintar(g);
			}
		}

		this.pintarFondo(g);
		this.pintarCabecera(g);

		final int panelX = Constantes.CENTROX - (PANEL_ANCHO / 2);

		Render2D.dibujarRectanguloRelleno(g, panelX, VISTA_Y, PANEL_ANCHO, PANEL_ALTO, new Color(16, 20, 26, 220));
		Render2D.dibujarRectanguloContorno(g, panelX, VISTA_Y, PANEL_ANCHO, PANEL_ALTO, new Color(55, 60, 75));

		for (int i = 0; i < this.componentes.size(); i++) {
			this.componentes.get(i).pintar(g);
		}

		this.pintarGuiaControles(g);
	}
}