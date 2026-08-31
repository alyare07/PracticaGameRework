package principal.entes.modelos.item;

import java.awt.Rectangle;
import java.util.HashMap;

import principal.utilidades.Textura;

/**
 * Catálogo maestro de definición de modelos de ítems (Flyweight Pattern).
 * Almacena los metadatos, dimensiones y códigos de textura para consumibles,
 * municiones y armas.
 * 
 * @version 2.6 (Java 8 Compatible)
 */
public class ListaModelosItem {

	// =========================================================================
	// === 1. MODELOS DE ÍTEMS CONSUMIBLES Y MUNICIONES (STACKABLE)
	// =========================================================================

	public static final HashMap<String, ModeloConsumible> LISTA_MODELOS_CONSUMIBLES = new HashMap<String, ModeloConsumible>();

	// --- Pociones y Arrojadizos ---
	public static final String COD_CONSUMIBLE_POCION_VIDA_MENOR = "Pocion Vida Menor";
	public static final String COD_CONSUMIBLE_POCION_RESISTENCIA = "Pocion Resistencia Menor";
	public static final String COD_CONSUMIBLE_GRANADAT1 = "Granada T1";

	// --- Cajas de Munición de Reserva para Inventario ---
	public static final String COD_CONSUMIBLE_MUNICION_PISTOLA = "Caja Municion 9mm";
	public static final String COD_CONSUMIBLE_MUNICION_ESCOPETA = "Caja Cartuchos Calibre 12";
	public static final String COD_CONSUMIBLE_MUNICION_FUSIL = "Caja Municion 7.62mm";
	public static final String COD_CONSUMIBLE_MUNICION_PESADA = "Caja Municion Pesada";

	static {
		// Pociones y Granadas
		LISTA_MODELOS_CONSUMIBLES.put(COD_CONSUMIBLE_POCION_VIDA_MENOR, new ModeloConsumible("Pocion vida menor", 10,
				10, false, new Rectangle(), Textura.TEXTURA_x16_POCION_ROJA, Textura.TEXTURA_x10_POCION_ROJA));

		LISTA_MODELOS_CONSUMIBLES.put(COD_CONSUMIBLE_POCION_RESISTENCIA, new ModeloConsumible("Pocion resistencia", 10,
				99, false, new Rectangle(), Textura.TEXTURA_x16_POCION_AZUL, Textura.TEXTURA_x10_POCION_AZUL));

		LISTA_MODELOS_CONSUMIBLES.put(COD_CONSUMIBLE_GRANADAT1,
				new ModeloGranada("Granada T1", 10, 50, false, new Rectangle(), Textura.TEXTURA_X16_GRANADA_1,
						Textura.TEXTURA_X10_GRANADA_1, Textura.TEXTURA_x50_EXPLOSION));

		// Cajas de Munición con textura asignada TEXTURA_x4_BALA
		LISTA_MODELOS_CONSUMIBLES.put(COD_CONSUMIBLE_MUNICION_PISTOLA,
				new ModeloConsumible("Munición 9mm (Pistola/SMG)", 8, 150, false, new Rectangle(),
						Textura.TEXTURA_x4_BALA, Textura.TEXTURA_x4_BALA));

		LISTA_MODELOS_CONSUMIBLES.put(COD_CONSUMIBLE_MUNICION_ESCOPETA, new ModeloConsumible("Cartuchos Calibre 12", 8,
				64, false, new Rectangle(), Textura.TEXTURA_x4_BALA, Textura.TEXTURA_x4_BALA));

		LISTA_MODELOS_CONSUMIBLES.put(COD_CONSUMIBLE_MUNICION_FUSIL, new ModeloConsumible("Munición 7.62mm (Rifle)", 8,
				180, false, new Rectangle(), Textura.TEXTURA_x4_BALA, Textura.TEXTURA_x4_BALA));

		LISTA_MODELOS_CONSUMIBLES.put(COD_CONSUMIBLE_MUNICION_PESADA,
				new ModeloConsumible("Cinta Munición Pesada (LMG)", 8, 300, false, new Rectangle(),
						Textura.TEXTURA_x4_BALA, Textura.TEXTURA_x4_BALA));
	}

	public static ModeloConsumible getModeloConsumible(final String codModelo) {
		return LISTA_MODELOS_CONSUMIBLES.get(codModelo);
	}

	// =========================================================================
	// === 2. MODELOS DE ÍTEMS PORTABLES / EQUIPABLES (ARMAS Y ARMADURAS)
	// =========================================================================

	public static final HashMap<String, ModeloPortable> LISTA_MODELOS_PORTABLE = new HashMap<String, ModeloPortable>();

	public static final String COD_PORTABLE_BOTAS = "Botas";
	public static final String COD_EQUIPABLE_ARMA = "Pistola";

	public static final String COD_ARMA_ESCOPETA_RECORTADA = "Escopeta Recortada";
	public static final String COD_ARMA_ESCOPETA_TACTICA = "Escopeta Tactica";
	public static final String COD_ARMA_ESCOPETA_AUTOMATICA = "Escopeta Automatica";

	public static final String COD_ARMA_SUBFUSIL_LIGERO = "Subfusil Ligero";
	public static final String COD_ARMA_RIFLE_ASALTO = "Rifle de Asalto";
	public static final String COD_ARMA_AMETRALLADORA_PESADA = "Ametralladora Pesada";

	static {
		LISTA_MODELOS_PORTABLE.put(COD_PORTABLE_BOTAS, new ModeloPortable("BOTAS LIGERAS", 10, false, new Rectangle(),
				Textura.TEXTURA_x16_BOTAS_CUERO_MARRON, Textura.TEXTURA_x10_BOTAS_CUERO_MARRON));

		LISTA_MODELOS_PORTABLE.put(COD_EQUIPABLE_ARMA, new ModeloPortable("Pistola", 8, false, new Rectangle(),
				Textura.TEXTURA_x16_PISTOLA, Textura.TEXTURA_x8_PISTOLA));

		LISTA_MODELOS_PORTABLE.put(COD_ARMA_ESCOPETA_RECORTADA, new ModeloPortable("Escopeta Recortada", 10, false,
				new Rectangle(), Textura.TEXTURA_x16_PISTOLA, Textura.TEXTURA_x8_PISTOLA));

		LISTA_MODELOS_PORTABLE.put(COD_ARMA_ESCOPETA_TACTICA, new ModeloPortable("Escopeta Táctica", 10, false,
				new Rectangle(), Textura.TEXTURA_x16_PISTOLA, Textura.TEXTURA_x8_PISTOLA));

		LISTA_MODELOS_PORTABLE.put(COD_ARMA_ESCOPETA_AUTOMATICA, new ModeloPortable("Escopeta Automática", 10, false,
				new Rectangle(), Textura.TEXTURA_x16_PISTOLA, Textura.TEXTURA_x8_PISTOLA));

		LISTA_MODELOS_PORTABLE.put(COD_ARMA_SUBFUSIL_LIGERO, new ModeloPortable("Subfusil Ligero", 10, false,
				new Rectangle(), Textura.TEXTURA_x16_PISTOLA, Textura.TEXTURA_x8_PISTOLA));

		LISTA_MODELOS_PORTABLE.put(COD_ARMA_RIFLE_ASALTO, new ModeloPortable("Rifle de Asalto", 10, false,
				new Rectangle(), Textura.TEXTURA_x16_PISTOLA, Textura.TEXTURA_x8_PISTOLA));

		LISTA_MODELOS_PORTABLE.put(COD_ARMA_AMETRALLADORA_PESADA, new ModeloPortable("Ametralladora Pesada", 10, false,
				new Rectangle(), Textura.TEXTURA_x16_PISTOLA, Textura.TEXTURA_x8_PISTOLA));
	}

	public static ModeloPortable getModeloPortable(final String codModelo) {
		return LISTA_MODELOS_PORTABLE.get(codModelo);
	}
}