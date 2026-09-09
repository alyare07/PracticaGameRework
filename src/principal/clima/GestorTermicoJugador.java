package principal.clima;

import principal.entes.efectos.TipoEfectoEstado;
import principal.iluminacion.FuenteLuz;
import principal.iluminacion.TipoLuz;
import principal.mapa.renderEntidades.camara.efectos.TipoEfectoCamara;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;

/**
 * Gestor de termorregulación, inercia térmica corporal y conexión reactiva con
 * el sistema de Efectos de Estado infinitos y residuales (Zero-GC / O(1)).
 * 
 * @version 2.4 (Vanilla Java 8 - Enhanced Thermodynamic Curve & Tiers)
 */
public class GestorTermicoJugador {

	// =========================================================================
	// === 1. CONSTANTES TERMODINÁMICAS Y UMBRALES
	// =========================================================================

	public static final double TEMP_NOMINAL_CUERPO = 37.0; // 37.0 °C (Confort humano óptimo)

	// Umbrales de Hipotermia Escalonada
	public static final double UMBRAL_HIPOTERMIA_NIVEL_1 = 35.0; // Hipotermia I (Leve / Alerta)
	public static final double UMBRAL_HIPOTERMIA_NIVEL_2 = 32.0; // Hipotermia II (Moderada / Daño)
	public static final double UMBRAL_HIPOTERMIA_NIVEL_3 = 28.0; // Hipotermia III (Severa / Crítica)

	// Umbrales de Hipertermia
	public static final double UMBRAL_HIPERTERMIA_LEVE = 38.5;
	public static final double UMBRAL_HIPERTERMIA_SEVERA = 40.0;

	// Umbrales de Hipertermia Escalonada
	public static final double UMBRAL_HIPERTERMIA_NIVEL_1 = 38.2; // Nivel 1: Bochorno / Fatiga
	public static final double UMBRAL_HIPERTERMIA_NIVEL_2 = 39.5; // Nivel 2: Agotamiento / Mareo
	public static final double UMBRAL_HIPERTERMIA_NIVEL_3 = 41.0; // Nivel 3: Golpe de Calor Crítico

	private static final double TIEMPO_RESIDUAL_RECUPERACION = 6.0; // 6 segundos de transición

	// =========================================================================
	// === 2. ESTADO TÉRMICO
	// =========================================================================

	private double temperaturaCorporal = TEMP_NOMINAL_CUERPO;
	private double calorRecibidoFuego = 0.0;
	private double tendenciaTermica = 0.0;

	private boolean cercaDeFuenteCalor = false;
	private boolean expuestoAIntemperieFria = false;

	private final GestorTiempo GT_TEMBLOR_FRIO = new GestorTiempo();
	private final GestorTiempo GT_DANIO_EXTREMO_CALOR = new GestorTiempo();

	public GestorTermicoJugador() {
	}

	// =========================================================================
	// === 3. CICLO DE ACTUALIZACIÓN (60 APS)
	// =========================================================================

	public void actualizar(final double dt) {
		if ((Globales.JUGADOR == null) || Globales.JUGADOR.estaEliminado()) {
			return;
		}

		final double jx = Globales.JUGADOR.getCentroX();
		final double jy = Globales.JUGADOR.getCentroY();

		// 1. Escaneo de radiación térmica de luces activas (fogatas, antorchas)
		this.escanearRadiacionLuces(jx, jy);

		// 2. Temperatura ambiental base desde GestorClima
		final double tempAmbiente = (Globales.GESTOR_CLIMA != null) ? Globales.GESTOR_CLIMA.getTemperaturaCelsius()
				: 20.0;

		final TipoClima clima = (Globales.GESTOR_CLIMA != null) ? Globales.GESTOR_CLIMA.getClimaActual()
				: TipoClima.DESPEJADO;

		final boolean lluvia = (clima == TipoClima.LLUVIA_LEVE) || (clima == TipoClima.LLUVIA_TORMENTA)
				|| (clima == TipoClima.LLUVIA_ACIDA);
		final boolean nieve = (clima == TipoClima.NIEVE) || (clima == TipoClima.VENTISCA);

		this.expuestoAIntemperieFria = lluvia || nieve;

		// 3. Sensación térmica por viento (Wind Chill)
		double enfriamientoViento = 0.0;
		if ((tempAmbiente < 20.0) && (Globales.GESTOR_CLIMA != null)) {
			enfriamientoViento = Globales.GESTOR_CLIMA.getFuerzaViento() * 1.2;
		}

		// 4. Aislamiento térmico de prendas equipadas
		final int aislamientoPrendas = Globales.JUGADOR.getAislamientoTermicoEquipo();

		// 5. Temperatura efectiva percibida por el cuerpo
		final double tempPercibida = (tempAmbiente - enfriamientoViento) + this.calorRecibidoFuego + aislamientoPrendas;

		// 6. Transferencia e inercia térmica
		double velocidadCambio = 0.055; // Tasa base de intercambio térmico

		if (this.cercaDeFuenteCalor) {
			// El cuerpo absorbe calor mucho más rápido cerca de una fogata
			velocidadCambio *= 2.5;
		} else if (this.expuestoAIntemperieFria) {
			// Mojarse o estar bajo nieve acelera el enfriamiento corporal
			velocidadCambio *= (nieve ? 2.2 : 1.8);
		}

		// Curva termodinámica con rango de Homeostasis Metabólica
		double tempObjetivoCuerpo = TEMP_NOMINAL_CUERPO;

		if (tempPercibida < 10.0) {
			// Pérdida de calor cuando la sensación térmica cae por debajo de 10 °C
			final double deficitTermico = 10.0 - tempPercibida;
			tempObjetivoCuerpo = Math.max(20.0, TEMP_NOMINAL_CUERPO - (deficitTermico * 0.90));

		} else if (tempPercibida > 27.0) {
			// Sobrecalentamiento cuando la sensación térmica supera los 27 °C
			final double excesoTermico = tempPercibida - 27.0;
			tempObjetivoCuerpo = Math.min(42.0, TEMP_NOMINAL_CUERPO + (excesoTermico * 0.55));
		}

		final double prevTemp = this.temperaturaCorporal;
		this.temperaturaCorporal += (tempObjetivoCuerpo - this.temperaturaCorporal) * (dt * velocidadCambio);
		this.tendenciaTermica = this.temperaturaCorporal - prevTemp;

		// 7. Conexión reactiva con el Motor de Efectos de Estado
		this.actualizarEfectosEstadoAmbientales();
	}

	private void actualizarEfectosEstadoAmbientales() {
		// =====================================================================
		// A. GESTIÓN DE HIPOTERMIA ESCALONADA (NIVELES I, II, III)
		// =====================================================================
		if (this.temperaturaCorporal < UMBRAL_HIPOTERMIA_NIVEL_1) {
			final int nivelHipotermia;
			final double danioBasePorNivel;

			if (this.temperaturaCorporal < UMBRAL_HIPOTERMIA_NIVEL_3) {
				// Nivel 3: Congelación Crítica (< 28.0 °C)
				nivelHipotermia = 3;
				danioBasePorNivel = 1.50; // 1.50 * 3 = 4.5 HP Daño Directo cada tick (2 seg)

				if (this.GT_TEMBLOR_FRIO.transcurrioMiliSegundos(1600) && (Globales.CAMARA != null)) {
					Globales.CAMARA.aplicarTemblor(300, 1.8);
					this.GT_TEMBLOR_FRIO.establecerReferenciaTiempoActual();
				}

			} else if (this.temperaturaCorporal < UMBRAL_HIPOTERMIA_NIVEL_2) {
				// Nivel 2: Hipotermia Moderada (28.0 °C a 32.0 °C)
				nivelHipotermia = 2;
				danioBasePorNivel = 0.75; // 0.75 * 2 = 1.5 HP Daño Directo cada tick (2 seg)

				if (this.GT_TEMBLOR_FRIO.transcurrioMiliSegundos(3200) && (Globales.CAMARA != null)) {
					Globales.CAMARA.aplicarTemblor(200, 0.9);
					this.GT_TEMBLOR_FRIO.establecerReferenciaTiempoActual();
				}

			} else {
				// Nivel 1: Alerta / Enfriamiento (32.0 °C a 35.0 °C)
				nivelHipotermia = 1;
				danioBasePorNivel = 0.0; // Sin daño directo en Nivel 1 (da tiempo a buscar refugio)
			}

			// Aplicar debuff infinito con la cantidad de stacks según el nivel
			Globales.JUGADOR.aplicarEfectoInfinito(TipoEfectoEstado.HIPOTERMIA, danioBasePorNivel, nivelHipotermia);

		} else // Si la temperatura se normaliza (>= 35.0 °C), transiciona a tiempo residual (6
				// seg)
		if (Globales.JUGADOR.tieneEfectoActivo(TipoEfectoEstado.HIPOTERMIA)) {
			Globales.JUGADOR.finalizarEfectoInfinito(TipoEfectoEstado.HIPOTERMIA, TIEMPO_RESIDUAL_RECUPERACION);
		}
		// =====================================================================
		// B. GESTIÓN DE HIPERTERMIA ESCALONADA (CALOR NIVELES I, II, III)
		// =====================================================================
		if (this.temperaturaCorporal > UMBRAL_HIPERTERMIA_NIVEL_1) {
			final int nivelHipertermia;
			final double danioBasePorNivel;

			if (this.temperaturaCorporal >= UMBRAL_HIPERTERMIA_NIVEL_3) {
				// Nivel 3: Golpe de Calor Crítico (>= 41.0 °C)
				nivelHipertermia = 3;
				danioBasePorNivel = 1.35; // 1.35 * 3 ≈ 4.05 HP Daño Directo cada tick (2 seg)

				// Efecto borracho / distorsión permanente
				if ((Globales.CAMARA != null)
						&& !Globales.CAMARA.getGestorEfectos().getEfecto(TipoEfectoCamara.BORRACHO).isActivo()) {
					Globales.CAMARA.activarModoBorracho(true);
				}

			} else if (this.temperaturaCorporal >= UMBRAL_HIPERTERMIA_NIVEL_2) {
				// Nivel 2: Agotamiento Térmico (39.5 °C a 41.0 °C)
				nivelHipertermia = 2;
				danioBasePorNivel = 0.75; // 0.75 * 2 = 1.5 HP Daño Directo cada tick (2 seg)

				// Efecto borracho intermitente por sofoco
				if ((Globales.CAMARA != null)
						&& !Globales.CAMARA.getGestorEfectos().getEfecto(TipoEfectoCamara.BORRACHO).isActivo()) {
					Globales.CAMARA.activarModoBorracho(true);
				}

			} else {
				// Nivel 1: Bochorno / Fatiga (38.2 °C a 39.5 °C)
				nivelHipertermia = 1;
				danioBasePorNivel = 0.0; // Sin daño directo en Nivel 1 (solo fatiga de estamina)

				if ((Globales.CAMARA != null)
						&& Globales.CAMARA.getGestorEfectos().getEfecto(TipoEfectoCamara.BORRACHO).isActivo()) {
					Globales.CAMARA.activarModoBorracho(false);
				}
			}

			// Aplicar debuff infinito con stacks según el nivel
			Globales.JUGADOR.aplicarEfectoInfinito(TipoEfectoEstado.HIPERTERMIA, danioBasePorNivel, nivelHipertermia);

		} else {
			// Si la temperatura baja de 38.2 °C, transiciona a tiempo residual (6 seg)
			if (Globales.JUGADOR.tieneEfectoActivo(TipoEfectoEstado.HIPERTERMIA)) {
				Globales.JUGADOR.finalizarEfectoInfinito(TipoEfectoEstado.HIPERTERMIA, TIEMPO_RESIDUAL_RECUPERACION);
			}
			if ((Globales.CAMARA != null)
					&& Globales.CAMARA.getGestorEfectos().getEfecto(TipoEfectoCamara.BORRACHO).isActivo()) {
				Globales.CAMARA.activarModoBorracho(false);
			}
		}
	}

	private void escanearRadiacionLuces(final double jx, final double jy) {
		this.calorRecibidoFuego = 0.0;
		this.cercaDeFuenteCalor = false;

		if (Globales.GESTOR_LUZ == null) {
			return;
		}

		final int totalLuces = Globales.GESTOR_LUZ.getCantidadActivas();
		for (int i = 0; i < totalLuces; i++) {
			final FuenteLuz luz = Globales.GESTOR_LUZ.getLuzPorIndice(i);
			if ((luz != null) && luz.isActiva() && (luz.getEnteAnclado() != Globales.JUGADOR)) {
				final double dx = jx - luz.getPosX();
				final double dy = jy - luz.getPosY();
				final double dist = Math.sqrt((dx * dx) + (dy * dy));

				this.aportarCalor(luz.getTipo(), dist);
			}
		}
	}

	public void aportarCalor(final TipoLuz tipo, final double distancia) {
		if (tipo == null) {
			return;
		}

		double calorBase = 0.0;
		double radioCalor = 100.0;

		switch (tipo) {
		case FOGATA:
			calorBase = 28.0;
			radioCalor = 140.0;
			break;
		case ANTORCHA:
			calorBase = 14.0;
			radioCalor = 70.0;
			break;
		case BOLA_FUEGO:
			calorBase = 16.0;
			radioCalor = 85.0;
			break;
		case VELA_TENUE:
			calorBase = 5.0;
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
	// === GETTERS Y SETTERS
	// =========================================================================

	public double getTemperaturaCorporal() {
		return this.temperaturaCorporal;
	}

	public double getTendenciaTermica() {
		return this.tendenciaTermica;
	}

	public boolean isHipotermia() {
		return this.temperaturaCorporal < UMBRAL_HIPOTERMIA_NIVEL_1;
	}

	public boolean isHipotermiaSevera() {
		return this.temperaturaCorporal < UMBRAL_HIPOTERMIA_NIVEL_3;
	}

	public boolean isHipertermia() {
		return this.temperaturaCorporal > UMBRAL_HIPERTERMIA_LEVE;
	}

	public boolean isCercaDeFuenteCalor() {
		return this.cercaDeFuenteCalor;
	}

	public boolean isExpuestoAIntemperieFria() {
		return this.expuestoAIntemperieFria;
	}

	public void setTemperaturaCorporal(final double temp) {
		this.temperaturaCorporal = Math.max(18.0, Math.min(45.0, temp));
	}

	public void restablecerTemperaturaNominal() {
		this.temperaturaCorporal = TEMP_NOMINAL_CUERPO;
	}
}