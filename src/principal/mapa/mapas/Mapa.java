package principal.mapa.mapas;

import java.awt.Graphics2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.JOptionPane;

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
import principal.mapa.escenario.EscenarioLoader;
import principal.maquinaestado.estados.GestorPartida;
import principal.maquinaestado.estados.pantallaCarga.GestorCarga;
import principal.utilidades.Globales;

/**
 * Clase encargada de controlar los mundos que contiene dicho mapa
 */

public abstract class Mapa {
	protected final GestorPartida GP;
	final HashMap<String, Mundo> MUNDOS = new HashMap<String, Mundo>();
	Mundo mundoActual;

	public Mapa(final GestorCarga gc, final int porcentajeCarga, final GestorPartida gp) {
		this.GP = gp;
		gc.setPorcentajeCarga(0);
		gc.setCompleto(false);
		this.establecerMundos(gc, porcentajeCarga);
		this.establecerMundoActual();
		this.cargarFuncionalidadesPropias();
		gc.setDetalleCarga("Carga de recursos completa!");
	}

	public Mapa(final GestorCarga gc, final int porcentajeCarga, final GestorPartida gp, final JSONObject jsonMapa) {
		this.GP = gp;
		gc.setPorcentajeCarga(0);
		gc.setCompleto(false);
		String nombreMundo = "";
		final JSONArray listaMundoJson = ((JSONArray) jsonMapa.get("nombreMundos"));
		final int porcentajeXMundo = porcentajeCarga / listaMundoJson.size();
		for (final Object obj : listaMundoJson) {
			nombreMundo = (String) obj;
			gc.setDetalleCarga("Generando mundo: " + nombreMundo);
			this.MUNDOS.put(nombreMundo,
					this.generarMundo((JSONObject) jsonMapa.get(nombreMundo), gc, porcentajeCarga));
			gc.setPorcentajeCarga(gc.getPorcentaje() + porcentajeXMundo);
		}
		this.mundoActual = this.MUNDOS.get(jsonMapa.get("mundoActual").toString());
		this.cargarFuncionalidadesPropias();
	}

	public void actualizar() {
		this.mundoActual.actualizar();
	}

	public void pintar(final Graphics2D g) {
		this.mundoActual.pintar(g);
	}

	public Mundo getMundoActual() {
		return this.mundoActual;
	}

	public Mundo getMundo(final String nombreMundo) {
		return this.MUNDOS.get(nombreMundo);
	}

	public Collection<Mundo> getMundos() {
		return this.MUNDOS.values();
	}

	public abstract String getNombre();

	protected abstract void establecerMundos(final GestorCarga gc, final int porcentajeCarga);

	protected abstract void establecerMundoActual();

	protected abstract void cargarFuncionalidadesPropias();

	protected Escenario cargarEscenario(final GestorCarga gc, final int porcentajeCarga, final File ruta) {

		final Escenario esc = EscenarioLoader.importarEscenario(ruta, gc, porcentajeCarga);
		if (esc == null) {
			System.err.println("No se ha podido cargar el escenario: " + ruta.getAbsolutePath());
			JOptionPane.showConfirmDialog(null, "Error al cargar el mundo " + ruta.getPath(), "Error",
					JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
//		Constantes.LADO_TILE = esc.getTerreno().ladoTile();
		return esc;
	}

	private Mundo generarMundo(final JSONObject jsonMundo, final GestorCarga gc, final int porcentajeCarga) {
		final ArrayList<Spawn> listaSpawn = new ArrayList<Spawn>();
		Spawn comienzo = null;
		JSONObject jsonSpawn = null;
		for (final Object obj : (JSONArray) jsonMundo
				.get(Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Spawn.class))) {
			jsonSpawn = (JSONObject) obj;
			listaSpawn.add(new Spawn(Integer.parseInt(jsonSpawn.get("x").toString()),
					Integer.parseInt(jsonSpawn.get("y").toString()), jsonSpawn.get("nombre").toString()));
			if (jsonSpawn.get("nombre").toString().equals(Mundo.CLAVE_PUNTO_SPAWN_COMIENZO)) {
				comienzo = new Spawn(Integer.parseInt(jsonSpawn.get("x").toString()),
						Integer.parseInt(jsonSpawn.get("y").toString()), jsonSpawn.get("nombre").toString());
			}
		}
		final Mundo m = new Mundo(new Escenario(
				new Terreno((JSONObject) jsonMundo.get(Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Tile.class))),
				((JSONArray) jsonMundo.get(Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Criatura.class)))
						.toString(),
				((JSONArray) jsonMundo.get(Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Item.class))).toString(),
				((JSONArray) jsonMundo.get(Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Complemento.class)))
						.toString(),
				((JSONArray) jsonMundo.get(Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Objeto.class))).toString()),
				comienzo.getPoint());
		m.llenarSpawn(listaSpawn);
		return m;
	}

	public abstract String[] getNombreMundos();

}
