package principal.particulas;

import java.awt.Color;
import java.awt.Graphics2D;

import principal.utilidades.DibujoDebug;

/**
 * Unidad individual de partícula física visible con sombra de contraste y
 * referencia de cámara.
 */
public class Particula {

	private boolean activa;
	private TipoParticula tipo;

	private double posX;
	private double posY;
	private double velX;
	private double velY;

	private double duracionSegundos;
	private double vidaRestante;

	public Particula() {
		this.activa = false;
	}

	public void spawn(final double x, final double y, final double velX, final double velY, final TipoParticula tipo,
			final double factorVida) {
		this.posX = x;
		this.posY = y;
		this.velX = velX;
		this.velY = velY;
		this.tipo = tipo;

		this.duracionSegundos = Math.max(0.1, tipo.getDuracionBaseSeg() * factorVida);
		this.vidaRestante = this.duracionSegundos;
		this.activa = true;
	}

	public void actualizar(final double dt) {
		if (!this.activa) {
			return;
		}

		this.vidaRestante -= dt;
		if (this.vidaRestante <= 0.0) {
			this.activa = false;
			return;
		}

		// 1. Físicas de movimiento
		this.posX += this.velX * dt;
		this.posY += this.velY * dt;

		// 2. Gravedad
		this.velY += this.tipo.getGravedad() * dt;

		// 3. Fricción del aire
		this.velX *= Math.pow(this.tipo.getFriccion(), dt * 60.0);
		this.velY *= Math.pow(this.tipo.getFriccion(), dt * 60.0);
	}

	/**
	 * Dibuja la partícula proyectada en el mundo con tamaño sólido e inconfundible.
	 */
	public void pintar(final Graphics2D g) {
		if (!this.activa) {
			return;
		}

		// Progreso de vida (1.0 = recién nacida, 0.0 = muriendo)
		final double progresoRestante = this.vidaRestante / this.duracionSegundos;

		// Interpolación lineal de tamaño (se va achicando a medida que muere)
		final float tamano = (float) ((this.tipo.getTamanoInicial() * progresoRestante)
				+ (this.tipo.getTamanoFinal() * (1.0 - progresoRestante)));
		final int lado = Math.max(2, Math.round(tamano));

		// Transición de color sólido (de color brillante inicial a color oscuro final)
		final Color colorActual = (progresoRestante > 0.5) ? this.tipo.getColorInicio() : this.tipo.getColorFin();

		// Coordenadas centradas de mundo
		final int renderX = (int) Math.round(this.posX) - (lado / 2);
		final int renderY = (int) Math.round(this.posY) - (lado / 2);

		// 1. Micro-sombra negra de fondo (hace que resalte 100% sobre cualquier tile)
		DibujoDebug.dibujarRectanguloRellenoRefCamara(g, renderX + 1, renderY + 1, lado, lado, Color.BLACK);

		// 2. Relleno frontal sólido brillante
		DibujoDebug.dibujarRectanguloRellenoRefCamara(g, renderX, renderY, lado, lado, colorActual);
	}

	public boolean isActiva() {
		return this.activa;
	}
}