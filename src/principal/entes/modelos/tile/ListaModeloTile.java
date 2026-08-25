package principal.entes.modelos.tile;

import java.awt.image.BufferedImage;
import java.util.HashMap;

import principal.animaciones.Animacion;
import principal.mapa.Tile;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.HojaSprite;
import principal.utilidades.Textura;

/**
 * Catálogo centralizado y repositorio Flyweight de todos los modelos de
 * {@link ModeloTile} del juego.
 * <p>
 * <b>Rol Arquitectónico:</b>
 * <ul>
 * <li><b>Registro Único e Inmutable:</b> Inicializa estáticamente en memoria
 * todos los modelos al arrancar el juego. Cada celda {@link Tile} en el mapa
 * consulta este catálogo mediante su identificador numérico ({@code COD_*}),
 * logrando acceso $O(1)$ sin duplicar objetos.</li>
 * <li><b>Definición de Propiedades Físicas:</b> Asigna a cada terreno su estado
 * de colisión (traspasable u obstáculo) y sus modificadores sobre la velocidad
 * de desplazamiento de las criaturas.</li>
 * <li><b>Empaquetado de Autotiles y Animación:</b> Mapea automáticamente los
 * bloques continuos de 20 texturas por terreno (16 bordes + 4 variaciones) y
 * configura el reloj maestro para terrenos animados.</li>
 * </ul>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public final class ListaModeloTile {

	/**
	 * Diccionario centralizado de modelos indexados por su código identificador.
	 */
	private static final HashMap<Integer, ModeloTile> LISTA_MODELOS = new HashMap<Integer, ModeloTile>();

	/**
	 * Contador secuencial para la generación de identificadores únicos de modelos.
	 */
	private static int nextId = 1;

	// =========================================================================
	// === IDENTIFICADORES DE TERRENO (CÓDIGOS DE MODELO)
	// =========================================================================

	/** Césped clásico templado. Traspasable. Velocidad normal (0.0). */
	public static final int COD_CESPED = getSiguienteID();

	/** Camino de asfalto / adoquín. Traspasable. Acelera el movimiento (+25%). */
	public static final int COD_ASFALTO = getSiguienteID();

	/** Arena de playa / dunas. Traspasable. Ralentiza el movimiento (-20%). */
	public static final int COD_ARENA = getSiguienteID();

	/** Formación de roca / montaña. Obstáculo impenetrable (sólido). */
	public static final int COD_PIEDRA = getSiguienteID();

	/** Abismo / Vacío. Obstáculo impenetrable. Modificador de velocidad (-100%). */
	public static final int COD_VACIO = getSiguienteID();

	/**
	 * Masa de agua profunda con animación fluida. Obstáculo impenetrable (-50%).
	 */
	public static final int COD_AGUA = getSiguienteID();

	/** Tierra base / suelo fértil. Traspasable. Velocidad normal (0.0). */
	public static final int COD_TIERRA = getSiguienteID();

	/**
	 * Tierra compacta / camino de tierra. Traspasable. Acelera ligeramente (+20%).
	 */
	public static final int COD_TIERRA_2 = getSiguienteID();

	/** Césped seco / pradera de olivos. Traspasable. Velocidad normal (0.0). */
	public static final int COD_CESPED_2 = getSiguienteID();

	/** Césped de bosque profundo / hongos. Traspasable. Velocidad normal (0.0). */
	public static final int COD_CESPED_3 = getSiguienteID();

	/** Tundra / Césped nevado. Traspasable. Ralentiza el movimiento (-25%). */
	public static final int COD_CESPED_3_NEVADO = getSiguienteID();

	// =========================================================================
	// === INICIALIZACIÓN ESTÁTICA DEL CATÁLOGO
	// =========================================================================

	/*
	 * =========================================================================
	 * EXPLICACIÓN TÉCNICA: INICIALIZACIÓN EN TIEMPO DE ARRANQUE (BOOT-TIME)
	 * ------------------------------------------------------------------------- 1.
	 * Reloj Maestro de Agua: Se construye una 'Animacion' de 3 frames (350 ms).
	 * Esta instancia actúa como reloj sincronizado para que todos los tiles de agua
	 * del mapa se muevan al unísono con cero sobrecarga en el Game Loop. 2. Mapeo
	 * Automático de IDs: Cada llamada a 'crearModeloAutotile()' empaqueta los 16
	 * IDs contiguos de bordes direccionales y los 4 IDs de variaciones (v0..v3).
	 * =========================================================================
	 */
	static {
		// Reloj maestro de 3 frames para la animación del agua
		final BufferedImage dummyAnim = Globales.FUNCIONES.TEXTURAS_TOOLS.crearImagenVRAM(Constantes.LADO_TILE * 3,
				Constantes.LADO_TILE, java.awt.Transparency.TRANSLUCENT);
		final Animacion animAgua = new Animacion(new HojaSprite(dummyAnim, Constantes.LADO_TILE, false), true, 350);

		// Registro de modelos de terreno estáticos
		LISTA_MODELOS.put(COD_CESPED,
				crearModeloAutotile(ModeloTile.ESTADO_TRASPASABLE, Textura.INICIO_AUTOTILE_CESPED, 0.0));
		LISTA_MODELOS.put(COD_TIERRA,
				crearModeloAutotile(ModeloTile.ESTADO_TRASPASABLE, Textura.INICIO_AUTOTILE_TIERRA, 0.0));
		LISTA_MODELOS.put(COD_TIERRA_2,
				crearModeloAutotile(ModeloTile.ESTADO_TRASPASABLE, Textura.INICIO_AUTOTILE_TIERRA_2, 0.20));
		LISTA_MODELOS.put(COD_ARENA,
				crearModeloAutotile(ModeloTile.ESTADO_TRASPASABLE, Textura.INICIO_AUTOTILE_ARENA, -0.20));
		LISTA_MODELOS.put(COD_ASFALTO,
				crearModeloAutotile(ModeloTile.ESTADO_TRASPASABLE, Textura.INICIO_AUTOTILE_ASFALTO, 0.25));
		LISTA_MODELOS.put(COD_PIEDRA,
				crearModeloAutotile(ModeloTile.ESTADO_OBSTACULO, Textura.INICIO_AUTOTILE_PIEDRA, 0.0));

		// Registro de modelo de terreno animado (Agua con 3 fases de animación)
		LISTA_MODELOS.put(COD_AGUA, crearModeloAutotileAnimado(ModeloTile.ESTADO_OBSTACULO,
				Textura.INICIO_AUTOTILE_AGUA, 3, animAgua, -0.50));

		// Registro de biomas adicionales
		LISTA_MODELOS.put(COD_CESPED_2,
				crearModeloAutotile(ModeloTile.ESTADO_TRASPASABLE, Textura.INICIO_AUTOTILE_CESPED_2, 0.0));
		LISTA_MODELOS.put(COD_CESPED_3,
				crearModeloAutotile(ModeloTile.ESTADO_TRASPASABLE, Textura.INICIO_AUTOTILE_CESPED_3, 0.0));
		LISTA_MODELOS.put(COD_CESPED_3_NEVADO,
				crearModeloAutotile(ModeloTile.ESTADO_TRASPASABLE, Textura.INICIO_AUTOTILE_CESPED_3_NEVADO, -0.25));
		LISTA_MODELOS.put(COD_VACIO,
				crearModeloAutotile(ModeloTile.ESTADO_OBSTACULO, Textura.INICIO_AUTOTILE_VACIO, -1.0));
	}

	/**
	 * Constructor privado para impedir la instanciación de esta clase utilitaria.
	 */
	private ListaModeloTile() {
	}

	// =========================================================================
	// === MÉTODOS DE CONSTRUCCIÓN Y EMPAQUETADO DE AUTOTILES
	// =========================================================================

	/**
	 * Construye un {@link ModeloTile} estático configurando automáticamente sus 16
	 * variantes direccionales consecutivas y sus 4 variaciones decorativas de
	 * centro.
	 *
	 * @param estado              Estado físico de colisión (traspasable u
	 *                            obstáculo).
	 * @param idBase              Identificador base en {@link Textura} donde inicia
	 *                            la fila del terreno.
	 * @param alteracionVelocidad Modificador de velocidad al transitar sobre el
	 *                            terreno.
	 * @return Instancia configurada de {@link ModeloTile}.
	 */
	private static ModeloTile crearModeloAutotile(final int estado, final int idBase,
			final double alteracionVelocidad) {
		final int[] texturasAutotile = new int[16];
		for (int i = 0; i < 16; i++) {
			texturasAutotile[i] = idBase + i; // Máscaras 0 a 15
		}

		final int[] variacionesCentro = new int[] { idBase + 16, // v0 (Terreno limpio base)
				idBase + 17, // v1 (Detalle decorativo A)
				idBase + 18, // v2 (Detalle decorativo B)
				idBase + 19 // v3 (Detalle decorativo C)
		};

		return new ModeloTile(estado, texturasAutotile, variacionesCentro, alteracionVelocidad);
	}

	/**
	 * Construye un {@link ModeloTile} animado con soporte de bordes autotile y
	 * reloj sincronizado.
	 *
	 * @param estado              Estado físico de colisión.
	 * @param idBase              Identificador base de la primera fila en
	 *                            {@link Textura}.
	 * @param cantFrames          Cantidad de filas consecutivas de animación en el
	 *                            spritesheet.
	 * @param animacion           Reloj maestro {@link Animacion} que sincroniza el
	 *                            frame global.
	 * @param alteracionVelocidad Modificador de velocidad.
	 * @return Instancia animada de {@link ModeloTile}.
	 */
	private static ModeloTile crearModeloAutotileAnimado(final int estado, final int idBase, final int cantFrames,
			final Animacion animacion, final double alteracionVelocidad) {
		final int[] texturasAutotile = new int[16];
		for (int i = 0; i < 16; i++) {
			texturasAutotile[i] = idBase + i;
		}

		final int[] variacionesCentro = new int[] { idBase + 16, idBase + 17, idBase + 18, idBase + 19 };

		return new ModeloTile(estado, texturasAutotile, variacionesCentro, cantFrames, animacion, alteracionVelocidad);
	}

	// =========================================================================
	// === ACCESORES PÚBLICOS
	// =========================================================================

	/**
	 * Obtiene el modelo lógico {@link ModeloTile} correspondiente al código
	 * especificado. Acceso directo instantáneo $O(1)$.
	 *
	 * @param codModeloTile Identificador numérico del modelo (ej:
	 *                      {@link #COD_CESPED}).
	 * @return Instancia compartida de {@link ModeloTile} o {@code null} si el
	 *         código no existe.
	 */
	public static ModeloTile getModelo(final int codModeloTile) {
		return LISTA_MODELOS.get(codModeloTile);
	}

	/**
	 * Genera un nuevo identificador secuencial para los modelos.
	 *
	 * @return ID consecutivo generado.
	 */
	private static int getSiguienteID() {
		return nextId++;
	}

	/**
	 * Retorna el último identificador de modelo registrado.
	 *
	 * @return Valor entero del último ID en uso.
	 */
	public static int getUltimoIdUsado() {
		return nextId - 1;
	}
}