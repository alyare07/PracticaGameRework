package principal.utilidades;

public class Constantes {

	private Constantes() {

	}

	public static final int ANCHO_JUEGO = 640;
	public static final int ALTO_JUEGO = 360;
	public static final int LADO_TILE = 16;
	// --- PRESETS DE ESCALAS ENTERAS (PIXEL-PERFECT) ---
	public static final int ESCALA_MINIMA = 1; // 640 x 360 (1x Base)
	public static final int ESCALA_1X_640x360 = 1;
	public static final int ESCALA_2X_1280x720 = 2; // 1280 x 720 (2x HD)
	public static final int ESCALA_3X_1920x1080 = 3; // 1920 x 1080 (3x Full HD)
	public static final int ESCALA_4X_2560x1440 = 4; // 2560 x 1440 (4x 2K / QHD)
	public static final int ESCALA_6X_3840x2160 = 6; // 3840 x 2160 (6x 4K / UHD)

	public static final double RADIO_AUDIO_DISTANCIA_MAXIMA = Math.hypot(ANCHO_JUEGO, ALTO_JUEGO) * 0.75;
	public static final float TAMANO_FUENTE = 9f;
	public static final int LIMITE_ANIMACION = 32767;
	public static final int CENTROX = ANCHO_JUEGO / 2;
	public static final int CENTROY = ALTO_JUEGO / 2;

}
