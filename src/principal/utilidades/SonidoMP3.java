package principal.utilidades;

import java.io.File;

import jaco.mp3.player.MP3Player;

public class SonidoMP3 {
	private final MP3Player MP3;

	public SonidoMP3(final String urlInterna) {
		this.MP3 = new MP3Player(new File(urlInterna));
	}

	public void reproducir() {
		this.MP3.play();
	}

	public void pausar() {
		this.MP3.pause();
	}

	public void repetir(final boolean repetir) {
		this.MP3.setRepeat(repetir);
	}

	public void actualizar(final boolean reproducir) {
		if (reproducir) {
			if (this.MP3.isPaused() || this.MP3.isStopped()) {
				this.MP3.play();
			}
		} else if (!this.MP3.isPaused()) {
			this.MP3.pause();
		}
	}

}
