package principal.maquinaestado.estados.menu.herramientas;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

import principal.utilidades.Globales;
import principal.utilidades.Render2D;

public class Label extends Componente {
	protected String texto;
	protected final Point PUNTO;
	protected Color color;
	protected float tamano;
	protected int ancho;
	protected int alto;

	public Label(final String text, final int x, final int y, final Color color, final float tamano) {
		this.texto = text;
		this.PUNTO = new Point(x, y);
		this.color = color;
		this.tamano = tamano;
		this.calcularMedidas();
	}

	@Override
	public void pintar(final Graphics2D g) {
		g.setFont(Globales.GESTOR_FUENTES.getFuente(this.tamano));
		Render2D.dibujarString(g, this.texto, this.PUNTO, this.color);
	}

	@Override
	public void pintar(final Graphics2D g, final int desplazamientoY) {
		g.setFont(Globales.GESTOR_FUENTES.getFuente(this.tamano));
		Render2D.dibujarString(g, this.texto, this.PUNTO.x, this.PUNTO.y - desplazamientoY, this.color);
	}

	@Override
	public void actualizar() {

	}

	private void calcularMedidas() {
		final BufferedImage img = new BufferedImage(this.texto.length() * 5, this.texto.length() * 5,
				BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = (Graphics2D) img.getGraphics();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(this.tamano));
		this.ancho = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, this.texto);
		this.alto = Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, this.texto);
		g.dispose();
	}

	public int getAncho() {
		return this.ancho;
	}

	public int getAlto() {
		return this.alto;
	}

	public Point getPunto() {
		return this.PUNTO;
	}

}
