package principal.mapa.escenario.tps;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.jugador.Jugador;
import principal.mapa.Mundo;
import principal.mapa.mapas.Mapa;
import principal.mapa.mapas.MapaManager;
import principal.maquinaestado.estados.GestorPartida;
import principal.utilidades.Globales;

public class PuertaMundo extends PuertaTP {

	private final String nombreMundoDestino;
	private final String nombreSpawnDestino;

	public PuertaMundo(final String nombreMundoDestino, final String nombreSpawnDestino) {
		this.nombreMundoDestino = (nombreMundoDestino != null) ? nombreMundoDestino.trim() : "exterior";
		this.nombreSpawnDestino = (nombreSpawnDestino != null) ? nombreSpawnDestino.trim()
				: Mundo.CLAVE_PUNTO_SPAWN_COMIENZO;
	}

	@Override
	public void teletransportar(final Criatura c) {
		if (!(c instanceof Jugador)) {
			return;
		}

		final Mundo mundoOrigen = c.getMundo();
		if (mundoOrigen == null) {
			System.err.println("[PuertaMundo] Error: El jugador no tiene un mundo asignado.");
			return;
		}

		// 1. Obtener el mapa con fallback seguro para evitar salidas silenciosas
		Mapa mapaActual = mundoOrigen.getMapa();
		if (mapaActual == null) {
			final GestorPartida gp = MapaManager.getGestorPartida();
			if ((gp != null) && (gp.getGestorJuego() != null)) {
				mapaActual = gp.getGestorJuego().getMapa();
			}
		}

		if (mapaActual == null) {
			System.err.println("[PuertaMundo] Error crítico: No se encontró la instancia activa de Mapa.");
			return;
		}

		final double liderOrigenX = c.getCentroX();
		final double liderOrigenY = c.getCentroY();

		// 2. Transición del jugador al nuevo submundo
		mapaActual.cambiarMundoInterno(this.nombreMundoDestino, this.nombreSpawnDestino);

		// 3. Migración atómica de los seguidores de la escolta
		final Mundo mundoDestino = mapaActual.getMundo(this.nombreMundoDestino);
		if ((mundoDestino != null) && (Globales.GESTOR_GRUPO != null)) {
			Globales.GESTOR_GRUPO.migrarEscoltaSubmundo(mundoOrigen, mundoDestino, c, liderOrigenX, liderOrigenY, 64.0);
		}
	}

	public String getNombreMundoDestino() {
		return this.nombreMundoDestino;
	}

	public String getNombreSpawnDestino() {
		return this.nombreSpawnDestino;
	}
}