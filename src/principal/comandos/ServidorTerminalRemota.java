package principal.comandos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servidor TCP para recibir comandos desde Termux (Android) u otras terminales
 * en la red local.
 */
public class ServidorTerminalRemota {

	private final int puerto;
	private final GestorComandos gestorComandos;
	private ServerSocket serverSocket;
	private Thread hiloServidor;
	private volatile boolean ejecutando;

	/** Set concurrente de clientes conectados. */
	private final Set<ClienteHandler> clientesConectados = Collections
			.newSetFromMap(new ConcurrentHashMap<ClienteHandler, Boolean>());

	public ServidorTerminalRemota(final int puerto, final GestorComandos gestorComandos) {
		this.puerto = puerto;
		this.gestorComandos = gestorComandos;
		this.ejecutando = false;
	}

	/**
	 * Inicia la escucha de conexiones en 0.0.0.0 (todas las interfaces) para
	 * aceptar a Termux.
	 */
	public void iniciar() {
		if (this.ejecutando) {
			return;
		}
		this.ejecutando = true;

		this.hiloServidor = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// new ServerSocket(puerto) vincula a 0.0.0.0 (acepta conexiones locales y de la
					// red Wi-Fi)
					ServidorTerminalRemota.this.serverSocket = new ServerSocket(ServidorTerminalRemota.this.puerto);

					final String ipLocal = ServidorTerminalRemota.this.obtenerIpLocalLAN();
					System.out.println("=================================================================");
					System.out.println("[Terminal Remota] Servidor activo para Termux (Android).");
					System.out.println("[Terminal Remota] Conectate desde Termux con: nc " + ipLocal + " "
							+ ServidorTerminalRemota.this.puerto);
					System.out.println("=================================================================");

					while (ServidorTerminalRemota.this.ejecutando
							&& !ServidorTerminalRemota.this.serverSocket.isClosed()) {
						final Socket socketCliente = ServidorTerminalRemota.this.serverSocket.accept();
						final ClienteHandler handler = new ClienteHandler(socketCliente);
						ServidorTerminalRemota.this.clientesConectados.add(handler);

						final Thread hiloCliente = new Thread(handler,
								"Hilo-Cliente-Termux-" + socketCliente.getPort());
						hiloCliente.setDaemon(true);
						hiloCliente.start();
					}
				} catch (final SocketException se) {
					// Cierre normal al cerrar el juego
				} catch (final IOException e) {
					System.err.println("[Terminal Remota] Error en el socket: " + e.getMessage());
				}
			}
		}, "Hilo-Servidor-Terminal-Remota");

		this.hiloServidor.setDaemon(true);
		this.hiloServidor.start();
	}

	/**
	 * Obtiene la dirección IP local de la PC en la red Wi-Fi / Ethernet.
	 */
	private String obtenerIpLocalLAN() {
		try (final DatagramSocket socket = new DatagramSocket()) {
			socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
			return socket.getLocalAddress().getHostAddress();
		} catch (final Exception e) {
			try {
				return InetAddress.getLocalHost().getHostAddress();
			} catch (final Exception ex) {
				return "127.0.0.1";
			}
		}
	}

	public void detener() {
		this.ejecutando = false;
		try {
			if ((this.serverSocket != null) && !this.serverSocket.isClosed()) {
				this.serverSocket.close();
			}
		} catch (final IOException e) {
			// Ignorar
		}

		for (final ClienteHandler c : this.clientesConectados) {
			c.desconectar();
		}
		this.clientesConectados.clear();
	}

	/**
	 * Manejador de la sesión de Termux.
	 */
	private class ClienteHandler implements Runnable, EmisorRespuesta {

		private final Socket socket;
		private BufferedReader in;
		private PrintWriter out;
		private volatile boolean conectado;

		public ClienteHandler(final Socket socket) {
			this.socket = socket;
			this.conectado = true;
		}

		@Override
		public void run() {
			try {
				this.in = new BufferedReader(
						new InputStreamReader(this.socket.getInputStream(), StandardCharsets.UTF_8));
				this.out = new PrintWriter(
						new OutputStreamWriter(this.socket.getOutputStream(), StandardCharsets.UTF_8), true);

				// Mensaje de bienvenida a Termux
				this.out.println("\n╔══════════════════════════════════════════════════╗");
				this.out.println("║   MOTOR RPG 2D - CONSOLA REMOTA TERMUX (v3.2)   ║");
				this.out.println("╚══════════════════════════════════════════════════╝");
				this.out.println("Escribe 'ayuda' para ver los comandos o 'salir' para desconectar.\n");
				this.enviarPrompt();

				String linea;
				while (this.conectado && ((linea = this.in.readLine()) != null)) {
					linea = linea.trim();
					if (linea.isEmpty()) {
						this.enviarPrompt();
						continue;
					}

					if (linea.equalsIgnoreCase("salir") || linea.equalsIgnoreCase("exit")
							|| linea.equalsIgnoreCase("quit")) {
						this.out.println("Desconectando de la sesion del juego...");
						break;
					}
					if (linea.equalsIgnoreCase("clear") || linea.equalsIgnoreCase("cls")) {
						// Secuencia de escape ANSI compatible con Termux para limpiar la pantalla
						this.out.print("\033[H\033[2J");
						this.out.flush();
						this.enviarPrompt();
						continue;
					}

					// Encolar al Game Loop principal de 60 APS
					ServidorTerminalRemota.this.gestorComandos.encolarComando(linea, this);
				}
			} catch (final SocketException e) {
				// Termux cerrado o desconectado
			} catch (final Exception e) {
				System.err.println("[Terminal Remota] Error en cliente Termux: " + e.getMessage());
			} finally {
				this.desconectar();
				ServidorTerminalRemota.this.clientesConectados.remove(this);
			}
		}

		private void enviarPrompt() {
			if (this.out != null) {
				this.out.print("game-termux> ");
				this.out.flush();
			}
		}

		public void desconectar() {
			this.conectado = false;
			try {
				if ((this.socket != null) && !this.socket.isClosed()) {
					this.socket.close();
				}
			} catch (final IOException e) {
				// Ignorar
			}
		}

		@Override
		public void enviarMensaje(final String mensaje) {
			if (this.out != null) {
				this.out.println(mensaje);
				this.enviarPrompt();
			}
		}

		@Override
		public void enviarError(final String error) {
			if (this.out != null) {
				this.out.println("[ERROR] " + error);
				this.enviarPrompt();
			}
		}
	}
}