package principal.animaciones.listaHojasSprite;

import principal.utilidades.Constantes;
import principal.utilidades.HojaSprite;
import principal.utilidades.Textura;

public class ListaHojaSpriteBandido{

    private final int lado = 32;
    private final boolean opaca = false;

    public final HojaSprite ESTANDAR_ARRIBA = new HojaSprite(
	    Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/bandido.png").getSubimage(0, this.lado, this.lado * 4, this.lado), this.lado, this.opaca);
    public final HojaSprite ESTANDAR_ABAJO = new HojaSprite(
	    Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/bandido.png").getSubimage(0, 0, this.lado * 4, this.lado), this.lado, this.opaca);
    public final HojaSprite ESTANDAR_DERECHA = new HojaSprite(
	    Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/bandido.png").getSubimage(0, 2 * this.lado, this.lado * 4, this.lado), this.lado, this.opaca);
    public final HojaSprite ESTANDAR_IZQUIERDA = new HojaSprite(
    		Constantes.FUNCIONES.TEXTURAS_TOOLS.voltearImagenH(Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/bandido.png").getSubimage(0, 2 * this.lado, this.lado * 4, this.lado)),
	    this.lado, this.opaca);

    public final HojaSprite CAMINANDO_ARRIBA = new HojaSprite(
	    Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/bandido.png").getSubimage(0, 4 * this.lado, this.lado * 4, this.lado), this.lado, this.opaca);
    public final HojaSprite CAMINANDO_ABAJO = new HojaSprite(
	    Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/bandido.png").getSubimage(0, 3 * this.lado, this.lado * 4, this.lado), this.lado, this.opaca);
    public final HojaSprite CAMINANDO_DERECHA = new HojaSprite(
	    Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/bandido.png").getSubimage(0, 5 * this.lado, this.lado * 4, this.lado), this.lado, this.opaca);
    public final HojaSprite CAMINANDO_IZQUIERDA = new HojaSprite(
    		Constantes.FUNCIONES.TEXTURAS_TOOLS.voltearImagenH(Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/bandido.png").getSubimage(0, 5 * this.lado, this.lado * 4, this.lado)),
	    this.lado, this.opaca);

    public final HojaSprite PISTOLA_ESTANDAR_ARRIBA = new HojaSprite(
	    Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/bandido.png").getSubimage(0, 7 * this.lado, this.lado * 4, this.lado), this.lado, this.opaca);
    public final HojaSprite PISTOLA_ESTANDAR_ABAJO = new HojaSprite(
	    Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/bandido.png").getSubimage(0, 6 * this.lado, this.lado * 4, this.lado), this.lado, this.opaca);
    public final HojaSprite PISTOLA_ESTANDAR_DERECHA = new HojaSprite(
	    Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/bandido.png").getSubimage(0, 8 * this.lado, this.lado * 4, this.lado), this.lado, this.opaca);
    public final HojaSprite PISTOLA_ESTANDAR_IZQUIERDA = new HojaSprite(
    		Constantes.FUNCIONES.TEXTURAS_TOOLS.voltearImagenH(Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/bandido.png").getSubimage(0, 8 * this.lado, this.lado * 4, this.lado)),
	    this.lado, this.opaca);

    public final HojaSprite PISTOLA_CAMINANDO_ARRIBA = new HojaSprite(
	    Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/bandido.png").getSubimage(0, 10 * this.lado, this.lado * 4, this.lado), this.lado, this.opaca);
    public final HojaSprite PISTOLA_CAMINANDO_ABAJO = new HojaSprite(
	    Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/bandido.png").getSubimage(0, 9 * this.lado, this.lado * 4, this.lado), this.lado, this.opaca);
    public final HojaSprite PISTOLA_CAMINANDO_DERECHA = new HojaSprite(
	    Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/bandido.png").getSubimage(0, 11 * this.lado, this.lado * 4, this.lado), this.lado, this.opaca);
    public final HojaSprite PISTOLA_CAMINANDO_IZQUIERDA = new HojaSprite(
    		Constantes.FUNCIONES.TEXTURAS_TOOLS.voltearImagenH(Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/bandido.png").getSubimage(0, 11 * this.lado, this.lado * 4, this.lado)),
	    this.lado, this.opaca);

    public final HojaSprite GARROTE_ESTANDAR_ARRIBA = new HojaSprite(
	    Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/bandido.png").getSubimage(0, 13 * this.lado, this.lado * 4, this.lado), this.lado, this.opaca);
    public final HojaSprite GARROTE_ESTANDAR_ABAJO = new HojaSprite(
	    Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/bandido.png").getSubimage(0, 12 * this.lado, this.lado * 4, this.lado), this.lado, this.opaca);
    public final HojaSprite GARROTE_ESTANDAR_DERECHA = new HojaSprite(
	    Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/bandido.png").getSubimage(0, 14 * this.lado, this.lado * 4, this.lado), this.lado, this.opaca);
    public final HojaSprite GARROTE_ESTANDAR_IZQUIERDA = new HojaSprite(
    		Constantes.FUNCIONES.TEXTURAS_TOOLS.voltearImagenH(Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/bandido.png").getSubimage(0, 14 * this.lado, this.lado * 4, this.lado)),
	    this.lado, this.opaca);

    public final HojaSprite GARROTE_CAMINANDO_ARRIBA = new HojaSprite(
	    Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/bandido.png").getSubimage(0, 16 * this.lado, this.lado * 4, this.lado), this.lado, this.opaca);
    public final HojaSprite GARROTE_CAMINANDO_ABAJO = new HojaSprite(
	    Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/bandido.png").getSubimage(0, 15 * this.lado, this.lado * 4, this.lado), this.lado, this.opaca);
    public final HojaSprite GARROTE_CAMINANDO_DERECHA = new HojaSprite(
	    Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/bandido.png").getSubimage(0, 17 * this.lado, this.lado * 4, this.lado), this.lado, this.opaca);
    public final HojaSprite GARROTE_CAMINANDO_IZQUIERDA = new HojaSprite(
    		Constantes.FUNCIONES.TEXTURAS_TOOLS.voltearImagenH(Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/bandido.png").getSubimage(0, 17 * this.lado, this.lado * 4, this.lado)),
	    this.lado, this.opaca);

    public final HojaSprite GARROTE_ATACANDO_ARRIBA = new HojaSprite(
	    Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/bandido.png").getSubimage(0, 19 * this.lado, this.lado * 4, this.lado), this.lado, this.opaca);
    public final HojaSprite GARROTE_ATACANDO_ABAJO = new HojaSprite(
	    Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/bandido.png").getSubimage(0, 18 * this.lado, this.lado * 4, this.lado), this.lado, this.opaca);
    public final HojaSprite GARROTE_ATACANDO_DERECHA = new HojaSprite(
	    Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/bandido.png").getSubimage(0, 20 * this.lado, this.lado * 4, this.lado), this.lado, this.opaca);
    public final HojaSprite GARROTE_ATACANDO_IZQUIERDA = new HojaSprite(
    		Constantes.FUNCIONES.TEXTURAS_TOOLS.voltearImagenH(Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/bandido.png").getSubimage(0, 20 * this.lado, this.lado * 4, this.lado)),
	    this.lado, this.opaca);

    public ListaHojaSpriteBandido() {

    }
}
