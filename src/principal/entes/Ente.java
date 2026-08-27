package principal.entes;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import principal.mapa.Mundo;

/**
 * Clase base abstracta para todos los elementos activos, interactivos y
 * renderizables del mundo del juego (Criaturas, Jugador, Objetos,
 * Complementos).
 * <p>
 * <b>Optimizaciones de Memoria y Profundidad:</b>
 * <ul>
 * <li><b>Zero-GC en Consultas Geométricas:</b> Reutiliza la estructura fija
 * {@link #AREA_ENTE_RETORNO} para evitar instanciar {@code new Rectangle()} en
 * colisiones.</li>
 * <li><b>Pivote de Profundidad (Y-Base Anchor):</b> Define
 * {@link #getPosicionYBase()} como el punto de contacto con el suelo para el
 * ordenamiento de capas (Y-Sorting).</li>
 * </ul>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public abstract class Ente {

	/** Bandera que indica si el ente debe ser eliminado y purgado del mundo. */
	protected boolean eliminado;

	/** Código de frame para deduplicar el renderizado en múltiples celdas. */
	protected long codRender;

	/** Referencia al mundo al cual pertenece la entidad. */
	protected Mundo mundo;

	/**
	 * Rectángulo reutilizable pre-asignado. Evita crear miles de objetos
	 * 'Rectangle' en el Heap durante las consultas de colisión en cada frame.
	 */
	protected final Rectangle AREA_ENTE_RETORNO = new Rectangle();

	public void actualizar() {
	}

	public void pintar(final Graphics2D g) {
	}

	public abstract void eliminar();

	public abstract int getPosicionXInt();

	public abstract int getPosicionYInt();

	public abstract double getPosicionX();

	public abstract double getPosicionY();

	public abstract void modificarPosicionX(final double desplazamientoX);

	public abstract void modificarPosicionY(final double desplazamientoY);

	public abstract boolean estaEliminado();

	public abstract int getAncho();

	public abstract int getAlto();

	// =========================================================================
	// === PIVOTE DE PROFUNDIDAD (Y-SORTING)
	// =========================================================================

	/*
	 * =========================================================================
	 * EXPLICACIÓN DIDÁCTICA: ¿QUÉ ES EL Y-BASE Y POR QUÉ ES VITAL?
	 * ------------------------------------------------------------------------- En
	 * un juego top-down, la "profundidad" de un sprite no es su esquina superior
	 * izquierda (Y), sino el punto donde sus PIES o RAÍCES tocan la tierra.
	 * 
	 * Si un árbol mide 96 px de alto y el jugador mide 32 px: - Cuando el jugador
	 * camina detrás del tronco (Y_base_jugador < Y_base_arbol), el árbol se dibuja
	 * DESPUÉS del jugador, tapando su cuerpo con las hojas. - Cuando el jugador
	 * pasa por delante de las raíces (Y_base_jugador > Y_base_arbol), el jugador se
	 * dibuja DESPUÉS del árbol, quedando al frente.
	 * =========================================================================
	 */
	/**
	 * Retorna la coordenada Y del punto de apoyo / contacto con el suelo de la
	 * entidad. Utilizado por el motor de renderizado para calcular el Z-Index en
	 * tiempo real.
	 *
	 * @return Coordenada Y de la base del sprite en píxeles de mundo.
	 */
	public int getPosicionYBase() {
		return this.getPosicionYInt() + this.getAlto();
	}

	// =========================================================================
	// === ACCESORES Y GESTIÓN DE ÁREA ZERO-GC
	// =========================================================================

	/**
	 * Retorna el delimitador rectangular de la entidad reutilizando memoria fija.
	 *
	 * @return {@link Rectangle} pre-asignado mutado con las dimensiones actuales.
	 */
	public Rectangle getArea() {
		this.AREA_ENTE_RETORNO.setBounds(this.getPosicionXInt(), this.getPosicionYInt(), this.getAncho(),
				this.getAlto());
		return this.AREA_ENTE_RETORNO;
	}

	public long getCodRender() {
		return this.codRender;
	}

	public void setCodRender(final long cod) {
		this.codRender = cod;
	}

	public void setMundo(final Mundo mundo) {
		this.mundo = mundo;
	}

	public Mundo getMundo() {
		return this.mundo;
	}
}