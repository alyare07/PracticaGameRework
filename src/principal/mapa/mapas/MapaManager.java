package principal.mapa.mapas;

import principal.maquinaestado.estados.GestorPartida;
import principal.maquinaestado.estados.pantallaCarga.GestorCarga;
import principal.utilidades.Globales;

public abstract class MapaManager {

	public static final String MAPA_1 = Mapa1.NOMBRE_MAPA;
	public static final String MAPA_0 = MapaPlano.NOMBRE_MAPA;

	protected static GestorPartida gestorPartida;

	private MapaManager() {
	}

	public static Mapa cargarMapa1(final GestorCarga gc) {
		final Mapa mapa = new Mapa1(gc, 100, gestorPartida);
		if (mapa.getMundoActual() != null) {
			mapa.getMundoActual().setNombreMundo(MAPA_1 + "_Exterior");
			Globales.GESTOR_DELTAS.aplicarDelta(mapa.getMundoActual());
		}
		return mapa;
	}

	public static Mapa cargarMapa(final String nombreMapa, final GestorCarga gc) {
		Mapa mapa = null;
		switch (nombreMapa) {
		case MAPA_1:
			mapa = cargarMapa1(gc);
			break;
		case MAPA_0:
			mapa = new MapaPlano(gc, 100, gestorPartida);
			if (mapa.getMundoActual() != null) {
				mapa.getMundoActual().setNombreMundo(MAPA_0 + "_Exterior");
				Globales.GESTOR_DELTAS.aplicarDelta(mapa.getMundoActual());
			}
			break;
		default:
			System.err.println("Error: No se encontró la definición para cargar el mapa: " + nombreMapa);
			return null;
		}
		return mapa;
	}

	public static void setGestorPartida(final GestorPartida gp) {
		gestorPartida = gp;
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