package principal.utilidades.funciones;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Utilidades para manipulación, escalado, volteo y generación de máscaras de
 * texturas en VRAM.
 * 
 * @author Copiloto Técnico
 * @version 2.5
 */
public class TexturaTools {

	public BufferedImage crearTexturaError(final int tamaño) {
		final BufferedImage img = this.crearImagenVRAM(tamaño, tamaño, Transparency.OPAQUE);
		final Graphics2D g = img.createGraphics();

		// Fondo magenta
		g.setColor(Color.MAGENTA);
		g.fillRect(0, 0, tamaño, tamaño);

		// Cuadros negros opuestos
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, tamaño / 2, tamaño / 2);
		g.fillRect(tamaño / 2, tamaño / 2, tamaño / 2, tamaño / 2);

		g.dispose();
		return img;
	}

	public BufferedImage crearTextura(final Color c, final int ancho, final int alto) {
		final BufferedImage img = this.crearImagenVRAM(ancho, alto, Transparency.TRANSLUCENT);
		final Graphics2D g = img.createGraphics();
		g.setColor(c);
		g.fillRect(0, 0, ancho, alto);
		g.dispose();
		return img;
	}

	// =========================================================================
	// === GENERACIÓN DE MÁSCARAS HIT-FLASH (PRE-HORNEADO EN ARRANQUE)
	// =========================================================================

	/*
	 * =========================================================================
	 * EXPLICACIÓN DIDÁCTICA: ¿CÓMO SE CREA UNA SILUETA BLANCA A NIVEL DE BITS?
	 * ------------------------------------------------------------------------- Un
	 * píxel en memoria ARGB está compuesto por 4 bytes: [Alpha, Red, Green, Blue].
	 * 
	 * 1. EXTRAER EL CANAL ALPHA: - '(pixel >> 24) & 0xFF' nos da la transparencia
	 * del píxel (0 = invisible, 255 = opaco).
	 * 
	 * 2. MANTENER LA FORMA DEL SPRITE: - Si alpha == 0: El píxel es transparente y
	 * se deja en 0 (vacío). - Si alpha > 0: Es parte del cuerpo del personaje.
	 * Conservamos su transparencia original desplaza a la izquierda '(alpha << 24)'
	 * y reemplazamos los colores RGB por blanco puro '| 0x00FFFFFF'.
	 * 
	 * Resultado: Una silueta blanca perfecta que calza al 100% sobre el sprite sin
	 * bordes negros ni deformaciones.
	 * =========================================================================
	 */
	/**
	 * Genera una versión monocromática blanca pura del sprite para el efecto
	 * Hit-Flash, preservando intacto el canal de transparencia Alpha original.
	 *
	 * @param original Imagen original a procesar.
	 * @return Nueva {@link BufferedImage} en VRAM con la máscara blanca
	 *         pre-calculada.
	 */
	public BufferedImage crearMascaraBlanca(final BufferedImage original) {
		if (original == null) {
			return null;
		}

		final int w = original.getWidth();
		final int h = original.getHeight();
		final BufferedImage mascara = this.crearImagenVRAM(w, h, Transparency.TRANSLUCENT);

		final int[] pixeles = new int[w * h];
		original.getRGB(0, 0, w, h, pixeles, 0, w);

		for (int i = 0; i < pixeles.length; i++) {
			final int alpha = (pixeles[i] >> 24) & 0xFF;
			if (alpha > 0) {
				// Mantiene el alpha original y fuerza RGB a blanco absoluto (0xFFFFFF)
				pixeles[i] = (alpha << 24) | 0x00FFFFFF;
			} else {
				pixeles[i] = 0;
			}
		}

		mascara.setRGB(0, 0, w, h, pixeles, 0, w);
		return mascara;
	}

	// =========================================================================
	// === MANEJO DE VRAM Y HERRAMIENTAS GRÁFICAS
	// =========================================================================

	public BufferedImage crearImagenVRAM(final int ancho, final int alto, final int transparencia) {
		final GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDefaultConfiguration();
		return gc.createCompatibleImage(ancho, alto, transparencia);
	}

	public BufferedImage convertirAVRAM(final BufferedImage img) {
		if (img == null) {
			return null;
		}
		final BufferedImage compatible = this.crearImagenVRAM(img.getWidth(), img.getHeight(), img.getTransparency());
		final Graphics2D g = compatible.createGraphics();
		g.drawImage(img, 0, 0, null);
		g.dispose();
		return compatible;
	}

	public BufferedImage crearImagenRectanguloContornoEnVRAM(final int lado, final Color colorBorde) {
		final BufferedImage img = this.crearImagenVRAM(lado, lado, Transparency.TRANSLUCENT);
		final Graphics2D gImg = img.createGraphics();
		gImg.setColor(colorBorde);
		gImg.drawRect(0, 0, lado - 1, lado - 1);
		gImg.dispose();
		return img;
	}

	public BufferedImage crearImagenRectanguloRellenoEnVRAM(final Color colorBorde, final int ancho, final int alto) {
		final BufferedImage img = this.crearImagenVRAM(ancho, alto, Transparency.TRANSLUCENT);
		final Graphics2D gImg = img.createGraphics();
		gImg.setColor(colorBorde);
		gImg.fillRect(0, 0, ancho - 1, alto - 1);
		gImg.dispose();
		return img;
	}

	public BufferedImage redimensionar(final BufferedImage img, final int anchoNuevo, final int altoNuevo) {
		final BufferedImage imgNueva = this.crearImagenVRAM(anchoNuevo, altoNuevo, img.getTransparency());
		final Graphics2D g = imgNueva.createGraphics();
		g.drawImage(img, 0, 0, anchoNuevo, altoNuevo, null);
		g.dispose();
		return imgNueva;
	}

	public BufferedImage voltearImagenH(final BufferedImage image) {
		final int w = image.getWidth();
		final int h = image.getHeight();
		final BufferedImage newImage = this.crearImagenVRAM(w, h, image.getTransparency());
		final Graphics2D g = newImage.createGraphics();
		g.drawImage(image, 0, 0, w, h, w, 0, 0, h, null);
		g.dispose();
		return newImage;
	}

	public BufferedImage voltearImagenV(final BufferedImage image) {
		final int w = image.getWidth();
		final int h = image.getHeight();
		final BufferedImage newImage = this.crearImagenVRAM(w, h, image.getTransparency());
		final Graphics2D g = newImage.createGraphics();
		g.drawImage(image, 0, 0, w, h, 0, h, w, 0, null);
		g.dispose();
		return newImage;
	}

	public BufferedImage voltearImagen90GradosIzquierda(final BufferedImage image) {
		final BufferedImage b2 = this.crearImagenVRAM(image.getHeight(), image.getWidth(), image.getTransparency());
		final Graphics2D g2d = b2.createGraphics();
		final AffineTransform at = AffineTransform.getRotateInstance(Math.toRadians(270), image.getWidth() / 2.0,
				image.getWidth() / 2.0);
		g2d.drawImage(image, at, null);
		g2d.dispose();
		return b2;
	}

	public BufferedImage voltearImagen90GradosDerecha(final BufferedImage image) {
		return this.voltearImagenV(this.voltearImagen90GradosIzquierda(image));
	}
}