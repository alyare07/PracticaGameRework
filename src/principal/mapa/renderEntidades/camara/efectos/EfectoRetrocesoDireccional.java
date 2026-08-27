package principal.mapa.renderEntidades.camara.efectos;

/**
 * Efecto cinemático de retroceso balístico e impacto direccional (Weapon
 * Recoil).
 * <p>
 * <b>Comportamiento y Física de Acción-Reacción (Game Feel):</b><br>
 * Simula la 3ª Ley de Newton (cada acción genera una reacción de igual magnitud
 * y en sentido opuesto) para otorgar contundencia física al armamento:
 * <ul>
 * <li><b>Disparos de Armas Pesadas:</b> Al disparar una escopeta, cañón o rifle
 * de francotirador, la cámara sufre una sacudida brusca a lo largo del vector
 * de tiro.</li>
 * <li><b>Embestidas y Golpes de Escudo:</b> Cuando el jugador recibe un placaje
 * de un enemigo o bloquea con éxito un proyectil pesado.</li>
 * <li><b>Diferencia con el Terremoto:</b> Mientras que un terremoto es ruido
 * aleatorio multidireccional, el retroceso sigue una <b>línea física recta y
 * enfocada</b> con retorno armónico.</li>
 * </ul>
 * </p>
 * <p>
 * <b>Fórmula Matemática del Resorte Amortiguado (Spring-Damper):</b><br>
 * 
 * <pre>
 *   VectorUnitario = (dirX, dirY) / ||Vector||
 *   resorte        = e^(-progreso * 6.0) * cos(progreso * π * 4.0)
 *   magnitud       = fuerzaPx * resorte * Intensidad
 *   offsetX        = VectorUnitario.x * magnitud
 *   offsetY        = VectorUnitario.y * magnitud
 * </pre>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public class EfectoRetrocesoDireccional extends EfectoCamara {

	// =========================================================================
	// === PARÁMETROS VECTORIALES CONFIGURABLES
	// =========================================================================

	/** Componente horizontal del vector director normalizado del retroceso. */
	private double dirX = 1.0;

	/** Componente vertical del vector director normalizado del retroceso. */
	private double dirY = 0.0;

	/** Desplazamiento máximo inicial en píxeles sobre la línea de retroceso. */
	private double fuerzaPx = 15.0;

	// =========================================================================
	// === CONSTRUCTOR
	// =========================================================================

	/**
	 * Instancia el efecto registrándolo en el catálogo con su tipo correspondiente.
	 */
	public EfectoRetrocesoDireccional() {
		super(TipoEfectoCamara.RETROCESO_DIRECCIONAL);
	}

	// =========================================================================
	// === CONFIGURACIÓN Y NORMALIZACIÓN VECTORIAL
	// =========================================================================

	/**
	 * Configura la dirección del retroceso y la magnitud del empuje en píxeles.
	 *
	 * @param dirX     Componente horizontal del retroceso (ej: si disparas a la
	 *                 derecha +1, retrocedes a la izquierda -1).
	 * @param dirY     Componente vertical del retroceso.
	 * @param fuerzaPx Fuerza máxima de la sacudida en píxeles (recomendado 10.0 a
	 *                 25.0 px).
	 */
	public void configurarDireccion(final double dirX, final double dirY, final double fuerzaPx) {
		// Calculamos la hipotenusa (módulo) para normalizar a Vector Unitario
		final double hipotenusa = Math.hypot(dirX, dirY);

		if (hipotenusa > 0.0001) {
			this.dirX = dirX / hipotenusa;
			this.dirY = dirY / hipotenusa;
		} else {
			// Dirección por defecto hacia la derecha si el vector recibido es nulo
			this.dirX = 1.0;
			this.dirY = 0.0;
		}

		this.fuerzaPx = Math.max(0.0, fuerzaPx);
	}

	// =========================================================================
	// === CÁLCULO MATEMÁTICO (CERO ASIGNACIONES EN HEAP)
	// =========================================================================

	/**
	 * Calcula la oscilación armónica del resorte sobre la línea vectorial de tiro.
	 *
	 * @param delta Tiempo transcurrido en segundos (1.0 / 60.0 en bucle fijo).
	 */
	@Override
	protected void calcularTransformaciones(final double delta) {
		/*
		 * =====================================================================
		 * EXPLICACIÓN DIDÁCTICA: ¿POR QUÉ UN MODELO DE RESORTE (SPRING-DAMPER)?
		 * --------------------------------------------------------------------- 1. EL
		 * PICO INSTANTÁNEO EN t = 0: - cos(0 * 4π) = cos(0) = 1.0 - e^(-0 * 6.0) = e^0
		 * = 1.0 --> magnitud = fuerzaPx * 1.0 = fuerzaPx (Pateo máximo al disparar).
		 * 
		 * 2. LAS 2 OSCILACIONES DE REBOTE (cos * 4π): Al multiplicar por 4π, el resorte
		 * completa 2 ciclos completos de ida y vuelta: el arma patea hacia atrás, se
		 * pasa un poco hacia adelante, y regresa rápidamente a la posición neutra.
		 * 
		 * 3. EL AMORTIGUAMIENTO RÁPIDO (e^-6x): Simula la resistencia de los brazos del
		 * personaje sosteniendo el arma, disipando toda la energía en unos 150 a 200
		 * ms. =====================================================================
		 */
		final double progreso = this.tiempoTranscurrido / this.duracionSegundos;

		// Curva de resorte elástico sub-amortiguado de alta absorción
		final double resorte = Math.exp(-progreso * 6.0) * Math.cos(progreso * Math.PI * 4.0);

		// Magnitud escalar del desplazamiento en este frame
		final double magnitud = this.fuerzaPx * this.intensidad * resorte;

		// Proyección de la fuerza sobre los ejes X e Y de la pantalla
		this.offsetX = this.dirX * magnitud;
		this.offsetY = this.dirY * magnitud;

		// Sin zoom ni rotación (el retroceso es un desplazamiento puramente lineal)
		this.offsetZoom = 0.0;
		this.anguloRotacion = 0.0;
	}

	// =========================================================================
	// === GETTERS
	// =========================================================================

	public double getDirX() {
		return this.dirX;
	}

	public double getDirY() {
		return this.dirY;
	}

	public double getFuerzaPx() {
		return this.fuerzaPx;
	}
}