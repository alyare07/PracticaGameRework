package principal.utilidades.audio.musica;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import principal.utilidades.audio.DatosAudio;

/**
 * Administrador global de pistas musicales y bandas sonoras del juego.
 * <p>
 * Se encarga de analizar y registrar las configuraciones de música desde
 * archivos JSON, gestionar el ciclo de vida de la <b>música de fondo
 * principal</b> (BGM) del mapa/estado activo, e instanciar emisores secundarios
 * e independientes en streaming (radios, vehículos, zonas ambientadas).
 * </p>
 * 
 * @author TuNombre / Proyecto Java2D
 */
public class GestorMusica {

	// =========================================================================
	// ATRIBUTOS ESTÁTICOS Y REGISTRO DE DATOS
	// =========================================================================

	/**
	 * Diccionario en memoria RAM que almacena los metadatos (ruta y volumen base)
	 * de cada pista de música cargada desde el JSON, indexados por su ID.
	 */
	private static final Map<String, DatosAudio> REGISTRO = new HashMap<>();

	/** Referencia a la instancia activa de la banda sonora principal del juego. */
	private static MusicaStream musicaFondoPrincipal;

	/**
	 * Identificador único (ID) de la música de fondo principal que está sonando
	 * actualmente.
	 */
	private static String idMusicaFondoPrincipal;

	// Constructor privado para impedir la instanciación de esta clase utilitaria
	// estática
	private GestorMusica() {
	}

	// =========================================================================
	// CARGA Y CONFIGURACIÓN (JSON)
	// =========================================================================

	/**
	 * Carga y registra los metadatos de todas las músicas declaradas en el archivo
	 * JSON especificado.
	 * <p>
	 * Filtra automáticamente las entradas cuyo ID comience con el prefijo
	 * {@code "musicas."}. No carga los datos de audio pesados en RAM, únicamente
	 * sus rutas y volúmenes.
	 * </p>
	 *
	 * @param rutaJson Ubicación del archivo de configuración JSON.
	 */
	public static void cargarMusicasDesdeJSON(final String rutaJson) {
		final JSONParser parser = new JSONParser();

		try (final FileReader reader = new FileReader(rutaJson)) {
			final Object obj = parser.parse(reader);
			final JSONObject jsonObject = (JSONObject) obj;

			for (final Object key : jsonObject.keySet()) {
				final String idMusica = (String) key;

				// Filtrado para registrar únicamente las claves correspondientes a música
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

	// =========================================================================
	// GESTIÓN DE LA MÚSICA DE FONDO PRINCIPAL (BGM Global)
	// =========================================================================

	/**
	 * Inicia la reproducción en bucle de la música de fondo principal de la escena
	 * o nivel.
	 * <p>
	 * Si la pista solicitada ya está sonando como la música principal actual, no se
	 * reinicia. Si había una música principal distinta reproduciéndose previamente,
	 * esta se detiene automáticamente.
	 * </p>
	 *
	 * @param idMusica ID de la música registrada en el JSON.
	 */
	public static void reproducirMusicaFondoPrincipal(final String idMusica) {
		if (idMusica == null) {
			return;
		}

		// Evita reiniciar la misma canción si ya está sonando como música principal
		if (idMusica.equals(idMusicaFondoPrincipal) && (musicaFondoPrincipal != null)) {
			musicaFondoPrincipal.actualizar(true);
			return;
		}

		// Detiene la pista anterior para no superponer bandas sonoras globales
		detenerMusicaFondoPrincipal();

		final DatosAudio datos = REGISTRO.get(idMusica);
		if (datos == null) {
			System.err.println("⚠ GestorMusica: El ID '" + idMusica + "' no existe en el registro.");
			return;
		}

		// Instancia la nueva música en un hilo de streaming independiente
		musicaFondoPrincipal = new MusicaStream(datos.getRuta(), datos.getVolumen());
		musicaFondoPrincipal.repetir(true);
		musicaFondoPrincipal.reproducir();
		idMusicaFondoPrincipal = idMusica;
	}

	/**
	 * Pausa o reanuda la música de fondo principal en función del estado de pausa
	 * del juego.
	 *
	 * @param reproducir {@code true} para mantener/reanudar la reproducción;
	 *                   {@code false} para pausarla.
	 */
	public static void actualizarMusicaFondoPrincipal(final boolean reproducir) {
		if (musicaFondoPrincipal != null) {
			musicaFondoPrincipal.actualizar(reproducir);
		}
	}

	/**
	 * Detiene completamente la música de fondo principal activa y libera su hilo de
	 * ejecución.
	 */
	public static void detenerMusicaFondoPrincipal() {
		if (musicaFondoPrincipal != null) {
			musicaFondoPrincipal.detener();
			musicaFondoPrincipal = null;
			idMusicaFondoPrincipal = null;
		}
	}

	/**
	 * Modifica el volumen de la música de fondo principal en tiempo real.
	 *
	 * @param volumen Porcentaje lineal del volumen (rango de 0.0 a 1.0).
	 */
	public static void setVolumenMusicaFondoPrincipal(final double volumen) {
		if (musicaFondoPrincipal != null) {
			musicaFondoPrincipal.setVolumen(volumen);
		}
	}

	// =========================================================================
	// INSTANCIACIÓN DE FUENTES MÚLTIPLES (Emisores Secundarios / Radios)
	// =========================================================================

	/**
	 * Crea y devuelve un objeto {@link MusicaStream} totalmente independiente.
	 * <p>
	 * Este método permite la coexistencia de múltiples canales de audio
	 * simultáneos. Es ideal para objetos emisores dentro del juego (radios en
	 * casas, vehículos encendidos, zonas ambientales) cuyo volumen varíe mediante
	 * atenuación posicional 2D sin interferir con la música de fondo principal.
	 * </p>
	 *
	 * @param idMusica ID de la pista registrada en el JSON.
	 * @return Una nueva instancia en hilo independiente, o {@code null} si el ID no
	 *         existe.
	 */
	public static MusicaStream obtenerInstancia(final String idMusica) {
		final DatosAudio datos = REGISTRO.get(idMusica);
		if (datos != null) {
			return new MusicaStream(datos.getRuta(), datos.getVolumen());
		}
		System.err.println("⚠ GestorMusica: No se pudo instanciar la música ID '" + idMusica + "'");
		return null;
	}

	// =========================================================================
	// SOBRECARGAS CON ENUM (Seguridad de tipos y autocompletado)
	// =========================================================================

	/**
	 * Sobrecarga de {@link #reproducirMusicaFondoPrincipal(String)} utilizando el
	 * enum {@link IDMusica}.
	 *
	 * @param id Elemento del enum que contiene la clave String válida.
	 */
	public static void reproducirMusicaFondoPrincipal(final IDMusica id) {
		if (id != null) {
			reproducirMusicaFondoPrincipal(id.getId());
		}
	}

	/**
	 * Sobrecarga de {@link #obtenerInstancia(String)} utilizando el enum
	 * {@link IDMusica}.
	 *
	 * @param id Elemento del enum que contiene la clave String válida.
	 * @return Una nueva instancia en hilo independiente, o {@code null} si el ID es
	 *         nulo o inválido.
	 */
	public static MusicaStream obtenerInstancia(final IDMusica id) {
		if (id != null) {
			return obtenerInstancia(id.getId());
		}
		return null;
	}
}