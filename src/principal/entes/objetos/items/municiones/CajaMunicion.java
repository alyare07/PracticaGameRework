package principal.entes.objetos.items.municiones;

import org.json.simple.JSONObject;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Jugador;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.Consumible;
import principal.entes.objetos.items.armas.Arma;
import principal.recursos.TexturaItem;

public class CajaMunicion extends Consumible {

	private static final long serialVersionUID = 81920391203912093L;

	public static final String COD_9MM = "Caja Municion 9mm";
	public static final String COD_12CAL = "Caja Cartuchos Calibre 12";
	public static final String COD_762MM = "Caja Municion 7.62mm";
	public static final String COD_PESADA = "Caja Municion Pesada";

	public CajaMunicion(final int x, final int y, final int cantidad, final String codModelo) {
		super(x, y, cantidad, codModelo, codModelo, TexturaItem.CAJA_MUNICION_INV, TexturaItem.CAJA_MUNICION_MAPA,
				resolverLimiteMunicion(codModelo));
	}

	public CajaMunicion(final int cantidad, final String codModelo) {
		this(0, 0, cantidad, codModelo);
	}

	public static CajaMunicion crear9mm(final int x, final int y, final int cantidad) {
		return new CajaMunicion(x, y, cantidad, COD_9MM);
	}

	public static CajaMunicion crearCartuchos12(final int x, final int y, final int cantidad) {
		return new CajaMunicion(x, y, cantidad, COD_12CAL);
	}

	public static CajaMunicion crear762mm(final int x, final int y, final int cantidad) {
		return new CajaMunicion(x, y, cantidad, COD_762MM);
	}

	public static CajaMunicion crearPesada(final int x, final int y, final int cantidad) {
		return new CajaMunicion(x, y, cantidad, COD_PESADA);
	}

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
		json.put("x", Integer.valueOf(this.getPosicionXInt()));
		json.put("y", Integer.valueOf(this.getPosicionYInt()));
		json.put("codModelo", this.getCodigoModelo());
		json.put("cantidad", Integer.valueOf(this.getCantidad()));
		return json;
	}

	public static CajaMunicion crearDesdeJson(final JSONObject json) {
		if (json == null) {
			return null;
		}

		final int x = (json.get("x") != null) ? ((Number) json.get("x")).intValue() : 0;
		final int y = (json.get("y") != null) ? ((Number) json.get("y")).intValue() : 0;
		final String codModelo = (json.get("codModelo") != null) ? json.get("codModelo").toString() : COD_9MM;
		final int cantidad = (json.get("cantidad") != null) ? ((Number) json.get("cantidad")).intValue() : 30;

		return new CajaMunicion(x, y, cantidad, codModelo);
	}

	@Override
	public String exportarTipoItem() {
		return "CajaMunicion";
	}

	private static int resolverLimiteMunicion(final String cod) {
		if (COD_12CAL.equals(cod)) {
			return 64;
		}
		if (COD_762MM.equals(cod)) {
			return 180;
		}
		if (COD_PESADA.equals(cod)) {
			return 300;
		}
		return 150; // 9mm por defecto
	}
}