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
 * @version 2.2 (Vanilla Java 8 - Status Effect Integration)
 */
public class GestorTermicoJugador {

	// =========================================================================
	// === 1. CONSTANTES TERMODINÁMICAS Y UMBRALES
	// =========================================================================

	public static final double TEMP_NOMINAL_CUERPO = 37.0; // 37.0 °C (Confort humano óptimo)
	public static final double UMBRAL_HIPOTERMIA_LEVE = 35.0; // Debuff Hipotermia
	public static final double UMBRAL_HIPOTERMIA_SEVERA = 32.0; // Congelación y daño
	public static final double UMBRAL_HIPERTERMIA_LEVE = 38.5; // Debuff Hipertermia
	public static final double UMBRAL_HIPERTERMIA_SEVERA = 40.0; // Golpe de calor extremo

	private static final double TIEMPO_RESIDUAL_RECUPERACION = 6.0; // 6 segundos de transición tras abrigarse

	// =========================================================================
	// === 2. ESTADO TÉRMICO
	// =========================================================================

	private double temperaturaCorporal = TEMP_NOMINAL_CUERPO;
	private double calorRecibidoFuego = 0.0;
	private double tendenciaTermica = 0.0;

	private boolean cercaDeFuenteCalor = false;
	private boolean expuestoALluvia = false;

	private final GestorTiempo GT_DANIO_EXTREMO = new GestorTiempo();
	private final GestorTiempo GT_TEMBLOR_FRIO = new GestorTiempo();

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

		// 1. Escaneo de radiación térmica de luces activas
		this.escanearRadiacionLuces(jx, jy);

		// 2. Temperatura ambiental base y clima
		final double tempAmbiente = (Globales.GESTOR_CLIMA != null) ? Globales.GESTOR_CLIMA.getTemperaturaCelsius()
				: 20.0;

		final boolean llueve = (Globales.GESTOR_CLIMA != null)
				&& ((Globales.GESTOR_CLIMA.getClimaActual() == TipoClima.LLUVIA_LEVE)
						|| (Globales.GESTOR_CLIMA.getClimaActual() == TipoClima.LLUVIA_TORMENTA)
						|| (Globales.GESTOR_CLIMA.getClimaActual() == TipoClima.LLUVIA_ACIDA));

		this.expuestoALluvia = llueve;

		// 3. Aislamiento térmico de prendas
		final int aislamientoPrendas = Globales.JUGADOR.getAislamientoTermicoEquipo();

		// 4. Temperatura efectiva percibida
		final double tempPercibida = tempAmbiente + this.calorRecibidoFuego + aislamientoPrendas;

		// 5. Inercia térmica corporal
		double velocidadCambio = 0.045;
		if ((tempPercibida < 15.0) && this.expuestoALluvia) {
			velocidadCambio *= 2.0;
		}

		double tempObjetivoCuerpo = TEMP_NOMINAL_CUERPO;
		if (tempPercibida < 19.0) {
			tempObjetivoCuerpo = Math.max(26.0, TEMP_NOMINAL_CUERPO - ((19.0 - tempPercibida) * 0.55));
		} else if (tempPercibida > 29.0) {
			tempObjetivoCuerpo = Math.min(42.0, TEMP_NOMINAL_CUERPO + ((tempPercibida - 29.0) * 0.40));
		}

		final double prevTemp = this.temperaturaCorporal;
		this.temperaturaCorporal += (tempObjetivoCuerpo - this.temperaturaCorporal) * (dt * velocidadCambio);
		this.tendenciaTermica = this.temperaturaCorporal - prevTemp;

		// 6. Conexión Reactiva con el Motor de Efectos de Estado
		this.actualizarEfectosEstadoAmbientales();
	}

	private void actualizarEfectosEstadoAmbientales() {
		// A. GESTIÓN DE HIPOTERMIA (Frío)
		if (this.temperaturaCorporal < UMBRAL_HIPOTERMIA_LEVE) {
			// Condición activa -> Efecto Infinito mientras esté frío
			Globales.JUGADOR.aplicarEfectoInfinito(TipoEfectoEstado.HIPOTERMIA, 1.0);

			if (this.isHipotermiaSevera()) {
				if (this.GT_DANIO_EXTREMO.transcurrioMiliSegundos(2500)) {
					Globales.JUGADOR.recibirAtaque(2.0, null);
					this.GT_DANIO_EXTREMO.establecerReferenciaTiempoActual();
				}
				if (this.GT_TEMBLOR_FRIO.transcurrioMiliSegundos(3500) && (Globales.CAMARA != null)) {
					Globales.CAMARA.aplicarTemblor(250, 1.0);
					this.GT_TEMBLOR_FRIO.establecerReferenciaTiempoActual();
				}
			}
		} else // Condición regulada -> Transiciona a cuenta regresiva residual (6 seg)
		if (Globales.JUGADOR.tieneEfectoActivo(TipoEfectoEstado.HIPOTERMIA)) {
			Globales.JUGADOR.finalizarEfectoInfinito(TipoEfectoEstado.HIPOTERMIA, TIEMPO_RESIDUAL_RECUPERACION);
		}

		// B. GESTIÓN DE HIPERTERMIA (Calor)
		if (this.temperaturaCorporal > UMBRAL_HIPERTERMIA_LEVE) {
			Globales.JUGADOR.aplicarEfectoInfinito(TipoEfectoEstado.HIPERTERMIA, 1.0);

			if (this.temperaturaCorporal > UMBRAL_HIPERTERMIA_SEVERA) {
				if (this.GT_DANIO_EXTREMO.transcurrioMiliSegundos(3000)) {
					Globales.JUGADOR.recibirAtaque(2.0, null);
					this.GT_DANIO_EXTREMO.establecerReferenciaTiempoActual();
				}
				if ((Globales.CAMARA != null)
						&& !Globales.CAMARA.getGestorEfectos().getEfecto(TipoEfectoCamara.BORRACHO).isActivo()) {
					Globales.CAMARA.activarModoBorracho(true);
				}
			}
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
	// === GETTERS Y SETTERS
	// =========================================================================

	public double getTemperaturaCorporal() {
		return this.temperaturaCorporal;
	}

	public double getTendenciaTermica() {
		return this.tendenciaTermica;
	}

	public boolean isHipotermia() {
		return this.temperaturaCorporal < UMBRAL_HIPOTERMIA_LEVE;
	}

	public boolean isHipotermiaSevera() {
		return this.temperaturaCorporal < UMBRAL_HIPOTERMIA_SEVERA;
	}

	public boolean isHipertermia() {
		return this.temperaturaCorporal > UMBRAL_HIPERTERMIA_LEVE;
	}

	public boolean isCercaDeFuenteCalor() {
		return this.cercaDeFuenteCalor;
	}

	public boolean isExpuestoALluvia() {
		return this.expuestoALluvia;
	}

	public void setTemperaturaCorporal(final double temp) {
		this.temperaturaCorporal = Math.max(25.0, Math.min(45.0, temp));
	}

	public void restablecerTemperaturaNominal() {
		this.temperaturaCorporal = TEMP_NOMINAL_CUERPO;
	}
}