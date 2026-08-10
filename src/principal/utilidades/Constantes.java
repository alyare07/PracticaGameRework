package principal.utilidades;

import java.awt.Toolkit;

import principal.Main;
import principal.animaciones.listaHojasSprite.ListaHojaSprites;
import principal.controles.Raton;
import principal.controles.Teclado;
import principal.entes.criaturas.Jugador;
import principal.inventario.Inventario;
import principal.mapa.renderEntidades.camara.Camara;
import principal.maquinaestado.estados.GestorJuego;
import principal.maquinaestado.estados.GestorPartida;
import principal.maquinaestado.estados.editor.EditorMapa;
import principal.utilidades.funciones.Funciones;

public class Constantes {
	public static final int ANCHO_JUEGO = 640;
	public static final int ALTO_JUEGO = 360;
	public static int LADO_TILE = 16;
//    public static int ANCHO_PANTALLA_COMPLETA = 1600;
//    public static int ALTO_PANTALLA_COMPLETA = 900;
//	public static int ANCHO_PANTALLA_COMPLETA = 640*2;
//	public static int ALTO_PANTALLA_COMPLETA = 360*2;
//	public static int ANCHO_PANTALLA_COMPLETA = 1920;
//	public static int ALTO_PANTALLA_COMPLETA = 1080;d
	public static int ANCHO_PANTALLA_COMPLETA = Toolkit.getDefaultToolkit().getScreenSize().width;
	public static int ALTO_PANTALLA_COMPLETA = Toolkit.getDefaultToolkit().getScreenSize().height;
	public static double FACTOR_ESCALADO_X = (double) ANCHO_PANTALLA_COMPLETA / (double) ANCHO_JUEGO;
	public static double FACTOR_ESCALADO_Y = (double) ALTO_PANTALLA_COMPLETA / (double) ALTO_JUEGO;

	public static final double RADIO_AUDIO_DISTANCIA_MAXIMA = Math.hypot(ANCHO_JUEGO, ALTO_JUEGO) * 0.75;
	public static final float TAMANO_FUENTE = 9f;
	public static final int LIMITE_ANIMACION = 32767;
	public static final int CENTROX = ANCHO_JUEGO / 2;
	public static final int CENTROY = ALTO_JUEGO / 2;
	public static final GlobalesDinamicas GLOBALES = new GlobalesDinamicas();
	public static final GestorTiempo TECLEO_RECOGIDA = new GestorTiempo();
	public static final Teclado TECLADO = new Teclado();
	public static final Raton RATON = new Raton();
	public static final Funciones FUNCIONES = new Funciones();
//	public static final Mapa MAPA = GLOBALES.mapa;
	public static final Jugador JUGADOR = new Jugador(0, 0);
	public static final Inventario INVENTARIO = new Inventario();
//	public static final String RUTA_TEXTURA = "/imagenes/texturas/textura.png";
	public static final HojaSprite HOJA_JUGADORES = new HojaSprite("/imagenes/sprites/jugadores.png", 32, false);
	public static final int LADO_CURSOR = 4;
	public static final ListaHojaSprites LISTA_HOJAS_SPRITES = new ListaHojaSprites();
	public static Camara CAMARA = new Camara(JUGADOR);

	public static void actualizarFactorEscalado() {
		FACTOR_ESCALADO_X = (double) ANCHO_PANTALLA_COMPLETA / (double) ANCHO_JUEGO;
		FACTOR_ESCALADO_Y = (double) ALTO_PANTALLA_COMPLETA / (double) ALTO_JUEGO;
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
