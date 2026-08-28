package principal.clima;

/**
 * Catálogo de perfiles climáticos por bioma que define las tablas de
 * probabilidad y matrices de transición meteorológica (Cadenas de Markov) para
 * el mundo del juego.
 * <p>
 * <b>Rol Arquitectónico y Diseño:</b>
 * <ul>
 * <li><b>Matriz de Transición de Estados (Markov):</b> Garantiza que el clima
 * evolucione de forma realista según el bioma (ej. una tormenta eléctrica en el
 * bosque templado siempre amaina a llovizna antes de salir el sol, o un
 * desierto solo genera tormentas de arena).</li>
 * <li><b>Termodinámica Base:</b> Establece la temperatura promedio
 * ($^\circ\text{C}$) y la humedad relativa ($0.0 \dots 1.0$) de referencia para
 * los cálculos barométricos de {@code GestorClima}.</li>
 * <li><b>Zero-GC Estricto:</b> Todas las decisiones se resuelven mediante
 * comparaciones primitivas directas en tiempo constante $O(1)$ sin generar
 * objetos en el Heap.</li>
 * </ul>
 * </p>
 * 
 * @version 2.0
 */
public enum PerfilClima {

	// =========================================================================
	// === 1. PERFILES CLIMÁTICOS DE BIOMA
	// =========================================================================

	/**
	 * Bosques, llanuras templadas, praderas y aldeas estándar (18°C / 50% humedad).
	 */
	TEMPLADO_BOSQUE("Bosque Templado", 18.0, 0.50),

	/** Desiertos áridos, dunas y cañones secos (34°C / 15% humedad). */
	DESIERTO_CALIDO("Desierto", 34.0, 0.15),

	/** Picos montañosos, glaciares y tundra helada (-4°C / 80% humedad). */
	MONTANA_NEVADA("Montaña Helada", -4.0, 0.80),

	/** Ciénagas venenosas, pantanos y marismas húmedas (22°C / 90% humedad). */
	PANTANO_HUMEDO("Pantano Húmedo", 22.0, 0.90),

	/** Tierras calcinadas, cráteres y zonas volcánicas (40°C / 20% humedad). */
	VOLCANICO("Tierras Volcánicas", 40.0, 0.20),

	/**
	 * Arboledas místicas, bosques de hadas y reinos élficos (20°C / 70% humedad).
	 */
	BOSQUE_MISTICO("Bosque Místico", 20.0, 0.70);

	// =========================================================================
	// === 2. ATRIBUTOS INMUTABLES
	// =========================================================================

	/** Nombre descriptivo del bioma para interfaces o reportes meteorológicos. */
	private final String nombreVisible;

	/** Temperatura promedio de referencia en grados Celsius (°C). */
	private final double temperaturaBase;

	/**
	 * Humedad relativa promedio de referencia (0.0 = seco absoluto, 1.0 =
	 * saturación).
	 */
	private final double humedadBase;

	/**
	 * Constructor interno del enum para definir las propiedades base del bioma.
	 *
	 * @param nombreVisible   Nombre legible del bioma.
	 * @param temperaturaBase Temperatura promedio en °C.
	 * @param humedadBase     Humedad relativa promedio (0.0 a 1.0).
	 */
	PerfilClima(final String nombreVisible, final double temperaturaBase, final double humedadBase) {
		this.nombreVisible = nombreVisible;
		this.temperaturaBase = temperaturaBase;
		this.humedadBase = humedadBase;
	}

	// =========================================================================
	// === 3. MOTOR DE TRANSICIÓN PROBABILÍSTICA (CADENAS DE MARKOV)
	// =========================================================================

	/*
	 * =========================================================================
	 * EXPLICACIÓN DIDÁCTICA: ¿CÓMO FUNCIONA LA SELECCIÓN PONDERADA POR RANGOS?
	 * -------------------------------------------------------------------------
	 * 'Math.random()' genera un número decimal aleatorio entre 0.00 y 1.00. Para
	 * repartir probabilidades sin crear listas ni objetos:
	 *
	 * Ejemplo en TEMPLADO_BOSQUE desde DESPEJADO: 1. Si azar < 0.35 -> 35% de
	 * probabilidad de VENTOSO (Rango: 0.00 a 0.35). 2. Si azar < 0.65 -> 30% de
	 * probabilidad de LLUVIA_LEVE (Rango: 0.35 a 0.65 -> 0.65 - 0.35 = 0.30). 3. Si
	 * azar < 0.85 -> 20% de probabilidad de PETALOS_CEREZO(Rango: 0.65 a 0.85 ->
	 * 0.85 - 0.65 = 0.20). 4. De lo contrario -> 15% restante para NIEBLA_CERRADA
	 * (Rango: 0.85 a 1.00 -> 1.00 - 0.85 = 0.15).
	 *
	 * Esto garantiza una simulación meteorológica orgánica, lógica y con 0% de
	 * basura en memoria.
	 * =========================================================================
	 */
	/**
	 * Determina cuál será el próximo clima lógico en la simulación evaluando el
	 * clima actual mediante la matriz de probabilidades específica del bioma.
	 *
	 * @param actual Estado meteorológico que se encuentra activo en el momento.
	 * @return Próximo {@link TipoClima} seleccionado para el pronóstico.
	 */
	public TipoClima calcularSiguienteClima(final TipoClima actual) {
		final double azar = Math.random();

		switch (this) {
		case DESIERTO_CALIDO:
			if (actual == TipoClima.DESPEJADO) {
				return (azar < 0.25) ? TipoClima.VENTOSO
						: ((azar < 0.40) ? TipoClima.TORMENTA_ARENA : TipoClima.DESPEJADO);
			}
			if (actual == TipoClima.VENTOSO) {
				return (azar < 0.60) ? TipoClima.TORMENTA_ARENA : TipoClima.DESPEJADO;
			}
			return TipoClima.DESPEJADO;

		case MONTANA_NEVADA:
			if (actual == TipoClima.DESPEJADO) {
				return (azar < 0.65) ? TipoClima.NIEVE : TipoClima.VENTOSO;
			}
			if (actual == TipoClima.NIEVE) {
				return (azar < 0.35) ? TipoClima.VENTISCA : TipoClima.DESPEJADO;
			}
			if (actual == TipoClima.VENTISCA) {
				// Una ventisca helada siempre calma a nieve suave antes de despejar por
				// completo
				return TipoClima.NIEVE;
			}
			return TipoClima.NIEVE;

		case PANTANO_HUMEDO:
			if (actual == TipoClima.DESPEJADO) {
				return (azar < 0.45) ? TipoClima.NIEBLA_CERRADA : TipoClima.LLUVIA_LEVE;
			}
			if (actual == TipoClima.LLUVIA_LEVE) {
				return (azar < 0.40) ? TipoClima.LLUVIA_ACIDA : TipoClima.NIEBLA_CERRADA;
			}
			if (actual == TipoClima.NIEBLA_CERRADA) {
				return (azar < 0.50) ? TipoClima.LLUVIA_ACIDA : TipoClima.DESPEJADO;
			}
			return TipoClima.NIEBLA_CERRADA;

		case VOLCANICO:
			return (azar < 0.75) ? TipoClima.CENIZA_VOLCANICA : TipoClima.VENTOSO;

		case BOSQUE_MISTICO:
			if (actual == TipoClima.DESPEJADO) {
				return (azar < 0.50) ? TipoClima.ESPORAS_MAGICAS : TipoClima.PETALOS_CEREZO;
			}
			return TipoClima.DESPEJADO;

		case TEMPLADO_BOSQUE:
		default:
			if (actual == TipoClima.DESPEJADO) {
				if (azar < 0.35) {
					return TipoClima.VENTOSO;
				}
				if (azar < 0.65) {
					return TipoClima.LLUVIA_LEVE;
				}
				if (azar < 0.85) {
					return TipoClima.PETALOS_CEREZO;
				}
				return TipoClima.NIEBLA_CERRADA;
			}
			if (actual == TipoClima.VENTOSO) {
				return (azar < 0.55) ? TipoClima.LLUVIA_LEVE : TipoClima.DESPEJADO;
			}
			if (actual == TipoClima.LLUVIA_LEVE) {
				return (azar < 0.40) ? TipoClima.LLUVIA_TORMENTA : TipoClima.DESPEJADO;
			} else if (actual == TipoClima.LLUVIA_TORMENTA) {
				// Una tormenta eléctrica severa siempre amaina a llovizna primero antes de
				// cesar
				return TipoClima.LLUVIA_LEVE;
			}
			return TipoClima.DESPEJADO;
		}
	}

	// =========================================================================
	// === 4. GETTERS INMUTABLES
	// =========================================================================

	public String getNombreVisible() {
		return this.nombreVisible;
	}

	public double getTemperaturaBase() {
		return this.temperaturaBase;
	}

	public double getHumedadBase() {
		return this.humedadBase;
	}
}