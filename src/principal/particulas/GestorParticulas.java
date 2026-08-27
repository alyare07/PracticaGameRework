package principal.particulas;

import java.awt.Graphics2D;
import java.util.Random;

import principal.utilidades.Globales;

/**
 * Gestor centralizado de partículas con emisión visible de alto impacto.
 */
public class GestorParticulas {

	private static final int CAPACIDAD_MAXIMA = 2048;

	private final Particula[] pool;
	private final Particula[] activas;
	private int cantidadActivas;
	private int punteroCircular;
	private final Random random;

	public GestorParticulas() {
		this.pool = new Particula[CAPACIDAD_MAXIMA];
		this.activas = new Particula[CAPACIDAD_MAXIMA];
		this.cantidadActivas = 0;
		this.punteroCircular = 0;
		this.random = new Random();

		for (int i = 0; i < CAPACIDAD_MAXIMA; i++) {
			this.pool[i] = new Particula();
		}
	}

	// =========================================================================
	// === EMISORES DE ALTA VISIBILIDAD (API PÚBLICA)
	// =========================================================================

	/**
	 * Emite una explosión radial masiva de fuego y humo en el mundo.
	 */
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

	/**
	 * Emite salpicaduras direccionales de sangre al golpear a un enemigo.
	 */
	public void emitirSangre(final double x, final double y, final double dirX, final double dirY, final int cantidad) {
		for (int i = 0; i < cantidad; i++) {
			final double dispersion = (this.random.nextDouble() * 1.6) - 0.8;
			final double velocidad = 50.0 + (this.random.nextDouble() * 120.0);

			final double vx = (dirX * velocidad) + (dispersion * 50.0);
			final double vy = (dirY * velocidad) - (40.0 + (this.random.nextDouble() * 80.0)); // Salto vertical inicial

			this.spawnParticula(x, y, vx, vy, TipoParticula.SANGRE, 0.8 + (this.random.nextDouble() * 0.5));
		}
	}

	/**
	 * Emite polvo visible bajo los pies al caminar o hacer Dash.
	 */
	public void emitirPolvoPaso(final double x, final double y, final int cantidad) {
		for (int i = 0; i < cantidad; i++) {
			final double vx = (this.random.nextDouble() * 30.0) - 15.0;
			final double vy = -(this.random.nextDouble() * 25.0);

			this.spawnParticula(x, y, vx, vy, TipoParticula.POLVO_TIERRA, 0.7 + (this.random.nextDouble() * 0.4));
		}
	}

	/**
	 * Emite destellos mágicos cian flotantes.
	 */
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
		final Particula p = this.pool[this.punteroCircular];
		this.punteroCircular = (this.punteroCircular + 1) % CAPACIDAD_MAXIMA;

		final boolean yaEstabaActiva = p.isActiva();
		p.spawn(x, y, vx, vy, tipo, factorVida);

		if (!yaEstabaActiva && (this.cantidadActivas < CAPACIDAD_MAXIMA)) {
			this.activas[this.cantidadActivas] = p;
			this.cantidadActivas++;
		}
	}

	public void actualizar() {
		final double dt = (Globales.delta > 0.0) ? Globales.delta : (1.0 / 60.0);

		int i = 0;
		while (i < this.cantidadActivas) {
			final Particula p = this.activas[i];
			p.actualizar(dt);

			if (p.isActiva()) {
				i++;
			} else {
				this.activas[i] = this.activas[this.cantidadActivas - 1];
				this.activas[this.cantidadActivas - 1] = null;
				this.cantidadActivas--;
			}
		}
	}

	public void pintar(final Graphics2D g) {
		for (int i = 0; i < this.cantidadActivas; i++) {
			this.activas[i].pintar(g);
		}
	}

	public void limpiar() {
		for (int i = 0; i < this.cantidadActivas; i++) {
			this.activas[i] = null;
		}
		this.cantidadActivas = 0;
	}

	public int getCantidadActivas() {
		return this.cantidadActivas;
	}
}