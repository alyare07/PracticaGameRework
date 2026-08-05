package principal.entes.modelos.item;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import principal.entes.objetos.items.Item;
import principal.utilidades.Textura;

public class ModeloPortable extends ModeloItem {
	private final int CODIGO_TEXTURA_INVENTARIO;
	private final int CODIGO_TEXTURA_MAPA;

	public ModeloPortable(final String nombre, int lado, boolean esSolido, Rectangle margenesInterseccion,
			final int codTexturaInv, final int codTexturaMapa) {
		super(nombre, Item.COD_ITEM_PORTABLE, lado, esSolido, margenesInterseccion);
		this.CODIGO_TEXTURA_INVENTARIO = codTexturaInv;
		this.CODIGO_TEXTURA_MAPA = codTexturaMapa;
	}

	public ModeloPortable(final String nombre, int ancho, int alto, boolean esSolido, Rectangle margenesInterseccion,
			final int codTexturaInv, final int codTexturaMapa) {
		super(nombre, Item.COD_ITEM_PORTABLE, ancho, alto, esSolido, margenesInterseccion);
		this.CODIGO_TEXTURA_INVENTARIO = codTexturaInv;
		this.CODIGO_TEXTURA_MAPA = codTexturaMapa;
	}

	@Override
	public BufferedImage getTexturaInventario() {
		return Textura.getTextura(CODIGO_TEXTURA_INVENTARIO);
	}

	@Override
	public BufferedImage getTexturaMapa() {
		return Textura.getTextura(CODIGO_TEXTURA_MAPA);
	}

}
