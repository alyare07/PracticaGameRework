package principal.controles;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import principal.utilidades.Constantes;

public class Teclado implements KeyListener {

//	public boolean arriba = false;
//	public boolean abajo = false;
//	public boolean izquierda = false;
//	public boolean derecha = false;
//	public boolean recogiendo = false;
//	public boolean corriendo = false;
//	public boolean debug = false;
//	public boolean verColisiones = false;
//	public boolean dijkstra = false;
//	public boolean dijkstraInfo = false;
//	public boolean guardarMapa = false;
//	public boolean debugTile = false;
//	public boolean debugTileInfo = false;
//	public boolean debugGroupTile = false;
//	public boolean ocultarTerreno = false;
//	public boolean ocultarComplementos = false;
//	public boolean verAlcanceAtaque = false;
//	public boolean atacando = false;
//	public boolean scape = false;
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
	public boolean[] teclas = new boolean[300];
    private boolean[] teclasPresionadasAnterior = new boolean[300];
	
	
	public Teclado() {
		this.TECLA_ARRIBA = new Tecla(KeyEvent.VK_UP,"Mover Arriba");
		this.TECLA_ABAJO = new Tecla(KeyEvent.VK_DOWN,"Mover Abajo");
		this.TECLA_IZQUIERDA = new Tecla(KeyEvent.VK_LEFT,"Mover Izquierda");
		this.TECLA_DERECHA = new Tecla(KeyEvent.VK_RIGHT,"Mover Derecha");
		this.TECLA_RECOGIENDO = new Tecla(KeyEvent.VK_E,"Recoger");
		this.TECLA_CORRIENDO = new Tecla(KeyEvent.VK_SHIFT, "Correr");
		this.TECLA_DEBUG = new Tecla(KeyEvent.VK_F1,true, "Debug");
		this.TECLA_VER_COLISIONES = new Tecla(KeyEvent.VK_F7,true, "Ver Colisiones");
		this.TECLA_DIJKSTRA = new Tecla(KeyEvent.VK_F2,true, "IA");
		this.TECLA_DIJKSTRA_INFO = new Tecla(KeyEvent.VK_F6,true, "IA Info");
		this.TECLA_GUARDAR_MAPA = new Tecla(KeyEvent.VK_ENTER, "Guardar Mapa");
		this.TECLA_DEBUG_TILE = new Tecla(KeyEvent.VK_F3,true, "Debug Tile");
		this.TECLA_DEBUG_TILE_INFO = new Tecla(KeyEvent.VK_F5,true, "Tile Info");
		this.TECLA_DEBUG_GROUP_TILE = new Tecla(KeyEvent.VK_F4,true, "Debug Group Tile");
		this.TECLA_OCULTAR_TERRENO = new Tecla(KeyEvent.VK_F8,true, "Ocultar Terreno");
		this.TECLA_OCULTAR_COMPLEMENTOS = new Tecla(KeyEvent.VK_F9,true, "Ocultar Complementos");
		this.TECLA_VER_ALCANCE_ATAQUE = new Tecla(KeyEvent.VK_F10,true, "Ver Alcance");
		this.TECLA_ATACANDO = new Tecla(KeyEvent.VK_SPACE, "Atacar");
		this.TECLA_ESCAPE = new Tecla(KeyEvent.VK_ESCAPE, "Escape");
		this.TECLA_PUNTO = new Tecla(KeyEvent.VK_PERIOD, "Punto");
		this.TECLA_INVENTARIO = new TeclaAccionCondicionada(KeyEvent.VK_I,true, "Inventario") {
			
			@Override
			public boolean condicion() {
				return !Constantes.GLOBALES.pausa;
			}

			@Override
			public void accionar() {
				Constantes.INVENTARIO.invertirVisibilidad();
				if(Constantes.GLOBALES.inventarioVault!=null && !Constantes.INVENTARIO.esVisible()) {
					Constantes.GLOBALES.inventarioVault.cerrar();
				}
			}
		};
		
		this.TECLA_PAUSA = new TeclaAccionCondicionada(KeyEvent.VK_P,"Pausa") {
			
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
		
		System.out.println("Config Teclado cargada? "+ String.valueOf(this.cargarConfig()));
		
	}
	
	
	private void cargarTeclasALista() {
		this.TECLAS.add(TECLA_INVENTARIO);
		this.TECLAS.add(TECLA_ESCAPE);
		this.TECLAS.add(TECLA_ARRIBA);
		this.TECLAS.add(TECLA_ABAJO);
		this.TECLAS.add(TECLA_IZQUIERDA);
		this.TECLAS.add(TECLA_DERECHA);
		this.TECLAS.add(TECLA_RECOGIENDO);
		this.TECLAS.add(TECLA_CORRIENDO);
		this.TECLAS.add(TECLA_DEBUG);
		this.TECLAS.add(TECLA_VER_COLISIONES);
		this.TECLAS.add(TECLA_DIJKSTRA);
		this.TECLAS.add(TECLA_DIJKSTRA_INFO);
		this.TECLAS.add(TECLA_GUARDAR_MAPA);
		this.TECLAS.add(TECLA_DEBUG_TILE);
		this.TECLAS.add(TECLA_DEBUG_TILE_INFO);
		this.TECLAS.add(TECLA_DEBUG_GROUP_TILE);
		this.TECLAS.add(TECLA_OCULTAR_TERRENO);
		this.TECLAS.add(TECLA_OCULTAR_COMPLEMENTOS);
		this.TECLAS.add(TECLA_VER_ALCANCE_ATAQUE);
		this.TECLAS.add(TECLA_ATACANDO);
		this.TECLAS.add(TECLA_PUNTO);
		this.TECLAS.add(TECLA_PAUSA);
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
	
	//COD DE PRUEBA
	public void actualizarEstadosTeclas() {
        System.arraycopy(teclas, 0, teclasPresionadasAnterior, 0, teclas.length);
    }
	
	
	 public boolean isTeclaPresionadaUnaVez(int codigoTecla) {
	        return teclas[codigoTecla] && !teclasPresionadasAnterior[codigoTecla];
	    }
	 
	 public boolean isTeclaPresionadaUnaVez(final Tecla tecla) {
	        return teclas[tecla.codigoTecla] && !teclasPresionadasAnterior[tecla.codigoTecla];
	    }
	//FIN COD PRUEBA
	


	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() >= 0 && e.getKeyCode() < teclas.length) {
			this.teclas[e.getKeyCode()] = true;
		}
		
		
		for(Tecla t : this.TECLAS) {
			if(t.getCodigoTecla()==e.getKeyCode()) {
				t.presionar();
			}
		}
		
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() >= 0 && e.getKeyCode() < teclas.length) {
			this.teclas[e.getKeyCode()] = false;
		}
		
		for(Tecla t : this.TECLAS) {
			if(t.getCodigoTecla()==e.getKeyCode() && !t.accionarInvertible) {
				t.soltar();
			}
		}
	}

	public boolean presionaTeclaEnLista(final int codigo) {
		if (codigo >= 0 && codigo < teclas.length) {
			return this.teclas[codigo];
		} else {
			return false;
		}
	}
	
	/**
	 * Obtiene todos los seteos del teclado en un json.
	 * en el json la clave seria el nombre de la tecla y
	 * el valor el codigo de la tecla.
	 * 
	 * @return El json de todas las teclas modificables.
	 */
	protected JSONObject getConfigJson() {
		final JSONObject jo = new JSONObject();
		for(Tecla t : this.TECLAS_MODIFICABLES.values()) {
			t.agregarEnJSON(jo);
		}
		return jo;
	}
	/**
	 * Establece a todas las teclas modificables los valores 
	 * pasados en el json. En el json la clave seria el nombre de la tecla y
	 * el valor el codigo de la tecla.
	 * @param jo El json con los valores de las teclas modificables a establecer.
	 */
	protected void establecerConfig(final JSONObject jo) {
		for(Tecla t : this.TECLAS_MODIFICABLES.values()) {
			if(jo.containsKey(t.nombre)) {
				t.establecerCodigoTecla(Integer.parseInt(jo.get(t.nombre).toString()));
			}
		}
	}
	
	/**
	 * Carga el archivo con la configuracion del teclado para las tecla modificables.
	 * Si no se puede cargar dejara la configuracion por defecto.
	 * 
	 * @return TRUE si se pudo carga la configuracion o FALSE si no se pudo.
	 */
	protected boolean cargarConfig() {
		if(!this.ARCHIVO_CONFIG.exists()) {
			return false;
		}
		FileReader fr = null;
		boolean exito = false;
		try {
			fr = new FileReader(ARCHIVO_CONFIG);
			int code;
			StringBuilder txt = new StringBuilder();
			while((code = fr.read()) != -1) {
				txt.append((char)code);
			}
			final JSONObject jo = (JSONObject) (new JSONParser()).parse(txt.toString());
			establecerConfig(jo);
			exito = true;
		} catch (Exception e) {
			exito = false;
		}finally {
			if(fr == null) {
				return false;
			}
			try {
				fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return exito;
	}
	
	/**
	 * Guarda el archivo con el json de la configuracion de las teclas modificables.
	 */
	public void guardarConfig() {
		final JSONObject jo = this.getConfigJson();
		FileWriter fw = null;
		try {
			fw = new FileWriter(ARCHIVO_CONFIG);
			fw.write(jo.toJSONString().replaceAll(",", ",\n"));
			System.out.println("Config guardada? True");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Config guardada? False");
		}finally {
			try {
				fw.flush();
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}

}
