package principal.entes.objetos.items.armas.distancia.fuego.escopetas;

import org.json.simple.JSONObject;

import principal.entes.objetos.Objeto;

public class EscopetaTactica extends Escopeta {

	private static final long serialVersionUID = 591829301923812L;

	public EscopetaTactica() {
		super(Escopeta.COD_TACTICA, 5, 200, false, 8, 2400, 750, 8, 16.0, 4.8);
	}

	public EscopetaTactica(final int x, final int y) {
		super(x, y, Escopeta.COD_TACTICA, 5, 200, false, 8, 2400, 750, 8, 16.0, 4.8);
	}

	public EscopetaTactica(final int x, final int y, final int balasCargador) {
		super(x, y, Escopeta.COD_TACTICA, 5, 200, false, 8, 2400, 750, 8, 16.0, 4.8);
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