package principal.comandos;

import principal.iluminacion.FuenteLuz;
import principal.iluminacion.TipoLuz;
import principal.utilidades.Globales;

/**
 * Comando para generar fuentes de luz estáticas en el mapa en tiempo de
 * ejecución.
 * <p>
 * Compatible con la consola de Eclipse y la terminal remota de Termux
 * (Android).
 * </p>
 */
public class ComandoLuzMundo extends Comando {

	public ComandoLuzMundo() {
		super("spawnluz", "spawnluz <tipo> [x y] [radio]",
				"Planta una fuente de luz estatica en el suelo (en la posicion del jugador o en X,Y).");
	}

	@Override
	public void ejecutar(final String[] args) {
		this.ejecutar(args, null);
	}

	@Override
	public void ejecutar(final String[] args, final EmisorRespuesta emisor) {
		if ((Globales.GESTOR_LUZ == null) || (Globales.JUGADOR == null)) {
			this.enviarError(emisor, "El subsistema de iluminacion o el jugador no estan inicializados.");
			return;
		}

		if (args.length == 0) {
			this.enviarError(emisor, "Sintaxis requerida: spawnluz <tipo> [radio] o spawnluz <tipo> <x> <y> [radio]");
			return;
		}

		final String tipoStr = args[0].toUpperCase();
		TipoLuz tipo;
		try {
			tipo = TipoLuz.valueOf(tipoStr);
		} catch (final IllegalArgumentException e) {
			final StringBuilder sb = new StringBuilder("Tipo de luz no valido: '").append(tipoStr)
					.append("'. Disponibles: ");
			for (final TipoLuz t : TipoLuz.values()) {
				sb.append(t.name().toLowerCase()).append(" ");
			}
			this.enviarError(emisor, sb.toString());
			return;
		}

		double x = Globales.JUGADOR.getCentroX();
		double y = Globales.JUGADOR.getCentroY();
		double radio = tipo.getRadioBase();

		if (args.length == 2) {
			// spawnluz <tipo> <radio> (en los pies del jugador con radio personalizado)
			radio = this.parsearDouble(args[1], radio);
		} else if (args.length >= 3) {
			// spawnluz <tipo> <x> <y> [radio]
			x = this.parsearDouble(args[1], x);
			y = this.parsearDouble(args[2], y);
			if (args.length >= 4) {
				radio = this.parsearDouble(args[3], radio);
			}
		}

		final FuenteLuz luz = Globales.GESTOR_LUZ.agregarLuzEstatica(x, y, tipo, radio);
		if (luz != null) {
			this.enviarInfo(emisor, "Luz estatica '" + tipo.name() + "' creada en (" + (int) x + ", " + (int) y
					+ ") con radio " + (int) radio + "px.");
		} else {
			this.enviarError(emisor, "No hay ranuras disponibles en el pool de luces (Capacidad maxima alcanzada).");
		}
	}
}