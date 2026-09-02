package principal.entes.objetos.items.equipamiento;

import java.util.ArrayList;

import org.json.simple.JSONObject;

import principal.entes.modelos.item.ListaModelosItem;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.Portable;

/**
 * Representa cualquier pieza de equipo armable (Casco, Torso, Botas, Anillo)
 * con bonificadores de atributos RPG.
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class PiezaEquipo extends Portable {

	private static final long serialVersionUID = 1L;

	protected final TipoEquipo tipoEquipo;
	protected final int bonifFuerza;
	protected final int bonifAgilidad;
	protected final int bonifInteligencia;
	protected final int armaduraDefensa;

	public PiezaEquipo(final String codModelo, final TipoEquipo tipoEquipo, final int bonifFuerza,
			final int bonifAgilidad, final int bonifInteligencia, final int armaduraDefensa) {
		super(codModelo);
		this.tipoEquipo = (tipoEquipo != null) ? tipoEquipo : TipoEquipo.CASCO;
		this.bonifFuerza = bonifFuerza;
		this.bonifAgilidad = bonifAgilidad;
		this.bonifInteligencia = bonifInteligencia;
		this.armaduraDefensa = armaduraDefensa;
		this.rellenarInfo(this.LISTA_INFO);
	}

	public PiezaEquipo(final int x, final int y, final String codModelo, final TipoEquipo tipoEquipo,
			final int bonifFuerza, final int bonifAgilidad, final int bonifInteligencia, final int armaduraDefensa) {
		super(x, y, codModelo);
		this.tipoEquipo = (tipoEquipo != null) ? tipoEquipo : TipoEquipo.CASCO;
		this.bonifFuerza = bonifFuerza;
		this.bonifAgilidad = bonifAgilidad;
		this.bonifInteligencia = bonifInteligencia;
		this.armaduraDefensa = armaduraDefensa;
		this.rellenarInfo(this.LISTA_INFO);
	}

	public TipoEquipo getTipoEquipo() {
		return this.tipoEquipo;
	}

	public int getBonifFuerza() {
		return this.bonifFuerza;
	}

	public int getBonifAgilidad() {
		return this.bonifAgilidad;
	}

	public int getBonifInteligencia() {
		return this.bonifInteligencia;
	}

	public int getArmaduraDefensa() {
		return this.armaduraDefensa;
	}

	@Override
	public ArrayList<String> getInfo() {
		this.LISTA_INFO.clear();
		this.rellenarInfo(this.LISTA_INFO);
		return this.LISTA_INFO;
	}

	@Override
	protected void rellenarInfo(final ArrayList<String> listaInfo) {
		listaInfo.add("Tipo: " + this.tipoEquipo.getNombreVisible());
		if (this.armaduraDefensa > 0) {
			listaInfo.add("Defensa: +" + this.armaduraDefensa + " pts.");
		}
		if (this.bonifFuerza > 0) {
			listaInfo.add("Fuerza: +" + this.bonifFuerza);
		}
		if (this.bonifAgilidad > 0) {
			listaInfo.add("Agilidad: +" + this.bonifAgilidad);
		}
		if (this.bonifInteligencia > 0) {
			listaInfo.add("Inteligencia: +" + this.bonifInteligencia);
		}
	}

	@Override
	public Objeto copiar() {
		return new PiezaEquipo(this.getPosicionXInt(), this.getPosicionYInt(), this.CODIGO_MODELO, this.tipoEquipo,
				this.bonifFuerza, this.bonifAgilidad, this.bonifInteligencia, this.armaduraDefensa);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JSONObject exportarParaJSON() {
		final JSONObject json = new JSONObject();
		json.put("x", this.getPosicionXInt());
		json.put("y", this.getPosicionYInt());
		json.put("codModelo", this.CODIGO_MODELO);
		json.put("tipoEquipo", this.tipoEquipo.name());
		json.put("fuerza", this.bonifFuerza);
		json.put("agilidad", this.bonifAgilidad);
		json.put("inteligencia", this.bonifInteligencia);
		json.put("defensa", this.armaduraDefensa);
		return json;
	}

	public static PiezaEquipo crearDesdeJson(final JSONObject json) {
		if (json == null) {
			return null;
		}

		final int x = (json.get("x") != null) ? ((Number) json.get("x")).intValue() : 0;
		final int y = (json.get("y") != null) ? ((Number) json.get("y")).intValue() : 0;
		final String codModelo = (json.get("codModelo") != null) ? json.get("codModelo").toString() : "";
		final String tipoStr = (json.get("tipoEquipo") != null) ? json.get("tipoEquipo").toString() : "CASCO";

		TipoEquipo tipo = TipoEquipo.CASCO;
		try {
			tipo = TipoEquipo.valueOf(tipoStr);
		} catch (final Exception ignored) {
		}

		final int f = (json.get("fuerza") != null) ? ((Number) json.get("fuerza")).intValue() : 0;
		final int a = (json.get("agilidad") != null) ? ((Number) json.get("agilidad")).intValue() : 0;
		final int i = (json.get("inteligencia") != null) ? ((Number) json.get("inteligencia")).intValue() : 0;
		final int def = (json.get("defensa") != null) ? ((Number) json.get("defensa")).intValue() : 0;

		return new PiezaEquipo(x, y, codModelo, tipo, f, a, i, def);
	}

	@Override
	public String exportarTipoItem() {
		return "PiezaEquipo";
	}
}