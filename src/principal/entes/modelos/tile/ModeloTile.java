package principal.entes.modelos.tile;

import java.awt.image.BufferedImage;

import principal.animaciones.Animacion;
import principal.utilidades.Textura;

public class ModeloTile {
	private static int siguienteId = 1;
	public static final int ESTADO_TRASPASABLE = 0;
	public static final int ESTADO_OBSTACULO = 1;
	public static final int ESTADO_MULTIPLE = 2;
	protected final int ESTADO;
	protected int COD_TEXTURA;
	protected final int id;
	protected final double ALTERACION_VELOCIDAD;
	protected Animacion animacion;
	
	protected ModeloTile(final int estado, final int textura, final double alteracionVelocidad) {
		this.ESTADO = estado;
		this.COD_TEXTURA = textura;
		this.id = getSIguienteId();
		this.ALTERACION_VELOCIDAD = alteracionVelocidad;
	}
	
	protected ModeloTile(final int estado, final Animacion animacion, final double alteracionVelocidad, final int codTexturaDefecto) {
		this.ESTADO = estado;
		this.COD_TEXTURA = codTexturaDefecto;
		this.animacion = animacion;
		this.id = getSIguienteId();
		this.ALTERACION_VELOCIDAD = alteracionVelocidad;
	}


	public static int getSIguienteId() {
		int id = siguienteId;
		siguienteId++;
		return id;
	}

	public void establecerTextura(final int textura) {
		this.COD_TEXTURA = textura;
	}

	public BufferedImage getTextura() {
		return Textura.getTextura(this.COD_TEXTURA);
	}
	
	public int getCodTextura() {
		return this.COD_TEXTURA;
	}

	public int getEstado() {
		return this.ESTADO;
	}

	public int getId() {
		return this.id;
	}

	public double getAlteracionVelocidad() {
		return this.ALTERACION_VELOCIDAD;
	}
	
	public Animacion getAnimacion() {
		return this.animacion;
	}
	
	public boolean contieneAnimacion() {
		return this.animacion != null;
	}

	@Override
	public String toString() {
		return "[estado=" + ESTADO + ", textura=" + COD_TEXTURA + ", altVel: "
				+ (this.ALTERACION_VELOCIDAD > 0 ? "+" + String.valueOf(this.ALTERACION_VELOCIDAD) : String.valueOf(this.ALTERACION_VELOCIDAD)) + ", id=" + id + "]";
	}


}
