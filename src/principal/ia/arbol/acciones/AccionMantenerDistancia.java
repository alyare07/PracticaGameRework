package principal.ia.arbol.acciones;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.ia.arbol.BlackboardIA;
import principal.ia.arbol.EstadoBT;
import principal.ia.arbol.NodoBT;

/**
 * Acción táctica de KITING ofensivo para tiradores. Retrocede físicamente
 * manteniendo la orientación visual fijada hacia el agresor sin que la marcha
 * sobreescriba la dirección en cada fotograma (Zero-GC / O(1)).
 * 
 * @version 2.1 (Vanilla Java 8 - Steady-Aim Retrograde Kiting)
 */
public class AccionMantenerDistancia implements NodoBT {

	private final double distanciaMinimaSeguridadSq;
	private final double distanciaDeseada;
	private final NodoBT accionDuranteRetroceso;

	public AccionMantenerDistancia(final double distanciaMinima, final double distanciaDeseada,
			final NodoBT accionDuranteRetroceso) {
		this.distanciaMinimaSeguridadSq = distanciaMinima * distanciaMinima;
		this.distanciaDeseada = Math.max(distanciaMinima + 20.0, distanciaDeseada);
		this.accionDuranteRetroceso = accionDuranteRetroceso;
	}

	public AccionMantenerDistancia(final double distanciaMinima, final double distanciaDeseada) {
		this(distanciaMinima, distanciaDeseada, null);
	}

	@Override
	public EstadoBT ejecutar(final Criatura criatura, final BlackboardIA bb, final double dt) {
		final Ente objetivo = bb.getObjetivoActual();
		if ((objetivo == null) || objetivo.estaEliminado()) {
			return EstadoBT.FRACASO;
		}

		final double dx = criatura.getCentroX() - objetivo.getCentroX();
		final double dy = criatura.getCentroY() - objetivo.getCentroY();
		final double distSq = (dx * dx) + (dy * dy);

		if (distSq >= (this.distanciaDeseada * this.distanciaDeseada)) {
			if (!criatura.estaEstadoEstandar()) {
				criatura.setEstadoEstandar();
			}
			if (objetivo instanceof Criatura) {
				criatura.setDireccionMirandoCriatura((Criatura) objetivo);
			}
			return EstadoBT.EXITO;
		}

		final double dist = Math.sqrt(distSq);
		final double dirAtrasX = (dist > 0.001) ? (dx / dist) : 1.0;
		final double dirAtrasY = (dist > 0.001) ? (dy / dist) : 0.0;

		final double puntoRetrocesoX = criatura.getCentroX() + (dirAtrasX * 40.0);
		final double puntoRetrocesoY = criatura.getCentroY() + (dirAtrasY * 40.0);

		// 1. Mantiene la mirada fija hacia el agresor antes y después de desplazarse
		if (objetivo instanceof Criatura) {
			criatura.setDireccionMirandoCriatura((Criatura) objetivo);
		}

		// 2. Desplazamiento retrógrado SIN actualizar la dirección hacia el vector de
		// marcha (actualizarDireccion = false)
		final boolean seMovio = criatura.moverHaciaPuntoContinuo(puntoRetrocesoX, puntoRetrocesoY, 4.0, false);

		// 3. FUEGO CONCURRENTE: Dispara mientras retrocede
		if (this.accionDuranteRetroceso != null) {
			this.accionDuranteRetroceso.ejecutar(criatura, bb, dt);
		}

		if (!seMovio) {
			return EstadoBT.FRACASO;
		}

		return EstadoBT.EN_PROCESO;
	}
}