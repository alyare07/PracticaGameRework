package principal.comandos;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Administrador maestro de comandos por consola.
 * <p>
 * Escucha la entrada estándar (System.in) en un hilo Daemon y despacha
 * las acciones de forma segura en el ciclo lógico de 60 APS.
 * </p>
 * 
 * @version 1.0
 */
public class GestorComandos {

	/** Cola concurrente no bloqueante (Lock-Free) para transferir cadenas entre hilos. */
	private final ConcurrentLinkedQueue<String> colaComandosEntrantes;

	/** Diccionario de comandos registrados indexados por su nombre en minúsculas. */
	private final Map<String, Comando> comandosRegistrados;

	/** Hilo de segundo plano para la lectura de la consola. */
	private Thread hiloConsola;
	private volatile boolean escuchando;

	public GestorComandos() {
		this.colaComandosEntrantes = new ConcurrentLinkedQueue<String>();
		this.comandosRegistrados = new HashMap<String, Comando>();
		this.escuchando = false;

		// Registramos el comando de ayuda por defecto
		this.registrarComando(new Comando("ayuda", "ayuda", "Muestra la lista de todos los comandos disponibles.") {
			@Override
			public void ejecutar(final String[] args) {
				System.out.println("=================== COMANDOS DISPONIBLES ===================");
				for (final Comando cmd : GestorComandos.this.comandosRegistrados.values()) {
					System.out.println("> " + cmd.getSintaxis() + " -> " + cmd.getDescripcion());
				}
				System.out.println("============================================================");
			}
		});
	}

	/**
	 * Inicia el hilo en segundo plano como DAEMON para escuchar System.in.
	 * (Al ser Daemon, la JVM se cerrará limpiamente cuando cierres la ventana).
	 */
	public void iniciarEscuchaConsola() {
		if (this.escuchando) {
			return;
		}
		this.escuchando = true;

		this.hiloConsola = new Thread(new Runnable() {
			@Override
			public void run() {
				final Scanner scanner = new Scanner(System.in);
				System.out.println("[Consola] Consola de desarrollador activa. Escribe 'ayuda' para ver opciones.");

				while (GestorComandos.this.escuchando) {
					if (scanner.hasNextLine()) {
						final String linea = scanner.nextLine();
						if ((linea != null) && !linea.trim().isEmpty()) {
							GestorComandos.this.colaComandosEntrantes.offer(linea.trim());
						}
					}
				}
				scanner.close();
			}
		}, "Hilo-Consola-Dev");

		this.hiloConsola.setDaemon(true); // ¡CRUCIAL para que no impida el cierre del juego!
		this.hiloConsola.start();
	}

	/**
	 * Registra un comando en el catálogo maestro.
	 */
	public void registrarComando(final Comando comando) {
		if (comando != null) {
			this.comandosRegistrados.put(comando.getNombre().toLowerCase(), comando);
		}
	}

	/**
	 * Procesa y vacía la cola de comandos en el hilo principal (ejecutado a 60 APS).
	 */
	public void actualizar() {
		// Zero-GC: Si no hay comandos en cola, poll() retorna null inmediatamente sin crear objetos.
		while (!this.colaComandosEntrantes.isEmpty()) {
			final String linea = this.colaComandosEntrantes.poll();
			if (linea != null) {
				this.procesarCadena(linea);
			}
		}
	}

	private void procesarCadena(final String linea) {
		// Separamos por espacios múltiples
		final String[] tokens = linea.split("\\s+");
		if (tokens.length == 0) {
			return;
		}

		final String nombreComando = tokens[0].toLowerCase();

		// Extraemos los argumentos
		final String[] args = new String[tokens.length - 1];
		System.arraycopy(tokens, 1, args, 0, args.length);

		final Comando comando = this.comandosRegistrados.get(nombreComando);
		if (comando != null) {
			try {
				comando.ejecutar(args);
			} catch (final Exception e) {
				System.err.println("[Consola] Error inesperado ejecutando '" + nombreComando + "': " + e.getMessage());
				e.printStackTrace();
			}
		} else {
			System.err.println("[Consola] Comando desconocido: '" + nombreComando + "'. Escribe 'ayuda' para ver comandos.");
		}
	}
}