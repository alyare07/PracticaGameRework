package principal.ia.arbol;

import principal.entes.criaturas.Criatura;

/**
 * Decorador que bloquea la ejecución de su nodo hijo si el temporizador asociado
 * en el {@link BlackboardIA} aún no ha expirado.
 * 
 * @version 1.0 (Vanilla Java 8 - Zero-GC Decorator Pattern)
 */
public class DecoradorCooldown implements NodoBT {

	private final int idCooldown;
	private final double duracionSegundos;
	private final NodoBT hijo;

	/**
	 * @param idCooldown       Índice del temporizador en {@link BlackboardIA}.
	 * @param duracionSegundos Tiempo de enfriamiento a aplicar al tener éxito.
	 * @param hijo             Nodo subordinado a evaluar.
	 */
	public DecoradorCooldown(final int idCooldown, final double duracionSegundos, final NodoBT hijo) {
		this.idCooldown = idCooldown;
		this.duracionSegundos = Math.max(0.0, duracionSegundos);
		this.hijo = hijo;
	}

	@Override
	public EstadoBT ejecutar(final Criatura criatura, final BlackboardIA bb, final double dt) {
		if (bb.estaEnCooldown(this.idCooldown)) {
			return EstadoBT.FRACASO;
		}

		final EstadoBT resultado = this.hijo.ejecutar(criatura, bb, dt);

		if (resultado == EstadoBT.EXITO) {
			bb.iniciarCooldown(this.idCooldown, this.duracionSegundos);
		}

		return resultado;
	}
}