package principal.entes.facciones;

/**
 * Gestor maestro de facciones y relaciones de hostilidad entre entidades.
 * <p>
 * <b>ARQUITECTURA DE ALTO RENDIMIENTO (Zero-GC / O(1)):</b> Toda la lógica de
 * relaciones opera mediante álgebra booleana sobre enteros primitivos
 * (Bitmasks). Cada facción ocupa exactamente 1 bit (soporta hasta 32 facciones
 * simultáneas con un entero de 32 bits). La comprobación de hostilidad requiere
 * exactamente 1 operación a nivel de CPU: {@code (mascara & faccion) != 0}.
 * </p>
 * 
 * @version 1.0 (Java 8 Compatible - Zero-GC Architecture)
 */
public final class GestorFacciones {

	// =========================================================================
	// === 1. IDENTIFICADORES DE FACCIÓN EN BITS (POTENCIAS DE 2)
	// =========================================================================

	/**
	 * Entidades neutrales que no pertenecen a ningún bando ni generan hostilidad
	 * base.
	 */
	public static final int FACCION_NEUTRAL = 0;

	/** Facción del personaje principal controlado por el jugador. */
	public static final int FACCION_JUGADOR = 1 << 0; // 0000 0001 (1)

	/** Facción de forajidos y asaltantes humanoides. */
	public static final int FACCION_BANDIDOS = 1 << 1; // 0000 0010 (2)

	/** Facción de criaturas salvajes, bestias y muertos vivientes. */
	public static final int FACCION_MONSTRUOS = 1 << 2; // 0000 0100 (4)

	/** Facción de ciudadanos, comerciantes y NPCs pacíficos. */
	public static final int FACCION_ALDEANOS = 1 << 3; // 0000 1000 (8)

	/** Facción de fauna silvestre pasiva (conejos, ciervos, aves). */
	public static final int FACCION_FAUNA_PASIVA = 1 << 4; // 0001 0000 (16)

	/** Cantidad máxima de facciones soportadas por el motor. */
	private static final int MAX_FACCIONES = 32;

	// =========================================================================
	// === 2. TABLA GLOBAL DE HOSTILIDAD POR DEFECTO
	// =========================================================================

	/**
	 * Arreglo de máscaras de hostilidad globales indexado por la posición del bit.
	 * Cada índice almacena los bits de todas las facciones consideradas enemigas
	 * naturales.
	 */
	private static final int[] TABLA_HOSTILIDAD_GLOBAL = new int[MAX_FACCIONES];

	static {
		inicializarMatrizHostilidadPorDefecto();
	}

	private GestorFacciones() {
		// Constructor privado para evitar instanciación (Clase puramente estática)
	}

	/**
	 * Configura las relaciones diplomáticas iniciales del mundo.
	 */
	public static void inicializarMatrizHostilidadPorDefecto() {
		for (int i = 0; i < MAX_FACCIONES; i++) {
			TABLA_HOSTILIDAD_GLOBAL[i] = 0;
		}

		// 1. Relaciones del JUGADOR: Hostil hacia Bandidos y Monstruos
		setHostilidadPorDefecto(FACCION_JUGADOR, FACCION_BANDIDOS, true);
		setHostilidadPorDefecto(FACCION_JUGADOR, FACCION_MONSTRUOS, true);

		// 2. Relaciones de BANDIDOS: Hostiles hacia Jugador, Monstruos y Aldeanos (pero
		// no entre sí)
		setHostilidadPorDefecto(FACCION_BANDIDOS, FACCION_JUGADOR, true);
		setHostilidadPorDefecto(FACCION_BANDIDOS, FACCION_MONSTRUOS, true);
		setHostilidadPorDefecto(FACCION_BANDIDOS, FACCION_ALDEANOS, true);

		// 3. Relaciones de MONSTRUOS: Hostiles hacia todos los seres vivos excepto
		// otros monstruos
		setHostilidadPorDefecto(FACCION_MONSTRUOS, FACCION_JUGADOR, true);
		setHostilidadPorDefecto(FACCION_MONSTRUOS, FACCION_BANDIDOS, true);
		setHostilidadPorDefecto(FACCION_MONSTRUOS, FACCION_ALDEANOS, true);
		setHostilidadPorDefecto(FACCION_MONSTRUOS, FACCION_FAUNA_PASIVA, true);

		// 4. Relaciones de ALDEANOS: Pacíficos por defecto (no atacan)
		// 5. Relaciones de FAUNA PASIVA: Pacíficos por defecto
	}

	// =========================================================================
	// === 3. CONSULTAS Y OPERACIONES BITWISE O(1)
	// =========================================================================

	/**
	 * Evalúa en tiempo $O(1)$ sin generar objetos si un sujeto es hostil hacia un
	 * objetivo.
	 *
	 * @param faccionObjetivo      Bit de facción que posee la criatura observada.
	 * @param mascaraHostilidadMia Máscara de bits de hostilidad del observador.
	 * @return {@code true} si el bit de la facción objetivo está activo en la
	 *         máscara del observador.
	 */
	public static boolean esHostil(final int faccionObjetivo, final int mascaraHostilidadMia) {
		if (faccionObjetivo == FACCION_NEUTRAL) {
			return false;
		}
		return (mascaraHostilidadMia & faccionObjetivo) != 0;
	}

	/**
	 * Obtiene la máscara de hostilidad por defecto preconfigurada para una facción.
	 *
	 * @param faccionBit Bit de la facción a consultar (ej:
	 *                   {@link #FACCION_BANDIDOS}).
	 * @return Entero con la combinación de bits de todas las facciones enemigas
	 *         naturales.
	 */
	public static int getMascaraHostilidadPorDefecto(final int faccionBit) {
		if (faccionBit == FACCION_NEUTRAL) {
			return 0;
		}
		final int indice = Integer.numberOfTrailingZeros(faccionBit);
		if ((indice >= 0) && (indice < MAX_FACCIONES)) {
			return TABLA_HOSTILIDAD_GLOBAL[indice];
		}
		return 0;
	}

	/**
	 * Modifica en caliente una relación de hostilidad entre dos facciones a nivel
	 * global en todo el mundo. Permite eventos como pactos de paz o declaraciones
	 * de guerra en tiempo de ejecución.
	 *
	 * @param faccionOrigen  Bit de la facción que siente la hostilidad.
	 * @param faccionDestino Bit de la facción objetivo.
	 * @param hostil         {@code true} para declararla enemiga; {@code false}
	 *                       para pactar neutralidad/alianza.
	 */
	public static void setHostilidadGlobal(final int faccionOrigen, final int faccionDestino, final boolean hostil) {
		if ((faccionOrigen == FACCION_NEUTRAL) || (faccionDestino == FACCION_NEUTRAL)) {
			return;
		}
		final int indice = Integer.numberOfTrailingZeros(faccionOrigen);
		if ((indice >= 0) && (indice < MAX_FACCIONES)) {
			if (hostil) {
				TABLA_HOSTILIDAD_GLOBAL[indice] |= faccionDestino;
			} else {
				TABLA_HOSTILIDAD_GLOBAL[indice] &= ~faccionDestino;
			}
		}
	}

	/**
	 * Configura bidireccionalmente la hostilidad inicial en la tabla interna.
	 */
	private static void setHostilidadPorDefecto(final int faccionOrigen, final int faccionDestino,
			final boolean hostil) {
		final int indice = Integer.numberOfTrailingZeros(faccionOrigen);
		if ((indice >= 0) && (indice < MAX_FACCIONES)) {
			if (hostil) {
				TABLA_HOSTILIDAD_GLOBAL[indice] |= faccionDestino;
			} else {
				TABLA_HOSTILIDAD_GLOBAL[indice] &= ~faccionDestino;
			}
		}
	}
}