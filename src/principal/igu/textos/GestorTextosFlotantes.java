package principal.igu.textos;

import java.awt.Graphics2D;
import java.util.Random;

import principal.utilidades.Globales;

/**
 * Gestor centralizado de textos flotantes de combate (Zero-GC / O(1)).
 * <p>
 * <b>Arquitectura de Memoria:</b>
 * <ul>
 * <li><b>Pool Circular de 64 Elementos:</b> Todas las instancias de
 * {@link TextoFlotante} se reservan en el inicio del juego. 0 llamadas a
 * {@code new} en caliente durante el combate.</li>
 * <li><b>Adquisición Instantánea O(1):</b> Si los 64 slots se llenan por un
 * ataque de área masivo, recicla el texto más antiguo mediante un puntero
 * circular sin bloquear el hilo.</li>
 * <li><b>Eliminación por Swap-and-Pop:</b> Los textos terminados se retiran de
 * la lista activa en 1 ciclo de reloj.</li>
 * </ul>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 1.0
 */
public class GestorTextosFlotantes {

	private static final int CAPACIDAD_MAXIMA = 64;

	/** Pool maestro pre-asignado en memoria. */
	private final TextoFlotante[] pool;

	/** Arreglo contiguo de textos activos para iteración ultra-rápida. */
	private final TextoFlotante[] activos;
	private int cantidadActivos;

	/** Puntero circular para adquisición sin búsqueda lineal. */
	private int punteroCircular;

	/** Generador aleatorio pre-asignado para dispersión lateral. */
	private final Random random;

	public GestorTextosFlotantes() {
		this.pool = new TextoFlotante[CAPACIDAD_MAXIMA];
		this.activos = new TextoFlotante[CAPACIDAD_MAXIMA];
		this.cantidadActivos = 0;
		this.punteroCircular = 0;
		this.random = new Random();

		// Pre-instanciación única al arrancar el motor
		for (int i = 0; i < CAPACIDAD_MAXIMA; i++) {
			this.pool[i] = new TextoFlotante();
		}
	}

	// =========================================================================
	// === MÉTODOS DE DISPARO RÁPIDO (API PÚBLICA)
	// =========================================================================

	/**
	 * Genera un número de daño balístico sobre una posición del mundo.
	 *
	 * @param danio   Cantidad numérica de daño.
	 * @param x       Coordenada X del impacto en píxeles de mundo.
	 * @param y       Coordenada Y del impacto en píxeles de mundo.
	 * @param critico {@code true} para aplicar estilo visual crítico (más grande y
	 *                rojo).
	 */
	public void agregarDanio(final int danio, final double x, final double y, final boolean critico) {
		final TipoTextoFlotante tipo = critico ? TipoTextoFlotante.CRITICO : TipoTextoFlotante.DANIO_NORMAL;
		final String texto = (critico ? "¡" + danio + "!" : String.valueOf(danio));
		this.agregarTexto(texto, x, y, tipo);
	}

	/**
	 * Genera un número de curación verde sobre el objetivo.
	 *
	 * @param curacion Cantidad de puntos de vida recuperados.
	 * @param x        Coordenada X de mundo.
	 * @param y        Coordenada Y de mundo.
	 */
	public void agregarCuracion(final int curacion, final double x, final double y) {
		this.agregarTexto("+" + curacion, x, y, TipoTextoFlotante.CURACION);
	}

	/**
	 * Dispara un texto flotante con un estilo predefinido.
	 *
	 * @param texto Mensaje a mostrar.
	 * @param x     Coordenada X en píxeles de mundo.
	 * @param y     Coordenada Y en píxeles de mundo.
	 * @param tipo  Preset de color y animación.
	 */
	public void agregarTexto(final String texto, final double x, final double y, final TipoTextoFlotante tipo) {
		if ((texto == null) || texto.isEmpty()) {
			return;
		}

		// Dispersión horizontal aleatoria centrada [-1.0, +1.0)
		final double dispersion = (this.random.nextDouble() * 2.0) - 1.0;

		// 1. Obtenemos un slot del pool circular
		final TextoFlotante tf = this.pool[this.punteroCircular];
		this.punteroCircular = (this.punteroCircular + 1) % CAPACIDAD_MAXIMA;

		final boolean yaEstabaEnActivos = tf.isActivo();

		// 2. Activamos la partícula con sus nuevas físicas
		tf.activar(texto, x, y, tipo, dispersion);

		// 3. Si no estaba en la lista activa, la agregamos
		if (!yaEstabaEnActivos && (this.cantidadActivos < CAPACIDAD_MAXIMA)) {
			this.activos[this.cantidadActivos] = tf;
			this.cantidadActivos++;
		}
	}

	// =========================================================================
	// === CICLO DE VIDA (GAME LOOP)
	// =========================================================================

	/**
	 * Actualiza las físicas balísticas y descarta los textos expirados en O(1).
	 */
	public void actualizar() {
		final double dt = (Globales.delta > 0.0) ? Globales.delta : (1.0 / 60.0);

		int i = 0;
		while (i < this.cantidadActivos) {
			final TextoFlotante tf = this.activos[i];
			tf.actualizar(dt);

			if (tf.isActivo()) {
				i++;
			} else {
				// Eliminación Swap-and-Pop O(1)
				this.activos[i] = this.activos[this.cantidadActivos - 1];
				this.activos[this.cantidadActivos - 1] = null;
				this.cantidadActivos--;
			}
		}
	}

	/**
	 * Renderiza todos los textos activos sobre la capa de mundo.
	 *
	 * @param g Contexto gráfico {@link Graphics2D}.
	 */
	public void pintar(final Graphics2D g) {
		for (int i = 0; i < this.cantidadActivos; i++) {
			this.activos[i].pintar(g);
		}
	}

	/**
	 * Limpia y apaga todos los textos activos (ej: al cambiar de mapa).
	 */
	public void limpiar() {
		for (int i = 0; i < this.cantidadActivos; i++) {
			this.activos[i] = null;
		}
		this.cantidadActivos = 0;
		for (int i = 0; i < CAPACIDAD_MAXIMA; i++) {
			this.pool[i].actualizar(100.0); // Fuerza la desactivación
		}
	}

	public int getCantidadActivos() {
		return this.cantidadActivos;
	}
}