package principal.utilidades.audio.musica;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import principal.utilidades.audio.DatosAudio;

/**
 * Administrador global de pistas musicales y bandas sonoras del juego con
 * soporte de atenuación proporcional en interiores y cuevas (Zero-GC / O(1)).
 * 
 * @version 3.0 (Vanilla Java 8 - Proportional Interior Attenuation)
 */
public class GestorMusica {

	private static MusicaStream musicaAmbienteClima;
	private static String idAmbienteClimaActual;
	private static double factorAtenuacionAmbiente = 1.0;

	private static final Map<String, DatosAudio> REGISTRO = new HashMap<>();

	private static MusicaStream musicaFondoPrincipal;
	private static String idMusicaFondoPrincipal;

	private GestorMusica() {
	}

	public static void cargarMusicasDesdeJSON(final String rutaJson) {
		final JSONParser parser = new JSONParser();

		try (final FileReader reader = new FileReader(rutaJson)) {
			final Object obj = parser.parse(reader);
			final JSONObject jsonObject = (JSONObject) obj;

			for (final Object key : jsonObject.keySet()) {
				final String idMusica = (String) key;

				if (idMusica.startsWith("musicas.")) {
					final JSONObject config = (JSONObject) jsonObject.get(idMusica);
					final String ruta = (String) config.get("ruta");

					double volumen = 1.0;
					if (config.get("volumen") != null) {
						volumen = Double.parseDouble(config.get("volumen").toString());
					}

					REGISTRO.put(idMusica, new DatosAudio(ruta, volumen));
				}
			}

			System.out.println("GestorMusica: Se registraron " + REGISTRO.size() + " pistas de música correctamente.");

		} catch (final Exception e) {
			System.err.println("⚠ GestorMusica: Error al cargar el archivo de música JSON desde '" + rutaJson + "'");
			e.printStackTrace();
		}
	}

	public static void reproducirMusicaFondoPrincipal(final String idMusica) {
		if (idMusica == null) {
			return;
		}

		if (idMusica.equals(idMusicaFondoPrincipal) && (musicaFondoPrincipal != null)) {
			musicaFondoPrincipal.actualizar(true);
			return;
		}

		detenerMusicaFondoPrincipal();

		final DatosAudio datos = REGISTRO.get(idMusica);
		if (datos == null) {
			System.err.println("⚠ GestorMusica: El ID '" + idMusica + "' no existe en el registro.");
			return;
		}

		musicaFondoPrincipal = new MusicaStream(datos.getRuta(), datos.getVolumen());
		musicaFondoPrincipal.repetir(true);
		musicaFondoPrincipal.reproducir();
		idMusicaFondoPrincipal = idMusica;
	}

	public static void actualizarMusicaFondoPrincipal(final boolean reproducir) {
		if (musicaFondoPrincipal != null) {
			musicaFondoPrincipal.actualizar(reproducir);
		}
	}

	public static void detenerMusicaFondoPrincipal() {
		if (musicaFondoPrincipal != null) {
			musicaFondoPrincipal.detener();
			musicaFondoPrincipal = null;
			idMusicaFondoPrincipal = null;
		}
	}

	public static void setVolumenMusicaFondoPrincipal(final double volumen) {
		if (musicaFondoPrincipal != null) {
			musicaFondoPrincipal.setVolumen(volumen);
		}
	}

	public static MusicaStream obtenerInstancia(final String idMusica) {
		final DatosAudio datos = REGISTRO.get(idMusica);
		if (datos != null) {
			return new MusicaStream(datos.getRuta(), datos.getVolumen());
		}
		System.err.println("⚠ GestorMusica: No se pudo instanciar la música ID '" + idMusica + "'");
		return null;
	}

	public static void reproducirMusicaFondoPrincipal(final IDMusica id) {
		if (id != null) {
			reproducirMusicaFondoPrincipal(id.getId());
		}
	}

	public static MusicaStream obtenerInstancia(final IDMusica id) {
		if (id != null) {
			return obtenerInstancia(id.getId());
		}
		return null;
	}

	// =========================================================================
	// GESTIÓN PROPORCIONAL DE AUDIO CLIMÁTICO Y AMBIENTAL
	// =========================================================================

	public static void reproducirAmbienteClima(final String idAmbiente) {
		if (idAmbiente == null) {
			detenerAmbienteClima();
			return;
		}

		if (idAmbiente.equals(idAmbienteClimaActual) && (musicaAmbienteClima != null)) {
			musicaAmbienteClima.actualizar(true);
			thisAplicarVolumenEfectivo();
			return;
		}

		detenerAmbienteClima();

		final DatosAudio datos = REGISTRO.get(idAmbiente);
		if (datos != null) {
			final double volumenEfectivo = datos.getVolumen() * factorAtenuacionAmbiente;
			musicaAmbienteClima = new MusicaStream(datos.getRuta(), volumenEfectivo);
			musicaAmbienteClima.repetir(true);
			musicaAmbienteClima.reproducir();
			idAmbienteClimaActual = idAmbiente;
		}
	}

	public static void reproducirAmbienteClima(final IDMusica id) {
		if (id != null) {
			reproducirAmbienteClima(id.getId());
		} else {
			detenerAmbienteClima();
		}
	}

	public static void detenerAmbienteClima() {
		if (musicaAmbienteClima != null) {
			musicaAmbienteClima.detener();
			musicaAmbienteClima = null;
			idAmbienteClimaActual = null;
		}
	}

	public static void actualizarAmbienteClima(final boolean reproducir) {
		if (musicaAmbienteClima != null) {
			musicaAmbienteClima.actualizar(reproducir);
		}
	}

	/**
	 * Configura el factor multiplicador de volumen para climas (1.0 = Exterior,
	 * 0.20 = Casa, 0.0 = Cueva).
	 */
	public static void setFactorAtenuacionAmbiente(final double factor) {
		factorAtenuacionAmbiente = Math.max(0.0, Math.min(1.0, factor));
		thisAplicarVolumenEfectivo();
	}

	public static double getFactorAtenuacionAmbiente() {
		return factorAtenuacionAmbiente;
	}

	private static void thisAplicarVolumenEfectivo() {
		if ((musicaAmbienteClima != null) && (idAmbienteClimaActual != null)) {
			final DatosAudio datos = REGISTRO.get(idAmbienteClimaActual);
			if (datos != null) {
				final double volumenCalculado = datos.getVolumen() * factorAtenuacionAmbiente;
				musicaAmbienteClima.setVolumen(volumenCalculado);
			}
		}
	}
}