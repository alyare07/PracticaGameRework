package principal.entes.objetos.items;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import principal.recursos.ClaveHoja;
import principal.utilidades.Globales;
import principal.utilidades.HojaSprite;
import principal.utilidades.Render2D;

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
		final HojaSprite hoja = Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.DUNGEON_16);
		if (hoja != null) {
			return hoja.getSprite(35); // Sprite provisional de mazmorra
		}
		return Globales.GESTOR_TEXTURAS.getTexturaError();
	}

	@Override
	public BufferedImage getTextura() {
		return this.getTexturaInventario();
	}

	@Override
	public void pintarInventario(final Graphics2D g, final int x, final int y) {
		final BufferedImage img = this.getTexturaInventario();
		if (img != null) {
			Render2D.dibujarImagen(g, img, x, y);
		} else {
			// Placeholder procedural mientras no tenga textura final
			Render2D.dibujarRectanguloRelleno(g, x + 6, y + 4, 4, 10, new Color(130, 80, 30));
			Render2D.dibujarRectanguloRelleno(g, x + 5, y + 2, 6, 4, new Color(255, 140, 20));
		}
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