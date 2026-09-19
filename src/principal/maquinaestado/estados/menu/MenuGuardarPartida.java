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
import principal.igu.textos.TipoTextoFlotante;
import principal.maquinaestado.GestorEstados;
import principal.maquinaestado.estados.GestorPartida;
import principal.maquinaestado.estados.menu.herramientas.BotonPixel;
import principal.maquinaestado.estados.menu.herramientas.CajaTextoPixel;
import principal.persistencia.GestorGuardado;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Menú para guardar partida en 10 slots con asignación de nombre personalizado.
 * 
 * @version 1.0 (Vanilla Java 8 - Multi-Slot Save Menu with Custom Naming)
 */
public class MenuGuardarPartida extends Menu {

	private static final int TOTAL_SLOTS = GestorGuardado.CANTIDAD_SLOTS_MAX;
	private static final int ANCHO_PANEL = 380;
	private static final int ALTO_PANEL_SLOT = 34;
	private static final int ESPACIADO_SLOT = 5;

	private static final int VISTA_Y = 105;
	private static final int VISTA_ALTO = 195;

	private final GestorPartida GP;
	private final BotonPixel[] botonesSlots = new BotonPixel[TOTAL_SLOTS];
	private final String[] infoSlots = new String[TOTAL_SLOTS];
	private final boolean[] slotsOcupados = new boolean[TOTAL_SLOTS];

	private CajaTextoPixel ctNombrePartida;
	private BotonPixel botonGuardarConfirmar;
	private BotonPixel botonVolver;

	private int slotSeleccionado = 0; // 0..9 (Slot 1 a 10)
	private int scrollY = 0;
	private int maxScrollY = 0;
	private static final SimpleDateFormat FORMATO_FECHA = new SimpleDateFormat("dd/MM/yyyy HH:mm");

	public MenuGuardarPartida(final GestorEstados ge, final GestorPartida gp) {
		super(ge, "GUARDAR PARTIDA");
		this.GP = gp;
		this.subtituloMenu = "- ELIGE UN SLOT Y ASIGNA UN NOMBRE -";
		this.colorFondo = new Color(6, 8, 12, 220);
		this.inicializarMenu();
	}

	@Override
	protected void inicializarMenu() {
		this.componentes.clear();
		this.botones.clear();

		final int panelX = Constantes.CENTROX - (ANCHO_PANEL / 2);

		// 1. Caja de texto para el nombre de la partida
		this.ctNombrePartida = new CajaTextoPixel(new Rectangle(panelX + 110, 72, ANCHO_PANEL - 110, 18), "Mi Partida", 26, false);
		this.componentes.add(this.ctNombrePartida);

		// 2. Slots de guardado
		final int altoTotal = TOTAL_SLOTS * (ALTO_PANEL_SLOT + ESPACIADO_SLOT);
		this.maxScrollY = Math.max(0, altoTotal - VISTA_ALTO);

		this.recargarSlots();

		// 3. Botones de acción inferiores
		final int yBotones = Constantes.ALTO_JUEGO - 36;
		this.botonGuardarConfirmar = new BotonPixel("Guardar", new Rectangle(Constantes.CENTROX - 110, yBotones, 100, 18), () -> {
			this.ejecutarGuardado();
		});

		this.botonVolver = new BotonPixel("Cancelar", new Rectangle(Constantes.CENTROX + 10, yBotones, 100, 18), () -> {
			this.alPresionarEscape();
		});

		this.componentes.add(this.botonGuardarConfirmar);
		this.componentes.add(this.botonVolver);
		this.botones.add(this.botonGuardarConfirmar);
		this.botones.add(this.botonVolver);

		this.establecerIndiceEnfocado(0);
	}

	public void recargarSlots() {
		final int panelX = Constantes.CENTROX - (ANCHO_PANEL / 2);

		for (int i = 0; i < TOTAL_SLOTS; i++) {
			final int slotNum = i + 1;
			final int ySlot = VISTA_Y + 4 + (i * (ALTO_PANEL_SLOT + ESPACIADO_SLOT));

			final JSONObject saveJson = GestorGuardado.leerJsonGuardado(slotNum);
			this.slotsOcupados[i] = (saveJson != null);

			String nombreSlot = "Slot " + slotNum;
			if (this.slotsOcupados[i]) {
				if (saveJson.get("nombreGuardado") != null) {
					nombreSlot = saveJson.get("nombreGuardado").toString();
				}
				final long timestamp = (saveJson.get("timestamp") != null) ? ((Number) saveJson.get("timestamp")).longValue() : 0L;
				this.infoSlots[i] = (timestamp > 0) ? FORMATO_FECHA.format(new Date(timestamp)) : "Sin fecha";
			} else {
				nombreSlot = "Slot " + slotNum + " [Vacío]";
				this.infoSlots[i] = "Disponible para guardar";
			}

			final int idx = i;
			final BotonPixel btn = new BotonPixel(nombreSlot, new Rectangle(panelX, ySlot, ANCHO_PANEL, ALTO_PANEL_SLOT), () -> {
				this.slotSeleccionado = idx;
				if (this.slotsOcupados[idx]) {
					final JSONObject json = GestorGuardado.leerJsonGuardado(idx + 1);
					if (json != null && json.get("nombreGuardado") != null) {
						this.ctNombrePartida.setTexto(json.get("nombreGuardado").toString());
					}
				} else {
					this.ctNombrePartida.setTexto("Partida " + (idx + 1));
				}
			});

			this.botonesSlots[i] = btn;
			this.componentes.add(btn);
		}
	}

	private void ejecutarGuardado() {
		if (this.GP == null || this.GP.getGestorJuego() == null) {
			return;
		}

		final int slotNum = this.slotSeleccionado + 1;
		final String nombre = this.ctNombrePartida.getTexto().trim();

		final boolean exito = GestorGuardado.guardarPartida(slotNum, nombre, this.GP.getGestorJuego());
		if (exito) {
			GestorSonido.reproducir(IDSonido.SELECT);
			Globales.GESTOR_TEXTOS.agregarTexto("¡Guardado en Slot " + slotNum + "!", Constantes.CENTROX, Constantes.CENTROY - 40, TipoTextoFlotante.ORO_EXP);
			this.alPresionarEscape();
		} else {
			GestorSonido.reproducir(IDSonido.SIN_MUNICION);
		}
	}

	@Override
	public void actualizar() {
		final Raton raton = Globales.RATON;

		// 1. Scroll
		final int rueda = raton.getRotacionRueda();
		if (rueda != 0 && this.maxScrollY > 0) {
			this.scrollY = Math.max(0, Math.min(this.maxScrollY, this.scrollY + (rueda * 24)));
		}

		this.ctNombrePartida.actualizar(raton);

		// 2. Selección de Slot
		if (raton.presionadoClickIzqUnicaAct()) {
			final Point p = raton.getPuntoPosicionEscalado();
			if (p.y >= VISTA_Y && p.y <= VISTA_Y + VISTA_ALTO) {
				for (int i = 0; i < TOTAL_SLOTS; i++) {
					final Rectangle rAparente = new Rectangle(this.botonesSlots[i].getArea().x,
							this.botonesSlots[i].getArea().y - this.scrollY, this.botonesSlots[i].getArea().width,
							this.botonesSlots[i].getArea().height);
					if (rAparente.contains(p)) {
						this.slotSeleccionado = i;
						this.botonesSlots[i].accionar();
						break;
					}
				}
			}
		}

		// 3. Botones inferiores
		this.botonGuardarConfirmar.actualizar(raton);
		this.botonVolver.actualizar(raton);

		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_ESCAPE)) {
			this.alPresionarEscape();
		}
	}

	@Override
	protected void alPresionarEscape() {
		this.GE.establecerEstadoActual(GestorEstados.NUMERO_ESTADO_PARTIDA);
		if (this.GE.getEstadoActual() instanceof GestorPartida) {
			((GestorPartida) this.GE.getEstadoActual()).establecerEstadoActivoMenu();
		}
	}

	@Override
	public void pintar(final Graphics2D g) {
		if (this.GP != null && this.GP.getGestorJuego() != null) {
			this.GP.getGestorJuego().pintar(g);
		}

		this.pintarFondo(g);
		this.pintarCabecera(g);

		final int panelX = Constantes.CENTROX - (ANCHO_PANEL / 2);

		// Campo de nombre
		final Font fontPrevia = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 14f));
		Render2D.dibujarStringConSombra(g, "Nombre Partida:", panelX + 4, 85, Color.WHITE, Color.BLACK);
		g.setFont(fontPrevia);

		this.ctNombrePartida.pintar(g);

		// Panel de slots
		Render2D.dibujarRectanguloRelleno(g, panelX, VISTA_Y, ANCHO_PANEL, VISTA_ALTO, new Color(16, 20, 26, 230));
		Render2D.dibujarRectanguloContorno(g, panelX, VISTA_Y, ANCHO_PANEL, VISTA_ALTO, new Color(55, 60, 75));

		final Graphics2D gClip = (Graphics2D) g.create();
		try {
			gClip.setClip(panelX + 2, VISTA_Y + 2, ANCHO_PANEL - 4, VISTA_ALTO - 4);

			for (int i = 0; i < TOTAL_SLOTS; i++) {
				final BotonPixel btn = this.botonesSlots[i];
				final int ySlot = btn.getArea().y - this.scrollY;

				if (ySlot + ALTO_PANEL_SLOT >= VISTA_Y && ySlot <= VISTA_Y + VISTA_ALTO) {
					final boolean esElSeleccionado = (i == this.slotSeleccionado);
					btn.setEnfocado(esElSeleccionado);

					final int yReal = btn.getArea().y;
					btn.getArea().y = ySlot;
					btn.pintar(gClip);
					btn.getArea().y = yReal;

					gClip.setFont(Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, 12f));
					final Color cInfo = this.slotsOcupados[i] ? new Color(200, 205, 220) : new Color(120, 125, 135);
					Render2D.dibujarStringConSombra(gClip, this.infoSlots[i], btn.getArea().x + 14, ySlot + ALTO_PANEL_SLOT - 5, cInfo, Color.BLACK);
				}
			}
		} finally {
			gClip.dispose();
		}

		// Scrollbar
		if (this.maxScrollY > 0) {
			final int trackX = panelX + ANCHO_PANEL - 5;
			final int trackY = VISTA_Y + 4;
			final int trackH = VISTA_ALTO - 8;
			Render2D.dibujarRectanguloRelleno(g, trackX, trackY, 3, trackH, new Color(30, 35, 45));

			final double ratio = (double) this.scrollY / this.maxScrollY;
			final int thumbH = Math.max(16, (int) (((double) VISTA_ALTO / (VISTA_ALTO + this.maxScrollY)) * trackH));
			final int thumbY = trackY + (int) (ratio * (trackH - thumbH));

			Render2D.dibujarRectanguloRelleno(g, trackX, thumbY, 3, thumbH, new Color(220, 180, 50));
		}

		this.botonGuardarConfirmar.pintar(g);
		this.botonVolver.pintar(g);
		this.pintarGuiaControles(g);
	}
}