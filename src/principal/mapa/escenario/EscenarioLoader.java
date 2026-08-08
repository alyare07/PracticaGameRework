package principal.mapa.escenario;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.stream.Stream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import principal.entes.criaturas.Criatura;
import principal.entes.objetos.Complemento;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.Item;
import principal.mapa.Terreno;
import principal.mapa.Tile;
import principal.maquinaestado.estados.pantallaCarga.GestorCarga;
import principal.utilidades.Constantes;

public abstract class EscenarioLoader{
    private EscenarioLoader() {
    }

    @SuppressWarnings("unchecked")
    public static void exportarEscenario(final Escenario esc, final File ruta) {
	final String criaturas = esc.LISTA_CREACION_CRIATURAS_JSON;
	final String items = esc.LISTA_CREACION_ITEMS_JSON;
	final String objetos = esc.LISTA_CREACION_OBJETOS_JSON;
	final String complementos = esc.LISTA_CREACION_COMPLEMENTOS_JSON;
	final String terreno = null;
	final JSONObject jsonExp = new JSONObject();
	JSONArray jsonCriatura = null;
	JSONArray jsonItems = null;
	JSONArray jsonObjetos = null;
	JSONArray jsonComplementos = null;
	try {
	    jsonCriatura = (JSONArray) new JSONParser().parse(criaturas);
	    jsonItems = (JSONArray) new JSONParser().parse(items);
	    jsonObjetos = (JSONArray) new JSONParser().parse(objetos);
	    jsonComplementos = (JSONArray) new JSONParser().parse(complementos);
	} catch (final ParseException e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	}

	jsonExp.put(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Criatura.class), jsonCriatura);
	jsonExp.put(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Item.class), jsonItems);
	jsonExp.put(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Objeto.class), jsonObjetos);
	jsonExp.put(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Complemento.class), jsonComplementos);
	jsonExp.put(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Tile.class), esc.getTerreno().getTilesJson());

	final String jsonExpEncriptado = Constantes.FUNCIONES.ENCRIPTADOR_STRING.encriptar(jsonExp.toJSONString());

	PrintWriter pw = null;
	try {
	    pw = new PrintWriter(ruta);
	    pw.print(jsonExpEncriptado);
	    pw.flush();
	    System.out.println("Mundo exportado en:  " + ruta);
	} catch (final IOException e) {
	    System.out.println("Error al exportar escenario: " + e.getMessage());
	} finally {
	    pw.close();
	}
    }

    public static Escenario importarEscenario(final File ruta) {
	Escenario esc = null;
	try {
	    final StringBuilder contentBuilder = new StringBuilder();

	    try (Stream<String> stream = Files.lines(ruta.toPath(), StandardCharsets.UTF_8)) {

		stream.forEach(s -> contentBuilder.append(s).append("\n"));
	    } catch (final IOException e) {
		System.out.println("Error al importar escenario: " + e.getMessage());
	    }
	    final String jsonImpEncriptado = contentBuilder.toString();
	    final JSONObject jsonImp = (JSONObject) new JSONParser().parse(Constantes.FUNCIONES.ENCRIPTADOR_STRING.desencriptar(jsonImpEncriptado));
	    final String jsonCriaturas = jsonImp.get(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Criatura.class)).toString();
	    final String jsonItems = jsonImp.get(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Item.class)).toString();
	    final String jsonObjetos = jsonImp.get(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Objeto.class)).toString();
	    final String jsonComplementos = jsonImp.get(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Complemento.class)).toString();
	    Terreno terreno = null;
	    final JSONObject jsonTerreno = (JSONObject) (new JSONParser()).parse(jsonImp.get(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Tile.class)).toString());

	    terreno = new Terreno(jsonTerreno);
	    esc = new Escenario(terreno, jsonCriaturas, jsonItems, jsonComplementos, jsonObjetos);

	} catch (final Exception e) {
	    System.out.println("Error al importar escenario: " + e.getMessage());
	    e.printStackTrace();
	} finally {
	    try {
	    } catch (final Exception e2) {
		e2.printStackTrace();
	    }
	}
	return esc;
    }

    public static Escenario importarEscenario(final File ruta, final GestorCarga gc, final int porcentajeCarga) {
	Escenario esc = null;
	///////
	int pesoCarga = 20;
	try {
	    gc.setDetalleCarga("Cargando archivos");
	    final StringBuilder contentBuilder = new StringBuilder();
	    try (Stream<String> stream = Files.lines(ruta.toPath(), StandardCharsets.UTF_8)) {

		stream.forEach(s -> contentBuilder.append(s).append("\n"));
	    } catch (final IOException e) {
		System.out.println("Error al importar escenario: " + e.getMessage());
	    }
	    gc.setPorcentajeCarga(gc.getPorcentaje() + (pesoCarga * porcentajeCarga / 100));
	    ////////////////////
	    pesoCarga = 20;
	    gc.setDetalleCarga("Leyendo datos");
	    final String jsonImpEncriptado = contentBuilder.toString();
	    final JSONObject jsonImp = (JSONObject) new JSONParser().parse(Constantes.FUNCIONES.ENCRIPTADOR_STRING.desencriptar(jsonImpEncriptado));
	    final String jsonCriaturas = jsonImp.get(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Criatura.class)).toString();
	    final String jsonItems = jsonImp.get(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Item.class)).toString();
	    final String jsonObjetos = jsonImp.get(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Objeto.class)).toString();
	    final String jsonComplementos = jsonImp.get(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Complemento.class)).toString();
	    Terreno terreno = null;
	    final JSONObject jsonTerreno = (JSONObject) (new JSONParser()).parse(jsonImp.get(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Tile.class)).toString());
	    gc.setPorcentajeCarga(gc.getPorcentaje() + (pesoCarga * porcentajeCarga / 100));
	    ///////////////
	    pesoCarga = 60;
	    gc.setDetalleCarga("Generando terreno");
	    terreno = new Terreno(jsonTerreno);
	    esc = new Escenario(terreno, jsonCriaturas, jsonItems, jsonComplementos, jsonObjetos);
	    gc.setPorcentajeCarga(gc.getPorcentaje() + (pesoCarga * porcentajeCarga / 100));

	} catch (final Exception e) {
	    System.out.println("Error al importar escenario: " + e.getMessage());
	    e.printStackTrace();
	} finally {
	    try {
	    } catch (final Exception e2) {
		e2.printStackTrace();
	    }
	}
	return esc;
    }

//    private static String serializableAString(final Serializable o) throws IOException {
//	final ByteArrayOutputStream baos = new ByteArrayOutputStream();
//	final ObjectOutputStream oos = new ObjectOutputStream(baos);
//	oos.writeObject(o);
//	oos.close();
//	return Base64.getEncoder().encodeToString(baos.toByteArray());
//    }
//
//    private static Object stringASerializable(final String s) throws IOException, ClassNotFoundException {
//	final byte[] data = Base64.getDecoder().decode(s);
//	final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
//	final Object o = ois.readObject();
//	ois.close();
//	return o;
//    }

}
