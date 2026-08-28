package principal.comandos;

import principal.iluminacion.IntensidadNiebla;
import principal.utilidades.Globales;

public class ComandoAmbiente extends Comando {

	public ComandoAmbiente() {
		super("ambiente", "ambiente <cueva | exterior | niebla <nivel> | rayo | blackout <true/false>>",
				"Modula la atmosfera: entra/sale de cuevas, cambia niebla o dispara rayos.");
	}

	@Override
	public void ejecutar(final String[] args) {
		if (Globales.GESTOR_LUZ == null) {
			System.err.println("[Consola] GestorLuz no inicializado.");
			return;
		}

		if (args.length == 0) {
			System.out.println("[Consola] Modo Cueva Fijo: " + Globales.GESTOR_LUZ.isModoAmbienteFijo()
					+ " | Oscuridad Alpha: " + Globales.GESTOR_LUZ.getAlphaOscuridadActual() + "/255"
					+ " | Luces activas: " + Globales.GESTOR_LUZ.getCantidadActivas());
			return;
		}

		final String accion = args[0].toLowerCase();

		switch (accion) {
		case "cueva":
			final boolean total = (args.length >= 2) && args[1].equalsIgnoreCase("total");
			Globales.GESTOR_LUZ.establecerModoCueva(total);
			System.out.println("[Consola] Modo Cueva activado (Oscuridad total: " + total + ").");
			break;

		case "exterior":
		case "salir":
			Globales.GESTOR_LUZ.restablecerModoExterior();
			System.out.println("[Consola] Modo Exterior restaurado. El reloj solar vuelve a controlar la luz.");
			break;

		case "rayo":
		case "relampago":
			final double duracion = (args.length >= 2) ? this.parsearDouble(args[1], 0.30) : 0.30;
			Globales.GESTOR_LUZ.dispararFlashGlobal(duracion, true);
			System.out.println("[Consola] Relampago disparado (" + duracion + "s).");
			break;

		case "flash":
			// Flash posicional en la ubicación del jugador (ej: explosión)
			if (Globales.JUGADOR != null) {
				final double radioExp = (args.length >= 2) ? this.parsearDouble(args[1], 180.0) : 180.0;
				final double durExp = (args.length >= 3) ? this.parsearDouble(args[2], 0.40) : 0.40;
				Globales.GESTOR_LUZ.dispararFlashPosicional(Globales.JUGADOR.getCentroX(),
						Globales.JUGADOR.getCentroY(), radioExp, durExp);
				System.out.println("[Consola] Flash detonado en el jugador (Radio: " + (int) radioExp + "px).");
			}
			break;

		case "blackout":
			final boolean bo = (args.length >= 2) ? Boolean.parseBoolean(args[1])
					: !Globales.GESTOR_LUZ.getCiclo().isModoOscuridadTotal();
			Globales.GESTOR_LUZ.getCiclo().setModoOscuridadTotal(bo);
			System.out.println("[Consola] Modo Oscuridad Total Nocturna: " + bo);
			break;

		case "niebla":
			if (args.length < 2) {
				System.err.println("[Consola] Opciones de niebla: DESACTIVADA, LEVE, MODERADA, INTENSA");
				return;
			}
			try {
				final IntensidadNiebla nivel = IntensidadNiebla.valueOf(args[1].toUpperCase());
				final double segs = (args.length >= 3) ? this.parsearDouble(args[2], 2.0) : 2.0;
				Globales.GESTOR_CLIMA.setNivelNiebla(nivel, segs);
				System.out.println("[Consola] Niebla cambiada a " + nivel.name() + " en " + segs + "s.");
			} catch (final IllegalArgumentException e) {
				System.err.println("[Consola] Nivel de niebla desconocido: " + args[1]);
			}
			break;

		case "limpiarluces":
			Globales.GESTOR_LUZ.apagarTodasLasLuces();
			System.out.println("[Consola] Todas las luces del mapa han sido apagadas y devueltas al pool.");
			break;

		default:
			System.err.println("[Consola] Accion no reconocida: " + accion);
			break;
		}
	}
}