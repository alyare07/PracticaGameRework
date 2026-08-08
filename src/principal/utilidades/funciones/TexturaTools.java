package principal.utilidades.funciones;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class TexturaTools {


    public BufferedImage crearTexturaError(int tamaño) {
        BufferedImage img = crearImagenVRAM(tamaño, tamaño, Transparency.OPAQUE);
        Graphics2D g = img.createGraphics();

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

    public  BufferedImage crearTextura(final Color c, final int ancho, final int alto) {
        BufferedImage img = crearImagenVRAM(ancho, alto, Transparency.TRANSLUCENT);
        Graphics2D g = img.createGraphics();
        g.setColor(c);
        g.fillRect(0, 0, ancho, alto);
        g.dispose();
        return img;
    }

    // --- MANEJO DE VRAM Y HERRAMIENTAS GRÁFICAS ---

    public  BufferedImage crearImagenVRAM(int ancho, int alto, int transparencia) {
        GraphicsConfiguration gc = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration();
        return gc.createCompatibleImage(ancho, alto, transparencia);
    }

    public  BufferedImage convertirAVRAM(BufferedImage img) {
        if (img == null) return null;
        BufferedImage compatible = crearImagenVRAM(img.getWidth(), img.getHeight(), img.getTransparency());
        Graphics2D g = compatible.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return compatible;
    }

    public  BufferedImage crearImagenRectanguloContornoEnVRAM(int lado, Color colorBorde) {
        BufferedImage img = crearImagenVRAM(lado, lado, Transparency.TRANSLUCENT);
        Graphics2D gImg = img.createGraphics();
        gImg.setColor(colorBorde);
        gImg.drawRect(0, 0, lado - 1, lado - 1);
        gImg.dispose();
        return img;
    }

    public  BufferedImage crearImagenRectanguloRellenoEnVRAM(final Color colorBorde, final int ancho, final int alto) {
        BufferedImage img = crearImagenVRAM(ancho, alto, Transparency.TRANSLUCENT);
        Graphics2D gImg = img.createGraphics();
        gImg.setColor(colorBorde);
        gImg.fillRect(0, 0, ancho - 1, alto - 1);
        gImg.dispose();
        return img;
    }

    public  BufferedImage redimensionar(final BufferedImage img, final int anchoNuevo, final int altoNuevo) {
        BufferedImage imgNueva = crearImagenVRAM(anchoNuevo, altoNuevo, img.getTransparency());
        Graphics2D g = imgNueva.createGraphics();
        g.drawImage(img, 0, 0, anchoNuevo, altoNuevo, null);
        g.dispose();
        return imgNueva;
    }

    public  BufferedImage voltearImagenH(final BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage newImage = crearImagenVRAM(w, h, image.getTransparency());
        Graphics2D g = newImage.createGraphics();
        g.drawImage(image, 0, 0, w, h, w, 0, 0, h, null);
        g.dispose();
        return newImage;
    }

    public  BufferedImage voltearImagenV(final BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage newImage = crearImagenVRAM(w, h, image.getTransparency());
        Graphics2D g = newImage.createGraphics();
        g.drawImage(image, 0, 0, w, h, 0, h, w, 0, null);
        g.dispose();
        return newImage;
    }

    public  BufferedImage voltearImagen90GradosIzquierda(final BufferedImage image) {
        BufferedImage b2 = crearImagenVRAM(image.getHeight(), image.getWidth(), image.getTransparency());
        Graphics2D g2d = b2.createGraphics();
        AffineTransform at = AffineTransform.getRotateInstance(Math.toRadians(270), image.getWidth() / 2.0, image.getWidth() / 2.0);
        g2d.drawImage(image, at, null);
        g2d.dispose();
        return b2;
    }

    public  BufferedImage voltearImagen90GradosDerecha(final BufferedImage image) {
        return voltearImagenV(voltearImagen90GradosIzquierda(image));
    }
}
