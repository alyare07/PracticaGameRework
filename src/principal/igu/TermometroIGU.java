package principal.igu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import principal.clima.GestorTermicoJugador;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

/**
 * Componente visual del HUD para monitoreo térmico en tiempo real. Renderiza
 * temperatura ambiente, corporal, tendencia y estado termodinámico unificado
 * con coexistencia de fuentes (Fuego + Aislamiento + Sofoco) (Zero-GC / O(1)).
 * 
 * @version 2.1 (Vanilla Java 8 - Concurrent Thermal Status Listing)
 */
public class TermometroIGU {

	private static final int ANCHO_WIDGET = 98;
	private static final int ALTO_WIDGET = 34; // +2px para que los descendentes (g, p, y) respiren

	private static final Color COLOR_FONDO = new Color(16, 20, 26, 225);
	private static final Color COLOR_BORDE = new Color(55, 60, 75, 240);

	private static final Color COLOR_FRIO = new Color(130, 210, 255);
	private static final Color COLOR_TEMPLADO = new Color(225, 235, 245);
	private static final Color COLOR_CALOR = new Color(255, 125, 45);

	private static final Color COLOR_CONFORT_CORP = new Color(90, 240, 130);
	private static final Color COLOR_HIPOTERMIA = new Color(80, 190, 255);
	private static final Color COLOR_HIPERTERMIA = new Color(255, 75, 65);

	private static final Color COLOR_SOFOCO = new Color(255, 85, 60);
	private static final Color COLOR_FUEGO = new Color(255, 185, 45);
	private static final Color COLOR_ALERTA_FRIO = new Color(100, 195, 255);

	private final Rectangle areaWidget;

	// Caché de estado y texto (Zero-GC)
	private int lastTempAmbInt = -999;
	private int lastTempCorpInt = -999;
	private int lastAislaFrio = -999;
	private int lastAislaCalor = -999;
	private int lastSofoco = -999;
	private boolean lastCercaFuego = false;

	private String cachedAmbiente = "";
	private String cachedCorporal = "";
	private String cachedTendencia = "[-]";
	private String cachedEstadoAislamiento = "";
	private String cachedTooltipDesc = "";
	private Color cachedColorEstado = COLOR_CONFORT_CORP;

	private boolean visible = true;

	public TermometroIGU() {
		final int posX = Constantes.ANCHO_JUEGO - ANCHO_WIDGET - 6;
		final int posY = 52;
		this.areaWidget = new Rectangle(posX, posY, ANCHO_WIDGET, ALTO_WIDGET);
	}

	public void actualizar() {
		if (!this.visible || (Globales.GESTOR_CLIMA == null) || (Globales.GESTOR_TERMICO_JUGADOR == null)) {
			return;
		}

		final double tempAmb = Globales.GESTOR_CLIMA.getTemperaturaCelsius();
		final double tempCorp = Globales.GESTOR_TERMICO_JUGADOR.getTemperaturaCorporal();
		final double tendencia = Globales.GESTOR_TERMICO_JUGADOR.getTendenciaTermica();
		final boolean cercaFuego = Globales.GESTOR_TERMICO_JUGADOR.isCercaDeFuenteCalor();

		final int aislaFrio = (Globales.JUGADOR != null) ? Globales.JUGADOR.getAislamientoFrioTotal() : 0;
		final int aislaCalor = (Globales.JUGADOR != null) ? Globales.JUGADOR.getAislamientoCalorTotal() : 0;
		final int sofoco = (Globales.JUGADOR != null) ? Globales.JUGADOR.getPenalizacionSofocoTotal() : 0;

		final int ambInt = (int) Math.round(tempAmb * 10.0);
		final int corpInt = (int) Math.round(tempCorp * 10.0);

		if ((ambInt != this.lastTempAmbInt) || (corpInt != this.lastTempCorpInt) || (aislaFrio != this.lastAislaFrio)
				|| (aislaCalor != this.lastAislaCalor) || (sofoco != this.lastSofoco)
				|| (cercaFuego != this.lastCercaFuego)) {

			this.lastTempAmbInt = ambInt;
			this.lastTempCorpInt = corpInt;
			this.lastAislaFrio = aislaFrio;
			this.lastAislaCalor = aislaCalor;
			this.lastSofoco = sofoco;
			this.lastCercaFuego = cercaFuego;

			final int ambDec = Math.abs(ambInt % 10);
			final int corpDec = Math.abs(corpInt % 10);

			this.cachedAmbiente = "Amb: " + (ambInt / 10) + "." + ambDec + " °C";
			this.cachedCorporal = "Corp: " + (corpInt / 10) + "." + corpDec + " °C";

			if (tendencia > 0.0001) {
				this.cachedTendencia = "[^]";
			} else if (tendencia < -0.0001) {
				this.cachedTendencia = "[v]";
			} else {
				this.cachedTendencia = "[-]";
			}

			// =================================================================
			// COMPOSICIÓN LISTADA Y CONCURRENTE DEL ESTADO TÉRMICO (LÍNEA 3)
			// =================================================================
			final StringBuilder sbWidget = new StringBuilder();
			final StringBuilder sbTooltip = new StringBuilder();

			sbTooltip.append("Equipo: Frío +").append(aislaFrio).append("°C | Calor +").append(aislaCalor).append("°C");
			if (sofoco > 0) {
				sbTooltip.append(" | Sofoco +").append(sofoco).append("°C");
			}
			sbTooltip.append(". Condición: ");

			// 1. Caso AMBIENTE FRÍO (< 10.0 °C)
			if (tempAmb < 10.0) {
				if (cercaFuego) {
					sbWidget.append("Fuego (+) | ");
					sbTooltip.append("Junto al Fuego, ");
					this.cachedColorEstado = COLOR_FUEGO;
				} else {
					this.cachedColorEstado = (aislaFrio > 0) ? COLOR_FRIO : COLOR_ALERTA_FRIO;
				}

				if (aislaFrio > 0) {
					sbWidget.append("Frío +").append(aislaFrio).append("°C");
					sbTooltip.append("Protegido contra el frío.");
				} else {
					sbWidget.append("¡Sin Abrigo!");
					sbTooltip.append("¡Expuesto al frío sin aislamiento!");
				}
			}
			// 2. Caso AMBIENTE CÁLIDO (> 27.0 °C)
			else if (tempAmb > 27.0) {
				if (sofoco > 0) {
					sbWidget.append("¡Sofoco +").append(sofoco).append("°!");
					if (aislaCalor > 0) {
						sbWidget.append(" • C+").append(aislaCalor);
					}
					sbTooltip.append("¡Sofocado por ropa pesada en calor!");
					this.cachedColorEstado = COLOR_SOFOCO;
				} else if (aislaCalor > 0) {
					sbWidget.append("Aisla Calor: +").append(aislaCalor).append("°C");
					sbTooltip.append("Protegido contra la radiación solar.");
					this.cachedColorEstado = COLOR_CALOR;
				} else {
					sbWidget.append("Expuesto al Calor");
					sbTooltip.append("Expuesto al calor directo.");
					this.cachedColorEstado = COLOR_CALOR;
				}
			} else if (cercaFuego) {
				sbWidget.append("Fuego (+) | Confort");
				sbTooltip.append("Calentándose junto al fuego.");
				this.cachedColorEstado = COLOR_FUEGO;
			} else if ((aislaFrio > 0) || (aislaCalor > 0)) {
				sbWidget.append("Confort (F+").append(aislaFrio).append("/C+").append(aislaCalor).append(")");
				sbTooltip.append("Temperatura agradable y confortable.");
				this.cachedColorEstado = COLOR_CONFORT_CORP;
			} else {
				sbWidget.append("Confort Térmico");
				sbTooltip.append("Temperatura agradable y confortable.");
				this.cachedColorEstado = COLOR_CONFORT_CORP;
			}

			this.cachedEstadoAislamiento = sbWidget.toString();
			this.cachedTooltipDesc = sbTooltip.toString();
		}
	}

	public void pintar(final Graphics2D g) {
		if (!this.visible || (Globales.GESTOR_CLIMA == null) || (Globales.GESTOR_TERMICO_JUGADOR == null)) {
			return;
		}

		final int x = this.areaWidget.x;
		final int y = this.areaWidget.y;
		final int w = this.areaWidget.width;
		final int h = this.areaWidget.height;

		// 1. Fondo semitransparente táctico y borde
		Render2D.dibujarRectanguloRelleno(g, x, y, w, h, COLOR_FONDO);
		Render2D.dibujarRectanguloContorno(g, x, y, w, h, COLOR_BORDE);

		final Font fontPrevia = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 7f));

		// 2. Línea 1: Temperatura Ambiente (y + 9)
		final double tempAmb = Globales.GESTOR_CLIMA.getTemperaturaCelsius();
		final Color colorAmb = (tempAmb < 10.0) ? COLOR_FRIO : ((tempAmb > 27.0) ? COLOR_CALOR : COLOR_TEMPLADO);
		Render2D.dibujarStringConSombra(g, this.cachedAmbiente, x + 4, y + 9, colorAmb, Color.BLACK, 7f, true);

		// 3. Línea 2: Temperatura Corporal + Tendencia (y + 18)
		final GestorTermicoJugador termico = Globales.GESTOR_TERMICO_JUGADOR;
		final Color colorCorp = termico.isHipotermia() ? COLOR_HIPOTERMIA
				: (termico.isHipertermia() ? COLOR_HIPERTERMIA : COLOR_CONFORT_CORP);

		Render2D.dibujarStringConSombra(g, this.cachedCorporal, x + 4, y + 18, colorCorp, Color.BLACK, 7f, true);
		Render2D.dibujarStringConSombra(g, this.cachedTendencia, (x + w) - 14, y + 18, colorCorp, Color.BLACK, 7f,
				true);

		// 4. Línea 3: Estado de Aislamiento / Fuentes concurrentes (y + 27)
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 6f));
		Render2D.dibujarStringConSombra(g, this.cachedEstadoAislamiento, x + 4, y + 27, this.cachedColorEstado,
				Color.BLACK, 6f, true);

		g.setFont(fontPrevia);
	}

	public void pintarTooltips(final Graphics2D g) {
		if (!this.visible || (Globales.RATON == null) || (Globales.JUGADOR == null)) {
			return;
		}

		final Point pMouse = Globales.RATON.getPuntoPosicionEscalado();
		if (this.areaWidget.contains(pMouse)) {
			Globales.FUNCIONES.GENERADOR_TOOLTIP.dibujarTooltipConCabecera(g, "Fisiología Térmica: ",
					this.cachedTooltipDesc, this.cachedColorEstado, COLOR_TEMPLADO, COLOR_FONDO);
		}
	}

	public boolean isVisible() {
		return this.visible;
	}

	public void setVisible(final boolean visible) {
		this.visible = visible;
	}

	public Rectangle getArea() {
		return this.areaWidget;
	}
}