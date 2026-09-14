package principal.ia.arbol;

import principal.entes.criaturas.Criatura;

/**
 * Decorador que invierte el resultado lógico de su nodo hijo (NOT booleano).
 * <ul>
 * <li>{@link EstadoBT#EXITO} pasa a ser {@link EstadoBT#FRACASO}.</li>
 * <li>{@link EstadoBT#FRACASO} pasa a ser {@link EstadoBT#EXITO}.</li>
 * <li>{@link EstadoBT#EN_PROCESO} se propaga sin cambios.</li>
 * </ul>
 * 
 * @version 1.0 (Vanilla Java 8 - Inverter Decorator Pattern)
 */
public class DecoradorInvertir implements NodoBT {

	private final NodoBT hijo;

	public DecoradorInvertir(final NodoBT hijo) {
		this.hijo = hijo;
	}

	@Override
	public EstadoBT ejecutar(final Criatura criatura, final BlackboardIA bb, final double dt) {
		if (this.hijo == null) {
			return EstadoBT.FRACASO;
		}

		final EstadoBT resultado = this.hijo.ejecutar(criatura, bb, dt);

		if (resultado == EstadoBT.EXITO) {
			return EstadoBT.FRACASO;
		} else if (resultado == EstadoBT.FRACASO) {
			return EstadoBT.EXITO;
		}

		return EstadoBT.EN_PROCESO;
	}
}