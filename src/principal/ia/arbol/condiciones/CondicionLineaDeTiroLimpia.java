package principal.ia.arbol.condiciones;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.ia.arbol.BlackboardIA;
import principal.ia.arbol.EstadoBT;
import principal.ia.arbol.NodoBT;
import principal.mapa.Mundo;

/**
 * Condición que comprueba mediante Raycasting rápido si existe una trayectoria
 * balística libre de obstáculos entre la Criatura y su objetivo actual.
 * 
 * @version 1.0 (Vanilla Java 8 - Collision Raycast Condition)
 */
public class CondicionLineaDeTiroLimpia implements NodoBT {

	public CondicionLineaDeTiroLimpia() {
	}

	@Override
	public EstadoBT ejecutar(final Criatura criatura, final BlackboardIA bb, final double dt) {
		final Mundo mundo = criatura.getMundo();
		final Ente objetivo = bb.getObjetivoActual();

		if ((mundo == null) || (objetivo == null) || objetivo.estaEliminado()) {
			return EstadoBT.FRACASO;
		}

		final double x0 = criatura.getCentroX();
		final double y0 = criatura.getCentroY();
		final double x1 = objetivo.getCentroX();
		final double y1 = objetivo.getCentroY();

		final boolean tiroDespejado = mundo.hayLineaDeTiroLimpia(x0, y0, x1, y1);

		return tiroDespejado ? EstadoBT.EXITO : EstadoBT.FRACASO;
	}
}