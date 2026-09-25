package principal.persistencia.json.adaptadores;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import principal.entes.objetos.ArbolCofre;
import principal.entes.objetos.cofres.Cofre;
import principal.entes.objetos.cofres.CofreMediano;
import principal.entes.objetos.cofres.CofrePequeño;
import principal.entes.objetos.items.Item;
import principal.entes.objetos.recursos.ArbolCosechable;
import principal.entes.objetos.recursos.ArbustoCosechable;
import principal.entes.objetos.recursos.arboles.TipoArbol;
import principal.entes.objetos.recursos.minerales.MineralCarbon;
import principal.entes.objetos.recursos.minerales.MineralCobre;
import principal.entes.objetos.recursos.minerales.MineralCosechable;
import principal.entes.objetos.recursos.minerales.MineralCristal;
import principal.entes.objetos.recursos.minerales.MineralHierro;
import principal.entes.objetos.recursos.minerales.MineralOro;
import principal.entes.objetos.recursos.minerales.MineralRoca;
import principal.mapa.Mundo;
import principal.persistencia.json.AdaptadorEntidad;
import principal.persistencia.json.LectorJSON;
import principal.persistencia.json.RegistroEntidades;

/**
 * Adaptadores para Cofres de inventario persistente y Recursos Cosechables.
 */
public final class AdaptadoresContenedoresYRecursos {

	private AdaptadoresContenedoresYRecursos() {
	}

	@SuppressWarnings("unchecked")
	private static void serializarItemsContenedor(final Cofre c, final JSONObject json) {
		final JSONArray arr = new JSONArray();
		for (final Item i : c.getInventario().getItems()) {
			final JSONObject sobreItem = RegistroEntidades.exportar(i);
			if (sobreItem != null) {
				arr.add(sobreItem);
			}
		}
		json.put("items", arr);
	}

	private static void deserializarItemsContenedor(final Cofre c, final JSONObject json) {
		final JSONArray arr = LectorJSON.getArray(json, "items");
		if (arr != null) {
			c.getInventario().vaciar();
			for (final Object obj : arr) {
				if (obj instanceof JSONObject) {
					final Item i = (Item) RegistroEntidades.importar((JSONObject) obj, null);
					if (i != null) {
						c.meterItem(i);
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static void serializarMineralBase(final MineralCosechable m, final JSONObject json) {
		json.put("x", Integer.valueOf(m.getPosicionXInt()));
		json.put("y", Integer.valueOf(m.getPosicionYInt()));
		json.put("durabilidad", Double.valueOf(m.getDurabilidad()));
	}

	// =========================================================================
	// COFRES
	// =========================================================================

	public static class AdaptadorCofrePequeno implements AdaptadorEntidad<CofrePequeño> {
		@Override
		public String getId() {
			return "COFRE_PEQUENO";
		}

		@Override
		public Class<CofrePequeño> getClaseEntidad() {
			return CofrePequeño.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final CofrePequeño c) {
			final JSONObject json = new JSONObject();
			json.put("x", Integer.valueOf(c.getPosicionXInt()));
			json.put("y", Integer.valueOf(c.getPosicionYInt()));
			serializarItemsContenedor(c, json);
			return json;
		}

		@Override
		public CofrePequeño deserializar(final JSONObject d, final Mundo mundo) {
			final CofrePequeño c = new CofrePequeño(LectorJSON.getInt(d, "x", 0), LectorJSON.getInt(d, "y", 0));
			c.setMundo(mundo);
			deserializarItemsContenedor(c, d);
			return c;
		}
	}

	public static class AdaptadorCofreMediano implements AdaptadorEntidad<CofreMediano> {
		@Override
		public String getId() {
			return "COFRE_MEDIANO";
		}

		@Override
		public Class<CofreMediano> getClaseEntidad() {
			return CofreMediano.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final CofreMediano c) {
			final JSONObject json = new JSONObject();
			json.put("x", Integer.valueOf(c.getPosicionXInt()));
			json.put("y", Integer.valueOf(c.getPosicionYInt()));
			serializarItemsContenedor(c, json);
			return json;
		}

		@Override
		public CofreMediano deserializar(final JSONObject d, final Mundo mundo) {
			final CofreMediano c = new CofreMediano(LectorJSON.getInt(d, "x", 0), LectorJSON.getInt(d, "y", 0));
			c.setMundo(mundo);
			deserializarItemsContenedor(c, d);
			return c;
		}
	}

	public static class AdaptadorArbolCofre implements AdaptadorEntidad<ArbolCofre> {
		@Override
		public String getId() {
			return "ARBOL_COFRE";
		}

		@Override
		public Class<ArbolCofre> getClaseEntidad() {
			return ArbolCofre.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final ArbolCofre a) {
			final JSONObject json = new JSONObject();
			json.put("x", Integer.valueOf(a.getPosicionXInt()));
			json.put("y", Integer.valueOf(a.getPosicionYInt()));
			final JSONArray arr = new JSONArray();
			for (final Item i : a.getInventario().getItems()) {
				final JSONObject sobreItem = RegistroEntidades.exportar(i);
				if (sobreItem != null) {
					arr.add(sobreItem);
				}
			}
			json.put("items", arr);
			return json;
		}

		@Override
		public ArbolCofre deserializar(final JSONObject d, final Mundo mundo) {
			final ArbolCofre a = new ArbolCofre(LectorJSON.getInt(d, "x", 0), LectorJSON.getInt(d, "y", 0));
			a.setMundo(mundo);
			final JSONArray arr = LectorJSON.getArray(d, "items");
			if (arr != null) {
				a.getInventario().vaciar();
				for (final Object obj : arr) {
					if (obj instanceof JSONObject) {
						final Item item = (Item) RegistroEntidades.importar((JSONObject) obj, null);
						if (item != null) {
							a.getInventario().agregarItem(item);
						}
					}
				}
			}
			return a;
		}
	}

	// =========================================================================
	// RECURSOS COSECHABLES
	// =========================================================================
	public static class AdaptadorArbolCosechable implements AdaptadorEntidad<ArbolCosechable> {
		@Override
		public String getId() {
			return "ARBOL_COSECHABLE";
		}

		@Override
		public Class<ArbolCosechable> getClaseEntidad() {
			return ArbolCosechable.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final ArbolCosechable a) {
			final JSONObject json = new JSONObject();
			json.put("x", Integer.valueOf(a.getPosicionXInt()));
			json.put("y", Integer.valueOf(a.getPosicionYInt()));
			json.put("tipoArbol", a.getTipoArbol().name());
			json.put("esTocon", Boolean.valueOf(a.isEsTocon()));
			json.put("durabilidad", Double.valueOf(a.getDurabilidad()));
			return json;
		}

		@Override
		public ArbolCosechable deserializar(final JSONObject d, final Mundo mundo) {
			final int x = LectorJSON.getInt(d, "x", 0);
			final int y = LectorJSON.getInt(d, "y", 0);
			final TipoArbol tipo = LectorJSON.getEnum(d, "tipoArbol", TipoArbol.ROBLE, TipoArbol.class);

			final ArbolCosechable a = new ArbolCosechable(x, y, tipo);
			a.setMundo(mundo);
			a.setEsTocon(LectorJSON.getBoolean(d, "esTocon", false));
			a.importarEstadoRecursoJSON(d);
			return a;
		}
	}

	public static class AdaptadorArbustoCosechable implements AdaptadorEntidad<ArbustoCosechable> {
		@Override
		public String getId() {
			return "ARBUSTO_COSECHABLE";
		}

		@Override
		public Class<ArbustoCosechable> getClaseEntidad() {
			return ArbustoCosechable.class;
		}

		@Override
		public JSONObject serializar(final ArbustoCosechable a) {
			return a.exportarParaJSON();
		}

		@Override
		public ArbustoCosechable deserializar(final JSONObject d, final Mundo mundo) {
			final ArbustoCosechable a = ArbustoCosechable.crearDesdeJson(d);
			a.setMundo(mundo);
			return a;
		}
	}

	// =========================================================================
	// MINERALES ESPECIALIZADOS
	// =========================================================================

	public static class AdaptadorMineralRoca implements AdaptadorEntidad<MineralRoca> {
		@Override
		public String getId() {
			return "MINERAL_ROCA";
		}

		@Override
		public Class<MineralRoca> getClaseEntidad() {
			return MineralRoca.class;
		}

		@Override
		public JSONObject serializar(final MineralRoca m) {
			final JSONObject json = new JSONObject();
			serializarMineralBase(m, json);
			return json;
		}

		@Override
		public MineralRoca deserializar(final JSONObject d, final Mundo mundo) {
			final int x = LectorJSON.getInt(d, "x", 0);
			final int y = LectorJSON.getInt(d, "y", 0);
			final double dur = LectorJSON.getDouble(d, "durabilidad", MineralRoca.DURABILIDAD_ROCA);
			final MineralRoca m = new MineralRoca(x, y, dur);
			m.setMundo(mundo);
			return m;
		}
	}

	public static class AdaptadorMineralCobre implements AdaptadorEntidad<MineralCobre> {
		@Override
		public String getId() {
			return "MINERAL_COBRE";
		}

		@Override
		public Class<MineralCobre> getClaseEntidad() {
			return MineralCobre.class;
		}

		@Override
		public JSONObject serializar(final MineralCobre m) {
			final JSONObject json = new JSONObject();
			serializarMineralBase(m, json);
			return json;
		}

		@Override
		public MineralCobre deserializar(final JSONObject d, final Mundo mundo) {
			final int x = LectorJSON.getInt(d, "x", 0);
			final int y = LectorJSON.getInt(d, "y", 0);
			final double dur = LectorJSON.getDouble(d, "durabilidad", MineralCobre.DURABILIDAD_COBRE);
			final MineralCobre m = new MineralCobre(x, y, dur);
			m.setMundo(mundo);
			return m;
		}
	}

	public static class AdaptadorMineralHierro implements AdaptadorEntidad<MineralHierro> {
		@Override
		public String getId() {
			return "MINERAL_HIERRO";
		}

		@Override
		public Class<MineralHierro> getClaseEntidad() {
			return MineralHierro.class;
		}

		@Override
		public JSONObject serializar(final MineralHierro m) {
			final JSONObject json = new JSONObject();
			serializarMineralBase(m, json);
			return json;
		}

		@Override
		public MineralHierro deserializar(final JSONObject d, final Mundo mundo) {
			final int x = LectorJSON.getInt(d, "x", 0);
			final int y = LectorJSON.getInt(d, "y", 0);
			final double dur = LectorJSON.getDouble(d, "durabilidad", MineralHierro.DURABILIDAD_HIERRO);
			final MineralHierro m = new MineralHierro(x, y, dur);
			m.setMundo(mundo);
			return m;
		}
	}

	public static class AdaptadorMineralOro implements AdaptadorEntidad<MineralOro> {
		@Override
		public String getId() {
			return "MINERAL_ORO";
		}

		@Override
		public Class<MineralOro> getClaseEntidad() {
			return MineralOro.class;
		}

		@Override
		public JSONObject serializar(final MineralOro m) {
			final JSONObject json = new JSONObject();
			serializarMineralBase(m, json);
			return json;
		}

		@Override
		public MineralOro deserializar(final JSONObject d, final Mundo mundo) {
			final int x = LectorJSON.getInt(d, "x", 0);
			final int y = LectorJSON.getInt(d, "y", 0);
			final double dur = LectorJSON.getDouble(d, "durabilidad", MineralOro.DURABILIDAD_ORO);
			final MineralOro m = new MineralOro(x, y, dur);
			m.setMundo(mundo);
			return m;
		}
	}

	public static class AdaptadorMineralCarbon implements AdaptadorEntidad<MineralCarbon> {
		@Override
		public String getId() {
			return "MINERAL_CARBON";
		}

		@Override
		public Class<MineralCarbon> getClaseEntidad() {
			return MineralCarbon.class;
		}

		@Override
		public JSONObject serializar(final MineralCarbon m) {
			final JSONObject json = new JSONObject();
			serializarMineralBase(m, json);
			return json;
		}

		@Override
		public MineralCarbon deserializar(final JSONObject d, final Mundo mundo) {
			final int x = LectorJSON.getInt(d, "x", 0);
			final int y = LectorJSON.getInt(d, "y", 0);
			final double dur = LectorJSON.getDouble(d, "durabilidad", MineralCarbon.DURABILIDAD_CARBON);
			final MineralCarbon m = new MineralCarbon(x, y, dur);
			m.setMundo(mundo);
			return m;
		}
	}

	public static class AdaptadorMineralCristal implements AdaptadorEntidad<MineralCristal> {
		@Override
		public String getId() {
			return "MINERAL_CRISTAL";
		}

		@Override
		public Class<MineralCristal> getClaseEntidad() {
			return MineralCristal.class;
		}

		@Override
		public JSONObject serializar(final MineralCristal m) {
			final JSONObject json = new JSONObject();
			serializarMineralBase(m, json);
			return json;
		}

		@Override
		public MineralCristal deserializar(final JSONObject d, final Mundo mundo) {
			final int x = LectorJSON.getInt(d, "x", 0);
			final int y = LectorJSON.getInt(d, "y", 0);
			final double dur = LectorJSON.getDouble(d, "durabilidad", MineralCristal.DURABILIDAD_CRISTAL);
			final MineralCristal m = new MineralCristal(x, y, dur);
			m.setMundo(mundo);
			return m;
		}
	}
}