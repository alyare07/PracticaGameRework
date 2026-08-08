package principal.maquinaestado;

import java.awt.Graphics2D;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import principal.mapa.Terreno;
import principal.maquinaestado.estados.EstadoJuego;
import principal.maquinaestado.estados.EstadoPrueba;
import principal.maquinaestado.estados.GestorPartida;
import principal.maquinaestado.estados.editor.EditorMapa;
import principal.maquinaestado.estados.menu.MenuConfiguracion;
import principal.maquinaestado.estados.menu.MenuConfiguracionEnPartida;
import principal.maquinaestado.estados.menu.MenuEdirorSeleccion;
import principal.maquinaestado.estados.menu.MenuEditorNuevo;
import principal.maquinaestado.estados.menu.MenuPrincipal;
import principal.utilidades.Constantes;

public class GestorEstados{
    public static final int NUMERO_ESTADO_PRUEBA = -1;
    public static final int NUMERO_ESTADO_PARTIDA = 0;
    public static final int NUMERO_ESTADO_MENU = 1;
    public static final int NUMERO_ESTADO_EDITOR_MAPA = 2;
    public static final int NUMERO_ESTADO_MENU_EDITOR_MAPA = 3;
    public static final int NUMERO_ESTADO_MENU_CONFIGURACIONES = 4;
    public static final int NUMERO_ESTADO_MENU_CONFIGURACIONES_EN_PARTIDA = 5;

    private final EstadoJuego[] estados;
    private EstadoJuego estadoActual;

    public GestorEstados() {
	this.estados = new EstadoJuego[3];
	this.estados[1] = new MenuPrincipal(this);

	this.establecerEstadoActual(NUMERO_ESTADO_MENU);
//		establecerEstadoActual(NUMERO_ESTADO_PRUEBA);
    }

    public void pintar(final Graphics2D g) {
	this.estadoActual.pintar(g);
    }

    public void actualizar() {
	this.estadoActual.actualizar();
    }

    public void establecerEstadoActual(final int numeroEstado) {
	if (numeroEstado == NUMERO_ESTADO_PARTIDA) {

	} else {
	    Constantes.GLOBALES.estadoJuego = false;
	}
	switch (numeroEstado) {
	case NUMERO_ESTADO_PARTIDA:
	    Constantes.GLOBALES.estadoJuego = true;
	    this.estadoActual = this.estados[0];
	    break;
	case NUMERO_ESTADO_MENU:
	    if (this.estados.length <= 1) {
		break;
	    }
	    Constantes.GLOBALES.estadoJuego = false;
	    this.estadoActual = this.estados[1];
	    break;
	case NUMERO_ESTADO_EDITOR_MAPA:
	    if (this.estados.length <= 1) {
		break;
	    }
	    this.editorMapaSeleccion();
	    break;
	case NUMERO_ESTADO_MENU_CONFIGURACIONES:
	    this.estadoActual = new MenuConfiguracion(this);
	    break;
	case NUMERO_ESTADO_MENU_CONFIGURACIONES_EN_PARTIDA:
	    this.estadoActual = new MenuConfiguracionEnPartida(this);
	    break;
	case NUMERO_ESTADO_PRUEBA:
//			this.estadoActual = new EstadoPruebaVacio();
	    this.estadoActual = new EstadoPrueba();
	    break;
	}

    }

    public void editorMapaSeleccion() {
	Constantes.GLOBALES.estadoJuego = false;
	this.estados[2] = new MenuEdirorSeleccion(this);
	this.estadoActual = this.estados[2];
    }

    public void editorMapa(final int cantAncho, final int cantAlto, final int idModeloTile) {
	Constantes.GLOBALES.estadoJuego = false;
	this.estados[2] = new EditorMapa(Constantes.LADO_TILE, cantAncho, cantAlto, idModeloTile, this);
	this.estadoActual = this.estados[2];
    }

    public void editorMapa(final Terreno mapa) {
	Constantes.GLOBALES.estadoJuego = false;
	this.estados[2] = new EditorMapa(mapa, this);
	this.estadoActual = this.estados[2];
    }

    public void editorMapaNuevoMenu() {
	Constantes.GLOBALES.estadoJuego = false;
	this.estados[2] = new MenuEditorNuevo(this);
	this.estadoActual = this.estados[2];
    }

    public void disposeEditor() {
	this.estados[2] = new EstadoJuego() {

	    @Override
	    public void pintar(final Graphics2D g) {
	    }

	    @Override
	    public void actualizar() {
		System.out.println("Editor ANONIMO");
	    }
	};
    }

    public void iniciarPartidaNueva() {
	Constantes.GLOBALES.estadoJuego = true;
	this.estados[0] = new GestorPartida(this);
	this.estadoActual = this.estados[0];
	Constantes.INVENTARIO.vaciar();
	Constantes.INVENTARIO.ocultar();
    }

    public void seleccionarMundo() {
	final File directorio = new File("mundos\\");
	if (!directorio.exists()) {
	    directorio.mkdirs();
	}

	final JFileChooser selector = new JFileChooser();
	selector.setCurrentDirectory(new File(directorio.getAbsolutePath()));
	final FileNameExtensionFilter filtro = new FileNameExtensionFilter("Mundos", "mp", "esc");
	selector.setFileFilter(filtro);

	selector.setApproveButtonText("Seleccionar");

	if (selector.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
	    final File seleccion = selector.getSelectedFile();
	    if (seleccion != null && seleccion.exists()) {
		Constantes.GLOBALES.estadoJuego = true;
		this.estados[0] = new GestorPartida(this, seleccion.getAbsolutePath(), true);
		this.estadoActual = this.estados[0];
		Constantes.INVENTARIO.vaciar();
		Constantes.INVENTARIO.ocultar();
	    }
	} else {
	    JOptionPane.showMessageDialog(selector, "No se ha podido cargar el mundo seleccionado!", "Error al cargar mundo", JOptionPane.WARNING_MESSAGE);
	}

    }

    public void disposePartida() {
	this.estados[0] = new EstadoJuego() {

	    @Override
	    public void pintar(final Graphics2D g) {
	    }

	    @Override
	    public void actualizar() {
		System.out.println("Partida ANONIMA");
	    }
	};
    }

    public EstadoJuego getEstadoActual() {
	return this.estadoActual;
    }

}
