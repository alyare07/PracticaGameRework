package principal.comandos;

import principal.utilidades.Globales;

/**
 * Comando para modificar en tiempo real la velocidad de desplazamiento del
 * jugador. Compatible con la consola de Eclipse y la terminal remota de Termux.
 */
public class ComandoVelocidad extends Comando {

	public ComandoVelocidad() {
		super("velocidad", "velocidad <valor | reset>", "Ajusta la velocidad base de movimiento del jugador.");
	}

	@Override
	public void ejecutar(final String[] args) {
		this.ejecutar(args, null);
	}

	@Override
	public void ejecutar(final String[] args, final EmisorRespuesta emisor) {
		if (Globales.JUGADOR == null) {
			this.enviarError(emisor, "El jugador no esta disponible o no ha sido inicializado en el mundo.");
			return;
		}

		if (args.length == 0) {
			this.enviarError(emisor,
					"Debes especificar un valor. Uso: velocidad <valor> (ej: velocidad 2.5) o velocidad reset");
			return;
		}

		final String param = args[0].toLowerCase();

		// Restablece la velocidad estándar (1.0)
		if (param.equals("reset") || param.equals("normal") || param.equals("default")) {
			Globales.JUGADOR.setVelocidadBase(1.0);
			this.enviarInfo(emisor, "Velocidad base del jugador restablecida a 1.0.");
			return;
		}

		// Parseo del valor numérico
		final double nuevaVelocidad = this.parsearDouble(args[0], -1.0);
		if (nuevaVelocidad > 0.0) {
			Globales.JUGADOR.setVelocidadBase(nuevaVelocidad);
			this.enviarInfo(emisor, "Velocidad base del jugador ajustada a: " + nuevaVelocidad);
		} else {
			this.enviarError(emisor, "La velocidad debe ser un numero decimal positivo (ej: 0.8, 1.5, 3.0).");
		}
	}
}