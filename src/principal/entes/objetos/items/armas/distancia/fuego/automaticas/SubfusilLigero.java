package principal.entes.objetos.items.armas.distancia.fuego.automaticas;

import org.json.simple.JSONObject;

import principal.entes.modelos.item.ListaModelosItem;
import principal.entes.objetos.Objeto;

/**
 * Subfusil automático compacto de 9mm (Tier 1). Cadencia ultra-rápida de 110
 * ms, cargador de 30 balas y recarga ágil de 1.4 s.
 * 
 * @version 2.0 (Java 8 Compatible - Zero-GC Architecture)
 */
public class SubfusilLigero extends ArmaAutomatica {

	private static final long serialVersionUID = 881920391209381L;

	public SubfusilLigero() {
		super(ListaModelosItem.COD_ARMA_SUBFUSIL_LIGERO, 4, 220, false, 30, 1400, 110,
				ListaModelosItem.COD_CONSUMIBLE_MUNICION_PISTOLA, 4.5, 4.5);
	}

	public SubfusilLigero(final int x, final int y) {
		super(x, y, ListaModelosItem.COD_ARMA_SUBFUSIL_LIGERO, 4, 220, false, 30, 1400, 110,
				ListaModelosItem.COD_CONSUMIBLE_MUNICION_PISTOLA, 4.5, 4.5);
	}

	public SubfusilLigero(final int x, final int y, final int balasCargador) {
		super(x, y, ListaModelosItem.COD_ARMA_SUBFUSIL_LIGERO, 4, 220, false, 30, 1400, 110,
				ListaModelosItem.COD_CONSUMIBLE_MUNICION_PISTOLA, 4.5, 4.5);
		this.balasCargador = Math.max(0, Math.min(this.capacidadCargador, balasCargador));
	}

	@Override
	public Objeto copiar() {
		return new SubfusilLigero(this.getPosicionXInt(), this.getPosicionYInt(), this.balasCargador);
	}

	@Override
	public String exportarTipoItem() {
		return "SubfusilLigero";
	}

	public static SubfusilLigero crearDesdeJson(final JSONObject json) {
		final int x = Integer.parseInt(json.get("x").toString());
		final int y = Integer.parseInt(json.get("y").toString());
		final int balasCargador = Integer.parseInt(json.get("balasCargador").toString());

		return new SubfusilLigero(x, y, balasCargador);
	}
}