package principal.entes.modelos.item;

import java.awt.Rectangle;
import java.util.HashMap;

import principal.utilidades.Textura;

public class ListaModelosItem {

	//-------------------------------------------------------------HAY QUE MEJORAR LOS ID. QUE SEAN ID FIJOS, PUEDEN SER STRING!

	// -------- MODELOS ITEMS CONSUMIBLES -----------------
	public static final HashMap<String, ModeloConsumible> LISTA_MODELOS_CONSUMIBLES = new HashMap<String, ModeloConsumible>();

	public static final String COD_CONSUMIBLE_POCION_VIDA_MENOR = "Pocion Vida Menor";
	public static final String COD_CONSUMIBLE_POCION_RESISTENCIA = "Pocion Resistencia Menor";
	public static final String COD_CONSUMIBLE_GRANADAT1 = "Granada T1";
	static {
		LISTA_MODELOS_CONSUMIBLES.put(COD_CONSUMIBLE_POCION_VIDA_MENOR,
				new ModeloConsumible("Pocion vida menor", 10, 10, false, new Rectangle(), Textura.TEXTURA_x16_POCION_ROJA, Textura.TEXTURA_x10_POCION_ROJA));
		LISTA_MODELOS_CONSUMIBLES.put(COD_CONSUMIBLE_POCION_RESISTENCIA,
				new ModeloConsumible("Pocion resistencia", 10, 99, false, new Rectangle(), Textura.TEXTURA_x16_POCION_AZUL, Textura.TEXTURA_x10_POCION_AZUL));
		LISTA_MODELOS_CONSUMIBLES.put(COD_CONSUMIBLE_GRANADAT1,
				new ModeloGranada("Granada T1", 10, 50, false, new Rectangle(), Textura.TEXTURA_X16_GRANADA_1, Textura.TEXTURA_X10_GRANADA_1, Textura.TEXTURA_x50_EXPLOSION));
	}

	public static ModeloConsumible getModeloConsumible(final String codModelo) {
		return LISTA_MODELOS_CONSUMIBLES.get(codModelo);
	}

	// -------- MODELOS ITEMS PORTABLES -----------------

	public static final HashMap<String, ModeloPortable> LISTA_MODELOS_PORTABLE = new HashMap<String, ModeloPortable>();
	public static final String COD_EQUIPABLE_ARMA = "Pistola";
	public static final String COD_PORTABLE_BOTAS = "Botas";
	

	static {
		LISTA_MODELOS_PORTABLE.put(COD_PORTABLE_BOTAS,
				new ModeloPortable("BOTAS LIGERAS", 10, false, new Rectangle(), Textura.TEXTURA_x16_BOTAS_CUERO_MARRON, Textura.TEXTURA_x10_BOTAS_CUERO_MARRON));
		LISTA_MODELOS_PORTABLE.put(COD_EQUIPABLE_ARMA,
				new ModeloPortable("Pistola", 8, false, new Rectangle(), Textura.TEXTURA_x16_PISTOLA, Textura.TEXTURA_x8_PISTOLA));
	}
	
	public static ModeloPortable getModeloPortable(final String codModelo) {
		return LISTA_MODELOS_PORTABLE.get(codModelo);
	}
}
