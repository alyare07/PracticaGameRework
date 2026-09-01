package principal.entes.objetos.items.herramientas;

public enum TipoHerramienta {

	DESARMADO("Mano Desnuda"),
	HACHA("Hacha"),
	PICO("Pico"),
	PALA("Pala"),
	HOZ("Hoz");

	private final String nombre;

	TipoHerramienta(final String nombre) {
		this.nombre = nombre;
	}

	public String getNombre() {
		return this.nombre;
	}
}