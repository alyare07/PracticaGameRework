package principal.mapa.escenario.tps;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Jugador;
import principal.mapa.Mundo;
import principal.mapa.mapas.Mapa;
import principal.utilidades.Globales;

public class PuertaMundo extends PuertaTP {

	private final String nombreMundoDestino;
	private final String nombreSpawnDestino;

	public PuertaMundo(final String nombreMundoDestino, final String nombreSpawnDestino) {
		this.nombreMundoDestino = (nombreMundoDestino != null) ? nombreMundoDestino : "exterior";
		this.nombreSpawnDestino = (nombreSpawnDestino != null) ? nombreSpawnDestino : Mundo.CLAVE_PUNTO_SPAWN_COMIENZO;
	}

	@Override
	public void teletransportar(final Criatura c) {
		// Solo el jugador dispara la transición del mapa completo
		if (!(c instanceof Jugador)) {
			return;
		}

		final Mundo mundoOrigen = c.getMundo();
		if (mundoOrigen == null) {
			return;
		}

		final Mapa mapaActual = mundoOrigen.getMapa();
		if (mapaActual == null) {
			return;
		}

		final double liderOrigenX = c.getCentroX();
		final double liderOrigenY = c.getCentroY();

		// 1. Transición del jugador al nuevo mundo
		mapaActual.cambiarMundoInterno(this.nombreMundoDestino, this.nombreSpawnDestino);

		// 2. Migración atómica de los seguidores activos hacia el nuevo submundo
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