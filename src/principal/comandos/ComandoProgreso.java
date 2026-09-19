package principal.comandos;

import principal.utilidades.Globales;
import principal.utilidades.progreso.FlagProgreso;

public class ComandoProgreso extends Comando {

	public ComandoProgreso() {
		super("flag", "flag <list | set <nombre> <true/false> | toggle <nombre> | clear | ayuda>",
				"Consulta, activa o desactiva banderas de historia, misiones y jefes derrotados.");
	}

	@Override
	public void ejecutar(final String[] args) {
		this.ejecutar(args, null);
	}

	@Override
	public void ejecutar(final String[] args, final EmisorRespuesta emisor) {
		if (Globales.GESTOR_PROGRESO == null) {
			this.enviarError(emisor, "El gestor de progreso no está inicializado.");
			return;
		}

		if (args.length == 0 || args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("lista")) {
			final StringBuilder sb = new StringBuilder("=== ESTADO DE FLAGS DE HISTORIA ===\n");
			for (final FlagProgreso f : FlagProgreso.values()) {
				final boolean activo = Globales.GESTOR_PROGRESO.isActivo(f);
				sb.append(activo ? " [x] " : " [ ] ").append(f.name()).append(" -> ").append(f.getDescripcion()).append("\n");
			}
			this.enviarInfo(emisor, sb.toString());
			return;
		}

		final String sub = args[0].toLowerCase().trim();

		if (sub.equals("clear") || sub.equals("reset")) {
			Globales.GESTOR_PROGRESO.limpiar();
			this.enviarInfo(emisor, "Todos los flags de historia han sido reiniciados a falso.");
			return;
		}

		if (args.length < 2) {
			this.enviarError(emisor, "Uso: flag set <NOMBRE_FLAG> <true/false> o flag toggle <NOMBRE_FLAG>");
			return;
		}

		final String flagStr = args[1].toUpperCase().trim();
		FlagProgreso flag;
		try {
			flag = FlagProgreso.valueOf(flagStr);
		} catch (final IllegalArgumentException e) {
			this.enviarError(emisor, "Flag desconocido: '" + args[1] + "'. Escribe 'flag list' para ver nombres válidos.");
			return;
		}

		if (sub.equals("toggle") || sub.equals("cambiar")) {
			Globales.GESTOR_PROGRESO.conmutar(flag);
			final boolean ahora = Globales.GESTOR_PROGRESO.isActivo(flag);
			this.enviarInfo(emisor, "Flag " + flag.name() + " conmutado a: " + (ahora ? "ACTIVO" : "INACTIVO"));
			return;
		}

		if (sub.equals("set") || sub.equals("fijar")) {
			final boolean valor = (args.length >= 3) ? Boolean.parseBoolean(args[2]) : true;
			if (valor) {
				Globales.GESTOR_PROGRESO.activar(flag);
			} else {
				Globales.GESTOR_PROGRESO.desactivar(flag);
			}
			this.enviarInfo(emisor, "Flag " + flag.name() + " establecido en: " + (valor ? "ACTIVO" : "INACTIVO"));
			return;
		}

		this.enviarError(emisor, "Opción no válida. Usa 'flag list', 'flag set' o 'flag toggle'.");
	}
}