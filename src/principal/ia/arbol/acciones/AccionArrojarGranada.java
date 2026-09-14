package principal.ia.arbol.acciones;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.enemigos.bandido.BandidoGranadero;
import principal.ia.arbol.BlackboardIA;
import principal.ia.arbol.EstadoBT;
import principal.ia.arbol.NodoBT;

/**
 * Acción de lanzamiento balístico de granadas y explosivos.
 * 
 * @version 1.0 (Vanilla Java 8 - Grenade Lob Action Node)
 */
public class AccionArrojarGranada implements NodoBT {

	private final double cadenciaSegundos;

	public AccionArrojarGranada(final double cadenciaSegundos) {
		this.cadenciaSegundos = Math.max(0.5, cadenciaSegundos);
	}

	@Override
	public EstadoBT ejecutar(final Criatura criatura, final BlackboardIA bb, final double dt) {
		final Ente objetivo = bb.getObjetivoActual();
		if ((objetivo == null) || objetivo.estaEliminado()) {
			return EstadoBT.FRACASO;
		}

		if (criatura instanceof BandidoGranadero) {
			final BandidoGranadero bg = (BandidoGranadero) criatura;

			final int targetX = objetivo.getCentroX();
			final int targetY = objetivo.getCentroY();

			bg.setDireccionMirandoCriatura((Criatura) objetivo);
			bg.arrojarGranadaHacia(targetX, targetY);

			bb.iniciarCooldown(BlackboardIA.CD_DISPARO, this.cadenciaSegundos);
			return EstadoBT.EXITO;
		}

		return EstadoBT.FRACASO;
	}
}