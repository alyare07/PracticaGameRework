package principal.utilidades.audio.musica;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import principal.utilidades.audio.DatosAudio;

/**
 * Administrador global de pistas musicales con soporte para multiplicadores de
 * volumen maestro y atenuación ambiental (Zero-GC / O(1)).
 * 
 * @version 4.0 (Vanilla Java 8 - Real-Time Master Volume Scaling)
 */
public class GestorMusica {

	private static double multiplicadorVolumenGeneral = 1.0;
	private static double multiplicadorVolumenMusica = 1.0;
	private static double factorAtenuacionAmbiente = 1.0;

	private static MusicaStream musicaAmbienteClima;
	private static String idAmbienteClimaActual;

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
			actualizarVolumenPrincipal();
			return;
		}

		detenerMusicaFondoPrincipal();

		final DatosAudio datos = REGISTRO.get(idMusica);
		if (datos == null) {
			System.err.println("⚠ GestorMusica: El ID '" + idMusica + "' no existe en el registro.");
			return;
		}

		final double volumenFinal = datos.getVolumen() * multiplicadorVolumenGeneral * multiplicadorVolumenMusica;
		musicaFondoPrincipal = new MusicaStream(datos.getRuta(), volumenFinal);
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

	public static void reproducirMusicaFondoPrincipal(final IDMusica id) {
		if (id != null) {
			reproducirMusicaFondoPrincipal(id.getId());
		}
	}

	// =========================================================================
	// GESTIÓN DE AUDIO CLIMÁTICO Y AMBIENTAL
	// =========================================================================

	public static void reproducirAmbienteClima(final String idAmbiente) {
		if (idAmbiente == null) {
			detenerAmbienteClima();
			return;
		}

		if (idAmbiente.equals(idAmbienteClimaActual) && (musicaAmbienteClima != null)) {
			musicaAmbienteClima.actualizar(true);
			actualizarVolumenAmbiente();
			return;
		}

		detenerAmbienteClima();

		final DatosAudio datos = REGISTRO.get(idAmbiente);
		if (datos != null) {
			final double volumenFinal = datos.getVolumen() * factorAtenuacionAmbiente * multiplicadorVolumenGeneral
					* multiplicadorVolumenMusica;
			musicaAmbienteClima = new MusicaStream(datos.getRuta(), volumenFinal);
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

	public static void setFactorAtenuacionAmbiente(final double factor) {
		factorAtenuacionAmbiente = Math.max(0.0, Math.min(1.0, factor));
		actualizarVolumenAmbiente();
	}

	public static double getFactorAtenuacionAmbiente() {
		return factorAtenuacionAmbiente;
	}

	// =========================================================================
	// ACTUALIZACIÓN EN CALIENTE DE VOLÚMENES
	// =========================================================================

	public static void setMultiplicadorVolumenGeneral(final double vol) {
		multiplicadorVolumenGeneral = Math.max(0.0, Math.min(1.0, vol));
		actualizarVolumenPrincipal();
		actualizarVolumenAmbiente();
	}

	public static void setMultiplicadorVolumenMusica(final double vol) {
		multiplicadorVolumenMusica = Math.max(0.0, Math.min(1.0, vol));
		actualizarVolumenPrincipal();
		actualizarVolumenAmbiente();
	}

	private static void actualizarVolumenPrincipal() {
		if ((musicaFondoPrincipal != null) && (idMusicaFondoPrincipal != null)) {
			final DatosAudio datos = REGISTRO.get(idMusicaFondoPrincipal);
			if (datos != null) {
				final double vol = datos.getVolumen() * multiplicadorVolumenGeneral * multiplicadorVolumenMusica;
				musicaFondoPrincipal.setVolumen(vol);
			}
		}
	}

	private static void actualizarVolumenAmbiente() {
		if ((musicaAmbienteClima != null) && (idAmbienteClimaActual != null)) {
			final DatosAudio datos = REGISTRO.get(idAmbienteClimaActual);
			if (datos != null) {
				final double vol = datos.getVolumen() * factorAtenuacionAmbiente * multiplicadorVolumenGeneral
						* multiplicadorVolumenMusica;
				musicaAmbienteClima.setVolumen(vol);
			}
		}
	}
}