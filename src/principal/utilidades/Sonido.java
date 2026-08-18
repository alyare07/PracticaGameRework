package principal.utilidades;

import javax.sound.sampled.Clip;

public class Sonido {
	final private Clip sonido;

	public Sonido(final String ruta) {
		this.sonido = Globales.FUNCIONES.CARGADOR_RECURSOS.cargarSonido(ruta);
	}

	public void reproducir() {
		this.sonido.stop();
		this.sonido.flush();
		this.sonido.setMicrosecondPosition(0);
		this.sonido.start();
	}

	public void repetir() {
		this.sonido.stop();
		this.sonido.flush();
		this.sonido.setMicrosecondPosition(0);
		this.sonido.loop(Clip.LOOP_CONTINUOUSLY);
	}

	public long getDuracion() {
		return this.sonido.getMicrosecondLength();
	}
}
