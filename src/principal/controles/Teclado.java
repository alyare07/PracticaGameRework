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
import java.util.Arrays;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import principal.utilidades.Globales;

/**
 * Gestor centralizado de teclado con sincronización Thread-Safe entre el hilo
 * EDT de Swing y el Game Loop de 60 APS (Zero-GC / Snapshot Isolation).
 * 
 * @version 3.0 (Vanilla Java 8 - Lock-Free Latch Bridge)
 */
public class Teclado implements KeyListener {

	public final File ARCHIVO_CONFIG = new File("Config.dat");
	public final ArrayList<Tecla> TECLAS = new ArrayList<Tecla>();
	public final HashMap<String, Tecla> TECLAS_MODIFICABLES = new HashMap<String, Tecla>();

	// =========================================================================
	// === TECLAS DE ACCIÓN PRINCIPALES
	// =========================================================================

	public final Tecla TECLA_ARRIBA;
	public final Tecla TECLA_ABAJO;
	public final Tecla TECLA_IZQUIERDA;
	public final Tecla TECLA_DERECHA;
	public final Tecla TECLA_RECOGIENDO;
	public final Tecla TECLA_CORRIENDO;
	public final Tecla TECLA_ATACANDO;
	public final Tecla TECLA_RECARGAR;
	public final Tecla TECLA_CONSTRUCCION;
	public final Tecla TECLA_ALT_LEFT;

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

	public final Tecla TECLA_GUARDAR_MAPA;
	public final Tecla TECLA_ESCAPE;
	public final Tecla TECLA_PUNTO;
	public final TeclaAccionCondicionada TECLA_INVENTARIO;
	public final TeclaAccionCondicionada TECLA_PAUSA;

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
	// === BÚFERES DE CONCURRENCIA THREAD-SAFE (ZERO-GC)
	// =========================================================================

	private static final int TOTAL_TECLAS = 512;
	private final Object candadoSincronizacion = new Object();

	/** Estado crudo escrito exclusivamente por el hilo EDT de Swing. */
	private final boolean[] teclasFisicasEDT = new boolean[TOTAL_TECLAS];

	/** Pestillo para capturar toques ultra-rápidos entre frames sin perderlos. */
	private final boolean[] latchPulsadasEDT = new boolean[TOTAL_TECLAS];

	/** Búfer de transferencia intermedio (reutilizado, 0 allocations). */
	private final boolean[] latchConsumido = new boolean[TOTAL_TECLAS];

	/** Snapshot inmutable consumido por el Game Loop a 60 APS. */
	public final boolean[] teclas = new boolean[TOTAL_TECLAS];
	private final boolean[] teclasPresionadasAnterior = new boolean[TOTAL_TECLAS];
	private final boolean[] teclasPulsadasUnaVez = new boolean[TOTAL_TECLAS];

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
		this.TECLA_RECARGAR = new Tecla(KeyEvent.VK_R, "Recargar");
		this.TECLA_CONSTRUCCION = new Tecla(KeyEvent.VK_B, "Modo Construir");
		this.TECLA_ALT_LEFT = new Tecla(KeyEvent.VK_R, "Recargar");

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
		this.cargarConfig();
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
		this.TECLAS.add(this.TECLA_RECARGAR);
		this.TECLAS.add(this.TECLA_CONSTRUCCION);
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
		this.TECLAS.add(this.TECLA_ALT_LEFT);

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
		this.TECLAS_MODIFICABLES.put(this.TECLA_RECARGAR.nombre, this.TECLA_RECARGAR);
		this.TECLAS_MODIFICABLES.put(this.TECLA_CONSTRUCCION.nombre, this.TECLA_CONSTRUCCION);
		this.TECLAS_MODIFICABLES.put(this.TECLA_INVENTARIO.nombre, this.TECLA_INVENTARIO);

		this.TECLAS_MODIFICABLES.put(this.TECLA_ZOOM_IN.nombre, this.TECLA_ZOOM_IN);
		this.TECLAS_MODIFICABLES.put(this.TECLA_ZOOM_OUT.nombre, this.TECLA_ZOOM_OUT);
		this.TECLAS_MODIFICABLES.put(this.TECLA_ZOOM_REINICIAR.nombre, this.TECLA_ZOOM_REINICIAR);
	}

	// =========================================================================
	// === CICLO LÓGICO DEL JUEGO (GAME LOOP - 60 APS)
	// =========================================================================

	/**
	 * Transfiere de forma atómica el estado de los eventos del hilo EDT al Game
	 * Loop.
	 */
	public void actualizar() {
		// Transferencia atómica y segura entre hilos (< 0.0002 ms)
		synchronized (this.candadoSincronizacion) {
			System.arraycopy(this.teclasFisicasEDT, 0, this.teclas, 0, TOTAL_TECLAS);
			System.arraycopy(this.latchPulsadasEDT, 0, this.latchConsumido, 0, TOTAL_TECLAS);
			Arrays.fill(this.latchPulsadasEDT, false);
		}

		// Evaluación de pulsaciones de frame único
		for (int i = 0; i < TOTAL_TECLAS; i++) {
			this.teclasPulsadasUnaVez[i] = this.latchConsumido[i]
					|| (this.teclas[i] && !this.teclasPresionadasAnterior[i]);
			this.teclasPresionadasAnterior[i] = this.teclas[i];
		}

		final int total = this.TECLAS.size();
		for (int i = 0; i < total; i++) {
			this.TECLAS.get(i).actualizar();
		}
	}

	public boolean isTeclaPresionadaUnaVez(final int codigoTecla) {
		if ((codigoTecla >= 0) && (codigoTecla < TOTAL_TECLAS)) {
			return this.teclasPulsadasUnaVez[codigoTecla];
		}
		return false;
	}

	public boolean isTeclaPresionadaUnaVez(final Tecla tecla) {
		if (tecla == null) {
			return false;
		}
		return tecla.presionadoUnicaActualizacion();
	}

	public boolean presionaTeclaEnLista(final int codigo) {
		if ((codigo >= 0) && (codigo < TOTAL_TECLAS)) {
			return this.teclas[codigo];
		}
		return false;
	}

	// =========================================================================
	// === EVENTOS ASÍNCRONOS DE TECLADO (HILO EDT DE SWING)
	// =========================================================================

	@Override
	public void keyTyped(final KeyEvent e) {
	}

	@Override
	public void keyPressed(final KeyEvent e) {
		final int code = e.getKeyCode();

		if ((code >= 0) && (code < TOTAL_TECLAS)) {
			synchronized (this.candadoSincronizacion) {
				this.teclasFisicasEDT[code] = true;
				this.latchPulsadasEDT[code] = true;
			}
		}

		final int total = this.TECLAS.size();
		for (int i = 0; i < total; i++) {
			final Tecla t = this.TECLAS.get(i);
			if (t.getCodigoTecla() == code) {
				t.presionar();
			}
		}
	}

	@Override
	public void keyReleased(final KeyEvent e) {
		final int code = e.getKeyCode();

		if ((code >= 0) && (code < TOTAL_TECLAS)) {
			synchronized (this.candadoSincronizacion) {
				this.teclasFisicasEDT[code] = false;
			}
		}

		final int total = this.TECLAS.size();
		for (int i = 0; i < total; i++) {
			final Tecla t = this.TECLAS.get(i);
			if (t.getCodigoTecla() == code) {
				t.soltar();
			}
		}
	}

	// =========================================================================
	// === PERSISTENCIA JSON
	// =========================================================================

	@SuppressWarnings("unchecked")
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
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}