package principal.mapa.mapas;

import java.io.File;

import principal.maquinaestado.estados.GestorPartida;
import principal.maquinaestado.estados.pantallaCarga.GestorCarga;
import principal.utilidades.Globales;

/**
 * Fachada agnóstica para la instanciación de mapas. Erradica los switchs
 * hardcodeados y carga proyectos directamente desde el disco.
 */
public abstract class MapaManager {

	// Claves canónicas estándar para proyectos por defecto
	public static final String MAPA_1 = "mapa1";
	public static final String MAPA_0 = "mapaplano";

	protected static GestorPartida gestorPartida;

	private MapaManager() {
	}

	public static Mapa cargarMapa(final String nombreMapa, final GestorCarga gc) {
		if ((nombreMapa == null) || nombreMapa.trim().isEmpty()) {
			System.err.println("[MapaManager] Error: Nombre de mapa inválido.");
			return null;
		}

		// Resolución de carpeta de proyecto: 1. mapas/ 2. mundos/
		File directorio = new File("mapas", nombreMapa);
		if (!directorio.exists() || !directorio.isDirectory()) {
			directorio = new File("mundos", nombreMapa);
		}

		if (!directorio.exists() || !directorio.isDirectory()) {
			System.err.println(
					"[MapaManager] Error: No se encontró la carpeta del proyecto en: " + directorio.getAbsolutePath());
			return null;
		}

		final Mapa mapa = new Mapa(directorio, gc, 100, gestorPartida);

		// Sincronizar estado inicial con deltas
		if (mapa.getMundoActual() != null) {
			Globales.GESTOR_DELTAS.aplicarDelta(mapa.getMundoActual());
		}

		return mapa;
	}

	public static void setGestorPartida(final GestorPartida gp) {
		gestorPartida = gp;
	}

	public static GestorPartida getGestorPartida() {
		return gestorPartida;
	}

	public static void guardarMapaEnTemp(final Mapa mapa) {
		if ((mapa == null) || (mapa.getMundoActual() == null)) {
			return;
		}
		Globales.GESTOR_DELTAS.capturarDelta(mapa.getMundoActual(), 0);
	}

	public static void vaciarTemp() {
		Globales.GESTOR_DELTAS.limpiarTodosLosDeltas();
	}
}