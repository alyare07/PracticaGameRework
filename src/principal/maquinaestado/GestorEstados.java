package principal.maquinaestado;

import java.awt.Graphics2D;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import principal.mapa.Terreno;
import principal.maquinaestado.estados.EstadoJuego;
import principal.maquinaestado.estados.GestorPartida;
import principal.maquinaestado.estados.editor.EditorMapa;
import principal.maquinaestado.estados.menu.MenuConfiguracion;
import principal.maquinaestado.estados.menu.MenuConfiguracionEnPartida;
import principal.maquinaestado.estados.menu.MenuEdirorSeleccion;
import principal.maquinaestado.estados.menu.MenuEditorNuevo;
import principal.maquinaestado.estados.menu.MenuPrincipal;
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

	/**
	 * Estado nulo/vacío reutilizable para evitar instanciaciones efímeras en la
	 * recolección de basura
	 */
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
			this.estadoActual.pintar(g);
		}
	}

	public void actualizar() {
		if (this.estadoActual != null) {
			this.estadoActual.actualizar();
		}
	}

	public void establecerEstadoActual(final int numeroEstado) {
		// Sincronizar el flag global de estado de partida
		Globales.estadoJuego = (numeroEstado == NUMERO_ESTADO_PARTIDA);

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
			this.estadoActual = new MenuConfiguracion(this);
			break;
		case NUMERO_ESTADO_MENU_CONFIGURACIONES_EN_PARTIDA:
			this.estadoActual = new MenuConfiguracionEnPartida(this);
			break;
		default:
			break;
		}
	}

	public void editorMapaSeleccion() {
		Globales.estadoJuego = false;
		this.estados[2] = new MenuEdirorSeleccion(this);
		this.estadoActual = this.estados[2];
	}

	public void editorMapa(final int cantAncho, final int cantAlto, final int idModeloTile) {
		Globales.estadoJuego = false;
		this.estados[2] = new EditorMapa(Globales.CONSTANTES.LADO_TILE, cantAncho, cantAlto, idModeloTile,
				this);
		this.estadoActual = this.estados[2];
	}

	public void editorMapa(final Terreno mapa) {
		Globales.estadoJuego = false;
		this.estados[2] = new EditorMapa(mapa, this);
		this.estadoActual = this.estados[2];
	}

	public void editorMapaNuevoMenu() {
		Globales.estadoJuego = false;
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
		this.estados[0] = new GestorPartida(this);
		this.estadoActual = this.estados[0];
		Globales.GESTOR_INVENTARIO.getInventarioJugador().vaciar();
		Globales.GESTOR_INVENTARIO.getInventarioJugador().ocultar();
	}

	public void seleccionarMundo() {
		// Ruta compatible entre todos los sistemas operativos (Windows, Linux, macOS)
		final File directorio = new File("mundos");
		if (!directorio.exists()) {
			directorio.mkdirs();
		}

		final JFileChooser selector = new JFileChooser(directorio);
		final FileNameExtensionFilter filtro = new FileNameExtensionFilter("Mundos (*.mp, *.esc)", "mp", "esc");
		selector.setFileFilter(filtro);
		selector.setApproveButtonText("Seleccionar");

		final int resultado = selector.showOpenDialog(null);

		if (resultado == JFileChooser.APPROVE_OPTION) {
			final File seleccion = selector.getSelectedFile();
			if ((seleccion != null) && seleccion.exists()) {
				Globales.estadoJuego = true;
				this.estados[0] = new GestorPartida(this, seleccion.getAbsolutePath(), true);
				this.estadoActual = this.estados[0];
				Globales.GESTOR_INVENTARIO.getInventarioJugador().vaciar();
				Globales.GESTOR_INVENTARIO.getInventarioJugador().ocultar();
			}
		} else if (resultado == JFileChooser.ERROR_OPTION) {
			JOptionPane.showMessageDialog(null, "No se ha podido cargar el mundo seleccionado!",
					"Error al cargar mundo", JOptionPane.WARNING_MESSAGE);
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