package principal.entes.modelos.complemento;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;

import principal.entes.objetos.Complemento;
import principal.utilidades.Animacion;

public class ModeloComplementoT1 extends ModeloComplemento {

	private final Rectangle MARGENES_INTERSECCION;

	public ModeloComplementoT1(final int ancho, final int alto, final BufferedImage textura, final boolean solido,
			final boolean contieneZonaNoSolida, final Rectangle margenesInterseccion) {
		super(ancho, alto, textura, solido, contieneZonaNoSolida);
		this.MARGENES_INTERSECCION = (margenesInterseccion != null) ? margenesInterseccion : new Rectangle();
	}

	public ModeloComplementoT1(final int lado, final BufferedImage textura, final boolean solido,
			final boolean contieneZonaNoSolida, final Rectangle margenesInterseccion) {
		super(lado, textura, solido, contieneZonaNoSolida);
		this.MARGENES_INTERSECCION = (margenesInterseccion != null) ? margenesInterseccion : new Rectangle();
	}

	public ModeloComplementoT1(final int lado, final BufferedImage textura, final boolean solido,
			final boolean contieneZonaNoSolida, final Rectangle margenesInterseccion, final Animacion animacion) {
		super(lado, textura, solido, contieneZonaNoSolida, animacion);
		this.MARGENES_INTERSECCION = (margenesInterseccion != null) ? margenesInterseccion : new Rectangle();
	}

	public ModeloComplementoT1(final int ancho, final int alto, final BufferedImage textura, final boolean solido,
			final boolean contieneZonaNoSolida, final Rectangle margenesInterseccion, final Animacion animacion) {
		super(ancho, alto, textura, solido, contieneZonaNoSolida, animacion);
		this.MARGENES_INTERSECCION = (margenesInterseccion != null) ? margenesInterseccion : new Rectangle();
	}

	public Rectangle getMargenesInterseccion() {
		return this.MARGENES_INTERSECCION;
	}

	public Rectangle getMargenesInterseccionEnBasePosicion(final int x, final int y) {
		return new Rectangle(x + this.MARGENES_INTERSECCION.x, y + this.MARGENES_INTERSECCION.y,
				this.getAncho() - this.MARGENES_INTERSECCION.width - this.MARGENES_INTERSECCION.x,
				this.getAlto() - this.MARGENES_INTERSECCION.height - this.MARGENES_INTERSECCION.y);
	}

	@Override
	public boolean intersecta(final Shape area, final Complemento cPropietario) {
		if ((area == null) || (cPropietario == null)) {
			return false;
		}
		final int x = cPropietario.getPosicionXInt() + this.MARGENES_INTERSECCION.x;
		final int y = cPropietario.getPosicionYInt() + this.MARGENES_INTERSECCION.y;
		final int w = this.getAncho() - this.MARGENES_INTERSECCION.width - this.MARGENES_INTERSECCION.x;
		final int h = this.getAlto() - this.MARGENES_INTERSECCION.height - this.MARGENES_INTERSECCION.y;

		return area.intersects(x, y, w, h); // 0 bytes en Heap
	}
}