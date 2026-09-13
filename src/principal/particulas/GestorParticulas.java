package principal.particulas;

import java.awt.Graphics2D;
import java.util.Random;

import principal.configuracion.ConfiguracionGrafica;
import principal.utilidades.Globales;

/**
 * Gestor centralizado del sistema de partículas 2D con tope dinámico según el
 * perfil de hardware (Zero-GC / O(1)).
 * 
 * @version 3.1 (Vanilla Java 8)
 */
public class GestorParticulas {

	private static final int CAPACIDAD_MAXIMA = 2048;
	private static final int TOTAL_TIPOS = TipoParticula.values().length;

	private final Particula[] pool;
	private int cantidadActivas;
	private final double[] friccionesPrecalculadas;
	private final Random random;

	public GestorParticulas() {
		this.pool = new Particula[CAPACIDAD_MAXIMA];
		this.cantidadActivas = 0;
		this.friccionesPrecalculadas = new double[TOTAL_TIPOS];
		this.random = new Random();

		for (int i = 0; i < CAPACIDAD_MAXIMA; i++) {
			this.pool[i] = new Particula();
		}
	}

	public void emitirExplosion(final double x, final double y, final int cantidad) {
		for (int i = 0; i < cantidad; i++) {
			final double angulo = this.random.nextDouble() * Math.PI * 2.0;
			final double velocidad = 60.0 + (this.random.nextDouble() * 180.0);

			final double vx = Math.cos(angulo) * velocidad;
			final double vy = Math.sin(angulo) * velocidad;

			final TipoParticula tipo = (this.random.nextDouble() < 0.7) ? TipoParticula.FUEGO_CHISPA
					: TipoParticula.HUMO;
			this.spawnParticula(x, y, vx, vy, tipo, 0.9 + (this.random.nextDouble() * 0.4));
		}
	}

	public void emitirSangre(final double x, final double y, final double dirX, final double dirY, final int cantidad) {
		for (int i = 0; i < cantidad; i++) {
			final double dispersion = (this.random.nextDouble() * 1.6) - 0.8;
			final double velocidad = 50.0 + (this.random.nextDouble() * 120.0);

			final double vx = (dirX * velocidad) + (dispersion * 50.0);
			final double vy = (dirY * velocidad) - (40.0 + (this.random.nextDouble() * 80.0));

			this.spawnParticula(x, y, vx, vy, TipoParticula.SANGRE, 0.8 + (this.random.nextDouble() * 0.5));
		}
	}

	public void emitirPolvoPaso(final double x, final double y, final int cantidad) {
		for (int i = 0; i < cantidad; i++) {
			final double vx = (this.random.nextDouble() * 30.0) - 15.0;
			final double vy = -(this.random.nextDouble() * 25.0);

			this.spawnParticula(x, y, vx, vy, TipoParticula.POLVO_TIERRA, 0.7 + (this.random.nextDouble() * 0.4));
		}
	}

	public void emitirMagia(final double x, final double y, final int cantidad) {
		for (int i = 0; i < cantidad; i++) {
			final double angulo = this.random.nextDouble() * Math.PI * 2.0;
			final double velocidad = 25.0 + (this.random.nextDouble() * 75.0);

			final double vx = Math.cos(angulo) * velocidad;
			final double vy = Math.sin(angulo) * velocidad;

			this.spawnParticula(x, y, vx, vy, TipoParticula.MAGIA, 0.9 + (this.random.nextDouble() * 0.5));
		}
	}

	public void spawnParticula(final double x, final double y, final double vx, final double vy,
			final TipoParticula tipo, final double factorVida) {
		// Tope dinámico según el perfil configurado
		if (this.cantidadActivas >= ConfiguracionGrafica.LIMITE_PARTICULAS_MAX) {
			return;
		}

		final Particula p = this.pool[this.cantidadActivas];
		p.spawn(x, y, vx, vy, tipo, factorVida);
		this.cantidadActivas++;
	}

	public void actualizar() {
		if (this.cantidadActivas <= 0) {
			return;
		}

		final double dt = (Globales.delta > 0.0) ? Globales.delta : (1.0 / 60.0);
		final double factorDelta = dt * 60.0;
		final boolean deltaEstandar = (factorDelta >= 0.9999) && (factorDelta <= 1.0001);

		for (final TipoParticula tipo : TipoParticula.values()) {
			this.friccionesPrecalculadas[tipo.ordinal()] = deltaEstandar ? tipo.getFriccion()
					: Math.pow(tipo.getFriccion(), factorDelta);
		}

		int i = 0;
		while (i < this.cantidadActivas) {
			final Particula p = this.pool[i];
			final double friccion = this.friccionesPrecalculadas[p.getTipo().ordinal()];
			p.actualizar(dt, friccion);

			if (p.isActiva()) {
				i++;
			} else {
				final Particula temp = this.pool[i];
				this.pool[i] = this.pool[this.cantidadActivas - 1];
				this.pool[this.cantidadActivas - 1] = temp;
				this.cantidadActivas--;
			}
		}
	}

	public void pintar(final Graphics2D g) {
		if (this.cantidadActivas <= 0) {
			return;
		}

		final int camX = (Globales.CAMARA != null) ? Globales.CAMARA.getPosicionXInt() : 0;
		final int camY = (Globales.CAMARA != null) ? Globales.CAMARA.getPosicionYInt() : 0;
		final int margenX = (Globales.CAMARA != null) ? Globales.CAMARA.getMargenX() : 0;
		final int margenY = (Globales.CAMARA != null) ? Globales.CAMARA.getMargenY() : 0;

		final int camOffsetX = camX - margenX;
		final int camOffsetY = camY - margenY;

		for (int i = 0; i < this.cantidadActivas; i++) {
			this.pool[i].pintar(g, camOffsetX, camOffsetY);
		}
	}

	public void limpiar() {
		for (int i = 0; i < this.cantidadActivas; i++) {
			this.pool[i].desactivar();
		}
		this.cantidadActivas = 0;
	}

	public int getCantidadActivas() {
		return this.cantidadActivas;
	}
}