package principal.utilidades.funciones;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

public class CargadorRecursos {

	protected CargadorRecursos() {

	}

	public BufferedImage cargarImagenCompatibleOpaca(final String ruta) {
		Image imagen = null;
		try {
			imagen = ImageIO.read(CargadorRecursos.class.getResource(ruta));
		} catch (final IOException e) {
			e.printStackTrace();
		}

		final GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDefaultConfiguration();
		final BufferedImage imagenAcelerada = gc.createCompatibleImage(imagen.getWidth(null), imagen.getHeight(null),
				Transparency.OPAQUE);
		final Graphics2D g = (Graphics2D) imagenAcelerada.getGraphics();
		g.drawImage(imagen, 0, 0, null);
		g.dispose();
		return imagenAcelerada;
	}

	public BufferedImage cargarImagenCompatibleTranslucida(final String ruta) {
		Image imagen = null;
		try {
			imagen = ImageIO.read(CargadorRecursos.class.getResource(ruta));
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final ExceptionInInitializerError e) {
			System.err.println("ERROR AL CARGAR LA IMAGEN: " + ruta);
			// Obtener la causa real del problema
			System.err.println("Causa real: " + e.getCause());
		} catch (final Exception e) {
			System.err.println("ERROR AL CARGAR LA IMAGEN: " + ruta);
			// Obtener la causa real del problema
			System.err.println("Causa real: " + e.getCause());
		}
		final GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDefaultConfiguration();
		final BufferedImage imagenAcelerada = gc.createCompatibleImage(imagen.getWidth(null), imagen.getHeight(null),
				Transparency.TRANSLUCENT);
		final Graphics2D g = (Graphics2D) imagenAcelerada.getGraphics();
		g.drawImage(imagen, 0, 0, null);
		g.dispose();
		return imagenAcelerada;
	}

	public String leerArchivoTexto(final String ruta) {
		String contenido = "";
		final InputStream entradaBytes = CargadorRecursos.class.getResourceAsStream(ruta);
		final BufferedReader lector = new BufferedReader(new InputStreamReader(entradaBytes));
		String linea;
		try {
			while ((linea = lector.readLine()) != null) {
				contenido += linea;
			}
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (entradaBytes != null) {
					entradaBytes.close();
				}
				if (lector != null) {
					lector.close();
				}
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
		}
		return contenido;
	}

	public Font cargarFuente(final String ruta) {
		Font fuente = null;
		final InputStream entradaBytes = CargadorRecursos.class.getResourceAsStream(ruta);
		try {
			fuente = Font.createFont(Font.TRUETYPE_FONT, entradaBytes);
		} catch (final FontFormatException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}

		fuente = fuente.deriveFont(12f);
		return fuente;
	}

	public Clip cargarSonido(final String ruta) {
		Clip clip = null;
		try {
			final InputStream is = CargadorRecursos.class.getResourceAsStream(ruta);
			final AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
			final DataLine.Info info = new DataLine.Info(Clip.class, ais.getFormat());
			clip = (Clip) AudioSystem.getLine(info);
			clip.open(ais);
		} catch (final Exception e) {
			e.printStackTrace();
		}

		return clip;
	}
}
