package principal.entes.objetos.items;

import java.awt.image.BufferedImage;

import principal.recursos.TexturaItem;
import principal.utilidades.Globales;

/**
 * Ítem portátil equipable en la mano secundaria [SEC] o mano principal para
 * iluminar penumbras, noches y cuevas (Zero-GC / O(1)).
 * 
 * @version 1.1 (Vanilla Java 8 - Portable Exact Hierarchy Alignment)
 */
public class Antorcha extends Portable {

	private static final long serialVersionUID = 1L;

	public static final String CODIGO_MODELO = "Antorcha";

	public Antorcha(final int x, final int y) {
		super(x, y, CODIGO_MODELO);
	}

	public Antorcha() {
		this(0, 0);
	}

	@Override
	public BufferedImage getTexturaInventario() {
		return Globales.GESTOR_TEXTURAS.get(TexturaItem.ANTORCHA_INV);
	}

	@Override
	public BufferedImage getTextura() {
		return Globales.GESTOR_TEXTURAS.get(TexturaItem.ANTORCHA_MAPA);
	}

	@Override
	public Item copiar() {
		return new Antorcha(this.getPosicionXInt(), this.getPosicionYInt());
	}

	@Override
	public String exportarTipoItem() {
		return "Antorcha";
	}
}