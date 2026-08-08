package principal;

import principal.graficos.SuperficieDibujo;
import principal.graficos.Ventana;
import principal.maquinaestado.GestorEstados;
import principal.utilidades.Constantes;

/**
 * Núcleo principal del juego (Game Loop). Mantiene las actualizaciones lógicas
 * (APS) clavadas a 60 Hz constantes y permite FPS limitados (60 FPS) o
 * ilimitados mediante teclado.
 */
public class GestorPrincipal {

	// --- Constantes de Tiempo ---
	private static final long NS_POR_SEGUNDO = 1_000_000_000L;

	// Objetivo de Lógica: 60 APS constantes
	private static final byte APS_OBJETIVO = 60;
	private static final double NS_POR_ACTUALIZACION = (double) NS_POR_SEGUNDO / APS_OBJETIVO;

	// Objetivo de Gráficos cuando los FPS están LIMITADOS (60 FPS)
	private static final byte FPS_OBJETIVO_LIMITADO = 60;
	private static final double NS_POR_FRAME_LIMITADO = (double) NS_POR_SEGUNDO / FPS_OBJETIVO_LIMITADO;

	// --- Componentes Principales ---
	private GestorEstados gestorEstados;
	private SuperficieDibujo superficieDibujo;
	private Ventana ventana;

	// --- Estado del Motor ---
	private boolean enFuncionamiento;
	private int codActualizacion;

	// --- Métricas de Rendimiento ---
	private int actualizacionesAcumuladas = 0;
	private int framesAcumulados = 0;

	public GestorPrincipal() {
		this.codActualizacion = Integer.MIN_VALUE;
	}

	/**
	 * Inicializa los componentes principales e inicia el estado del motor.
	 */
	public void iniciarJuego() {
		this.enFuncionamiento = true;
		this.gestorEstados = new GestorEstados();
		this.superficieDibujo = SuperficieDibujo.obetenerSuperficieDibujo();
		this.ventana = new Ventana("Juego", this.superficieDibujo);
	}

	/**
	 * Game Loop Principal.
	 */
	public void iniciarBuclePrincipal() {
		long referenciaActualizacion = System.nanoTime();
		long referenciaContador = System.nanoTime();
		double tiempoTranscurrido;
		double delta = 0;

		while (this.enFuncionamiento) {
			final long inicioBucle = System.nanoTime();
			tiempoTranscurrido = inicioBucle - referenciaActualizacion;
			referenciaActualizacion = inicioBucle;

			// Acumulador de delta para clavar las actualizaciones a 60 Hz
			delta += tiempoTranscurrido / NS_POR_ACTUALIZACION;
			Constantes.GLOBALES.delta = tiempoTranscurrido / NS_POR_SEGUNDO;

			// --- 1. LÓGICA (APS clavados en 60) ---
			while (delta >= 1) {
				this.actualizar();
				delta--;
			}

			// --- 2. RENDERIZADO (FPS) ---
			this.pintar();

			// --- 3. CONTROL DE LÍMITE DE FPS ---
			final boolean fpsLimitados = Constantes.TECLADO.TECLA_FPS_LIMITE.presionado();

			if (fpsLimitados) {
				// Modo Limitado: Esperar el tiempo restante para no sobrepasar los 60 FPS
				final long tiempoFrame = System.nanoTime() - inicioBucle;
				final double tiempoRestanteNS = NS_POR_FRAME_LIMITADO - tiempoFrame;

				if (tiempoRestanteNS > 0) {
					try {
						final long msParaEsperar = (long) (tiempoRestanteNS / 1_000_000);
						final int nsResiduales = (int) (tiempoRestanteNS % 1_000_000);
						Thread.sleep(msParaEsperar, nsResiduales);
					} catch (final InterruptedException e) {
						e.printStackTrace();
					}
				}
			} else {
				// Modo Ilimitado: Cedemos una fracción microscópica al hilo para fluidez sin
				// estrangular el SO
				Thread.yield();
			}

			// --- 4. MÉTRICAS (Contador cada 1 segundo) ---
			if ((System.nanoTime() - referenciaContador) > NS_POR_SEGUNDO) {
				this.actualizarTiempoJugado();
				Constantes.GLOBALES.aps = this.actualizacionesAcumuladas;
				Constantes.GLOBALES.fps = this.framesAcumulados;

				this.actualizacionesAcumuladas = 0;
				this.framesAcumulados = 0;
				referenciaContador = System.nanoTime();
			}
		}
	}

	/**
	 * Procesa las actualizaciones lógicas del motor.
	 */
	private void actualizar() {
		Constantes.RATON.actualizar(this.superficieDibujo);
		Constantes.TECLADO.actualizarEstadosTeclas();
		this.gestorEstados.actualizar();
		Constantes.CAMARA.actualizar();

		this.siguienteAnimacion();
		this.actualizacionesAcumuladas++;
		this.actualizarCodActualizacion();
	}

	/**
	 * Dibuja los gráficos en la ventana.
	 */
	private void pintar() {
		this.superficieDibujo.pintar(this.gestorEstados);
		this.framesAcumulados++;
	}

	private void siguienteAnimacion() {
		if (Constantes.GLOBALES.animacion < Constantes.LIMITE_ANIMACION) {
			Constantes.GLOBALES.animacion++;
		} else {
			Constantes.GLOBALES.animacion = 0;
		}
	}

	private void actualizarTiempoJugado() {
		Constantes.GLOBALES.segundosJugados++;

		if (Constantes.GLOBALES.segundosJugados >= 60) {
			Constantes.GLOBALES.segundosJugados = 0;
			Constantes.GLOBALES.minutosJugados++;

			if (Constantes.GLOBALES.minutosJugados >= 60) {
				Constantes.GLOBALES.minutosJugados = 0;
				Constantes.GLOBALES.horasJugadas++;
			}
		}
	}

	private void actualizarCodActualizacion() {
		if (this.codActualizacion >= Integer.MAX_VALUE) {
			this.codActualizacion = Integer.MIN_VALUE;
		} else {
			this.codActualizacion++;
		}
	}

	// --- Getters ---

	public int getCodigoActualizacion() {
		return this.codActualizacion;
	}

	public GestorEstados getGestorEstados() {
		return this.gestorEstados;
	}

	public Ventana getVentana() {
		return this.ventana;
	}
}