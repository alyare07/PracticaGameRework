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

		// Guarda la posición en la puerta ANTES de cambiar de submundo
		final double liderOrigenX = c.getCentroX();
		final double liderOrigenY = c.getCentroY();

		// 1. Transición del jugador al nuevo mundo del mapa
		mapaActual.cambiarMundoInterno(this.nombreMundoDestino, this.nombreSpawnDestino);

		// 2. Migración atómica en abanico evaluando cercanía real en la puerta
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