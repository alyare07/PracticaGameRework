package principal.entes.objetos.items.armas.distancia.fuego.automaticas;

import org.json.simple.JSONObject;

import principal.entes.modelos.item.ListaModelosItem;
import principal.entes.objetos.Objeto;

/**
 * Rifle de asalto militar de 7.62mm (Tier 2). Alta precisión de largo alcance,
 * cadencia de 160 ms y recarga táctica de 1.8 s.
 * 
 * @version 2.0 (Java 8 Compatible - Zero-GC Architecture)
 */
public class RifleAsalto extends ArmaAutomatica {

	private static final long serialVersionUID = 981920391209381L;

	public RifleAsalto() {
		super(ListaModelosItem.COD_ARMA_RIFLE_ASALTO, 8, 320, false, 30, 1800, 160,
				ListaModelosItem.COD_CONSUMIBLE_MUNICION_FUSIL, 2.0, 5.5);
	}

	public RifleAsalto(final int x, final int y) {
		super(x, y, ListaModelosItem.COD_ARMA_RIFLE_ASALTO, 8, 320, false, 30, 1800, 160,
				ListaModelosItem.COD_CONSUMIBLE_MUNICION_FUSIL, 2.0, 5.5);
	}

	public RifleAsalto(final int x, final int y, final int balasCargador) {
		super(x, y, ListaModelosItem.COD_ARMA_RIFLE_ASALTO, 8, 320, false, 30, 1800, 160,
				ListaModelosItem.COD_CONSUMIBLE_MUNICION_FUSIL, 2.0, 5.5);
		this.balasCargador = Math.max(0, Math.min(this.capacidadCargador, balasCargador));
	}

	@Override
	public Objeto copiar() {
		return new RifleAsalto(this.getPosicionXInt(), this.getPosicionYInt(), this.balasCargador);
	}

	@Override
	public String exportarTipoItem() {
		return "RifleAsalto";
	}

	public static RifleAsalto crearDesdeJson(final JSONObject json) {
		final int x = Integer.parseInt(json.get("x").toString());
		final int y = Integer.parseInt(json.get("y").toString());
		final int balasCargador = Integer.parseInt(json.get("balasCargador").toString());

		return new RifleAsalto(x, y, balasCargador);
	}
}