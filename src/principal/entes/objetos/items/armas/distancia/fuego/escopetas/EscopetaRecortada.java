package principal.entes.objetos.items.armas.distancia.fuego.escopetas;

import org.json.simple.JSONObject;

import principal.entes.modelos.item.ListaModelosItem;
import principal.entes.objetos.Objeto;

/**
 * Escopeta recortada de dos cañones (Tier 1). Capacidad de 2 cartuchos con
 * disparo casi instantáneo entre cañones (300 ms) y recarga de 1.6 s.
 * 
 * @version 2.0 (Java 8 Compatible - Zero-GC Architecture)
 */
public class EscopetaRecortada extends Escopeta {

	private static final long serialVersionUID = 491029301920391L;

	public EscopetaRecortada() {
		super(ListaModelosItem.COD_ARMA_ESCOPETA_RECORTADA, 7, 140, false, 2, 1600, 300, 6, 26.0, 4.2);
	}

	public EscopetaRecortada(final int x, final int y) {
		super(x, y, ListaModelosItem.COD_ARMA_ESCOPETA_RECORTADA, 7, 140, false, 2, 1600, 300, 6, 26.0, 4.2);
	}

	public EscopetaRecortada(final int x, final int y, final int balasCargador) {
		super(x, y, ListaModelosItem.COD_ARMA_ESCOPETA_RECORTADA, 7, 140, false, 2, 1600, 300, 6, 26.0, 4.2);
		this.balasCargador = Math.max(0, Math.min(this.capacidadCargador, balasCargador));
	}

	@Override
	public Objeto copiar() {
		return new EscopetaRecortada(this.getPosicionXInt(), this.getPosicionYInt(), this.balasCargador);
	}

	@Override
	public String exportarTipoItem() {
		return "EscopetaRecortada";
	}

	public static EscopetaRecortada crearDesdeJson(final JSONObject json) {
		final int x = Integer.parseInt(json.get("x").toString());
		final int y = Integer.parseInt(json.get("y").toString());
		final int balasCargador = Integer.parseInt(json.get("balasCargador").toString());

		return new EscopetaRecortada(x, y, balasCargador);
	}
}