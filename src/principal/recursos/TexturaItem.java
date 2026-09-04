package principal.recursos;

/**
 * Catálogo fuertemente tipado de todas las texturas de ítems, armas, municiones
 * y partículas del juego. Cada constante declara su hoja de origen y su índice
 * de corte, eliminando duplicaciones de I/O y números mágicos en el código.
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public enum TexturaItem {

	// --- Pociones ---
	POCION_ROJA_INV(ClaveHoja.ITEMS_16, 50), POCION_ROJA_MAPA(ClaveHoja.ITEMS_10, 50),
	POCION_AZUL_INV(ClaveHoja.ITEMS_16, 48), POCION_AZUL_MAPA(ClaveHoja.ITEMS_10, 48),

	// --- Joyería y Materiales ---
	MADERA_INV(ClaveHoja.ITEMS_16, 67), MADERA_MAPA(ClaveHoja.ITEMS_10, 67), PIEDRA_INV(ClaveHoja.ITEMS_16, 39),
	PIEDRA_MAPA(ClaveHoja.ITEMS_10, 39), ANILLO_PLATA_INV(ClaveHoja.ITEMS_16, 6),
	ANILLO_PLATA_MAPA(ClaveHoja.ITEMS_10, 6), ANILLO_ORO_INV(ClaveHoja.ITEMS_16, 19),
	ANILLO_ORO_MAPA(ClaveHoja.ITEMS_10, 19), ESMERALDA_INV(ClaveHoja.ITEMS_16, 28), CORONA_MAPA(ClaveHoja.ITEMS_10, 22),
	CORONA_INV(ClaveHoja.ITEMS_16, 22), LLAVE_INV(ClaveHoja.ITEMS_16, 37), LLAVE_MAPA(ClaveHoja.ITEMS_16, 37),

	// --- Equipamiento ---
	BOTAS_CUERO_INV(ClaveHoja.ITEMS_16, 122), BOTAS_CUERO_MAPA(ClaveHoja.ITEMS_10, 122),
	CASCO_BASE_INV(ClaveHoja.ITEMS_16, 112), CASCO_BASE_MAPA(ClaveHoja.ITEMS_10, 113),
	ARMADURA_BASE_INV(ClaveHoja.ITEMS_16, 113), ARMADURA_BASE_MAPA(ClaveHoja.ITEMS_10, 114),

	// --- Herramientas ----
	PICO_BASICO_INV(ClaveHoja.ITEMS_16, 70), PICO_BASICO_MAPA(ClaveHoja.ITEMS_10, 70),
	HACHA_BASICO_INV(ClaveHoja.ITEMS_16, 73), HACHA_BASICO_MAPA(ClaveHoja.ITEMS_10, 73),

	// --- Armas de Fuego (16x16 Inventario / 8x8 HUD y Suelo) ---
	PISTOLA_INV(ClaveHoja.PISTOLA_16, 0), PISTOLA_MAPA(ClaveHoja.PISTOLA_8, 0),
	ESCOPETA_RECORTADA_INV(ClaveHoja.ARMAS_PACK_16, 11), ESCOPETA_RECORTADA_MAPA(ClaveHoja.ARMAS_PACK_16, 11), // Redimensionado
																												// por
																												// el
																												// gestor
																												// a 8x8
	ESCOPETA_TACTICA_INV(ClaveHoja.ARMAS_PACK_16, 9), ESCOPETA_TACTICA_MAPA(ClaveHoja.ARMAS_PACK_16, 9),
	ESCOPETA_AUTOMATICA_INV(ClaveHoja.ARMAS_PACK_16, 1), ESCOPETA_AUTOMATICA_MAPA(ClaveHoja.ARMAS_PACK_16, 1),
	SUBFUSIL_LIGERO_INV(ClaveHoja.ARMAS_PACK_16, 24), SUBFUSIL_LIGERO_MAPA(ClaveHoja.ARMAS_PACK_16, 24),
	RIFLE_ASALTO_INV(ClaveHoja.ARMAS_PACK_16, 2), RIFLE_ASALTO_MAPA(ClaveHoja.ARMAS_PACK_16, 2),
	AMETRALLADORA_PESADA_INV(ClaveHoja.ARMAS_PACK_16, 19), AMETRALLADORA_PESADA_MAPA(ClaveHoja.ARMAS_PACK_16, 19),

	// --- Munición y Balística ---
	CAJA_MUNICION_INV(ClaveHoja.CAJA_MUNICION_16, 0), CAJA_MUNICION_MAPA(ClaveHoja.CAJA_MUNICION_8, 0),
	BALA_PROYECTIL(ClaveHoja.BALA_4, 0),

	// --- Granadas y Arrojadizos ---
	GRANADA_T1_INV(ClaveHoja.GRANADAS_16, 0), GRANADA_T1_MAPA(ClaveHoja.GRANADAS_10, 0),

	// --- Partículas Fijas ---
	PARTICULA_SANGRE_8(ClaveHoja.SANGRE_8, 0);

	private final ClaveHoja hojaOrigen;
	private final int indiceSprite;

	TexturaItem(final ClaveHoja hojaOrigen, final int indiceSprite) {
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