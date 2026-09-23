package principal.persistencia;

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

import principal.maquinaestado.estados.GestorJuego;
import principal.maquinaestado.estados.GestorPartida;
import principal.utilidades.Globales;

/**
 * Gestor maestro de partidas guardadas con cifrado AES simétrico por ranura (1
 * a 10 slots).
 * 
 * @version 3.0 (Vanilla Java 8 - Encrypted Save/Load Engine)
 */
public final class GestorGuardado {

	public static final int CANTIDAD_SLOTS_MAX = 10;
	private static final String CARPETA_GUARDADOS = "saves";
	private static final String EXTENSION = ".sav";
	private static final int VERSION_GUARDADO = 1;

	private GestorGuardado() {
	}

	public static File obtenerArchivoSlot(final int slot) {
		final File carpeta = new File(CARPETA_GUARDADOS);
		if (!carpeta.exists()) {
			carpeta.mkdirs();
		}
		return new File(carpeta, "slot_" + slot + EXTENSION);
	}

	public static boolean existePartida(final int slot) {
		final File archivo = obtenerArchivoSlot(slot);
		return archivo.exists() && archivo.isFile() && (archivo.length() > 0);
	}

	@SuppressWarnings("unchecked")
	public static boolean guardarPartida(final int slot, final String nombrePersonalizado,
			final GestorJuego gestorJuego) {
		if ((gestorJuego == null) || (gestorJuego.getMapa() == null) || (gestorJuego.getMundo() == null)
				|| (Globales.JUGADOR == null)) {
			System.err.println("[GestorGuardado] Error: Estado del juego no válido para guardar.");
			return false;
		}

		final File archivo = obtenerArchivoSlot(slot);

		try {
			// 1. Capturar el delta del mundo actual antes de serializar
			Globales.GESTOR_DELTAS.capturarDelta(gestorJuego.getMundo(), 0);

			// 2. Construir objeto raíz de guardado
			final JSONObject saveJson = new JSONObject();
			saveJson.put("version", Integer.valueOf(VERSION_GUARDADO));
			saveJson.put("timestamp", Long.valueOf(System.currentTimeMillis()));
			saveJson.put("nombreGuardado",
					((nombrePersonalizado != null) && !nombrePersonalizado.trim().isEmpty())
							? nombrePersonalizado.trim()
							: ("Partida " + slot));
			saveJson.put("mapa", gestorJuego.getMapa().getNombre());
			saveJson.put("mundo", gestorJuego.getMundo().getNombreMundo());

			// 3. Serializar Jugador (con atributos, inventario y efectos)
			saveJson.put("jugador", Globales.JUGADOR.exportarParaJSON());

			if (Globales.GESTOR_RESURRECCION != null) {
				saveJson.put("resurreccion", Globales.GESTOR_RESURRECCION.exportarJSON());
			}

			// 4. Serializar Séquito y Mascotas Activas
			if (Globales.GESTOR_GRUPO != null) {
				saveJson.put("grupo", Globales.GESTOR_GRUPO.exportarJSON());
			}

			// 5. Serializar Deltas de todos los mundos visitados
			saveJson.put("deltas", Globales.GESTOR_DELTAS.exportarJSON());

			// 6. Serializar Bóveda Celeste, Calendario y Mecánica Cósmica
			if (Globales.GESTOR_ASTRONOMICO != null) {
				saveJson.put("astronomia", Globales.GESTOR_ASTRONOMICO.exportarJSON());
			}

			// 7. Serializar Progreso e Historia (Flags)
			if (Globales.GESTOR_PROGRESO != null) {
				saveJson.put("progreso", Globales.GESTOR_PROGRESO.exportarJSON());
			}
			saveJson.put("dificultad", Globales.dificultad.name());
			// 8. Cifrado simétrico AES + Base64
			final String jsonPlano = saveJson.toJSONString();
			final String jsonCifrado = Globales.FUNCIONES.ENCRIPTADOR_STRING.encriptar(jsonPlano);

			// 9. Escribir a disco en UTF-8
			try (final BufferedWriter writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(archivo), StandardCharsets.UTF_8))) {
				writer.write(jsonCifrado);
				writer.flush();
			}

			System.out.println("[GestorGuardado] Partida cifrada y guardada en: " + archivo.getAbsolutePath());
			return true;

		} catch (final Exception e) {
			System.err.println("[GestorGuardado] Error crítico al guardar en Slot " + slot + ": " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	public static boolean guardarPartida(final int slot, final GestorJuego gestorJuego) {
		return guardarPartida(slot, "Partida " + slot, gestorJuego);
	}

	public static JSONObject leerJsonGuardado(final int slot) {
		final File archivo = obtenerArchivoSlot(slot);
		if (!archivo.exists()) {
			return null;
		}

		try (final BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(archivo), StandardCharsets.UTF_8))) {

			final StringBuilder sb = new StringBuilder();
			String linea;
			while ((linea = reader.readLine()) != null) {
				sb.append(linea);
			}

			// Descifrado simétrico automático
			final String jsonDescifrado = Globales.FUNCIONES.ENCRIPTADOR_STRING.desencriptar(sb.toString());
			final JSONParser parser = new JSONParser();
			return (JSONObject) parser.parse(jsonDescifrado);

		} catch (final Exception e) {
			System.err.println(
					"[GestorGuardado] Error al leer/descifrar archivo del Slot " + slot + ": " + e.getMessage());
			return null;
		}
	}

	public static boolean cargarPartida(final int slot, final GestorPartida gestorPartida) {
		final JSONObject saveJson = leerJsonGuardado(slot);
		if ((saveJson == null) || (gestorPartida == null)) {
			return false;
		}

		gestorPartida.cargarPartidaDesdeJSON(saveJson);
		return true;
	}
}