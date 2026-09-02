package principal.entes.modelos.item;

import java.awt.Rectangle;
import java.util.HashMap;

import principal.utilidades.Textura;

public class ListaModelosItem {

	// =========================================================================
	// === 1. CONSUMIBLES, MUNICIONES Y MATERIALES
	// =========================================================================

	public static final HashMap<String, ModeloConsumible> LISTA_MODELOS_CONSUMIBLES = new HashMap<String, ModeloConsumible>();

	public static final String COD_CONSUMIBLE_POCION_VIDA_MENOR = "Pocion Vida Menor";
	public static final String COD_CONSUMIBLE_POCION_RESISTENCIA = "Pocion Resistencia Menor";
	public static final String COD_CONSUMIBLE_GRANADAT1 = "Granada T1";

	public static final String COD_CONSUMIBLE_MUNICION_PISTOLA = "Caja Municion 9mm";
	public static final String COD_CONSUMIBLE_MUNICION_ESCOPETA = "Caja Cartuchos Calibre 12";
	public static final String COD_CONSUMIBLE_MUNICION_FUSIL = "Caja Municion 7.62mm";
	public static final String COD_CONSUMIBLE_MUNICION_PESADA = "Caja Municion Pesada";

	public static final String COD_RECURSO_MADERA = "Madera";
	public static final String COD_RECURSO_PIEDRA = "Piedra";

	static {
		LISTA_MODELOS_CONSUMIBLES.put(COD_CONSUMIBLE_POCION_VIDA_MENOR, new ModeloConsumible("Pocion vida menor", 10,
				10, false, new Rectangle(), Textura.TEXTURA_x16_POCION_ROJA, Textura.TEXTURA_x10_POCION_ROJA));

		LISTA_MODELOS_CONSUMIBLES.put(COD_CONSUMIBLE_POCION_RESISTENCIA, new ModeloConsumible("Pocion resistencia", 10,
				99, false, new Rectangle(), Textura.TEXTURA_x16_POCION_AZUL, Textura.TEXTURA_x10_POCION_AZUL));

		LISTA_MODELOS_CONSUMIBLES.put(COD_CONSUMIBLE_GRANADAT1,
				new ModeloGranada("Granada T1", 10, 50, false, new Rectangle(), Textura.TEXTURA_X16_GRANADA_1,
						Textura.TEXTURA_X10_GRANADA_1, Textura.TEXTURA_x50_EXPLOSION));

		LISTA_MODELOS_CONSUMIBLES.put(COD_CONSUMIBLE_MUNICION_PISTOLA,
				new ModeloConsumible("Munición 9mm (Pistola/SMG)", 8, 150, false, new Rectangle(),
						Textura.TEXTURA_x16_CAJA_MUNICION, Textura.TEXTURA_x8_CAJA_MUNICION));

		LISTA_MODELOS_CONSUMIBLES.put(COD_CONSUMIBLE_MUNICION_ESCOPETA, new ModeloConsumible("Cartuchos Calibre 12", 8,
				64, false, new Rectangle(), Textura.TEXTURA_x16_CAJA_MUNICION, Textura.TEXTURA_x8_CAJA_MUNICION));

		LISTA_MODELOS_CONSUMIBLES.put(COD_CONSUMIBLE_MUNICION_FUSIL, new ModeloConsumible("Munición 7.62mm (Rifle)", 8,
				180, false, new Rectangle(), Textura.TEXTURA_x16_CAJA_MUNICION, Textura.TEXTURA_x8_CAJA_MUNICION));

		LISTA_MODELOS_CONSUMIBLES.put(COD_CONSUMIBLE_MUNICION_PESADA,
				new ModeloConsumible("Cinta Munición Pesada (LMG)", 8, 300, false, new Rectangle(),
						Textura.TEXTURA_x16_CAJA_MUNICION, Textura.TEXTURA_x8_CAJA_MUNICION));

		LISTA_MODELOS_CONSUMIBLES.put(COD_RECURSO_MADERA, new ModeloConsumible("Tronco de Madera", 10, 999, false,
				new Rectangle(), Textura.TEXTURA_x16_BOTAS_CUERO_MARRON, Textura.TEXTURA_x10_BOTAS_CUERO_MARRON));

		LISTA_MODELOS_CONSUMIBLES.put(COD_RECURSO_PIEDRA, new ModeloConsumible("Piedra Bruta", 10, 999, false,
				new Rectangle(), Textura.TEXTURA_x16_ANILLO_PLATA, Textura.TEXTURA_x10_POCION_AZUL));
	}

	public static ModeloConsumible getModeloConsumible(final String codModelo) {
		return LISTA_MODELOS_CONSUMIBLES.get(codModelo);
	}

	// =========================================================================
	// === 2. PORTABLES, EQUIPO Y ARMAS
	// =========================================================================

	public static final HashMap<String, ModeloPortable> LISTA_MODELOS_PORTABLE = new HashMap<String, ModeloPortable>();

	public static final String COD_PORTABLE_BOTAS_CUERO = "Botas Cuero";
	public static final String COD_EQUIPABLE_CASCO_LIGERA = "Casco Ligero";
	public static final String COD_EQUIPABLE_ARMADURA_LIGERA = "Armadura Ligera";
	public static final String COD_EQUIPABLE_ANILLO_ORO = "Anillo de Oro";
	public static final String COD_EQUIPABLE_ANILLO_PLATA = "Anillo de Plata";

	public static final String COD_EQUIPABLE_ARMA = "Pistola";
	public static final String COD_HERRAMIENTA_HACHA = "Hacha de Tala";
	public static final String COD_HERRAMIENTA_PICO = "Pico de Minería";

	public static final String COD_ARMA_ESCOPETA_RECORTADA = "Escopeta Recortada";
	public static final String COD_ARMA_ESCOPETA_TACTICA = "Escopeta Tactica";
	public static final String COD_ARMA_ESCOPETA_AUTOMATICA = "Escopeta Automatica";

	public static final String COD_ARMA_SUBFUSIL_LIGERO = "Subfusil Ligero";
	public static final String COD_ARMA_RIFLE_ASALTO = "Rifle de Asalto";
	public static final String COD_ARMA_AMETRALLADORA_PESADA = "Ametralladora Pesada";

	static {
		// Piezas de Armadura y Joyería RPG
		LISTA_MODELOS_PORTABLE.put(COD_PORTABLE_BOTAS_CUERO, new ModeloPortable("Botas de Cuero", 10, false,
				new Rectangle(), Textura.TEXTURA_x16_BOTAS_CUERO_MARRON, Textura.TEXTURA_x10_BOTAS_CUERO_MARRON));

		LISTA_MODELOS_PORTABLE.put(COD_EQUIPABLE_CASCO_LIGERA, new ModeloPortable("Casco Ligero", 10, false,
				new Rectangle(), Textura.TEXTURA_x16_CASCO_BASE, Textura.TEXTURA_x10_CASCO_BASE));

		LISTA_MODELOS_PORTABLE.put(COD_EQUIPABLE_ARMADURA_LIGERA, new ModeloPortable("Armadura Ligera", 10, false,
				new Rectangle(), Textura.TEXTURA_x16_ARMADURA_BASE, Textura.TEXTURA_x10_ARMADURA_BASE));

		LISTA_MODELOS_PORTABLE.put(COD_EQUIPABLE_ANILLO_ORO, new ModeloPortable("Anillo de Oro Fino", 10, false,
				new Rectangle(), Textura.TEXTURA_x16_ANILLO_ORO, Textura.TEXTURA_x10_ANILLO_ORO));

		LISTA_MODELOS_PORTABLE.put(COD_EQUIPABLE_ANILLO_PLATA, new ModeloPortable("Anillo de Plata", 10, false,
				new Rectangle(), Textura.TEXTURA_x16_ANILLO_PLATA, Textura.TEXTURA_x10_ANILLO_PLATA));

		// Herramientas y Armas
		LISTA_MODELOS_PORTABLE.put(COD_HERRAMIENTA_HACHA, new ModeloPortable("Hacha de Tala", 10, false,
				new Rectangle(), Textura.TEXTURA_x16_ESMERALDA, Textura.TEXTURA_x10_BOTAS_CUERO_MARRON));

		LISTA_MODELOS_PORTABLE.put(COD_HERRAMIENTA_PICO, new ModeloPortable("Pico de Minería", 10, false,
				new Rectangle(), Textura.TEXTURA_x16_ANILLO_ORO, Textura.TEXTURA_x10_POCION_AZUL));

		LISTA_MODELOS_PORTABLE.put(COD_EQUIPABLE_ARMA, new ModeloPortable("Pistola", 8, false, new Rectangle(),
				Textura.TEXTURA_x16_PISTOLA, Textura.TEXTURA_x8_PISTOLA));

		LISTA_MODELOS_PORTABLE.put(COD_ARMA_ESCOPETA_RECORTADA, new ModeloPortable("Escopeta Recortada", 10, false,
				new Rectangle(), Textura.TEXTURA_x16_ESCOPETA_RECORTADA, Textura.TEXTURA_x8_ESCOPETA_RECORTADA));

		LISTA_MODELOS_PORTABLE.put(COD_ARMA_ESCOPETA_TACTICA, new ModeloPortable("Escopeta Táctica", 10, false,
				new Rectangle(), Textura.TEXTURA_x16_ESCOPETA_TACTICA, Textura.TEXTURA_x8_ESCOPETA_TACTICA));

		LISTA_MODELOS_PORTABLE.put(COD_ARMA_ESCOPETA_AUTOMATICA, new ModeloPortable("Escopeta Automática", 10, false,
				new Rectangle(), Textura.TEXTURA_x16_ESCOPETA_AUTOMATICA, Textura.TEXTURA_x8_ESCOPETA_AUTOMATICA));

		LISTA_MODELOS_PORTABLE.put(COD_ARMA_SUBFUSIL_LIGERO, new ModeloPortable("Subfusil Ligero", 10, false,
				new Rectangle(), Textura.TEXTURA_x16_SUBFUSIL_LIGERO, Textura.TEXTURA_x8_SUBFUSIL_LIGERO));

		LISTA_MODELOS_PORTABLE.put(COD_ARMA_RIFLE_ASALTO, new ModeloPortable("Rifle de Asalto", 10, false,
				new Rectangle(), Textura.TEXTURA_x16_RIFLE_DE_ASALTO, Textura.TEXTURA_x8_RIFLE_DE_ASALTO));

		LISTA_MODELOS_PORTABLE.put(COD_ARMA_AMETRALLADORA_PESADA, new ModeloPortable("Ametralladora Pesada", 10, false,
				new Rectangle(), Textura.TEXTURA_x16_AMETRALLADORA_PESADA, Textura.TEXTURA_x8_AMETRALLADORA_PESADA));
	}

	public static ModeloPortable getModeloPortable(final String codModelo) {
		return LISTA_MODELOS_PORTABLE.get(codModelo);
	}
}