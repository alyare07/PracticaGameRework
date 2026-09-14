package principal.ia.arbol.condiciones;

import principal.entes.criaturas.Criatura;
import principal.ia.arbol.BlackboardIA;
import principal.ia.arbol.EstadoBT;
import principal.ia.arbol.NodoBT;

/**
 * Condición que comprueba si la Criatura tiene una alerta acústica o un rastro
 * recientemente perdido que amerite investigación.
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class CondicionTieneSospecha implements NodoBT {

	private final double tiempoMaximoMemoriaSegundos;

	/**
	 * @param tiempoMaximoMemoriaSegundos Tiempo máximo durante el cual un objetivo
	 *                                    perdido sigue siendo buscado.
	 */
	public CondicionTieneSospecha(final double tiempoMaximoMemoriaSegundos) {
		this.tiempoMaximoMemoriaSegundos = Math.max(1.0, tiempoMaximoMemoriaSegundos);
	}

	@Override
	public EstadoBT ejecutar(final Criatura criatura, final BlackboardIA bb, final double dt) {
		// 1. Prioridad: Ruido escuchado en el mapa
		if (bb.tieneSospechaPendiente()) {
			return EstadoBT.EXITO;
		}

		// 2. Objetivo que desapareció tras una esquina recientemente
		if (bb.tienePosicionObjetivoRecordada()) {
			if (bb.getTiempoSinVerObjetivo() <= this.tiempoMaximoMemoriaSegundos) {
				return EstadoBT.EXITO;
			}
			// El recuerdo caducó
			bb.olvidarPosicionObjetivo();
		}

		return EstadoBT.FRACASO;
	}
}