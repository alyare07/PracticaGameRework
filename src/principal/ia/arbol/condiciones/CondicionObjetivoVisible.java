package principal.ia.arbol.condiciones;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.ia.arbol.BlackboardIA;
import principal.ia.arbol.EstadoBT;
import principal.ia.arbol.NodoBT;
import principal.ia.sensores.SensorPercepcion;

/**
 * Condición sensorial de combate con ventana de memoria activa persistente
 * (2.5s). Evita el colapso de estado y el parpadeo de animaciones cuando el
 * blanco quiebra la línea visual momentáneamente tras una esquina o cobertura
 * (Zero-GC / O(1)).
 * 
 * @version 3.1 (Vanilla Java 8 - Sustained Combat Tracking & Anti-Flapping)
 */
public class CondicionObjetivoVisible implements NodoBT {

	private static final double TIEMPO_PERSISTENCIA_PERSECUCION = 2.5;
	private final double rangoVision;
	private final double aperturaConoFactor;

	public CondicionObjetivoVisible(final double rangoVision, final double aperturaConoFactor) {
		this.rangoVision = Math.max(10.0, rangoVision);
		this.aperturaConoFactor = Math.max(0.4, aperturaConoFactor);
	}

	@Override
	public EstadoBT ejecutar(final Criatura criatura, final BlackboardIA bb, final double dt) {
		final Ente objetivo = bb.getObjetivoActual();

		// 1. Objetivo ya fijado en combate activo
		if ((objetivo != null) && !objetivo.estaEliminado()) {
			// Si tiene línea de visión directa: refresca posición y reinicia reloj
			if (SensorPercepcion.puedeVerObjetivo(criatura, objetivo, this.rangoVision, this.aperturaConoFactor)) {
				bb.memorizarPosicionObjetivo(objetivo.getCentroX(), objetivo.getCentroY());
				return EstadoBT.EXITO;
			}

			// MEMORIA ACTIVA DE COMBATE: Durante 2.5s continúa en combate corriendo
			// hacia la última posición recordada mediante AccionAproximarse (sin parpadeo a
			// INVESTIGANDO)
			if (bb.getTiempoSinVerObjetivo() <= TIEMPO_PERSISTENCIA_PERSECUCION) {
				return EstadoBT.EXITO;
			}

			// Transcurrido el tiempo de persistencia, se libera el blanco para permitir la
			// inspección perimetral
			bb.setObjetivoActual(null);
		}

		// 2. Detección de nuevo agresor en el entorno
		final Criatura nuevoBlanco = SensorPercepcion.buscarHostilMasCercano(criatura, this.rangoVision,
				this.aperturaConoFactor);

		if (nuevoBlanco != null) {
			bb.setObjetivoActual(nuevoBlanco);
			criatura.alertarAliadosCercanos(nuevoBlanco, 120.0);
			return EstadoBT.EXITO;
		}

		return EstadoBT.FRACASO;
	}
}