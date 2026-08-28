package principal;

import java.util.concurrent.locks.LockSupport;

import principal.comandos.ComandoAmbiente;
import principal.comandos.ComandoClima;
import principal.comandos.ComandoCurar;
import principal.comandos.ComandoHora;
import principal.comandos.ComandoKillAll;
import principal.comandos.ComandoLuz;
import principal.comandos.ComandoLuzMundo;
import principal.comandos.ComandoSigilo;
import principal.comandos.ComandoTeleport;
import principal.comandos.ComandoVelocidad;
import principal.graficos.SuperficieDibujo;
import principal.graficos.Ventana;
import principal.maquinaestado.GestorEstados;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.audio.musica.GestorMusica;
import principal.utilidades.audio.sonido.GestorSonido;

/**
 * Núcleo principal del juego (Game Loop). Mantiene las actualizaciones lógicas
 * (APS) clavadas a 60 Hz constantes y permite FPS limitados (60 FPS) o
 * ilimitados mediante teclado.
 */
public class GestorPrincipal {

	// --- Constantes de Tiempo ---
	private static final long NS_POR_SEGUNDO = 1_000_000_000L;

	// Objetivo de Lógica: 60 APS constantes
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
		GestorSonido.cargarSonidosDesdeJSON("sonidos/Sonidos.json");
		GestorMusica.cargarMusicasDesdeJSON("sonidos/Musicas.json");
	}

	/**
	 * Inicializa los componentes principales e inicia el estado del motor.
	 */
	public void iniciarJuego() {
		this.enFuncionamiento = true;
		this.tiempoInicioSesionMs = System.currentTimeMillis();
		this.gestorEstados = new GestorEstados();
		this.superficieDibujo = SuperficieDibujo.obetenerSuperficieDibujo();
		this.ventana = new Ventana("Juego", this.superficieDibujo);

		// --- Registro de Comandos de Consola ---
		this.registrarComandos();

		// Inicia el hilo en segundo plano
		Globales.GESTOR_COMANDOS.iniciarEscuchaConsola();
	}

	/**
	 * Game Loop Principal.
	 */
	public void iniciarBuclePrincipal(final boolean Vsync) {
		long referenciaActualizacion = System.nanoTime();
		long referenciaContador = System.nanoTime();
		double tiempoTranscurrido;
		double delta = 0;

		// El delta para físicas/movimiento lógicas a 60 Hz siempre es 1/60 de segundo
		Globales.delta = 1.0 / APS_OBJETIVO;

		// Si se solicita VSync y el límite no está activo, se activa
		if (Vsync && !Globales.TECLADO.TECLA_FPS_LIMITE.presionado()) {
			Globales.TECLADO.TECLA_FPS_LIMITE.presionar();
		}

		while (this.enFuncionamiento) {
			final long inicioBucle = System.nanoTime();
			tiempoTranscurrido = inicioBucle - referenciaActualizacion;
			referenciaActualizacion = inicioBucle;

			// Acumulador de delta para clavar las actualizaciones a 60 Hz
			delta += tiempoTranscurrido / NS_POR_ACTUALIZACION;

			// --- 1. LÓGICA (APS clavados en 60 + Control de Espiral de la Muerte) ---
			int actualizacionesEnEsteFrame = 0;
			while ((delta >= 1.0) && (actualizacionesEnEsteFrame < MAX_ACTUALIZACIONES_POR_FRAME)) {
				this.actualizar();
				delta--;
				actualizacionesEnEsteFrame++;
			}

			// Si el lag fue severo y alcanzó el límite máximo, descartamos el retraso
			// acumulado
			if (actualizacionesEnEsteFrame >= MAX_ACTUALIZACIONES_POR_FRAME) {
				delta = 0;
			}

			// --- 2. RENDERIZADO (FPS) ---
			this.pintar();

			// --- 3. CONTROL DE LÍMITE DE FPS (Sleep + ParkNanos para Java 8) ---
			final boolean fpsLimitados = Globales.TECLADO.TECLA_FPS_LIMITE.presionado();

			if (fpsLimitados) {
				final long tiempoFrame = System.nanoTime() - inicioBucle;
				final double tiempoRestanteNS = NS_POR_FRAME_LIMITADO - tiempoFrame;

				if (tiempoRestanteNS > 0) {
					final long finEsperado = System.nanoTime() + (long) tiempoRestanteNS;

					// Dormimos la mayor parte del tiempo (margen de 2ms para la imprecisión del SO)
					if (tiempoRestanteNS > 2_000_000) {
						try {
							final long msParaEsperar = (long) ((tiempoRestanteNS - 2_000_000) / 1_000_000);
							Thread.sleep(msParaEsperar);
						} catch (final InterruptedException e) {
							Thread.currentThread().interrupt();
						}
					}

					// Espera activa de alta precisión con LockSupport
					while (System.nanoTime() < finEsperado) {
						LockSupport.parkNanos(1);
					}
				}
			} else {
				// Modo Ilimitado: Cede una fracción microscópica de CPU para no ahogar al SO
				Thread.yield();
			}

			// --- 4. MÉTRICAS (Contador cada 1 segundo exacto sin deriva temporal) ---
			if ((inicioBucle - referenciaContador) >= NS_POR_SEGUNDO) {
				this.actualizarTiempoJugado();
				Globales.aps = this.actualizacionesAcumuladas;
				Globales.fps = this.framesAcumulados;

				this.actualizacionesAcumuladas = 0;
				this.framesAcumulados = 0;
				referenciaContador += NS_POR_SEGUNDO;
			}
		}
	}

	/**
	 * Procesa las actualizaciones lógicas del motor.
	 */
	private void actualizar() {
		Globales.GESTOR_COMANDOS.actualizar(); // <-- Procesa comandos pendientes de forma segura
		Globales.RATON.actualizar(this.superficieDibujo);
		Globales.TECLADO.actualizar();
		this.gestorEstados.actualizar();
		Globales.CAMARA.actualizar();

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
		if (Globales.animacion < Constantes.LIMITE_ANIMACION) {
			Globales.animacion++;
		} else {
			Globales.animacion = 0;
		}
	}

	/**
	 * Calcula el tiempo total transcurrido mediante sellos de tiempo reales.
	 */
	private void actualizarTiempoJugado() {
		final long totalSegundos = (System.currentTimeMillis() - this.tiempoInicioSesionMs) / 1000;

		Globales.horasJugadas = (int) (totalSegundos / 3600);
		Globales.minutosJugados = (int) ((totalSegundos % 3600) / 60);
		Globales.segundosJugados = (int) (totalSegundos % 60);
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

	private void registrarComandos() {
		// =====================================================================
		// === REGISTRO DE COMANDOS DE DESARROLLADOR
		// =====================================================================
		Globales.GESTOR_COMANDOS.registrarComando(new ComandoCurar());
		Globales.GESTOR_COMANDOS.registrarComando(new ComandoClima());
		Globales.GESTOR_COMANDOS.registrarComando(new ComandoTeleport());
		Globales.GESTOR_COMANDOS.registrarComando(new ComandoHora());
		Globales.GESTOR_COMANDOS.registrarComando(new ComandoLuz());
		Globales.GESTOR_COMANDOS.registrarComando(new ComandoLuzMundo());
		Globales.GESTOR_COMANDOS.registrarComando(new ComandoAmbiente());
		Globales.GESTOR_COMANDOS.registrarComando(new ComandoSigilo());
		Globales.GESTOR_COMANDOS.registrarComando(new ComandoKillAll());
		Globales.GESTOR_COMANDOS.registrarComando(new ComandoVelocidad());
	}
}