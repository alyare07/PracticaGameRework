package principal.ia.arbol.acciones;

import java.util.Random;

import principal.entes.criaturas.Criatura;
import principal.ia.arbol.BlackboardIA;
import principal.ia.arbol.EstadoBT;
import principal.ia.arbol.NodoBT;

/**
 * Acción de ocio pasivo que hace girar la mirada de la Criatura a intervalos
 * aleatorios regulados por los cooldowns del Blackboard (Zero-GC).
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class AccionMirarAlrededor implements NodoBT {

	private static final Random RANDOM = new Random();
	private final double intervaloMinimoSegundos;
	private final double intervaloMaximoSegundos;

	public AccionMirarAlrededor(final double minSegundos, final double maxSegundos) {
		this.intervaloMinimoSegundos = Math.max(1.0, minSegundos);
		this.intervaloMaximoSegundos = Math.max(this.intervaloMinimoSegundos, maxSegundos);
	}

	public AccionMirarAlrededor() {
		this(3.0, 6.0);
	}

	@Override
	public EstadoBT ejecutar(final Criatura criatura, final BlackboardIA bb, final double dt) {
		if (criatura.estaEstadoCaminando() || criatura.estaEstadoCorriendo() || criatura.estaEstadoAtacando()) {
			return EstadoBT.FRACASO;
		}

		if (bb.estaEnCooldown(BlackboardIA.CD_PAUSA_PATRULLA)) {
			return EstadoBT.EXITO;
		}

		// Rota a una dirección aleatoria
		criatura.setDireccion(Criatura.DIRECCIONES_ARRAY[RANDOM.nextInt(Criatura.DIRECCIONES_ARRAY.length)]);

		final double duracion = this.intervaloMinimoSegundos
				+ (RANDOM.nextDouble() * (this.intervaloMaximoSegundos - this.intervaloMinimoSegundos));

		bb.iniciarCooldown(BlackboardIA.CD_PAUSA_PATRULLA, duracion);
		return EstadoBT.EXITO;
	}
}