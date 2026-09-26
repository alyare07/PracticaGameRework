package principal.mapa.mapas;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import principal.mapa.Mundo;
import principal.persistencia.json.LectorJSON;
import principal.utilidades.Globales;

/**
 * Representa el contrato y manifiesto de configuración de un proyecto de Mapa
 * (.mp). Soporta resolución transparente de submundos (.wld / .json) y
 * desencriptado automático.
 */
public class ManifiestoMapa {

	public static final String NOMBRE_MANIFIESTO = "mapa.mp";
	public static final String NOMBRE_MANIFIESTO_FALLBACK = "mapa.json";

	public enum ModoCarga {
		PESADO, // Exclusión mutua: Solo 1 en RAM (Exteriores o Mazmorras gigantes)
		LIGERO // Caché en RAM: Múltiples en memoria (Casas, tiendas, cuevas chicas)
	}

	public static class EntradaSubmundo {
		private final String id;
		private final String archivoRelativo;
		private final ModoCarga modoCarga;
		private final String tipoAmbiente;

		public EntradaSubmundo(final String id, final String archivoRelativo, final ModoCarga modoCarga,
				final String tipoAmbiente) {
			this.id = id;
			this.archivoRelativo = archivoRelativo;
			this.modoCarga = modoCarga;
			this.tipoAmbiente = tipoAmbiente;
		}

		public String getId() {
			return this.id;
		}

		public String getArchivoRelativo() {
			return this.archivoRelativo;
		}

		public ModoCarga getModoCarga() {
			return this.modoCarga;
		}

		public boolean esPesado() {
			return this.modoCarga == ModoCarga.PESADO;
		}

		public String getTipoAmbiente() {
			return this.tipoAmbiente;
		}
	}

	private final String idMapa;
	private final String nombreVisible;
	private final String mundoComienzo;
	private final String spawnComienzo;
	private final Map<String, EntradaSubmundo> submundos;
	private final List<String> ordenSubmundos;

	public ManifiestoMapa(final String idMapa, final String nombreVisible, final String mundoComienzo,
			final String spawnComienzo, final Map<String, EntradaSubmundo> submundos,
			final List<String> ordenSubmundos) {
		this.idMapa = idMapa;
		this.nombreVisible = nombreVisible;
		this.mundoComienzo = mundoComienzo;
		this.spawnComienzo = spawnComienzo;
		this.submundos = Collections.unmodifiableMap(submundos);
		this.ordenSubmundos = Collections.unmodifiableList(ordenSubmundos);
	}

	/**
	 * Carga el manifiesto inspeccionando la carpeta del proyecto. Aplica Content
	 * Sniffing para desencriptar con AES solo si es necesario.
	 */
	public static ManifiestoMapa cargarDesdeDirectorio(final File directorioMapa) {
		if ((directorioMapa == null) || !directorioMapa.isDirectory()) {
			System.err.println("[ManifiestoMapa] Directorio inválido: " + directorioMapa);
			return null;
		}

		File archivoManifiesto = new File(directorioMapa, NOMBRE_MANIFIESTO);
		if (!archivoManifiesto.exists()) {
			archivoManifiesto = new File(directorioMapa, NOMBRE_MANIFIESTO_FALLBACK);
			if (!archivoManifiesto.exists()) {
				System.err.println("[ManifiestoMapa] No se encontró " + NOMBRE_MANIFIESTO + " en: "
						+ directorioMapa.getAbsolutePath());
				return null;
			}
		}

		try {
			final StringBuilder sb = new StringBuilder();
			try (final BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(archivoManifiesto), StandardCharsets.UTF_8))) {
				String linea;
				while ((linea = reader.readLine()) != null) {
					sb.append(linea);
				}
			}

			String contenido = sb.toString().trim();
			// Content Sniffing: Si no inicia con llave '{', asumimos cifrado AES
			if (!contenido.startsWith("{")) {
				contenido = Globales.FUNCIONES.ENCRIPTADOR_STRING.desencriptar(contenido);
			}

			final JSONObject json = (JSONObject) new JSONParser().parse(contenido);
			final String idMapa = LectorJSON.getString(json, "idMapa", directorioMapa.getName());
			final String nombreVisible = LectorJSON.getString(json, "nombreVisible", idMapa);
			final String mundoComienzo = LectorJSON.getString(json, "mundoComienzo", "exterior");
			final String spawnComienzo = LectorJSON.getString(json, "spawnComienzo", Mundo.CLAVE_PUNTO_SPAWN_COMIENZO);

			final Map<String, EntradaSubmundo> mapaSubmundos = new HashMap<>();
			final List<String> orden = new ArrayList<>();

			final JSONArray arrSubmundos = LectorJSON.getArray(json, "submundos");
			if (arrSubmundos != null) {
				for (final Object obj : arrSubmundos) {
					if (obj instanceof JSONObject) {
						final JSONObject jSub = (JSONObject) obj;
						final String idSub = LectorJSON.getString(jSub, "id", "");
						final String archivo = LectorJSON.getString(jSub, "archivo", "submundos/" + idSub + ".wld");
						final String tipoAmb = LectorJSON.getString(jSub, "tipoAmbiente", "EXTERIOR");

						// Por defecto, EXTERIOR es PESADO; INTERIOR/CUEVA son LIGEROS salvo que se
						// especifique lo contrario
						ModoCarga modo = "EXTERIOR".equalsIgnoreCase(tipoAmb) ? ModoCarga.PESADO : ModoCarga.LIGERO;
						if (jSub.containsKey("modoCarga")) {
							modo = ModoCarga
									.valueOf(LectorJSON.getString(jSub, "modoCarga", modo.name()).toUpperCase());
						}

						final EntradaSubmundo entrada = new EntradaSubmundo(idSub, archivo, modo, tipoAmb);
						mapaSubmundos.put(idSub.toLowerCase(), entrada);
						orden.add(idSub);
					}
				}
			}

			return new ManifiestoMapa(idMapa, nombreVisible, mundoComienzo, spawnComienzo, mapaSubmundos, orden);

		} catch (final Exception e) {
			System.err.println(
					"[ManifiestoMapa] Error crítico al leer manifiesto en: " + archivoManifiesto.getAbsolutePath());
			e.printStackTrace();
			return null;
		}
	}

	public EntradaSubmundo getSubmundo(final String idSubmundo) {
		if (idSubmundo == null) {
			return null;
		}
		return this.submundos.get(idSubmundo.toLowerCase());
	}

	/**
	 * Resuelve la ubicación del archivo en disco con soporte para extensión .wld o
	 * .json
	 */
	public File resolverArchivoSubmundo(final File directorioMapa, final String idSubmundo) {
		final EntradaSubmundo entrada = this.getSubmundo(idSubmundo);
		if (entrada == null) {
			return null;
		}

		final File f = new File(directorioMapa, entrada.getArchivoRelativo());
		if (f.exists()) {
			return f;
		}

		// Fallback de extensión: Si buscaba .wld y no existe, prueba .json (o
		// viceversa)
		final String ruta = entrada.getArchivoRelativo();
		if (ruta.endsWith(".wld")) {
			final File fJson = new File(directorioMapa, ruta.substring(0, ruta.length() - 4) + ".json");
			if (fJson.exists()) {
				return fJson;
			}
		} else if (ruta.endsWith(".json")) {
			final File fWld = new File(directorioMapa, ruta.substring(0, ruta.length() - 5) + ".wld");
			if (fWld.exists()) {
				return fWld;
			}
		}

		return f; // Retorna f original (fallará limpiamente en el loader si no existe)
	}

	public String getIdMapa() {
		return this.idMapa;
	}

	public String getNombreVisible() {
		return this.nombreVisible;
	}

	public String getMundoComienzo() {
		return this.mundoComienzo;
	}

	public String getSpawnComienzo() {
		return this.spawnComienzo;
	}

	public List<String> getListaIdsSubmundos() {
		return this.ordenSubmundos;
	}
}