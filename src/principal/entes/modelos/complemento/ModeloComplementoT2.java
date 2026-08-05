package principal.entes.modelos.complemento;

import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;

import principal.entes.objetos.Complemento;
import principal.utilidades.Animacion;

public final class ModeloComplementoT2 extends ModeloComplemento {
	
	private final ArrayList<Rectangle> MARGENES_INTERSECCION;
	
	public ModeloComplementoT2(int ancho, int alto, int codImagen, boolean solido, final boolean ContieneZonaNoSolida, ArrayList<Rectangle> margenesInterseccion) {
		super(ancho, alto, codImagen, solido, ContieneZonaNoSolida);
		this.MARGENES_INTERSECCION = margenesInterseccion;
	}
	public ModeloComplementoT2(int lado, int codImagen, boolean solido, final boolean ContieneZonaNoSolida, ArrayList<Rectangle> margenesInterseccion,
			Animacion animacion) {
		super(lado, codImagen, solido, ContieneZonaNoSolida, animacion);
		this.MARGENES_INTERSECCION = margenesInterseccion;
	}
	public ModeloComplementoT2(int lado, int codImagen, boolean solido, final boolean ContieneZonaNoSolida, ArrayList<Rectangle> margenesInterseccion) {
		super(lado, codImagen, solido, ContieneZonaNoSolida);
		this.MARGENES_INTERSECCION = margenesInterseccion;
	}

	public ModeloComplementoT2(int ancho, int alto, int codImagen, boolean solido, final boolean ContieneZonaNoSolida, ArrayList<Rectangle> margenesInterseccion,
			Animacion animacion) {
		super(ancho, alto, codImagen, solido, ContieneZonaNoSolida, animacion);
		this.MARGENES_INTERSECCION = margenesInterseccion;
	}
	
	public ArrayList<Rectangle> getMargenesInterseccion(){
		return this.MARGENES_INTERSECCION;
	}
	
	public ArrayList<Rectangle> getMargenesInterseccionEnBasePosicion(final int x, final int y) {
		ArrayList<Rectangle> lista = new ArrayList<Rectangle>();
		for(Rectangle margen : this.MARGENES_INTERSECCION) {
			lista.add(new Rectangle(x + margen.x,
					y + margen.y,
					this.getAncho() - margen.width - margen.x,
					this.getAlto() - margen.height - margen.y));
		}
		return lista;
	}
	
	@Override
	public boolean intersecta(Shape area, Complemento cPropietario) {
		Rectangle r = null;
		for(Rectangle margen : this.MARGENES_INTERSECCION) {
			r = new Rectangle(cPropietario.getPosicionXInt() + margen.x,
					cPropietario.getPosicionYInt() + margen.y,
					getAncho() - margen.width - margen.x,
					getAlto() - margen.height - margen.y);
			if(area.intersects(r)){
				return true;
			}
		}
		return false;
	}
}
