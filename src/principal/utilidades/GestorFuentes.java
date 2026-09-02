package principal.utilidades;

import java.awt.Font;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestor centralizado y almacén Flyweight de tipografías (Zero-GC / O(1)).
 * Carga 'm5x7.ttf' como fuente maestra pixel-art y pre-deriva tamaños en
 * VRAM/RAM.
 * 
 * @version 2.0 (Vanilla Java 8)
 */
public class GestorFuentes {

	private static final String RUTA_FUENTE_PIXEL = "/fuentes/m5x7.ttf";
	private static final String FAMILIA_FALLBACK = Font.SANS_SERIF;
	private static final int MAX_TAMANO_PRECALCULADO = 64;

	private Font fuenteBasePixel;

	/** Matriz plana O(1): [Estilo (0..3)][Tamaño (1..64)] */
	private final Font[][] cachePlano = new Font[4][MAX_TAMANO_PRECALCULADO + 1];

	/** Caché para tamaños fraccionarios excepcionales */
	private final Map<String, Font> cacheDinamico = new HashMap<String, Font>();

	public GestorFuentes() {
		this.cargarFuenteBase();
		this.precalcularCache();
	}

	/**
	 * Carga el archivo TTF del classpath con try-with-resources y fallback seguro.
	 */
	private void cargarFuenteBase() {
		try (final InputStream is = GestorFuentes.class.getResourceAsStream(RUTA_FUENTE_PIXEL)) {
			if (is != null) {
				this.fuenteBasePixel = Font.createFont(Font.TRUETYPE_FONT, is);
				System.out.println("[GestorFuentes] Tipografia Pixel 'm5x7.ttf' cargada correctamente.");
			} else {
				System.err.println(
						"[GestorFuentes] Advertencia: No se encontro '" + RUTA_FUENTE_PIXEL + "'. Usando fallback.");
				this.fuenteBasePixel = new Font(FAMILIA_FALLBACK, Font.PLAIN, 12);
			}
		} catch (final Exception e) {
			System.err.println("[GestorFuentes] Error al procesar la fuente: " + e.getMessage());
			this.fuenteBasePixel = new Font(FAMILIA_FALLBACK, Font.PLAIN, 12);
		}
	}

	/**
	 * Llena la tabla de consulta directa para acceso instantáneo O(1).
	 */
	private void precalcularCache() {
		for (int estilo = Font.PLAIN; estilo <= Font.BOLD; estilo++) {
			for (int tam = 1; tam <= MAX_TAMANO_PRECALCULADO; tam++) {
				this.cachePlano[estilo][tam] = this.fuenteBasePixel.deriveFont(estilo, tam);
			}
		}
	}

	/**
	 * Obtiene la fuente pixel en estilo PLAIN en tiempo O(1) puro.
	 */
	public Font getFuente(final float tamano) {
		return this.getFuente(Font.PLAIN, tamano);
	}

	/**
	 * Obtiene la fuente pixel con estilo (PLAIN, BOLD) en tiempo O(1) puro.
	 */
	public Font getFuente(final int estilo, final float tamano) {
		final int estiloValidado = Math.max(0, Math.min(3, estilo));
		final int tamInt = Math.round(tamano);
		final boolean esEnteroExacto = Math.abs(tamano - tamInt) < 0.001f;

		if (esEnteroExacto && (tamInt >= 1) && (tamInt <= MAX_TAMANO_PRECALCULADO)) {
			return this.cachePlano[estiloValidado][tamInt];
		}

		// Fallback dinámico para tamaños flotantes poco comunes
		final String clave = "pixel_" + estiloValidado + "_" + tamano;
		Font f = this.cacheDinamico.get(clave);
		if (f == null) {
			f = this.fuenteBasePixel.deriveFont(estiloValidado, tamano);
			this.cacheDinamico.put(clave, f);
		}
		return f;
	}

	/**
	 * Sobrecarga por compatibilidad para fuentes del sistema si fueran necesarias.
	 */
	public Font getFuente(final String familia, final int estilo, final float tamano) {
		if ((familia == null) || familia.equalsIgnoreCase("pixel") || familia.equalsIgnoreCase("m5x7")) {
			return this.getFuente(estilo, tamano);
		}
		// Consulta genérica para fuentes estándar del SO
		final String clave = familia + "_" + estilo + "_" + tamano;
		Font f = this.cacheDinamico.get(clave);
		if (f == null) {
			f = new Font(familia, estilo, Math.round(tamano)).deriveFont(tamano);
			this.cacheDinamico.put(clave, f);
		}
		return f;
	}
}