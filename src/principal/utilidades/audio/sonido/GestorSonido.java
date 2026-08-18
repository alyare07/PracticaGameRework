package principal.utilidades.audio.sonido;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import principal.utilidades.Globales;

/**
 * Gestor global y centralizado de efectos de sonido (SFX).
 * <p>
 * Encargado de precargar los archivos de audio de corta duración (disparos,
 * explosiones, golpes, UI) directamente en la memoria RAM durante la fase de
 * inicio para garantizar una latencia cero ($0\text{ ms}$) en el Game Loop.
 * Incluye soporte para atenuación posicional $2\text{D}$ y polifonía por
 * clonación rápida.
 * </p>
 * 
 */
public class GestorSonido {

	// =========================================================================
	// ATRIBUTOS ESTÁTICOS Y REGISTRO EN MEMORIA RAM
	// =========================================================================

	/**
	 * Diccionario que almacena las instancias base de {@link SonidoJavaSound}
	 * precargadas en memoria RAM al inicio del juego, indexadas por su ID único.
	 */
	private static final Map<String, SonidoJavaSound> REGISTRO = new HashMap<>();

	// Constructor privado para evitar la instanciación de esta clase estática
	private GestorSonido() {
	}

	// =========================================================================
	// CARGA Y PREPROCESAMIENTO (JSON -> RAM)
	// =========================================================================

	/**
	 * Lee el archivo de configuración JSON, decodifica los archivos de audio (.wav
	 * / .mp3) y los almacena listos para ser reproducidos instantáneamente desde la
	 * memoria RAM.
	 *
	 * @param rutaJson Ubicación del archivo JSON que contiene los registros de los
	 *                 sonidos.
	 */
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

				// Precarga completa del buffer de audio en RAM a través de la fábrica
				final SonidoJavaSound sonido = FabricaSonido.crearSonido(ruta, volumen);
				if (sonido != null) {
					REGISTRO.put(idSonido, sonido);
				}
			}

			System.out
					.println("GestorSonido: Se precargaron " + REGISTRO.size() + " efectos de sonido en memoria RAM.");

		} catch (final Exception e) {
			System.err.println("⚠ GestorSonido: Error al cargar el archivo de sonidos JSON desde '" + rutaJson + "'");
			e.printStackTrace();
		}
	}

	// =========================================================================
	// REPRODUCCIÓN ESTÁNDAR Y POSICIONAL 2D
	// =========================================================================

	/**
	 * Reproduce un efecto de sonido de forma global utilizando su volumen original
	 * (JSON).
	 * <p>
	 * Ejecuta internamente un {@code resetVolumen()} para garantizar que el volumen
	 * no quede alterado por atenuaciones posicionales previas.
	 * </p>
	 *
	 * @param idSonido Identificador único del sonido registrado.
	 */
	public static void reproducir(final String idSonido) {
		final SonidoJavaSound sonido = REGISTRO.get(idSonido);

		if (sonido == null) {
			System.err.println("⚠ GestorSonido: El ID '" + idSonido + "' no existe en el registro.");
			return;
		}

		// Restaura el volumen nominal del JSON por si había sido atenuado
		sonido.resetVolumen();
		sonido.reproducir();
	}

	/**
	 * Reproduce un sonido modulando su volumen en tiempo real en función de la
	 * distancia $2\text{D}$ entre el origen del sonido (emisor) y la ubicación del
	 * jugador/cámara (receptor).
	 *
	 * @param idSonido    ID del sonido a reproducir.
	 * @param xEmisor     Coordenada X de la entidad u objeto que emite el sonido.
	 * @param yEmisor     Coordenada Y de la entidad u objeto que emite el sonido.
	 * @param xReceptor   Coordenada X del oyente (Jugador / Centro de la pantalla).
	 * @param yReceptor   Coordenada Y del oyente.
	 * @param radioMaximo Distancia en píxeles a la cual el sonido se vuelve
	 *                    inaudible.
	 */
	public static void reproducirEnPosicion(final String idSonido, final double xEmisor, final double yEmisor,
			final double xReceptor, final double yReceptor, final double radioMaximo) {

		final SonidoJavaSound sonido = REGISTRO.get(idSonido);

		if (sonido == null) {
			System.err.println("⚠ GestorSonido: No se encontró el sonido posicional con ID '" + idSonido + "'");
			return;
		}

		// Distancia en línea recta entre emisor y receptor (Pitágoras)
		final double distancia = Math.hypot(xEmisor - xReceptor, yEmisor - yReceptor);

		// Si está fuera del rango máximo de audición, se descarta para no gastar
		// hardware
		if (distancia >= radioMaximo) {
			return;
		}

		// Factor de atenuación lineal (1.0 = distancia cero, 0.0 = al borde del radio)
		final double factorDistancia = Math.max(0.0, 1.0 - (distancia / radioMaximo));

		// Modula el volumen basándose en el volumen por defecto original
		final double volumenFinal = sonido.getVolumenPorDefecto() * factorDistancia;

		sonido.setVolumen(volumenFinal);
		sonido.reproducir();
	}

	/**
	 * Sobrecarga de reproducción posicional $2\text{D}$ utilizando el radio de
	 * distancia máximo global.
	 *
	 * @param idSonido  ID del sonido a reproducir.
	 * @param xEmisor   Coordenada X del emisor.
	 * @param yEmisor   Coordenada Y del emisor.
	 * @param xReceptor Coordenada X del receptor.
	 * @param yReceptor Coordenada Y del receptor.
	 */
	public static void reproducirEnPosicion(final String idSonido, final double xEmisor, final double yEmisor,
			final double xReceptor, final double yReceptor) {
		reproducirEnPosicion(idSonido, xEmisor, yEmisor, xReceptor, yReceptor,
				Globales.CONSTANTES.RADIO_AUDIO_DISTANCIA_MAXIMA);
	}

	// =========================================================================
	// CLONACIÓN Y POLIFONÍA (Instancias independiente en RAM)
	// =========================================================================

	/**
	 * Devuelve un clon independiente en memoria RAM de la pista precargada.
	 * <p>
	 * Útil cuando se necesita asignar un sonido propio a una entidad que debe sonar
	 * en simultáneo sin interrumpir o reiniciar el clip de otras entidades.
	 * </p>
	 *
	 * @param idSonido ID del sonido base registrado.
	 * @return Nueva instancia duplicada de {@link SonidoJavaSound}, o {@code null}
	 *         si no existe.
	 */
	public static SonidoJavaSound obtenerInstancia(final String idSonido) {
		final SonidoJavaSound sonidoBase = REGISTRO.get(idSonido);
		if (sonidoBase != null) {
			return sonidoBase.clonar();
		}
		return null;
	}

	// =========================================================================
	// SOBRECARGAS CON ENUM (IDSonido)
	// =========================================================================

	/**
	 * Sobrecarga de {@link #reproducir(String)} mediante Enum.
	 */
	public static void reproducir(final IDSonido id) {
		if (id != null) {
			reproducir(id.getId());
		}
	}

	/**
	 * Sobrecarga de
	 * {@link #reproducirEnPosicion(String, double, double, double, double, double)}
	 * mediante Enum.
	 */
	public static void reproducirEnPosicion(final IDSonido id, final double xEmisor, final double yEmisor,
			final double xReceptor, final double yReceptor, final double radioMaximo) {
		if (id != null) {
			reproducirEnPosicion(id.getId(), xEmisor, yEmisor, xReceptor, yReceptor, radioMaximo);
		}
	}

	/**
	 * Sobrecarga de
	 * {@link #reproducirEnPosicion(String, double, double, double, double)}
	 * mediante Enum.
	 */
	public static void reproducirEnPosicion(final IDSonido id, final double xEmisor, final double yEmisor,
			final double xReceptor, final double yReceptor) {
		if (id != null) {
			reproducirEnPosicion(id.getId(), xEmisor, yEmisor, xReceptor, yReceptor);
		}
	}

	/**
	 * Sobrecarga de {@link #obtenerInstancia(String)} mediante Enum.
	 */
	public static SonidoJavaSound obtenerInstancia(final IDSonido id) {
		if (id != null) {
			return obtenerInstancia(id.getId());
		}
		return null;
	}
}