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
import principal.entes.criaturas.Jugador;
import principal.entes.objetos.ArbolCofre;
import principal.entes.objetos.Complemento;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.cofres.Cofre;
import principal.entes.objetos.items.Item;
import principal.entes.objetos.particulas.Particula;
import principal.entes.objetos.recursos.ArbolCosechable;
import principal.entes.objetos.recursos.RocaCosechable;
import principal.entes.proyectil.GestorProyectiles;
import principal.entes.proyectil.Proyectil;
import principal.entes.proyectil.ProyectilGeneral;
import principal.ia.aEstrella.AEstrella;
import principal.ia.dijkstra.DijkstraRework;
import principal.ia.dijkstra.NodoD;
import principal.mapa.escenario.Escenario;
import principal.mapa.mapas.Spawn;
import principal.mapa.renderEntidades.ZoneBox;
import principal.maquinaestado.estados.pantallaCarga.GestorCarga;
import principal.utilidades.AccionEntidad;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

/**
 * Gestor maestro del mapa activo, flujo de navegación, zonas de indexación y
 * ciclo de vida de entidades.
 * 
 * @version 5.1 (Vanilla Java 8 - Universal JSON Object Serialization)
 */
public class Mundo {

	protected String nombreMundo = "Exterior";
	protected final Escenario ESCENARIO;
	protected final HashMap<String, Spawn> PUNTOS_SPAWN_JUGADOR = new HashMap<String, Spawn>();
	private boolean forzarUnaActualizacionDijkstra;

	protected ZoneBox[] ZONAS_ARRAY;
	protected int cantZonasX;
	protected int cantZonasY;
	protected final int LADO_ZONEBOX = 64;

	private static final int CAPACIDAD_INICIAL_COLA = 512;
	private Ente[] colaRenderEntidades = new Ente[CAPACIDAD_INICIAL_COLA];
	private int cantEntidadesEnCola = 0;

	protected final Set<Ente> ENTES_REGISTRADOS = Collections.newSetFromMap(new IdentityHashMap<Ente, Boolean>());
	protected final ArrayList<Particula> PARTICULAS = new ArrayList<Particula>();
	protected final GestorProyectiles GESTOR_PROYECTILES = new GestorProyectiles();

	protected final DijkstraRework dijkstra;
	protected final AEstrella AESTRELLA_X12X20;

	protected int codAct;
	protected int codPintado;

	public static final String CLAVE_PUNTO_SPAWN_COMIENZO = "Comienzo";

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
		this.AESTRELLA_X12X20 = new AEstrella(this, new Dimension(Constantes.LADO_TILE, Constantes.LADO_TILE));
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
		this.AESTRELLA_X12X20 = new AEstrella(this, new Dimension(Constantes.LADO_TILE, Constantes.LADO_TILE));
	}

	public Mundo(final Terreno terrenoSoloParaEDITOR) {
		this.ESCENARIO = new Escenario(terrenoSoloParaEDITOR, "[]", "[]", "[]", "[]");
		this.PUNTOS_SPAWN_JUGADOR.put(CLAVE_PUNTO_SPAWN_COMIENZO, new Spawn(new Point(), CLAVE_PUNTO_SPAWN_COMIENZO));
		this.dijkstra = new DijkstraRework(this, new Dimension(16, 16));
		this.AESTRELLA_X12X20 = new AEstrella(this, new Dimension(Constantes.LADO_TILE, Constantes.LADO_TILE));
		this.generarZonas();
	}

	public String getNombreMundo() {
		return (this.nombreMundo != null) ? this.nombreMundo : "Exterior";
	}

	public void setNombreMundo(final String nombreMundo) {
		this.nombreMundo = nombreMundo;
	}

	private void generarZonas() {
		final int limiteY = this.ESCENARIO.getTerreno().getAlto();
		final int limiteX = this.ESCENARIO.getTerreno().getAncho();

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

	public ZoneBox getZonaGrid(final int gx, final int gy) {
		if ((gx < 0) || (gx >= this.cantZonasX) || (gy < 0) || (gy >= this.cantZonasY)) {
			return null;
		}
		return this.ZONAS_ARRAY[(gy * this.cantZonasX) + gx];
	}

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

	protected void pintarZonas(final Graphics2D g) {
		if ((this.ZONAS_ARRAY == null) || (this.ZONAS_ARRAY.length == 0)) {
			return;
		}

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

		if (!Globales.JUGADOR.estaEliminado() && !Globales.isEstadoEditor()) {
			this.agregarAColaRender(Globales.JUGADOR);
		}

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

		for (int i = 0; i < this.cantEntidadesEnCola; i++) {
			this.colaRenderEntidades[i].pintar(g);
			this.colaRenderEntidades[i] = null;
		}
	}

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
		if ((e == null) || e.estaEliminado()) {
			return;
		}
		if (this.cantEntidadesEnCola >= this.colaRenderEntidades.length) {
			final Ente[] nuevoArreglo = new Ente[this.colaRenderEntidades.length * 2];
			System.arraycopy(this.colaRenderEntidades, 0, nuevoArreglo, 0, this.colaRenderEntidades.length);
			this.colaRenderEntidades = nuevoArreglo;
		}
		this.colaRenderEntidades[this.cantEntidadesEnCola++] = e;
	}

	public boolean meterEntidad(final Ente e) {
		if ((e == null) || e.estaEliminado() || this.ENTES_REGISTRADOS.contains(e)) {
			return false;
		}
		if (!this.getTerreno().AreaDentroDelTerreno(e.getArea())) {
			return false;
		}

		final int posX = e.getPosicionXInt();
		final int posY = e.getPosicionYInt();
		final int ancho = e.getAncho();
		final int alto = e.getAlto();

		final int minGX = Math.max(0, Math.floorDiv(posX, this.LADO_ZONEBOX));
		final int maxGX = Math.min(this.cantZonasX - 1,
				Math.floorDiv((posX + Math.max(1, ancho)) - 1, this.LADO_ZONEBOX));
		final int minGY = Math.max(0, Math.floorDiv(posY, this.LADO_ZONEBOX));
		final int maxGY = Math.min(this.cantZonasY - 1,
				Math.floorDiv((posY + Math.max(1, alto)) - 1, this.LADO_ZONEBOX));

		e.setMundo(this);

		for (int gy = minGY; gy <= maxGY; gy++) {
			final int offset = gy * this.cantZonasX;
			for (int gx = minGX; gx <= maxGX; gx++) {
				final ZoneBox zb = this.ZONAS_ARRAY[offset + gx];
				if (zb != null) {
					zb.addEntidad(e);
					e.getZonasOcupadas().add(zb);
				}
			}
		}

		this.ENTES_REGISTRADOS.add(e);
		return true;
	}

	public boolean hayLineaDeTiroLimpia(final double x0, final double y0, final double x1, final double y1) {
		if (!this.getTerreno().hayLineaDeVisionLimpia(x0, y0, x1, y1)) {
			return false;
		}

		final int minX = (int) Math.min(x0, x1);
		final int maxX = (int) Math.max(x0, x1);
		final int minY = (int) Math.min(y0, y1);
		final int maxY = (int) Math.max(y0, y1);

		final int minGX = Math.max(0, Math.floorDiv(minX, this.LADO_ZONEBOX));
		final int maxGX = Math.min(this.cantZonasX - 1, Math.floorDiv(maxX, this.LADO_ZONEBOX));
		final int minGY = Math.max(0, Math.floorDiv(minY, this.LADO_ZONEBOX));
		final int maxGY = Math.min(this.cantZonasY - 1, Math.floorDiv(maxY, this.LADO_ZONEBOX));

		for (int gy = minGY; gy <= maxGY; gy++) {
			final int offset = gy * this.cantZonasX;
			for (int gx = minGX; gx <= maxGX; gx++) {
				final ZoneBox zb = this.ZONAS_ARRAY[offset + gx];
				if ((zb != null) && zb.intersectaLineaSolida(x0, y0, x1, y1)) {
					return false;
				}
			}
		}

		return true;
	}

	public void notificarModificacionEstructura() {
		if (this.dijkstra != null) {
			this.dijkstra.calcularMatrizClearance();
			this.forzarActDijkstra();
		}
		if (this.AESTRELLA_X12X20 != null) {
			this.AESTRELLA_X12X20.calcularMatrizClearance();
		}
	}

	public void paraCadaCriaturaEn(final Shape area, final boolean incluirJugador,
			final AccionEntidad<Criatura> accion) {
		if ((area == null) || (accion == null)) {
			return;
		}

		if (incluirJugador && !Globales.JUGADOR.estaEliminado() && area.intersects(Globales.JUGADOR.getArea())) {
			accion.ejecutar(Globales.JUGADOR);
		}

		final Rectangle r = area.getBounds();
		final int minGX = Math.max(0, Math.floorDiv(r.x, this.LADO_ZONEBOX));
		final int maxGX = Math.min(this.cantZonasX - 1, Math.floorDiv((r.x + r.width) - 1, this.LADO_ZONEBOX));
		final int minGY = Math.max(0, Math.floorDiv(r.y, this.LADO_ZONEBOX));
		final int maxGY = Math.min(this.cantZonasY - 1, Math.floorDiv((r.y + r.height) - 1, this.LADO_ZONEBOX));

		for (int gy = minGY; gy <= maxGY; gy++) {
			final int offset = gy * this.cantZonasX;
			for (int gx = minGX; gx <= maxGX; gx++) {
				final ZoneBox zb = this.ZONAS_ARRAY[offset + gx];
				if (zb != null) {
					zb.paraCadaCriatura(area, accion);
				}
			}
		}
	}

	public void paraCadaItemEn(final Shape area, final AccionEntidad<Item> accion) {
		if ((area == null) || (accion == null)) {
			return;
		}

		final Rectangle r = area.getBounds();
		final int minGX = Math.max(0, Math.floorDiv(r.x, this.LADO_ZONEBOX));
		final int maxGX = Math.min(this.cantZonasX - 1, Math.floorDiv((r.x + r.width) - 1, this.LADO_ZONEBOX));
		final int minGY = Math.max(0, Math.floorDiv(r.y, this.LADO_ZONEBOX));
		final int maxGY = Math.min(this.cantZonasY - 1, Math.floorDiv((r.y + r.height) - 1, this.LADO_ZONEBOX));

		for (int gy = minGY; gy <= maxGY; gy++) {
			final int offset = gy * this.cantZonasX;
			for (int gx = minGX; gx <= maxGX; gx++) {
				final ZoneBox zb = this.ZONAS_ARRAY[offset + gx];
				if (zb != null) {
					zb.paraCadaItem(area, accion);
				}
			}
		}
	}

	public void paraCadaObjetoEn(final Shape area, final AccionEntidad<Objeto> accion) {
		if ((area == null) || (accion == null)) {
			return;
		}

		final Rectangle r = area.getBounds();
		final int minGX = Math.max(0, Math.floorDiv(r.x, this.LADO_ZONEBOX));
		final int maxGX = Math.min(this.cantZonasX - 1, Math.floorDiv((r.x + r.width) - 1, this.LADO_ZONEBOX));
		final int minGY = Math.max(0, Math.floorDiv(r.y, this.LADO_ZONEBOX));
		final int maxGY = Math.min(this.cantZonasY - 1, Math.floorDiv((r.y + r.height) - 1, this.LADO_ZONEBOX));

		for (int gy = minGY; gy <= maxGY; gy++) {
			final int offset = gy * this.cantZonasX;
			for (int gx = minGX; gx <= maxGX; gx++) {
				final ZoneBox zb = this.ZONAS_ARRAY[offset + gx];
				if (zb != null) {
					zb.paraCadaObjeto(area, accion);
				}
			}
		}
	}

	public void paraCadaEnteEn(final Shape area, final boolean incluirJugador, final AccionEntidad<Ente> accion) {
		if ((area == null) || (accion == null)) {
			return;
		}

		if (incluirJugador && !Globales.JUGADOR.estaEliminado() && area.intersects(Globales.JUGADOR.getArea())) {
			accion.ejecutar(Globales.JUGADOR);
		}

		final Rectangle r = area.getBounds();
		final int minGX = Math.max(0, Math.floorDiv(r.x, this.LADO_ZONEBOX));
		final int maxGX = Math.min(this.cantZonasX - 1, Math.floorDiv((r.x + r.width) - 1, this.LADO_ZONEBOX));
		final int minGY = Math.max(0, Math.floorDiv(r.y, this.LADO_ZONEBOX));
		final int maxGY = Math.min(this.cantZonasY - 1, Math.floorDiv((r.y + r.height) - 1, this.LADO_ZONEBOX));

		for (int gy = minGY; gy <= maxGY; gy++) {
			final int offset = gy * this.cantZonasX;
			for (int gx = minGX; gx <= maxGX; gx++) {
				final ZoneBox zb = this.ZONAS_ARRAY[offset + gx];
				if (zb != null) {
					zb.paraCadaEnte(area, accion);
				}
			}
		}
	}

	public boolean colisionaConZonaUObjetoSolido(final Shape area) {
		return this.getTerreno().intersectaAlgoSolido(area) || this.colisionaConObjetoSolido(area);
	}

	public boolean colisionaConObjetoSolido(final Shape area) {
		if (area == null) {
			return false;
		}

		final Rectangle r = area.getBounds();
		final int minGX = Math.max(0, Math.floorDiv(r.x, this.LADO_ZONEBOX));
		final int maxGX = Math.min(this.cantZonasX - 1, Math.floorDiv((r.x + r.width) - 1, this.LADO_ZONEBOX));
		final int minGY = Math.max(0, Math.floorDiv(r.y, this.LADO_ZONEBOX));
		final int maxGY = Math.min(this.cantZonasY - 1, Math.floorDiv((r.y + r.height) - 1, this.LADO_ZONEBOX));

		for (int gy = minGY; gy <= maxGY; gy++) {
			final int offset = gy * this.cantZonasX;
			for (int gx = minGX; gx <= maxGX; gx++) {
				final ZoneBox zb = this.ZONAS_ARRAY[offset + gx];
				if ((zb != null) && zb.intersectaObjetoSolido(area)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean colisionaConAlgoSolidoPermanente(final Shape area) {
		if (area == null) {
			return false;
		}
		if (this.getTerreno().intersectaAlgoSolido(area)) {
			return true;
		}
		return this.colisionaConObjetoSolido(area);
	}

	public boolean colisionaConObjetoSolidoPeroEnZonaNoSolida(final Shape area) {
		if (area == null) {
			return false;
		}

		final Rectangle r = area.getBounds();
		final int minGX = Math.max(0, Math.floorDiv(r.x, this.LADO_ZONEBOX));
		final int maxGX = Math.min(this.cantZonasX - 1, Math.floorDiv((r.x + r.width) - 1, this.LADO_ZONEBOX));
		final int minGY = Math.max(0, Math.floorDiv(r.y, this.LADO_ZONEBOX));
		final int maxGY = Math.min(this.cantZonasY - 1, Math.floorDiv((r.y + r.height) - 1, this.LADO_ZONEBOX));

		for (int gy = minGY; gy <= maxGY; gy++) {
			final int offset = gy * this.cantZonasX;
			for (int gx = minGX; gx <= maxGX; gx++) {
				final ZoneBox zb = this.ZONAS_ARRAY[offset + gx];
				if ((zb != null) && zb.intersectaAreaNoSolidaDeAlgunComplemento(area)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean intersectaAlgunaCriatura(final Shape area, final boolean tenerEnCuentaJugador) {
		if (area == null) {
			return false;
		}
		if (tenerEnCuentaJugador && !Globales.JUGADOR.estaEliminado() && area.intersects(Globales.JUGADOR.getArea())) {
			return true;
		}

		final Rectangle r = area.getBounds();
		final int minGX = Math.max(0, Math.floorDiv(r.x, this.LADO_ZONEBOX));
		final int maxGX = Math.min(this.cantZonasX - 1, Math.floorDiv((r.x + r.width) - 1, this.LADO_ZONEBOX));
		final int minGY = Math.max(0, Math.floorDiv(r.y, this.LADO_ZONEBOX));
		final int maxGY = Math.min(this.cantZonasY - 1, Math.floorDiv((r.y + r.height) - 1, this.LADO_ZONEBOX));

		for (int gy = minGY; gy <= maxGY; gy++) {
			final int offset = gy * this.cantZonasX;
			for (int gx = minGX; gx <= maxGX; gx++) {
				final ZoneBox zb = this.ZONAS_ARRAY[offset + gx];
				if ((zb != null) && zb.intersectaAlgunaCriatura(area)) {
					return true;
				}
			}
		}
		return false;
	}

	public ArrayList<ZoneBox> getZonasIntersectadas(final Shape s) {
		final ArrayList<ZoneBox> lista = new ArrayList<>(4);
		if ((s == null) || (this.cantZonasX <= 0) || (this.cantZonasY <= 0)) {
			return lista;
		}

		final Rectangle r = s.getBounds();
		final int minGX = Math.max(0, Math.floorDiv(r.x, this.LADO_ZONEBOX));
		final int maxGX = Math.min(this.cantZonasX - 1,
				Math.floorDiv((r.x + Math.max(1, r.width)) - 1, this.LADO_ZONEBOX));
		final int minGY = Math.max(0, Math.floorDiv(r.y, this.LADO_ZONEBOX));
		final int maxGY = Math.min(this.cantZonasY - 1,
				Math.floorDiv((r.y + Math.max(1, r.height)) - 1, this.LADO_ZONEBOX));

		for (int gy = minGY; gy <= maxGY; gy++) {
			final int offset = gy * this.cantZonasX;
			for (int gx = minGX; gx <= maxGX; gx++) {
				final ZoneBox zb = this.ZONAS_ARRAY[offset + gx];
				if ((zb != null) && s.intersects(zb.getArea())) {
					lista.add(zb);
				}
			}
		}
		return lista;
	}

	public ArrayList<ZoneBox> getZonasIntersectadas(final Ente e) {
		return (e != null) ? this.getZonasIntersectadas(e.getArea()) : new ArrayList<ZoneBox>(0);
	}

	public ArrayList<Criatura> getCriaturasIntersectadas(final Shape area, final boolean tenerEnCuentaJugador) {
		final ArrayList<Criatura> lista = new ArrayList<>();
		this.paraCadaCriaturaEn(area, tenerEnCuentaJugador, lista::add);
		return lista;
	}

	public ArrayList<Criatura> getCriaturasIntersectadasConEnte(final Ente e) {
		final ArrayList<Criatura> criaturas = new ArrayList<>();
		if (e != null) {
			this.paraCadaCriaturaEn(e.getArea(), false, c -> {
				if (!criaturas.contains(c)) {
					criaturas.add(c);
				}
			});
		}
		return criaturas;
	}

	public HashSet<Item> getItemsIntersectados(final Shape area) {
		final HashSet<Item> lista = new HashSet<>();
		this.paraCadaItemEn(area, lista::add);
		return lista;
	}

	public ArrayList<Ente> getEnteIntersectados(final Shape area, final boolean tenerEnCuentaJugador) {
		final ArrayList<Ente> lista = new ArrayList<>();
		this.paraCadaEnteEn(area, tenerEnCuentaJugador, lista::add);
		this.GESTOR_PROYECTILES.agregarIntersecciones(area.getBounds(), lista);
		return lista;
	}

	public boolean agregarItemEnPosicionJugador(final Item item, final boolean copiar) {
		if (item == null) {
			return false;
		}
		final int x = Globales.JUGADOR.getPosicionXParado();
		final int y = Globales.JUGADOR.getPosicionYParado();
		item.setPosicion(x, y);

		if (copiar) {
			return this.meterEntidad(item.copiar());
		}
		return this.meterEntidad(item);
	}

	private void actualizarParticulas() {
		for (int i = this.PARTICULAS.size() - 1; i >= 0; i--) {
			final Particula p = this.PARTICULAS.get(i);
			p.actualizar();
			if (p.estaEliminado()) {
				this.PARTICULAS.remove(i);
			}
		}
	}

	private void pintarParticulas(final Graphics2D g) {
		for (int i = 0; i < this.PARTICULAS.size(); i++) {
			this.PARTICULAS.get(i).pintar(g);
		}
	}

	private void actualizarProyectiles() {
		this.GESTOR_PROYECTILES.actualizar();
	}

	private void pintarProyectiles(final Graphics2D g) {
		this.GESTOR_PROYECTILES.pintar(g);
	}

	public void crearProyectil(final Proyectil proyectil) {
		if (proyectil != null) {
			this.GESTOR_PROYECTILES.agregarProyectil(proyectil);
		}
	}

	public GestorProyectiles getGestorProyectiles() {
		return this.GESTOR_PROYECTILES;
	}

	public void agregarParticula(final Particula p) {
		if (p != null) {
			this.PARTICULAS.add(p);
		}
	}

	public void crearProyectil(final int damage, final double velocidad, final boolean penetrante, final int alcance,
			final double x, final double y, final int ancho, final int alto, final Direccion direccion,
			final Criatura causante) {
		this.GESTOR_PROYECTILES.agregarProyectil((new ProyectilGeneral(damage, velocidad, penetrante, alcance, this, x,
				y, ancho, alto, direccion, causante)));
	}

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
					Render2D.dibujarStringRefCamara(g, textoDistancia, x, y + 10, color);
				}
			}
		}
		g.setFont(fontOriginal);
	}

	private void actualizarDijkstra() {
		if (this.dijkstra.hayEntidadesAlPendiente() || this.forzarUnaActualizacionDijkstra) {
			this.dijkstra.actualizar(Globales.JUGADOR.getPosicionParado());
			this.forzarUnaActualizacionDijkstra = false;
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

	public void paraCadaCriaturaEn(final int x, final int y, final int w, final int h, final boolean incluirJugador,
			final AccionEntidad<Criatura> accion) {
		if (accion == null) {
			return;
		}

		if (incluirJugador && !Globales.JUGADOR.estaEliminado() && Globales.JUGADOR.getArea().intersects(x, y, w, h)) {
			accion.ejecutar(Globales.JUGADOR);
		}

		final int minGX = Math.max(0, Math.floorDiv(x, this.LADO_ZONEBOX));
		final int maxGX = Math.min(this.cantZonasX - 1, Math.floorDiv((x + w) - 1, this.LADO_ZONEBOX));
		final int minGY = Math.max(0, Math.floorDiv(y, this.LADO_ZONEBOX));
		final int maxGY = Math.min(this.cantZonasY - 1, Math.floorDiv((y + h) - 1, this.LADO_ZONEBOX));

		for (int gy = minGY; gy <= maxGY; gy++) {
			final int offset = gy * this.cantZonasX;
			for (int gx = minGX; gx <= maxGX; gx++) {
				final ZoneBox zb = this.ZONAS_ARRAY[offset + gx];
				if (zb != null) {
					zb.paraCadaCriatura(x, y, w, h, accion);
				}
			}
		}
	}

	public void paraCadaCriaturaEn(final Rectangle r, final boolean incluirJugador,
			final AccionEntidad<Criatura> accion) {
		if (r != null) {
			this.paraCadaCriaturaEn(r.x, r.y, r.width, r.height, incluirJugador, accion);
		}
	}

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
			if ((e instanceof Criatura) && !(e instanceof Jugador)) {
				e.eliminar();
				it.remove();
			}
		}
	}

	public void agregarCriatura(final Criatura c) {
		this.meterEntidad(c);
	}

	private int generarCriaturas(final ArrayList<Criatura> criaturas) {
		int cant = 0;
		for (int i = 0; i < criaturas.size(); i++) {
			this.meterEntidad(criaturas.get(i));
			cant++;
		}
		return cant;
	}

	public void eliminarEntidad(final Ente e) {
		if (e != null) {
			this.ENTES_REGISTRADOS.remove(e);
			e.eliminar();
		}
	}

	public void eliminarEntidadRegistro(final Ente e) {
		if (e != null) {
			this.ENTES_REGISTRADOS.remove(e);
		}
	}

	public long getCantEntidadesEnTerreno() {
		return this.ENTES_REGISTRADOS.size();
	}

	public long getCantEntidadesTotal() {
		return this.ENTES_REGISTRADOS.size() + this.ESCENARIO.getTerreno().getCantidadTiles();
	}

	public void moverJugadorPuntoComienzo() {
		final Spawn spawn = this.PUNTOS_SPAWN_JUGADOR.get(CLAVE_PUNTO_SPAWN_COMIENZO);
		if (spawn != null) {
			spawn.moverJugadorCentrado();
		}
	}

	public void llenarSpawn(final ArrayList<Spawn> lista) {
		if (lista != null) {
			for (final Spawn spawn : lista) {
				this.PUNTOS_SPAWN_JUGADOR.put(spawn.getNombre(), spawn);
			}
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

		for (final Ente e : this.getEntes()) {
			if (e.estaEliminado()) {
				continue;
			}
			if (e instanceof Criatura) {
				if (!(e instanceof Jugador)) {
					listaCriaturas.add(((Criatura) e).getJsonCriatura());
				}
			} else if (e instanceof Complemento) {
				listaComplementos.add(((Complemento) e).exportarParaJSON());
			} else if (e instanceof Item) {
				listaItems.add(((Item) e).getJsonItem());
			} else if (e instanceof Cofre) {
				listaObjetos.add(((Cofre) e).exportarParaJson());
			} else if (e instanceof ArbolCofre) {
				listaObjetos.add(((ArbolCofre) e).exportarParaJson());
			} else if (e instanceof ArbolCosechable) {
				final JSONObject wrapper = new JSONObject();
				wrapper.put("tipoObjeto", "ArbolCosechable");
				wrapper.put("entiti", ((ArbolCosechable) e).exportarParaJSON());
				listaObjetos.add(wrapper);
			} else if (e instanceof RocaCosechable) {
				final JSONObject wrapper = new JSONObject();
				wrapper.put("tipoObjeto", "RocaCosechable");
				wrapper.put("entiti", ((RocaCosechable) e).exportarParaJSON());
				listaObjetos.add(wrapper);
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
		for (final Spawn s : this.PUNTOS_SPAWN_JUGADOR.values()) {
			final JSONObject jsonSpawn = new JSONObject();
			jsonSpawn.put("x", Integer.valueOf(s.getX()));
			jsonSpawn.put("y", Integer.valueOf(s.getY()));
			jsonSpawn.put("nombre", s.getNombre());
			listaPuntosSpawn.add(jsonSpawn);
		}

		jsonMundo.put(Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Spawn.class), listaPuntosSpawn);
		return jsonMundo;
	}
}