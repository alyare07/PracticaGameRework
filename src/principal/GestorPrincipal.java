package principal;

import principal.graficos.SuperficieDibujo;
import principal.graficos.Ventana;
import principal.maquinaestado.GestorEstados;
import principal.utilidades.Constantes;

public class GestorPrincipal {
	private boolean enFuncionamiento;
	private GestorEstados GE;
	private SuperficieDibujo SD;
	@SuppressWarnings("unused")
	private Ventana VENTANA;
	private int actualizacionesAcumuladas = 0;
	private int framesAcumulados = 0;
	private boolean SyncApsFrame;
	private int codActualizacion;
	public GestorPrincipal() {
	}

	public void iniciarJuego(final boolean SyncApsFrame) {
		enFuncionamiento = true;
		this.SyncApsFrame = SyncApsFrame;
		this.GE = new GestorEstados();
		this.SD = SuperficieDibujo.obetenerSuperficieDibujo();
		this.VENTANA = new Ventana("Juego", SD);
		this.codActualizacion = Integer.MIN_VALUE;

	}

	public void iniciarBuclePrincipal() {
		final int NS_POR_SEGUNDO = 1000000000;
		final byte APS_OBJETIVO = 60;
		final double NS_POR_ACTUALIZACION = NS_POR_SEGUNDO / APS_OBJETIVO;
		long referenciaActualizacion = System.nanoTime();
		long referenciaContador = System.nanoTime();
		double tiempoTranscurrido;
		double delta = 0;
//		actualizarTiempoJugado();
		while (enFuncionamiento) {
			final long inicioBucle = System.nanoTime();
			tiempoTranscurrido = inicioBucle - referenciaActualizacion;
			referenciaActualizacion = inicioBucle;
			delta += tiempoTranscurrido / NS_POR_ACTUALIZACION;
			Constantes.GLOBALES.delta = tiempoTranscurrido / NS_POR_SEGUNDO;
//			System.out.println(Constantes.GLOBALES.delta);
			while (delta >= 1) {
				actualizar();
				if (this.SyncApsFrame) {
					pintar();
				}

				delta--;

			}
			if (!this.SyncApsFrame) {
				pintar();
			}
			if (System.nanoTime() - referenciaContador > NS_POR_SEGUNDO) {
				actualizarTiempoJugado();
				Constantes.GLOBALES.aps = actualizacionesAcumuladas;
				Constantes.GLOBALES.fps = framesAcumulados;
				actualizacionesAcumuladas = 0;
				framesAcumulados = 0;
				referenciaContador = System.nanoTime();

			}
		}
	}
	
	public void iniciarBuclePrueba() {
		final int NS_POR_SEGUNDO = 1000000000;
		final byte APS_OBJETIVO = 60;
		final double NS_POR_ACTUALIZACION = NS_POR_SEGUNDO / APS_OBJETIVO;
		long referenciaActualizacion = System.nanoTime();
		long referenciaContador = System.nanoTime();
		double tiempoTranscurrido;
		double delta = 0;
//		actualizarTiempoJugado();
		while (enFuncionamiento) {
			final long inicioBucle = System.nanoTime();
			tiempoTranscurrido = inicioBucle - referenciaActualizacion;
			referenciaActualizacion = inicioBucle;
			delta += tiempoTranscurrido / NS_POR_ACTUALIZACION;
			Constantes.GLOBALES.delta = tiempoTranscurrido / NS_POR_SEGUNDO;
//			System.out.println(Constantes.GLOBALES.delta);
			while (delta >= 1) {
				actualizar();
				pintar();
				delta--;
			}
			if (!this.SyncApsFrame) {
				pintar();
			}
			if (System.nanoTime() - referenciaContador > NS_POR_SEGUNDO) {
				actualizarTiempoJugado();
				Constantes.GLOBALES.aps = actualizacionesAcumuladas;
				Constantes.GLOBALES.fps = framesAcumulados;
				actualizacionesAcumuladas = 0;
				framesAcumulados = 0;
				referenciaContador = System.nanoTime();

			}
		}
	}
	
	public int getCodigoActualizacion() {
		return this.codActualizacion;
	}

	private void pintar() {
		this.SD.pintar(GE);
		framesAcumulados++;
	}

	private void actualizar() {
		Constantes.RATON.actualizar(SD);
		Constantes.TECLADO.actualizarEstadosTeclas();
		this.GE.actualizar();
		Constantes.CAMARA.actualizar();
		siguienteAnimacion();
		actualizacionesAcumuladas++;
		this.actualizarCodActualizacion();
	}

	private void siguienteAnimacion() {
		if (Constantes.GLOBALES.animacion < Constantes.LIMITE_ANIMACION) {
			Constantes.GLOBALES.animacion++;
		} else {
			Constantes.GLOBALES.animacion = 0;
		}
	}

	private void actualizarTiempoJugado() {
		if (Constantes.GLOBALES.segundosJugados >= 60) {
			Constantes.GLOBALES.segundosJugados = 0;
			if (Constantes.GLOBALES.minutosJugados >= 60) {
				Constantes.GLOBALES.minutosJugados = 0;
				Constantes.GLOBALES.horasJugadas++;
			} else {
				Constantes.GLOBALES.minutosJugados++;
			}
		} else {
			Constantes.GLOBALES.segundosJugados++;
		}
	}
	private void actualizarCodActualizacion() {
		if(this.codActualizacion >= Integer.MAX_VALUE) {
			this.codActualizacion = Integer.MIN_VALUE;
		}else {
			this.codActualizacion++;
		}
	}
	
	public GestorEstados getGestorEstados() {
		return this.GE;
	}

//	private void actualizarTiempoJugado() {
//		final TimerTask tarea = new TimerTask() {
//
//			@Override
//			public void run() {
//				if (Constantes.GLOBALES.segundosJugados >= 60) {
//					Constantes.GLOBALES.segundosJugados = 0;
//					if (Constantes.GLOBALES.minutosJugados >= 60) {
//						Constantes.GLOBALES.minutosJugados = 0;
//						Constantes.GLOBALES.horasJugadas++;
//					} else {
//						Constantes.GLOBALES.minutosJugados++;
//					}
//				} else {
//					Constantes.GLOBALES.segundosJugados++;
//				}
//			}
//		};
//		final Timer tiempo = new Timer();
//		tiempo.schedule(tarea, 0, 1000);
//
//	}

}