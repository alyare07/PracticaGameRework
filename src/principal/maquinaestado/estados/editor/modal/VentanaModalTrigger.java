package principal.maquinaestado.estados.editor.modal;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import principal.controles.Raton;
import principal.mapa.escenario.tps.PuertaArea;
import principal.mapa.escenario.tps.PuertaMapa;
import principal.mapa.escenario.tps.PuertaMundo;
import principal.mapa.escenario.tps.ZonaTP;
import principal.maquinaestado.estados.menu.herramientas.BotonPixel;
import principal.maquinaestado.estados.menu.herramientas.CajaTextoPixel;
import principal.maquinaestado.estados.menu.herramientas.ComponenteMenu;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Inspector modal interactivo para configurar Triggers (ZonaTP). Soporta 3
 * tipos de puertas: A Otro Mapa (.mp), Entre Mundos (mismo mapa) y Local
 * (Coordenadas).
 * 
 * @version 2.0 (Vanilla Java 8)
 */
public class VentanaModalTrigger extends ComponenteMenu {

	private static final int ANCHO_MODAL = 340;
	private static final int ALTO_MODAL = 190;

	private static final Color COLOR_FONDO = new Color(16, 20, 28, 245);
	private static final Color COLOR_BORDE = new Color(255, 60, 60);

	private static final String[] TIPOS_PUERTA = { "A Otro Mapa (.mp)", "Entre Mundos (Mismo Mapa)",
			"Local (Coordenadas X, Y)" };
	private int idxTipoPuerta = 0; // 0 = Mapa, 1 = Mundo, 2 = Local

	private ZonaTP triggerSeleccionado;
	private boolean abierta = false;

	private final Rectangle areaBtnTipo = new Rectangle();
	private CajaTextoPixel ctParametro1;
	private CajaTextoPixel ctParametro2;
	private CajaTextoPixel ctParametro3;

	private BotonPixel btnAplicar;
	private BotonPixel btnCerrar;

	public VentanaModalTrigger() {
		super(new Rectangle(Constantes.CENTROX - (ANCHO_MODAL / 2), Constantes.CENTROY - (ALTO_MODAL / 2), ANCHO_MODAL,
				ALTO_MODAL));
		this.inicializarComponentes();
	}

	private void inicializarComponentes() {
		final int x = this.area.x;
		final int y = this.area.y;

		this.areaBtnTipo.setBounds(x + 130, y + 40, 190, 18);
		this.ctParametro1 = new CajaTextoPixel(new Rectangle(x + 130, y + 68, 190, 16), "Mapa1", 18, false);
		this.ctParametro2 = new CajaTextoPixel(new Rectangle(x + 130, y + 96, 190, 16), "Exterior", 18, false);
		this.ctParametro3 = new CajaTextoPixel(new Rectangle(x + 130, y + 124, 190, 16), "Comienzo", 18, false);

		this.btnAplicar = new BotonPixel("Guardar", new Rectangle(x + 40, (y + ALTO_MODAL) - 30, 110, 18), () -> {
			this.guardarCambios();
			this.cerrar();
		});

		this.btnCerrar = new BotonPixel("Cerrar", new Rectangle(x + 190, (y + ALTO_MODAL) - 30, 110, 18), () -> {
			this.cerrar();
		});
	}

	public void abrir(final ZonaTP trigger) {
		if (trigger == null) {
			return;
		}
		this.triggerSeleccionado = trigger;
		this.abierta = true;
		this.visible = true;

		// Sincronizar el estado del modal con la configuración actual del trigger
		if (trigger.getPuertaTP() instanceof PuertaMundo) {
			this.idxTipoPuerta = 1;
		} else if (trigger.getPuertaTP() instanceof PuertaArea) {
			this.idxTipoPuerta = 2;
		} else {
			this.idxTipoPuerta = 0; // Por defecto PuertaMapa
		}

		this.actualizarPlaceholders();
		GestorSonido.reproducir(IDSonido.GOLPE_1);
	}

	public void cerrar() {
		this.abierta = false;
		this.visible = false;
		this.triggerSeleccionado = null;
	}

	private void guardarCambios() {
		if (this.triggerSeleccionado == null) {
			return;
		}

		final String p1 = this.ctParametro1.getTexto().trim();
		final String p2 = this.ctParametro2.getTexto().trim();
		final String p3 = this.ctParametro3.getTexto().trim();

		switch (this.idxTipoPuerta) {
		case 0: // PuertaMapa
			this.triggerSeleccionado.setPuertaTP(new PuertaMapa(p1, p2, p3, false, null));
			break;
		case 1: // PuertaMundo
			this.triggerSeleccionado.setPuertaTP(new PuertaMundo(p2, p3));
			break;
		case 2: // PuertaArea
			final int dx = this.ctParametro2.getNumeroEntero(0);
			final int dy = this.ctParametro3.getNumeroEntero(0);
			this.triggerSeleccionado.setPuertaTP(new PuertaArea(new Rectangle(dx, dy, 16, 16)));
			break;
		}
	}

	@Override
	public void actualizar(final Raton raton) {
		if (!this.abierta || (raton == null)) {
			return;
		}

		if (raton.presionadoClickIzqUnicaAct()) {
			final Point p = raton.getPuntoPosicionEscalado();
			if (this.areaBtnTipo.contains(p)) {
				this.idxTipoPuerta = (this.idxTipoPuerta + 1) % TIPOS_PUERTA.length;
				this.actualizarPlaceholders();
				GestorSonido.reproducir(IDSonido.GOLPE_1);
			}
		}

		this.ctParametro1.actualizar(raton);
		this.ctParametro2.actualizar(raton);
		this.ctParametro3.actualizar(raton);
		this.btnAplicar.actualizar(raton);
		this.btnCerrar.actualizar(raton);
	}

	private void actualizarPlaceholders() {
		switch (this.idxTipoPuerta) {
		case 0:
			this.ctParametro1.setVisible(true);
			this.ctParametro2.setVisible(true);
			this.ctParametro3.setVisible(true);

			this.ctParametro1.setTexto("Mapa1");
			this.ctParametro2.setTexto("Exterior");
			this.ctParametro3.setTexto("Comienzo");
			break;
		case 1:
			this.ctParametro1.setVisible(false);
			this.ctParametro2.setVisible(true);
			this.ctParametro3.setVisible(true);
			this.ctParametro2.setTexto("Interior_1");
			this.ctParametro3.setTexto("Spawn_Entrada");
			break;
		case 2:
			this.ctParametro1.setVisible(false);
			this.ctParametro2.setVisible(true);
			this.ctParametro3.setVisible(true);
			this.ctParametro2.setTexto("500");
			this.ctParametro3.setTexto("350");
			break;
		}
	}

	@Override
	public void pintar(final Graphics2D g) {
		if (!this.abierta) {
			return;
		}

		final int x = this.area.x;
		final int y = this.area.y;
		final int w = this.area.width;
		final int h = this.area.height;

		Render2D.dibujarRectanguloRelleno(g, 0, 0, Constantes.ANCHO_JUEGO, Constantes.ALTO_JUEGO,
				new Color(0, 0, 0, 180));
		Render2D.dibujarRectanguloRelleno(g, x, y, w, h, COLOR_FONDO);
		Render2D.dibujarRectanguloContorno(g, x, y, w, h, COLOR_BORDE);

		final Font fontPrevia = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 16f));

		final String titulo = "CONFIGURACION DE TRIGGER (TP)";
		final int anchoTit = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, titulo);
		Render2D.dibujarStringConSombra(g, titulo, x + ((w - anchoTit) / 2), y + 22, new Color(255, 100, 100),
				Color.BLACK);

		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, 14f));

		String lbl1 = "Mapa Destino:";
		String lbl2 = "Mundo Destino:";
		String lbl3 = "Spawn Destino:";
		if (this.idxTipoPuerta == 1) {
			lbl1 = "";
			lbl2 = "Mundo Destino:";
			lbl3 = "Spawn Destino:";
		} else if (this.idxTipoPuerta == 2) {
			lbl1 = "";
			lbl2 = "Destino X:";
			lbl3 = "Destino Y:";
		}

		Render2D.dibujarStringConSombra(g, "Tipo de Puerta:", x + 16, y + 54, Color.WHITE, Color.BLACK);
		Render2D.dibujarStringConSombra(g, lbl1, x + 16, y + 80, Color.WHITE, Color.BLACK);
		Render2D.dibujarStringConSombra(g, lbl2, x + 16, y + 108, Color.WHITE, Color.BLACK);
		Render2D.dibujarStringConSombra(g, lbl3, x + 16, y + 136, Color.WHITE, Color.BLACK);

		// Selector de Tipo de Puerta
		Render2D.dibujarRectanguloRelleno(g, this.areaBtnTipo, new Color(28, 35, 48));
		Render2D.dibujarRectanguloContorno(g, this.areaBtnTipo, new Color(75, 80, 95));
		final String txtTipo = TIPOS_PUERTA[this.idxTipoPuerta];
		final int anchoTipo = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, txtTipo);
		Render2D.dibujarStringConSombra(g, txtTipo, this.areaBtnTipo.x + ((this.areaBtnTipo.width - anchoTipo) / 2),
				this.areaBtnTipo.y + 13, new Color(255, 200, 60), Color.BLACK);

		this.ctParametro1.pintar(g);
		this.ctParametro2.pintar(g);
		this.ctParametro3.pintar(g);
		this.btnAplicar.pintar(g);
		this.btnCerrar.pintar(g);

		g.setFont(fontPrevia);
	}

	public boolean isAbierta() {
		return this.abierta;
	}
}