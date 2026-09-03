package principal.utilidades.audio.musica;

import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

import principal.utilidades.Constantes;
import principal.utilidades.audio.Audio;

/**
 * Gestor de reproducción de audio por flujo de datos (Streaming). Decodifica
 * bloques de 4KB en un hilo Daemon seguro con control estricto de recursos de
 * hardware.
 * 
 * @version 2.0 (Vanilla Java 8 - Thread-Safe Lifecycle)
 */
public class MusicaStream implements Audio, Runnable {

	private final String ruta;
	private final double volumenPorDefecto;
	private double volumenActual;

	private SourceDataLine line;
	private FloatControl gainControl;
	private Thread hiloAudio;

	private volatile boolean reproduciendo = false;
	private volatile boolean pausado = false;
	private boolean enBucle = false;

	public MusicaStream(final String ruta, final double volumenPorDefecto) {
		this.ruta = ruta;
		this.volumenPorDefecto = Math.max(0.0, Math.min(1.0, volumenPorDefecto));
		this.volumenActual = this.volumenPorDefecto;
	}

	@Override
	public synchronized void reproducir() {
		if (this.reproduciendo) {
			this.pausado = false;
			return;
		}

		this.reproduciendo = true;
		this.pausado = false;

		this.hiloAudio = new Thread(this, "HiloMusicaStream-" + this.ruta);
		this.hiloAudio.setDaemon(true);
		this.hiloAudio.start();
	}

	@Override
	public void run() {
		do {
			AudioInputStream aisOriginal = null;
			AudioInputStream aisDecodificado = null;

			try {
				final File archivo = new File(this.ruta);
				if (!archivo.exists()) {
					System.err.println("⚠ MusicaStream: El archivo no existe en '" + this.ruta + "'");
					break;
				}

				aisOriginal = AudioSystem.getAudioInputStream(archivo);
				final AudioFormat formatoBase = aisOriginal.getFormat();

				final AudioFormat formatoDecodificado = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
						formatoBase.getSampleRate(), 16, formatoBase.getChannels(), formatoBase.getChannels() * 2,
						formatoBase.getSampleRate(), false);

				aisDecodificado = AudioSystem.getAudioInputStream(formatoDecodificado, aisOriginal);
				final DataLine.Info info = new DataLine.Info(SourceDataLine.class, formatoDecodificado);

				synchronized (this) {
					if (!this.reproduciendo) {
						break;
					}
					this.line = (SourceDataLine) AudioSystem.getLine(info);
					this.line.open(formatoDecodificado);

					if (this.line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
						this.gainControl = (FloatControl) this.line.getControl(FloatControl.Type.MASTER_GAIN);
						this.setVolumen(this.volumenActual);
					}
					this.line.start();
				}

				final byte[] buffer = new byte[4096];
				int bytesLeidos = 0;

				while (this.reproduciendo && (bytesLeidos != -1)) {
					if (this.pausado) {
						try {
							Thread.sleep(40);
						} catch (final InterruptedException e) {
							Thread.currentThread().interrupt();
							break;
						}
						continue;
					}

					bytesLeidos = aisDecodificado.read(buffer, 0, buffer.length);
					if ((bytesLeidos > 0) && (this.line != null) && this.line.isOpen()) {
						this.line.write(buffer, 0, bytesLeidos);
					}
				}

			} catch (final Exception e) {
				System.err.println("⚠ Error durante streaming de audio: " + this.ruta + " -> " + e.getMessage());
				break;
			} finally {
				this.cerrarHardwareYStreams(aisOriginal, aisDecodificado);
			}

		} while (this.enBucle && this.reproduciendo);

		this.reproduciendo = false;
	}

	private synchronized void cerrarHardwareYStreams(final AudioInputStream aisOrig, final AudioInputStream aisDec) {
		try {
			if (this.line != null) {
				if (this.line.isOpen()) {
					this.line.drain();
					this.line.stop();
					this.line.close();
				}
				this.line = null;
				this.gainControl = null;
			}
			if (aisDec != null) {
				aisDec.close();
			}
			if (aisOrig != null) {
				aisOrig.close();
			}
		} catch (final Exception ignored) {
		}
	}

	@Override
	public void pausar() {
		this.pausado = true;
	}

	@Override
	public synchronized void detener() {
		this.reproduciendo = false;
		this.pausado = false;
		if (this.line != null) {
			try {
				if (this.line.isOpen()) {
					this.line.stop();
					this.line.flush();
					this.line.close();
				}
			} catch (final Exception ignored) {
			}
			this.line = null;
			this.gainControl = null;
		}
	}

	@Override
	public void repetir(final boolean repetir) {
		this.enBucle = repetir;
	}

	@Override
	public void actualizar(final boolean reproducir) {
		if (reproducir) {
			if (!this.reproduciendo) {
				this.reproducir();
			} else {
				this.pausado = false;
			}
		} else {
			this.pausar();
		}
	}

	@Override
	public synchronized void setVolumen(final double volumen) {
		this.volumenActual = Math.max(0.0, Math.min(1.0, volumen));

		if (this.gainControl == null) {
			return;
		}

		if (this.volumenActual == 0.0) {
			this.gainControl.setValue(this.gainControl.getMinimum());
		} else {
			float dB = (float) (Math.log10(this.volumenActual) * 20.0);
			dB = Math.max(this.gainControl.getMinimum(), Math.min(this.gainControl.getMaximum(), dB));
			this.gainControl.setValue(dB);
		}
	}

	public void setVolumen(final double xEmisor, final double yEmisor, final double xReceptor, final double yReceptor,
			final double radioMaximo) {
		final double dx = xEmisor - xReceptor;
		final double dy = yEmisor - yReceptor;
		final double distancia = Math.sqrt((dx * dx) + (dy * dy));

		if (distancia >= radioMaximo) {
			this.setVolumen(0.0);
			return;
		}

		final double factorDistancia = Math.max(0.0, 1.0 - (distancia / radioMaximo));
		final double volumenFinal = this.volumenPorDefecto * factorDistancia;

		this.setVolumen(volumenFinal);
	}

	public void setVolumen(final double xEmisor, final double yEmisor, final double xReceptor, final double yReceptor) {
		this.setVolumen(xEmisor, yEmisor, xReceptor, yReceptor, Constantes.RADIO_AUDIO_DISTANCIA_MAXIMA);
	}
}