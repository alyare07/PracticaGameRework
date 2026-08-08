package principal.utilidades;

import java.awt.image.BufferedImage;

/**
 * Encargada de obtener todos los sprites de una imagen y gestionarlos segun se
 * le solicite. Facilitar su carga, etc.
 */
public class HojaSprite{

    final private int anchoHojaEnPixeles;
    final private int altoHojaEnPixeles;

    final private int anchoHojaEnSprites;
    final private int altoHojaEnSprites;

    final private int anchoSprites;
    final private int altoSprites;

    final private BufferedImage[] sprites;

    public HojaSprite(final String ruta, final int ladoSprites, final boolean hojaOpaca) {
	BufferedImage imagen;
	if (hojaOpaca) {
	    imagen = Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleOpaca(ruta);
	} else {
	    imagen = Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida(ruta);
	}

	this.anchoHojaEnPixeles = imagen.getWidth();
	this.altoHojaEnPixeles = imagen.getHeight();

	this.anchoHojaEnSprites = this.anchoHojaEnPixeles / ladoSprites;
	this.altoHojaEnSprites = this.altoHojaEnPixeles / ladoSprites;

	this.anchoSprites = ladoSprites;
	this.altoSprites = ladoSprites;
	this.sprites = new BufferedImage[this.anchoHojaEnSprites * this.altoHojaEnSprites];
	this.rellenarSpritesDesdeImagen(imagen);
    }

    public HojaSprite(final BufferedImage imagen, final int ladoSprites, final boolean hojaOpaca) {
	this.anchoHojaEnPixeles = imagen.getWidth();
	this.altoHojaEnPixeles = imagen.getHeight();

	this.anchoHojaEnSprites = this.anchoHojaEnPixeles / ladoSprites;
	this.altoHojaEnSprites = this.altoHojaEnPixeles / ladoSprites;

	this.anchoSprites = ladoSprites;
	this.altoSprites = ladoSprites;
	this.sprites = new BufferedImage[this.anchoHojaEnSprites * this.altoHojaEnSprites];
	this.rellenarSpritesDesdeImagen(imagen);
    }

    public HojaSprite(final BufferedImage imagen, final int anchoSprites, final int altoSprites, final boolean hojaOpaca) {
	this.anchoHojaEnPixeles = imagen.getWidth();
	this.altoHojaEnPixeles = imagen.getHeight();

	this.anchoHojaEnSprites = this.anchoHojaEnPixeles / anchoSprites;
	this.altoHojaEnSprites = this.altoHojaEnPixeles / altoSprites;

	this.anchoSprites = anchoSprites;
	this.altoSprites = altoSprites;
	this.sprites = new BufferedImage[this.anchoHojaEnSprites * this.altoHojaEnSprites];
	this.rellenarSpritesDesdeImagen(imagen);
    }

    public HojaSprite(final String ruta, final int anchoSprites, final int altoSprites, final boolean hojaOpaca) {
	BufferedImage imagen;
	if (hojaOpaca) {
	    imagen = Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleOpaca(ruta);
	} else {
	    imagen = Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida(ruta);
	}

	this.anchoHojaEnPixeles = imagen.getWidth();
	this.altoHojaEnPixeles = imagen.getHeight();

	this.anchoHojaEnSprites = this.anchoHojaEnPixeles / anchoSprites;
	this.altoHojaEnSprites = this.altoHojaEnPixeles / altoSprites;

	this.anchoSprites = anchoSprites;
	this.altoSprites = altoSprites;
	this.sprites = new BufferedImage[this.anchoHojaEnSprites * this.altoHojaEnSprites];
	this.rellenarSpritesDesdeImagen(imagen);
    }

    /**
     * Rellena el vector de sprites obteniendolos desde la imagen mencionada los
     * atributos que tambien se usaran son: anchoHojaEnPixeles, altoHojaEnPixeles,
     * anchoSprites, altoSprites (Tenerlos ya inicializados).
     * 
     * @param imagen La imagen de la que se sacaran los sprites.
     */
    private void rellenarSpritesDesdeImagen(final BufferedImage imagen) {
	for (int y = 0; y < this.altoHojaEnSprites; y++) {
	    for (int x = 0; x < this.anchoHojaEnSprites; x++) {
		final int posicionX = x * this.anchoSprites;
		final int posicionY = y * this.altoSprites;
		this.sprites[x + y * this.anchoHojaEnSprites] = imagen.getSubimage(posicionX, posicionY, this.anchoSprites, this.altoSprites);
	    }
	}
    }

    /**
     * Obtener un sprite de la Hoja De Sprite.
     * 
     * @param indice Posicion del sprite en el vector
     * @return
     */
    public BufferedImage getSprite(final int indice) {
	return this.sprites[indice];
    }

    /**
     * Obtener un sprite de la Hoja De Sprite.
     * 
     * @param x El valor X en la coordena de la posicion del Sprite en la imagen
     * @param y El valor Y en la coordena de la posicion del Sprite en la imagen
     * @return El sprite ubicado en dicha posicion.
     */
    public BufferedImage getSprite(final int x, final int y) {
	return this.sprites[x + y * this.anchoHojaEnSprites];
    }

    /**
     * Obtener un sprite de una imagen.
     * 
     * @param imagen La imagen de la que se sacara el sprite.
     * @param x      La posicion X del sprite en la imagen.
     * @param y      La posicion Y del sprite en la imagen.
     * @param ancho  El ancho del sprite en la imagen.
     * @param alto   El alto del sprite en la imagen.
     * @return El sprite obtenido del area mencionada para la imagen.
     */
    public static BufferedImage getSpriteEspecifico(final BufferedImage imagen, final int x, final int y, final int ancho, final int alto) {
	return imagen.getSubimage(x, y, ancho, alto);
    }

    /**
     * Obtener un sprite de una imagen.
     * 
     * @param imagen La imagen de la que se sacara el sprite.
     * @param indice La posicion del sprite a obtener de la imagen (indice avanza de
     *               1 en 1, izq a der, arriba hacia abajo)
     * @param lado   Especifica el valor del ancho y alto que tendra cada sprite en
     *               dicha imagen.
     * @return El sprite obtenido de la posicion mencionada para la imagen.
     */
    public static BufferedImage getSpriteIndice(final BufferedImage imagen, final int indice, final int lado) {
	if (indice < 0 || indice > (imagen.getHeight() * imagen.getWidth() - lado)) {
	    return new BufferedImage(lado, lado, BufferedImage.TYPE_INT_RGB);
	}
	int contador = 0;
	for (int y = 0; y < imagen.getHeight(); y += lado) {
	    for (int x = 0; x < imagen.getWidth(); x += lado) {
		contador++;
		if (contador == indice) {
		    return imagen.getSubimage(x, y, lado, lado);
		}
	    }
	}
	return new BufferedImage(lado, lado, BufferedImage.TYPE_INT_RGB);
    }

    /**
     * 
     * @param ruta  La ruta donde se encuentra la imagen a cargar para sacar el
     *              sprite.
     * @param x     La posicion X del sprite en la imagen.
     * @param y     La posicion Y del sprite en la imagen.
     * @param ancho El ancho del sprite en la imagen.
     * @param alto  El alto del sprite en la imagen.
     * @param opaca (True) -> Opaca. (False) -> Translucida
     * @return El sprite obtenido de la posicion mencionada para la imagen cargada.
     */
    public static BufferedImage getSpriteEspecifico(final String ruta, final int x, final int y, final int ancho, final int alto, final boolean opaca) {
	if (opaca) {
	    return Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleOpaca(ruta).getSubimage(x, y, ancho, alto);
	} else {
	    return Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida(ruta).getSubimage(x, y, ancho, alto);
	}

    }

    /**
     * 
     * @param ruta   La ruta donde se encuentra la imagen a cargar para sacar el
     *               sprite.
     * @param indice indice La posicion del sprite a obtener de la imagen (indice
     *               avanza de 1 en 1, izq a der, arriba hacia abajo)
     * @param lado   Especifica el valor del ancho y alto que tendra cada sprite en
     *               dicha imagen.
     * @param opaca  (True) -> Opaca. (False) -> Translucida
     * @return El sprite obtenido de la posicion mencionada para la imagen cargada.
     */
    public static BufferedImage getSpriteIndice(final String ruta, final int indice, final int lado, final boolean opaca) {

	BufferedImage imagen = null;
	if (opaca) {
	    imagen = Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleOpaca(ruta);
	} else {
	    imagen = Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida(ruta);
	}
	if (indice < 0 || indice > (imagen.getHeight() * imagen.getWidth() - lado)) {
	    return new BufferedImage(lado, lado, BufferedImage.TYPE_INT_RGB);
	}
	int contador = 0;
	for (int y = 0; y < imagen.getHeight(); y += lado) {
	    for (int x = 0; x < imagen.getWidth(); x += lado) {
		contador++;
		if (contador == indice) {
		    imagen = imagen.getSubimage(x, y, lado, lado);
		}
	    }
	}
	return imagen;
    }

    /**
     * Obtiene un sprite en el indice mencionado y lo invierte horizontalmente.
     * 
     * @param indice La posicion en el vector (0 seria la primera posicion).
     * @return El esprite obtenido, pero invertido horizontalmente.
     */
    public BufferedImage getSpriteInvertidoHorizontal(final int indice) {
	return Constantes.FUNCIONES.TEXTURAS_TOOLS.voltearImagenH(this.sprites[indice]);
    }

    /**
     * Obtiene un sprite en el indice mencionado y lo invierte verticalmente.
     * 
     * @param indice La posicion en el vector (0 seria la primera posicion).
     * @return El esprite obtenido, pero invertido verticalmente.
     */
    public BufferedImage getSpriteInvertidoVertical(final int indice) {
	return Constantes.FUNCIONES.TEXTURAS_TOOLS.voltearImagenV(this.sprites[indice]);
    }

    /**
     * Obtiene un sprite en el indice mencionado y lo rota 90° a la izquierda.
     * 
     * @param indice La posicion en el vector (0 seria la primera posicion).
     * @return El esprite obtenido, pero rotado 90° a la izquierda.
     */
    public BufferedImage getSpriteRotado90GradosIzquierda(final int indice) {
	return Constantes.FUNCIONES.TEXTURAS_TOOLS.voltearImagen90GradosIzquierda(this.sprites[indice]);
    }

    /**
     * Obtiene un sprite en el indice mencionado y lo rota 90° a la derecha.
     * 
     * @param indice La posicion en el vector (0 seria la primera posicion).
     * @return El esprite obtenido, pero rotado 90° a la derecha.
     */
    public BufferedImage getSpriteRotado90GradosDerecha(final int indice) {
	return Constantes.FUNCIONES.TEXTURAS_TOOLS.voltearImagen90GradosDerecha(this.sprites[indice]);
    }

    public int getCantidadSprite() {
	return this.sprites.length;
    }

}
