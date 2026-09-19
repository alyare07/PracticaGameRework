package principal.maquinaestado.estados.menu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.simple.JSONObject;

import principal.controles.Raton;
import principal.maquinaestado.GestorEstados;
import principal.maquinaestado.estados.menu.herramientas.BotonPixel;
import principal.persistencia.GestorGuardado;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Menú de Cargar Partida con 10 slots desplazables por scroll y teclado.
 * 
 * @version 2.0 (Vanilla Java 8 - Scrollable 10-Slot Load Menu)
 */
public class MenuCargarPartida extends Menu {

	private static final int TOTAL_SLOTS = GestorGuardado.CANTIDAD_SLOTS_MAX;
	private static final int ANCHO_PANEL_SLOT = 380;
	private static final int ALTO_PANEL_SLOT = 36;
	private static final int ESPACIADO_SLOT = 6;

	private static final int VISTA_Y = 80;
	private static final int VISTA_ALTO = 220;

	private final BotonPixel[] botonesSlots = new BotonPixel[TOTAL_SLOTS];
	private final String[] infoSlots = new String[TOTAL_SLOTS];
	private final String[] nombresGuardados = new String[TOTAL_SLOTS];
	private final boolean[] slotsOcupados = new boolean[TOTAL_SLOTS];

	private BotonPixel botonVolver;
	private static final SimpleDateFormat FORMATO_FECHA = new SimpleDateFormat("dd/MM/yyyy HH:mm");

	private int scrollY = 0;
	private int maxScrollY = 0;
	private int ultimoMouseY = -999;
	private boolean esDesdePausa = false;

	public MenuCargarPartida(final GestorEstados ge) {
		super(ge, "CARGAR PARTIDA");
		this.subtituloMenu = "- SELECCIONA UN ARCHIVO DE GUARDADO -";
		this.colorFondo = new Color(10, 12, 16, 255);
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

		final int panelX = Constantes.CENTROX - (ANCHO_PANEL_SLOT / 2);
		final int altoTotalContenido = TOTAL_SLOTS * (ALTO_PANEL_SLOT + ESPACIADO_SLOT);
		this.maxScrollY = Math.max(0, altoTotalContenido - VISTA_ALTO);

		for (int i = 0; i < TOTAL_SLOTS; i++) {
			final int slotNum = i + 1;
			final int ySlot = VISTA_Y + 4 + (i * (ALTO_PANEL_SLOT + ESPACIADO_SLOT));

			final JSONObject saveJson = GestorGuardado.leerJsonGuardado(slotNum);
			this.slotsOcupados[i] = (saveJson != null);

			if (this.slotsOcupados[i]) {
				final long timestamp = (saveJson.get("timestamp") != null)
						? ((Number) saveJson.get("timestamp")).longValue()
						: 0L;
				final String fechaStr = (timestamp > 0) ? FORMATO_FECHA.format(new Date(timestamp)) : "Fecha desc.";

				this.nombresGuardados[i] = (saveJson.get("nombreGuardado") != null)
						? saveJson.get("nombreGuardado").toString()
						: ("Slot " + slotNum);

				String infoExtra = "";
				if (saveJson.get("calendario") instanceof JSONObject) {
					final JSONObject jCal = (JSONObject) saveJson.get("calendario");
					final String dia = (jCal.get("diaActual") != null) ? jCal.get("diaActual").toString() : "1";
					final String hora = (jCal.get("hora24h") != null) ? jCal.get("hora24h").toString() : "12:00";
					infoExtra = "Día " + dia + " · " + hora;
				}
				this.infoSlots[i] = fechaStr + " (" + infoExtra + ")";
			} else {
				this.nombresGuardados[i] = "Slot " + slotNum + " [Vacío]";
				this.infoSlots[i] = "Sin datos guardados";
			}

			final BotonPixel btnSlot = new BotonPixel(this.nombresGuardados[i],
					new Rectangle(panelX, ySlot, ANCHO_PANEL_SLOT, ALTO_PANEL_SLOT), () -> {
						if (GestorGuardado.existePartida(slotNum)) {
							GestorSonido.reproducir(IDSonido.SELECT);
							MenuCargarPartida.this.GE.cargarPartidaSlot(slotNum);
						} else {
							GestorSonido.reproducir(IDSonido.SIN_MUNICION);
						}
					});

			this.botonesSlots[i] = btnSlot;
			this.componentes.add(btnSlot);
			this.botones.add(btnSlot);
		}

		final int yVolver = Constantes.ALTO_JUEGO - 36;
		this.botonVolver = new BotonPixel("Volver", new Rectangle(Constantes.CENTROX - 60, yVolver, 120, 18), () -> {
			this.alPresionarEscape();
		});

		this.componentes.add(this.botonVolver);
		this.botones.add(this.botonVolver);

		this.establecerIndiceEnfocado(0);
	}

	@Override
	public void actualizar() {
		final Raton raton = Globales.RATON;

		// 1. Desplazamiento con la rueda del ratón
		final int rueda = raton.getRotacionRueda();
		if ((rueda != 0) && (this.maxScrollY > 0)) {
			this.scrollY = Math.max(0, Math.min(this.maxScrollY, this.scrollY + (rueda * 24)));
		}

		// 2. Hover con el ratón ajustado al scroll
		final int my = raton.getPosicionYEscalada();
		if (my != this.ultimoMouseY) {
			this.ultimoMouseY = my;
			final Point pMouse = raton.getPuntoPosicionEscalado();

			for (int i = 0; i < TOTAL_SLOTS; i++) {
				final Rectangle rAparente = new Rectangle(this.botonesSlots[i].getArea().x,
						this.botonesSlots[i].getArea().y - this.scrollY, this.botonesSlots[i].getArea().width,
						this.botonesSlots[i].getArea().height);

				if (rAparente.contains(pMouse) && (pMouse.y >= VISTA_Y) && (pMouse.y <= (VISTA_Y + VISTA_ALTO))) {
					if (this.indiceBotonEnfocado != i) {
						this.establecerIndiceEnfocado(i);
					}
					break;
				}
			}

			if (this.botonVolver.getArea().contains(pMouse)) {
				this.establecerIndiceEnfocado(TOTAL_SLOTS);
			}
		}

		// 3. Navegación por teclado con auto-scroll
		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_UP)
				|| Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_W)) {
			final int total = this.botones.size();
			this.establecerIndiceEnfocado((this.indiceBotonEnfocado <= 0) ? total - 1 : this.indiceBotonEnfocado - 1);
			this.ajustarScrollAlFoco();
			GestorSonido.reproducir(IDSonido.SELECT_MENU);
		}

		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_DOWN)
				|| Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_S)) {
			final int total = this.botones.size();
			this.establecerIndiceEnfocado((this.indiceBotonEnfocado >= (total - 1)) ? 0 : this.indiceBotonEnfocado + 1);
			this.ajustarScrollAlFoco();
			GestorSonido.reproducir(IDSonido.SELECT_MENU);
		}

		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_ENTER)
				|| Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_SPACE)) {
			if ((this.indiceBotonEnfocado >= 0) && (this.indiceBotonEnfocado < this.botones.size())) {
				this.botones.get(this.indiceBotonEnfocado).accionar();
			}
		}

		// 4. Actualización de clics
		for (int i = 0; i < TOTAL_SLOTS; i++) {
			final BotonPixel b = this.botonesSlots[i];
			final int yReal = b.getArea().y;
			b.getArea().y -= this.scrollY;
			if (((b.getArea().y + b.getArea().height) >= VISTA_Y) && (b.getArea().y <= (VISTA_Y + VISTA_ALTO))) {
				b.actualizar(raton);
			}
			b.getArea().y = yReal;
		}

		this.botonVolver.actualizar(raton);

		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_ESCAPE)) {
			this.alPresionarEscape();
		}
	}

	private void ajustarScrollAlFoco() {
		if ((this.indiceBotonEnfocado >= 0) && (this.indiceBotonEnfocado < TOTAL_SLOTS)) {
			final int ySlot = this.botonesSlots[this.indiceBotonEnfocado].getArea().y - VISTA_Y;
			if ((ySlot - this.scrollY) < 0) {
				this.scrollY = Math.max(0, ySlot);
			} else if (((ySlot + ALTO_PANEL_SLOT) - this.scrollY) > VISTA_ALTO) {
				this.scrollY = Math.min(this.maxScrollY, ((ySlot + ALTO_PANEL_SLOT) - VISTA_ALTO) + 8);
			}
		}
	}

	@Override
	protected void alPresionarEscape() {
		if (this.esDesdePausa) {
			this.GE.establecerEstadoActual(GestorEstados.NUMERO_ESTADO_PARTIDA);
			if (this.GE.getEstadoActual() instanceof principal.maquinaestado.estados.GestorPartida) {
				((principal.maquinaestado.estados.GestorPartida) this.GE.getEstadoActual())
						.establecerEstadoActivoMenu();
			}
		} else {
			this.GE.establecerEstadoActual(GestorEstados.NUMERO_ESTADO_MENU);
		}
	}

	@Override
	public void pintar(final Graphics2D g) {
		if (this.esDesdePausa && (this.GE.getEstadoActual() instanceof principal.maquinaestado.estados.GestorPartida)) {
			final principal.maquinaestado.estados.GestorPartida gp = (principal.maquinaestado.estados.GestorPartida) this.GE
					.getEstadoActual();
			if (gp.getGestorJuego() != null) {
				gp.getGestorJuego().pintar(g);
			}
		}

		this.pintarFondo(g);
		this.pintarCabecera(g);

		final int panelX = Constantes.CENTROX - (ANCHO_PANEL_SLOT / 2);

		// Contenedor
		Render2D.dibujarRectanguloRelleno(g, panelX, VISTA_Y, ANCHO_PANEL_SLOT, VISTA_ALTO, new Color(16, 20, 26, 230));
		Render2D.dibujarRectanguloContorno(g, panelX, VISTA_Y, ANCHO_PANEL_SLOT, VISTA_ALTO, new Color(55, 60, 75));

		final Graphics2D gClip = (Graphics2D) g.create();
		try {
			gClip.setClip(panelX + 2, VISTA_Y + 2, ANCHO_PANEL_SLOT - 4, VISTA_ALTO - 4);

			for (int i = 0; i < TOTAL_SLOTS; i++) {
				final BotonPixel btn = this.botonesSlots[i];
				final int ySlot = btn.getArea().y - this.scrollY;

				if (((ySlot + ALTO_PANEL_SLOT) >= VISTA_Y) && (ySlot <= (VISTA_Y + VISTA_ALTO))) {
					final int yReal = btn.getArea().y;
					btn.getArea().y = ySlot;
					btn.pintar(gClip);
					btn.getArea().y = yReal;

					// Subtítulo con fecha y detalles
					final Font fontPrevia = gClip.getFont();
					gClip.setFont(Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, 12f));
					final Color cInfo = this.slotsOcupados[i] ? new Color(200, 205, 220) : new Color(120, 125, 135);
					Render2D.dibujarStringConSombra(gClip, this.infoSlots[i], btn.getArea().x + 14,
							(ySlot + ALTO_PANEL_SLOT) - 5, cInfo, Color.BLACK);
					gClip.setFont(fontPrevia);
				}
			}
		} finally {
			gClip.dispose();
		}

		// Scrollbar
		if (this.maxScrollY > 0) {
			final int trackX = (panelX + ANCHO_PANEL_SLOT) - 5;
			final int trackY = VISTA_Y + 4;
			final int trackH = VISTA_ALTO - 8;
			Render2D.dibujarRectanguloRelleno(g, trackX, trackY, 3, trackH, new Color(30, 35, 45));

			final double ratio = (double) this.scrollY / this.maxScrollY;
			final int thumbH = Math.max(16, (int) (((double) VISTA_ALTO / (VISTA_ALTO + this.maxScrollY)) * trackH));
			final int thumbY = trackY + (int) (ratio * (trackH - thumbH));

			Render2D.dibujarRectanguloRelleno(g, trackX, thumbY, 3, thumbH, new Color(220, 180, 50));
		}

		this.botonVolver.pintar(g);
		this.pintarGuiaControles(g);
	}
}