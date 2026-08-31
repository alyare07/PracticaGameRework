package principal.utilidades;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Almacén maestro y catálogo unificado de texturas cargadas en VRAM.
 * <p>
 * <b>Arquitectura de Rendimiento:</b>
 * <ul>
 * <li><b>Acceso Directo por Arreglo Primitivo O(1):</b> Elimina el
 * boxing/unboxing de {@code Integer} en el renderizado continuo de tiles
 * mediante un arreglo plano con desplazamiento de índices negativos.</li>
 * <li><b>Auto-Expansión Segura:</b> Si se registra un ID superior a la
 * capacidad base durante el arranque, el catálogo se expande automáticamente
 * sin fragmentación.</li>
 * </ul>
 * </p>
 * 
 * @version 3.0
 */
public final class Textura {

	// =========================================================================
	// === 1. IDENTIFICADORES DE RANGOS BASE
	// =========================================================================

	private static final int INICIO_TERRENO = 1;
	private static final int INICIO_SUB_TERRENO = 500;
	private static final int INICIO_OBJETOS = 1000;
	private static final int INICIO_ITEMS = 5000;
	private static final int INICIO_PARTICULAS = 15000;
	private static final int INICIO_EFECTOS = 50000;
	private static final int INICIO_ESTRUCTURAS = 150000;

	private static int idSiguienteTerreno = INICIO_TERRENO;
	private static int idSiguienteSubTerreno = INICIO_SUB_TERRENO;
	private static int idSiguienteObjeto = INICIO_OBJETOS;
	private static int idSiguienteItem = INICIO_ITEMS;
	private static int idSiguienteParticula = INICIO_PARTICULAS;
	private static int idSiguienteEfecto = INICIO_EFECTOS;
	private static int idSiguienteEstructura = INICIO_ESTRUCTURAS;

	private static int getSiguienteIdTerreno() {
		return idSiguienteTerreno++;
	}

	private static int getSiguienteIdSubTerreno() {
		return idSiguienteSubTerreno++;
	}

	private static int getSiguienteIdObjeto() {
		return idSiguienteObjeto++;
	}

	private static int getSiguienteIdItem() {
		return idSiguienteItem++;
	}

	private static int getSiguienteIdParticula() {
		return idSiguienteParticula++;
	}

	private static int getSiguienteIdEfecto() {
		return idSiguienteEfecto++;
	}

	private static int getSiguienteIdEstructura() {
		return idSiguienteEstructura++;
	}

	private static int getSiguienteRangoTerreno(final int cantidad) {
		final int inicio = idSiguienteTerreno;
		idSiguienteTerreno += cantidad;
		return inicio;
	}

	// =========================================================================
	// === 2. CONSTANTES DE TEXTURAS Y PRESETS
	// =========================================================================

	// IDs Especiales
	public static final int TEXTURA_ERROR = -2;
	public static final int TEXTURA_TRANSPARENTE = -1;
	public static final int TEXTURA_x32_VACIO = 0;
	public static final int idTexturaContornoTile = -3;
	public static final int idTexturaContornoGroupTile = -4;

	// Autotiles Terreno
	public static final int INICIO_AUTOTILE_CESPED = getSiguienteRangoTerreno(20);
	public static final int INICIO_AUTOTILE_TIERRA = getSiguienteRangoTerreno(20);
	public static final int INICIO_AUTOTILE_TIERRA_2 = getSiguienteRangoTerreno(20);
	public static final int INICIO_AUTOTILE_ARENA = getSiguienteRangoTerreno(20);
	public static final int INICIO_AUTOTILE_ASFALTO = getSiguienteRangoTerreno(20);
	public static final int INICIO_AUTOTILE_PIEDRA = getSiguienteRangoTerreno(20);
	public static final int INICIO_AUTOTILE_AGUA = getSiguienteRangoTerreno(60); // 3 frames * 20
	public static final int INICIO_AUTOTILE_CESPED_2 = getSiguienteRangoTerreno(20);
	public static final int INICIO_AUTOTILE_CESPED_3 = getSiguienteRangoTerreno(20);
	public static final int INICIO_AUTOTILE_CESPED_3_NEVADO = getSiguienteRangoTerreno(20);
	public static final int INICIO_AUTOTILE_VACIO = getSiguienteRangoTerreno(20);

	// Subterreno 16x16
	public static final int TEXTURA_x16_AGUA = getSiguienteIdSubTerreno();
	public static final int TEXTURA_x16_MURO_PIEDRA_NEGRA = getSiguienteIdSubTerreno();
	public static final int TEXTURA_x16_TIERRA = getSiguienteIdSubTerreno();
	public static final int TEXTURA_x16_CESPED = getSiguienteIdSubTerreno();
	public static final int TEXTURA_x16_AGUA_HORIZONTAL = getSiguienteIdSubTerreno();
	public static final int TEXTURA_x16_AGUA_HORIZONTAL_2 = getSiguienteIdSubTerreno();
	public static final int TEXTURA_x16_AGUA_HORIZONTAL_3 = getSiguienteIdSubTerreno();
	public static final int TEXTURA_x16_AGUA_HORIZONTAL_4 = getSiguienteIdSubTerreno();
	public static final int TEXTURA_x16_AGUA_HORIZONTAL_5 = getSiguienteIdSubTerreno();
	public static final int TEXTURA_x16_AGUA_HORIZONTAL_6 = getSiguienteIdSubTerreno();
	public static final int TEXTURA_x16_AGUA_VERTICAL = getSiguienteIdSubTerreno();
	public static final int TEXTURA_x16_AGUA_VERTICAL_2 = getSiguienteIdSubTerreno();
	public static final int TEXTURA_x16_AGUA_VERTICAL_3 = getSiguienteIdSubTerreno();
	public static final int TEXTURA_x16_AGUA_VERTICAL_4 = getSiguienteIdSubTerreno();

	// Objetos
	public static final int TEXTURA_x32_ARBOL_0 = getSiguienteIdObjeto();
	public static final int TEXTURA_x32_ARBOL_1 = getSiguienteIdObjeto();
	public static final int TEXTURA_x32_ARBOL_2 = getSiguienteIdObjeto();
	public static final int TEXTURA_x32_ARBOL_3 = getSiguienteIdObjeto();
	public static final int TEXTURA_x32_ARBOL_1_NEVADO = getSiguienteIdObjeto();
	public static final int TEXTURA_x32_ARBOL_2_NEVADO = getSiguienteIdObjeto();
	public static final int TEXTURA_x32_ARBOL_3_NEVADO = getSiguienteIdObjeto();
	public static final int TEXTURA_x32_ARBOL_0_NEVADO = getSiguienteIdObjeto();

	// Items
	public static final int TEXTURA_x16_ANILLO_PLATA = getSiguienteIdItem();
	public static final int TEXTURA_x16_ANILLO_ORO = getSiguienteIdItem();
	public static final int TEXTURA_x16_ESMERALDA = getSiguienteIdItem();
	public static final int TEXTURA_x16_POCION_ROJA = getSiguienteIdItem();
	public static final int TEXTURA_x16_POCION_AZUL = getSiguienteIdItem();
	public static final int TEXTURA_x16_BOTAS_CUERO_MARRON = getSiguienteIdItem();
	public static final int TEXTURA_x16_PISTOLA = getSiguienteIdItem();

	public static final int TEXTURA_X16_GRANADA_1 = getSiguienteIdItem();
	public static final int TEXTURA_X16_GRANADA_2 = getSiguienteIdItem();
	public static final int TEXTURA_X16_GRANADA_3 = getSiguienteIdItem();
	public static final int TEXTURA_X16_GRANADA_4 = getSiguienteIdItem();
	public static final int TEXTURA_X16_GRANADA_5 = getSiguienteIdItem();
	public static final int TEXTURA_X16_GRANADA_6 = getSiguienteIdItem();
	public static final int TEXTURA_X16_GRANADA_7 = getSiguienteIdItem();
	public static final int TEXTURA_X16_GRANADA_8 = getSiguienteIdItem();

	public static final int TEXTURA_x10_POCION_AZUL = getSiguienteIdItem();
	public static final int TEXTURA_x10_POCION_ROJA = getSiguienteIdItem();
	public static final int TEXTURA_x10_BOTAS_CUERO_MARRON = getSiguienteIdItem();
	public static final int TEXTURA_x8_PISTOLA = getSiguienteIdItem();
	public static final int TEXTURA_x4_BALA = getSiguienteIdItem();

	public static final int TEXTURA_X10_GRANADA_1 = getSiguienteIdItem();
	public static final int TEXTURA_X10_GRANADA_2 = getSiguienteIdItem();
	public static final int TEXTURA_X10_GRANADA_3 = getSiguienteIdItem();
	public static final int TEXTURA_X10_GRANADA_4 = getSiguienteIdItem();
	public static final int TEXTURA_X10_GRANADA_5 = getSiguienteIdItem();
	public static final int TEXTURA_X10_GRANADA_6 = getSiguienteIdItem();
	public static final int TEXTURA_X10_GRANADA_7 = getSiguienteIdItem();
	public static final int TEXTURA_X10_GRANADA_8 = getSiguienteIdItem();

	// Partículas y Efectos
	public static final int TEXTURA_X8_PARTICULA_SANGRE = getSiguienteIdParticula();
	public static final int TEXTURA_x50_EXPLOSION = getSiguienteIdEfecto();

	// Estructuras
	public static final int TEXTURA_X64_CASA1 = getSiguienteIdEstructura();

	// =========================================================================
	// === 3. ESTRUCTURAS DE ALMACENAMIENTO DE ALTO RENDIMIENTO (ZERO-GC)
	// =========================================================================

	/** Offset para mapear IDs negativos (-4 .. -1) dentro del arreglo continuo. */
	private static final int OFFSET_INDICE = 16;

	/** Capacidad base del arreglo directo de texturas. */
	private static final int CAPACIDAD_BASE = 250000;

	/** Arreglo contiguo de punteros a texturas indexado directamente por ID. */
	private static BufferedImage[] catalogoTexturas = new BufferedImage[CAPACIDAD_BASE];

	/** Referencia directa a la textura de fallback. */
	private static BufferedImage texturaError;

	/** Mapa de compatibilidad para herramientas externas o inspectores. */
	public static final Map<Integer, BufferedImage> TEXTURAS = new HashMap<>();

	public static HojaSprite HOJA_AGUA;

	// =========================================================================
	// === 4. INICIALIZACIÓN Y CARGA
	// =========================================================================

	static {
		cargarTodasLasTexturas();
	}

	private Textura() {
	}

	private static void cargarTodasLasTexturas() {

		// 1. Texturas Especiales y Debug
		final BufferedImage vacio = Globales.FUNCIONES.TEXTURAS_TOOLS.crearImagenVRAM(32, 32, Transparency.OPAQUE);
		final Graphics2D gVacio = vacio.createGraphics();
		gVacio.setColor(Color.DARK_GRAY);
		gVacio.fillRect(0, 0, 32, 32);
		gVacio.dispose();

		texturaError = Globales.FUNCIONES.TEXTURAS_TOOLS.crearTexturaError(32);

		guardar(TEXTURA_x32_VACIO, vacio);
		guardar(TEXTURA_ERROR, texturaError);
		guardar(TEXTURA_TRANSPARENTE,
				Globales.FUNCIONES.TEXTURAS_TOOLS.crearImagenVRAM(1, 1, Transparency.TRANSLUCENT));
		guardar(idTexturaContornoTile,
				Globales.FUNCIONES.TEXTURAS_TOOLS.crearImagenRectanguloContornoEnVRAM(Constantes.LADO_TILE, Color.RED));
		guardar(idTexturaContornoGroupTile, Globales.FUNCIONES.TEXTURAS_TOOLS
				.crearImagenRectanguloContornoEnVRAM(Constantes.LADO_TILE * 2, Color.BLUE));

		// 2. Objetos y Árboles
		final HojaSprite hojaArboles = new HojaSprite("/imagenes/texturas/trees.png", 32, false);
		guardar(TEXTURA_x32_ARBOL_1, hojaArboles.getSprite(0));
		guardar(TEXTURA_x32_ARBOL_2, hojaArboles.getSprite(1));
		guardar(TEXTURA_x32_ARBOL_3, hojaArboles.getSprite(2));
		guardar(TEXTURA_x32_ARBOL_0, hojaArboles.getSprite(3));

		final HojaSprite hojaArbolesNevados = new HojaSprite("/imagenes/texturas/treesNevado.png", 32, false);
		guardar(TEXTURA_x32_ARBOL_1_NEVADO, hojaArbolesNevados.getSprite(0));
		guardar(TEXTURA_x32_ARBOL_2_NEVADO, hojaArbolesNevados.getSprite(1));
		guardar(TEXTURA_x32_ARBOL_3_NEVADO, hojaArbolesNevados.getSprite(2));
		guardar(TEXTURA_x32_ARBOL_0_NEVADO, hojaArbolesNevados.getSprite(3));

		// 3. Items y Granadas
		final HojaSprite hojaItems16 = new HojaSprite("/imagenes/objetos/items.png", 16, false);
		guardar(TEXTURA_x16_ANILLO_PLATA, hojaItems16.getSprite(0));
		guardar(TEXTURA_x16_ANILLO_ORO, hojaItems16.getSprite(13));
		guardar(TEXTURA_x16_ESMERALDA, hojaItems16.getSprite(28));
		guardar(TEXTURA_x16_POCION_ROJA, hojaItems16.getSprite(50));
		guardar(TEXTURA_x16_POCION_AZUL, hojaItems16.getSprite(48));
		guardar(TEXTURA_x16_BOTAS_CUERO_MARRON, hojaItems16.getSprite(122));
		guardar(TEXTURA_x16_PISTOLA, Globales.FUNCIONES.CARGADOR_RECURSOS
				.cargarImagenCompatibleTranslucida("/imagenes/objetos/gun16x16.png"));

		cargarGranadas("/imagenes/objetos/granadas.png", 16, TEXTURA_X16_GRANADA_1);

		final HojaSprite hojaItems10 = new HojaSprite("/imagenes/objetos/itemsx10.png", 10, false);
		guardar(TEXTURA_x10_POCION_ROJA, hojaItems10.getSprite(50));
		guardar(TEXTURA_x10_POCION_AZUL, hojaItems10.getSprite(48));
		guardar(TEXTURA_x10_BOTAS_CUERO_MARRON, hojaItems10.getSprite(122));
		guardar(TEXTURA_x8_PISTOLA,
				Globales.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/objetos/gun8x8.png"));
		guardar(TEXTURA_x4_BALA,
				Globales.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/objetos/bala.png"));

		cargarGranadas("/imagenes/objetos/granadas.png", 10, TEXTURA_X10_GRANADA_1);

		// 4. Partículas, Efectos y Estructuras
		final HojaSprite hojaParticulas = new HojaSprite("/imagenes/objetos/sangrex8.png", 8, false);
		guardar(TEXTURA_X8_PARTICULA_SANGRE, hojaParticulas.getSprite(0));

		final BufferedImage auxExp = Globales.FUNCIONES.CARGADOR_RECURSOS
				.cargarImagenCompatibleTranslucida("/imagenes/sprites/explosion.png");
		guardar(TEXTURA_x50_EXPLOSION, Globales.FUNCIONES.TEXTURAS_TOOLS.redimensionar(auxExp, 50 * 10, 50 * 5));

		final BufferedImage auxCasa = Globales.FUNCIONES.CARGADOR_RECURSOS
				.cargarImagenCompatibleTranslucida("/imagenes/texturas/house/1.png");
		guardar(TEXTURA_X64_CASA1, Globales.FUNCIONES.TEXTURAS_TOOLS.redimensionar(auxCasa, 64, 64));

		// 5. Terrenos y Autotiles
		final HojaSprite hojaTerrenos = new HojaSprite("/imagenes/texturas/terrenos16.png", 16, false);

		cargarSetTerreno(hojaTerrenos, 0, INICIO_AUTOTILE_CESPED);
		cargarSetTerreno(hojaTerrenos, 1, INICIO_AUTOTILE_TIERRA);
		cargarSetTerreno(hojaTerrenos, 2, INICIO_AUTOTILE_TIERRA_2);
		cargarSetTerreno(hojaTerrenos, 3, INICIO_AUTOTILE_ARENA);
		cargarSetTerreno(hojaTerrenos, 4, INICIO_AUTOTILE_ASFALTO);
		cargarSetTerreno(hojaTerrenos, 5, INICIO_AUTOTILE_PIEDRA);

		// 3 Filas de Agua
		cargarSetTerreno(hojaTerrenos, 6, INICIO_AUTOTILE_AGUA);
		cargarSetTerreno(hojaTerrenos, 7, INICIO_AUTOTILE_AGUA + 20);
		cargarSetTerreno(hojaTerrenos, 8, INICIO_AUTOTILE_AGUA + 40);

		// Terrenos restantes
		cargarSetTerreno(hojaTerrenos, 9, INICIO_AUTOTILE_CESPED_2);
		cargarSetTerreno(hojaTerrenos, 10, INICIO_AUTOTILE_CESPED_3);
		cargarSetTerreno(hojaTerrenos, 11, INICIO_AUTOTILE_CESPED_3_NEVADO);
		cargarSetTerreno(hojaTerrenos, 12, INICIO_AUTOTILE_VACIO);
	}

	private static void cargarSetTerreno(final HojaSprite hoja, final int fila, final int idBase) {
		final int inicioSprite = fila * 20;
		for (int i = 0; i < 20; i++) {
			guardar(idBase + i, hoja.getSprite(inicioSprite + i));
		}
	}

	// =========================================================================
	// === 5. ALMACENAMIENTO Y CONSULTAS EN TIEMPO CONSTANTE O(1)
	// =========================================================================

	private static void guardar(final int id, final BufferedImage img) {
		if (img == null) {
			System.err.println(
					"Advertencia: No se pudo cargar la imagen para el ID " + id + ". Usando textura de error.");
			registrarEnCatalogo(id, texturaError);
			TEXTURAS.put(id, texturaError);
			return;
		}

		final BufferedImage vram = Globales.FUNCIONES.TEXTURAS_TOOLS.convertirAVRAM(img);
		registrarEnCatalogo(id, vram);
		TEXTURAS.put(id, vram);
	}

	private static void registrarEnCatalogo(final int id, final BufferedImage img) {
		final int indice = id + OFFSET_INDICE;
		if (indice < 0) {
			return;
		}

		if (indice >= catalogoTexturas.length) {
			final int nuevoTam = Math.max(indice + 1, catalogoTexturas.length * 2);
			final BufferedImage[] nuevoArr = new BufferedImage[nuevoTam];
			System.arraycopy(catalogoTexturas, 0, nuevoArr, 0, catalogoTexturas.length);
			catalogoTexturas = nuevoArr;
		}

		catalogoTexturas[indice] = img;
	}

	/**
	 * Obtiene la textura correspondiente al identificador en tiempo constante
	 * $O(1)$ sin generar objetos en el Heap (Zero-GC / Zero-Autoboxing).
	 *
	 * @param codTextura Identificador de la textura solicitada.
	 * @return Instancia de {@link BufferedImage} lista en VRAM.
	 */
	public static BufferedImage getTextura(final int codTextura) {
		final int indice = codTextura + OFFSET_INDICE;
		if ((indice >= 0) && (indice < catalogoTexturas.length)) {
			final BufferedImage img = catalogoTexturas[indice];
			if (img != null) {
				return img;
			}
		}
		return texturaError;
	}

	private static void cargarGranadas(final String ruta, final int tamano, final int idBase) {
		final BufferedImage granadasBase = Globales.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida(ruta);
		final BufferedImage aux = Globales.FUNCIONES.TEXTURAS_TOOLS.redimensionar(granadasBase, tamano * 8, tamano);
		final HojaSprite hoja = new HojaSprite(aux, tamano, false);
		for (int i = 0; i < 8; i++) {
			guardar(idBase + i, hoja.getSprite(i));
		}
	}
}