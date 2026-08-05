package principal.entes.modelos.item;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import principal.utilidades.Textura;

public class ModeloGranada extends ModeloConsumible {
	private final int COD_TEXTURA_EXPLOSION;
	public ModeloGranada(String nombre, int lado, int limite, boolean esSolido, Rectangle margenesInterseccion,
			int codTexturaInv, int codTexturaMapa, final int codTexturaExplosion) {
		super(nombre, lado, limite, esSolido, margenesInterseccion, codTexturaInv, codTexturaMapa);
		this.COD_TEXTURA_EXPLOSION = codTexturaExplosion;
	}
	
	
	public BufferedImage getTexturaExplosion() {
		return Textura.getTextura(this.COD_TEXTURA_EXPLOSION);
	}

}
