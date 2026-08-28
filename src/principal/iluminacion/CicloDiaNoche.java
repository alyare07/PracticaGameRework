package principal.iluminacion;

import java.awt.Color;

/**
 * Gestor del ciclo solar de 24 horas, transiciones de color ambiental y fases
 * del día.
 * <p>
 * <b>Pilares de Rendimiento (Zero-GC):</b>
 * <ul>
 * <li><b>Dirty-Flag de Color:</b> No crea instancias de {@link Color} en el
 * bucle principal a menos que cambie un canal entero RGBA.</li>
 * <li><b>Caché de Reloj:</b> El texto {@code "HH:MM"} solo se reconstruye
 * cuando el minuto del juego avanza.</li>
 * <li><b>Enum Seguro:</b> {@link FaseDia} usa un arreglo pre-cacheado para
 * evitar el clonado de {@code values()}.</li>
 * </ul>
 * </p>
 * 
 * @version 5.0
 */
public class CicloDiaNoche {

	// =========================================================================
	// === 1. ENUM DE FASES HORARIAS DEL DÍA
	// =========================================================================

	/**
	 * Representa las fases y momentos del día en el ciclo solar de 24 horas.
	 * Encapsula la hora decimal de inicio y el nombre visible para la interfaz.
	 */
	public enum FaseDia {

		MEDIANOCHE(0.0, "Medianoche"), MADRUGADA(4.5, "Madrugada"), AMANECER(6.5, "Amanecer"), MANANA(8.5, "Mañana"),
		MEDIODIA(12.0, "Mediodía"), TARDE(15.0, "Tarde"), ATARDECER(17.5, "Atardecer"), ANOCHECER(19.5, "Anochecer"),
		NOCHE(21.5, "Noche");

		// Arreglo pre-cacheado para evitar asignaciones en bucles
		private static final FaseDia[] VALORES = FaseDia.values();

		private final double horaInicio;
		private final String nombre;

		FaseDia(final double horaInicio, final String nombre) {
			this.horaInicio = horaInicio;
			this.nombre = nombre;
		}

		/**
		 * Obtiene la fase del día correspondiente a una hora decimal continua (0.0 a
		 * 24.0).
		 *
		 * @param hora Hora actual in-game.
		 * @return Instancia de {@link FaseDia} activa ($O(1)$ Zero-GC).
		 */
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
	// === 2. PALETAS DE COLOR BASE
	// =========================================================================
	private static final Color NOCHE_ATMOSFERICA = new Color(8, 14, 32, 235);
	private static final Color NOCHE_BLACKOUT = new Color(0, 0, 0, 255);
	private static final Color MADRUGADA = new Color(35, 18, 55, 160);
	private static final Color AMANECER = new Color(255, 140, 40, 60);
	private static final Color PLENO_DIA = new Color(0, 0, 0, 0); // 100% Transparente
	private static final Color ATARDECER = new Color(245, 95, 20, 80);

	// =========================================================================
	// === 3. ESTADO Y RENDIMIENTO ZERO-GC
	// =========================================================================
	private double duracionDiaSegundos = 480.0; // 8 minutos por día completo in-game
	private double horaActual = FaseDia.MEDIODIA.getHoraInicio();
	private boolean tiempoPausado = false;
	private boolean modoOscuridadTotal = false;

	// Cache de color por Dirty-Flag (Zero-GC)
	private int lastR = -1;
	private int lastG = -1;
	private int lastB = -1;
	private int lastA = -1;
	private Color colorAmbienteActual = PLENO_DIA;

	// Cache de reloj formateado en String (Solo se recrea cuando el minuto cambia)
	private int lastHoraInt = -1;
	private int lastMinutoInt = -1;
	private String cachedHora24h = "12:00";

	// =========================================================================
	// === CICLO LÓGICO DE ACTUALIZACIÓN (60 APS)
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
	 * Interpola el color y la opacidad ambiental según las fases horarias.
	 */
	public void calcularColorAmbiente() {
		final double h = this.horaActual;
		final Color colorNoche = this.modoOscuridadTotal ? NOCHE_BLACKOUT : NOCHE_ATMOSFERICA;

		final double hMadrugada = FaseDia.MADRUGADA.getHoraInicio();
		final double hAmanecer = FaseDia.AMANECER.getHoraInicio();
		final double hManana = FaseDia.MANANA.getHoraInicio();
		final double hAtardecer = FaseDia.ATARDECER.getHoraInicio();
		final double hAnochecer = FaseDia.ANOCHECER.getHoraInicio();
		final double hNoche = FaseDia.NOCHE.getHoraInicio();

		if ((h >= hNoche) || (h < hMadrugada)) {
			// Noche Profunda
			this.aplicarColorDirty(colorNoche.getRed(), colorNoche.getGreen(), colorNoche.getBlue(),
					colorNoche.getAlpha());

		} else if ((h >= hMadrugada) && (h < hAmanecer)) {
			// Madrugada (Noche -> Púrpura)
			final double f = (h - hMadrugada) / (hAmanecer - hMadrugada);
			this.interpolar(colorNoche, MADRUGADA, f);

		} else if ((h >= hAmanecer) && (h < hManana)) {
			// Amanecer (Púrpura -> Dorado cálido)
			final double f = (h - hAmanecer) / (hManana - hAmanecer);
			this.interpolar(MADRUGADA, AMANECER, f);

		} else if ((h >= hManana) && (h < 9.5)) {
			// Mañana temprana (El tinte desaparece abriendo visión 100%)
			final double f = (h - hManana) / (9.5 - hManana);
			this.interpolar(AMANECER, PLENO_DIA, f);

		} else if ((h >= 9.5) && (h < hAtardecer)) {
			// Pleno Día (Visión despejada sin sombras)
			this.aplicarColorDirty(0, 0, 0, 0);

		} else if ((h >= hAtardecer) && (h < hAnochecer)) {
			// Atardecer cálido
			final double f = (h - hAtardecer) / (hAnochecer - hAtardecer);
			this.interpolar(PLENO_DIA, ATARDECER, f);

		} else {
			// Anochecer (El campo de visión se va cerrando hacia la noche)
			final double f = (h - hAnochecer) / (hNoche - hAnochecer);
			this.interpolar(ATARDECER, colorNoche, f);
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

	/*
	 * =========================================================================
	 * EXPLICACIÓN DIDÁCTICA: PATRÓN DIRTY-FLAG QUANTIZADO (ZERO-GC)
	 * ------------------------------------------------------------------------- En
	 * lugar de hacer 'return new Color(r, g, b, a)' 60 veces por segundo:
	 * Comparamos los 4 valores enteros (R, G, B, A). Si ninguno cambió respecto al
	 * fotograma anterior, NO HACEMOS NADA y reutilizamos la misma instancia en
	 * memoria.
	 *
	 * Esto reduce las asignaciones de memoria a prácticamente 0.
	 * =========================================================================
	 */
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
	// === MÉTODOS DE CONSULTA Y ESTADO (API PÚBLICA)
	// =========================================================================

	/**
	 * Retorna la fase del día actual como enum fuertemente tipado.
	 *
	 * @return Instancia de {@link FaseDia}.
	 */
	public FaseDia getFaseActual() {
		return FaseDia.obtenerPorHora(this.horaActual);
	}

	/**
	 * Retorna el nombre descriptivo de la fase del día actual para el HUD o
	 * diálogos.
	 *
	 * @return Nombre legible (ej: "Amanecer", "Mediodía", "Noche").
	 */
	public String getNombreMomentoDelDia() {
		return this.getFaseActual().getNombre();
	}

	/**
	 * Retorna la hora actual formateada como reloj digital 24h (ej:
	 * {@code "07:05"}, {@code "14:30"}).
	 *
	 * @return Cadena inmutable con formato {@code "HH:MM"}.
	 */
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

	// =========================================================================
	// === CONFIGURACIÓN Y ATAJOS DE HORA
	// =========================================================================

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