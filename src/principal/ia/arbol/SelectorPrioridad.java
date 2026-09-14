package principal.ia.arbol;

import principal.entes.criaturas.Criatura;

/**
 * Nodo Compuesto de tipo Selector / Fallback (OR lógico).
 * <p>
 * Ejecuta a sus hijos secuencialmente por orden de prioridad. Si un hijo retorna
 * {@link EstadoBT#EXITO} o {@link EstadoBT#EN_PROCESO}, el selector se detiene y
 * retorna dicho resultado. Solo si todos los hijos fallan retorna {@link EstadoBT#FRACASO}.
 * </p>
 * 
 * @version 1.0 (Vanilla Java 8 - Cache-Friendly Array Traversal)
 */
public class SelectorPrioridad implements NodoBT {

	private final NodoBT[] hijos;

	public SelectorPrioridad(final NodoBT... hijos) {
		this.hijos = (hijos != null) ? hijos : new NodoBT[0];
	}

	@Override
	public EstadoBT ejecutar(final Criatura criatura, final BlackboardIA bb, final double dt) {
		for (int i = 0; i < this.hijos.length; i++) {
			final EstadoBT resultado = this.hijos[i].ejecutar(criatura, bb, dt);

			if (resultado != EstadoBT.FRACASO) {
				return resultado;
			}
		}
		return EstadoBT.FRACASO;
	}
}