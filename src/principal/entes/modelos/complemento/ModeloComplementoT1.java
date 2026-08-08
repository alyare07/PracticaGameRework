package principal.entes.modelos.complemento;

import java.awt.Rectangle;
import java.awt.Shape;

import principal.entes.objetos.Complemento;
import principal.utilidades.Animacion;

public final class ModeloComplementoT1 extends ModeloComplemento {
	
	private final Rectangle MARGENES_INTERSECCION;
	
	/*
	 * AGREGAR UNA LISTA QUE CONTENGAN LAS AREAS NO SOLIDAS PARA AFINAR MAS EL TEMA DE CUANDO HACER TRANSPARENTE A UNA ENTIDAD QUE ESTE ATRAS DEL COMPLEMENTO!
	 * TANTO EN ESTE CLASE COMO LA CLASE DE MODELOCOMPLEMENTOT2
	 */
	
	
	public ModeloComplementoT1(int ancho, int alto, int codImagen, boolean solido, final boolean ContieneZonaNoSolida, Rectangle margenesInterseccion) {
		super(ancho, alto, codImagen, solido, ContieneZonaNoSolida);
		this.MARGENES_INTERSECCION = margenesInterseccion;
	}
	public ModeloComplementoT1(int lado, int codImagen, boolean solido, final boolean ContieneZonaNoSolida, Rectangle margenesInterseccion,
			Animacion animacion) {
		super(lado, codImagen, solido, ContieneZonaNoSolida, animacion);
		this.MARGENES_INTERSECCION = margenesInterseccion;
	}
	public ModeloComplementoT1(int lado, int codImagen, boolean solido, final boolean ContieneZonaNoSolida, Rectangle margenesInterseccion) {
		super(lado, codImagen, solido, ContieneZonaNoSolida);
		this.MARGENES_INTERSECCION = margenesInterseccion;
	}

	public ModeloComplementoT1(int ancho, int alto, int codImagen, boolean solido, final boolean ContieneZonaNoSolida, Rectangle margenesInterseccion,
			Animacion animacion) {
		super(ancho, alto, codImagen, solido, ContieneZonaNoSolida, animacion);
		this.MARGENES_INTERSECCION = margenesInterseccion;
	}
	
	public Rectangle getMargenesInterseccion() {
		return MARGENES_INTERSECCION;
	}
	
	public Rectangle getMargenesInterseccionEnBasePosicion(final int x, final int y) {
		return new Rectangle(x + this.MARGENES_INTERSECCION.x,
				y + this.MARGENES_INTERSECCION.y,
				this.getAncho() - this.MARGENES_INTERSECCION.width - this.MARGENES_INTERSECCION.x,
				this.getAlto() - this.MARGENES_INTERSECCION.height - this.MARGENES_INTERSECCION.y);
	} 
	
	@Override
	public boolean intersecta(Shape area, Complemento cPropietario) {
		final Rectangle r = new Rectangle(cPropietario.getPosicionXInt() + MARGENES_INTERSECCION.x,
				cPropietario.getPosicionYInt() + MARGENES_INTERSECCION.y,
				getAncho() - MARGENES_INTERSECCION.width - MARGENES_INTERSECCION.x,
				getAlto() - MARGENES_INTERSECCION.height - this.MARGENES_INTERSECCION.y);
		return area.intersects(r);
	}
}
