package principal.ia.arbol.acciones;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.ia.arbol.BlackboardIA;
import principal.ia.arbol.EstadoBT;
import principal.ia.arbol.NodoBT;
import principal.mapa.Mundo;

/**
 * Acción de flanqueo táctico para tiradores (100% Stateless). Se desplaza
 * perpendicularmente hacia la línea de visión sin interferir entre múltiples
 * tiradores concurrentes (Zero-GC / O(1)).
 * 
 * @version 1.1 (Vanilla Java 8 - Stateless Blackboard Flanking)
 */
public class AccionFlanquearTiro implements NodoBT {

	private final double distanciaPasoLateral;

	public AccionFlanquearTiro(final double distanciaPasoLateral) {
		this.distanciaPasoLateral = Math.max(24.0, distanciaPasoLateral);
	}

	public AccionFlanquearTiro() {
		this(48.0);
	}

	@Override
	public EstadoBT ejecutar(final Criatura criatura, final BlackboardIA bb, final double dt) {
		final Mundo mundo = criatura.getMundo();
		final Ente objetivo = bb.getObjetivoActual();

		if ((mundo == null) || (objetivo == null) || objetivo.estaEliminado()) {
			return EstadoBT.FRACASO;
		}

		final double miX = criatura.getCentroX();
		final double miY = criatura.getCentroY();
		final double targetX = objetivo.getCentroX();
		final double targetY = objetivo.getCentroY();

		if (mundo.hayLineaDeTiroLimpia(miX, miY, targetX, targetY)) {
			return EstadoBT.EXITO;
		}

		final double dx = targetX - miX;
		final double dy = targetY - miY;
		final double dist = Math.sqrt((dx * dx) + (dy * dy));

		if (dist < 0.001) {
			return EstadoBT.FRACASO;
		}

		final int dirFlanqueo = bb.getDireccionFlanqueo();
		final double perpX = (-dy / dist) * dirFlanqueo;
		final double perpY = (dx / dist) * dirFlanqueo;

		final double strafeX = miX + (perpX * this.distanciaPasoLateral);
		final double strafeY = miY + (perpY * this.distanciaPasoLateral);

		final boolean seMovio = criatura.moverHaciaPuntoContinuo(strafeX, strafeY, 4.0);

		if (!seMovio) {
			bb.invertirDireccionFlanqueo();
			return EstadoBT.FRACASO;
		}

		return EstadoBT.EN_PROCESO;
	}
}