package principal.iluminacion;

import java.awt.Color;
import java.awt.Rectangle;

/**
 * Representa una zona espacial rectangular en el mapa (Bioma o Cueva) que
 * modula la atmósfera de la pantalla con una transición suave y progresiva al
 * entrar en ella.
 * <p>
 * <b>Comportamientos Soportados:</b>
 * <ul>
 * <li><b>Bioma Exterior (ej. Pantano, Cementerio):</b> Respeta el reloj solar
 * de 24 horas, modulando únicamente el tinte de la luz y la niebla sin apagar
 * el ciclo día/noche.</li>
 * <li><b>Interior / Cueva (ej. Mazmorra, Mina):</b> Bloquea la luz solar
 * exterior y fija una oscuridad permanente.</li>
 * <li><b>Inmersión Progresiva:</b> Mantiene una variable escalar
 * {@link #factorInmersion} ($0.0 \dots 1.0$) calculada por
 * {@code GestorZonasAmbiente} para que la atmósfera te envuelva suavemente al
 * cruzar el umbral.</li>
 * </ul>
 * </p>
 * 
 * @version 4.0
 */
public class ZonaAmbiente {

	// =========================================================================
	// === 1. LÍMITES ESPACIALES Y CONFIGURACIÓN
	// =========================================================================

	/**
	 * Límites rectangulares de la zona en coordenadas absolutas de píxeles de
	 * mundo.
	 */
	private final Rectangle limites;

	/**
	 * Color de tinte ambiental para exteriores o color de oscuridad para
	 * interiores.
	 */
	private final Color colorAmbiente;

	/** Nivel de densidad de niebla asociado a este volumen. */
	private final IntensidadNiebla nivelNiebla;

	/** Nombre descriptivo o identificador de la zona (ej: "Cementerio Maldito"). */
	private final String nombre;

	/**
	 * Indica si es un espacio subterráneo/interior que bloquea la luz solar
	 * exterior.
	 */
	private final boolean esInterior;

	/** Interruptor de activación de la zona. */
	private boolean activa;

	/*
	 * =========================================================================
	 * EXPLICACIÓN DIDÁCTICA: FACTOR DE INMERSIÓN PROGRESIVA (LERP)
	 * ------------------------------------------------------------------------- En
	 * lugar de que la pantalla cambie de color de golpe en un solo fotograma apenas
	 * el jugador toca el borde del rectángulo:
	 *
	 * 1. 'factorInmersion = 0.0': El jugador está fuera (0% de efecto de la zona).
	 * 2. 'factorInmersion = 0.5': El jugador está a mitad de camino (50% de niebla
	 * y tinte). 3. 'factorInmersion = 1.0': El jugador está completamente dentro
	 * (100% inmersión).
	 *
	 * Al usar este valor para interpolar los colores, la transición visual se
	 * siente natural, suave y cinematográfica.
	 * =========================================================================
	 */
	private double factorInmersion = 0.0;

	// =========================================================================
	// === 2. CONSTRUCTORES
	// =========================================================================

	/**
	 * Constructor para Biomas Exteriores (por defecto {@code esInterior = false}).
	 * <p>
	 * Respeta el ciclo natural de 24 horas y aplica el tinte y la niebla sobre la
	 * luz del sol/luna.
	 * </p>
	 *
	 * @param x             Coordenada X inicial en píxeles de mundo.
	 * @param y             Coordenada Y inicial en píxeles de mundo.
	 * @param ancho         Ancho de la zona en píxeles.
	 * @param alto          Alto de la zona en píxeles.
	 * @param colorAmbiente Color de tinte que teñirá el ambiente (ej. morado para
	 *                      cementerios).
	 * @param nivelNiebla   Densidad de niebla deseada en la zona.
	 * @param nombre        Nombre identificador del área.
	 */
	public ZonaAmbiente(final int x, final int y, final int ancho, final int alto, final Color colorAmbiente,
			final IntensidadNiebla nivelNiebla, final String nombre) {
		this(x, y, ancho, alto, colorAmbiente, nivelNiebla, nombre, false);
	}

	/**
	 * Constructor general con discriminación de espacios interiores/cuevas.
	 *
	 * @param x             Coordenada X inicial en píxeles de mundo.
	 * @param y             Coordenada Y inicial en píxeles de mundo.
	 * @param ancho         Ancho de la zona en píxeles.
	 * @param alto          Alto de la zona en píxeles.
	 * @param colorAmbiente Color ambiental asignado.
	 * @param nivelNiebla   Densidad de niebla asignada.
	 * @param nombre        Nombre identificador del área.
	 * @param esInterior    {@code true} si es cueva o mazmorra (bloquea el sol
	 *                      exterior), {@code false} si es un bioma exterior al aire
	 *                      libre.
	 */
	public ZonaAmbiente(final int x, final int y, final int ancho, final int alto, final Color colorAmbiente,
			final IntensidadNiebla nivelNiebla, final String nombre, final boolean esInterior) {
		this.limites = new Rectangle(x, y, ancho, alto);
		this.colorAmbiente = (colorAmbiente != null) ? colorAmbiente : new Color(0, 0, 0, 255);
		this.nivelNiebla = (nivelNiebla != null) ? nivelNiebla : IntensidadNiebla.DESACTIVADA;
		this.nombre = nombre;
		this.esInterior = esInterior;
		this.activa = true;
	}

	// =========================================================================
	// === 3. MÉTODOS ESPACIALES Y CONSULTAS (ZERO-GC)
	// =========================================================================

	/*
	 * =========================================================================
	 * EXPLICACIÓN DIDÁCTICA: COLISIÓN PUNTO-RECTÁNGULO EN TIEMPO O(1)
	 * -------------------------------------------------------------------------
	 * Este método evalúa si la posición (X, Y) del jugador está dentro del
	 * rectángulo de la zona.
	 *
	 * El método 'limites.contains(px, py)' simplemente realiza 4 comparaciones
	 * numéricas primitivas: (px >= x && py >= y && px < x + ancho && py < y + alto)
	 *
	 * Es ultra-rápido, consume 0 nanosegundos de procesamiento y no crea ningún
	 * objeto en memoria Heap (Zero-GC estricto).
	 * =========================================================================
	 */
	/**
	 * Evalúa en tiempo constante $O(1)$ si una coordenada puntual del mundo (ej. la
	 * posición del jugador) se encuentra contenida dentro de este volumen.
	 *
	 * @param px Coordenada X puntual en píxeles de mundo.
	 * @param py Coordenada Y puntual en píxeles de mundo.
	 * @return {@code true} si el punto colisiona con el área y la zona está activa.
	 */
	public boolean contiene(final double px, final double py) {
		return this.activa && this.limites.contains(px, py);
	}

	// =========================================================================
	// === 4. GETTERS Y SETTERS
	// =========================================================================

	public Rectangle getLimites() {
		return this.limites;
	}

	public Color getColorAmbiente() {
		return this.colorAmbiente;
	}

	public IntensidadNiebla getNivelNiebla() {
		return this.nivelNiebla;
	}

	public String getNombre() {
		return this.nombre;
	}

	public boolean isEsInterior() {
		return this.esInterior;
	}

	public boolean isActiva() {
		return this.activa;
	}

	public void setActiva(final boolean activa) {
		this.activa = activa;
	}

	public double getFactorInmersion() {
		return this.factorInmersion;
	}

	/**
	 * Establece el factor de inmersión actual, acotándolo estrictamente entre $0.0$
	 * y $1.0$.
	 *
	 * @param factor Valor escalar de inmersión.
	 */
	public void setFactorInmersion(final double factor) {
		this.factorInmersion = Math.max(0.0, Math.min(1.0, factor));
	}
}