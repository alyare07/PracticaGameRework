package principal.entes.objetos.items.herramientas;

import java.util.ArrayList;

import org.json.simple.JSONObject;

import principal.entes.criaturas.jugador.Jugador;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.armas.Arma;
import principal.igu.textos.TipoTextoFlotante;
import principal.utilidades.Globales;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

public class Herramienta extends Arma {

	private static final long serialVersionUID = 1L;

	public static final String COD_HACHA = "Hacha de Tala";
	public static final String COD_PICO = "Pico de Minería";

	public static final int DURABILIDAD_DEFECTO_HACHA = 80;
	public static final int DURABILIDAD_DEFECTO_PICO = 100;

	protected final TipoHerramienta tipoHerramienta;
	protected final double potenciaCosecha;

	protected int durabilidadMaxima;
	protected int durabilidadActual;

	public Herramienta(final String codModelo, final int damageCombate, final int alcance, final int cadenciaMs,
			final TipoHerramienta tipoHerramienta, final double potenciaCosecha, final int durabilidadMax,
			final int durabilidadActual) {
		super(codModelo, damageCombate, alcance, false);
		this.cadenciaMs = cadenciaMs;
		this.tipoHerramienta = (tipoHerramienta != null) ? tipoHerramienta : TipoHerramienta.HACHA;
		this.potenciaCosecha = Math.max(1.0, potenciaCosecha);
		this.durabilidadMaxima = Math.max(1, durabilidadMax);
		this.durabilidadActual = Math.max(0, Math.min(this.durabilidadMaxima, durabilidadActual));
		this.rellenarInfo(this.LISTA_INFO);
	}

	public Herramienta(final String codModelo, final int damageCombate, final int alcance, final int cadenciaMs,
			final TipoHerramienta tipoHerramienta, final double potenciaCosecha) {
		this(codModelo, damageCombate, alcance, cadenciaMs, tipoHerramienta, potenciaCosecha,
				tipoHerramienta == TipoHerramienta.PICO ? DURABILIDAD_DEFECTO_PICO : DURABILIDAD_DEFECTO_HACHA,
				tipoHerramienta == TipoHerramienta.PICO ? DURABILIDAD_DEFECTO_PICO : DURABILIDAD_DEFECTO_HACHA);
	}

	public Herramienta(final int x, final int y, final String codModelo, final int damageCombate, final int alcance,
			final int cadenciaMs, final TipoHerramienta tipoHerramienta, final double potenciaCosecha,
			final int durabilidadMax, final int durabilidadActual) {
		super(x, y, codModelo, damageCombate, alcance, false);
		this.cadenciaMs = cadenciaMs;
		this.tipoHerramienta = (tipoHerramienta != null) ? tipoHerramienta : TipoHerramienta.HACHA;
		this.potenciaCosecha = Math.max(1.0, potenciaCosecha);
		this.durabilidadMaxima = Math.max(1, durabilidadMax);
		this.durabilidadActual = Math.max(0, Math.min(this.durabilidadMaxima, durabilidadActual));
		this.rellenarInfo(this.LISTA_INFO);
	}

	public Herramienta(final int x, final int y, final String codModelo, final int damageCombate, final int alcance,
			final int cadenciaMs, final TipoHerramienta tipoHerramienta, final double potenciaCosecha) {
		this(x, y, codModelo, damageCombate, alcance, cadenciaMs, tipoHerramienta, potenciaCosecha,
				tipoHerramienta == TipoHerramienta.PICO ? DURABILIDAD_DEFECTO_PICO : DURABILIDAD_DEFECTO_HACHA,
				tipoHerramienta == TipoHerramienta.PICO ? DURABILIDAD_DEFECTO_PICO : DURABILIDAD_DEFECTO_HACHA);
	}

	/**
	 * Reduce la durabilidad tras conectar un impacto contra un recurso cosechable.
	 * Si se destruye, la elimina de la mano del portador.
	 */
	public void desgastar(final int puntos, final Jugador portador) {
		if (this.durabilidadActual <= 0) {
			return;
		}

		this.durabilidadActual = Math.max(0, this.durabilidadActual - puntos);

		if (this.durabilidadActual == 0) {
			GestorSonido.reproducir(IDSonido.GOLPE_1);

			if (portador != null) {
				Globales.GESTOR_TEXTOS.agregarTextoFijo("¡" + this.nombre + " destruida!", portador.getCentroX(),
						portador.getPosicionYInt() - 10, TipoTextoFlotante.FALLO);

				// Purga del slot de equipamiento activo
				if ((Globales.GESTOR_INVENTARIO != null)
						&& (Globales.GESTOR_INVENTARIO.getInventarioJugador() != null)) {
					Globales.GESTOR_INVENTARIO.getInventarioJugador().getSlotManager().getSlotArma().eliminarObjeto();
				}
			}
			this.eliminar();
		}
	}

	public int getDurabilidadActual() {
		return this.durabilidadActual;
	}

	public int getDurabilidadMaxima() {
		return this.durabilidadMaxima;
	}

	public double getRatioDurabilidad() {
		return (double) this.durabilidadActual / this.durabilidadMaxima;
	}

	public TipoHerramienta getTipoHerramienta() {
		return this.tipoHerramienta;
	}

	public double getPotenciaCosecha() {
		return this.potenciaCosecha;
	}

	@Override
	public Objeto copiar() {
		return new Herramienta(this.getPosicionXInt(), this.getPosicionYInt(), this.codigoModelo, this.damage,
				this.alcance, this.cadenciaMs, this.tipoHerramienta, this.potenciaCosecha, this.durabilidadMaxima,
				this.durabilidadActual);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JSONObject exportarParaJSON() {
		final JSONObject json = new JSONObject();
		json.put("x", Integer.valueOf(this.getPosicionXInt()));
		json.put("y", Integer.valueOf(this.getPosicionYInt()));
		json.put("codModelo", this.codigoModelo);
		json.put("damage", Integer.valueOf(this.damage));
		json.put("alcance", Integer.valueOf(this.alcance));
		json.put("cadencia", Integer.valueOf(this.cadenciaMs));
		json.put("tipoHerramienta", this.tipoHerramienta.name());
		json.put("potencia", Double.valueOf(this.potenciaCosecha));
		json.put("durabilidadMax", Integer.valueOf(this.durabilidadMaxima));
		json.put("durabilidad", Integer.valueOf(this.durabilidadActual));
		return json;
	}

	public static Herramienta crearDesdeJson(final JSONObject json) {
		if (json == null) {
			return new Herramienta(COD_HACHA, 8, 14, 350, TipoHerramienta.HACHA, 35.0);
		}

		final int x = (json.get("x") != null) ? ((Number) json.get("x")).intValue() : 0;
		final int y = (json.get("y") != null) ? ((Number) json.get("y")).intValue() : 0;
		final String codModelo = (json.get("codModelo") != null) ? json.get("codModelo").toString() : COD_HACHA;
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

		final int maxDur = (json.get("durabilidadMax") != null) ? ((Number) json.get("durabilidadMax")).intValue()
				: (tipoH == TipoHerramienta.PICO ? DURABILIDAD_DEFECTO_PICO : DURABILIDAD_DEFECTO_HACHA);

		final int actDur = (json.get("durabilidad") != null) ? ((Number) json.get("durabilidad")).intValue() : maxDur;

		return new Herramienta(x, y, codModelo, damage, alcance, cadencia, tipoH, potencia, maxDur, actDur);
	}

	@Override
	protected void rellenarInfo(final ArrayList<String> listaInfo) {
		listaInfo.clear();
		listaInfo.add("Tipo: " + this.tipoHerramienta.getNombre());
		listaInfo.add("Durabilidad: " + this.durabilidadActual + "/" + this.durabilidadMaxima);
		listaInfo.add("Potencia de Cosecha: " + (int) this.potenciaCosecha + " pts.");
		listaInfo.add("Daño en Combate: " + this.damage + " pts.");
		listaInfo.add("Cadencia: " + this.cadenciaMs + " ms.");
	}

	@Override
	public String exportarTipoItem() {
		return "Herramienta";
	}
}