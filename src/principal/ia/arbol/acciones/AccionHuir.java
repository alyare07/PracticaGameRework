package principal.ia.arbol.acciones;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Criatura.Estado;
import principal.ia.arbol.BlackboardIA;
import principal.ia.arbol.EstadoBT;
import principal.ia.arbol.NodoBT;

/**
 * Acción de huida táctica sin bucle de re-enganche (Zero-GC).
 * 
 * @version 3.0 (Vanilla Java 8 - Sustained Flee Integration)
 */
public class AccionHuir implements NodoBT {

	private final double distanciaSeguraSq;

	public AccionHuir(final double distanciaSegura) {
		this.distanciaSeguraSq = distanciaSegura * distanciaSegura;
	}

	public AccionHuir() {
		this(220.0);
	}

	@Override
	public EstadoBT ejecutar(final Criatura criatura, final BlackboardIA bb, final double dt) {
		final Ente amenaza = bb.getObjetivoActual();
		final double amenazaX = (amenaza != null) ? amenaza.getCentroX() : bb.getUltimoTargetX();
		final double amenazaY = (amenaza != null) ? amenaza.getCentroY() : bb.getUltimoTargetY();

		final double dx = criatura.getCentroX() - amenazaX;
		final double dy = criatura.getCentroY() - amenazaY;
		final double distSq = (dx * dx) + (dy * dy);

		// Si ya alcanzó la distancia segura: frenado completo y permite que pase a
		// merodear
		if (distSq >= this.distanciaSeguraSq) {
			bb.setEnPanico(false);
			criatura.removerEstado(Estado.HUYENDO);
			if (!criatura.estaEstadoEstandar()) {
				criatura.setEstadoEstandar();
			}
			criatura.detenerMovimiento();
			return EstadoBT.FRACASO;
		}

		final double dist = Math.sqrt(distSq);
		final double dirEscapeX = (dist > 0.001) ? (dx / dist) : 1.0;
		final double dirEscapeY = (dist > 0.001) ? (dy / dist) : 0.0;

		final double destinoX = criatura.getCentroX() + (dirEscapeX * 64.0);
		final double destinoY = criatura.getCentroY() + (dirEscapeY * 64.0);

		criatura.meterEstado(Estado.HUYENDO);
		criatura.moverHaciaPuntoContinuo(destinoX, destinoY, 4.0);

		return EstadoBT.EN_PROCESO;
	}
}