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
	POCION_ROJA_INV(ClaveHoja.ITEMS_16, 50), POCION_ROJA_MAPA(ClaveHoja.ITEMS_12, 50),
	POCION_AZUL_INV(ClaveHoja.ITEMS_16, 48), POCION_AZUL_MAPA(ClaveHoja.ITEMS_12, 48),

	// --- Joyería y Materiales ---
	MADERA_INV(ClaveHoja.ITEMS_16, 67), MADERA_MAPA(ClaveHoja.ITEMS_12, 67), PIEDRA_INV(ClaveHoja.ITEMS_16, 39),
	PIEDRA_MAPA(ClaveHoja.ITEMS_12, 39), ANILLO_PLATA_INV(ClaveHoja.ITEMS_16, 6),
	ANILLO_PLATA_MAPA(ClaveHoja.ITEMS_12, 6), ANILLO_ORO_INV(ClaveHoja.ITEMS_16, 19),
	ANILLO_ORO_MAPA(ClaveHoja.ITEMS_12, 19), ESMERALDA_INV(ClaveHoja.ITEMS_16, 28), CORONA_MAPA(ClaveHoja.ITEMS_12, 22),
	CORONA_INV(ClaveHoja.ITEMS_16, 22), LLAVE_INV(ClaveHoja.ITEMS_16, 37), LLAVE_MAPA(ClaveHoja.ITEMS_16, 37),

	// --- Equipamiento ---
	BOTAS_CUERO_INV(ClaveHoja.ITEMS_16, 122), BOTAS_CUERO_MAPA(ClaveHoja.ITEMS_12, 122),
	CASCO_BASE_INV(ClaveHoja.ITEMS_16, 112), CASCO_BASE_MAPA(ClaveHoja.ITEMS_12, 113),
	ARMADURA_BASE_INV(ClaveHoja.ITEMS_16, 113), ARMADURA_BASE_MAPA(ClaveHoja.ITEMS_12, 114),

	// --- COMIDAS ----
	COMIDA_BAYAS_INV(ClaveHoja.ITEMS_16_COMIDAS, 16), COMIDA_BAYAS_MAPA(ClaveHoja.ITEMS_12_COMIDAS, 16),
	COMIDA_PATA_POLLO_CRUDA_INV(ClaveHoja.ITEMS_16_COMIDAS2, 59),
	COMIDA_PATA_POLLO_CRUDA_MAPA(ClaveHoja.ITEMS_12_COMIDAS2, 59),
	COMIDA_PATA_POLLO_COCIDA_INV(ClaveHoja.ITEMS_16_COMIDAS2, 38),
	COMIDA_PATA_POLLO_COCIDA_MAPA(ClaveHoja.ITEMS_12_COMIDAS2, 38),
	// --- CICLO HÍDRICO (BLOQUE 4) ---
	CUENCO_VACIO_INV(ClaveHoja.ITEMS_16_COMIDAS2, 52), CUENCO_VACIO_MAPA(ClaveHoja.ITEMS_12_COMIDAS2, 52),

	CUENCO_AGUA_SUCIA_INV(ClaveHoja.ITEMS_16_COMIDAS2, 54), CUENCO_AGUA_SUCIA_MAPA(ClaveHoja.ITEMS_12_COMIDAS2, 54),

	CUENCO_AGUA_HERVIDA_INV(ClaveHoja.ITEMS_16_COMIDAS2, 55), CUENCO_AGUA_HERVIDA_MAPA(ClaveHoja.ITEMS_12_COMIDAS2, 55),

	// --- Herramientas ----
	PICO_BASICO_INV(ClaveHoja.ITEMS_16, 70), PICO_BASICO_MAPA(ClaveHoja.ITEMS_12, 70),
	HACHA_BASICO_INV(ClaveHoja.ITEMS_16, 73), HACHA_BASICO_MAPA(ClaveHoja.ITEMS_12, 73),

	// --- Armas de Fuego (16x16 Inventario / 8x8 HUD y Suelo) ---
	PISTOLA_INV(ClaveHoja.PISTOLA_16, 0), PISTOLA_MAPA(ClaveHoja.PISTOLA_8, 0),
	ESCOPETA_RECORTADA_INV(ClaveHoja.ARMAS_PACK_16, 11), ESCOPETA_RECORTADA_MAPA(ClaveHoja.ARMAS_PACK_16, 11),

	KIT_CARPA_INV(ClaveHoja.ITEMS_16, 140), KIT_CARPA_MAPA(ClaveHoja.ITEMS_12, 140),
	KIT_CAMA_INV(ClaveHoja.ITEMS_16, 169), KIT_CAMA_MAPA(ClaveHoja.ITEMS_12, 169),
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