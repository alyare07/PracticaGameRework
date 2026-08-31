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

/**
 * Clase base abstracta para todos los proyectiles balísticos, mágicos y de área
 * del juego.
 * <p>
 * <b>MOTOR BALÍSTICO EN 360° (Zero-GC / O(1)):</b> Soporta trayectorias
 * vectoriales continuas en cualquier ángulo calculando los componentes
 * {@code velX} y {@code velY} una sola vez en el constructor. El movimiento en
 * cada frame no realiza cálculos trigonométricos lentos ni genera objetos en el
 * Heap.
 * </p>
 * 
 * @version 2.5 (Java 8 Compatible - Balística Vectorial 360°)
 */
public abstract class Proyectil extends Ente implements Serializable {

	private static final long serialVersionUID = -5309717278363977059L;

	protected final double DAMAGE;
	protected final double VELOCIDAD;
	protected final boolean PENETRANTE;
	protected final double ALCANCE;
	protected final Direccion DIRECCION;
	protected final Ente CAUSANTE;

	/** Velocidad vectorial continua en el eje horizontal. */
	protected double velX;

	/** Velocidad vectorial continua en el eje vertical. */
	protected double velY;

	protected double distanciaRecorrida;
	protected boolean eliminado;
	protected double x;
	protected double y;
	protected int ancho;
	protected int alto;

	/**
	 * Constructor estándar por dirección cardinal (Norte, Sur, Este, Oeste).
	 */
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

		// Conversión de dirección cardinal a vector de velocidad constante
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

	/**
	 * Constructor vectorial en 360 grados hacia un punto de destino exacto del
	 * mundo.
	 *
	 * @param xOrigen  Posición X de disparo.
	 * @param yOrigen  Posición Y de disparo.
	 * @param xDestino Coordenada X del objetivo/cursor.
	 * @param yDestino Coordenada Y del objetivo/cursor.
	 */
	public Proyectil(final double damage, final double velocidad, final boolean penetrante, final double alcance,
			final Mundo mundo, final double xOrigen, final double yOrigen, final double xDestino, final double yDestino,
			final int ancho, final int alto, final Ente causante) {
		this.DAMAGE = damage;
		this.VELOCIDAD = velocidad;
		this.PENETRANTE = penetrante;
		this.ALCANCE = alcance;
		this.mundo = mundo;
		this.distanciaRecorrida = 0;
		this.eliminado = false;
		this.x = xOrigen;
		this.y = yOrigen;
		this.ancho = ancho;
		this.alto = alto;
		this.CAUSANTE = causante;

		// Cálculo vectorial normalizado en 360 grados
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

		// Asignación de orientación aproximada para compatibilidad gráfica
		if (Math.abs(dx) > Math.abs(dy)) {
			this.DIRECCION = (dx > 0) ? Direccion.ESTE : Direccion.OESTE;
		} else {
			this.DIRECCION = (dy > 0) ? Direccion.SUR : Direccion.NORTE;
		}
	}

	@Override
	public void pintar(final Graphics2D g) {
		if (Globales.TECLADO.TECLA_VER_COLISIONES.presionado() && Globales.estadoJuego) {
			Render2D.dibujarRectanguloContornoRefCamara(g, this.getArea(), Color.gray);
		}
	}

	/**
	 * Desplaza el proyectil a lo largo de su vector en tiempo $O(1)$ sin
	 * asignaciones de memoria.
	 */
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
		return new Rectangle((int) x, (int) y, ancho, alto);
	}

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

	public abstract void pintarAnimacionImpacto(final Graphics2D g);
}