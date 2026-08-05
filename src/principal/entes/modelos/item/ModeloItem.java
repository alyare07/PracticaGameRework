package principal.entes.modelos.item;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public abstract class ModeloItem {
	private final boolean SOLIDO;
	private final Rectangle MARGENES_INTERSECCION;
	private final int ALTO;
	private final int ANCHO;
	private final int TIPO_ITEM;
	private final String NOMBRE;

	public ModeloItem(final String nombre, final int tipoItem, final int ancho, final int alto, final boolean esSolido,
			final Rectangle margenesInterseccion) {
		this.SOLIDO = esSolido;
		this.MARGENES_INTERSECCION = margenesInterseccion;
		this.ALTO = alto;
		this.ANCHO = ancho;
		this.TIPO_ITEM = tipoItem;
		this.NOMBRE = nombre;
	}

	public ModeloItem(final String nombre, final int tipoItem, final int lado, final boolean esSolido,
			final Rectangle margenesInterseccion) {
		this.SOLIDO = esSolido;
		this.MARGENES_INTERSECCION = margenesInterseccion;
		this.ALTO = lado;
		this.ANCHO = lado;
		this.TIPO_ITEM = tipoItem;
		this.NOMBRE = nombre;
	}

	public boolean esSolido() {
		return this.SOLIDO;
	}

	public Rectangle getMargenesInterseccion() {
		return MARGENES_INTERSECCION;
	}

	public abstract BufferedImage getTexturaInventario();

	public abstract BufferedImage getTexturaMapa();

	public int getTipoItem() {
		return this.TIPO_ITEM;
	}

	public int getAncho() {
		return this.ANCHO;
	}

	public int getAlto() {
		return this.ALTO;
	}

	public String getNombre() {
		return this.NOMBRE;
	}

}
