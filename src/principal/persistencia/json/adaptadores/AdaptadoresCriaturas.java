package principal.persistencia.json.adaptadores;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Criatura.Direccion;
import principal.entes.criaturas.animales.Gallina;
import principal.entes.criaturas.enemigos.bandido.BandidoGarrote;
import principal.entes.criaturas.enemigos.bandido.BandidoGranadero;
import principal.entes.criaturas.enemigos.bandido.BandidoPistolero;
import principal.entes.criaturas.grupo.TipoVinculo;
import principal.entes.criaturas.jugador.Jugador;
import principal.entes.criaturas.mascotas.Mascota;
import principal.entes.criaturas.neutrales.Comerciante;
import principal.entes.criaturas.neutrales.ModoRenovacion;
import principal.entes.efectos.EfectoEstado;
import principal.entes.efectos.TipoEfectoEstado;
import principal.entes.objetos.items.Item;
import principal.mapa.Mundo;
import principal.persistencia.json.AdaptadorEntidad;
import principal.persistencia.json.LectorJSON;
import principal.persistencia.json.RegistroEntidades;

/**
 * Catálogo de adaptadores de persistencia JSON para la jerarquía de Criaturas.
 */
public final class AdaptadoresCriaturas {

	private AdaptadoresCriaturas() {
	}

	@SuppressWarnings("unchecked")
	public static void serializarBaseCriatura(final Criatura c, final JSONObject json) {
		json.put("x", Double.valueOf(c.getPosicionX()));
		json.put("y", Double.valueOf(c.getPosicionY()));
		json.put("xInicial", Integer.valueOf(c.getPosicionXInicial()));
		json.put("yInicial", Integer.valueOf(c.getPosicionYInicial()));
		json.put("direccion", c.getDireccion().name());
		json.put("vida", Double.valueOf(c.getVida()));
		json.put("vidaMaxima", Double.valueOf(c.getVidaMaxima()));
		json.put("velocidadBase", Double.valueOf(c.getVelocidad()));
		json.put("faccionBit", Integer.valueOf(c.getFaccionBit()));
		json.put("vinculo", c.getVinculo().name());

		final JSONArray arrEfectos = new JSONArray();
		for (final EfectoEstado ef : c.getEfectos()) {
			if (ef != null && ef.isActivo()) {
				final JSONObject jEf = new JSONObject();
				jEf.put("tipo", ef.getTipo().name());
				jEf.put("duracion", Double.valueOf(ef.getTiempoRestante()));
				jEf.put("potencia", Double.valueOf(ef.getPotencia()));
				jEf.put("stacks", Integer.valueOf(ef.getStacks()));
				jEf.put("infinito", Boolean.valueOf(ef.isInfinito()));
				arrEfectos.add(jEf);
			}
		}
		json.put("efectos", arrEfectos);

		if (c.getBlackboard() != null) {
			final JSONObject jBb = new JSONObject();
			jBb.put("enPanico", Boolean.valueOf(c.getBlackboard().isEnPanico()));
			jBb.put("siguiendo", Boolean.valueOf(c.getBlackboard().isSiguiendoLider()));
			jBb.put("agresivo", Boolean.valueOf(c.getBlackboard().isModoAgresivo()));
			jBb.put("tieneAncla", Boolean.valueOf(c.getBlackboard().tieneAnclaRetorno()));
			jBb.put("anclaX", Double.valueOf(c.getBlackboard().getXAnclaRetorno()));
			jBb.put("anclaY", Double.valueOf(c.getBlackboard().getYAnclaRetorno()));
			jBb.put("tsRetorno", Double.valueOf(c.getBlackboard().getTimestampRetornoJuegoHoras()));
			json.put("blackboard", jBb);
		}
	}

	public static void deserializarBaseCriatura(final Criatura c, final JSONObject json) {
		c.setPosicion(LectorJSON.getDouble(json, "x", c.getPosicionX()),
				LectorJSON.getDouble(json, "y", c.getPosicionY()));
		c.setDireccion(LectorJSON.getEnum(json, "direccion", Direccion.SUR, Direccion.class));

		final double vidaMax = LectorJSON.getDouble(json, "vidaMaxima", c.getVidaMaxima());
		c.establecerVidaMaxima(vidaMax);
		c.establecerVida(LectorJSON.getDouble(json, "vida", vidaMax));

		c.setFaccion(LectorJSON.getInt(json, "faccionBit", c.getFaccionBit()));
		c.setVinculo(LectorJSON.getEnum(json, "vinculo", TipoVinculo.NINGUNO, TipoVinculo.class));

		c.limpiarEfectos();
		final JSONArray arrEfectos = LectorJSON.getArray(json, "efectos");
		if (arrEfectos != null) {
			for (final Object obj : arrEfectos) {
				if (obj instanceof JSONObject) {
					final JSONObject jEf = (JSONObject) obj;
					final TipoEfectoEstado tipo = LectorJSON.getEnum(jEf, "tipo", null, TipoEfectoEstado.class);
					if (tipo != null) {
						final double dur = LectorJSON.getDouble(jEf, "duracion", 1.0);
						final double pot = LectorJSON.getDouble(jEf, "potencia", 1.0);
						final int st = LectorJSON.getInt(jEf, "stacks", 1);
						final boolean inf = LectorJSON.getBoolean(jEf, "infinito", false);
						if (inf) {
							c.aplicarEfectoInfinito(tipo, pot, st);
						} else {
							c.aplicarEfecto(tipo, dur, pot, st);
						}
					}
				}
			}
		}

		final JSONObject jBb = LectorJSON.getObjeto(json, "blackboard");
		if (jBb != null && c.getBlackboard() != null) {
			c.getBlackboard().setEnPanico(LectorJSON.getBoolean(jBb, "enPanico", false));
			c.getBlackboard().setSiguiendoLider(LectorJSON.getBoolean(jBb, "siguiendo", false));
			c.getBlackboard().setModoAgresivo(LectorJSON.getBoolean(jBb, "agresivo", false));
			if (LectorJSON.getBoolean(jBb, "tieneAncla", false)) {
				c.getBlackboard().fijarAnclaRetorno(LectorJSON.getDouble(jBb, "anclaX", 0.0),
						LectorJSON.getDouble(jBb, "anclaY", 0.0));
			}
			c.getBlackboard().setTimestampRetornoJuegoHoras(LectorJSON.getDouble(jBb, "tsRetorno", 0.0));
		}
	}

	// =========================================================================
	// ADAPTADORES CONCRETOS
	// =========================================================================

	public static class AdaptadorJugador implements AdaptadorEntidad<Jugador> {
		@Override
		public String getId() {
			return "JUGADOR";
		}

		@Override
		public Class<Jugador> getClaseEntidad() {
			return Jugador.class;
		}

		@Override
		public JSONObject serializar(final Jugador j) {
			return j.exportarParaJSON();
		}

		@Override
		public Jugador deserializar(final JSONObject datos, final Mundo mundo) {
			final Jugador j = new Jugador(LectorJSON.getInt(datos, "x", 0), LectorJSON.getInt(datos, "y", 0));
			j.setMundo(mundo);
			j.importarDeJSON(datos);
			return j;
		}
	}

	public static class AdaptadorBandidoPistolero implements AdaptadorEntidad<BandidoPistolero> {
		@Override
		public String getId() {
			return "BANDIDO_PISTOLERO";
		}

		@Override
		public Class<BandidoPistolero> getClaseEntidad() {
			return BandidoPistolero.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final BandidoPistolero b) {
			final JSONObject json = new JSONObject();
			serializarBaseCriatura(b, json);
			return json;
		}

		@Override
		public BandidoPistolero deserializar(final JSONObject d, final Mundo mundo) {
			final double x = LectorJSON.getDouble(d, "x", 0.0);
			final double y = LectorJSON.getDouble(d, "y", 0.0);
			final double vm = LectorJSON.getDouble(d, "vidaMaxima", 50.0);
			final double v = LectorJSON.getDouble(d, "vida", vm);

			final BandidoPistolero b = new BandidoPistolero(x, y, v, vm, mundo);
			deserializarBaseCriatura(b, d);
			return b;
		}
	}

	public static class AdaptadorBandidoGarrote implements AdaptadorEntidad<BandidoGarrote> {
		@Override
		public String getId() {
			return "BANDIDO_GARROTE";
		}

		@Override
		public Class<BandidoGarrote> getClaseEntidad() {
			return BandidoGarrote.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final BandidoGarrote b) {
			final JSONObject json = new JSONObject();
			serializarBaseCriatura(b, json);
			return json;
		}

		@Override
		public BandidoGarrote deserializar(final JSONObject d, final Mundo mundo) {
			final double x = LectorJSON.getDouble(d, "x", 0.0);
			final double y = LectorJSON.getDouble(d, "y", 0.0);
			final double vm = LectorJSON.getDouble(d, "vidaMaxima", 60.0);
			final double v = LectorJSON.getDouble(d, "vida", vm);

			final BandidoGarrote b = new BandidoGarrote(x, y, v, vm, mundo);
			deserializarBaseCriatura(b, d);
			return b;
		}
	}

	public static class AdaptadorBandidoGranadero implements AdaptadorEntidad<BandidoGranadero> {
		@Override
		public String getId() {
			return "BANDIDO_GRANADERO";
		}

		@Override
		public Class<BandidoGranadero> getClaseEntidad() {
			return BandidoGranadero.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final BandidoGranadero b) {
			final JSONObject json = new JSONObject();
			serializarBaseCriatura(b, json);
			return json;
		}

		@Override
		public BandidoGranadero deserializar(final JSONObject d, final Mundo mundo) {
			final double x = LectorJSON.getDouble(d, "x", 0.0);
			final double y = LectorJSON.getDouble(d, "y", 0.0);
			final double vm = LectorJSON.getDouble(d, "vidaMaxima", 45.0);
			final double v = LectorJSON.getDouble(d, "vida", vm);

			final BandidoGranadero b = new BandidoGranadero(x, y, v, vm, mundo);
			deserializarBaseCriatura(b, d);
			return b;
		}
	}

	public static class AdaptadorGallina implements AdaptadorEntidad<Gallina> {
		@Override
		public String getId() {
			return "GALLINA";
		}

		@Override
		public Class<Gallina> getClaseEntidad() {
			return Gallina.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final Gallina g) {
			final JSONObject json = new JSONObject();
			serializarBaseCriatura(g, json);
			return json;
		}

		@Override
		public Gallina deserializar(final JSONObject d, final Mundo mundo) {
			final double x = LectorJSON.getDouble(d, "x", 0.0);
			final double y = LectorJSON.getDouble(d, "y", 0.0);
			final Gallina g = new Gallina(x, y);
			g.setMundo(mundo);
			deserializarBaseCriatura(g, d);
			return g;
		}
	}

	public static class AdaptadorMascota implements AdaptadorEntidad<Mascota> {
		@Override
		public String getId() {
			return "MASCOTA";
		}

		@Override
		public Class<Mascota> getClaseEntidad() {
			return Mascota.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final Mascota m) {
			final JSONObject json = new JSONObject();
			serializarBaseCriatura(m, json);
			json.put("nombre", m.getNombre());
			json.put("siguiendo", Boolean.valueOf(m.isSiguiendo()));
			json.put("agresivo", Boolean.valueOf(m.isAgresivo()));
			json.put("puedeTparse", Boolean.valueOf(m.isPuedeTparseAlLider()));
			return json;
		}

		@Override
		public Mascota deserializar(final JSONObject d, final Mundo mundo) {
			final double x = LectorJSON.getDouble(d, "x", 0.0);
			final double y = LectorJSON.getDouble(d, "y", 0.0);
			final String nombre = LectorJSON.getString(d, "nombre", "Compañero");
			final double vm = LectorJSON.getDouble(d, "vidaMaxima", 80.0);
			final TipoVinculo v = LectorJSON.getEnum(d, "vinculo", TipoVinculo.MASCOTA, TipoVinculo.class);

			final Mascota m = new Mascota(x, y, nombre, vm, v);
			m.setMundo(mundo);
			deserializarBaseCriatura(m, d);

			m.setSiguiendo(LectorJSON.getBoolean(d, "siguiendo", false));
			m.setAgresivo(LectorJSON.getBoolean(d, "agresivo", false));
			m.setPuedeTparseAlLider(LectorJSON.getBoolean(d, "puedeTparse", false));
			return m;
		}
	}

	public static class AdaptadorComerciante implements AdaptadorEntidad<Comerciante> {
		@Override
		public String getId() {
			return "COMERCIANTE";
		}

		@Override
		public Class<Comerciante> getClaseEntidad() {
			return Comerciante.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final Comerciante c) {
			final JSONObject json = new JSONObject();
			serializarBaseCriatura(c, json);
			json.put("nombre", c.getNombre());
			json.put("stockInfinito", Boolean.valueOf(c.isStockInfinito()));
			json.put("renovacionAuto", Boolean.valueOf(c.isRenovacionAutomatica()));
			json.put("modoRenovacion", c.getModoRenovacion().name());
			json.put("intervaloRenovacion", Integer.valueOf(c.getIntervaloDiasRenovacion()));
			json.put("ultimoDiaRenovado", Integer.valueOf(c.getUltimoDiaRenovado()));

			final JSONArray itemsArray = new JSONArray();
			for (final Item i : c.getInventario().getItems()) {
				final JSONObject sobreItem = RegistroEntidades.exportar(i);
				if (sobreItem != null) {
					itemsArray.add(sobreItem);
				}
			}
			json.put("stock", itemsArray);
			return json;
		}

		@Override
		public Comerciante deserializar(final JSONObject d, final Mundo mundo) {
			final double x = LectorJSON.getDouble(d, "x", 0.0);
			final double y = LectorJSON.getDouble(d, "y", 0.0);
			final String nombre = LectorJSON.getString(d, "nombre", "Comerciante");
			final double vm = LectorJSON.getDouble(d, "vidaMaxima", 100.0);

			final Comerciante c = new Comerciante(x, y, nombre, vm);
			c.setMundo(mundo);
			deserializarBaseCriatura(c, d);

			c.setStockInfinito(LectorJSON.getBoolean(d, "stockInfinito", false));
			c.setModoRenovacion(LectorJSON.getEnum(d, "modoRenovacion", ModoRenovacion.POR_INTERVALO_DIAS, ModoRenovacion.class));
			c.setRenovacionAutomatica(LectorJSON.getBoolean(d, "renovacionAuto", false),
					LectorJSON.getInt(d, "intervaloRenovacion", 3));
			c.setUltimoDiaRenovado(LectorJSON.getInt(d, "ultimoDiaRenovado", 1));

			final JSONArray stockArr = LectorJSON.getArray(d, "stock");
			if (stockArr != null) {
				c.getInventario().vaciar();
				for (final Object obj : stockArr) {
					if (obj instanceof JSONObject) {
						final Item item = (Item) RegistroEntidades.importar((JSONObject) obj, null);
						if (item != null) {
							c.getInventario().agregarItem(item);
						}
					}
				}
			}
			return c;
		}
	}
}