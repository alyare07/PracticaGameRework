package principal.iluminacion;

import java.awt.Color;

import org.json.simple.JSONObject;

/**
 * Gestor del ciclo solar de 24 horas, calendario canónico RPG (112 días / 16
 * semanas), fotoperiodo dinámico estacional y telemetría de HUD (Zero-GC /
 * O(1)).
 * 
 * @version 9.0 (Vanilla Java 8 - Canonical RPG Calendar & Dynamic Photoperiod)
 */
public class CicloDiaNoche {

	// =========================================================================
	// === 1. CONSTANTES DEL CALENDARIO CANÓNICO
	// =========================================================================

	public static final int DIAS_POR_SEMANA = 7;
	public static final int SEMANAS_POR_ESTACION = 4;
	public static final int DIAS_POR_ESTACION = 28; // 4 semanas exactas
	public static final int ESTACIONES_POR_ANIO = 4;
	public static final int DIAS_POR_ANIO = 112; // 16 semanas exactas
	public static final int SEMANAS_POR_ANIO = 16;

	private static final String[] NOMBRES_DIAS_CORTOS = { "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom" };
	private static final String[] NOMBRES_DIAS_LARGOS = { "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado",
			"Domingo" };

	// =========================================================================
	// === 2. FASES DEL DÍA (REFERENCIALES)
	// =========================================================================

	public enum FaseDia {
		MEDIANOCHE(0.0, "Medianoche"), MADRUGADA(4.5, "Madrugada"), AMANECER(6.5, "Amanecer"), MANANA(8.0, "Mañana"),
		MEDIODIA(12.0, "Mediodía"), TARDE(15.0, "Tarde"), ATARDECER(17.0, "Atardecer"), CREPUSCULO(19.0, "Crepúsculo"),
		ANOCHECER(20.5, "Anochecer"), NOCHE(21.5, "Noche");

		private static final FaseDia[] VALORES = FaseDia.values();
		private final double horaInicio;
		private final String nombre;

		FaseDia(final double horaInicio, final String nombre) {
			this.horaInicio = horaInicio;
			this.nombre = nombre;
		}

		public static FaseDia obtenerPorHora(final double hora) {
			for (int i = VALORES.length - 1; i >= 0; i--) {
				if (hora >= VALORES[i].horaInicio) {
					return VALORES[i];
				}
			}
			return MEDIANOCHE;
		}

		public double getHoraInicio() {
			return this.horaInicio;
		}

		public String getNombre() {
			return this.nombre;
		}
	}

	// =========================================================================
	// === 3. PALETA ESPECTRAL CONTINUA
	// =========================================================================

	private static final Color NOCHE_ATMOSFERICA = new Color(3, 6, 15, 248);
	private static final Color NOCHE_BLACKOUT = new Color(0, 0, 0, 255);
	private static final Color MADRUGADA = new Color(25, 12, 45, 185);
	private static final Color AMANECER = new Color(255, 140, 40, 60);
	private static final Color PLENO_DIA = new Color(0, 0, 0, 0);
	private static final Color ATARDECER = new Color(245, 95, 20, 80);
	private static final Color CREPUSCULO = new Color(35, 15, 55, 180);

	// =========================================================================
	// === 4. ESTADO DEL TIEMPO Y CALENDARIO
	// =========================================================================

	private double duracionDiaSegundos = 1800.0; // 30 min por defecto
	private double horaActual = FaseDia.MEDIODIA.getHoraInicio();

	/**
	 * Contador monótono creciente de días transcurridos in-game (1, 2, 3...).
	 * Garantiza que la persistencia de DeltasMundo no rompa la expiración de
	 * cofres.
	 */
	private int diaActual = 1;

	private double multiplicadorTiempo = 1.0;
	private boolean tiempoPausado = false;
	private boolean modoOscuridadTotal = false;

	// Umbrales Dinámicos de Luz por Fotoperiodo Estacional
	private double horaAmanecerActual = 6.375;
	private double horaAtardecerActual = 19.5;

	// Dirty-Flags de Color (Zero-GC)
	private int lastR = -1;
	private int lastG = -1;
	private int lastB = -1;
	private int lastA = -1;
	private Color colorAmbienteActual = PLENO_DIA;

	// Caché de texto formateado para HUD (Opción 3: Anual Continuo)
	private int lastHoraInt = -1;
	private int lastMinutoInt = -1;
	private String cachedHora24h = "12:00";

	private int lastDiaRecalculo = -1;
	private String cachedTextoLinea1HUD = "Año 1 · Sem 1 (Lun)";
	private String cachedTextoLinea2HUD = "12:00 · Primavera";

	// =========================================================================
	// === CICLO LÓGICO DE ACTUALIZACIÓN (60 APS)
	// =========================================================================

	public void actualizar(final double dt) {
		if (this.tiempoPausado || (this.multiplicadorTiempo <= 0.0)) {
			return;
		}

		final double horasPorSegundo = (24.0 / this.duracionDiaSegundos) * this.multiplicadorTiempo;
		this.horaActual += dt * horasPorSegundo;

		while (this.horaActual >= 24.0) {
			this.horaActual -= 24.0;
			this.diaActual++;
		}

		this.recalcularFotoperiodoYFechasSiCambioDia();
		this.calcularColorAmbiente();
	}

	/**
	 * Recalcula en O(1) los umbrales de luz solar y el texto de HUD solo al cruzar
	 * medianoche.
	 */
	private void recalcularFotoperiodoYFechasSiCambioDia() {
		if (this.diaActual == this.lastDiaRecalculo) {
			return;
		}
		this.lastDiaRecalculo = this.diaActual;

		final int diaAnio = (this.diaActual - 1) % DIAS_POR_ANIO;

		// Onda senoidal: 0 en equinoccios (Primavera/Otoño), +1 en Verano, -1 en
		// Invierno
		final double factorSolar = Math.sin(((diaAnio - 14.0) / DIAS_POR_ANIO) * (Math.PI * 2.0));

		// Verano: 05:00 | Invierno: 07:45 | Primavera/Otoño: 06:22
		this.horaAmanecerActual = 6.375 - (1.375 * factorSolar);

		// Verano: 21:30 | Invierno: 17:30 | Primavera/Otoño: 19:30
		this.horaAtardecerActual = 19.5 + (2.0 * factorSolar);

		// Regenerar Texto de Línea 1 (Año X · Sem Y (DíaSem))
		final int anio = this.getAnioActual();
		final int semanaAnio = this.getSemanaAnio();
		final String diaSemanaCorto = NOMBRES_DIAS_CORTOS[this.getIndiceDiaSemana()];

		this.cachedTextoLinea1HUD = "Año " + anio + " · Sem " + semanaAnio + " (" + diaSemanaCorto + ")";
		this.actualizarCacheLinea2();
	}

	/**
	 * Interpola de forma continua la atmósfera lumínica según el fotoperiodo de la
	 * estación.
	 */
	public void calcularColorAmbiente() {
		final double h = this.horaActual;
		final Color colorNoche = this.modoOscuridadTotal ? NOCHE_BLACKOUT : NOCHE_ATMOSFERICA;

		final double hAmanecer = this.horaAmanecerActual;
		final double hMadrugada = hAmanecer - 2.0;
		final double hPlenoDia = hAmanecer + 1.5;

		final double hAtardecer = this.horaAtardecerActual;
		final double hFinPlenoDia = hAtardecer - 1.5;
		final double hCrepusculo = hAtardecer + 1.5;
		final double hNoche = hAtardecer + 2.5;

		// 1. Noche cerrada profunda
		if ((h < hMadrugada) || (h >= hNoche)) {
			this.aplicarColorDirty(colorNoche.getRed(), colorNoche.getGreen(), colorNoche.getBlue(),
					colorNoche.getAlpha());

			// 2. Noche a Madrugada
		} else if (h < hAmanecer) {
			final double f = (h - hMadrugada) / (hAmanecer - hMadrugada);
			this.interpolar(colorNoche, MADRUGADA, f);

			// 3. Madrugada a Amanecer dorado
		} else if (h < (hAmanecer + 0.75)) {
			final double f = (h - hAmanecer) / 0.75;
			this.interpolar(MADRUGADA, AMANECER, f);

			// 4. Amanecer a Pleno Día (Luz diurna 0% alpha)
		} else if (h < hPlenoDia) {
			final double f = (h - (hAmanecer + 0.75)) / (hPlenoDia - (hAmanecer + 0.75));
			this.interpolar(AMANECER, PLENO_DIA, f);

			// 5. Pleno Día
		} else if (h < hFinPlenoDia) {
			this.aplicarColorDirty(0, 0, 0, 0);

			// 6. Pleno Día a Atardecer cálido
		} else if (h < hAtardecer) {
			final double f = (h - hFinPlenoDia) / (hAtardecer - hFinPlenoDia);
			this.interpolar(PLENO_DIA, ATARDECER, f);

			// 7. Atardecer a Crepúsculo violáceo
		} else if (h < hCrepusculo) {
			final double f = (h - hAtardecer) / (hCrepusculo - hAtardecer);
			this.interpolar(ATARDECER, CREPUSCULO, f);

			// 8. Crepúsculo a Noche
		} else {
			final double f = (h - hCrepusculo) / (hNoche - hCrepusculo);
			this.interpolar(CREPUSCULO, colorNoche, f);
		}
	}

	private void interpolar(final Color c1, final Color c2, final double factor) {
		final float f = (float) Math.max(0.0, Math.min(1.0, factor));
		final int r = (int) (c1.getRed() + ((c2.getRed() - c1.getRed()) * f));
		final int g = (int) (c1.getGreen() + ((c2.getGreen() - c1.getGreen()) * f));
		final int b = (int) (c1.getBlue() + ((c2.getBlue() - c1.getBlue()) * f));
		final int a = (int) (c1.getAlpha() + ((c2.getAlpha() - c1.getAlpha()) * f));

		this.aplicarColorDirty(r, g, b, a);
	}

	private void aplicarColorDirty(final int r, final int g, final int b, final int a) {
		if ((r != this.lastR) || (g != this.lastG) || (b != this.lastB) || (a != this.lastA)) {
			this.lastR = r;
			this.lastG = g;
			this.lastB = b;
			this.lastA = a;
			this.colorAmbienteActual = new Color(r, g, b, a);
		}
	}

	private void actualizarCacheLinea2() {
		this.cachedTextoLinea2HUD = this.getHoraFormato24h() + " · " + this.getEstacionActual().getNombre();
	}

	// =========================================================================
	// === MÉTODOS DEL CALENDARIO CANÓNICO (API ZERO-GC / O(1))
	// =========================================================================

	/**
	 * Contador total monótono de días (1, 2, 3...). Preserva contratos de
	 * persistencia.
	 */
	public int getDiaActual() {
		return this.diaActual;
	}

	public void setDiaActual(final int dia) {
		this.diaActual = Math.max(1, dia);
		this.recalcularFotoperiodoYFechasSiCambioDia();
	}

	public void avanzarDia() {
		this.diaActual++;
		this.recalcularFotoperiodoYFechasSiCambioDia();
	}

	/** Día dentro del año actual (1 a 112). */
	public int getDiaDelAnio() {
		return ((this.diaActual - 1) % DIAS_POR_ANIO) + 1;
	}

	/** Día dentro de la estación actual (1 a 28). */
	public int getDiaDeLaEstacion() {
		return ((this.diaActual - 1) % DIAS_POR_ESTACION) + 1;
	}

	/** Semana global del año (1 a 16). */
	public int getSemanaAnio() {
		return (((this.diaActual - 1) % DIAS_POR_ANIO) / DIAS_POR_SEMANA) + 1;
	}

	/** Semana dentro de la estación actual (1 a 4). */
	public int getSemanaDeLaEstacion() {
		return (((this.diaActual - 1) % DIAS_POR_ESTACION) / DIAS_POR_SEMANA) + 1;
	}

	/** Índice del día de la semana (0 = Lunes, 6 = Domingo). */
	public int getIndiceDiaSemana() {
		return (this.diaActual - 1) % DIAS_POR_SEMANA;
	}

	public String getNombreDiaSemanaLargo() {
		return NOMBRES_DIAS_LARGOS[this.getIndiceDiaSemana()];
	}

	public String getNombreDiaSemanaCorto() {
		return NOMBRES_DIAS_CORTOS[this.getIndiceDiaSemana()];
	}

	public Estacion getEstacionActual() {
		return Estacion.obtenerPorDiaAnio((this.diaActual - 1) % DIAS_POR_ANIO);
	}

	public int getAnioActual() {
		return 1 + ((this.diaActual - 1) / DIAS_POR_ANIO);
	}

	public String getTextoLinea1HUD() {
		return this.cachedTextoLinea1HUD;
	}

	public String getTextoLinea2HUD() {
		return this.cachedTextoLinea2HUD;
	}

	/**
	 * Retorna la hora de amanecer de este día (ej: 05.00 en verano, 07.75 en
	 * invierno).
	 */
	public double getHoraAmanecer() {
		return this.horaAmanecerActual;
	}

	/**
	 * Retorna la hora de atardecer de este día (ej: 21.50 en verano, 17.50 en
	 * invierno).
	 */
	public double getHoraAtardecer() {
		return this.horaAtardecerActual;
	}

	// =========================================================================
	// === CONTROL HORARIO Y FORMATO 24H (ZERO-GC)
	// =========================================================================

	public String getHoraFormato24h() {
		final int horas = (int) this.horaActual;
		final int minutos = (int) Math.round((this.horaActual - horas) * 60.0);
		final int hFinal = (minutos >= 60) ? (horas + 1) % 24 : horas;
		final int mFinal = (minutos >= 60) ? 0 : minutos;

		if ((hFinal != this.lastHoraInt) || (mFinal != this.lastMinutoInt)) {
			this.lastHoraInt = hFinal;
			this.lastMinutoInt = mFinal;
			this.cachedHora24h = (hFinal < 10 ? "0" + hFinal : String.valueOf(hFinal)) + ":"
					+ (mFinal < 10 ? "0" + mFinal : String.valueOf(mFinal));
			this.actualizarCacheLinea2();
		}
		return this.cachedHora24h;
	}

	public void setHora(final double hora) {
		this.horaActual = Math.max(0.0, Math.min(23.99, hora));
		this.calcularColorAmbiente();
	}

	public void setHora(final FaseDia fase) {
		if (fase != null) {
			this.setHora(fase.getHoraInicio());
		}
	}

	public void irAMedianoche() {
		this.setHora(FaseDia.MEDIANOCHE);
	}

	public void irAAmanecer() {
		this.setHora(this.horaAmanecerActual);
	}

	public void irAMediodia() {
		this.setHora(FaseDia.MEDIODIA);
	}

	public void irAAtardecer() {
		this.setHora(this.horaAtardecerActual);
	}

	public void irANoche() {
		this.setHora(this.horaAtardecerActual + 2.5);
	}

	public double getHoraActual() {
		return this.horaActual;
	}

	public Color getColorAmbienteActual() {
		return this.colorAmbienteActual;
	}

	public void acelerarTiempo(final double factor) {
		this.multiplicadorTiempo = Math.max(0.0, factor);
	}

	public void setMultiplicadorTiempo(final double factor) {
		this.multiplicadorTiempo = Math.max(0.0, factor);
	}

	public void restablecerVelocidadTiempo() {
		this.multiplicadorTiempo = 1.0;
	}

	public double getMultiplicadorTiempo() {
		return this.multiplicadorTiempo;
	}

	public void pausarTiempo() {
		this.tiempoPausado = true;
	}

	public void reanudarTiempo() {
		this.tiempoPausado = false;
	}

	public boolean isTiempoPausado() {
		return this.tiempoPausado;
	}

	public void setModoOscuridadTotal(final boolean blackout) {
		this.modoOscuridadTotal = blackout;
		this.calcularColorAmbiente();
	}

	public boolean isModoOscuridadTotal() {
		return this.modoOscuridadTotal;
	}

	public void setDuracionDiaMinutos(final double minutos) {
		this.duracionDiaSegundos = Math.max(10.0, minutos * 60.0);
	}

	public double getDuracionDiaSegundos() {
		return this.duracionDiaSegundos;
	}

	/** Método de retrocompatibilidad. */
	public String getTextoDia() {
		return this.cachedTextoLinea1HUD;
	}

	public FaseDia getFaseActual() {
		return FaseDia.obtenerPorHora(this.horaActual);
	}

	public String getNombreMomentoDelDia() {
		return this.getFaseActual().getNombre();
	}

	// =========================================================================
	// === PERSISTENCIA JSON (org.json.simple)
	// =========================================================================

	/**
	 * Exporta el estado temporal y calendario a un objeto JSON compatible con el
	 * Save Game.
	 * 
	 * @return JSONObject con el estado primario y metadatos legibles.
	 */
	@SuppressWarnings("unchecked")
	public JSONObject exportarJSON() {
		final JSONObject json = new JSONObject();

		// 1. Estado Escalar Primario (Datos deterministas indispensables)
		json.put("diaActual", Integer.valueOf(this.diaActual));
		json.put("horaActual", Double.valueOf(this.horaActual));
		json.put("duracionDiaSegundos", Double.valueOf(this.duracionDiaSegundos));
		json.put("multiplicadorTiempo", Double.valueOf(this.multiplicadorTiempo));
		json.put("tiempoPausado", Boolean.valueOf(this.tiempoPausado));

		// 2. Metadatos Informativos (Para inspección humana y depuración del archivo
		// Save)
		json.put("anio", Integer.valueOf(this.getAnioActual()));
		json.put("semanaAnio", Integer.valueOf(this.getSemanaAnio()));
		json.put("diaDelAnio", Integer.valueOf(this.getDiaDelAnio()));
		json.put("estacion", this.getEstacionActual().name());
		json.put("diaEstacion", Integer.valueOf(this.getDiaDeLaEstacion()));
		json.put("diaSemana", this.getNombreDiaSemanaLargo());
		json.put("hora24h", this.getHoraFormato24h());

		return json;
	}

	/**
	 * Restaura el tiempo y el calendario desde un JSON, reconstruyendo
	 * automáticamente fotoperiodo, colores de iluminación, estación y cadenas del
	 * HUD en O(1).
	 * 
	 * @param json Objeto JSON cargado del archivo de guardado.
	 */
	public void importarJSON(final JSONObject json) {
		if (json == null) {
			return;
		}

		if (json.get("diaActual") != null) {
			this.diaActual = Math.max(1, ((Number) json.get("diaActual")).intValue());
		}

		if (json.get("horaActual") != null) {
			this.horaActual = Math.max(0.0, Math.min(23.99, ((Number) json.get("horaActual")).doubleValue()));
		}

		if (json.get("duracionDiaSegundos") != null) {
			this.duracionDiaSegundos = Math.max(10.0, ((Number) json.get("duracionDiaSegundos")).doubleValue());
		}

		if (json.get("multiplicadorTiempo") != null) {
			this.multiplicadorTiempo = Math.max(0.0, ((Number) json.get("multiplicadorTiempo")).doubleValue());
		}

		if (json.get("tiempoPausado") != null) {
			this.tiempoPausado = Boolean.parseBoolean(json.get("tiempoPausado").toString());
		}

		// Invalidar Dirty-Flag para obligar al recálculo inmediato de todo el
		// calendario
		this.lastDiaRecalculo = -1;
		this.recalcularFotoperiodoYFechasSiCambioDia();
		this.calcularColorAmbiente();
	}
}