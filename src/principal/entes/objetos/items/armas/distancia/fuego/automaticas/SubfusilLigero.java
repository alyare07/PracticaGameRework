package principal.entes.objetos.items.armas.distancia.fuego.automaticas;

import org.json.simple.JSONObject;

import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.municiones.CajaMunicion;

public class SubfusilLigero extends ArmaAutomatica {

	private static final long serialVersionUID = 881920391209381L;

	public SubfusilLigero() {
		super(ArmaAutomatica.COD_SUBFUSIL, 4, 220, false, 30, 1400, 110, CajaMunicion.COD_9MM, 4.5, 4.5);
	}

	public SubfusilLigero(final int x, final int y) {
		super(x, y, ArmaAutomatica.COD_SUBFUSIL, 4, 220, false, 30, 1400, 110, CajaMunicion.COD_9MM, 4.5, 4.5);
	}

	public SubfusilLigero(final int x, final int y, final int balasCargador) {
		super(x, y, ArmaAutomatica.COD_SUBFUSIL, 4, 220, false, 30, 1400, 110, CajaMunicion.COD_9MM, 4.5, 4.5);
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