package principal.entes;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;

import principal.iluminacion.FuenteLuz;
import principal.mapa.Mundo;
import principal.mapa.renderEntidades.ZoneBox;

/**
 * Clase base abstracta para todos los elementos interactivos y renderizables
 * del mundo.
 * <p>
 * <b>Integración con el Sistema de Iluminación y Rendimiento:</b>
 * <ul>
 * <li><b>Deduplicación O(1) Zero-GC:</b> Incorpora códigos de frame directos
 * ({@link #codUltimaActualizacion} y {@link #codUltimoPintado}) que eliminan la
 * necesidad de consultas o wrappers en HashMaps durante la iteración de celdas
 * espaciales.</li>
 * <li><b>Autogestión de Particionado Espacial:</b> Administra directamente las
 * celdas que solapa mediante {@link #zonasOcupadas} y
 * {@link #verificarZoneBox()}, eliminando la capa intermedia de
 * {@code RenderEntidad}.</li> únicamente cuando la entidad cambia de
 * posición.</li>
 * <li><b>Vinculación Bidireccional de Luz:</b> Mantiene una referencia directa
 * a su {@link FuenteLuz} asignada.</li>
 * <li><b>Destrucción O(1) Inmediata:</b> Al invocarse {@link #eliminar()}, la
 * luz asociada se apaga y la entidad se desvincula al instante de todas sus
 * celdas espaciales activas sin dejar referencias huérfanas en memoria.</li>
 * </ul>
 * </p>
 * 
 * @version 3.2
 */
public abstract class Ente {

	/** Bandera que indica si el ente debe ser eliminado y purgado del mundo. */
	protected boolean eliminado;

	/** Referencia al mundo al cual pertenece la entidad. */
	protected Mundo mundo;

	/**
	 * Referencia a la fuente de luz dinámica anclada a este ente (linterna, fuego,
	 * aura).
	 */
	protected FuenteLuz luzAsignada;

	/** Código del último frame lógico en el que este ente fue actualizado. */
	protected int codUltimaActualizacion = Integer.MIN_VALUE;

	/** Código del último frame gráfico en el que este ente fue renderizado. */
	protected int codUltimoPintado = Integer.MIN_VALUE;

	/** Dirty flag para saber si la entidad cambió de posición en este frame */
	protected boolean posicionModificada = true;

	/**
	 * Celdas espaciales que esta entidad solapa actualmente (máximo 4
	 * habitualmente).
	 */
	protected final ArrayList<ZoneBox> zonasOcupadas = new ArrayList<>(4);

	/**
	 * Rectángulo reutilizable pre-asignado. Evita crear miles de objetos
	 * 'Rectangle' en el Heap durante las consultas de colisión en cada frame.
	 */
	protected final Rectangle AREA_ENTE_RETORNO = new Rectangle();

	public abstract void actualizar();

	public abstract void pintar(final Graphics2D g);

	/*
	 * =========================================================================
	 * EXPLICACIÓN DIDÁCTICA: DESTRUCTOR AUTOMÁTICO DE LUZ Y CELDAS
	 * -------------------------------------------------------------------------
	 * Cuando un enemigo muere, un proyectil impacta o un cofre es destruido,
	 * 'eliminar()' cambia la bandera 'eliminado = true', llama a 'desvincularLuz()'
	 * y a 'desvincularDeZonas()'.
	 *
	 * Esto apaga la luz en tiempo O(1) devolviendo la ranura al pool maestro de
	 * GestorLuz y purga inmediatamente al ente de todas las celdas espaciales
	 * (ZoneBox), eliminando completamente cualquier retraso o fuga de memoria.
	 * =========================================================================
	 */
	/**
	 * Marca la entidad como eliminada para su purga del mundo, apaga inmediatamente
	 * cualquier fuente de luz asignada y la desvincula de todas sus celdas
	 * espaciales en tiempo $O(1)$.
	 */
	public void eliminar() {
		this.eliminado = true;
		this.desvincularLuz();
		this.desvincularDeZonas();
	}

	public abstract int getPosicionXInt();

	public abstract int getPosicionYInt();

	public abstract double getPosicionX();

	public abstract double getPosicionY();

	public abstract void modificarPosicionX(final double desplazamientoX);

	public abstract void modificarPosicionY(final double desplazamientoY);

	public abstract void setPosicion(final double x, final double y);

	public boolean estaEliminado() {
		return this.eliminado;
	}

	public abstract int getAncho();

	public abstract int getAlto();

	// === DIRTY FLAG DE MOVIMIENTO ===

	public boolean haCambiadoPosicion() {
		return this.posicionModificada;
	}

	public void limpiarFlagMovimiento() {
		this.posicionModificada = false;
	}

	public void marcarPosicionModificada() {
		this.posicionModificada = true;
	}

	// =========================================================================
	// === GESTIÓN DE PARTICIONADO ESPACIAL (ZERO-GC / O(1))
	// =========================================================================

	/**
	 * Retorna la lista de celdas espaciales que esta entidad solapa en este
	 * momento.
	 *
	 * @return Lista compacta de {@link ZoneBox}.
	 */
	public ArrayList<ZoneBox> getZonasOcupadas() {
		return this.zonasOcupadas;
	}

	/**
	 * Desvincula la entidad de todas las celdas espaciales que ocupaba en tiempo
	 * $O(1)$.
	 */
	public void desvincularDeZonas() {
		for (int i = 0; i < this.zonasOcupadas.size(); i++) {
			this.zonasOcupadas.get(i).eliminarEntidad(this);
		}
		this.zonasOcupadas.clear();
	}

	/**
	 * Reevalúa y actualiza las celdas espaciales que ocupa la entidad
	 */
	public void verificarZoneBox() {
		if ((this.mundo == null) && (!this.haCambiadoPosicion())) {
			return;
		}
		this.limpiarFlagMovimiento();

		// 1. Desvincular de celdas que ya no toca (en reversa para remoción segura)
		for (int i = this.zonasOcupadas.size() - 1; i >= 0; i--) {
			final ZoneBox zb = this.zonasOcupadas.get(i);
			if (!zb.getArea().intersects(this.getArea())) {
				zb.eliminarEntidad(this);
				this.zonasOcupadas.remove(i);
			}
		}

		// 2. Registrar en las nuevas celdas intersectadas
		final ArrayList<ZoneBox> nuevasZonas = this.mundo.getZonasIntersectadas(this);
		for (int i = 0; i < nuevasZonas.size(); i++) {
			final ZoneBox zb = nuevasZonas.get(i);
			if (!this.zonasOcupadas.contains(zb)) {
				this.zonasOcupadas.add(zb);
				zb.addEntidad(this);
			}
		}

		// 3. Si quedó completamente fuera de los límites del mapa, se auto-elimina
		if (this.zonasOcupadas.isEmpty()) {
			this.eliminar();
		}

	}

	// =========================================================================
	// === DEDUPLICACIÓN DE FRAME DIRECTA (ZERO-GC / O(1))
	// =========================================================================

	/**
	 * Comprueba si la entidad ya fue actualizada en el tick lógico actual.
	 *
	 * @param codFrameAct Código de frame lógico del ciclo actual del mundo.
	 * @return {@code true} si ya fue actualizada en este frame; {@code false} en
	 *         caso contrario.
	 */
	public boolean estaActualizado(final int codFrameAct) {
		return this.codUltimaActualizacion == codFrameAct;
	}

	/**
	 * Sella la entidad con el código de frame lógico actual para evitar ejecuciones
	 * duplicadas.
	 *
	 * @param codFrameAct Código de frame lógico del ciclo actual del mundo.
	 */
	public void marcarActualizado(final int codFrameAct) {
		this.codUltimaActualizacion = codFrameAct;
	}

	/**
	 * Comprueba si la entidad ya fue dibujada en el frame de renderizado actual.
	 *
	 * @param codFramePaint Código de frame de renderizado del ciclo actual del
	 *                      mundo.
	 * @return {@code true} si ya fue dibujada en este frame; {@code false} en caso
	 *         contrario.
	 */
	public boolean estaPintado(final int codFramePaint) {
		return this.codUltimoPintado == codFramePaint;
	}

	/**
	 * Sella la entidad con el código de frame de renderizado actual para evitar
	 * duplicados.
	 *
	 * @param codFramePaint Código de frame de renderizado del ciclo actual del
	 *                      mundo.
	 */
	public void marcarPintado(final int codFramePaint) {
		this.codUltimoPintado = codFramePaint;
	}

	// =========================================================================
	// === GESTIÓN BIDIRECCIONAL DE FUENTE DE LUZ
	// =========================================================================

	/**
	 * Asigna y vincula una fuente de luz a esta entidad. Si ya poseía una luz
	 * previa, la anterior se apaga de forma segura.
	 *
	 * @param luz Fuente de luz activa asignada.
	 */
	public void asignarLuz(final FuenteLuz luz) {
		if (this.luzAsignada == luz) {
			return;
		}

		// Desacoplamos primero la referencia local para romper el ciclo recursivo
		final FuenteLuz luzPrevia = this.luzAsignada;
		this.luzAsignada = luz;

		if (luzPrevia != null) {
			luzPrevia.apagar();
		}
	}

	/**
	 * Apaga y desvincula la luz activa de esta entidad en tiempo O(1).
	 */
	public void desvincularLuz() {
		if (this.luzAsignada != null) {
			final FuenteLuz luzPrevia = this.luzAsignada;
			this.luzAsignada = null; // Rompe la referencia antes de apagar
			luzPrevia.apagar();
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

	public void setMundo(final Mundo mundo) {
		this.mundo = mundo;
	}

	public Mundo getMundo() {
		return this.mundo;
	}
}