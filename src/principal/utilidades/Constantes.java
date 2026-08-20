package principal.utilidades;

public class Constantes {

	private Constantes() {

	}

	public static final int ANCHO_JUEGO = 640;
	public static final int ALTO_JUEGO = 360;
	public static final int LADO_TILE = 16;
//    public static int ANCHO_PANTALLA_COMPLETA = 1600;
//    public static int ALTO_PANTALLA_COMPLETA = 900;
//	public static int ANCHO_PANTALLA_COMPLETA = 640*2;
//	public static int ALTO_PANTALLA_COMPLETA = 360*2;
//	public static int ANCHO_PANTALLA_COMPLETA = 1920;
//	public static int ALTO_PANTALLA_COMPLETA = 1080;d

	public static final double RADIO_AUDIO_DISTANCIA_MAXIMA = Math.hypot(ANCHO_JUEGO, ALTO_JUEGO) * 0.75;
	public static final float TAMANO_FUENTE = 9f;
	public static final int LIMITE_ANIMACION = 32767;
	public static final int CENTROX = ANCHO_JUEGO / 2;
	public static final int CENTROY = ALTO_JUEGO / 2;

}
