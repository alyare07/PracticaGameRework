package principal.entes.objetos.items.equipablesmano;

import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.EquipableManoPrincipal;
import principal.entes.objetos.items.Portable;
import principal.recursos.TexturaItem;

/**
 * Ítem físico de mapa cartográfico. Puede almacenarse en la mochila o empuñarse
 * en cualquiera de las dos manos (SlotArma o SlotManoSecundaria).
 * 
 * @version 1.0 (Vanilla Java 8 - Diegetic Map Subsystem)
 */
public class ItemMapa extends Portable implements EquipableManoPrincipal {

	private static final long serialVersionUID = 8912389127391823L;

	public static final String CODIGO_MODELO = "ITEM_MAPA";
	public static final String NOMBRE_MAPA = "Mapa Cartográfico";

	public ItemMapa(final int x, final int y) {
		super(x, y, CODIGO_MODELO, NOMBRE_MAPA, TexturaItem.MAPA_INV, TexturaItem.MAPA_MAPA);
		this.precioBasePlata = 60L; // 60 monedas de plata en el mercado
	}

	public ItemMapa() {
		this(0, 0);
	}

	@Override
	public Objeto copiar() {
		return new ItemMapa(this.getPosicionXInt(), this.getPosicionYInt());
	}

	@Override
	public String exportarTipoItem() {
		return CODIGO_MODELO;
	}
}