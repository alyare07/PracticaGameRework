package principal.utilidades;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public final class Textura {

	private static final int INICIO_TERRENO = 1;
	private static final int INICIO_SUB_TERRENO = 500;
	private static final int INICIO_OBJETOS = 1000;
	private static final int INICIO_ITEMS = 5000;
	private static final int INICIO_PARTICULAS = 15000;
	private static final int INICIO_EFECTOS = 50000;
	private static final int INICIO_ESTRUCTURAS = 150000;
	public static final int TEXTURA_x32_VACIO = 0;
	
	public static final int TEXTURA_TRANSPARENTE = -1;
	private static int idSiguienteItem = INICIO_ITEMS;
	private static int idSiguienteTerreno = INICIO_TERRENO;
	private static int idSiguienteSubTerreno = INICIO_SUB_TERRENO;
	private static int idSiguienteObjeto = INICIO_OBJETOS;
	private static int idSiguienteParticula = INICIO_PARTICULAS;
	private static int idSiguienteEfecto = INICIO_EFECTOS;
	private static int idSiguienteEstructura = INICIO_ESTRUCTURAS;

	private static int getSiguienteIdTerreno() {
		return idSiguienteTerreno++;
	}

	private static int getSiguienteIdSubTerreno() {
		return idSiguienteSubTerreno++;
	}

	private static int getSiguienteIdObjeto() {
		return idSiguienteObjeto++;
	}

	private static int getSiguienteIdItem() {
		return idSiguienteItem++;
	}

	private static int getSiguienteIdParticula() {
		return idSiguienteParticula++;
	}
	
	private static int getSiguienteIdEfecto() {
		return idSiguienteEfecto++;
	}
	
	private static int getSiguienteIdEstructura() {
		return idSiguienteEstructura++;
	}

	// TEXTURAS DE TERRENO 32 * 32
	public static final int TEXTURA_x32_ASFALTO = getSiguienteIdTerreno();
	public static final int TEXTURA_x32_ARENA = getSiguienteIdTerreno();
	public static final int TEXTURA_x32_PIEDRA = getSiguienteIdTerreno();
	public static final int TEXTURA_x32_CESPED = getSiguienteIdTerreno();
	public static final int TEXTURA_x32_CESPED_2 = getSiguienteIdTerreno();
	public static final int TEXTURA_x32_AGUA_1 = getSiguienteIdTerreno();
	public static final int TEXTURA_x32_TIERRA = getSiguienteIdTerreno();
	public static final int TEXTURA_x32_TIERRA_2 = getSiguienteIdTerreno();
	public static final int TEXTURA_x32_CESPED_3 = getSiguienteIdTerreno();
	public static final int TEXTURA_x32_CESPED_3_NEVADO = getSiguienteIdTerreno();
	public static final int TEXTURA_x32_AGUA_1_INV_H = getSiguienteIdTerreno();
	public static final HashMap<Integer, BufferedImage> TEXTURAS = new HashMap<Integer, BufferedImage>();
	static {
		BufferedImage vacio = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
		DibujoDebug.dibujarRectanguloRelleno((Graphics2D)vacio.getGraphics(), 0, 0, 32, 32, Color.DARK_GRAY);
		vacio.flush();
		HojaSprite HOJA_TERRENO = new HojaSprite("/imagenes/texturas/textura.png", 32, true);
		TEXTURAS.put(TEXTURA_x32_VACIO, vacio);
		TEXTURAS.put(TEXTURA_x32_ASFALTO, redimensionar(HOJA_TERRENO.getSprite(0), 16, 16));
		TEXTURAS.put(TEXTURA_x32_ARENA, redimensionar(HOJA_TERRENO.getSprite(1), 16, 16));
		TEXTURAS.put(TEXTURA_x32_PIEDRA, redimensionar(HOJA_TERRENO.getSprite(2), 16, 16));
		TEXTURAS.put(TEXTURA_x32_CESPED, redimensionar(HOJA_TERRENO.getSprite(3), 16, 16));
		TEXTURAS.put(TEXTURA_x32_AGUA_1, redimensionar(HOJA_TERRENO.getSprite(9), 16, 16));
		TEXTURAS.put(TEXTURA_x32_CESPED_2, redimensionar(HOJA_TERRENO.getSprite(5), 16, 16));
		TEXTURAS.put(TEXTURA_x32_TIERRA, redimensionar(HOJA_TERRENO.getSprite(7), 16, 16));
		TEXTURAS.put(TEXTURA_x32_TIERRA_2, redimensionar(HOJA_TERRENO.getSprite(8), 16, 16));
		TEXTURAS.put(TEXTURA_x32_CESPED_3, redimensionar(HOJA_TERRENO.getSprite(12), 16, 16));
		TEXTURAS.put(TEXTURA_x32_AGUA_1_INV_H, voltearImagenH(redimensionar(HOJA_TERRENO.getSprite(9), 16, 16)));
	}

	public static BufferedImage getTextura(final int codTextura) {
		return TEXTURAS.get(codTextura);
	}

	// TEXTURAS DE SUBTERRENO 16 * 16
	public static final int TEXTURA_x16_AGUA = getSiguienteIdSubTerreno();

	public static final int TEXTURA_x16_MURO_PIEDRA_NEGRA = getSiguienteIdSubTerreno();
	public static final int TEXTURA_x16_TIERRA = getSiguienteIdSubTerreno();
	public static final int TEXTURA_x16_CESPED = getSiguienteIdSubTerreno();
	public static final int TEXTURA_x16_AGUA_HORIZONTAL = getSiguienteIdSubTerreno();
	public static final int TEXTURA_x16_AGUA_HORIZONTAL_2 = getSiguienteIdSubTerreno();
	public static final int TEXTURA_x16_AGUA_HORIZONTAL_3 = getSiguienteIdSubTerreno();
	public static final int TEXTURA_x16_AGUA_HORIZONTAL_4 = getSiguienteIdSubTerreno();
	public static final int TEXTURA_x16_AGUA_HORIZONTAL_5 = getSiguienteIdSubTerreno();
	public static final int TEXTURA_x16_AGUA_HORIZONTAL_6 = getSiguienteIdSubTerreno();
	public static final int TEXTURA_x16_AGUA_VERTICAL = getSiguienteIdSubTerreno();
	public static final int TEXTURA_x16_AGUA_VERTICAL_2 = getSiguienteIdSubTerreno();
	public static final int TEXTURA_x16_AGUA_VERTICAL_3 = getSiguienteIdSubTerreno();
	public static final int TEXTURA_x16_AGUA_VERTICAL_4 = getSiguienteIdSubTerreno();

	static {
		final HojaSprite HOJA_SUBTERRENO = new HojaSprite("/imagenes/texturas/subterreno.png", 16, false);
		TEXTURAS.put(TEXTURA_x16_AGUA, HOJA_SUBTERRENO.getSprite(0));
		TEXTURAS.put(TEXTURA_x16_MURO_PIEDRA_NEGRA, HOJA_SUBTERRENO.getSprite(1));
		TEXTURAS.put(TEXTURA_x16_TIERRA, HOJA_SUBTERRENO.getSprite(7));
		BufferedImage cesped = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		cesped.getGraphics().drawImage(TEXTURAS.get(Textura.TEXTURA_x32_CESPED), 0, 0, 16, 16, 0, 0, 32, 32, null);
		cesped.flush();
		TEXTURAS.put(TEXTURA_x16_CESPED, cesped);
		final HojaSprite HOJA_AGUAH = new HojaSprite("/imagenes/texturas/aguaH.png", 16, 16, false);
		TEXTURAS.put(TEXTURA_x16_AGUA_HORIZONTAL, HOJA_AGUAH.getSprite(0));
		TEXTURAS.put(TEXTURA_x16_AGUA_HORIZONTAL_2, HOJA_AGUAH.getSprite(1));
		TEXTURAS.put(TEXTURA_x16_AGUA_HORIZONTAL_3, HOJA_AGUAH.getSprite(2));
		TEXTURAS.put(TEXTURA_x16_AGUA_HORIZONTAL_4, HOJA_AGUAH.getSprite(3));
		TEXTURAS.put(TEXTURA_x16_AGUA_HORIZONTAL_5, HOJA_AGUAH.getSprite(4));
		TEXTURAS.put(TEXTURA_x16_AGUA_HORIZONTAL_6, HOJA_AGUAH.getSprite(5));
		final HojaSprite HOJA_AGUAV = new HojaSprite("/imagenes/texturas/aguaV.png", 16, false);
		TEXTURAS.put(TEXTURA_x16_AGUA_VERTICAL, HOJA_AGUAV.getSprite(0));
		TEXTURAS.put(TEXTURA_x16_AGUA_VERTICAL_2, HOJA_AGUAV.getSprite(1));
		TEXTURAS.put(TEXTURA_x16_AGUA_VERTICAL_3, HOJA_AGUAV.getSprite(2));
		TEXTURAS.put(TEXTURA_x16_AGUA_VERTICAL_4, HOJA_AGUAV.getSprite(3));

	}

	// TEXTURAS DE OBJETOS

	public static final int TEXTURA_x32_ARBOL_0 = getSiguienteIdObjeto();
	public static final int TEXTURA_x32_ARBOL_1 = getSiguienteIdObjeto();
	public static final int TEXTURA_x32_ARBOL_2 = getSiguienteIdObjeto();
	public static final int TEXTURA_x32_ARBOL_3 = getSiguienteIdObjeto();

	static {
		HojaSprite HOJA_ARBOLES = new HojaSprite("/imagenes/texturas/trees.png", 32, false);
		TEXTURAS.put(TEXTURA_x32_ARBOL_1, HOJA_ARBOLES.getSprite(0));
		TEXTURAS.put(TEXTURA_x32_ARBOL_2, HOJA_ARBOLES.getSprite(1));
		TEXTURAS.put(TEXTURA_x32_ARBOL_3, HOJA_ARBOLES.getSprite(2));
		TEXTURAS.put(TEXTURA_x32_ARBOL_0, HOJA_ARBOLES.getSprite(3));

	}
	public static final int TEXTURA_x32_ARBOL_1_NEVADO = getSiguienteIdObjeto();
	public static final int TEXTURA_x32_ARBOL_2_NEVADO = getSiguienteIdObjeto();
	public static final int TEXTURA_x32_ARBOL_3_NEVADO = getSiguienteIdObjeto();
	public static final int TEXTURA_x32_ARBOL_0_NEVADO = getSiguienteIdObjeto();
	static {
		HojaSprite HOJA_ARBOLES_NEVADOS = new HojaSprite("/imagenes/texturas/treesNevado.png", 32, false);
		TEXTURAS.put(TEXTURA_x32_ARBOL_1_NEVADO, HOJA_ARBOLES_NEVADOS.getSprite(0));
		TEXTURAS.put(TEXTURA_x32_ARBOL_2_NEVADO, HOJA_ARBOLES_NEVADOS.getSprite(1));
		TEXTURAS.put(TEXTURA_x32_ARBOL_3_NEVADO, HOJA_ARBOLES_NEVADOS.getSprite(2));
		TEXTURAS.put(TEXTURA_x32_ARBOL_0_NEVADO, HOJA_ARBOLES_NEVADOS.getSprite(3));

	}

	// IMAGENES DE ITEMS

//	public static final int TEXTURA_x16_ANILLO_PLATA = getSiguienteIdItem();
//	public static final int TEXTURA_x16_ANILLO_ORO = getSiguienteIdItem();
//	public static final int TEXTURA_x16_ESMERALDA = getSiguienteIdItem();
//	public static final int TEXTURA_x16_POCION_ROJA = getSiguienteIdItem();
//	public static final int TEXTURA_x8_POCION_ROJA = getSiguienteIdItem();
//	public static final int TEXTURA_x16_POCION_AZUL = getSiguienteIdItem();
//	public static final int TEXTURA_x8_POCION_AZUL = getSiguienteIdItem();
//	public static final int TEXTURA_x16_BOTAS_CUERO_MARRON = getSiguienteIdItem();
//	public static final int TEXTURA_x8_BOTAS_CUERO_MARRON = getSiguienteIdItem();
//	static {
//		HojaSprite HOJA_OBJETOS = new HojaSprite("/imagenes/objetos/items.png", 16, false);
//		TEXTURAS.put(TEXTURA_x16_ANILLO_PLATA, HOJA_OBJETOS.getSprite(0));
//		TEXTURAS.put(TEXTURA_x16_ANILLO_ORO, HOJA_OBJETOS.getSprite(13));
//		TEXTURAS.put(TEXTURA_x16_ESMERALDA, HOJA_OBJETOS.getSprite(28));
//		TEXTURAS.put(TEXTURA_x16_POCION_ROJA, HOJA_OBJETOS.getSprite(50));
//		TEXTURAS.put(TEXTURA_x16_POCION_AZUL, HOJA_OBJETOS.getSprite(48));
//		TEXTURAS.put(TEXTURA_x16_BOTAS_CUERO_MARRON, HOJA_OBJETOS.getSprite(122));
//
//		// redimensionado de texturas
//		{
//			BufferedImage pocionRojax8 = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
//			pocionRojax8.getGraphics2D().drawImage(TEXTURAS.get(TEXTURA_x16_POCION_ROJA), 0, 0, 8, 8, 0, 0, 16, 16, null);
//			pocionRojax8.flush();
//			TEXTURAS.put(TEXTURA_x8_POCION_ROJA, pocionRojax8);
//		}
//		{
//			BufferedImage pocionAzulx8 = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
//			pocionAzulx8.getGraphics2D().drawImage(TEXTURAS.get(TEXTURA_x16_POCION_AZUL), 0, 0, 8, 8, 0, 0, 16, 16, null);
//			pocionAzulx8.flush();
//			TEXTURAS.put(TEXTURA_x8_POCION_AZUL, pocionAzulx8);
//		}
//		{
//			BufferedImage botasCueroMarronx8 = new BufferedImage(12, 12, BufferedImage.TYPE_INT_ARGB);
//			botasCueroMarronx8.getGraphics2D().drawImage(TEXTURAS.get(TEXTURA_x16_BOTAS_CUERO_MARRON), 0, 0, 12, 12, 0, 0,
//					16, 16, null);
//			botasCueroMarronx8.flush();
//			TEXTURAS.put(TEXTURA_x8_BOTAS_CUERO_MARRON, botasCueroMarronx8);
//		}
//	}

	public static final int TEXTURA_x16_ANILLO_PLATA = getSiguienteIdItem();
	public static final int TEXTURA_x16_ANILLO_ORO = getSiguienteIdItem();
	public static final int TEXTURA_x16_ESMERALDA = getSiguienteIdItem();
	public static final int TEXTURA_x16_POCION_ROJA = getSiguienteIdItem();
	public static final int TEXTURA_x16_POCION_AZUL = getSiguienteIdItem();
	public static final int TEXTURA_x16_BOTAS_CUERO_MARRON = getSiguienteIdItem();
	public static final int TEXTURA_x16_PISTOLA = getSiguienteIdItem();
	static {
		HojaSprite HOJA_OBJETOS = new HojaSprite("/imagenes/objetos/items.png", 16, false);
		TEXTURAS.put(TEXTURA_x16_ANILLO_PLATA, HOJA_OBJETOS.getSprite(0));
		TEXTURAS.put(TEXTURA_x16_ANILLO_ORO, HOJA_OBJETOS.getSprite(13));
		TEXTURAS.put(TEXTURA_x16_ESMERALDA, HOJA_OBJETOS.getSprite(28));
		TEXTURAS.put(TEXTURA_x16_POCION_ROJA, HOJA_OBJETOS.getSprite(50));
		TEXTURAS.put(TEXTURA_x16_POCION_AZUL, HOJA_OBJETOS.getSprite(48));
		TEXTURAS.put(TEXTURA_x16_BOTAS_CUERO_MARRON, HOJA_OBJETOS.getSprite(122));
		TEXTURAS.put(TEXTURA_x16_BOTAS_CUERO_MARRON, HOJA_OBJETOS.getSprite(4));
		TEXTURAS.put(TEXTURA_x16_PISTOLA, Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/objetos/gun16x16.png"));

	}
	public static final int TEXTURA_X16_GRANADA_1 = getSiguienteIdItem();
	public static final int TEXTURA_X16_GRANADA_2 = getSiguienteIdItem();
	public static final int TEXTURA_X16_GRANADA_3 = getSiguienteIdItem();
	public static final int TEXTURA_X16_GRANADA_4 = getSiguienteIdItem();
	public static final int TEXTURA_X16_GRANADA_5 = getSiguienteIdItem();
	public static final int TEXTURA_X16_GRANADA_6 = getSiguienteIdItem();
	public static final int TEXTURA_X16_GRANADA_7 = getSiguienteIdItem();
	public static final int TEXTURA_X16_GRANADA_8 = getSiguienteIdItem();
	static {
		BufferedImage aux = new BufferedImage(16*8, 16, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = (Graphics2D) aux.getGraphics();
		g.drawImage(Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/objetos/granadas.png"), 0, 0, 16*8, 16, 0, 0, 16*8, 16, null);
		g.dispose();
		HojaSprite HOJA_GRANADAS = new HojaSprite(aux, 16, false);
		TEXTURAS.put(TEXTURA_X16_GRANADA_1, HOJA_GRANADAS.getSprite(0));
		TEXTURAS.put(TEXTURA_X16_GRANADA_2, HOJA_GRANADAS.getSprite(1));
		TEXTURAS.put(TEXTURA_X16_GRANADA_3, HOJA_GRANADAS.getSprite(2));
		TEXTURAS.put(TEXTURA_X16_GRANADA_4, HOJA_GRANADAS.getSprite(3));
		TEXTURAS.put(TEXTURA_X16_GRANADA_5, HOJA_GRANADAS.getSprite(4));
		TEXTURAS.put(TEXTURA_X16_GRANADA_6, HOJA_GRANADAS.getSprite(5));
		TEXTURAS.put(TEXTURA_X16_GRANADA_7, HOJA_GRANADAS.getSprite(6));
		TEXTURAS.put(TEXTURA_X16_GRANADA_8, HOJA_GRANADAS.getSprite(7));
	}
	public static final int TEXTURA_x10_POCION_AZUL = getSiguienteIdItem();
	public static final int TEXTURA_x10_POCION_ROJA = getSiguienteIdItem();
	public static final int TEXTURA_x10_BOTAS_CUERO_MARRON = getSiguienteIdItem();
	public static final int TEXTURA_x8_PISTOLA = getSiguienteIdItem();
	public static final int TEXTURA_x4_BALA = getSiguienteIdItem();
	static {
		HojaSprite HOJA_OBJETOS = new HojaSprite("/imagenes/objetos/itemsx10.png", 10, false);
		TEXTURAS.put(TEXTURA_x10_POCION_ROJA, HOJA_OBJETOS.getSprite(50));
		TEXTURAS.put(TEXTURA_x10_POCION_AZUL, HOJA_OBJETOS.getSprite(48));
		TEXTURAS.put(TEXTURA_x10_BOTAS_CUERO_MARRON, HOJA_OBJETOS.getSprite(122));
		TEXTURAS.put(TEXTURA_x8_PISTOLA, Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/objetos/gun8x8.png"));
		TEXTURAS.put(TEXTURA_x4_BALA, Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/objetos/bala.png"));

	}
	
	public static final int TEXTURA_X10_GRANADA_1 = getSiguienteIdItem();
	public static final int TEXTURA_X10_GRANADA_2 = getSiguienteIdItem();
	public static final int TEXTURA_X10_GRANADA_3 = getSiguienteIdItem();
	public static final int TEXTURA_X10_GRANADA_4 = getSiguienteIdItem();
	public static final int TEXTURA_X10_GRANADA_5 = getSiguienteIdItem();
	public static final int TEXTURA_X10_GRANADA_6 = getSiguienteIdItem();
	public static final int TEXTURA_X10_GRANADA_7 = getSiguienteIdItem();
	public static final int TEXTURA_X10_GRANADA_8 = getSiguienteIdItem();
	
	static {
		BufferedImage aux = new BufferedImage(10*8, 10, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = (Graphics2D) aux.getGraphics();
		g.drawImage(Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/objetos/granadas.png"), 0, 0, 10*8, 10, 0, 0, 16*8, 16, null);
		g.dispose();
		HojaSprite HOJA_GRANADAS = new HojaSprite(aux, 10, false);
		TEXTURAS.put(TEXTURA_X10_GRANADA_1, HOJA_GRANADAS.getSprite(0));
		TEXTURAS.put(TEXTURA_X10_GRANADA_2, HOJA_GRANADAS.getSprite(1));
		TEXTURAS.put(TEXTURA_X10_GRANADA_3, HOJA_GRANADAS.getSprite(2));
		TEXTURAS.put(TEXTURA_X10_GRANADA_4, HOJA_GRANADAS.getSprite(3));
		TEXTURAS.put(TEXTURA_X10_GRANADA_5, HOJA_GRANADAS.getSprite(4));
		TEXTURAS.put(TEXTURA_X10_GRANADA_6, HOJA_GRANADAS.getSprite(5));
		TEXTURAS.put(TEXTURA_X10_GRANADA_7, HOJA_GRANADAS.getSprite(6));
		TEXTURAS.put(TEXTURA_X10_GRANADA_8, HOJA_GRANADAS.getSprite(7));
	}
	
	

	public static final int TEXTURA_X8_PARTICULA_SANGRE = getSiguienteIdParticula();
	
	static {
		HojaSprite HOJA_PARTICULAS = new HojaSprite("/imagenes/objetos/sangrex8.png", 8, false);
		TEXTURAS.put(TEXTURA_X8_PARTICULA_SANGRE, HOJA_PARTICULAS.getSprite(0));
	}
	
	
	public static final int TEXTURA_x50_EXPLOSION = getSiguienteIdEfecto();
	
	
	
	static {
		BufferedImage auxExplosion100 = Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/explosion.png");
		BufferedImage imgExplosion50 = new BufferedImage(50 *10, 50*5, BufferedImage.TYPE_INT_ARGB);
		
		final Graphics2D gAuxExplosion50 = (Graphics2D) imgExplosion50.getGraphics();
		gAuxExplosion50.drawImage(auxExplosion100, 0, 0, 50*10, 50*5, 0, 0, 100*10, 100*5, null);
		gAuxExplosion50.dispose();
		auxExplosion100.flush();
		
		TEXTURAS.put(TEXTURA_x50_EXPLOSION, imgExplosion50);
	}
	
	public static final int TEXTURA_X64_CASA1 = getSiguienteIdEstructura();
	
	static {
		BufferedImage auxCasa116x112 = Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/texturas/house/1.png");
		BufferedImage imgCasa1 = new BufferedImage(64,64, BufferedImage.TYPE_INT_ARGB);
		
		final Graphics2D gAuxExplosion50 = (Graphics2D) imgCasa1.getGraphics();
		gAuxExplosion50.drawImage(auxCasa116x112, 0, 0, 64, 64, 0, 0, auxCasa116x112.getWidth(), auxCasa116x112.getHeight(), null);
		gAuxExplosion50.dispose();
		auxCasa116x112.flush();
		
//		try {
//			ImageIO.write(imgCasa1, "png", new File("casa1.png"));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		TEXTURAS.put(TEXTURA_X64_CASA1, imgCasa1);
	}

	static {
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		TEXTURAS.put(TEXTURA_TRANSPARENTE, img);
	}

	private Textura() {

	}

	public static BufferedImage crearTextura(final Color c, final int ancho, final int alto) {
		BufferedImage image = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = (Graphics2D) image.getGraphics();
		g.setColor(c);
		g.fillRect(0, 0, ancho, alto);
		g.dispose();
		return image;
	}
	
	public static BufferedImage voltearImagenH(BufferedImage image) {
		int widthOfImage = image.getWidth();
		int heightOfImage = image.getHeight();
		int typeOfImage = image.getType();

		BufferedImage newImageFromBuffer = new BufferedImage(widthOfImage, heightOfImage, typeOfImage);

		Graphics2D g = newImageFromBuffer.createGraphics();

		g.drawImage(image, 0, 0, widthOfImage, heightOfImage, widthOfImage, 0, 0, heightOfImage, null);

		return newImageFromBuffer;
	}
	
	public static BufferedImage voltearImagenV(BufferedImage image) {
		int widthOfImage = image.getWidth();
		int heightOfImage = image.getHeight();
		int typeOfImage = image.getType();

		BufferedImage newImageFromBuffer = new BufferedImage(widthOfImage, heightOfImage, typeOfImage);

		Graphics2D g = newImageFromBuffer.createGraphics();
		// 							X  Y    W      			H			XD YD 				WD 			HD

		g.drawImage(image, 0, 0, widthOfImage, heightOfImage,  0, heightOfImage, widthOfImage, 0, null);
		
		return newImageFromBuffer;
	}
	
	public static BufferedImage voltearImagen90GradosIzquierda(final BufferedImage image) {
		BufferedImage b2 = new BufferedImage(image.getHeight(), image.getWidth(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d =(Graphics2D) b2.getGraphics();
		AffineTransform at = AffineTransform.getRotateInstance(Math.toRadians(270), 
				image.getWidth()/2, image.getWidth()/2);
		g2d.drawImage(image, at, null);
		g2d.dispose();
		return b2;
	}
	
	public static BufferedImage voltearImagen90GradosDerecha(final BufferedImage image) {
		return voltearImagenV(voltearImagen90GradosIzquierda(image));
	}
	
	public static BufferedImage redimensionar(BufferedImage img ,final int anchoNuevo, final int altoNuevo) {
		BufferedImage imgNueva = new BufferedImage(anchoNuevo, altoNuevo, img.getType());
		Graphics2D g = imgNueva.createGraphics();
		g.drawImage(img, 0, 0, anchoNuevo, altoNuevo, null);
		g.dispose();
		return imgNueva;
	}
	
	public static HojaSprite HOJA_AGUA;
	static {
		BufferedImage img = new BufferedImage(Textura.getTextura(Textura.TEXTURA_x32_AGUA_1).getWidth(), Textura.getTextura(Textura.TEXTURA_x32_AGUA_1).getWidth()*2, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D)img.getGraphics();
		g.drawImage(Textura.getTextura(Textura.TEXTURA_x32_AGUA_1), 0, 0, null);
		g.drawImage(Textura.getTextura(Textura.TEXTURA_x32_AGUA_1_INV_H), Textura.getTextura(Textura.TEXTURA_x32_AGUA_1).getWidth(), 0, null);
		g.dispose();
		HOJA_AGUA = new HojaSprite(img, Textura.getTextura(Textura.TEXTURA_x32_AGUA_1).getWidth(), true);
	}
	

}
