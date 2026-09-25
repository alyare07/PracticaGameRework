package principal.persistencia.json.adaptadores;

import org.json.simple.JSONObject;

import principal.entes.modelos.complemento.ListaModeloComplemento;
import principal.entes.objetos.Complemento;
import principal.entes.objetos.EntradaCueva;
import principal.entes.objetos.especial.CuadradoInvisible;
import principal.entes.objetos.fabricables.Cama;
import principal.entes.objetos.fabricables.Carpa;
import principal.entes.objetos.fabricables.Fogata;
import principal.mapa.Mundo;
import principal.persistencia.json.AdaptadorEntidad;
import principal.persistencia.json.LectorJSON;
import principal.utilidades.Globales;

/**
 * Catálogo de adaptadores de persistencia JSON para Complementos, Barreras y
 * Fabricables.
 */
public final class AdaptadoresEstructuras {

	private AdaptadoresEstructuras() {
	}

	public static class AdaptadorComplemento implements AdaptadorEntidad<Complemento> {
		@Override
		public String getId() {
			return "COMPLEMENTO";
		}

		@Override
		public Class<Complemento> getClaseEntidad() {
			return Complemento.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final Complemento c) {
			final JSONObject json = new JSONObject();
			json.put("x", Integer.valueOf(c.getPosicionXInt()));
			json.put("y", Integer.valueOf(c.getPosicionYInt()));
			json.put("codModelo", Integer.valueOf(c.getCodigoModelo()));
			return json;
		}

		@Override
		public Complemento deserializar(final JSONObject d, final Mundo mundo) {
			final int x = LectorJSON.getInt(d, "x", 0);
			final int y = LectorJSON.getInt(d, "y", 0);
			final int cod = LectorJSON.getInt(d, "codModelo", ListaModeloComplemento.COD_ARBOL_1);
			final Complemento c = new Complemento(x, y, cod);
			c.setMundo(mundo);
			return c;
		}
	}

	public static class AdaptadorCuadradoInvisible implements AdaptadorEntidad<CuadradoInvisible> {
		@Override
		public String getId() {
			return "CUADRADO_INVISIBLE";
		}

		@Override
		public Class<CuadradoInvisible> getClaseEntidad() {
			return CuadradoInvisible.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final CuadradoInvisible c) {
			final JSONObject json = new JSONObject();
			json.put("x", Integer.valueOf(c.getPosicionXInt()));
			json.put("y", Integer.valueOf(c.getPosicionYInt()));
			json.put("codModelo", Integer.valueOf(c.getCodigoModelo()));
			return json;
		}

		@Override
		public CuadradoInvisible deserializar(final JSONObject d, final Mundo mundo) {
			final int x = LectorJSON.getInt(d, "x", 0);
			final int y = LectorJSON.getInt(d, "y", 0);
			final int cod = LectorJSON.getInt(d, "codModelo", 1);
			final CuadradoInvisible c = new CuadradoInvisible(x, y, cod);
			c.setMundo(mundo);
			return c;
		}
	}

	public static class AdaptadorFogata implements AdaptadorEntidad<Fogata> {
		@Override
		public String getId() {
			return "FOGATA";
		}

		@Override
		public Class<Fogata> getClaseEntidad() {
			return Fogata.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final Fogata f) {
			final JSONObject json = new JSONObject();
			json.put("x", Integer.valueOf(f.getPosicionXInt()));
			json.put("y", Integer.valueOf(f.getPosicionYInt()));
			json.put("madera", Integer.valueOf(f.getMaderaAlmacenada()));
			json.put("encendida", Boolean.valueOf(f.isEncendida()));
			json.put("fuegoAzul", Boolean.valueOf(f.isFuegoAzul()));
			json.put("vida", Double.valueOf(f.getVida()));
			return json;
		}

		@Override
		public Fogata deserializar(final JSONObject d, final Mundo mundo) {
			final int x = LectorJSON.getInt(d, "x", 0);
			final int y = LectorJSON.getInt(d, "y", 0);
			final int madera = LectorJSON.getInt(d, "madera", 2);
			final boolean encendida = LectorJSON.getBoolean(d, "encendida", true);
			final boolean azul = LectorJSON.getBoolean(d, "fuegoAzul", false);
			final double vida = LectorJSON.getDouble(d, "vida", Fogata.VIDA_FOGATA);

			final Fogata f = new Fogata(x, y, madera, encendida, azul, Globales.JUGADOR);
			f.setVida(vida);
			f.setMundo(mundo);
			return f;
		}
	}

	public static class AdaptadorCarpa implements AdaptadorEntidad<Carpa> {
		@Override
		public String getId() {
			return "CARPA";
		}

		@Override
		public Class<Carpa> getClaseEntidad() {
			return Carpa.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final Carpa c) {
			final JSONObject json = new JSONObject();
			json.put("x", Integer.valueOf(c.getPosicionXInt()));
			json.put("y", Integer.valueOf(c.getPosicionYInt()));
			json.put("vida", Double.valueOf(c.getVida()));
			return json;
		}

		@Override
		public Carpa deserializar(final JSONObject d, final Mundo mundo) {
			final int x = LectorJSON.getInt(d, "x", 0);
			final int y = LectorJSON.getInt(d, "y", 0);
			final double vida = LectorJSON.getDouble(d, "vida", Carpa.VIDA_CARPA);

			final Carpa c = new Carpa(x, y, vida, Globales.JUGADOR);
			c.setMundo(mundo);
			return c;
		}
	}

	public static class AdaptadorCama implements AdaptadorEntidad<Cama> {
		@Override
		public String getId() {
			return "CAMA";
		}

		@Override
		public Class<Cama> getClaseEntidad() {
			return Cama.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final Cama c) {
			final JSONObject json = new JSONObject();
			json.put("x", Integer.valueOf(c.getPosicionXInt()));
			json.put("y", Integer.valueOf(c.getPosicionYInt()));
			json.put("vida", Double.valueOf(c.getVida()));
			return json;
		}

		@Override
		public Cama deserializar(final JSONObject d, final Mundo mundo) {
			final int x = LectorJSON.getInt(d, "x", 0);
			final int y = LectorJSON.getInt(d, "y", 0);
			final double vida = LectorJSON.getDouble(d, "vida", Cama.VIDA_CAMA);

			final Cama c = new Cama(x, y, Globales.JUGADOR);
			c.setVida(vida);
			c.setMundo(mundo);
			return c;
		}
	}

	public static class AdaptadorEntradaCueva implements AdaptadorEntidad<EntradaCueva> {
		@Override
		public String getId() {
			return "ENTRADA_CUEVA";
		}

		@Override
		public Class<EntradaCueva> getClaseEntidad() {
			return EntradaCueva.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final EntradaCueva e) {
			return e.exportarParaJSON();
		}

		@Override
		public EntradaCueva deserializar(final JSONObject d, final Mundo mundo) {
			final EntradaCueva cueva = EntradaCueva.crearDesdeJson(d);
			cueva.setMundo(mundo);
			return cueva;
		}
	}

	public static class AdaptadorZonaTP implements AdaptadorEntidad<principal.mapa.escenario.tps.ZonaTP> {
		@Override
		public String getId() {
			return "ZONA_TP";
		}

		@Override
		public Class<principal.mapa.escenario.tps.ZonaTP> getClaseEntidad() {
			return principal.mapa.escenario.tps.ZonaTP.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final principal.mapa.escenario.tps.ZonaTP tp) {
			final JSONObject json = new JSONObject();
			json.put("x", Integer.valueOf(tp.getPosicionXInt()));
			json.put("y", Integer.valueOf(tp.getPosicionYInt()));
			json.put("w", Integer.valueOf(tp.getAncho()));
			json.put("h", Integer.valueOf(tp.getAlto()));

			final JSONObject jPuerta = principal.mapa.escenario.tps.SerializadorPuertas
					.serializarPuerta(tp.getPuertaTP());
			if (jPuerta != null) {
				json.put("puerta", jPuerta);
			}
			return json;
		}

		@Override
		public principal.mapa.escenario.tps.ZonaTP deserializar(final JSONObject d, final Mundo mundo) {
			final int x = LectorJSON.getInt(d, "x", 0);
			final int y = LectorJSON.getInt(d, "y", 0);
			final int w = LectorJSON.getInt(d, "w", 16);
			final int h = LectorJSON.getInt(d, "h", 16);

			final JSONObject jPuerta = LectorJSON.getObjeto(d, "puerta");
			final principal.mapa.escenario.tps.PuertaTP puerta = (jPuerta != null)
					? principal.mapa.escenario.tps.SerializadorPuertas.deserializarPuerta(jPuerta)
					: principal.mapa.escenario.tps.SerializadorPuertas.deserializarPuerta(d);

			final principal.mapa.escenario.tps.ZonaTP tp = new principal.mapa.escenario.tps.ZonaTP(
					new java.awt.Rectangle(x, y, w, h), puerta);
			tp.setMundo(mundo);
			return tp;
		}
	}

	public static class AdaptadorEdificio implements AdaptadorEntidad<principal.entes.estructuras.Edificio> {
		@Override
		public String getId() {
			return "EDIFICIO";
		}

		@Override
		public Class<principal.entes.estructuras.Edificio> getClaseEntidad() {
			return principal.entes.estructuras.Edificio.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject serializar(final principal.entes.estructuras.Edificio e) {
			final JSONObject json = new JSONObject();
			json.put("x", Integer.valueOf(e.getPosicionXInt()));
			json.put("y", Integer.valueOf(e.getPosicionYInt()));
			json.put("tipoEdificio", e.getTipoEdificio().name());
			json.put("mundoDestino", e.getNombreMundoDestino());
			json.put("spawnDestino", e.getNombreSpawnDestino());
			json.put("bloqueada", Boolean.valueOf(e.isBloqueada()));
			return json;
		}

		@Override
		public principal.entes.estructuras.Edificio deserializar(final JSONObject d, final Mundo mundo) {
			final int x = LectorJSON.getInt(d, "x", 0);
			final int y = LectorJSON.getInt(d, "y", 0);
			final principal.entes.estructuras.TipoEdificio tipo = LectorJSON.getEnum(d, "tipoEdificio",
					principal.entes.estructuras.TipoEdificio.CASA_CAMPO,
					principal.entes.estructuras.TipoEdificio.class);
			final String mDest = LectorJSON.getString(d, "mundoDestino", null);
			final String spDest = LectorJSON.getString(d, "spawnDestino", "Entrada");
			final boolean bloq = LectorJSON.getBoolean(d, "bloqueada", false);

			final principal.entes.estructuras.Edificio e = new principal.entes.estructuras.Edificio(x, y, tipo, mDest,
					spDest, bloq);
			e.setMundo(mundo);
			return e;
		}

	}
}
