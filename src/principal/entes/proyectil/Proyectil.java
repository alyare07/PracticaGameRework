package principal.entes.proyectil;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.Serializable;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Criatura.Direccion;
import principal.mapa.Mundo;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

public abstract class Proyectil extends Ente implements Serializable {

	private static final long serialVersionUID = -5309717278363977059L;

	protected final double DAMAGE;
	protected final double VELOCIDAD;
	protected final boolean PENETRANTE;
	protected final double ALCANCE;
	protected final Direccion DIRECCION;
	protected final Ente CAUSANTE;

	protected double velX;
	protected double velY;
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
		this.distanciaRecorrida = 0.0;
		this.eliminado = false;
		this.x = x;
		this.y = y;
		this.ancho = ancho;
		this.alto = alto;
		this.DIRECCION = direccion;
		this.CAUSANTE = causante;

		if (direccion == Direccion.OESTE) {
			this.velX = -velocidad;
			this.velY = 0.0;
		} else if (direccion == Direccion.ESTE) {
			this.velX = velocidad;
			this.velY = 0.0;
		} else if (direccion == Direccion.NORTE) {
			this.velX = 0.0;
			this.velY = -velocidad;
		} else if (direccion == Direccion.SUR) {
			this.velX = 0.0;
			this.velY = velocidad;
		} else {
			this.velX = 0.0;
			this.velY = 0.0;
		}
	}

	public Proyectil(final double damage, final double velocidad, final boolean penetrante, final double alcance,
			final Mundo mundo, final double xOrigen, final double yOrigen, final double xDestino, final double yDestino,
			final int ancho, final int alto, final Ente causante) {
		this.DAMAGE = damage;
		this.VELOCIDAD = velocidad;
		this.PENETRANTE = penetrante;
		this.ALCANCE = alcance;
		this.mundo = mundo;
		this.distanciaRecorrida = 0.0;
		this.eliminado = false;
		this.x = xOrigen;
		this.y = yOrigen;
		this.ancho = ancho;
		this.alto = alto;
		this.CAUSANTE = causante;

		final double dx = xDestino - xOrigen;
		final double dy = yDestino - yOrigen;
		final double dist = Math.hypot(dx, dy);

		if (dist > 0.0001) {
			this.velX = (dx / dist) * velocidad;
			this.velY = (dy / dist) * velocidad;
		} else {
			this.velX = velocidad;
			this.velY = 0.0;
		}

		if (Math.abs(dx) > Math.abs(dy)) {
			this.DIRECCION = (dx > 0) ? Direccion.ESTE : Direccion.OESTE;
		} else {
			this.DIRECCION = (dy > 0) ? Direccion.SUR : Direccion.NORTE;
		}
	}

	@Override
	public void pintar(final Graphics2D g) {
		if (Globales.TECLADO.TECLA_VER_COLISIONES.presionado() && Globales.estadoJuego) {
			Render2D.dibujarRectanguloContornoRefCamara(g, this.getArea(), Color.GRAY);
		}
	}

	protected void mover() {
		this.x += this.velX;
		this.y += this.velY;
		this.distanciaRecorrida += this.VELOCIDAD;
	}

	@Override
	public void setPosicion(final double x, final double y) {
		this.x = x;
		this.y = y;
	}

	public Direccion getDireccion() {
		return this.DIRECCION;
	}

	public double getVelX() {
		return this.velX;
	}

	public double getVelY() {
		return this.velY;
	}

	public Ente getCausante() {
		return this.CAUSANTE;
	}

	protected abstract void impactar(final Criatura c);

	@Override
	public int getAncho() {
		return this.ancho;
	}

	@Override
	public int getAlto() {
		return this.alto;
	}

	@Override
	public Rectangle getArea() {
		this.AREA_ENTE_RETORNO.setBounds((int) this.x, (int) this.y, this.ancho, this.alto);
		return this.AREA_ENTE_RETORNO;
	}

	@Override
	public int getPosicionXInt() {
		return (int) this.x;
	}

	@Override
	public int getPosicionYInt() {
		return (int) this.y;
	}

	@Override
	public double getPosicionX() {
		return this.x;
	}

	@Override
	public double getPosicionY() {
		return this.y;
	}

	@Override
	public void modificarPosicionX(final double desplazamientoX) {
		this.x += desplazamientoX;
	}

	@Override
	public void modificarPosicionY(final double desplazamientoY) {
		this.y += desplazamientoY;
	}

	@Override
	public boolean estaEliminado() {
		return this.eliminado;
	}

	@Override
	public void eliminar() {
		this.eliminado = true;
	}

	public abstract void pintarAnimacionImpacto(final Graphics2D g);
}