package principal.mapa.renderEntidades.camara.efectos;

import principal.utilidades.Globales;

/**
 * Gestor centralizado de efectos de cámara de alto rendimiento (Zero-GC /
 * O(1)).
 * <p>
 * <b>Pilares de Arquitectura y Rendimiento:</b>
 * <ul>
 * <li><b>Pool Estático por Ordinal O(1):</b> Cada tipo de efecto se instancia
 * <b>una única vez</b> al iniciar el motor en un arreglo primitivo indexado por
 * {@link Enum#ordinal()}. Elimina por completo las tablas hash, las búsquedas
 * por clave, el boxing/unboxing y la creación de objetos {@code new} en
 * caliente.</li>
 * <li><b>Eliminación Instantánea Swap-and-Pop O(1):</b> Cuando un efecto
 * temporal termina, se retira de la lista activa en un solo ciclo de CPU
 * reemplazándolo por el último elemento, evitando el costo de corrimiento de
 * memoria ({@code System.arraycopy}) de las listas tradicionales.</li>
 * <li><b>Bucle de Render sin Iteradores:</b> Itera sobre arreglos nativos
 * usando índices primitivos enteros, garantizando <b>0 bytes de basura en el
 * Heap</b> tanto a 60 FPS como a 240+ FPS.</li>
 * </ul>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public class GestorEfectosCamara {

	// =========================================================================
	// === 1. CAPACIDAD Y POOL ESTÁTICO DE EFECTOS
	// =========================================================================

	/** Cantidad total de efectos registrados en el catálogo de tipos. */
	private static final int TOTAL_TIPOS = TipoEfectoCamara.values().length;

	/**
	 * Arreglo maestro pre-instanciado. Cada casilla contiene la única instancia
	 * viva de cada efecto, indexada directamente por su posición ordinal en el
	 * Enum.
	 */
	private final EfectoCamara[] poolEfectos;

	/**
	 * Arreglo contiguo de efectos actualmente activos para ser procesados en el
	 * frame.
	 */
	private final EfectoCamara[] efectosActivos;

	/** Cantidad actual de efectos activos procesándose en este fotograma. */
	private int cantidadActivos;

	// =========================================================================
	// === 2. ACUMULADORES DE TRANSFORMACIÓN PARA GRAPHICS2D
	// =========================================================================

	/**
	 * Suma total de sacudida horizontal de todos los efectos activos en píxeles.
	 */
	private double totalOffsetX;

	/** Suma total de sacudida vertical de todos los efectos activos en píxeles. */
	private double totalOffsetY;

	/** Suma total de desplazamiento de zoom de todos los efectos activos. */
	private double totalOffsetZoom;

	/** Suma total de rotación angular de todos los efectos activos en radianes. */
	private double totalAnguloRotacion;

	// =========================================================================
	// === CONSTRUCTOR: INICIALIZACIÓN ÚNICA DEL POOL
	// =========================================================================

	/**
	 * Inicializa el pool de efectos instanciando cada una de las 16 clases
	 * concretas una única vez en la memoria VRAM/RAM.
	 */
	public GestorEfectosCamara() {
		this.poolEfectos = new EfectoCamara[TOTAL_TIPOS];
		this.efectosActivos = new EfectoCamara[TOTAL_TIPOS];
		this.cantidadActivos = 0;

		// --- Registro de Efectos Base de Entorno y Estado (9) ---
		this.poolEfectos[TipoEfectoCamara.TERREMOTO.ordinal()] = new EfectoTerremoto();
		this.poolEfectos[TipoEfectoCamara.PISOTON.ordinal()] = new EfectoPisoton();
		this.poolEfectos[TipoEfectoCamara.LATIDO.ordinal()] = new EfectoLatido();
		this.poolEfectos[TipoEfectoCamara.BORRACHO.ordinal()] = new EfectoBorracho();
		this.poolEfectos[TipoEfectoCamara.BERSERK.ordinal()] = new EfectoBerserk();
		this.poolEfectos[TipoEfectoCamara.RETROCESO_DIRECCIONAL.ordinal()] = new EfectoRetrocesoDireccional();
		this.poolEfectos[TipoEfectoCamara.RESPIRACION.ordinal()] = new EfectoRespiracion();
		this.poolEfectos[TipoEfectoCamara.VIENTO_TORMENTA.ordinal()] = new EfectoVientoTormenta();
		this.poolEfectos[TipoEfectoCamara.ATURDIMIENTO.ordinal()] = new EfectoAturdimiento();

		// --- Registro de Efectos Avanzados de Combate y Cinemática (7) ---
		this.poolEfectos[TipoEfectoCamara.IMPACTO_CRITICO.ordinal()] = new EfectoImpactoCritico();
		this.poolEfectos[TipoEfectoCamara.ONDA_EXPANSIVA.ordinal()] = new EfectoOndaExpansiva();
		this.poolEfectos[TipoEfectoCamara.INERCIA_DASH.ordinal()] = new EfectoInerciaDash();
		this.poolEfectos[TipoEfectoCamara.BARCO_NAVEGACION.ordinal()] = new EfectoBarcoNavegacion();
		this.poolEfectos[TipoEfectoCamara.ALERTA_SOBRESALTO.ordinal()] = new EfectoAlertaSobresalto();
		this.poolEfectos[TipoEfectoCamara.CAMARA_LENTA_ENFOQUE.ordinal()] = new EfectoCamaraLentaEnfoque();
		this.poolEfectos[TipoEfectoCamara.CAIDA_ABISMO.ordinal()] = new EfectoCaidaAbismo();
	}

	// =========================================================================
	// === DISPARADORES Y CONTROL DE ESTADO
	// =========================================================================

	/**
	 * Dispara o reinicia un efecto de duración temporal. Si el efecto ya estaba
	 * corriendo, actualiza su temporizador y potencia sin duplicarlo en memoria.
	 *
	 * @param tipo       Identificador del efecto en el catálogo.
	 * @param duracionMs Duración en milisegundos.
	 * @param intensidad Fuerza o multiplicador de magnitud.
	 */
	public void reproducirEfectoTemporal(final TipoEfectoCamara tipo, final double duracionMs,
			final double intensidad) {
		final EfectoCamara efecto = this.poolEfectos[tipo.ordinal()];
		final boolean yaEstabaActivo = efecto.isActivo();

		efecto.activarTemporal(duracionMs, intensidad);

		// Si no estaba activo en la lista contigua, lo añadimos al final
		if (!yaEstabaActivo) {
			this.efectosActivos[this.cantidadActivos] = efecto;
			this.cantidadActivos++;
		}
	}

	/**
	 * Conmuta un efecto de duración indefinida (activar o desactivar).
	 *
	 * @param tipo       Identificador del efecto en el catálogo.
	 * @param activar    {@code true} para encenderlo, {@code false} para apagarlo.
	 * @param intensidad Fuerza o multiplicador de magnitud.
	 */
	public void conmutarEfectoInfinito(final TipoEfectoCamara tipo, final boolean activar, final double intensidad) {
		final EfectoCamara efecto = this.poolEfectos[tipo.ordinal()];
		final boolean yaEstabaActivo = efecto.isActivo();

		if (activar) {
			efecto.activarInfinito(intensidad);
			if (!yaEstabaActivo) {
				this.efectosActivos[this.cantidadActivos] = efecto;
				this.cantidadActivos++;
			}
		} else if (yaEstabaActivo) {
			efecto.desactivar();
		}
	}

	/**
	 * Detiene y apaga instantáneamente todos los efectos activos. Ideal para
	 * transiciones de pantalla, pantallas de carga o pantallas de muerte.
	 */
	public void detenerTodosLosEfectos() {
		for (int i = 0; i < this.cantidadActivos; i++) {
			this.efectosActivos[i].desactivar();
			this.efectosActivos[i] = null;
		}
		this.cantidadActivos = 0;
		this.totalOffsetX = 0.0;
		this.totalOffsetY = 0.0;
		this.totalOffsetZoom = 0.0;
		this.totalAnguloRotacion = 0.0;
	}

	// =========================================================================
	// === BUCLE DE ACTUALIZACIÓN Y ACUMULACIÓN (ZERO-GC / SWAP-AND-POP)
	// =========================================================================

	/**
	 * Actualiza los temporizadores de todos los efectos activos, acumula las
	 * matrices resultantes y descarta los finalizados en tiempo constante $O(1)$.
	 */
	public void actualizar() {
		// Reiniciamos los acumuladores del frame a cero
		this.totalOffsetX = 0.0;
		this.totalOffsetY = 0.0;
		this.totalOffsetZoom = 0.0;
		this.totalAnguloRotacion = 0.0;

		final double dt = (Globales.delta > 0.0) ? Globales.delta : (1.0 / 60.0);

		int i = 0;
		while (i < this.cantidadActivos) {
			final EfectoCamara efecto = this.efectosActivos[i];
			efecto.actualizar(dt);

			if (efecto.isActivo()) {
				// Sumamos las transformaciones del efecto activo
				this.totalOffsetX += efecto.getOffsetX();
				this.totalOffsetY += efecto.getOffsetY();
				this.totalOffsetZoom += efecto.getOffsetZoom();
				this.totalAnguloRotacion += efecto.getAnguloRotacion();
				i++;
			} else {
				/*
				 * ============================================================= EXPLICACIÓN
				 * DIDÁCTICA: ¿CÓMO FUNCIONA EL SWAP-AND-POP O(1)?
				 * ------------------------------------------------------------- Supongamos que
				 * tenemos 4 efectos activos: [A, B, C, D] (cantidad = 4).
				 * 
				 * Si el efecto 'B' (índice 1) termina: 1. En una lista normal
				 * (ArrayList.remove), Java tendría que mover 'C' y 'D' un lugar a la izquierda
				 * (operación O(N) lenta).
				 * 
				 * 2. Con SWAP-AND-POP: - Tomamos el ÚLTIMO elemento activo 'D' (índice 3). - Lo
				 * colocamos en la posición del que acaba de morir (índice 1). - La lista queda:
				 * [A, D, C, null] y cantidad pasa a ser 3. - NO incrementamos 'i', para que en
				 * la siguiente vuelta del bucle se evalúe 'D' (que ahora está en la casilla 1).
				 * 
				 * Resultado: Eliminación instantánea en 1 ciclo de CPU sin copias de memoria.
				 * =============================================================
				 */
				this.efectosActivos[i] = this.efectosActivos[this.cantidadActivos - 1];
				this.efectosActivos[this.cantidadActivos - 1] = null;
				this.cantidadActivos--;
			}
		}
	}

	// =========================================================================
	// === ACCESO TIPADO Y GETTERS
	// =========================================================================

	/**
	 * Retorna la instancia pre-instanciada de un efecto concreto con casteo
	 * genérico automático para configurar sus parámetros particulares.
	 *
	 * @param <T>  Subclase concreta de {@link EfectoCamara}.
	 * @param tipo Constante del enum a consultar.
	 * @return Instancia del efecto casteada automáticamente.
	 */
	@SuppressWarnings("unchecked")
	public <T extends EfectoCamara> T getEfecto(final TipoEfectoCamara tipo) {
		return (T) this.poolEfectos[tipo.ordinal()];
	}

	public double getOffsetX() {
		return this.totalOffsetX;
	}

	public double getOffsetY() {
		return this.totalOffsetY;
	}

	public double getOffsetZoom() {
		return this.totalOffsetZoom;
	}

	public double getAnguloRotacion() {
		return this.totalAnguloRotacion;
	}

	public boolean tieneEfectosActivos() {
		return this.cantidadActivos > 0;
	}

	public int getCantidadActivos() {
		return this.cantidadActivos;
	}
}