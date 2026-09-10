package principal.igu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import principal.clima.GestorTermicoJugador;
import principal.clima.TipoClima;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

/**
 * Componente visual del HUD para monitoreo térmico en tiempo real. Integra
 * micro-barra de mercurio, detección barométrica de tormentas, alerta temprana
 * pre-temporal en pantalla y cálculo Zero-GC (O(1)).
 * 
 * @version 3.3 (Vanilla Java 8 - Active Early Storm Warning Integration)
 */
public class TermometroIGU {

	private static final int ANCHO_WIDGET = 104;
	private static final int ALTO_WIDGET = 34;

	private static final Color COLOR_FONDO = new Color(16, 20, 26, 225);
	private static final Color COLOR_BORDE_BASE = new Color(55, 60, 75, 240);

	private static final Color COLOR_FRIO = new Color(130, 210, 255);
	private static final Color COLOR_TEMPLADO = new Color(225, 235, 245);
	private static final Color COLOR_CALOR = new Color(255, 125, 45);

	private static final Color COLOR_CONFORT_CORP = new Color(90, 240, 130);
	private static final Color COLOR_HIPOTERMIA = new Color(80, 190, 255);
	private static final Color COLOR_HIPERTERMIA = new Color(255, 75, 65);

	private static final Color COLOR_SOFOCO = new Color(255, 85, 60);
	private static final Color COLOR_FUEGO = new Color(255, 185, 45);
	private static final Color COLOR_ALERTA_FRIO = new Color(100, 195, 255);
	private static final Color COLOR_ALERTA_ACIDA = new Color(135, 240, 90);
	private static final Color COLOR_ALERTA_ECLIPSE = new Color(220, 45, 65);

	// Paleta de la Micro-Barra
	private static final Color COLOR_BARRA_FONDO = new Color(10, 12, 16);
	private static final Color COLOR_ZONA_FRIO = new Color(60, 150, 240);
	private static final Color COLOR_ZONA_CONFORT = new Color(50, 210, 110);
	private static final Color COLOR_ZONA_CALOR = new Color(240, 60, 50);
	private static final Color COLOR_MARCADOR_CORP = Color.WHITE;

	private final Rectangle areaWidget;

	private final StringBuilder sbWidget = new StringBuilder(64);
	private final StringBuilder sbTooltip = new StringBuilder(160);

	// Caché de estado (Zero-GC)
	private int lastTempAmbInt = -999;
	private int lastTempCorpInt = -999;
	private int lastAislaFrio = -999;
	private int lastAislaCalor = -999;
	private int lastSofoco = -999;
	private int lastPresionInt = -999;
	private boolean lastCercaFuego = false;
	private boolean lastEnAlertaTormenta = false;
	private TipoClima lastClima = null;
	private TipoClima lastPronostico = null;

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
		final double presion = Globales.GESTOR_CLIMA.getPresionHPa();
		final boolean cercaFuego = Globales.GESTOR_TERMICO_JUGADOR.isCercaDeFuenteCalor();
		final TipoClima climaActual = Globales.GESTOR_CLIMA.getClimaActual();
		final TipoClima climaPronosticado = Globales.GESTOR_CLIMA.getClimaPronosticado();
		final double tiempoRestanteClima = Globales.GESTOR_CLIMA.getTiempoRestanteEstadoClima();

		// Alerta activa si faltan 60s o menos para un clima peligroso
		final boolean enAlertaTormenta = (tiempoRestanteClima <= 60.0) && this.esClimaPeligroso(climaPronosticado);

		final int aislaFrio = (Globales.JUGADOR != null) ? Globales.JUGADOR.getAislamientoFrioTotal() : 0;
		final int aislaCalor = (Globales.JUGADOR != null) ? Globales.JUGADOR.getAislamientoCalorTotal() : 0;
		final int sofoco = (Globales.JUGADOR != null) ? Globales.JUGADOR.getPenalizacionSofocoTotal() : 0;

		final int ambInt = (int) Math.round(tempAmb * 10.0);
		final int corpInt = (int) Math.round(tempCorp * 10.0);
		final int presInt = (int) Math.round(presion);

		if ((ambInt != this.lastTempAmbInt) || (corpInt != this.lastTempCorpInt) || (aislaFrio != this.lastAislaFrio)
				|| (aislaCalor != this.lastAislaCalor) || (sofoco != this.lastSofoco)
				|| (presInt != this.lastPresionInt) || (cercaFuego != this.lastCercaFuego)
				|| (climaActual != this.lastClima) || (climaPronosticado != this.lastPronostico)
				|| (enAlertaTormenta != this.lastEnAlertaTormenta)) {

			this.lastTempAmbInt = ambInt;
			this.lastTempCorpInt = corpInt;
			this.lastAislaFrio = aislaFrio;
			this.lastAislaCalor = aislaCalor;
			this.lastSofoco = sofoco;
			this.lastPresionInt = presInt;
			this.lastCercaFuego = cercaFuego;
			this.lastClima = climaActual;
			this.lastPronostico = climaPronosticado;
			this.lastEnAlertaTormenta = enAlertaTormenta;

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

			// Composición Zero-GC con StringBuilder mutables
			this.sbWidget.setLength(0);
			this.sbTooltip.setLength(0);

			if (climaActual != null) {
				this.sbTooltip.append("Clima: ").append(climaActual.getNombre());
				if (presion < 1000.0) {
					this.sbTooltip.append(" [¡Baja Presión: ").append(presInt).append(" hPa!]");
				}
				this.sbTooltip.append(". ");
			}
			this.sbTooltip.append("Equipo: Frío +").append(aislaFrio).append("°C | Calor +").append(aislaCalor)
					.append("°C");
			if (sofoco > 0) {
				this.sbTooltip.append(" | Sofoco +").append(sofoco).append("°C");
			}
			this.sbTooltip.append(". Condición: ");

			// =================================================================
			// 1. PRIORIDAD MÁXIMA: ALERTA TEMPRANA PRE-TORMENTA (ÚLTIMO MINUTO)
			// =================================================================
			if (enAlertaTormenta && (climaPronosticado != null)) {
				switch (climaPronosticado) {
				case LLUVIA_TORMENTA:
					this.sbWidget.append("¡Alerta: Tormenta!");
					this.cachedColorEstado = COLOR_FUEGO;
					break;
				case VENTISCA:
					this.sbWidget.append("¡Alerta: Ventisca!");
					this.cachedColorEstado = COLOR_ALERTA_FRIO;
					break;
				case TORMENTA_ARENA:
					this.sbWidget.append("¡Alerta: T. Arena!");
					this.cachedColorEstado = COLOR_CALOR;
					break;
				case LLUVIA_ACIDA:
					this.sbWidget.append("¡Alerta: L. Ácida!");
					this.cachedColorEstado = COLOR_ALERTA_ACIDA;
					break;
				case ECLIPSE_SOLAR:
					this.sbWidget.append("¡Alerta: Eclipse!");
					this.cachedColorEstado = COLOR_ALERTA_ECLIPSE;
					break;
				default:
					this.sbWidget.append("¡Alerta Temporal!");
					this.cachedColorEstado = COLOR_FUEGO;
					break;
				}
				this.sbTooltip.append("¡ALERTA TEMPRANA! Se avecina ").append(climaPronosticado.getNombre())
						.append(" en menos de 1 min. ¡Busque refugio!");
			}
			// =================================================================
			// 2. ESTADO TERMODINÁMICO HABITUAL
			// =================================================================
			else if (tempAmb < 10.0) {
				if (cercaFuego) {
					this.sbWidget.append("Fuego (+) | ");
					this.sbTooltip.append("Junto al Fuego, ");
					this.cachedColorEstado = COLOR_FUEGO;
				} else {
					this.cachedColorEstado = (aislaFrio > 0) ? COLOR_FRIO : COLOR_ALERTA_FRIO;
				}

				if (aislaFrio > 0) {
					this.sbWidget.append("Frío +").append(aislaFrio).append("°C");
					this.sbTooltip.append("Protegido contra el frío.");
				} else {
					this.sbWidget.append("¡Sin Abrigo!");
					this.sbTooltip.append("¡Expuesto al frío sin aislamiento!");
				}
			} else if (tempAmb > 27.0) {
				if (sofoco > 0) {
					this.sbWidget.append("¡Sofoco +").append(sofoco).append("°!");
					if (aislaCalor > 0) {
						this.sbWidget.append(" | C+").append(aislaCalor);
					}
					this.sbTooltip.append("¡Sofocado por ropa pesada en calor!");
					this.cachedColorEstado = COLOR_SOFOCO;
				} else if (aislaCalor > 0) {
					this.sbWidget.append("Aisla Calor: +").append(aislaCalor).append("°C");
					this.sbTooltip.append("Protegido contra el calor.");
					this.cachedColorEstado = COLOR_CALOR;
				} else {
					this.sbWidget.append("Expuesto al Calor");
					this.sbTooltip.append("Expuesto al calor directo.");
					this.cachedColorEstado = COLOR_CALOR;
				}
			} else if (cercaFuego) {
				this.sbWidget.append("Fuego (+) | Confort");
				this.sbTooltip.append("Calentándose junto al fuego.");
				this.cachedColorEstado = COLOR_FUEGO;
			} else if ((aislaFrio > 0) || (aislaCalor > 0)) {
				this.sbWidget.append("Confort (F+").append(aislaFrio).append("/C+").append(aislaCalor).append(")");
				this.sbTooltip.append("Temperatura agradable y confortable.");
				this.cachedColorEstado = COLOR_CONFORT_CORP;
			} else {
				this.sbWidget.append("Confort Térmico");
				this.sbTooltip.append("Temperatura agradable y confortable.");
				this.cachedColorEstado = COLOR_CONFORT_CORP;
			}

			this.cachedEstadoAislamiento = this.sbWidget.toString();
			this.cachedTooltipDesc = this.sbTooltip.toString();
		}
	}

	private boolean esClimaPeligroso(final TipoClima clima) {
		if (clima == null) {
			return false;
		}
		switch (clima) {
		case LLUVIA_TORMENTA:
		case VENTISCA:
		case TORMENTA_ARENA:
		case LLUVIA_ACIDA:
		case ECLIPSE_SOLAR:
			return true;
		default:
			return false;
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

		Render2D.dibujarRectanguloRelleno(g, x, y, w, h, COLOR_FONDO);

		final GestorTermicoJugador termico = Globales.GESTOR_TERMICO_JUGADOR;
		Color colorBordeEfectivo = COLOR_BORDE_BASE;

		// Pulso de advertencia en el borde ante peligro térmico O alerta de tormenta
		if (this.lastEnAlertaTormenta) {
			final float alphaPulso = (float) (0.55 + (Math.sin(Globales.animacion * 0.25) * 0.40));
			colorBordeEfectivo = new Color(1.0f, 0.8f, 0.2f, Math.max(0.2f, Math.min(1.0f, alphaPulso)));
		} else if (termico.isHipotermiaSevera()) {
			final float alphaPulso = (float) (0.55 + (Math.sin(Globales.animacion * 0.20) * 0.40));
			colorBordeEfectivo = new Color(0.3f, 0.75f, 1.0f, Math.max(0.2f, Math.min(1.0f, alphaPulso)));
		} else if (termico.isHipertermia()) {
			final float alphaPulso = (float) (0.55 + (Math.sin(Globales.animacion * 0.20) * 0.40));
			colorBordeEfectivo = new Color(1.0f, 0.3f, 0.25f, Math.max(0.2f, Math.min(1.0f, alphaPulso)));
		}
		Render2D.dibujarRectanguloContorno(g, x, y, w, h, colorBordeEfectivo);

		this.pintarMicroBarraMercurio(g, x + 3, y + 4, 3, h - 8, termico.getTemperaturaCorporal());

		final Font fontPrevia = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 7f));

		final int xTexto = x + 9;

		final double tempAmb = Globales.GESTOR_CLIMA.getTemperaturaCelsius();
		final Color colorAmb = (tempAmb < 10.0) ? COLOR_FRIO : ((tempAmb > 27.0) ? COLOR_CALOR : COLOR_TEMPLADO);
		Render2D.dibujarStringConSombra(g, this.cachedAmbiente, xTexto, y + 9, colorAmb, Color.BLACK, 7f, true);

		final Color colorCorp = termico.isHipotermia() ? COLOR_HIPOTERMIA
				: (termico.isHipertermia() ? COLOR_HIPERTERMIA : COLOR_CONFORT_CORP);

		Render2D.dibujarStringConSombra(g, this.cachedCorporal, xTexto, y + 18, colorCorp, Color.BLACK, 7f, true);
		Render2D.dibujarStringConSombra(g, this.cachedTendencia, (x + w) - 14, y + 18, colorCorp, Color.BLACK, 7f,
				true);

		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 6f));
		Render2D.dibujarStringConSombra(g, this.cachedEstadoAislamiento, xTexto, y + 27, this.cachedColorEstado,
				Color.BLACK, 6f, true);

		g.setFont(fontPrevia);
	}

	private void pintarMicroBarraMercurio(final Graphics2D g, final int bx, final int by, final int bw, final int bh,
			final double tempCorp) {
		Render2D.dibujarRectanguloRelleno(g, bx, by, bw, bh, COLOR_BARRA_FONDO);

		final double tempMin = 24.0;
		final double tempMax = 42.0;
		final double rangoTotal = tempMax - tempMin;

		final double umbralHiper = GestorTermicoJugador.UMBRAL_HIPERTERMIA_NIVEL_1;
		final double umbralHipo = GestorTermicoJugador.UMBRAL_HIPOTERMIA_NIVEL_1;

		final int yHiper = by + (int) Math.round(((tempMax - umbralHiper) / rangoTotal) * bh);
		final int yHipo = by + (int) Math.round(((tempMax - umbralHipo) / rangoTotal) * bh);

		final int altoCalor = Math.max(1, yHiper - by);
		Render2D.dibujarRectanguloRelleno(g, bx, by, bw, altoCalor, COLOR_ZONA_CALOR);

		final int altoConfort = Math.max(1, yHipo - yHiper);
		Render2D.dibujarRectanguloRelleno(g, bx, yHiper, bw, altoConfort, COLOR_ZONA_CONFORT);

		final int altoFrio = Math.max(1, (by + bh) - yHipo);
		Render2D.dibujarRectanguloRelleno(g, bx, yHipo, bw, altoFrio, COLOR_ZONA_FRIO);

		final double tempClamp = Math.max(tempMin, Math.min(tempMax, tempCorp));
		final double ratio = (tempMax - tempClamp) / rangoTotal;
		final int markerY = by + (int) Math.round(ratio * (bh - 1));

		Render2D.dibujarRectanguloRelleno(g, bx - 1, markerY, bw + 2, 1, COLOR_MARCADOR_CORP);
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