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
 * Orquestador maestro de persistencia y gestión de partidas guardadas
 * (Save/Load). Centraliza la serialización y lectura del estado del juego en
 * formato JSON.
 * 
 * @version 1.1 (Vanilla Java 8 - Atomic Persistence Engine)
 */
public final class GestorGuardado {

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
	public static boolean guardarPartida(final int slot, final GestorJuego gestorJuego) {
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
			saveJson.put("mapa", gestorJuego.getMapa().getNombre());
			saveJson.put("mundo", gestorJuego.getMundo().getNombreMundo());

			// 3. Serializar Jugador (Posición, atributos, vida, dinero e inventario)
			saveJson.put("jugador", Globales.JUGADOR.exportarParaJSON());

			// 4. Serializar Deltas de todos los mundos visitados
			saveJson.put("deltas", Globales.GESTOR_DELTAS.exportarJSON());

			// 5. Serializar Calendario y Tiempo (Ciclo solar de 24h)
			if ((Globales.GESTOR_LUZ != null) && (Globales.GESTOR_LUZ.getCiclo() != null)) {
				saveJson.put("calendario", Globales.GESTOR_LUZ.getCiclo().exportarJSON());
			}

			// 6. Serializar Progreso e Historia (Flags)
			if (Globales.GESTOR_PROGRESO != null) {
				saveJson.put("progreso", Globales.GESTOR_PROGRESO.exportarJSON());
			}

			// 7. Escribir atómicamente a disco en UTF-8
			try (final BufferedWriter writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(archivo), StandardCharsets.UTF_8))) {
				writer.write(saveJson.toJSONString());
				writer.flush();
			}

			System.out.println("[GestorGuardado] Partida guardada exitosamente en: " + archivo.getAbsolutePath());
			return true;

		} catch (final Exception e) {
			System.err.println("[GestorGuardado] Error crítico al guardar en Slot " + slot + ": " + e.getMessage());
			e.printStackTrace();
			return false;
		}
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

			final JSONParser parser = new JSONParser();
			return (JSONObject) parser.parse(sb.toString());

		} catch (final Exception e) {
			System.err.println("[GestorGuardado] Error al leer archivo del Slot " + slot + ": " + e.getMessage());
			e.printStackTrace();
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