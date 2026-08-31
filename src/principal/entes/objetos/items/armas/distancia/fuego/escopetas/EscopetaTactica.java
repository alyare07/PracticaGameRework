package principal.entes.objetos.items.armas.distancia.fuego.escopetas;

import org.json.simple.JSONObject;

import principal.entes.modelos.item.ListaModelosItem;
import principal.entes.objetos.Objeto;

/**
 * Escopeta táctica de corredera (Tier 2). Capacidad de 8 cartuchos, cadencia de
 * 750 ms y recarga tubular de 2.4 s.
 * 
 * @version 2.0 (Java 8 Compatible - Zero-GC Architecture)
 */
public class EscopetaTactica extends Escopeta {

	private static final long serialVersionUID = 591829301923812L;

	public EscopetaTactica() {
		super(ListaModelosItem.COD_ARMA_ESCOPETA_TACTICA, 5, 200, false, 8, 2400, 750, 8, 16.0, 4.8);
	}

	public EscopetaTactica(final int x, final int y) {
		super(x, y, ListaModelosItem.COD_ARMA_ESCOPETA_TACTICA, 5, 200, false, 8, 2400, 750, 8, 16.0, 4.8);
	}

	public EscopetaTactica(final int x, final int y, final int balasCargador) {
		super(x, y, ListaModelosItem.COD_ARMA_ESCOPETA_TACTICA, 5, 200, false, 8, 2400, 750, 8, 16.0, 4.8);
		this.balasCargador = Math.max(0, Math.min(this.capacidadCargador, balasCargador));
	}

	@Override
	public Objeto copiar() {
		return new EscopetaTactica(this.getPosicionXInt(), this.getPosicionYInt(), this.balasCargador);
	}

	@Override
	public String exportarTipoItem() {
		return "EscopetaTactica";
	}

	public static EscopetaTactica crearDesdeJson(final JSONObject json) {
		final int x = Integer.parseInt(json.get("x").toString());
		final int y = Integer.parseInt(json.get("y").toString());
		final int balasCargador = Integer.parseInt(json.get("balasCargador").toString());

		return new EscopetaTactica(x, y, balasCargador);
	}
}