package principal.utilidades.audio.sonido;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import principal.utilidades.audio.Audio;

/**
 * Implementación de efectos de sonido cortos (SFX) basada en {@link Clip} de
 * Java Sound API.
 * <p>
 * Decodifica y almacena el contenido del archivo de audio íntegramente como un
 * búfer de bytes PCM en la memoria RAM. Esto permite una reproducción de
 * altísima velocidad ($0\text{ ms}$ de latencia) y habilita la técnica de
 * clonación en RAM para polifonía múltiple (ej. disparos en ráfaga simultáneos)
 * sin volver a leer el disco rígido.
 * </p>
 * 
 * @author TuNombre / Proyecto Java2D
 */
public class SonidoJavaSound implements Audio {

	// =========================================================================
	// ATRIBUTOS DE HARDWARE Y MEMORIA RAM
	// =========================================================================

	/**
	 * Clip de audio de la API de Java cargado directamente en la línea del
	 * mezclador de la JVM.
	 */
	private Clip clip;

	/** Control de ganancia del mezclador para ajustar decibelios (dB). */
	private FloatControl gainControl;

	/**
	 * Búfer de datos en memoria RAM con los bytes decodificados del sonido en PCM
	 * firmado a 16 bits.
	 */
	private byte[] datosAudioPCM;

	/**
	 * Metadatos de formato del audio PCM (frecuencia de muestreo, número de
	 * canales, etc.).
	 */
	private AudioFormat formatoPCM;

	// =========================================================================
	// CONFIGURACIÓN DE VOLUMEN
	// =========================================================================

	/** Volumen base por defecto configurado desde el archivo JSON. */
	private final double volumenPorDefecto;

	/** Volumen actual aplicado al clip (en escala lineal [0.0 - 1.0]). */
	private double volumenActual;

	// =========================================================================
	// CONSTRUCTORES
	// =========================================================================

	/**
	 * Constructor principal: Lee, decodifica y precarga el archivo de audio desde
	 * el disco hacia la memoria RAM.
	 * <p>
	 * Este constructor se ejecuta únicamente durante la fase de carga inicial del
	 * juego desde el {@link GestorSonido}.
	 * </p>
	 *
	 * @param ruta              Ubicación física del archivo (.wav / .mp3).
	 * @param volumenPorDefecto Volumen base asignado (se clampa entre 0.0 y 1.0).
	 */
	public SonidoJavaSound(final String ruta, final double volumenPorDefecto) {
		this.volumenPorDefecto = Math.max(0.0, Math.min(1.0, volumenPorDefecto));
		this.volumenActual = this.volumenPorDefecto;

		try {
			final File archivo = new File(ruta);
			if (archivo.exists()) {
				// 1. Obtener el flujo de audio original desde el archivo en disco
				final AudioInputStream aisOriginal = AudioSystem.getAudioInputStream(archivo);
				final AudioFormat formatoBase = aisOriginal.getFormat();

				// 2. Definir formato de destino universal PCM firmado a 16 bits
				this.formatoPCM = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, formatoBase.getSampleRate(), 16,
						formatoBase.getChannels(), formatoBase.getChannels() * 2, // Frame size en bytes (2 bytes por
																					// canal)
						formatoBase.getSampleRate(), false // Little-Endian
				);

				// 3. Crear el flujo de conversión decodificado
				final AudioInputStream aisDecodificado = AudioSystem.getAudioInputStream(this.formatoPCM, aisOriginal);

				// 4. Lectura manual en bloque (Compatible con versiones de Java anteriores como
				// Java 8)
				final ByteArrayOutputStream baos = new ByteArrayOutputStream();
				final byte[] buffer = new byte[4096];
				int bytesLeidos;

				while ((bytesLeidos = aisDecodificado.read(buffer)) != -1) {
					baos.write(buffer, 0, bytesLeidos);
				}

				// Almacenamos el arreglo completo de bytes PCM en la variable de instancia
				this.datosAudioPCM = baos.toByteArray();

				// Cierre de flujos de lectura de disco
				baos.close();
				aisDecodificado.close();
				aisOriginal.close();

				// 5. Inicializar la línea del reproductor de sonido
				this.inicializarClip();
			} else {
				System.err.println("⚠ SonidoJavaSound: No existe el archivo en '" + ruta + "'");
			}
		} catch (final Exception e) {
			System.err.println("⚠ SonidoJavaSound: Error al precargar el audio desde '" + ruta + "'");
			e.printStackTrace();
		}
	}

	/**
	 * Constructor privado para clonación rápida en memoria RAM.
	 * <p>
	 * Evita lecturas a disco reutilizando las referencias del búfer de bytes PCM y
	 * el formato.
	 * </p>
	 */
	private SonidoJavaSound(final byte[] datosAudioPCM, final AudioFormat formatoPCM, final double volumenPorDefecto) {
		this.datosAudioPCM = datosAudioPCM;
		this.formatoPCM = formatoPCM;
		this.volumenPorDefecto = volumenPorDefecto;
		this.volumenActual = volumenPorDefecto;

		this.inicializarClip();
	}

	// =========================================================================
	// INICIALIZACIÓN DE HARDWARE
	// =========================================================================

	/**
	 * Construye el objeto {@link Clip} de Java Sound reconvirtiendo los bytes en
	 * RAM a un flujo de lectura.
	 */
	private void inicializarClip() {
		if ((this.datosAudioPCM == null) || (this.formatoPCM == null)) {
			return;
		}

		try {
			final ByteArrayInputStream bais = new ByteArrayInputStream(this.datosAudioPCM);
			final AudioInputStream ais = new AudioInputStream(bais, this.formatoPCM,
					this.datosAudioPCM.length / this.formatoPCM.getFrameSize());

			this.clip = AudioSystem.getClip();
			this.clip.open(ais);

			if (this.clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
				this.gainControl = (FloatControl) this.clip.getControl(FloatControl.Type.MASTER_GAIN);
			}

			// Aplica el volumen inicial
			this.setVolumen(this.volumenActual);
		} catch (final Exception e) {
			System.err.println("⚠ SonidoJavaSound: Error al inicializar el Clip de audio.");
			e.printStackTrace();
		}
	}

	// =========================================================================
	// MÉTODOS DE REPRODUCCIÓN Y CONTROL (Interfaz Audio)
	// =========================================================================

	/**
	 * Reproduce el efecto de sonido inmediatamente.
	 * <p>
	 * Si el sonido se estaba ejecutando (ej: disparo repetido muy rápido), se
	 * rebobina al marco cero ($0\text{ ms}$) de forma instantánea sin superponer el
	 * mismo canal.
	 * </p>
	 */
	@Override
	public void reproducir() {
		if (this.clip == null) {
			return;
		}

		if (this.clip.isRunning()) {
			this.clip.stop();
		}
		this.clip.setFramePosition(0);
		this.clip.start();
	}

	/**
	 * Pausa la ejecución del Clip en la posición actual.
	 */
	@Override
	public void pausar() {
		if ((this.clip != null) && this.clip.isRunning()) {
			this.clip.stop();
		}
	}

	/**
	 * Detiene completamente la reproducción y reinicia la aguja de lectura al
	 * principio.
	 */
	@Override
	public void detener() {
		if (this.clip != null) {
			this.clip.stop();
			this.clip.setFramePosition(0);
		}
	}

	/**
	 * Configura si el efecto de sonido debe repetirse en un bucle infinito.
	 *
	 * @param repetir {@code true} para activar repetición continua.
	 */
	@Override
	public void repetir(final boolean repetir) {
		if (this.clip != null) {
			this.clip.loop(repetir ? Clip.LOOP_CONTINUOUSLY : 0);
		}
	}

	/**
	 * Evalúa dinámicamente si el efecto de sonido debe continuar o pausarse según
	 * el estado del juego.
	 *
	 * @param reproducir {@code true} para reproducir; {@code false} para pausar.
	 */
	@Override
	public void actualizar(final boolean reproducir) {
		if (reproducir) {
			this.reproducir();
		} else {
			this.pausar();
		}
	}

	// =========================================================================
	// GESTIÓN DE VOLUMEN
	// =========================================================================

	/**
	 * Restablece el volumen actual al valor por defecto configurado originalmente
	 * desde el JSON.
	 */
	public void resetVolumen() {
		this.setVolumen(this.volumenPorDefecto);
	}

	/**
	 * @return El volumen nominal cargado desde el archivo JSON.
	 */
	public double getVolumenPorDefecto() {
		return this.volumenPorDefecto;
	}

	/**
	 * Aplica un volumen en escala lineal [0.0 - 1.0] convirtiéndolo a decibelios
	 * ($dB$).
	 *
	 * @param volumen Porcentaje de volumen deseado.
	 */
	@Override
	public void setVolumen(final double volumen) {
		this.volumenActual = Math.max(0.0, Math.min(1.0, volumen));

		if ((this.clip == null) || (this.gainControl == null)) {
			return;
		}

		if (this.volumenActual == 0.0) {
			this.gainControl.setValue(this.gainControl.getMinimum());
		} else {
			// Conversión Matemática: Escala lineal -> Escala logarítmica de decibelios (dB)
			float dB = (float) (Math.log10(this.volumenActual) * 20.0);
			dB = Math.max(this.gainControl.getMinimum(), Math.min(this.gainControl.getMaximum(), dB));
			this.gainControl.setValue(dB);
		}
	}

	// =========================================================================
	// POLIFONÍA Y PATRÓN PROTOTIPO (CLONACIÓN)
	// =========================================================================

	/**
	 * Crea un duplicado ligero de este sonido compartiendo el búfer de datos en
	 * memoria RAM.
	 * <p>
	 * Permite polifonía pura: disparar varios proyectiles idénticos al mismo tiempo
	 * sin bloquear los canales ni reabrir archivos desde el almacenamiento masivo.
	 * </p>
	 *
	 * @return Una nueva instancia de {@link SonidoJavaSound} lista para ejecutarse.
	 */
	public SonidoJavaSound clonar() {
		return new SonidoJavaSound(this.datosAudioPCM, this.formatoPCM, this.volumenPorDefecto);
	}
}