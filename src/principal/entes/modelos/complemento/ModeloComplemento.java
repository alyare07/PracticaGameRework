package principal.entes.modelos.complemento;

import java.awt.Shape;
import java.awt.image.BufferedImage;
import principal.entes.objetos.Complemento;
import principal.utilidades.Animacion;
import principal.utilidades.Textura;

public abstract class ModeloComplemento {
	private final boolean SOLIDO;
	private final int COD_IMAGEN;
	private final int ALTO;
	private final int ANCHO;
	private boolean animar;
	private final Animacion ANIMACION;
	private final boolean CONTIENE_ZONA_NO_SOLIDA;

	public ModeloComplemento(final int ancho, final int alto, final int codImagen, final boolean solido, final boolean ContieneZonaNoSolida) {
		this.SOLIDO = solido;
		this.CONTIENE_ZONA_NO_SOLIDA = solido? ContieneZonaNoSolida : false;
		this.COD_IMAGEN = codImagen;
		this.ANCHO = ancho;
		this.ALTO = alto;
		this.ANIMACION = ((G, X, Y) -> {
		});
	}

	public ModeloComplemento(final int lado, final int codImagen, final boolean solido, final boolean ContieneZonaNoSolida) {
		this.SOLIDO = solido;
		this.CONTIENE_ZONA_NO_SOLIDA = solido? ContieneZonaNoSolida : false;
		this.COD_IMAGEN = codImagen;
		this.ANCHO = lado;
		this.ALTO = lado;
		this.ANIMACION = ((G, X, Y) -> {
		});
	}

	public ModeloComplemento(final int ancho, final int alto, final int codImagen, final boolean solido, final boolean ContieneZonaNoSolida, final Animacion animacion) {
		this.SOLIDO = solido;
		this.CONTIENE_ZONA_NO_SOLIDA = solido? ContieneZonaNoSolida : false;
		this.COD_IMAGEN = codImagen;
		this.ANCHO = ancho;
		this.ALTO = alto;
		if (animacion != null) {
			this.animar = true;
			this.ANIMACION = animacion;
		} else {
			this.ANIMACION = ((G, X, Y) -> {
			});
		}
	}

	public ModeloComplemento(final int lado, final int codImagen, final boolean solido, final boolean ContieneZonaNoSolida, final Animacion animacion) {
		this.SOLIDO = solido;
		this.CONTIENE_ZONA_NO_SOLIDA = solido? ContieneZonaNoSolida : false;
		this.COD_IMAGEN = codImagen;
		this.ANCHO = lado;
		this.ALTO = lado;
		if (animacion != null) {
			this.animar = true;
			this.ANIMACION = animacion;
		} else {
			this.ANIMACION = ((G, X, Y) -> {
			});
		}
	}

	public boolean esSolido() {
		return SOLIDO;
	}
	
	public boolean contieneZonaNoSolida() {
		return this.CONTIENE_ZONA_NO_SOLIDA;
	}

	public BufferedImage getTextura() {
		return Textura.getTextura(COD_IMAGEN);
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
