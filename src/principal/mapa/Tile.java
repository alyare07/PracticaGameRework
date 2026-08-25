package principal.mapa;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.HashMap;

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
 * Representa la unidad celda individual (Tile) dentro de la grilla del mapa.
 * <p>
 * <b>Arquitectura y Patrones de Diseño:</b>
 * <ul>
 * <li><b>Patrón Flyweight:</b> Cada instancia de {@code Tile} solo almacena su
 * posición, su geometría básica y referencias numéricas. Toda la información
 * pesada (texturas, animaciones, alteración de velocidad y estados de
 * obstáculo) reside de forma compartida en {@link ModeloTile} dentro de
 * {@link ListaModeloTile}.</li>
 * <li><b>Subgrilla de Sólidos Locales:</b> Mantiene una colección de
 * referencias directas a los objetos físicos que solapan esta celda espacial
 * ({@code OBJETOS_SOLIDADOS}), permitiendo que las físicas y la navegación por
 * Pathfinding (Dijkstra / A*) evalúen transitabilidad en tiempo constante
 * $O(1)$.</li>
 * <li><b>Renderizado Multicapa Inteligente:</b> Soporta una capa de fondo
 * opcional ({@code codigoModeloFondo}) y una capa principal autotileada y
 * animada con cero asignación en memoria en el bucle principal.</li>
 * </ul>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public class Tile implements Serializable {
	private static final long serialVersionUID = -445324235886L;

	/** Dimensión en píxeles del lado del tile (ej: 16 px). */
	protected final int LADO;

	/** Coordenada X absoluta en el mundo (píxeles). */
	protected final int X;

	/** Coordenada Y absoluta en el mundo (píxeles). */
	protected final int Y;

	/** Área rectangular que delimita los límites físicos del tile en el mundo. */
	protected final Rectangle AREA;

	/** Identificador del modelo lógico base asignado a este tile. */
	protected final int CODIGO_MODELO_TILE;

	/**
	 * Contenedor de entidades y complementos sólidos que intersectan el área de
	 * este tile. Utilizado para acelerar la detección de colisiones y la matriz de
	 * transitabilidad de Dijkstra.
	 */
	protected final HashMap<Objeto, Objeto> OBJETOS_SOLIDADOS = new HashMap<Objeto, Objeto>();

	/**
	 * Identificador del modelo que se dibujará como capa inferior (fondo). Valor
	 * {@code 0} indica ausencia de fondo extra.
	 */
	protected int codigoModeloFondo = 0;

	/**
	 * Máscara calculada de 4-bits (0 a 15) que determina los bordes del autotile.
	 */
	protected byte mascaraBit = 0;

	/**
	 * Índice determinista de variación decorativa (0 a 3: base limpia, flores,
	 * piedras, etc.).
	 */
	protected byte variacionPropia = 0;

	// =========================================================================
	// === CONSTRUCTORES
	// =========================================================================

	/**
	 * Construye una celda individual de terreno con su delimitador espacial.
	 *
	 * @param x                Coordenada X en píxeles.
	 * @param y                Coordenada Y en píxeles.
	 * @param lado             Dimensión del lado del tile en píxeles.
	 * @param codigoModeloTile Identificador del modelo de tile asignado.
	 */
	public Tile(final int x, final int y, final int lado, final int codigoModeloTile) {
		this.X = x;
		this.Y = y;
		this.LADO = lado;
		this.CODIGO_MODELO_TILE = codigoModeloTile;
		this.AREA = new Rectangle(x, y, lado, lado);
	}

	// =========================================================================
	// === RENDERIZADO (GAME LOOP Y EDITOR)
	// =========================================================================

	/*
	 * =========================================================================
	 * EXPLICACIÓN TÉCNICA: RENDERIZADO POR CAPAS EN O(1)
	 * ------------------------------------------------------------------------- 1.
	 * Capa Inferior (Fondo): Si 'codigoModeloFondo != 0', dibuja primero la textura
	 * del terreno base (ej. Tierra o Agua) para que los bordes transparentes del
	 * pasto superior no muestren el fondo negro de la ventana. 2. Capa Superior
	 * (Principal): Consulta a 'ModeloTile.getCodTextura()' pasándole la máscara de
	 * 4-bits y la variación. Si el tile tiene animación global (como el agua), se
	 * le suma el offset del frame activo (+0, +20, +40). 3. Cero Asignaciones: No
	 * se instancian objetos 'new' durante el renderizado.
	 * =========================================================================
	 */

	/**
	 * Dibuja las capas gráfica del tile (fondo y textura principal calculada).
	 * Método de rendimiento crítico $O(1)$, seguro para el recolector de basura (GC
	 * Friendly).
	 *
	 * @param g Contexto gráfico {@link Graphics2D}.
	 */
	private void pintarCapas(final Graphics2D g) {
		// 1. CAPA INFERIOR: Pintar el fondo si existe
		if (this.codigoModeloFondo != 0) {
			final ModeloTile modeloFondo = ListaModeloTile.getModelo(this.codigoModeloFondo);
			if (modeloFondo != null) {
				final int texFondo = modeloFondo.getCodTextura(this.mascaraBit, this.variacionPropia);
				DibujoDebug.dibujarImagenRefCamara(g, Textura.getTextura(texFondo), this.X, this.Y);
			}
		}

		// 2. CAPA SUPERIOR: Pintar el tile principal (Autotile estático o animado)
		final ModeloTile modelo = ListaModeloTile.getModelo(this.CODIGO_MODELO_TILE);
		if (modelo != null) {
			final int texturaFinal = modelo.getCodTextura(this.mascaraBit, this.variacionPropia);
			DibujoDebug.dibujarImagenRefCamara(g, Textura.getTextura(texturaFinal), this.X, this.Y);
		}
	}

	/**
	 * Renderiza el tile en pantalla durante la ejecución normal del juego.
	 *
	 * @param g Contexto gráfico {@link Graphics2D}.
	 */
	public void pintar(final Graphics2D g) {
		if (!Globales.TECLADO.TECLA_OCULTAR_TERRENO.presionado()) {
			this.pintarCapas(g);
		}

		if (Globales.TECLADO.TECLA_DEBUG_TILE.presionado() && Globales.estadoJuego) {
			DibujoDebug.dibujarImagenRefCamara(g, Textura.getTextura(Textura.idTexturaContornoTile), this.X, this.Y);
		}
	}

	/**
	 * Renderiza el tile en el editor de mapas con soporte de contornos de
	 * depuración.
	 *
	 * @param g Contexto gráfico {@link Graphics2D}.
	 */
	public void pintarEditor(final Graphics2D g) {
		this.pintarCapas(g);

		if (!Globales.editorSelectGroupTile && Globales.TECLADO.TECLA_DEBUG_TILE.presionado()) {
			DibujoDebug.dibujarImagenRefCamara(g, Textura.getTextura(Textura.idTexturaContornoTile), this.X, this.Y);
		}
	}

	/**
	 * Renderiza el tile dentro de la interfaz gráfica de paletas del editor (sin
	 * compensación de cámara).
	 *
	 * @param g Contexto gráfico {@link Graphics2D}.
	 */
	public void pintarPaleta(final Graphics2D g) {
		DibujoDebug.dibujarImagen(g, this.getTexturaImagen(), this.X, this.Y);
		DibujoDebug.dibujarImagen(g, Textura.getTextura(Textura.idTexturaContornoTile), this.X, this.Y);
	}

	/**
	 * Dibuja un rectángulo de contorno para resaltar visualmente el tile.
	 *
	 * @param g     Contexto gráfico {@link Graphics2D}.
	 * @param color Color del contorno.
	 */
	public void pintarContorno(final Graphics2D g, final Color color) {
		DibujoDebug.dibujarRectanguloContornoRefCamara(g, this.X, this.Y, this.LADO, this.LADO, color);
	}

	// =========================================================================
	// === GESTIÓN DE OBJETOS SÓLIDOS Y COLISIONES
	// =========================================================================

	/*
	 * =========================================================================
	 * EXPLICACIÓN TÉCNICA: REGISTRO DE SÓLIDOS ESPACIALES
	 * -------------------------------------------------------------------------
	 * Cuando un 'Objeto' o 'Complemento' sólido se coloca en el mapa, se registra
	 * en las celdas 'Tile' que su caja de colisión solapa.
	 * 
	 * De esta forma, cuando el algoritmo de Dijkstra o el Jugador comprueban
	 * colisión: 1. Primero evalúan 'this.esSolido()' (Tile base). 2. Si es falso,
	 * evalúan si 'this.contieneObjetosSolidos()' es verdadero. 3. Solo iteran los
	 * objetos específicos de esta celda con retorno temprano.
	 * =========================================================================
	 */

	/**
	 * Asocia un objeto sólido a esta celda si existe intersección espacial con su
	 * área.
	 *
	 * @param obj Objeto o complemento a registrar.
	 */
	public void meterObjetoSolido(final Objeto obj) {
		if (obj == null) {
			return;
		}

		if (obj.getArea().intersects(this.AREA)) {
			if (obj instanceof Complemento) {
				final Complemento c = (Complemento) obj;
				if (c.intersecta(this.AREA)) {
					this.OBJETOS_SOLIDADOS.put(c, obj);
				}
				return;
			}
			if (this.AREA.intersects(obj.getArea())) {
				this.OBJETOS_SOLIDADOS.put(obj, obj);
			}
		}
	}

	/**
	 * Desregistra un objeto sólido de esta celda.
	 *
	 * @param obj Objeto a remover.
	 */
	public void sacarObjetoSolido(final Objeto obj) {
		if (obj != null) {
			this.OBJETOS_SOLIDADOS.remove(obj);
		}
	}

	public int getCantObjetosSolidos() {
		return this.OBJETOS_SOLIDADOS.size();
	}

	public void limpiarObjetosSolidos() {
		this.OBJETOS_SOLIDADOS.clear();
	}

	public boolean contieneObjetosSolidos() {
		return !this.OBJETOS_SOLIDADOS.isEmpty();
	}

	/**
	 * Evalúa si este tile es intransitable para el algoritmo de Pathfinding /
	 * Dijkstra.
	 *
	 * @return {@code true} si el tile base es obstáculo o si contiene objetos
	 *         sólidos.
	 */
	public boolean esSolidoDijkstra() {
		if (this.getEstado() == ModeloTile.ESTADO_OBSTACULO) {
			return true;
		}
		return this.contieneObjetosSolidos();
	}

	/**
	 * Evalúa si el modelo base del tile está configurado como obstáculo
	 * impenetrable.
	 *
	 * @return {@code true} si el estado del modelo es
	 *         {@link ModeloTile#ESTADO_OBSTACULO}.
	 */
	public boolean esSolido() {
		return this.getEstado() == ModeloTile.ESTADO_OBSTACULO;
	}

	/**
	 * Comprueba colisión física precisa entre una forma geométrica arbitraria y
	 * este tile (incluyendo los objetos sólidos que residen en él).
	 *
	 * @param s Forma geométrica {@link Shape} a verificar.
	 * @return {@code true} al encontrar la primera colisión sólida.
	 */
	public boolean hayColisionConAlgoSolido(final Shape s) {
		if (s == null) {
			return false;
		}

		if (this.esSolido()) {
			return true;
		}

		if (this.contieneObjetosSolidos()) {
			for (final Objeto obj : this.OBJETOS_SOLIDADOS.values()) {
				if ((obj != null) && obj.intersecta(s)) {
					return true; // Retorno temprano al primer objeto colisionado
				}
			}
		}
		return false;
	}

	/**
	 * Evalúa si un rectángulo intersecta con el área física delimitadora de este
	 * tile.
	 *
	 * @param area Rectángulo a comprobar.
	 * @return {@code true} si existe solapamiento espacial.
	 */
	public boolean intersecta(final Rectangle area) {
		if (area == null) {
			return false;
		}
		return area.intersects(this.AREA);
	}

	/**
	 * Calcula la posición de colocación de un objeto según el modo de alineación
	 * del editor.
	 * <p>
	 * <i>Nota de rendimiento:</i> Instancia un nuevo {@link Point}. Usar únicamente
	 * en rutinas de edición o inicialización fuera del bucle de juego continuo.
	 * </p>
	 *
	 * @param codigoZonaPosicion Código de posicionamiento (ej.
	 *                           {@link PaletaComplento#POSICIONAMIENTO_CENTRO}).
	 * @param obj                Objeto a posicionar.
	 * @return Coordenadas de inserción calculadas.
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
				punto.x = (this.X + (this.LADO / 2)) - (ancho / 2);
				punto.y = (this.Y + (this.LADO / 2)) - (alto / 2);
			}
			break;
		default:
			punto.x = this.X;
			punto.y = this.Y;
		}
		return punto;
	}

	// =========================================================================
	// === AUTOTILING Y CONFIGURACIÓN DINÁMICA
	// =========================================================================

	public void setMascaraBit(final byte mascara) {
		this.mascaraBit = mascara;
	}

	public void setVariacionPropia(final byte variacion) {
		this.variacionPropia = variacion;
	}

	public void setCodigoModeloFondo(final int codigoFondo) {
		this.codigoModeloFondo = codigoFondo;
	}

	// =========================================================================
	// === ACCESORES Y GETTERS FLYWEIGHT
	// =========================================================================

	/**
	 * Obtiene la imagen de textura calculada en tiempo real para este tile.
	 *
	 * @return {@link BufferedImage} correspondiente a la máscara y variación
	 *         actual.
	 */
	public BufferedImage getTexturaImagen() {
		final ModeloTile m = ListaModeloTile.getModelo(this.CODIGO_MODELO_TILE);
		if (m != null) {
			return Textura.getTextura(m.getCodTextura(this.mascaraBit, this.variacionPropia));
		}
		return null;
	}

	public int getEstado() {
		final ModeloTile m = ListaModeloTile.getModelo(this.CODIGO_MODELO_TILE);
		return (m != null) ? m.getEstado() : 0;
	}

	public int getCodigoTextura() {
		final ModeloTile m = ListaModeloTile.getModelo(this.CODIGO_MODELO_TILE);
		return (m != null) ? m.getCodTextura() : 0;
	}

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
	 * Retorna la posición en píxeles del tile.
	 * <p>
	 * <i>Nota:</i> Genera una nueva instancia de {@link Point}. Para lecturas de
	 * rendimiento en el Game Loop, preferir {@link #getPosicionX()} y
	 * {@link #getPosicionY()}.
	 * </p>
	 */
	public Point getPosicion() {
		return new Point(this.X, this.Y);
	}

	/**
	 * Retorna la coordenada discreta en la grilla de tiles.
	 * <p>
	 * <i>Nota:</i> Genera una nueva instancia de {@link Point}.
	 * </p>
	 */
	public Point getPosicionTile() {
		return new Point(this.X / this.LADO, this.Y / this.LADO);
	}

	// =========================================================================
	// === SERIALIZACIÓN JSON
	// =========================================================================

	/**
	 * Exporta los datos lógicos esenciales del tile para su persistencia en JSON.
	 *
	 * @return Objeto {@link JSONObject} serializado.
	 */
	@SuppressWarnings("unchecked")
	public JSONObject exportarParaJSON() {
		final JSONObject json = new JSONObject();
		json.put("x", this.getPosicionX());
		json.put("y", this.getPosicionY());
		json.put("codModelo", this.getCodModelo());
		return json;
	}

	/**
	 * Reconstruye una instancia de {@link Tile} desde su representación en JSON.
	 *
	 * @param json Objeto JSON con los datos deserializados.
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
		return "Tile [AREA= x: " + this.AREA.x + " ,y: " + this.AREA.y + " , W: " + this.AREA.width + " ,H: "
				+ this.AREA.height + ", MODELO_TILE=" + ListaModeloTile.getModelo(this.CODIGO_MODELO_TILE) + "]";
	}
}