package principal.crafteo;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import principal.entes.objetos.Objeto;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.Textura;

/**
 * Objeto físico del mapa que implementa EstacionInteractiva para habilitar
 * recetas de crafteo avanzadas cuando el jugador está en su rango.
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class ObjetoEstacion extends Objeto implements EstacionInteractiva {

	private static final long serialVersionUID = 1L;

	private final EstacionCrafteo tipoEstacion;
	private final int codTextura;
	private final int ancho;
	private final int alto;
	private final boolean solido;

	public ObjetoEstacion(final int x, final int y, final EstacionCrafteo tipoEstacion, final int codTextura,
			final int ancho, final int alto, final boolean solido) {
		super(x, y);
		this.tipoEstacion = (tipoEstacion != null) ? tipoEstacion : EstacionCrafteo.MESA_TRABAJO;
		this.codTextura = codTextura;
		this.ancho = Math.max(8, ancho);
		this.alto = Math.max(8, alto);
		this.solido = solido;
	}

	@Override
	public EstacionCrafteo getTipoEstacion() {
		return this.tipoEstacion;
	}

	@Override
	public void pintar(final Graphics2D g) {
		final BufferedImage img = this.getTextura();
		if (img != null) {
			Render2D.dibujarImagenRefCamara(g, img, this.getPosicionXInt(), this.getPosicionYInt());
		}

		if (Globales.TECLADO.TECLA_VER_COLISIONES.presionado() && Globales.estadoJuego) {
			Render2D.dibujarRectanguloContornoRefCamara(g, this.getArea(), Color.CYAN);
		}
	}

	@Override
	public BufferedImage getTextura() {
		return Textura.getTextura(this.codTextura);
	}

	@Override
	public int getAncho() {
		return this.ancho;
	}

	@Override
	public int getAlto() {
		return this.alto;
	}

	@Override
	public boolean esSolido() {
		return this.solido;
	}

	@Override
	public Objeto copiar() {
		return new ObjetoEstacion(this.getPosicionXInt(), this.getPosicionYInt(), this.tipoEstacion, this.codTextura,
				this.ancho, this.alto, this.solido);
	}
}