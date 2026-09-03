package principal.igu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import principal.clima.GestorTermicoJugador;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

/**
 * Componente visual del HUD para monitoreo térmico en tiempo real. Renderiza la
 * temperatura ambiente del bioma/clima y la temperatura corporal interna del
 * jugador con indicadores de tendencia (Zero-GC / O(1)).
 * 
 * @version 1.1 (Vanilla Java 8 - Static Crisp Font)
 */
public class TermometroIGU {

	private static final int ANCHO_WIDGET = 92;
	private static final int ALTO_WIDGET = 22;

	private static final Color COLOR_FONDO = new Color(16, 20, 26, 215);
	private static final Color COLOR_BORDE = new Color(55, 60, 75, 240);

	private static final Color COLOR_FRIO = new Color(130, 210, 255);
	private static final Color COLOR_TEMPLADO = new Color(225, 235, 245);
	private static final Color COLOR_CALOR = new Color(255, 125, 45);

	private static final Color COLOR_CONFORT_CORP = new Color(90, 240, 130);
	private static final Color COLOR_HIPOTERMIA = new Color(80, 190, 255);
	private static final Color COLOR_HIPERTERMIA = new Color(255, 75, 65);

	private final Rectangle areaWidget;

	// Caché de texto (Zero-GC)
	private int lastTempAmbInt = -999;
	private int lastTempCorpInt = -999;
	private String cachedAmbiente = "";
	private String cachedCorporal = "";
	private String cachedTendencia = "[-]";

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

		final int ambInt = (int) Math.round(tempAmb * 10.0);
		final int corpInt = (int) Math.round(tempCorp * 10.0);

		if ((ambInt != this.lastTempAmbInt) || (corpInt != this.lastTempCorpInt)) {
			this.lastTempAmbInt = ambInt;
			this.lastTempCorpInt = corpInt;

			this.cachedAmbiente = "Amb: " + String.format("%.1f", ambInt / 10.0) + " °C";
			this.cachedCorporal = "Corp: " + String.format("%.1f", corpInt / 10.0) + " °C";

			if (tendencia > 0.0001) {
				this.cachedTendencia = "[^]"; // Subiendo
			} else if (tendencia < -0.0001) {
				this.cachedTendencia = "[v]"; // Bajando
			} else {
				this.cachedTendencia = "[-]"; // Estable
			}
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
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 8f));

		// 2. Línea 1: Temperatura Ambiente
		final double tempAmb = Globales.GESTOR_CLIMA.getTemperaturaCelsius();
		final Color colorAmb = (tempAmb < 5.0) ? COLOR_FRIO : ((tempAmb > 28.0) ? COLOR_CALOR : COLOR_TEMPLADO);
		Render2D.dibujarStringConSombra(g, this.cachedAmbiente, x + 4, y + 9, colorAmb, Color.BLACK, 8f, true);

		// 3. Línea 2: Temperatura Corporal + Tendencia
		final GestorTermicoJugador termico = Globales.GESTOR_TERMICO_JUGADOR;
		final Color colorCorp = termico.isHipotermia() ? COLOR_HIPOTERMIA
				: (termico.isHipertermia() ? COLOR_HIPERTERMIA : COLOR_CONFORT_CORP);

		Render2D.dibujarStringConSombra(g, this.cachedCorporal, x + 4, (y + h) - 3, colorCorp, Color.BLACK, 8f, true);
		Render2D.dibujarStringConSombra(g, this.cachedTendencia, (x + w) - 16, (y + h) - 3, colorCorp, Color.BLACK, 8f,
				true);

		g.setFont(fontPrevia);
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