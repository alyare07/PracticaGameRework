package principal.comandos;

import principal.clima.GestorTermicoJugador;
import principal.utilidades.Globales;

public class ComandoTermico extends Comando {

	public ComandoTermico() {
		super("temp", "temp <celsius | freeze | heat | reset | status>",
				"Fija la temperatura corporal del jugador para probar hipotermia severa, hipertermia y viñetas.");
	}

	@Override
	public void ejecutar(final String[] args) {
		this.ejecutar(args, null);
	}

	@Override
	public void ejecutar(final String[] args, final EmisorRespuesta emisor) {
		if (Globales.GESTOR_TERMICO_JUGADOR == null) {
			this.enviarError(emisor, "El gestor térmico del jugador no está disponible.");
			return;
		}

		final GestorTermicoJugador termico = Globales.GESTOR_TERMICO_JUGADOR;

		if (args.length == 0 || args[0].equalsIgnoreCase("status")) {
			this.enviarInfo(emisor, "ESTADO TÉRMICO:"
					+ "\n -> Temp Corporal : " + String.format("%.2f", termico.getTemperaturaCorporal()) + " °C"
					+ "\n -> Hipotermia    : " + (termico.isHipotermia() ? (termico.isHipotermiaSevera() ? "SEVERA (Nivel 3)" : "Nivel 1-2") : "NO")
					+ "\n -> Hipertermia   : " + (termico.isHipertermia() ? "ACTIVA" : "NO")
					+ "\n -> Cerca Fuego   : " + (termico.isCercaDeFuenteCalor() ? "SÍ" : "NO")
					+ "\n -> Bajo Techo    : " + (termico.isBajoTechoInterior() ? "SÍ" : "NO"));
			return;
		}

		final String sub = args[0].toLowerCase().trim();

		if (sub.equals("freeze") || sub.equals("congelar") || sub.equals("frio")) {
			termico.setTemperaturaCorporal(26.0); // Hipotermia Nivel 3 instantánea
			this.enviarInfo(emisor, "Temperatura forzada a 26.0 °C (Hipotermia Severa / Viñeta de Escarcha Máxima).");
			return;
		}

		if (sub.equals("heat") || sub.equals("quemar") || sub.equals("calor")) {
			termico.setTemperaturaCorporal(41.5); // Hipertermia Nivel 3 instantánea
			this.enviarInfo(emisor, "Temperatura forzada a 41.5 °C (Hipertermia Severa / Modo Borracho por Golpe de Calor).");
			return;
		}

		if (sub.equals("reset") || sub.equals("normal")) {
			termico.restablecerTemperaturaNominal();
			this.enviarInfo(emisor, "Temperatura corporal restablecida a 37.0 °C (Confort).");
			return;
		}

		final double temp = this.parsearDouble(args[0], -999);
		if (temp > -100) {
			termico.setTemperaturaCorporal(temp);
			this.enviarInfo(emisor, "Temperatura corporal fijada en: " + temp + " °C");
		} else {
			this.enviarError(emisor, "Parámetro inválido. Usa: temp freeze, temp heat, temp reset o temp <grados>");
		}
	}
}