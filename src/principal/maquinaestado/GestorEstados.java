package principal.maquinaestado;

import java.awt.Graphics2D;

import javax.swing.JOptionPane;

import org.json.simple.JSONObject;

import principal.mapa.Terreno;
import principal.mapa.escenario.Escenario;
import principal.maquinaestado.estados.EstadoJuego;
import principal.maquinaestado.estados.GestorPartida;
import principal.maquinaestado.estados.editor.EditorMapa;
import principal.maquinaestado.estados.menu.MenuCargarPartida;
import principal.maquinaestado.estados.menu.MenuConfiguracion;
import principal.maquinaestado.estados.menu.MenuConfiguracionEnPartida;
import principal.maquinaestado.estados.menu.MenuConfiguracionGrafica;
import principal.maquinaestado.estados.menu.MenuConfiguracionSeleccion;
import principal.maquinaestado.estados.menu.MenuEdirorSeleccion;
import principal.maquinaestado.estados.menu.MenuEditorNuevo;
import principal.maquinaestado.estados.menu.MenuPrincipal;
import principal.persistencia.GestorGuardado;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;

/**
 * Máquina de Estados Finita (FSM) que gestiona el ciclo de vida, transiciones,
 * actualización y renderizado del estado activo del juego.
 */
public class GestorEstados {

	public static final int NUMERO_ESTADO_PRUEBA = -1;
	public static final int NUMERO_ESTADO_PARTIDA = 0;
	public static final int NUMERO_ESTADO_MENU = 1;
	public static final int NUMERO_ESTADO_EDITOR_MAPA = 2;
	public static final int NUMERO_ESTADO_MENU_EDITOR_MAPA = 3;
	public static final int NUMERO_ESTADO_MENU_CONFIGURACIONES = 4;
	public static final int NUMERO_ESTADO_MENU_CONFIGURACIONES_EN_PARTIDA = 5;
	public static final int NUMERO_ESTADO_MENU_CONFIGURACIONES_GRAFICAS = 6;
	public static final int NUMERO_ESTADO_MENU_CONFIGURACIONES_CONTROLES = 7;
	public static final int NUMERO_ESTADO_MENU_CARGAR_PARTIDA = 8;

	private static final EstadoJuego ESTADO_VACIO = new EstadoJuego() {
		@Override
		public void pintar(final Graphics2D g) {
		}

		@Override
		public void actualizar() {
		}
	};

	private final EstadoJuego[] estados;
	private EstadoJuego estadoActual;

	public GestorEstados() {
		this.estados = new EstadoJuego[3];
		this.estados[1] = new MenuPrincipal(this);
		this.establecerEstadoActual(NUMERO_ESTADO_MENU);
	}

	public void pintar(final Graphics2D g) {
		if (this.estadoActual != null) {
			g.setFont(Globales.GESTOR_FUENTES.getFuente(12f));
			this.estadoActual.pintar(g);
		}
	}

	public void actualizar() {
		if (this.estadoActual != null) {
			this.estadoActual.actualizar();
		}
	}

	public void establecerEstadoActual(final int numeroEstado) {
		Globales.estadoJuego = (numeroEstado == NUMERO_ESTADO_PARTIDA);
		Globales.RATON.soltar();

		switch (numeroEstado) {
		case NUMERO_ESTADO_PARTIDA:
			this.estadoActual = this.estados[0];
			break;
		case NUMERO_ESTADO_MENU:
			if (this.estados.length > 1) {
				this.estadoActual = this.estados[1];
			}
			break;
		case NUMERO_ESTADO_EDITOR_MAPA:
			if (this.estados.length > 1) {
				this.editorMapaSeleccion();
			}
			break;
		case NUMERO_ESTADO_MENU_CONFIGURACIONES:
			this.estadoActual = new MenuConfiguracionSeleccion(this);
			break;
		case NUMERO_ESTADO_MENU_CONFIGURACIONES_GRAFICAS:
			this.estadoActual = new MenuConfiguracionGrafica(this);
			break;
		case NUMERO_ESTADO_MENU_CONFIGURACIONES_CONTROLES:
			this.estadoActual = new MenuConfiguracion(this);
			break;
		case NUMERO_ESTADO_MENU_CONFIGURACIONES_EN_PARTIDA:
			this.estadoActual = new MenuConfiguracionEnPartida(this);
			break;
		case NUMERO_ESTADO_MENU_CARGAR_PARTIDA:
			this.estadoActual = new MenuCargarPartida(this);
			break;
		default:
			break;
		}
	}

	public void editorMapaSeleccion() {
		Globales.estadoJuego = false;
		Globales.RATON.soltar();
		this.estados[2] = new MenuEdirorSeleccion(this);
		this.estadoActual = this.estados[2];
	}

	public void editorMapa(final int cantAncho, final int cantAlto, final principal.recursos.TipoTerreno tipoInicial) {
		Globales.estadoJuego = false;
		Globales.RATON.soltar();
		this.estados[2] = new EditorMapa(Constantes.LADO_TILE, cantAncho, cantAlto, tipoInicial, this);
		this.estadoActual = this.estados[2];
	}

	public void editorMapa(final Escenario esc) {
		Globales.estadoJuego = false;
		Globales.RATON.soltar();
		this.estados[2] = new EditorMapa(esc, this);
		this.estadoActual = this.estados[2];
	}

	public void editorMapa(final Terreno mapa) {
		Globales.estadoJuego = false;
		Globales.RATON.soltar();
		this.estados[2] = new EditorMapa(mapa, this);
		this.estadoActual = this.estados[2];
	}

	@Deprecated
	public void editorMapa(final int cantAncho, final int cantAlto, final int idModeloTile) {
		this.editorMapa(cantAncho, cantAlto, principal.recursos.TipoTerreno.TIERRA);
	}

	public void editorMapaNuevoMenu() {
		Globales.estadoJuego = false;
		Globales.RATON.soltar();
		this.estados[2] = new MenuEditorNuevo(this);
		this.estadoActual = this.estados[2];
	}

	public void disposeEditor() {
		this.estados[2] = ESTADO_VACIO;
		if (this.estadoActual == this.estados[2]) {
			this.establecerEstadoActual(NUMERO_ESTADO_MENU);
		}
	}

	public void iniciarPartidaNueva() {
		Globales.estadoJuego = true;
		Globales.RATON.soltar();
		this.estados[0] = new GestorPartida(this);
		this.estadoActual = this.estados[0];
		Globales.GESTOR_INVENTARIO.getInventarioJugador().vaciar();
		Globales.GESTOR_INVENTARIO.getInventarioJugador().ocultar();
	}

	public void iniciarPartidaGuardada(final JSONObject saveJson) {
		if (saveJson == null) {
			return;
		}
		Globales.estadoJuego = true;
		Globales.RATON.soltar();
		// Constructor directo y limpio sin cargas iniciales en conflicto
		final GestorPartida gp = new GestorPartida(this, saveJson);
		this.estados[0] = gp;
		this.estadoActual = gp;
	}

	public void cargarPartidaSlot(final int slot) {
		final JSONObject saveJson = GestorGuardado.leerJsonGuardado(slot);
		if (saveJson != null) {
			this.iniciarPartidaGuardada(saveJson);
		} else {
			JOptionPane.showMessageDialog(null, "No existe archivo de guardado en el Slot " + slot,
					"Partida no encontrada", JOptionPane.WARNING_MESSAGE);
		}
	}

	public void disposePartida() {
		this.estados[0] = ESTADO_VACIO;
		if (this.estadoActual == this.estados[0]) {
			this.establecerEstadoActual(NUMERO_ESTADO_MENU);
		}
	}

	public EstadoJuego getEstadoActual() {
		return this.estadoActual;
	}
}