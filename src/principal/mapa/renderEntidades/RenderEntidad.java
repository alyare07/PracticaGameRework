package principal.mapa.renderEntidades;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import principal.entes.Ente;
import principal.mapa.Mundo;

/**
 * Gestor individual de renderizado y particionado espacial para una única
 * entidad.
 * <p>
 * Sirve como puente entre la entidad ({@link Ente}) y las regiones del mapa
 * ({@link ZoneBox}) que la entidad ocupa actualmente. Además, sincroniza los
 * ticks de lógica y dibujo mediante identificadores para prevenir ciclos
 * redundantes en un mismo fotograma.
 * </p>
 */
public class RenderEntidad {

	/** Referencia al mundo/escenario principal. */
	protected Mundo mundo;

	/**
	 * Conjunto de zonas espaciales ({@link ZoneBox}) que la entidad solapa.
	 * <p>
	 * <b>Optimización de Rendimiento:</b> Utiliza {@link LinkedHashSet} para
	 * permitir consultas y remociones en tiempo $O(1)$ sin perder el orden de
	 * inserción, optimizando el consumo de CPU en movimientos rápidos.
	 * </p>
	 */
	protected Set<ZoneBox> lista;

	/** Instancia de la entidad administrada por este envoltorio. */
	protected final Ente ENTIDAD;

	/**
	 * Identificador de frame correspondiente a la última actualización lógica
	 * (update).
	 */
	protected int codAct;

	/** Identificador de frame correspondiente al último ciclo de dibujo (paint). */
	protected int codPaint;

	/**
	 * Asocia una entidad con el sistema de renderizado del mundo.
	 *
	 * @param e     Instancia de {@link Ente} a envolver.
	 * @param mundo Instancia del {@link Mundo} donde habita la entidad.
	 */
	public RenderEntidad(final Ente e, final Mundo mundo) {
		this.ENTIDAD = e;
		this.mundo = mundo;
		this.lista = new LinkedHashSet<>();
	}

	/**
	 * Marca esta entidad como procesada en el tick de actualización lógica del
	 * frame actual.
	 */
	public void renderizado() {
		this.codAct = this.mundo.getCodAct();
	}

	/**
	 * Evalúa si la entidad ya fue actualizada lógicamente dentro del frame en
	 * curso.
	 *
	 * @return {@code true} si ya fue procesada en este tick; {@code false} en caso
	 *         contrario.
	 */
	public boolean estaRenderizado() {
		return this.codAct == this.mundo.getCodAct();
	}

	/**
	 * Evalúa si la entidad ya fue dibujada en la pantalla dentro del frame en
	 * curso.
	 *
	 * @return {@code true} si ya fue pintada en este tick; {@code false} en caso
	 *         contrario.
	 */
	public boolean estaPintado() {
		return this.codPaint == this.mundo.getCodPintado();
	}

	/**
	 * Marca esta entidad como dibujada en el tick de renderizado visual del frame
	 * actual.
	 */
	public void pintado() {
		this.codPaint = this.mundo.getCodPintado();
	}

	/**
	 * Actualiza y sincroniza las cuadrículas espaciales ({@link ZoneBox}) que
	 * intersecta la entidad.
	 * <p>
	 * 1. Elimina la entidad de aquellas zonas que dejó de tocar tras su
	 * movimiento.<br>
	 * 2. Registra la entidad en las nuevas zonas que acaba de solapar.<br>
	 * 3. Elimina la entidad del mundo si dejó de pertenecer a cualquier zona activa
	 * (fuera del mapa).
	 * </p>
	 */
	private void verificarZoneBox() {
		// Fase 1: Remoción de zonas obsoletas mediante un Iterator seguro
		final Iterator<ZoneBox> iter = this.lista.iterator();
		while (iter.hasNext()) {
			final ZoneBox zb = iter.next();
			if (!zb.getArea().intersects(this.ENTIDAD.getArea())) {
				zb.eliminarEntidad(this.ENTIDAD);
				iter.remove(); // Desvinculación segura en tiempo O(1)
			}
		}

		// Fase 2: Inserción en nuevas zonas intersectadas
		for (final ZoneBox zb : this.mundo.getZonasIntersectadas(this.ENTIDAD)) {
			if (!this.contieneZona(zb)) {
				this.lista.add(zb);
				zb.addEntidad(this.ENTIDAD);
			}
		}

		// Fase 3: Autolimpieza si la entidad quedó fuera de la grilla del mapa
		if (this.lista.isEmpty()) {
			this.ENTIDAD.eliminar();
			this.eliminarEntidad();
		}
	}

	/**
	 * Ejecuta el ciclo de actualización de la entidad en el espacio. Si la entidad
	 * se marca como eliminada, se desvincula de las regiones.
	 */
	public void update() {
		if (this.ENTIDAD.estaEliminado()) {
			this.eliminarEntidad();
			return;
		}
		this.verificarZoneBox();
	}

	/**
	 * Remueve completamente la entidad del mapa de renderizado y desvincula sus
	 * referencias espaciales.
	 */
	private void eliminarEntidad() {
		this.limpiarZonas();
		this.mundo.getRenders().eliminarEntidad(this.ENTIDAD);
	}

	/**
	 * Registra manualmente una nueva zona de espacio para la entidad.
	 *
	 * @param zona Cuadrícula {@link ZoneBox} a vincular.
	 */
	public void meterZoneBox(final ZoneBox zona) {
		this.lista.add(zona);
	}

	/**
	 * Consulta rápida para determinar si la entidad ya está registrada en una
	 * cuadrícula específica.
	 *
	 * @param zona La zona espacial a verificar.
	 * @return {@code true} si la zona está asociada a la entidad; {@code false} de
	 *         lo contrario.
	 */
	public boolean contieneZona(final ZoneBox zona) {
		return this.lista.contains(zona);
	}

	/**
	 * Limpia la relación bidireccional entre la entidad y todas las cuadrículas
	 * {@link ZoneBox} que ocupaba.
	 */
	protected void limpiarZonas() {
		for (final ZoneBox z : this.lista) {
			z.eliminarEntidad(this.ENTIDAD);
		}
		this.lista.clear();
	}

	/**
	 * Obtiene la entidad envuelta por esta clase.
	 *
	 * @return La instancia de {@link Ente}.
	 */
	public Ente getEntidad() {
		return this.ENTIDAD;
	}
}