package principal.entes.objetos.items.equipamiento;

import java.util.ArrayList;

import org.json.simple.JSONObject;

import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.Portable;
import principal.recursos.ClaveHoja;

/**
 * Representa una pieza de equipamiento equipable con atributos RPG, precio de
 * mercado y resistencia térmica clasificada (Zero-GC / O(1)).
 * 
 * @version 4.0 (Vanilla Java 8 - Classified Thermal Insulation)
 */
public class PiezaEquipo extends Portable {

	private static final long serialVersionUID = 1L;

	public static final String COD_CASCO_BASE = "Casco Ligero";
	public static final String COD_ARMADURA_BASE = "Armadura Ligera";
	public static final String COD_BOTAS_CUERO = "Botas Cuero";
	public static final String COD_ANILLO_ORO = "Anillo de Oro";
	public static final String COD_ANILLO_PLATA = "Anillo de Plata";

	protected final TipoEquipo tipoEquipo;
	protected final int bonifFuerza;
	protected final int bonifAgilidad;
	protected final int bonifInteligencia;
	protected final int armaduraDefensa;
	protected final TipoAislamiento tipoAislamiento;
	protected final int valorAislamiento;
	protected ClaveHoja hojaEquipado;

	public PiezaEquipo(final String codModelo, final TipoEquipo tipoEquipo, final int bonifFuerza,
			final int bonifAgilidad, final int bonifInteligencia, final int armaduraDefensa,
			final TipoAislamiento tipoAislamiento, final int valorAislamiento) {
		super(codModelo);
		this.tipoEquipo = (tipoEquipo != null) ? tipoEquipo : TipoEquipo.CASCO;
		this.bonifFuerza = bonifFuerza;
		this.bonifAgilidad = bonifAgilidad;
		this.bonifInteligencia = bonifInteligencia;
		this.armaduraDefensa = armaduraDefensa;
		this.tipoAislamiento = (tipoAislamiento != null) ? tipoAislamiento : TipoAislamiento.NINGUNO;
		this.valorAislamiento = Math.max(0, valorAislamiento);
		this.asignarPrecioPorDefecto();
		this.rellenarInfo(this.LISTA_INFO);

		if (this.tipoEquipo == TipoEquipo.CASCO) {
			this.hojaEquipado = ClaveHoja.EQUIPADO_CASCO1;
		} else if (this.tipoEquipo == TipoEquipo.TORSO) {
			this.hojaEquipado = ClaveHoja.EQUIPADO_PETO1;
		} else {
			this.hojaEquipado = null;
		}
	}

	public PiezaEquipo(final int x, final int y, final String codModelo, final TipoEquipo tipoEquipo,
			final int bonifFuerza, final int bonifAgilidad, final int bonifInteligencia, final int armaduraDefensa,
			final TipoAislamiento tipoAislamiento, final int valorAislamiento) {
		super(x, y, codModelo);
		this.tipoEquipo = (tipoEquipo != null) ? tipoEquipo : TipoEquipo.CASCO;
		this.bonifFuerza = bonifFuerza;
		this.bonifAgilidad = bonifAgilidad;
		this.bonifInteligencia = bonifInteligencia;
		this.armaduraDefensa = armaduraDefensa;
		this.tipoAislamiento = (tipoAislamiento != null) ? tipoAislamiento : TipoAislamiento.NINGUNO;
		this.valorAislamiento = Math.max(0, valorAislamiento);
		this.asignarPrecioPorDefecto();
		this.rellenarInfo(this.LISTA_INFO);

		if (this.tipoEquipo == TipoEquipo.CASCO) {
			this.hojaEquipado = ClaveHoja.EQUIPADO_CASCO1;
		} else if (this.tipoEquipo == TipoEquipo.TORSO) {
			this.hojaEquipado = ClaveHoja.EQUIPADO_PETO1;
		} else {
			this.hojaEquipado = null;
		}
	}

	public PiezaEquipo(final String codModelo, final TipoEquipo tipoEquipo, final int bonifFuerza,
			final int bonifAgilidad, final int bonifInteligencia, final int armaduraDefensa) {
		this(codModelo, tipoEquipo, bonifFuerza, bonifAgilidad, bonifInteligencia, armaduraDefensa,
				TipoAislamiento.NINGUNO, 0);
	}

	public PiezaEquipo(final int x, final int y, final String codModelo, final TipoEquipo tipoEquipo,
			final int bonifFuerza, final int bonifAgilidad, final int bonifInteligencia, final int armaduraDefensa) {
		this(x, y, codModelo, tipoEquipo, bonifFuerza, bonifAgilidad, bonifInteligencia, armaduraDefensa,
				TipoAislamiento.NINGUNO, 0);
	}

	private void asignarPrecioPorDefecto() {
		if (this.codigoModelo.equals(COD_ANILLO_ORO)) {
			this.precioBasePlata = 800L;
		} else if (this.codigoModelo.equals(COD_ANILLO_PLATA)) {
			this.precioBasePlata = 300L;
		} else if (this.tipoEquipo == TipoEquipo.TORSO) {
			this.precioBasePlata = 350L;
		} else if (this.tipoEquipo == TipoEquipo.CASCO) {
			this.precioBasePlata = 200L;
		} else if (this.tipoEquipo == TipoEquipo.BOTAS) {
			this.precioBasePlata = 150L;
		} else {
			this.precioBasePlata = 100L;
		}
	}

	public ClaveHoja getHojaEquipado() {
		return this.hojaEquipado;
	}

	public void setHojaEquipado(final ClaveHoja hoja) {
		this.hojaEquipado = hoja;
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

	public TipoAislamiento getTipoAislamiento() {
		return this.tipoAislamiento;
	}

	public int getValorAislamiento() {
		return this.valorAislamiento;
	}

	@Override
	public ArrayList<String> getInfo() {
		this.LISTA_INFO.clear();
		this.rellenarInfo(this.LISTA_INFO);
		return this.LISTA_INFO;
	}

	@Override
	protected void rellenarInfo(final ArrayList<String> listaInfo) {
		listaInfo.clear();
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

		if ((this.tipoAislamiento != TipoAislamiento.NINGUNO) && (this.valorAislamiento > 0)) {
			switch (this.tipoAislamiento) {
			case FRIO:
				listaInfo.add("Aislamiento Frío: +" + this.valorAislamiento + " °C");
				break;
			case CALOR:
				listaInfo.add("Aislamiento Calor: +" + this.valorAislamiento + " °C");
				break;
			case UNIVERSAL:
				listaInfo.add("Aislamiento Térmico: +" + this.valorAislamiento + " °C");
				break;
			default:
				break;
			}
		}
	}

	@Override
	public Objeto copiar() {
		final PiezaEquipo p = new PiezaEquipo(this.getPosicionXInt(), this.getPosicionYInt(), this.codigoModelo,
				this.tipoEquipo, this.bonifFuerza, this.bonifAgilidad, this.bonifInteligencia, this.armaduraDefensa,
				this.tipoAislamiento, this.valorAislamiento);
		p.setPrecioBasePlata(this.precioBasePlata);
		return p;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JSONObject exportarParaJSON() {
		final JSONObject json = new JSONObject();
		json.put("x", Integer.valueOf(this.getPosicionXInt()));
		json.put("y", Integer.valueOf(this.getPosicionYInt()));
		json.put("codModelo", this.codigoModelo);
		json.put("tipoEquipo", this.tipoEquipo.name());
		json.put("fuerza", Integer.valueOf(this.bonifFuerza));
		json.put("agilidad", Integer.valueOf(this.bonifAgilidad));
		json.put("inteligencia", Integer.valueOf(this.bonifInteligencia));
		json.put("defensa", Integer.valueOf(this.armaduraDefensa));
		json.put("tipoAislamiento", this.tipoAislamiento.name());
		json.put("valorAislamiento", Integer.valueOf(this.valorAislamiento));
		json.put("precio", Long.valueOf(this.precioBasePlata));
		return json;
	}

	public static PiezaEquipo crearDesdeJson(final JSONObject json) {
		if (json == null) {
			return null;
		}

		final int x = (json.get("x") != null) ? ((Number) json.get("x")).intValue() : 0;
		final int y = (json.get("y") != null) ? ((Number) json.get("y")).intValue() : 0;
		final String codModelo = (json.get("codModelo") != null) ? json.get("codModelo").toString() : COD_CASCO_BASE;
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

		final String aislaStr = (json.get("tipoAislamiento") != null) ? json.get("tipoAislamiento").toString()
				: "NINGUNO";
		TipoAislamiento tipoAisla = TipoAislamiento.NINGUNO;
		try {
			tipoAisla = TipoAislamiento.valueOf(aislaStr);
		} catch (final Exception ignored) {
		}

		final int valorAisla = (json.get("valorAislamiento") != null)
				? ((Number) json.get("valorAislamiento")).intValue()
				: 0;

		final PiezaEquipo pieza = new PiezaEquipo(x, y, codModelo, tipo, f, a, i, def, tipoAisla, valorAisla);
		if (json.get("precio") != null) {
			pieza.setPrecioBasePlata(((Number) json.get("precio")).longValue());
		}
		return pieza;
	}

	@Override
	public String exportarTipoItem() {
		return "PiezaEquipo";
	}
}