package principal.igu;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.text.DecimalFormat;

import principal.entes.Ente;
import principal.utilidades.DibujoDebug;
import principal.utilidades.Globales;

public abstract class Barra extends Ente {
	private final Rectangle AREA;
	private final Color COLOR_BORDES;
	private final Color COLOR_RELLENO;
	private final Color COLOR_FONDO;
	private final Color COLOR_TEXTO;
	private final DecimalFormat DF;
	private int ancho;

	public Barra(final Rectangle area, final Color colorBordes, final Color colorFondo, final Color colorRelleno,
			final Color colorTexto) {
		this.AREA = area;
		this.COLOR_BORDES = colorBordes;
		this.COLOR_FONDO = colorFondo;
		this.COLOR_RELLENO = colorRelleno;
		this.COLOR_TEXTO = colorTexto;
		this.DF = new DecimalFormat("0.00");

	}

	@Override
	public void pintar(final Graphics2D g) {
//		System.out.println("pintando barra: "+ this.AREA+" , porcentaje: "+this.anchoVida);
		DibujoDebug.dibujarRectanguloContorno(g, this.AREA, this.COLOR_BORDES);
		DibujoDebug.dibujarRectanguloRelleno(g, this.AREA.x, this.AREA.y, this.AREA.width, this.AREA.height,
				this.COLOR_FONDO);
		DibujoDebug.dibujarRectanguloRelleno(g, this.AREA.x, this.AREA.y, this.ancho, this.AREA.height,
				this.COLOR_RELLENO);
		this.pintarInfo(g);

	}

	@Override
	public void actualizar() {

		final int porcentaje = (int) ((this.getCantidadActual() * 100) / this.getLimite());

		this.ancho = (porcentaje * (this.AREA.width)) / (100);

	}

	private void pintarInfo(final Graphics2D g) {
		final String info = String.valueOf((this.DF.format(this.getCantidadActual()))) + " / "
				+ String.valueOf(this.DF.format(this.getLimite()));
		final float tamaFuente = g.getFont().getSize();

		g.setFont(g.getFont().deriveFont(6f));
		final int anchoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, info);
		final int altoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, info);

		final int x = this.AREA.x + ((this.AREA.width - anchoTexto) / 2);
		final int y = (this.AREA.y + this.AREA.height) - ((this.AREA.height - altoTexto) / 2) - 2;

		DibujoDebug.dibujarString(g, info, x, y, this.COLOR_TEXTO);

		g.setFont(g.getFont().deriveFont(tamaFuente));
	}

	protected abstract double getLimite();

	protected abstract double getCantidadActual();

	@Override
	public Rectangle getArea() {
		return new Rectangle(this.getPosicionXInt(), this.getPosicionYInt(), this.getAncho(), this.getAlto());
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

	public void restaurar() {
		this.eliminado = false;
	}
}
