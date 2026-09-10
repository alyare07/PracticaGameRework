package principal.entes.modelos.complemento;

import java.awt.Shape;
import java.awt.image.BufferedImage;

import principal.entes.objetos.Complemento;
import principal.utilidades.Animacion;
import principal.utilidades.Globales;

/**
 * Metadatos Flyweight para complementos del escenario con soporte de colisiones
 * AABB/T2, animación de frames y balanceo eólico vegetal (Zero-GC).
 * 
 * @version 2.1 (Vanilla Java 8 - Vegetation Wind Flag Integration)
 */
public abstract class ModeloComplemento {

	private final boolean SOLIDO;
	private final BufferedImage textura;
	private final int ALTO;
	private final int ANCHO;
	private boolean animar;
	private final Animacion ANIMACION;
	private final boolean CONTIENE_ZONA_NO_SOLIDA;
	private final boolean ES_VEGETACION;

	public ModeloComplemento(final int ancho, final int alto, final BufferedImage textura, final boolean solido,
			final boolean contieneZonaNoSolida, final boolean esVegetacion) {
		this.SOLIDO = solido;
		this.CONTIENE_ZONA_NO_SOLIDA = solido && contieneZonaNoSolida;
		this.ES_VEGETACION = esVegetacion;
		this.textura = textura;
		this.ANCHO = ancho;
		this.ALTO = alto;
		this.ANIMACION = ((g, x, y) -> {
		});
	}

	public ModeloComplemento(final int ancho, final int alto, final BufferedImage textura, final boolean solido,
			final boolean contieneZonaNoSolida) {
		this(ancho, alto, textura, solido, contieneZonaNoSolida, false);
	}

	public ModeloComplemento(final int lado, final BufferedImage textura, final boolean solido,
			final boolean contieneZonaNoSolida, final boolean esVegetacion) {
		this(lado, lado, textura, solido, contieneZonaNoSolida, esVegetacion);
	}

	public ModeloComplemento(final int lado, final BufferedImage textura, final boolean solido,
			final boolean contieneZonaNoSolida) {
		this(lado, lado, textura, solido, contieneZonaNoSolida, false);
	}

	public ModeloComplemento(final int ancho, final int alto, final BufferedImage textura, final boolean solido,
			final boolean contieneZonaNoSolida, final Animacion animacion, final boolean esVegetacion) {
		this.SOLIDO = solido;
		this.CONTIENE_ZONA_NO_SOLIDA = solido && contieneZonaNoSolida;
		this.ES_VEGETACION = esVegetacion;
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

	public ModeloComplemento(final int ancho, final int alto, final BufferedImage textura, final boolean solido,
			final boolean contieneZonaNoSolida, final Animacion animacion) {
		this(ancho, alto, textura, solido, contieneZonaNoSolida, animacion, false);
	}

	public ModeloComplemento(final int lado, final BufferedImage textura, final boolean solido,
			final boolean contieneZonaNoSolida, final Animacion animacion) {
		this(lado, lado, textura, solido, contieneZonaNoSolida, animacion, false);
	}

	public boolean esSolido() {
		return this.SOLIDO;
	}

	public boolean contieneZonaNoSolida() {
		return this.CONTIENE_ZONA_NO_SOLIDA;
	}

	public boolean esVegetacion() {
		return this.ES_VEGETACION;
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