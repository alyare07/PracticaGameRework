package principal.comandos;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Jugador;
import principal.entes.criaturas.grupo.GestorGrupo;
import principal.entes.criaturas.grupo.TipoVinculo;
import principal.entes.criaturas.mascotas.Mascota;
import principal.mapa.Mundo;
import principal.utilidades.Globales;

public class ComandoGrupo extends Comando {

	public ComandoGrupo() {
		super("grupo", "grupo <list | tp / summon | dismissall | reclutar [nombre]>",
				"Gestiona el séquito del jugador, teletransporta seguidores rezagados o recluta compañeros.");
	}

	@Override
	public void ejecutar(final String[] args) {
		this.ejecutar(args, null);
	}

	@Override
	public void ejecutar(final String[] args, final EmisorRespuesta emisor) {
		if (Globales.GESTOR_GRUPO == null || Globales.JUGADOR == null) {
			this.enviarError(emisor, "Gestor de grupo o jugador no disponible.");
			return;
		}

		final GestorGrupo grupo = Globales.GESTOR_GRUPO;

		if (args.length == 0 || args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("lista")) {
			final StringBuilder sb = new StringBuilder("=== SÉQUITO DEL JUGADOR ===\n");
			sb.append("Capacidad: ").append(grupo.getCantidadSeguidoresActivos()).append(" / ").append(grupo.getCapacidadMaximaActual()).append(" activos\n");
			sb.append("Total Roster: ").append(grupo.getRosterGeneral().size()).append(" vinculados\n");

			for (int i = 0; i < grupo.getCantidadSeguidoresActivos(); i++) {
				final Criatura c = grupo.getEscoltaActiva()[i];
				if (c != null) {
					sb.append(" -> Slot ").append(i + 1).append(": ").append(c.getNombre())
					  .append(" [").append(c.getVinculo().getNombreLegible()).append("] HP: ")
					  .append((int) c.getVida()).append("/").append((int) c.getVidaMaxima()).append("\n");
				}
			}
			this.enviarInfo(emisor, sb.toString());
			return;
		}

		final String sub = args[0].toLowerCase().trim();

		if (sub.equals("tp") || sub.equals("summon") || sub.equals("traer")) {
			final Jugador j = Globales.JUGADOR;
			for (int i = 0; i < grupo.getCantidadSeguidoresActivos(); i++) {
				final Criatura c = grupo.getEscoltaActiva()[i];
				if (c != null && !c.estaEliminado()) {
					c.setPosicion(j.getPosicionX() + ((i + 1) * 12), j.getPosicionY());
					c.reiniciarRecorridoAEstrella();
					c.detenerMovimiento();
				}
			}
			this.enviarInfo(emisor, "Todos los seguidores de la escolta han sido teletransportados junto al jugador.");
			return;
		}

		if (sub.equals("dismissall") || sub.equals("liberar") || sub.equals("vaciar")) {
			grupo.vaciar();
			this.enviarInfo(emisor, "Todos los seguidores han sido liberados del grupo.");
			return;
		}

		if (sub.equals("reclutar") || sub.equals("adoptar")) {
			final Mundo mundo = Globales.JUGADOR.getMundo();
			final Mascota companiero = new Mascota(Globales.JUGADOR.getCentroX() + 16, Globales.JUGADOR.getCentroY(),
					(args.length >= 2 ? args[1] : "Compañero"), 100.0, TipoVinculo.MASCOTA);
			mundo.meterEntidad(companiero);
			grupo.agregarSeguidor(companiero);
			this.enviarInfo(emisor, "Nuevo compañero '" + companiero.getNombre() + "' reclutado y añadido al séquito.");
			return;
		}

		this.enviarError(emisor, "Subcomando desconocido. Usa: grupo list, grupo tp, grupo reclutar [nombre] o grupo dismissall");
	}
}