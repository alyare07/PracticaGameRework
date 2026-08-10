package principal.utilidades.audio.sonido;

public enum IDSonido {

	EXPLOSION_1("efectos.explosion_1"), CRIATURA_MUERTA("efectos.criatura_muerta"),
	DISPARO_PISTOLA("efectos.disparo_pistola"), SIN_MUNICION("efectos.sin_municion"), GOLPE_1("efectos.golpe_1"),
	GOLPE_2("efectos.golpe_2");

	private final String id;

	IDSonido(final String id) {
		this.id = id;
	}

	public String getId() {
		return this.id;
	}
}