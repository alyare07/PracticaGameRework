package principal.entes.modelos.complemento;

import java.awt.Shape;
import java.awt.image.BufferedImage;

import principal.entes.objetos.Complemento;
import principal.utilidades.Animacion;
import principal.utilidades.Globales;

public abstract class ModeloComplemento {

	private final boolean SOLIDO;
	private final BufferedImage textura;
	private final int ALTO;
	private final int ANCHO;
	private boolean animar;
	private final Animacion ANIMACION;
	private final boolean CONTIENE_ZONA_NO_SOLIDA;

	public ModeloComplemento(final int ancho, final int alto, final BufferedImage textura, final boolean solido,
			final boolean contieneZonaNoSolida) {
		this.SOLIDO = solido;
		this.CONTIENE_ZONA_NO_SOLIDA = solido && contieneZonaNoSolida;
		this.textura = textura;
		this.ANCHO = ancho;
		this.ALTO = alto;
		this.ANIMACION = ((g, x, y) -> {
		});
	}

	public ModeloComplemento(final int lado, final BufferedImage textura, final boolean solido,
			final boolean contieneZonaNoSolida) {
		this(lado, lado, textura, solido, contieneZonaNoSolida);
	}

	public ModeloComplemento(final int ancho, final int alto, final BufferedImage textura, final boolean solido,
			final boolean contieneZonaNoSolida, final Animacion animacion) {
		this.SOLIDO = solido;
		this.CONTIENE_ZONA_NO_SOLIDA = solido && contieneZonaNoSolida;
		this.textura = textura;
		this.ANCHO = ancho;
		this.ALTO = alto;
		if (animacion != null) {
			this.animar = true;
			this.ANIMACION = animacion;
		} else {
			this.ANIMACION = ((g, x, y) -> {
			});
		}
	}

	public ModeloComplemento(final int lado, final BufferedImage textura, final boolean solido,
			final boolean contieneZonaNoSolida, final Animacion animacion) {
		this(lado, lado, textura, solido, contieneZonaNoSolida, animacion);
	}

	public boolean esSolido() {
		return this.SOLIDO;
	}

	public boolean contieneZonaNoSolida() {
		return this.CONTIENE_ZONA_NO_SOLIDA;
	}

	public BufferedImage getTextura() {
		return (this.textura != null) ? this.textura : Globales.GESTOR_TEXTURAS.getTexturaError();
	}

	public int getAncho() {
		return this.ANCHO;
	}

	public int getAlto() {
		return this.ALTO;
	}

	public boolean animar() {
		return this.animar;
	}

	public void establecerAnimar(final boolean animar) {
		this.animar = animar;
	}

	public Animacion getAnimacion() {
		return this.ANIMACION;
	}

	public abstract boolean intersecta(final Shape area, final Complemento cPropietario);
}