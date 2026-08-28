package principal.clima;

import principal.iluminacion.ZonaAmbiente;
import principal.utilidades.Globales;

/**
 * Gestor y mediador de volúmenes espaciales de biomas y áreas en el mundo.
 * <p>
 * <b>Rol Arquitectónico (Patrón Mediador):</b><br>
 * Esta clase actúa como un puente desacoplado entre el espacio físico del mapa
 * (coordenadas del jugador dentro de un {@link ZonaAmbiente}) y los subsistemas
 * visuales ({@code GestorLuz} y {@code GestorClima}).
 * </p>
 * <p>
 * <b>Capacidades y Rendimiento:</b>
 * <ul>
 * <li><b>Cero Recolección de Basura (Zero-GC):</b> Utiliza un arreglo estático
 * contiguo de tamaño fijo para evitar asignaciones dinámicas en memoria
 * Heap.</li>
 * <li><b>Inmersión Progresiva ($\Delta t$ Lerp):</b> Suaviza la entrada y
 * salida de las zonas en un intervalo de ~1.5 segundos, evitando cambios
 * bruscos en la pantalla.</li>
 * <li><b>Diferenciación de Espacios:</b> Discrimina automáticamente entre
 * biomas al aire libre (que modulan el tinte y la niebla sin apagar el sol) y
 * mazmorras/cuevas subterráneas.</li>
 * </ul>
 * </p>
 * 
 * @version 2.0
 */
public class GestorZonasAmbiente {

	// =========================================================================
	// === 1. CONSTANTES Y CONFIGURACIÓN DE MEMORIA
	// =========================================================================

	/** Capacidad máxima de zonas ambientales simultáneas por mapa. */
	private static final int MAX_ZONAS = 32;

	/*
	 * =========================================================================
	 * EXPLICACIÓN DIDÁCTICA: VELOCIDAD DE TRANSICIÓN MATEMÁTICA
	 * ------------------------------------------------------------------------- Si
	 * queremos que una transición dure exactamente 1.5 segundos:
	 *
	 * Velocidad = 1.0 (inmersión total) / 1.5 segundos = ~0.66 unidades por
	 * segundo.
	 *
	 * Al multiplicar 'VELOCIDAD_TRANSICION * dt' en cada fotograma: 1. Si el
	 * jugador entra: 'factorInmersion' sube de 0.0 a 1.0 en 1.5s. 2. Si el jugador
	 * sale: 'factorInmersion' baja de 1.0 a 0.0 en 1.5s.
	 * =========================================================================
	 */
	private static final double VELOCIDAD_TRANSICION = 0.66;

	// =========================================================================
	// === 2. ESTRUCTURA DE DATOS (ZERO-GC)
	// =========================================================================

	/** Arreglo estático de zonas registradas en el mapa actual. */
	private final ZonaAmbiente[] zonas = new ZonaAmbiente[MAX_ZONAS];

	/** Cantidad de zonas activas registradas en el arreglo. */
	private int cantidadZonas = 0;

	/** Referencia a la zona ambiental que el jugador está ocupando actualmente. */
	private ZonaAmbiente zonaActual = null;

	// =========================================================================
	// === 3. GESTIÓN Y REGISTRO DE ZONAS (API PÚBLICA)
	// =========================================================================

	/**
	 * Registra un nuevo volumen de bioma en el gestor para el mapa en curso.
	 *
	 * @param zona Instancia de {@link ZonaAmbiente} a registrar.
	 */
	public void registrarZona(final ZonaAmbiente zona) {
		if ((zona != null) && (this.cantidadZonas < MAX_ZONAS)) {
			this.zonas[this.cantidadZonas++] = zona;
		}
	}

	/**
	 * Vierte y limpia todas las zonas registradas (usado en transiciones de mapa).
	 */
	public void limpiarZonas() {
		for (int i = 0; i < this.cantidadZonas; i++) {
			this.zonas[i] = null;
		}
		this.cantidadZonas = 0;
		this.zonaActual = null;
	}

	// =========================================================================
	// === 4. CICLO LÓGICO DE ACTUALIZACIÓN (60 APS)
	// =========================================================================

	/**
	 * Evalúa la posición del jugador respecto a las zonas registradas, calcula los
	 * factores de inmersión y modula la luz y el clima correspondientes.
	 *
	 * @param dt Delta de tiempo en segundos (1.0 / 60.0).
	 */
	public void actualizar(final double dt) {
		if (Globales.JUGADOR == null) {
			return;
		}

		final double jx = Globales.JUGADOR.getPosicionXInt();
		final double jy = Globales.JUGADOR.getPosicionYInt();

		ZonaAmbiente zonaEncontrada = null;

		// 1. Evaluación espacial de colisión punto-rectángulo para cada zona
		for (int i = 0; i < this.cantidadZonas; i++) {
			final ZonaAmbiente z = this.zonas[i];

			if (z.contiene(jx, jy)) {
				zonaEncontrada = z;
				// Incrementa la inmersión de forma progresiva hasta un tope de 1.0
				z.setFactorInmersion(z.getFactorInmersion() + (VELOCIDAD_TRANSICION * dt));
			} else {
				// Reduce la inmersión de forma progresiva hasta un mínimo de 0.0
				z.setFactorInmersion(z.getFactorInmersion() - (VELOCIDAD_TRANSICION * dt));
			}
		}

		/*
		 * =====================================================================
		 * EXPLICACIÓN DIDÁCTICA: DETECCIÓN DE CAMBIO DE ESTADO ATMOSFÉRICO
		 * ---------------------------------------------------------------------
		 * Comprobamos si el jugador cruzó el umbral hacia una nueva zona:
		 *
		 * A) Si entra a una Cueva (esInterior == true): Ordena a GestorLuz iniciar un
		 * fundido hacia la oscuridad total, desconectando el reloj solar.
		 *
		 * B) Si sale al exterior o entra a un bioma al aire libre: Restaura el modo
		 * exterior natural para que el sol y el reloj de 24h vuelvan a tomar el control
		 * de la luz ambiental.
		 * =====================================================================
		 */
		if (zonaEncontrada != this.zonaActual) {
			this.zonaActual = zonaEncontrada;

			if (zonaEncontrada != null) {
				if (zonaEncontrada.isEsInterior()) {
					Globales.GESTOR_LUZ.establecerAmbienteTransicion(zonaEncontrada.getColorAmbiente(), 1.5);
				} else {
					Globales.GESTOR_LUZ.restablecerModoExterior();
				}
			} else {
				Globales.GESTOR_LUZ.restablecerModoExterior();
			}
		}

		/*
		 * =====================================================================
		 * MODULACIÓN CONTINUA DE CLIMA Y TINTE DE LUZ
		 * --------------------------------------------------------------------- En cada
		 * fotograma enviamos el 'factorInmersion' (0.0 a 1.0) a los gestores: - A
		 * GestorLuz: Aplica el tinte de color suavemente sobre el sol/noche. - A
		 * GestorClima: Aumenta o disminuye la densidad de la niebla local.
		 * =====================================================================
		 */
		if (this.zonaActual != null) {
			final double f = this.zonaActual.getFactorInmersion();

			if (!this.zonaActual.isEsInterior()) {
				Globales.GESTOR_LUZ.setTinteBiomaExterior(this.zonaActual.getColorAmbiente(), f);
			}
			Globales.GESTOR_CLIMA.setNieblaBiomaLocal(this.zonaActual.getNivelNiebla(), f);
		} else {
			Globales.GESTOR_CLIMA.setNieblaBiomaLocal(null, 0.0);
		}
	}

	// =========================================================================
	// === 5. GETTERS DE CONSULTA
	// =========================================================================

	/**
	 * Retorna la zona ambiental en la que se encuentra inmerso el jugador
	 * actualmente.
	 *
	 * @return Instancia de {@link ZonaAmbiente} activa o {@code null} si está en
	 *         zona neutra.
	 */
	public ZonaAmbiente getZonaActual() {
		return this.zonaActual;
	}

	/**
	 * Indica si el jugador se encuentra actualmente dentro de una zona interior
	 * (cueva o mazmorra).
	 *
	 * @return {@code true} si la zona actual es interior y bloquea el sol.
	 */
	public boolean isEnZonaInterior() {
		return (this.zonaActual != null) && this.zonaActual.isEsInterior();
	}

	/**
	 * Retorna la cantidad de zonas ambientales registradas en el mapa activo.
	 *
	 * @return Cantidad entera de zonas registradas (0 a 32).
	 */
	public int getCantidadZonas() {
		return this.cantidadZonas;
	}
}