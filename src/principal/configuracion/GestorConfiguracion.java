package principal.configuracion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import principal.utilidades.Globales;
import principal.utilidades.audio.musica.GestorMusica;
import principal.utilidades.audio.sonido.GestorSonido;

/**
 * Administrador centralizado de configuración unificada del motor. Consolida
 * Configuración General (Audio/Volúmenes), Video y Teclado en un único archivo
 * maestro cifrado ('Config.dat').
 * 
 * @version 2.0 (Vanilla Java 8 - Audio Engine Synchronization)
 */
public final class GestorConfiguracion {

	private static final File ARCHIVO_CONFIG = new File("Config.dat");

	// --- Volúmenes de Audio Globales (0.0 a 1.0) ---
	private static double volumenGeneral = 1.0;
	private static double volumenMusica = 0.8;
	private static double volumenEfectos = 1.0;

	private GestorConfiguracion() {
	}

	public static void inicializar() {
		if (!cargarConfiguracion()) {
			ConfiguracionGrafica.detectarConfiguracionOptima();
			guardarConfiguracion();
		}
		ConfiguracionGrafica.aplicar();
		aplicarAudio();
	}

	public static void aplicarAudio() {
		GestorMusica.setMultiplicadorVolumenGeneral(volumenGeneral);
		GestorMusica.setMultiplicadorVolumenMusica(volumenMusica);
		GestorSonido.setMultiplicadorVolumenGeneral(volumenGeneral);
		GestorSonido.setMultiplicadorVolumenEfectos(volumenEfectos);
	}

	// =========================================================================
	// GUARDADO UNIFICADO CIFRADO
	// =========================================================================

	@SuppressWarnings("unchecked")
	public static synchronized void guardarConfiguracion() {
		final JSONObject jsonRaiz = new JSONObject();

		// 1. Sección Gráfica y Pantalla
		jsonRaiz.put("grafica", ConfiguracionGrafica.exportarJSON());

		// 2. Sección Teclado y Controles
		if (Globales.TECLADO != null) {
			jsonRaiz.put("teclado", Globales.TECLADO.getConfigJson());
		}

		// 3. Sección General y Audio
		final JSONObject jsonAudio = new JSONObject();
		jsonAudio.put("volumenGeneral", Double.valueOf(volumenGeneral));
		jsonAudio.put("volumenMusica", Double.valueOf(volumenMusica));
		jsonAudio.put("volumenEfectos", Double.valueOf(volumenEfectos));
		jsonRaiz.put("audio", jsonAudio);

		// 4. Cifrado simétrico AES + Base64
		final String textoPlano = jsonRaiz.toJSONString();
		final String textoCifrado = Globales.FUNCIONES.ENCRIPTADOR_STRING.encriptar(textoPlano);

		try (final BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(ARCHIVO_CONFIG), StandardCharsets.UTF_8))) {
			writer.write(textoCifrado);
			writer.flush();
		} catch (final Exception e) {
			System.err.println("[GestorConfiguracion] Error al guardar configuración: " + e.getMessage());
		}
	}

	// =========================================================================
	// CARGA Y DESENCRIPTADO UNIFICADO
	// =========================================================================

	public static synchronized boolean cargarConfiguracion() {
		if (!ARCHIVO_CONFIG.exists() || (ARCHIVO_CONFIG.length() == 0)) {
			return false;
		}

		try (final BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(ARCHIVO_CONFIG), StandardCharsets.UTF_8))) {

			final StringBuilder sb = new StringBuilder();
			String linea;
			while ((linea = reader.readLine()) != null) {
				sb.append(linea);
			}

			final String textoDescifrado = Globales.FUNCIONES.ENCRIPTADOR_STRING.desencriptar(sb.toString());
			final JSONObject jsonRaiz = (JSONObject) (new JSONParser()).parse(textoDescifrado);

			if (jsonRaiz == null) {
				return false;
			}

			// 1. Cargar sección gráfica
			if (jsonRaiz.get("grafica") instanceof JSONObject) {
				ConfiguracionGrafica.importarJSON((JSONObject) jsonRaiz.get("grafica"));
			}

			// 2. Cargar sección teclado
			if ((jsonRaiz.get("teclado") instanceof JSONObject) && (Globales.TECLADO != null)) {
				Globales.TECLADO.establecerConfig((JSONObject) jsonRaiz.get("teclado"));
			}

			// 3. Cargar sección de audio
			if (jsonRaiz.get("audio") instanceof JSONObject) {
				final JSONObject jAudio = (JSONObject) jsonRaiz.get("audio");
				if (jAudio.get("volumenGeneral") != null) {
					volumenGeneral = ((Number) jAudio.get("volumenGeneral")).doubleValue();
				}
				if (jAudio.get("volumenMusica") != null) {
					volumenMusica = ((Number) jAudio.get("volumenMusica")).doubleValue();
				}
				if (jAudio.get("volumenEfectos") != null) {
					volumenEfectos = ((Number) jAudio.get("volumenEfectos")).doubleValue();
				}
			}

			aplicarAudio();
			return true;

		} catch (final Exception e) {
			System.err.println("[GestorConfiguracion] Error al leer configuración: " + e.getMessage());
			return false;
		}
	}

	// =========================================================================
	// GETTERS & SETTERS
	// =========================================================================

	public static double getVolumenGeneral() {
		return volumenGeneral;
	}

	public static void setVolumenGeneral(final double vol) {
		volumenGeneral = Math.max(0.0, Math.min(1.0, vol));
		aplicarAudio();
	}

	public static double getVolumenMusica() {
		return volumenMusica;
	}

	public static void setVolumenMusica(final double vol) {
		volumenMusica = Math.max(0.0, Math.min(1.0, vol));
		aplicarAudio();
	}

	public static double getVolumenEfectos() {
		return volumenEfectos;
	}

	public static void setVolumenEfectos(final double vol) {
		volumenEfectos = Math.max(0.0, Math.min(1.0, vol));
		aplicarAudio();
	}
}