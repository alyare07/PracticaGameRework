package principal.utilidades.audio.musica;

public enum IDMusica {

	FONDO_FOREST("musicas.fondo_forest");

	private final String id;

	IDMusica(final String id) {
		this.id = id;
	}

	public String getId() {
		return this.id;
	}
}