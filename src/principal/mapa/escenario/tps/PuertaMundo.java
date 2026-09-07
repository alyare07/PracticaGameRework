package principal.mapa.escenario.tps;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Jugador;
import principal.mapa.Mundo;
import principal.mapa.mapas.Mapa;

public class PuertaMundo extends PuertaTP {

	private final String nombreMundoDestino;
	private final String nombreSpawnDestino;

	public PuertaMundo(final String nombreMundoDestino, final String nombreSpawnDestino) {
		this.nombreMundoDestino = (nombreMundoDestino != null) ? nombreMundoDestino : "exterior";
		this.nombreSpawnDestino = (nombreSpawnDestino != null) ? nombreSpawnDestino : Mundo.CLAVE_PUNTO_SPAWN_COMIENZO;
	}

	@Override
	public void teletransportar(final Criatura c) {
		if (!(c instanceof Jugador)) {
			return;
		}

		final Mundo mundoOrigen = c.getMundo();
		if (mundoOrigen == null) {
			return;
		}

		final Mapa mapaActual = mundoOrigen.getMapa();
		if (mapaActual != null) {
			mapaActual.cambiarMundoInterno(this.nombreMundoDestino, this.nombreSpawnDestino);
		}
	}

	public String getNombreMundoDestino() {
		return this.nombreMundoDestino;
	}

	public String getNombreSpawnDestino() {
		return this.nombreSpawnDestino;
	}
}