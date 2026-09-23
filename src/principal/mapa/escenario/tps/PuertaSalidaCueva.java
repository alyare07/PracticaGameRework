package principal.mapa.escenario.tps;

import principal.entes.criaturas.Criatura;
import principal.utilidades.Globales;

/**
 * Puerta de teletransporte dinámica para interiores de cueva (Zero-GC / O(1)).
 * No requiere configurar el mundo exterior a mano: consulta el anclaje de
 * origen registrado en GestorDerrumbes y devuelve al pionero al exterior frente
 * a la entrada.
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class PuertaSalidaCueva extends PuertaTP {

	public PuertaSalidaCueva() {
	}

	@Override
	public void teletransportar(final Criatura c) {
		if (Globales.GESTOR_DERRUMBES == null) {
			return;
		}
		Globales.GESTOR_DERRUMBES.retornarAlExterior(c);
	}
}