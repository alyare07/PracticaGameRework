package principal.ia.arbol.acciones;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.jugador.Jugador;
import principal.ia.arbol.BlackboardIA;
import principal.ia.arbol.EstadoBT;
import principal.ia.arbol.NodoBT;
import principal.ia.dijkstra.DijkstraRework;
import principal.ia.dijkstra.NodoD;
import principal.mapa.Mundo;
import principal.utilidades.Globales;

/**
 * Acción táctica para acompañantes con formación escalonada por ranuras
 * (anti-dogpile), banda muerta anti-vibraciones y discriminación de
 * teletransporte por dificultad (Zero-GC).
 * 
 * @version 2.4 (Vanilla Java 8 - Multi-Follower Slot Staggering)
 */
public class AccionSeguirLider implements NodoBT {

	// Distancia de pérdida de paso / abandono táctico
	private static final double DIST_ABANDONO = 280.0;
	private static final double DIST_ABANDONO_SQ = DIST_ABANDONO * DIST_ABANDONO;

	// Distancia para teletransporte forzado en modo fácil
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

		// Consulta la ranura en la escolta activa (0 a 4) para escalonar la distancia
		// de parada
		final int indiceEscolta = (Globales.GESTOR_GRUPO != null) ? Globales.GESTOR_GRUPO.obtenerIndiceEscolta(criatura)
				: 0;
		final int slotSeguro = Math.max(0, indiceEscolta);

		// Escalonamiento: Slot 0 = 22 px, Slot 1 = 30 px, Slot 2 = 38 px, Slot 3 = 46
		// px, Slot 4 = 54 px
		final double distStop = 22.0 + (slotSeguro * 8.0);
		final double distStopSq = distStop * distStop;
		final double distStart = distStop + 12.0;
		final double distStartSq = distStart * distStart;

		final double targetX = lider.getPieX();
		final double targetY = lider.getPieY();
		final double miX = criatura.getPieX();
		final double miY = criatura.getPieY();

		final double dx = targetX - miX;
		final double dy = targetY - miY;
		final double distSq = (dx * dx) + (dy * dy);

		// 1. Evaluación de distancia excesiva / Abandono
		if (distSq > DIST_ABANDONO_SQ) {
			if (!bb.isPuedeTparseAlLider()) {
				// MODO NORMAL / TÁCTICO: Se detiene, entra en reposo y espera al jugador
				criatura.detenerMovimiento();
				criatura.reiniciarRecorridoAEstrella();
				if (!criatura.estaEstadoEstandar()) {
					criatura.setEstadoEstandar();
				}
				criatura.setDireccionMirandoCriatura(lider);
				return EstadoBT.EXITO;
			}
			// MODO FÁCIL: Teletransporte de rescate instantáneo al superar 360 px
			if (distSq > DIST_TELEPORT_RESCATE_SQ) {
				criatura.setPosicion(lider.getPosicionX(), lider.getPosicionY());
				criatura.reiniciarRecorridoAEstrella();
				criatura.detenerMovimiento();
				criatura.setEstadoEstandar();
				return EstadoBT.EXITO;
			}
		}

		// 2. Zona de confort: Si está a distancia de parada escalonada de su ranura,
		// frena
		if (distSq <= distStopSq) {
			criatura.detenerMovimiento();
			criatura.reiniciarRecorridoAEstrella();
			if (!criatura.estaEstadoEstandar()) {
				criatura.setEstadoEstandar();
			}
			criatura.setDireccionMirandoCriatura(lider);
			return EstadoBT.EXITO;
		}

		// 3. Banda muerta (Hysteresis): Si estaba parada, solo arranca si supera
		// distStart
		if (!criatura.estaEnMovimientoFisico() && (distSq < distStartSq)) {
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
			criatura.moverHaciaPuntoContinuo(targetX, targetY, distStop);
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