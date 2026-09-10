package principal.entes.objetos.items.equipamiento;

/**
 * Define la especialización térmica de una prenda o armadura (Zero-GC).
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public enum TipoAislamiento {

	NINGUNO("Ninguno"),
	FRIO("Frío"),
	CALOR("Calor"),
	UNIVERSAL("Universal");

	private final String nombreVisible;

	TipoAislamiento(final String nombreVisible) {
		this.nombreVisible = nombreVisible;
	}

	public String getNombreVisible() {
		return this.nombreVisible;
	}
}