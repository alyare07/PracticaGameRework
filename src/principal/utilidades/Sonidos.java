package principal.utilidades;

public abstract class Sonidos {
	
	public static final SonidoMP3 EXPLOSION1 = new SonidoMP3("sonidos/explosion1.mp3");
	public static final SonidoMP3 SONIDO_DEAD_CRIATURE = new SonidoMP3("sonidos/dead_criature.mp3");
	public static final SonidoMP3 SONIDO_DISPARO_PISTOLA = new SonidoMP3("sonidos/DisparoPistola.mp3");
	public static final SonidoMP3 SONIDO_SIN_MUNICION = new SonidoMP3("sonidos/SinMunicion.mp3");
	public static final SonidoMP3 SONIDO_GOLPE = new SonidoMP3("sonidos/hit_punch.mp3");
	public static final SonidoMP3 SONIDO_GOLPE_2 = new SonidoMP3("sonidos/hit_punch_2.mp3");
	
	private Sonidos() {
		
	}
	
	
	public static SonidoMP3 crearSonido(final String ruta) {
		return new SonidoMP3(ruta);
	}
}
