package principal.recursos;

public enum TipoTerreno {

	// 1. Superficie y Campo
	CESPED("Césped Templado", false, 0.0, 0, 1, 4), TIERRA("Tierra Fértil", false, 0.0, 1, 1, 2),
	TIERRA_2("Camino de Tierra", false, 0.20, 2, 1, 5), ARENA("Arena de Duna", false, -0.20, 3, 1, 3),
	ASFALTO("Camino de Adoquín", false, 0.25, 4, 1, 6), PIEDRA("Roca Impenetrable", true, 0.0, 5, 1, 7),

	// 2. Agua Animada (Filas 6, 7 y 8)
	AGUA("Agua Profunda", true, -0.50, 6, 3, 1),

	// 3. Estaciones y Abismo
	CESPED_2("Pradera Seca", false, 0.0, 9, 1, 4), CESPED_3("Bosque Profundo", false, 0.0, 10, 1, 4),
	CESPED_3_NEVADO("Tundra Nevada", false, -0.25, 11, 1, 4), VACIO("Abismo", true, -1.0, 12, 1, 0),

	// 4. Autotile Base
	RELIEVE_AUTO("Montaña Autotile", true, 0.0, 13, 1, 8),

	// 5. Kit Modular de Montaña Independiente (Filas 14 a 23)
	RELIEVE_CRESTA("Cresta Horizontal", true, 0.0, 14, 1, 8),
	RELIEVE_MURO_IZQ("Muro Lateral Izquierdo", true, 0.0, 15, 1, 8),
	RELIEVE_MURO_DER("Muro Lateral Derecho", true, 0.0, 16, 1, 8),
	RELIEVE_ESQ_NO("Esquina Superior Izq", true, 0.0, 17, 1, 8),
	RELIEVE_ESQ_NE("Esquina Superior Der", true, 0.0, 18, 1, 8),
	RELIEVE_ESQ_SO("Esquina en L (Inferior)", true, 0.0, 19, 1, 8),
	RELIEVE_ESQ_SE("Esquina L Invertida", true, 0.0, 20, 1, 8), RELIEVE_BASE("Base con Sombra", true, 0.0, 21, 1, 8),
	RAMPA_MADERA("Rampa de Madera", false, 0.0, 22, 1, 9), RAMPA_CESPED("Rampa de Césped", false, 0.0, 23, 1, 9),

	// 6. Subterráneo, Lava y Hielo
	CUEVA_SUELO("Suelo de Caverna", false, 0.0, 24, 1, 2), CUEVA_GEMAS("Veta de Minerales", true, 0.0, 25, 1, 8),
	LAVA("Lava Ardiente", true, -0.80, 26, 3, 1), // Filas 26, 27, 28
	HIELO_MURO("Muro de Glaciar", true, 0.0, 29, 1, 8), HIELO_SUELO("Pista de Hielo", false, 0.40, 30, 3, 3), // Filas
																												// 30,
																												// 31,
																												// 32

	// 7. Pantano y Ciénaga
	PANTANO_AGUA("Agua Turbia", true, -0.40, 33, 3, 1), // Filas 33, 34, 35
	PANTANO_BARRO("Lodo Profundo", false, -0.30, 36, 1, 2),

	// 8. Mazmorras e Interiores
	DUNGEON_LOSAS("Losas de Templo", false, 0.10, 37, 1, 6), DUNGEON_LADRILLO("Muro de Mazmorra", true, 0.0, 38, 1, 8),
	MADERA_TABLONES("Piso de Tablones", false, 0.15, 39, 1, 5),
	ALFOMBRA_ROJA("Alfombra Imperial", false, 0.10, 40, 1, 6);

	private final String nombre;
	private final boolean solido;
	private final double alteracionVelocidad;
	private final int filaSpritesheet;
	private final int cantFramesAnimacion;
	private final int prioridad;

	TipoTerreno(final String nombre, final boolean solido, final double alteracionVelocidad, final int filaSpritesheet,
			final int cantFramesAnimacion, final int prioridad) {
		this.nombre = nombre;
		this.solido = solido;
		this.alteracionVelocidad = alteracionVelocidad;
		this.filaSpritesheet = filaSpritesheet;
		this.cantFramesAnimacion = cantFramesAnimacion;
		this.prioridad = prioridad;
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

	public int getPrioridad() {
		return this.prioridad;
	}
}