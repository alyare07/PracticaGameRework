package principal.comandos;

import principal.utilidades.Globales;

/**
 * Comando para restaurar la salud del jugador. Compatible con la consola local
 * y la terminal remota de Termux.
 */
public class ComandoCurar extends Comando {

	public ComandoCurar() {
		super("curar", "curar [cantidad]", "Restaura la salud del jugador. Si no se indica cantidad, cura al 100%.");
	}

	@Override
	public void ejecutar(final String[] args) {
		this.ejecutar(args, null);
	}

	@Override
	public void ejecutar(final String[] args, final EmisorRespuesta emisor) {
		if (Globales.JUGADOR == null) {
			this.enviarError(emisor, "El jugador aun no esta inicializado en el mundo.");
			return;
		}

		if (args.length == 0) {
			// Sin argumentos: Curación total
			Globales.JUGADOR.sanar();
			this.enviarInfo(emisor, "Jugador curado al 100%.");
		} else {
			// Con argumento: curar cantidad específica
			final int cantidad = this.parsearEntero(args[0], 0);
			if (cantidad > 0) {
				Globales.JUGADOR.curar(cantidad);
				this.enviarInfo(emisor, "Se han restaurado " + cantidad + " puntos de salud al jugador.");
			} else {
				this.enviarError(emisor, "La cantidad a curar debe ser un numero entero mayor a 0.");
			}
		}
	}
}