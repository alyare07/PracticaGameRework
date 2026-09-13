package principal.igu;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import principal.clima.GestorTermicoJugador;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

/**
 * Overlay de pantalla completa (640x360) que renderiza viñetas periféricas con
 * bypass instantáneo de dibujo cuando la opacidad es nula (Zero-GC).
 * 
 * @version 1.2 (Vanilla Java 8)
 */
public class VinetaTermicaIGU {

	private static final int ANCHO = Constantes.ANCHO_JUEGO;
	private static final int ALTO = Constantes.ALTO_JUEGO;

	private static final AlphaComposite[] COMPOSITES_OPACIDAD = new AlphaComposite[101];
	static {
		for (int i = 0; i <= 100; i++) {
			COMPOSITES_OPACIDAD[i] = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, i / 100.0f);
		}
	}

	private static AlphaComposite obtenerComposite(final float opacidad) {
		final int indice = Math.max(0, Math.min(100, Math.round(opacidad * 100.0f)));
		return COMPOSITES_OPACIDAD[indice];
	}

	private final BufferedImage texturaEscarcha;
	private final BufferedImage texturaCalor;

	private float opacidadFrioActual = 0.0f;
	private float opacidadCalorActual = 0.0f;

	public VinetaTermicaIGU() {
		this.texturaEscarcha = this.hornearTexturaEscarcha();
		this.texturaCalor = this.hornearTexturaCalor();
	}

	private BufferedImage hornearTexturaEscarcha() {
		final BufferedImage img = new BufferedImage(ANCHO, ALTO, BufferedImage.TYPE_INT_ARGB);
		final double cx = ANCHO / 2.0;
		final double cy = ALTO / 2.0;
		final double maxDist = Math.sqrt((cx * cx) + (cy * cy));

		for (int y = 0; y < ALTO; y++) {
			for (int x = 0; x < ANCHO; x++) {
				final double dx = x - cx;
				final double dy = y - cy;
				final double dist = Math.sqrt((dx * dx) + (dy * dy));
				final double distNorm = dist / maxDist;

				final double angulo = Math.atan2(dy, dx);
				final double ruidoCristal = (Math.sin(angulo * 14.0) * 0.04) + (Math.cos(angulo * 8.0) * 0.03);

				final double factorBorde = distNorm + ruidoCristal;

				if (factorBorde > 0.45) {
					final double t = (factorBorde - 0.45) / 0.55;
					final double curva = Math.pow(Math.min(1.0, Math.max(0.0, t)), 1.8);

					final int alpha = (int) Math.round(curva * 255.0);
					final int r = Math.min(255, 180 + (int) (curva * 75));
					final int g = Math.min(255, 220 + (int) (curva * 35));
					final int b = 255;

					final int rgba = (alpha << 24) | (r << 16) | (g << 8) | b;
					img.setRGB(x, y, rgba);
				} else {
					img.setRGB(x, y, 0);
				}
			}
		}
		return img;
	}

	private BufferedImage hornearTexturaCalor() {
		final BufferedImage img = new BufferedImage(ANCHO, ALTO, BufferedImage.TYPE_INT_ARGB);
		final double cx = ANCHO / 2.0;
		final double cy = ALTO / 2.0;
		final double maxDist = Math.sqrt((cx * cx) + (cy * cy));

		for (int y = 0; y < ALTO; y++) {
			for (int x = 0; x < ANCHO; x++) {
				final double dx = x - cx;
				final double dy = y - cy;
				final double dist = Math.sqrt((dx * dx) + (dy * dy));
				final double distNorm = dist / maxDist;

				final double angulo = Math.atan2(dy, dx);
				final double ondaCalor = Math.sin(angulo * 6.0) * 0.025;

				final double factorBorde = distNorm + ondaCalor;

				if (factorBorde > 0.48) {
					final double t = (factorBorde - 0.48) / 0.52;
					final double curva = Math.pow(Math.min(1.0, Math.max(0.0, t)), 1.6);

					final int alpha = (int) Math.round(curva * 240.0);
					final int r = 255;
					final int g = Math.max(0, 75 - (int) (curva * 40));
					final int b = Math.max(0, 20 - (int) (curva * 20));

					final int rgba = (alpha << 24) | (r << 16) | (g << 8) | b;
					img.setRGB(x, y, rgba);
				} else {
					img.setRGB(x, y, 0);
				}
			}
		}
		return img;
	}

	public void actualizar() {
		if (Globales.GESTOR_TERMICO_JUGADOR == null) {
			this.opacidadFrioActual = 0.0f;
			this.opacidadCalorActual = 0.0f;
			return;
		}

		final double dt = (Globales.delta > 0.0) ? Globales.delta : (1.0 / 60.0);
		final double tempCorp = Globales.GESTOR_TERMICO_JUGADOR.getTemperaturaCorporal();

		float targetFrio = 0.0f;
		if (tempCorp < GestorTermicoJugador.UMBRAL_HIPOTERMIA_NIVEL_1) {
			if (tempCorp < GestorTermicoJugador.UMBRAL_HIPOTERMIA_NIVEL_3) {
				final float pulso = (float) (Math.sin(Globales.animacion * 0.15) * 0.08);
				targetFrio = Math.min(0.92f, 0.78f + pulso);
			} else if (tempCorp < GestorTermicoJugador.UMBRAL_HIPOTERMIA_NIVEL_2) {
				final double t = (GestorTermicoJugador.UMBRAL_HIPOTERMIA_NIVEL_1 - tempCorp)
						/ (GestorTermicoJugador.UMBRAL_HIPOTERMIA_NIVEL_1
								- GestorTermicoJugador.UMBRAL_HIPOTERMIA_NIVEL_3);
				targetFrio = (float) Math.min(0.65, 0.35 + (t * 0.30));
			} else {
				targetFrio = 0.22f;
			}
		}

		float targetCalor = 0.0f;
		if (tempCorp > GestorTermicoJugador.UMBRAL_HIPERTERMIA_NIVEL_1) {
			if (tempCorp >= GestorTermicoJugador.UMBRAL_HIPERTERMIA_NIVEL_3) {
				final float pulso = (float) (Math.sin(Globales.animacion * 0.20) * 0.09);
				targetCalor = Math.min(0.88f, 0.72f + pulso);
			} else if (tempCorp >= GestorTermicoJugador.UMBRAL_HIPERTERMIA_NIVEL_2) {
				final double t = (tempCorp - GestorTermicoJugador.UMBRAL_HIPERTERMIA_NIVEL_1)
						/ (GestorTermicoJugador.UMBRAL_HIPERTERMIA_NIVEL_3
								- GestorTermicoJugador.UMBRAL_HIPERTERMIA_NIVEL_1);
				targetCalor = (float) Math.min(0.60, 0.30 + (t * 0.30));
			} else {
				targetCalor = 0.20f;
			}
		}

		this.opacidadFrioActual += (targetFrio - this.opacidadFrioActual) * (dt * 2.5);
		this.opacidadCalorActual += (targetCalor - this.opacidadCalorActual) * (dt * 2.5);

		if (this.opacidadFrioActual < 0.005f) {
			this.opacidadFrioActual = 0.0f;
		}
		if (this.opacidadCalorActual < 0.005f) {
			this.opacidadCalorActual = 0.0f;
		}
	}

	public void pintar(final Graphics2D g) {
		// Bypass de Fill-Rate: Si ambas opacidades son 0, no tocamos el composite ni
		// dibujamos en pantalla
		if ((this.opacidadFrioActual <= 0.0f) && (this.opacidadCalorActual <= 0.0f)) {
			return;
		}

		if (this.opacidadFrioActual > 0.0f) {
			g.setComposite(obtenerComposite(this.opacidadFrioActual));
			Render2D.dibujarImagen(g, this.texturaEscarcha, 0, 0);
		}

		if (this.opacidadCalorActual > 0.0f) {
			g.setComposite(obtenerComposite(this.opacidadCalorActual));
			Render2D.dibujarImagen(g, this.texturaCalor, 0, 0);
		}

		g.setComposite(obtenerComposite(1.0f));
	}
}