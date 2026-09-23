package principal.igu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import principal.entes.criaturas.jugador.GestorMetabolismoJugador;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

/**
 * Componente visual del HUD para monitoreo fisiológico en tiempo real (Hambre,
 * Sed y Sueño/Energía) con micro-barras y tipografía 'm3x6' (Zero-GC / O(1)).
 * 
 * @version 2.0 (Vanilla Java 8 - Tri-Metabolic Sleep HUD)
 */
public class MetabolismoIGU {

	private static final int ANCHO_WIDGET = 120; // Alineado a 120 px con RelojCiclo y TermometroIGU
	private static final int ALTO_WIDGET = 37; // Expandido a 37 px para albergar las 3 micro-filas limpiamente

	private static final Color COLOR_FONDO = new Color(16, 20, 26, 225);
	private static final Color COLOR_BORDE_BASE = new Color(55, 60, 75, 240);
	private static final Color COLOR_BARRA_FONDO = new Color(10, 12, 16);

	private static final Color COLOR_HAMBRE = new Color(255, 175, 50);
	private static final Color COLOR_HAMBRE_ALERTA = new Color(255, 75, 60);

	private static final Color COLOR_SED = new Color(60, 210, 255);
	private static final Color COLOR_SED_ALERTA = new Color(255, 75, 60);

	private static final Color COLOR_SUENIO = new Color(185, 110, 235); // Violeta / Índigo
	private static final Color COLOR_SUENIO_ALERTA = new Color(255, 75, 60);

	private static final Color COLOR_TEXTO_NORMAL = new Color(225, 235, 245);

	private final Rectangle areaWidget;

	// Caché de telemetría Zero-GC
	private int lastHambreInt = -1;
	private int lastSedInt = -1;
	private int lastSuenioInt = -1;

	private String cachedHambrePct = "100%";
	private String cachedSedPct = "100%";
	private String cachedSuenioPct = "100%";
	private String cachedTooltipDesc = "";

	private final StringBuilder sbTooltip = new StringBuilder(192);
	private boolean visible = true;

	public MetabolismoIGU() {
		final int MARGEN_DERECHO = 6;
		final int posX = Constantes.ANCHO_JUEGO - ANCHO_WIDGET - MARGEN_DERECHO; // 514
		final int posY = 58 + 34 + 4; // y = 96 (debajo del termómetro)
		this.areaWidget = new Rectangle(posX, posY, ANCHO_WIDGET, ALTO_WIDGET);
	}

	public void actualizar() {
		if (!this.visible || (Globales.GESTOR_METABOLISMO == null)) {
			return;
		}

		final GestorMetabolismoJugador meta = Globales.GESTOR_METABOLISMO;
		final int hInt = meta.getHambrePorcentajeInt();
		final int sInt = meta.getSedPorcentajeInt();
		final int zInt = meta.getSuenioPorcentajeInt();

		if ((hInt != this.lastHambreInt) || (sInt != this.lastSedInt) || (zInt != this.lastSuenioInt)) {
			this.lastHambreInt = hInt;
			this.lastSedInt = sInt;
			this.lastSuenioInt = zInt;

			this.cachedHambrePct = hInt + "%";
			this.cachedSedPct = sInt + "%";
			this.cachedSuenioPct = zInt + "%";

			this.sbTooltip.setLength(0);

			// 1. Nutrición
			this.sbTooltip.append("Alimentación: ").append(hInt).append("%");
			if (meta.isInanicion()) {
				this.sbTooltip.append(" [¡INANICION! Daño continuo]");
			} else if (meta.isAlertaHambre()) {
				this.sbTooltip.append(" (Hambriento)");
			}

			// 2. Hidratación
			this.sbTooltip.append(" | Hidratación: ").append(sInt).append("%");
			if (meta.isDeshidratado()) {
				this.sbTooltip.append(" [¡DESHIDRATADO! Sin estamina]");
			} else if (meta.isAlertaSed()) {
				this.sbTooltip.append(" (Sediento)");
			}

			// 3. Energía y Sueño
			this.sbTooltip.append(" | Energía: ").append(zInt).append("%");
			if (meta.isAgotadoExtremo()) {
				this.sbTooltip.append(" [¡EXHAUSTO! Necesitas dormir]");
			} else if (meta.isAlertaSuenio()) {
				this.sbTooltip.append(" (Somnoliento)");
			}

			this.cachedTooltipDesc = this.sbTooltip.toString();
		}
	}

	public void pintar(final Graphics2D g) {
		if (!this.visible || (Globales.GESTOR_METABOLISMO == null)) {
			return;
		}

		final int x = this.areaWidget.x;
		final int y = this.areaWidget.y;
		final int w = this.areaWidget.width;
		final int h = this.areaWidget.height;

		Render2D.dibujarRectanguloRelleno(g, x, y, w, h, COLOR_FONDO);

		final GestorMetabolismoJugador meta = Globales.GESTOR_METABOLISMO;
		Color colorBorde = COLOR_BORDE_BASE;

		// Titila en rojo si hay peligro de inanición, deshidratación o agotamiento
		// crítico
		if (meta.isInanicion() || meta.isDeshidratado() || meta.isAgotadoExtremo()) {
			final float pulse = (float) (0.55 + (Math.sin(Globales.animacion * 0.25) * 0.40));
			colorBorde = new Color(1.0f, 0.25f, 0.25f, Math.max(0.2f, Math.min(1.0f, pulse)));
		}
		Render2D.dibujarRectanguloContorno(g, x, y, w, h, colorBorde);

		final Font fontPrevia = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuenteSmall(Font.PLAIN, 16f));

		final int xLabel = x + 5;
		final int xBarra = x + 18;
		final int anchoBarra = 68;
		final int xPct = (x + w) - 28;

		// === 1. FILA DE HAMBRE (C) ===
		final int yFila1 = y + 9;
		final Color cTitHambre = meta.isAlertaHambre() ? COLOR_HAMBRE_ALERTA : COLOR_HAMBRE;
		Render2D.dibujarStringConSombra(g, "C", xLabel, yFila1 + 1, cTitHambre, Color.BLACK);

		Render2D.dibujarRectanguloRelleno(g, xBarra, yFila1 - 5, anchoBarra, 4, COLOR_BARRA_FONDO);
		final int anchoProgresoH = (int) Math.round((meta.getHambre() / 100.0) * anchoBarra);
		if (anchoProgresoH > 0) {
			Render2D.dibujarRectanguloRelleno(g, xBarra, yFila1 - 5, anchoProgresoH, 4, cTitHambre);
		}
		Render2D.dibujarStringConSombra(g, this.cachedHambrePct, xPct, yFila1 + 1, COLOR_TEXTO_NORMAL, Color.BLACK);

		// === 2. FILA DE SED (A) ===
		final int yFila2 = y + 20;
		final Color cTitSed = meta.isAlertaSed() ? COLOR_SED_ALERTA : COLOR_SED;
		Render2D.dibujarStringConSombra(g, "A", xLabel, yFila2 + 1, cTitSed, Color.BLACK);

		Render2D.dibujarRectanguloRelleno(g, xBarra, yFila2 - 5, anchoBarra, 4, COLOR_BARRA_FONDO);
		final int anchoProgresoS = (int) Math.round((meta.getSed() / 100.0) * anchoBarra);
		if (anchoProgresoS > 0) {
			Render2D.dibujarRectanguloRelleno(g, xBarra, yFila2 - 5, anchoProgresoS, 4, cTitSed);
		}
		Render2D.dibujarStringConSombra(g, this.cachedSedPct, xPct, yFila2 + 1, COLOR_TEXTO_NORMAL, Color.BLACK);

		// === 3. FILA DE SUEÑO / ENERGÍA (Z) ===
		final int yFila3 = y + 31;
		final Color cTitSuenio = meta.isAlertaSuenio() ? COLOR_SUENIO_ALERTA : COLOR_SUENIO;
		Render2D.dibujarStringConSombra(g, "E", xLabel, yFila3 + 1, cTitSuenio, Color.BLACK);

		Render2D.dibujarRectanguloRelleno(g, xBarra, yFila3 - 5, anchoBarra, 4, COLOR_BARRA_FONDO);
		final int anchoProgresoZ = (int) Math.round((meta.getSuenio() / 100.0) * anchoBarra);
		if (anchoProgresoZ > 0) {
			Render2D.dibujarRectanguloRelleno(g, xBarra, yFila3 - 5, anchoProgresoZ, 4, cTitSuenio);
		}
		Render2D.dibujarStringConSombra(g, this.cachedSuenioPct, xPct, yFila3 + 1, COLOR_TEXTO_NORMAL, Color.BLACK);

		g.setFont(fontPrevia);
	}

	public void pintarTooltips(final Graphics2D g) {
		if (!this.visible || (Globales.RATON == null) || (Globales.JUGADOR == null)) {
			return;
		}

		final Point pMouse = Globales.RATON.getPuntoPosicionEscalado();
		if (this.areaWidget.contains(pMouse)) {
			Globales.FUNCIONES.GENERADOR_TOOLTIP.dibujarTooltipConCabecera(g, "Fisiologia y Descanso: ",
					this.cachedTooltipDesc, COLOR_HAMBRE, COLOR_TEXTO_NORMAL, COLOR_FONDO);
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