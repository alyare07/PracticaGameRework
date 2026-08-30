package principal.comandos;

import principal.iluminacion.IntensidadNiebla;
import principal.utilidades.Globales;

/**
 * Comando para modular los efectos atmosféricos del juego (iluminación, cuevas,
 * rayos, niebla).
 * <p>
 * Compatible con la consola local de Eclipse y sesiones remotas de Termux.
 * </p>
 */
public class ComandoAmbiente extends Comando {

	public ComandoAmbiente() {
		super("ambiente",
				"ambiente <cueva | exterior | niebla <nivel> | rayo | flash | blackout <true/false> | limpiarluces>",
				"Modula la atmosfera: entra/sale de cuevas, cambia niebla o dispara rayos.");
	}

	@Override
	public void ejecutar(final String[] args) {
		this.ejecutar(args, null);
	}

	@Override
	public void ejecutar(final String[] args, final EmisorRespuesta emisor) {
		if (Globales.GESTOR_LUZ == null) {
			this.enviarError(emisor, "GestorLuz no inicializado.");
			return;
		}

		// Si se escribe solo "ambiente", muestra el estado actual
		if (args.length == 0) {
			this.enviarInfo(emisor,
					"Modo Cueva Fijo: " + Globales.GESTOR_LUZ.isModoAmbienteFijo() + " | Oscuridad Alpha: "
							+ Globales.GESTOR_LUZ.getAlphaOscuridadActual() + "/255" + " | Luces activas: "
							+ Globales.GESTOR_LUZ.getCantidadActivas());
			return;
		}

		final String accion = args[0].toLowerCase();

		switch (accion) {
		case "cueva":
			final boolean total = (args.length >= 2) && args[1].equalsIgnoreCase("total");
			Globales.GESTOR_LUZ.establecerModoCueva(total);
			this.enviarInfo(emisor, "Modo Cueva activado (Oscuridad total: " + total + ").");
			break;

		case "exterior":
		case "salir":
			Globales.GESTOR_LUZ.restablecerModoExterior();
			this.enviarInfo(emisor, "Modo Exterior restaurado. El reloj solar vuelve a controlar la luz.");
			break;

		case "rayo":
		case "relampago":
			final double duracion = (args.length >= 2) ? this.parsearDouble(args[1], 0.30) : 0.30;
			Globales.GESTOR_LUZ.dispararFlashGlobal(duracion, true);
			this.enviarInfo(emisor, "Relampago disparado (" + duracion + "s).");
			break;

		case "flash":
			// Flash posicional en la ubicación del jugador (ej: explosión)
			if (Globales.JUGADOR != null) {
				final double radioExp = (args.length >= 2) ? this.parsearDouble(args[1], 180.0) : 180.0;
				final double durExp = (args.length >= 3) ? this.parsearDouble(args[2], 0.40) : 0.40;
				Globales.GESTOR_LUZ.dispararFlashPosicional(Globales.JUGADOR.getCentroX(),
						Globales.JUGADOR.getCentroY(), radioExp, durExp);
				this.enviarInfo(emisor, "Flash detonado en el jugador (Radio: " + (int) radioExp + "px).");
			} else {
				this.enviarError(emisor, "El jugador no esta activo en el mapa para detonar el flash.");
			}
			break;

		case "blackout":
			if (Globales.GESTOR_LUZ.getCiclo() == null) {
				this.enviarError(emisor, "Ciclo solar no disponible.");
				return;
			}
			final boolean bo = (args.length >= 2) ? Boolean.parseBoolean(args[1])
					: !Globales.GESTOR_LUZ.getCiclo().isModoOscuridadTotal();
			Globales.GESTOR_LUZ.getCiclo().setModoOscuridadTotal(bo);
			this.enviarInfo(emisor, "Modo Oscuridad Total Nocturna: " + bo);
			break;

		case "niebla":
			if (Globales.GESTOR_CLIMA == null) {
				this.enviarError(emisor, "GestorClima no inicializado.");
				return;
			}
			if (args.length < 2) {
				final StringBuilder sb = new StringBuilder("Opciones de niebla: ");
				for (final IntensidadNiebla nivelEnum : IntensidadNiebla.values()) {
					sb.append(nivelEnum.name()).append(" ");
				}
				this.enviarError(emisor, sb.toString());
				return;
			}
			try {
				final IntensidadNiebla nivel = IntensidadNiebla.valueOf(args[1].toUpperCase());
				final double segs = (args.length >= 3) ? this.parsearDouble(args[2], 2.0) : 2.0;
				Globales.GESTOR_CLIMA.setNivelNiebla(nivel, segs);
				this.enviarInfo(emisor, "Niebla cambiada a " + nivel.name() + " en " + segs + "s.");
			} catch (final IllegalArgumentException e) {
				this.enviarError(emisor, "Nivel de niebla desconocido: '" + args[1] + "'.");
			}
			break;

		case "limpiarluces":
			Globales.GESTOR_LUZ.apagarTodasLasLuces();
			this.enviarInfo(emisor, "Todas las luces del mapa han sido apagadas y devueltas al pool.");
			break;

		default:
			this.enviarError(emisor, "Accion no reconocida: '" + accion + "'. Escribe 'ayuda' para ver la sintaxis.");
			break;
		}
	}
}