package principal.comandos;

import principal.entes.objetos.items.Item;
import principal.utilidades.Globales;

public class ComandoDinero extends Comando {

	public ComandoDinero() {
		super("dinero", "dinero <add | set | clear> <cantidad> [oro|plata]",
				"Añade, consulta o fija el saldo de monedas de plata y oro del jugador.");
	}

	@Override
	public void ejecutar(final String[] args) {
		this.ejecutar(args, null);
	}

	@Override
	public void ejecutar(final String[] args, final EmisorRespuesta emisor) {
		if (Globales.JUGADOR == null) {
			this.enviarError(emisor, "El jugador no está disponible.");
			return;
		}

		if (args.length == 0) {
			final long plata = Globales.JUGADOR.getDineroPlata();
			this.enviarInfo(emisor, "BILLETERA ACTUAL: " + Item.formatearMoneda(plata) + " (Total: " + plata + " de Plata)");
			return;
		}

		final String accion = args[0].toLowerCase().trim();

		if (accion.equals("clear") || accion.equals("vaciar") || accion.equals("cero")) {
			Globales.JUGADOR.setDineroPlata(0);
			this.enviarInfo(emisor, "Saldo de la billetera vaciado a 0.");
			return;
		}

		if (args.length < 2) {
			this.enviarError(emisor, "Uso: dinero add <cantidad> [oro/plata] o dinero set <cantidad> [oro/plata]");
			return;
		}

		final long cant = this.parsearEntero(args[1], 0);
		final boolean esOro = (args.length >= 3) && args[2].toLowerCase().startsWith("oro");
		final long valorPlata = esOro ? (cant * 100L) : cant;

		if (accion.equals("add") || accion.equals("sumar") || accion.equals("+")) {
			Globales.JUGADOR.sumarDinero(valorPlata);
			this.enviarInfo(emisor, "Añadidos +" + Item.formatearMoneda(valorPlata) + " -> Total: "
					+ Item.formatearMoneda(Globales.JUGADOR.getDineroPlata()));
		} else if (accion.equals("set") || accion.equals("fijar") || accion.equals("=")) {
			Globales.JUGADOR.setDineroPlata(valorPlata);
			this.enviarInfo(emisor, "Saldo fijado en: " + Item.formatearMoneda(valorPlata));
		} else {
			this.enviarError(emisor, "Acción desconocida. Usa: add, set o clear");
		}
	}
}