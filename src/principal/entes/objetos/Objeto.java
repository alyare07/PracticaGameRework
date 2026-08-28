package principal.entes.objetos;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import principal.entes.Ente;
import principal.utilidades.DibujoDebug;
import principal.utilidades.Globales;

/**
 * Clase base abstracta para todos los objetos interactivos, contenedores y
 * elementos estáticos o dinámicos del escenario (cofres, barriles, palancas,
 * etc.).
 * <p>
 * <b>Optimizaciones de Rendimiento:</b>
 * <ul>
 * <li><b>Zero-GC en consultas espaciales:</b> {@link #getArea()} reutiliza la
 * instancia {@link #AREA_ENTE_RETORNO} preasignada en {@link Ente}, evitando la
 * instanciación de miles de objetos {@link Rectangle} por segundo en el bucle
 * principal.</li>
 * <li><b>Control de Posición (Dirty Flag):</b> Cualquier cambio en las
 * coordenadas dispara automáticamente {@link #marcarPosicionModificada()} para
 * actualizar la indexación en las celdas espaciales ({@code ZoneBox}).</li>
 * </ul>
 * </p>
 * 
 * @version 3.1
 */
public abstract class Objeto extends Ente implements Serializable {

	private static final long serialVersionUID = -465657672324L;

	/** Coordenada X de la posición del objeto en el mundo (en píxeles). */
	private int x;

	/** Coordenada Y de la posición del objeto en el mundo (en píxeles). */
	private int y;

	/**
	 * Crea un nuevo objeto del escenario en las coordenadas especificadas.
	 *
	 * @param x Posición horizontal inicial en píxeles.
	 * @param y Posición vertical inicial en píxeles.
	 */
	public Objeto(final int x, final int y) {
		this.x = x;
		this.y = y;
	}

	// =========================================================================
	// === GESTIÓN DE ÁREA Y COLISIÓN ZERO-GC
	// =========================================================================

	/**
	 * Retorna el área rectangular del objeto reutilizando la instancia interna para
	 * evitar asignaciones en el Garbage Collector.
	 *
	 * @return Instancia compartida de {@link Rectangle} con los límites actuales.
	 */
	@Override
	public Rectangle getArea() {
		this.AREA_ENTE_RETORNO.setBounds(this.x, this.y, this.getAncho(), this.getAlto());
		return this.AREA_ENTE_RETORNO;
	}

	/**
	 * Comprueba si este objeto colisiona o se intersecta con una forma geométrica
	 * dada.
	 *
	 * @param s Forma geométrica a evaluar.
	 * @return {@code true} si hay intersección; {@code false} en caso contrario.
	 */
	public boolean intersecta(final Shape s) {
		return s.intersects(this.getArea());
	}

	// =========================================================================
	// === COORDENADAS Y TRASLACIÓN CON DIRTY FLAG
	// =========================================================================

	@Override
	public int getPosicionXInt() {
		return this.x;
	}

	@Override
	public int getPosicionYInt() {
		return this.y;
	}

	@Override
	public double getPosicionX() {
		return this.x;
	}

	@Override
	public double getPosicionY() {
		return this.y;
	}

	/**
	 * Modifica la posición X mediante un desplazamiento relativo y activa el flag
	 * de modificación si hubo traslación.
	 *
	 * @param desplazamientoX Variación horizontal en píxeles.
	 */
	@Override
	public void modificarPosicionX(final double desplazamientoX) {
		if (desplazamientoX != 0.0) {
			this.x += (int) desplazamientoX;
			this.marcarPosicionModificada();
		}
	}

	/**
	 * Modifica la posición Y mediante un desplazamiento relativo y activa el flag
	 * de modificación si hubo traslación.
	 *
	 * @param desplazamientoY Variación vertical en píxeles.
	 */
	@Override
	public void modificarPosicionY(final double desplazamientoY) {
		if (desplazamientoY != 0.0) {
			this.y += (int) desplazamientoY;
			this.marcarPosicionModificada();
		}
	}

	/**
	 * Establece una nueva posición absoluta en el mundo activando el flag de
	 * modificación si las coordenadas difieren de las actuales.
	 *
	 * @param x Nueva coordenada horizontal.
	 * @param y Nueva coordenada vertical.
	 */
	@Override
	public void setPosicion(final double x, final double y) {
		if ((this.x != x) || (this.y != y)) {
			this.x = ((int) Math.round(x));
			this.y = ((int) Math.round(y));
			this.marcarPosicionModificada();
		}
	}

	public void establecerPosicionX(final int x) {
		if (this.x != x) {
			this.x = x;
			this.marcarPosicionModificada();
		}
	}

	public void establecerPosicionY(final int y) {
		if (this.y != y) {
			this.y = y;
			this.marcarPosicionModificada();
		}
	}

	// =========================================================================
	// === RENDERIZADO
	// =========================================================================

	@Override
	public void actualizar() {
		this.verificarZoneBox();
	}

	@Override
	public void pintar(final Graphics2D g) {
		DibujoDebug.dibujarImagenRefCamara(g, this.getTextura(), this.x, this.y);
		if (Globales.TECLADO.TECLA_VER_COLISIONES.presionado() && Globales.estadoJuego) {
			DibujoDebug.dibujarRectanguloContornoRefCamara(g, this.getArea(), Color.ORANGE);
		}
	}

	/**
	 * Dibuja el objeto en pantalla fija ignorando el desplazamiento de la cámara.
	 *
	 * @param g Contexto gráfico 2D.
	 */
	public void pintarFijo(final Graphics2D g) {
		DibujoDebug.dibujarImagen(g, this.getTextura(), this.getPosicionXInt(), this.getPosicionYInt());
	}

	// =========================================================================
	// === CONTRATOS ABSTRACTOS
	// =========================================================================

	@Override
	public abstract int getAncho();

	@Override
	public abstract int getAlto();

	/**
	 * Retorna la textura visual asignada a este objeto.
	 *
	 * @return Imagen de textura del sprite.
	 */
	public abstract BufferedImage getTextura();

	/**
	 * Determina si el objeto bloquea el paso físico de criaturas y entidades.
	 *
	 * @return {@code true} si tiene colisión sólida; {@code false} si es
	 *         atravesable.
	 */
	public abstract boolean esSolido();

	/**
	 * Crea un duplicado superficial o profundo de este objeto para instanciación
	 * rápida.
	 *
	 * @return Clon o copia del objeto.
	 */
	public abstract Objeto copiar();
}