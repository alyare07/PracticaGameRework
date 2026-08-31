package principal.iluminacion;

import principal.entes.Ente;
import principal.utilidades.Globales;

/**
 * Calculador táctico de sigilo y visibilidad fotorreactiva para la Inteligencia
 * Artificial de criaturas y enemigos (Zero-GC / O(1)).
 * 
 * @author Copiloto Técnico
 * @version 1.0
 */
public final class CalculadorSigilo {

	// =========================================================================
	// === CONSTANTES DE VISIBILIDAD
	// =========================================================================

	/**
	 * Nivel mínimo de visibilidad base en oscuridad total (25% por silueta y
	 * ruido).
	 */
	private static final float VISIBILIDAD_MINIMA_OSCURIDAD = 0.25f;

	private CalculadorSigilo() {
	}

	// =========================================================================
	// === CONSULTAS DE SIGILO EN TIEMPO CONSTANTE O(1)
	// =========================================================================

	/**
	 * Calcula el factor de visibilidad de una entidad (0.25 en sombras densas a
	 * 1.0 en luz plena).
	 *
	 * @param objetivo Entidad a evaluar (ej: Jugador).
	 * @return Multiplicador escalar de visibilidad.
	 */
	public static float calcularFactorVisibilidad(final Ente objetivo) {
		if ((objetivo == null) || (Globales.GESTOR_LUZ == null)) {
			return 1.0f;
		}

		final double x = objetivo.getCentroX();
		final double y = objetivo.getCentroY();

		// Nivel de claridad lumínica en el punto (0.0 = oscuridad, 1.0 = claridad)
		final float nivelLuz = Globales.GESTOR_LUZ.getNivelLuzEn(x, y);

		// Interpolación de visibilidad
		return VISIBILIDAD_MINIMA_OSCURIDAD + ((1.0f - VISIBILIDAD_MINIMA_OSCURIDAD) * nivelLuz);
	}

	/**
	 * Evalúa si un observador (enemigo/guardia) puede detectar a un objetivo
	 * considerando su distancia, ángulo y el nivel de luz en el que se oculta.
	 *
	 * @param observador       Criatura que busca (IA).
	 * @param objetivo         Entidad a detectar (Jugador).
	 * @param rangoAlertaBase  Distancia máxima de visión del enemigo a plena luz
	 *                         (ej: 180 px).
	 * @return {@code true} si el objetivo es detectado en su nivel actual de
	 *         penumbra.
	 */
	public static boolean puedeDetectar(final Ente observador, final Ente objetivo, final double rangoAlertaBase) {
		if ((observador == null) || (objetivo == null)) {
			return false;
		}

		final double dx = objetivo.getCentroX() - observador.getCentroX();
		final double dy = objetivo.getCentroY() - observador.getCentroY();
		final double distSq = (dx * dx) + (dy * dy);

		final float factorLuz = calcularFactorVisibilidad(objetivo);
		final double rangoEfectivo = rangoAlertaBase * factorLuz;

		return distSq <= (rangoEfectivo * rangoEfectivo);
	}
}