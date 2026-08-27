package principal.particulas;

import java.awt.Graphics2D;
import java.util.Random;

import principal.utilidades.Globales;

/**
 * Gestor centralizado del sistema de partículas 2D de ultra-alto rendimiento
 * (Zero-GC / O(1)).
 * <p>
 * <b>Pilares de Arquitectura y Rendimiento:</b>
 * <ul>
 * <li><b>Pool Circular Pre-Asignado (2.048 Partículas):</b> Todas las
 * partículas se crean una sola vez al arrancar el motor. Durante el juego, los
 * emisores no hacen {@code new Particula()}, sino que reciclan las casillas
 * existentes en memoria continua.</li>
 * <li><b>Eliminación Instantánea Swap-and-Pop O(1):</b> Las partículas que
 * mueren se retiran de la lista activa en un solo ciclo de CPU reemplazando su
 * posición por la última partícula viva, evitando los costosos corrimientos de
 * memoria de las listas tradicionales.</li>
 * <li><b>Dispersión Balística en Coordenadas Polares:</b> Transforma ángulos
 * aleatorios y velocidades escalares a vectores cartesianos ($V_x =
 * \cos(\theta) \cdot V$, $V_y = \sin(\theta) \cdot V$).</li>
 * <li><b>Iteración Lineal sobre Arreglo Denso:</b> {@link #actualizar()} y
 * {@link #pintar(Graphics2D)} recorren un arreglo contiguo de punteros,
 * aprovechando al máximo la memoria caché L1/L2 de la CPU.</li>
 * </ul>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public class GestorParticulas {

	// =========================================================================
	// === 1. CAPACIDAD Y ARREGLOS DE MEMORIA (ZERO-GC)
	// =========================================================================

	/** Capacidad máxima de partículas físicas simultáneas en el mundo. */
	private static final int CAPACIDAD_MAXIMA = 2048;

	/** Pool maestro de instancias pre-asignadas en memoria fija. */
	private final Particula[] pool;

	/**
	 * Arreglo denso y contiguo que contiene únicamente las partículas actualmente
	 * vivas.
	 */
	private final Particula[] activas;

	/** Contador de partículas activas en el fotograma actual. */
	private int cantidadActivas;

	/** Puntero circular para reciclaje y asignación instantánea en O(1). */
	private int punteroCircular;

	/** Generador pseudo-aleatorio pre-asignado para evitar basura en el Heap. */
	private final Random random;

	// =========================================================================
	// === CONSTRUCTOR: RESERVA DE MEMORIA
	// =========================================================================

	/**
	 * Inicializa el gestor y reserva los 2.048 objetos de partículas en memoria.
	 */
	public GestorParticulas() {
		this.pool = new Particula[CAPACIDAD_MAXIMA];
		this.activas = new Particula[CAPACIDAD_MAXIMA];
		this.cantidadActivas = 0;
		this.punteroCircular = 0;
		this.random = new Random();

		// Pre-instanciación única en el arranque del juego
		for (int i = 0; i < CAPACIDAD_MAXIMA; i++) {
			this.pool[i] = new Particula();
		}
	}

	// =========================================================================
	// === EMISORES PRE-CALIBRADOS (API PÚBLICA DE GAMEPLAY)
	// =========================================================================

	/*
	 * =========================================================================
	 * EXPLICACIÓN DIDÁCTICA: ¿CÓMO SE DISPERSA UNA EXPLOSIÓN CIRCULAR?
	 * -------------------------------------------------------------------------
	 * Para que una explosión salga disparada en todas las direcciones (360°):
	 * 
	 * 1. ÁNGULO ALEATORIO EN RADIANES: - Math.random() * (2 * π) nos da un ángulo
	 * 'θ' en cualquier dirección (0 a 360°).
	 * 
	 * 2. CONVERSIÓN DE POLAR A CARTESIANO: - vx = cos(θ) * velocidad - vy = sin(θ)
	 * * velocidad
	 * 
	 * 3. MEZCLA DE TIPOS: El 70% de las partículas son chispas de fuego brillantes
	 * que suben, y el 30% son nubes de humo que se expanden lentamente.
	 * =========================================================================
	 */
	/**
	 * Emite una explosión radial masiva de fuego y humo en el mundo.
	 *
	 * @param x        Coordenada X del centro de la detonación en píxeles de mundo.
	 * @param y        Coordenada Y del centro de la detonación en píxeles de mundo.
	 * @param cantidad Cantidad de partículas a disparar (ej: 30 a 60).
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
	 * Emite salpicaduras direccionales de sangre al asestar un golpe crítico o
	 * recibir daño.
	 *
	 * @param x        Coordenada X del impacto.
	 * @param y        Coordenada Y del impacto.
	 * @param dirX     Vector horizontal del golpe (-1 a +1).
	 * @param dirY     Vector vertical del golpe (-1 a +1).
	 * @param cantidad Cantidad de gotas de sangre a emitir (ej: 15 a 25).
	 */
	public void emitirSangre(final double x, final double y, final double dirX, final double dirY, final int cantidad) {
		for (int i = 0; i < cantidad; i++) {
			// Dispersión perpendicular al corte
			final double dispersion = (this.random.nextDouble() * 1.6) - 0.8;
			final double velocidad = 50.0 + (this.random.nextDouble() * 120.0);

			final double vx = (dirX * velocidad) + (dispersion * 50.0);
			final double vy = (dirY * velocidad) - (40.0 + (this.random.nextDouble() * 80.0)); // Salto hacia arriba

			this.spawnParticula(x, y, vx, vy, TipoParticula.SANGRE, 0.8 + (this.random.nextDouble() * 0.5));
		}
	}

	/**
	 * Emite pequeñas nubes de polvo en el suelo al caminar, correr o esquivar
	 * (Dash).
	 *
	 * @param x        Coordenada X en los pies del personaje.
	 * @param y        Coordenada Y en los pies del personaje.
	 * @param cantidad Cantidad de partículas de tierra (ej: 3 a 6).
	 */
	public void emitirPolvoPaso(final double x, final double y, final int cantidad) {
		for (int i = 0; i < cantidad; i++) {
			final double vx = (this.random.nextDouble() * 30.0) - 15.0;
			final double vy = -(this.random.nextDouble() * 25.0); // Flotan suavemente

			this.spawnParticula(x, y, vx, vy, TipoParticula.POLVO_TIERRA, 0.7 + (this.random.nextDouble() * 0.4));
		}
	}

	/**
	 * Emite destellos mágicos arcanos (pociones, auras, teletransporte o conjuros).
	 *
	 * @param x        Coordenada X del foco mágico.
	 * @param y        Coordenada Y del foco mágico.
	 * @param cantidad Cantidad de chispas arcanas (ej: 10 a 20).
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

	// =========================================================================
	// === GESTIÓN DE POOL Y ASIGNACIÓN O(1)
	// =========================================================================

	/**
	 * Extrae una partícula del pool circular, reinicia sus físicas y la incorpora a
	 * la lista activa.
	 *
	 * @param x          Coordenada X de spawn.
	 * @param y          Coordenada Y de spawn.
	 * @param vx         Velocidad horizontal inicial.
	 * @param vy         Velocidad vertical inicial.
	 * @param tipo       Preset de partícula.
	 * @param factorVida Multiplicador de duración.
	 */
	public void spawnParticula(final double x, final double y, final double vx, final double vy,
			final TipoParticula tipo, final double factorVida) {
		// 1. Obtenemos la siguiente casilla libre mediante puntero circular
		final Particula p = this.pool[this.punteroCircular];
		this.punteroCircular = (this.punteroCircular + 1) % CAPACIDAD_MAXIMA;

		final boolean yaEstabaActiva = p.isActiva();

		// 2. Reseteamos sus físicas en el lugar
		p.spawn(x, y, vx, vy, tipo, factorVida);

		// 3. Si no estaba en el arreglo denso activo, la incorporamos
		if (!yaEstabaActiva && (this.cantidadActivas < CAPACIDAD_MAXIMA)) {
			this.activas[this.cantidadActivas] = p;
			this.cantidadActivas++;
		}
	}

	// =========================================================================
	// === CICLO LÓGICO Y RENDERIZADO (60 APS)
	// =========================================================================

	/**
	 * Actualiza la cinemática de todas las partículas activas y descarta las
	 * finalizadas en tiempo constante $O(1)$ mediante Swap-and-Pop.
	 */
	public void actualizar() {
		final double dt = (Globales.delta > 0.0) ? Globales.delta : (1.0 / 60.0);

		int i = 0;
		while (i < this.cantidadActivas) {
			final Particula p = this.activas[i];
			p.actualizar(dt);

			if (p.isActiva()) {
				i++;
			} else {
				/*
				 * ============================================================= EXPLICACIÓN
				 * DIDÁCTICA: SWAP-AND-POP EN PARTÍCULAS
				 * ------------------------------------------------------------- Cuando una
				 * partícula muere: 1. Tomamos la ÚLTIMA partícula viva del arreglo:
				 * activas[cantidadActivas - 1]. 2. La movemos a la posición 'i' que acaba de
				 * quedar libre. 3. Ponemos null en la última posición y restamos 1 a
				 * 'cantidadActivas'. 4. NO incrementamos 'i', para evaluar en la siguiente
				 * vuelta la partícula que acabamos de mover.
				 * 
				 * Resultado: 0 corrimientos de memoria y 0 llamadas al Garbage Collector.
				 * =============================================================
				 */
				this.activas[i] = this.activas[this.cantidadActivas - 1];
				this.activas[this.cantidadActivas - 1] = null;
				this.cantidadActivas--;
			}
		}
	}

	/**
	 * Renderiza todas las partículas activas proyectadas en el mundo.
	 *
	 * @param g Contexto gráfico {@link Graphics2D}.
	 */
	public void pintar(final Graphics2D g) {
		for (int i = 0; i < this.cantidadActivas; i++) {
			this.activas[i].pintar(g);
		}
	}

	/**
	 * Desactiva y apaga todas las partículas activas (ej: al cambiar de mapa).
	 */
	public void limpiar() {
		for (int i = 0; i < this.cantidadActivas; i++) {
			this.activas[i] = null;
		}
		this.cantidadActivas = 0;
	}

	// =========================================================================
	// === GETTERS
	// =========================================================================

	public int getCantidadActivas() {
		return this.cantidadActivas;
	}
}