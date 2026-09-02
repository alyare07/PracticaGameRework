package principal.entes.objetos.items.equipamiento;

/**
 * Define las ranuras de equipamiento soportadas por el motor RPG.
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public enum TipoEquipo {

	ARMA("Arma"),
	CASCO("Casco"),
	TORSO("Indumentaria"),
	BOTAS("Botas"),
	ANILLO("Anillo");

	private final String nombreVisible;

	TipoEquipo(final String nombreVisible) {
		this.nombreVisible = nombreVisible;
	}

	public String getNombreVisible() {
		return this.nombreVisible;
	}
}