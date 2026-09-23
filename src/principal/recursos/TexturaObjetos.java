package principal.recursos;

/**
 * Catálogo fuertemente tipado de todas las texturas de Objetos no items. Cada
 * constante declara su hoja de origen y su índice de corte, eliminando
 * duplicaciones de I/O y números mágicos en el código.
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public enum TexturaObjetos {

	// --- X32 ---
	CARPA_X32(ClaveHoja.OBJETOS_X32, 0), CAMA_X32(ClaveHoja.OBJETOS_X32, 1);

	private final ClaveHoja hojaOrigen;
	private final int indiceSprite;

	TexturaObjetos(final ClaveHoja hojaOrigen, final int indiceSprite) {
		this.hojaOrigen = hojaOrigen;
		this.indiceSprite = indiceSprite;
	}

	public ClaveHoja getHojaOrigen() {
		return this.hojaOrigen;
	}

	public int getIndiceSprite() {
		return this.indiceSprite;
	}
}