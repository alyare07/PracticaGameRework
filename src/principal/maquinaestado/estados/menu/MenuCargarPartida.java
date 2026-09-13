package principal.maquinaestado.estados.menu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.simple.JSONObject;

import principal.maquinaestado.GestorEstados;
import principal.maquinaestado.estados.menu.herramientas.BotonPixel;
import principal.persistencia.GestorGuardado;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Menú interactivo nativo para seleccionar y cargar partidas guardadas (Slots 1, 2 y 3).
 * Erradica las ventanas modales de Swing eliminando los bloqueos y falsos clics.
 * 
 * @version 1.0 (Vanilla Java 8 - In-Engine Save Slots UI)
 */
public class MenuCargarPartida extends Menu {

	private static final int TOTAL_SLOTS = 3;
	private static final int ANCHO_PANEL_SLOT = 340;
	private static final int ALTO_PANEL_SLOT = 48;
	private static final int ESPACIADO_SLOT = 8;
	private static final int Y_INICIO_SLOTS = 95;

	private final BotonPixel[] botonesSlots = new BotonPixel[TOTAL_SLOTS];
	private final String[] infoSlots = new String[TOTAL_SLOTS];
	private final boolean[] slotsOcupados = new boolean[TOTAL_SLOTS];

	private BotonPixel botonVolver;
	private static final SimpleDateFormat FORMATO_FECHA = new SimpleDateFormat("dd/MM/yyyy HH:mm");

	public MenuCargarPartida(final GestorEstados ge) {
		super(ge, "CARGAR PARTIDA");
		this.subtituloMenu = "- SELECCIONA UN ARCHIVO DE GUARDADO -";
		this.colorFondo = new Color(10, 12, 16, 255);
		this.inicializarMenu();
	}

	@Override
	protected void inicializarMenu() {
		this.componentes.clear();
		this.botones.clear();

		final int panelX = Constantes.CENTROX - (ANCHO_PANEL_SLOT / 2);

		for (int i = 0; i < TOTAL_SLOTS; i++) {
			final int slotNum = i + 1;
			final int ySlot = Y_INICIO_SLOTS + (i * (ALTO_PANEL_SLOT + ESPACIADO_SLOT));

			final JSONObject saveJson = GestorGuardado.leerJsonGuardado(slotNum);
			this.slotsOcupados[i] = (saveJson != null);

			if (this.slotsOcupados[i]) {
				final long timestamp = (saveJson.get("timestamp") != null)
						? ((Number) saveJson.get("timestamp")).longValue()
						: 0L;
				final String fechaStr = (timestamp > 0) ? FORMATO_FECHA.format(new Date(timestamp)) : "Fecha desconocida";

				String infoExtra = "";
				if (saveJson.get("calendario") instanceof JSONObject) {
					final JSONObject jCal = (JSONObject) saveJson.get("calendario");
					final String dia = (jCal.get("diaActual") != null) ? jCal.get("diaActual").toString() : "1";
					final String hora = (jCal.get("hora24h") != null) ? jCal.get("hora24h").toString() : "12:00";
					infoExtra = "Día " + dia + " · " + hora;
				}

				this.infoSlots[i] = fechaStr + " (" + infoExtra + ")";
			} else {
				this.infoSlots[i] = "[Slot Vacío - Sin Partida Guardada]";
			}

			final BotonPixel btnSlot = new BotonPixel("Slot " + slotNum,
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

		final int yVolver = Constantes.ALTO_JUEGO - 42;
		this.botonVolver = new BotonPixel("Volver", new Rectangle(Constantes.CENTROX - 60, yVolver, 120, 18), () -> {
			this.alPresionarEscape();
		});

		this.componentes.add(this.botonVolver);
		this.botones.add(this.botonVolver);

		this.establecerIndiceEnfocado(0);
	}

	@Override
	protected void alPresionarEscape() {
		this.GE.establecerEstadoActual(GestorEstados.NUMERO_ESTADO_MENU);
	}

	@Override
	public void pintar(final Graphics2D g) {
		this.pintarFondo(g);
		this.pintarCabecera(g);

		final Font fontPrevia = g.getFont();

		for (int i = 0; i < TOTAL_SLOTS; i++) {
			final BotonPixel btn = this.botonesSlots[i];
			btn.pintar(g);

			// Renderizado de metadatos del slot debajo del título
			g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, 14f));
			final Color cInfo = this.slotsOcupados[i] ? new Color(220, 225, 240) : new Color(130, 135, 145);
			final int xInfo = btn.getArea().x + 14;
			final int yInfo = (btn.getArea().y + btn.getArea().height) - 8;

			Render2D.dibujarStringConSombra(g, this.infoSlots[i], xInfo, yInfo, cInfo, Color.BLACK);
		}

		this.botonVolver.pintar(g);
		this.pintarGuiaControles(g);

		g.setFont(fontPrevia);
	}
}