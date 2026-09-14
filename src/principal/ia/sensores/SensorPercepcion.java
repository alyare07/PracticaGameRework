package principal.ia.sensores;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Criatura.Direccion;
import principal.entes.criaturas.Jugador;
import principal.iluminacion.CalculadorSigilo;
import principal.mapa.Mundo;
import principal.mapa.renderEntidades.ZoneBox;
import principal.utilidades.Globales;

/**
 * Motor sensorial táctico con oclusión de muros sólidos reales (anti-rayos X) y
 * detección cónica angular (Zero-GC / O(1)).
 * 
 * @version 3.1 (Vanilla Java 8 - Solid Obstacle Occlusion & LOS Verification)
 */
public final class SensorPercepcion {

	private static final double BURBUJA_PROXIMIDAD_SQ = 32.0 * 32.0;

	private SensorPercepcion() {
	}

	public static boolean puedeVerObjetivo(final Criatura observador, final Ente objetivo, final double rangoMaximo,
			final double aperturaFactor) {

		if ((observador == null) || (objetivo == null) || objetivo.estaEliminado()) {
			return false;
		}

		final double dx = objetivo.getCentroX() - observador.getCentroX();
		final double dy = objetivo.getCentroY() - observador.getCentroY();
		final double distSq = (dx * dx) + (dy * dy);

		final double maxRangoSq = rangoMaximo * rangoMaximo;
		if (distSq > maxRangoSq) {
			return false;
		}

		final Mundo mundo = observador.getMundo();

		// 1. Burbuja de proximidad a corta distancia (32 px)
		if (distSq <= BURBUJA_PROXIMIDAD_SQ) {
			// No detecta si hay un muro sólido o casa de por medio
			if ((mundo != null) && !mundo.hayLineaDeTiroLimpia(observador.getCentroX(), observador.getCentroY(),
					objetivo.getCentroX(), objetivo.getCentroY())) {
				return false;
			}
			return true;
		}

		// 2. Cono direccional si no está en combate
		if (!observador.estaEstadoPersiguiendo() && !observador.estaEstadoAtacando()) {
			if (!estaEnConoDireccional(observador.getDireccion(), dx, dy, aperturaFactor)) {
				return false;
			}
		}

		// 3. Verificación de oclusión por estructuras y muros sólidos
		if ((mundo != null) && !mundo.hayLineaDeTiroLimpia(observador.getCentroX(), observador.getCentroY(),
				objetivo.getCentroX(), objetivo.getCentroY())) {
			return false;
		}

		return CalculadorSigilo.puedeDetectar(distSq, objetivo, rangoMaximo);
	}

	public static boolean estaEnConoDireccional(final Direccion dir, final double dx, final double dy,
			final double aperturaFactor) {

		final double factor = Math.max(1.2, aperturaFactor);

		switch (dir) {
		case NORTE:
			return (dy < 0.0) && (Math.abs(dx) <= (-dy * factor));
		case SUR:
			return (dy > 0.0) && (Math.abs(dx) <= (dy * factor));
		case ESTE:
			return (dx > 0.0) && (Math.abs(dy) <= (dx * factor));
		case OESTE:
			return (dx < 0.0) && (Math.abs(dy) <= (-dx * factor));
		default:
			return true;
		}
	}

	public static Criatura buscarHostilMasCercano(final Criatura observador, final double rangoMaximo,
			final double aperturaFactor) {

		if ((observador == null) || (observador.getMundo() == null)) {
			return null;
		}

		// 1. Evalúa al Jugador
		final Jugador jugador = Globales.JUGADOR;
		if ((jugador != null) && !jugador.estaEliminado() && observador.esHostilHacia(jugador)) {
			if (puedeVerObjetivo(observador, jugador, rangoMaximo, aperturaFactor)) {
				return jugador;
			}
		}

		// 2. Escaneo espacial en cuadrícula
		final Mundo mundo = observador.getMundo();
		final int ladoZB = mundo.getLadoZoneBox();
		final double miX = observador.getCentroX();
		final double miY = observador.getCentroY();

		final int minGX = Math.max(0, Math.floorDiv((int) (miX - rangoMaximo), ladoZB));
		final int maxGX = Math.min(mundo.getCantZonasX() - 1, Math.floorDiv((int) (miX + rangoMaximo), ladoZB));
		final int minGY = Math.max(0, Math.floorDiv((int) (miY - rangoMaximo), ladoZB));
		final int maxGY = Math.min(mundo.getCantZonasY() - 1, Math.floorDiv((int) (miY + rangoMaximo), ladoZB));

		Criatura mejorBlanco = null;
		double menorDistSq = rangoMaximo * rangoMaximo;

		for (int gy = minGY; gy <= maxGY; gy++) {
			for (int gx = minGX; gx <= maxGX; gx++) {
				final ZoneBox zb = mundo.getZonaGrid(gx, gy);
				if (zb == null) {
					continue;
				}

				final int totalCriat = zb.getCriaturas().size();
				for (int i = 0; i < totalCriat; i++) {
					final Criatura candidata = zb.getCriaturas().get(i);

					if ((candidata == observador) || candidata.estaEliminado()
							|| !observador.esHostilHacia(candidata)) {
						continue;
					}

					final double dx = candidata.getCentroX() - miX;
					final double dy = candidata.getCentroY() - miY;
					final double distSq = (dx * dx) + (dy * dy);

					if (distSq >= menorDistSq) {
						continue;
					}

					if (puedeVerObjetivo(observador, candidata, rangoMaximo, aperturaFactor)) {
						menorDistSq = distSq;
						mejorBlanco = candidata;
					}
				}
			}
		}

		return mejorBlanco;
	}
}