package principal.mapa.mapas;

import java.awt.Graphics2D;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import principal.mapa.Mundo;
import principal.mapa.escenario.Escenario;
import principal.mapa.escenario.EscenarioLoader;
import principal.maquinaestado.estados.GestorPartida;
import principal.maquinaestado.estados.pantallaCarga.GestorCarga;
import principal.utilidades.Globales;

/**
 * Gestor dinámico de proyectos de mapa con soporte de Carga Híbrida (Zero-OOM).
 * Gobierna la memoria RAM manteniendo como máximo un submundo pesado
 * (exterior/mazmorra) y reteniendo en caché perezosa los interiores ligeros.
 */
public class Mapa {

	protected final GestorPartida GP;
	private final File directorioProyecto;
	private final ManifiestoMapa manifiesto;

	// Política de Memoria Híbrida
	private Mundo submundoPesadoActivo = null;
	private final Map<String, Mundo> cacheInteriores = new HashMap<String, Mundo>();
	private Mundo mundoActual = null;

	public Mapa(final File directorioProyecto, final GestorCarga gc, final int porcentajeCarga,
			final GestorPartida gp) {
		this.GP = gp;
		this.directorioProyecto = directorioProyecto;

		if (gc != null) {
			gc.setPorcentajeCarga(0);
			gc.setCompleto(false);
			gc.setDetalleCarga("Leyendo manifiesto del proyecto");
		}

		this.manifiesto = ManifiestoMapa.cargarDesdeDirectorio(directorioProyecto);
		if (this.manifiesto == null) {
			System.err.println("[Mapa] Error crítico: No se pudo cargar el manifiesto en: "
					+ directorioProyecto.getAbsolutePath());
			JOptionPane.showMessageDialog(null, "Error al cargar manifiesto de mapa: " + directorioProyecto.getName(),
					"Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}

		// Carga el submundo inicial definido por el manifiesto
		final String idInicio = this.manifiesto.getMundoComienzo();
		this.mundoActual = this.obtenerOCargarMundo(idInicio, gc);

		if (this.mundoActual == null) {
			System.err.println("[Mapa] Error fatal: El submundo inicial '" + idInicio + "' no se pudo instanciar.");
			System.exit(0);
		}

		if (gc != null) {
			gc.setDetalleCarga("Carga de mapa completa");
		}
	}

	/**
	 * Obtiene un submundo en O(1) si ya reside en RAM o lo carga desde disco
	 * aplicando la regla de exclusión mutua para submundos pesados.
	 */
	public Mundo obtenerOCargarMundo(final String idMundo, final GestorCarga gc) {
		if ((idMundo == null) || (this.manifiesto == null)) {
			return null;
		}

		final String idNorm = idMundo.toLowerCase();

		// 1. Comprobar si es el submundo pesado actualmente activo
		if ((this.submundoPesadoActivo != null)
				&& this.submundoPesadoActivo.getNombreMundo().equalsIgnoreCase(idNorm)) {
			return this.submundoPesadoActivo;
		}

		// 2. Comprobar si reside en la caché de interiores
		final Mundo interiorEnCache = this.cacheInteriores.get(idNorm);
		if (interiorEnCache != null) {
			return interiorEnCache;
		}

		// 3. Cargar desde disco según su política en el manifiesto
		final ManifiestoMapa.EntradaSubmundo entrada = this.manifiesto.getSubmundo(idNorm);
		if (entrada == null) {
			System.err.println("[Mapa] Submundo no registrado en manifiesto: " + idMundo);
			return null;
		}

		final File archivoSubmundo = this.manifiesto.resolverArchivoSubmundo(this.directorioProyecto, idNorm);
		if ((archivoSubmundo == null) || !archivoSubmundo.exists()) {
			System.err.println("[Mapa] Archivo físico de submundo no encontrado: " + idMundo);
			return null;
		}

		// Regla de Exclusión Mutua para mundos pesados (Exterior / Mazmorras masivas)
		if (entrada.esPesado()) {
			if (this.submundoPesadoActivo != null) {
				// Guardar estado persistente antes de purgar
				Globales.GESTOR_DELTAS.capturarDelta(this.submundoPesadoActivo, 0);
				this.submundoPesadoActivo.dispose();
				this.submundoPesadoActivo = null;
				System.gc(); // Sugerencia de recolección para liberar VRAM/Heap nativo
			}

			final Mundo nuevoPesado = this.cargarMundoDesdeDisco(archivoSubmundo, entrada.getId(), gc);
			this.submundoPesadoActivo = nuevoPesado;
			return nuevoPesado;
		}
		// Mundo Ligero: Carga y retención en caché
		final Mundo nuevoInterior = this.cargarMundoDesdeDisco(archivoSubmundo, entrada.getId(), gc);
		if (nuevoInterior != null) {
			this.cacheInteriores.put(idNorm, nuevoInterior);
		}
		return nuevoInterior;
	}

	private Mundo cargarMundoDesdeDisco(final File archivo, final String nombreMundo, final GestorCarga gc) {
		if (gc != null) {
			gc.setDetalleCarga("Cargando submundo: " + nombreMundo);
		}

		final Escenario esc = EscenarioLoader.importarEscenario(archivo, gc, 100);
		if (esc == null) {
			System.err.println("[Mapa] Error al parsear escenario desde: " + archivo.getAbsolutePath());
			return null;
		}

		final Mundo m = new Mundo(esc, new Point(0, 0), gc, 100);
		m.setNombreMundo(nombreMundo);
		m.setMapa(this);
		return m;
	}

	public Mundo getMundo(final String nombreMundo) {
		return this.obtenerOCargarMundo(nombreMundo, null);
	}

	public void cambiarMundoInterno(final String nombreMundoDestino, final String nombreSpawnDestino) {
		final Mundo mundoViejo = this.mundoActual;

		// 1. Guardar cambios del mundo ANTES de cargar o desechar nada
		if ((mundoViejo != null) && !mundoViejo.isDisposed()) {
			Globales.GESTOR_DELTAS.capturarDelta(mundoViejo, 0);
		}

		// 2. Ahora sí cargamos el nuevo mundo (y desecha el viejo de forma segura)
		final Mundo nuevoMundo = this.obtenerOCargarMundo(nombreMundoDestino, null);

		if (nuevoMundo == null) {
			System.err.println("[Mapa] El mundo destino '" + nombreMundoDestino + "' no existe o falló al cargar.");
			return;
		}

		// 3. Establecer nuevo mundo activo
		this.mundoActual = nuevoMundo;
		nuevoMundo.setNombreMundo(nombreMundoDestino);

		// 4. Vincular al jugador y posicionarlo
		Globales.JUGADOR.setMundo(nuevoMundo);
		final Spawn spawnDest = nuevoMundo.getSpawn(nombreSpawnDestino);
		if (spawnDest != null) {
			spawnDest.moverJugadorCentrado();
		} else {
			nuevoMundo.moverJugadorPuntoComienzo();
		}

		// 5. Aplicar cambios persistentes acumulados
		Globales.GESTOR_DELTAS.aplicarDelta(nuevoMundo);

		// 6. Configurar atmósfera, música y luz
		nuevoMundo.aplicarMetadatosAtmosfericos();

		// 7. Recalcular límites de cámara y actualizar inventario
		Globales.CAMARA.habilitarGestorLimite();
		Globales.GESTOR_INVENTARIO.getInventarioJugador().establecerMundo(nuevoMundo);
		Globales.RATON.soltar();
	}

	public void establecerMundoActual(final String nombreMundo) {
		if (nombreMundo == null) {
			return;
		}
		final Mundo m = this.obtenerOCargarMundo(nombreMundo, null);
		if (m != null) {
			this.mundoActual = m;
		}
	}

	public void actualizar() {
		if (this.mundoActual != null) {
			this.mundoActual.actualizar();
		}
	}

	public void pintar(final Graphics2D g) {
		if (this.mundoActual != null) {
			this.mundoActual.pintar(g);
		}
	}

	public Mundo getMundoActual() {
		return this.mundoActual;
	}

	public Collection<Mundo> getMundos() {
		final List<Mundo> activos = new ArrayList<Mundo>(this.cacheInteriores.values());
		if ((this.submundoPesadoActivo != null) && !activos.contains(this.submundoPesadoActivo)) {
			activos.add(this.submundoPesadoActivo);
		}
		return activos;
	}

	public String getNombre() {
		return (this.manifiesto != null) ? this.manifiesto.getNombreVisible() : "Mapa Sin Nombre";
	}

	public String[] getNombreMundos() {
		if (this.manifiesto == null) {
			return new String[0];
		}
		final List<String> ids = this.manifiesto.getListaIdsSubmundos();
		return ids.toArray(new String[ids.size()]);
	}

	public ManifiestoMapa getManifiesto() {
		return this.manifiesto;
	}

	public File getDirectorioProyecto() {
		return this.directorioProyecto;
	}

	public void dispose() {
		if (this.submundoPesadoActivo != null) {
			this.submundoPesadoActivo.dispose();
			this.submundoPesadoActivo = null;
		}
		for (final Mundo m : this.cacheInteriores.values()) {
			if (m != null) {
				m.dispose();
			}
		}
		this.cacheInteriores.clear();
		this.mundoActual = null;
	}
}