package principal.entes.objetos.items.desplegables;

import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.EquipableManoPrincipal;
import principal.entes.objetos.items.Portable;
import principal.recursos.TexturaItem;

/**
 * Brújula náutica/topográfica. Desbloquea la orientación magnética,
 * la aguja cardinal y los puntos N, S, E, O en el pergamino cartográfico.
 * 
 * @version 1.0 (Vanilla Java 8 - Diegetic Compass Instrument)
 */
public class ItemBrujula extends Portable implements EquipableManoPrincipal {

	private static final long serialVersionUID = 7182938129381293L;

	public static final String CODIGO_MODELO = "ITEM_BRUJULA";
	public static final String NOMBRE_BRUJULA = "Brújula de Latón";

	public ItemBrujula(final int x, final int y) {
		super(x, y, CODIGO_MODELO, NOMBRE_BRUJULA, TexturaItem.ANILLO_ORO_INV, TexturaItem.ANILLO_ORO_MAPA);
		this.precioBasePlata = 85L;
	}

	public ItemBrujula() {
		this(0, 0);
	}

	@Override
	public Objeto copiar() {
		return new ItemBrujula(this.getPosicionXInt(), this.getPosicionYInt());
	}

	@Override
	public String exportarTipoItem() {
		return CODIGO_MODELO;
	}
}