package principal.utilidades.audio.musica;

import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

import principal.utilidades.Globales;
import principal.utilidades.audio.Audio;

/**
 * Gestor de reproducción de audio por flujo de datos (Streaming).
 * <p>
 * Diseñado para la reproducción de música de fondo y archivos de audio extensos
 * (MP3 / WAV) sin cargar la totalidad del archivo descomprimido en la memoria
 * RAM ni congelar el hilo principal de renderizado (Game Loop). La lectura se
 * realiza en búferes pequeños dentro de un hilo secundario.
 * </p>
 * 
 * @author TuNombre / Proyecto Java2D
 */
public class MusicaStream implements Audio, Runnable {

	// =========================================================================
	// ATRIBUTOS DE CONFIGURACIÓN Y ESTADO
	// =========================================================================

	/** Ruta relativa o absoluta del archivo de audio en disco. */
	private final String ruta;

	/** Volumen base configurado inicialmente (rango 0.0 a 1.0). */
	private final double volumenPorDefecto;

	/** Volumen activo aplicado actualmente al canal de audio (rango 0.0 a 1.0). */
	private double volumenActual;

	// =========================================================================
	// RECURSOS DE AUDIO Y MULTIHILO
	// =========================================================================

	/**
	 * Canal de salida de audio que transmite los datos procesados a la tarjeta de
	 * sonido.
	 */
	private SourceDataLine line;

	/**
	 * Control de ganancia del mezclador de la JVM para manipular decibelios (dB).
	 */
	private FloatControl gainControl;

	/**
	 * Hilo de ejecución secundario en el cual se decodifica y transmite el audio.
	 */
	private Thread hiloAudio;

	/**
	 * Flag de control multihilo: indica si el stream está corriendo activamente.
	 */
	private volatile boolean reproduciendo = false;

	/**
	 * Flag de control multihilo: indica si la reproducción está temporalmente
	 * pausada.
	 */
	private volatile boolean pausado = false;

	/**
	 * Determina si la música debe reiniciarse automáticamente al finalizar el
	 * archivo.
	 */
	private boolean enBucle = false;

	// =========================================================================
	// CONSTRUCTOR
	// =========================================================================

	/**
	 * Crea un nuevo reproductor en Stream para archivos de audio pesados.
	 *
	 * @param ruta              Ubicación del archivo de audio (.mp3 o .wav).
	 * @param volumenPorDefecto Volumen base asignado (se clampa automáticamente
	 *                          entre 0.0 y 1.0).
	 */
	public MusicaStream(final String ruta, final double volumenPorDefecto) {
		this.ruta = ruta;
		this.volumenPorDefecto = Math.max(0.0, Math.min(1.0, volumenPorDefecto));
		this.volumenActual = this.volumenPorDefecto;
	}

	// =========================================================================
	// CONTROL DE REPRODUCCIÓN (Interfaz Audio)
	// =========================================================================

	/**
	 * Inicia o reanuda la reproducción en un nuevo hilo secundario.
	 */
	@Override
	public void reproducir() {
		if (this.reproduciendo) {
			this.pausado = false;
			return;
		}

		this.reproduciendo = true;
		this.pausado = false;

		// Creamos el hilo y lo marcamos como Daemon para que no bloquee el cierre del
		// programa
		this.hiloAudio = new Thread(this, "HiloMúsicaStream: " + this.ruta);
		this.hiloAudio.setDaemon(true);
		this.hiloAudio.start();
	}

	/**
	 * Bucle principal de procesamiento de audio en segundo plano (Ejecutado por el
	 * HiloSecundario).
	 * <p>
	 * Lee bloques de 4KB desde el archivo de audio, los convierte a PCM lineal de
	 * 16 bits y los envía línea por línea a la tarjeta de sonido.
	 * </p>
	 */
	@Override
	public void run() {
		do {
			try {
				final File archivo = new File(this.ruta);
				if (!archivo.exists()) {
					System.err.println("⚠ MusicaStream: El archivo no existe en '" + this.ruta + "'");
					break;
				}

				// 1. Obtener el flujo de audio original del archivo (.mp3 o .wav)
				final AudioInputStream aisOriginal = AudioSystem.getAudioInputStream(archivo);
				final AudioFormat formatoBase = aisOriginal.getFormat();

				// 2. Construir el formato de decodificación universal: PCM firmado a 16-bit
				final AudioFormat formatoDecodificado = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
						formatoBase.getSampleRate(), 16, formatoBase.getChannels(), formatoBase.getChannels() * 2, // Frame
																													// size
																													// en
																													// bytes
																													// (2
																													// bytes
																													// por
																													// canal)
						formatoBase.getSampleRate(), false // Little-Endian (estándar en sistemas x86/x64)
				);

				// 3. Crear el flujo decodificado y vincularlo con la línea de salida del
				// hardware
				final AudioInputStream aisDecodificado = AudioSystem.getAudioInputStream(formatoDecodificado,
						aisOriginal);
				final DataLine.Info info = new DataLine.Info(SourceDataLine.class, formatoDecodificado);

				this.line = (SourceDataLine) AudioSystem.getLine(info);
				this.line.open(formatoDecodificado);

				// 4. Obtener y configurar el control de volumen maestro de la línea
				if (this.line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
					this.gainControl = (FloatControl) this.line.getControl(FloatControl.Type.MASTER_GAIN);
					this.setVolumen(this.volumenActual);
				}

				this.line.start();

				// 5. Lectura dinámica por bloques (Búfer de 4 KB = Cero impacto en RAM)
				final byte[] buffer = new byte[4096];
				int bytesLeidos = 0;

				while ((bytesLeidos != -1) && this.reproduciendo) {
					// Si el juego se pausa, dormimos el hilo para no consumir CPU inútilmente
					if (this.pausado) {
						Thread.sleep(50);
						continue;
					}

					bytesLeidos = aisDecodificado.read(buffer, 0, buffer.length);
					if (bytesLeidos >= 0) {
						// Escribe en la tarjeta de audio (Operación bloqueante natural del búfer de
						// audio)
						this.line.write(buffer, 0, bytesLeidos);
					}
				}

				// 6. Limpieza de recursos al finalizar o detener la canción actual
				this.line.drain();
				this.line.close();
				aisDecodificado.close();
				aisOriginal.close();

			} catch (final Exception e) {
				System.err.println("⚠ Error durante la reproducción en stream: " + this.ruta);
				e.printStackTrace();
				break;
			}
		} while (this.enBucle && this.reproduciendo); // Bucle para reiniciar la pista si 'enBucle' es true

		this.reproduciendo = false;
	}

	/**
	 * Pausa temporalmente la lectura del audio sin destruir el hilo de
	 * reproducción.
	 */
	@Override
	public void pausar() {
		this.pausado = true;
	}

	/**
	 * Detiene por completo la transmisión y cierra la línea de salida del hardware.
	 */
	@Override
	public void detener() {
		this.reproduciendo = false;
		this.pausado = false;
		if (this.line != null) {
			this.line.stop();
			this.line.close();
		}
	}

	/**
	 * Configura si la pista debe reproducirse indefinidamente en bucle.
	 *
	 * @param repetir {@code true} para activar el bucle continuo.
	 */
	@Override
	public void repetir(final boolean repetir) {
		this.enBucle = repetir;
	}

	/**
	 * Evalúa dinámicamente si el audio debe estar sonando o pausado según el estado
	 * del juego.
	 *
	 * @param reproducir {@code true} si el juego se está actualizando;
	 *                   {@code false} si está en pausa.
	 */
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

	// =========================================================================
	// GESTIÓN DE VOLUMEN Y ATENUACIÓN POSICIONAL 2D
	// =========================================================================

	/**
	 * Aplica una escala de volumen lineal [0.0 a 1.0] convirtiéndola a la escala
	 * logarítmica de decibelios (dB).
	 *
	 * @param volumen Nivel de audio en porcentaje lineal (0.0 = silencio, 1.0 =
	 *                volumen máximo).
	 */
	@Override
	public void setVolumen(final double volumen) {
		this.volumenActual = Math.max(0.0, Math.min(1.0, volumen));

		if (this.gainControl == null) {
			return;
		}

		if (this.volumenActual == 0.0) {
			// Silencio absoluto
			this.gainControl.setValue(this.gainControl.getMinimum());
		} else {
			// Conversión Matemática: Escala lineal -> Escala logarítmica de decibelios (dB)
			float dB = (float) (Math.log10(this.volumenActual) * 20.0);
			dB = Math.max(this.gainControl.getMinimum(), Math.min(this.gainControl.getMaximum(), dB));
			this.gainControl.setValue(dB);
		}
	}

	/**
	 * Modula el volumen en tiempo real según la distancia 2D entre una fuente
	 * emisora y el receptor.
	 *
	 * @param xEmisor     Coordenada X del objeto emisor de sonido (Radio, Vehículo,
	 *                    etc.).
	 * @param yEmisor     Coordenada Y del objeto emisor de sonido.
	 * @param xReceptor   Coordenada X del oyente (Jugador / Cámara).
	 * @param yReceptor   Coordenada Y del oyente.
	 * @param radioMaximo Distancia máxima a partir de la cual el volumen llega a
	 *                    cero.
	 */
	public void setVolumen(final double xEmisor, final double yEmisor, final double xReceptor, final double yReceptor,
			final double radioMaximo) {
		final double distancia = Math.hypot(xEmisor - xReceptor, yEmisor - yReceptor);

		if (distancia >= radioMaximo) {
			this.setVolumen(0.0);
			return;
		}

		// Factor de atenuación lineal (1.0 = encima de la fuente, 0.0 = al límite del
		// radio)
		final double factorDistancia = Math.max(0.0, 1.0 - (distancia / radioMaximo));
		final double volumenFinal = this.volumenPorDefecto * factorDistancia;

		this.setVolumen(volumenFinal);
	}

	/**
	 * Sobrecarga de atenuación posicional 2D utilizando el radio estándar del
	 * sistema.
	 *
	 * @param xEmisor   Coordenada X del emisor.
	 * @param yEmisor   Coordenada Y del emisor.
	 * @param xReceptor Coordenada X del oyente.
	 * @param yReceptor Coordenada Y del oyente.
	 */
	public void setVolumen(final double xEmisor, final double yEmisor, final double xReceptor, final double yReceptor) {
		this.setVolumen(xEmisor, yEmisor, xReceptor, yReceptor,
				Globales.CONSTANTES.RADIO_AUDIO_DISTANCIA_MAXIMA);
	}
}