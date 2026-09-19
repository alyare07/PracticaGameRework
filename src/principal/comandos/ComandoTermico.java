package principal.comandos;

import principal.clima.GestorTermicoJugador;
import principal.utilidades.Globales;

public class ComandoTermico extends Comando {

	public ComandoTermico() {
		super("temp", "temp <celsius | freeze | heat | reset | on | off | toggle | status>",
				"Fija la temperatura corporal o activa/desactiva la simulación térmica del jugador para pruebas.");
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

		if ((args.length == 0) || args[0].equalsIgnoreCase("status")) {
			this.enviarInfo(emisor, "ESTADO TÉRMICO:" + "\n -> Simulación    : "
					+ (termico.isSimulacionHabilitada() ? "HABILITADA" : "DESHABILITADA (Modo Pruebas)")
					+ "\n -> Temp Corporal : " + String.format("%.2f", termico.getTemperaturaCorporal()) + " °C"
					+ "\n -> Hipotermia    : "
					+ (termico.isHipotermia() ? (termico.isHipotermiaSevera() ? "SEVERA (Nivel 3)" : "Nivel 1-2")
							: "NO")
					+ "\n -> Hipertermia   : " + (termico.isHipertermia() ? "ACTIVA" : "NO") + "\n -> Cerca Fuego   : "
					+ (termico.isCercaDeFuenteCalor() ? "SÍ" : "NO") + "\n -> Bajo Techo    : "
					+ (termico.isBajoTechoInterior() ? "SÍ" : "NO"));
			return;
		}

		final String sub = args[0].toLowerCase().trim();

		// --- Control de Activación / Desactivación ---
		if (sub.equals("off") || sub.equals("desactivar") || sub.equals("disable")) {
			termico.setSimulacionHabilitada(false);
			this.enviarInfo(emisor,
					"Simulación térmica DESACTIVADA. Temperatura fijada en 37.0 °C y efectos purgados.");
			return;
		}

		if (sub.equals("on") || sub.equals("activar") || sub.equals("enable")) {
			termico.setSimulacionHabilitada(true);
			this.enviarInfo(emisor, "Simulación térmica HABILITADA.");
			return;
		}

		if (sub.equals("toggle")) {
			final boolean nuevoEstado = !termico.isSimulacionHabilitada();
			termico.setSimulacionHabilitada(nuevoEstado);
			this.enviarInfo(emisor, "Simulación térmica: " + (nuevoEstado ? "HABILITADA" : "DESACTIVADA"));
			return;
		}

		// --- Presets de Prueba Rápida ---
		if (sub.equals("freeze") || sub.equals("congelar") || sub.equals("frio")) {
			if (!termico.isSimulacionHabilitada()) {
				termico.setSimulacionHabilitada(true);
			}
			termico.setTemperaturaCorporal(26.0); // Hipotermia Nivel 3 instantánea
			this.enviarInfo(emisor, "Temperatura forzada a 26.0 °C (Hipotermia Severa / Viñeta de Escarcha Máxima).");
			return;
		}

		if (sub.equals("heat") || sub.equals("quemar") || sub.equals("calor")) {
			if (!termico.isSimulacionHabilitada()) {
				termico.setSimulacionHabilitada(true);
			}
			termico.setTemperaturaCorporal(41.5); // Hipertermia Nivel 3 instantánea
			this.enviarInfo(emisor,
					"Temperatura forzada a 41.5 °C (Hipertermia Severa / Modo Borracho por Golpe de Calor).");
			return;
		}

		if (sub.equals("reset") || sub.equals("normal")) {
			termico.reiniciar();
			if (Globales.JUGADOR != null) {
				Globales.JUGADOR.removerEfecto(principal.entes.efectos.TipoEfectoEstado.HIPOTERMIA);
				Globales.JUGADOR.removerEfecto(principal.entes.efectos.TipoEfectoEstado.HIPERTERMIA);
			}
			this.enviarInfo(emisor, "Temperatura corporal restablecida a 37.0 °C (Confort) y debuffs purgados.");
			return;
		}

		// --- Fijar temperatura numérica exacta ---
		final double temp = this.parsearDouble(args[0], -999);
		if (temp > -100) {
			termico.setTemperaturaCorporal(temp);
			this.enviarInfo(emisor, "Temperatura corporal fijada en: " + temp + " °C");
		} else {
			this.enviarError(emisor,
					"Parámetro inválido. Usa: temp on/off, temp freeze, temp heat, temp reset o temp <grados>");
		}
	}
}