package principal.recursos;

/**
 * Define cada tipo de terreno del mundo eliminando los enteros mágicos.
 * Encapsula propiedades de colisión, modificadores de velocidad física
 * y la fila correspondiente dentro de la hoja 'terrenos16.png'.
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public enum TipoTerreno {

	CESPED("Césped Templado", false, 0.0, 0, 1),
	TIERRA("Tierra Fértil", false, 0.0, 1, 1),
	TIERRA_2("Camino de Tierra", false, 0.20, 2, 1),
	ARENA("Arena de Duna", false, -0.20, 3, 1),
	ASFALTO("Camino de Adoquín", false, 0.25, 4, 1),
	PIEDRA("Roca Impenetrable", true, 0.0, 5, 1),
	AGUA("Agua Profunda", true, -0.50, 6, 3), // 3 frames de animación (filas 6, 7 y 8)
	CESPED_2("Pradera Seca", false, 0.0, 9, 1),
	CESPED_3("Bosque Profundo", false, 0.0, 10, 1),
	CESPED_3_NEVADO("Tundra Nevada", false, -0.25, 11, 1),
	VACIO("Abismo", true, -1.0, 12, 1);

	private final String nombre;
	private final boolean solido;
	private final double alteracionVelocidad;
	private final int filaSpritesheet;
	private final int cantFramesAnimacion;

	TipoTerreno(final String nombre, final boolean solido, final double alteracionVelocidad,
			final int filaSpritesheet, final int cantFramesAnimacion) {
		this.nombre = nombre;
		this.solido = solido;
		this.alteracionVelocidad = alteracionVelocidad;
		this.filaSpritesheet = filaSpritesheet;
		this.cantFramesAnimacion = cantFramesAnimacion;
	}

	public String getNombre() {
		return this.nombre;
	}

	public boolean isSolido() {
		return this.solido;
	}

	public double getAlteracionVelocidad() {
		return this.alteracionVelocidad;
	}

	public int getFilaSpritesheet() {
		return this.filaSpritesheet;
	}

	public int getCantFramesAnimacion() {
		return this.cantFramesAnimacion;
	}

	public boolean esAnimado() {
		return this.cantFramesAnimacion > 1;
	}
}