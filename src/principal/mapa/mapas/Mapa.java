package principal.mapa.mapas;

import java.awt.Graphics2D;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.JOptionPane;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import principal.mapa.Mundo;
import principal.mapa.Terreno;
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
		this.establecerMundoComienzo();
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

	/**
	 * Transfiere de forma atómica al jugador entre mundos pertenecientes a este
	 * mapa.
	 */
	public Mundo getMundo(final String nombreMundo) {
		if (nombreMundo == null) {
			return null;
		}
		final Mundo m = this.MUNDOS.get(nombreMundo);
		if (m != null) {
			return m;
		}
		// Búsqueda tolerante a mayúsculas/minúsculas
		for (final java.util.Map.Entry<String, Mundo> entry : this.MUNDOS.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(nombreMundo)) {
				return entry.getValue();
			}
		}
		return null;
	}

	public void cambiarMundoInterno(final String nombreMundoDestino, final String nombreSpawnDestino) {
		final Mundo nuevoMundo = this.getMundo(nombreMundoDestino);

		if (nuevoMundo == null) {
			System.err.println("[Mapa] El mundo destino '" + nombreMundoDestino + "' no existe en este mapa.");
			return;
		}

		final Mundo mundoViejo = this.mundoActual;

		// 1. Guardar cambios del mundo que se abandona en su Delta
		if (mundoViejo != null) {
			Globales.GESTOR_DELTAS.capturarDelta(mundoViejo, 0);
		}

		// 2. Establecer el nuevo mundo activo
		this.mundoActual = nuevoMundo;
		nuevoMundo.setNombreMundo(nombreMundoDestino);

		// 3. Vincular al jugador con el nuevo mundo y ubicarlo en el Spawn
		Globales.JUGADOR.setMundo(nuevoMundo);
		final Spawn spawnDest = nuevoMundo.getSpawn(nombreSpawnDestino);
		if (spawnDest != null) {
			spawnDest.moverJugadorCentrado();
		} else {
			nuevoMundo.moverJugadorPuntoComienzo();
		}

		// 4. Aplicar cambios persistentes del nuevo mundo
		Globales.GESTOR_DELTAS.aplicarDelta(nuevoMundo);

		// 5. Configurar atmósfera
		nuevoMundo.aplicarMetadatosAtmosfericos();

		// 6. Recalcular límites de cámara y actualizar inventario
		Globales.CAMARA.habilitarGestorLimite();
		Globales.GESTOR_INVENTARIO.getInventarioJugador().establecerMundo(nuevoMundo);
		Globales.RATON.soltar();
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

	public Collection<Mundo> getMundos() {
		return this.MUNDOS.values();
	}

	public abstract String getNombre();

	protected abstract void establecerMundos(final GestorCarga gc, final int porcentajeCarga);

	public abstract void establecerMundoActual(final String nombreMundo);

	protected abstract void establecerMundoComienzo();

	protected abstract void cargarFuncionalidadesPropias();

	protected Escenario cargarEscenario(final GestorCarga gc, final int porcentajeCarga, final File ruta) {
		final Escenario esc = EscenarioLoader.importarEscenario(ruta, gc, porcentajeCarga);
		if (esc == null) {
			System.err.println("No se ha podido cargar el escenario: " + ruta.getAbsolutePath());
			JOptionPane.showConfirmDialog(null, "Error al cargar el mundo " + ruta.getPath(), "Error",
					JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		return esc;
	}

	private Mundo generarMundo(final JSONObject jsonMundo, final GestorCarga gc, final int porcentajeCarga) {
		final ArrayList<Spawn> listaSpawn = new ArrayList<Spawn>();
		Spawn comienzo = null;
		JSONObject jsonSpawn = null;

		final JSONArray listaSpawns = (JSONArray) jsonMundo.get("spawns");
		if (listaSpawns != null) {
			for (final Object obj : listaSpawns) {
				jsonSpawn = (JSONObject) obj;
				final int sx = ((Number) jsonSpawn.get("x")).intValue();
				final int sy = ((Number) jsonSpawn.get("y")).intValue();
				final String nombreSpawn = jsonSpawn.get("nombre").toString();

				listaSpawn.add(new Spawn(sx, sy, nombreSpawn));
				if (nombreSpawn.equals(Mundo.CLAVE_PUNTO_SPAWN_COMIENZO)) {
					comienzo = new Spawn(sx, sy, nombreSpawn);
				}
			}
		}

		final JSONObject jsonTerreno = (JSONObject) jsonMundo.get("terreno");
		final Terreno terreno = new Terreno(
				jsonTerreno != null ? jsonTerreno : (JSONObject) jsonMundo.get("principal.mapa.Tile"));

		final JSONArray arrCriaturas = (JSONArray) jsonMundo.get("criaturas");
		final JSONArray arrItems = (JSONArray) jsonMundo.get("items");
		final JSONArray arrComplementos = (JSONArray) jsonMundo.get("complementos");
		final JSONArray arrObjetos = (JSONArray) jsonMundo.get("objetos");

		final Escenario esc = new Escenario(terreno, arrCriaturas, arrItems, arrComplementos, arrObjetos, listaSpawns,
				null, null, null, new principal.maquinaestado.estados.editor.metadatos.MetadatosEscenario());

		final Point pComienzo = (comienzo != null) ? comienzo.getPoint() : new Point(0, 0);
		final Mundo m = new Mundo(esc, pComienzo);
		m.setMapa(this);
		m.llenarSpawn(listaSpawn);
		return m;
	}

	public abstract String[] getNombreMundos();
}