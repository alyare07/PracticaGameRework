package principal.comandos;

import principal.mapa.mapas.Mapa;
import principal.utilidades.Globales;

public class ComandoMundo extends Comando {

	public ComandoMundo() {
		super("mundo", "mundo <list | warp <nombreMundo> [nombreSpawn]>",
				"Lista los submundos del mapa activo y teletransporta instantáneamente al jugador entre ellos.");
	}

	@Override
	public void ejecutar(final String[] args) {
		this.ejecutar(args, null);
	}

	@Override
	public void ejecutar(final String[] args, final EmisorRespuesta emisor) {
		if (Globales.JUGADOR == null || Globales.JUGADOR.getMundo() == null || Globales.JUGADOR.getMundo().getMapa() == null) {
			this.enviarError(emisor, "El mapa activo no está disponible.");
			return;
		}

		final Mapa mapa = Globales.JUGADOR.getMundo().getMapa();

		if (args.length == 0 || args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("lista")) {
			final StringBuilder sb = new StringBuilder("SUBMUNDOS DISPONIBLES EN [").append(mapa.getNombre()).append("]:\n");
			for (final String nombre : mapa.getNombreMundos()) {
				final boolean esActual = nombre.equalsIgnoreCase(Globales.JUGADOR.getMundo().getNombreMundo());
				sb.append(esActual ? " -> * " : " ->   ").append(nombre).append(esActual ? " (ACTUAL)\n" : "\n");
			}
			this.enviarInfo(emisor, sb.toString());
			return;
		}

		final String sub = args[0].toLowerCase().trim();

		if (sub.equals("warp") || sub.equals("ir") || sub.equals("tp")) {
			if (args.length < 2) {
				this.enviarError(emisor, "Especifica el nombre del mundo. Ejemplo: 'mundo warp interior_casa1'");
				return;
			}
			final String destino = args[1];
			final String spawn = (args.length >= 3) ? args[2] : "Comienzo";

			if (mapa.getMundo(destino) != null) {
				mapa.cambiarMundoInterno(destino, spawn);
				this.enviarInfo(emisor, "Transición exitosa hacia: " + destino + " [Spawn: " + spawn + "]");
			} else {
				this.enviarError(emisor, "El submundo '" + destino + "' no existe en este mapa. Usa 'mundo list' para ver nombres.");
			}
			return;
		}

		this.enviarError(emisor, "Comando desconocido. Usa 'mundo list' o 'mundo warp <nombre>'");
	}
}