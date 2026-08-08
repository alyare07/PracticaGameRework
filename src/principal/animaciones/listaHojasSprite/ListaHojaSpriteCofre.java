package principal.animaciones.listaHojasSprite;

import java.awt.image.BufferedImage;

import principal.utilidades.Constantes;
import principal.utilidades.HojaSprite;


public class ListaHojaSpriteCofre {
	private final BufferedImage TEXTURA_COFRE;

	private final HojaSprite ESTANDAR;
	
	public ListaHojaSpriteCofre() {
		this.TEXTURA_COFRE = Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/objetos/cofres.png");
		
		this.ESTANDAR = new HojaSprite(this.TEXTURA_COFRE.getSubimage(0, 0, 16*2, 16), 16, false);

	}
	
	
	public BufferedImage getCofreAbierto() {
		return this.ESTANDAR.getSprite(0);
	}
	
	public BufferedImage getCofreCerrado() {
		return this.ESTANDAR.getSprite(1);
	}
	
}
