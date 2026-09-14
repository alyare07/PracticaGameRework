package principal.ia.arbol.acciones;

import principal.entes.criaturas.Criatura;
import principal.ia.arbol.BlackboardIA;
import principal.ia.arbol.EstadoBT;
import principal.ia.arbol.NodoBT;
import principal.mapa.Mundo;

/**
 * Acción de retorno a la base / spawn original para NPCs, comerciantes y
 * guardias (Zero-GC).
 * 
 * @version 1.0 (Vanilla Java 8 - Home Anchor Leash Action)
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

		final double spawnX = criatura.getPosicionXInicial();
		final double spawnY = criatura.getPosicionYInicial();

		final double dx = spawnX - criatura.getPieX();
		final double dy = spawnY - criatura.getPieY();
		final double distSq = (dx * dx) + (dy * dy);

		// Si ya está en su puesto
		if (distSq <= this.distanciaToleranciaSq) {
			criatura.detenerMovimiento();
			if (!criatura.estaEstadoEstandar()) {
				criatura.setEstadoEstandar();

			}
			return EstadoBT.EXITO;
		}

		// 1. Si hay camino libre directo hacia su puesto
		final boolean lineaLimpia = mundo.hayLineaDeTiroLimpia(criatura.getPieX(), criatura.getPieY(), spawnX, spawnY);

		if (lineaLimpia) {
			criatura.reiniciarRecorridoAEstrella();
			criatura.moverHaciaPuntoContinuo(spawnX, spawnY, this.distanciaTolerancia);
			return EstadoBT.EN_PROCESO;
		}

		// 2. Si hay casas o árboles en medio, calcula camino A* de vuelta a casa
		if ((criatura.getNodoADestino() == null) && criatura.getRecorridoA().isEmpty()) {
			criatura.calcularRutaAEstrella((int) spawnX, (int) spawnY);
		}

		criatura.moverANodoADestino();
		return EstadoBT.EN_PROCESO;
	}
}