package principal.comandos;

import principal.utilidades.Globales;

public class ComandoVelocidad extends Comando {

	public ComandoVelocidad() {
		super("velocidad", "velocidad <valor | reset>", "Ajusta la velocidad base de movimiento del jugador.");
	}

	@Override
	public void ejecutar(final String[] args) {
		if (Globales.JUGADOR == null) {
			System.err.println("[Consola] El jugador no esta disponible.");
			return;
		}

		if (args.length == 0) {
			System.err.println("[Consola] Debes especificar un valor. Uso: velocidad <valor> (ej: velocidad 2.5) o velocidad reset");
			return;
		}

		final String param = args[0].toLowerCase();

		// Restablece la velocidad estándar
		if (param.equals("reset") || param.equals("normal") || param.equals("default")) {
			Globales.JUGADOR.setVelocidadBase(1.0);
			System.out.println("[Consola] Velocidad base del jugador restablecida a 1.0.");
			return;
		}

		// Parseo de valor numérico
		final double nuevaVelocidad = this.parsearDouble(args[0], -1.0);
		if (nuevaVelocidad > 0.0) {
			Globales.JUGADOR.setVelocidadBase(nuevaVelocidad);
			System.out.println("[Consola] Velocidad base del jugador ajustada a: " + nuevaVelocidad);
		} else {
			System.err.println("[Consola] La velocidad debe ser un numero decimal positivo (ej: 0.8, 1.5, 3.0).");
		}
	}
}