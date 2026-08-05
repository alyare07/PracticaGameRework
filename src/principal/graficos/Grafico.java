package principal.graficos;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import principal.utilidades.Constantes;

public class Grafico {
	
	//ESTA CLASE FUE UNA PRUEBA NO EXITOSA DE LOGRAR UN Z-INDEX EN LOS OBJETOS :C
	
	private static final HashMap<Integer, BufferedImage> HOJAS = new HashMap<Integer, BufferedImage>();
	private static final HashMap<Integer, Graphics> GRAFICOS = new HashMap<Integer, Graphics>();
	
	
	public static void llenarHojas() {
		BufferedImage hoja = null;
		for(int y = 0 ; y <= Constantes.ALTO_JUEGO;y++) {
			hoja = new BufferedImage(Constantes.ANCHO_JUEGO, y+1, BufferedImage.TYPE_INT_ARGB);
			HOJAS.put(y, hoja);
		}
	}
	
	public static void restablecerGraficos() {
		GRAFICOS.clear();
		BufferedImage buffer = null;
		for(int y = 0; y <= Constantes.ALTO_JUEGO;y++) {
			buffer = HOJAS.get(y);
			GRAFICOS.put(y,buffer.getGraphics());
//			GRAFICOS.get(y).setColor(Color.yellow);
//			GRAFICOS.get(y).fillRect(0, 0,buffer.getWidth(), buffer.getHeight());
			
			((Graphics2D)GRAFICOS.get(y)).setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
			((Graphics2D)GRAFICOS.get(y)).setColor(new Color(0, 0, 0, 0));
			((Graphics2D)GRAFICOS.get(y)).fillRect(0, 0,buffer.getWidth(), buffer.getHeight());
			((Graphics2D)GRAFICOS.get(y)).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
		}
	}
	
	public static void pintarHojas(final Graphics g) {
		for(int y = 0; y <= Constantes.ALTO_JUEGO;y++) {
			GRAFICOS.get(y).dispose();
			g.drawImage(HOJAS.get(y), 0, 0, null);
		}
	}
	
	public static Graphics getGraphics(final int yParado) {
		if(yParado >= Constantes.ALTO_JUEGO) {
			return GRAFICOS.get(Constantes.ALTO_JUEGO);
		}
		return GRAFICOS.get(yParado);
	}

}
