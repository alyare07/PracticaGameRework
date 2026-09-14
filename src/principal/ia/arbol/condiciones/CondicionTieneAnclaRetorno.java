package principal.ia.arbol.condiciones;

import principal.entes.criaturas.Criatura;
import principal.ia.arbol.BlackboardIA;
import principal.ia.arbol.EstadoBT;
import principal.ia.arbol.NodoBT;

/**
 * Condición que verifica si la Criatura tiene un anclaje dinámico de regreso
 * programado tras haber huido de un peligro (Zero-GC / O(1)).
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class CondicionTieneAnclaRetorno implements NodoBT {

	public CondicionTieneAnclaRetorno() {
	}

	@Override
	public EstadoBT ejecutar(final Criatura criatura, final BlackboardIA bb, final double dt) {
		return bb.tieneAnclaRetorno() ? EstadoBT.EXITO : EstadoBT.FRACASO;
	}
}