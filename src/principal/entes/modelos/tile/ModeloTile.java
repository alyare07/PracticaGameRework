package principal.entes.modelos.tile;

import java.awt.image.BufferedImage;

import principal.animaciones.Animacion;
import principal.mapa.Tile;
import principal.utilidades.Textura;

/**
 * Representa el modelo lógico, físico y gráfico compartido (Flyweight) de un
 * tipo de Tile.
 * <p>
 * <b>Patrón de Diseño Flyweight:</b><br>
 * En un mapa de miles de celdas, solo existe <b>una única instancia</b> de
 * {@code ModeloTile} por cada tipo de terreno (ej. {@code COD_CESPED},
 * {@code COD_AGUA}). Cada {@link Tile} individual en el mapa solo guarda una
 * referencia numérica a este modelo, ahorrando megabytes de memoria RAM al no
 * duplicar arreglos de texturas, físicas ni temporizadores.
 * </p>
 * <p>
 * <b>Capacidades del Modelo:</b>
 * <ul>
 * <li><b>Tiles Clásicos:</b> Soporte para tiles con una única textura
 * fija.</li>
 * <li><b>Autotiling Estático:</b> Mapeo de 16 texturas direccionales + 4
 * variaciones de centro.</li>
 * <li><b>Autotiling Animado:</b> Sincronización global de frames mediante reloj
 * maestro, permitiendo que terrenos como el agua tengan animación continua y
 * bordes inteligentes en $O(1)$.</li>
 * </ul>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public class ModeloTile {

	// =========================================================================
	// === CONSTANTES DE ESTADO Y FÍSICA
	// =========================================================================

	/** El tile puede ser atravesado libremente por cualquier entidad. */
	public static final int ESTADO_TRASPASABLE = 0;

	/**
	 * El tile actúa como una pared/obstáculo infranqueable para entidades y
	 * Pathfinding.
	 */
	public static final int ESTADO_OBSTACULO = 1;

	/**
	 * Estado compuesto para tiles con comportamientos condicionales o múltiples
	 * capas.
	 */
	public static final int ESTADO_MULTIPLE = 2;

	/**
	 * Contador secuencial para asignar identificadores numéricos únicos a cada
	 * modelo.
	 */
	private static int siguienteId = 1;

	// =========================================================================
	// === ATRIBUTOS DE INSTANCIA
	// =========================================================================

	/** Identificador numérico único de esta instancia de modelo. */
	protected final int id;

	/**
	 * Estado físico de colisión ({@link #ESTADO_TRASPASABLE} u
	 * {@link #ESTADO_OBSTACULO}).
	 */
	protected final int ESTADO;

	/**
	 * Modificador multiplicador sobre la velocidad del jugador/criaturas al pisar
	 * este tile. (Ej: {@code 0.0} = neutro, {@code 0.25} = acelera en asfalto,
	 * {@code -0.20} = ralentiza en arena).
	 */
	protected final double ALTERACION_VELOCIDAD;

	/** Código de textura de reserva (fallback) o textura fija en tiles clásicos. */
	protected int COD_TEXTURA;

	/**
	 * Arreglo de 16 IDs de textura que corresponden exactamente a las máscaras
	 * binarias de 4 bits (0 a 15). Nulo si el modelo no utiliza el sistema de
	 * autotiling.
	 */
	protected int[] texturasAutotile;

	/**
	 * Arreglo de IDs de textura para las variantes decorativas del centro (máscara
	 * 15: v0, v1, v2, v3). Nulo si el modelo no tiene variaciones.
	 */
	protected int[] variacionesCentro;

	/**
	 * Cantidad de fases/frames de animación que posee este terreno (1 = estático,
	 * >1 = animado).
	 */
	protected int cantFramesAnimacion = 1;

	/**
	 * Instancia de animación que actúa como reloj global compartido para
	 * sincronizar los frames del terreno.
	 */
	protected Animacion animacion;

	// =========================================================================
	// === CONSTRUCTORES
	// =========================================================================

	/**
	 * Construye un modelo de tile clásico con una única textura estática (sin
	 * autotile).
	 *
	 * @param estado              Estado físico de colisión (traspasable u
	 *                            obstáculo).
	 * @param textura             Identificador de la textura fija en
	 *                            {@link Textura}.
	 * @param alteracionVelocidad Modificador de velocidad al caminar sobre este
	 *                            tile.
	 */
	protected ModeloTile(final int estado, final int textura, final double alteracionVelocidad) {
		this.ESTADO = estado;
		this.COD_TEXTURA = textura;
		this.id = getSiguienteId();
		this.ALTERACION_VELOCIDAD = alteracionVelocidad;
	}

	/**
	 * Construye un modelo de tile con soporte completo de Autotiling estático (16
	 * bordes + variaciones).
	 *
	 * @param estado              Estado físico de colisión.
	 * @param texturasAutotile    Arreglo de 16 IDs de textura correspondientes a
	 *                            las máscaras binarias.
	 * @param variacionesCentro   Arreglo de IDs de textura para las decoraciones de
	 *                            centro (máscara 15).
	 * @param alteracionVelocidad Modificador de velocidad.
	 */
	protected ModeloTile(final int estado, final int[] texturasAutotile, final int[] variacionesCentro,
			final double alteracionVelocidad) {
		this.ESTADO = estado;
		this.texturasAutotile = texturasAutotile;
		this.variacionesCentro = variacionesCentro;
		this.COD_TEXTURA = ((texturasAutotile != null) && (texturasAutotile.length == 16)) ? texturasAutotile[15]
				: Textura.TEXTURA_ERROR;
		this.id = getSiguienteId();
		this.ALTERACION_VELOCIDAD = alteracionVelocidad;
	}

	/**
	 * Construye un modelo de tile avanzado con Autotiling y Animación Global
	 * sincronizada.
	 *
	 * @param estado              Estado físico de colisión.
	 * @param texturasAutotile    Arreglo de 16 IDs de textura base (Frame 0).
	 * @param variacionesCentro   Arreglo de IDs de variaciones base (Frame 0).
	 * @param cantFrames          Cantidad de filas/frames de animación consecutivas
	 *                            en el spritesheet.
	 * @param animacion           Reloj maestro {@link Animacion} para sincronizar
	 *                            el avance temporal.
	 * @param alteracionVelocidad Modificador de velocidad.
	 */
	protected ModeloTile(final int estado, final int[] texturasAutotile, final int[] variacionesCentro,
			final int cantFrames, final Animacion animacion, final double alteracionVelocidad) {
		this.ESTADO = estado;
		this.texturasAutotile = texturasAutotile;
		this.variacionesCentro = variacionesCentro;
		this.cantFramesAnimacion = cantFrames;
		this.animacion = animacion;
		this.COD_TEXTURA = ((texturasAutotile != null) && (texturasAutotile.length == 16)) ? texturasAutotile[15]
				: Textura.TEXTURA_ERROR;
		this.id = getSiguienteId();
		this.ALTERACION_VELOCIDAD = alteracionVelocidad;
	}

	// =========================================================================
	// === RESOLUCIÓN DE TEXTURAS EN TIEMPO REAL O(1)
	// =========================================================================

	/*
	 * =========================================================================
	 * EXPLICACIÓN TÉCNICA: CÁLCULO DE OFFSET DE FRAME Y RESOLUCIÓN O(1)
	 * ------------------------------------------------------------------------- 1.
	 * Offset de Animación: Si el modelo es animado (ej. Agua con 3 frames),
	 * consulta a su 'Animacion' el frame actual (0, 1 o 2). Como cada set de
	 * terreno ocupa exactamente 20 columnas en el spritesheet, el desplazamiento de
	 * textura en VRAM es: offsetFrame = (frame % cantFramesAnimacion) * 20;
	 * 
	 * 2. Selección de Sprite: - Si es un tile clásico (sin autotile): Retorna
	 * COD_TEXTURA + offset. - Si es centro rodeado (mascara == 15): Retorna
	 * variacionesCentro[idVar] + offset. - Si es borde o esquina (mascara 0..14):
	 * Retorna texturasAutotile[mascara] + offset.
	 * 
	 * Complejidad: O(1) puro sin condicionales pesados ni asignaciones 'new'.
	 * =========================================================================
	 */

	/**
	 * Resuelve instantáneamente el ID de textura exacto en VRAM para un tile,
	 * considerando sus vecinos cardinales (máscara), su variación decorativa y el
	 * frame activo de animación.
	 *
	 * @param mascaraBit  Valor de la máscara de bits (0 a 15).
	 * @param idVariacion Índice determinista de variación (0 a 3).
	 * @return Identificador entero de textura registrado en {@link Textura}.
	 */
	public int getCodTextura(final byte mascaraBit, final byte idVariacion) {
		int offsetFrame = 0;
		if ((this.animacion != null) && (this.cantFramesAnimacion > 1)) {
			final int frame = this.animacion.getSpritePosicion(); // Obtiene frame sincronizado (0, 1 o 2)
			offsetFrame = (frame % this.cantFramesAnimacion) * 20; // 20 tiles por fila de spritesheet
		}

		// 1. Fallback para tiles clásicos de una sola textura
		if ((this.texturasAutotile == null) || (this.texturasAutotile.length < 16)) {
			return this.COD_TEXTURA + offsetFrame;
		}

		// 2. Si es el centro (15) y posee variaciones decorativas (flores, hongos,
		// etc.)
		if ((mascaraBit == 15) && (this.variacionesCentro != null) && (this.variacionesCentro.length > 0)) {
			return this.variacionesCentro[idVariacion % this.variacionesCentro.length] + offsetFrame;
		}

		// 3. Borde o esquina correspondiente a la máscara de bits
		return this.texturasAutotile[mascaraBit] + offsetFrame;
	}

	/**
	 * Obtiene la imagen {@link BufferedImage} estática por defecto registrada para
	 * este modelo.
	 *
	 * @return Imagen de VRAM correspondiente a la textura base.
	 */
	public BufferedImage getTextura() {
		return Textura.getTextura(this.COD_TEXTURA);
	}

	/**
	 * Sobrescribe manualmente la textura base de reserva de este modelo.
	 *
	 * @param textura Identificador de textura en {@link Textura}.
	 */
	public void establecerTextura(final int textura) {
		this.COD_TEXTURA = textura;
	}

	// =========================================================================
	// === ACCESORES Y GETTERS
	// =========================================================================

	public int getCodTextura() {
		return this.COD_TEXTURA;
	}

	public int getEstado() {
		return this.ESTADO;
	}

	public int getId() {
		return this.id;
	}

	public double getAlteracionVelocidad() {
		return this.ALTERACION_VELOCIDAD;
	}

	public Animacion getAnimacion() {
		return this.animacion;
	}

	public boolean contieneAnimacion() {
		return this.animacion != null;
	}

	public int getCantFramesAnimacion() {
		return this.cantFramesAnimacion;
	}

	// =========================================================================
	// === MÉTODOS UTILITARIOS
	// =========================================================================

	/**
	 * Genera un nuevo identificador secuencial para el registro de modelos.
	 *
	 * @return ID único consecutivo.
	 */
	public static synchronized int getSiguienteId() {
		return siguienteId++;
	}

	/**
	 * Método alias de compatibilidad hacia atrás para {@link #getSiguienteId()}.
	 */
	public static int getSIguienteId() {
		return getSiguienteId();
	}

	@Override
	public String toString() {
		return "ModeloTile [id=" + this.id + ", ESTADO=" + this.ESTADO + ", COD_TEXTURA=" + this.COD_TEXTURA
				+ ", altVel="
				+ ((this.ALTERACION_VELOCIDAD > 0) ? "+" + this.ALTERACION_VELOCIDAD : this.ALTERACION_VELOCIDAD)
				+ ", frames=" + this.cantFramesAnimacion + "]";
	}
}