package principal.ia.arbol.condiciones;

import principal.entes.criaturas.Criatura;
import principal.ia.arbol.BlackboardIA;
import principal.ia.arbol.EstadoBT;
import principal.ia.arbol.NodoBT;

/**
 * Condición que verifica si la salud actual de la Criatura es inferior o igual
 * a un umbral porcentual específico (ej: 0.25 para 25% de HP).
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class CondicionSaludBaja implements NodoBT {

	private final double umbralPorcentual;

	/**
	 * @param umbralPorcentual Valor entre 0.0 (0%) y 1.0 (100%).
	 */
	public CondicionSaludBaja(final double umbralPorcentual) {
		this.umbralPorcentual = Math.max(0.0, Math.min(1.0, umbralPorcentual));
	}

	@Override
	public EstadoBT ejecutar(final Criatura criatura, final BlackboardIA bb, final double dt) {
		final double hpMax = Math.max(1.0, criatura.getVidaMaxima());
		final double ratio = criatura.getVida() / hpMax;

		final boolean bajoHp = (ratio <= this.umbralPorcentual);

		if (bajoHp) {
			bb.setEnPanico(true);
			return EstadoBT.EXITO;
		}

		bb.setEnPanico(false);
		return EstadoBT.FRACASO;
	}
}