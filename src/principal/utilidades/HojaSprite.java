package principal.utilidades;

import java.awt.image.BufferedImage;

/**
 * Administra y recorta una cuadrícula de sprites individuales desde una textura
 * maestra, pre-horneando en VRAM las máscaras blancas para el efecto Hit-Flash
 * (Zero-GC).
 * 
 * @version 3.0 (Vanilla Java 8)
 */
public class HojaSprite {

	protected final BufferedImage[] sprites;
	protected final BufferedImage[] spritesFlash;

	protected final int anchoSprite;
	protected final int altoSprite;
	protected final int cantidadSprite;

	public HojaSprite(final BufferedImage imagen, final int lado, final boolean opaca) {
		this(imagen, lado, lado, opaca);
	}

	public HojaSprite(final BufferedImage imagen, final int ancho, final int alto, final boolean opaca) {
		this.anchoSprite = ancho;
		this.altoSprite = alto;

		final int columnas = Math.max(1, imagen.getWidth() / ancho);
		final int filas = Math.max(1, imagen.getHeight() / alto);
		this.cantidadSprite = columnas * filas;

		this.sprites = new BufferedImage[this.cantidadSprite];
		this.spritesFlash = new BufferedImage[this.cantidadSprite];

		int index = 0;
		for (int f = 0; f < filas; f++) {
			for (int c = 0; c < columnas; c++) {
				final BufferedImage recorte = imagen.getSubimage(c * ancho, f * alto, ancho, alto);
				this.sprites[index] = Globales.FUNCIONES.TEXTURAS_TOOLS.convertirAVRAM(recorte);
				this.spritesFlash[index] = Globales.FUNCIONES.TEXTURAS_TOOLS.crearMascaraBlanca(this.sprites[index]);
				index++;
			}
		}
	}

	/**
	 * Constructor interno que permite crear instancias pre-procesadas (como hojas
	 * volteadas).
	 */
	public HojaSprite(final BufferedImage[] sprites, final BufferedImage[] spritesFlash, final int ancho,
			final int alto) {
		this.sprites = sprites;
		this.spritesFlash = spritesFlash;
		this.anchoSprite = ancho;
		this.altoSprite = alto;
		this.cantidadSprite = (sprites != null) ? sprites.length : 0;
	}

	@Deprecated
	public HojaSprite(final String ruta, final int lado, final boolean opaca) {
		this((Globales.GESTOR_TEXTURAS != null) ? Globales.GESTOR_TEXTURAS.getImagenBase(ruta)
				: Globales.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida(ruta), lado, opaca);
	}

	/**
	 * Crea una nueva hoja con todos los fotogramas y sus máscaras flash volteados
	 * horizontalmente en VRAM.
	 */
	public HojaSprite crearVolteadaHorizontal() {
		final BufferedImage[] vSprites = new BufferedImage[this.cantidadSprite];
		final BufferedImage[] vFlash = new BufferedImage[this.cantidadSprite];

		for (int i = 0; i < this.cantidadSprite; i++) {
			vSprites[i] = Globales.FUNCIONES.TEXTURAS_TOOLS.voltearImagenH(this.sprites[i]);
			vFlash[i] = Globales.FUNCIONES.TEXTURAS_TOOLS.crearMascaraBlanca(vSprites[i]);
		}

		return new HojaSprite(vSprites, vFlash, this.anchoSprite, this.altoSprite);
	}

	/**
	 * Extrae un rango continuo de sprites y sus máscaras flash pre-horneadas
	 * compartiendo las referencias en VRAM sin recortes redundantes ni lecturas a
	 * disco.
	 * 
	 * @param indiceInicio Índice del primer sprite en la hoja.
	 * @param cantidad     Cantidad de fotogramas de la animación.
	 * @return Nueva HojaSprite lista para asignarse a una Animacion.
	 */
	public HojaSprite recortarRango(final int indiceInicio, final int cantidad) {
		final int cant = Math.max(1, cantidad);
		final BufferedImage[] subSprites = new BufferedImage[cant];
		final BufferedImage[] subFlash = new BufferedImage[cant];

		for (int i = 0; i < cant; i++) {
			final int idx = indiceInicio + i;
			if ((idx >= 0) && (idx < this.cantidadSprite)) {
				subSprites[i] = this.sprites[idx];
				subFlash[i] = this.spritesFlash[idx];
			} else {
				subSprites[i] = this.sprites[0];
				subFlash[i] = this.spritesFlash[0];
			}
		}

		return new HojaSprite(subSprites, subFlash, this.anchoSprite, this.altoSprite);
	}

	public BufferedImage getSprite(final int index) {
		if ((index < 0) || (index >= this.cantidadSprite)) {
			return this.sprites[0];
		}
		return this.sprites[index];
	}

	public BufferedImage getSpriteFlash(final int index) {
		if ((index < 0) || (index >= this.cantidadSprite)) {
			return this.spritesFlash[0];
		}
		return this.spritesFlash[index];
	}

	public int getCantidadSprite() {
		return this.cantidadSprite;
	}

	public int getAnchoSprite() {
		return this.anchoSprite;
	}

	public int getAltoSprite() {
		return this.altoSprite;
	}
}