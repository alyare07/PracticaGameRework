package principal.mapa;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.io.Serializable;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import principal.entes.modelos.tile.ListaModeloTile;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;

/**
 * Representa la superficie del mapa del juego (grilla continua de tiles).
 * <p>
 * <b>ARQUITECTURA DE ALTO RENDIMIENTO (Zero-GC & Cache Locality):</b> En lugar
 * de usar una matriz bidimensional {@code Tile[][]}, este motor almacena todos
 * los tiles en un único arreglo unidimensional {@code Tile[]}. <br>
 * <i>¿Por qué?</i> En Java, una matriz 2D {@code Tile[Y][X]} es en realidad un
 * arreglo de punteros a otros arreglos, dispersos por la memoria RAM. Un
 * arreglo plano {@code Tile[]} garantiza que los datos estén contiguos,
 * permitiendo que la CPU cargue bloques enteros en su memoria Caché L1/L2
 * (Cache Line Prefetching), acelerando drásticamente el render y las colisiones
 * a 60 FPS continuos.
 * </p>
 * 
 * @version 2.0 (Java 8 Compatible)
 */
public class Terreno implements Serializable {

	private static final long serialVersionUID = -230565732234345L;

	/** Ancho total del terreno en píxeles. */
	protected final int ANCHO;

	/** Alto total del terreno en píxeles. */
	protected final int ALTO;

	/** Cantidad de columnas (tiles horizontales) en la grilla. */
	protected final int CANTIDAD_TILES_X;

	/** Cantidad de filas (tiles verticales) en la grilla. */
	protected final int CANTIDAD_TILES_Y;

	/** Tamaño en píxeles del lado de cada celda (generalmente 16 o 32 px). */
	protected final int LADO_TILE;

	/**
	 * Total absoluto de celdas en el terreno (CANTIDAD_TILES_X * CANTIDAD_TILES_Y).
	 */
	protected final long CANT_TILES;

	/**
	 * Arreglo unidimensional que contiene la totalidad de los tiles del mapa.
	 * Acceso matemático directo O(1):
	 * {@code indice = (ty * CANTIDAD_TILES_X) + tx}.
	 */
	protected final Tile[] TILES;

	/**
	 * Constructor para generar un terreno nuevo con un modelo de suelo por defecto
	 * (Tierra).
	 *
	 * @param cantTilesAncho Cantidad de celdas horizontales.
	 * @param cantTilesAlto  Cantidad de celdas verticales.
	 * @param ladoTile       Tamaño de cada celda en píxeles.
	 */
	public Terreno(final int cantTilesAncho, final int cantTilesAlto, final int ladoTile) {
		this(cantTilesAncho, cantTilesAlto, ladoTile, ListaModeloTile.COD_TIERRA);
	}

	/**
	 * Constructor principal para inicializar un terreno completamente vacío con un
	 * tipo de tile específico.
	 *
	 * @param cantTilesAncho Cantidad de celdas horizontales.
	 * @param cantTilesAlto  Cantidad de celdas verticales.
	 * @param ladoTile       Tamaño de cada celda en píxeles.
	 * @param idModeloTile   ID del modelo base con el que se rellenará el mapa.
	 */
	public Terreno(final int cantTilesAncho, final int cantTilesAlto, final int ladoTile, final int idModeloTile) {
		this.LADO_TILE = ladoTile;
		this.CANTIDAD_TILES_X = cantTilesAncho;
		this.CANTIDAD_TILES_Y = cantTilesAlto;
		this.ANCHO = cantTilesAncho * ladoTile;
		this.ALTO = cantTilesAlto * ladoTile;
		this.CANT_TILES = (long) cantTilesAncho * cantTilesAlto;

		this.TILES = new Tile[cantTilesAncho * cantTilesAlto];
		this.llenarVacioTerreno(idModeloTile);
		this.calcularAutotiles();
	}

	/**
	 * Constructor para deserializar un terreno desde un archivo JSON. Incluye
	 * retrocompatibilidad con versiones heredadas (que usaban agrupaciones "GT" o
	 * "GroupTiles").
	 *
	 * @param jso Objeto {@link JSONObject} con la estructura guardada del terreno.
	 */
	public Terreno(final JSONObject jso) {
		this.LADO_TILE = Constantes.LADO_TILE;
		this.ANCHO = ((Number) jso.get("ancho")).intValue();
		this.ALTO = ((Number) jso.get("alto")).intValue();
		this.CANTIDAD_TILES_X = this.ANCHO / this.LADO_TILE;
		this.CANTIDAD_TILES_Y = this.ALTO / this.LADO_TILE;
		this.CANT_TILES = (long) this.CANTIDAD_TILES_X * this.CANTIDAD_TILES_Y;
		this.TILES = new Tile[this.CANTIDAD_TILES_X * this.CANTIDAD_TILES_Y];

		// =====================================================================
		// 1. CARGA FORMATO MODERNO: Lista plana de Tiles
		// =====================================================================
		final Object tilesObj = jso.get("Tiles");
		if (tilesObj instanceof JSONArray) {
			for (final Object o : (JSONArray) tilesObj) {
				if (o instanceof JSONObject) {
					final Tile t = Tile.crearDesdeJson((JSONObject) o);
					// Usamos Math.floorDiv para indexación segura de coordenadas
					final int tx = Math.floorDiv(t.getPosicionX(), this.LADO_TILE);
					final int ty = Math.floorDiv(t.getPosicionY(), this.LADO_TILE);
					if ((tx >= 0) && (tx < this.CANTIDAD_TILES_X) && (ty >= 0) && (ty < this.CANTIDAD_TILES_Y)) {
						this.TILES[(ty * this.CANTIDAD_TILES_X) + tx] = t;
					}
				}
			}
		}
		// =====================================================================
		// 2. RETROCOMPATIBILIDAD: Cargar formato antiguo con "GroupTiles" ("GT")
		// =====================================================================
		else {
			final Object gtObj = jso.get("GT");
			JSONArray listaGT = null;
			if (gtObj instanceof JSONArray) {
				listaGT = (JSONArray) gtObj;
			} else if (gtObj != null) {
				try {
					listaGT = (JSONArray) new JSONParser().parse(gtObj.toString());
				} catch (final ParseException e) {
					e.printStackTrace();
				}
			}

			if (listaGT != null) {
				for (final Object o : listaGT) {
					if (o instanceof JSONObject) {
						final JSONObject gtJson = (JSONObject) o;
						final Object subTiles = gtJson.get("Tiles");
						if (subTiles instanceof JSONArray) {
							for (final Object st : (JSONArray) subTiles) {
								final Tile t = Tile.crearDesdeJson((JSONObject) st);
								final int tx = Math.floorDiv(t.getPosicionX(), this.LADO_TILE);
								final int ty = Math.floorDiv(t.getPosicionY(), this.LADO_TILE);
								if ((tx >= 0) && (tx < this.CANTIDAD_TILES_X) && (ty >= 0)
										&& (ty < this.CANTIDAD_TILES_Y)) {
									this.TILES[(ty * this.CANTIDAD_TILES_X) + tx] = t;
								}
							}
						}
					}
				}
			}
		}

		// Rellenar huecos con suelo por defecto en caso de celdas faltantes en el JSON
		for (int i = 0; i < this.TILES.length; i++) {
			if (this.TILES[i] == null) {
				final int tx = i % this.CANTIDAD_TILES_X;
				final int ty = i / this.CANTIDAD_TILES_X;
				this.TILES[i] = new Tile(tx * this.LADO_TILE, ty * this.LADO_TILE, this.LADO_TILE,
						ListaModeloTile.COD_TIERRA);
			}
		}

		// Recalcular autotiling para conectar todas las texturas leídas
		this.calcularAutotiles();
	}

	/**
	 * Exporta la totalidad de la estructura del terreno a un objeto JSON.
	 *
	 * @return Objeto {@link JSONObject} serializado.
	 */
	@SuppressWarnings("unchecked")
	public JSONObject getTilesJson() {
		final JSONObject terreno = new JSONObject();
		terreno.put("cantTile", Long.valueOf(this.CANT_TILES));
		terreno.put("ladoTile", Integer.valueOf(this.LADO_TILE));
		terreno.put("ancho", Integer.valueOf(this.ANCHO));
		terreno.put("alto", Integer.valueOf(this.ALTO));

		final JSONArray tilesLista = new JSONArray();
		for (int i = 0; i < this.TILES.length; i++) {
			if (this.TILES[i] != null) {
				tilesLista.add(this.TILES[i].exportarParaJSON());
			}
		}
		terreno.put("Tiles", tilesLista);
		return terreno;
	}

	// =========================================================================
	// === AUTOTILING DETERMINISTA Y BITS
	// =========================================================================

	/**
	 * Genera una variación estética (0, 1, 2 o 3) de manera completamente
	 * matemática y determinista.
	 * <p>
	 * <b>EXPLICACIÓN PARA EL DESARROLLADOR:</b> En vez de guardar un número
	 * aleatorio en el archivo para cada flor o piedra en el suelo, usamos una
	 * función Hash con números primos grandes (fórmula tipo MurmurHash / PCG). La
	 * coordenada {@code (gridX, gridY)} y el modelo de tile siempre producirán
	 * exactamente el mismo número. Ahorra cientos de kilobytes en el archivo de
	 * guardado y no gasta CPU.
	 * </p>
	 *
	 * @param gridX    Columna del tile en la grilla.
	 * @param gridY    Fila del tile en la grilla.
	 * @param idModelo Identificador del tipo de tile.
	 * @return Índice de variación cosmética (0 = Común 90%, 1 = Rara 5%, 2 = Muy
	 *         Rara 4%, 3 = Especial 1%).
	 */
	private byte calcularVariacionDeterminista(final int gridX, final int gridY, final int idModelo) {
		int h = (gridX * 374761393) ^ (gridY * 668265263) ^ (idModelo * 3571);
		h = (h ^ (h >>> 13)) * 1274126177;
		final int roll = (h & 0x7FFFFFFF) % 100;

		if (roll < 90) {
			return 0; // 90% de probabilidad: Textura estándar base
		}
		if (roll < 95) {
			return 1; // 5% de probabilidad: Variación leve
		}
		if (roll < 99) {
			return 2; // 4% de probabilidad: Variación media
		}
		return 3; // 1% de probabilidad: Variación única/rara
	}

	/**
	 * Recalcula la máscara de bits (Autotiling) y las variaciones de todos los
	 * tiles del mapa.
	 * <p>
	 * <b>CÓMO FUNCIONA EL AUTOTILING POR BITS (4 BITS = 16 DIRECCIONES):</b>
	 * Evaluamos los 4 vecinos cardinales. Si el vecino tiene el mismo modelo,
	 * sumamos su potencia de 2:
	 * <ul>
	 * <li><b>Norte</b>: +1 (Binario: 0001)</li>
	 * <li><b>Este</b>: +2 (Binario: 0010)</li>
	 * <li><b>Sur</b>: +4 (Binario: 0100)</li>
	 * <li><b>Oeste</b>: +8 (Binario: 1000)</li>
	 * </ul>
	 * Si un tile está rodeado por el Norte y Este, su máscara es 1 + 2 = 3 (0011
	 * binario). Esto permite seleccionar la textura de borde exacta de forma
	 * instantánea.
	 * </p>
	 */
	public void calcularAutotiles() {
		for (int ty = 0; ty < this.CANTIDAD_TILES_Y; ty++) {
			final int fila = ty * this.CANTIDAD_TILES_X;
			for (int tx = 0; tx < this.CANTIDAD_TILES_X; tx++) {
				final Tile tileActual = this.TILES[fila + tx];
				if (tileActual == null) {
					continue;
				}

				final int modelo = tileActual.getCodModelo();
				byte mascara = 0;

				// Norte (Fila superior)
				if ((ty > 0) && (this.TILES[((ty - 1) * this.CANTIDAD_TILES_X) + tx].getCodModelo() == modelo)) {
					mascara += 1;
				}
				// Este (Columna derecha)
				if ((tx < (this.CANTIDAD_TILES_X - 1)) && (this.TILES[fila + tx + 1].getCodModelo() == modelo)) {
					mascara += 2;
				}
				// Sur (Fila inferior)
				if ((ty < (this.CANTIDAD_TILES_Y - 1))
						&& (this.TILES[((ty + 1) * this.CANTIDAD_TILES_X) + tx].getCodModelo() == modelo)) {
					mascara += 4;
				}
				// Oeste (Columna izquierda)
				if ((tx > 0) && (this.TILES[(fila + tx) - 1].getCodModelo() == modelo)) {
					mascara += 8;
				}

				tileActual.setMascaraBit(mascara);
				tileActual.setVariacionPropia(this.calcularVariacionDeterminista(tx, ty, modelo));
			}
		}
	}

	/**
	 * Actualiza el autotiling de un único tile situado en una coordenada del mundo.
	 *
	 * @param worldX Coordenada X absoluta en píxeles.
	 * @param worldY Coordenada Y absoluta en píxeles.
	 */
	public void actualizarAutotile(final int worldX, final int worldY) {
		final int tx = Math.floorDiv(worldX, this.LADO_TILE);
		final int ty = Math.floorDiv(worldY, this.LADO_TILE);
		if ((tx < 0) || (tx >= this.CANTIDAD_TILES_X) || (ty < 0) || (ty >= this.CANTIDAD_TILES_Y)) {
			return;
		}

		final Tile tileActual = this.TILES[(ty * this.CANTIDAD_TILES_X) + tx];
		if (tileActual == null) {
			return;
		}

		final int modelo = tileActual.getCodModelo();
		byte mascara = 0;

		final Tile tN = this.getTileGrid(tx, ty - 1);
		if ((tN != null) && (tN.getCodModelo() == modelo)) {
			mascara += 1;
		}

		final Tile tE = this.getTileGrid(tx + 1, ty);
		if ((tE != null) && (tE.getCodModelo() == modelo)) {
			mascara += 2;
		}

		final Tile tS = this.getTileGrid(tx, ty + 1);
		if ((tS != null) && (tS.getCodModelo() == modelo)) {
			mascara += 4;
		}

		final Tile tO = this.getTileGrid(tx - 1, ty);
		if ((tO != null) && (tO.getCodModelo() == modelo)) {
			mascara += 8;
		}

		tileActual.setMascaraBit(mascara);
		tileActual.setVariacionPropia(this.calcularVariacionDeterminista(tx, ty, modelo));
	}

	/**
	 * Actualiza el autotiling del tile seleccionado y de sus 4 tiles adyacentes (N,
	 * S, E, O). Utilizado durante la edición de mapas cuando el usuario coloca o
	 * borra un bloque.
	 *
	 * @param worldX Coordenada X en píxeles.
	 * @param worldY Coordenada Y en píxeles.
	 */
	public void actualizarAutotileLocal(final int worldX, final int worldY) {
		this.actualizarAutotile(worldX, worldY);
		this.actualizarAutotile(worldX, worldY - this.LADO_TILE);
		this.actualizarAutotile(worldX + this.LADO_TILE, worldY);
		this.actualizarAutotile(worldX, worldY + this.LADO_TILE);
		this.actualizarAutotile(worldX - this.LADO_TILE, worldY);
	}

	// =========================================================================
	// === ACCESO ESPACIAL O(1) DIRECTO
	// =========================================================================

	/**
	 * Obtiene el tile ubicado en la coordenada de la matriz (columna, fila).
	 *
	 * @param tx Índice horizontal (columna).
	 * @param ty Índice vertical (fila).
	 * @return Instancia del {@link Tile} o {@code null} si está fuera de los
	 *         límites.
	 */
	public Tile getTileGrid(final int tx, final int ty) {
		if ((tx < 0) || (tx >= this.CANTIDAD_TILES_X) || (ty < 0) || (ty >= this.CANTIDAD_TILES_Y)) {
			return null;
		}
		return this.TILES[(ty * this.CANTIDAD_TILES_X) + tx];
	}

	/**
	 * Obtiene el tile que se encuentra debajo de una coordenada absoluta del mundo
	 * (en píxeles).
	 *
	 * @param x Coordenada X absoluta en píxeles.
	 * @param y Coordenada Y absoluta en píxeles.
	 * @return El {@link Tile} intersectado o {@code null} si está fuera de los
	 *         límites.
	 */
	public Tile getTileReferenciado(final int x, final int y) {
		return this.getTileGrid(Math.floorDiv(x, this.LADO_TILE), Math.floorDiv(y, this.LADO_TILE));
	}

	/**
	 * Obtiene el tile que se encuentra debajo de un punto en píxeles.
	 *
	 * @param p Punto en coordenadas del mundo.
	 * @return El {@link Tile} o {@code null}.
	 */
	public Tile getTileReferenciado(final Point p) {
		return (p != null) ? this.getTileReferenciado(p.x, p.y) : null;
	}

	/**
	 * Llena la totalidad del terreno con nuevas instancias de tiles del modelo
	 * especificado.
	 *
	 * @param idModeloTile ID del modelo a asignar.
	 */
	public void llenarVacioTerreno(final int idModeloTile) {
		for (int ty = 0; ty < this.CANTIDAD_TILES_Y; ty++) {
			final int fila = ty * this.CANTIDAD_TILES_X;
			for (int tx = 0; tx < this.CANTIDAD_TILES_X; tx++) {
				this.TILES[fila + tx] = new Tile(tx * this.LADO_TILE, ty * this.LADO_TILE, this.LADO_TILE,
						idModeloTile);
			}
		}
	}

	/**
	 * Reemplaza el tile en una coordenada de píxeles específica por un nuevo modelo
	 * y actualiza sus vecinos.
	 *
	 * @param x    Posición X en píxeles.
	 * @param y    Posición Y en píxeles.
	 * @param tile Tile de referencia con el modelo deseado.
	 */
	public void establecerTileReferenciado(final int x, final int y, final Tile tile) {
		if (tile == null) {
			return;
		}
		final int tx = Math.floorDiv(x, this.LADO_TILE);
		final int ty = Math.floorDiv(y, this.LADO_TILE);
		if ((tx >= 0) && (tx < this.CANTIDAD_TILES_X) && (ty >= 0) && (ty < this.CANTIDAD_TILES_Y)) {
			this.TILES[(ty * this.CANTIDAD_TILES_X) + tx] = new Tile(tx * this.LADO_TILE, ty * this.LADO_TILE,
					this.LADO_TILE, tile.getCodModelo());
			this.actualizarAutotileLocal(x, y);
		}
	}

	/**
	 * Reemplaza el tile ubicado en el {@link Point} indicado.
	 *
	 * @param punto Coordenadas del mundo en píxeles.
	 * @param tile  Tile con el modelo a colocar.
	 */
	public void establecerTileReferenciado(final Point punto, final Tile tile) {
		if (punto != null) {
			this.establecerTileReferenciado(punto.x, punto.y, tile);
		}
	}

	/**
	 * @return {@code true} si el punto en píxeles se encuentra dentro de los
	 *         límites del mapa.
	 */
	public boolean contienePuntoTileReferenciado(final int x, final int y) {
		return this.getTileReferenciado(x, y) != null;
	}

	/**
	 * @return {@code true} si el punto se encuentra dentro de los límites del mapa.
	 */
	public boolean contienePuntoTileReferenciado(final Point p) {
		return (p != null) && this.contienePuntoTileReferenciado(p.x, p.y);
	}

	// =========================================================================
	// === RENDERIZADO Y FRUSTUM CULLING DINÁMICO
	// =========================================================================

	/**
	 * Dibuja únicamente los tiles visibles dentro del área de la cámara (Frustum
	 * Culling).
	 * <p>
	 * <b>EXPLICACIÓN MATEMÁTICA DEL CULLING DINÁMICO:</b> Si la cámara tiene
	 * efectos como <i>Zoom out</i> (alejamiento), rotación o <i>Camera Shake</i>
	 * (temblor), la pantalla muestra más tiles de lo normal o se inclina en
	 * diagonal. <br>
	 * Para calcular exactamente el rango de tiles visibles sin pintar todo el mapa:
	 * 1. Proyectamos el rectángulo de pantalla rotado usando trigonometría básica:
	 * {@code (CENTROX * cos + CENTROY * sin) / zoom}. 2. Sumamos los
	 * desplazamientos de temblor (shake) y un margen de seguridad de 1 tile. 3.
	 * Solo iteramos desde {@code startTile} hasta {@code endTile}, logrando 0% de
	 * desperdicio de GPU/CPU.
	 * </p>
	 *
	 * @param g Contexto gráfico de Java2D.
	 */
	public void pintar(final Graphics2D g) {
		final double zoomActivo = Math.max(0.2, Globales.CAMARA.getZoomFinal());
		final double rotAbs = Math.abs(Globales.CAMARA.getGestorEfectos().getAnguloRotacion());
		final double shakeX = Math.abs(Globales.CAMARA.getGestorEfectos().getOffsetX());
		final double shakeY = Math.abs(Globales.CAMARA.getGestorEfectos().getOffsetY());

		final double cos = Math.cos(rotAbs);
		final double sin = Math.sin(rotAbs);

		// Cálculo del radio visible en píxeles considerando rotación, zoom y vibración
		// de cámara
		final int radioVisibleX = (int) Math
				.ceil(((Constantes.CENTROX * cos) + (Constantes.CENTROY * sin)) / zoomActivo) + (int) shakeX
				+ this.LADO_TILE;
		final int radioVisibleY = (int) Math
				.ceil(((Constantes.CENTROX * sin) + (Constantes.CENTROY * cos)) / zoomActivo) + (int) shakeY
				+ this.LADO_TILE;

		final int camX = Globales.CAMARA.getPosicionXInt();
		final int camY = Globales.CAMARA.getPosicionYInt();

		// Convertir límites en píxeles a índices de columnas y filas con límites
		// acotados (Clamping)
		final int startTileX = Math.max(0, Math.floorDiv(camX - radioVisibleX, this.LADO_TILE));
		final int endTileX = Math.min(this.CANTIDAD_TILES_X - 1, Math.floorDiv(camX + radioVisibleX, this.LADO_TILE));
		final int startTileY = Math.max(0, Math.floorDiv(camY - radioVisibleY, this.LADO_TILE));
		final int endTileY = Math.min(this.CANTIDAD_TILES_Y - 1, Math.floorDiv(camY + radioVisibleY, this.LADO_TILE));

		// Bucle de dibujado acotado al frustum visible
		for (int ty = startTileY; ty <= endTileY; ty++) {
			final int fila = ty * this.CANTIDAD_TILES_X;
			for (int tx = startTileX; tx <= endTileX; tx++) {
				final Tile t = this.TILES[fila + tx];
				if (t != null) {
					t.pintar(g);
				}
			}
		}
	}

	// =========================================================================
	// === GESTIÓN DE COLISIONES ESPACIALES (ALGORITMO DE LÍMITES INCLUSIVOS)
	// =========================================================================

	/**
	 * Obtiene la lista de tiles que intersectan con una figura geométrica
	 * arbitraria.
	 * <p>
	 * <b>POR QUÉ USAR {@code ((x + width) - 1)}:</b> Si un objeto mide 16 píxeles
	 * de ancho y empieza en la posición X = 0, ocupa físicamente los píxeles del 0
	 * al 15 (dentro del tile 0). Si hiciéramos {@code (0 + 16) / 16}, daría 1,
	 * haciendo que el motor crea incorrectamente que el objeto está tocando el tile
	 * adyacente. Restar 1 asegura evaluar el último píxel físico real ocupado.
	 * </p>
	 *
	 * @param s Forma geométrica a comprobar.
	 * @return Lista de tiles intersectados.
	 */
	public ArrayList<Tile> getTilesIntersectados(final Shape s) {
		final ArrayList<Tile> lista = new ArrayList<>();
		if (s == null) {
			return lista;
		}

		final Rectangle b = s.getBounds();
		final int minTileX = Math.max(0, Math.floorDiv(b.x, this.LADO_TILE));
		final int maxTileX = Math.min(this.CANTIDAD_TILES_X - 1, Math.floorDiv((b.x + b.width) - 1, this.LADO_TILE));
		final int minTileY = Math.max(0, Math.floorDiv(b.y, this.LADO_TILE));
		final int maxTileY = Math.min(this.CANTIDAD_TILES_Y - 1, Math.floorDiv((b.y + b.height) - 1, this.LADO_TILE));

		for (int ty = minTileY; ty <= maxTileY; ty++) {
			final int fila = ty * this.CANTIDAD_TILES_X;
			for (int tx = minTileX; tx <= maxTileX; tx++) {
				final Tile t = this.TILES[fila + tx];
				if ((t != null) && s.intersects(t.getArea())) {
					lista.add(t);
				}
			}
		}
		return lista;
	}

	/**
	 * Determina si un rectángulo se solapa con algún tile existente en el mapa.
	 *
	 * @param r Rectángulo de comprobación.
	 * @return {@code true} si al menos una celda válida cae dentro del rectángulo.
	 */
	public boolean intersecta(final Rectangle r) {
		if ((r == null) || r.isEmpty()) {
			return false;
		}

		final int minTileX = Math.max(0, Math.floorDiv(r.x, this.LADO_TILE));
		final int maxTileX = Math.min(this.CANTIDAD_TILES_X - 1, Math.floorDiv((r.x + r.width) - 1, this.LADO_TILE));
		final int minTileY = Math.max(0, Math.floorDiv(r.y, this.LADO_TILE));
		final int maxTileY = Math.min(this.CANTIDAD_TILES_Y - 1, Math.floorDiv((r.y + r.height) - 1, this.LADO_TILE));

		for (int ty = minTileY; ty <= maxTileY; ty++) {
			final int fila = ty * this.CANTIDAD_TILES_X;
			for (int tx = minTileX; tx <= maxTileX; tx++) {
				if (this.TILES[fila + tx] != null) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Determina si el área solapa con un tile considerado sólido por los algoritmos
	 * de Pathfinding (incluye obstáculos de mapa y objetos/árboles/cofres sólidos).
	 *
	 * @param area Forma geométrica a evaluar.
	 * @return {@code true} si hay un obstáculo que bloquee el paso para
	 *         Dijkstra/A*.
	 */
	public boolean intersectaSolidoDijkstra(final Shape area) {
		if (area == null) {
			return false;
		}
		final Rectangle b = area.getBounds();

		final int minTileX = Math.max(0, Math.floorDiv(b.x, this.LADO_TILE));
		final int maxTileX = Math.min(this.CANTIDAD_TILES_X - 1, Math.floorDiv((b.x + b.width) - 1, this.LADO_TILE));
		final int minTileY = Math.max(0, Math.floorDiv(b.y, this.LADO_TILE));
		final int maxTileY = Math.min(this.CANTIDAD_TILES_Y - 1, Math.floorDiv((b.y + b.height) - 1, this.LADO_TILE));

		for (int ty = minTileY; ty <= maxTileY; ty++) {
			final int fila = ty * this.CANTIDAD_TILES_X;
			for (int tx = minTileX; tx <= maxTileX; tx++) {
				final Tile t = this.TILES[fila + tx];
				if ((t != null) && t.esSolidoDijkstra() && area.intersects(t.getArea())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Comprueba colisión física precisa con la base del terreno o cualquier objeto
	 * sólido que resida en él.
	 *
	 * @param area Forma geométrica (hitbox).
	 * @return {@code true} si hay colisión.
	 */
	public boolean intersectaAlgoSolido(final Shape area) {
		if (area == null) {
			return false;
		}
		final Rectangle b = area.getBounds();

		final int minTileX = Math.max(0, Math.floorDiv(b.x, this.LADO_TILE));
		final int maxTileX = Math.min(this.CANTIDAD_TILES_X - 1, Math.floorDiv((b.x + b.width) - 1, this.LADO_TILE));
		final int minTileY = Math.max(0, Math.floorDiv(b.y, this.LADO_TILE));
		final int maxTileY = Math.min(this.CANTIDAD_TILES_Y - 1, Math.floorDiv((b.y + b.height) - 1, this.LADO_TILE));

		for (int ty = minTileY; ty <= maxTileY; ty++) {
			final int fila = ty * this.CANTIDAD_TILES_X;
			for (int tx = minTileX; tx <= maxTileX; tx++) {
				final Tile t = this.TILES[fila + tx];
				if ((t != null) && t.hayColisionConAlgoSolido(area)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Comprueba colisión exclusivamente contra la naturaleza base del tile (ignora
	 * objetos colocados encima).
	 *
	 * @param area Forma a evaluar.
	 * @return {@code true} si colisiona con un tile base sólido (ej: pared de roca
	 *         o agua profunda).
	 */
	public boolean intersectaTileSolido(final Shape area) {
		if (area == null) {
			return false;
		}
		final Rectangle b = area.getBounds();

		final int minTileX = Math.max(0, Math.floorDiv(b.x, this.LADO_TILE));
		final int maxTileX = Math.min(this.CANTIDAD_TILES_X - 1, Math.floorDiv((b.x + b.width) - 1, this.LADO_TILE));
		final int minTileY = Math.max(0, Math.floorDiv(b.y, this.LADO_TILE));
		final int maxTileY = Math.min(this.CANTIDAD_TILES_Y - 1, Math.floorDiv((b.y + b.height) - 1, this.LADO_TILE));

		for (int ty = minTileY; ty <= maxTileY; ty++) {
			final int fila = ty * this.CANTIDAD_TILES_X;
			for (int tx = minTileX; tx <= maxTileX; tx++) {
				final Tile t = this.TILES[fila + tx];
				if ((t != null) && t.esSolido() && area.intersects(t.getArea())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Verifica si un rectángulo se encuentra completamente contenido dentro de los
	 * bordes del mapa.
	 *
	 * @param r Rectángulo a evaluar.
	 * @return {@code true} si todo el rectángulo está dentro de los límites del
	 *         terreno.
	 */
	public boolean areaDentroDelTerreno(final Rectangle r) {
		if (r == null) {
			return false;
		}
		return !((r.x < 0) || (r.y < 0) || ((r.x + r.width) > this.ANCHO) || ((r.y + r.height) > this.ALTO));
	}

	/**
	 * Alias de compatibilidad para {@link #areaDentroDelTerreno(Rectangle)}.
	 */
	public boolean AreaDentroDelTerreno(final Rectangle r) {
		return this.areaDentroDelTerreno(r);
	}

	/**
	 * Verifica si un área rectangular está dentro del terreno y sobre una posición
	 * transitable (no sólida).
	 *
	 * @param r Rectángulo a evaluar.
	 * @return {@code true} si es un sector seguro y libre de obstáculos.
	 */
	public boolean areaEnSectorNoSolido(final Rectangle r) {
		if (!this.areaDentroDelTerreno(r)) {
			return false;
		}
		final Tile tile = this.getTileReferenciado(r.x, r.y);
		return (tile == null) || !tile.esSolidoDijkstra();
	}

	// =========================================================================
	// === ACCESORES Y MÉTODOS DE COMPATIBILIDAD
	// =========================================================================

	/**
	 * Retorna una lista con todos los tiles no nulos del terreno. <i>Nota: Úsalo
	 * solo para operaciones puntuales (ej: guardado o inicialización), no dentro
	 * del tick de 60 FPS.</i>
	 *
	 * @return Lista de todos los tiles activos.
	 */
	public ArrayList<Tile> getTILES() {
		final ArrayList<Tile> lista = new ArrayList<>((int) this.CANT_TILES);
		for (int i = 0; i < this.TILES.length; i++) {
			if (this.TILES[i] != null) {
				lista.add(this.TILES[i]);
			}
		}
		return lista;
	}

	public int getAncho() {
		return this.ANCHO;
	}

	public int getAlto() {
		return this.ALTO;
	}

	public int ladoTile() {
		return this.LADO_TILE;
	}

	public int ladoGrupoTile() {
		return this.LADO_TILE * 2;
	}

	public long getCantidadTiles() {
		return this.CANT_TILES;
	}
}