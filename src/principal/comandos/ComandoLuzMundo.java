package principal.comandos;

import principal.iluminacion.FuenteLuz;
import principal.iluminacion.TipoLuz;
import principal.utilidades.Globales;

public class ComandoLuzMundo extends Comando {

	public ComandoLuzMundo() {
		super("spawnluz", "spawnluz <tipo> [x y] [radio]", 
				"Planta una fuente de luz estatica en el suelo (en la posicion del jugador o en X,Y).");
	}

	@Override
	public void ejecutar(final String[] args) {
		if (Globales.GESTOR_LUZ == null || Globales.JUGADOR == null) {
			System.err.println("[Consola] Gestores no disponibles.");
			return;
		}

		if (args.length == 0) {
			System.err.println("[Consola] Uso: spawnluz <tipo> (en posicion actual) o spawnluz <tipo> <x> <y> [radio]");
			return;
		}

		final String tipoStr = args[0].toUpperCase();
		TipoLuz tipo;
		try {
			tipo = TipoLuz.valueOf(tipoStr);
		} catch (final IllegalArgumentException e) {
			System.err.println("[Consola] Tipo de luz no valido: " + tipoStr);
			return;
		}

		double x = Globales.JUGADOR.getCentroX();
		double y = Globales.JUGADOR.getCentroY();
		double radio = tipo.getRadioBase();

		if (args.length == 2) {
			// spawnluz <tipo> <radio> (en los pies del jugador)
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
			System.out.println("[Consola] Luz estatica '" + tipo.name() + "' creada en (" 
					+ (int) x + ", " + (int) y + ") con radio " + (int) radio + "px.");
		} else {
			System.err.println("[Consola] No hay ranuras disponibles en el pool de luces (Capacidad: 256).");
		}
	}
}