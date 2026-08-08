package principal;

import principal.graficos.SuperficieDibujo;
import principal.graficos.Ventana;
import principal.maquinaestado.GestorEstados;
import principal.utilidades.Constantes;

public class GestorPrincipalMultiHilo {
	private boolean enFuncionamiento;
	private GestorEstados GE;
	private SuperficieDibujo SD;
	private Ventana VENTANA;
	private final int ANCHO;
	private final int ALTO;
	private final Thread hiloFrames = new Thread(new Runnable() {

		@Override
		public void run() {
			iniciarBucleFrames();
		}
	});

	public GestorPrincipalMultiHilo(final int ancho, final int alto) {
		this.ANCHO = ancho;
		this.ALTO = alto;
	}

	private void inicializar() {
		this.GE = new GestorEstados();
		this.SD = SuperficieDibujo.obetenerSuperficieDibujo();
		this.VENTANA = new Ventana("Juego", SD);
	}

	public void iniciarJuego() {
		enFuncionamiento = true;
		inicializar();
	}

	public void iniciarBucleActualizaciones() {
		int actualizacionesAcumuladas = 0;
		final int NS_POR_SEGUNDO = 1000000000;
		final byte APS_OBJETIVO = 60;
		final double NS_POR_ACTUALIZACION = NS_POR_SEGUNDO / APS_OBJETIVO;
		long referenciaActualizacion = System.nanoTime();
		long referenciaContador = System.nanoTime();
		double tiempoTranscurrido;
		double delta = 0;

		while (enFuncionamiento) {
			final long inicioBucle = System.nanoTime();
			tiempoTranscurrido = inicioBucle - referenciaActualizacion;
			referenciaActualizacion = inicioBucle;
			delta += tiempoTranscurrido / NS_POR_ACTUALIZACION;

			while (delta >= 1) {
				actualizar();
				actualizacionesAcumuladas++;
				delta--;

			}
			if (System.nanoTime() - referenciaContador > NS_POR_SEGUNDO) {
				Constantes.GLOBALES.aps = actualizacionesAcumuladas;
				actualizacionesAcumuladas = 0;
				referenciaContador = System.nanoTime();
			}
		}
	}

	public void iniciarBucleFrames() {
		int framesAcumulados = 0;
		final int NS_POR_SEGUNDO = 1000000000;
		long referenciaContador = System.nanoTime();

		while (enFuncionamiento) {

			pintar();
			framesAcumulados++;
			if (System.nanoTime() - referenciaContador > NS_POR_SEGUNDO) {
				Constantes.GLOBALES.fps = framesAcumulados;

				framesAcumulados = 0;
				referenciaContador = System.nanoTime();
			}
		}
	}

	private void pintar() {
		this.SD.pintar(GE);
	}

	private void actualizar() {
		this.GE.actualizar();
		siguienteAnimacion();
	}

	private void siguienteAnimacion() {
		if (Constantes.GLOBALES.animacion < Constantes.LIMITE_ANIMACION) {
			Constantes.GLOBALES.animacion++;
		} else {
			Constantes.GLOBALES.animacion = 0;
		}
	}

	public void iniciarBucles() {
		iniciarJuego();
		this.hiloFrames.start();
		this.iniciarBucleActualizaciones();
	}

//	public static void main(String[] args) {
//		GestorPrincipalMultiHilo gp = new GestorPrincipalMultiHilo(Constantes.ANCHO_PANTALLA_COMPLETA,
//				Constantes.ALTO_PANTALLA_COMPLETA);
//		gp.iniciarJuego();
//		gp.hiloFrames.start();
//		gp.iniciarBucleActualizaciones();
////
////		Point p1 = new Point(20, 54);
////		Point p2 = new Point(-20, 454);
////
////		double ti = System.nanoTime();
////		System.out.println(Math.sqrt(Math.pow(p2.getX() - p1.getX(), 2) + Math.pow(p2.getY() - p1.getY(), 2)));
////		double tf = System.nanoTime();
////
////		double ti2 = System.nanoTime();
////		System.out.println(p1.distance(p2));
////		double tf2 = System.nanoTime();
////
////		System.out.println("t1: " + (tf - ti) + "\nt2: " + (tf2 - ti2));
//	}

}
