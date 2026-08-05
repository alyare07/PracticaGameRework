package principal.igu;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.text.DecimalFormat;
import principal.entes.Ente;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;

public abstract class Barra extends Ente {
	private final Rectangle AREA;
	private final Color COLOR_BORDES;
	private final Color COLOR_RELLENO;
	private final Color COLOR_FONDO;
	private final Color COLOR_TEXTO;
	private DecimalFormat DF;
	private int ancho;

	public Barra(final Rectangle area, final Color colorBordes, final Color colorFondo, final Color colorRelleno, final Color colorTexto) {
		this.AREA = area;
		this.COLOR_BORDES = colorBordes;
		this.COLOR_FONDO = colorFondo;
		this.COLOR_RELLENO = colorRelleno;
		this.COLOR_TEXTO = colorTexto;
		this.DF = new DecimalFormat("0.00");

	}
	
	public void pintar(final Graphics2D g) {
//		System.out.println("pintando barra: "+ this.AREA+" , porcentaje: "+this.anchoVida);
		DibujoDebug.dibujarRectanguloContorno(g, AREA, COLOR_BORDES);
		DibujoDebug.dibujarRectanguloRelleno(g, this.AREA.x, this.AREA.y, this.AREA.width, this.AREA.height, COLOR_FONDO);
		DibujoDebug.dibujarRectanguloRelleno(g, this.AREA.x, this.AREA.y, this.ancho, this.AREA.height, COLOR_RELLENO);
		this.pintarInfo(g);
		
	}
	
	public void actualizar() {
		
		int porcentaje = (int) (this.getCantidadActual() * 100 / this.getLimite());
		
		this.ancho = porcentaje * (this.AREA.width) / (100);
		
	}
	
	private void pintarInfo(final Graphics2D g) {
		final String info = String.valueOf((DF.format(this.getCantidadActual())))+" / "+String.valueOf(DF.format(this.getLimite()));
		final float tamaFuente = g.getFont().getSize();
		
		g.setFont(g.getFont().deriveFont(6f));
		final int anchoTexto = Constantes.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, info);
		final int altoTexto = Constantes.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, info);
		
		int x = this.AREA.x + (this.AREA.width - anchoTexto)/2;
		int y = this.AREA.y + this.AREA.height - (this.AREA.height - altoTexto)/2 -2;
		
		DibujoDebug.dibujarString(g, info, x, y, COLOR_TEXTO);
		
		g.setFont(g.getFont().deriveFont(tamaFuente));
	}
	
	protected abstract double getLimite();
	
	protected abstract double getCantidadActual();
	
	public Rectangle getArea() {
		return new Rectangle(getPosicionXInt(), getPosicionYInt(), getAncho(), getAlto());
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
	
	public int getAncho() {
		return this.AREA.width;
	}
	
	public int getAlto() {
		return this.AREA.height;
	}

	@Override
	public void modificarPosicionX(double desplazamientoX) {
	}

	@Override
	public void modificarPosicionY(double desplazamientoY) {

	}

	@Override
	public boolean estaEliminado() {
		return this.eliminado;
	}
	
	public void restaurar() {
		this.eliminado = false;
	}
}
