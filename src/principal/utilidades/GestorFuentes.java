package principal.utilidades;

import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestor centralizado y almacén Flyweight de tipografías (Zero-GC / O(1)).
 * Pre-computa tamaños comunes en VRAM/RAM y elimina deriveFont() en caliente.
 * 
 * @version 1.1 (Vanilla Java 8 - Service Locator Pattern)
 */
public class GestorFuentes {

	private final String FAMILIA_DEFECTO = Font.SANS_SERIF;
	private final int MAX_TAMANO_PRECALCULADO = 64;

	/** Matriz plana O(1): [Estilo (0..3)][Tamaño (1..64)] */
	private final Font[][] cachePlano = new Font[4][this.MAX_TAMANO_PRECALCULADO + 1];

	/** Mapa para tamaños decimales (ej. 6.5f) o fuentes especiales */
	private final Map<String, Font> cacheDinamico = new HashMap<String, Font>();

	public GestorFuentes() {
		// Pre-computación en tiempo de arranque (Boot-time)
		for (int estilo = Font.PLAIN; estilo <= Font.BOLD; estilo++) {
			for (int tam = 1; tam <= this.MAX_TAMANO_PRECALCULADO; tam++) {
				this.cachePlano[estilo][tam] = new Font(this.FAMILIA_DEFECTO, estilo, tam);
			}
		}
	}

	/**
	 * Obtiene una fuente estándar (PLAIN, SansSerif) en tiempo O(1).
	 */
	public Font getFuente(final float tamano) {
		return this.getFuente(this.FAMILIA_DEFECTO, Font.PLAIN, tamano);
	}

	/**
	 * Obtiene una fuente SansSerif con estilo específico (PLAIN, BOLD, ITALIC).
	 */
	public Font getFuente(final int estilo, final float tamano) {
		return this.getFuente(this.FAMILIA_DEFECTO, estilo, tamano);
	}

	/**
	 * Obtiene o almacena en caché dinámico cualquier combinación de fuente.
	 */
	public Font getFuente(final String familia, final int estilo, final float tamano) {
		final int estiloValidado = Math.max(0, Math.min(3, estilo));
		final boolean esFamiliaDefecto = (familia == null) || familia.equals(this.FAMILIA_DEFECTO);

		// 1. Acceso directo por índice de arreglo O(1)
		final int tamInt = Math.round(tamano);
		final boolean esEnteroExacto = Math.abs(tamano - tamInt) < 0.001f;

		if (esFamiliaDefecto && esEnteroExacto && (tamInt >= 1) && (tamInt <= this.MAX_TAMANO_PRECALCULADO)) {
			Font f = this.cachePlano[estiloValidado][tamInt];
			if (f == null) {
				f = new Font(this.FAMILIA_DEFECTO, estiloValidado, tamInt);
				this.cachePlano[estiloValidado][tamInt] = f;
			}
			return f;
		}

		// 2. Caché dinámico para tamaños fraccionarios
		final String clave = (familia != null ? familia : this.FAMILIA_DEFECTO) + "_" + estiloValidado + "_" + tamano;
		Font fuente = this.cacheDinamico.get(clave);

		if (fuente == null) {
			fuente = new Font(familia != null ? familia : this.FAMILIA_DEFECTO, estiloValidado, tamInt)
					.deriveFont(tamano);
			this.cacheDinamico.put(clave, fuente);
		}

		return fuente;
	}
}