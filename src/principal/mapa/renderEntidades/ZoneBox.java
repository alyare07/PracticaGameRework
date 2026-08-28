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
import principal.utilidades.Globales;

/**
 * Representa una cuadrícula o celda del espacio (partición espacial) en la
 * grilla del mapa.
 * <p>
 * Administra el ciclo de vida, actualización y renderizado local de todas las
 * entidades que intersectan con su área delimitada. Reduce drásticamente la
 * complejidad de detección de colisiones de $O(N^2)$ a $O(1)$ por vecindario.
 * </p>
 * <p>
 * <b>Optimizaciones Arquitectónicas (v3.1):</b>
 * <ul>
 * <li><b>Deduplicación Directa O(1):</b> Elimina wrappers y HashMaps
 * intermedios (`RenderEntidad`), consultando directamente los códigos de frame
 * de cada {@link Ente}.</li>
 * <li><b>Listas Compactas Zero-GC:</b> Reemplaza conjuntos basados en nodos
 * (`LinkedHashSet`) por {@link ArrayList} de capacidad ajustada, optimizando la
 * localidad de caché y la velocidad de iteración indexada.</li>
 * </ul>
 * </p>
 * 
 * @version 3.1
 */
public class ZoneBox extends Ente {

	/** Área rectangular que delimita los límites de esta celda espacial. */
	protected final Rectangle AREA;

	/** Referencia al escenario o mundo contenedor. */
	protected final Mundo mundo;

	/** Listas compactas de entidades contenidas en esta celda espacial. */
	protected final ArrayList<Criatura> CRIATURAS = new ArrayList<>(4);
	protected final ArrayList<Item> ITEMS = new ArrayList<>(4);
	protected final ArrayList<Objeto> OBJETOS = new ArrayList<>(4);
	protected final ArrayList<Complemento> COMPLEMENTOS = new ArrayList<>(8);
	protected final ArrayList<ZonaTP> ZONAS_TP = new ArrayList<>(2);

	/**
	 * Construye una celda de particionado espacial en las coordenadas y dimensiones
	 * especificadas.
	 *
	 * @param x     Coordenada X del borde superior izquierdo.
	 * @param y     Coordenada Y del borde superior izquierdo.
	 * @param ancho Anchura de la celda.
	 * @param alto  Altura de la celda.
	 * @param mundo Instancia del {@link Mundo} asociado.
	 */
	public ZoneBox(final int x, final int y, final int ancho, final int alto, final Mundo mundo) {
		this.AREA = new Rectangle(x, y, ancho, alto);
		this.mundo = mundo;
	}

	/**
	 * Ejecuta el ciclo de actualización lógica de todas las entidades presentes en
	 * la celda mediante validación directa de código de frame lógico.
	 */
	@Override
	public void actualizar() {
		if (Globales.isEstadoEditor()) {
			return;
		}

		final int codAct = this.mundo.getCodAct();

		// --- ACTUALIZACIÓN DE OBJETOS / CONTENEDORES ---
		for (int i = 0; i < this.OBJETOS.size(); i++) {
			final Objeto o = this.OBJETOS.get(i);
			if (!o.estaActualizado(codAct)) {
				o.actualizar();
				o.marcarActualizado(codAct);
			}
		}

		// --- ACTUALIZACIÓN DE CRIATURAS ---
		for (int i = 0; i < this.CRIATURAS.size(); i++) {
			final Criatura c = this.CRIATURAS.get(i);
			if (!c.estaActualizado(codAct)) {
				c.actualizar();
				c.marcarActualizado(codAct);
			}
		}

		// --- ACTUALIZACIÓN DE ITEMS ---
		for (int i = 0; i < this.ITEMS.size(); i++) {
			final Item item = this.ITEMS.get(i);
			if (!item.estaActualizado(codAct)) {
				item.actualizar();
				item.marcarActualizado(codAct);
			}
		}

		// --- ACTUALIZACIÓN DE ZONAS TP ---
		for (int i = 0; i < this.ZONAS_TP.size(); i++) {
			final ZonaTP tp = this.ZONAS_TP.get(i);
			if (!tp.estaActualizado(codAct)) {
				tp.actualizar();
				tp.marcarActualizado(codAct);
			}
		}
	}

	/**
	 * Recolecta todas las entidades visibles de esta celda en la cola de
	 * renderizado unificada de {@link Mundo}, deduplicando aquellas que ocupen
	 * múltiples celdas mediante el código de frame de render.
	 *
	 * @param mundo Referencia al mundo contenedor.
	 */
	public void recolectarEntidadesParaRender(final Mundo mundo) {
		final int codPaint = mundo.getCodPintado();

		// 1. Complementos (Árboles, casas, muros)
		for (int i = 0; i < this.COMPLEMENTOS.size(); i++) {
			final Complemento c = this.COMPLEMENTOS.get(i);
			if (!c.estaPintado(codPaint)) {
				mundo.agregarAColaRender(c);
				c.marcarPintado(codPaint);
			}
		}

		// 2. Objetos (Cofres, barriles)
		for (int i = 0; i < this.OBJETOS.size(); i++) {
			final Objeto o = this.OBJETOS.get(i);
			if (!o.estaPintado(codPaint)) {
				mundo.agregarAColaRender(o);
				o.marcarPintado(codPaint);
			}
		}

		// 3. Criaturas (Enemigos, NPCs)
		for (int i = 0; i < this.CRIATURAS.size(); i++) {
			final Criatura c = this.CRIATURAS.get(i);
			if (!c.estaPintado(codPaint)) {
				mundo.agregarAColaRender(c);
				c.marcarPintado(codPaint);
			}
		}

		// 4. Zonas TP
		for (int i = 0; i < this.ZONAS_TP.size(); i++) {
			final ZonaTP tp = this.ZONAS_TP.get(i);
			if (!tp.estaPintado(codPaint)) {
				mundo.agregarAColaRender(tp);
				tp.marcarPintado(codPaint);
			}
		}
	}

	@Override
	public void pintar(final Graphics2D g) {
		// Los ítems planos del suelo se pintan en su propia pasada de base
		final int codPaint = this.mundo.getCodPintado();
		for (int i = 0; i < this.ITEMS.size(); i++) {
			final Item item = this.ITEMS.get(i);
			if (!item.estaPintado(codPaint)) {
				item.pintar(g);
				item.marcarPintado(codPaint);
			}
		}
	}

	/**
	 * Clasifica y agrega un {@link Ente} a su lista correspondiente dentro de la
	 * celda evitando duplicados.
	 *
	 * @param e Entidad a incorporar.
	 */
	public void addEntidad(final Ente e) {
		if (e instanceof Criatura) {
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

	/**
	 * Desvincula y remueve una entidad de la lista correspondiente en la celda.
	 *
	 * @param e Entidad a remover.
	 */
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
	// === MÉTODOS DE BÚSQUEDA Y COLISIÓN ESPACIAL
	// =========================================================================

	/**
	 * Recolecta todas las entidades (criaturas, ítems y cofres) que colisionan con
	 * un área dada dentro de la celda.
	 *
	 * @param area Área geométrica a consultar.
	 * @return Lista con los entes intersectados.
	 */
	public ArrayList<Ente> getEntesIntersectados(final Shape area) {
		final ArrayList<Ente> lista = new ArrayList<>();
		if (!this.intersectaZona(area)) {
			return lista;
		}

		for (int i = 0; i < this.CRIATURAS.size(); i++) {
			final Criatura c = this.CRIATURAS.get(i);
			if (area.intersects(c.getArea())) {
				lista.add(c);
			}
		}

		for (int i = 0; i < this.ITEMS.size(); i++) {
			final Item item = this.ITEMS.get(i);
			if (area.intersects(item.getArea())) {
				lista.add(item);
			}
		}

		for (int i = 0; i < this.OBJETOS.size(); i++) {
			final Objeto o = this.OBJETOS.get(i);
			if (area.intersects(o.getArea())) {
				lista.add(o);
			}
		}
		return lista;
	}

	public ArrayList<Item> getItemsIntersectados(final Shape area) {
		final ArrayList<Item> lista = new ArrayList<>();
		if (!this.intersectaZona(area)) {
			return lista;
		}
		for (int i = 0; i < this.ITEMS.size(); i++) {
			final Item item = this.ITEMS.get(i);
			if (area.intersects(item.getArea())) {
				lista.add(item);
			}
		}
		return lista;
	}

	public ArrayList<Criatura> getCriaturasIntersectadas(final Shape area) {
		final ArrayList<Criatura> lista = new ArrayList<>();
		if (!this.intersectaZona(area)) {
			return lista;
		}
		for (int i = 0; i < this.CRIATURAS.size(); i++) {
			final Criatura c = this.CRIATURAS.get(i);
			if (area.intersects(c.getArea())) {
				lista.add(c);
			}
		}
		return lista;
	}

	public ArrayList<Complemento> getComplementosIntersectados(final Shape area) {
		final ArrayList<Complemento> lista = new ArrayList<>();
		if (!this.intersectaZona(area)) {
			return lista;
		}
		for (int i = 0; i < this.COMPLEMENTOS.size(); i++) {
			final Complemento c = this.COMPLEMENTOS.get(i);
			if (area.intersects(c.getArea())) {
				lista.add(c);
			}
		}
		return lista;
	}

	public boolean intersectaAlgunComplemento(final Shape area) {
		if (!this.intersectaZona(area)) {
			return false;
		}
		for (int i = 0; i < this.COMPLEMENTOS.size(); i++) {
			final Complemento c = this.COMPLEMENTOS.get(i);
			if (area.intersects(c.getArea())) {
				return true;
			}
		}
		return false;
	}

	public boolean intersectaAlgunaCriatura(final Shape area) {
		if (!this.intersectaZona(area)) {
			return false;
		}
		for (int i = 0; i < this.CRIATURAS.size(); i++) {
			final Criatura c = this.CRIATURAS.get(i);
			if (area.intersects(c.getArea())) {
				return true;
			}
		}
		return false;
	}

	public boolean intersectaAlgunItem(final Shape area) {
		if (!this.intersectaZona(area)) {
			return false;
		}
		for (int i = 0; i < this.ITEMS.size(); i++) {
			final Item item = this.ITEMS.get(i);
			if (area.intersects(item.getArea())) {
				return true;
			}
		}
		return false;
	}

	public boolean intersectaObjetoSolido(final Shape area) {
		if (!this.intersectaZona(area)) {
			return false;
		}
		for (int i = 0; i < this.COMPLEMENTOS.size(); i++) {
			final Complemento c = this.COMPLEMENTOS.get(i);
			if (c.intersecta(area) && c.esSolido()) {
				return true;
			}
		}
		for (int i = 0; i < this.OBJETOS.size(); i++) {
			final Objeto o = this.OBJETOS.get(i);
			if (area.intersects(o.getArea()) && o.esSolido()) {
				return true;
			}
		}
		return false;
	}

	public boolean intersectaObjetoSolidoPermanente(final Shape area) {
		if (!this.intersectaZona(area)) {
			return false;
		}
		for (int i = 0; i < this.COMPLEMENTOS.size(); i++) {
			final Complemento c = this.COMPLEMENTOS.get(i);
			if (c.intersecta(area) && c.esSolido()) {
				return true;
			}
		}
		for (int i = 0; i < this.OBJETOS.size(); i++) {
			final Objeto o = this.OBJETOS.get(i);
			if (area.intersects(o.getArea()) && o.esSolido()) {
				return true;
			}
		}
		return false;
	}

	public boolean intersectaAreaNoSolidaDeAlgunComplemento(final Shape area) {
		if (!this.intersectaZona(area)) {
			return false;
		}
		for (int i = 0; i < this.COMPLEMENTOS.size(); i++) {
			final Complemento c = this.COMPLEMENTOS.get(i);
			if (c.esSolido() && c.intersectaAreaNoSolida(area)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Verifica si una forma geométrica solapa los límites de esta celda.
	 *
	 * @param area Forma geométrica a evaluar.
	 * @return {@code true} si hay intersección; {@code false} de lo contrario.
	 */
	public boolean intersectaZona(final Shape area) {
		return area.intersects(this.AREA);
	}

	// =========================================================================
	// === GETTERS DIRECTOS & MÉTODOS HEREDADOS DE ENTE
	// =========================================================================

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