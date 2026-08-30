package principal.comandos;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Administrador maestro de comandos por consola local y terminal remota TCP.
 * <p>
 * Escucha la entrada de {@link System#in} y sockets de red en hilos Daemon
 * concurrentes, y despacha las acciones de forma sincronizada en el ciclo de 60
 * APS.
 * </p>
 */
public class GestorComandos {

	/** Puerto por defecto para la terminal remota. */
	public static final int PUERTO_TERMINAL_DEFAULT = 4444;

	/**
	 * Cola concurrente no bloqueante (Lock-Free) para transferir peticiones entre
	 * hilos.
	 */
	private final ConcurrentLinkedQueue<PeticionComando> colaComandosEntrantes;

	/**
	 * Diccionario de comandos registrados indexados por su nombre en minúsculas.
	 */
	private final Map<String, Comando> comandosRegistrados;

	/** Servidor de socket TCP para terminales remotas. */
	private final ServidorTerminalRemota servidorRemoto;

	/** Emisor por defecto para la consola estándar del IDE. */
	private final EmisorRespuesta emisorConsolaLocal;

	/** Hilo de segundo plano para la lectura de System.in. */
	private Thread hiloConsolaLocal;
	private volatile boolean escuchandoConsolaLocal;

	public GestorComandos() {
		this.colaComandosEntrantes = new ConcurrentLinkedQueue<PeticionComando>();
		this.comandosRegistrados = new HashMap<String, Comando>();
		this.escuchandoConsolaLocal = false;
		this.servidorRemoto = new ServidorTerminalRemota(PUERTO_TERMINAL_DEFAULT, this);

		// Emisor que imprime en System.out / System.err
		this.emisorConsolaLocal = new EmisorRespuesta() {
			@Override
			public void enviarMensaje(final String mensaje) {
				System.out.println(mensaje);
			}

			@Override
			public void enviarError(final String error) {
				System.err.println(error);
			}
		};

		// Comando de ayuda que responde al emisor que lo llamó
		this.registrarComando(new Comando("ayuda", "ayuda", "Muestra la lista de todos los comandos disponibles.") {
			@Override
			public void ejecutar(final String[] args) {
				this.ejecutar(args, GestorComandos.this.emisorConsolaLocal);
			}

			@Override
			public void ejecutar(final String[] args, final EmisorRespuesta emisor) {
				final StringBuilder sb = new StringBuilder();
				sb.append("\n=================== COMANDOS DISPONIBLES ===================\n");
				for (final Comando cmd : GestorComandos.this.comandosRegistrados.values()) {
					sb.append("> ").append(cmd.getSintaxis()).append(" -> ").append(cmd.getDescripcion()).append("\n");
				}
				sb.append("============================================================");
				if (emisor != null) {
					emisor.enviarMensaje(sb.toString());
				} else {
					System.out.println(sb.toString());
				}
			}
		});
	}

	/**
	 * Inicia la escucha tanto de la consola local (Scanner) como de la terminal
	 * remota (Socket TCP).
	 */
	public void iniciarServicios() {
		this.iniciarEscuchaConsola();
		this.servidorRemoto.iniciar();
	}

	/**
	 * Inicia el hilo en segundo plano como DAEMON para escuchar System.in.
	 */
	public void iniciarEscuchaConsola() {
		if (this.escuchandoConsolaLocal) {
			return;
		}
		this.escuchandoConsolaLocal = true;

		this.hiloConsolaLocal = new Thread(new Runnable() {
			@Override
			public void run() {
				final Scanner scanner = new Scanner(System.in);
				System.out.println("[Consola] Consola local activa. Escribe 'ayuda' para ver opciones.");

				while (GestorComandos.this.escuchandoConsolaLocal) {
					if (scanner.hasNextLine()) {
						final String linea = scanner.nextLine();
						if ((linea != null) && !linea.trim().isEmpty()) {
							GestorComandos.this.encolarComando(linea.trim(), GestorComandos.this.emisorConsolaLocal);
						}
					}
				}
				scanner.close();
			}
		}, "Hilo-Consola-Dev-Local");

		this.hiloConsolaLocal.setDaemon(true);
		this.hiloConsolaLocal.start();
	}

	/**
	 * Encola un comando proveniente de cualquier fuente (Consola IDE o Socket
	 * Remoto).
	 *
	 * @param textoComando Cadena de texto recibida.
	 * @param emisor       Canal a través del cual responder.
	 */
	public void encolarComando(final String textoComando, final EmisorRespuesta emisor) {
		if ((textoComando != null) && !textoComando.trim().isEmpty()) {
			this.colaComandosEntrantes.offer(new PeticionComando(textoComando.trim(), emisor));
		}
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
	 * Procesa y vacía la cola de comandos en el hilo principal del Game Loop a 60
	 * APS.
	 */
	public void actualizar() {
		// Zero-GC: Si no hay comandos pendientes, poll() retorna null inmediatamente
		// sin allocaciones.
		while (!this.colaComandosEntrantes.isEmpty()) {
			final PeticionComando peticion = this.colaComandosEntrantes.poll();
			if (peticion != null) {
				this.procesarPeticion(peticion);
			}
		}
	}

	private void procesarPeticion(final PeticionComando peticion) {
		final String linea = peticion.getTextoComando();
		final EmisorRespuesta emisor = (peticion.getEmisor() != null) ? peticion.getEmisor() : this.emisorConsolaLocal;

		final String[] tokens = linea.split("\\s+");
		if (tokens.length == 0) {
			return;
		}

		final String nombreComando = tokens[0].toLowerCase();
		final String[] args = new String[tokens.length - 1];
		System.arraycopy(tokens, 1, args, 0, args.length);

		final Comando comando = this.comandosRegistrados.get(nombreComando);
		if (comando != null) {
			try {
				// Ejecuta pasando el emisor de respuesta
				comando.ejecutar(args, emisor);
			} catch (final Exception e) {
				emisor.enviarError("[Consola] Error ejecutando '" + nombreComando + "': " + e.getMessage());
				e.printStackTrace();
			}
		} else {
			emisor.enviarError(
					"[Consola] Comando desconocido: '" + nombreComando + "'. Escribe 'ayuda' para ver comandos.");
		}
	}

	public ServidorTerminalRemota getServidorRemoto() {
		return this.servidorRemoto;
	}
}