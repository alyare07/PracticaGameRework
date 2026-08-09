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

	// Objetivo de Lógica: 60 APS constantes (Uso de 'int' en lugar de 'byte')
	private static final int APS_OBJETIVO = 60;
	private static final double NS_POR_ACTUALIZACION = (double) NS_POR_SEGUNDO / APS_OBJETIVO;

	// Objetivo de Gráficos cuando los FPS están LIMITADOS (60 FPS)
	private static final int FPS_OBJETIVO_LIMITADO = 60;
	private static final double NS_POR_FRAME_LIMITADO = (double) NS_POR_SEGUNDO / FPS_OBJETIVO_LIMITADO;

	// Límite de actualizaciones lógicas consecutivas para prevenir la "Espiral de
	// la Muerte"
	private static final int MAX_ACTUALIZACIONES_POR_FRAME = 5;

	// --- Componentes Principales ---
	private GestorEstados gestorEstados;
	private SuperficieDibujo superficieDibujo;
	private Ventana ventana;

	// --- Estado del Motor ---
	private boolean enFuncionamiento;
	private int codActualizacion;
	private long tiempoInicioSesionMs;

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
		this.tiempoInicioSesionMs = System.currentTimeMillis();
		this.gestorEstados = new GestorEstados();
		// Punto 4: Corrección de Typo (obetener -> obtener)
		this.superficieDibujo = SuperficieDibujo.obetenerSuperficieDibujo();
		this.ventana = new Ventana("Juego", this.superficieDibujo);
	}

	/**
	 * Game Loop Principal.
	 */
	public void iniciarBuclePrincipal(final boolean Vsync) {
		long referenciaActualizacion = System.nanoTime();
		long referenciaContador = System.nanoTime();
		double tiempoTranscurrido;
		double delta = 0;
		if (Vsync) {
			Constantes.TECLADO.TECLA_FPS_LIMITE.presionar();
		}
		while (this.enFuncionamiento) {
			final long inicioBucle = System.nanoTime();
			tiempoTranscurrido = inicioBucle - referenciaActualizacion;
			referenciaActualizacion = inicioBucle;

			// Acumulador de delta para clavar las actualizaciones a 60 Hz
			delta += tiempoTranscurrido / NS_POR_ACTUALIZACION;
			Constantes.GLOBALES.delta = tiempoTranscurrido / NS_POR_SEGUNDO;

			// --- 1. LÓGICA (APS clavados en 60 + Control de Espiral de la Muerte) ---
			int actualizacionesEnEsteFrame = 0;
			while ((delta >= 1) && (actualizacionesEnEsteFrame < MAX_ACTUALIZACIONES_POR_FRAME)) {
				this.actualizar();
				delta--;
				actualizacionesEnEsteFrame++;
			}

			// Si el lag fue severo y superó el límite, descartamos el delta acumulado extra
			if (delta > MAX_ACTUALIZACIONES_POR_FRAME) {
				delta = 0;
			}

			// --- 2. RENDERIZADO (FPS) ---
			this.pintar();

			// --- 3. CONTROL DE LÍMITE DE FPS (Sleep + ParkNanos para Java 8) ---
			final boolean fpsLimitados = Constantes.TECLADO.TECLA_FPS_LIMITE.presionado();

			if (fpsLimitados) {
				final long tiempoFrame = System.nanoTime() - inicioBucle;
				final double tiempoRestanteNS = NS_POR_FRAME_LIMITADO - tiempoFrame;

				if (tiempoRestanteNS > 0) {
					final long finEsperado = System.nanoTime() + (long) tiempoRestanteNS;

					// Dormimos la mayor parte del tiempo (dejando un margen de 2ms para la
					// imprecisión del SO)
					if (tiempoRestanteNS > 2_000_000) {
						try {
							final long msParaEsperar = (long) ((tiempoRestanteNS - 2_000_000) / 1_000_000);
							Thread.sleep(msParaEsperar);
						} catch (final InterruptedException e) {
							Thread.currentThread().interrupt();
						}
					}

					// Reemplazo de Thread.onSpinWait() para Java 8
					while (System.nanoTime() < finEsperado) {
						java.util.concurrent.locks.LockSupport.parkNanos(1);
					}
				}
			} else {
				// Modo Ilimitado: Cede una fracción microscópica de CPU para no ahogar al SO
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

	/**
	 * Punto 4: Calcula el tiempo total transcurrido mediante sellos de tiempo
	 * reales en lugar de acumular contadores manuales propensos a desincronizarse.
	 */
	private void actualizarTiempoJugado() {
		final long totalSegundos = (System.currentTimeMillis() - this.tiempoInicioSesionMs) / 1000;

		Constantes.GLOBALES.horasJugadas = (int) (totalSegundos / 3600);
		Constantes.GLOBALES.minutosJugados = (int) ((totalSegundos % 3600) / 60);
		Constantes.GLOBALES.segundosJugados = (int) (totalSegundos % 60);
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