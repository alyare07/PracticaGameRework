package principal.animaciones.listaHojasSprite;

import java.util.HashMap;

import principal.utilidades.HojaSprite;

public class ListaHojaSprites{
    public HashMap<String, HojaSprite> HOJAS_SPRITE = new HashMap<String, HojaSprite>();

    public final ListaHojaSpriteJugador JUGADOR = new ListaHojaSpriteJugador();
    public final ListaHojaSpriteBolaFuego BOLA_FUEGO = new ListaHojaSpriteBolaFuego();
    public final ListaHojaSpriteCofre COFRES = new ListaHojaSpriteCofre();
    public final ListaHojaSpriteBandido BANDIDO = new ListaHojaSpriteBandido();

    /**
     * SE PUEDE CREAR UNA VARIABLE QUE CONTROLE CUANTAS ENTIDADES USAN ESA HOJA DE
     * SPRITE COSA DE QUE SI NO POSEE ENTIDAD QUE LA USE ESTA SE SETE A NULL Y AL
     * TRATAR DE OBTENERLA NUEVAMENTE SE CARGARIA DE NUEVO (METODO GET Y LOS
     * ATRIBUTOS EN PRIVATE
     */

    public ListaHojaSprites() {

    }

    public HojaSprite getHojaSprite(final String nombre) {
	if (this.HOJAS_SPRITE.containsKey(nombre)) {
	    return this.HOJAS_SPRITE.get(nombre);
	} else {
	    return this.cargarHojaSprite(nombre);
	}

    }

    private HojaSprite cargarHojaSprite(final String nombre) {
	switch (nombre) {
//Completar la carga de hojassprite necesarias. declarando en un atributo static los string correspondientes a cada una
	default:
	    break;
	}
	return null;
    }

}
