package principal.entes.objetos.items.municiones;

import org.json.simple.JSONObject;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Jugador;
import principal.entes.modelos.item.ListaModelosItem;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.Consumible;
import principal.entes.objetos.items.armas.Arma;

/**
 * Representa una caja de munición recolectable en el mundo o apilable en la
 * mochila del jugador. Compatible con pistolas, escopetas, subfusiles, rifles y
 * ametralladoras.
 * 
 * @version 1.0 (Java 8 Compatible - Zero-GC Architecture)
 */
public class CajaMunicion extends Consumible {

	private static final long serialVersionUID = 81920391203912093L;

	public CajaMunicion(final int x, final int y, final int cantidad, final String codModelo) {
		super(x, y, cantidad, codModelo);
	}

	public CajaMunicion(final int cantidad, final String codModelo) {
		super(cantidad, codModelo);
	}

	// =========================================================================
	// === FÁBRICAS CONVENIENTES PARA SPAWN
	// =========================================================================

	public static CajaMunicion crear9mm(final int x, final int y, final int cantidad) {
		return new CajaMunicion(x, y, cantidad, ListaModelosItem.COD_CONSUMIBLE_MUNICION_PISTOLA);
	}

	public static CajaMunicion crearCartuchos12(final int x, final int y, final int cantidad) {
		return new CajaMunicion(x, y, cantidad, ListaModelosItem.COD_CONSUMIBLE_MUNICION_ESCOPETA);
	}

	public static CajaMunicion crear762mm(final int x, final int y, final int cantidad) {
		return new CajaMunicion(x, y, cantidad, ListaModelosItem.COD_CONSUMIBLE_MUNICION_FUSIL);
	}

	public static CajaMunicion crearPesada(final int x, final int y, final int cantidad) {
		return new CajaMunicion(x, y, cantidad, ListaModelosItem.COD_CONSUMIBLE_MUNICION_PESADA);
	}

	// =========================================================================
	// === INTERACCIÓN Y CONSUMO
	// =========================================================================

	/**
	 * Acción rápida al hacer clic derecho en inventario: intenta recargar el arma
	 * equipada si es compatible.
	 */
	@Override
	public void consumir(final Criatura c) {
		if (c instanceof Jugador) {
			final Jugador j = (Jugador) c;
			final Arma arma = j.getArmaEquipada();

			if ((arma != null) && arma.esArmaDistancia()
					&& this.getCodigoModelo().equals(arma.getTipoMunicionRequerida())) {
				arma.iniciarRecarga(j);
			}
		}
	}

	@Override
	public Objeto copiar() {
		return new CajaMunicion(this.getPosicionXInt(), this.getPosicionYInt(), this.getCantidad(),
				this.getCodigoModelo());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JSONObject exportarParaJSON() {
		final JSONObject json = new JSONObject();
		json.put("x", this.getPosicionXInt());
		json.put("y", this.getPosicionYInt());
		json.put("codModelo", this.getCodigoModelo());
		json.put("cantidad", this.getCantidad());
		return json;
	}

	public static CajaMunicion crearDesdeJson(final JSONObject json) {
		if (json == null) {
			return null;
		}

		final int x = (json.get("x") != null) ? ((Number) json.get("x")).intValue() : 0;
		final int y = (json.get("y") != null) ? ((Number) json.get("y")).intValue() : 0;
		final String codModelo = (json.get("codModelo") != null) ? json.get("codModelo").toString()
				: ListaModelosItem.COD_CONSUMIBLE_MUNICION_PISTOLA;
		final int cantidad = (json.get("cantidad") != null) ? ((Number) json.get("cantidad")).intValue() : 30;

		return new CajaMunicion(x, y, cantidad, codModelo);
	}

	@Override
	public String exportarTipoItem() {
		return "CajaMunicion";
	}
}