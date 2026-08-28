package principal.mapa;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Criatura.Direccion;
import principal.entes.modelos.complemento.ListaModeloComplemento;
import principal.entes.modelos.complemento.ModeloComplementoT1;
import principal.entes.modelos.complemento.ModeloComplementoT2;
import principal.entes.objetos.Complemento;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.cofres.Cofre;
import principal.entes.objetos.items.Item;
import principal.entes.objetos.particulas.Particula;
import principal.entes.proyectil.Proyectil;
import principal.entes.proyectil.ProyectilGeneral;
import principal.ia.aEstrella.AEstrella;
import principal.ia.dijkstra.DijkstraRework;
import principal.ia.dijkstra.NodoD;
import principal.mapa.escenario.Escenario;
import principal.mapa.mapas.Spawn;
import principal.mapa.renderEntidades.ZoneBox;
import principal.maquinaestado.estados.pantallaCarga.GestorCarga;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.Globales;

/**
 * Gestor principal del mundo del juego.
 * <p>
 * Administra el terreno, las partículas, proyectiles, nodos de navegación
 * masiva (Dijkstra) y el particionado espacial mediante una grilla plana 1D de
 * {@link ZoneBox} para acceso en memoria contigua $O(1)$ puro sin sobrecarga de
 * HashMaps.
 * </p>
 * 
 * @version 3.1
 */
public class Mundo {

	protected final Escenario ESCENARIO;
	protected final HashMap<String, Spawn> PUNTOS_SPAWN_JUGADOR = new HashMap<String, Spawn>();
	protected final int LADO_ZONEBOX = 64;
	private boolean forzarUnaActualizacionDijkstra;

	// =========================================================================
	// === ARREGLO PLANO 1D PARA BÚSQUEDA O(1) REAL EN MEMORIA CONTIGUA
	// =========================================================================
	protected ZoneBox[] ZONAS_ARRAY;
	protected int cantZonasX;
	protected int cantZonasY;

	// =========================================================================
	// === COLA DE RENDERIZADO AUTO-EXPANDIBLE CON Y-SORTING (ZERO-GC)
	// =========================================================================
	private static final int CAPACIDAD_INICIAL_COLA = 512;
	private Ente[] colaRenderEntidades = new Ente[CAPACIDAD_INICIAL_COLA];
	private int cantEntidadesEnCola = 0;

	// Set ultra-rápido basado en identidad de puntero (==) sin costo de .equals()
	// ni .hashCode()
	protected final Set<Ente> ENTES_REGISTRADOS = Collections.newSetFromMap(new IdentityHashMap<>());

	protected final ArrayList<Particula> PARTICULAS = new ArrayList<Particula>();
	protected final ArrayList<Proyectil> PROYECTILES = new ArrayList<Proyectil>();
	protected final DijkstraRework dijkstra;
	protected final AEstrella AESTRELLA_X12X20;
	protected int codAct;
	protected int codPintado;
	public static final String CLAVE_PUNTO_SPAWN_COMIENZO = "Comienzo";

	// =========================================================================
	// === CONSTRUCTORES
	// =========================================================================

	public Mundo(final Escenario esc, final Point comienzo) {
		this.ESCENARIO = esc;
		this.generarZonas();
		for (final Item i : this.ESCENARIO.generarItemsEnTerreno()) {
			this.meterEntidad(i);
		}
		esc.generarListaComplementos(this);
		this.generarCriaturas(esc.generarListaCriaturas(this));
		this.ESCENARIO.generarObjetosEnTerreno(this);
		this.PUNTOS_SPAWN_JUGADOR.put(CLAVE_PUNTO_SPAWN_COMIENZO, new Spawn(comienzo, CLAVE_PUNTO_SPAWN_COMIENZO));
		this.dijkstra = new DijkstraRework(this, new Dimension(16, 16));
		this.dijkstra.actualizar(new Point(this.PUNTOS_SPAWN_JUGADOR.get(CLAVE_PUNTO_SPAWN_COMIENZO).getX(),
				this.PUNTOS_SPAWN_JUGADOR.get(CLAVE_PUNTO_SPAWN_COMIENZO).getY()));
		this.AESTRELLA_X12X20 = new AEstrella(this, new Dimension(12, 20));
	}

	public Mundo(final Escenario esc, final Point comienzo, final GestorCarga gc, final int porcentajeCarga) {
		this.ESCENARIO = esc;
		int pesoCarga = 25;
		gc.setDetalleCarga("Generando render zonas");
		this.generarZonas();
		gc.setPorcentajeCarga(gc.getPorcentaje() + ((pesoCarga * porcentajeCarga) / 100));

		pesoCarga = 15;
		gc.setDetalleCarga("Generando items");
		for (final Item i : this.ESCENARIO.generarItemsEnTerreno()) {
			this.meterEntidad(i);
		}
		gc.setPorcentajeCarga(gc.getPorcentaje() + ((pesoCarga * porcentajeCarga) / 100));

		pesoCarga = 35;
		gc.setDetalleCarga("Generando complementos");
		esc.generarListaComplementos(this);
		gc.setPorcentajeCarga(gc.getPorcentaje() + ((pesoCarga * porcentajeCarga) / 100));

		pesoCarga = 15;
		gc.setDetalleCarga("Generando criaturas");
		this.generarCriaturas(esc.generarListaCriaturas(this));
		gc.setPorcentajeCarga(gc.getPorcentaje() + ((pesoCarga * porcentajeCarga) / 100));

		pesoCarga = 10;
		gc.setDetalleCarga("Generando objetos");
		this.ESCENARIO.generarObjetosEnTerreno(this);
		gc.setPorcentajeCarga(gc.getPorcentaje() + ((pesoCarga * porcentajeCarga) / 100));

		this.PUNTOS_SPAWN_JUGADOR.put(CLAVE_PUNTO_SPAWN_COMIENZO, new Spawn(comienzo, CLAVE_PUNTO_SPAWN_COMIENZO));
		this.dijkstra = new DijkstraRework(this, new Dimension(16, 16));
		this.dijkstra.actualizar(new Point(this.PUNTOS_SPAWN_JUGADOR.get(CLAVE_PUNTO_SPAWN_COMIENZO).getX(),
				this.PUNTOS_SPAWN_JUGADOR.get(CLAVE_PUNTO_SPAWN_COMIENZO).getY()));
		this.AESTRELLA_X12X20 = new AEstrella(this, new Dimension(12, 20));
	}

	public Mundo(final Terreno terrenoSoloParaEDITOR) {
		this.ESCENARIO = new Escenario(terrenoSoloParaEDITOR, "[]", "[]", "[]", "[]");
		this.PUNTOS_SPAWN_JUGADOR.put(CLAVE_PUNTO_SPAWN_COMIENZO, new Spawn(new Point(), CLAVE_PUNTO_SPAWN_COMIENZO));
		this.dijkstra = new DijkstraRework(this, new Dimension(16, 16));
		this.AESTRELLA_X12X20 = new AEstrella(this, new Dimension(12, 20));
		this.generarZonas();
	}

	// =========================================================================
	// === PARTICIONADO ESPACIAL (GRILLA 1D)
	// =========================================================================

	private void generarZonas() {
		final int limiteY = this.ESCENARIO.getTerreno().CANTIDAD_ALTO_GROUPTILE
				* this.ESCENARIO.getTerreno().LADO_GRUPO_TILE;
		final int limiteX = this.ESCENARIO.getTerreno().CANTIDAD_ANCHO_GROUPTILE
				* this.ESCENARIO.getTerreno().LADO_GRUPO_TILE;

		this.cantZonasX = Math.max(1, (int) Math.ceil((double) limiteX / this.LADO_ZONEBOX));
		this.cantZonasY = Math.max(1, (int) Math.ceil((double) limiteY / this.LADO_ZONEBOX));

		this.ZONAS_ARRAY = new ZoneBox[this.cantZonasX * this.cantZonasY];

		for (int gy = 0; gy < this.cantZonasY; gy++) {
			for (int gx = 0; gx < this.cantZonasX; gx++) {
				final int x = gx * this.LADO_ZONEBOX;
				final int y = gy * this.LADO_ZONEBOX;
				this.ZONAS_ARRAY[(gy * this.cantZonasX) + gx] = new ZoneBox(x, y, this.LADO_ZONEBOX, this.LADO_ZONEBOX,
						this);
			}
		}
	}

	/**
	 * Acceso $O(1)$ directo por cálculo aritmético en el arreglo continuo.
	 *
	 * @param gx Índice de la columna en la grilla espacial.
	 * @param gy Índice de la fila en la grilla espacial.
	 * @return La celda {@link ZoneBox} correspondiente o {@code null} si está fuera
	 *         de rango.
	 */
	public ZoneBox getZonaGrid(final int gx, final int gy) {
		if ((gx < 0) || (gx >= this.cantZonasX) || (gy < 0) || (gy >= this.cantZonasY)) {
			return null;
		}
		return this.ZONAS_ARRAY[(gy * this.cantZonasX) + gx];
	}

	// =========================================================================
	// === BUCLE PRINCIPAL (ACTUALIZAR & PINTAR)
	// =========================================================================

	public void actualizar() {
		this.actualizarDijkstra();
		this.actualizarZonas();
		this.actualizarParticulas();
		this.actualizarProyectiles();
		this.updateNextCodAct();
	}

	public void pintar(final Graphics2D g) {
		this.ESCENARIO.getTerreno().pintar(g);
		this.pintarParticulas(g);
		this.pintarZonas(g);
		this.pintarProyectiles(g);

		if (Globales.TECLADO.TECLA_DIJKSTRA_INFO.presionado()) {
			this.pintarNodosOptimizado(g);
		}

		this.updateNextCodPintado();
	}

	/**
	 * Renderiza únicamente las celdas espaciales ({@link ZoneBox}) y las entidades
	 * contenidas dentro del campo de visión visible (Frustum Culling) con
	 * Y-Sorting.
	 *
	 * @param g Contexto gráfico 2D.
	 */
	protected void pintarZonas(final Graphics2D g) {
		if ((this.ZONAS_ARRAY == null) || (this.ZONAS_ARRAY.length == 0)) {
			return;
		}

		// 1. Limpiamos la cola de render del frame anterior
		this.cantEntidadesEnCola = 0;

		final double zoomActivo = Math.max(0.2, Globales.CAMARA.getZoomFinal());
		final double rotAbs = Math.abs(Globales.CAMARA.getGestorEfectos().getAnguloRotacion());
		final double shakeX = Math.abs(Globales.CAMARA.getGestorEfectos().getOffsetX());
		final double shakeY = Math.abs(Globales.CAMARA.getGestorEfectos().getOffsetY());

		final double cos = Math.cos(rotAbs);
		final double sin = Math.sin(rotAbs);

		final int radioVisibleX = (int) Math
				.ceil(((Constantes.CENTROX * cos) + (Constantes.CENTROY * sin)) / zoomActivo) + (int) shakeX
				+ this.LADO_ZONEBOX;
		final int radioVisibleY = (int) Math
				.ceil(((Constantes.CENTROX * sin) + (Constantes.CENTROY * cos)) / zoomActivo) + (int) shakeY
				+ this.LADO_ZONEBOX;

		final int camX = Globales.CAMARA.getPosicionXInt();
		final int camY = Globales.CAMARA.getPosicionYInt();

		final int inicioGridX = Math.max(0, Math.floorDiv(camX - radioVisibleX, this.LADO_ZONEBOX));
		final int finGridX = Math.min(this.cantZonasX - 1, Math.floorDiv(camX + radioVisibleX, this.LADO_ZONEBOX));
		final int inicioGridY = Math.max(0, Math.floorDiv(camY - radioVisibleY, this.LADO_ZONEBOX));
		final int finGridY = Math.min(this.cantZonasY - 1, Math.floorDiv(camY + radioVisibleY, this.LADO_ZONEBOX));

		ZoneBox zbAux = null;

		// 2. Pasada 1: Pintar ítems del suelo y recolectar entidades a ordenar
		for (int gridY = inicioGridY; gridY <= finGridY; gridY++) {
			final int offsetFila = gridY * this.cantZonasX;
			for (int gridX = inicioGridX; gridX <= finGridX; gridX++) {
				zbAux = this.ZONAS_ARRAY[offsetFila + gridX];
				if (zbAux != null) {
					zbAux.pintar(g);
					zbAux.recolectarEntidadesParaRender(this);
				}
			}
		}

		// 3. Incluimos al Jugador en la cola para que participe del ordenamiento
		if (!Globales.JUGADOR.estaEliminado()) {
			this.agregarAColaRender(Globales.JUGADOR);
		}

		// Insertion sort in-place (Zero-GC y $O(N)$ sobre lista casi ordenada)
		for (int i = 1; i < this.cantEntidadesEnCola; i++) {
			final Ente clave = this.colaRenderEntidades[i];
			final int yBaseClave = clave.getPosicionYBase();
			int j = i - 1;

			while ((j >= 0) && (this.colaRenderEntidades[j].getPosicionYBase() > yBaseClave)) {
				this.colaRenderEntidades[j + 1] = this.colaRenderEntidades[j];
				j--;
			}
			this.colaRenderEntidades[j + 1] = clave;
		}

		// 4. Pasada 2: Dibujar todas las entidades en orden de profundidad perfecto
		for (int i = 0; i < this.cantEntidadesEnCola; i++) {
			this.colaRenderEntidades[i].pintar(g);
			this.colaRenderEntidades[i] = null; // Evita retención en memoria (GC friendly)
		}
	}

	/**
	 * Actualiza la lógica de las celdas espaciales contenidas dentro del frustum de
	 * simulación activa.
	 */
	protected void actualizarZonas() {
		if ((this.ZONAS_ARRAY == null) || (this.ZONAS_ARRAY.length == 0)) {
			return;
		}

		final double zoomActivo = Math.max(0.2, Globales.CAMARA.getZoomFinal());
		final double rotAbs = Math.abs(Globales.CAMARA.getGestorEfectos().getAnguloRotacion());
		final double shakeX = Math.abs(Globales.CAMARA.getGestorEfectos().getOffsetX());
		final double shakeY = Math.abs(Globales.CAMARA.getGestorEfectos().getOffsetY());

		final double cos = Math.cos(rotAbs);
		final double sin = Math.sin(rotAbs);

		final int margenSimulacion = this.LADO_ZONEBOX * 2;
		final int radioSimulacionX = (int) Math
				.ceil(((Constantes.CENTROX * cos) + (Constantes.CENTROY * sin)) / zoomActivo) + (int) shakeX
				+ margenSimulacion;
		final int radioSimulacionY = (int) Math
				.ceil(((Constantes.CENTROX * sin) + (Constantes.CENTROY * cos)) / zoomActivo) + (int) shakeY
				+ margenSimulacion;

		final int camX = Globales.CAMARA.getPosicionXInt();
		final int camY = Globales.CAMARA.getPosicionYInt();

		final int inicioGridX = Math.max(0, Math.floorDiv(camX - radioSimulacionX, this.LADO_ZONEBOX));
		final int finGridX = Math.min(this.cantZonasX - 1, Math.floorDiv(camX + radioSimulacionX, this.LADO_ZONEBOX));
		final int inicioGridY = Math.max(0, Math.floorDiv(camY - radioSimulacionY, this.LADO_ZONEBOX));
		final int finGridY = Math.min(this.cantZonasY - 1, Math.floorDiv(camY + radioSimulacionY, this.LADO_ZONEBOX));

		ZoneBox zbAux = null;
		for (int gridY = inicioGridY; gridY <= finGridY; gridY++) {
			final int offsetFila = gridY * this.cantZonasX;
			for (int gridX = inicioGridX; gridX <= finGridX; gridX++) {
				zbAux = this.ZONAS_ARRAY[offsetFila + gridX];
				if (zbAux != null) {
					zbAux.actualizar();
				}
			}
		}
	}

	public void agregarAColaRender(final Ente e) {
		if (e == null) {
			return;
		}
		if (this.cantEntidadesEnCola >= this.colaRenderEntidades.length) {
			final Ente[] nuevoArreglo = new Ente[this.colaRenderEntidades.length * 2];
			System.arraycopy(this.colaRenderEntidades, 0, nuevoArreglo, 0, this.colaRenderEntidades.length);
			this.colaRenderEntidades = nuevoArreglo;
		}
		this.colaRenderEntidades[this.cantEntidadesEnCola++] = e;
	}

	// =========================================================================
	// === GESTIÓN E INSERCIÓN DE ENTIDADES
	// =========================================================================

	public boolean meterEntidad(final Ente e) {
		if ((e == null) || this.ENTES_REGISTRADOS.contains(e)) {
			return false;
		}
		if (!this.getTerreno().AreaDentroDelTerreno(e.getArea())) {
			return false;
		}

		// Obtenemos las celdas que intersecta de forma matemática AABB
		final ArrayList<ZoneBox> zonas = this.getZonasIntersectadas(e);
		if (zonas.isEmpty()) {
			return false;
		}

		// 1. Asignamos el mundo y vinculamos bidireccionalmente con cada ZoneBox
		e.setMundo(this);
		for (int i = 0; i < zonas.size(); i++) {
			final ZoneBox zb = zonas.get(i);
			zb.addEntidad(e);
			e.getZonasOcupadas().add(zb);
		}

		// 2. Registramos la entidad en el mundo
		this.ENTES_REGISTRADOS.add(e);

		// 3. Verificación de colisiones de sólidos en tiles
		if ((e instanceof Objeto) && ((Objeto) e).esSolido()) {
			this.objetoSolidoVerificarTile((Objeto) e);
		}

		// 4. Debug en modo editor
		if (Globales.isEstadoEditor()) {
			System.out.println(
					"Entidad " + e + " agregada en x: " + e.getPosicionXInt() + " , y: " + e.getPosicionYInt());
		}

		return true;
	}

	private void objetoSolidoVerificarTile(final Objeto obj) {
		if (obj instanceof Complemento) {
			final Complemento c = (Complemento) obj;
			if (ListaModeloComplemento.getModeloComplemento(c.getCodigoModelo()) instanceof ModeloComplementoT1) {
				this.objetoSolidoVerificarTileByArea(c,
						c.getAreaInterseccionEnBaseMargen(
								((ModeloComplementoT1) ListaModeloComplemento.getModeloComplemento(c.getCodigoModelo()))
										.getMargenesInterseccion()));
			} else if (ListaModeloComplemento
					.getModeloComplemento(c.getCodigoModelo()) instanceof ModeloComplementoT2) {
				for (final Rectangle margen : ((ModeloComplementoT2) ListaModeloComplemento
						.getModeloComplemento(c.getCodigoModelo())).getMargenesInterseccion()) {
					this.objetoSolidoVerificarTileByArea(c, c.getAreaInterseccionEnBaseMargen(margen));
				}
			}
		} else {
			this.objetoSolidoVerificarTileByArea(obj, obj.getArea());
		}
	}

	private void objetoSolidoVerificarTileByArea(final Objeto obj, final Rectangle area) {
		GroupTile gt = this.getTerreno().getGrupoTileReferenciado(area.x, area.y);
		if (gt != null) {
			for (final Tile t : gt.getTiles()) {
				t.meterObjetoSolido(obj);
			}
			gt = null;
		}

		gt = this.getTerreno().getGrupoTileReferenciado(area.x + area.width, area.y);
		if (gt != null) {
			for (final Tile t : gt.getTiles()) {
				t.meterObjetoSolido(obj);
			}
			gt = null;
		}

		gt = this.getTerreno().getGrupoTileReferenciado(area.x, area.y + area.height);
		if (gt != null) {
			for (final Tile t : gt.getTiles()) {
				t.meterObjetoSolido(obj);
			}
			gt = null;
		}

		gt = this.getTerreno().getGrupoTileReferenciado(area.x + area.width, area.y + area.height);
		if (gt != null) {
			for (final Tile t : gt.getTiles()) {
				t.meterObjetoSolido(obj);
			}
			gt = null;
		}
	}

	// =========================================================================
	// === MÉTODOS DE BÚSQUEDA Y CONSULTA ESPACIAL (ZERO-GC)
	// =========================================================================

	public ArrayList<ZoneBox> getZonasIntersectadas(final Ente e) {
		return this.getZonasIntersectadas(e.getArea());
	}

	/**
	 * Proyección espacial AABB uniforme para cualquier forma geométrica con cálculo
	 * O(1).
	 */
	public ArrayList<ZoneBox> getZonasIntersectadas(final Shape s) {
		final Rectangle r = s.getBounds();
		final ArrayList<ZoneBox> lista = new ArrayList<>(4);

		if ((this.cantZonasX <= 0) || (this.cantZonasY <= 0)) {
			return lista;
		}

		final int minGX = Math.max(0, Math.floorDiv(r.x, this.LADO_ZONEBOX));
		final int maxGX = Math.min(this.cantZonasX - 1, Math.floorDiv(r.x + r.width, this.LADO_ZONEBOX));
		final int minGY = Math.max(0, Math.floorDiv(r.y, this.LADO_ZONEBOX));
		final int maxGY = Math.min(this.cantZonasY - 1, Math.floorDiv(r.y + r.height, this.LADO_ZONEBOX));

		for (int gy = minGY; gy <= maxGY; gy++) {
			final int offsetFila = gy * this.cantZonasX;
			for (int gx = minGX; gx <= maxGX; gx++) {
				final ZoneBox zb = this.ZONAS_ARRAY[offsetFila + gx];
				if ((zb != null) && s.intersects(zb.getArea())) {
					lista.add(zb);
				}
			}
		}
		return lista;
	}

	public HashSet<ZoneBox> getZoneBoxsIntersectados(final Shape s) {
		return new HashSet<ZoneBox>(this.getZonasIntersectadas(s));
	}

	public HashSet<Item> getItemsIntersectados(final Shape area) {
		final Rectangle rArea = area.getBounds();
		final HashSet<Item> lista = new HashSet<Item>();
		if (!this.getTerreno().AreaDentroDelTerreno(rArea)) {
			return lista;
		}
		for (final ZoneBox zb : this.getZonasIntersectadas(area)) {
			lista.addAll(zb.getItemsIntersectados(area));
		}
		return lista;
	}

	public ArrayList<Criatura> getCriaturasIntersectadas(final Shape area, final boolean tenerEnCuentaJugador) {
		final Rectangle rArea = area.getBounds();
		final ArrayList<Criatura> lista = new ArrayList<Criatura>();
		if (!this.getTerreno().AreaDentroDelTerreno(rArea)) {
			return lista;
		}
		if (tenerEnCuentaJugador && area.intersects(Globales.JUGADOR.getArea())) {
			lista.add(Globales.JUGADOR);
		}

		for (final ZoneBox zb : this.getZonasIntersectadas(area)) {
			lista.addAll(zb.getCriaturasIntersectadas(area));
		}

		return lista;
	}

	public ArrayList<Ente> getEnteIntersectados(final Shape area, final boolean tenerEnCuentaJugador) {
		final Rectangle rArea = area.getBounds();
		final ArrayList<Ente> lista = new ArrayList<Ente>();
		if (!this.getTerreno().AreaDentroDelTerreno(rArea)) {
			return lista;
		}
		if (tenerEnCuentaJugador && area.intersects(Globales.JUGADOR.getArea())) {
			lista.add(Globales.JUGADOR);
		}

		for (final ZoneBox zb : this.getZonasIntersectadas(area)) {
			lista.addAll(zb.getEntesIntersectados(area));
		}

		for (int i = 0; i < this.PROYECTILES.size(); i++) {
			final Proyectil p = this.PROYECTILES.get(i);
			if (area.intersects(p.getArea())) {
				lista.add(p);
			}
		}

		return lista;
	}

	public ArrayList<Criatura> getCriaturasIntersectadasConEnte(final Ente e) {
		final ArrayList<Criatura> criaturas = new ArrayList<Criatura>();
		for (final ZoneBox zb : this.getZonasIntersectadas(e)) {
			final ArrayList<Criatura> criatZona = zb.getCriaturas();
			for (int i = 0; i < criatZona.size(); i++) {
				final Criatura c = criatZona.get(i);
				if (!criaturas.contains(c)) {
					criaturas.add(c);
				}
			}
		}
		return criaturas;
	}

	public boolean intersectaAlgunaCriatura(final Shape area, final boolean tenerEnCuentaJugador) {
		final Rectangle rArea = area.getBounds();
		if (!this.getTerreno().AreaDentroDelTerreno(rArea)) {
			return false;
		}
		if (tenerEnCuentaJugador && area.intersects(Globales.JUGADOR.getArea())) {
			return true;
		}

		for (final ZoneBox zb : this.getZonasIntersectadas(area)) {
			if (zb.intersectaAlgunaCriatura(area)) {
				return true;
			}
		}

		return false;
	}

	public boolean colisionaConZonaUObjetoSolido(final Shape area) {
		return this.getTerreno().intersectaTileSolido(area) || this.colisionaConObjetoSolido(area);
	}

	public boolean colisionaConObjetoSolido(final Shape area) {
		final Rectangle rArea = area.getBounds();
		if (!this.getTerreno().AreaDentroDelTerreno(rArea)) {
			return false;
		}

		for (final ZoneBox zb : this.getZonasIntersectadas(area)) {
			if (zb.intersectaObjetoSolido(area)) {
				return true;
			}
		}

		return false;
	}

	public boolean colisionaConAlgoSolidoPermanente(final Shape area) {
		if (this.getTerreno().intersectaSolidoDijkstra(area.getBounds())) {
			return true;
		}

		for (final ZoneBox zb : this.getZonasIntersectadas(area)) {
			if (zb.intersectaObjetoSolidoPermanente(area)) {
				return true;
			}
		}
		return false;
	}

	public boolean colisionaConObjetoSolidoPeroEnZonaNoSolida(final Shape area) {
		final Rectangle rArea = area.getBounds();
		if (!this.getTerreno().AreaDentroDelTerreno(rArea)) {
			return false;
		}

		for (final ZoneBox zb : this.getZonasIntersectadas(area)) {
			if (zb.intersectaAreaNoSolidaDeAlgunComplemento(area)) {
				return true;
			}
		}
		return false;
	}

	public boolean agregarItemEnPosicionJugador(final Item item, final boolean copiar) {
		final int x = Globales.JUGADOR.getPosicionXParado();
		final int y = Globales.JUGADOR.getPosicionYParado();
		item.setPosicion(x, y);

		if (copiar) {
			return this.meterEntidad(item.copiar());
		}
		return this.meterEntidad(item);
	}

	// =========================================================================
	// === GESTIÓN DE PARTÍCULAS Y PROYECTILES (CORREGIDO CON BUCLES INVERTIDOS)
	// =========================================================================

	private void actualizarParticulas() {
		for (int i = this.PARTICULAS.size() - 1; i >= 0; i--) {
			final Particula p = this.PARTICULAS.get(i);
			p.actualizar();
			if (p.estaEliminado()) {
				this.PARTICULAS.remove(i);
			}
		}
	}

	private void actualizarProyectiles() {
		for (int i = this.PROYECTILES.size() - 1; i >= 0; i--) {
			final Proyectil p = this.PROYECTILES.get(i);
			p.actualizar();
			if (p.estaEliminado()) {
				this.PROYECTILES.remove(i);
			}
		}
	}

	private void pintarParticulas(final Graphics2D g) {
		for (int i = 0; i < this.PARTICULAS.size(); i++) {
			this.PARTICULAS.get(i).pintar(g);
		}
	}

	private void pintarProyectiles(final Graphics2D g) {
		for (int i = 0; i < this.PROYECTILES.size(); i++) {
			this.PROYECTILES.get(i).pintar(g);
		}
	}

	public void agregarParticula(final Particula p) {
		this.PARTICULAS.add(p);
	}

	public void crearProyectil(final int damage, final double velocidad, final boolean penetrante, final int alcance,
			final double x, final double y, final int ancho, final int alto, final Direccion direccion,
			final Criatura causante) {
		this.PROYECTILES.add(new ProyectilGeneral(damage, velocidad, penetrante, alcance, this, x, y, ancho, alto,
				direccion, causante));
	}

	public void crearProyectil(final Proyectil proyectil) {
		this.PROYECTILES.add(proyectil);
	}

	// =========================================================================
	// === NAVEGACIÓN E INTELIGENCIA ARTIFICIAL (DIJKSTRA & A*)
	// =========================================================================

	private static final Font FUENTE_DEBUG_NODOS = new Font(Font.SANS_SERIF, Font.PLAIN, 6);

	private void pintarNodosOptimizado(final Graphics2D g) {
		final Font fontOriginal = g.getFont();
		g.setFont(FUENTE_DEBUG_NODOS);
		final Color color = Globales.TECLADO.TECLA_OCULTAR_TERRENO.presionado() ? Color.WHITE : Color.BLACK;

		final int anchoNodo = this.dijkstra.getDimensionNodo().width;
		final int altoNodo = this.dijkstra.getDimensionNodo().height;

		final double zoomActivo = Math.max(0.2, Globales.CAMARA.getZoomFinal());
		final double rotAbs = Math.abs(Globales.CAMARA.getGestorEfectos().getAnguloRotacion());
		final double shakeX = Math.abs(Globales.CAMARA.getGestorEfectos().getOffsetX());
		final double shakeY = Math.abs(Globales.CAMARA.getGestorEfectos().getOffsetY());

		final double cos = Math.cos(rotAbs);
		final double sin = Math.sin(rotAbs);

		final int radioVisibleX = (int) Math
				.ceil(((Constantes.CENTROX * cos) + (Constantes.CENTROY * sin)) / zoomActivo) + (int) shakeX
				+ (3 * anchoNodo);
		final int radioVisibleY = (int) Math
				.ceil(((Constantes.CENTROX * sin) + (Constantes.CENTROY * cos)) / zoomActivo) + (int) shakeY
				+ (3 * altoNodo);

		final int camX = Globales.CAMARA.getPosicionXInt();
		final int camY = Globales.CAMARA.getPosicionYInt();

		final int inicioX = Math.floorDiv(camX - radioVisibleX, anchoNodo) * anchoNodo;
		final int finX = Math.floorDiv(camX + radioVisibleX, anchoNodo) * anchoNodo;
		final int inicioY = Math.floorDiv(camY - radioVisibleY, altoNodo) * altoNodo;
		final int finY = Math.floorDiv(camY + radioVisibleY, altoNodo) * altoNodo;

		final int readBuf = this.dijkstra.getBufferLecturaIndex();
		final int codCompleto = this.dijkstra.getCodActCompleto();

		NodoD nodo = null;
		for (int y = inicioY; y <= finY; y += altoNodo) {
			for (int x = inicioX; x <= finX; x += anchoNodo) {
				nodo = this.dijkstra.getNodoReferenciado(x, y);
				if (nodo != null) {
					final double distanciaReal = (nodo.getCodAct(readBuf) == codCompleto) ? nodo.getDistancia(readBuf)
							: Double.MAX_VALUE;
					final String textoDistancia = (distanciaReal == Double.MAX_VALUE) ? "XX"
							: String.valueOf((long) (distanciaReal * 10) / 10.0);
					DibujoDebug.dibujarStringRefCamara(g, textoDistancia, x, y + 10, color);
				}
			}
		}
		g.setFont(fontOriginal);
	}

	private void actualizarDijkstra() {
		if (Globales.TECLADO.TECLA_DIJKSTRA.presionado() && this.dijkstra.hayEntidadesAlPendiente()) {
			this.dijkstra.actualizar(Globales.JUGADOR.getPosicionParado());
		}
	}

	public DijkstraRework getDijkstra() {
		return this.dijkstra;
	}

	public AEstrella getAEstrellaX12X20() {
		return this.AESTRELLA_X12X20;
	}

	public boolean hayQueForzarActDijkstra() {
		return this.forzarUnaActualizacionDijkstra;
	}

	public void forzarActDijkstra() {
		if (!this.forzarUnaActualizacionDijkstra) {
			this.forzarUnaActualizacionDijkstra = true;
		}
	}

	// =========================================================================
	// === ACCESORES, SPAWN Y SERIALIZACIÓN JSON
	// =========================================================================

	public void updateNextCodAct() {
		this.codAct = (this.codAct < Integer.MAX_VALUE) ? this.codAct + 1 : Integer.MIN_VALUE;
	}

	public void updateNextCodPintado() {
		this.codPintado = (this.codPintado < Integer.MAX_VALUE) ? this.codPintado + 1 : Integer.MIN_VALUE;
	}

	public int getCodAct() {
		return this.codAct;
	}

	public int getCodPintado() {
		return this.codPintado;
	}

	public Terreno getTerreno() {
		return this.ESCENARIO.getTerreno();
	}

	public int getLadoZoneBox() {
		return this.LADO_ZONEBOX;
	}

	public ZoneBox[] getZonasArray() {
		return this.ZONAS_ARRAY;
	}

	public int getCantZonasX() {
		return this.cantZonasX;
	}

	public int getCantZonasY() {
		return this.cantZonasY;
	}

	public Set<Ente> getEntes() {
		return this.ENTES_REGISTRADOS;
	}

	public void eliminarCriaturas() {
		final Iterator<Ente> it = this.ENTES_REGISTRADOS.iterator();
		while (it.hasNext()) {
			final Ente e = it.next();
			if (e instanceof Criatura) {
				e.eliminar(); // Apaga su luz y se desvincula de sus ZoneBox automáticamente
				it.remove(); // Se remueve del set de forma segura
			}
		}
	}

	public void agregarCriatura(final Criatura c) {
		this.meterEntidad(c);
	}

	private int generarCriaturas(final ArrayList<Criatura> criaturas) {
		int cant = 0;
		for (final Criatura c : criaturas) {
			this.meterEntidad(c);
			cant++;
		}
		return cant;
	}

	/**
	 * Remueve y desvincula una entidad individual del mundo.
	 */
	public void eliminarEntidad(final Ente e) {
		if ((e != null) && this.ENTES_REGISTRADOS.remove(e)) {
			e.eliminar(); // Apaga luz y limpia sus ZoneBox
		}
	}

	public long getCantEntidadesEnTerreno() {
		return this.ENTES_REGISTRADOS.size();
	}

	public long getCantEntidadesTotal() {
		return this.ENTES_REGISTRADOS.size() + this.ESCENARIO.getTerreno().getCantidadTiles();
	}

	public void moverJugadorPuntoComienzo() {
		this.PUNTOS_SPAWN_JUGADOR.get(CLAVE_PUNTO_SPAWN_COMIENZO).moverJugadorCentrado();
	}

	public void llenarSpawn(final ArrayList<Spawn> lista) {
		for (final Spawn spawn : lista) {
			this.PUNTOS_SPAWN_JUGADOR.put(spawn.getNombre(), spawn);
		}
	}

	public Spawn getSpawn(final String nombreSpawn) {
		return this.PUNTOS_SPAWN_JUGADOR.get(nombreSpawn);
	}

	@SuppressWarnings("unchecked")
	public JSONObject getEntesInJson() {
		final JSONObject listas = new JSONObject();
		final JSONArray listaComplementos = new JSONArray();
		final JSONArray listaCriaturas = new JSONArray();
		final JSONArray listaItems = new JSONArray();
		final JSONArray listaObjetos = new JSONArray();
		Complemento complemento = null;
		Item item = null;
		Cofre cofre = null;

		for (final Ente e : this.getEntes()) {
			if (e instanceof Criatura) {
				// Serialización de criaturas según se requiera
			} else if (e instanceof Complemento) {
				complemento = (Complemento) e;
				listaComplementos.add(complemento.exportarParaJSON());
			} else if (e instanceof Objeto) {
				if (e instanceof Item) {
					item = (Item) e;
					listaItems.add(item.getJsonItem());
				} else if (e instanceof Cofre) {
					cofre = (Cofre) e;
					listaObjetos.add(cofre.exportarParaJson());
				}
			}
		}
		listas.put(Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Complemento.class), listaComplementos);
		listas.put(Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Criatura.class), listaCriaturas);
		listas.put(Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Item.class), listaItems);
		listas.put(Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Objeto.class), listaObjetos);
		return listas;
	}

	@SuppressWarnings("unchecked")
	public JSONObject getMundoEnJson() {
		final JSONObject jsonMundo = this.getEntesInJson();
		jsonMundo.put(Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Tile.class),
				this.ESCENARIO.getTerreno().getTilesJson());
		final JSONArray listaPuntosSpawn = new JSONArray();
		JSONObject jsonSpawn = null;
		for (final Spawn s : this.PUNTOS_SPAWN_JUGADOR.values()) {
			jsonSpawn = new JSONObject();
			jsonSpawn.put("x", s.getX());
			jsonSpawn.put("y", s.getY());
			jsonSpawn.put("nombre", s.getNombre());
			listaPuntosSpawn.add(jsonSpawn);
		}
		jsonMundo.put(Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Spawn.class), listaPuntosSpawn);
		return jsonMundo;
	}
}