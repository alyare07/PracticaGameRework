package principal.recursos;

/**
 * Catálogo centralizado de todas las hojas de sprites (spritesheets) maestras
 * del juego. Define la ruta del recurso y el tamaño base de cada cuadro
 * (frame).
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public enum ClaveHoja {

	// --- Entidades y Criaturas ---
	JUGADOR("/imagenes/sprites/player_sprites.png", 32, 32), BANDIDO("/imagenes/sprites/bandido.png", 32, 32),

	// --- Proyectiles y Habilidades ---
	BOLA_FUEGO("/imagenes/sprites/firebolt.png", 16, 16),
	EXPLOSION_BOLA_FUEGO("/imagenes/sprites/firebolt_explosion.png", 32, 32),
	EXPLOSION_GRANADA("/imagenes/sprites/explosion.png", 50, 50),

	// --- Terrenos y Escenario ---
	TERRENOS_16("/imagenes/texturas/terrenos16.png", 16, 16), DUNGEON_16("/imagenes/texturas/dungeon.png", 16, 16),
	ARBOLES_32("/imagenes/texturas/trees.png", 32, 32),
	ARBOLES_NEVADOS_32("/imagenes/texturas/treesNevado.png", 32, 32), CASA_1("/imagenes/texturas/house/1.png", 64, 64),
	COFRES_16("/imagenes/objetos/cofres.png", 16, 16),

	// --- Ítems y Equipamiento ---
	ITEMS_16("/imagenes/objetos/items.png", 16, 16), ITEMS_10("/imagenes/objetos/itemsx10.png", 10, 10),
	ARMAS_PACK_16("/imagenes/objetos/pack.png", 16, 16), GRANADAS_16("/imagenes/objetos/granadas.png", 16, 16),
	GRANADAS_10("/imagenes/objetos/granadas.png", 10, 10),

	// --- Armas e Íconos Individuales ---
	PISTOLA_16("/imagenes/objetos/gun16x16.png", 16, 16), PISTOLA_8("/imagenes/objetos/gun8x8.png", 8, 8),
	BALA_4("/imagenes/objetos/bala.png", 4, 4), CAJA_MUNICION_16("/imagenes/objetos/boxbullet.png", 16, 16),
	CAJA_MUNICION_8("/imagenes/objetos/boxbullet.png", 8, 8), SANGRE_8("/imagenes/objetos/sangrex8.png", 8, 8),

	// --- IGU -----
	IGU_DISCO_CICLO_TIME("/imagenes/igu/disco_ciclo.png", 40, 40),
	IGU_MARCO_TIME("/imagenes/igu/marco_reloj.png", 44, 44);

	private final String ruta;
	private final int anchoFrame;
	private final int altoFrame;

	ClaveHoja(final String ruta, final int anchoFrame, final int altoFrame) {
		this.ruta = ruta;
		this.anchoFrame = anchoFrame;
		this.altoFrame = altoFrame;
	}

	public String getRuta() {
		return this.ruta;
	}

	public int getAnchoFrame() {
		return this.anchoFrame;
	}

	public int getAltoFrame() {
		return this.altoFrame;
	}
}