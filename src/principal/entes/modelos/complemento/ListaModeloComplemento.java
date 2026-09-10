package principal.entes.modelos.complemento;

import java.awt.Rectangle;
import java.util.HashMap;

import principal.recursos.ClaveHoja;
import principal.utilidades.Globales;
import principal.utilidades.HojaSprite;

/**
 * Catálogo maestro Flyweight de complementos con discriminación eólica de
 * vegetación (Zero-GC / O(1)).
 * 
 * @version 2.0 (Vanilla Java 8 - Vegetation Wind Catalog Setup)
 */
public class ListaModeloComplemento {

	private static final HashMap<Integer, ModeloComplemento> LISTA_MODELOS = new HashMap<Integer, ModeloComplemento>();

	public static final int COD_BARRERA_INVISIBLE = 0;
	public static final int COD_ARBOL_1 = 1;
	public static final int COD_ARBOL_2 = 2;
	public static final int COD_ARBOL_3 = 13;
	public static final int COD_ARBOL_4 = 14;
	public static final int COD_ARBOL_1_NEVADO = 15;
	public static final int COD_ARBOL_2_NEVADO = 16;
	public static final int COD_ARBOL_3_NEVADO = 17;
	public static final int COD_ARBOL_4_NEVADO = 18;
	public static final int COD_CASA_1 = 19;

	static {
		final HojaSprite arboles = Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.ARBOLES_32);
		final HojaSprite nevados = Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.ARBOLES_NEVADOS_32);
		final HojaSprite casa = Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.CASA_1);

		// 0. Barrera Invisible (Estructura Rígida)
		LISTA_MODELOS.put(COD_BARRERA_INVISIBLE, new ModeloComplementoT1(32,
				Globales.GESTOR_TEXTURAS.getTexturaTransparente(), true, false, new Rectangle()));

		// 1. Árboles Templados (Vegetación = true)
		LISTA_MODELOS.put(COD_ARBOL_1,
				new ModeloComplementoT1(32, arboles.getSprite(0), true, true, new Rectangle(12, 18, 11, 0)) {
					@Override
					public boolean esVegetacion() {
						return true;
					}
				});

		LISTA_MODELOS.put(COD_ARBOL_2,
				new ModeloComplementoT1(32, arboles.getSprite(1), true, true, new Rectangle(11, 18, 10, 0)) {
					@Override
					public boolean esVegetacion() {
						return true;
					}
				});

		LISTA_MODELOS.put(COD_ARBOL_3,
				new ModeloComplementoT1(32, arboles.getSprite(2), true, true, new Rectangle(13, 19, 14, 0)) {
					@Override
					public boolean esVegetacion() {
						return true;
					}
				});

		LISTA_MODELOS.put(COD_ARBOL_4,
				new ModeloComplementoT1(32, arboles.getSprite(3), true, true, new Rectangle(12, 18, 11, 0)) {
					@Override
					public boolean esVegetacion() {
						return true;
					}
				});

		// 2. Árboles Nevados (Vegetación = true)
		LISTA_MODELOS.put(COD_ARBOL_1_NEVADO,
				new ModeloComplementoT1(32, nevados.getSprite(0), true, true, new Rectangle(12, 18, 11, 0)) {
					@Override
					public boolean esVegetacion() {
						return true;
					}
				});

		LISTA_MODELOS.put(COD_ARBOL_2_NEVADO,
				new ModeloComplementoT1(32, nevados.getSprite(1), true, true, new Rectangle(11, 18, 10, 0)) {
					@Override
					public boolean esVegetacion() {
						return true;
					}
				});

		LISTA_MODELOS.put(COD_ARBOL_3_NEVADO,
				new ModeloComplementoT1(32, nevados.getSprite(2), true, true, new Rectangle(8, 18, 8, 0)) {
					@Override
					public boolean esVegetacion() {
						return true;
					}
				});

		LISTA_MODELOS.put(COD_ARBOL_4_NEVADO,
				new ModeloComplementoT1(32, nevados.getSprite(3), true, true, new Rectangle(12, 18, 11, 0)) {
					@Override
					public boolean esVegetacion() {
						return true;
					}
				});

		// 3. Casas y Estructuras Rígidas (Vegetación = false por defecto)
		LISTA_MODELOS.put(COD_CASA_1,
				new ModeloComplementoT1(64, 64, casa.getSprite(0), true, true, new Rectangle(5, 43, 6, 0)));
	}

	public static ModeloComplemento getModeloComplemento(final int codModeloComplemento) {
		return LISTA_MODELOS.get(codModeloComplemento);
	}
}