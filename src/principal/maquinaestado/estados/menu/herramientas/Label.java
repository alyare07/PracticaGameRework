package principal.maquinaestado.estados.menu.herramientas;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;

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
	public void pintar(Graphics2D g) {
		g.setFont(g.getFont().deriveFont(tamano));
		DibujoDebug.dibujarString(g, texto, PUNTO, color);
		g.setFont(g.getFont().deriveFont(Constantes.TAMANO_FUENTE));
	}
	
	@Override
	public void pintar(Graphics2D g, final int desplazamientoY) {
		g.setFont(g.getFont().deriveFont(tamano));
		DibujoDebug.dibujarString(g, texto, PUNTO.x, PUNTO.y -desplazamientoY, color);
		g.setFont(g.getFont().deriveFont(Constantes.TAMANO_FUENTE));
	}

	@Override
	public void actualizar() {

	}
	
	private void calcularMedidas() {
		final BufferedImage img = new BufferedImage(this.texto.length()*5, this.texto.length()*5, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = (Graphics2D) img.getGraphics();
		g.setFont(g.getFont().deriveFont(this.tamano));
		this.ancho =  Constantes.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, texto);
		this.alto =  Constantes.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, texto);
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
