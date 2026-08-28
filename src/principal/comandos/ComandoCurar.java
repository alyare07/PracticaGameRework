package principal.comandos;

import principal.utilidades.Globales;

public class ComandoCurar extends Comando {

	public ComandoCurar() {
		super("curar", "curar [cantidad]", "Restaura la salud del jugador. Si no se indica cantidad, cura al 100%.");
	}

	@Override
	public void ejecutar(final String[] args) {
		if (Globales.JUGADOR == null) {
			System.err.println("[Consola] El jugador aun no esta inicializado en el mundo.");
			return;
		}

		if (args.length == 0) {
			// Sin argumentos: Curación total (asumiendo método recuperarSalud o setSalud)
			Globales.JUGADOR.sanar();
			System.out.println("[Consola] Jugador curado al 100%.");
		} else {
			// Con argumento: curar cantidad específica
			final int cantidad = this.parsearEntero(args[0], 0);
			if (cantidad > 0) {
				Globales.JUGADOR.curar(cantidad);
				System.out.println("[Consola] Se han restaurado " + cantidad + " puntos de salud al jugador.");
			} else {
				System.err.println("[Consola] La cantidad a curar debe ser mayor a 0.");
			}
		}
	}
}