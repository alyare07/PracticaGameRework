package principal.comandos;

import principal.entes.criaturas.enemigos.bandido.BandidoGarrote;
import principal.iluminacion.TipoLuz;
import principal.mapa.Mundo;
import principal.utilidades.Globales;

public class ComandoStress extends Comando {

	public ComandoStress() {
		super("stress", "stress <enemies <cant> | particles <cant> | lights <cant>>",
				"Herramienta de pruebas de carga: genera oleadas de enemigos masivas, lluvia de partículas o luces simultáneas.");
	}

	@Override
	public void ejecutar(final String[] args) {
		this.ejecutar(args, null);
	}

	@Override
	public void ejecutar(final String[] args, final EmisorRespuesta emisor) {
		if (Globales.JUGADOR == null || Globales.JUGADOR.getMundo() == null) {
			this.enviarError(emisor, "El juego no está en un estado válido para pruebas de estrés.");
			return;
		}

		final Mundo mundo = Globales.JUGADOR.getMundo();
		final double jx = Globales.JUGADOR.getCentroX();
		final double jy = Globales.JUGADOR.getCentroY();

		if (args.length == 0) {
			this.enviarInfo(emisor, "Uso: stress enemies <cantidad> | stress particles <cantidad> | stress lights <cantidad>");
			return;
		}

		final String sub = args[0].toLowerCase().trim();
		final int cantidad = (args.length >= 2) ? Math.max(1, this.parsearEntero(args[1], 30)) : 30;

		if (sub.equals("enemies") || sub.equals("enemigos") || sub.equals("mobs")) {
			for (int i = 0; i < cantidad; i++) {
				final double angulo = Math.random() * Math.PI * 2.0;
				final double dist = 40.0 + (Math.random() * 200.0);
				final double x = jx + (Math.cos(angulo) * dist);
				final double y = jy + (Math.sin(angulo) * dist);
				mundo.meterEntidad(new BandidoGarrote(x, y, 40, 40, mundo));
			}
			this.enviarInfo(emisor, "Estrés: " + cantidad + " enemigos generados en anillo alrededor del jugador.");
			return;
		}

		if (sub.equals("particles") || sub.equals("particulas")) {
			if (Globales.GESTOR_PARTICULAS != null) {
				Globales.GESTOR_PARTICULAS.emitirExplosion(jx, jy, cantidad);
				this.enviarInfo(emisor, "Estrés: Explosión de " + cantidad + " partículas emitida.");
			}
			return;
		}

		if (sub.equals("lights") || sub.equals("luces")) {
			if (Globales.GESTOR_LUZ != null) {
				for (int i = 0; i < cantidad; i++) {
					final double angulo = Math.random() * Math.PI * 2.0;
					final double dist = 30.0 + (Math.random() * 180.0);
					Globales.GESTOR_LUZ.agregarLuzEstatica(jx + (Math.cos(angulo) * dist), jy + (Math.sin(angulo) * dist), TipoLuz.ANTORCHA, 75.0);
				}
				this.enviarInfo(emisor, "Estrés: " + cantidad + " fuentes de luz añadidas.");
			}
			return;
		}

		this.enviarError(emisor, "Opción no reconocida. Usa: stress enemies, stress particles o stress lights.");
	}
}