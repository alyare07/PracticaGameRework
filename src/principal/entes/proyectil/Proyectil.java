package principal.entes.proyectil;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.Serializable;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Criatura.Direccion;
import principal.mapa.Mundo;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;

public abstract class Proyectil extends Ente implements Serializable {

	private static final long serialVersionUID = -5309717278363977059L;
	protected final double DAMAGE;
	protected final double VELOCIDAD;
	protected final boolean PENETRANTE;
	protected final double ALCANCE;
	protected final Direccion DIRECCION;
	protected final Ente CAUSANTE;
	protected double distanciaRecorrida;
	protected boolean eliminado;
	protected double x;
	protected double y;
	protected int ancho;
	protected int alto;

	public Proyectil(final double damage, final double velocidad, final boolean penetrante, final double alcance,
			final Mundo mundo, final double x, final double y, final int ancho, final int alto,
			final Direccion direccion, final Ente causante) {
		this.DAMAGE = damage;
		this.VELOCIDAD = velocidad;
		this.PENETRANTE = penetrante;
		this.ALCANCE = alcance;
		this.mundo = mundo;
		this.distanciaRecorrida = 0;
		this.eliminado = false;
		this.x = x;
		this.y = y;
		this.ancho = ancho;
		this.alto = alto;
		this.DIRECCION = direccion;
		this.CAUSANTE = causante;
	}

	@Override
	public void pintar(final Graphics2D g) {
		if (Constantes.TECLADO.TECLA_VER_COLISIONES.presionado() && Constantes.GLOBALES.estadoJuego) {
			DibujoDebug.dibujarRectanguloContornoRefCamara(g, this.getArea(), Color.gray);
		}
	}

	protected void mover() {
		this.distanciaRecorrida += this.VELOCIDAD;
		if (this.DIRECCION == Direccion.OESTE) {
			this.x -= this.VELOCIDAD;
			if (this.distanciaRecorrida > this.ALCANCE) {
				this.x += (this.distanciaRecorrida - this.ALCANCE);
				this.distanciaRecorrida = this.ALCANCE;
			}
		} else if (this.DIRECCION == Direccion.NORTE) {
			this.y -= this.VELOCIDAD;
			if (this.distanciaRecorrida > this.ALCANCE) {
				this.y += (this.distanciaRecorrida - this.ALCANCE);
				this.distanciaRecorrida = this.ALCANCE;
			}
		} else if (this.DIRECCION == Direccion.ESTE) {
			this.x += this.VELOCIDAD;
			if (this.distanciaRecorrida > this.ALCANCE) {
				this.x -= (this.distanciaRecorrida - this.ALCANCE);
				this.distanciaRecorrida = this.ALCANCE;
			}
		} else if (this.DIRECCION == Direccion.SUR) {
			this.y += this.VELOCIDAD;
			if (this.distanciaRecorrida > this.ALCANCE) {
				this.y -= (this.distanciaRecorrida - this.ALCANCE);
				this.distanciaRecorrida = this.ALCANCE;
			}
		}

	}

	public Direccion getDireccion() {
		return this.DIRECCION;
	}

	protected abstract void impactar(final Criatura c);

	public static Rectangle reformarAreaHorizontalVertical(final Rectangle areaI) {
		final Rectangle areaF = new Rectangle();
		areaF.x = areaI.x;
		areaF.y = areaI.y;
		areaF.width = areaI.height;
		areaF.height = areaI.width;

		return areaF;
	}

	public static Rectangle reformarAreaHorizontalVertical(final double x, final double y, final int ancho,
			final int alto) {
		final Rectangle areaF = new Rectangle((int) x, (int) y, ancho, alto);

		return areaF;
	}

	@Override
	public int getAncho() {
		return this.ancho;
	}

	@Override
	public int getAlto() {
		return this.alto;
	}

	// sirve este metodo? no recuerdo que se use!
	public abstract void pintarAnimacionImpacto(final Graphics2D g);

}
