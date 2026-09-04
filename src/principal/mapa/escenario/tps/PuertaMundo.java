package principal.mapa.escenario.tps;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Jugador;
import principal.mapa.Mundo;
import principal.mapa.mapas.Spawn;
import principal.utilidades.Globales;

/**
 * Puerta de teletransporte que transfiere a la criatura a otro Mundo
 * perteneciente al MISMO Mapa (ejemplo: del Exterior a una Cueva o Mazmorra
 * interior).
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class PuertaMundo extends PuertaTP {

	private final String nombreMundoDestino;
	private final String nombreSpawnDestino;

	public PuertaMundo(final String nombreMundoDestino, final String nombreSpawnDestino) {
		this.nombreMundoDestino = (nombreMundoDestino != null) ? nombreMundoDestino : "Exterior";
		this.nombreSpawnDestino = (nombreSpawnDestino != null) ? nombreSpawnDestino : Mundo.CLAVE_PUNTO_SPAWN_COMIENZO;
	}

	@Override
	public void teletransportar(final Criatura c) {
		if (c == null) {
			return;
		}

		if ((c instanceof Jugador) && Globales.isEstadoJuego()) {
			final Mundo mundoOrigen = c.getMundo();
			if (mundoOrigen != null) {
				Globales.GESTOR_DELTAS.capturarDelta(mundoOrigen, 0);
			}
		}

		if (c.getMundo() != null) {
			final Spawn spawn = c.getMundo().getSpawn(this.nombreSpawnDestino);
			if (spawn != null) {
				spawn.moverJugadorCentrado();
			}
		}
	}

	public String getNombreMundoDestino() {
		return this.nombreMundoDestino;
	}

	public String getNombreSpawnDestino() {
		return this.nombreSpawnDestino;
	}
}