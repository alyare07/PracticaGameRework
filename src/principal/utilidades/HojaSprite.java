package principal.utilidades;

import java.awt.image.BufferedImage;

/**
 * Administra y recorta una cuadrícula de sprites individuales desde una textura
 * maestra, pre-horneando en VRAM las máscaras blancas para el efecto Hit-Flash
 * (Zero-GC).
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public class HojaSprite {

	protected final BufferedImage[] sprites;

	/** Arreglo paralelo con las siluetas blancas pre-horneadas para impacto. */
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

		final int columnas = imagen.getWidth() / ancho;
		final int filas = imagen.getHeight() / alto;
		this.cantidadSprite = columnas * filas;

		this.sprites = new BufferedImage[this.cantidadSprite];
		this.spritesFlash = new BufferedImage[this.cantidadSprite];

		int index = 0;
		for (int f = 0; f < filas; f++) {
			for (int c = 0; c < columnas; c++) {
				final BufferedImage recorte = imagen.getSubimage(c * ancho, f * alto, ancho, alto);
				this.sprites[index] = Globales.FUNCIONES.TEXTURAS_TOOLS.convertirAVRAM(recorte);

				// Pre-horneamos la máscara blanca del frame en memoria estática
				this.spritesFlash[index] = Globales.FUNCIONES.TEXTURAS_TOOLS.crearMascaraBlanca(this.sprites[index]);
				index++;
			}
		}
	}

	public HojaSprite(final String ruta, final int lado, final boolean opaca) {
		this(Globales.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida(ruta), lado, opaca);
	}

	public BufferedImage getSprite(final int index) {
		if ((index < 0) || (index >= this.cantidadSprite)) {
			return this.sprites[0];
		}
		return this.sprites[index];
	}

	/**
	 * Retorna la silueta blanca pre-horneada correspondiente al fotograma
	 * solicitado.
	 *
	 * @param index Índice del sprite en la animación.
	 * @return {@link BufferedImage} blanca acelerada en VRAM.
	 */
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