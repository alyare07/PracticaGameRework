package principal.utilidades.funciones;

import principal.utilidades.Constantes;

/**
 * Clase de utilidad para el cálculo matemático de trayectorias lineales y
 * curvas de Bézier 2D.
 */
public final class Trayectorias {

	public Trayectorias() {
	}

	/**
	 * Genera una trayectoria parabólica (Bézier cuadrática) desde el origen hasta
	 * el destino.
	 *
	 * @param px1                             x Origen
	 * @param py1                             y Origen
	 * @param px2                             x Destino
	 * @param py2                             y Destino
	 * @param tiempoMsTrayectoriaEnAnchoJuego Tiempo en MS que tardaría en cruzar la
	 *                                        pantalla completa.
	 * @return Matriz [2][pasos]: [0] contiene las coordenadas X, [1] contiene las
	 *         Y.
	 */
	public int[][] getTrayectoiaBezier(final int px1, final int py1, final int px2, final int py2,
			final double tiempoMsTrayectoriaEnAnchoJuego) {

		final int aps = 60;
		final int msPorSegundo = 1000;
		final double cantApsAncho = (tiempoMsTrayectoriaEnAnchoJuego / msPorSegundo) * aps;
		final double dist = this.calcularDistancia(px1, py1, px2, py2);

		final double cantApsDistancia = (dist <= (Constantes.ANCHO_JUEGO / 4.0))
				? (dist * (cantApsAncho * 1.5)) / Constantes.ANCHO_JUEGO
				: (dist * cantApsAncho) / Constantes.ANCHO_JUEGO;

		final double vel = Math.max(0.005, 1.0 / Math.max(1.0, cantApsDistancia));
		final int pasos = (int) Math.ceil(1.0 / vel) + 1;

		final int[][] coords = new int[2][pasos];

		final double x3 = (px1 + px2) / 2.0;
		final double alturaArco = Math.max(25.0, dist * 0.25);
		final double y3 = Math.min(py1, py2) - alturaArco;

		int i = 0;
		for (double t = 0.0; (t <= 1.0) && (i < pasos); t += vel) {
			final double oneMinusT = 1.0 - t;
			final double x = (oneMinusT * oneMinusT * px1) + (2 * oneMinusT * t * x3) + (t * t * px2);
			final double y = (oneMinusT * oneMinusT * py1) + (2 * oneMinusT * t * y3) + (t * t * py2);

			coords[0][i] = (int) Math.round(x);
			coords[1][i] = (int) Math.round(y);
			i++;
		}

		coords[0][pasos - 1] = px2;
		coords[1][pasos - 1] = py2;

		return coords;
	}

	/**
	 * Genera una trayectoria recta uniforme de P1 a P2.
	 */
	public int[][] getTrayectoiaLineal(final int px1, final int py1, final int px2, final int py2, final int pasos) {
		final int cantPasos = Math.max(2, pasos);
		final int[][] coords = new int[2][cantPasos];

		for (int i = 0; i < cantPasos; i++) {
			final double t = (double) i / (cantPasos - 1);
			coords[0][i] = (int) Math.round(px1 + (t * (px2 - px1)));
			coords[1][i] = (int) Math.round(py1 + (t * (py2 - py1)));
		}

		return coords;
	}

	/**
	 * Calcula la distancia euclidiana directa en O(1) con Math.sqrt.
	 */
	public double calcularDistancia(final double x1, final double y1, final double x2, final double y2) {
		final double dx = x2 - x1;
		final double dy = y2 - y1;
		return Math.sqrt((dx * dx) + (dy * dy));
	}
}