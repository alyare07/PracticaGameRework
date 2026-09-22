package principal.entes.criaturas.jugador;

import principal.entes.criaturas.Criatura.Estado;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;

/**
 * Gestor metabólico de fisiología interna del jugador: digestión de calorías
 * (hambre), balance hídrico (sed), daño letal por inanición y aceleración por
 * esfuerzo físico o severidad térmica (Zero-GC / O(1)).
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class GestorMetabolismoJugador {

	public static final double MAX_VALOR = 100.0;

	// Tasas base de consumo por hora in-game (24h solares = 1800s reales)
	// Hambre: dura ~25 horas de juego sin comer
	public static final double TASA_BASE_HAMBRE_HORA = 4.0;
	// Sed: dura ~15.4 horas de juego sin beber
	public static final double TASA_BASE_SED_HORA = 6.5;

	// Umbrales de advertencia
	public static final double UMBRAL_ALERTA_LEVE = 25.0;
	public static final double UMBRAL_CRITICO = 0.0;

	private double hambre = MAX_VALOR;
	private double sed = MAX_VALOR;
	private boolean simulacionHabilitada = true;

	// Cadencia de daño por inanición (cada 3 segundos reales)
	private final GestorTiempo gtDanioInanicion = new GestorTiempo();
	private static final int INTERVALO_MS_INANICION = 3000;

	public GestorMetabolismoJugador() {
	}

	public void actualizar(final double dt) {
		if (!this.simulacionHabilitada) {
			return;
		}

		if ((Globales.JUGADOR == null) || Globales.JUGADOR.estaEliminado() || (Globales.GESTOR_ASTRONOMICO == null)) {
			return;
		}

		// 1. Conversión de tiempo real (segundos) a horas solares in-game
		final double duracionDia = Math.max(10.0, Globales.GESTOR_ASTRONOMICO.getDuracionDiaSegundos());
		final double horasPorSegundo = (24.0 / duracionDia)
				* (Globales.GESTOR_ASTRONOMICO.isTiempoPausado() ? 0.0 : 1.0);
		final double deltaHoras = dt * horasPorSegundo;

		if (deltaHoras <= 0.0) {
			return;
		}

		// 2. Multiplicadores de dificultad activa
		final double multDifHambre = (Globales.dificultad != null) ? Globales.dificultad.getMultGastoHambre() : 1.0;
		final double multDifSed = (Globales.dificultad != null) ? Globales.dificultad.getMultGastoSed() : 1.0;

		// 3. Multiplicadores por esfuerzo físico (Correr acelera el gasto)
		double multEsfuerzoHambre = 1.0;
		double multEsfuerzoSed = 1.0;

		if (Globales.JUGADOR.tieneEstado(Estado.CORRIENDO)) {
			multEsfuerzoHambre = 1.45;
			multEsfuerzoSed = 1.65;
		}

		// 4. Modulación termodinámica cruzada (Unidireccional desde GestorTermicoJugador)
		double multTermicoHambre = 1.0;
		double multTermicoSed = 1.0;

		if (Globales.GESTOR_TERMICO_JUGADOR != null) {
			// El temblor muscular por frío (Hipotermia) consume calorías para generar calor
			if (Globales.GESTOR_TERMICO_JUGADOR.isHipotermia()) {
				multTermicoHambre = Globales.GESTOR_TERMICO_JUGADOR.isHipotermiaSevera() ? 2.0 : 1.5;
			}

			// La hipertermia / bochorno acelera drásticamente la deshidratación por sudoración
			if (Globales.GESTOR_TERMICO_JUGADOR.isHipertermia()) {
				multTermicoSed = 2.4;
			}
		}

		// Tormenta de arena o calor seco extremo en el exterior
		if ((Globales.GESTOR_CLIMA != null) && (Globales.GESTOR_CLIMA.getTemperaturaCelsius() > 30.0)) {
			multTermicoSed = Math.max(multTermicoSed, 1.8);
		}

		// 5. Aplicación del decaimiento continuo
		final double gastoHambre = TASA_BASE_HAMBRE_HORA * multDifHambre * multEsfuerzoHambre * multTermicoHambre
				* deltaHoras;
		final double gastoSed = TASA_BASE_SED_HORA * multDifSed * multEsfuerzoSed * multTermicoSed * deltaHoras;

		this.hambre = Math.max(0.0, this.hambre - gastoHambre);
		this.sed = Math.max(0.0, this.sed - gastoSed);

		// 6. Proceso de daño por inanición a 0% de hambre
		this.procesarInanicion();
	}

	private void procesarInanicion() {
		if (this.hambre > 0.0) {
			return;
		}

		// Si el hambre es 0, evalúa el daño cada 3 segundos
		if (this.gtDanioInanicion.transcurrioMiliSegundos(INTERVALO_MS_INANICION)) {
			this.gtDanioInanicion.establecerReferenciaTiempoActual();

			final double danioInanicion = (Globales.dificultad != null) ? Globales.dificultad.getDanioInanicion() : 1.0;

			if ((danioInanicion > 0.0) && (Globales.JUGADOR != null) && !Globales.JUGADOR.isModoDios()) {
				Globales.JUGADOR.recibirDanioDirecto(danioInanicion);
			}
		}
	}

	// =========================================================================
	// === INGESTA DE ALIMENTOS Y LÍQUIDOS (CONSUMIBLES)
	// =========================================================================

	public void ingerir(final double aporteHambre, final double aporteSed) {
		this.hambre = Math.max(0.0, Math.min(MAX_VALOR, this.hambre + aporteHambre));
		this.sed = Math.max(0.0, Math.min(MAX_VALOR, this.sed + aporteSed));
	}

	public void reiniciar() {
		this.hambre = MAX_VALOR;
		this.sed = MAX_VALOR;
		this.gtDanioInanicion.establecerReferenciaTiempoActual();
	}

	// =========================================================================
	// === CONSULTAS DE ESTADO (ZERO-GC)
	// =========================================================================

	public double getHambre() {
		return this.hambre;
	}

	public double getSed() {
		return this.sed;
	}

	public int getHambrePorcentajeInt() {
		return (int) Math.round(this.hambre);
	}

	public int getSedPorcentajeInt() {
		return (int) Math.round(this.sed);
	}

	public boolean isDeshidratado() {
		return this.sed <= UMBRAL_CRITICO;
	}

	public boolean isInanicion() {
		return this.hambre <= UMBRAL_CRITICO;
	}

	public boolean isAlertaHambre() {
		return this.hambre <= UMBRAL_ALERTA_LEVE;
	}

	public boolean isAlertaSed() {
		return this.sed <= UMBRAL_ALERTA_LEVE;
	}

	public void setHambre(final double hambre) {
		this.hambre = Math.max(0.0, Math.min(MAX_VALOR, hambre));
	}

	public void setSed(final double sed) {
		this.sed = Math.max(0.0, Math.min(MAX_VALOR, sed));
	}

	public boolean isSimulacionHabilitada() {
		return this.simulacionHabilitada;
	}

	public void setSimulacionHabilitada(final boolean habilitada) {
		this.simulacionHabilitada = habilitada;
		if (!habilitada) {
			this.reiniciar();
		}
	}
}