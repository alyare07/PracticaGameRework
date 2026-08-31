package principal.clima;

import principal.utilidades.Constantes;

/**
 * Partícula atmosférica en espacio de pantalla con envoltura toroidal fluida
 * (Zero-GC).
 * 
 * @version 5.0
 */
public class ParticulaClima {

	private static final int MARGEN_ENVOLTURA = 40;

	public double x;
	public double y;
	public double velocidadBase;
	public double longitudTrazo;
	public double faseOscilacion;
	public double tamano;

	public void inicializarAleatorio() {
		this.x = (Math.random() * (Constantes.ANCHO_JUEGO + (MARGEN_ENVOLTURA * 2))) - MARGEN_ENVOLTURA;
		this.y = (Math.random() * (Constantes.ALTO_JUEGO + (MARGEN_ENVOLTURA * 2))) - MARGEN_ENVOLTURA;
		this.velocidadBase = 0.8 + (Math.random() * 0.5);
		this.longitudTrazo = 6.0 + (Math.random() * 8.0);
		this.faseOscilacion = Math.random() * Math.PI * 2.0;
		this.tamano = 1.0 + (Math.random() * 2.0);
	}

	public void actualizar(final double vx, final double vy, final double dt) {
		this.x += vx * dt;
		this.y += vy * dt;

		final int limiteMaxX = Constantes.ANCHO_JUEGO + MARGEN_ENVOLTURA;
		final int limiteMinX = -MARGEN_ENVOLTURA;
		final int limiteMaxY = Constantes.ALTO_JUEGO + MARGEN_ENVOLTURA;
		final int limiteMinY = -MARGEN_ENVOLTURA;

		if (this.x > limiteMaxX) {
			this.x = limiteMinX;
		} else if (this.x < limiteMinX) {
			this.x = limiteMaxX;
		}

		if (this.y > limiteMaxY) {
			this.y = limiteMinY;
		} else if (this.y < limiteMinY) {
			this.y = limiteMaxY;
		}
	}
}