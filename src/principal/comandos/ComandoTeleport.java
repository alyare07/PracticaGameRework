package principal.comandos;

import principal.mapa.mapas.Spawn;
import principal.utilidades.Globales;

/**
 * Comando de depuración para teletransportar al jugador a coordenadas numéricas
 * o a puntos de aparición registrados en el mapa.
 * <p>
 * Totalmente compatible con la consola local de Eclipse y Termux (Android).
 * </p>
 */
public class ComandoTeleport extends Comando {

	public ComandoTeleport() {
		super("tp", "tp <x> <y> | tp <nombre_spawn>",
				"Teletransporta al jugador a las coordenadas (X, Y) o a un punto de Spawn registrado.");
	}

	@Override
	public void ejecutar(final String[] args) {
		this.ejecutar(args, null);
	}

	@Override
	public void ejecutar(final String[] args, final EmisorRespuesta emisor) {
		if (Globales.JUGADOR == null) {
			this.enviarError(emisor, "El jugador no esta cargado en memoria.");
			return;
		}

		if (args.length == 0) {
			this.enviarError(emisor, "Uso incorrecto. Sintaxis: " + this.getSintaxis());
			return;
		}

		// =====================================================================
		// === 1. TELETRANSPORTE POR NOMBRE DE SPAWN (Ej: "tp Comienzo")
		// =====================================================================
		if (args.length == 1) {
			final String nombreSpawn = args[0];
			if (Globales.JUGADOR.getMundo() != null) {
				final Spawn spawn = Globales.JUGADOR.getMundo().getSpawn(nombreSpawn);
				if (spawn != null) {
					spawn.moverJugadorCentrado();
					this.enviarInfo(emisor, "Jugador teletransportado al Spawn '" + nombreSpawn + "' (" + spawn.getX()
							+ ", " + spawn.getY() + ").");
					return;
				}
			}
			this.enviarError(emisor, "No se encontro el spawn '" + nombreSpawn + "'. Usa: tp <x> <y>");
			return;
		}

		// =====================================================================
		// === 2. TELETRANSPORTE POR COORDENADAS NUMÉRICAS (Ej: "tp 500 800")
		// =====================================================================
		final double posXActual = Globales.JUGADOR.getPosicionX();
		final double posYActual = Globales.JUGADOR.getPosicionY();

		final double destinoX = this.parsearDouble(args[0], posXActual);
		final double destinoY = this.parsearDouble(args[1], posYActual);

		Globales.JUGADOR.setPosicion(destinoX, destinoY);

		this.enviarInfo(emisor, "Jugador teletransportado a: (" + (int) destinoX + ", " + (int) destinoY + ")");
	}
}