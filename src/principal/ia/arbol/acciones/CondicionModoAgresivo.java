package principal.ia.arbol.acciones;

import principal.entes.criaturas.Criatura;
import principal.ia.arbol.BlackboardIA;
import principal.ia.arbol.EstadoBT;
import principal.ia.arbol.NodoBT;

/**
 * Condición que comprueba si la Criatura tiene activa la postura ofensiva/agresiva
 * en su Blackboard (Zero-GC / O(1)).
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class CondicionModoAgresivo implements NodoBT {

	public CondicionModoAgresivo() {
	}

	@Override
	public EstadoBT ejecutar(final Criatura criatura, final BlackboardIA bb, final double dt) {
		return bb.isModoAgresivo() ? EstadoBT.EXITO : EstadoBT.FRACASO;
	}
}