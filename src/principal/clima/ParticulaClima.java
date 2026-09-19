package principal.clima;

import principal.utilidades.Constantes;

/**
 * Partícula atmosférica en espacio de pantalla con envoltura toroidal fluida y
 * estilización cinemática de trazo largo para Pixel-Art (Zero-GC).
 * 
 * @version 6.0
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
		this.velocidadBase = 1.0 + (Math.random() * 0.5);
		// Trazo estilizado más largo: da sensación de velocidad sin saturar la pantalla
		this.longitudTrazo = 14.0 + (Math.random() * 10.0);
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