package principal.mapa.escenario;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import principal.mapa.Terreno;
import principal.maquinaestado.estados.editor.metadatos.MetadatosEscenario;
import principal.maquinaestado.estados.pantallaCarga.GestorCarga;
import principal.persistencia.json.LectorJSON;
import principal.utilidades.Globales;

/**
 * Gestor de importación y exportación de escenarios con cifrado simétrico AES.
 * Trabaja directamente sobre estructuras JSON sin re-parseos intermedios.
 */
public abstract class EscenarioLoader {

	private EscenarioLoader() {
	}

	@SuppressWarnings("unchecked")
	public static void exportarEscenario(final Escenario esc, final File ruta) {
		final JSONObject jsonExp = new JSONObject();

		jsonExp.put("criaturas", esc.getListaCriaturas());
		jsonExp.put("items", esc.getListaItems());
		jsonExp.put("complementos", esc.getListaComplementos());
		jsonExp.put("objetos", esc.getListaObjetos());
		jsonExp.put("spawns", esc.getListaSpawns());

		jsonExp.put("triggers", esc.getListaTriggers());
		jsonExp.put("zonasAmbiente", esc.getListaZonasAmbiente());
		jsonExp.put("luces", esc.getListaLuces());

		jsonExp.put("metadatos", esc.getMetadatos().exportarJSON());
		jsonExp.put("terreno", esc.getTerreno().getTilesJson());

		final String jsonPlano = jsonExp.toJSONString();
		final String jsonEncriptado = Globales.FUNCIONES.ENCRIPTADOR_STRING.encriptar(jsonPlano);

		try (final BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(ruta), StandardCharsets.UTF_8))) {
			writer.write(jsonEncriptado);
			writer.flush();
			System.out.println("[EscenarioLoader] Escenario exportado correctamente en: " + ruta.getAbsolutePath());
		} catch (final Exception e) {
			System.err.println("[EscenarioLoader] Error al exportar escenario: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static Escenario importarEscenario(final File ruta) {
		return importarEscenario(ruta, null, 100);
	}

	public static Escenario importarEscenario(final File ruta, final GestorCarga gc, final int porcentajeCarga) {
		Escenario esc = null;
		final int pesoCarga = 20;

		try {
			if (gc != null) {
				gc.setDetalleCarga("Leyendo archivo de mapa");
			}

			final StringBuilder sb = new StringBuilder();
			try (final BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(ruta), StandardCharsets.UTF_8))) {
				String linea;
				while ((linea = reader.readLine()) != null) {
					sb.append(linea);
				}
			}

			if (gc != null) {
				gc.setPorcentajeCarga(gc.getPorcentaje() + ((pesoCarga * porcentajeCarga) / 100));
			}

			final String textoDescifrado = Globales.FUNCIONES.ENCRIPTADOR_STRING.desencriptar(sb.toString());
			final JSONObject jsonImp = (JSONObject) new JSONParser().parse(textoDescifrado);

			final JSONArray arrCriaturas = LectorJSON.getArray(jsonImp, "criaturas");
			final JSONArray arrItems = LectorJSON.getArray(jsonImp, "items");
			final JSONArray arrComplementos = LectorJSON.getArray(jsonImp, "complementos");
			final JSONArray arrObjetos = LectorJSON.getArray(jsonImp, "objetos");
			final JSONArray arrSpawns = LectorJSON.getArray(jsonImp, "spawns");

			final JSONArray arrTriggers = LectorJSON.getArray(jsonImp, "triggers");
			final JSONArray arrZonas = LectorJSON.getArray(jsonImp, "zonasAmbiente");
			final JSONArray arrLuces = LectorJSON.getArray(jsonImp, "luces");

			MetadatosEscenario meta = new MetadatosEscenario();
			final JSONObject jsonMeta = LectorJSON.getObjeto(jsonImp, "metadatos");
			if (jsonMeta != null) {
				meta = MetadatosEscenario.crearDesdeJSON(jsonMeta);
			}

			if (gc != null) {
				gc.setDetalleCarga("Construyendo terreno");
			}

			// Compatibilidad de clave para terreno
			JSONObject jsonTerreno = LectorJSON.getObjeto(jsonImp, "terreno");
			if (jsonTerreno == null) {
				jsonTerreno = LectorJSON.getObjeto(jsonImp, "principal.mapa.Tile");
			}

			final Terreno terreno = new Terreno(jsonTerreno);
			esc = new Escenario(terreno, arrCriaturas, arrItems, arrComplementos, arrObjetos, arrSpawns, arrTriggers,
					arrZonas, arrLuces, meta);

			if (gc != null) {
				gc.setPorcentajeCarga(gc.getPorcentaje() + ((60 * porcentajeCarga) / 100));
			}

		} catch (final Exception e) {
			System.err.println("[EscenarioLoader] Error al importar escenario desde " + ruta.getAbsolutePath());
			e.printStackTrace();
		}
		return esc;
	}
}