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
 * Gestor centralizado de entrada para el teclado.
 * <p>
 * <b>Arquitectura del Sistema de Teclado:</b>
 * <ul>
 * <li><b>Búfer Primitivo Doble (512 Códigos):</b> Mantiene un arreglo booleano
 * de 512 posiciones para almacenar el estado físico de cualquier tecla estándar
 * o multimedia, evitando excepciones
 * {@link ArrayIndexOutOfBoundsException}.</li>
 * <li><b>Detección de Pulsación Única por Frame:</b> Utiliza
 * {@link System#arraycopy} al inicio de cada ciclo para comparar el estado
 * actual contra el frame anterior sin crear objetos en memoria.</li>
 * <li><b>Acciones Conmutables y Condicionadas:</b> Soporta teclas tipo toggle
 * (conmutables como el debug) y acciones condicionadas
 * ({@link TeclaAccionCondicionada}) como el inventario o la pausa.</li>
 * <li><b>Persistencia JSON:</b> Guarda y carga remapeos de teclas
 * personalizados en {@code Config.dat}.</li>
 * </ul>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public class Teclado implements KeyListener {

	/** Archivo local de persistencia de configuración de controles. */
	public final File ARCHIVO_CONFIG = new File("Config.dat");

	/** Lista completa de todas las teclas registradas en el motor. */
	public final ArrayList<Tecla> TECLAS = new ArrayList<Tecla>();

	/**
	 * Diccionario de teclas remapeables por el usuario para su guardado en JSON.
	 */
	public final HashMap<String, Tecla> TECLAS_MODIFICABLES = new HashMap<String, Tecla>();

	// =========================================================================
	// === DECLARACIÓN DE TECLAS DE ACCIÓN Y NAVEGACIÓN
	// =========================================================================

	public final Tecla TECLA_ARRIBA;
	public final Tecla TECLA_ABAJO;
	public final Tecla TECLA_IZQUIERDA;
	public final Tecla TECLA_DERECHA;
	public final Tecla TECLA_RECOGIENDO;
	public final Tecla TECLA_CORRIENDO;
	public final Tecla TECLA_ATACANDO;

	// Teclas de Depuración e Información
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

	// Teclas de Edición y Control del Sistema
	public final Tecla TECLA_GUARDAR_MAPA;
	public final Tecla TECLA_ESCAPE;
	public final Tecla TECLA_PUNTO;
	public final TeclaAccionCondicionada TECLA_INVENTARIO;
	public final TeclaAccionCondicionada TECLA_PAUSA;

	// NUEVAS TECLAS: Control de Zoom Dinámico
	public final Tecla TECLA_ZOOM_IN;
	public final Tecla TECLA_ZOOM_OUT;
	public final Tecla TECLA_ZOOM_REINICIAR;

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
	// === BÚFERES PRIMITIVOS DE ESTADO
	// =========================================================================

	/** Búfer de estado físico activo (true = presionada en este instante). */
	public boolean[] teclas = new boolean[512];

	/**
	 * Búfer histórico del tick anterior para detectar transiciones de subida
	 * (keydown único).
	 */
	private final boolean[] teclasPresionadasAnterior = new boolean[512];

	// =========================================================================
	// === CONSTRUCTOR
	// =========================================================================

	public Teclado() {
		this.TECLA_ARRIBA = new Tecla(KeyEvent.VK_UP, "Mover Arriba");
		this.TECLA_ABAJO = new Tecla(KeyEvent.VK_DOWN, "Mover Abajo");
		this.TECLA_IZQUIERDA = new Tecla(KeyEvent.VK_LEFT, "Mover Izquierda");
		this.TECLA_DERECHA = new Tecla(KeyEvent.VK_RIGHT, "Mover Derecha");
		this.TECLA_RECOGIENDO = new Tecla(KeyEvent.VK_E, "Recoger");
		this.TECLA_CORRIENDO = new Tecla(KeyEvent.VK_SHIFT, "Correr");
		this.TECLA_ATACANDO = new Tecla(KeyEvent.VK_SPACE, "Atacar");

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

		this.TECLA_NUM_1 = new Tecla(KeyEvent.VK_1, "Num_1");
		this.TECLA_NUM_2 = new Tecla(KeyEvent.VK_2, "Num_2");
		this.TECLA_NUM_3 = new Tecla(KeyEvent.VK_3, "Num_3");
		this.TECLA_NUM_4 = new Tecla(KeyEvent.VK_4, "Num_4");
		this.TECLA_NUM_5 = new Tecla(KeyEvent.VK_5, "Num_5");
		this.TECLA_NUM_6 = new Tecla(KeyEvent.VK_6, "Num_6");
		this.TECLA_NUM_7 = new Tecla(KeyEvent.VK_7, "Num_7");
		this.TECLA_NUM_8 = new Tecla(KeyEvent.VK_8, "Num_8");
		this.TECLA_NUM_9 = new Tecla(KeyEvent.VK_9, "Num_9");

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

		this.cargarTeclasALista();
		this.cargarTeclasAListaModificables();

		System.out.println("Configuración de Teclado cargada exitosamente: " + this.cargarConfig());
	}

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

		// Registro de Teclas de Zoom
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

	private void cargarTeclasAListaModificables() {
		this.TECLAS_MODIFICABLES.put(this.TECLA_ARRIBA.nombre, this.TECLA_ARRIBA);
		this.TECLAS_MODIFICABLES.put(this.TECLA_ABAJO.nombre, this.TECLA_ABAJO);
		this.TECLAS_MODIFICABLES.put(this.TECLA_IZQUIERDA.nombre, this.TECLA_IZQUIERDA);
		this.TECLAS_MODIFICABLES.put(this.TECLA_DERECHA.nombre, this.TECLA_DERECHA);
		this.TECLAS_MODIFICABLES.put(this.TECLA_ATACANDO.nombre, this.TECLA_ATACANDO);
		this.TECLAS_MODIFICABLES.put(this.TECLA_RECOGIENDO.nombre, this.TECLA_RECOGIENDO);
		this.TECLAS_MODIFICABLES.put(this.TECLA_CORRIENDO.nombre, this.TECLA_CORRIENDO);
		this.TECLAS_MODIFICABLES.put(this.TECLA_INVENTARIO.nombre, this.TECLA_INVENTARIO);

		// Controles modificables de Zoom
		this.TECLAS_MODIFICABLES.put(this.TECLA_ZOOM_IN.nombre, this.TECLA_ZOOM_IN);
		this.TECLAS_MODIFICABLES.put(this.TECLA_ZOOM_OUT.nombre, this.TECLA_ZOOM_OUT);
		this.TECLAS_MODIFICABLES.put(this.TECLA_ZOOM_REINICIAR.nombre, this.TECLA_ZOOM_REINICIAR);
	}

	// =========================================================================
	// === ACTUALIZACIÓN Y DOBLE BÚFER (GAME LOOP)
	// =========================================================================

	/*
	 * =========================================================================
	 * EXPLICACIÓN TÉCNICA: DOBLE BÚFER Y DETECCIÓN ATÓMICA DE PULSACIONES
	 * ------------------------------------------------------------------------- 1.
	 * En cada ciclo lógico, 'actualizar()' actualiza el estado de cada 'Tecla'. 2.
	 * 'System.arraycopy' realiza una copia ultra rápida de bloques de memoria
	 * nativa desde 'teclas' a 'teclasPresionadasAnterior'. 3.
	 * 'isTeclaPresionadaUnaVez(code)' solo retorna true si: teclas[code] == true &&
	 * teclasPresionadasAnterior[code] == false garantizando cero repeticiones
	 * espurias sin generar basura en el Heap.
	 * =========================================================================
	 */

	/**
	 * Debe llamarse al inicio de cada frame en el Game Loop para actualizar los
	 * estados de pulsación única.
	 */
	public void actualizar() {
		for (final Tecla t : this.TECLAS) {
			t.actualizar();
		}
		System.arraycopy(this.teclas, 0, this.teclasPresionadasAnterior, 0, this.teclas.length);
	}

	/**
	 * Verifica si una tecla física pasó de estar suelta a presionada en este tick
	 * exacto.
	 *
	 * @param codigoTecla Código de la tecla (ej: {@link KeyEvent#VK_SPACE}).
	 * @return {@code true} solo durante el primer tick de la pulsación.
	 */
	public boolean isTeclaPresionadaUnaVez(final int codigoTecla) {
		if ((codigoTecla >= 0) && (codigoTecla < this.teclas.length)) {
			return this.teclas[codigoTecla] && !this.teclasPresionadasAnterior[codigoTecla];
		}
		return false;
	}

	public boolean isTeclaPresionadaUnaVez(final Tecla tecla) {
		if (tecla == null) {
			return false;
		}
		return this.isTeclaPresionadaUnaVez(tecla.getCodigoTecla());
	}

	public boolean presionaTeclaEnLista(final int codigo) {
		if ((codigo >= 0) && (codigo < this.teclas.length)) {
			return this.teclas[codigo];
		}
		return false;
	}

	// =========================================================================
	// === EVENTOS NATIVOS AWT KEYLISTENER
	// =========================================================================

	@Override
	public void keyTyped(final KeyEvent e) {
	}

	@Override
	public void keyPressed(final KeyEvent e) {
		final int code = e.getKeyCode();
		if ((code >= 0) && (code < this.teclas.length)) {
			this.teclas[code] = true;
		}

		for (final Tecla t : this.TECLAS) {
			if (t.getCodigoTecla() == code) {
				t.presionar();
			}
		}
	}

	@Override
	public void keyReleased(final KeyEvent e) {
		final int code = e.getKeyCode();
		if ((code >= 0) && (code < this.teclas.length)) {
			this.teclas[code] = false;
		}

		for (final Tecla t : this.TECLAS) {
			if (t.getCodigoTecla() == code) {
				t.soltar();
			}
		}
	}

	// =========================================================================
	// === PERSISTENCIA JSON (CONFIG.DAT)
	// =========================================================================

	protected JSONObject getConfigJson() {
		final JSONObject jo = new JSONObject();
		for (final Tecla t : this.TECLAS_MODIFICABLES.values()) {
			t.agregarEnJSON(jo);
		}
		return jo;
	}

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

	protected boolean cargarConfig() {
		if (!this.ARCHIVO_CONFIG.exists()) {
			return false;
		}

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

	public void guardarConfig() {
		final JSONObject jo = this.getConfigJson();
		try (final BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(this.ARCHIVO_CONFIG), StandardCharsets.UTF_8))) {

			writer.write(jo.toJSONString().replaceAll(",", ",\n"));
			System.out.println("Configuración guardada en: " + this.ARCHIVO_CONFIG.getAbsolutePath());
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}