package principal.comandos;

import principal.utilidades.Globales;

public class ComandoTeleport extends Comando {

	public ComandoTeleport() {
		super("tp", "tp <x> <y>", "Teletransporta al jugador a las coordenadas X e Y en pixeles.");
	}

	@Override
	public void ejecutar(final String[] args) {
		if (Globales.JUGADOR == null) {
			System.err.println("[Consola] El jugador no esta cargado.");
			return;
		}

		if (args.length < 2) {
			System.err.println("[Consola] Uso incorrecto. Sintaxis: " + this.getSintaxis());
			return;
		}

		final double x = this.parsearDouble(args[0], Globales.JUGADOR.getPosicionX());
		final double y = this.parsearDouble(args[1], Globales.JUGADOR.getPosicionY());

		Globales.JUGADOR.establecerPosicion(x, y);
		System.out.println("[Consola] Jugador teletransportado a: (" + x + ", " + y + ")");
	}
}