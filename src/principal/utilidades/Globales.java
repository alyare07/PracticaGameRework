package principal.utilidades;

import principal.Main;
import principal.animaciones.listaHojasSprite.ListaHojaSprites;
import principal.controles.Raton;
import principal.controles.Teclado;
import principal.entes.criaturas.Jugador;
import principal.inventario.vault.InventarioVault;
import principal.mapa.renderEntidades.camara.Camara;
import principal.maquinaestado.estados.GestorJuego;
import principal.maquinaestado.estados.GestorPartida;
import principal.maquinaestado.estados.editor.EditorMapa;
import principal.utilidades.funciones.Funciones;
import principal.utilidades.inventario.GestorInventario;

public class Globales {

	public static int fps;
	public static int aps;
	public static boolean pausa;
	public static boolean debug;
	public static int animacion;
	public static boolean estadoJuego;
	public static int horasJugadas;
	public static int minutosJugados;
	public static int segundosJugados;
	public static double delta;
	public static boolean partidaIniciada;
	public static boolean editorSelectGroupTile;
	public static boolean viendoContenedor;
	public static InventarioVault inventarioVault;

	public static final Constantes CONSTANTES = new Constantes();
	public static double FACTOR_ESCALADO_X = CONSTANTES.ANCHO_PANTALLA_COMPLETA / (double) CONSTANTES.ANCHO_JUEGO;
	public static double FACTOR_ESCALADO_Y = CONSTANTES.ALTO_PANTALLA_COMPLETA / (double) CONSTANTES.ALTO_JUEGO;
	public static final GestorTiempo TECLEO_RECOGIDA = new GestorTiempo();
	public static final Teclado TECLADO = new Teclado();
	public static final Raton RATON = new Raton();
	public static final Funciones FUNCIONES = new Funciones();
	public static final Jugador JUGADOR = new Jugador(0, 0);
	public static final GestorInventario GESTOR_INVENTARIO = new GestorInventario();
	public static final HojaSprite HOJA_JUGADORES = new HojaSprite("/imagenes/sprites/jugadores.png", 32, false);
	public static final int LADO_CURSOR = 4;
	public static final ListaHojaSprites LISTA_HOJAS_SPRITES = new ListaHojaSprites();
	public static Camara CAMARA = new Camara(JUGADOR);

	public static void actualizarFactorEscalado() {
		FACTOR_ESCALADO_X = CONSTANTES.ANCHO_PANTALLA_COMPLETA / (double) CONSTANTES.ANCHO_JUEGO;
		FACTOR_ESCALADO_Y = CONSTANTES.ALTO_PANTALLA_COMPLETA / (double) CONSTANTES.ALTO_JUEGO;
	}

	/**
	 * Codigo de la actualizacion del momento.
	 * 
	 * @return El codigo de la actualizacion.
	 */
	public static int getCodActualizacion() {
		return Main.gp.getCodigoActualizacion();
	}

	/**
	 * Verifica si el estado del juego es {@link EditorMapa}.
	 * 
	 * @return TRUE si el estado es {@link EditorMapa} FALSE si no lo es.
	 */
	public static boolean isEstadoEditor() {
		if (Main.gp.getGestorEstados() == null) {
			return false;
		}
		return Main.gp.getGestorEstados().getEstadoActual() instanceof EditorMapa;
	}

	/**
	 * Verifica si el estado del juego es {@link GestorJuego}.
	 * 
	 * @return TRUE si el estado es {@link GestorJuego} o FALSE si no lo es.
	 */
	public static boolean isEstadoJuego() {
		if (Main.gp.getGestorEstados() == null) {
			return false;
		}
		if (Main.gp.getGestorEstados().getEstadoActual() instanceof GestorPartida) {
			return ((GestorPartida) Main.gp.getGestorEstados().getEstadoActual())
					.getEstadoActivo() instanceof GestorJuego;
		}
		return false;
	}

	/**
	 * Calcula el valor para una coordena X teniendo en cuenta el desplazamiento de
	 * la {@link Camara}.
	 * 
	 * @param x El valor de la coordenada X a calcular.
	 * @return El valor de X desplazado segun la {@link Camara}.
	 */
	public static int getXDesplazamientoCamara(final int x) {
		return (x - CAMARA.getPosicionXInt()) + CAMARA.getMargenX();
	}

	/**
	 * Calcula el valor para una coordena Y teniendo en cuenta el desplazamiento de
	 * la {@link Camara}.
	 * 
	 * @param y El valor de la coordenada Y a calcular.
	 * @return El valor de Y desplazado segun la {@link Camara}.
	 */
	public static int getYDesplazamientoCamara(final int y) {
		return (y - CAMARA.getPosicionYInt()) + CAMARA.getMargenY();
	}

}
