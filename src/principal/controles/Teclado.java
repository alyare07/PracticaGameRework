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

import principal.utilidades.Constantes;

public class Teclado implements KeyListener {

	public final File ARCHIVO_CONFIG = new File("Config.dat");
	public final ArrayList<Tecla> TECLAS = new ArrayList<Tecla>();
	public final HashMap<String, Tecla> TECLAS_MODIFICABLES = new HashMap<String, Tecla>();

	public final Tecla TECLA_ARRIBA;
	public final Tecla TECLA_ABAJO;
	public final Tecla TECLA_IZQUIERDA;
	public final Tecla TECLA_DERECHA;
	public final Tecla TECLA_RECOGIENDO;
	public final Tecla TECLA_CORRIENDO;
	public final Tecla TECLA_DEBUG;
	public final Tecla TECLA_FPS_LIMITE;
	public final Tecla TECLA_VER_COLISIONES;
	public final Tecla TECLA_DIJKSTRA;
	public final Tecla TECLA_DIJKSTRA_INFO;
	public final Tecla TECLA_GUARDAR_MAPA;
	public final Tecla TECLA_DEBUG_TILE;
	public final Tecla TECLA_DEBUG_TILE_INFO;
	public final Tecla TECLA_DEBUG_GROUP_TILE;
	public final Tecla TECLA_OCULTAR_TERRENO;
	public final Tecla TECLA_OCULTAR_COMPLEMENTOS;
	public final Tecla TECLA_VER_ALCANCE_ATAQUE;
	public final Tecla TECLA_ATACANDO;
	public final Tecla TECLA_PAUSA;
	public final Tecla TECLA_ESCAPE;
	public final Tecla TECLA_PUNTO;
	public final TeclaAccionCondicionada TECLA_INVENTARIO;

	/**
	 * Arreglo ampliado a 512 elementos para prevenir fuera de rango en teclas
	 * especiales
	 */
	public boolean[] teclas = new boolean[512];
	private final boolean[] teclasPresionadasAnterior = new boolean[512];

	public Teclado() {
		this.TECLA_ARRIBA = new Tecla(KeyEvent.VK_UP, "Mover Arriba");
		this.TECLA_ABAJO = new Tecla(KeyEvent.VK_DOWN, "Mover Abajo");
		this.TECLA_IZQUIERDA = new Tecla(KeyEvent.VK_LEFT, "Mover Izquierda");
		this.TECLA_DERECHA = new Tecla(KeyEvent.VK_RIGHT, "Mover Derecha");
		this.TECLA_RECOGIENDO = new Tecla(KeyEvent.VK_E, "Recoger");
		this.TECLA_CORRIENDO = new Tecla(KeyEvent.VK_SHIFT, "Correr");
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
		this.TECLA_ATACANDO = new Tecla(KeyEvent.VK_SPACE, "Atacar");
		this.TECLA_ESCAPE = new Tecla(KeyEvent.VK_ESCAPE, "Escape");
		this.TECLA_PUNTO = new Tecla(KeyEvent.VK_PERIOD, "Punto");

		this.TECLA_INVENTARIO = new TeclaAccionCondicionada(KeyEvent.VK_I, "Inventario") {
			@Override
			public boolean condicion() {
				return !Constantes.GLOBALES.pausa;
			}

			@Override
			public void accionar() {
				Constantes.GESTOR_INVENTARIO.getInventarioJugador().invertirVisibilidad();
				if ((Constantes.GLOBALES.inventarioVault != null)
						&& !Constantes.GESTOR_INVENTARIO.getInventarioJugador().esVisible()) {
					Constantes.GLOBALES.inventarioVault.cerrar();
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
				Constantes.GLOBALES.pausa = !Constantes.GLOBALES.pausa;
			}
		};

		this.cargarTeclasALista();
		this.cargarTeclasAListaModificables();

		System.out.println("Config Teclado cargada? " + this.cargarConfig());
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
		this.TECLAS.add(this.TECLA_ATACANDO);
		this.TECLAS.add(this.TECLA_PUNTO);
		this.TECLAS.add(this.TECLA_PAUSA);
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
	}

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

	public boolean presionaTeclaEnLista(final int codigo) {
		if ((codigo >= 0) && (codigo < this.teclas.length)) {
			return this.teclas[codigo];
		}
		return false;
	}

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
			System.out.println("Config guardada? True");
		} catch (final Exception e) {
			e.printStackTrace();
			System.out.println("Config guardada? False");
		}
	}
}