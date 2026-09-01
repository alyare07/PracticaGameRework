package principal.crafteo;

public enum EstacionCrafteo {

	MANUAL("A Mano / Inventario"),
	MESA_TRABAJO("Mesa de Trabajo"),
	HORNO("Horno de Fundición"),
	YUNQUE("Yunque de Forja"),
	FOGATA("Fogata / Cocina");

	private final String nombre;

	EstacionCrafteo(final String nombre) {
		this.nombre = nombre;
	}

	public String getNombre() {
		return this.nombre;
	}
}