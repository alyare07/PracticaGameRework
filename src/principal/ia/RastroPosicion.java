package principal.ia;

/**
 * Buffer circular de memoria contigua (Zero-GC) que almacena el rastro de posiciones
 * transitables verificadas dejadas por una entidad guía (Jugador).
 * 
 * @version 1.0 (Vanilla Java 8 - Zero-GC Circular Trail)
 */
public class RastroPosicion {

	private static final int CAPACIDAD = 24;
	private final int[] puntosX = new int[CAPACIDAD];
	private final int[] puntosY = new int[CAPACIDAD];

	private int indiceEscritura = 0;
	private int cantidadPuntos = 0;
	private int ultimoX = Integer.MIN_VALUE;
	private int ultimoY = Integer.MIN_VALUE;

	private static final int DISTANCIA_MINIMA_ENTRE_PUNTOS_SQ = 14 * 14; // ~196 px^2 (~1 tile)

	public RastroPosicion() {
	}

	public void actualizar(final int centroX, final int centroY) {
		if (this.cantidadPuntos > 0) {
			final int dx = centroX - this.ultimoX;
			final int dy = centroY - this.ultimoY;
			if (((dx * dx) + (dy * dy)) < DISTANCIA_MINIMA_ENTRE_PUNTOS_SQ) {
				return;
			}
		}

		this.puntosX[this.indiceEscritura] = centroX;
		this.puntosY[this.indiceEscritura] = centroY;
		this.ultimoX = centroX;
		this.ultimoY = centroY;

		this.indiceEscritura = (this.indiceEscritura + 1) % CAPACIDAD;
		if (this.cantidadPuntos < CAPACIDAD) {
			this.cantidadPuntos++;
		}
	}

	public int obtenerIndicePuntoMasCercano(final int miX, final int miY, final double rangoMaximoSq) {
		int mejorIndice = -1;
		double menorDistSq = rangoMaximoSq;

		for (int i = 0; i < this.cantidadPuntos; i++) {
			final int dx = this.puntosX[i] - miX;
			final int dy = this.puntosY[i] - miY;
			final double dSq = (dx * dx) + (dy * dy);

			if (dSq < menorDistSq) {
				menorDistSq = dSq;
				mejorIndice = i;
			}
		}
		return mejorIndice;
	}

	public int getPuntoX(final int indice) {
		return ((indice >= 0) && (indice < CAPACIDAD)) ? this.puntosX[indice] : this.ultimoX;
	}

	public int getPuntoY(final int indice) {
		return ((indice >= 0) && (indice < CAPACIDAD)) ? this.puntosY[indice] : this.ultimoY;
	}

	public int getCantidadPuntos() {
		return this.cantidadPuntos;
	}

	public void limpiar() {
		this.cantidadPuntos = 0;
		this.indiceEscritura = 0;
		this.ultimoX = Integer.MIN_VALUE;
		this.ultimoY = Integer.MIN_VALUE;
	}
}