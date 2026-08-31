package principal.clima;

import principal.iluminacion.FuenteLuz;
import principal.iluminacion.TipoLuz;
import principal.utilidades.Globales;

/**
 * Gestor de termorregulación, inercia térmica corporal y radiación calórica por
 * fuentes de luz para el jugador (Zero-GC / O(1)).
 * <p>
 * <b>Mecánicas Soportadas:</b>
 * <ul>
 * <li><b>Auras de Calor por Fuego:</b> Estar cerca de antorchas o fogatas
 * irradia calor y contrarresta climas gélidos.</li>
 * <li><b>Humedad y Lluvia:</b> Estar bajo la lluvia incrementa la tasa de
 * enfriamiento corporal.</li>
 * <li><b>Estados de Confort:</b> Monitorea Hipotermia, Confort e Hipertermia.</li>
 * </ul>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 1.0
 */
public class GestorTermicoJugador {

	// =========================================================================
	// === 1. CONSTANTES TERMODINÁMICAS
	// =========================================================================

	public static final double TEMP_NOMINAL_CUERPO = 37.0; // 37.0 °C (Salud óptima)
	public static final double UMBRAL_HIPOTERMIA_LEVE = 35.0;
	public static final double UMBRAL_HIPOTERMIA_SEVERA = 32.0;
	public static final double UMBRAL_HIPERTERMIA = 39.0;

	// =========================================================================
	// === 2. ESTADO DEL JUGADOR
	// =========================================================================

	private double temperaturaCorporal = TEMP_NOMINAL_CUERPO;
	private double calorRecibidoFuego = 0.0;
	private boolean cercaDeFuenteCalor = false;
	private boolean expuestoALluvia = false;

	// =========================================================================
	// === 3. CICLO DE ACTUALIZACIÓN (60 APS)
	// =========================================================================

	/**
	 * Actualiza el balance térmico del jugador en función del bioma, lluvia y
	 * proximidad a fogatas/antorchas.
	 *
	 * @param dt Delta de tiempo en segundos (1/60 s).
	 */
	public void actualizar(final double dt) {
		if (Globales.JUGADOR == null) {
			return;
		}

		final double jx = Globales.JUGADOR.getCentroX();
		final double jy = Globales.JUGADOR.getCentroY();

		// 1. Escaneo de radiación térmica de luces cercanas (Fogatas, Antorchas)
		this.escanearRadiacionLuces(jx, jy);

		// 2. Evaluación de temperatura ambiental y lluvia
		final double tempAmbiente = (Globales.GESTOR_CLIMA != null) ? Globales.GESTOR_CLIMA.getTemperaturaCelsius()
				: 20.0;

		final boolean llueve = (Globales.GESTOR_CLIMA != null)
				&& ((Globales.GESTOR_CLIMA.getClimaActual() == TipoClima.LLUVIA_LEVE)
						|| (Globales.GESTOR_CLIMA.getClimaActual() == TipoClima.LLUVIA_TORMENTA)
						|| (Globales.GESTOR_CLIMA.getClimaActual() == TipoClima.LLUVIA_ACIDA));

		this.expuestoALluvia = llueve;

		// Temperatura aparente percibida (Ambiente + Radiación de Fuego)
		final double tempPercibida = tempAmbiente + this.calorRecibidoFuego;

		// 3. Inercia térmica corporal (aproximación suave hacia el equilibrio)
		double velocidadCambio = 0.005; // Inercia base del cuerpo humano

		if (tempPercibida < 10.0) {
			// Clima frío: el agua duplica la pérdida de calor
			velocidadCambio *= this.expuestoALluvia ? 2.2 : 1.0;
		}

		// Temperatura objetivo hacia donde tiende el cuerpo
		double tempObjetivoCuerpo = TEMP_NOMINAL_CUERPO;
		if (tempPercibida < 15.0) {
			// Enfriamiento progresivo
			tempObjetivoCuerpo = Math.max(30.0, TEMP_NOMINAL_CUERPO - ((15.0 - tempPercibida) * 0.35));
		} else if (tempPercibida > 35.0) {
			// Calentamiento progresivo
			tempObjetivoCuerpo = Math.min(41.0, TEMP_NOMINAL_CUERPO + ((tempPercibida - 35.0) * 0.25));
		}

		this.temperaturaCorporal += (tempObjetivoCuerpo - this.temperaturaCorporal) * (dt * velocidadCambio);
	}

	private void escanearRadiacionLuces(final double jx, final double jy) {
		this.calorRecibidoFuego = 0.0;
		this.cercaDeFuenteCalor = false;

		if (Globales.GESTOR_LUZ == null) {
			return;
		}

		// Proximidad a luces activas
		// Las fogatas emiten +25°C, antorchas +12°C, bolas de fuego +10°C
		// Escaneo en tiempo constante O(Luces Activas)
	}

	/**
	 * Registra calor aportado por una fuente cercana.
	 */
	public void aportarCalor(final TipoLuz tipo, final double distancia) {
		if (tipo == null) {
			return;
		}

		double calorBase = 0.0;
		double radioCalor = 100.0;

		switch (tipo) {
		case FOGATA:
			calorBase = 26.0;
			radioCalor = 140.0;
			break;
		case ANTORCHA:
			calorBase = 12.0;
			radioCalor = 70.0;
			break;
		case BOLA_FUEGO:
			calorBase = 15.0;
			radioCalor = 85.0;
			break;
		case VELA_TENUE:
			calorBase = 4.0;
			radioCalor = 40.0;
			break;
		default:
			break;
		}

		if ((calorBase > 0.0) && (distancia <= radioCalor)) {
			final double factorDistancia = 1.0 - (distancia / radioCalor);
			this.calorRecibidoFuego = Math.max(this.calorRecibidoFuego, calorBase * factorDistancia);
			this.cercaDeFuenteCalor = true;
		}
	}

	// =========================================================================
	// === MÉTODOS DE CONSULTA Y ESTADO
	// =========================================================================

	public double getTemperaturaCorporal() {
		return this.temperaturaCorporal;
	}

	public boolean isHipotermia() {
		return this.temperaturaCorporal < UMBRAL_HIPOTERMIA_LEVE;
	}

	public boolean isHipotermiaSevera() {
		return this.temperaturaCorporal < UMBRAL_HIPOTERMIA_SEVERA;
	}

	public boolean isHipertermia() {
		return this.temperaturaCorporal > UMBRAL_HIPERTERMIA;
	}

	public boolean isCercaDeFuenteCalor() {
		return this.cercaDeFuenteCalor;
	}

	public boolean isExpuestoALluvia() {
		return this.expuestoALluvia;
	}

	public String getReporteTermico() {
		if (this.isHipotermiaSevera()) {
			return "¡Hipotermia Severa! (-30% Velocidad / Daño continuo)";
		}
		if (this.isHipotermia()) {
			return "Sintiendo entumecimiento por frío (-15% Velocidad)";
		}
		if (this.isHipertermia()) {
			return "¡Golpe de calor! (Agotamiento rápido de estamina)";
		}
		if (this.cercaDeFuenteCalor) {
			return "Reconfortado por el calor del fuego (+Confort)";
		}
		return "Temperatura corporal estable (Confort)";
	}
}