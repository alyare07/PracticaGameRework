package principal.particulas;

import java.awt.Color;
import java.awt.Graphics2D;

import principal.utilidades.DibujoDebug;

/**
 * Representa una partícula física individual reutilizable en memoria (Zero-GC).
 * <p>
 * <b>Cinemática y Comportamiento Visual:</b>
 * <ul>
 * <li><b>Integración de Euler Clásica:</b> Actualiza su posición y velocidad en
 * cada tick aplicando gravedad y desaceleración por fricción del aire.</li>
 * <li><b>Fricción Independiente del Framerate:</b> Utiliza
 * {@code Math.pow(friccion, dt * 60.0)} para que el frenado del aire sea
 * idéntico tanto a 60 FPS como a tasas de refresco desbloqueadas (144 Hz / 240
 * Hz).</li>
 * <li><b>Encogimiento Progresivo (Pixel-Art Shrink):</b> Interpola su tamaño
 * desde {@link TipoParticula#getTamanoInicial()} hasta
 * {@link TipoParticula#getTamanoFinal()} a medida que expira su tiempo de
 * vida.</li>
 * <li><b>Sombra Dura de Alto Contraste:</b> Dibuja un micro-rectángulo negro
 * desplazado (+1, +1 px) debajo del color principal para que la partícula
 * resalte con nitidez sobre cualquier textura del mapa (pasto, agua, nieve o
 * roca).</li>
 * </ul>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.0
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

		this.duracionSegundos = Math.max(0.1, tipo.getDuracionBaseSeg() * factorVida);
		this.vidaRestante = this.duracionSegundos;
		this.activa = true;
	}

	// =========================================================================
	// === ACTUALIZACIÓN FÍSICA (60 APS)
	// =========================================================================

	/**
	 * Avanza la trayectoria balística de la partícula aplicando gravedad y
	 * fricción.
	 *
	 * @param dt Delta de tiempo en segundos transcurrido desde el último frame
	 *           (1/60 s).
	 */
	public void actualizar(final double dt) {
		if (!this.activa) {
			return;
		}

		this.vidaRestante -= dt;

		// Si el tiempo expiró, se desactiva y queda lista para reciclarse
		if (this.vidaRestante <= 0.0) {
			this.activa = false;
			return;
		}

		/*
		 * =====================================================================
		 * EXPLICACIÓN DIDÁCTICA: INTEGRACIÓN DE EULER Y FRICCIÓN
		 * --------------------------------------------------------------------- 1.
		 * TRASLACIÓN DE POSICIÓN: Posición += Velocidad * dt
		 * 
		 * 2. ACELERACIÓN DE GRAVEDAD: VelocidadY += Gravedad * dt - Si gravedad > 0
		 * (ej. sangre): la partícula cae más rápido hacia el suelo (+Y). - Si gravedad
		 * < 0 (ej. humo o fuego): la partícula flota hacia arriba (-Y).
		 * 
		 * 3. FRICCIÓN DEL AIRE (Math.pow(friccion, dt * 60.0)): En lugar de multiplicar
		 * fijamente por 0.9, elevar la fricción a (dt * 60) garantiza que la partícula
		 * frene exactamente a la misma distancia si el juego corre a 60 FPS, 144 FPS o
		 * si sufre una caída temporal de rendimiento.
		 * =====================================================================
		 */
		// 1. Integración de posición espacial
		this.posX += this.velX * dt;
		this.posY += this.velY * dt;

		// 2. Aplicación de gravedad vertical
		this.velY += this.tipo.getGravedad() * dt;

		// 3. Resistencia del aire
		final double factorFriccion = Math.pow(this.tipo.getFriccion(), dt * 60.0);
		this.velX *= factorFriccion;
		this.velY *= factorFriccion;
	}

	// =========================================================================
	// === RENDERIZADO PIXEL-ART (ZERO-GC)
	// =========================================================================

	/**
	 * Dibuja la partícula proyectada en el mundo con escala, sombra y color
	 * dinámico.
	 *
	 * @param g Contexto gráfico {@link Graphics2D}.
	 */
	public void pintar(final Graphics2D g) {
		if (!this.activa) {
			return;
		}

		/*
		 * =====================================================================
		 * EXPLICACIÓN DIDÁCTICA: PROGRESO Y ENCOGIMIENTO PIXEL-ART
		 * --------------------------------------------------------------------- 1.
		 * PROGRESO NORMALIZADO (1.0 a 0.0): - Recién nacida (vidaRestante = duracion):
		 * progresoRestante = 1.0 - A mitad de vida: progresoRestante = 0.5 - Muriendo
		 * (vidaRestante = 0): progresoRestante = 0.0
		 * 
		 * 2. INTERPOLACIÓN LINEAL DE TAMAÑO (Lerp): tamano = (tamanoInicial * progreso)
		 * + (tamanoFinal * (1.0 - progreso)) Hace que una chispa nazca grande (7 px) y
		 * se encoja suavemente a 1 px antes de desaparecer, creando una animación
		 * fluida sin sprites pesados.
		 * 
		 * 3. SOMBRA DE CONTRASTE: Dibujar primero un cuadrito negro desplazado (+1, +1)
		 * crea una sombra dura que hace que el color brillante frontal resalte sobre
		 * cualquier tile.
		 * =====================================================================
		 */
		// 1. Cálculo de vida normalizada
		final double progresoRestante = this.vidaRestante / this.duracionSegundos;

		// 2. Tamaño interpolado
		final float tamano = (float) ((this.tipo.getTamanoInicial() * progresoRestante)
				+ (this.tipo.getTamanoFinal() * (1.0 - progresoRestante)));
		final int lado = Math.max(2, Math.round(tamano));

		// 3. Transición de color sólido sin instanciar 'new Color()'
		final Color colorActual = (progresoRestante > 0.5) ? this.tipo.getColorInicio() : this.tipo.getColorFin();

		// 4. Coordenadas centradas en el punto medio de la partícula
		final int renderX = (int) Math.round(this.posX) - (lado / 2);
		final int renderY = (int) Math.round(this.posY) - (lado / 2);

		// 5.1 Sombra negra de contraste pixel-art (+1, +1 px)
		DibujoDebug.dibujarRectanguloRellenoRefCamara(g, renderX + 1, renderY + 1, lado, lado, Color.BLACK);

		// 5.2 Relleno frontal sólido brillante
		DibujoDebug.dibujarRectanguloRellenoRefCamara(g, renderX, renderY, lado, lado, colorActual);
	}

	// =========================================================================
	// === GETTERS
	// =========================================================================

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