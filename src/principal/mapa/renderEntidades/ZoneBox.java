package principal.mapa.renderEntidades;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.objetos.Complemento;
import principal.entes.objetos.cofres.Cofre;
import principal.entes.objetos.items.Item;
import principal.inventario.Contenedor;
import principal.mapa.Mundo;
import principal.mapa.escenario.tps.ZonaTP;
import principal.utilidades.Constantes;

/**
 * Representa una cuadrícula o celda del espacio (partición espacial) en la
 * grilla del mapa.
 * <p>
 * Administra el ciclo de vida, actualización y renderizado local de todas las
 * entidades que intersectan con su área delimitada. Reduce drásticamente la
 * complejidad de detección de colisiones de $O(N^2)$ a $O(1)$ por vecindario.
 * </p>
 */
public class ZoneBox extends Ente {

	/** Área rectangular que delimita los límites de esta zona espacial. */
	protected final Rectangle AREA;

	/*
	 * =============================================================================
	 * ==== OPTIMIZACIÓN DE MEMORIA / GARBAGE COLLECTOR: Listas temporales internas
	 * reutilizables. Se inicializan una sola vez para evitar crear arreglos o
	 * iteradores dinámicos en cada frame, eliminando asignaciones en el Heap.
	 * =============================================================================
	 * ====
	 */
	private final ArrayList<Criatura> tempCriaturas = new ArrayList<>();
	private final ArrayList<Item> tempItems = new ArrayList<>();
	private final ArrayList<Contenedor> tempCofres = new ArrayList<>();
	private final ArrayList<ZonaTP> tempTPs = new ArrayList<>();

	/**
	 * Colecciones de entidades clasificadas por tipo para consultas rápidas $O(1)$.
	 */
	protected final Set<Criatura> CRIATURAS = new LinkedHashSet<>();
	protected final Set<Item> ITEMS = new LinkedHashSet<>();
	protected final Set<Cofre> CONTENEDORES = new LinkedHashSet<>();
	protected final Set<Complemento> COMPLEMENTOS = new LinkedHashSet<>();
	protected final Set<ZonaTP> ZONAS_TP = new LinkedHashSet<>();

	/** Referencia al escenario o mundo contenedor. */
	protected final Mundo mundo;

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
	 * la zona.
	 * <p>
	 * <b>Prevención de ConcurrentModificationException:</b> Se realiza un volcado
	 * previo a las listas temporales reusables. Si una entidad cambia de posición o
	 * se elimina durante su `actualizar()`, modifica el {@code Set} principal sin
	 * romper el bucle activo sobre la lista temporal.
	 * </p>
	 */
	@Override
	public void actualizar() {
		if (Constantes.isEstadoEditor()) {
			return;
		}
		RenderEntidad re = null;

		// --- ACTUALIZACIÓN DE CONTENEDORES ---
		if (!this.CONTENEDORES.isEmpty()) {
			this.tempCofres.clear();
			this.tempCofres.addAll(this.CONTENEDORES);
			for (int i = 0; i < this.tempCofres.size(); i++) {
				final Contenedor contenedor = this.tempCofres.get(i);
				// Valida que el cofre siga perteneciendo al Set antes de actualizar
				if (this.CONTENEDORES.contains(contenedor)) {
					re = this.mundo.getRenders().getRender(contenedor.getEntePropietario());
					if ((re != null) && !re.estaRenderizado()) {
						contenedor.getInventario().actualizarEstadoCofre();
						re.update();
						re.renderizado();
					}
				}
			}
		}

		// --- ACTUALIZACIÓN DE CRIATURAS ---
		if (!this.CRIATURAS.isEmpty()) {
			this.tempCriaturas.clear();
			this.tempCriaturas.addAll(this.CRIATURAS);
			for (int i = 0; i < this.tempCriaturas.size(); i++) {
				final Criatura criatura = this.tempCriaturas.get(i);
				if (this.CRIATURAS.contains(criatura)) {
					re = this.mundo.getRenders().getRender(criatura);
					if ((re != null) && !re.estaRenderizado()) {
						criatura.actualizar();
						re.update();
						re.renderizado();
					}
				}
			}
		}

		// --- ACTUALIZACIÓN DE ITEMS ---
		if (!this.ITEMS.isEmpty()) {
			this.tempItems.clear();
			this.tempItems.addAll(this.ITEMS);
			for (int i = 0; i < this.tempItems.size(); i++) {
				final Item item = this.tempItems.get(i);
				if (this.ITEMS.contains(item)) {
					re = this.mundo.getRenders().getRender(item);
					if ((re != null) && !re.estaRenderizado()) {
						item.actualizar();
						re.update();
						re.renderizado();
					}
				}
			}
		}

		// --- ACTUALIZACIÓN DE ZONAS DE TELETRANSPORTE ---
		if (!this.ZONAS_TP.isEmpty()) {
			this.tempTPs.clear();
			this.tempTPs.addAll(this.ZONAS_TP);
			for (int i = 0; i < this.tempTPs.size(); i++) {
				final ZonaTP zonaTP = this.tempTPs.get(i);
				if (this.ZONAS_TP.contains(zonaTP)) {
					re = this.mundo.getRenders().getRender(zonaTP);
					if ((re != null) && !re.estaRenderizado()) {
						zonaTP.actualizar();
						re.update();
						re.renderizado();
					}
				}
			}
		}
	}

	/**
	 * Renderiza en pantalla las entidades contenidas en esta celda.
	 * <p>
	 * Utiliza {@link RenderEntidad#estaPintado()} para garantizar que si una
	 * entidad abarca múltiples celdas vecinos, solo se dibuje una única vez por
	 * frame visual.
	 * </p>
	 *
	 * @param g Contexto gráfico {@link Graphics2D} sobre el cual pintar.
	 */
	@Override
	public void pintar(final Graphics2D g) {
		RenderEntidad re = null;

		for (final Complemento c : this.COMPLEMENTOS) {
			re = this.mundo.getRenders().getRender(c);
			if ((re != null) && !re.estaPintado()) {
				c.pintar(g);
				re.pintado();
			}
		}

		for (final Cofre c : this.CONTENEDORES) {
			re = this.mundo.getRenders().getRender(c);
			if ((re != null) && !re.estaPintado()) {
				c.pintar(g);
				re.pintado();
			}
		}

		for (final Item i : this.ITEMS) {
			re = this.mundo.getRenders().getRender(i);
			if ((re != null) && !re.estaPintado()) {
				i.pintar(g);
				re.pintado();
			}
		}

		for (final Criatura c : this.CRIATURAS) {
			re = this.mundo.getRenders().getRender(c);
			if ((re != null) && !re.estaPintado()) {
				c.pintar(g);
				re.pintado();
			}
		}

		for (final ZonaTP zonaTP : this.ZONAS_TP) {
			re = this.mundo.getRenders().getRender(zonaTP);
			if ((re != null) && !re.estaPintado()) {
				zonaTP.pintar(g);
				re.pintado();
			}
		}
	}

	/**
	 * Clasifica y agrega un {@link Ente} a su colección correspondiente dentro de
	 * la celda.
	 *
	 * @param e Entidad a incorporar.
	 */
	public void addEntidad(final Ente e) {
		if (e instanceof Criatura) {
			this.CRIATURAS.add((Criatura) e);
		} else if (e instanceof Item) {
			this.ITEMS.add((Item) e);
		} else if (e instanceof Cofre) {
			this.CONTENEDORES.add((Cofre) e);
		} else if (e instanceof Complemento) {
			this.COMPLEMENTOS.add((Complemento) e);
		} else if (e instanceof ZonaTP) {
			this.ZONAS_TP.add((ZonaTP) e);
		}
	}

	/**
	 * Desvincular y remueve una entidad del conjunto específico de la celda.
	 *
	 * @param e Entidad a remover.
	 */
	public void eliminarEntidad(final Ente e) {
		if (e instanceof Criatura) {
			this.CRIATURAS.remove(e);
		} else if (e instanceof Item) {
			this.ITEMS.remove(e);
		} else if (e instanceof Cofre) {
			this.CONTENEDORES.remove(e);
		} else if (e instanceof Complemento) {
			this.COMPLEMENTOS.remove(e);
		} else if (e instanceof ZonaTP) {
			this.ZONAS_TP.remove(e);
		}
	}

	// --- MÉTODOS DE BÚSQUEDA Y COLISIÓN ESPACIAL ---

	/**
	 * Recolecta todas las entidades (criaturas, ítems y cofres) que colisionan con
	 * un área dada dentro de la celda.
	 */
	public ArrayList<Ente> getEntesIntersectados(final Shape area) {
		final ArrayList<Ente> lista = new ArrayList<>();
		if (!this.intersectaZona(area)) {
			return lista;
		}

		for (final Criatura c : this.CRIATURAS) {
			if (area.intersects(c.getArea())) {
				lista.add(c);
			}
		}

		for (final Item i : this.ITEMS) {
			if (area.intersects(i.getArea())) {
				lista.add(i);
			}
		}

		for (final Cofre c : this.CONTENEDORES) {
			if (area.intersects(c.getArea())) {
				lista.add(c);
			}
		}
		return lista;
	}

	public ArrayList<Item> getItemsIntersectados(final Shape area) {
		final ArrayList<Item> lista = new ArrayList<>();
		if (!this.intersectaZona(area)) {
			return lista;
		}
		for (final Item i : this.ITEMS) {
			if (area.intersects(i.getArea())) {
				lista.add(i);
			}
		}
		return lista;
	}

	public ArrayList<Criatura> getCriaturasIntersectadas(final Shape area) {
		final ArrayList<Criatura> lista = new ArrayList<>();
		if (!this.intersectaZona(area)) {
			return lista;
		}
		for (final Criatura c : this.CRIATURAS) {
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
		for (final Complemento c : this.COMPLEMENTOS) {
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
		for (final Complemento c : this.COMPLEMENTOS) {
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
		for (final Criatura c : this.CRIATURAS) {
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
		for (final Item i : this.ITEMS) {
			if (area.intersects(i.getArea())) {
				return true;
			}
		}
		return false;
	}

	public boolean intersectaObjetoSolido(final Shape area) {
		if (!this.intersectaZona(area)) {
			return false;
		}
		for (final Complemento c : this.COMPLEMENTOS) {
			if (c.intersecta(area) && c.esSolido()) {
				return true;
			}
		}
		for (final Cofre c : this.CONTENEDORES) {
			if (area.intersects(c.getArea()) && c.esSolido()) {
				return true;
			}
		}
		return false;
	}

	public boolean intersectaObjetoSolidoPermanente(final Shape area) {
		if (!this.intersectaZona(area)) {
			return false;
		}
		for (final Complemento c : this.COMPLEMENTOS) {
			if (c.intersecta(area) && c.esSolido()) {
				return true;
			}
		}
		for (final Cofre c : this.CONTENEDORES) {
			if (area.intersects(c.getArea()) && c.esSolido()) {
				return true;
			}
		}
		return false;
	}

	public boolean intersectaAreaNoSolidaDeAlgunComplemento(final Shape area) {
		if (!this.intersectaZona(area)) {
			return false;
		}
		for (final Complemento c : this.COMPLEMENTOS) {
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

	// --- GETTERS & METODOS HEREDADOS DE ENTE ---

	public Set<Criatura> getCriaturas() {
		return this.CRIATURAS;
	}

	public Set<Item> getItems() {
		return this.ITEMS;
	}

	public Set<Cofre> getCofres() {
		return this.CONTENEDORES;
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
}