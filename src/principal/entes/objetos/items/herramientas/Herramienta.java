package principal.entes.objetos.items.herramientas;

import java.util.ArrayList;

import org.json.simple.JSONObject;

import principal.entes.modelos.item.ListaModelosItem;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.armas.Arma;

public class Herramienta extends Arma {

	private static final long serialVersionUID = 1L;

	protected final TipoHerramienta tipoHerramienta;
	protected final double potenciaCosecha;

	public Herramienta(final String codModelo, final int damageCombate, final int alcance, final int cadenciaMs,
			final TipoHerramienta tipoHerramienta, final double potenciaCosecha) {
		super(codModelo, damageCombate, alcance, false);
		this.cadenciaMs = cadenciaMs;
		this.tipoHerramienta = (tipoHerramienta != null) ? tipoHerramienta : TipoHerramienta.HACHA;
		this.potenciaCosecha = Math.max(1.0, potenciaCosecha);
		this.rellenarInfo(this.LISTA_INFO);
	}

	public Herramienta(final int x, final int y, final String codModelo, final int damageCombate, final int alcance,
			final int cadenciaMs, final TipoHerramienta tipoHerramienta, final double potenciaCosecha) {
		super(x, y, codModelo, damageCombate, alcance, false);
		this.cadenciaMs = cadenciaMs;
		this.tipoHerramienta = (tipoHerramienta != null) ? tipoHerramienta : TipoHerramienta.HACHA;
		this.potenciaCosecha = Math.max(1.0, potenciaCosecha);
		this.rellenarInfo(this.LISTA_INFO);
	}

	public TipoHerramienta getTipoHerramienta() {
		return this.tipoHerramienta;
	}

	public double getPotenciaCosecha() {
		return this.potenciaCosecha;
	}

	@Override
	public Objeto copiar() {
		return new Herramienta(this.getPosicionXInt(), this.getPosicionYInt(), this.CODIGO_MODELO, this.damage,
				this.alcance, this.cadenciaMs, this.tipoHerramienta, this.potenciaCosecha);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JSONObject exportarParaJSON() {
		final JSONObject json = new JSONObject();
		json.put("x", this.getPosicionXInt());
		json.put("y", this.getPosicionYInt());
		json.put("codModelo", this.CODIGO_MODELO);
		json.put("damage", this.damage);
		json.put("alcance", this.alcance);
		json.put("cadencia", this.cadenciaMs);
		json.put("tipoHerramienta", this.tipoHerramienta.name());
		json.put("potencia", this.potenciaCosecha);
		return json;
	}

	public static Herramienta crearDesdeJson(final JSONObject json) {
		if (json == null) {
			return new Herramienta(ListaModelosItem.COD_HERRAMIENTA_HACHA, 8, 14, 350, TipoHerramienta.HACHA, 35.0);
		}

		final int x = (json.get("x") != null) ? ((Number) json.get("x")).intValue() : 0;
		final int y = (json.get("y") != null) ? ((Number) json.get("y")).intValue() : 0;
		final String codModelo = (json.get("codModelo") != null) ? json.get("codModelo").toString()
				: ListaModelosItem.COD_HERRAMIENTA_HACHA;
		final int damage = (json.get("damage") != null) ? ((Number) json.get("damage")).intValue() : 8;
		final int alcance = (json.get("alcance") != null) ? ((Number) json.get("alcance")).intValue() : 14;
		final int cadencia = (json.get("cadencia") != null) ? ((Number) json.get("cadencia")).intValue() : 350;
		final double potencia = (json.get("potencia") != null) ? ((Number) json.get("potencia")).doubleValue() : 35.0;

		TipoHerramienta tipoH = TipoHerramienta.HACHA;
		if (json.get("tipoHerramienta") != null) {
			try {
				tipoH = TipoHerramienta.valueOf(json.get("tipoHerramienta").toString());
			} catch (final Exception ignored) {
			}
		}

		return new Herramienta(x, y, codModelo, damage, alcance, cadencia, tipoH, potencia);
	}

	@Override
	protected void rellenarInfo(final ArrayList<String> listaInfo) {
		listaInfo.clear();
		listaInfo.add("Tipo: " + this.tipoHerramienta.getNombre());
		listaInfo.add("Potencia de Cosecha: " + (int) this.potenciaCosecha + " pts.");
		listaInfo.add("Daño en Combate: " + this.damage + " pts.");
		listaInfo.add("Cadencia: " + this.cadenciaMs + " ms.");
	}

	@Override
	public String exportarTipoItem() {
		return "Herramienta";
	}
}