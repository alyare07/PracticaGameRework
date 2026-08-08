package principal.entes.modelos.item;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import principal.entes.objetos.items.Item;
import principal.utilidades.Textura;

public class ModeloConsumible extends ModeloItem {
	private final int CODIGO_TEXTURA_INVENTARIO;
	private final int CODIGO_TEXTURA_MAPA;
	private final int LIMITE;

	public ModeloConsumible(final String nombre, final int ancho, final int alto, final int limite,
			final boolean esSolido, final Rectangle margenesInterseccion, final int codTexturaInv,
			final int codTexturaMapa) {
		super(nombre, Item.COD_ITEM_CONSUMIBLE, ancho, alto, esSolido, margenesInterseccion);
		int max = 0;
		if (limite > 0) {
			max = limite;
		}
		this.LIMITE = max;
		this.CODIGO_TEXTURA_INVENTARIO = codTexturaInv;
		this.CODIGO_TEXTURA_MAPA = codTexturaMapa;
	}

	public ModeloConsumible(final String nombre, final int lado, final int limite, final boolean esSolido,
			final Rectangle margenesInterseccion, final int codTexturaInv, final int codTexturaMapa) {
		super(nombre, Item.COD_ITEM_CONSUMIBLE, lado, esSolido, margenesInterseccion);
		int max = 0;
		if (limite > 0) {
			max = limite;
		}
		this.LIMITE = max;
		this.CODIGO_TEXTURA_INVENTARIO = codTexturaInv;
		this.CODIGO_TEXTURA_MAPA = codTexturaMapa;
	}

	public int getLimite() {
		return this.LIMITE;
	}

	@Override
	public BufferedImage getTexturaInventario() {
		return Textura.getTextura(CODIGO_TEXTURA_INVENTARIO);
	}

	@Override
	public BufferedImage getTexturaMapa() {
		return Textura.getTextura(CODIGO_TEXTURA_MAPA);
	}

//	private final boolean SOLIDO;
//	private final Rectangle MARGENES_INTERSECCION;
//	private final int CODIGO_TEXTURA_INVENTARIO;
//	private final int CODIGO_TEXTURA_MAPA;
//	private final int LIMITE;
//	private final int TIPO_ITEM;
//	private final int ALTO;
//	private final int ANCHO;
//

//	public ModeloConsumible(final int ancho, final int alto, final int limite, final boolean esSolido,
//			final Rectangle margenesInterseccion, final int codTexturaInv, final int codTexturaMapa) {
//		this.TIPO_ITEM = Item.COD_ITEM_CONSUMIBLE;
//		this.SOLIDO = esSolido;
//		this.MARGENES_INTERSECCION = margenesInterseccion;
//		this.CODIGO_TEXTURA_INVENTARIO = codTexturaInv;
//		this.CODIGO_TEXTURA_MAPA = codTexturaMapa;
//		int max = 0;
//		if (limite > 0) {
//			max = limite;
//		}
//		this.LIMITE = max;
//		this.ALTO = alto;
//		this.ANCHO = ancho;
//	}
//
//	public ModeloConsumible(final int lado, final int limite, final boolean esSolido,
//			final Rectangle margenesInterseccion, final int codTexturaInv, final int codTexturaMapa) {
//		this.TIPO_ITEM = Item.COD_ITEM_CONSUMIBLE;
//		this.SOLIDO = esSolido;
//		this.MARGENES_INTERSECCION = margenesInterseccion;
//		this.CODIGO_TEXTURA_INVENTARIO = codTexturaInv;
//		this.CODIGO_TEXTURA_MAPA = codTexturaMapa;
//		int max = 0;
//		if (limite > 0) {
//			max = limite;
//		}
//		this.LIMITE = max;
//		this.ALTO = lado;
//		this.ANCHO = lado;
//	}
//
//	public boolean esSolido() {
//		return this.SOLIDO;
//	}
//
//	public int getLimite() {
//		return this.LIMITE;
//	}
//
//	public Rectangle getMargenesInterseccion() {
//		return MARGENES_INTERSECCION;
//	}
//
//	public BufferedImage getTexturaInventario() {
//		return Textura.getTextura(this.CODIGO_TEXTURA_INVENTARIO);
//	}
//
//	public BufferedImage getTexturaMapa() {
//		return Textura.getTextura(this.CODIGO_TEXTURA_MAPA);
//	}
//
//	public int getTipoItem() {
//		return this.TIPO_ITEM;
//	}
//
//	public int getAncho() {
//		return this.ANCHO;
//	}
//
//	public int getAlto() {
//		return this.ALTO;
//	}
}
