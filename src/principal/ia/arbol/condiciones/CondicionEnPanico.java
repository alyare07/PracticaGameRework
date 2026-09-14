package principal.ia.arbol.condiciones;

import principal.entes.criaturas.Criatura;
import principal.ia.arbol.BlackboardIA;
import principal.ia.arbol.EstadoBT;
import principal.ia.arbol.NodoBT;

/**
 * Condición que verifica si la Criatura se encuentra en estado de pánico
 * tras haber recibido un ataque (Zero-GC / O(1)).
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class CondicionEnPanico implements NodoBT {

	public CondicionEnPanico() {
	}

	@Override
	public EstadoBT ejecutar(final Criatura criatura, final BlackboardIA bb, final double dt) {
		return bb.isEnPanico() ? EstadoBT.EXITO : EstadoBT.FRACASO;
	}
}