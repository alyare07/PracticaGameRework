package principal.ia.arbol.acciones;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Criatura.Estado;
import principal.ia.arbol.BlackboardIA;
import principal.ia.arbol.EstadoBT;
import principal.ia.arbol.NodoBT;
import principal.mapa.Mundo;

/**
 * Acción de inspección perimetral con locomoción no destructiva (conserva la
 * animación de caminata fluida) y navegación inteligente alrededor de
 * obstáculos (Zero-GC / O(1)).
 * 
 * @version 1.1 (Vanilla Java 8 - Non-Destructive State & Pathfinding
 *          Integration)
 */
public class AccionInvestigarPunto implements NodoBT {

	private final double tiempoInspeccionSegundos;

	public AccionInvestigarPunto(final double tiempoInspeccionSegundos) {
		this.tiempoInspeccionSegundos = Math.max(1.0, tiempoInspeccionSegundos);
	}

	@Override
	public EstadoBT ejecutar(final Criatura criatura, final BlackboardIA bb, final double dt) {
		final double targetX = bb.tieneSospechaPendiente() ? bb.getXInvestigacion() : bb.getUltimoTargetX();
		final double targetY = bb.tieneSospechaPendiente() ? bb.getYInvestigacion() : bb.getUltimoTargetY();

		final double dx = targetX - criatura.getCentroX();
		final double dy = targetY - criatura.getCentroY();
		final double distSq = (dx * dx) + (dy * dy);

		// 1. Fase de Desplazamiento hacia el punto de interés
		if (distSq > (16.0 * 16.0)) {
			if (!criatura.tieneEstado(Estado.INVESTIGANDO)) {
				criatura.meterEstado(Estado.INVESTIGANDO);
			}

			final Mundo mundo = criatura.getMundo();
			final boolean pasoLimpio = (mundo != null)
					&& mundo.hayLineaDePasoLimpia(criatura.getPieX(), criatura.getPieY(), targetX, targetY,
							criatura.getAnchoColisionPies(), criatura.getAltoColisionPies());

			if (pasoLimpio) {
				criatura.reiniciarRecorridoAEstrella();
				criatura.moverHaciaPuntoContinuo(targetX, targetY, 4.0);
			} else {
				if ((criatura.getNodoADestino() == null) && criatura.getRecorridoA().isEmpty()) {
					if (bb.puedeRecalcularRuta()) {
						criatura.calcularRutaAEstrella((int) targetX, (int) targetY);
						if ((criatura.getNodoADestino() == null) && criatura.getRecorridoA().isEmpty()) {
							bb.setCooldownRecalculoRuta(0.5);
						}
					}
				}
				criatura.moverANodoADestino();
			}

			return EstadoBT.EN_PROCESO;
		}

		// 2. Llegada al punto: se detiene en seco para iniciar inspección in situ
		criatura.detenerMovimiento();
		if (!criatura.tieneEstado(Estado.INVESTIGANDO)) {
			criatura.meterEstado(Estado.INVESTIGANDO);
		}

		// 3. Fase de Inspección In Situ: Rota la mirada a intervalos regulares
		bb.aumentarTiempoInvestigando(dt);

		final int pasoRotacion = (int) (bb.getTiempoInvestigando() / 0.85) % 4;
		criatura.setDireccion(Criatura.DIRECCIONES_ARRAY[pasoRotacion]);

		if (bb.getTiempoInvestigando() >= this.tiempoInspeccionSegundos) {
			bb.limpiarSospechaRuido();
			bb.olvidarPosicionObjetivo();
			criatura.removerEstado(Estado.INVESTIGANDO);
			criatura.setEstadoEstandar();
			return EstadoBT.EXITO;
		}

		return EstadoBT.EN_PROCESO;
	}
}