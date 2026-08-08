package principal.animaciones.jugador;

import java.awt.Graphics2D;
import java.util.HashMap;

import principal.animaciones.Animacion;
import principal.animaciones.AnimacionDireccionada;
import principal.entes.criaturas.Criatura.Direccion;
import principal.entes.criaturas.Criatura.Estado;
import principal.entes.criaturas.Jugador;
import principal.utilidades.Constantes;
import principal.utilidades.HojaSprite;
import principal.utilidades.Textura;

public class AnimacionesJugador{

    private final HashMap<String, AnimacionDireccionada> ANIMACIONES;

    public static final String ESTANDAR = "Estandar";
    public static final String CAMINANDO = "Caminando";
    public static final String ARMADO_ESTANDAR = "Armado Estandar";
    public static final String ARMADO_CAMINANDO = "Armado Caminando";

    private final int TIEMPO_MS_POR_FRAME = 150;

    public AnimacionesJugador() {
	final int lado = 32;
	final boolean opaca = false;

	this.ANIMACIONES = new HashMap<String, AnimacionDireccionada>();
	this.ANIMACIONES.put(ESTANDAR, new AnimacionDireccionada(
		new Animacion(new HojaSprite(Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/player_sprites.png").getSubimage(0, lado, lado * 4, lado),
			lado, opaca), true, this.TIEMPO_MS_POR_FRAME),
		new Animacion(
			new HojaSprite(Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/player_sprites.png").getSubimage(0, 0, lado * 4, lado), lado, opaca),
			true, this.TIEMPO_MS_POR_FRAME),
		new Animacion(new HojaSprite(Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/player_sprites.png").getSubimage(0, 2 * lado, lado * 4, lado),
			lado, opaca), true, this.TIEMPO_MS_POR_FRAME),
		new Animacion(new HojaSprite(
			Constantes.FUNCIONES.TEXTURAS_TOOLS.voltearImagenH(
				Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/player_sprites.png").getSubimage(0, 2 * lado, lado * 4, lado)),
			lado, opaca), true, this.TIEMPO_MS_POR_FRAME)));
	this.ANIMACIONES.put(CAMINANDO, new AnimacionDireccionada(
		new Animacion(new HojaSprite(Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/player_sprites.png").getSubimage(0, 4 * lado, lado * 4, lado),
			lado, opaca), true, this.TIEMPO_MS_POR_FRAME - 50),
		new Animacion(new HojaSprite(Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/player_sprites.png").getSubimage(0, 3 * lado, lado * 4, lado),
			lado, opaca), true, this.TIEMPO_MS_POR_FRAME - 50),
		new Animacion(new HojaSprite(Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/player_sprites.png").getSubimage(0, 5 * lado, lado * 4, lado),
			lado, opaca), true, this.TIEMPO_MS_POR_FRAME - 50),
		new Animacion(new HojaSprite(
				Constantes.FUNCIONES.TEXTURAS_TOOLS.voltearImagenH(
				Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/player_sprites.png").getSubimage(0, 5 * lado, lado * 4, lado)),
			lado, opaca), true, this.TIEMPO_MS_POR_FRAME)));
	this.ANIMACIONES.put(ARMADO_ESTANDAR, new AnimacionDireccionada(
		new Animacion(new HojaSprite(Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/player_sprites.png").getSubimage(0, 7 * lado, lado * 4, lado),
			lado, opaca), true, this.TIEMPO_MS_POR_FRAME),
		new Animacion(new HojaSprite(Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/player_sprites.png").getSubimage(0, 6 * lado, lado * 4, lado),
			lado, opaca), true, this.TIEMPO_MS_POR_FRAME),
		new Animacion(new HojaSprite(Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/player_sprites.png").getSubimage(0, 8 * lado, lado * 4, lado),
			lado, opaca), true, this.TIEMPO_MS_POR_FRAME),
		new Animacion(new HojaSprite(
				Constantes.FUNCIONES.TEXTURAS_TOOLS.voltearImagenH(
				Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/player_sprites.png").getSubimage(0, 8 * lado, lado * 4, lado)),
			lado, opaca), true, this.TIEMPO_MS_POR_FRAME)));
	this.ANIMACIONES.put(ARMADO_CAMINANDO, new AnimacionDireccionada(
		new Animacion(new HojaSprite(Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/player_sprites.png").getSubimage(0, 10 * lado, lado * 4, lado),
			lado, opaca), true, this.TIEMPO_MS_POR_FRAME),
		new Animacion(new HojaSprite(Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/player_sprites.png").getSubimage(0, 9 * lado, lado * 4, lado),
			lado, opaca), true, this.TIEMPO_MS_POR_FRAME),
		new Animacion(new HojaSprite(Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/player_sprites.png").getSubimage(0, 11 * lado, lado * 4, lado),
			lado, opaca), true, this.TIEMPO_MS_POR_FRAME),
		new Animacion(new HojaSprite(
				Constantes.FUNCIONES.TEXTURAS_TOOLS.voltearImagenH(
				Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/player_sprites.png").getSubimage(0, 11 * lado, lado * 4, lado)),
			lado, opaca), true, this.TIEMPO_MS_POR_FRAME)));

    }

    public void pintar(final Graphics2D g, final int x, final int y) {
	final Jugador jugador = Constantes.JUGADOR;
	final boolean transparencia = jugador.atrasDeComplemento();
	final float alpha = 0.5f;
	final Direccion direccion = jugador.getDireccion();
	final HashMap<Estado, Estado> ESTADO = jugador.getEstado();
	if (jugador.pistolaEquipada() && !ESTADO.containsKey(Estado.ARROJANDO)) {
	    if (ESTADO.containsKey(Estado.ESTANDAR)) {
		if (transparencia) {
		    this.ANIMACIONES.get(ARMADO_ESTANDAR).pintarConTransparencia(g, x, y, false, alpha, direccion);
		} else {
		    this.ANIMACIONES.get(ARMADO_ESTANDAR).pintar(g, x, y, false, direccion);
		}
		return;
	    }
	    if (transparencia) {
		this.ANIMACIONES.get(ARMADO_CAMINANDO).pintarConTransparencia(g, x, y, false, alpha, direccion);
	    } else {
		this.ANIMACIONES.get(ARMADO_CAMINANDO).pintar(g, x, y, false, direccion);
	    }
	} else {
	    if (ESTADO.containsKey(Estado.ESTANDAR)) {
		if (transparencia) {
		    this.ANIMACIONES.get(ESTANDAR).pintarConTransparencia(g, x, y, false, alpha, direccion);
		} else {
		    this.ANIMACIONES.get(ESTANDAR).pintar(g, x, y, false, direccion);
		}
		return;
	    }

	    if (transparencia) {
		this.ANIMACIONES.get(CAMINANDO).pintarConTransparencia(g, x, y, false, alpha, direccion);
	    } else {
		this.ANIMACIONES.get(CAMINANDO).pintar(g, x, y, false, direccion);
	    }
	}
    }

}
