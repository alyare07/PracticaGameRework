package principal.comandos;

import principal.iluminacion.FuenteLuz;
import principal.iluminacion.TipoLuz;
import principal.utilidades.Globales;

/**
 * Comando para consultar, encender, apagar o modificar la fuente de luz
 * asociada al jugador.
 * <p>
 * Compatible con Java 8 y ejecución dual (Consola IDE / Terminal Remota
 * Termux).
 * </p>
 */
public class ComandoLuz extends Comando {

	public ComandoLuz() {
		super("luz", "luz <tipo | apagar | radio <mult>> [radio_base]",
				"Equipa una linterna/fuego al jugador, apaga su luz o multiplica su rango visual.");
	}

	@Override
	public void ejecutar(final String[] args) {
		this.ejecutar(args, null);
	}

	@Override
	public void ejecutar(final String[] args, final EmisorRespuesta emisor) {
		if ((Globales.JUGADOR == null) || (Globales.GESTOR_LUZ == null)) {
			this.enviarError(emisor, "El jugador o el gestor de luz no estan listos.");
			return;
		}

		// 1. Consulta del estado actual de la luz si no se pasan parámetros
		if (args.length == 0) {
			final FuenteLuz luz = Globales.JUGADOR.getLuzAsignada();
			if ((luz != null) && luz.isActiva()) {
				this.enviarInfo(emisor, "Luz activa: " + luz.getTipo().name() + " | Radio actual: "
						+ (int) luz.getRadioActual() + "px" + " | Es Cono: " + luz.getTipo().isEsCono());
			} else {
				this.enviarInfo(emisor, "El jugador no tiene ninguna fuente de luz encendida.");
			}
			return;
		}

		final String sub = args[0].toUpperCase();

		// 2. Apagar y desvincular luz
		if (sub.equals("APAGAR") || sub.equals("OFF") || sub.equals("QUITAR")) {
			Globales.JUGADOR.desvincularLuz();
			this.enviarInfo(emisor, "Luz del jugador apagada y ranura liberada.");
			return;
		}

		// 3. Modificador dinámico de radio (ej: pociones, mejoras de linterna)
		if (sub.equals("RADIO") || sub.equals("RANGO")) {
			if (args.length < 2) {
				this.enviarError(emisor, "Uso: luz radio <multiplicador> (ej: luz radio 1.5)");
				return;
			}
			final double factor = this.parsearDouble(args[1], 1.0);
			final FuenteLuz luz = Globales.JUGADOR.getLuzAsignada();
			if (luz != null) {
				luz.setMultiplicadorRadio(factor);
				this.enviarInfo(emisor, "Radio de luz escalado por x" + factor + " -> Nuevo radio: "
						+ (int) luz.getRadioActual() + "px");
			} else {
				this.enviarError(emisor, "El jugador no tiene luz asignada para modificar su radio.");
			}
			return;
		}

		// 4. Asignación directa por TipoLuz
		try {
			final TipoLuz tipo = TipoLuz.valueOf(sub);
			final double radio = (args.length >= 2) ? this.parsearDouble(args[1], tipo.getRadioBase())
					: tipo.getRadioBase();

			Globales.GESTOR_LUZ.agregarLuzAnclada(Globales.JUGADOR, tipo, radio);
			this.enviarInfo(emisor, "Luz '" + tipo.name() + "' vinculada al jugador con radio " + (int) radio + "px.");
		} catch (final IllegalArgumentException e) {
			final StringBuilder sb = new StringBuilder("TipoLuz desconocido: '").append(sub)
					.append("'. Opciones disponibles: ");
			for (final TipoLuz t : TipoLuz.values()) {
				sb.append(t.name()).append(" ");
			}
			this.enviarError(emisor, sb.toString());
		}
	}
}