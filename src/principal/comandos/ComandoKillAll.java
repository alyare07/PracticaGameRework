package principal.comandos;

import principal.utilidades.Globales;

/**
 * Comando de depuración para purgar todas las criaturas del mapa activo.
 * <p>
 * Envía la confirmación tanto a la consola local de Eclipse como a Termux en
 * Android.
 * </p>
 */
public class ComandoKillAll extends Comando {

	public ComandoKillAll() {
		super("killall", "killall", "Elimina a todos los enemigos y criaturas del mapa actual (excepto al jugador).");
	}

	@Override
	public void ejecutar(final String[] args) {
		this.ejecutar(args, null);
	}

	@Override
	public void ejecutar(final String[] args, final EmisorRespuesta emisor) {
		if ((Globales.JUGADOR == null) || (Globales.JUGADOR.getMundo() == null)) {
			this.enviarError(emisor, "El jugador o el mundo aun no estan cargados en memoria.");
			return;
		}

		// Ejecuta la purga en el hilo lógico del Game Loop
		Globales.JUGADOR.getMundo().eliminarCriaturas();

		this.enviarInfo(emisor, "Todas las criaturas del mapa han sido eliminadas y sus slots liberados.");
	}
}