package principal;

import principal.comandos.ComandoAmbiente;
import principal.comandos.ComandoCamara;
import principal.comandos.ComandoClima;
import principal.comandos.ComandoCrafteo;
import principal.comandos.ComandoCurar;
import principal.comandos.ComandoEfecto;
import principal.comandos.ComandoFPS;
import principal.comandos.ComandoHora;
import principal.comandos.ComandoInfo;
import principal.comandos.ComandoJugador;
import principal.comandos.ComandoKillAll;
import principal.comandos.ComandoLuz;
import principal.comandos.ComandoLuzMundo;
import principal.comandos.ComandoParticulas;
import principal.comandos.ComandoSigilo;
import principal.comandos.ComandoTeleport;
import principal.comandos.ComandoVelocidad;
import principal.configuracion.ConfiguracionGrafica;
import principal.configuracion.GestorConfiguracion;
import principal.configuracion.LimiteFPS;
import principal.graficos.SuperficieDibujo;
import principal.graficos.Ventana;
import principal.maquinaestado.GestorEstados;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.audio.musica.GestorMusica;
import principal.utilidades.audio.sonido.GestorSonido;

public class GestorPrincipal {

	private static final long NS_POR_SEGUNDO = 1_000_000_000L;
	private static final int APS_OBJETIVO = 60;
	private static final double NS_POR_ACTUALIZACION = (double) NS_POR_SEGUNDO / APS_OBJETIVO;
	private static final int MAX_ACTUALIZACIONES_POR_FRAME = 5;

	private GestorEstados gestorEstados;
	private SuperficieDibujo superficieDibujo;
	private Ventana ventana;

	private boolean enFuncionamiento;
	private int codActualizacion;
	private long tiempoInicioSesionMs;

	private int actualizacionesAcumuladas = 0;
	private int framesAcumulados = 0;

	public GestorPrincipal() {
		this.codActualizacion = Integer.MIN_VALUE;
		GestorSonido.cargarSonidosDesdeJSON("sonidos/Sonidos.json");
		GestorMusica.cargarMusicasDesdeJSON("sonidos/Musicas.json");
	}

	public void iniciarJuego() {
		this.enFuncionamiento = true;
		this.tiempoInicioSesionMs = System.currentTimeMillis();

		this.gestorEstados = new GestorEstados();
		this.superficieDibujo = SuperficieDibujo.obtenerSuperficieDibujo();
		this.ventana = new Ventana("Juego RPG", this.superficieDibujo);

		// Inicialización unificada cifrada de Video, Teclado y Preferencias
		GestorConfiguracion.inicializar();

		this.registrarComandos();
		Globales.GESTOR_COMANDOS.iniciarEscuchaConsola();
		Globales.GESTOR_COMANDOS.iniciarServicios();
	}

	public void iniciarBuclePrincipal(final boolean Vsync) {
		long referenciaActualizacion = System.nanoTime();
		long referenciaContador = System.nanoTime();
		double tiempoTranscurrido;
		double delta = 0;

		Globales.delta = 1.0 / APS_OBJETIVO;

		while (this.enFuncionamiento) {
			final long inicioBucle = System.nanoTime();
			tiempoTranscurrido = inicioBucle - referenciaActualizacion;
			referenciaActualizacion = inicioBucle;

			delta += tiempoTranscurrido / NS_POR_ACTUALIZACION;

			// --- 1. LÓGICA (60 APS DETERMINISTA) ---
			int actualizacionesEnEsteFrame = 0;
			while ((delta >= 1.0) && (actualizacionesEnEsteFrame < MAX_ACTUALIZACIONES_POR_FRAME)) {
				this.actualizar();
				delta--;
				actualizacionesEnEsteFrame++;
			}

			if (actualizacionesEnEsteFrame >= MAX_ACTUALIZACIONES_POR_FRAME) {
				delta = 0;
			}

			// --- 2. RENDERIZADO (FPS) ---
			this.pintar();

			// --- 3. CONTROL DE TASA DE CUADROS HÍBRIDO (SIN JITTER EN WINDOWS) ---
			final LimiteFPS lim = ConfiguracionGrafica.getLimiteFps();

			if (lim != LimiteFPS.ILIMITADO) {
				final double nsPorFrameObjetivo = lim.getNsPorFrame();
				final long tiempoFrame = System.nanoTime() - inicioBucle;
				final double tiempoRestanteNS = nsPorFrameObjetivo - tiempoFrame;

				if (tiempoRestanteNS > 0) {
					final long finEsperado = System.nanoTime() + (long) tiempoRestanteNS;

					// Si sobra más de 2.5 ms, dormimos el hilo para descansar la CPU
					if (tiempoRestanteNS > 2_500_000) {
						try {
							final long msParaEsperar = (long) ((tiempoRestanteNS - 2_000_000) / 1_000_000);
							Thread.sleep(msParaEsperar);
						} catch (final InterruptedException e) {
							Thread.currentThread().interrupt();
						}
					}

					// Tramo final (< 2 ms): espera activa de ultra-precisión para clavar los 60.0
					// FPS
					while (System.nanoTime() < finEsperado) {
						if ((finEsperado - System.nanoTime()) > 500_000) {
							Thread.yield();
						}
					}
				}
			} else {
				Thread.yield();
			}

			// --- 4. MÉTRICAS CADA 1 SEGUNDO ---
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

	private void actualizar() {
		Globales.GESTOR_COMANDOS.actualizar();
		Globales.RATON.actualizar(this.superficieDibujo);
		Globales.TECLADO.actualizar();
		this.gestorEstados.actualizar();
		Globales.CAMARA.actualizar();
		Globales.GESTOR_TEXTOS.actualizarFijos();
		this.siguienteAnimacion();
		this.actualizacionesAcumuladas++;
		this.actualizarCodActualizacion();
	}

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
		// --- COMANDOS BÁSICOS EXISTENTES ---
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
		Globales.GESTOR_COMANDOS.registrarComando(new ComandoCamara());
		Globales.GESTOR_COMANDOS.registrarComando(new ComandoParticulas());
		Globales.GESTOR_COMANDOS.registrarComando(new ComandoInfo());
		Globales.GESTOR_COMANDOS.registrarComando(new ComandoJugador());
		Globales.GESTOR_COMANDOS.registrarComando(new ComandoCrafteo());
		Globales.GESTOR_COMANDOS.registrarComando(new ComandoEfecto());
		Globales.GESTOR_COMANDOS.registrarComando(new ComandoFPS());

		Globales.GESTOR_COMANDOS.registrarComando(new principal.comandos.ComandoGive());
		Globales.GESTOR_COMANDOS.registrarComando(new principal.comandos.ComandoSpawn());
		Globales.GESTOR_COMANDOS.registrarComando(new principal.comandos.ComandoDinero());
		Globales.GESTOR_COMANDOS.registrarComando(new principal.comandos.ComandoProgreso());
		Globales.GESTOR_COMANDOS.registrarComando(new principal.comandos.ComandoTermico());
		Globales.GESTOR_COMANDOS.registrarComando(new principal.comandos.ComandoIA());
		Globales.GESTOR_COMANDOS.registrarComando(new principal.comandos.ComandoMundo());
		Globales.GESTOR_COMANDOS.registrarComando(new principal.comandos.ComandoFaccion());
		Globales.GESTOR_COMANDOS.registrarComando(new principal.comandos.ComandoGrupo());
		Globales.GESTOR_COMANDOS.registrarComando(new principal.comandos.ComandoDialogo());
		Globales.GESTOR_COMANDOS.registrarComando(new principal.comandos.ComandoAudio());
		Globales.GESTOR_COMANDOS.registrarComando(new principal.comandos.ComandoStress());
		Globales.GESTOR_COMANDOS.registrarComando(new principal.comandos.ComandoAstronomico());
	}
}