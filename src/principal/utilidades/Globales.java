package principal.utilidades;

import java.awt.Toolkit;

import principal.Main;
import principal.clima.GestorClima;
import principal.clima.GestorTermicoJugador;
import principal.clima.GestorZonasAmbiente;
import principal.comandos.GestorComandos;
import principal.configuracion.ConfiguracionGrafica;
import principal.construccion.GestorConstruccion;
import principal.controles.Raton;
import principal.controles.Teclado;
import principal.crafteo.GestorCrafteo;
import principal.dialogos.GestorDialogos;
import principal.entes.criaturas.Jugador;
import principal.eventos.GestorEventos;
import principal.igu.MotorIGU;
import principal.igu.textos.GestorTextosFlotantes;
import principal.iluminacion.GestorLuz;
import principal.interaccion.GestorInteraccion;
import principal.inventario.vault.InventarioVault;
import principal.mapa.persistencia.GestorDeltasMundo;
import principal.mapa.renderEntidades.camara.Camara;
import principal.maquinaestado.estados.GestorJuego;
import principal.maquinaestado.estados.GestorPartida;
import principal.maquinaestado.estados.editor.EditorMapa;
import principal.particulas.GestorParticulas;
import principal.recursos.GestorTexturas;
import principal.utilidades.funciones.Funciones;
import principal.utilidades.inventario.GestorInventario;
import principal.utilidades.progreso.GestorProgreso;

public class Globales {

	// =========================================================================
	// === 1. ESTADOS DEL MOTOR Y TELEMETRÍA
	// =========================================================================
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

	// =========================================================================
	// === 2. RESOLUCIÓN, ESCALADO Y OFFSETS DE CENTRADO (ZERO-GC)
	// =========================================================================
	public static int ANCHO_PANTALLA_COMPLETA = Toolkit.getDefaultToolkit().getScreenSize().width;
	public static int ALTO_PANTALLA_COMPLETA = Toolkit.getDefaultToolkit().getScreenSize().height;
	public static double FACTOR_ESCALADO_X = ANCHO_PANTALLA_COMPLETA / (double) (Constantes.ANCHO_JUEGO);
	public static double FACTOR_ESCALADO_Y = ALTO_PANTALLA_COMPLETA / (double) Constantes.ALTO_JUEGO;

	public static int DESPLAZAMIENTO_X = 0;
	public static int DESPLAZAMIENTO_Y = 0;

	// =========================================================================
	// === 3. SUBSISTEMAS BÁSICOS DE ENTRADA Y UTILIDADES (BOOTSTRAP FASE 1)
	// =========================================================================
	public static final GestorTiempo TECLEO_RECOGIDA = new GestorTiempo();
	public static final Teclado TECLADO = new Teclado();
	public static final Raton RATON = new Raton();
	public static final Funciones FUNCIONES = new Funciones();
	public static final GestorFuentes GESTOR_FUENTES = new GestorFuentes();

	// =========================================================================
	// === 4. SUBSISTEMA DE RECURSOS Y VRAM (DEBE ESTAR ANTES DE LOS ENTES)
	// =========================================================================
	public static final GestorTexturas GESTOR_TEXTURAS = new GestorTexturas();

	// =========================================================================
	// === 5. ENTES, INTERFAZ Y CÁMARA (BOOTSTRAP FASE 2)
	// =========================================================================
	public static final Jugador JUGADOR = new Jugador(0, 0);
	public static final MotorIGU MOTOR_IGU = new MotorIGU();
	public static final GestorInventario GESTOR_INVENTARIO = new GestorInventario();
	public static final int LADO_CURSOR = 4;
	public static Camara CAMARA = new Camara(JUGADOR);

	// =========================================================================
	// === 6. SUBSISTEMAS DE PARTÍCULAS, LUZ, CLIMA, IA Y TERMODINÁMICA
	// =========================================================================
	public static final GestorTextosFlotantes GESTOR_TEXTOS = new GestorTextosFlotantes();
	public static final GestorParticulas GESTOR_PARTICULAS = new GestorParticulas();
	public static final GestorLuz GESTOR_LUZ = new GestorLuz();
	public static final GestorClima GESTOR_CLIMA = new GestorClima();
	public static final GestorTermicoJugador GESTOR_TERMICO_JUGADOR = new GestorTermicoJugador();
	public static final GestorZonasAmbiente GESTOR_ZONAS_AMBIENTE = new GestorZonasAmbiente();
	public static final GestorComandos GESTOR_COMANDOS = new GestorComandos();
	public static final GestorConstruccion GESTOR_CONSTRUCCION = new GestorConstruccion();
	public static final GestorCrafteo GESTOR_CRAFTEO = new GestorCrafteo();
	public static final GestorDeltasMundo GESTOR_DELTAS = new GestorDeltasMundo();
	public static final GestorProgreso GESTOR_PROGRESO = new GestorProgreso();
	public static final GestorInteraccion GESTOR_INTERACCION = new GestorInteraccion();
	public static final GestorDialogos GESTOR_DIALOGOS = new GestorDialogos();
	public static final GestorEventos GESTOR_EVENTOS = new GestorEventos();

	// =========================================================================
	// === MÉTODOS DE ESCALADO Y CONSULTA
	// =========================================================================

	public static void actualizarFactorEscalado() {
		ConfiguracionGrafica.recalcularEscaladoYOffsets();
	}

	public static int getCodActualizacion() {
		return (Main.gp != null) ? Main.gp.getCodigoActualizacion() : 0;
	}

	public static boolean isEstadoEditor() {
		if ((Main.gp == null) || (Main.gp.getGestorEstados() == null)) {
			return false;
		}
		return Main.gp.getGestorEstados().getEstadoActual() instanceof EditorMapa;
	}

	public static boolean isEstadoJuego() {
		if ((Main.gp == null) || (Main.gp.getGestorEstados() == null)) {
			return false;
		}
		if (Main.gp.getGestorEstados().getEstadoActual() instanceof GestorPartida) {
			return ((GestorPartida) Main.gp.getGestorEstados().getEstadoActual())
					.getEstadoActivo() instanceof GestorJuego;
		}
		return false;
	}

	public static int getXDesplazamientoCamara(final int x) {
		return (CAMARA != null) ? (x - CAMARA.getPosicionXInt()) + CAMARA.getMargenX() : x;
	}

	public static int getYDesplazamientoCamara(final int y) {
		return (CAMARA != null) ? (y - CAMARA.getPosicionYInt()) + CAMARA.getMargenY() : y;
	}
}