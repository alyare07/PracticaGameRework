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
 * Gestor central del Mundo en el motor RPG 2D.
 * <p>
 * <b>RESPONSABILIDADES ARQUITECTURALES:</b>
 * <ul>
 * <li><b>Particionado Espacial:</b> Grilla contigua 1D de {@link ZoneBox}
 * (64x64 px) para consultas $O(1)$.</li>
 * <li><b>Renderizado por Profundidad (Y-Sorting):</b> Cola dinámica expandible
 * con ordenamiento in-place Zero-GC.</li>
 * <li><b>Inteligencia Artificial y Navegación:</b> Coordinador de grafos
 * {@link DijkstraRework} (multitud) y {@link AEstrella} (preciso).</li>
 * <li><b>Sincronización Lógica vs Gráfica:</b> Generación de códigos únicos de
 * frame ({@code codAct} / {@code codPintado}) para deduplicación de entidades
 * en múltiples zonas.</li>
 * </ul>
 * </p>
 * 
 * @version 3.2 (Java 8 Compatible - Zero-GC Architecture)
 */
public class Mundo {

	/** Escenario que contiene el terreno, capas y metadatos del mapa activo. */
	protected final Escenario ESCENARIO;

	/** Mapa de puntos de aparición (Spawns) guardados por nombre identificador. */
	protected final HashMap<String, Spawn> PUNTOS_SPAWN_JUGADOR = new HashMap<String, Spawn>();

	/**
	 * Dimensión en píxeles del lado de cada cuadrícula de particionado espacial.
	 */
	protected final int LADO_ZONEBOX = 64;

	/**
	 * Bandera para solicitar una recalibración inmediata del mapa de distancias
	 * Dijkstra.
	 */
	private boolean forzarUnaActualizacionDijkstra;

	// =========================================================================
	// === PARTICIONADO ESPACIAL EN MEMORIA CONTIGUA 1D (O(1))
	// =========================================================================

	/** Arreglo plano de celdas espaciales. Acceso: (gy * cantZonasX) + gx. */
	protected ZoneBox[] ZONAS_ARRAY;
	protected int cantZonasX;
	protected int cantZonasY;

	// =========================================================================
	// === COLA DE RENDERIZADO AUTO-EXPANDIBLE CON Y-SORTING (ZERO-GC)
	// =========================================================================

	/** Capacidad inicial del búfer de entidades a dibujar por frame. */
	private static final int CAPACIDAD_INICIAL_COLA = 512;

	/**
	 * Búfer plano reutilizable para ordenar entidades por eje Y sin generar basura
	 * en memoria (Zero-GC).
	 */
	private Ente[] colaRenderEntidades = new Ente[CAPACIDAD_INICIAL_COLA];
	private int cantEntidadesEnCola = 0;

	/**
	 * Set de entidades vivas en el mundo.
	 * <p>
	 * <b>OPTIMIZACIÓN:</b> Se usa un {@link IdentityHashMap} donde la comparación
	 * se hace por igualdad de punteros ({@code ==}) en vez de llamar a
	 * {@code equals()} y {@code hashCode()}. Esto ahorra miles de ciclos de CPU en
	 * cada tick.
	 * </p>
	 */
	protected final Set<Ente> ENTES_REGISTRADOS = Collections.newSetFromMap(new IdentityHashMap<Ente, Boolean>());

	/** Lista de partículas visuales efímeras (humo, chispas, sangre, etc.). */
	protected final ArrayList<Particula> PARTICULAS = new ArrayList<Particula>();

	/** Lista de proyectiles activos (flechas, magia, balas, etc.). */
	protected final ArrayList<Proyectil> PROYECTILES = new ArrayList<Proyectil>();

	/**
	 * Motor de búsqueda masiva hacia el jugador por campo de flujo (Flowfield /
	 * Dijkstra).
	 */
	protected final DijkstraRework dijkstra;

	/**
	 * Motor de búsqueda A* para criaturas que requieren rutas individuales
	 * precisas.
	 */
	protected final AEstrella AESTRELLA_X12X20;

	/**
	 * Código de frame lógico actual (previene doble actualización de entidades en
	 * múltiples zonas).
	 */
	protected int codAct;

	/**
	 * Código de frame de renderizado actual (previene dibujar dos veces la misma
	 * entidad en un frame).
	 */
	protected int codPintado;

	/** Clave por defecto para el punto de spawn inicial del jugador. */
	public static final String CLAVE_PUNTO_SPAWN_COMIENZO = "Comienzo";

	// =========================================================================
	// === CONSTRUCTORES
	// =========================================================================

	/**
	 * Constructor estándar para inicializar un Mundo con su escenario y spawn
	 * principal.
	 *
	 * @param esc      Escenario cargado.
	 * @param comienzo Coordenada del punto de spawn de partida.
	 */
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

	/**
	 * Constructor para inicializar el Mundo actualizando una barra de progreso en
	 * la pantalla de carga.
	 *
	 * @param esc             Escenario cargado.
	 * @param comienzo        Coordenada inicial de spawn.
	 * @param gc              Gestor de carga con la barra de progreso visual.
	 * @param porcentajeCarga Porcentaje asignado a esta etapa.
	 */
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

	/**
	 * Constructor ligero utilizado exclusivamente por el Editor de Mapas.
	 *
	 * @param terrenoSoloParaEDITOR Terreno base sobre el que se trabajará.
	 */
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

	/**
	 * Crea las celdas espaciales {@link ZoneBox} necesarias para cubrir el tamaño
	 * total del terreno.
	 */
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

	/**
	 * Obtiene la celda {@link ZoneBox} situada en la columna y fila dadas de la
	 * grilla.
	 *
	 * @param gx Columna en la grilla espacial.
	 * @param gy Fila en la grilla espacial.
	 * @return La instancia {@link ZoneBox} correspondiente, o {@code null} si está
	 *         fuera de rango.
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

	/**
	 * Ejecuta la actualización lógica del mundo a 60 ticks por segundo.
	 */
	public void actualizar() {
		this.actualizarDijkstra();
		this.actualizarZonas();
		this.actualizarParticulas();
		this.actualizarProyectiles();
		this.updateNextCodAct();
	}

	/**
	 * Renderiza el mundo completo en capas con Frustum Culling y Y-Sorting.
	 *
	 * @param g Contexto gráfico de Java 2D.
	 */
	public void pintar(final Graphics2D g) {
		// 1. Capa inferior: Suelo / Terreno
		this.ESCENARIO.getTerreno().pintar(g);

		// 2. Partículas debajo de entidades (efectos de suelo)
		this.pintarParticulas(g);

		// 3. Entidades, complementos y jugador ordenados en profundidad por su eje Y
		this.pintarZonas(g);

		// 4. Capa superior: Proyectiles en el aire
		this.pintarProyectiles(g);

		// 5. Capa de Depuración (si la tecla de debug está pulsada)
		if (Globales.TECLADO.TECLA_DIJKSTRA_INFO.presionado()) {
			this.pintarNodosOptimizado(g);
		}

		// Incrementar el token de frame para la siguiente pasada
		this.updateNextCodPintado();
	}

	/**
	 * Renderiza las celdas espaciales y ordena todas las entidades visibles por
	 * profundidad Y.
	 * <p>
	 * <b>EXPLICACIÓN DEL Y-SORTING Y ZERO-GC:</b> En los juegos 2D con vista
	 * cenital, los personajes que están más abajo en la pantalla (mayor Y) deben
	 * tapar visualmente a los que están más arriba. <br>
	 * 1. En lugar de usar {@code ArrayList.sort()} con lambdas (lo que crearía
	 * objetos basura y saturaría el GC), recolectamos las entidades en un arreglo
	 * plano pre-asignado {@code colaRenderEntidades}. 2. Aplicamos <b>Insertion
	 * Sort</b> in-place. Como las entidades se mueven pocos píxeles por frame, la
	 * lista ya viene casi ordenada del frame anterior, logrando que el ordenamiento
	 * corra en tiempo $O(N)$ casi puro. 3. Dibujamos en orden y limpiamos el
	 * arreglo asignando {@code null} para no retener objetos en RAM.
	 * </p>
	 *
	 * @param g Contexto gráfico.
	 */
	protected void pintarZonas(final Graphics2D g) {
		if ((this.ZONAS_ARRAY == null) || (this.ZONAS_ARRAY.length == 0)) {
			return;
		}

		// Reiniciamos el contador de la cola (sin instanciar arreglos nuevos)
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

		// 1. PASADA 1: Dibujar ítems del suelo y recolectar entidades para el
		// ordenamiento
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

		// 2. Incluir al Jugador para que participe del ordenamiento de profundidad
		if (!Globales.JUGADOR.estaEliminado() && !Globales.isEstadoEditor()) {
			this.agregarAColaRender(Globales.JUGADOR);
		}

		// 3. ORDENAMIENTO POR INSERCIÓN IN-PLACE (O(N) sobre listas casi ordenadas,
		// Zero-GC)
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

		// 4. PASADA 2: Dibujar todas las entidades en orden de profundidad perfecto
		for (int i = 0; i < this.cantEntidadesEnCola; i++) {
			this.colaRenderEntidades[i].pintar(g);
			this.colaRenderEntidades[i] = null; // Liberamos referencia para evitar Memory Leaks
		}
	}

	/**
	 * Actualiza la lógica de las celdas espaciales y entidades dentro del radio
	 * activo de simulación.
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

		// Margen adicional de 2 zonas para que los enemigos no se "congelen" en el
		// borde visible
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

	/**
	 * Agrega una entidad a la cola de renderizado del frame actual. Si la cola se
	 * llena, duplica su tamaño automáticamente usando {@link System#arraycopy}.
	 *
	 * @param e Entidad a dibujar en este frame.
	 */
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

	/**
	 * Inserta y vincula una entidad en el mundo y en todas las {@link ZoneBox} que
	 * intersecta.
	 *
	 * @param e Entidad a ingresar.
	 * @return {@code true} si la entidad fue agregada con éxito.
	 */
	public boolean meterEntidad(final Ente e) {
		if ((e == null) || this.ENTES_REGISTRADOS.contains(e)) {
			return false;
		}
		if (!this.getTerreno().AreaDentroDelTerreno(e.getArea())) {
			return false;
		}

		// Obtenemos las zonas que solapa
		final ArrayList<ZoneBox> zonas = this.getZonasIntersectadas(e);
		if (zonas.isEmpty()) {
			return false;
		}

		// 1. Asignar mundo y registrar bidireccionalmente
		e.setMundo(this);
		final int totalZonas = zonas.size();
		for (int i = 0; i < totalZonas; i++) {
			final ZoneBox zb = zonas.get(i);
			zb.addEntidad(e);
			e.getZonasOcupadas().add(zb);
		}

		// 2. Registrar en el set de identidades
		this.ENTES_REGISTRADOS.add(e);

		// 3. Registrar colisión sólida en los tiles subyacentes si corresponde
		if ((e instanceof Objeto) && ((Objeto) e).esSolido()) {
			this.objetoSolidoVerificarTile((Objeto) e);
		}

		// 4. Depuración en modo editor
		if (Globales.isEstadoEditor()) {
			System.out.println(
					"Entidad " + e + " agregada en x: " + e.getPosicionXInt() + " , y: " + e.getPosicionYInt());
		}

		return true;
	}

	/**
	 * Vincula un objeto sólido a los tiles correspondientes para que los algoritmos
	 * de pathfinding detecten el bloqueo de paso.
	 */
	private void objetoSolidoVerificarTile(final Objeto obj) {
		if (obj instanceof Complemento) {
			final Complemento c = (Complemento) obj;
			final Object modelo = ListaModeloComplemento.getModeloComplemento(c.getCodigoModelo());

			if (modelo instanceof ModeloComplementoT1) {
				this.objetoSolidoVerificarTileByArea(c,
						c.getAreaInterseccionEnBaseMargen(((ModeloComplementoT1) modelo).getMargenesInterseccion()));
			} else if (modelo instanceof ModeloComplementoT2) {
				for (final Rectangle margen : ((ModeloComplementoT2) modelo).getMargenesInterseccion()) {
					this.objetoSolidoVerificarTileByArea(c, c.getAreaInterseccionEnBaseMargen(margen));
				}
			}
		} else {
			this.objetoSolidoVerificarTileByArea(obj, obj.getArea());
		}
	}

	/**
	 * Asocia un objeto a los tiles que quedan cubiertos por su área de impacto.
	 */
	private void objetoSolidoVerificarTileByArea(final Objeto obj, final Rectangle area) {
		if (area == null) {
			return;
		}
		final int ladoTile = this.getTerreno().ladoTile();
		final int minTX = Math.max(0, Math.floorDiv(area.x, ladoTile));
		final int maxTX = Math.min(this.getTerreno().CANTIDAD_TILES_X - 1,
				Math.floorDiv((area.x + area.width) - 1, ladoTile));
		final int minTY = Math.max(0, Math.floorDiv(area.y, ladoTile));
		final int maxTY = Math.min(this.getTerreno().CANTIDAD_TILES_Y - 1,
				Math.floorDiv((area.y + area.height) - 1, ladoTile));

		for (int ty = minTY; ty <= maxTY; ty++) {
			for (int tx = minTX; tx <= maxTX; tx++) {
				final Tile t = this.getTerreno().getTileGrid(tx, ty);
				if (t != null) {
					t.meterObjetoSolido(obj);
				}
			}
		}
	}

	// =========================================================================
	// === MÉTODOS DE BÚSQUEDA Y CONSULTA ESPACIAL (ZERO-GC)
	// =========================================================================

	public ArrayList<ZoneBox> getZonasIntersectadas(final Ente e) {
		return (e != null) ? this.getZonasIntersectadas(e.getArea()) : new ArrayList<ZoneBox>(0);
	}

	/**
	 * Obtiene la lista de {@link ZoneBox} cubiertas por una figura geométrica.
	 * Corrige matemáticamente los bordes usando la regla inclusiva {@code - 1}.
	 *
	 * @param s Forma geométrica a evaluar.
	 * @return Lista de celdas espaciales intersectadas.
	 */
	public ArrayList<ZoneBox> getZonasIntersectadas(final Shape s) {
		final ArrayList<ZoneBox> lista = new ArrayList<ZoneBox>(4);
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
		final HashSet<Item> lista = new HashSet<Item>();
		if ((area == null) || !this.getTerreno().AreaDentroDelTerreno(area.getBounds())) {
			return lista;
		}
		final ArrayList<ZoneBox> zonas = this.getZonasIntersectadas(area);
		final int totalZonas = zonas.size();
		for (int i = 0; i < totalZonas; i++) {
			lista.addAll(zonas.get(i).getItemsIntersectados(area));
		}
		return lista;
	}

	public ArrayList<Criatura> getCriaturasIntersectadas(final Shape area, final boolean tenerEnCuentaJugador) {
		final ArrayList<Criatura> lista = new ArrayList<Criatura>();
		if ((area == null) || !this.getTerreno().AreaDentroDelTerreno(area.getBounds())) {
			return lista;
		}
		if (tenerEnCuentaJugador && area.intersects(Globales.JUGADOR.getArea())) {
			lista.add(Globales.JUGADOR);
		}

		final ArrayList<ZoneBox> zonas = this.getZonasIntersectadas(area);
		final int totalZonas = zonas.size();
		for (int i = 0; i < totalZonas; i++) {
			lista.addAll(zonas.get(i).getCriaturasIntersectadas(area));
		}

		return lista;
	}

	public ArrayList<Ente> getEnteIntersectados(final Shape area, final boolean tenerEnCuentaJugador) {
		final ArrayList<Ente> lista = new ArrayList<Ente>();
		if ((area == null) || !this.getTerreno().AreaDentroDelTerreno(area.getBounds())) {
			return lista;
		}
		if (tenerEnCuentaJugador && area.intersects(Globales.JUGADOR.getArea())) {
			lista.add(Globales.JUGADOR);
		}

		final ArrayList<ZoneBox> zonas = this.getZonasIntersectadas(area);
		final int totalZonas = zonas.size();
		for (int i = 0; i < totalZonas; i++) {
			lista.addAll(zonas.get(i).getEntesIntersectados(area));
		}

		final int totalProyectiles = this.PROYECTILES.size();
		for (int i = 0; i < totalProyectiles; i++) {
			final Proyectil p = this.PROYECTILES.get(i);
			if (area.intersects(p.getArea())) {
				lista.add(p);
			}
		}

		return lista;
	}

	public ArrayList<Criatura> getCriaturasIntersectadasConEnte(final Ente e) {
		final ArrayList<Criatura> criaturas = new ArrayList<Criatura>();
		if (e == null) {
			return criaturas;
		}
		final ArrayList<ZoneBox> zonas = this.getZonasIntersectadas(e);
		final int totalZonas = zonas.size();

		for (int z = 0; z < totalZonas; z++) {
			final ArrayList<Criatura> criatZona = zonas.get(z).getCriaturas();
			final int totalCriat = criatZona.size();
			for (int i = 0; i < totalCriat; i++) {
				final Criatura c = criatZona.get(i);
				if (!criaturas.contains(c)) {
					criaturas.add(c);
				}
			}
		}
		return criaturas;
	}

	public boolean intersectaAlgunaCriatura(final Shape area, final boolean tenerEnCuentaJugador) {
		if ((area == null) || !this.getTerreno().AreaDentroDelTerreno(area.getBounds())) {
			return false;
		}
		if (tenerEnCuentaJugador && area.intersects(Globales.JUGADOR.getArea())) {
			return true;
		}

		final ArrayList<ZoneBox> zonas = this.getZonasIntersectadas(area);
		final int totalZonas = zonas.size();
		for (int i = 0; i < totalZonas; i++) {
			if (zonas.get(i).intersectaAlgunaCriatura(area)) {
				return true;
			}
		}

		return false;
	}

	public boolean colisionaConZonaUObjetoSolido(final Shape area) {
		return this.getTerreno().intersectaTileSolido(area) || this.colisionaConObjetoSolido(area);
	}

	public boolean colisionaConObjetoSolido(final Shape area) {
		if ((area == null) || !this.getTerreno().AreaDentroDelTerreno(area.getBounds())) {
			return false;
		}

		final ArrayList<ZoneBox> zonas = this.getZonasIntersectadas(area);
		final int totalZonas = zonas.size();
		for (int i = 0; i < totalZonas; i++) {
			if (zonas.get(i).intersectaObjetoSolido(area)) {
				return true;
			}
		}

		return false;
	}

	public boolean colisionaConAlgoSolidoPermanente(final Shape area) {
		if (area == null) {
			return false;
		}
		if (this.getTerreno().intersectaSolidoDijkstra(area.getBounds())) {
			return true;
		}

		final ArrayList<ZoneBox> zonas = this.getZonasIntersectadas(area);
		final int totalZonas = zonas.size();
		for (int i = 0; i < totalZonas; i++) {
			if (zonas.get(i).intersectaObjetoSolidoPermanente(area)) {
				return true;
			}
		}
		return false;
	}

	public boolean colisionaConObjetoSolidoPeroEnZonaNoSolida(final Shape area) {
		if ((area == null) || !this.getTerreno().AreaDentroDelTerreno(area.getBounds())) {
			return false;
		}

		final ArrayList<ZoneBox> zonas = this.getZonasIntersectadas(area);
		final int totalZonas = zonas.size();
		for (int i = 0; i < totalZonas; i++) {
			if (zonas.get(i).intersectaAreaNoSolidaDeAlgunComplemento(area)) {
				return true;
			}
		}
		return false;
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

	// =========================================================================
	// === GESTIÓN DE PARTÍCULAS Y PROYECTILES (BUCLES INVERTIDOS SEGUROS)
	// =========================================================================

	/**
	 * Actualiza las partículas recorriendo de atrás hacia adelante.
	 * <p>
	 * <b>POR QUÉ UN BUCLE INVERTIDO:</b> Al eliminar un elemento de un
	 * {@link ArrayList} en un bucle tradicional (de 0 a N), todos los elementos
	 * subsiguientes se desplazan a la izquierda, provocando que el siguiente
	 * elemento se salte sin actualizar. Recorrer de {@code size - 1} a {@code 0}
	 * evita ese problema sin generar objetos {@link Iterator}.
	 * </p>
	 */
	private void actualizarParticulas() {
		for (int i = this.PARTICULAS.size() - 1; i >= 0; i--) {
			final Particula p = this.PARTICULAS.get(i);
			p.actualizar();
			if (p.estaEliminado()) {
				this.PARTICULAS.remove(i);
			}
		}
	}

	/**
	 * Actualiza los proyectiles activos con bucle invertido seguro.
	 */
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
		final int total = this.PARTICULAS.size();
		for (int i = 0; i < total; i++) {
			this.PARTICULAS.get(i).pintar(g);
		}
	}

	private void pintarProyectiles(final Graphics2D g) {
		final int total = this.PROYECTILES.size();
		for (int i = 0; i < total; i++) {
			this.PROYECTILES.get(i).pintar(g);
		}
	}

	public void agregarParticula(final Particula p) {
		if (p != null) {
			this.PARTICULAS.add(p);
		}
	}

	public void crearProyectil(final int damage, final double velocidad, final boolean penetrante, final int alcance,
			final double x, final double y, final int ancho, final int alto, final Direccion direccion,
			final Criatura causante) {
		this.PROYECTILES.add(new ProyectilGeneral(damage, velocidad, penetrante, alcance, this, x, y, ancho, alto,
				direccion, causante));
	}

	public void crearProyectil(final Proyectil proyectil) {
		if (proyectil != null) {
			this.PROYECTILES.add(proyectil);
		}
	}

	// =========================================================================
	// === NAVEGACIÓN E INTELIGENCIA ARTIFICIAL (DIJKSTRA & A*)
	// =========================================================================

	private static final Font FUENTE_DEBUG_NODOS = new Font(Font.SANS_SERIF, Font.PLAIN, 6);

	/**
	 * Dibuja los valores numéricos de distancia del mapa de calor de Dijkstra para
	 * depuración visual.
	 */
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

	/**
	 * Actualiza el grafo Dijkstra hacia la posición del jugador si hay criaturas
	 * activas requiriendo ruta.
	 */
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
	// === ACCESORES, CICLOS DE VIDA Y SERIALIZACIÓN JSON
	// =========================================================================

	/**
	 * Incrementa el token de ciclo lógico. Si alcanza {@link Integer#MAX_VALUE},
	 * reinicia a {@link Integer#MIN_VALUE} sin desbordamiento destructivo.
	 */
	public void updateNextCodAct() {
		this.codAct = (this.codAct < Integer.MAX_VALUE) ? this.codAct + 1 : Integer.MIN_VALUE;
	}

	/**
	 * Incrementa el token de ciclo de renderizado.
	 */
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

	/**
	 * Elimina todas las criaturas vivas del mundo de forma segura y desvincula sus
	 * recursos.
	 */
	public void eliminarCriaturas() {
		final Iterator<Ente> it = this.ENTES_REGISTRADOS.iterator();
		while (it.hasNext()) {
			final Ente e = it.next();
			if (e instanceof Criatura) {
				e.eliminar(); // Apaga luces y desvincula de las ZoneBox
				it.remove(); // Remueve del set de forma segura
			}
		}
	}

	public void agregarCriatura(final Criatura c) {
		this.meterEntidad(c);
	}

	private int generarCriaturas(final ArrayList<Criatura> criaturas) {
		int cant = 0;
		final int total = criaturas.size();
		for (int i = 0; i < total; i++) {
			this.meterEntidad(criaturas.get(i));
			cant++;
		}
		return cant;
	}

	/**
	 * Remueve y desvincula una entidad individual del mundo y de sus zonas.
	 *
	 * @param e Entidad a remover.
	 */
	public void eliminarEntidad(final Ente e) {
		if ((e != null) && this.ENTES_REGISTRADOS.remove(e)) {
			e.eliminar(); // Apaga luces y limpia referencias en ZoneBox
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
			if (e instanceof Criatura) {
				// Serialización opcional de criaturas
			} else if (e instanceof Complemento) {
				listaComplementos.add(((Complemento) e).exportarParaJSON());
			} else if (e instanceof Item) {
				listaItems.add(((Item) e).getJsonItem());
			} else if (e instanceof Cofre) {
				listaObjetos.add(((Cofre) e).exportarParaJson());
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