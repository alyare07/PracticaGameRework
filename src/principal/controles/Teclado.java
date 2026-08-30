package principal.controles;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import principal.utilidades.Globales;

/**
 * Gestor centralizado de entrada de teclado para el motor del juego.
 * <p>
 * <b>¿Cómo funciona este sistema?</b><br>
 * Los sistemas operativos envían eventos de teclado de forma asíncrona a través
 * del hilo de interfaz gráfica de Java (Event Dispatch Thread o EDT). Sin
 * embargo, nuestro motor de videojuegos actualiza la lógica a un ritmo fijo y
 * constante (60 ticks por segundo o APS) en su propio hilo principal.
 * </p>
 * <p>
 * Para sincronizar ambos mundos sin perder pulsaciones ni generar bloqueos:
 * <ul>
 * <li><b>Arreglo de Estado Físico (512 teclas):</b> El EDT actualiza al
 * instante si una tecla está o no bajada (en tiempo real $O(1)$).</li>
 * <li><b>Doble Búfer de Estado Lógico:</b> En cada tick del juego, comparamos
 * el estado actual con el del tick anterior para detectar el "flanco de subida"
 * (cuando una tecla recién se presiona).</li>
 * <li><b>Zero Allocation:</b> No creamos ningún objeto en memoria (`new`)
 * durante la ejecución de los métodos de actualización para mantener al Garbage
 * Collector completamente inactivo.</li>
 * </ul>
 * </p>
 * 
 * @version 2.1 (Java 8 Pure)
 */
public class Teclado implements KeyListener {

	// =========================================================================
	// === ARCHIVOS Y CONTENEDORES DE TECLAS
	// =========================================================================

	/**
	 * Archivo físico donde se persiste la configuración de controles en formato
	 * JSON.
	 */
	public final File ARCHIVO_CONFIG = new File("Config.dat");

	/**
	 * Lista general con todas las teclas registradas en el juego para su ciclo de
	 * actualización.
	 */
	public final ArrayList<Tecla> TECLAS = new ArrayList<Tecla>();

	/**
	 * Mapa de teclas cuyos códigos pueden ser remapeados o configurados por el
	 * usuario.
	 */
	public final HashMap<String, Tecla> TECLAS_MODIFICABLES = new HashMap<String, Tecla>();

	// =========================================================================
	// === DECLARACIÓN DE TECLAS (ACCIONES DEL JUGADOR)
	// =========================================================================

	public final Tecla TECLA_ARRIBA;
	public final Tecla TECLA_ABAJO;
	public final Tecla TECLA_IZQUIERDA;
	public final Tecla TECLA_DERECHA;
	public final Tecla TECLA_RECOGIENDO;
	public final Tecla TECLA_CORRIENDO;
	public final Tecla TECLA_ATACANDO;

	// =========================================================================
	// === DECLARACIÓN DE TECLAS DE DEPURACIÓN / DEBUG (MODO TOGGLE/INTERRUPTOR)
	// =========================================================================

	public final Tecla TECLA_DEBUG;
	public final Tecla TECLA_FPS_LIMITE;
	public final Tecla TECLA_VER_COLISIONES;
	public final Tecla TECLA_DIJKSTRA;
	public final Tecla TECLA_DIJKSTRA_INFO;
	public final Tecla TECLA_DEBUG_TILE;
	public final Tecla TECLA_DEBUG_TILE_INFO;
	public final Tecla TECLA_DEBUG_GROUP_TILE;
	public final Tecla TECLA_OCULTAR_TERRENO;
	public final Tecla TECLA_OCULTAR_COMPLEMENTOS;
	public final Tecla TECLA_VER_ALCANCE_ATAQUE;

	// =========================================================================
	// === DECLARACIÓN DE TECLAS DE SISTEMA E INTERFAZ
	// =========================================================================

	public final Tecla TECLA_GUARDAR_MAPA;
	public final Tecla TECLA_ESCAPE;
	public final Tecla TECLA_PUNTO;
	public final TeclaAccionCondicionada TECLA_INVENTARIO;
	public final TeclaAccionCondicionada TECLA_PAUSA;

	// Controles de Cámara / Zoom
	public final Tecla TECLA_ZOOM_IN;
	public final Tecla TECLA_ZOOM_OUT;
	public final Tecla TECLA_ZOOM_REINICIAR;

	// Acciones Rápidas (Barra numérica / Slots)
	public final Tecla TECLA_NUM_1;
	public final Tecla TECLA_NUM_2;
	public final Tecla TECLA_NUM_3;
	public final Tecla TECLA_NUM_4;
	public final Tecla TECLA_NUM_5;
	public final Tecla TECLA_NUM_6;
	public final Tecla TECLA_NUM_7;
	public final Tecla TECLA_NUM_8;
	public final Tecla TECLA_NUM_9;

	// =========================================================================
	// === BÚFERES PRIMITIVOS DE ESTADO (TRIPLE BÚFER DETERMINISTA)
	// =========================================================================

	/**
	 * Búfer físico activo (Hardware State).
	 * <p>
	 * Este arreglo almacena directamente si una tecla física está presionada o no.
	 * Se indexa por el código virtual de la tecla (ej: {@link KeyEvent#VK_W}). Es
	 * modificado asíncronamente por el EDT a través de los eventos de AWT.
	 * </p>
	 */
	public final boolean[] teclas = new boolean[512];

	/**
	 * Búfer histórico del tick lógico anterior.
	 * <p>
	 * Nos permite saber cómo estaba el teclado exactamente en el fotograma anterior
	 * para comparar y detectar cambios de estado.
	 * </p>
	 */
	private final boolean[] teclasPresionadasAnterior = new boolean[512];

	/**
	 * Búfer de pulsación única (Flanco de Subida / Just Pressed).
	 * <p>
	 * Es `true` <b>únicamente en el primer tick</b> en el que la tecla pasa de
	 * estar suelta a estar presionada. Si el jugador mantiene la tecla presionada,
	 * en los ticks siguientes volverá a ser `false`.
	 * </p>
	 */
	private final boolean[] teclasPulsadasUnaVez = new boolean[512];

	// =========================================================================
	// === CONSTRUCTOR
	// =========================================================================

	/**
	 * Inicializa todas las asignaciones por defecto, configura los disparadores
	 * condicionados y carga la configuración persistente desde el disco.
	 */
	public Teclado() {
		// Asignación de Controles Básicos
		this.TECLA_ARRIBA = new Tecla(KeyEvent.VK_UP, "Mover Arriba");
		this.TECLA_ABAJO = new Tecla(KeyEvent.VK_DOWN, "Mover Abajo");
		this.TECLA_IZQUIERDA = new Tecla(KeyEvent.VK_LEFT, "Mover Izquierda");
		this.TECLA_DERECHA = new Tecla(KeyEvent.VK_RIGHT, "Mover Derecha");
		this.TECLA_RECOGIENDO = new Tecla(KeyEvent.VK_E, "Recoger");
		this.TECLA_CORRIENDO = new Tecla(KeyEvent.VK_SHIFT, "Correr");
		this.TECLA_ATACANDO = new Tecla(KeyEvent.VK_SPACE, "Atacar");

		// Asignación de Teclas de Depuración (El segundo parámetro 'true' indica modo
		// Toggle/Interruptor)
		this.TECLA_DEBUG = new Tecla(KeyEvent.VK_F1, true, "Debug");
		this.TECLA_FPS_LIMITE = new Tecla(KeyEvent.VK_F11, true, "FPS Limite");
		this.TECLA_VER_COLISIONES = new Tecla(KeyEvent.VK_F7, true, "Ver Colisiones");
		this.TECLA_DIJKSTRA = new Tecla(KeyEvent.VK_F2, true, "IA");
		this.TECLA_DIJKSTRA_INFO = new Tecla(KeyEvent.VK_F6, true, "IA Info");
		this.TECLA_GUARDAR_MAPA = new Tecla(KeyEvent.VK_ENTER, "Guardar Mapa");
		this.TECLA_DEBUG_TILE = new Tecla(KeyEvent.VK_F3, true, "Debug Tile");
		this.TECLA_DEBUG_TILE_INFO = new Tecla(KeyEvent.VK_F5, true, "Tile Info");
		this.TECLA_DEBUG_GROUP_TILE = new Tecla(KeyEvent.VK_F4, true, "Debug Group Tile");
		this.TECLA_OCULTAR_TERRENO = new Tecla(KeyEvent.VK_F8, true, "Ocultar Terreno");
		this.TECLA_OCULTAR_COMPLEMENTOS = new Tecla(KeyEvent.VK_F9, true, "Ocultar Complementos");
		this.TECLA_VER_ALCANCE_ATAQUE = new Tecla(KeyEvent.VK_F10, true, "Ver Alcance");
		this.TECLA_ESCAPE = new Tecla(KeyEvent.VK_ESCAPE, "Escape");
		this.TECLA_PUNTO = new Tecla(KeyEvent.VK_PERIOD, "Punto");

		// Controles de Zoom
		this.TECLA_ZOOM_IN = new Tecla(KeyEvent.VK_PLUS, "Zoom In");
		this.TECLA_ZOOM_OUT = new Tecla(KeyEvent.VK_MINUS, "Zoom Out");
		this.TECLA_ZOOM_REINICIAR = new Tecla(KeyEvent.VK_ASTERISK, "Zoom Reset");

		// Teclas Numéricas
		this.TECLA_NUM_1 = new Tecla(KeyEvent.VK_1, "Num_1");
		this.TECLA_NUM_2 = new Tecla(KeyEvent.VK_2, "Num_2");
		this.TECLA_NUM_3 = new Tecla(KeyEvent.VK_3, "Num_3");
		this.TECLA_NUM_4 = new Tecla(KeyEvent.VK_4, "Num_4");
		this.TECLA_NUM_5 = new Tecla(KeyEvent.VK_5, "Num_5");
		this.TECLA_NUM_6 = new Tecla(KeyEvent.VK_6, "Num_6");
		this.TECLA_NUM_7 = new Tecla(KeyEvent.VK_7, "Num_7");
		this.TECLA_NUM_8 = new Tecla(KeyEvent.VK_8, "Num_8");
		this.TECLA_NUM_9 = new Tecla(KeyEvent.VK_9, "Num_9");

		// Tecla de Inventario: Solo se abre/cierra si el juego no se encuentra pausado
		this.TECLA_INVENTARIO = new TeclaAccionCondicionada(KeyEvent.VK_I, "Inventario") {
			@Override
			public boolean condicion() {
				return !Globales.pausa;
			}

			@Override
			public void accionar() {
				Globales.GESTOR_INVENTARIO.getInventarioJugador().invertirVisibilidad();
				if ((Globales.inventarioVault != null)
						&& !Globales.GESTOR_INVENTARIO.getInventarioJugador().esVisible()) {
					Globales.inventarioVault.cerrar();
				}
			}
		};

		// Tecla de Pausa: Siempre ejecutable
		this.TECLA_PAUSA = new TeclaAccionCondicionada(KeyEvent.VK_P, "Pausa") {
			@Override
			public boolean condicion() {
				return true;
			}

			@Override
			public void accionar() {
				Globales.pausa = !Globales.pausa;
			}
		};

		// Registro y carga de configuración
		this.cargarTeclasALista();
		this.cargarTeclasAListaModificables();
		this.cargarConfig();
	}

	/**
	 * Agrega todas las teclas a la lista maestra de actualización.
	 */
	private void cargarTeclasALista() {
		this.TECLAS.add(this.TECLA_INVENTARIO);
		this.TECLAS.add(this.TECLA_ESCAPE);
		this.TECLAS.add(this.TECLA_ARRIBA);
		this.TECLAS.add(this.TECLA_ABAJO);
		this.TECLAS.add(this.TECLA_IZQUIERDA);
		this.TECLAS.add(this.TECLA_DERECHA);
		this.TECLAS.add(this.TECLA_RECOGIENDO);
		this.TECLAS.add(this.TECLA_CORRIENDO);
		this.TECLAS.add(this.TECLA_ATACANDO);
		this.TECLAS.add(this.TECLA_DEBUG);
		this.TECLAS.add(this.TECLA_FPS_LIMITE);
		this.TECLAS.add(this.TECLA_VER_COLISIONES);
		this.TECLAS.add(this.TECLA_DIJKSTRA);
		this.TECLAS.add(this.TECLA_DIJKSTRA_INFO);
		this.TECLAS.add(this.TECLA_GUARDAR_MAPA);
		this.TECLAS.add(this.TECLA_DEBUG_TILE);
		this.TECLAS.add(this.TECLA_DEBUG_TILE_INFO);
		this.TECLAS.add(this.TECLA_DEBUG_GROUP_TILE);
		this.TECLAS.add(this.TECLA_OCULTAR_TERRENO);
		this.TECLAS.add(this.TECLA_OCULTAR_COMPLEMENTOS);
		this.TECLAS.add(this.TECLA_VER_ALCANCE_ATAQUE);
		this.TECLAS.add(this.TECLA_PUNTO);
		this.TECLAS.add(this.TECLA_PAUSA);

		this.TECLAS.add(this.TECLA_ZOOM_IN);
		this.TECLAS.add(this.TECLA_ZOOM_OUT);
		this.TECLAS.add(this.TECLA_ZOOM_REINICIAR);

		this.TECLAS.add(this.TECLA_NUM_1);
		this.TECLAS.add(this.TECLA_NUM_2);
		this.TECLAS.add(this.TECLA_NUM_3);
		this.TECLAS.add(this.TECLA_NUM_4);
		this.TECLAS.add(this.TECLA_NUM_5);
		this.TECLAS.add(this.TECLA_NUM_6);
		this.TECLAS.add(this.TECLA_NUM_7);
		this.TECLAS.add(this.TECLA_NUM_8);
		this.TECLAS.add(this.TECLA_NUM_9);
	}

	/**
	 * Mapea las teclas que el usuario tiene permitido reconfigurar desde menús u
	 * opciones.
	 */
	private void cargarTeclasAListaModificables() {
		this.TECLAS_MODIFICABLES.put(this.TECLA_ARRIBA.nombre, this.TECLA_ARRIBA);
		this.TECLAS_MODIFICABLES.put(this.TECLA_ABAJO.nombre, this.TECLA_ABAJO);
		this.TECLAS_MODIFICABLES.put(this.TECLA_IZQUIERDA.nombre, this.TECLA_IZQUIERDA);
		this.TECLAS_MODIFICABLES.put(this.TECLA_DERECHA.nombre, this.TECLA_DERECHA);
		this.TECLAS_MODIFICABLES.put(this.TECLA_ATACANDO.nombre, this.TECLA_ATACANDO);
		this.TECLAS_MODIFICABLES.put(this.TECLA_RECOGIENDO.nombre, this.TECLA_RECOGIENDO);
		this.TECLAS_MODIFICABLES.put(this.TECLA_CORRIENDO.nombre, this.TECLA_CORRIENDO);
		this.TECLAS_MODIFICABLES.put(this.TECLA_INVENTARIO.nombre, this.TECLA_INVENTARIO);

		this.TECLAS_MODIFICABLES.put(this.TECLA_ZOOM_IN.nombre, this.TECLA_ZOOM_IN);
		this.TECLAS_MODIFICABLES.put(this.TECLA_ZOOM_OUT.nombre, this.TECLA_ZOOM_OUT);
		this.TECLAS_MODIFICABLES.put(this.TECLA_ZOOM_REINICIAR.nombre, this.TECLA_ZOOM_REINICIAR);
	}

	// =========================================================================
	// === ACTUALIZACIÓN DETERMINISTA (GAME LOOP)
	// =========================================================================

	/**
	 * Actualiza los estados lógicos de todas las teclas al inicio del ciclo de
	 * juego.
	 * <p>
	 * <b>Principio de Optimización (Zero Garbage Collector):</b><br>
	 * Este método se invoca 60 veces por segundo. Para evitar que el recolector de
	 * basura pause el juego con micro-tirones (stuttering), no usamos foreach (que
	 * crea un objeto `Iterator` invisible), sino un bucle `for` tradicional con
	 * acceso por índice `size()`.
	 * </p>
	 */
	public void actualizar() {
		// ---------------------------------------------------------------------
		// 1. Detección de Flancos (Edge Detection)
		// ---------------------------------------------------------------------
		// Una tecla se considera "pulsada una sola vez" si actualmente está presionada
		// Y en el tick anterior NO lo estaba.
		for (int i = 0; i < this.teclas.length; i++) {
			this.teclasPulsadasUnaVez[i] = this.teclas[i] && !this.teclasPresionadasAnterior[i];
			this.teclasPresionadasAnterior[i] = this.teclas[i];
		}

		// ---------------------------------------------------------------------
		// 2. Actualización de Objetos Tecla
		// ---------------------------------------------------------------------
		final int total = this.TECLAS.size();
		for (int i = 0; i < total; i++) {
			this.TECLAS.get(i).actualizar();
		}
	}

	/**
	 * Verifica si un código de tecla de hardware fue presionado exactamente en este
	 * fotograma.
	 *
	 * @param codigoTecla Código de la tecla (ej. {@link KeyEvent#VK_SPACE}).
	 * @return {@code true} si la tecla se acaba de presionar en este tick;
	 *         {@code false} en caso contrario.
	 */
	public boolean isTeclaPresionadaUnaVez(final int codigoTecla) {
		if ((codigoTecla >= 0) && (codigoTecla < this.teclasPulsadasUnaVez.length)) {
			return this.teclasPulsadasUnaVez[codigoTecla];
		}
		return false;
	}

	/**
	 * Verifica si un objeto {@link Tecla} específico fue presionado exactamente en
	 * este fotograma.
	 *
	 * @param tecla Instancia de la tecla a comprobar.
	 * @return {@code true} si se accionó en este frame; {@code false} si es nula o
	 *         se mantiene presionada.
	 */
	public boolean isTeclaPresionadaUnaVez(final Tecla tecla) {
		if (tecla == null) {
			return false;
		}
		return tecla.presionadoUnicaActualizacion();
	}

	/**
	 * Comprueba si una tecla física está siendo mantenida presionada de forma
	 * continua.
	 *
	 * @param codigo Código virtual de la tecla.
	 * @return {@code true} si la tecla está abajo/activa; {@code false} en caso
	 *         contrario.
	 */
	public boolean presionaTeclaEnLista(final int codigo) {
		if ((codigo >= 0) && (codigo < this.teclas.length)) {
			return this.teclas[codigo];
		}
		return false;
	}

	// =========================================================================
	// === EVENTOS NATIVOS AWT KEYLISTENER (EJECUTADOS POR EL HILO EDT)
	// =========================================================================

	@Override
	public void keyTyped(final KeyEvent e) {
		// No se utiliza para la lógica de entrada directa del motor (se usa para
		// captura de texto/chat).
	}

	/**
	 * Invocado por el hilo de eventos de AWT cuando una tecla física desciende.
	 */
	@Override
	public void keyPressed(final KeyEvent e) {
		final int code = e.getKeyCode();

		// Actualizamos el arreglo físico directo en tiempo constante O(1)
		if ((code >= 0) && (code < this.teclas.length)) {
			this.teclas[code] = true;
		}

		// Notificamos a las instancias Tecla registradas
		final int total = this.TECLAS.size();
		for (int i = 0; i < total; i++) {
			final Tecla t = this.TECLAS.get(i);
			if (t.getCodigoTecla() == code) {
				t.presionar();
			}
		}
	}

	/**
	 * Invocado por el hilo de eventos de AWT cuando una tecla física se libera.
	 */
	@Override
	public void keyReleased(final KeyEvent e) {
		final int code = e.getKeyCode();

		// Actualizamos el arreglo físico directo en tiempo constante O(1)
		if ((code >= 0) && (code < this.teclas.length)) {
			this.teclas[code] = false;
		}

		// Notificamos a las instancias Tecla registradas
		final int total = this.TECLAS.size();
		for (int i = 0; i < total; i++) {
			final Tecla t = this.TECLAS.get(i);
			if (t.getCodigoTecla() == code) {
				t.soltar();
			}
		}
	}

	// =========================================================================
	// === PERSISTENCIA JSON (CONFIGURACIÓN DE CONTROLES)
	// =========================================================================

	/**
	 * Genera un objeto JSON estructurado con los nombres y códigos de las teclas
	 * modificables.
	 *
	 * @return {@link JSONObject} listo para serializar a disco.
	 */
	@SuppressWarnings("unchecked")
	protected JSONObject getConfigJson() {
		final JSONObject jo = new JSONObject();
		for (final Tecla t : this.TECLAS_MODIFICABLES.values()) {
			t.agregarEnJSON(jo);
		}
		return jo;
	}

	/**
	 * Aplica la configuración cargada desde un objeto JSON a las teclas del juego.
	 *
	 * @param jo Objeto JSON con el mapeo de teclas cargado.
	 */
	protected void establecerConfig(final JSONObject jo) {
		if (jo == null) {
			return;
		}
		for (final Tecla t : this.TECLAS_MODIFICABLES.values()) {
			if (jo.containsKey(t.nombre)) {
				try {
					t.establecerCodigoTecla(((Number) jo.get(t.nombre)).intValue());
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Carga la configuración de controles desde el archivo local en formato UTF-8.
	 *
	 * @return {@code true} si se cargó exitosamente; {@code false} si el archivo no
	 *         existe o hubo un error.
	 */
	protected boolean cargarConfig() {
		if (!this.ARCHIVO_CONFIG.exists()) {
			return false;
		}

		// Usamos Try-with-resources para garantizar el cierre seguro de los flujos de
		// lectura
		try (final BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(this.ARCHIVO_CONFIG), StandardCharsets.UTF_8))) {

			final StringBuilder sb = new StringBuilder();
			String linea;
			while ((linea = reader.readLine()) != null) {
				sb.append(linea);
			}

			final JSONObject jo = (JSONObject) (new JSONParser()).parse(sb.toString());
			this.establecerConfig(jo);
			return true;
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Guarda en disco de forma legible la configuración actual de los controles
	 * modificables.
	 */
	public void guardarConfig() {
		final JSONObject jo = this.getConfigJson();
		try (final BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(this.ARCHIVO_CONFIG), StandardCharsets.UTF_8))) {

			// Formateamos visualmente agregando saltos de línea tras cada propiedad JSON
			writer.write(jo.toJSONString().replaceAll(",", ",\n"));
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}