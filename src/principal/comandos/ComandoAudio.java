package principal.comandos;

import principal.utilidades.audio.musica.GestorMusica;
import principal.utilidades.audio.musica.IDMusica;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

public class ComandoAudio extends Comando {

	public ComandoAudio() {
		super("audio", "audio <sfx <id> | bgm <id> | stop | list>",
				"Reproduce efectos de sonido o pistas de música directamente para calibración de volumen.");
	}

	@Override
	public void ejecutar(final String[] args) {
		this.ejecutar(args, null);
	}

	@Override
	public void ejecutar(final String[] args, final EmisorRespuesta emisor) {
		if (args.length == 0 || args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("lista")) {
			final StringBuilder sb = new StringBuilder("=== AUDIOS DISPONIBLES ===\nSFX: ");
			for (final IDSonido s : IDSonido.values()) {
				sb.append(s.name()).append(" | ");
			}
			sb.append("\nBGM: ");
			for (final IDMusica m : IDMusica.values()) {
				sb.append(m.name()).append(" | ");
			}
			this.enviarInfo(emisor, sb.toString());
			return;
		}

		final String sub = args[0].toLowerCase().trim();

		if (sub.equals("stop") || sub.equals("parar")) {
			GestorMusica.detenerMusicaFondoPrincipal();
			GestorMusica.detenerAmbienteClima();
			this.enviarInfo(emisor, "Música y ambiente detenidos.");
			return;
		}

		if (args.length < 2) {
			this.enviarError(emisor, "Uso: audio sfx <ID> o audio bgm <ID>");
			return;
		}

		final String idStr = args[1].toUpperCase().trim();

		if (sub.equals("sfx") || sub.equals("sonido")) {
			try {
				final IDSonido sfx = IDSonido.valueOf(idStr);
				GestorSonido.reproducir(sfx);
				this.enviarInfo(emisor, "Reproduciendo SFX: " + sfx.name());
			} catch (final IllegalArgumentException e) {
				GestorSonido.reproducir(args[1]);
				this.enviarInfo(emisor, "Reproduciendo SFX por ID directo: " + args[1]);
			}
			return;
		}

		if (sub.equals("bgm") || sub.equals("musica")) {
			try {
				final IDMusica bgm = IDMusica.valueOf(idStr);
				GestorMusica.reproducirMusicaFondoPrincipal(bgm);
				this.enviarInfo(emisor, "Reproduciendo BGM: " + bgm.name());
			} catch (final IllegalArgumentException e) {
				GestorMusica.reproducirMusicaFondoPrincipal(args[1]);
				this.enviarInfo(emisor, "Reproduciendo BGM por ID directo: " + args[1]);
			}
			return;
		}

		this.enviarError(emisor, "Comando no reconocido. Usa 'audio list', 'audio sfx <ID>' o 'audio bgm <ID>'.");
	}
}