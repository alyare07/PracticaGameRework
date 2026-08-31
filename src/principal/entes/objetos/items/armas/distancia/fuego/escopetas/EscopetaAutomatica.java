package principal.entes.objetos.items.armas.distancia.fuego.escopetas;

import org.json.simple.JSONObject;

import principal.entes.modelos.item.ListaModelosItem;
import principal.entes.objetos.Objeto;

/**
 * Escopeta de combate automática de tambor (Tier 3). Capacidad de 12 cartuchos,
 * cadencia rápida de 350 ms, recarga de 2.8 s y perdigones penetrantes.
 * 
 * @version 2.0 (Java 8 Compatible - Zero-GC Architecture)
 */
public class EscopetaAutomatica extends Escopeta {

	private static final long serialVersionUID = 681920391209381L;

	public EscopetaAutomatica() {
		super(ListaModelosItem.COD_ARMA_ESCOPETA_AUTOMATICA, 5, 240, true, 12, 2800, 350, 10, 18.0, 5.2);
	}

	public EscopetaAutomatica(final int x, final int y) {
		super(x, y, ListaModelosItem.COD_ARMA_ESCOPETA_AUTOMATICA, 5, 240, true, 12, 2800, 350, 10, 18.0, 5.2);
	}

	public EscopetaAutomatica(final int x, final int y, final int balasCargador) {
		super(x, y, ListaModelosItem.COD_ARMA_ESCOPETA_AUTOMATICA, 5, 240, true, 12, 2800, 350, 10, 18.0, 5.2);
		this.balasCargador = Math.max(0, Math.min(this.capacidadCargador, balasCargador));
	}

	@Override
	public Objeto copiar() {
		return new EscopetaAutomatica(this.getPosicionXInt(), this.getPosicionYInt(), this.balasCargador);
	}

	@Override
	public String exportarTipoItem() {
		return "EscopetaAutomatica";
	}

	public static EscopetaAutomatica crearDesdeJson(final JSONObject json) {
		final int x = Integer.parseInt(json.get("x").toString());
		final int y = Integer.parseInt(json.get("y").toString());
		final int balasCargador = Integer.parseInt(json.get("balasCargador").toString());

		return new EscopetaAutomatica(x, y, balasCargador);
	}
}