package principal.ia.arbol;

import principal.entes.criaturas.Criatura;

/**
 * Nodo Compuesto de tipo Secuencia (AND lógico).
 * <p>
 * Ejecuta a sus hijos de forma encadenada. Si cualquiera de los hijos retorna
 * {@link EstadoBT#FRACASO} o {@link EstadoBT#EN_PROCESO}, la ejecución se detiene
 * de inmediato y propaga dicho estado. Solo si la totalidad de los hijos tienen
 * éxito retorna {@link EstadoBT#EXITO}.
 * </p>
 * 
 * @version 1.0 (Vanilla Java 8 - Cache-Friendly Array Traversal)
 */
public class Secuencia implements NodoBT {

	private final NodoBT[] hijos;

	public Secuencia(final NodoBT... hijos) {
		this.hijos = (hijos != null) ? hijos : new NodoBT[0];
	}

	@Override
	public EstadoBT ejecutar(final Criatura criatura, final BlackboardIA bb, final double dt) {
		for (int i = 0; i < this.hijos.length; i++) {
			final EstadoBT resultado = this.hijos[i].ejecutar(criatura, bb, dt);

			if (resultado != EstadoBT.EXITO) {
				return resultado;
			}
		}
		return EstadoBT.EXITO;
	}
}