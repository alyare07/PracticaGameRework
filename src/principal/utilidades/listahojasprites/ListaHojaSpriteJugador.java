package principal.utilidades.listahojasprites;

import java.awt.image.BufferedImage;

import principal.utilidades.Constantes;
import principal.utilidades.HojaSprite;
import principal.utilidades.funciones.CargadorRecursos;

public class ListaHojaSpriteJugador {
	private final BufferedImage TEXTURA_BASICA;
	private final BufferedImage TEXTURA_ARMADO;
	
	public final HojaSprite BASICA_ESTANDAR_DERECHA;
	public final HojaSprite BASICA_ESTANDAR_IZQUIERDA;
	public final HojaSprite BASICA_ESTANDAR_ARRIBA;
	public final HojaSprite BASICA_ESTANDAR_ABAJO;
	
	public final HojaSprite BASICA_DERECHA;
	public final HojaSprite BASICA_IZQUIERDA;
	public final HojaSprite BASICA_ARRIBA;
	public final HojaSprite BASICA_ABAJO;
	
	public final HojaSprite ARMADO_ESTANDAR_DERECHA;
	public final HojaSprite ARMADO_ESTANDAR_IZQUIERDA;
	public final HojaSprite ARMADO_ESTANDAR_ARRIBA;
	public final HojaSprite ARMADO_ESTANDAR_ABAJO;
	
	public final HojaSprite ARMADO_PISTOLA_DERECHA;
	public final HojaSprite ARMADO_PISTOLA_IZQUIERDA;
	public final HojaSprite ARMADO_PISTOLA_ARRIBA;
	public final HojaSprite ARMADO_PISTOLA_ABAJO;
	
	public ListaHojaSpriteJugador() {
		this.TEXTURA_BASICA = Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/player_walking.png");
		this.TEXTURA_ARMADO = Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/player_walking_armed.png");
		
		this.BASICA_ESTANDAR_DERECHA = new HojaSprite(TEXTURA_BASICA.getSubimage(0, 16*3,16, 16), 16, false);
		this.BASICA_ESTANDAR_IZQUIERDA = new HojaSprite(TEXTURA_BASICA.getSubimage(0, 16*2,16, 16), 16, false);
		this.BASICA_ESTANDAR_ARRIBA = new HojaSprite(TEXTURA_BASICA.getSubimage(0, 16,16, 16), 16, false);
		this.BASICA_ESTANDAR_ABAJO = new HojaSprite(TEXTURA_BASICA.getSubimage(0, 0,16, 16), 16, false);
		
		this.ARMADO_ESTANDAR_DERECHA = new HojaSprite(TEXTURA_ARMADO.getSubimage(0, 16*3,16, 16), 16, false);
		this.ARMADO_ESTANDAR_IZQUIERDA = new HojaSprite(TEXTURA_ARMADO.getSubimage(0, 16*2,16, 16), 16, false);
		this.ARMADO_ESTANDAR_ARRIBA = new HojaSprite(TEXTURA_ARMADO.getSubimage(0, 16,16, 16), 16, false);
		this.ARMADO_ESTANDAR_ABAJO = new HojaSprite(TEXTURA_ARMADO.getSubimage(0, 0,16, 16), 16, false);
		
		this.BASICA_DERECHA = new HojaSprite(TEXTURA_BASICA.getSubimage(0, 16*3,this.TEXTURA_BASICA.getWidth(), 16), 16, false);
		this.BASICA_IZQUIERDA = new HojaSprite(TEXTURA_BASICA.getSubimage(0, 16*2,this.TEXTURA_BASICA.getWidth(), 16), 16, false);
		this.BASICA_ARRIBA = new HojaSprite(TEXTURA_BASICA.getSubimage(0, 16,this.TEXTURA_BASICA.getWidth(), 16), 16, false);
		this.BASICA_ABAJO = new HojaSprite(TEXTURA_BASICA.getSubimage(0, 0,this.TEXTURA_BASICA.getWidth(), 16), 16, false);
		
		this.ARMADO_PISTOLA_DERECHA = new HojaSprite(TEXTURA_ARMADO.getSubimage(0, 16*3,this.TEXTURA_ARMADO.getWidth(), 16), 16, false);
		this.ARMADO_PISTOLA_IZQUIERDA = new HojaSprite(TEXTURA_ARMADO.getSubimage(0, 16*2,this.TEXTURA_ARMADO.getWidth(), 16), 16, false);
		this.ARMADO_PISTOLA_ARRIBA = new HojaSprite(TEXTURA_ARMADO.getSubimage(0, 16,this.TEXTURA_ARMADO.getWidth(), 16), 16, false);
		this.ARMADO_PISTOLA_ABAJO = new HojaSprite(TEXTURA_ARMADO.getSubimage(0, 0,this.TEXTURA_ARMADO.getWidth(), 16), 16, false);
	}

}
