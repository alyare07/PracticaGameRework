package principal.comandos;

import java.awt.Rectangle;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.jugador.Jugador;
import principal.ia.arbol.BlackboardIA;
import principal.mapa.Mundo;
import principal.utilidades.AccionEntidad;
import principal.utilidades.Globales;

public class ComandoIA extends Comando {

	private Criatura criaturaInspeccionada;

	public ComandoIA() {
		super("ia", "ia <inspect | alert <radio> | dijkstra | astar | clear>",
				"Inspecciona la memoria Blackboard de la criatura bajo el ratón o emite pulsos de alerta acústica.");
	}

	@Override
	public void ejecutar(final String[] args) {
		this.ejecutar(args, null);
	}

	@Override
	public void ejecutar(final String[] args, final EmisorRespuesta emisor) {
		if (Globales.JUGADOR == null || Globales.JUGADOR.getMundo() == null) {
			this.enviarError(emisor, "El mundo o el jugador no están disponibles.");
			return;
		}

		final Mundo mundo = Globales.JUGADOR.getMundo();

		if (args.length == 0 || args[0].equalsIgnoreCase("inspect") || args[0].equalsIgnoreCase("ver")) {
			final Criatura target = this.obtenerCriaturaBajoRaton(mundo);
			if (target != null) {
				final BlackboardIA bb = target.getBlackboard();
				this.enviarInfo(emisor, "DIAGNÓSTICO IA: [" + target.getNombre() + "]"
						+ "\n -> Vida          : " + (int) target.getVida() + "/" + (int) target.getVidaMaxima()
						+ "\n -> Posición      : (" + target.getPosicionXInt() + ", " + target.getPosicionYInt() + ")"
						+ "\n -> Estados       : [ " + target.getStringEstados() + "]"
						+ "\n -> En Pánico     : " + (bb != null && bb.isEnPanico())
						+ "\n -> Siguiendo     : " + (bb != null && bb.isSiguiendoLider())
						+ "\n -> Objetivo Act. : " + (bb != null && bb.getObjetivoActual() != null ? bb.getObjetivoActual().getClass().getSimpleName() : "Ninguno")
						+ "\n -> Tiene Ancla   : " + (bb != null && bb.tieneAnclaRetorno())
						+ "\n -> Clearance     : " + target.getClearanceRequerido());
			} else {
				this.enviarError(emisor, "No hay ninguna criatura bajo el cursor del ratón para inspeccionar.");
			}
			return;
		}

		final String sub = args[0].toLowerCase().trim();

		if (sub.equals("alert") || sub.equals("alerta") || sub.equals("ruido")) {
			final double radio = (args.length >= 2) ? this.parsearDouble(args[1], 250.0) : 250.0;
			mundo.emitirPulsoSonido(Globales.JUGADOR.getCentroX(), Globales.JUGADOR.getCentroY(), radio, Globales.JUGADOR);
			this.enviarInfo(emisor, "Pulso acústico de alerta emitido con radio de " + (int) radio + " px.");
			return;
		}

		if (sub.equals("dijkstra") || sub.equals("rebuild")) {
			mundo.notificarModificacionEstructura();
			this.enviarInfo(emisor, "Matrices de Dijkstra y A* recalculadas con éxito.");
			return;
		}

		if (sub.equals("clear") || sub.equals("pacificar")) {
			for (final principal.entes.Ente e : mundo.getEntes()) {
				if (e instanceof Criatura && !(e instanceof Jugador)) {
					final Criatura c = (Criatura) e;
					if (c.getBlackboard() != null) {
						c.getBlackboard().setObjetivoActual(null);
						c.getBlackboard().setEnPanico(false);
					}
					c.setEstadoEstandar();
					c.detenerMovimiento();
				}
			}
			this.enviarInfo(emisor, "Todas las criaturas han sido pacificadas y devueltas a reposo.");
			return;
		}

		this.enviarError(emisor, "Subcomando desconocido. Usa: ia inspect, ia alert <radio>, ia dijkstra o ia clear");
	}

	private Criatura obtenerCriaturaBajoRaton(final Mundo mundo) {
		if (Globales.RATON == null) {
			return null;
		}
		final Rectangle areaMouse = Globales.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara();
		this.criaturaInspeccionada = null;

		mundo.paraCadaCriaturaEn(areaMouse, false, new AccionEntidad<Criatura>() {
			@Override
			public void ejecutar(final Criatura entidad) {
				if (ComandoIA.this.criaturaInspeccionada == null && !(entidad instanceof Jugador) && !entidad.estaEliminado()) {
					ComandoIA.this.criaturaInspeccionada = entidad;
				}
			}
		});

		final Criatura res = this.criaturaInspeccionada;
		this.criaturaInspeccionada = null;
		return res;
	}
}