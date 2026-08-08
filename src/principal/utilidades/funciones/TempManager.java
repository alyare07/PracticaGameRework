package principal.utilidades.funciones;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.stream.Stream;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Encargado de crear, actualizar y eliminar un archivo temporal en el cual se
 * almacena un json con los datos que se le soliciten.
 */
public class TempManager{
    public final File ARCHIVO = new File("temp.json");
    private final EncriptadorString ENCRIPTADOR_STRING = new EncriptadorString();

    protected TempManager() {
	this.eliminarTemp();
	this.generarTemp();
    }

    /**
     * Elimina el archivo temporal
     */
    public void eliminarTemp() {
	if (this.ARCHIVO.exists()) {
	    this.ARCHIVO.delete();
	}
    }

    public void reiniciarTemp() {
	this.eliminarTemp();
	this.generarTemp();
    }

    /**
     * Crear el archivo temporal
     */
    private void generarTemp() {
	if (!this.ARCHIVO.exists()) {
	    final JSONObject json = new JSONObject();
	    try {
		final PrintWriter pw = new PrintWriter(this.ARCHIVO);
		pw.print(this.ENCRIPTADOR_STRING.encriptar(json.toJSONString()));
		pw.flush();
		pw.close();
	    } catch (final IOException e) {
		e.printStackTrace();
		System.out.println("Error: " + e.getMessage());
	    }
	}
    }

    /**
     * Carga el archivo temporal y devuelve el JsonObject almacenado.
     * 
     * @return El JsonObject almacenado en el archivo temporal.
     */
    public JSONObject getJsonTemp() {
	JSONObject json = null;
	try {
	    final StringBuilder sb = new StringBuilder();
	    try (Stream<String> stream = Files.lines(this.ARCHIVO.toPath(), StandardCharsets.UTF_8)) {

		stream.forEach(s -> sb.append(s).append("\n"));
	    } catch (final IOException e) {
		System.out.println("Error al importar escenario: " + e.getMessage());
	    }
	    json = (JSONObject) new JSONParser().parse(this.ENCRIPTADOR_STRING.desencriptar(sb.toString()));
//	    json = (JSONObject) new JSONParser().parse(new FileReader(this.ARCHIVO));
	} catch (final ParseException e) {
	    e.printStackTrace();
	}
	return json;
    }

    @SuppressWarnings("unchecked")
    /**
     * Actualiza el archivo temporal metiendo/remplazando el valor solicitado para
     * la clave mencionada
     * 
     * @param key   La clave para el Json
     * @param value El valor a guardar en el json
     */
    public void actualizarJson(final String key, final String value) {

	try {
	    final JSONObject json = this.getJsonTemp();
	    final JSONObject jsonValue = (JSONObject) new JSONParser().parse(value);
	    json.put(key, jsonValue);
	    this.eliminarTemp();
	    final PrintWriter pw = new PrintWriter(this.ARCHIVO);
	    pw.print(this.ENCRIPTADOR_STRING.encriptar(json.toJSONString()));
	    pw.flush();
	    pw.close();
	} catch (final IOException e) {
	    e.printStackTrace();
	} catch (final ParseException e) {
	    e.printStackTrace();
	}
    }

    @SuppressWarnings("unchecked")
    /**
     * Actualiza el archivo temporal metiendo/remplazando el valor solicitado para
     * la clave key mencionada que pertenece al objeto de la clave keyPadre
     * 
     * @param key      La clave para el Json
     * @param value    El valor a guardar en el json
     * @param keyPadre La clave padre que contiene la clave mencionada en el valor
     *                 key
     */
    public void actualizarJson(final String key, final String value, final String keyPadre) {
	try {
	    final JSONObject json = this.getJsonTemp();
	    final JSONObject jsonValue = (JSONObject) new JSONParser().parse(value);
	    if (json.containsKey(keyPadre)) {
		final JSONObject jsonPadre = ((JSONObject) json.get(keyPadre));
		if (jsonPadre.containsValue(key)) {
		    jsonPadre.remove(key);
		}
		jsonPadre.put(key, jsonValue);
	    } else {
		final JSONObject jsonPadre = new JSONObject();
		jsonPadre.put(key, jsonValue);
		json.put(keyPadre, jsonPadre);
	    }

	    this.eliminarTemp();
	    final PrintWriter pw = new PrintWriter(this.ARCHIVO);
	    pw.print(this.ENCRIPTADOR_STRING.encriptar(json.toJSONString()));
	    pw.flush();
	    pw.close();
	} catch (final IOException e) {
	    e.printStackTrace();
	} catch (final ParseException e) {
	    e.printStackTrace();
	}
    }

    /**
     * Elimina una clave del Json almacenado en el archivo temporal.
     * 
     * @param key La clave a eliminar del Json.
     */
    public void eliminarClaveDeJson(final String key) {
	try {
	    final JSONObject json = this.getJsonTemp();
	    json.remove(key);
	    this.eliminarTemp();
	    final PrintWriter pw = new PrintWriter(this.ARCHIVO);
	    pw.print(this.ENCRIPTADOR_STRING.encriptar(json.toJSONString()));
	    pw.flush();
	    pw.close();
	} catch (final IOException e) {
	}
    }

}
