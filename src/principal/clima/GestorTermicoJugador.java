package principal.clima;

import principal.entes.efectos.TipoEfectoEstado;
import principal.iluminacion.FuenteLuz;
import principal.iluminacion.TipoLuz;
import principal.mapa.renderEntidades.camara.efectos.TipoEfectoCamara;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;

/**
 * Gestor de termorregulación, inercia térmica corporal, resistencia de prendas,
 * aporte calórico de antorcha en mano e índice de calor/frío por humedad
 * (Zero-GC / O(1)).
 * 
 * @version 3.1 (Vanilla Java 8 - Held Torch Heat & Humidity Integration)
 */
public class GestorTermicoJugador {

	public static final double TEMP_NOMINAL_CUERPO = 37.0;

	// Umbrales de Hipotermia Escalonada
	public static final double UMBRAL_HIPOTERMIA_NIVEL_1 = 35.0;
	public static final double UMBRAL_HIPOTERMIA_NIVEL_2 = 32.0;
	public static final double UMBRAL_HIPOTERMIA_NIVEL_3 = 28.0;

	// Umbrales de Hipertermia
	public static final double UMBRAL_HIPERTERMIA_LEVE = 38.5;
	public static final double UMBRAL_HIPERTERMIA_SEVERA = 40.0;

	// Umbrales de Hipertermia Escalonada
	public static final double UMBRAL_HIPERTERMIA_NIVEL_1 = 38.2;
	public static final double UMBRAL_HIPERTERMIA_NIVEL_2 = 39.5;
	public static final double UMBRAL_HIPERTERMIA_NIVEL_3 = 41.0;

	private static final double TIEMPO_RESIDUAL_RECUPERACION = 6.0;

	private double temperaturaCorporal = TEMP_NOMINAL_CUERPO;
	private double calorRecibidoFuego = 0.0;
	private double tendenciaTermica = 0.0;

	private boolean cercaDeFuenteCalor = false;
	private boolean expuestoAIntemperieFria = false;

	private final GestorTiempo GT_TEMBLOR_FRIO = new GestorTiempo();
	private final GestorTiempo GT_DANIO_EXTREMO_CALOR = new GestorTiempo();

	public GestorTermicoJugador() {
	}

	public void actualizar(final double dt) {
		if ((Globales.JUGADOR == null) || Globales.JUGADOR.estaEliminado()) {
			return;
		}

		final double jx = Globales.JUGADOR.getCentroX();
		final double jy = Globales.JUGADOR.getCentroY();

		// 1. Escaneo de radiación térmica (incluyendo antorcha en mano del jugador)
		this.escanearRadiacionLuces(jx, jy);

		// 2. Variables ambientales y climáticas base
		final double tempAmbiente = (Globales.GESTOR_CLIMA != null) ? Globales.GESTOR_CLIMA.getTemperaturaCelsius()
				: 20.0;
		final double humedad = (Globales.GESTOR_CLIMA != null) ? Globales.GESTOR_CLIMA.getHumedadRelativa() : 0.50;

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

		// 4. Aislamiento clasificado de prendas equipadas
		final int aislaFrio = Globales.JUGADOR.getAislamientoFrioTotal();
		final int aislaCalor = Globales.JUGADOR.getAislamientoCalorTotal();
		final int sofoco = Globales.JUGADOR.getPenalizacionSofocoTotal();

		final double factorMitigacionViento = Math.max(0.20, 1.0 - (aislaFrio * 0.05));
		final double vientoEfectivo = enfriamientoViento * factorMitigacionViento;

		// 5. Cálculo de Temperatura Efectiva Percibida (Integrando Humedad / Heat
		// Index)
		double tempPercibida;

		if (tempAmbiente < 10.0) {
			// Frío Húmedo: La humedad alta (>50%) penetra más rápido en la ropa
			final double penetracionHumedad = 1.0 + Math.max(0.0, (humedad - 0.50) * 0.35);
			final double frioEfectivo = (10.0 - tempAmbiente) * penetracionHumedad;
			final double tempBaseHumedad = 10.0 - frioEfectivo;

			tempPercibida = (tempBaseHumedad - vientoEfectivo) + (aislaFrio * 0.85) + this.calorRecibidoFuego;
		} else if (tempAmbiente > 27.0) {
			// Bochorno Húmedo: La humedad alta impide la transpiración (+0 a +3.5 °C
			// percibidos)
			final double bochornoHumedad = Math.max(0.0, (humedad - 0.50) * 7.0);

			tempPercibida = ((tempAmbiente + bochornoHumedad) - (aislaCalor * 0.85)) + (sofoco * 0.60)
					+ this.calorRecibidoFuego;
		} else {
			tempPercibida = tempAmbiente + this.calorRecibidoFuego;
		}

		// 6. Transferencia e inercia térmica
		double velocidadCambio = 0.055;

		if (this.cercaDeFuenteCalor) {
			velocidadCambio *= 2.5;
		} else if (this.expuestoAIntemperieFria) {
			velocidadCambio *= (nieve ? 2.2 : 1.8);
		}

		double tempObjetivoCuerpo = TEMP_NOMINAL_CUERPO;

		if (tempPercibida < 10.0) {
			final double deficitTermico = 10.0 - tempPercibida;
			tempObjetivoCuerpo = Math.max(20.0, TEMP_NOMINAL_CUERPO - (deficitTermico * 0.90));
		} else if (tempPercibida > 27.0) {
			final double excesoTermico = tempPercibida - 27.0;
			tempObjetivoCuerpo = Math.min(42.0, TEMP_NOMINAL_CUERPO + (excesoTermico * 0.55));
		}

		final double prevTemp = this.temperaturaCorporal;
		this.temperaturaCorporal += (tempObjetivoCuerpo - this.temperaturaCorporal) * (dt * velocidadCambio);
		this.tendenciaTermica = this.temperaturaCorporal - prevTemp;

		// 7. Conexión reactiva con Efectos de Estado
		this.actualizarEfectosEstadoAmbientales();
	}

	private void actualizarEfectosEstadoAmbientales() {
		if (this.temperaturaCorporal < UMBRAL_HIPOTERMIA_NIVEL_1) {
			final int nivelHipotermia;
			final double danioBasePorNivel;

			if (this.temperaturaCorporal < UMBRAL_HIPOTERMIA_NIVEL_3) {
				nivelHipotermia = 3;
				danioBasePorNivel = 1.50;

				if (this.GT_TEMBLOR_FRIO.transcurrioMiliSegundos(1600) && (Globales.CAMARA != null)) {
					Globales.CAMARA.aplicarTemblor(300, 1.8);
					this.GT_TEMBLOR_FRIO.establecerReferenciaTiempoActual();
				}
			} else if (this.temperaturaCorporal < UMBRAL_HIPOTERMIA_NIVEL_2) {
				nivelHipotermia = 2;
				danioBasePorNivel = 0.75;

				if (this.GT_TEMBLOR_FRIO.transcurrioMiliSegundos(3200) && (Globales.CAMARA != null)) {
					Globales.CAMARA.aplicarTemblor(200, 0.9);
					this.GT_TEMBLOR_FRIO.establecerReferenciaTiempoActual();
				}
			} else {
				nivelHipotermia = 1;
				danioBasePorNivel = 0.0;
			}

			Globales.JUGADOR.aplicarEfectoInfinito(TipoEfectoEstado.HIPOTERMIA, danioBasePorNivel, nivelHipotermia);

		} else if (Globales.JUGADOR.tieneEfectoActivo(TipoEfectoEstado.HIPOTERMIA)) {
			Globales.JUGADOR.finalizarEfectoInfinito(TipoEfectoEstado.HIPOTERMIA, TIEMPO_RESIDUAL_RECUPERACION);
		}

		if (this.temperaturaCorporal > UMBRAL_HIPERTERMIA_NIVEL_1) {
			final int nivelHipertermia;
			final double danioBasePorNivel;

			if (this.temperaturaCorporal >= UMBRAL_HIPERTERMIA_NIVEL_3) {
				nivelHipertermia = 3;
				danioBasePorNivel = 1.35;

				if ((Globales.CAMARA != null)
						&& !Globales.CAMARA.getGestorEfectos().getEfecto(TipoEfectoCamara.BORRACHO).isActivo()) {
					Globales.CAMARA.activarModoBorracho(true);
				}
			} else if (this.temperaturaCorporal >= UMBRAL_HIPERTERMIA_NIVEL_2) {
				nivelHipertermia = 2;
				danioBasePorNivel = 0.75;

				if ((Globales.CAMARA != null)
						&& !Globales.CAMARA.getGestorEfectos().getEfecto(TipoEfectoCamara.BORRACHO).isActivo()) {
					Globales.CAMARA.activarModoBorracho(true);
				}
			} else {
				nivelHipertermia = 1;
				danioBasePorNivel = 0.0;

				if ((Globales.CAMARA != null)
						&& Globales.CAMARA.getGestorEfectos().getEfecto(TipoEfectoCamara.BORRACHO).isActivo()) {
					Globales.CAMARA.activarModoBorracho(false);
				}
			}

			Globales.JUGADOR.aplicarEfectoInfinito(TipoEfectoEstado.HIPERTERMIA, danioBasePorNivel, nivelHipertermia);

		} else {
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
			if ((luz != null) && luz.isActiva()) {

				// 1. Antorcha sostenida en la mano secundaria del propio jugador
				if (luz.getEnteAnclado() == Globales.JUGADOR) {
					if (luz.getTipo() == TipoLuz.ANTORCHA) {
						this.calorRecibidoFuego = Math.max(this.calorRecibidoFuego, 12.0); // +12 °C radiantes directos
						this.cercaDeFuenteCalor = true;
					}
					// AURA_JUGADOR o LINTERNA_CONICA no aportan calor (0 °C)
				}
				// 2. Fuentes de calor externas en el mundo (Fogatas, antorchas de pared)
				else {
					final double dx = jx - luz.getPosX();
					final double dy = jy - luz.getPosY();
					final double dist = Math.sqrt((dx * dx) + (dy * dy));

					this.aportarCalor(luz.getTipo(), dist);
				}
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
		case FOGATA_AZUL:
			calorBase = 28.0;
			radioCalor = 100.0;
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