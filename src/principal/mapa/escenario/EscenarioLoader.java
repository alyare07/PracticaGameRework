package principal.mapa.escenario;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.stream.Stream;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import principal.entes.criaturas.Criatura;
import principal.entes.objetos.Complemento;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.Item;
import principal.mapa.Terreno;
import principal.mapa.Tile;
import principal.mapa.mapas.Spawn;
import principal.maquinaestado.estados.editor.metadatos.MetadatosEscenario;
import principal.maquinaestado.estados.pantallaCarga.GestorCarga;
import principal.utilidades.Globales;

/**
 * Gestor de importación y exportación de escenarios con serialización unificada
 * para Terreno, Entidades, Spawns, Triggers, Zonas y Metadatos.
 * 
 * @version 3.0 (Vanilla Java 8)
 */
public abstract class EscenarioLoader {

	private EscenarioLoader() {
	}

	@SuppressWarnings("unchecked")
	public static void exportarEscenario(final Escenario esc, final File ruta) {
		final JSONObject jsonExp = new JSONObject();

		try {
			final JSONParser parser = new JSONParser();
			jsonExp.put(Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Criatura.class),
					parser.parse(esc.LISTA_CREACION_CRIATURAS_JSON));
			jsonExp.put(Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Item.class),
					parser.parse(esc.LISTA_CREACION_ITEMS_JSON));
			jsonExp.put(Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Objeto.class),
					parser.parse(esc.LISTA_CREACION_OBJETOS_JSON));
			jsonExp.put(Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Complemento.class),
					parser.parse(esc.LISTA_CREACION_COMPLEMENTOS_JSON));
			jsonExp.put(Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Spawn.class),
					parser.parse(esc.LISTA_CREACION_SPAWNS_JSON));

			jsonExp.put("triggers", parser.parse(esc.LISTA_CREACION_TRIGGERS_JSON));
			jsonExp.put("zonasAmbiente", parser.parse(esc.LISTA_CREACION_ZONAS_AMBIENTE_JSON));
			jsonExp.put("luces", parser.parse(esc.LISTA_CREACION_LUCES_JSON));

			jsonExp.put("metadatos", esc.getMetadatos().exportarJSON());
			jsonExp.put(Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Tile.class), esc.getTerreno().getTilesJson());

		} catch (final ParseException e) {
			e.printStackTrace();
		}

		final String jsonEncriptado = Globales.FUNCIONES.ENCRIPTADOR_STRING.encriptar(jsonExp.toJSONString());

		try (final PrintWriter pw = new PrintWriter(ruta)) {
			pw.print(jsonEncriptado);
			pw.flush();
			System.out.println("[EscenarioLoader] Escenario exportado en: " + ruta.getAbsolutePath());
		} catch (final IOException e) {
			System.err.println("[EscenarioLoader] Error al exportar escenario: " + e.getMessage());
		}
	}

	public static Escenario importarEscenario(final File ruta) {
		return importarEscenario(ruta, null, 100);
	}

	public static Escenario importarEscenario(final File ruta, final GestorCarga gc, final int porcentajeCarga) {
		Escenario esc = null;
		int pesoCarga = 20;

		try {
			if (gc != null) {
				gc.setDetalleCarga("Leyendo archivo de mapa");
			}

			final StringBuilder sb = new StringBuilder();
			try (Stream<String> stream = Files.lines(ruta.toPath(), StandardCharsets.UTF_8)) {
				stream.forEach(s -> sb.append(s).append("\n"));
			}

			if (gc != null) {
				gc.setPorcentajeCarga(gc.getPorcentaje() + ((pesoCarga * porcentajeCarga) / 100));
			}

			pesoCarga = 20;
			final String jsonImpEncriptado = sb.toString();
			final JSONObject jsonImp = (JSONObject) new JSONParser()
					.parse(Globales.FUNCIONES.ENCRIPTADOR_STRING.desencriptar(jsonImpEncriptado));

			final String jsonCriaturas = obtenerArrayStringSeguro(jsonImp,
					Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Criatura.class));
			final String jsonItems = obtenerArrayStringSeguro(jsonImp,
					Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Item.class));
			final String jsonObjetos = obtenerArrayStringSeguro(jsonImp,
					Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Objeto.class));
			final String jsonComplementos = obtenerArrayStringSeguro(jsonImp,
					Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Complemento.class));
			final String jsonSpawns = obtenerArrayStringSeguro(jsonImp,
					Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Spawn.class));

			final String jsonTriggers = obtenerArrayStringSeguro(jsonImp, "triggers");
			final String jsonZonas = obtenerArrayStringSeguro(jsonImp, "zonasAmbiente");
			final String jsonLuces = obtenerArrayStringSeguro(jsonImp, "luces");

			MetadatosEscenario meta = new MetadatosEscenario();
			if (jsonImp.get("metadatos") instanceof JSONObject) {
				meta = MetadatosEscenario.crearDesdeJSON((JSONObject) jsonImp.get("metadatos"));
			}

			if (gc != null) {
				gc.setPorcentajeCarga(gc.getPorcentaje() + ((pesoCarga * porcentajeCarga) / 100));
			}

			pesoCarga = 60;
			if (gc != null) {
				gc.setDetalleCarga("Construyendo terreno y chunks");
			}

			final JSONObject jsonTerreno = (JSONObject) (new JSONParser())
					.parse(jsonImp.get(Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Tile.class)).toString());

			final Terreno terreno = new Terreno(jsonTerreno);
			esc = new Escenario(terreno, jsonCriaturas, jsonItems, jsonComplementos, jsonObjetos, jsonSpawns,
					jsonTriggers, jsonZonas, jsonLuces, meta);

			if (gc != null) {
				gc.setPorcentajeCarga(gc.getPorcentaje() + ((pesoCarga * porcentajeCarga) / 100));
			}

		} catch (final Exception e) {
			System.err.println("[EscenarioLoader] Error al importar escenario desde " + ruta.getAbsolutePath());
			e.printStackTrace();
		}
		return esc;
	}

	private static String obtenerArrayStringSeguro(final JSONObject json, final String clave) {
		if ((json != null) && (clave != null) && json.containsKey(clave) && (json.get(clave) != null)) {
			return json.get(clave).toString();
		}
		return "[]";
	}
}