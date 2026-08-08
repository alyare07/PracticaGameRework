package principal.mapa.mapas;

import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import principal.maquinaestado.estados.GestorPartida;
import principal.maquinaestado.estados.pantallaCarga.GestorCarga;
import principal.utilidades.Constantes;

public abstract class MapaManager{
    public final static String MAPA_1 = Mapa1.NOMBRE_MAPA;
    public final static String MAPA_0 = MapaPlano.NOMBRE_MAPA;
    protected static GestorPartida gestorPartida;
    private static HashMap<String, String> MAPAS_EN_TEMP = new HashMap<String, String>();
    private static final String CLAVE_JSON_TEMP = "MAPAS";
    private static final String PREFIJO_JSON_MAPA_TEMP = "MAPA_";

    private MapaManager() {
    }

    public static Mapa cargarMapa1(final GestorCarga gc) {
	return new Mapa1(gc, 100, gestorPartida);
    }

    public static Mapa cargarMapa(final String nombre, final GestorCarga gc) {
	if (MAPAS_EN_TEMP.containsKey(nombre)) {
	    return cargarMapaDeTemp(nombre, gc);
	}
	switch (nombre) {
	case MAPA_1:
	    return cargarMapa1(gc);
	case MAPA_0:
	    return new MapaPlano(gc, 100, gestorPartida);
	default:
	    System.err.println("Error al solicitar carga mapa " + nombre);
	    return null;
	}
    }

    public static void setGestorPartida(final GestorPartida gp) {
	gestorPartida = gp;
    }

    @SuppressWarnings("unchecked")
    public static void guardarMapaEnTemp(final Mapa mapa) {
	final JSONObject mundos = new JSONObject();
	mundos.put("nombreMapa", mapa.getNombre());
	final JSONArray nombreMundos = new JSONArray();
	for (final String nombre : mapa.getNombreMundos()) {
	    nombreMundos.add(nombre);
	    mundos.put(nombre, mapa.getMundo(nombre).getMundoEnJson());
	    if (mapa.getMundo(nombre) == mapa.getMundoActual()) {
		mundos.put("mundoActual", nombre);
	    }
	}
	mundos.put("nombreMundos", nombreMundos);
	Constantes.FUNCIONES.TEMP_MANAGER.actualizarJson(PREFIJO_JSON_MAPA_TEMP + mapa.getNombre(), mundos.toJSONString(), CLAVE_JSON_TEMP);
	MAPAS_EN_TEMP.put(mapa.getNombre(), mapa.getNombre());
    }

    private static Mapa cargarMapaDeTemp(final String nombreMapa, final GestorCarga gc) {
	final JSONObject jsonGeneral = ((JSONObject) Constantes.FUNCIONES.TEMP_MANAGER.getJsonTemp().get(CLAVE_JSON_TEMP));
	final JSONObject jsonMapa = (JSONObject) jsonGeneral.get(PREFIJO_JSON_MAPA_TEMP + nombreMapa);

	if (nombreMapa.equals(Mapa1.NOMBRE_MAPA)) {
	    return new Mapa1(gc, 100, gestorPartida, jsonMapa);
	} else if (nombreMapa.equals(MapaPlano.NOMBRE_MAPA)) {
	    return new MapaPlano(gc, 100, gestorPartida, jsonMapa);
	}
	System.out.println(nombreMapa);
	new Throwable();
	return null;
    }

    public static void vaciarTemp() {
	MAPAS_EN_TEMP.clear();
	Constantes.FUNCIONES.TEMP_MANAGER.eliminarClaveDeJson(CLAVE_JSON_TEMP);
    }

}
