package principal.comandos;

import principal.entes.facciones.GestorFacciones;
import principal.utilidades.Globales;

public class ComandoFaccion extends Comando {

	public ComandoFaccion() {
		super("faccion", "faccion <jugador <bando> | hostil <faccion1> <faccion2> <true/false> | reset>",
				"Modifica la facción del jugador o altera la diplomacia global de hostilidad entre bandos.");
	}

	@Override
	public void ejecutar(final String[] args) {
		this.ejecutar(args, null);
	}

	@Override
	public void ejecutar(final String[] args, final EmisorRespuesta emisor) {
		if (Globales.JUGADOR == null) {
			this.enviarError(emisor, "El jugador no está listo.");
			return;
		}

		if (args.length == 0) {
			this.enviarInfo(emisor, "FACCIÓN DEL JUGADOR: Bit " + Globales.JUGADOR.getFaccionBit()
					+ "\nComandos: faccion jugador <jugador|bandidos|monstruos|aldeanos|neutral> | faccion reset");
			return;
		}

		final String sub = args[0].toLowerCase().trim();

		if (sub.equals("reset") || sub.equals("default")) {
			GestorFacciones.inicializarMatrizHostilidadPorDefecto();
			Globales.JUGADOR.setFaccion(GestorFacciones.FACCION_JUGADOR);
			this.enviarInfo(emisor, "Diplomacia y facciones restauradas a sus valores por defecto.");
			return;
		}

		if (sub.equals("jugador") || sub.equals("player")) {
			if (args.length < 2) {
				this.enviarError(emisor, "Elige bando: JUGADOR, BANDIDOS, MONSTRUOS, ALDEANOS, NEUTRAL");
				return;
			}
			final int bit = this.parsearFaccion(args[1]);
			Globales.JUGADOR.setFaccion(bit);
			this.enviarInfo(emisor, "Facción del jugador establecida en: " + args[1].toUpperCase() + " (Bit " + bit + ")");
			return;
		}

		this.enviarError(emisor, "Uso: faccion jugador <bando> o faccion reset");
	}

	private int parsearFaccion(final String str) {
		final String s = str.toUpperCase().trim();
		if (s.contains("BANDIDO")) return GestorFacciones.FACCION_BANDIDOS;
		if (s.contains("MONSTRUO") || s.contains("BESTIA")) return GestorFacciones.FACCION_MONSTRUOS;
		if (s.contains("ALDEANO") || s.contains("NPC")) return GestorFacciones.FACCION_ALDEANOS;
		if (s.contains("FAUNA")) return GestorFacciones.FACCION_FAUNA_PASIVA;
		if (s.contains("NEUTRAL")) return GestorFacciones.FACCION_NEUTRAL;
		return GestorFacciones.FACCION_JUGADOR;
	}
}