package principal.ia.arbol.acciones;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.mascotas.Mascota;
import principal.ia.arbol.BlackboardIA;
import principal.ia.arbol.EstadoBT;
import principal.ia.arbol.NodoBT;
import principal.mapa.Mundo;

/**
 * Acción de retorno a la base / spawn original o al anclaje de espera dinámico
 * tras haber huido de un peligro (Zero-GC / O(1)).
 * 
 * @version 2.1 (Vanilla Java 8 - Companion Anchor Verification)
 */
public class AccionRegresarASpawn implements NodoBT {

	private final double distanciaToleranciaSq;
	private final double distanciaTolerancia;

	public AccionRegresarASpawn(final double distanciaTolerancia) {
		this.distanciaTolerancia = Math.max(12.0, distanciaTolerancia);
		this.distanciaToleranciaSq = this.distanciaTolerancia * this.distanciaTolerancia;
	}

	public AccionRegresarASpawn() {
		this(20.0);
	}

	@Override
	public EstadoBT ejecutar(final Criatura criatura, final BlackboardIA bb, final double dt) {
		final Mundo mundo = criatura.getMundo();
		if (mundo == null) {
			return EstadoBT.FRACASO;
		}

		// Para mascotas, si no hay un ancla de retorno registrada, no debe regresar al
		// spawn del mapa
		if ((criatura instanceof Mascota) && !bb.tieneAnclaRetorno()) {
			return EstadoBT.FRACASO;
		}

		// Si tiene ancla dinámica de retorno (punto de espera donde fue atacada), la
		// usa; sino usa el spawn inicial
		final double targetX = bb.tieneAnclaRetorno() ? bb.getXAnclaRetorno() : criatura.getPosicionXInicial();
		final double targetY = bb.tieneAnclaRetorno() ? bb.getYAnclaRetorno() : criatura.getPosicionYInicial();

		final double dx = targetX - criatura.getPieX();
		final double dy = targetY - criatura.getPieY();
		final double distSq = (dx * dx) + (dy * dy);

		// Si ya está en su puesto
		if (distSq <= this.distanciaToleranciaSq) {
			criatura.detenerMovimiento();
			if (bb.tieneAnclaRetorno()) {
				bb.limpiarAnclaRetorno();
			}
			if (!criatura.estaEstadoEstandar()) {
				criatura.setEstadoEstandar();
			}
			return EstadoBT.EXITO;
		}

		// 1. Si el paso físico está despejado para el cuerpo entero
		final boolean pasoLimpio = mundo.hayLineaDePasoLimpia(criatura.getPieX(), criatura.getPieY(), targetX, targetY,
				criatura.getAnchoColisionPies(), criatura.getAltoColisionPies());

		if (pasoLimpio) {
			criatura.reiniciarRecorridoAEstrella();
			criatura.moverHaciaPuntoContinuo(targetX, targetY, this.distanciaTolerancia);
			return EstadoBT.EN_PROCESO;
		}

		// 2. Si hay obstáculos o árboles en medio, calcula camino A* de vuelta al
		// puesto
		if ((criatura.getNodoADestino() == null) && criatura.getRecorridoA().isEmpty()) {
			if (bb.puedeRecalcularRuta()) {
				criatura.calcularRutaAEstrella((int) targetX, (int) targetY);
				if ((criatura.getNodoADestino() == null) && criatura.getRecorridoA().isEmpty()) {
					bb.setCooldownRecalculoRuta(0.5);
				}
			}
		}

		criatura.moverANodoADestino();
		return EstadoBT.EN_PROCESO;
	}
}