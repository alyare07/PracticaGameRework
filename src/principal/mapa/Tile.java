package principal.mapa;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;

import org.json.simple.JSONObject;

import principal.entes.modelos.tile.ListaModeloTile;
import principal.entes.modelos.tile.ModeloTile;
import principal.entes.objetos.Complemento;
import principal.entes.objetos.Objeto;
import principal.maquinaestado.estados.editor.PaletaComplento;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.Globales;
import principal.utilidades.Textura;

/**
 * Representa una celda individual (Tile) dentro de la grilla espacial del mapa.
 * <p>
 * <b>Patrón de Diseño Flyweight (Peso Ligero):</b> En un mapa con miles de
 * tiles, guardar la imagen, propiedades de colisión completas y animaciones en
 * cada celda consumiría gigabytes de RAM. Por ello, la clase {@code Tile} solo
 * guarda sus coordenadas espaciales y un identificador
 * {@code CODIGO_MODELO_TILE}. La información pesada se consulta en tiempo real
 * a {@link ModeloTile}.
 * </p>
 * 
 * @version 2.0 (Java 8 Compatible - Zero-GC Friendly)
 */
public class Tile implements Serializable {

	private static final long serialVersionUID = -445324235886L;

	/** Dimensión del lado del tile en píxeles (ancho y alto). */
	protected final int LADO;

	/** Posición espacial X absoluta en píxeles dentro del Mundo. */
	protected final int X;

	/** Posición espacial Y absoluta en píxeles dentro del Mundo. */
	protected final int Y;

	/**
	 * Rectángulo inmutable que define los límites espaciales del tile. Se crea una
	 * única vez en el constructor para evitar instanciar 'new Rectangle()' durante
	 * las comprobaciones continuas de colisión en el Game Loop.
	 */
	protected final Rectangle AREA;

	/** Identificador del tipo de tile (ej: Pasto, Agua, Roca, Muro). */
	protected final int CODIGO_MODELO_TILE;

	/**
	 * Lista de objetos o complementos sólidos presentes sobre este tile.
	 * <p>
	 * <b>OPTIMIZACIÓN DE MEMORIA (Lazy Initialization):</b> Más del 80% de los
	 * tiles del mapa suelen ser transitables y estar vacíos. Mantener la lista como
	 * {@code null} en lugar de instanciar {@code new ArrayList<>()} ahorra cientos
	 * de megabytes de RAM en mapas extensos. Solo se crea memoria si un objeto
	 * sólido realmente entra al tile.
	 * </p>
	 */
	protected ArrayList<Objeto> objetosSolidos = null;

	/**
	 * Código del modelo del fondo si el tile utiliza una capa inferior (ej: arena
	 * debajo de pasto).
	 */
	protected int codigoModeloFondo = 0;

	/**
	 * Máscara de bits (Autotiling / Bitmasking). Permite determinar de forma
	 * matemática qué bordes o esquinas deben conectarse con tiles vecinos del mismo
	 * tipo (valores del 0 al 255 codificados en 8 bits).
	 */
	protected byte mascaraBit = 0;

	/**
	 * Índice de variación cosmética aleatoria (ej: flor en el pasto, grieta en la
	 * piedra). Usar un 'byte' en vez de 'int' ahorra 3 bytes por cada tile del
	 * mapa.
	 */
	protected byte variacionPropia = 0;

	/**
	 * Constructor principal para un Tile de la grilla.
	 *
	 * @param x                Posición X en píxeles.
	 * @param y                Posición Y en píxeles.
	 * @param lado             Tamaño del lado en píxeles (generalmente 32 o 64 px).
	 * @param codigoModeloTile ID del modelo base que define la apariencia y
	 *                         comportamiento.
	 */
	public Tile(final int x, final int y, final int lado, final int codigoModeloTile) {
		this.X = x;
		this.Y = y;
		this.LADO = lado;
		this.CODIGO_MODELO_TILE = codigoModeloTile;
		this.AREA = new Rectangle(x, y, lado, lado);
	}

	// =========================================================================
	// === MÉTODOS DE RENDERIZADO (DIBUJADO)
	// =========================================================================

	/**
	 * Dibuja las capas que componen el aspecto visual del Tile: 1. Capa de Fondo
	 * (si existe una transición o capa base). 2. Capa Principal (la textura
	 * calculada con su máscara de autotiling y variación).
	 *
	 * @param g Contexto gráfico de Java AWT/Graphics2D.
	 */
	private void pintarCapas(final Graphics2D g) {
		// 1. Capa Fondo (ejemplo: fondo de tierra debajo de una capa de pasto
		// transparente)
		if (this.codigoModeloFondo != 0) {
			final ModeloTile modeloFondo = ListaModeloTile.getModelo(this.codigoModeloFondo);
			if (modeloFondo != null) {
				final int texFondo = modeloFondo.getCodTextura(this.mascaraBit, this.variacionPropia);
				DibujoDebug.dibujarImagenRefCamara(g, Textura.getTextura(texFondo), this.X, this.Y);
			}
		}

		// 2. Capa Principal
		final ModeloTile modelo = ListaModeloTile.getModelo(this.CODIGO_MODELO_TILE);
		if (modelo != null) {
			final int texturaFinal = modelo.getCodTextura(this.mascaraBit, this.variacionPropia);
			DibujoDebug.dibujarImagenRefCamara(g, Textura.getTextura(texturaFinal), this.X, this.Y);
		}
	}

	/**
	 * Dibuja el tile durante el ciclo de juego normal aplicando coordenadas de
	 * cámara.
	 *
	 * @param g Contexto gráfico.
	 */
	public void pintar(final Graphics2D g) {
		// Verificamos si el usuario desactivó visualmente el terreno para depuración
		if (!Globales.TECLADO.TECLA_OCULTAR_TERRENO.presionado()) {
			this.pintarCapas(g);
		}

		// Dibuja la cuadrícula de depuración si la tecla de debug está activa
		if (Globales.TECLADO.TECLA_DEBUG_TILE.presionado() && Globales.estadoJuego) {
			DibujoDebug.dibujarImagenRefCamara(g, Textura.getTextura(Textura.idTexturaContornoTile), this.X, this.Y);
		}
	}

	/**
	 * Dibuja el tile dentro del entorno del Editor de Mapas.
	 *
	 * @param g Contexto gráfico.
	 */
	public void pintarEditor(final Graphics2D g) {
		this.pintarCapas(g);
		if (Globales.TECLADO.TECLA_DEBUG_TILE.presionado()) {
			DibujoDebug.dibujarImagenRefCamara(g, Textura.getTextura(Textura.idTexturaContornoTile), this.X, this.Y);
		}
	}

	/**
	 * Dibuja el tile en la paleta de herramientas de selección del Editor (sin
	 * referencia a cámara).
	 *
	 * @param g Contexto gráfico.
	 */
	public void pintarPaleta(final Graphics2D g) {
		DibujoDebug.dibujarImagen(g, this.getTexturaImagen(), this.X, this.Y);
		DibujoDebug.dibujarImagen(g, Textura.getTextura(Textura.idTexturaContornoTile), this.X, this.Y);
	}

	/**
	 * Dibuja un rectángulo coloreado en el perímetro del tile para depuración
	 * visual de áreas.
	 *
	 * @param g     Contexto gráfico.
	 * @param color Color del contorno.
	 */
	public void pintarContorno(final Graphics2D g, final Color color) {
		DibujoDebug.dibujarRectanguloContornoRefCamara(g, this.X, this.Y, this.LADO, this.LADO, color);
	}

	// =========================================================================
	// === GESTIÓN DE OBJETOS SÓLIDOS Y COLISIONES
	// =========================================================================

	/**
	 * Registra un objeto sólido dentro de este tile si sus límites espaciales se
	 * intersectan.
	 *
	 * @param obj Objeto u obstáculo a registrar.
	 */
	public void meterObjetoSolido(final Objeto obj) {
		if ((obj == null) || (obj.getArea() == null)) {
			return;
		}

		// Comprobación preliminar de colisión por caja delimitadora (AABB)
		if (obj.getArea().intersects(this.AREA)) {
			// Si es un complemento con colisión precisa, validamos su forma exacta
			if (obj instanceof Complemento) {
				final Complemento c = (Complemento) obj;
				if (!c.intersecta(this.AREA)) {
					return;
				}
			}

			// Inicialización perezosa: reservamos memoria solo cuando se necesita
			if (this.objetosSolidos == null) {
				this.objetosSolidos = new ArrayList<>(2);
			}

			if (!this.objetosSolidos.contains(obj)) {
				this.objetosSolidos.add(obj);
			}
		}
	}

	/**
	 * Remueve un objeto sólido del tile. Si la lista queda vacía, se libera de la
	 * memoria regresándola a {@code null}.
	 *
	 * @param obj Objeto a remover.
	 */
	public void sacarObjetoSolido(final Objeto obj) {
		if ((this.objetosSolidos != null) && (obj != null)) {
			this.objetosSolidos.remove(obj);
			if (this.objetosSolidos.isEmpty()) {
				this.objetosSolidos = null; // Liberamos la referencia para ahorro de RAM
			}
		}
	}

	/**
	 * @return Cantidad de objetos sólidos registrados en esta celda.
	 */
	public int getCantObjetosSolidos() {
		return (this.objetosSolidos != null) ? this.objetosSolidos.size() : 0;
	}

	/**
	 * Elimina todos los objetos sólidos registrados y libera la lista interna.
	 */
	public void limpiarObjetosSolidos() {
		this.objetosSolidos = null;
	}

	/**
	 * @return {@code true} si el tile contiene al menos un objeto sólido
	 *         registrado.
	 */
	public boolean contieneObjetosSolidos() {
		return (this.objetosSolidos != null) && !this.objetosSolidos.isEmpty();
	}

	/**
	 * Determina si el tile es intransitable para los algoritmos de Pathfinding
	 * (Dijkstra y A*). Una celda es intransitable si su modelo base es un obstáculo
	 * (ej: Agua profunda, Pared) o si tiene objetos/estructuras encima que impidan
	 * el paso.
	 *
	 * @return {@code true} si no se puede caminar a través de este tile.
	 */
	public boolean esSolidoDijkstra() {
		if (this.getEstado() == ModeloTile.ESTADO_OBSTACULO) {
			return true;
		}
		return this.contieneObjetosSolidos();
	}

	/**
	 * Determina si la naturaleza intrínseca del tile es sólida (sin contar objetos
	 * externos).
	 *
	 * @return {@code true} si el modelo base del tile está marcado como obstáculo.
	 */
	public boolean esSolido() {
		return this.getEstado() == ModeloTile.ESTADO_OBSTACULO;
	}

	/**
	 * Comprueba si una forma geométrica (hitbox de criatura, ataque o proyectil)
	 * colisiona con el tile o con alguno de los objetos sólidos presentes en él.
	 * <p>
	 * <b>OPTIMIZACIÓN ZERO-GC:</b> Se utiliza un bucle {@code for} indexado
	 * estándar con {@code get(i)} en lugar de un {@code for-each}. El
	 * {@code for-each} crearía un objeto {@link java.util.Iterator} en memoria en
	 * cada llamada, saturando el Garbage Collector a 60 FPS.
	 * </p>
	 *
	 * @param s Forma geométrica a evaluar (Rectangle, Polygon, Area, etc.).
	 * @return {@code true} si existe colisión física.
	 */
	public boolean hayColisionConAlgoSolido(final Shape s) {
		if (s == null) {
			return false;
		}

		// Si el tile mismo es sólido, colisiona inmediatamente
		if (this.esSolido()) {
			return true;
		}

		// Si hay objetos registrados, recorremos uno a uno sin generar iteradores
		if (this.objetosSolidos != null) {
			final int total = this.objetosSolidos.size();
			for (int i = 0; i < total; i++) {
				final Objeto obj = this.objetosSolidos.get(i);
				if ((obj != null) && obj.intersecta(s)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Comprueba si un rectángulo colisiona con el área rectangular de este tile.
	 *
	 * @param area Rectángulo a comprobar.
	 * @return {@code true} si hay intersección.
	 */
	public boolean intersecta(final Rectangle area) {
		return (area != null) && area.intersects(this.AREA);
	}

	/**
	 * Calcula la posición donde debe encajar un objeto dentro del tile según una
	 * regla de alineación.
	 *
	 * @param codigoZonaPosicion Constante de alineación (ej: CENTRO).
	 * @param obj                Objeto a posicionar.
	 * @return Un nuevo {@link Point} con la posición absoluta calculada.
	 */
	public Point getPosicionSegunZonaYArea(final int codigoZonaPosicion, final Objeto obj) {
		final Point punto = new Point();
		if (obj == null) {
			return punto;
		}

		final int ancho = obj.getAncho();
		final int alto = obj.getAlto();

		switch (codigoZonaPosicion) {
		case PaletaComplento.POSICIONAMIENTO_CENTRO:
			if ((ancho == this.LADO) && (alto == this.LADO)) {
				punto.x = this.X;
				punto.y = this.Y;
			} else {
				// Centrado matemático estándar: (Posicion + MitadContenedor) - MitadElemento
				punto.x = (this.X + (this.LADO / 2)) - (ancho / 2);
				punto.y = (this.Y + (this.LADO / 2)) - (alto / 2);
			}
			break;
		default:
			punto.x = this.X;
			punto.y = this.Y;
			break;
		}
		return punto;
	}

	// =========================================================================
	// === GETTERS Y SETTERS
	// =========================================================================

	public void setMascaraBit(final byte mascara) {
		this.mascaraBit = mascara;
	}

	public byte getMascaraBit() {
		return this.mascaraBit;
	}

	public void setVariacionPropia(final byte variacion) {
		this.variacionPropia = variacion;
	}

	public byte getVariacionPropia() {
		return this.variacionPropia;
	}

	public void setCodigoModeloFondo(final int codigoFondo) {
		this.codigoModeloFondo = codigoFondo;
	}

	public int getCodigoModeloFondo() {
		return this.codigoModeloFondo;
	}

	/**
	 * Obtiene la imagen de textura actual asignada a este tile según su modelo y
	 * máscara.
	 *
	 * @return {@link BufferedImage} con los píxeles de la textura, o {@code null}
	 *         si el modelo no existe.
	 */
	public BufferedImage getTexturaImagen() {
		final ModeloTile m = ListaModeloTile.getModelo(this.CODIGO_MODELO_TILE);
		return (m != null) ? Textura.getTextura(m.getCodTextura(this.mascaraBit, this.variacionPropia)) : null;
	}

	/**
	 * Retorna el estado base del tile (ej: SÓLIDO, TRANSITABLE, ETC.).
	 *
	 * @return Código de estado entero definido en {@link ModeloTile}.
	 */
	public int getEstado() {
		final ModeloTile m = ListaModeloTile.getModelo(this.CODIGO_MODELO_TILE);
		return (m != null) ? m.getEstado() : 0;
	}

	/**
	 * @return ID de textura base del modelo de tile.
	 */
	public int getCodigoTextura() {
		final ModeloTile m = ListaModeloTile.getModelo(this.CODIGO_MODELO_TILE);
		return (m != null) ? m.getCodTextura() : 0;
	}

	/**
	 * @return El rectángulo inmutable que delimita al Tile.
	 */
	public Rectangle getArea() {
		return this.AREA;
	}

	public int getPosicionX() {
		return this.X;
	}

	public int getPosicionY() {
		return this.Y;
	}

	public int getLado() {
		return this.LADO;
	}

	public int getCodModelo() {
		return this.CODIGO_MODELO_TILE;
	}

	/**
	 * @return La posición absoluta en píxeles del tile como un {@link Point}.
	 */
	public Point getPosicion() {
		return new Point(this.X, this.Y);
	}

	/**
	 * Obtiene las coordenadas discretas de este tile en la matriz del mapa
	 * (columna, fila).
	 * <p>
	 * <b>EXPLICACIÓN MATEMÁTICA (Math.floorDiv vs División Común):</b> Si la
	 * coordenada X es -5 y el LADO es 32:
	 * <ul>
	 * <li>División común (-5 / 32) = 0 (trunca hacia cero, mezclando el cuadrante
	 * negativo con el positivo).</li>
	 * <li>Math.floorDiv(-5, 32) = -1 (redondea hacia abajo, asignando la celda
	 * correcta en mapas con coordenadas negativas).</li>
	 * </ul>
	 * </p>
	 *
	 * @return Coordenadas de grilla (X/LADO, Y/LADO).
	 */
	public Point getPosicionTile() {
		return new Point(Math.floorDiv(this.X, this.LADO), Math.floorDiv(this.Y, this.LADO));
	}

	// =========================================================================
	// === SERIALIZACIÓN JSON
	// =========================================================================

	/**
	 * Serializa las propiedades esenciales del tile a un objeto {@link JSONObject}.
	 *
	 * @return Objeto JSON listo para persistir en disco.
	 */
	@SuppressWarnings("unchecked")
	public JSONObject exportarParaJSON() {
		final JSONObject json = new JSONObject();
		json.put("x", Integer.valueOf(this.X));
		json.put("y", Integer.valueOf(this.Y));
		json.put("codModelo", Integer.valueOf(this.CODIGO_MODELO_TILE));
		return json;
	}

	/**
	 * Deserializa un tile a partir de su representación JSON.
	 *
	 * @param json Objeto JSON con los datos del tile.
	 * @return Nueva instancia de {@link Tile}.
	 */
	public static Tile crearDesdeJson(final JSONObject json) {
		final int x = ((Number) json.get("x")).intValue();
		final int y = ((Number) json.get("y")).intValue();
		final int codModelo = ((Number) json.get("codModelo")).intValue();
		return new Tile(x, y, Constantes.LADO_TILE, codModelo);
	}

	@Override
	public String toString() {
		return "Tile [AREA= x: " + this.AREA.x + ", y: " + this.AREA.y + ", COD=" + this.CODIGO_MODELO_TILE + "]";
	}
}