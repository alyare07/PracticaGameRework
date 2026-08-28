package principal.comandos;

import principal.utilidades.Globales;

public class ComandoKillAll extends Comando {

	public ComandoKillAll() {
		super("killall", "killall", "Elimina a todos los enemigos y criaturas del mapa actual (excepto al jugador).");
	}

	@Override
	public void ejecutar(final String[] args) {
		if ((Globales.JUGADOR == null) || (Globales.JUGADOR.getMundo() == null)) {
			System.err.println("[Consola] El jugador o el mundo aun no estan cargados en memoria.");
			return;
		}

		Globales.JUGADOR.getMundo().eliminarCriaturas();
		System.out.println("[Consola] Todas las criaturas del mapa han sido eliminadas y sus slots liberados.");
	}
}