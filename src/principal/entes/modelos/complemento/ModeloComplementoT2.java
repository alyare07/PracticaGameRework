package principal.entes.modelos.complemento;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import principal.entes.objetos.Complemento;
import principal.utilidades.Animacion;

public final class ModeloComplementoT2 extends ModeloComplemento {

	private final ArrayList<Rectangle> MARGENES_INTERSECCION;

	public ModeloComplementoT2(final int ancho, final int alto, final BufferedImage textura, final boolean solido,
			final boolean contieneZonaNoSolida, final ArrayList<Rectangle> margenesInterseccion) {
		super(ancho, alto, textura, solido, contieneZonaNoSolida);
		this.MARGENES_INTERSECCION = (margenesInterseccion != null) ? margenesInterseccion : new ArrayList<Rectangle>();
	}

	public ModeloComplementoT2(final int lado, final BufferedImage textura, final boolean solido,
			final boolean contieneZonaNoSolida, final ArrayList<Rectangle> margenesInterseccion,
			final Animacion animacion) {
		super(lado, textura, solido, contieneZonaNoSolida, animacion);
		this.MARGENES_INTERSECCION = (margenesInterseccion != null) ? margenesInterseccion : new ArrayList<Rectangle>();
	}

	public ModeloComplementoT2(final int lado, final BufferedImage textura, final boolean solido,
			final boolean contieneZonaNoSolida, final ArrayList<Rectangle> margenesInterseccion) {
		super(lado, textura, solido, contieneZonaNoSolida);
		this.MARGENES_INTERSECCION = (margenesInterseccion != null) ? margenesInterseccion : new ArrayList<Rectangle>();
	}

	public ModeloComplementoT2(final int ancho, final int alto, final BufferedImage textura, final boolean solido,
			final boolean contieneZonaNoSolida, final ArrayList<Rectangle> margenesInterseccion,
			final Animacion animacion) {
		super(ancho, alto, textura, solido, contieneZonaNoSolida, animacion);
		this.MARGENES_INTERSECCION = (margenesInterseccion != null) ? margenesInterseccion : new ArrayList<Rectangle>();
	}

	public ArrayList<Rectangle> getMargenesInterseccion() {
		return this.MARGENES_INTERSECCION;
	}

	public ArrayList<Rectangle> getMargenesInterseccionEnBasePosicion(final int x, final int y) {
		final ArrayList<Rectangle> lista = new ArrayList<Rectangle>();
		for (final Rectangle margen : this.MARGENES_INTERSECCION) {
			lista.add(new Rectangle(x + margen.x, y + margen.y, this.getAncho() - margen.width - margen.x,
					this.getAlto() - margen.height - margen.y));
		}
		return lista;
	}

	@Override
	public boolean intersecta(final Shape area, final Complemento cPropietario) {
		if ((area == null) || (cPropietario == null)) {
			return false;
		}
		final int px = cPropietario.getPosicionXInt();
		final int py = cPropietario.getPosicionYInt();
		final int anchoTotal = this.getAncho();
		final int altoTotal = this.getAlto();
		final int totalMargenes = this.MARGENES_INTERSECCION.size();

		for (int i = 0; i < totalMargenes; i++) {
			final Rectangle margen = this.MARGENES_INTERSECCION.get(i);
			final int x = px + margen.x;
			final int y = py + margen.y;
			final int w = anchoTotal - margen.width - margen.x;
			final int h = altoTotal - margen.height - margen.y;

			if (area.intersects(x, y, w, h)) {
				return true;
			}
		}
		return false;
	}
}