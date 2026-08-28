package principal.clima;

import java.awt.Color;

import principal.iluminacion.IntensidadNiebla;

/**
 * Catálogo central de estados y perfiles meteorológicos predefinidos para el
 * mundo del juego.
 * <p>
 * <b>Rol Arquitectónico y Diseño:</b>
 * <ul>
 * <li><b>Preset de Configuración Multidimensional:</b> Cada constante del enum
 * encapsula de forma calibrada e inmutable 9 variables físicas y visuales
 * (niebla, color de bruma, conteo de partículas, nubes, viento y
 * relámpagos).</li>
 * <li><b>Zero-GC Estricto:</b> Al ser un {@code Enum}, sus instancias son
 * constantes únicas en memoria creadas en la carga de la clase. Pasar un
 * {@code TipoClima} no genera ninguna instanciación en la memoria Heap.</li>
 * <li><b>Control por Capas Gráficas:</b> Permite que {@code GestorClima}
 * configure simultáneamente la capa de partículas, la capa de niebla y la capa
 * de nubes con una sola llamada a {@code setClima(tipo)}.</li>
 * </ul>
 * </p>
 * 
 * @version 3.5
 */
public enum TipoClima {

	// =========================================================================
	// === 1. CLIMAS CLÁSICOS Y NATURALES
	// =========================================================================

	/**
	 * Cielo despejado: día soleado con sombras de nubes suaves en el suelo y brisa
	 * ligera.
	 * <p>
	 * <b>Parámetros:</b> 0 partículas, niebla desactivada, viento a 45° con fuerza
	 * 1.0.
	 * </p>
	 */
	DESPEJADO("Despejado", IntensidadNiebla.DESACTIVADA, new Color(200, 215, 230), 0, false, true, 0.32f, 1.0, 45.0),

	/**
	 * Clima ventoso: día soleado donde las nubes cruzan rápido y vuelan hojas
	 * verdes/otoñales.
	 * <p>
	 * <b>Parámetros:</b> 140 hojas voladoras, viento fuerte a 35° con fuerza 2.6.
	 * </p>
	 */
	VENTOSO("Ventoso", IntensidadNiebla.DESACTIVADA, new Color(200, 215, 230), 140, false, true, 0.38f, 2.6, 35.0),

	/**
	 * Lluvia moderada: cielo encapotado (sin sombras de nubes) y llovizna continua.
	 * <p>
	 * <b>Parámetros:</b> 180 gotas diagonales, niebla leve azulada, viento a 65°
	 * con fuerza 1.4.
	 * </p>
	 */
	LLUVIA_LEVE("Lluvia Leve", IntensidadNiebla.LEVE, new Color(170, 190, 215), 180, false, false, 0.0f, 1.4, 65.0),

	/**
	 * Tormenta eléctrica: lluvia torrencial, niebla de humedad, viento violento y
	 * relámpagos automáticos.
	 * <p>
	 * <b>Parámetros:</b> 360 gotas rápidas, relámpagos activos, viento a 75° con
	 * fuerza 2.4.
	 * </p>
	 */
	LLUVIA_TORMENTA("Tormenta Eléctrica", IntensidadNiebla.LEVE, new Color(130, 150, 180), 360, true, false, 0.0f, 2.4,
			75.0),

	/**
	 * Nevada suave: copos de nieve que caen oscilando con bruma fría.
	 * <p>
	 * <b>Parámetros:</b> 220 copos oscilantes, niebla leve blanca, viento suave a
	 * 30° con fuerza 0.8.
	 * </p>
	 */
	NIEVE("Nieve", IntensidadNiebla.LEVE, new Color(220, 235, 255), 220, false, false, 0.0f, 0.8, 30.0),

	/**
	 * Ventisca helada (Blizzard): nieve a gran velocidad y niebla moderada.
	 * <p>
	 * <b>Parámetros:</b> 380 copos rápidos, niebla moderada, viento huracanado a
	 * 80° con fuerza 2.8.
	 * </p>
	 */
	VENTISCA("Ventisca", IntensidadNiebla.MODERADA, new Color(200, 220, 250), 380, false, false, 0.0f, 2.8, 80.0),

	/**
	 * Tormenta de arena: niebla densa amarillenta con ráfagas de polvo a alta
	 * velocidad.
	 * <p>
	 * <b>Parámetros:</b> 320 partículas de arena horizontales, niebla intensa,
	 * viento a 10° con fuerza 3.2.
	 * </p>
	 */
	TORMENTA_ARENA("Tormenta de Arena", IntensidadNiebla.INTENSA, new Color(210, 165, 90), 320, false, false, 0.0f, 3.2,
			10.0),

	// =========================================================================
	// === 2. CLIMAS TEMÁTICOS, BIOMAS Y FANTASÍA
	// =========================================================================

	/**
	 * Lluvia de ceniza y brasas: niebla grisácea oscura con copos de ceniza y
	 * chispas incandescentes.
	 * <p>
	 * <b>Parámetros:</b> 180 cenizas/brasas flotantes, niebla moderada oscura,
	 * viento a 60° con fuerza 0.6.
	 * </p>
	 */
	CENIZA_VOLCANICA("Ceniza Volcánica", IntensidadNiebla.MODERADA, new Color(90, 80, 85), 180, false, false, 0.0f, 0.6,
			60.0),

	/**
	 * Esporas mágicas: bruma mística suave con partículas arcanas celestes que
	 * flotan hacia arriba.
	 * <p>
	 * <b>Parámetros:</b> 110 esporas ascendentes, niebla leve cian, viento a 270°
	 * (hacia arriba) con fuerza 0.4.
	 * </p>
	 */
	ESPORAS_MAGICAS("Esporas Mágicas", IntensidadNiebla.LEVE, new Color(150, 220, 240), 110, false, false, 0.0f, 0.4,
			270.0),

	/**
	 * Niebla cerrada (Silent Hill): visibilidad mínima para crear tensión y
	 * atmósfera de misterio.
	 * <p>
	 * <b>Parámetros:</b> 0 partículas, niebla intensa blanca (68% opacidad), viento
	 * calmo a 45° con fuerza 0.5.
	 * </p>
	 */
	NIEBLA_CERRADA("Niebla Cerrada", IntensidadNiebla.INTENSA, new Color(210, 220, 230), 0, false, false, 0.0f, 0.5,
			45.0),

	/**
	 * Primavera / Sakura: día soleado con pétalos rosados planeando suavemente con
	 * la brisa.
	 * <p>
	 * <b>Parámetros:</b> 120 pétalos rosados, nubes diurnas visibles, viento a 40°
	 * con fuerza 1.2.
	 * </p>
	 */
	PETALOS_CEREZO("Pétalos de Cerezo", IntensidadNiebla.DESACTIVADA, new Color(200, 215, 230), 120, false, true, 0.32f,
			1.2, 40.0),

	/**
	 * Lluvia ácida: niebla tóxica verdosa con gotas de lluvia verde brillante
	 * corrosivas.
	 * <p>
	 * <b>Parámetros:</b> 240 gotas verdes ácidas, niebla leve verde, viento a 70°
	 * con fuerza 1.6.
	 * </p>
	 */
	LLUVIA_ACIDA("Lluvia Ácida", IntensidadNiebla.LEVE, new Color(130, 185, 95), 240, false, false, 0.0f, 1.6, 70.0);

	// =========================================================================
	// === 3. ATRIBUTOS INMUTABLES DEL PRESET
	// =========================================================================

	/**
	 * Nombre descriptivo del clima para mostrar en interfaces, reportes o diálogos.
	 */
	private final String nombre;

	/** Nivel de densidad predefinido para la capa de niebla. */
	private final IntensidadNiebla nivelNiebla;

	/**
	 * Color tonal de la niebla ambiental (blanco, grisáceo, verde tóxico, etc.).
	 */
	private final Color colorNiebla;

	/** Cantidad de partículas activas simultáneas en pantalla (0 a 400). */
	private final int cantidadParticulas;

	/** Indica si el clima activa el secuenciador de rayos y truenos automáticos. */
	private final boolean tieneTormentaRayos;

	/** Indica si se dibuja la textura de sombras de nubes en el suelo soleado. */
	private final boolean tieneNubes;

	/**
	 * Nivel de opacidad de las sombras de nubes (0.0f = transparente, 1.0f =
	 * opaco).
	 */
	private final float opacidadNubes;

	/**
	 * Multiplicador de fuerza del viento que afecta a las partículas y al
	 * desplazamiento UV.
	 */
	private final double fuerzaViento;

	/*
	 * =========================================================================
	 * EXPLICACIÓN DIDÁCTICA: ÁNGULO DE VIENTO EN GRADOS
	 * -------------------------------------------------------------------------
	 * Especificamos la dirección en grados sexagesimales (0° a 360°) porque es
	 * intuitivo para diseñar: - 0° : Viento soplando puramente hacia la Derecha
	 * (Este). - 45° : Viento en diagonal hacia Abajo-Derecha (Sureste). - 90° :
	 * Viento cayendo vertical hacia Abajo (Sur). - 270° : Viento ascendiendo
	 * vertical hacia Arriba (Norte / Esporas mágicas).
	 *
	 * Al inicializar el clima, 'GestorClima' convierte estos grados a radianes y
	 * calcula los vectores 'cos(angulo)' y 'sin(angulo)'.
	 * =========================================================================
	 */
	/** Dirección del viento en grados sexagesimales (0.0° a 360.0°). */
	private final double anguloVientoGrados;

	// =========================================================================
	// === 4. CONSTRUCTOR
	// =========================================================================

	TipoClima(final String nombre, final IntensidadNiebla nivelNiebla, final Color colorNiebla,
			final int cantidadParticulas, final boolean tieneTormentaRayos, final boolean tieneNubes,
			final float opacidadNubes, final double fuerzaViento, final double anguloVientoGrados) {
		this.nombre = nombre;
		this.nivelNiebla = nivelNiebla;
		this.colorNiebla = colorNiebla;
		this.cantidadParticulas = cantidadParticulas;
		this.tieneTormentaRayos = tieneTormentaRayos;
		this.tieneNubes = tieneNubes;
		this.opacidadNubes = opacidadNubes;
		this.fuerzaViento = fuerzaViento;
		this.anguloVientoGrados = anguloVientoGrados;
	}

	// =========================================================================
	// === 5. GETTERS INMUTABLES
	// =========================================================================

	public String getNombre() {
		return this.nombre;
	}

	public IntensidadNiebla getNivelNiebla() {
		return this.nivelNiebla;
	}

	public Color getColorNiebla() {
		return this.colorNiebla;
	}

	public int getCantidadParticulas() {
		return this.cantidadParticulas;
	}

	public boolean isTieneTormentaRayos() {
		return this.tieneTormentaRayos;
	}

	public boolean isTieneNubes() {
		return this.tieneNubes;
	}

	public float getOpacidadNubes() {
		return this.opacidadNubes;
	}

	public double getFuerzaViento() {
		return this.fuerzaViento;
	}

	public double getAnguloVientoGrados() {
		return this.anguloVientoGrados;
	}
}