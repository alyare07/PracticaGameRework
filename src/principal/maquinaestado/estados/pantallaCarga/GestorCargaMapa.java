package principal.maquinaestado.estados.pantallaCarga;

import org.json.simple.JSONObject;

import principal.maquinaestado.estados.GestorJuego;

public class GestorCargaMapa extends GestorCarga {

	public GestorCargaMapa() {
	}

	public void cargar(final cargaMapa cj, final GestorCarga gc, final String nombreMapa, final String nombreMundo,
			final String nombreSpawmn, final boolean reset) {
		if (this.completo) {
			this.completo = false;
			this.porcentaje = 0;
		}

		this.hiloCarga = new Thread(new Runnable() {
			@Override
			public void run() {
				cj.cargarMapa(gc, nombreMapa, nombreMundo, nombreSpawmn, reset);
			}
		}, "Hilo-Carga-Mapa");
		this.hiloCarga.start();
	}

	public void cargarSave(final GestorJuego gj, final GestorCarga gc, final JSONObject saveJson) {
		if (this.completo) {
			this.completo = false;
			this.porcentaje = 0;
		}

		this.hiloCarga = new Thread(new Runnable() {
			@Override
			public void run() {
				gj.cargarPartidaGuardada(gc, saveJson);
			}
		}, "Hilo-Carga-SaveGame");
		this.hiloCarga.start();
	}
}