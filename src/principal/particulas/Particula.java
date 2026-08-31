package principal.particulas;

import java.awt.Color;
import java.awt.Graphics2D;

import principal.utilidades.Render2D;

/**
 * Representa una partícula física individual reutilizable en memoria (Zero-GC).
 * <p>
 * <b>Cinemática y Optimización:</b>
 * <ul>
 * <li><b>Integración de Euler Liviana:</b> Actualiza posición y velocidad
 * recibiendo la fricción pre-calculada desde el gestor, eliminando por completo
 * llamadas costosas a {@code Math.pow} por frame.</li>
 * <li><b>Encogimiento Progresivo (Pixel-Art Shrink):</b> Interpola su tamaño
 * entre {@link TipoParticula#getTamanoInicial()} y
 * {@link TipoParticula#getTamanoFinal()} en función de la vida restante.</li>
 * <li><b>Render Directo sin Overhead:</b> Dibuja sombra y cuerpo frontal con
 * cálculo de cámara local para maximizar el fill-rate en VRAM.</li>
 * </ul>
 * </p>
 * 
 * @version 3.0
 */
public class Particula {

	// =========================================================================
	// === 1. ESTADO Y TIPO
	// =========================================================================

	/** Indica si la partícula está viva y debe procesarse en el Game Loop. */
	private boolean activa;

	/** Preset que define la paleta de color, gravedad y tamaño de la partícula. */
	private TipoParticula tipo;

	// =========================================================================
	// === 2. CINEMÁTICA EN EL ESPACIO CONTINUO DEL MUNDO
	// =========================================================================

	/** Coordenada X actual en píxeles absolutos de mundo. */
	private double posX;

	/** Coordenada Y actual en píxeles absolutos de mundo. */
	private double posY;

	/** Velocidad horizontal en píxeles por segundo (px/s). */
	private double velX;

	/** Velocidad vertical en píxeles por segundo (px/s). */
	private double velY;

	// =========================================================================
	// === 3. TEMPORIZACIÓN Y VIDA
	// =========================================================================

	/** Tiempo total de vida asignado a esta partícula en segundos. */
	private double duracionSegundos;

	/** Tiempo de vida restante en segundos (decae hasta 0.0). */
	private double vidaRestante;

	// =========================================================================
	// === CONSTRUCTOR
	// =========================================================================

	/**
	 * Crea la partícula en estado inactivo dentro del pool de memoria.
	 */
	public Particula() {
		this.activa = false;
	}

	// =========================================================================
	// === INICIALIZACIÓN / SPAWN (CERO ASIGNACIONES EN RUNTIME)
	// =========================================================================

	/**
	 * Activa y reinicia las propiedades cinemáticas de la partícula.
	 *
	 * @param x          Coordenada X inicial de emisión en píxeles de mundo.
	 * @param y          Coordenada Y inicial de emisión en píxeles de mundo.
	 * @param velX       Velocidad horizontal inicial en px/s.
	 * @param velY       Velocidad vertical inicial en px/s.
	 * @param tipo       Preset de comportamiento físico y visual.
	 * @param factorVida Multiplicador de duración (ej: 0.8 a 1.3 para variedad
	 *                   orgánica).
	 */
	public void spawn(final double x, final double y, final double velX, final double velY, final TipoParticula tipo,
			final double factorVida) {
		this.posX = x;
		this.posY = y;
		this.velX = velX;
		this.velY = velY;
		this.tipo = tipo;

		this.duracionSegundos = Math.max(0.05, tipo.getDuracionBaseSeg() * factorVida);
		this.vidaRestante = this.duracionSegundos;
		this.activa = true;
	}

	// =========================================================================
	// === ACTUALIZACIÓN FÍSICA (60 APS)
	// =========================================================================

	/**
	 * Avanza la trayectoria balística aplicando gravedad y fricción precalculada.
	 *
	 * @param dt             Delta de tiempo en segundos (1/60 s).
	 * @param factorFriccion Coeficiente de frenado pre-ajustado al framerate
	 *                       (Zero-Math.pow).
	 */
	public void actualizar(final double dt, final double factorFriccion) {
		if (!this.activa) {
			return;
		}

		this.vidaRestante -= dt;

		if (this.vidaRestante <= 0.0) {
			this.activa = false;
			return;
		}

		// 1. Integración de posición
		this.posX += this.velX * dt;
		this.posY += this.velY * dt;

		// 2. Aplicación de gravedad
		this.velY += this.tipo.getGravedad() * dt;

		// 3. Resistencia del aire de bajo costo computacional
		this.velX *= factorFriccion;
		this.velY *= factorFriccion;
	}

	// =========================================================================
	// === RENDERIZADO PIXEL-ART (ZERO-GC / ALTO RENDIMIENTO)
	// =========================================================================

	/**
	 * Dibuja la partícula proyectada en el contexto gráfico utilizando offsets de
	 * cámara pre-calculados.
	 *
	 * @param g          Contexto gráfico {@link Graphics2D}.
	 * @param camOffsetX Desplazamiento X consolidado de la cámara.
	 * @param camOffsetY Desplazamiento Y consolidado de la cámara.
	 */
	public void pintar(final Graphics2D g, final int camOffsetX, final int camOffsetY) {
		if (!this.activa) {
			return;
		}

		// 1. Progreso normalizado de vida (1.0 = nacimiento, 0.0 = muerte)
		final double progresoRestante = this.vidaRestante / this.duracionSegundos;

		// 2. Tamaño interpolado
		final float tamano = (float) ((this.tipo.getTamanoInicial() * progresoRestante)
				+ (this.tipo.getTamanoFinal() * (1.0 - progresoRestante)));
		final int lado = Math.max(2, Math.round(tamano));

		// 3. Alternancia de color pre-instanciada (Zero-GC)
		final Color colorActual = (progresoRestante > 0.5) ? this.tipo.getColorInicio() : this.tipo.getColorFin();

		// 4. Coordenadas de pantalla relativas a cámara
		final int screenX = (int) Math.round(this.posX) - camOffsetX - (lado / 2);
		final int screenY = (int) Math.round(this.posY) - camOffsetY - (lado / 2);

		// 5.1 Sombra negra de contraste pixel-art (+1, +1 px)
		Render2D.dibujarRectanguloRelleno(g, screenX + 1, screenY + 1, lado, lado, Color.BLACK);

		// 5.2 Relleno frontal sólido
		Render2D.dibujarRectanguloRelleno(g, screenX, screenY, lado, lado, colorActual);
	}

	// =========================================================================
	// === GETTERS Y SETTERS
	// =========================================================================

	public void desactivar() {
		this.activa = false;
	}

	public boolean isActiva() {
		return this.activa;
	}

	public double getPosX() {
		return this.posX;
	}

	public double getPosY() {
		return this.posY;
	}

	public double getVelX() {
		return this.velX;
	}

	public double getVelY() {
		return this.velY;
	}

	public TipoParticula getTipo() {
		return this.tipo;
	}
}