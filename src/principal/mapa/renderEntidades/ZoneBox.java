package principal.mapa.renderEntidades;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.objetos.Complemento;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.Item;
import principal.mapa.Mundo;
import principal.mapa.escenario.tps.ZonaTP;
import principal.utilidades.AccionEntidad;
import principal.utilidades.Globales;

/**
 * Celda espacial de indexación de entidades (64x64 px). Realiza barrido
 * continuo, purga activa y despacho seguro de visitors en reversa para prevenir
 * ConcurrentModification e IndexOutOfBounds al eliminar entidades en caliente
 * (Zero-GC / O(1)).
 * 
 * @version 2.2 (Vanilla Java 8 - Mutation-Safe Visitor Suite)
 */
public class ZoneBox extends Ente {

	protected final Rectangle AREA;
	protected final Mundo mundo;

	protected final ArrayList<Criatura> CRIATURAS = new ArrayList<>(4);
	protected final ArrayList<Item> ITEMS = new ArrayList<>(4);
	protected final ArrayList<Objeto> OBJETOS = new ArrayList<>(4);
	protected final ArrayList<Complemento> COMPLEMENTOS = new ArrayList<>(8);
	protected final ArrayList<ZonaTP> ZONAS_TP = new ArrayList<>(2);

	public ZoneBox(final int x, final int y, final int ancho, final int alto, final Mundo mundo) {
		this.AREA = new Rectangle(x, y, ancho, alto);
		this.mundo = mundo;
	}

	@Override
	public void actualizar() {
		if (Globales.isEstadoEditor()) {
			return;
		}

		final int codAct = this.mundo.getCodAct();

		// 1. Actualización y Purgado de Objetos
		for (int i = this.OBJETOS.size() - 1; i >= 0; i--) {
			final Objeto o = this.OBJETOS.get(i);
			if (o.estaEliminado()) {
				this.OBJETOS.remove(i);
				this.mundo.eliminarEntidadRegistro(o);
				continue;
			}
			if (!o.estaActualizado(codAct)) {
				o.actualizar();
				o.marcarActualizado(codAct);
			}
		}

		// 2. Actualización y Purgado de Criaturas
		for (int i = this.CRIATURAS.size() - 1; i >= 0; i--) {
			final Criatura c = this.CRIATURAS.get(i);
			if (c.estaEliminado()) {
				this.CRIATURAS.remove(i);
				this.mundo.eliminarEntidadRegistro(c);
				continue;
			}
			if (!c.estaActualizado(codAct)) {
				c.actualizar();
				c.marcarActualizado(codAct);
			}
		}

		// 3. Actualización y Purgado de Items
		for (int i = this.ITEMS.size() - 1; i >= 0; i--) {
			final Item item = this.ITEMS.get(i);
			if (item.estaEliminado()) {
				this.ITEMS.remove(i);
				this.mundo.eliminarEntidadRegistro(item);
				continue;
			}
			if (!item.estaActualizado(codAct)) {
				item.actualizar();
				item.marcarActualizado(codAct);
			}
		}

		// 4. Actualización y Purgado de Zonas TP
		for (int i = this.ZONAS_TP.size() - 1; i >= 0; i--) {
			final ZonaTP tp = this.ZONAS_TP.get(i);
			if (tp.estaEliminado()) {
				this.ZONAS_TP.remove(i);
				this.mundo.eliminarEntidadRegistro(tp);
				continue;
			}
			if (!tp.estaActualizado(codAct)) {
				tp.actualizar();
				tp.marcarActualizado(codAct);
			}
		}
	}

	public void recolectarEntidadesParaRender(final Mundo mundo) {
		final int codPaint = mundo.getCodPintado();

		// Complementos
		for (int i = this.COMPLEMENTOS.size() - 1; i >= 0; i--) {
			final Complemento c = this.COMPLEMENTOS.get(i);
			if (c.estaEliminado()) {
				this.COMPLEMENTOS.remove(i);
				mundo.eliminarEntidadRegistro(c);
				continue;
			}
			if (!c.estaPintado(codPaint)) {
				mundo.agregarAColaRender(c);
				c.marcarPintado(codPaint);
			}
		}

		// Objetos
		for (int i = this.OBJETOS.size() - 1; i >= 0; i--) {
			final Objeto o = this.OBJETOS.get(i);
			if (o.estaEliminado()) {
				this.OBJETOS.remove(i);
				mundo.eliminarEntidadRegistro(o);
				continue;
			}
			if (!o.estaPintado(codPaint)) {
				mundo.agregarAColaRender(o);
				o.marcarPintado(codPaint);
			}
		}

		// Criaturas
		for (int i = this.CRIATURAS.size() - 1; i >= 0; i--) {
			final Criatura c = this.CRIATURAS.get(i);
			if (c.estaEliminado()) {
				this.CRIATURAS.remove(i);
				mundo.eliminarEntidadRegistro(c);
				continue;
			}
			if (!c.estaPintado(codPaint)) {
				mundo.agregarAColaRender(c);
				c.marcarPintado(codPaint);
			}
		}

		// Zonas TP
		for (int i = this.ZONAS_TP.size() - 1; i >= 0; i--) {
			final ZonaTP tp = this.ZONAS_TP.get(i);
			if (tp.estaEliminado()) {
				this.ZONAS_TP.remove(i);
				mundo.eliminarEntidadRegistro(tp);
				continue;
			}
			if (!tp.estaPintado(codPaint)) {
				mundo.agregarAColaRender(tp);
				tp.marcarPintado(codPaint);
			}
		}
	}

	@Override
	public void pintar(final Graphics2D g) {
		final int codPaint = this.mundo.getCodPintado();
		for (int i = this.ITEMS.size() - 1; i >= 0; i--) {
			final Item item = this.ITEMS.get(i);
			if (!item.estaEliminado() && !item.estaPintado(codPaint)) {
				item.pintar(g);
				item.marcarPintado(codPaint);
			}
		}
	}

	public void addEntidad(final Ente e) {
		if ((e == null) || e.estaEliminado()) {
			return;
		}
		if ((e instanceof Criatura) && !(e instanceof principal.entes.criaturas.Jugador)) {
			if (!this.CRIATURAS.contains(e)) {
				this.CRIATURAS.add((Criatura) e);
			}
		} else if (e instanceof Item) {
			if (!this.ITEMS.contains(e)) {
				this.ITEMS.add((Item) e);
			}
		} else if (e instanceof Complemento) {
			if (!this.COMPLEMENTOS.contains(e)) {
				this.COMPLEMENTOS.add((Complemento) e);
			}
		} else if (e instanceof Objeto) {
			if (!this.OBJETOS.contains(e)) {
				this.OBJETOS.add((Objeto) e);
			}
		} else if (e instanceof ZonaTP) {
			if (!this.ZONAS_TP.contains(e)) {
				this.ZONAS_TP.add((ZonaTP) e);
			}
		}
	}

	public void eliminarEntidad(final Ente e) {
		if (e instanceof Criatura) {
			this.CRIATURAS.remove(e);
		} else if (e instanceof Item) {
			this.ITEMS.remove(e);
		} else if (e instanceof Complemento) {
			this.COMPLEMENTOS.remove(e);
		} else if (e instanceof Objeto) {
			this.OBJETOS.remove(e);
		} else if (e instanceof ZonaTP) {
			this.ZONAS_TP.remove(e);
		}
	}

	// =========================================================================
	// === EVALUACIÓN DE RAYCASTING / LÍNEA DE TIRO (ZERO-GC / O(1))
	// =========================================================================

	public boolean intersectaLineaSolida(final double x0, final double y0, final double x1, final double y1) {
		for (int i = this.OBJETOS.size() - 1; i >= 0; i--) {
			final Objeto o = this.OBJETOS.get(i);
			if (o.esSolido() && !o.estaEliminado() && o.getArea().intersectsLine(x0, y0, x1, y1)) {
				return true;
			}
		}

		for (int i = this.COMPLEMENTOS.size() - 1; i >= 0; i--) {
			final Complemento c = this.COMPLEMENTOS.get(i);
			if (c.esSolido() && !c.estaEliminado() && c.getArea().intersectsLine(x0, y0, x1, y1)) {
				return true;
			}
		}

		return false;
	}

	// =========================================================================
	// === MÉTODOS VISITOR SEGUROS EN REVERSA (ZERO-GC / MUTATION-SAFE)
	// =========================================================================

	public void paraCadaCriatura(final Shape area, final AccionEntidad<Criatura> accion) {
		if (!this.intersectaZona(area)) {
			return;
		}
		for (int i = this.CRIATURAS.size() - 1; i >= 0; i--) {
			if (i < this.CRIATURAS.size()) {
				final Criatura c = this.CRIATURAS.get(i);
				if (!c.estaEliminado() && area.intersects(c.getArea())) {
					accion.ejecutar(c);
				}
			}
		}
	}

	public void paraCadaCriatura(final int x, final int y, final int w, final int h,
			final AccionEntidad<Criatura> accion) {
		if (!this.intersectaZona(x, y, w, h)) {
			return;
		}
		for (int i = this.CRIATURAS.size() - 1; i >= 0; i--) {
			if (i < this.CRIATURAS.size()) {
				final Criatura c = this.CRIATURAS.get(i);
				if (!c.estaEliminado() && c.getArea().intersects(x, y, w, h)) {
					accion.ejecutar(c);
				}
			}
		}
	}

	public void paraCadaItem(final Shape area, final AccionEntidad<Item> accion) {
		if (!this.intersectaZona(area)) {
			return;
		}
		for (int i = this.ITEMS.size() - 1; i >= 0; i--) {
			if (i < this.ITEMS.size()) {
				final Item item = this.ITEMS.get(i);
				if (!item.estaEliminado() && area.intersects(item.getArea())) {
					accion.ejecutar(item);
				}
			}
		}
	}

	public void paraCadaObjeto(final Shape area, final AccionEntidad<Objeto> accion) {
		if (!this.intersectaZona(area)) {
			return;
		}
		for (int i = this.OBJETOS.size() - 1; i >= 0; i--) {
			if (i < this.OBJETOS.size()) {
				final Objeto o = this.OBJETOS.get(i);
				if (!o.estaEliminado() && area.intersects(o.getArea())) {
					accion.ejecutar(o);
				}
			}
		}
	}

	public void paraCadaComplemento(final Shape area, final AccionEntidad<Complemento> accion) {
		if (!this.intersectaZona(area)) {
			return;
		}
		for (int i = this.COMPLEMENTOS.size() - 1; i >= 0; i--) {
			if (i < this.COMPLEMENTOS.size()) {
				final Complemento c = this.COMPLEMENTOS.get(i);
				if (!c.estaEliminado() && area.intersects(c.getArea())) {
					accion.ejecutar(c);
				}
			}
		}
	}

	public void paraCadaEnte(final Shape area, final AccionEntidad<Ente> accion, final boolean tenerEncuentaZonaTP) {
		if (!this.intersectaZona(area)) {
			return;
		}
		for (int i = this.CRIATURAS.size() - 1; i >= 0; i--) {
			if (i < this.CRIATURAS.size()) {
				final Criatura c = this.CRIATURAS.get(i);
				if (!c.estaEliminado() && area.intersects(c.getArea())) {
					accion.ejecutar(c);
				}
			}
		}
		for (int i = this.ITEMS.size() - 1; i >= 0; i--) {
			if (i < this.ITEMS.size()) {
				final Item item = this.ITEMS.get(i);
				if (!item.estaEliminado() && area.intersects(item.getArea())) {
					accion.ejecutar(item);
				}
			}
		}
		for (int i = this.OBJETOS.size() - 1; i >= 0; i--) {
			if (i < this.OBJETOS.size()) {
				final Objeto o = this.OBJETOS.get(i);
				if (!o.estaEliminado() && area.intersects(o.getArea())) {
					accion.ejecutar(o);
				}
			}
		}
		for (int i = this.COMPLEMENTOS.size() - 1; i >= 0; i--) {
			if (i < this.COMPLEMENTOS.size()) {
				final Complemento c = this.COMPLEMENTOS.get(i);
				if (!c.estaEliminado() && c.intersecta(area)) {
					accion.ejecutar(c);
				}
			}
		}

		if (tenerEncuentaZonaTP) {
			for (int i = this.ZONAS_TP.size() - 1; i >= 0; i--) {
				if (i < this.ZONAS_TP.size()) {
					final ZonaTP z = this.ZONAS_TP.get(i);
					if (!z.estaEliminado() && area.intersects(z.getArea())) {
						accion.ejecutar(z);
					}
				}
			}
		}
	}

	// =========================================================================
	// === EVALUACIÓN DE COLISIÓN DIRECTA O(1)
	// =========================================================================

	public boolean intersectaObjetoSolido(final Shape area) {
		if (!this.intersectaZona(area)) {
			return false;
		}
		for (int i = this.COMPLEMENTOS.size() - 1; i >= 0; i--) {
			final Complemento c = this.COMPLEMENTOS.get(i);
			if (!c.estaEliminado() && c.esSolido() && c.intersecta(area)) {
				return true;
			}
		}
		for (int i = this.OBJETOS.size() - 1; i >= 0; i--) {
			final Objeto o = this.OBJETOS.get(i);
			if (!o.estaEliminado() && o.esSolido() && area.intersects(o.getArea())) {
				return true;
			}
		}
		return false;
	}

	public boolean intersectaObjetoSolidoPermanente(final Shape area) {
		return this.intersectaObjetoSolido(area);
	}

	public boolean intersectaAreaNoSolidaDeAlgunComplemento(final Shape area) {
		if (!this.intersectaZona(area)) {
			return false;
		}
		for (int i = this.COMPLEMENTOS.size() - 1; i >= 0; i--) {
			final Complemento c = this.COMPLEMENTOS.get(i);
			if (!c.estaEliminado() && c.esSolido() && c.intersectaAreaNoSolida(area)) {
				return true;
			}
		}
		return false;
	}

	public boolean intersectaAlgunaCriatura(final Shape area) {
		if (!this.intersectaZona(area)) {
			return false;
		}
		for (int i = this.CRIATURAS.size() - 1; i >= 0; i--) {
			final Criatura c = this.CRIATURAS.get(i);
			if (!c.estaEliminado() && area.intersects(c.getArea())) {
				return true;
			}
		}
		return false;
	}

	public boolean intersectaAlgunItem(final Shape area) {
		if (!this.intersectaZona(area)) {
			return false;
		}
		for (int i = this.ITEMS.size() - 1; i >= 0; i--) {
			final Item item = this.ITEMS.get(i);
			if (!item.estaEliminado() && area.intersects(item.getArea())) {
				return true;
			}
		}
		return false;
	}

	public boolean intersectaAlgunComplemento(final Shape area) {
		if (!this.intersectaZona(area)) {
			return false;
		}
		for (int i = this.COMPLEMENTOS.size() - 1; i >= 0; i--) {
			final Complemento c = this.COMPLEMENTOS.get(i);
			if (!c.estaEliminado() && area.intersects(c.getArea())) {
				return true;
			}
		}
		return false;
	}

	public boolean intersectaZona(final int x, final int y, final int w, final int h) {
		return this.AREA.intersects(x, y, w, h);
	}

	public boolean intersectaZona(final Shape area) {
		return (area != null) && area.intersects(this.AREA);
	}

	public ArrayList<Criatura> getCriaturas() {
		return this.CRIATURAS;
	}

	public ArrayList<Item> getItems() {
		return this.ITEMS;
	}

	public ArrayList<Objeto> getObjetos() {
		return this.OBJETOS;
	}

	public ArrayList<Complemento> getComplementos() {
		return this.COMPLEMENTOS;
	}

	public ArrayList<ZonaTP> getZonasTP() {
		return this.ZONAS_TP;
	}

	@Override
	public void eliminar() {
	}

	@Override
	public int getPosicionXInt() {
		return this.AREA.x;
	}

	@Override
	public int getPosicionYInt() {
		return this.AREA.y;
	}

	@Override
	public double getPosicionX() {
		return this.AREA.x;
	}

	@Override
	public double getPosicionY() {
		return this.AREA.y;
	}

	@Override
	public void modificarPosicionX(final double desplazamientoX) {
	}

	@Override
	public void modificarPosicionY(final double desplazamientoY) {
	}

	@Override
	public boolean estaEliminado() {
		return false;
	}

	@Override
	public int getAncho() {
		return this.AREA.width;
	}

	@Override
	public int getAlto() {
		return this.AREA.height;
	}

	@Override
	public void setPosicion(final double x, final double y) {
	}
}