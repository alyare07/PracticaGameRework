package principal.igu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import principal.configuracion.Dificultad;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.funciones.GeneradorTooltip;

/**
 * Widget HUD superior de estabilidad geológica para interiores de cueva
 * (Zero-GC / O(1)).
 * 
 * @version 2.0 (Vanilla Java 8 - Dedicated MotorIGU Layering)
 */
public class BarraEstabilidadCueva extends Barra {

	private static final int ANCHO_BARRA = 96;
	private static final int ALTO_BARRA = 7;
	private static final int POS_X = Constantes.CENTROX - (ANCHO_BARRA / 2);
	private static final int POS_Y = 8;

	private static final Color COLOR_BORDE = new Color(55, 60, 75);
	private static final Color COLOR_FONDO = new Color(15, 18, 25, 235);
	private static final Color COLOR_VERDE = new Color(45, 210, 110);
	private static final Color COLOR_AMBAR = new Color(245, 180, 40);
	private static final Color COLOR_ROJO = new Color(230, 45, 45);

	private final GeneradorTooltip motorTooltip = new GeneradorTooltip();

	// Caché Zero-GC para el tooltip
	private int lastHorasCuantizadas = -1;
	private Dificultad lastDificultad = null;
	private String cachedTooltipTexto = "";

	private double duracionTotal = 72.0;
	private double horasRestantes = 72.0;

	public BarraEstabilidadCueva() {
		super(new Rectangle(POS_X, POS_Y, ANCHO_BARRA, ALTO_BARRA), COLOR_BORDE, COLOR_FONDO, COLOR_VERDE, Color.WHITE);
	}

	@Override
	public void actualizar() {
		if (Globales.GESTOR_DERRUMBES == null) {
			return;
		}

		this.duracionTotal = Globales.GESTOR_DERRUMBES.getDuracionTotalCueva();
		this.horasRestantes = Globales.GESTOR_DERRUMBES.getHorasRestantes();

		final double ratio = Math.max(0.0, Math.min(1.0, this.horasRestantes / this.duracionTotal));
		this.anchoActual = (int) Math.round(ratio * (this.AREA.width - 2));
		this.anchoLag = this.anchoActual;
	}

	@Override
	public void pintar(final Graphics2D g) {
		if ((Globales.GESTOR_DERRUMBES != null) && Globales.GESTOR_DERRUMBES.isSecuenciaEscapeActiva()) {
			this.pintarBannerEscape(g);
			return;
		}

		// 1. Chasis y fondo base
		Render2D.dibujarRectanguloRelleno(g, this.AREA.x, this.AREA.y, this.AREA.width, this.AREA.height,
				this.COLOR_FONDO);

		// 2. Barra activa con color adaptativo según fase geológica
		if (this.anchoActual > 0) {
			final double pct = (this.horasRestantes / this.duracionTotal);
			final Color cRelleno = (pct > 0.50) ? COLOR_VERDE : ((pct > 0.20) ? COLOR_AMBAR : COLOR_ROJO);
			Render2D.dibujarRectanguloRelleno(g, this.AREA.x + 1, this.AREA.y + 1, this.anchoActual,
					this.AREA.height - 2, cRelleno);
		}

		// 3. Borde delimitador
		Render2D.dibujarRectanguloContorno(g, this.AREA, this.COLOR_BORDE);

		// 4. Porcentaje impreso en Fácil y Normal
		this.pintarInfo(g);
	}

	private void pintarBannerEscape(final Graphics2D g) {
		final Font previa = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 13f));

		final int segs = (int) Math.ceil(Globales.GESTOR_DERRUMBES.getTiempoRestanteEscape());
		final String alerta = "¡COLAPSO EN: " + segs + "s! ¡ESCAPA!";

		final int ancho = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, alerta);
		final int x = Constantes.CENTROX - (ancho / 2);
		final int y = 20;

		Render2D.dibujarRectanguloRelleno(g, x - 8, y - 12, ancho + 16, 16, new Color(20, 0, 0, 220));
		Render2D.dibujarRectanguloContorno(g, x - 8, y - 12, ancho + 16, 16, new Color(220, 40, 40));
		Render2D.dibujarStringConSombra(g, alerta, x, y, Color.YELLOW, Color.BLACK);

		g.setFont(previa);
	}

	@Override
	protected void pintarInfo(final Graphics2D g) {
		if ((Globales.dificultad != null) && Globales.dificultad.esHardcore()) {
			return;
		}

		final Font fontPrevia = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 9f));

		final int pct = (int) Math.round((this.horasRestantes / this.duracionTotal) * 100.0);
		final String txt = pct + "%";

		final int anchoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, txt);
		final int x = this.AREA.x + ((this.AREA.width - anchoTexto) / 2);
		final int y = (this.AREA.y + this.AREA.height) - 1;

		Render2D.dibujarStringConSombra(g, txt, x, y, Color.WHITE, Color.BLACK);
		g.setFont(fontPrevia);
	}

	public void pintarTooltip(final Graphics2D g) {
		if ((Globales.RATON == null)
				|| ((Globales.GESTOR_DERRUMBES != null) && Globales.GESTOR_DERRUMBES.isSecuenciaEscapeActiva())) {
			return;
		}

		final int mx = Globales.RATON.getPosicionXEscalada();
		final int my = Globales.RATON.getPosicionYEscalada();

		if (!this.AREA.contains(mx, my)) {
			return;
		}

		final int cuantizado = (Globales.dificultad == Dificultad.FACIL) ? (int) Math.round(this.horasRestantes * 10.0)
				: (int) Math.round(this.horasRestantes);

		if ((cuantizado != this.lastHorasCuantizadas) || (Globales.dificultad != this.lastDificultad)) {
			this.lastHorasCuantizadas = cuantizado;
			this.lastDificultad = Globales.dificultad;
			this.reconstruirTextoTooltip();
		}

		this.motorTooltip.dibujarTooltipConSaltoDeLinea(g, this.cachedTooltipTexto, Color.WHITE, this.COLOR_FONDO);
	}

	private void reconstruirTextoTooltip() {
		final double pct = (this.horasRestantes / this.duracionTotal) * 100.0;
		final String fase = (pct > 50.0) ? "Estable" : ((pct > 20.0) ? "Inestable" : "¡Peligro Crítico!");

		final Dificultad dif = (Globales.dificultad != null) ? Globales.dificultad : Dificultad.NORMAL;

		if (dif == Dificultad.FACIL) {
			final String hStr = String.valueOf((long) (this.horasRestantes * 10.0) / 10.0);
			final String pctStr = String.valueOf((long) (pct * 10.0) / 10.0);
			this.cachedTooltipTexto = "Estabilidad Geológica: " + fase + "\nIntegridad estructural: " + pctStr
					+ "%\nTiempo restante: ~" + hStr + " h";

		} else if (dif == Dificultad.NORMAL) {
			final int hAprox = (int) Math.round(this.horasRestantes);
			this.cachedTooltipTexto = "Estabilidad Geológica: " + fase + "\nTiempo estimado: ~" + hAprox
					+ " h (Margen ±1 h)\nLa roca cruje ocasionalmente.";

		} else {
			final int minH = Math.max(0, (int) Math.floor(this.horasRestantes * 0.65));
			final int maxH = (int) Math.ceil(this.horasRestantes * 1.35);
			this.cachedTooltipTexto = "Estabilidad Geológica: " + fase + "\nTiempo estimado: Entre " + minH + " y "
					+ maxH + " h\n'La roca gime bajo una presión inmensa...'";
		}
	}

	@Override
	protected double getLimite() {
		return this.duracionTotal;
	}

	@Override
	protected double getCantidadActual() {
		return this.horasRestantes;
	}
}