package principal.comandos;

import principal.iluminacion.FuenteLuz;
import principal.iluminacion.TipoLuz;
import principal.utilidades.Globales;

public class ComandoLuz extends Comando {

	public ComandoLuz() {
		super("luz", "luz <tipo | apagar | radio <mult>> [radio_base]", 
				"Equipa una linterna/fuego al jugador, apaga su luz o multiplica su rango visual.");
	}

	@Override
	public void ejecutar(final String[] args) {
		if (Globales.JUGADOR == null || Globales.GESTOR_LUZ == null) {
			System.err.println("[Consola] El jugador o el gestor de luz no estan listos.");
			return;
		}

		if (args.length == 0) {
			final FuenteLuz luz = Globales.JUGADOR.getLuzAsignada();
			if (luz != null && luz.isActiva()) {
				System.out.println("[Consola] Luz activa: " + luz.getTipo().name() 
						+ " | Radio actual: " + (int) luz.getRadioActual() + "px"
						+ " | Es Cono: " + luz.getTipo().isEsCono());
			} else {
				System.out.println("[Consola] El jugador no tiene ninguna fuente de luz encendida.");
			}
			return;
		}

		final String sub = args[0].toUpperCase();

		// Apagar la luz del jugador
		if (sub.equals("APAGAR") || sub.equals("OFF") || sub.equals("QUITAR")) {
			Globales.JUGADOR.desvincularLuz();
			System.out.println("[Consola] Luz del jugador apagada y ranura liberada.");
			return;
		}

		// Modificador de radio (ej: poción visión nocturna x1.6)
		if (sub.equals("RADIO") || sub.equals("RANGO")) {
			if (args.length < 2) {
				System.err.println("[Consola] Uso: luz radio <multiplicador> (ej: luz radio 1.5)");
				return;
			}
			final double factor = this.parsearDouble(args[1], 1.0);
			final FuenteLuz luz = Globales.JUGADOR.getLuzAsignada();
			if (luz != null) {
				luz.setMultiplicadorRadio(factor);
				System.out.println("[Consola] Radio de luz escalado por x" + factor 
						+ " -> Nuevo radio: " + (int) luz.getRadioActual() + "px");
			} else {
				System.err.println("[Consola] El jugador no tiene luz asignada para modificar su radio.");
			}
			return;
		}

		// Asignación de un TipoLuz
		try {
			final TipoLuz tipo = TipoLuz.valueOf(sub);
			final double radio = (args.length >= 2) 
					? this.parsearDouble(args[1], tipo.getRadioBase()) 
					: tipo.getRadioBase();

			Globales.GESTOR_LUZ.agregarLuzAnclada(Globales.JUGADOR, tipo, radio);
			System.out.println("[Consola] Luz '" + tipo.name() + "' vinculada al jugador con radio " + (int) radio + "px.");
		} catch (final IllegalArgumentException e) {
			System.err.println("[Consola] TipoLuz desconocido: '" + sub + "'. Opciones disponibles:");
			for (final TipoLuz t : TipoLuz.values()) {
				System.err.print(t.name() + " ");
			}
			System.err.println();
		}
	}
}