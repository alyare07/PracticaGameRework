package principal.eventos;

import principal.dialogos.MensajeDialogo;
import principal.entes.Ente;
import principal.utilidades.Globales;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;
import principal.utilidades.progreso.FlagProgreso;

public final class Pasos {

	private Pasos() {
	}

	public static PasoEvento esperar(final double segundos) {
		return new PasoEvento() {
			private double restante = segundos;

			@Override
			public void iniciar() {
				this.restante = segundos;
			}

			@Override
			public void actualizar(final double dt) {
				this.restante -= dt;
			}

			@Override
			public boolean haTerminado() {
				return this.restante <= 0.0;
			}
		};
	}

	public static PasoEvento dialogo(final MensajeDialogo mensaje) {
		return new PasoEvento() {
			private boolean iniciado = false;

			@Override
			public void iniciar() {
				Globales.GESTOR_DIALOGOS.iniciarDialogo(mensaje);
				this.iniciado = true;
			}

			@Override
			public void actualizar(final double dt) {
			}

			@Override
			public boolean haTerminado() {
				return this.iniciado && !Globales.GESTOR_DIALOGOS.isActivo();
			}
		};
	}

	public static PasoEvento enfocarCamara(final Ente objetivo) {
		return new PasoEvento() {
			private boolean listo = false;

			@Override
			public void iniciar() {
				Globales.CAMARA.setEntidadEnfocada(objetivo);
				this.listo = true;
			}

			@Override
			public void actualizar(final double dt) {
			}

			@Override
			public boolean haTerminado() {
				return this.listo;
			}
		};
	}

	public static PasoEvento temblorCamara(final double ms, final double fuerza) {
		return new PasoEvento() {
			private double tiempo = ms / 1000.0;

			@Override
			public void iniciar() {
				Globales.CAMARA.aplicarTemblor(ms, fuerza);
				this.tiempo = ms / 1000.0;
			}

			@Override
			public void actualizar(final double dt) {
				this.tiempo -= dt;
			}

			@Override
			public boolean haTerminado() {
				return this.tiempo <= 0.0;
			}
		};
	}

	public static PasoEvento sonido(final IDSonido sonido) {
		return new PasoEvento() {
			private boolean ejecutado = false;

			@Override
			public void iniciar() {
				GestorSonido.reproducir(sonido);
				this.ejecutado = true;
			}

			@Override
			public void actualizar(final double dt) {
			}

			@Override
			public boolean haTerminado() {
				return this.ejecutado;
			}
		};
	}

	public static PasoEvento activarFlag(final FlagProgreso flag) {
		return new PasoEvento() {
			private boolean listo = false;

			@Override
			public void iniciar() {
				Globales.GESTOR_PROGRESO.activar(flag);
				this.listo = true;
			}

			@Override
			public void actualizar(final double dt) {
			}

			@Override
			public boolean haTerminado() {
				return this.listo;
			}
		};
	}

	public static PasoEvento ejecutarCodigo(final Runnable accion) {
		return new PasoEvento() {
			private boolean listo = false;

			@Override
			public void iniciar() {
				if (accion != null) {
					accion.run();
				}
				this.listo = true;
			}

			@Override
			public void actualizar(final double dt) {
			}

			@Override
			public boolean haTerminado() {
				return this.listo;
			}
		};
	}
}