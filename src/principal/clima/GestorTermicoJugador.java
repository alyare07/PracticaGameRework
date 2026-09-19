package principal.clima;

import principal.entes.efectos.TipoEfectoEstado;
import principal.iluminacion.FuenteLuz;
import principal.iluminacion.TipoLuz;
import principal.mapa.renderEntidades.camara.efectos.TipoEfectoCamara;
import principal.maquinaestado.estados.editor.metadatos.MetadatosEscenario;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;

/**
 * Gestor de termorregulación, inercia térmica corporal, resistencia de prendas,
 * refugio bajo techo en interiores y modulación termodinámica según dificultad
 * de partida (Zero-GC / O(1)).
 * 
 * @version 5.0 (Vanilla Java 8 - Difficulty-Scaled Thermodynamics)
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
	private boolean bajoTechoInterior = false;

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

		// 1. Detección de espacio interior / refugio bajo techo
		this.bajoTechoInterior = false;
		double tempBaseEspacio = 20.0;

		if ((Globales.JUGADOR.getMundo() != null) && (Globales.JUGADOR.getMundo().getEscenario() != null)) {
			final MetadatosEscenario meta = Globales.JUGADOR.getMundo().getEscenario().getMetadatos();
			if (meta != null) {
				this.bajoTechoInterior = meta.esEspacioInterior();
				if (meta.getPerfilBioma() != null) {
					tempBaseEspacio = meta.getPerfilBioma().getTemperaturaBase();
				}
			}
		}

		// 2. Escaneo de radiación térmica (fuegos del mundo y antorcha sostenida)
		this.escanearRadiacionLuces(jx, jy);

		// 3. Variables climáticas exteriores
		final double tempExterior = (Globales.GESTOR_CLIMA != null) ? Globales.GESTOR_CLIMA.getTemperaturaCelsius()
				: 20.0;
		final double humedad = (Globales.GESTOR_CLIMA != null) ? Globales.GESTOR_CLIMA.getHumedadRelativa() : 0.50;

		final TipoClima clima = (Globales.GESTOR_CLIMA != null) ? Globales.GESTOR_CLIMA.getClimaActual()
				: TipoClima.DESPEJADO;

		final boolean lluvia = (clima == TipoClima.LLUVIA_LEVE) || (clima == TipoClima.LLUVIA_TORMENTA)
				|| (clima == TipoClima.LLUVIA_ACIDA);
		final boolean nieve = (clima == TipoClima.NIEVE) || (clima == TipoClima.VENTISCA);

		// Si estás bajo techo, NO te mojas ni estás expuesto a la intemperie
		this.expuestoAIntemperieFria = !this.bajoTechoInterior && (lluvia || nieve);

		// 4. Aislamiento de prendas
		final int aislaFrio = Globales.JUGADOR.getAislamientoFrioTotal();
		final int aislaCalor = Globales.JUGADOR.getAislamientoCalorTotal();
		final int sofoco = Globales.JUGADOR.getPenalizacionSofocoTotal();

		// 5. Cálculo de Temperatura Efectiva Percibida
		double tempPercibida;

		// =====================================================================
		// CASO A: BAJO TECHO (Refugio Interior - Protegido de viento y precipitación)
		// =====================================================================
		if (this.bajoTechoInterior) {
			tempPercibida = tempBaseEspacio + this.calorRecibidoFuego;
			if (tempBaseEspacio > 27.0) {
				tempPercibida += (sofoco * 0.50);
			}
		}
		// =====================================================================
		// CASO B: EN EL EXTERIOR (A la intemperie)
		// =====================================================================
		else {
			// Multiplicador de convección eólica según el tipo de tormenta
			double multViento = 1.2;
			if (clima == TipoClima.VENTISCA) {
				multViento = 2.2; // El viento polar a 15° roba calor a velocidad extrema
			} else if (clima == TipoClima.LLUVIA_TORMENTA) {
				multViento = 1.6; // Ráfagas con gotas gruesas aceleran la pérdida
			}

			double enfriamientoViento = 0.0;
			if ((tempExterior < 20.0) && (Globales.GESTOR_CLIMA != null)) {
				enfriamientoViento = Globales.GESTOR_CLIMA.getFuerzaViento() * multViento;
			}

			final double factorMitigacionViento = Math.max(0.20, 1.0 - (aislaFrio * 0.05));
			final double vientoEfectivo = enfriamientoViento * factorMitigacionViento;

			// Penalización por empapado / calado de agua y escarcha
			double penalizacionCalado = 0.0;
			switch (clima) {
			case VENTISCA:
				penalizacionCalado = 6.5; // Escarcha ártica directa en la piel
				break;
			case LLUVIA_TORMENTA:
				penalizacionCalado = 4.5; // Ropa empapada hasta las costuras (Soaked)
				break;
			case LLUVIA_ACIDA:
				penalizacionCalado = 3.0;
				break;
			case NIEVE:
				penalizacionCalado = 2.0; // Nieve seca moderada
				break;
			case LLUVIA_LEVE:
				penalizacionCalado = 1.2; // Humedad ligera superficial
				break;
			default:
				break;
			}

			final double factorMitigacionCalado = Math.max(0.15, 1.0 - (aislaFrio * 0.04));
			final double caladoEfectivo = penalizacionCalado * factorMitigacionCalado;

			if (tempExterior < 10.0) {
				final double penetracionHumedad = 1.0 + Math.max(0.0, (humedad - 0.50) * 0.35);
				final double frioEfectivo = (10.0 - tempExterior) * penetracionHumedad;
				final double tempBaseHumedad = 10.0 - frioEfectivo;

				tempPercibida = (tempBaseHumedad - vientoEfectivo - caladoEfectivo) + (aislaFrio * 0.85)
						+ this.calorRecibidoFuego;
			} else if (tempExterior > 27.0) {
				final double bochornoHumedad = Math.max(0.0, (humedad - 0.50) * 7.0);

				// Convección eólica caliente: en tormenta de arena el viento a >35°C actúa como
				// secador de pelo
				double conveccionVientoCaliente = 0.0;
				if ((clima == TipoClima.TORMENTA_ARENA) && (Globales.GESTOR_CLIMA != null)) {
					conveccionVientoCaliente = Globales.GESTOR_CLIMA.getFuerzaViento() * 1.8;
				}

				tempPercibida = ((tempExterior + bochornoHumedad + conveccionVientoCaliente) - (aislaCalor * 0.85))
						+ (sofoco * 0.60) + this.calorRecibidoFuego;
			} else {
				// Clima fresco (10°C - 20°C): El empapado y el viento siguen enfriando al
				// jugador
				tempPercibida = (tempExterior - vientoEfectivo - caladoEfectivo) + this.calorRecibidoFuego;
			}
		}

		// 6. Transferencia e inercia térmica modulada por dificultad y severidad
		// climática
		final double factorInercia = (Globales.dificultad != null) ? Globales.dificultad.getFactorInerciaTermica()
				: 1.0;
		double velocidadCambio = 0.055 * factorInercia;

		if (this.cercaDeFuenteCalor) {
			velocidadCambio *= 2.5; // El fuego recalienta rápido
		} else if (this.expuestoAIntemperieFria) {
			double multiplicadorIntemperie;
			switch (clima) {
			case VENTISCA:
				multiplicadorIntemperie = 3.8;
				break;
			case LLUVIA_TORMENTA:
				multiplicadorIntemperie = 2.5;
				break;
			case NIEVE:
			case LLUVIA_ACIDA:
				multiplicadorIntemperie = 2.0;
				break;
			case LLUVIA_LEVE:
			default:
				multiplicadorIntemperie = 1.5;
				break;
			}
			velocidadCambio *= multiplicadorIntemperie;
			// NUEVO: La exposición directa al vendaval de arena a más de 30°C acelera el
			// golpe de calor (Hipertermia)
		} else if ((clima == TipoClima.TORMENTA_ARENA) && !this.bajoTechoInterior && (tempExterior > 30.0)) {
			velocidadCambio *= 2.4;
		} else if (this.bajoTechoInterior) {
			velocidadCambio *= 1.5;
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

		// 7. Conexión reactiva con Efectos de Estado (Hipotermia / Hipertermia /
		// Temblor de cámara)
		this.actualizarEfectosEstadoAmbientales();
	}

	private void actualizarEfectosEstadoAmbientales() {
		// =====================================================================
		// 1. HIPOTERMIA ESCALONADA SEGÚN DIFICULTAD
		// =====================================================================
		if (this.temperaturaCorporal < UMBRAL_HIPOTERMIA_NIVEL_1) {
			final int nivelHipotermia;

			if (this.temperaturaCorporal < UMBRAL_HIPOTERMIA_NIVEL_3) {
				nivelHipotermia = 3;
				if (this.GT_TEMBLOR_FRIO.transcurrioMiliSegundos(1600) && (Globales.CAMARA != null)) {
					Globales.CAMARA.aplicarTemblor(300, 1.8);
					this.GT_TEMBLOR_FRIO.establecerReferenciaTiempoActual();
				}
			} else if (this.temperaturaCorporal < UMBRAL_HIPOTERMIA_NIVEL_2) {
				nivelHipotermia = 2;
				if (this.GT_TEMBLOR_FRIO.transcurrioMiliSegundos(3200) && (Globales.CAMARA != null)) {
					Globales.CAMARA.aplicarTemblor(200, 0.9);
					this.GT_TEMBLOR_FRIO.establecerReferenciaTiempoActual();
				}
			} else {
				nivelHipotermia = 1;
			}

			// Obtenemos el daño total deseado para la dificultad activa
			final double danioFinalDeseado = (Globales.dificultad != null)
					? Globales.dificultad.getDanioHipotermia(nivelHipotermia)
					: (nivelHipotermia == 3 ? 2.50 : (nivelHipotermia == 2 ? 1.00 : 0.25));

			// Compensación: EfectoEstado multiplica (potencia * stacks), por lo que
			// dividimos entre el nivel
			final double potenciaEfecto = danioFinalDeseado / nivelHipotermia;

			Globales.JUGADOR.aplicarEfectoInfinito(TipoEfectoEstado.HIPOTERMIA, potenciaEfecto, nivelHipotermia);

		} else if (Globales.JUGADOR.tieneEfectoActivo(TipoEfectoEstado.HIPOTERMIA)) {
			Globales.JUGADOR.finalizarEfectoInfinito(TipoEfectoEstado.HIPOTERMIA, TIEMPO_RESIDUAL_RECUPERACION);
		}

		// =====================================================================
		// 2. HIPERTERMIA ESCALONADA SEGÚN DIFICULTAD
		// =====================================================================
		if (this.temperaturaCorporal > UMBRAL_HIPERTERMIA_NIVEL_1) {
			final int nivelHipertermia;

			if (this.temperaturaCorporal >= UMBRAL_HIPERTERMIA_NIVEL_3) {
				nivelHipertermia = 3;
				if ((Globales.CAMARA != null)
						&& !Globales.CAMARA.getGestorEfectos().getEfecto(TipoEfectoCamara.BORRACHO).isActivo()) {
					Globales.CAMARA.activarModoBorracho(true);
				}
			} else if (this.temperaturaCorporal >= UMBRAL_HIPERTERMIA_NIVEL_2) {
				nivelHipertermia = 2;
				if ((Globales.CAMARA != null)
						&& !Globales.CAMARA.getGestorEfectos().getEfecto(TipoEfectoCamara.BORRACHO).isActivo()) {
					Globales.CAMARA.activarModoBorracho(true);
				}
			} else {
				nivelHipertermia = 1;
				if ((Globales.CAMARA != null)
						&& Globales.CAMARA.getGestorEfectos().getEfecto(TipoEfectoCamara.BORRACHO).isActivo()) {
					Globales.CAMARA.activarModoBorracho(false);
				}
			}

			final double danioFinalDeseado = (Globales.dificultad != null)
					? Globales.dificultad.getDanioHipotermia(nivelHipertermia)
					: (nivelHipertermia == 3 ? 2.50 : (nivelHipertermia == 2 ? 1.00 : 0.25));

			final double potenciaEfecto = danioFinalDeseado / nivelHipertermia;

			Globales.JUGADOR.aplicarEfectoInfinito(TipoEfectoEstado.HIPERTERMIA, potenciaEfecto, nivelHipertermia);

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

				if (luz.getEnteAnclado() == Globales.JUGADOR) {
					if (luz.getTipo() == TipoLuz.ANTORCHA) {
						this.calorRecibidoFuego = Math.max(this.calorRecibidoFuego, 12.0);
						this.cercaDeFuenteCalor = true;
					}
				} else {
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

	public boolean isBajoTechoInterior() {
		return this.bajoTechoInterior;
	}

	public void setTemperaturaCorporal(final double temp) {
		this.temperaturaCorporal = Math.max(18.0, Math.min(45.0, temp));
	}

	public void restablecerTemperaturaNominal() {
		this.temperaturaCorporal = TEMP_NOMINAL_CUERPO;
	}
}