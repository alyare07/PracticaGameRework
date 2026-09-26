package principal.mapa.escenario.tps;

import java.io.File;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.jugador.Jugador;
import principal.mapa.Mundo;
import principal.mapa.mapas.Mapa;
import principal.mapa.mapas.MapaManager;
import principal.maquinaestado.estados.GestorPartida;
import principal.utilidades.Globales;

/**
 * Mecanismo de teletransporte inter-mapa seguro y desacoplado. Incluye
 * Fast-Bypass en O(1): si el mapa destino es el proyecto actualmente en
 * memoria, delega a una transición interna en 0 ms sin pantalla de carga.
 */
public class PuertaMapa extends PuertaTP {

	private final File ARCHIVO_MAPA;
	private final String NOMBRE_MUNDO;
	private final String NOMBRE_SPAWN;
	private final GestorPartida GP;

	public PuertaMapa(final String rutaMapa, final String nombreMundo, final String nombreSpawn, final boolean temp,
			final GestorPartida gp) {
		this.ARCHIVO_MAPA = new File(rutaMapa);
		this.NOMBRE_MUNDO = (nombreMundo != null) ? nombreMundo.trim() : "exterior";
		this.NOMBRE_SPAWN = (nombreSpawn != null) ? nombreSpawn.trim() : Mundo.CLAVE_PUNTO_SPAWN_COMIENZO;
		this.GP = gp;
	}

	@Override
	public void teletransportar(final Criatura c) {
		if (!(c instanceof Jugador)) {
			return;
		}

		final Mundo mundoOrigen = c.getMundo();

		GestorPartida gestorPartida = this.GP;
		if ((gestorPartida == null) && Globales.isEstadoJuego()) {
			gestorPartida = MapaManager.getGestorPartida();
		}

		if ((gestorPartida == null) || (gestorPartida.getGestorJuego() == null)) {
			return;
		}

		final Mapa mapaActual = gestorPartida.getGestorJuego().getMapa();
		final String idMapaDestino = this.extraerIdMapa(this.ARCHIVO_MAPA.getPath());

		// =====================================================================
		// 1. FAST-BYPASS (0 ms): Si el mapa destino ya es el que está en RAM
		// =====================================================================
		if ((mapaActual != null) && (mapaActual.getManifiesto() != null)
				&& mapaActual.getManifiesto().getIdMapa().equalsIgnoreCase(idMapaDestino)) {

			final double liderOrigenX = c.getCentroX();
			final double liderOrigenY = c.getCentroY();

			// Salto interno instantáneo sin recargar el proyecto ni mostrar pantalla negra
			mapaActual.cambiarMundoInterno(this.NOMBRE_MUNDO, this.NOMBRE_SPAWN);

			// Migración atómica de escoltas / mascotas
			final Mundo mundoDestino = mapaActual.getMundo(this.NOMBRE_MUNDO);
			if ((mundoDestino != null) && (Globales.GESTOR_GRUPO != null) && (mundoOrigen != null)) {
				Globales.GESTOR_GRUPO.migrarEscoltaSubmundo(mundoOrigen, mundoDestino, c, liderOrigenX, liderOrigenY,
						64.0);
			}
			return;
		}

		// =====================================================================
		// 2. INTER-MAPA REAL: Cargar nuevo proyecto desde disco con pantalla de carga
		// =====================================================================
		if (mapaActual != null) {
			MapaManager.guardarMapaEnTemp(mapaActual);
		}
		gestorPartida.cambiarMundo(idMapaDestino, this.NOMBRE_MUNDO, this.NOMBRE_SPAWN);
	}

	private String extraerIdMapa(final String ruta) {
		if (ruta == null) {
			return "mapa1";
		}
		String limpia = ruta.replace('\\', '/').trim();
		if (limpia.endsWith("/mapa.mp") || limpia.endsWith("/mapa.json")) {
			limpia = limpia.substring(0, limpia.lastIndexOf('/'));
		}
		if (limpia.contains("/")) {
			limpia = limpia.substring(limpia.lastIndexOf('/') + 1);
		}
		return limpia.replaceAll("[^a-zA-Z0-9_]", "").toLowerCase();
	}

	public String getNombreMundoDestino() {
		return this.NOMBRE_MUNDO;
	}

	public String getRutaMapaDestino() {
		return this.ARCHIVO_MAPA.getPath();
	}

	public String getNombreSpawnDelMundoDestino() {
		return this.NOMBRE_SPAWN;
	}
}