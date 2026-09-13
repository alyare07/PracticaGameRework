package principal.utilidades;

import java.awt.Font;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestor centralizado y almacén Flyweight de tipografías Pixel-Art (Zero-GC /
 * O(1)). Administra 'm5x7.ttf' (estándar) y 'm3x6.ttf' (micro / compacta).
 * 
 * @version 3.0 (Vanilla Java 8 - Dual Pixel Font Architecture)
 */
public class GestorFuentes {

	private static final String RUTA_FUENTE_PIXEL = "/fuentes/m5x7.ttf";
	private static final String RUTA_FUENTE_PIXEL_SMALL = "/fuentes/m3x6.ttf";
	private static final String FAMILIA_FALLBACK = Font.SANS_SERIF;
	private static final int MAX_TAMANO_PRECALCULADO = 64;

	private Font fuenteBasePixel;
	private Font fuenteBasePixelSmall;

	/** Matriz plana O(1) para m5x7: [Estilo (0..3)][Tamaño (1..64)] */
	private final Font[][] cachePlano = new Font[4][MAX_TAMANO_PRECALCULADO + 1];

	/** Matriz plana O(1) para m3x6: [Estilo (0..3)][Tamaño (1..64)] */
	private final Font[][] cachePlanoSmall = new Font[4][MAX_TAMANO_PRECALCULADO + 1];

	/** Caché para tamaños fraccionarios excepcionales */
	private final Map<String, Font> cacheDinamico = new HashMap<String, Font>();

	public GestorFuentes() {
		this.cargarFuentesBase();
		this.precalcularCache();
	}

	private void cargarFuentesBase() {
		// 1. Cargar fuente estándar (m5x7)
		try (final InputStream is = GestorFuentes.class.getResourceAsStream(RUTA_FUENTE_PIXEL)) {
			if (is != null) {
				this.fuenteBasePixel = Font.createFont(Font.TRUETYPE_FONT, is);
				System.out.println("[GestorFuentes] Tipografia Estandar 'm5x7.ttf' cargada correctamente.");
			} else {
				System.err.println("[GestorFuentes] Advertencia: No se encontro '" + RUTA_FUENTE_PIXEL + "'.");
				this.fuenteBasePixel = new Font(FAMILIA_FALLBACK, Font.PLAIN, 12);
			}
		} catch (final Exception e) {
			System.err.println("[GestorFuentes] Error cargando 'm5x7.ttf': " + e.getMessage());
			this.fuenteBasePixel = new Font(FAMILIA_FALLBACK, Font.PLAIN, 12);
		}

		// 2. Cargar fuente micro (m3x6)
		try (final InputStream isSmall = GestorFuentes.class.getResourceAsStream(RUTA_FUENTE_PIXEL_SMALL)) {
			if (isSmall != null) {
				this.fuenteBasePixelSmall = Font.createFont(Font.TRUETYPE_FONT, isSmall);
				System.out.println("[GestorFuentes] Tipografia Micro 'm3x6.ttf' cargada correctamente.");
			} else {
				System.err.println("[GestorFuentes] Advertencia: No se encontro '" + RUTA_FUENTE_PIXEL_SMALL
						+ "'. Usando m5x7 como fallback.");
				this.fuenteBasePixelSmall = this.fuenteBasePixel;
			}
		} catch (final Exception e) {
			System.err.println("[GestorFuentes] Error cargando 'm3x6.ttf': " + e.getMessage());
			this.fuenteBasePixelSmall = this.fuenteBasePixel;
		}
	}

	private void precalcularCache() {
		for (int estilo = Font.PLAIN; estilo <= Font.BOLD; estilo++) {
			for (int tam = 1; tam <= MAX_TAMANO_PRECALCULADO; tam++) {
				this.cachePlano[estilo][tam] = this.fuenteBasePixel.deriveFont(estilo, tam);
				this.cachePlanoSmall[estilo][tam] = this.fuenteBasePixelSmall.deriveFont(estilo, tam);
			}
		}
	}

	// =========================================================================
	// === 1. FUENTE ESTÁNDAR (m5x7.ttf) - Diálogos, Inventario, Menús, HUD
	// =========================================================================

	public Font getFuente(final float tamano) {
		return this.getFuente(Font.PLAIN, tamano);
	}

	public Font getFuente(final int estilo, final float tamano) {
		final int estiloValidado = Math.max(0, Math.min(3, estilo));
		final int tamInt = Math.round(tamano);
		final boolean esEnteroExacto = Math.abs(tamano - tamInt) < 0.001f;

		if (esEnteroExacto && (tamInt >= 1) && (tamInt <= MAX_TAMANO_PRECALCULADO)) {
			return this.cachePlano[estiloValidado][tamInt];
		}

		final String clave = "pixel_" + estiloValidado + "_" + tamano;
		Font f = this.cacheDinamico.get(clave);
		if (f == null) {
			f = this.fuenteBasePixel.deriveFont(estiloValidado, tamano);
			this.cacheDinamico.put(clave, f);
		}
		return f;
	}

	// =========================================================================
	// === 2. FUENTE MICRO / COMPACTA (m3x6.ttf) - Termómetro, Badges, Balas
	// =========================================================================

	public Font getFuenteSmall(final float tamano) {
		return this.getFuenteSmall(Font.PLAIN, tamano);
	}

	public Font getFuenteSmall(final int estilo, final float tamano) {
		final int estiloValidado = Math.max(0, Math.min(3, estilo));
		final int tamInt = Math.round(tamano);
		final boolean esEnteroExacto = Math.abs(tamano - tamInt) < 0.001f;

		if (esEnteroExacto && (tamInt >= 1) && (tamInt <= MAX_TAMANO_PRECALCULADO)) {
			return this.cachePlanoSmall[estiloValidado][tamInt];
		}

		final String clave = "small_" + estiloValidado + "_" + tamano;
		Font f = this.cacheDinamico.get(clave);
		if (f == null) {
			f = this.fuenteBasePixelSmall.deriveFont(estiloValidado, tamano);
			this.cacheDinamico.put(clave, f);
		}
		return f;
	}

	// =========================================================================
	// === 3. SOBRECARGA GENÉRICA
	// =========================================================================

	public Font getFuente(final String familia, final int estilo, final float tamano) {
		if ((familia == null) || familia.equalsIgnoreCase("pixel") || familia.equalsIgnoreCase("m5x7")) {
			return this.getFuente(estilo, tamano);
		}
		if (familia.equalsIgnoreCase("small") || familia.equalsIgnoreCase("m3x6")
				|| familia.equalsIgnoreCase("micro")) {
			return this.getFuenteSmall(estilo, tamano);
		}

		final String clave = familia + "_" + estilo + "_" + tamano;
		Font f = this.cacheDinamico.get(clave);
		if (f == null) {
			f = new Font(familia, estilo, Math.round(tamano)).deriveFont(tamano);
			this.cacheDinamico.put(clave, f);
		}
		return f;
	}
}