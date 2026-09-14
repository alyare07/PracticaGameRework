package principal.ia.arbol.acciones;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Jugador;
import principal.ia.arbol.BlackboardIA;
import principal.ia.arbol.EstadoBT;
import principal.ia.arbol.NodoBT;
import principal.ia.dijkstra.DijkstraRework;
import principal.ia.dijkstra.NodoD;
import principal.mapa.Mundo;

/**
 * Acción de aproximación híbrida de alto rendimiento:
 * <ul>
 * <li>Línea de paso libre (ancho de hombros y pies): Avance cinemático
 * directo.</li>
 * <li>Persecución masiva del Jugador (Clearance 1): Flowfield Dijkstra en
 * O(1).</li>
 * <li>Jefes (Clearance >= 2) o blancos no-jugador: A* Clearance-Aware.</li>
 * </ul>
 * 
 * @version 5.1 (Vanilla Java 8 - Footprint Raycast & Waypoint Arrival
 *          Decoupling)
 */
public class AccionAproximarse implements NodoBT {

	private static final double RADIO_LLEGADA_WAYPOINT = 4.0;
	private final double distanciaFrenado;

	public AccionAproximarse(final double distanciaFrenado) {
		this.distanciaFrenado = Math.max(2.0, distanciaFrenado);
	}

	@Override
	public EstadoBT ejecutar(final Criatura criatura, final BlackboardIA bb, final double dt) {
		final Mundo mundo = criatura.getMundo();
		if (mundo == null) {
			return EstadoBT.FRACASO;
		}

		double targetPieX;
		double targetPieY;

		final Ente objetivo = bb.getObjetivoActual();
		if ((objetivo != null) && !objetivo.estaEliminado()) {
			targetPieX = (objetivo instanceof Criatura) ? ((Criatura) objetivo).getPieX() : objetivo.getCentroX();
			targetPieY = (objetivo instanceof Criatura) ? ((Criatura) objetivo).getPieY()
					: (objetivo.getPosicionYBase() - 4.0);
		} else if (bb.tienePosicionObjetivoRecordada()) {
			targetPieX = bb.getUltimoTargetX();
			targetPieY = bb.getUltimoTargetY();
		} else {
			return EstadoBT.FRACASO;
		}

		final double miPieX = criatura.getPieX();
		final double miPieY = criatura.getPieY();

		final double dx = targetPieX - miPieX;
		final double dy = targetPieY - miPieY;
		final double distSq = (dx * dx) + (dy * dy);

		// Si ya está dentro del rango de combate frente al objetivo final:
		if (distSq <= (this.distanciaFrenado * this.distanciaFrenado)) {
			criatura.detenerMovimiento();
			if (objetivo instanceof Criatura) {
				criatura.setDireccionMirandoCriatura((Criatura) objetivo);
			}
			return EstadoBT.EXITO;
		}

		// 1. Línea de PASO limpia (considera si el cuerpo entero cabe físicamente en el
		// trayecto directo)
		final boolean pasoLimpio = mundo.hayLineaDePasoLimpia(miPieX, miPieY, targetPieX, targetPieY,
				criatura.getAnchoColisionPies(), criatura.getAltoColisionPies());

		if (pasoLimpio) {
			criatura.reiniciarRecorridoAEstrella();
			criatura.moverHaciaPuntoContinuo(targetPieX, targetPieY, this.distanciaFrenado);
			return EstadoBT.EN_PROCESO;
		}

		// 2. Si persigue al JUGADOR y es de tamaño estándar (Clearance 1): Lee
		// Flowfield Dijkstra en O(1)
		final DijkstraRework dijkstra = mundo.getDijkstra();
		if ((objetivo instanceof Jugador) && (criatura.getClearanceRequerido() == 1) && (dijkstra != null)) {
			final NodoD nodo = dijkstra.getNodoCercano((int) miPieX, (int) miPieY);

			if (nodo != null) {
				final int readBuf = dijkstra.getBufferLecturaIndex();
				final NodoD siguiente = nodo.getNodoProcedente(readBuf);

				if (siguiente != null) {
					final double waypointX = siguiente.getXMundo() + (siguiente.getAncho() / 2.0);
					final double waypointY = siguiente.getYMundo() + (siguiente.getAlto() / 2.0);

					// A un waypoint intermedio se avanza con RADIO_LLEGADA_WAYPOINT (4 px), no con
					// la distancia de ataque
					criatura.moverHaciaPuntoContinuo(waypointX, waypointY, RADIO_LLEGADA_WAYPOINT);
					return EstadoBT.EN_PROCESO;
				}
			}
		}

		// 3. Jefes grandes (Clearance >= 2) o fallback cuando Dijkstra no llega: A*
		// Clearance-Aware
		if ((criatura.getNodoADestino() == null) && criatura.getRecorridoA().isEmpty()) {
			if (bb.puedeRecalcularRuta()) {
				criatura.calcularRutaAEstrella((int) targetPieX, (int) targetPieY);

				// Solo penaliza con cooldown si realmente falló (ambos vacíos tras calcular)
				if ((criatura.getNodoADestino() == null) && criatura.getRecorridoA().isEmpty()) {
					bb.setCooldownRecalculoRuta(0.5);
				} else {
					bb.setCooldownRecalculoRuta(0.2);
				}
			}
		}

		criatura.moverANodoADestino();
		return EstadoBT.EN_PROCESO;
	}
}