package principal.mapa;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import principal.mapa.renderEntidades.MapRender;
import principal.mapa.renderEntidades.RenderEntidad;
import principal.mapa.renderEntidades.ZoneBox;
import principal.maquinaestado.estados.pantallaCarga.GestorCarga;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.Globales;

public class Mundo {
	protected final Escenario ESCENARIO;
	protected final HashMap<String, Spawn> PUNTOS_SPAWN_JUGADOR = new HashMap<String, Spawn>();
	protected final int LADO_ZONEBOX = 64;
	protected final MapRender RENDERS;
	private boolean forzarUnaActualizacionDijkstra;
	// Estructura auxiliar reutilizable para consultas O(1) de ZoneBox sin
	// instanciar 'new Point'
	private final Point CLAVE_BUSQUEDA_ZONAS = new Point();
	/*
	 * LAS ZONAS SE PODRIAN SEPARAR EN ZONAS DE ITEM, CRIATURAS, ETC. SEGUN SEA
	 * NECESARIO?
	 */
	protected final HashMap<Point, ZoneBox> ZONAS = new HashMap<Point, ZoneBox>();
	protected final ArrayList<Particula> PARTICULAS = new ArrayList<Particula>();
	protected final ArrayList<Proyectil> PROYECTILES = new ArrayList<Proyectil>();
	protected final DijkstraRework dijkstra;
	protected final AEstrella AESTRELLA_X12X20;
	protected int codAct;
	protected int codPintado;
	public static final String CLAVE_PUNTO_SPAWN_COMIENZO = "Comienzo";

	public Mundo(final Escenario esc, final Point comienzo) {
		this.ESCENARIO = esc;
		this.generarZonas();
		this.RENDERS = new MapRender(this);
		for (final Item i : this.ESCENARIO.generarItemsEnTerreno()) {
			this.meterEntidad(i);
		}
		esc.generarListaComplementos(this);
		final int cantCriaturas = this.generarCriaturas(esc.generarListaCriaturas(this));
		final int cantObjetos = this.ESCENARIO.generarObjetosEnTerreno(this);
		this.PUNTOS_SPAWN_JUGADOR.put(CLAVE_PUNTO_SPAWN_COMIENZO, new Spawn(comienzo, CLAVE_PUNTO_SPAWN_COMIENZO));
		this.dijkstra = new DijkstraRework(this, new Dimension(16, 16));
		this.dijkstra.actualizar(new Point(this.PUNTOS_SPAWN_JUGADOR.get(CLAVE_PUNTO_SPAWN_COMIENZO).getX(),
				this.PUNTOS_SPAWN_JUGADOR.get(CLAVE_PUNTO_SPAWN_COMIENZO).getY()));
		this.AESTRELLA_X12X20 = new AEstrella(this, new Dimension(12, 20));
	}

	public Mundo(final Escenario esc, final Point comienzo, final GestorCarga gc, final int porcentajeCarga) {
		this.ESCENARIO = esc;
		//////////////////////
		int pesoCarga = 25;
		gc.setDetalleCarga("Generando render zonas");
		this.generarZonas();
		gc.setPorcentajeCarga(gc.getPorcentaje() + ((pesoCarga * porcentajeCarga) / 100));
		/////////////////////
		this.RENDERS = new MapRender(this);
		pesoCarga = 15;
		gc.setDetalleCarga("Generando items");
		for (final Item i : this.ESCENARIO.generarItemsEnTerreno()) {
			this.meterEntidad(i);
		}
		gc.setPorcentajeCarga(gc.getPorcentaje() + ((pesoCarga * porcentajeCarga) / 100));
		/////////////////////
		pesoCarga = 35;
		gc.setDetalleCarga("Generando complementos");
		esc.generarListaComplementos(this);
		gc.setPorcentajeCarga(gc.getPorcentaje() + ((pesoCarga * porcentajeCarga) / 100));
		/////////////
		pesoCarga = 15;
		gc.setDetalleCarga("Generando criaturas");
		final int cantCriaturas = this.generarCriaturas(esc.generarListaCriaturas(this));
		gc.setPorcentajeCarga(gc.getPorcentaje() + ((pesoCarga * porcentajeCarga) / 100));
		////////////////////
		pesoCarga = 10;
		gc.setDetalleCarga("Generando objetos");
		final int cantObjetos = this.ESCENARIO.generarObjetosEnTerreno(this);
		gc.setPorcentajeCarga(gc.getPorcentaje() + ((pesoCarga * porcentajeCarga) / 100));
		//////////////////
		this.PUNTOS_SPAWN_JUGADOR.put(CLAVE_PUNTO_SPAWN_COMIENZO, new Spawn(comienzo, CLAVE_PUNTO_SPAWN_COMIENZO));
		this.dijkstra = new DijkstraRework(this, new Dimension(16, 16));
		this.dijkstra.actualizar(new Point(this.PUNTOS_SPAWN_JUGADOR.get(CLAVE_PUNTO_SPAWN_COMIENZO).getX(),
				this.PUNTOS_SPAWN_JUGADOR.get(CLAVE_PUNTO_SPAWN_COMIENZO).getY()));
		this.AESTRELLA_X12X20 = new AEstrella(this, new Dimension(12, 20));
	}

	public Mundo(final Terreno terrenoSoloParaEDITOR) {
		this.ESCENARIO = new Escenario(terrenoSoloParaEDITOR, "[]", "[]", "[]", "[]");
		this.RENDERS = new MapRender(this);
		this.PUNTOS_SPAWN_JUGADOR.put(CLAVE_PUNTO_SPAWN_COMIENZO, new Spawn(new Point(), CLAVE_PUNTO_SPAWN_COMIENZO));
		this.dijkstra = new DijkstraRework(this, new Dimension(16, 16));
		this.AESTRELLA_X12X20 = new AEstrella(this, new Dimension(12, 20));
		this.generarZonas();
	}

	public void actualizar() {

		this.actualizarDijkstra();
		this.actualizarZonas();
		this.actualizarParticulas();
		this.actualizarProyectiles();
		this.updateNextCodAct();
	}

	// En Mundo.java:

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
	 * contenidas dentro del campo de visión visible (Frustum Culling).
	 * <p>
	 * <b>Optimizaciones y Arquitectura de Alto Rendimiento:</b>
	 * <ul>
	 * <li><b>Prevención de "Entity Popping":</b> Compensa automáticamente la
	 * rotación angular ($\theta$), el temblor sísmico ($X, Y$) y el zoom activo
	 * para que los enemigos, cofres y proyectiles nunca desaparezcan ni parpadeen
	 * en las esquinas de la pantalla durante un efecto de cámara.</li>
	 * <li><b>Particionado Espacial O(1):</b> Proyecta el cuadro envolvente
	 * directamente a índices discretos de grilla mediante
	 * {@link Math#floorDiv(int, int)}, soportando coordenadas negativas del mundo
	 * sin desfases.</li>
	 * <li><b>Cero Asignaciones en el Heap (Zero-GC):</b> Reutiliza la estructura
	 * fija {@code CLAVE_BUSQUEDA_ZONAS} mutando su posición en lugar de instanciar
	 * objetos {@code Point} por celda.</li>
	 * </ul>
	 * </p>
	 *
	 * @param g Contexto gráfico {@link Graphics2D}.
	 */
	protected void pintarZonas(final Graphics2D g) {
		if ((this.ZONAS == null) || this.ZONAS.isEmpty()) {
			return;
		}

		/*
		 * =====================================================================
		 * EXPLICACIÓN DIDÁCTICA: CULLING DE ENTIDADES CON CÁMARA DINÁMICA
		 * --------------------------------------------------------------------- 1. ¿POR
		 * QUÉ NO ALCANZABA CON CAMARA.getZoom()? Si un proyectil o enemigo está en el
		 * borde de la pantalla y la cámara sufre un temblor hacia la derecha o gira en
		 * Modo Borracho, el cono visual se desplaza. Si no compensamos el temblor y la
		 * rotación, el juego consideraría que la 'ZoneBox' de esa entidad está
		 * "fuera de vista", haciendo que el enemigo desaparezca de golpe (Entity
		 * Popping).
		 * 
		 * 2. LA CAJA ENVOLVENTE MÍNIMA (AABB Rotado): Calculamos el semiancho
		 * (radioVisibleX) y semialto (radioVisibleY) exactos proyectando la rotación
		 * (|cos| y |sin|) sobre el centro visual (320x180) y dividiendo por el Zoom
		 * real.
		 * 
		 * 3. MARGEN DE SEGURIDAD (+LADO_ZONEBOX): Añadimos el tamaño de una celda
		 * completa para asegurar que cualquier criatura grande cuyo sprite sobresalga
		 * de su ZoneBox se dibuje completa.
		 * =====================================================================
		 */
		final double zoomActivo = Math.max(0.2, Globales.CAMARA.getZoomFinal());
		final double rotAbs = Math.abs(Globales.CAMARA.getGestorEfectos().getAnguloRotacion());
		final double shakeX = Math.abs(Globales.CAMARA.getGestorEfectos().getOffsetX());
		final double shakeY = Math.abs(Globales.CAMARA.getGestorEfectos().getOffsetY());

		final double cos = Math.cos(rotAbs);
		final double sin = Math.sin(rotAbs);

		// 1. Semiancho y semialto del cuadro delimitador envolvente en píxeles de mundo
		final int radioVisibleX = (int) Math
				.ceil(((Constantes.CENTROX * cos) + (Constantes.CENTROY * sin)) / zoomActivo) + (int) shakeX
				+ this.LADO_ZONEBOX;
		final int radioVisibleY = (int) Math
				.ceil(((Constantes.CENTROX * sin) + (Constantes.CENTROY * cos)) / zoomActivo) + (int) shakeY
				+ this.LADO_ZONEBOX;

		// Coordenadas absolutas de la cámara en el mundo
		final int camX = Globales.CAMARA.getPosicionXInt();
		final int camY = Globales.CAMARA.getPosicionYInt();

		final int minX = camX - radioVisibleX;
		final int maxX = camX + radioVisibleX;
		final int minY = camY - radioVisibleY;
		final int maxY = camY + radioVisibleY;

		// 2. Proyección matemática a coordenadas discretas de grilla de ZoneBox
		final int inicioGridX = Math.floorDiv(minX, this.LADO_ZONEBOX);
		final int finGridX = Math.floorDiv(maxX, this.LADO_ZONEBOX);

		final int inicioGridY = Math.floorDiv(minY, this.LADO_ZONEBOX);
		final int finGridY = Math.floorDiv(maxY, this.LADO_ZONEBOX);

		ZoneBox zbAux = null;

		// 3. Iteración directa sobre las celdas espaciales activas (0 objetos
		// instanciados)
		for (int gridY = inicioGridY; gridY <= finGridY; gridY++) {
			for (int gridX = inicioGridX; gridX <= finGridX; gridX++) {
				// Reutilizamos la clave de búsqueda en memoria para no hacer 'new Point()'
				this.CLAVE_BUSQUEDA_ZONAS.setLocation(gridX, gridY);
				zbAux = this.ZONAS.get(this.CLAVE_BUSQUEDA_ZONAS);

				if (zbAux != null) {
					zbAux.pintar(g);
				}
			}
		}
	}

	/**
	 * Actualiza la lógica, físicas, proyectiles e inteligencia artificial de las
	 * celdas espaciales ({@link ZoneBox}) contenidas dentro del área de simulación
	 * activa de la cámara.
	 * <p>
	 * <b>Arquitectura y Prevención de Congelamiento:</b>
	 * <ul>
	 * <li><b>Sincronización Total con la Lente:</b> Adapta el rango de simulación
	 * al {@code getZoomFinal()}, rotación y vibración de la cámara para que ninguna
	 * criatura visible en pantalla quede congelada en pose estática.</li>
	 * <li><b>Margen de Simulación Extendido (+2 ZoneBoxes):</b> Procesa las celdas
	 * ligeramente por fuera del borde visible, permitiendo que proyectiles y
	 * enemigos que entran o salen de pantalla continúen su trayectoria de forma
	 * fluida.</li>
	 * <li><b>Indexación Espacial O(1) Zero-GC:</b> Reutiliza la clave en memoria
	 * {@code CLAVE_BUSQUEDA_ZONAS} y {@link Math#floorDiv(int, int)}.</li>
	 * </ul>
	 * </p>
	 */
	protected void actualizarZonas() {
		if ((this.ZONAS == null) || this.ZONAS.isEmpty()) {
			return;
		}

		/*
		 * =====================================================================
		 * EXPLICACIÓN DIDÁCTICA: ÁREA DE SIMULACIÓN ACTIVA (UPDATE FRUSTUM)
		 * --------------------------------------------------------------------- 1. EL
		 * MARGEN DE SIMULACIÓN (+2 ZoneBoxes): A diferencia de la función 'pintar' (que
		 * solo necesita dibujar lo que se ve en el píxel exacto), la función
		 * 'actualizar' necesita un margen extra. Si un monstruo está persiguiendo al
		 * jugador y sale de la pantalla por 2 centímetros, queremos que siga corriendo
		 * hacia nosotros y no que se "congele" en seco en el borde.
		 * 
		 * 2. COMPENSACIÓN MATRICIAL DINÁMICA: Calculamos el semiancho y semialto
		 * envolvente rotado (|cos| + |sin|) dividido por el Zoom final, garantizando
		 * que el radio de actualización cubra el 100% de lo que la cámara está
		 * enfocando.
		 * =====================================================================
		 */
		final double zoomActivo = Math.max(0.2, Globales.CAMARA.getZoomFinal());
		final double rotAbs = Math.abs(Globales.CAMARA.getGestorEfectos().getAnguloRotacion());
		final double shakeX = Math.abs(Globales.CAMARA.getGestorEfectos().getOffsetX());
		final double shakeY = Math.abs(Globales.CAMARA.getGestorEfectos().getOffsetY());

		final double cos = Math.cos(rotAbs);
		final double sin = Math.sin(rotAbs);

		// Margen de simulación activa (2 celdas completas de holgura por lado)
		final int margenSimulacion = this.LADO_ZONEBOX * 2;

		// 1. Semiancho y semialto de la caja envolvente de simulación en píxeles de
		// mundo
		final int radioSimulacionX = (int) Math
				.ceil(((Constantes.CENTROX * cos) + (Constantes.CENTROY * sin)) / zoomActivo) + (int) shakeX
				+ margenSimulacion;
		final int radioSimulacionY = (int) Math
				.ceil(((Constantes.CENTROX * sin) + (Constantes.CENTROY * cos)) / zoomActivo) + (int) shakeY
				+ margenSimulacion;

		// Coordenadas absolutas de la cámara
		final int camX = Globales.CAMARA.getPosicionXInt();
		final int camY = Globales.CAMARA.getPosicionYInt();

		final int minX = camX - radioSimulacionX;
		final int maxX = camX + radioSimulacionX;
		final int minY = camY - radioSimulacionY;
		final int maxY = camY + radioSimulacionY;

		// 2. Mapeo a índices discretos de la grilla espacial de ZoneBox
		final int inicioGridX = Math.floorDiv(minX, this.LADO_ZONEBOX);
		final int finGridX = Math.floorDiv(maxX, this.LADO_ZONEBOX);

		final int inicioGridY = Math.floorDiv(minY, this.LADO_ZONEBOX);
		final int finGridY = Math.floorDiv(maxY, this.LADO_ZONEBOX);

		ZoneBox zbAux = null;

		// 3. Actualización lógica de las celdas activas (0 objetos instanciados en
		// Heap)
		for (int gridY = inicioGridY; gridY <= finGridY; gridY++) {
			for (int gridX = inicioGridX; gridX <= finGridX; gridX++) {
				// Reutilizamos la posición del Point clave para búsqueda en O(1)
				this.CLAVE_BUSQUEDA_ZONAS.setLocation(gridX, gridY);
				zbAux = this.ZONAS.get(this.CLAVE_BUSQUEDA_ZONAS);

				if (zbAux != null) {
					zbAux.actualizar();
				}
			}
		}
	}

	public boolean meterEntidad(final Ente e) {

		if (this.RENDERS.containsKey(e)) {
			return false;
		}
		if (!this.getTerreno().AreaDentroDelTerreno(e.getArea())) {
			return false;
		}
		boolean exito = false;

		final RenderEntidad re = new RenderEntidad(e, this);
		for (final ZoneBox zb : this.getZonasIntersectadas(e)) {
			if (re.contieneZona(zb)) {
				continue;
			}
			re.meterZoneBox(zb);
			zb.addEntidad(e);
			if (exito != true) {
				exito = true;
			}
		}

		if (exito) {
			e.setMundo(this);
			this.RENDERS.meterEntidad(re);
			if ((e instanceof Objeto) && ((Objeto) e).esSolido()) {
				this.objetoSolidoVerificarTile((Objeto) e);
			}
			if (Globales.isEstadoEditor()) {
				System.out.println("entidad " + e + " agregada en el punto x: " + e.getPosicionXInt() + " , y: "
						+ e.getPosicionYInt());
			}
		}

		return exito;
	}

	public MapRender getRenders() {
		return this.RENDERS;
	}

	public HashMap<Point, ZoneBox> getZonas() {
		return this.ZONAS;
	}

	public Iterable<ZoneBox> getZonasIntersectadas(final Ente e) {
		return this.getZoneBoxsIntersectados(e.getArea());
	}

	public int getCodAct() {
		return this.codAct;
	}

	public int getCodPintado() {
		return this.codPintado;
	}

	public HashSet<Item> getItemsIntersectados(final Shape area) {
		final Rectangle rArea = area.getBounds();
		final HashSet<Item> lista = new HashSet<Item>();
		if (!this.getTerreno().AreaDentroDelTerreno(rArea)) {
			System.out.println("area Afuera del terreno. (Mundo L200)");
			return lista;

		}
		for (final ZoneBox zb : this.getZoneBoxsIntersectados(area)) {
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

		for (final ZoneBox zb : this.getZoneBoxsIntersectados(area)) {
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

		for (final ZoneBox zb : this.getZoneBoxsIntersectados(area)) {
			lista.addAll(zb.getEntesIntersectados(area));
		}

		for (final Proyectil p : this.PROYECTILES) {
			if (area.intersects(p.getArea())) {
				lista.add(p);
			}
		}
		for (final Proyectil p : this.PROYECTILES) {
			if (area.intersects(p.getArea())) {
				lista.add(p);
			}
		}

		return lista;
	}

	public boolean intersectaAlgunaCriatura(final Shape area, final boolean tenerEnCuentaJugador) {
		final Rectangle rArea = area.getBounds();
		if (!this.getTerreno().AreaDentroDelTerreno(rArea)) {
			return false;
		}
		if (tenerEnCuentaJugador && area.intersects(Globales.JUGADOR.getArea())) {
			return true;
		}

		for (final ZoneBox zb : this.getZoneBoxsIntersectados(area)) {
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

		for (final ZoneBox zb : this.getZoneBoxsIntersectados(area)) {
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

		for (final ZoneBox zb : this.getZoneBoxsIntersectados(area)) {
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

		for (final ZoneBox zb : this.getZoneBoxsIntersectados(area)) {
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

	/**
	 * Calcula y recolecta todas las celdas espaciales ({@link ZoneBox}) que
	 * intersectan con una forma geométrica determinada dentro del mapa.
	 * <p>
	 * <b>Optimización Matemática (Coordenadas Negativas):</b> Utiliza
	 * {@link Math#floorDiv(int, int)} en lugar de la división convencional con
	 * truncado a cero ({@code /}). Esto asegura que las coordenadas negativas del
	 * mundo se proyecten correctamente a los índices de celdas en la grilla sin
	 * desfasajes de posición (ej. evitado el problema donde {@code -15 / 32} daría
	 * {@code 0} en lugar de la celda {@code -1}).
	 * </p>
	 *
	 * @param s Forma geométrica ({@link Shape}) a evaluar en el espacio del mundo.
	 * @return Un {@link HashSet} con las celdas {@link ZoneBox} que colisionan con
	 *         la forma dada.
	 */
	public HashSet<ZoneBox> getZoneBoxsIntersectados(final Shape s) {
		final int x = s.getBounds().x;
		final int y = s.getBounds().y;
		final int w = s.getBounds().width;
		final int h = s.getBounds().height;

		final HashSet<ZoneBox> lista = new HashSet<ZoneBox>();
		ZoneBox zona = null;

		// Proyección de límites de píxeles a índices discretos de la grilla espacial
		final int xZB = Math.floorDiv(x, this.LADO_ZONEBOX);
		final int limiteXZB = Math.floorDiv(x + w, this.LADO_ZONEBOX);
		final int yZB = Math.floorDiv(y, this.LADO_ZONEBOX);
		final int limiteYZB = Math.floorDiv(y + h, this.LADO_ZONEBOX);

		// Recorrido acotado del rango de celdas intersectadas
		for (int x2 = xZB; x2 <= limiteXZB; x2++) {
			for (int y2 = yZB; y2 <= limiteYZB; y2++) {
				zona = this.getZonaPuntoSinReferir(x2, y2);
				if ((zona != null) && s.intersects(zona.getArea())) {
					lista.add(zona);
				}
			}
		}
		return lista;
	}

	public ArrayList<ZoneBox> getZonasIntersectadas(final Shape e) {
		final ArrayList<ZoneBox> zonas = new ArrayList<ZoneBox>();

		final int x = e.getBounds().x;
		final int y = e.getBounds().y;
		final int ancho = e.getBounds().width;
		final int alto = e.getBounds().height;

		ZoneBox zb = null;
		// zona 1
		zb = this.getZonaPuntoReferido(x, y);
		if (zb != null) {
			if (e.intersects(zb.getArea())) {
				zonas.add(zb);
			}
		}

		// zona 2
		zb = this.getZonaPuntoReferido(x + ancho, y);
		if (zb != null) {
			if (e.intersects(zb.getArea())) {
				zonas.add(zb);
			}
		}

		// zona 3
		zb = this.getZonaPuntoReferido(x, y + alto);
		if (zb != null) {
			if (e.intersects(zb.getArea())) {
				zonas.add(zb);
			}
		}

		// zona 4
		zb = this.getZonaPuntoReferido(x + ancho, y + alto);
		if (zb != null) {
			if (e.intersects(zb.getArea())) {
				zonas.add(zb);
			}
		}
		return zonas;
	}

	// VER ACA
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

	private int generarCriaturas(final ArrayList<Criatura> criaturas) {
		int cant = 0;
		for (final Criatura c : criaturas) {
			this.meterEntidad(c);
			++cant;
		}
		return cant;
	}

	private void generarZonas() {
		this.ZONAS.clear();
		int x;
		int y;
		final int limiteY = this.ESCENARIO.getTerreno().CANTIDAD_ALTO_GROUPTILE
				* (this.ESCENARIO.getTerreno().LADO_GRUPO_TILE);
		final int limiteX = this.ESCENARIO.getTerreno().CANTIDAD_ANCHO_GROUPTILE
				* (this.ESCENARIO.getTerreno().LADO_GRUPO_TILE);
		for (y = 0; y < limiteY; y += this.LADO_ZONEBOX) {
			for (x = 0; x < limiteX; x += this.LADO_ZONEBOX) {
				this.ZONAS.put(new Point(x / this.LADO_ZONEBOX, y / this.LADO_ZONEBOX),
						new ZoneBox(x, y, this.LADO_ZONEBOX, this.LADO_ZONEBOX, this));
			}
		}

	}

	private ZoneBox getZonaPuntoReferido(final float x, final float y) {
		return this.ZONAS.get(new Point((int) x / this.LADO_ZONEBOX, (int) y / this.LADO_ZONEBOX));
	}

	private ZoneBox getZonaPuntoSinReferir(final int x, final int y) {
		return this.ZONAS.get(new Point(x, y));
	}

	public void updateNextCodAct() {
		if (this.codAct < Integer.MAX_VALUE) {
			this.codAct++;
		} else {
			this.codAct = Integer.MIN_VALUE;
		}
	}

	public void updateNextCodPintado() {
		if (this.codPintado < Integer.MAX_VALUE) {
			this.codPintado++;
		} else {
			this.codPintado = Integer.MIN_VALUE;
		}
	}

	private void pintarParticulas(final Graphics2D g) {
		if (this.PARTICULAS.size() == 0) {
			return;
		}
		for (final Particula p : this.PARTICULAS) {
			p.pintar(g);
		}
	}

	private void pintarProyectiles(final Graphics2D g) {
		if (this.PROYECTILES.isEmpty()) {
			return;
		}

		Proyectil p = null;
		for (int i = 0; i < this.PROYECTILES.size(); i++) {
			p = this.PROYECTILES.get(i);
			p.pintar(g);
		}
	}

	/**
	 * Fuente pre-instanciada reutilizable para la depuración de pesos de IA. Evita
	 * crear objetos 'Font' nuevos con deriveFont() en cada frame de depuración.
	 */
	private static final Font FUENTE_DEBUG_NODOS = new Font(Font.SANS_SERIF, Font.PLAIN, 6);

	/**
	 * Renderiza en pantalla el mapa de calor/distancias de los nodos de navegación
	 * masiva (Dijkstra Flowfield) contenidos dentro del frustum dinámico de la
	 * cámara.
	 * <p>
	 * <b>Pilares de Optimización y Rendimiento:</b>
	 * <ul>
	 * <li><b>Frustum Culling AABB Dinámico:</b> Calcula el área visible compensando
	 * automáticamente la rotación ($\theta$), el temblor ($X, Y$) y el zoom para
	 * que los números de depuración cubran toda la pantalla a cualquier escala sin
	 * cortes.</li>
	 * <li><b>Lectura Lock-Free de Doble Búfer:</b> Lee el estado de la IA de forma
	 * segura mientras el hilo secundario recalcula caminos en paralelo, sin
	 * bloqueos ({@code synchronized}) ni pausas en el hilo de renderizado.</li>
	 * <li><b>Formateo de Texto sin Heap Allocation:</b> Elimina
	 * {@link String#format} y operaciones de reflexión, reduciendo a cero la
	 * presión sobre el Garbage Collector.</li>
	 * <li><b>Salto Discreto en Grilla O(Nodos Visibles):</b> Itera únicamente sobre
	 * las coordenadas alineadas a la dimensión de cada nodo.</li>
	 * </ul>
	 * </p>
	 *
	 * @param g Contexto gráfico {@link Graphics2D}.
	 */
	private void pintarNodosOptimizado(final Graphics2D g) {
		final Font fontOriginal = g.getFont();
		g.setFont(FUENTE_DEBUG_NODOS);

		final Color color = Globales.TECLADO.TECLA_OCULTAR_TERRENO.presionado() ? Color.WHITE : Color.BLACK;

		final int anchoNodo = this.dijkstra.getDimensionNodo().width;
		final int altoNodo = this.dijkstra.getDimensionNodo().height;

		/*
		 * =====================================================================
		 * EXPLICACIÓN DIDÁCTICA: CULLING DE NODOS DE IA CON CÁMARA DINÁMICA
		 * --------------------------------------------------------------------- 1.
		 * COMPENSACIÓN COMPLETA DE CÁMARA: Al igual que en 'Terreno.pintar' y
		 * 'Mundo.pintarZonas', proyectamos el semiancho y semialto de la pantalla
		 * considerando la rotación y el Zoom activo. Si el jugador hace zoom out a
		 * 0.5x, el radio visible se duplica automáticamente para pintar todos los nodos
		 * visibles.
		 * 
		 * 2. PROYECCIÓN CON Math.floorDiv: Alinea las coordenadas continuas de mundo a
		 * múltiplos enteros exactos del tamaño del nodo (ej: 0, 16, 32, 48...),
		 * soportando coordenadas negativas sin desalinear la cuadrícula.
		 * =====================================================================
		 */
		final double zoomActivo = Math.max(0.2, Globales.CAMARA.getZoomFinal());
		final double rotAbs = Math.abs(Globales.CAMARA.getGestorEfectos().getAnguloRotacion());
		final double shakeX = Math.abs(Globales.CAMARA.getGestorEfectos().getOffsetX());
		final double shakeY = Math.abs(Globales.CAMARA.getGestorEfectos().getOffsetY());

		final double cos = Math.cos(rotAbs);
		final double sin = Math.sin(rotAbs);

		// 1. Radio envolvente rotado + margen de seguridad de 3 nodos
		final int radioVisibleX = (int) Math
				.ceil(((Constantes.CENTROX * cos) + (Constantes.CENTROY * sin)) / zoomActivo) + (int) shakeX
				+ (3 * anchoNodo);
		final int radioVisibleY = (int) Math
				.ceil(((Constantes.CENTROX * sin) + (Constantes.CENTROY * cos)) / zoomActivo) + (int) shakeY
				+ (3 * altoNodo);

		final int camX = Globales.CAMARA.getPosicionXInt();
		final int camY = Globales.CAMARA.getPosicionYInt();

		final int minX = camX - radioVisibleX;
		final int maxX = camX + radioVisibleX;
		final int minY = camY - radioVisibleY;
		final int maxY = camY + radioVisibleY;

		// 2. Proyección exacta a múltiplos discretos de la grilla de nodos
		final int inicioX = Math.floorDiv(minX, anchoNodo) * anchoNodo;
		final int finX = Math.floorDiv(maxX, anchoNodo) * anchoNodo;

		final int inicioY = Math.floorDiv(minY, altoNodo) * altoNodo;
		final int finY = Math.floorDiv(maxY, altoNodo) * altoNodo;

		/*
		 * =====================================================================
		 * EXPLICACIÓN DIDÁCTICA: ARQUITECTURA LOCK-FREE DE DOBLE BÚFER
		 * --------------------------------------------------------------------- El
		 * algoritmo de Dijkstra corre en un hilo secundario para no congelar los 60 FPS
		 * del juego.
		 * 
		 * Para que el hilo de dibujo pueda leer las distancias sin chocar con el hilo
		 * que está escribiendo (evitando bloqueos synchronized lentos): - 'readBuf'
		 * indica cuál de los 2 arreglos internos (0 o 1) está listo para leer. -
		 * 'codCompleto' es un código de generación que verifica si el nodo fue
		 * alcanzado por la onda expansiva del camino en el cálculo actual.
		 * =====================================================================
		 */
		final int readBuf = this.dijkstra.getBufferLecturaIndex();
		final int codCompleto = this.dijkstra.getCodActCompleto();

		NodoD nodo = null;

		// 3. Iteración directa paso a paso alineada a la grilla
		for (int y = inicioY; y <= finY; y += altoNodo) {
			for (int x = inicioX; x <= finX; x += anchoNodo) {
				nodo = this.dijkstra.getNodoReferenciado(x, y);

				if (nodo != null) {
					// Si el nodo fue procesado en el pulso actual leemos su distancia; sino es
					// inalcanzable ("XX")
					final double distanciaReal = (nodo.getCodAct(readBuf) == codCompleto) ? nodo.getDistancia(readBuf)
							: Double.MAX_VALUE;

					// Truncado rápido a 1 solo decimal sin usar String.format() (ahorra miles de
					// objetos por segundo)
					final String textoDistancia = (distanciaReal == Double.MAX_VALUE) ? "XX"
							: String.valueOf((long) (distanciaReal * 10) / 10.0);

					DibujoDebug.dibujarStringRefCamara(g, textoDistancia, x, y + 10, color);
				}
			}
		}

		// 4. Restauramos la fuente previa del contexto gráfico
		g.setFont(fontOriginal);
	}

	private void actualizarDijkstra() {
//		System.out.println("Creaturas al pendiente: " + this.dijkstra.hayEntidadesAlPendiente());
		if (Globales.TECLADO.TECLA_DIJKSTRA.presionado() && this.dijkstra.hayEntidadesAlPendiente()) {
			this.dijkstra.actualizar(Globales.JUGADOR.getPosicionParado());
		}
	}

	private void actualizarParticulas() {
		if (this.PARTICULAS.size() == 0) {
			return;
		}
		Particula p = null;
		for (int i = 0; i < this.PARTICULAS.size(); i++) {
			p = this.PARTICULAS.get(i);
			p.actualizar();
			if (p.estaEliminado()) {
				this.PARTICULAS.remove(i);
			}
		}

	}

	private void actualizarProyectiles() {
		if (this.PROYECTILES.size() == 0) {
			return;
		}

		Proyectil p = null;
		for (int i = 0; i < this.PROYECTILES.size(); i++) {
			p = this.PROYECTILES.get(i);
			p.actualizar();
			if (p.estaEliminado()) {
				this.PROYECTILES.remove(i);
			}
		}
	}

	public long getCantEntidadesEnTerreno() {
		return this.RENDERS.getCantEntidades();
	}

	public long getCantEntidadesTotal() {
		return this.RENDERS.getCantEntidades() + this.ESCENARIO.getTerreno().getCantidadTiles();
	}

	public void moverJugadorPuntoComienzo() {
		this.PUNTOS_SPAWN_JUGADOR.get(CLAVE_PUNTO_SPAWN_COMIENZO).moverJugadorCentrado();
	}

	public void agregarParticula(final Particula p) {
		this.PARTICULAS.add(p);
	}

	public void agregarCriatura(final Criatura c) {
		this.meterEntidad(c);
	}

	public Terreno getTerreno() {
		return this.ESCENARIO.getTerreno();
	}

	public int getLadoZoneBox() {
		return this.LADO_ZONEBOX;
	}

	public ArrayList<Criatura> getCriaturasIntersectadasConEnte(final Ente e) {
		final ArrayList<Criatura> criaturas = new ArrayList<Criatura>();
		for (final ZoneBox zb : this.getZonasIntersectadas(e)) {
			for (final Criatura c : zb.getCriaturas()) {
				if (!criaturas.contains(c)) {
					criaturas.add(c);
				}
			}
		}
		return criaturas;
	}

	public boolean hayQueForzarActDijkstra() {
		return this.forzarUnaActualizacionDijkstra;
	}

	public void forzarActDijkstra() {
		if (!this.forzarUnaActualizacionDijkstra) {
			this.forzarUnaActualizacionDijkstra = true;
		}
	}

	public Set<Ente> getEntes() {
		return this.RENDERS.getEntes();
	}

	public DijkstraRework getDijkstra() {
		return this.dijkstra;
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

	public void llenarSpawn(final ArrayList<Spawn> lista) {
		for (final Spawn spawn : lista) {
			this.PUNTOS_SPAWN_JUGADOR.put(spawn.getNombre(), spawn);
		}
	}

	public AEstrella getAEstrellaX12X20() {
		return this.AESTRELLA_X12X20;
	}

	public void eliminarCriaturas() {
		this.RENDERS.eliminarCriaturas();
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
//	final CosaNeutral cosaNeutral = null;
//	final Enemigo enemigo = null;
		Item item = null;
		Cofre cofre = null;
		final JSONObject jsonAux = null;
		for (final Ente e : this.getEntes()) {
			if (e instanceof Criatura) {
//		if (e instanceof Enemigo) {
//		    enemigo = (Enemigo) e;
//		    jsonAux = new JSONObject();
//		    jsonAux.put("tipo", enemigo.exportarTipoCriatura());
//		    jsonAux.put("entiti", enemigo.exportarParaJSON());
//		    listaCriaturas.add(jsonAux);
//		} else if (e instanceof CosaNeutral) {
//		    cosaNeutral = (CosaNeutral) e;
//		    jsonAux = new JSONObject();
//		    jsonAux.put("tipo", cosaNeutral.exportarTipoCriatura());
//		    jsonAux.put("entiti", cosaNeutral.exportarParaJSON());
//		    listaCriaturas.add(jsonAux);
//		}
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
