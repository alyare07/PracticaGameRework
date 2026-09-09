package principal.utilidades.audio.musica;

public enum IDMusica {

	FONDO_FOREST("musicas.fondo_forest"), FONDO_RELAX("musicas.fondo_relax"),
	AMBIENTE_LLUVIA("musicas.ambiente_lluvia"), AMBIENTE_TORMENTA("musicas.ambiente_tormenta"),
	AMBIENTE_VENTOSO("musicas.ambiente_ventoso");

	private final String id;

	IDMusica(final String id) {
		this.id = id;
	}

	public String getId() {
		return this.id;
	}
}