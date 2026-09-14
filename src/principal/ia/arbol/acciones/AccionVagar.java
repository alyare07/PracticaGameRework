package principal.ia.arbol.acciones;

import java.awt.Rectangle;
import java.util.Random;

import principal.entes.criaturas.Criatura;
import principal.ia.arbol.BlackboardIA;
import principal.ia.arbol.EstadoBT;
import principal.ia.arbol.NodoBT;
import principal.mapa.Mundo;

/**
 * Acción de merodeo con descarte estricto de puntos detrás de muros continuos
 * (Zero-GC).
 * 
 * @version 2.1 (Vanilla Java 8 - Wall Boundary & Clearance Patrol Integration)
 */
public class AccionVagar implements NodoBT {

	private static final Random RANDOM = new Random();
	private final double radioVagabundeo;
	private final double tiempoDescansoMin;
	private final double tiempoDescansoMax;
	private final Rectangle rectColisionAux = new Rectangle();

	public AccionVagar(final double radioVagabundeo, final double minPausaSegundos, final double maxPausaSegundos) {
		this.radioVagabundeo = Math.max(20.0, radioVagabundeo);
		this.tiempoDescansoMin = Math.max(1.0, minPausaSegundos);
		this.tiempoDescansoMax = Math.max(this.tiempoDescansoMin, maxPausaSegundos);
	}

	public AccionVagar() {
		this(48.0, 2.5, 5.0);
	}

	@Override
	public EstadoBT ejecutar(final Criatura criatura, final BlackboardIA bb, final double dt) {
		if (bb.estaEnCooldown(BlackboardIA.CD_PAUSA_PATRULLA)) {
			if (!criatura.estaEstadoEstandar()) {
				criatura.setEstadoEstandar();
			}
			return EstadoBT.EXITO;
		}

		final Mundo mundo = criatura.getMundo();

		if (!bb.tieneDestinoVagarActivo()) {
			final double centroBaseX = criatura.getPosicionXInicial();
			final double centroBaseY = criatura.getPosicionYInicial();

			double candidataX = centroBaseX;
			double candidataY = centroBaseY;
			boolean puntoValido = false;

			for (int intento = 0; intento < 5; intento++) {
				final double angulo = RANDOM.nextDouble() * Math.PI * 2.0;
				final double dist = 16.0 + (RANDOM.nextDouble() * (this.radioVagabundeo - 16.0));

				candidataX = centroBaseX + (Math.cos(angulo) * dist);
				candidataY = centroBaseY + (Math.sin(angulo) * dist);

				if (mundo != null) {
					this.rectColisionAux.setBounds((int) candidataX - 4, (int) candidataY - 4, 8, 8);

					// Debe ser suelo libre Y no tener un muro sólido en el trayecto
					final boolean esSueloLibre = !mundo.colisionaConZonaUObjetoSolido(this.rectColisionAux);
					final boolean sinMuroEnMedio = mundo.hayLineaDeTiroLimpia(centroBaseX, centroBaseY, candidataX,
							candidataY);

					if (esSueloLibre && sinMuroEnMedio) {
						puntoValido = true;
						break;
					}
				}
			}

			if (!puntoValido) {
				bb.iniciarCooldown(BlackboardIA.CD_PAUSA_PATRULLA, this.tiempoDescansoMin);
				return EstadoBT.EXITO;
			}
			bb.fijarDestinoVagar(candidataX, candidataY);
		}

		final double targetX = bb.getDestinoVagarX();
		final double targetY = bb.getDestinoVagarY();

		final double dx = targetX - criatura.getCentroX();
		final double dy = targetY - criatura.getCentroY();
		final double distSq = (dx * dx) + (dy * dy);

		if (distSq <= (8.0 * 8.0)) {
			bb.limpiarDestinoVagar();
			criatura.setEstadoEstandar();

			final double pausa = this.tiempoDescansoMin
					+ (RANDOM.nextDouble() * (this.tiempoDescansoMax - this.tiempoDescansoMin));
			bb.iniciarCooldown(BlackboardIA.CD_PAUSA_PATRULLA, pausa);

			return EstadoBT.EXITO;
		}

		final boolean movio = criatura.moverHaciaPuntoContinuo(targetX, targetY, 6.0);
		if (!movio) {
			bb.limpiarDestinoVagar();
			bb.iniciarCooldown(BlackboardIA.CD_PAUSA_PATRULLA, this.tiempoDescansoMin);
			criatura.setEstadoEstandar();
			return EstadoBT.FRACASO;
		}

		return EstadoBT.EN_PROCESO;
	}
}