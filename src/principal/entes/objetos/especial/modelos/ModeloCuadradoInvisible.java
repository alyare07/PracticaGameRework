package principal.entes.objetos.especial.modelos;

import java.awt.Color;
import java.awt.image.BufferedImage;

import principal.utilidades.Textura;

public class ModeloCuadradoInvisible {
	private final int lado;
	private final boolean solido;
	private final Color color;

	public ModeloCuadradoInvisible(final int lado, final boolean solido, final Color color) {
		this.lado = lado;
		this.solido = solido;
		this.color = color;
	}

	public int getLado() {
		return this.lado;
	}

	public boolean esSolido() {
		return this.solido;
	}

	public BufferedImage getImagen() {
		return Textura.getTextura(Textura.TEXTURA_TRANSPARENTE);
	}

	public Color getColor() {
		return this.color;
	}

}
