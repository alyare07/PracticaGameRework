package principal.entes;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import principal.iluminacion.FuenteLuz;
import principal.mapa.Mundo;

/**
 * Clase base abstracta para todos los elementos interactivos y renderizables
 * del mundo.
 * <p>
 * <b>Integración con el Sistema de Iluminación:</b>
 * <ul>
 * <li><b>Vinculación Bidireccional:</b> Mantiene una referencia directa a su
 * {@link FuenteLuz} asignada.</li>
 * <li><b>Destrucción O(1) Zero-GC:</b> Al invocarse {@link #eliminar()}, la luz
 * asociada se apaga y se reintegra al pool maestro automáticamente sin dejar
 * luces huérfanas en el mapa.</li>
 * </ul>
 * </p>
 * 
 * @version 3.0
 */
public abstract class Ente {

	/** Bandera que indica si el ente debe ser eliminado y purgado del mundo. */
	protected boolean eliminado;

	/** Código de frame para deduplicar el renderizado en múltiples celdas. */
	protected long codRender;

	/** Referencia al mundo al cual pertenece la entidad. */
	protected Mundo mundo;

	/**
	 * Referencia a la fuente de luz dinámica anclada a este ente (linterna, fuego,
	 * aura).
	 */
	protected FuenteLuz luzAsignada;

	/**
	 * Rectángulo reutilizable pre-asignado. Evita crear miles de objetos
	 * 'Rectangle' en el Heap durante las consultas de colisión en cada frame.
	 */
	protected final Rectangle AREA_ENTE_RETORNO = new Rectangle();

	public void actualizar() {
	}

	public void pintar(final Graphics2D g) {
	}

	/*
	 * =========================================================================
	 * EXPLICACIÓN DIDÁCTICA: DESTRUCTOR AUTOMÁTICO DE LUZ
	 * -------------------------------------------------------------------------
	 * Cuando un enemigo muere, un proyectil impacta o un cofre es destruido,
	 * 'eliminar()' cambia la bandera 'eliminado = true' y llama a
	 * 'desvincularLuz()'.
	 *
	 * Esto apaga la luz en el milisegundo exacto de la muerte y devuelve la ranura
	 * al pool maestro de GestorLuz en tiempo O(1), evitando luces fantasmas
	 * flotando.
	 * =========================================================================
	 */
	/**
	 * Marca la entidad como eliminada para su purga del mundo y apaga
	 * inmediatamente cualquier fuente de luz que tenga vinculada.
	 */
	public void eliminar() {
		this.eliminado = true;
		this.desvincularLuz();
	}

	public abstract int getPosicionXInt();

	public abstract int getPosicionYInt();

	public abstract double getPosicionX();

	public abstract double getPosicionY();

	public abstract void modificarPosicionX(final double desplazamientoX);

	public abstract void modificarPosicionY(final double desplazamientoY);

	public boolean estaEliminado() {
		return this.eliminado;
	}

	public abstract int getAncho();

	public abstract int getAlto();

	// =========================================================================
	// === GESTIÓN BIDIRECCIONAL DE FUENTE DE LUZ
	// =========================================================================

	/**
	 * Asigna y vincula una fuente de luz a esta entidad. Si ya poseía una luz
	 * previa, la anterior se apaga para evitar duplicados.
	 *
	 * @param luz Fuente de luz activa asignada.
	 */
	public void asignarLuz(final FuenteLuz luz) {
		if ((this.luzAsignada != null) && (this.luzAsignada != luz)) {
			this.luzAsignada.apagar();
		}
		this.luzAsignada = luz;
	}

	/**
	 * Apaga y desvincula la luz activa de esta entidad en tiempo $O(1)$.
	 */
	public void desvincularLuz() {
		if (this.luzAsignada != null) {
			this.luzAsignada.apagar();
			this.luzAsignada = null;
		}
	}

	public FuenteLuz getLuzAsignada() {
		return this.luzAsignada;
	}

	public boolean tieneLuzAsignada() {
		return (this.luzAsignada != null) && this.luzAsignada.isActiva();
	}

	// =========================================================================
	// === PIVOTE DE PROFUNDIDAD (Y-SORTING)
	// =========================================================================

	/**
	 * Retorna la coordenada Y del punto de contacto con el suelo para el Z-Sorting.
	 *
	 * @return Coordenada Y de la base del sprite en píxeles de mundo.
	 */
	public int getPosicionYBase() {
		return this.getPosicionYInt() + this.getAlto();
	}

	// =========================================================================
	// === ACCESORES Y GESTIÓN DE ÁREA ZERO-GC
	// =========================================================================

	public Rectangle getArea() {
		this.AREA_ENTE_RETORNO.setBounds(this.getPosicionXInt(), this.getPosicionYInt(), this.getAncho(),
				this.getAlto());
		return this.AREA_ENTE_RETORNO;
	}

	public int getCentroX() {
		return this.getPosicionXInt() + (this.getAncho() / 2);
	}

	public int getCentroY() {
		return this.getPosicionYInt() + (this.getAlto() / 2);
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