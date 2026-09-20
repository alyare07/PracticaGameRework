package principal.ia.arbol.condiciones;

import principal.entes.criaturas.Criatura;
import principal.ia.arbol.BlackboardIA;
import principal.ia.arbol.EstadoBT;
import principal.ia.arbol.NodoBT;
import principal.utilidades.Globales;

/**
 * Condición que verifica si el tiempo del calendario in-game aún no alcanza la
 * hora programada para volver al puesto de comercio.
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class CondicionEsperaRetornoActiva implements NodoBT {

	public CondicionEsperaRetornoActiva() {
	}

	@Override
	public EstadoBT ejecutar(final Criatura criatura, final BlackboardIA bb, final double dt) {
		if (Globales.GESTOR_ASTRONOMICO == null) {
			return EstadoBT.FRACASO;
		}

		final double ahoraHoras = Globales.GESTOR_ASTRONOMICO.getHorasTotalesJuego();
		final double objetivoHoras = bb.getTimestampRetornoJuegoHoras();

		return (ahoraHoras < objetivoHoras) ? EstadoBT.EXITO : EstadoBT.FRACASO;
	}
}