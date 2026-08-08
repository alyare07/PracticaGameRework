package principal.mapa.mapas;

import java.awt.Point;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import principal.entes.criaturas.Criatura;
import principal.entes.objetos.Complemento;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.Item;
import principal.mapa.Mundo;
import principal.mapa.Terreno;
import principal.mapa.Tile;
import principal.mapa.escenario.Escenario;
import principal.maquinaestado.estados.GestorPartida;
import principal.maquinaestado.estados.pantallaCarga.GestorCarga;
import principal.utilidades.Constantes;

public class MapaCargadoDeTemp extends Mapa{
    private final String nombre;
    private final ArrayList<String> nombreMundos = new ArrayList<String>();

    public MapaCargadoDeTemp(final GestorCarga gc, final int porcentajeCarga, final GestorPartida gp, final JSONObject jsonMapa) {
	super(gc, porcentajeCarga, gp);
	this.nombre = jsonMapa.get("nombreMapa").toString(); // VER EL NULL ACA AL CARGAR

	String nombreMundo = "";
	for (final Object obj : ((JSONArray) jsonMapa.get("nombreMundos"))) {
	    nombreMundo = (String) obj;
	    this.nombreMundos.add(nombreMundo);
	    this.MUNDOS.put(nombreMundo, this.generarMundo((JSONObject) jsonMapa.get(nombreMundo)));
	}
	this.mundoActual = this.MUNDOS.get(jsonMapa.get("mundoActual").toString());
    }

    private Mundo generarMundo(final JSONObject jsonMundo) {
	return new Mundo(
		new Escenario(new Terreno((JSONObject) jsonMundo.get(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Tile.class))),
			((JSONArray) jsonMundo.get(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Criatura.class))).toString(),
			((JSONArray) jsonMundo.get(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Item.class))).toString(),
			((JSONArray) jsonMundo.get(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Complemento.class))).toString(),
			((JSONArray) jsonMundo.get(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Objeto.class))).toString()),
		new Point(Integer.parseInt(jsonMundo.get("Punto Comienzo X").toString()), Integer.parseInt(jsonMundo.get("Punto Comienzo Y").toString())));
    }

    @Override
    public String getNombre() {
	return this.nombre;
    }

    @Override
    protected void establecerMundos(final GestorCarga gc, final int porcentajeCarga) {

    }

    @Override
    protected void establecerMundoActual() {

    }

    @Override
    protected void cargarFuncionalidadesPropias() {
	// TODO Auto-generated method stub

    }

    @Override
    public String[] getNombreMundos() {
	return (String[]) this.nombreMundos.toArray();
    }

}
