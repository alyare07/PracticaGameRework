package principal.astronomia;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

import org.json.simple.JSONObject;

import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

/**
 * Gestor soberano del Dominio Astronómico: tiempo solar de 24h, calendario
 * canónico (112 días / 16 semanas), fotoperiodo dinámico estacional, fases
 * lunares, eventos cósmicos y renderizado de la bóveda celeste en VRAM
 * (Zero-GC).
 * 
 * @version 1.0 (Vanilla Java 8 - Sovereign Celestial Architecture)
 */
public class GestorAstronomico {

	// =========================================================================
	// 1. CONSTANTES DEL CALENDARIO CANÓNICO
	// =========================================================================
	public static final int DIAS_POR_SEMANA = 7;
	public static final int SEMANAS_POR_ESTACION = 4;
	public static final int DIAS_POR_ESTACION = 28;
	public static final int ESTACIONES_POR_ANIO = 4;
	public static final int DIAS_POR_ANIO = 112;
	public static final int SEMANAS_POR_ANIO = 16;

	private static final String[] NOMBRES_DIAS_CORTOS = { "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom" };
	private static final String[] NOMBRES_DIAS_LARGOS = { "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado",
			"Domingo" };

	// =========================================================================
	// 2. FASES DEL DÍA
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
	// 3. PALETAS ESPECTRALES BASE (CONSTANTES ZERO-GC)
	// =========================================================================
	private static final Color NOCHE_ATMOSFERICA = new Color(3, 6, 15, 248);
	private static final Color NOCHE_BLACKOUT = new Color(0, 0, 0, 255);
	private static final Color NOCHE_LUNA_ROJA = new Color(145, 15, 20, 185);
	private static final Color NOCHE_CONJUNCION = new Color(55, 20, 85, 215);

	private static final Color MADRUGADA = new Color(25, 12, 45, 185);
	private static final Color AMANECER = new Color(255, 140, 40, 60);
	private static final Color PLENO_DIA = new Color(0, 0, 0, 0);
	private static final Color ATARDECER = new Color(245, 95, 20, 80);
	private static final Color CREPUSCULO = new Color(35, 15, 55, 180);
	private static final Color NOCHE_AURORA = new Color(10, 35, 32, 155); // Turquesa ártico luminoso

	// =========================================================================
	// CONSTANTES ESTÁTICAS DE LA CONJUNCIÓN ASTRAL (ZERO-GC)
	// =========================================================================
	private static final int[] ASTRO_OFF_X = { -35, 0, 42 };
	private static final int[] ASTRO_OFF_Y = { -15, 0, 18 };

	private static final Color[] COLORES_ASTROS = { new Color(130, 220, 255, 240), // Astro azul celeste
			new Color(245, 180, 255, 240), // Astro violeta místico
			new Color(255, 225, 140, 240) // Astro dorado
	};

	private static final Color[] COLORES_HALOS_ASTROS = { new Color(130, 220, 255, 45), new Color(245, 180, 255, 45),
			new Color(255, 225, 140, 45) };

	// =========================================================================
	// 4. CACHÉ DIRECTA DE COLORES ZERO-GC (1024 SLOTS)
	// =========================================================================
	private static final int CACHE_SIZE = 1024;
	private static final int CACHE_MASK = CACHE_SIZE - 1;
	private final int[] cacheKeys = new int[CACHE_SIZE];
	private final Color[] cacheColors = new Color[CACHE_SIZE];

	// =========================================================================
	// 5. ESTADO CELESTE Y TEMPORAL
	// =========================================================================
	private double duracionDiaSegundos = 1800.0; // 30 min reales por defecto
	private double horaActual = FaseDia.MEDIODIA.getHoraInicio();
	private int diaActual = 1;
	private double multiplicadorTiempo = 1.0;
	private boolean tiempoPausado = false;
	private boolean modoOscuridadTotal = false;

	private double horaAmanecerActual = 6.375;
	private double horaAtardecerActual = 19.5;

	private FenomenoAstronomico fenomenoActivo = FenomenoAstronomico.NORMAL;
	private double duracionFenomenoRestante = 0.0;

	// Componentes primitivos de la luz ambiente actual
	private int luzR = 0;
	private int luzG = 0;
	private int luzB = 0;
	private int luzA = 0;
	private Color colorAmbienteActual = PLENO_DIA;

	// Caché de texto para HUD
	private int lastHoraInt = -1;
	private int lastMinutoInt = -1;
	private String cachedHora24h = "12:00";
	private int lastDiaRecalculo = -1;
	private String cachedTextoLinea1HUD = "Año 1 · Sem 1 (Lun)";
	private String cachedTextoLinea2HUD = "12:00 · Primavera";

	// =========================================================================
	// 6. RECURSOS VRAM DE LA BÓVEDA CELESTE (AURORA Y METEOROS)
	// =========================================================================
	private static final int MAX_ESTRELLAS_FUGAZ = 6;
	private static final int ANCHO_AURORA_HD = 640;
	private static final int ALTO_AURORA_HD = 360;

	private static final Color COLOR_ESTRELLA_TRAIL = new Color(255, 255, 220, 240);
	private static final AlphaComposite COMPOSITE_OPACO = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);

	private static final AlphaComposite[] COMPOSITES_OPACIDAD = new AlphaComposite[101];
	static {
		for (int i = 0; i <= 100; i++) {
			COMPOSITES_OPACIDAD[i] = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, i / 100.0f);
		}
	}

	private final BufferedImage texturaAurora;
	private double faseOndaAurora = 0.0;

	private final double[] estrellaX = new double[MAX_ESTRELLAS_FUGAZ];
	private final double[] estrellaY = new double[MAX_ESTRELLAS_FUGAZ];
	private final double[] estrellaVelX = new double[MAX_ESTRELLAS_FUGAZ];
	private final double[] estrellaVelY = new double[MAX_ESTRELLAS_FUGAZ];
	private final double[] estrellaVida = new double[MAX_ESTRELLAS_FUGAZ];
	private final double[] estrellaLongitud = new double[MAX_ESTRELLAS_FUGAZ];
	private final boolean[] estrellaActiva = new boolean[MAX_ESTRELLAS_FUGAZ];
	private double temporizadorSpawnEstrella = 0.0;
	// Contadores de racha (Pity System) y disparadores
	private int diasDesdeUltimaLunaRoja = 0;
	private int diasDesdeUltimoEclipse = 0;
	private boolean evaluadaLunaRojaHoy = false;
	private boolean evaluadoEclipseHoy = false;

	public GestorAstronomico() {
		this.texturaAurora = this.hornearTexturaAurora();
		for (int i = 0; i < MAX_ESTRELLAS_FUGAZ; i++) {
			this.estrellaActiva[i] = false;
		}
		this.recalcularFotoperiodoYFechasSiCambioDia();
		this.calcularColorAmbiente();
	}

	private BufferedImage hornearTexturaAurora() {
		final int w = ANCHO_AURORA_HD;
		final int h = ALTO_AURORA_HD;
		final BufferedImage img = Globales.FUNCIONES.TEXTURAS_TOOLS.crearImagenVRAM(w, h, Transparency.TRANSLUCENT);
		final Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		for (int y = 0; y < h; y++) {
			// Envolvente vertical (curva de campana suave)
			final double sinY = Math.sin((y / (double) h) * Math.PI);
			final double factorY = sinY * sinY;

			for (int x = 0; x < w; x++) {
				// Envolvente horizontal: los bordes X=0 y X=640 se desvanecen suavemente a 0%
				final double sinX = Math.sin((x / (double) w) * Math.PI);
				final double factorX = Math.sin(sinX * (Math.PI / 2.0));

				final double alphaNorm = Math.max(0.0, Math.min(1.0, factorY * factorX));
				final int a = (int) (alphaNorm * 180.0);

				// Gradiente cromático: esmeralda ártico en la base, cian en el medio y violeta
				// arriba
				final double ratioY = y / (double) h;
				final int r = (int) (30 + (110 * (1.0 - ratioY)));
				final int gr = (int) ((230 * ratioY) + (80 * (1.0 - ratioY)));
				final int b = (int) (160 + (95 * (1.0 - ratioY)));

				final int rgba = (a << 24) | (r << 16) | (gr << 8) | b;
				img.setRGB(x, y, rgba);
			}
		}

		g.dispose();
		return img;
	}

	// =========================================================================
	// 7. BUCLE DE ACTUALIZACIÓN DETERMINISTA (60 APS)
	// =========================================================================
	public void actualizar(final double dt) {
		if (!this.tiempoPausado && (this.multiplicadorTiempo > 0.0)) {
			final double horasPorSegundo = (24.0 / this.duracionDiaSegundos) * this.multiplicadorTiempo;
			this.horaActual += dt * horasPorSegundo;

			// Evaluación de ventanas celestes
			if ((this.horaActual >= 11.3) && (this.horaActual <= 11.7)) {
				this.evaluarDisparadorEclipse();
			} else if ((this.horaActual >= 20.8) && (this.horaActual <= 21.3)) {
				this.evaluarDisparadorLunaRoja();
			}

			while (this.horaActual >= 24.0) {
				this.horaActual -= 24.0;
				this.diaActual++;
				this.evaluarDisparadoresMedianoche();
			}

			this.recalcularFotoperiodoYFechasSiCambioDia();
			this.calcularColorAmbiente();
		}

		// Actualización de fenómenos cósmicos
		if (this.duracionFenomenoRestante > 0.0) {
			this.duracionFenomenoRestante -= dt;
			if (this.duracionFenomenoRestante <= 0.0) {
				this.fenomenoActivo = FenomenoAstronomico.NORMAL;
			}
		}

		if (this.fenomenoActivo == FenomenoAstronomico.AURORA_BOREAL) {
			this.faseOndaAurora += dt * 0.8;
		}

		this.actualizarEstrellasFugaces(dt);
	}

	public double getHorasTotalesJuego() {
		return ((this.diaActual - 1) * 24.0) + this.horaActual;
	}

	private void evaluarDisparadoresMedianoche() {
		this.diasDesdeUltimaLunaRoja++;
		this.diasDesdeUltimoEclipse++;
		this.evaluadaLunaRojaHoy = false;
		this.evaluadoEclipseHoy = false;
	}

	/**
	 * Evalúa a la entrada de la noche (21:00) si se desata una Luna Roja. Requiere
	 * LUNA_LLENA y aplica sistema de garantía (Pity a 56 días o Día 84 canónico).
	 */
	private void evaluarDisparadorLunaRoja() {
		if (this.evaluadaLunaRojaHoy) {
			return;
		}
		this.evaluadaLunaRojaHoy = true;

		final FaseLunar fase = this.getFaseLunarActual();
		if (fase != FaseLunar.LUNA_LLENA) {
			return;
		}

		final boolean esNocheDeBrujas = (this.diaActual % DIAS_POR_ANIO) == 84; // Fin de Otoño
		final boolean pityAlcanzado = this.diasDesdeUltimaLunaRoja >= 56; // 2 estaciones sin evento
		final boolean azarFavorable = Math.random() < 0.25;

		if (esNocheDeBrujas || pityAlcanzado || azarFavorable) {
			// Dura toda la noche hasta el alba (~7.5 horas in-game)
			final double duracionSegs = (7.5 / 24.0) * this.duracionDiaSegundos;
			this.activarFenomeno(FenomenoAstronomico.LUNA_ROJA, duracionSegs);
			this.diasDesdeUltimaLunaRoja = 0;
		}
	}

	/**
	 * Evalúa al acercarse el mediodía (11:30) si se desata un Eclipse Solar.
	 * Requiere LUNA_NUEVA y aplica sistema de garantía (Pity a 112 días o Día 42
	 * canónico).
	 */
	private void evaluarDisparadorEclipse() {
		if (this.evaluadoEclipseHoy) {
			return;
		}
		this.evaluadoEclipseHoy = true;

		final FaseLunar fase = this.getFaseLunarActual();
		if (fase != FaseLunar.LUNA_NUEVA) {
			return;
		}

		final boolean esSolsticioVerano = (this.diaActual % DIAS_POR_ANIO) == 42; // Apogeo solar
		final boolean pityAlcanzado = this.diasDesdeUltimoEclipse >= 112; // 1 año sin eclipse
		final boolean azarFavorable = Math.random() < 0.10;

		if (esSolsticioVerano || pityAlcanzado || azarFavorable) {
			// Dura 2 horas solares (11:30 a 13:30)
			final double duracionSegs = (2.0 / 24.0) * this.duracionDiaSegundos;
			this.activarFenomeno(FenomenoAstronomico.ECLIPSE_SOLAR, duracionSegs);
			this.diasDesdeUltimoEclipse = 0;
		}
	}

	private void recalcularFotoperiodoYFechasSiCambioDia() {
		if (this.diaActual == this.lastDiaRecalculo) {
			return;
		}
		this.lastDiaRecalculo = this.diaActual;

		final int diaAnio = (this.diaActual - 1) % DIAS_POR_ANIO;
		final double factorSolar = Math.sin(((diaAnio - 14.0) / DIAS_POR_ANIO) * (Math.PI * 2.0));

		this.horaAmanecerActual = 6.375 - (1.375 * factorSolar);
		this.horaAtardecerActual = 19.5 + (2.0 * factorSolar);

		final int anio = this.getAnioActual();
		final int semanaAnio = this.getSemanaAnio();
		final String diaSemanaCorto = NOMBRES_DIAS_CORTOS[this.getIndiceDiaSemana()];

		this.cachedTextoLinea1HUD = "Año " + anio + " · Sem " + semanaAnio + " (" + diaSemanaCorto + ")";
		this.actualizarCacheLinea2();
	}

	public void calcularColorAmbiente() {
		final double h = this.horaActual;

		// 1. Efecto del Eclipse Solar a Mediodía (Resuelto con enteros primitivos y
		// Caché Directa)
		if ((this.fenomenoActivo == FenomenoAstronomico.ECLIPSE_SOLAR) && (h >= 11.0) && (h <= 14.0)) {
			final double progreso = 1.0 - (Math.abs(h - 12.5) / 1.5);
			final double campana = Math.sin(Math.max(0.0, Math.min(1.0, progreso)) * (Math.PI / 2.0));
			final int r = (int) (110 * campana);
			final int g = (int) (20 * campana);
			final int b = (int) (35 * campana);
			final int a = (int) (215 * campana);
			this.establecerColorDirecto(r, g, b, a);
			return;
		}

		// 2. Selección de Color Nocturno base
		final Color colorNoche;
		if (this.modoOscuridadTotal) {
			colorNoche = NOCHE_BLACKOUT;
		} else if (this.fenomenoActivo == FenomenoAstronomico.LUNA_ROJA) {
			colorNoche = NOCHE_LUNA_ROJA;
		} else if (this.fenomenoActivo == FenomenoAstronomico.CONJUNCION_ASTRAL) {
			colorNoche = NOCHE_CONJUNCION;
		} else if (this.fenomenoActivo == FenomenoAstronomico.AURORA_BOREAL) {
			colorNoche = NOCHE_AURORA;
		} else {
			colorNoche = NOCHE_ATMOSFERICA;
		}

		final double hAmanecer = this.horaAmanecerActual;
		final double hMadrugada = hAmanecer - 2.0;
		final double hPlenoDia = hAmanecer + 1.5;

		final double hAtardecer = this.horaAtardecerActual;
		final double hFinPlenoDia = hAtardecer - 1.5;
		final double hCrepusculo = hAtardecer + 1.5;
		final double hNoche = hAtardecer + 2.5;

		if ((h < hMadrugada) || (h >= hNoche)) {
			this.establecerColorDirecto(colorNoche.getRed(), colorNoche.getGreen(), colorNoche.getBlue(),
					colorNoche.getAlpha());
		} else if (h < hAmanecer) {
			final double f = (h - hMadrugada) / (hAmanecer - hMadrugada);
			this.interpolar(colorNoche, MADRUGADA, f);
		} else if (h < (hAmanecer + 0.75)) {
			final double f = (h - hAmanecer) / 0.75;
			this.interpolar(MADRUGADA, AMANECER, f);
		} else if (h < hPlenoDia) {
			final double f = (h - (hAmanecer + 0.75)) / (hPlenoDia - (hAmanecer + 0.75));
			this.interpolar(AMANECER, PLENO_DIA, f);
		} else if (h < hFinPlenoDia) {
			this.establecerColorDirecto(0, 0, 0, 0);
		} else if (h < hAtardecer) {
			final double f = (h - hFinPlenoDia) / (hAtardecer - hFinPlenoDia);
			this.interpolar(PLENO_DIA, ATARDECER, f);
		} else if (h < hCrepusculo) {
			final double f = (h - hAtardecer) / (hCrepusculo - hAtardecer);
			this.interpolar(ATARDECER, CREPUSCULO, f);
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
		this.establecerColorDirecto(r, g, b, a);
	}

	/**
	 * Almacena los escalares primitivos y resuelve el objeto Color mediante Caché
	 * Directa Zero-GC.
	 */
	private void establecerColorDirecto(final int r, final int g, final int b, final int a) {
		this.luzR = r;
		this.luzG = g;
		this.luzB = b;
		this.luzA = a;

		final int key = (r << 24) | (g << 16) | (b << 8) | a;
		final int idx = (key ^ (key >>> 16)) & CACHE_MASK;

		if ((this.cacheKeys[idx] == key) && (this.cacheColors[idx] != null)) {
			this.colorAmbienteActual = this.cacheColors[idx];
		} else {
			final Color nuevoColor = new Color(r, g, b, a);
			this.cacheKeys[idx] = key;
			this.cacheColors[idx] = nuevoColor;
			this.colorAmbienteActual = nuevoColor;
		}
	}

	// =========================================================================
	// 8. RENDERIZADO DE LA BÓVEDA CELESTE (ESTRELLAS FUGACES Y AURORAS)
	// =========================================================================
	public void pintar(final Graphics2D g) {
		// Los interiores y subterráneos no tienen visibilidad del cosmos
		if ((Globales.JUGADOR != null) && (Globales.JUGADOR.getMundo() != null)
				&& (Globales.JUGADOR.getMundo().getEscenario() != null)
				&& (Globales.JUGADOR.getMundo().getEscenario().getMetadatos() != null)
				&& Globales.JUGADOR.getMundo().getEscenario().getMetadatos().esEspacioInterior()) {
			return;
		}

		final int camX = (Globales.CAMARA != null) ? Globales.CAMARA.getPosicionXInt() : 0;
		final int camY = (Globales.CAMARA != null) ? Globales.CAMARA.getPosicionYInt() : 0;

		if (this.fenomenoActivo == FenomenoAstronomico.AURORA_BOREAL) {
			this.pintarAuroraBoreal(g, camX, camY);
		}

		if (this.fenomenoActivo == FenomenoAstronomico.LLUVIA_ESTRELLAS) {
			this.pintarEstrellasFugaces(g);
		}
		if (this.fenomenoActivo == FenomenoAstronomico.CONJUNCION_ASTRAL) {
			this.pintarConjuncionAstral(g, camX, camY);
		}
	}

	private void pintarConjuncionAstral(final Graphics2D g, final int camX, final int camY) {
		final int oscuridad = this.colorAmbienteActual.getAlpha();
		if (oscuridad < 60) {
			return;
		}

		final float factorNoche = Math.min(1.0f, (oscuridad - 60) / 110.0f);

		// Pulso armónico modulado en el Composite (Zero-GC)
		final double pulso = Math.sin(Globales.animacion * 0.05);
		final float factorBrillo = (float) (0.75 + (0.25 * pulso));

		final int idxComp = Math.max(0, Math.min(100, (int) (0.85f * factorNoche * factorBrillo * 100.0f)));
		g.setComposite(COMPOSITES_OPACIDAD[idxComp]);

		// Paralaje ultra-lejano en el firmamento
		final int baseX = (Constantes.CENTROX + 80) - (int) Math.round(camX * 0.02);
		final int baseY = 55 - (int) Math.round(camY * 0.02);

		for (int i = 0; i < 3; i++) {
			final int ax = baseX + ASTRO_OFF_X[i];
			final int ay = baseY + ASTRO_OFF_Y[i];

			// 1. Halo exterior con color estático
			Render2D.dibujarFiguraEllipseRefCamara(g, ax - 8, ay - 8, 16, 16, COLORES_HALOS_ASTROS[i]);

			// 2. Haz en cruz (+) de difracción con color estático
			g.setColor(COLORES_ASTROS[i]);
			g.drawLine(ax - 10, ay, ax + 10, ay);
			g.drawLine(ax, ay - 10, ax, ay + 10);

			// 3. Núcleo brillante blanco estático
			Render2D.dibujarRectanguloRelleno(g, ax - 1, ay - 1, 3, 3, Color.WHITE);
		}

		g.setComposite(COMPOSITE_OPACO);
	}

	private void pintarAuroraBoreal(final Graphics2D g, final int camX, final int camY) {
		// La aurora solo es visible si hay penumbra en el cielo (noche, madrugada o
		// crepúsculo)
		final int oscuridad = this.colorAmbienteActual.getAlpha();
		if (oscuridad < 70) {
			return; // De día la luz solar la hace invisible
		}

		// Opacidad modulada por la oscuridad nocturna
		final float factorNoche = Math.min(1.0f, (oscuridad - 70) / 100.0f);
		final int idxComp = Math.max(0, Math.min(100, (int) (0.65f * factorNoche * 100.0f)));
		g.setComposite(COMPOSITES_OPACIDAD[idxComp]);

		final double onda1 = Math.sin(this.faseOndaAurora * 0.7) * 20.0;
		final double onda2 = Math.cos(this.faseOndaAurora * 0.5) * 14.0;

		final int ox = Math.floorMod((int) Math.round((this.faseOndaAurora * 14.0) - (camX * 0.10)), ANCHO_AURORA_HD);
		final int oy = (int) Math.round((-(camY * 0.04) + onda1) - 15.0);

		// Margen amplio de triple pasada: garantiza cobertura total a la izquierda y
		// derecha
		for (int x = (-ANCHO_AURORA_HD * 2) + ox; x < (Constantes.ANCHO_JUEGO
				+ ANCHO_AURORA_HD); x += ANCHO_AURORA_HD) {
			Render2D.dibujarImagen(g, this.texturaAurora, x, oy);
			Render2D.dibujarImagen(g, this.texturaAurora, x + 240, (int) Math.round(oy + onda2 + 15.0));
		}
		g.setComposite(COMPOSITE_OPACO);
	}

	private void pintarEstrellasFugaces(final Graphics2D g) {
		g.setComposite(COMPOSITE_OPACO);
		for (int i = 0; i < MAX_ESTRELLAS_FUGAZ; i++) {
			if (this.estrellaActiva[i]) {
				final int x1 = (int) Math.round(this.estrellaX[i]);
				final int y1 = (int) Math.round(this.estrellaY[i]);
				final int x2 = (int) Math.round(this.estrellaX[i] + (this.estrellaLongitud[i] * 0.8));
				final int y2 = (int) Math.round(this.estrellaY[i] - (this.estrellaLongitud[i] * 0.5));

				Render2D.dibujarLinea(g, x1, y1, x2, y2, COLOR_ESTRELLA_TRAIL);
				Render2D.dibujarRectanguloRelleno(g, x1 - 1, y1 - 1, 3, 3, Color.WHITE);
			}
		}
	}

	private void actualizarEstrellasFugaces(final double dt) {
		if (this.fenomenoActivo == FenomenoAstronomico.LLUVIA_ESTRELLAS) {
			this.temporizadorSpawnEstrella += dt;
			if (this.temporizadorSpawnEstrella >= 0.45) {
				this.temporizadorSpawnEstrella = 0.0;
				for (int i = 0; i < MAX_ESTRELLAS_FUGAZ; i++) {
					if (!this.estrellaActiva[i]) {
						this.estrellaActiva[i] = true;
						this.estrellaX[i] = 100.0 + (Math.random() * (Constantes.ANCHO_JUEGO + 150));
						this.estrellaY[i] = -20.0 + (Math.random() * 80.0);
						this.estrellaVelX[i] = -(420.0 + (Math.random() * 200.0));
						this.estrellaVelY[i] = 280.0 + (Math.random() * 150.0);
						this.estrellaVida[i] = 0.55 + (Math.random() * 0.35);
						this.estrellaLongitud[i] = 25.0 + (Math.random() * 30.0);
						break;
					}
				}
			}
		}

		for (int i = 0; i < MAX_ESTRELLAS_FUGAZ; i++) {
			if (this.estrellaActiva[i]) {
				this.estrellaX[i] += this.estrellaVelX[i] * dt;
				this.estrellaY[i] += this.estrellaVelY[i] * dt;
				this.estrellaVida[i] -= dt;
				if ((this.estrellaVida[i] <= 0.0) || (this.estrellaX[i] < -100)
						|| (this.estrellaY[i] > (Constantes.ALTO_JUEGO + 50))) {
					this.estrellaActiva[i] = false;
				}
			}
		}
	}

	public void setModoOscuridadTotal(final boolean blackout) {
		this.modoOscuridadTotal = blackout;
		this.calcularColorAmbiente();
	}

	public boolean isModoOscuridadTotal() {
		return this.modoOscuridadTotal;
	}

	public double getDuracionDiaSegundos() {
		return this.duracionDiaSegundos;
	}

	// =========================================================================
	// 9. API DE CONSULTA Y CONTROL
	// =========================================================================
	public void activarFenomeno(final FenomenoAstronomico fenomeno, final double duracionSegundos) {
		this.fenomenoActivo = (fenomeno != null) ? fenomeno : FenomenoAstronomico.NORMAL;
		this.duracionFenomenoRestante = Math.max(0.0, duracionSegundos);
	}

	public void desactivarFenomeno() {
		this.fenomenoActivo = FenomenoAstronomico.NORMAL;
		this.duracionFenomenoRestante = 0.0;
	}

	public FenomenoAstronomico getFenomenoActivo() {
		return this.fenomenoActivo;
	}

	public FaseLunar getFaseLunarActual() {
		return FaseLunar.obtenerPorDia(this.diaActual);
	}

	public double getHoraActual() {
		return this.horaActual;
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

	public int getDiaActual() {
		return this.diaActual;
	}

	public void setDiaActual(final int dia) {
		this.diaActual = Math.max(1, dia);
		this.recalcularFotoperiodoYFechasSiCambioDia();
		this.calcularColorAmbiente();
	}

	public int getDiaDelAnio() {
		return ((this.diaActual - 1) % DIAS_POR_ANIO) + 1;
	}

	public int getDiaDeLaEstacion() {
		return ((this.diaActual - 1) % DIAS_POR_ESTACION) + 1;
	}

	public int getSemanaAnio() {
		return (((this.diaActual - 1) % DIAS_POR_ANIO) / DIAS_POR_SEMANA) + 1;
	}

	public int getSemanaDeLaEstacion() {
		return (((this.diaActual - 1) % DIAS_POR_ESTACION) / DIAS_POR_SEMANA) + 1;
	}

	public int getIndiceDiaSemana() {
		return (this.diaActual - 1) % DIAS_POR_SEMANA;
	}

	public String getNombreDiaSemanaCorto() {
		return NOMBRES_DIAS_CORTOS[this.getIndiceDiaSemana()];
	}

	public String getNombreDiaSemanaLargo() {
		return NOMBRES_DIAS_LARGOS[this.getIndiceDiaSemana()];
	}

	public Estacion getEstacionActual() {
		return Estacion.obtenerPorDiaAnio((this.diaActual - 1) % DIAS_POR_ANIO);
	}

	public int getAnioActual() {
		return 1 + ((this.diaActual - 1) / DIAS_POR_ANIO);
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
			this.actualizarCacheLinea2();
		}
		return this.cachedHora24h;
	}

	private void actualizarCacheLinea2() {
		this.cachedTextoLinea2HUD = this.getHoraFormato24h() + " · " + this.getEstacionActual().getNombre();
	}

	public String getTextoLinea1HUD() {
		return this.cachedTextoLinea1HUD;
	}

	public String getTextoLinea2HUD() {
		return this.cachedTextoLinea2HUD;
	}

	public Color getColorAmbienteActual() {
		return this.colorAmbienteActual;
	}

	public int getLuzR() {
		return this.luzR;
	}

	public int getLuzG() {
		return this.luzG;
	}

	public int getLuzB() {
		return this.luzB;
	}

	public int getLuzA() {
		return this.luzA;
	}

	public double getHoraAmanecer() {
		return this.horaAmanecerActual;
	}

	public double getHoraAtardecer() {
		return this.horaAtardecerActual;
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

	public void forzarLunaRoja() {
		this.setDiaActual(15); // Luna Llena
		this.setHora(21.5); // Entrada de la noche
		final double duracionSegs = (7.5 / 24.0) * this.duracionDiaSegundos;
		this.activarFenomeno(FenomenoAstronomico.LUNA_ROJA, duracionSegs);
		this.diasDesdeUltimaLunaRoja = 0;
	}

	public void forzarEclipse() {
		this.setDiaActual(1); // Luna Nueva
		this.setHora(12.0); // Mediodía
		final double duracionSegs = (2.0 / 24.0) * this.duracionDiaSegundos;
		this.activarFenomeno(FenomenoAstronomico.ECLIPSE_SOLAR, duracionSegs);
		this.diasDesdeUltimoEclipse = 0;
	}

	public void reiniciar() {
		this.horaActual = FaseDia.MEDIODIA.getHoraInicio();
		this.diaActual = 1;
		this.multiplicadorTiempo = 1.0;
		this.tiempoPausado = false;
		this.modoOscuridadTotal = false;
		this.fenomenoActivo = FenomenoAstronomico.NORMAL;
		this.duracionFenomenoRestante = 0.0;
		this.lastDiaRecalculo = -1;
		this.recalcularFotoperiodoYFechasSiCambioDia();
		this.calcularColorAmbiente();
	}

	// =========================================================================
	// 10. PERSISTENCIA JSON
	// =========================================================================
	@SuppressWarnings("unchecked")
	public JSONObject exportarJSON() {
		final JSONObject json = new JSONObject();
		json.put("diaActual", Integer.valueOf(this.diaActual));
		json.put("horaActual", Double.valueOf(this.horaActual));
		json.put("duracionDiaSegundos", Double.valueOf(this.duracionDiaSegundos));
		json.put("multiplicadorTiempo", Double.valueOf(this.multiplicadorTiempo));
		json.put("tiempoPausado", Boolean.valueOf(this.tiempoPausado));
		json.put("fenomenoActivo", this.fenomenoActivo.name());
		json.put("duracionFenomeno", Double.valueOf(this.duracionFenomenoRestante));
		json.put("diasDesdeLunaRoja", Integer.valueOf(this.diasDesdeUltimaLunaRoja));
		json.put("diasDesdeEclipse", Integer.valueOf(this.diasDesdeUltimoEclipse));
		// Metadatos informativos
		json.put("anio", Integer.valueOf(this.getAnioActual()));
		json.put("estacion", this.getEstacionActual().name());
		json.put("faseLunar", this.getFaseLunarActual().name());
		json.put("hora24h", this.getHoraFormato24h());
		return json;
	}

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
		if (json.get("diasDesdeLunaRoja") != null) {
			this.diasDesdeUltimaLunaRoja = ((Number) json.get("diasDesdeLunaRoja")).intValue();
		}
		if (json.get("diasDesdeEclipse") != null) {
			this.diasDesdeUltimoEclipse = ((Number) json.get("diasDesdeEclipse")).intValue();
		}
		if (json.get("fenomenoActivo") != null) {
			try {
				this.fenomenoActivo = FenomenoAstronomico.valueOf(json.get("fenomenoActivo").toString());
			} catch (final Exception ignored) {
				this.fenomenoActivo = FenomenoAstronomico.NORMAL;
			}
		}
		if (json.get("duracionFenomeno") != null) {
			this.duracionFenomenoRestante = Math.max(0.0, ((Number) json.get("duracionFenomeno")).doubleValue());
		}

		this.lastDiaRecalculo = -1;
		this.recalcularFotoperiodoYFechasSiCambioDia();
		this.calcularColorAmbiente();
	}
}