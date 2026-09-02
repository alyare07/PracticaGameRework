package principal.igu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import principal.entes.Ente;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

/**
 * Clase base abstracta para las barras de estado del HUD (Zero-GC / O(1)).
 * 
 * @version 3.0 (Vanilla Java 8 - m5x7 Pixel Typography)
 */
public abstract class Barra extends Ente {

	protected final Rectangle AREA;
	protected final Color COLOR_BORDES;
	protected final Color COLOR_FONDO;
	protected final Color COLOR_RELLENO;
	protected final Color COLOR_LAG;
	protected final Color COLOR_TEXTO;

	protected int anchoActual;
	protected int anchoLag;

	// Caché de texto formateado para evitar concatenar Strings en caliente
	// (Zero-GC)
	private int lastActualInt = -1;
	private int lastLimiteInt = -1;
	private String cachedInfoTexto = "";

	public Barra(final Rectangle area, final Color colorBordes, final Color colorFondo, final Color colorRelleno,
			final Color colorTexto) {
		this(area, colorBordes, colorFondo, colorRelleno, new Color(255, 205, 40), colorTexto);
	}

	public Barra(final Rectangle area, final Color colorBordes, final Color colorFondo, final Color colorRelleno,
			final Color colorLag, final Color colorTexto) {
		this.AREA = area;
		this.COLOR_BORDES = colorBordes;
		this.COLOR_FONDO = colorFondo;
		this.COLOR_RELLENO = colorRelleno;
		this.COLOR_LAG = colorLag;
		this.COLOR_TEXTO = colorTexto;
	}

	@Override
	public void actualizar() {
		final double limite = Math.max(0.001, this.getLimite());

		final double ratioActual = Math.max(0.0, Math.min(1.0, this.getCantidadActual() / limite));
		this.anchoActual = (int) Math.round(ratioActual * (this.AREA.width - 2));

		final double ratioLag = Math.max(0.0, Math.min(1.0, this.getCantidadLag() / limite));
		this.anchoLag = (int) Math.round(ratioLag * (this.AREA.width - 2));
	}

	@Override
	public void pintar(final Graphics2D g) {
		// 1. Fondo base de la barra
		Render2D.dibujarRectanguloRelleno(g, this.AREA.x, this.AREA.y, this.AREA.width, this.AREA.height,
				this.COLOR_FONDO);

		// 2. Barra fantasma de amortiguación (Amarilla / Lag)
		if (this.anchoLag > this.anchoActual) {
			Render2D.dibujarRectanguloRelleno(g, this.AREA.x + 1, this.AREA.y + 1, this.anchoLag, this.AREA.height - 2,
					this.COLOR_LAG);
		}

		// 3. Barra frontal activa
		if (this.anchoActual > 0) {
			Render2D.dibujarRectanguloRelleno(g, this.AREA.x + 1, this.AREA.y + 1, this.anchoActual,
					this.AREA.height - 2, this.COLOR_RELLENO);
		}

		// 4. Borde exterior delimitador
		Render2D.dibujarRectanguloContorno(g, this.AREA, this.COLOR_BORDES);

		// 5. Texto de valores numéricos centrado
		this.pintarInfo(g);
	}

	protected void pintarInfo(final Graphics2D g) {
		final int act = (int) Math.ceil(this.getCantidadActual());
		final int lim = (int) Math.ceil(this.getLimite());

		if ((act != this.lastActualInt) || (lim != this.lastLimiteInt)) {
			this.lastActualInt = act;
			this.lastLimiteInt = lim;
			this.cachedInfoTexto = act + " / " + lim;
		}

		final Font fontPrevia = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 16f));

		final int anchoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, this.cachedInfoTexto);
		final int x = this.AREA.x + ((this.AREA.width - anchoTexto) / 2);
		final int y = (this.AREA.y + this.AREA.height) - 2;

		Render2D.dibujarStringConSombra(g, this.cachedInfoTexto, x, y, this.COLOR_TEXTO, Color.BLACK);

		g.setFont(fontPrevia);
	}

	protected abstract double getLimite();

	protected abstract double getCantidadActual();

	protected double getCantidadLag() {
		return this.getCantidadActual();
	}

	@Override
	public Rectangle getArea() {
		this.AREA_ENTE_RETORNO.setBounds(this.AREA);
		return this.AREA_ENTE_RETORNO;
	}

	@Override
	public void eliminar() {
		this.eliminado = true;
	}

	@Override
	public int getPosicionXInt() {
		return this.AREA.x;
	}

	@Override
	public int getPosicionYInt() {
		return this.AREA.y;
	}

	@Override
	public double getPosicionX() {
		return this.AREA.x;
	}

	@Override
	public double getPosicionY() {
		return this.AREA.y;
	}

	@Override
	public int getAncho() {
		return this.AREA.width;
	}

	@Override
	public int getAlto() {
		return this.AREA.height;
	}

	@Override
	public void modificarPosicionX(final double desplazamientoX) {
	}

	@Override
	public void modificarPosicionY(final double desplazamientoY) {
	}

	@Override
	public boolean estaEliminado() {
		return this.eliminado;
	}

	@Override
	public void setPosicion(final double x, final double y) {
	}
}