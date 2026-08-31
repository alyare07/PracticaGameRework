package principal.iluminacion;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

/**
 * Gestor de haces de luz volumétricos diurnos (God Rays) con desvanecimiento
 * elíptico 2D suave, anclaje espacial al mundo y motas de polvo en suspensión
 * (Zero-GC / O(1)).
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public class GestorRayosSol {

	// =========================================================================
	// === 1. CONSTANTES Y CONFIGURACIÓN
	// =========================================================================

	private static final int TOTAL_MOTAS = 24;
	private static final int PERIODO_ESPACIADO_HACES = 280; // Distancia entre haces en píxeles

	private static final int ANCHO_HAZ_HD = 850;
	private static final int ALTO_HAZ_HD = 140;

	// Colores cálidos calibrados (no lechosos)
	private static final Color COLOR_AMANECER = new Color(255, 220, 150);
	private static final Color COLOR_ATARDECER = new Color(255, 150, 60);

	// LUT de 101 Composites pre-instanciados (Opacidad sutil máx 18%)
	private static final AlphaComposite[] COMPOSITES = new AlphaComposite[101];
	static {
		for (int i = 0; i <= 100; i++) {
			COMPOSITES[i] = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (i / 100.0f) * 0.18f);
		}
	}

	private static AlphaComposite obtenerComposite(final float factor) {
		final int idx = Math.max(0, Math.min(100, Math.round(factor * 100.0f)));
		return COMPOSITES[idx];
	}

	/** Textura pre-horneada con desvanecimiento elíptico 2D continuo. */
	private final BufferedImage texturaHazLuz;

	// =========================================================================
	// === 2. ESTADO Y MOTAS DE POLVO
	// =========================================================================

	private final double[] motaX = new double[TOTAL_MOTAS];
	private final double[] motaY = new double[TOTAL_MOTAS];
	private final double[] motaVelY = new double[TOTAL_MOTAS];
	private final double[] motaFase = new double[TOTAL_MOTAS];

	private double derivaViento = 0.0;
	private double tiempoAcumulado = 0.0;
	private float opacidadEfectiva = 0.0f;
	private double anguloRotacion = Math.toRadians(35.0);
	private boolean esTarde = false;
	private Color colorTinteActual = COLOR_AMANECER;

	// =========================================================================
	// === CONSTRUCTOR
	// =========================================================================

	public GestorRayosSol() {
		this.texturaHazLuz = this.hornearTexturaHazSuave();

		// Inicialización de motas de polvo
		for (int i = 0; i < TOTAL_MOTAS; i++) {
			this.motaX[i] = Math.random() * Constantes.ANCHO_JUEGO;
			this.motaY[i] = Math.random() * Constantes.ALTO_JUEGO;
			this.motaVelY[i] = 6.0 + (Math.random() * 10.0);
			this.motaFase[i] = Math.random() * Math.PI * 2.0;
		}
	}

	// =========================================================================
	// === PRE-HORNEADO CON ENVOLVENTE 2D (CERO BORDES RECTOS)
	// =========================================================================

	/**
	 * Hornea un haz de luz con envolvente senoidal bidimensional (X e Y),
	 * garantizando que los 4 bordes alcancen opacidad 0% de forma suave.
	 */
	private BufferedImage hornearTexturaHazSuave() {
		final int w = ANCHO_HAZ_HD;
		final int h = ALTO_HAZ_HD;
		final BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

		for (int y = 0; y < h; y++) {
			// Envolvente vertical suave (curva cúbica de campana)
			final double sinY = Math.sin((y / (double) h) * Math.PI);
			final double factorY = sinY * sinY * sinY;

			for (int x = 0; x < w; x++) {
				// Envolvente horizontal suave (desvanecimiento gradual en los extremos)
				final double sinX = Math.sin((x / (double) w) * Math.PI);
				final double factorX = Math.sin(sinX * (Math.PI / 2.0));

				final double alphaNormalizado = Math.max(0.0, Math.min(1.0, factorY * factorX));
				final int alpha = (int) (alphaNormalizado * 255.0);

				// Tinte blanco-dorado cálido en VRAM
				final int rgba = (alpha << 24) | (255 << 16) | (235 << 8) | 170;
				img.setRGB(x, y, rgba);
			}
		}
		return img;
	}

	// =========================================================================
	// === ACTUALIZACIÓN LÓGICA (60 APS)
	// =========================================================================

	public void actualizar(final double dt, final double horaActual, final boolean esInterior,
			final boolean hayTormenta) {
		float opacidadObjetivo = 0.0f;

		if (!esInterior && !hayTormenta) {
			if ((horaActual >= 6.0) && (horaActual <= 11.0)) {
				// Mañana: Haces de izquierda a derecha (Amanecer cálido)
				final double f = (horaActual < 8.0) ? (horaActual - 6.0) / 2.0 : 1.0 - ((horaActual - 8.0) / 3.0);
				opacidadObjetivo = (float) Math.max(0.0, Math.min(1.0, f));
				this.anguloRotacion = Math.toRadians(35.0);
				this.esTarde = false;
				this.colorTinteActual = COLOR_AMANECER;

			} else if ((horaActual >= 15.5) && (horaActual <= 19.5)) {
				// Tarde: Haces invertidos de derecha a izquierda (Atardecer ámbar)
				final double f = (horaActual < 17.5) ? (horaActual - 15.5) / 2.0 : 1.0 - ((horaActual - 17.5) / 2.0);
				opacidadObjetivo = (float) Math.max(0.0, Math.min(1.0, f));
				this.anguloRotacion = Math.toRadians(145.0);
				this.esTarde = true;
				this.colorTinteActual = COLOR_ATARDECER;
			}
		}

		// Interpolación suave de opacidad
		this.opacidadEfectiva += (opacidadObjetivo - this.opacidadEfectiva) * (dt * 1.5);

		if (this.opacidadEfectiva <= 0.005f) {
			this.opacidadEfectiva = 0.0f;
			return;
		}

		this.tiempoAcumulado += dt;

		// Desplazamiento sutil por viento
		this.derivaViento = (this.derivaViento + (dt * 6.0)) % PERIODO_ESPACIADO_HACES;

		// Movimiento de motas de polvo
		for (int i = 0; i < TOTAL_MOTAS; i++) {
			this.motaFase[i] += dt * 1.5;
			this.motaX[i] += Math.sin(this.motaFase[i]) * 6.0 * dt;
			this.motaY[i] += this.motaVelY[i] * dt;

			if (this.motaY[i] > Constantes.ALTO_JUEGO) {
				this.motaY[i] = -10.0;
				this.motaX[i] = Math.random() * Constantes.ANCHO_JUEGO;
			}
		}
	}

	// =========================================================================
	// === RENDERIZADO CON ANCLAJE ESPACIAL AL MUNDO (ZERO-GC)
	// =========================================================================

	public void pintar(final Graphics2D g) {
		if (this.opacidadEfectiva <= 0.0f) {
			return;
		}

		final int camX = (Globales.CAMARA != null) ? Globales.CAMARA.getPosicionXInt() : 0;

		g.setComposite(obtenerComposite(this.opacidadEfectiva));

		// Anclaje al mundo: los haces se desplazan con la cámara (Paralaje 75%)
		final int ox = Math.floorMod((int) Math.round(this.derivaViento - (camX * 0.75)), PERIODO_ESPACIADO_HACES);

		final int ancho = ANCHO_HAZ_HD;
		final int alto = ALTO_HAZ_HD;
		final int yPivot = -60;

		if (!this.esTarde) {
			// MAÑANA: Rayos naciendo en la parte superior izquierda
			for (int x = (-PERIODO_ESPACIADO_HACES + ox) - 200; x < (Constantes.ANCHO_JUEGO
					+ 200); x += PERIODO_ESPACIADO_HACES) {
				g.rotate(this.anguloRotacion, x, yPivot);
				g.drawImage(this.texturaHazLuz, x, yPivot, ancho, alto, null);
				Render2D.registrarLlamadas(1);
				g.rotate(-this.anguloRotacion, x, yPivot);
			}
		} else {
			// TARDE: Rayos naciendo en la parte superior derecha
			for (int x = -PERIODO_ESPACIADO_HACES + ox; x < (Constantes.ANCHO_JUEGO
					+ 400); x += PERIODO_ESPACIADO_HACES) {
				final int rx = (Constantes.ANCHO_JUEGO + 200) - x;
				g.rotate(this.anguloRotacion, rx, yPivot);
				g.drawImage(this.texturaHazLuz, rx, yPivot, ancho, alto, null);
				Render2D.registrarLlamadas(1);
				g.rotate(-this.anguloRotacion, rx, yPivot);
			}
		}

		// Motas de polvo brillantes
		g.setColor(this.colorTinteActual);
		for (int i = 0; i < TOTAL_MOTAS; i++) {
			final int mx = (int) Math.round(this.motaX[i]);
			final int my = (int) Math.round(this.motaY[i]);
			Render2D.dibujarRectanguloRelleno(g, mx, my, 2, 2, this.colorTinteActual);
		}

		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
	}
}