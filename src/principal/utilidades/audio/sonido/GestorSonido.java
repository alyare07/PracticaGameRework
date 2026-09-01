package principal.utilidades.audio.sonido;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import principal.utilidades.Constantes;

public class GestorSonido {

	private static final Map<String, PoolSonido> REGISTRO = new HashMap<>();

	private GestorSonido() {
	}

	public static void cargarSonidosDesdeJSON(final String rutaJson) {
		final JSONParser parser = new JSONParser();

		try (final FileReader reader = new FileReader(rutaJson)) {
			final Object obj = parser.parse(reader);
			final JSONObject jsonObject = (JSONObject) obj;

			for (final Object key : jsonObject.keySet()) {
				final String idSonido = (String) key;
				final JSONObject config = (JSONObject) jsonObject.get(idSonido);

				final String ruta = (String) config.get("ruta");

				double volumen = 1.0;
				if (config.get("volumen") != null) {
					volumen = Double.parseDouble(config.get("volumen").toString());
				}

				// Precarga el sonido base y crea su pool polifónico de 6 voces en RAM
				final SonidoJavaSound sonidoBase = FabricaSonido.crearSonido(ruta, volumen);
				if (sonidoBase != null) {
					REGISTRO.put(idSonido, new PoolSonido(sonidoBase, 6));
				}
			}

			System.out.println("GestorSonido: Se precargaron " + REGISTRO.size()
					+ " bancos de sonido polifonicos en memoria RAM.");

		} catch (final Exception e) {
			System.err.println("⚠ GestorSonido: Error al cargar el archivo de sonidos JSON desde '" + rutaJson + "'");
			e.printStackTrace();
		}
	}

	public static void reproducir(final String idSonido) {
		final PoolSonido pool = REGISTRO.get(idSonido);
		if (pool == null) {
			return;
		}
		pool.reproducir();
	}

	public static void reproducirEnPosicion(final String idSonido, final double xEmisor, final double yEmisor,
			final double xReceptor, final double yReceptor, final double radioMaximo) {

		final PoolSonido pool = REGISTRO.get(idSonido);
		if (pool == null) {
			return;
		}

		final double distancia = Math.hypot(xEmisor - xReceptor, yEmisor - yReceptor);
		if (distancia >= radioMaximo) {
			return;
		}

		final double factorDistancia = Math.max(0.0, 1.0 - (distancia / radioMaximo));
		final double volumenFinal = pool.getVolumenPorDefecto() * factorDistancia;

		pool.reproducirConVolumen(volumenFinal);
	}

	public static void reproducirEnPosicion(final String idSonido, final double xEmisor, final double yEmisor,
			final double xReceptor, final double yReceptor) {
		reproducirEnPosicion(idSonido, xEmisor, yEmisor, xReceptor, yReceptor, Constantes.RADIO_AUDIO_DISTANCIA_MAXIMA);
	}

	// =========================================================================
	// SOBRECARGAS CON ENUM (IDSonido)
	// =========================================================================

	public static void reproducir(final IDSonido id) {
		if (id != null) {
			reproducir(id.getId());
		}
	}

	public static void reproducirEnPosicion(final IDSonido id, final double xEmisor, final double yEmisor,
			final double xReceptor, final double yReceptor, final double radioMaximo) {
		if (id != null) {
			reproducirEnPosicion(id.getId(), xEmisor, yEmisor, xReceptor, yReceptor, radioMaximo);
		}
	}

	public static void reproducirEnPosicion(final IDSonido id, final double xEmisor, final double yEmisor,
			final double xReceptor, final double yReceptor) {
		if (id != null) {
			reproducirEnPosicion(id.getId(), xEmisor, yEmisor, xReceptor, yReceptor);
		}
	}
}