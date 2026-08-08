package principal.entes.modelos.tile;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import principal.animaciones.Animacion;
import principal.utilidades.Constantes;
import principal.utilidades.HojaSprite;
import principal.utilidades.Textura;

public final class ListaModeloTile {
	private static final HashMap<Integer, ModeloTile> LISTA_MODELOS = new HashMap<Integer, ModeloTile>();
	private static int nextId = 1;

	private static int getSiguienteID() {
		return nextId++;
	}

	public static int getUltimoIdUsado() {
		return nextId - 1;
	}

	public static final int COD_CESPED = getSiguienteID();
	public static final int COD_ASFALTO = getSiguienteID();
	public static final int COD_ARENA = getSiguienteID();
	public static final int COD_PIEDRA = getSiguienteID();
	public static final int COD_VACIO = getSiguienteID();
	public static final int COD_AGUA = getSiguienteID();
	public static final int COD_TIERRA = getSiguienteID();
	public static final int COD_TIERRA_2 = getSiguienteID();
	public static final int COD_CESPED_2 = getSiguienteID();
	public static final int COD_CESPED_3 = getSiguienteID();
	public static final int COD_CESPED_3_NEVADO = getSiguienteID();

	// agregar dichos modelos
	static {
		LISTA_MODELOS.put(COD_CESPED, new ModeloTile(ModeloTile.ESTADO_TRASPASABLE, Textura.TEXTURA_x32_CESPED, 0));
		LISTA_MODELOS.put(COD_ASFALTO, new ModeloTile(ModeloTile.ESTADO_TRASPASABLE, Textura.TEXTURA_x32_ASFALTO, 0.25));
		LISTA_MODELOS.put(COD_ARENA, new ModeloTile(ModeloTile.ESTADO_TRASPASABLE, Textura.TEXTURA_x32_ARENA, -0.2));
		LISTA_MODELOS.put(COD_PIEDRA, new ModeloTile(ModeloTile.ESTADO_OBSTACULO, Textura.TEXTURA_x32_PIEDRA, 0));
		LISTA_MODELOS.put(COD_VACIO, new ModeloTile(ModeloTile.ESTADO_OBSTACULO, Textura.TEXTURA_x32_VACIO, -1));
		LISTA_MODELOS.put(COD_TIERRA, new ModeloTile(ModeloTile.ESTADO_TRASPASABLE, Textura.TEXTURA_x32_TIERRA, 0));
		LISTA_MODELOS.put(COD_TIERRA_2, new ModeloTile(ModeloTile.ESTADO_TRASPASABLE, Textura.TEXTURA_x32_TIERRA_2, 0.2));
		LISTA_MODELOS.put(COD_CESPED_2, new ModeloTile(ModeloTile.ESTADO_TRASPASABLE, Textura.TEXTURA_x32_CESPED_2, 0));
		LISTA_MODELOS.put(COD_CESPED_3, new ModeloTile(ModeloTile.ESTADO_TRASPASABLE, Textura.TEXTURA_x32_CESPED_3, 0));
		LISTA_MODELOS.put(COD_CESPED_3_NEVADO, new ModeloTile(ModeloTile.ESTADO_TRASPASABLE, Textura.TEXTURA_x32_CESPED_3_NEVADO, -0.25));
		{
			HojaSprite HOJA_TERRENO = new HojaSprite("/imagenes/texturas/textura.png", 32, true);
			BufferedImage img = new BufferedImage(Textura.getTextura(Textura.TEXTURA_x32_AGUA_1).getWidth()*3, Textura.getTextura(Textura.TEXTURA_x32_AGUA_1).getWidth(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = (Graphics2D)img.getGraphics();
			g.drawImage(Constantes.FUNCIONES.TEXTURAS_TOOLS.redimensionar(HOJA_TERRENO.getSprite(9),Textura.getTextura(Textura.TEXTURA_x32_AGUA_1).getWidth(), Textura.getTextura(Textura.TEXTURA_x32_AGUA_1).getWidth()), 0, 0, null);
			g.drawImage(Constantes.FUNCIONES.TEXTURAS_TOOLS.redimensionar(HOJA_TERRENO.getSprite(10),Textura.getTextura(Textura.TEXTURA_x32_AGUA_1).getWidth(), Textura.getTextura(Textura.TEXTURA_x32_AGUA_1).getWidth()), Textura.getTextura(Textura.TEXTURA_x32_AGUA_1).getWidth(), 0, null);
			g.drawImage(Constantes.FUNCIONES.TEXTURAS_TOOLS.redimensionar(HOJA_TERRENO.getSprite(11),Textura.getTextura(Textura.TEXTURA_x32_AGUA_1).getWidth(), Textura.getTextura(Textura.TEXTURA_x32_AGUA_1).getWidth()), Textura.getTextura(Textura.TEXTURA_x32_AGUA_1).getWidth()*2, 0, null);
			g.dispose();
			Animacion animacionAgua = new Animacion(new HojaSprite(img, Textura.getTextura(Textura.TEXTURA_x32_AGUA_1).getWidth(), true), true, 750);
			ModeloTile agua = new ModeloTile(ModeloTile.ESTADO_OBSTACULO, animacionAgua, -0.5, Textura.TEXTURA_x32_AGUA_1);
			LISTA_MODELOS.put(COD_AGUA, agua);
		}
//		LISTA_MODELOS.get(COD_AGUA).establecerTexturas(Textura.TEXTURA_x32_AGUA_1, Textura.TEXTURA_x32_AGUA_2,
//				Textura.TEXTURA_x32_AGUA_3);
	}

	public static ModeloTile getModelo(final int codModeloTile) {
		return LISTA_MODELOS.get(codModeloTile);
	}


}
