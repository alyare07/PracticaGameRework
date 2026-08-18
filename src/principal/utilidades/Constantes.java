package principal.utilidades;

import java.awt.Toolkit;

public class Constantes {

	public final int ANCHO_JUEGO = 640;
	public final int ALTO_JUEGO = 360;
	public int LADO_TILE = 16;
//    public static int ANCHO_PANTALLA_COMPLETA = 1600;
//    public static int ALTO_PANTALLA_COMPLETA = 900;
//	public static int ANCHO_PANTALLA_COMPLETA = 640*2;
//	public static int ALTO_PANTALLA_COMPLETA = 360*2;
//	public static int ANCHO_PANTALLA_COMPLETA = 1920;
//	public static int ALTO_PANTALLA_COMPLETA = 1080;d
	public int ANCHO_PANTALLA_COMPLETA = Toolkit.getDefaultToolkit().getScreenSize().width;
	public int ALTO_PANTALLA_COMPLETA = Toolkit.getDefaultToolkit().getScreenSize().height;

	public final double RADIO_AUDIO_DISTANCIA_MAXIMA = Math.hypot(this.ANCHO_JUEGO, this.ALTO_JUEGO) * 0.75;
	public final float TAMANO_FUENTE = 9f;
	public final int LIMITE_ANIMACION = 32767;
	public final int CENTROX = this.ANCHO_JUEGO / 2;
	public final int CENTROY = this.ALTO_JUEGO / 2;

}
