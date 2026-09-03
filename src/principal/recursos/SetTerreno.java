package principal.recursos;

import java.awt.image.BufferedImage;

/**
 * Contenedor inmutable de alta velocidad que almacena las 20 texturas de
 * autotile (16 bordes/esquinas + 4 variaciones decorativas de centro) para un
 * terreno. Resuelve el sprite exacto en O(1) puro sin búsquedas hash ni boxing.
 * 
 * @version 1.0 (Vanilla Java 8 - Zero-GC)
 */
public class SetTerreno {

	private final TipoTerreno tipo;
	private final int cantFrames;

	/**
	 * Matriz plana de texturas: Dimensión 1: Frame de animación (0..cantFrames-1).
	 * Dimensión 2: Índice de textura del autotile (0..15 bordes, 16..19 variaciones
	 * centro).
	 */
	private final BufferedImage[][] sprites;

	public SetTerreno(final TipoTerreno tipo, final BufferedImage[][] sprites) {
		this.tipo = tipo;
		this.cantFrames = ((sprites != null) && (sprites.length > 0)) ? sprites.length : 1;
		this.sprites = sprites;
	}

	/**
	 * Resuelve la textura de VRAM correspondiente en O(1) puro.
	 * 
	 * @param mascaraBit      Máscara de 4 bits cardinales (0..15).
	 * @param variacionCentro Índice determinista de variación decorativa (0..3).
	 * @param frameAnimacion  Fotograma global del reloj de animación.
	 * @return Instancia acelerada de BufferedImage en VRAM.
	 */
	public BufferedImage getSprite(final byte mascaraBit, final byte variacionCentro, final int frameAnimacion) {
		final int f = (this.cantFrames > 1) ? (frameAnimacion % this.cantFrames) : 0;
		final BufferedImage[] tira = this.sprites[f];

		// Máscara 15 representa centro rodeado en las 4 direcciones (aplica variación
		// de centro)
		if (mascaraBit == 15) {
			final int indiceVariacion = 16 + (Math.abs(variacionCentro) % 4);
			return tira[indiceVariacion];
		}

		// Bordes y esquinas de autotile (máscaras 0 a 14)
		final int m = Math.max(0, Math.min(15, mascaraBit));
		return tira[m];
	}

	public TipoTerreno getTipo() {
		return this.tipo;
	}

	public int getCantFrames() {
		return this.cantFrames;
	}

	public BufferedImage getSpriteBase() {
		return this.sprites[0][16]; // Centro limpio sin variaciones
	}
}