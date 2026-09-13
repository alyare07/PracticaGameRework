package principal.maquinaestado.estados.menu;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import principal.configuracion.ConfiguracionGrafica;
import principal.configuracion.LimiteFPS;
import principal.configuracion.ModoEscalado;
import principal.configuracion.PerfilRendimiento;
import principal.configuracion.TipoPantalla;
import principal.controles.Raton;
import principal.igu.textos.TipoTextoFlotante;
import principal.maquinaestado.GestorEstados;
import principal.maquinaestado.estados.menu.herramientas.BotonPixel;
import principal.maquinaestado.estados.menu.herramientas.SelectorOpcionPixel;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Menú de configuración gráfica completo con control de Tipo de Pantalla,
 * Escala de Ventana, Modo de Escalado, Perfil de Hardware y FPS (Zero-GC).
 * 
 * @version 2.0 (Vanilla Java 8)
 */
public class MenuConfiguracionGrafica extends Menu {

	private static final int PANEL_ANCHO = 370;
	private static final int PANEL_ALTO = 220;
	private static final int FILA_ALTO = 22;
	private static final int VISTA_Y = 75;

	private SelectorOpcionPixel selectorTipoPantalla;
	private SelectorOpcionPixel selectorEscalaVentana;
	private SelectorOpcionPixel selectorEscalado;
	private SelectorOpcionPixel selectorPerfil;
	private SelectorOpcionPixel selectorFps;

	private final ArrayList<SelectorOpcionPixel> selectores = new ArrayList<SelectorOpcionPixel>();

	private BotonPixel botonAutoDetectar;
	private BotonPixel botonGuardar;
	private BotonPixel botonVolver;

	private int indiceFilaEnfocada = 0;
	private int ultimoMouseX = -999;
	private int ultimoMouseY = -999;

	public MenuConfiguracionGrafica(final GestorEstados ge) {
		super(ge, "CONFIGURACION GRAFICA");
		this.subtituloMenu = "- VIDEO Y RENDIMIENTO -";
		this.inicializarMenu();
	}

	@Override
	protected void inicializarMenu() {
		this.componentes.clear();
		this.botones.clear();
		this.selectores.clear();

		final int panelX = Constantes.CENTROX - (PANEL_ANCHO / 2);
		final int anchoItem = PANEL_ANCHO - 20;
		final int xItem = panelX + 10;
		int yItem = VISTA_Y + 8;

		// 1. Selector de Tipo de Pantalla (Ventana, Sin Bordes, Exclusiva)
		final TipoPantalla[] tiposPantalla = TipoPantalla.values();
		final String[] nombresPantallas = new String[tiposPantalla.length];
		for (int i = 0; i < tiposPantalla.length; i++) {
			nombresPantallas[i] = tiposPantalla[i].getNombreLegible();
		}
		this.selectorTipoPantalla = new SelectorOpcionPixel(new Rectangle(xItem, yItem, anchoItem, 16), "Visualizacion",
				nombresPantallas, ConfiguracionGrafica.getTipoPantalla().ordinal(), null);
		yItem += FILA_ALTO;

		// 2. Selector de Escala de Ventana (1x, 2x, 3x, 4x, 6x)
		final String[] opcionesEscala = new String[] { "1x (640x360)", "2x (1280x720)", "3x (1920x1080)",
				"4x (2560x1440)", "6x (3840x2160)" };
		final int idxEscala = Math.max(0,
				Math.min(opcionesEscala.length - 1, ConfiguracionGrafica.getEscalaVentana() - 1));
		this.selectorEscalaVentana = new SelectorOpcionPixel(new Rectangle(xItem, yItem, anchoItem, 16),
				"Escala Ventana", opcionesEscala, idxEscala, null);
		yItem += FILA_ALTO;

		// 3. Selector de Modo de Escalado (Entero, Ajuste 16:9, Estirar)
		final ModoEscalado[] modos = ModoEscalado.values();
		final String[] nombresModos = new String[modos.length];
		for (int i = 0; i < modos.length; i++) {
			nombresModos[i] = modos[i].getNombreLegible();
		}
		this.selectorEscalado = new SelectorOpcionPixel(new Rectangle(xItem, yItem, anchoItem, 16), "Escalado (Full)",
				nombresModos, ConfiguracionGrafica.getModoEscalado().ordinal(), null);
		yItem += FILA_ALTO;

		// 4. Selector de Perfil de Rendimiento (POTATO, BASICO, MEDIO, ALTO)
		final PerfilRendimiento[] perfiles = PerfilRendimiento.values();
		final String[] nombresPerfiles = new String[perfiles.length];
		for (int i = 0; i < perfiles.length; i++) {
			nombresPerfiles[i] = perfiles[i].getNombreLegible();
		}
		this.selectorPerfil = new SelectorOpcionPixel(new Rectangle(xItem, yItem, anchoItem, 16), "Perfil Hardware",
				nombresPerfiles, ConfiguracionGrafica.getPerfil().ordinal(), null);
		yItem += FILA_ALTO;

		// 5. Selector de Límite de FPS (30 FPS, 60 FPS, Ilimitado)
		final LimiteFPS[] limites = LimiteFPS.values();
		final String[] nombresLimites = new String[limites.length];
		for (int i = 0; i < limites.length; i++) {
			nombresLimites[i] = limites[i].getNombreLegible();
		}
		this.selectorFps = new SelectorOpcionPixel(new Rectangle(xItem, yItem, anchoItem, 16), "Limite FPS",
				nombresLimites, ConfiguracionGrafica.getLimiteFps().ordinal(), null);

		this.selectores.add(this.selectorTipoPantalla);
		this.selectores.add(this.selectorEscalaVentana);
		this.selectores.add(this.selectorEscalado);
		this.selectores.add(this.selectorPerfil);
		this.selectores.add(this.selectorFps);

		for (int i = 0; i < this.selectores.size(); i++) {
			this.componentes.add(this.selectores.get(i));
		}

		// 6. Botones Inferiores de Acción
		final int yBotones = Constantes.ALTO_JUEGO - 40;
		final int anchoBoton = 100;

		this.botonAutoDetectar = new BotonPixel("Auto-Detectar",
				new Rectangle(Constantes.CENTROX - 160, yBotones, anchoBoton + 10, 18), () -> {
					ConfiguracionGrafica.detectarConfiguracionOptima();
					this.sincronizarSelectoresConConfig();
					Globales.GESTOR_TEXTOS.agregarTexto("Hardware Detectado", Constantes.CENTROX,
							Constantes.CENTROY - 40, TipoTextoFlotante.DANIO_NORMAL);
				});

		this.botonGuardar = new BotonPixel("Aplicar",
				new Rectangle(Constantes.CENTROX - 45, yBotones, anchoBoton - 10, 18), () -> {
					this.aplicarValoresDeSelectores();
					ConfiguracionGrafica.aplicar();
					ConfiguracionGrafica.guardarConfig();
					Globales.GESTOR_TEXTOS.agregarTexto("Configuracion Aplicada", Constantes.CENTROX,
							Constantes.CENTROY - 40, TipoTextoFlotante.ORO_EXP);
				});

		this.botonVolver = new BotonPixel("Volver",
				new Rectangle(Constantes.CENTROX + 55, yBotones, anchoBoton - 10, 18), () -> {
					this.alPresionarEscape();
				});

		this.botones.add(this.botonAutoDetectar);
		this.botones.add(this.botonGuardar);
		this.botones.add(this.botonVolver);

		this.componentes.add(this.botonAutoDetectar);
		this.componentes.add(this.botonGuardar);
		this.componentes.add(this.botonVolver);

		this.actualizarFocoVisual();
	}

	private void sincronizarSelectoresConConfig() {
		this.selectorTipoPantalla.setIndiceSeleccionado(ConfiguracionGrafica.getTipoPantalla().ordinal());
		this.selectorEscalaVentana.setIndiceSeleccionado(ConfiguracionGrafica.getEscalaVentana() - 1);
		this.selectorEscalado.setIndiceSeleccionado(ConfiguracionGrafica.getModoEscalado().ordinal());
		this.selectorPerfil.setIndiceSeleccionado(ConfiguracionGrafica.getPerfil().ordinal());
		this.selectorFps.setIndiceSeleccionado(ConfiguracionGrafica.getLimiteFps().ordinal());
	}

	private void aplicarValoresDeSelectores() {
		ConfiguracionGrafica.setTipoPantalla(TipoPantalla.values()[this.selectorTipoPantalla.getIndiceSeleccionado()]);

		final int idxEsc = this.selectorEscalaVentana.getIndiceSeleccionado();
		final int[] escalas = { 1, 2, 3, 4, 6 };
		final int escalaElegida = ((idxEsc >= 0) && (idxEsc < escalas.length)) ? escalas[idxEsc] : 1;
		ConfiguracionGrafica.setEscalaVentana(escalaElegida);

		ConfiguracionGrafica.setModoEscalado(ModoEscalado.values()[this.selectorEscalado.getIndiceSeleccionado()]);
		ConfiguracionGrafica.setPerfil(PerfilRendimiento.values()[this.selectorPerfil.getIndiceSeleccionado()]);
		ConfiguracionGrafica.setLimiteFps(LimiteFPS.values()[this.selectorFps.getIndiceSeleccionado()]);
	}

	@Override
	public void actualizar() {
		final Raton raton = Globales.RATON;

		// 1. Detección de Hover de Ratón
		final int mx = raton.getPosicionXEscalada();
		final int my = raton.getPosicionYEscalada();
		final boolean mouseSeMovio = (mx != this.ultimoMouseX) || (my != this.ultimoMouseY);

		if (mouseSeMovio) {
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

		// 2. Navegación por Teclado
		this.actualizarNavegacionTecladoGrafico();

		// 3. Actualizar Componentes
		for (int i = 0; i < this.componentes.size(); i++) {
			this.componentes.get(i).actualizar(raton);
		}
	}

	private void actualizarNavegacionTecladoGrafico() {
		final int totalFilas = this.selectores.size() + this.botones.size();

		// W / Flecha Arriba
		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_UP)
				|| Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_W)) {
			this.indiceFilaEnfocada = (this.indiceFilaEnfocada <= 0) ? totalFilas - 1 : this.indiceFilaEnfocada - 1;
			this.actualizarFocoVisual();
			GestorSonido.reproducir(IDSonido.SELECT_MENU);
		}

		// S / Flecha Abajo
		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_DOWN)
				|| Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_S)) {
			this.indiceFilaEnfocada = (this.indiceFilaEnfocada >= (totalFilas - 1)) ? 0 : this.indiceFilaEnfocada + 1;
			this.actualizarFocoVisual();
			GestorSonido.reproducir(IDSonido.SELECT_MENU);
		}

		// A / Flecha Izquierda
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

		// D / Flecha Derecha
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

		// Enter / Espacio
		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_ENTER)
				|| Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_SPACE)) {
			if (this.indiceFilaEnfocada < this.selectores.size()) {
				this.selectores.get(this.indiceFilaEnfocada).siguiente();
			} else {
				final int idxBoton = this.indiceFilaEnfocada - this.selectores.size();
				this.botones.get(idxBoton).accionar();
			}
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
		this.GE.establecerEstadoActual(GestorEstados.NUMERO_ESTADO_MENU_CONFIGURACIONES);
	}

	@Override
	public void pintar(final Graphics2D g) {
		this.pintarFondo(g);
		this.pintarCabecera(g);

		final int panelX = Constantes.CENTROX - (PANEL_ANCHO / 2);

		// Fondo del Panel
		Render2D.dibujarRectanguloRelleno(g, panelX, VISTA_Y, PANEL_ANCHO, PANEL_ALTO, new Color(16, 20, 26, 220));
		Render2D.dibujarRectanguloContorno(g, panelX, VISTA_Y, PANEL_ANCHO, PANEL_ALTO, new Color(55, 60, 75));

		// Renderizado de Selectores y Botones
		for (int i = 0; i < this.componentes.size(); i++) {
			this.componentes.get(i).pintar(g);
		}

		this.pintarGuiaControles(g);
	}
}