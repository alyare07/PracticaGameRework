package principal.entes.objetos.items.armas.distancia.fuego.automaticas;

import org.json.simple.JSONObject;

import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.municiones.CajaMunicion;

public class AmetralladoraPesada extends ArmaAutomatica {

	private static final long serialVersionUID = 1081920391209381L;

	public AmetralladoraPesada() {
		super(ArmaAutomatica.COD_AMETRALLADORA, 7, 360, true, 100, 3500, 120, CajaMunicion.COD_PESADA, 5.5, 5.0);
	}

	public AmetralladoraPesada(final int x, final int y) {
		super(x, y, ArmaAutomatica.COD_AMETRALLADORA, 7, 360, true, 100, 3500, 120, CajaMunicion.COD_PESADA, 5.5, 5.0);
	}

	public AmetralladoraPesada(final int x, final int y, final int balasCargador) {
		super(x, y, ArmaAutomatica.COD_AMETRALLADORA, 7, 360, true, 100, 3500, 120, CajaMunicion.COD_PESADA, 5.5, 5.0);
		this.balasCargador = Math.max(0, Math.min(this.capacidadCargador, balasCargador));
	}

	@Override
	public Objeto copiar() {
		return new AmetralladoraPesada(this.getPosicionXInt(), this.getPosicionYInt(), this.balasCargador);
	}

	@Override
	public String exportarTipoItem() {
		return "AmetralladoraPesada";
	}

	public static AmetralladoraPesada crearDesdeJson(final JSONObject json) {
		final int x = Integer.parseInt(json.get("x").toString());
		final int y = Integer.parseInt(json.get("y").toString());
		final int balasCargador = Integer.parseInt(json.get("balasCargador").toString());

		return new AmetralladoraPesada(x, y, balasCargador);
	}
}