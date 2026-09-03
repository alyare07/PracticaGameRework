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
	 * Calcula el factor de visibilidad de una entidad (0.25 en sombras densas a 1.0
	 * en luz plena).
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
	 * @param observador      Criatura que busca (IA).
	 * @param objetivo        Entidad a detectar (Jugador).
	 * @param rangoAlertaBase Distancia máxima de visión del enemigo a plena luz
	 *                        (ej: 180 px).
	 * @return {@code true} si el objetivo es detectado en su nivel actual de
	 *         penumbra.
	 */
	/**
	 * Evalúa si un observador (IA) puede detectar a un objetivo considerando
	 * distancia y nivel de luz. Aplica descarte temprano en O(1) antes de consultar
	 * el GestorLuz.
	 */
	public static boolean puedeDetectar(final Ente observador, final Ente objetivo, final double rangoAlertaBase) {
		if ((observador == null) || (objetivo == null)) {
			return false;
		}

		final double dx = objetivo.getCentroX() - observador.getCentroX();
		final double dy = objetivo.getCentroY() - observador.getCentroY();
		final double distSq = (dx * dx) + (dy * dy);

		return puedeDetectar(distSq, objetivo, rangoAlertaBase);
	}

	/**
	 * Sobrecarga de alto rendimiento que reutiliza la distancia al cuadrado
	 * (distSq) ya calculada por la IA.
	 */
	public static boolean puedeDetectar(final double distSq, final Ente objetivo, final double rangoAlertaBase) {
		final double maxRangoSq = rangoAlertaBase * rangoAlertaBase;

		// 1. PODA TEMPRANA: Como el factor de luz está acotado en [0.25 .. 1.0],
		// si la distancia ya supera el rango máximo a plena luz, es imposible
		// detectarlo.
		if (distSq > maxRangoSq) {
			return false; // 0 consultas a GestorLuz
		}

		// 2. Solo si está dentro del radio absoluto, evaluamos la penumbra
		final float factorLuz = calcularFactorVisibilidad(objetivo);
		final double rangoEfectivo = rangoAlertaBase * factorLuz;

		return distSq <= (rangoEfectivo * rangoEfectivo);
	}
}