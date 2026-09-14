package principal.ia.arbol.acciones;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Jugador;
import principal.ia.arbol.BlackboardIA;
import principal.ia.arbol.EstadoBT;
import principal.ia.arbol.NodoBT;
import principal.ia.dijkstra.DijkstraRework;
import principal.ia.dijkstra.NodoD;
import principal.mapa.Mundo;
import principal.utilidades.Globales;

/**
 * Acción táctica para acompañantes con banda muerta anti-vibraciones,
 * seguimiento directo mediante línea de paso volumétrica y navegación híbrida
 * (Zero-GC).
 * 
 * @version 2.1 (Vanilla Java 8 - Line-of-Walk & Hybrid Flowfield Navigation)
 */
public class AccionSeguirLider implements NodoBT {

	private static final double DIST_STOP = 26.0;
	private static final double DIST_STOP_SQ = DIST_STOP * DIST_STOP;
	private static final double DIST_START = 42.0;
	private static final double DIST_START_SQ = DIST_START * DIST_START;
	private static final double DIST_TELEPORT_RESCATE_SQ = 360.0 * 360.0;
	private static final double RADIO_LLEGADA_WAYPOINT = 4.0;

	public AccionSeguirLider() {
	}

	@Override
	public EstadoBT ejecutar(final Criatura criatura, final BlackboardIA bb, final double dt) {
		if (!bb.isSiguiendoLider()) {
			return EstadoBT.FRACASO;
		}

		final Jugador lider = Globales.JUGADOR;
		if ((lider == null) || lider.estaEliminado()) {
			return EstadoBT.FRACASO;
		}

		final double targetX = lider.getPieX();
		final double targetY = lider.getPieY();
		final double miX = criatura.getPieX();
		final double miY = criatura.getPieY();

		final double dx = targetX - miX;
		final double dy = targetY - miY;
		final double distSq = (dx * dx) + (dy * dy);

		// 1. Rescate por teletransporte si quedó excesivamente rezagada
		if (distSq > DIST_TELEPORT_RESCATE_SQ) {
			criatura.setPosicion(lider.getPosicionX(), lider.getPosicionY());
			criatura.reiniciarRecorridoAEstrella();
			criatura.detenerMovimiento();
			criatura.setEstadoEstandar();
			return EstadoBT.EXITO;
		}

		// 2. Zona de confort: Si está a distancia de parada, frena y mira al líder
		if (distSq <= DIST_STOP_SQ) {
			criatura.detenerMovimiento();
			criatura.reiniciarRecorridoAEstrella();
			if (!criatura.estaEstadoEstandar()) {
				criatura.setEstadoEstandar();
			}
			criatura.setDireccionMirandoCriatura(lider);
			return EstadoBT.EXITO;
		}

		// 3. Banda muerta (Hysteresis): Si estaba parada, solo arranca si el líder se
		// alejó > DIST_START
		if (!criatura.estaEnMovimientoFisico() && (distSq < DIST_START_SQ)) {
			criatura.setDireccionMirandoCriatura(lider);
			return EstadoBT.EXITO;
		}

		final Mundo mundo = criatura.getMundo();
		if (mundo == null) {
			return EstadoBT.FRACASO;
		}

		// 4. Línea de PASO limpia (considera si el cuerpo entero cabe físicamente
		// directo al líder)
		final boolean pasoLimpio = mundo.hayLineaDePasoLimpia(miX, miY, targetX, targetY,
				criatura.getAnchoColisionPies(), criatura.getAltoColisionPies());

		if (pasoLimpio) {
			criatura.reiniciarRecorridoAEstrella();
			criatura.moverHaciaPuntoContinuo(targetX, targetY, DIST_STOP);
			return EstadoBT.EN_PROCESO;
		}

		// 5. Si el paso directo está bloqueado (ej: árbol o muro), usa el Flowfield de
		// Dijkstra en O(1)
		final DijkstraRework dijkstra = mundo.getDijkstra();
		if ((criatura.getClearanceRequerido() == 1) && (dijkstra != null)) {
			final NodoD nodo = dijkstra.getNodoCercano((int) miX, (int) miY);
			if (nodo != null) {
				final int readBuf = dijkstra.getBufferLecturaIndex();
				final NodoD siguiente = nodo.getNodoProcedente(readBuf);

				if (siguiente != null) {
					final double waypointX = siguiente.getXMundo() + (siguiente.getAncho() / 2.0);
					final double waypointY = siguiente.getYMundo() + (siguiente.getAlto() / 2.0);
					criatura.moverHaciaPuntoContinuo(waypointX, waypointY, RADIO_LLEGADA_WAYPOINT);
					return EstadoBT.EN_PROCESO;
				}
			}
		}

		// 6. Fallback a A* si Dijkstra no tiene ruta activa
		if ((criatura.getNodoADestino() == null) && criatura.getRecorridoA().isEmpty()) {
			if (bb.puedeRecalcularRuta()) {
				criatura.calcularRutaAEstrella((int) targetX, (int) targetY);
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