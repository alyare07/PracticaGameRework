package principal.eventos;

import java.util.ArrayDeque;
import java.util.Queue;

import principal.utilidades.Globales;

public class SecuenciaEvento {

	private final Queue<PasoEvento> colaPasos = new ArrayDeque<PasoEvento>();
	private PasoEvento pasoActual = null;
	private boolean enEjecucion = false;
	private boolean bloquearControles = true;

	public SecuenciaEvento agregarPaso(final PasoEvento paso) {
		if (paso != null) {
			this.colaPasos.add(paso);
		}
		return this;
	}

	public SecuenciaEvento setBloquearControles(final boolean bloquear) {
		this.bloquearControles = bloquear;
		return this;
	}

	public void iniciar() {
		if (this.colaPasos.isEmpty()) {
			return;
		}
		this.enEjecucion = true;
		if (this.bloquearControles) {
			Globales.CAMARA.activarModoCinematico(true);
		}
		this.siguientePaso();
		Globales.GESTOR_EVENTOS.registrarSecuenciaActiva(this);
	}

	public void actualizar(final double dt) {
		if (!this.enEjecucion) {
			return;
		}

		if (this.pasoActual != null) {
			this.pasoActual.actualizar(dt);
			if (this.pasoActual.haTerminado()) {
				this.siguientePaso();
			}
		} else {
			this.finalizar();
		}
	}

	private void siguientePaso() {
		if (!this.colaPasos.isEmpty()) {
			this.pasoActual = this.colaPasos.poll();
			this.pasoActual.iniciar();
		} else {
			this.finalizar();
		}
	}

	private void finalizar() {
		this.enEjecucion = false;
		this.pasoActual = null;
		if (this.bloquearControles) {
			Globales.CAMARA.activarModoCinematico(false);
		}
	}

	public boolean isEnEjecucion() {
		return this.enEjecucion;
	}
}