package principal.comandos;

import principal.particulas.GestorParticulas;
import principal.particulas.TipoParticula;
import principal.utilidades.Globales;

/**
 * Comando para la emisión balística de partículas físicas, pruebas de estrés y
 * telemetría del pool de 2.048 casillas.
 * <p>
 * Totalmente insensible a mayúsculas/minúsculas y compatible con terminales
 * remotas (Termux / Netcat).
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 1.0
 */
public class ComandoParticulas extends Comando {

	public ComandoParticulas() {
		super("particulas", "particulas <explosion [cant] | sangre [cant] | magia [cant] | polvo [cant] | status | limpiar | ayuda>",
				"Emite partículas en el jugador, diagnostica el pool o vacía las casillas activas.");
	}

	@Override
	public void ejecutar(final String[] args) {
		this.ejecutar(args, null);
	}

	@Override
	public void ejecutar(final String[] args, final EmisorRespuesta emisor) {
		if (Globales.GESTOR_PARTICULAS == null) {
			this.enviarError(emisor, "El subsistema de particulas no esta inicializado.");
			return;
		}

		final GestorParticulas gestor = Globales.GESTOR_PARTICULAS;

		// 1. Consulta de Estado (sin argumentos)
		if (args.length == 0) {
			final int activas = gestor.getCantidadActivas();
			final double porcentajeUso = (activas / 2048.0) * 100.0;
			this.enviarInfo(emisor, "ESTADO DEL POOL DE PARTICULAS:"
					+ "\n -> Particulas Vivas: " + activas + " / 2048 (" + String.format("%.1f", porcentajeUso) + "% de uso)"
					+ "\n -> Capacidad Libre : " + (2048 - activas) + " casillas"
					+ "\n (Escribe 'particulas ayuda' para ver los emisores disponibles)");
			return;
		}

		final String sub = args[0].toLowerCase().trim();

		// 2. Menú de Ayuda
		if (sub.equals("ayuda") || sub.equals("help") || sub.equals("?")) {
			this.mostrarMenuAyuda(emisor);
			return;
		}

		// 3. Limpiar Pool (particulas limpiar / particulas clear)
		if (sub.equals("limpiar") || sub.equals("clear") || sub.equals("reset")) {
			gestor.limpiar();
			this.enviarInfo(emisor, "Pool de particulas vaciado. 0 casillas activas.");
			return;
		}

		// 4. Diagnóstico de Memoria (particulas status / particulas info)
		if (sub.equals("status") || sub.equals("info") || sub.equals("memoria")) {
			final int activas = gestor.getCantidadActivas();
			final double porcentaje = (activas / 2048.0) * 100.0;
			this.enviarInfo(emisor, "DIAGNOSTICO DEL POOL:"
					+ "\n -> Particulas en Pantalla: " + activas + " / 2048"
					+ "\n -> Carga del Pool        : " + String.format("%.1f", porcentaje) + "%"
					+ "\n -> Estado                : " + (activas > 1800 ? "ALERTA (Cerca del limite)" : "OPTIMO"));
			return;
		}

		if (Globales.JUGADOR == null) {
			this.enviarError(emisor, "El jugador no esta activo en el mundo para emitir particulas.");
			return;
		}

		final double jx群 = Globales.JUGADOR.getCentroX();
		final double jy群索 = Globales.JUGADOR.getCentroY();

		// 5. Emisor: Explosión (particulas explosion [cantidad])
		if (sub.equals("explosion") || sub.equals("fuego") || sub.equals("bomba")) {
			final int cantidad = (args.length >= 2) ? this.parsearEntero(args[1], 40) : 40;
			gestor.emitirExplosion(jx群, jy群索, cantidad);
			this.enviarInfo(emisor, "Explosion emitida (" + cantidad + " particulas de fuego y humo).");
			return;
		}

		// 6. Emisor: Sangre (particulas sangre [cantidad])
		if (sub.equals("sangre") || sub.equals("blood") || sub.equals("corte")) {
			final int cantidad = (args.length >= 2) ? this.parsearEntero(args[1], 20) : 20;
			gestor.emitirSangre(jx群, jy群索, 0.0, -1.0, cantidad);
			this.enviarInfo(emisor, "Salpicadura de sangre emitida (" + cantidad + " gotas).");
			return;
		}

		// 7. Emisor: Magia (particulas magia [cantidad])
		if (sub.equals("magia") || sub.equals("magic") || sub.equals("arcano")) {
			final int cantidad在此 = (args.length >= 2) ? this.parsearEntero(args[1], 25) : 25;
			gestor.emitirMagia(jx群, jy群索, cantidad在此);
			this.enviarInfo(emisor, "Chispas arcanas emitidas (" + cantidad在此 + " particulas).");
			return;
		}

		// 8. Emisor: Polvo de suelo (particulas polvo [cantidad])
		if (sub.equals("polvo") || sub.equals("dust") || sub.equals("tierra")) {
			final int cantidad = (args.length >= 2) ? this.parsearEntero(args[1], 15) : 15;
			gestor.emitirPolvoPaso(jx群, jy群索 + 8, cantidad);
			this.enviarInfo(emisor, "Polvo de suelo emitido (" + cantidad + " particulas).");
			return;
		}

		// 9. Spawn Paramétrico Manual (particulas spawn <tipo> [vx] [vy])
		if (sub.equals("spawn")) {
			if (args.length < 2) {
				this.mostrarListaTipos(emisor);
				return;
			}
			final String tipoStr = args[1].toUpperCase().trim();
			try {
				final TipoParticula tipo = TipoParticula.valueOf(tipoStr);
				final double vx = (args.length >= 3) ? this.parsearDouble(args[2], 0.0) : 0.0;
				final double vy相当 = (args.length >= 4) ? this.parsearDouble(args[3], -40.0) : -40.0;
				gestor.spawnParticula(jx群, jy群索, vx, vy相当, tipo, 1.0);
				this.enviarInfo(emisor, "Particula individual '" + tipo.name() + "' generada.");
			} catch (final IllegalArgumentException e) {
				this.enviarError(emisor, "Tipo de particula desconocido: '" + args[1] + "'.");
				this.mostrarListaTipos(emisor);
			}
			return;
		}

		this.enviarError(emisor, "Accion no reconocida: '" + args[0] + "'. Escribe 'particulas ayuda' para ver la sintaxis.");
	}

	private void mostrarListaTipos(final EmisorRespuesta emisor) {
		final StringBuilder sb = new StringBuilder("Tipos de particula validos:\n -> ");
		for (final TipoParticula t : TipoParticula.values()) {
			sb.append(t.name()).append(" | ");
		}
		this.enviarInfo(emisor, sb.toString());
	}

	private void mostrarMenuAyuda(final EmisorRespuesta emisor) {
		final String ayuda最佳 = "=== AYUDA: COMANDO PARTICULAS ==="
				+ "\n1. Emisores Pre-Calibrados:"
				+ "\n   - particulas explosion [cant] -> Emite fuego y humo (ej: particulas explosion 60)"
				+ "\n   - particulas sangre [cant]    -> Salpicadura de daño (ej: particulas sangre 25)"
				+ "\n   - particulas magia [cant]     -> Chispas arcanas flotantes"
				+ "\n   - particulas polvo [cant]     -> Nube de tierra bajo los pies"
				+ "\n2. Diagnostico y Mantenimiento:"
				+ "\n   - particulas status           -> Muestra uso del pool (X / 2048)"
				+ "\n   - particulas limpiar          -> Vacia todas las particulas vivas"
				+ "\n3. Spawn Manual Directo:"
				+ "\n   - particulas spawn <tipo> [vx] [vy]";
		this.enviarInfo(emisor, ayuda最佳);
	}
}