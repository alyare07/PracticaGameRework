package principal.iluminacion;

import java.awt.Color;

/**
 * Gestor del ciclo solar de 24 horas, calendario de días y control de velocidad
 * temporal (Zero-GC / O(1)).
 * 
 * @version 8.0
 */
public class CicloDiaNoche {

	// =========================================================================
	// === 1. FASES DEL DÍA
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
	// === 2. PALETA ESPECTRAL CONTINUA (ALTO CONTRASTE)
	// =========================================================================

	private static final Color NOCHE_ATMOSFERICA = new Color(3, 6, 15, 248);
	private static final Color NOCHE_BLACKOUT = new Color(0, 0, 0, 255);
	private static final Color MADRUGADA = new Color(25, 12, 45, 185);
	private static final Color AMANECER = new Color(255, 140, 40, 60);
	private static final Color PLENO_DIA = new Color(0, 0, 0, 0);
	private static final Color ATARDECER = new Color(245, 95, 20, 80);
	private static final Color CREPUSCULO = new Color(35, 15, 55, 180);

	// =========================================================================
	// === 3. ESTADO DEL TIEMPO Y CALENDARIO
	// =========================================================================

	/** Duración de un día completo in-game a velocidad 1x (por defecto 8 min). */
	private double duracionDiaSegundos = 480.0;

	/** Hora decimal actual (0.00 a 23.99). */
	private double horaActual = FaseDia.MEDIODIA.getHoraInicio();

	/** Contador de días transcurridos in-game (inicia en Día 1). */
	private int diaActual = 1;

	/** Multiplicador escalar de velocidad (1.0 = normal, 5.0 = 5x rápido). */
	private double multiplicadorTiempo = 1.0;

	private boolean tiempoPausado = false;
	private boolean modoOscuridadTotal = false;

	// Dirty-Flags de Color (Zero-GC)
	private int lastR = -1;
	private int lastG = -1;
	private int lastB = -1;
	private int lastA = -1;
	private Color colorAmbienteActual = PLENO_DIA;

	// Caché de texto formateado (Zero-GC)
	private int lastHoraInt = -1;
	private int lastMinutoInt = -1;
	private String cachedHora24h = "12:00";

	private int lastDiaInt = -1;
	private String cachedTextoDia = "Día 1";

	// =========================================================================
	// === CICLO LÓGICO DE ACTUALIZACIÓN (60 APS)
	// =========================================================================

	/**
	 * Avanza el reloj y el calendario en función del delta time y el multiplicador
	 * de velocidad activo.
	 *
	 * @param dt Delta de tiempo en segundos (1/60 s).
	 */
	public void actualizar(final double dt) {
		if (this.tiempoPausado || (this.multiplicadorTiempo <= 0.0)) {
			return;
		}

		final double horasPorSegundo = (24.0 / this.duracionDiaSegundos) * this.multiplicadorTiempo;
		this.horaActual += dt * horasPorSegundo;

		// Paso a un nuevo día al cruzar la medianoche (24:00 -> 00:00)
		while (this.horaActual >= 24.0) {
			this.horaActual -= 24.0;
			this.diaActual++;
		}

		this.calcularColorAmbiente();
	}

	/**
	 * Interpola de forma continua la atmósfera lumínica según la hora del día.
	 */
	public void calcularColorAmbiente() {
		final double h = this.horaActual;
		final Color colorNoche = this.modoOscuridadTotal ? NOCHE_BLACKOUT : NOCHE_ATMOSFERICA;

		if ((h >= 21.5) || (h < 4.5)) {
			this.aplicarColorDirty(colorNoche.getRed(), colorNoche.getGreen(), colorNoche.getBlue(),
					colorNoche.getAlpha());

		} else if ((h >= 4.5) && (h < 6.5)) {
			final double f = (h - 4.5) / (6.5 - 4.5);
			this.interpolar(colorNoche, MADRUGADA, f);

		} else if ((h >= 6.5) && (h < 8.0)) {
			final double f = (h - 6.5) / (8.0 - 6.5);
			this.interpolar(MADRUGADA, AMANECER, f);

		} else if ((h >= 8.0) && (h < 9.0)) {
			final double f = (h - 8.0) / (9.0 - 8.0);
			this.interpolar(AMANECER, PLENO_DIA, f);

		} else if ((h >= 9.0) && (h < 17.0)) {
			this.aplicarColorDirty(0, 0, 0, 0);

		} else if ((h >= 17.0) && (h < 19.0)) {
			final double f = (h - 17.0) / (19.0 - 17.0);
			this.interpolar(PLENO_DIA, ATARDECER, f);

		} else if ((h >= 19.0) && (h < 20.5)) {
			final double f = (h - 19.0) / (20.5 - 19.0);
			this.interpolar(ATARDECER, CREPUSCULO, f);

		} else {
			final double f = (h - 20.5) / (21.5 - 20.5);
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

	// =========================================================================
	// === MÉTODOS DE DÍAS Y CALENDARIO (API PÚBLICA)
	// =========================================================================

	/**
	 * Retorna el número de día actual in-game (1, 2, 3...).
	 */
	public int getDiaActual() {
		return this.diaActual;
	}

	/**
	 * Establece manualmente el día actual del calendario.
	 *
	 * @param dia Número de día deseado (mínimo 1).
	 */
	public void setDiaActual(final int dia) {
		this.diaActual = Math.max(1, dia);
	}

	/**
	 * Avanza el calendario en 1 día.
	 */
	public void avanzarDia() {
		this.diaActual++;
	}

	/**
	 * Retorna una representación en texto del día formateada sin crear basura en el
	 * Heap (ej: {@code "Día 1"}, {@code "Día 14"}).
	 */
	public String getTextoDia() {
		if (this.diaActual != this.lastDiaInt) {
			this.lastDiaInt = this.diaActual;
			this.cachedTextoDia = "Día " + this.diaActual;
		}
		return this.cachedTextoDia;
	}

	// =========================================================================
	// === CONTROL DE VELOCIDAD DEL TIEMPO (TIME WARP)
	// =========================================================================

	/**
	 * Acelera o ralentiza el transcurso del tiempo por un factor multiplicador.
	 *
	 * @param factor Escala de velocidad (ej: 2.0 = 2x rápido, 10.0 = 10x rápido).
	 */
	public void acelerarTiempo(final double factor) {
		this.multiplicadorTiempo = Math.max(0.0, factor);
	}

	/**
	 * Establece directamente el multiplicador de velocidad del tiempo.
	 */
	public void setMultiplicadorTiempo(final double factor) {
		this.multiplicadorTiempo = Math.max(0.0, factor);
	}

	/**
	 * Restablece la velocidad del tiempo a la progresión normal (1.0x).
	 */
	public void restablecerVelocidadTiempo() {
		this.multiplicadorTiempo = 1.0;
	}

	public double getMultiplicadorTiempo() {
		return this.multiplicadorTiempo;
	}

	// =========================================================================
	// === CONSULTAS DE HORA Y FORMATO 24H (ZERO-GC)
	// =========================================================================

	public FaseDia getFaseActual() {
		return FaseDia.obtenerPorHora(this.horaActual);
	}

	public String getNombreMomentoDelDia() {
		return this.getFaseActual().getNombre();
	}

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
		this.setHora(FaseDia.AMANECER);
	}

	public void irAMediodia() {
		this.setHora(FaseDia.MEDIODIA);
	}

	public void irAAtardecer() {
		this.setHora(FaseDia.ATARDECER);
	}

	public void irANoche() {
		this.setHora(FaseDia.NOCHE);
	}

	public double getHoraActual() {
		return this.horaActual;
	}

	public Color getColorAmbienteActual() {
		return this.colorAmbienteActual;
	}

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

	public void setDuracionDiaMinutos(final double minutos) {
		this.duracionDiaSegundos = Math.max(10.0, minutos * 60.0);
	}

	public double getDuracionDiaSegundos() {
		return this.duracionDiaSegundos;
	}
}