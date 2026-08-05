package principal.mapa.escenario;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.stream.Stream;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import principal.entes.criaturas.Criatura;
import principal.entes.objetos.Complemento;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.Item;
import principal.mapa.Mapa;
import principal.mapa.Tile;
import principal.maquinaestado.estados.pantallaCarga.GestorCarga;
import principal.utilidades.Constantes;
public abstract class EscenarioLoader {
	private EscenarioLoader() {
	}
	
	
	@SuppressWarnings("unchecked")
	public static void exportarEscenario(final Escenario esc, final File ruta) {
		String criaturas = esc.LISTA_CREACION_CRIATURAS_JSON;
		String items = esc.LISTA_CREACION_ITEMS_JSON;
		String objetos = esc.LISTA_CREACION_OBJETOS_JSON;
		String complementos = esc.LISTA_CREACION_COMPLEMENTOS_JSON;
		String mapa = null;
		JSONObject jsonExp = new JSONObject();
		JSONArray jsonCriatura = null;
		JSONArray jsonItems = null;
		JSONArray jsonObjetos = null;
		JSONArray jsonComplementos = null;
		try {
			jsonCriatura = (JSONArray) new JSONParser().parse(criaturas);
			jsonItems = (JSONArray) new JSONParser().parse(items);
			jsonObjetos = (JSONArray) new JSONParser().parse(objetos);
			jsonComplementos = (JSONArray) new JSONParser().parse(complementos);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		jsonExp.put(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Criatura.class), jsonCriatura);
		jsonExp.put(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Item.class), jsonItems);
		jsonExp.put(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Objeto.class), jsonObjetos);
		jsonExp.put(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Complemento.class), jsonComplementos);
		jsonExp.put(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Tile.class) ,esc.getMapa().getTilesJson());
		
		String jsonExpEncriptado = Constantes.FUNCIONES.ENCRIPTADOR_STRING.encriptar(jsonExp.toJSONString());
		
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(ruta);
			pw.print(jsonExpEncriptado);
			pw.flush();
			System.out.println("Mundo exportado en:  "+ ruta);
		} catch (IOException e) {
			System.out.println("Error al exportar escenario: "+ e.getMessage());
		}finally {
			pw.close();
		}
		
	}
	
	public static Escenario importarEscenario(final File ruta) {
		Escenario esc = null;
		try {
			StringBuilder contentBuilder = new StringBuilder();

			try (Stream<String> stream = Files.lines(ruta.toPath(), StandardCharsets.UTF_8)) {

			  stream.forEach(s -> contentBuilder.append(s).append("\n"));
			} catch (IOException e) {
				System.out.println("Error al importar escenario: "+ e.getMessage());
			}
			String jsonImpEncriptado = contentBuilder.toString();
			JSONObject jsonImp = (JSONObject) new JSONParser().parse(Constantes.FUNCIONES.ENCRIPTADOR_STRING.desencriptar(jsonImpEncriptado));
			String jsonCriaturas = jsonImp.get(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Criatura.class)).toString();
			String jsonItems = jsonImp.get(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Item.class)).toString();
			String jsonObjetos = jsonImp.get(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Objeto.class)).toString();
			String jsonComplementos = jsonImp.get(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Complemento.class)).toString();
			Mapa mapa = null;
			JSONObject terreno = (JSONObject)(new JSONParser()).parse(jsonImp.get(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Tile.class)).toString());
			
			mapa = new Mapa(terreno);
			esc = new Escenario(mapa, jsonCriaturas, jsonItems,jsonComplementos,jsonObjetos);
			
		} catch (Exception e) {
			System.out.println("Error al importar escenariox: "+ e.getMessage());
			e.printStackTrace();
		}finally {
			try {
			} catch (Exception e2) {
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
			StringBuilder contentBuilder = new StringBuilder();
			try (Stream<String> stream = Files.lines(ruta.toPath(), StandardCharsets.UTF_8)) {

			  stream.forEach(s -> contentBuilder.append(s).append("\n"));
			} catch (IOException e) {
				System.out.println("Error al importar escenario: "+ e.getMessage());
			}
			gc.setPorcentajeCarga(gc.getPorcentaje() + (pesoCarga*porcentajeCarga/100));
			////////////////////
			pesoCarga = 20;
			gc.setDetalleCarga("Leyendo datos");
			String jsonImpEncriptado = contentBuilder.toString();
			JSONObject jsonImp = (JSONObject) new JSONParser().parse(Constantes.FUNCIONES.ENCRIPTADOR_STRING.desencriptar(jsonImpEncriptado));
			String jsonCriaturas = jsonImp.get(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Criatura.class)).toString();
			String jsonItems = jsonImp.get(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Item.class)).toString();
			String jsonObjetos = jsonImp.get(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Objeto.class)).toString();
			String jsonComplementos = jsonImp.get(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Complemento.class)).toString();
			Mapa mapa = null;
			JSONObject terreno = (JSONObject)(new JSONParser()).parse(jsonImp.get(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Tile.class)).toString());
			gc.setPorcentajeCarga(gc.getPorcentaje() + (pesoCarga*porcentajeCarga/100));
			///////////////
			pesoCarga = 60;
			gc.setDetalleCarga("Generando terreno");
			mapa = new Mapa(terreno);
			esc = new Escenario(mapa, jsonCriaturas, jsonItems,jsonComplementos,jsonObjetos);
			gc.setPorcentajeCarga(gc.getPorcentaje() + (pesoCarga*porcentajeCarga/100));
			
		} catch (Exception e) {
			System.out.println("Error al importar escenariox: "+ e.getMessage());
			e.printStackTrace();
		}finally {
			try {
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return esc;
	}
	
	
	private static String serializableAString(Serializable o ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( o );
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray()); 
    }
	
	private static Object stringASerializable( String s ) throws IOException ,ClassNotFoundException {
		byte [] data = Base64.getDecoder().decode( s );
		ObjectInputStream ois = new ObjectInputStream( 
		new ByteArrayInputStream(  data ) );
		Object o  = ois.readObject();
		ois.close();
		return o;
	}
	
	
	

}
