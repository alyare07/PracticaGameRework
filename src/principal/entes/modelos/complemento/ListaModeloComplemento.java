package principal.entes.modelos.complemento;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.HashMap;

import principal.utilidades.Animacion;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.Textura;

public class ListaModeloComplemento {
	private static final HashMap<Integer, ModeloComplemento> LISTA_MODELOS = new HashMap<Integer, ModeloComplemento>();
	public static final int COD_BARRERA_INVISIBLE = 0;
	public static final int COD_ARBOL_1 = 1;
	public static final int COD_ARBOL_2 = 2;
	public static final int COD_AGUA_VERTICAL_X16 = 3;
	public static final int COD_AGUA_VERTICAL_X16_2 = 4;
	public static final int COD_AGUA_VERTICAL_X16_3 = 5;
	public static final int COD_AGUA_VERTICAL_X16_4 = 6;
	public static final int COD_AGUA_HORIZONTAL_X16 = 7;
	public static final int COD_AGUA_HORIZONTAL_X16_2 = 8;
	public static final int COD_AGUA_HORIZONTAL_X16_3 = 9;
	public static final int COD_AGUA_HORIZONTAL_X16_4 = 10;
	public static final int COD_AGUA_HORIZONTAL_X16_5 = 11;
	public static final int COD_AGUA_HORIZONTAL_X16_6 = 12;
	public static final int COD_ARBOL_3 = 13;
	public static final int COD_ARBOL_4 = 14;
	public static final int COD_ARBOL_1_NEVADO = 15;
	public static final int COD_ARBOL_2_NEVADO = 16;
	public static final int COD_ARBOL_3_NEVADO = 17;
	public static final int COD_ARBOL_4_NEVADO = 18;
	public static final int COD_CASA_1 = 19;
	static {
		LISTA_MODELOS.put(COD_BARRERA_INVISIBLE, new ModeloComplementoT1(32, 0, true, false, new Rectangle()));
		LISTA_MODELOS.put(COD_ARBOL_1,
				new ModeloComplementoT1(32, Textura.TEXTURA_x32_ARBOL_1, true, false, new Rectangle(12, 2, 11, 0)));
		LISTA_MODELOS.put(COD_ARBOL_2,
				new ModeloComplementoT1(32, Textura.TEXTURA_x32_ARBOL_2, true, false, new Rectangle(11, 0, 10, 0)));
		LISTA_MODELOS.put(COD_ARBOL_3,
				new ModeloComplementoT1(32, Textura.TEXTURA_x32_ARBOL_3, true, true, new Rectangle(13, 19, 14, 0)));
		LISTA_MODELOS.put(COD_ARBOL_4,
				new ModeloComplementoT1(32, Textura.TEXTURA_x32_ARBOL_0, true, false, new Rectangle(), new Animacion() {

					@Override
					public void pintar(final Graphics2D g, final int x, final int y) {
						final int resto = Globales.animacion % 400;
						if ((resto >= 100) && (resto <= 200)) {
							Render2D.dibujarImagenRefCamara(g, Textura.getTextura(Textura.TEXTURA_x32_ARBOL_0), x, y);
						} else if ((resto > 200) && (resto <= 300)) {
							Render2D.dibujarImagenRefCamara(g, Textura.getTextura(Textura.TEXTURA_x32_ARBOL_1), x, y);
						} else if ((resto > 300) && (resto <= 400)) {
							Render2D.dibujarImagenRefCamara(g, Textura.getTextura(Textura.TEXTURA_x32_ARBOL_2), x, y);
						} else {
							Render2D.dibujarImagenRefCamara(g, Textura.getTextura(Textura.TEXTURA_x32_ARBOL_3), x, y);
						}
					}
				}));

		LISTA_MODELOS.put(COD_ARBOL_1_NEVADO, new ModeloComplementoT1(32, Textura.TEXTURA_x32_ARBOL_1_NEVADO, true,
				false, new Rectangle(12, 0, 11, 0)));
		LISTA_MODELOS.put(COD_ARBOL_2_NEVADO, new ModeloComplementoT1(32, Textura.TEXTURA_x32_ARBOL_2_NEVADO, true,
				false, new Rectangle(11, 0, 10, 0)));
		LISTA_MODELOS.put(COD_ARBOL_3_NEVADO, new ModeloComplementoT1(32, Textura.TEXTURA_x32_ARBOL_3_NEVADO, true,
				false, new Rectangle(8, 0, 8, 0)));
		LISTA_MODELOS.put(COD_ARBOL_4_NEVADO, new ModeloComplementoT1(32, Textura.TEXTURA_x32_ARBOL_0_NEVADO, true,
				false, new Rectangle(12, 0, 11, 0)));

		LISTA_MODELOS.put(COD_CASA_1,
				new ModeloComplementoT1(64, 64, Textura.TEXTURA_X64_CASA1, true, true, new Rectangle(5, 43, 6, 0)));
	}

	public static ModeloComplemento getModeloComplemento(final int codModeloComplemento) {
		return LISTA_MODELOS.get(codModeloComplemento);
	}

}
