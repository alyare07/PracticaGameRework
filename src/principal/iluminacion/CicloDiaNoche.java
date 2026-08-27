package principal.iluminacion;

import java.awt.Color;

/**
 * Gestor del ciclo solar de 24 horas con soporte para presets estáticos de
 * hora, formato de reloj 24h y control de iluminación ambiental.
 * <p>
 * <b>Uso Sencillo desde el Exterior:</b><br>
 * 
 * <pre>
 * Globales.GESTOR_LUZ.getCiclo().setHora(CicloDiaNoche.HORA_MEDIODIA);
 * Globales.GESTOR_LUZ.getCiclo().setHora(CicloDiaNoche.HORA_NOCHE);
 * String reloj = Globales.GESTOR_LUZ.getCiclo().getHoraFormato24h(); // "18:30"
 * </pre>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 3.5
 */
public class CicloDiaNoche {

	// =========================================================================
	// === 1. PRESETS DE HORAS DEL DÍA (CONSTANTES ESTÁTICAS PÚBLICAS)
	// =========================================================================

	/** 00:00 - Medianoche (Noche profunda / Oscuridad máxima). */
	public static final double HORA_MEDIANOCHE = 0.0;

	/** 04:30 - Madrugada (La noche empieza a tornarse púrpura). */
	public static final double HORA_MADRUGADA = 4.5;

	/**
	 * 06:30 - Amanecer (Aparece el tinte dorado y la visión comienza a abrirse).
	 */
	public static final double HORA_AMANECER = 6.5;

	/** 08:30 - Mañana (Visión completamente abierta al 100%). */
	public static final double HORA_MANANA = 8.5;

	/** 12:00 - Mediodía (Sol radiante, 0% sombras ambientales). */
	public static final double HORA_MEDIODIA = 12.0;

	/** 15:00 - Tarde (Plena luz solar). */
	public static final double HORA_TARDE = 15.0;

	/** 17:30 - Atardecer (Comienza el tinte cálido dorado y rojizo). */
	public static final double HORA_ATARDECER = 17.5;

	/**
	 * 19:30 - Anochecer / Crepúsculo (El campo visual se va cerrando hacia la
	 * noche).
	 */
	public static final double HORA_ANOCHECER = 19.5;

	/**
	 * 21:30 - Noche Cerrada (La oscuridad cubre el mapa y la linterna es
	 * necesaria).
	 */
	public static final double HORA_NOCHE = 21.5;

	// =========================================================================
	// === 2. PALETAS DE COLOR AMBIENTAL
	// =========================================================================

	/**
	 * Noche estándar atmosférica (Azul medianoche profundo con 90% de opacidad).
	 */
	private static final Color NOCHE_ATMOSFERICA = new Color(8, 14, 32, 230);

	/**
	 * Noche Total / Blackout (100% Negro absoluto, 0 visibilidad fuera de la luz).
	 */
	private static final Color NOCHE_BLACKOUT = new Color(0, 0, 0, 255);

	private static final Color MADRUGADA = new Color(35, 18, 55, 150);
	private static final Color AMANECER = new Color(255, 150, 40, 50);
	private static final Color PLENO_DIA = new Color(0, 0, 0, 0); // 100% transparente
	private static final Color ATARDECER = new Color(245, 95, 20, 70);

	// =========================================================================
	// === 3. ESTADO Y CONFIGURACIÓN DEL TIEMPO
	// =========================================================================

	/**
	 * Duración total de un día completo in-game (24h) en segundos reales (ej: 480s
	 * = 8 minutos).
	 */
	private double duracionDiaSegundos = 480.0;

	/** Hora actual in-game en formato decimal continuo (0.0 a 24.0). */
	private double horaActual = HORA_MEDIODIA;

	private boolean tiempoPausado = false;
	private boolean modoOscuridadTotal = false;

	/** Color ambiental interpolado activo en el fotograma actual. */
	private Color colorAmbienteActual = PLENO_DIA;

	// =========================================================================
	// === CICLO LÓGICO (60 APS)
	// =========================================================================

	/**
	 * Avanza el reloj solar en función del delta de tiempo del Game Loop.
	 *
	 * @param dt Delta de tiempo en segundos (1.0 / 60.0).
	 */
	public void actualizar(final double dt) {
		if (this.tiempoPausado) {
			return;
		}

		final double horasPorSegundo = 24.0 / this.duracionDiaSegundos;
		this.horaActual += dt * horasPorSegundo;

		if (this.horaActual >= 24.0) {
			this.horaActual -= 24.0;
		}

		this.calcularColorAmbiente();
	}

	/**
	 * Interpola el color y la opacidad ambiental según las constantes horarias del
	 * día.
	 */
	public void calcularColorAmbiente() {
		final double h = this.horaActual;
		final Color colorNoche = this.modoOscuridadTotal ? NOCHE_BLACKOUT : NOCHE_ATMOSFERICA;

		if ((h >= HORA_NOCHE) || (h < HORA_MADRUGADA)) {
			// 21:30 a 04:30 -> Noche Profunda
			this.colorAmbienteActual = colorNoche;

		} else if ((h >= HORA_MADRUGADA) && (h < HORA_AMANECER)) {
			// 04:30 a 06:30 -> Madrugada (Noche -> Púrpura)
			final double f = (h - HORA_MADRUGADA) / (HORA_AMANECER - HORA_MADRUGADA);
			this.colorAmbienteActual = this.interpolar(colorNoche, MADRUGADA, f);

		} else if ((h >= HORA_AMANECER) && (h < HORA_MANANA)) {
			// 06:30 a 08:30 -> Amanecer (Púrpura -> Dorado cálido)
			final double f = (h - HORA_AMANECER) / (HORA_MANANA - HORA_AMANECER);
			this.colorAmbienteActual = this.interpolar(MADRUGADA, AMANECER, f);

		} else if ((h >= HORA_MANANA) && (h < 9.5)) {
			// 08:30 a 09:30 -> Mañana temprana (El tinte desaparece abriendo visión 100%)
			final double f = (h - HORA_MANANA) / 1.0;
			this.colorAmbienteActual = this.interpolar(AMANECER, PLENO_DIA, f);

		} else if ((h >= 9.5) && (h < HORA_ATARDECER)) {
			// 09:30 a 17:30 -> Pleno Día (Visión despejada sin sombras)
			this.colorAmbienteActual = PLENO_DIA;

		} else if ((h >= HORA_ATARDECER) && (h < HORA_ANOCHECER)) {
			// 17:30 a 19:30 -> Atardecer cálido
			final double f = (h - HORA_ATARDECER) / (HORA_ANOCHECER - HORA_ATARDECER);
			this.colorAmbienteActual = this.interpolar(PLENO_DIA, ATARDECER, f);

		} else {
			// 19:30 a 21:30 -> Anochecer (La visión se va cerrando hacia la noche)
			final double f = (h - HORA_ANOCHECER) / (HORA_NOCHE - HORA_ANOCHECER);
			this.colorAmbienteActual = this.interpolar(ATARDECER, colorNoche, f);
		}
	}

	private Color interpolar(final Color c1, final Color c2, final double factor) {
		final float f = (float) Math.max(0.0, Math.min(1.0, factor));
		final int r = (int) (c1.getRed() + ((c2.getRed() - c1.getRed()) * f));
		final int g = (int) (c1.getGreen() + ((c2.getGreen() - c1.getGreen()) * f));
		final int b = (int) (c1.getBlue() + ((c2.getBlue() - c1.getBlue()) * f));
		final int a = (int) (c1.getAlpha() + ((c2.getAlpha() - c1.getAlpha()) * f));

		return new Color(r, g, b, a);
	}

	// =========================================================================
	// === FORMATEO Y CONSULTAS DE HORA (API PÚBLICA)
	// =========================================================================

	/*
	 * =========================================================================
	 * EXPLICACIÓN DIDÁCTICA: CONVERSIÓN DE HORA DECIMAL A FORMATO 24H (HH:MM)
	 * ------------------------------------------------------------------------- Si
	 * la hora actual es 14.75: 1. Las horas son la parte entera: (int) 14.75 = 14.
	 * 2. La fracción (0.75) se multiplica por 60 minutos: 0.75 * 60 = 45 minutos.
	 * 3. Se formatea con ceros a la izquierda para devolver exactamente "14:45".
	 * =========================================================================
	 */
	/**
	 * Retorna la hora actual del juego formateada como un reloj digital de 24 horas
	 * (ej: {@code "07:05"}, {@code "14:30"}).
	 *
	 * @return Cadena con formato {@code "HH:MM"}.
	 */
	public String getHoraFormato24h() {
		final int horas = (int) this.horaActual;
		final int minutos = (int) Math.round((this.horaActual - horas) * 60.0);

		// Ajuste por si el redondeo llega a 60 minutos
		final int hFinal = (minutos >= 60) ? (horas + 1) % 24 : horas;
		final int mFinal = (minutos >= 60) ? 0 : minutos;

		final String strH = (hFinal < 10) ? ("0" + hFinal) : String.valueOf(hFinal);
		final String strM = (mFinal < 10) ? ("0" + mFinal) : String.valueOf(mFinal);

		return strH + ":" + strM;
	}

	/**
	 * Retorna el nombre descriptivo de la fase del día actual. Ideal para
	 * interfaces, mensajes de descanso o diálogo de NPCs.
	 *
	 * @return Nombre de la fase (ej: "Amanecer", "Mediodía", "Atardecer", "Noche
	 *         Cerrada").
	 */
	public String getNombreFaseActual() {
		final double h = this.horaActual;

		if ((h >= HORA_NOCHE) || (h < HORA_MADRUGADA)) {
			return "Noche Cerrada";
		}
		if ((h >= HORA_MADRUGADA) && (h < HORA_AMANECER)) {
			return "Madrugada";
		}
		if ((h >= HORA_AMANECER) && (h < HORA_MANANA)) {
			return "Amanecer";
		}
		if ((h >= HORA_MANANA) && (h < HORA_MEDIODIA)) {
			return "Mañana";
		}
		if ((h >= HORA_MEDIODIA) && (h < HORA_TARDE)) {
			return "Mediodía";
		}
		if ((h >= HORA_TARDE) && (h < HORA_ATARDECER)) {
			return "Tarde";
		}
		if ((h >= HORA_ATARDECER) && (h < HORA_ANOCHECER)) {
			return "Atardecer";
		}
		return "Anochecer";
	}

	// =========================================================================
	// === ATAJOS DIRECTOS DE HORA
	// =========================================================================

	public void irAMediodia() {
		this.setHora(HORA_MEDIODIA);
	}

	public void irANoche() {
		this.setHora(HORA_NOCHE);
	}

	public void irAAmanecer() {
		this.setHora(HORA_AMANECER);
	}

	public void irAAtardecer() {
		this.setHora(HORA_ATARDECER);
	}

	public void irAMedianoche() {
		this.setHora(HORA_MEDIANOCHE);
	}

	// =========================================================================
	// === MÉTODOS DE CONFIGURACIÓN Y CONTROL
	// =========================================================================

	public void pausarTiempo() {
		this.tiempoPausado = true;
	}

	public void reanudarTiempo() {
		this.tiempoPausado = false;
	}

	public void conmutarPausaTiempo(final boolean pausar) {
		this.tiempoPausado = pausar;
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

	public void setHora(final double hora) {
		this.horaActual = Math.max(0.0, Math.min(23.99, hora));
		this.calcularColorAmbiente();
	}

	public double getHoraActual() {
		return this.horaActual;
	}

	public Color getColorAmbienteActual() {
		return this.colorAmbienteActual;
	}

	public void setDuracionDiaMinutos(final double minutos) {
		this.duracionDiaSegundos = Math.max(10.0, minutos * 60.0);
	}

	public double getDuracionDiaSegundos() {
		return this.duracionDiaSegundos;
	}
}