package principal.entes.modelos.complemento;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;

import principal.entes.objetos.Complemento;
import principal.recursos.ClaveHoja;
import principal.utilidades.Globales;
import principal.utilidades.HojaSprite;

/**
 * Catálogo maestro Flyweight declarativo de complementos del escenario (Zero-GC
 * / O(1)). Incluye los 19 árboles grandes (32x48), casas (64x64) y los 25
 * objetos decorativos (16x16).
 */
public enum TipoModeloComplemento {

	// =========================================================================
	// 0. BARRERA INVISIBLE
	// =========================================================================
	BARRERA_INVISIBLE(0, "Barrera Invisible", 32, 32, null, 0, true, false, false, 0, 0, 0, 0),

	// =========================================================================
	// 1 AL 19: ÁRBOLES DECORATIVOS GRANDES (ClaveHoja.ARBOLES_32x48)
	// =========================================================================
	ARBOL_ROBLE(1, "Roble Decorativo", 32, 48, ClaveHoja.ARBOLES_32x48, 0, true, true, true, 10, 32, 12, 14),
	ARBOL_PINO(2, "Pino Decorativo", 32, 48, ClaveHoja.ARBOLES_32x48, 1, true, true, true, 10, 32, 12, 14),
	ARBOL_ABEDUL(3, "Abedul Decorativo", 32, 48, ClaveHoja.ARBOLES_32x48, 2, true, true, true, 10, 32, 12, 14),
	ARBOL_FRUTAL(4, "Manzano Decorativo", 32, 48, ClaveHoja.ARBOLES_32x48, 3, true, true, true, 10, 32, 12, 14),
	ARBOL_OTONO(5, "Árbol Otoño Decorativo", 32, 48, ClaveHoja.ARBOLES_32x48, 4, true, true, true, 10, 32, 12, 14),
	ARBOL_ROJO(6, "Árbol Rojo Decorativo", 32, 48, ClaveHoja.ARBOLES_32x48, 5, true, true, true, 10, 32, 12, 14),
	ARBOL_SAUCE(7, "Sauce Llorón Decorativo", 32, 48, ClaveHoja.ARBOLES_32x48, 6, true, true, true, 10, 32, 12, 14),
	ARBOL_AZUL(8, "Árbol Azul Místico", 32, 48, ClaveHoja.ARBOLES_32x48, 7, true, true, true, 10, 32, 12, 14),
	ARBOL_PINO_NEVADO(9, "Pino Nevado Decorativo", 32, 48, ClaveHoja.ARBOLES_32x48, 8, true, true, true, 10, 32, 12,
			14),
	ARBOL_SECO(10, "Árbol Seco Decorativo", 32, 48, ClaveHoja.ARBOLES_32x48, 9, true, true, true, 10, 32, 12, 14),
	ARBOL_TENEBROSO(11, "Árbol Tenebroso", 32, 48, ClaveHoja.ARBOLES_32x48, 10, true, true, true, 10, 32, 12, 14),
	ARBOL_PALMERA(12, "Palmera Decorativa", 32, 48, ClaveHoja.ARBOLES_32x48, 11, true, true, true, 10, 32, 12, 14),
	ARBOL_PALMERA_SECA(13, "Palmera Seca Decorativa", 32, 48, ClaveHoja.ARBOLES_32x48, 12, true, true, true, 10, 32, 12,
			14),
	ARBOL_CACTUS(14, "Cactus del Desierto", 32, 48, ClaveHoja.ARBOLES_32x48, 13, true, true, true, 10, 32, 12, 14),
	ARBOL_SAKURA(15, "Cerezo Sakura", 32, 48, ClaveHoja.ARBOLES_32x48, 14, true, true, true, 10, 32, 12, 14),
	CHAMPINON_ROJO(16, "Champiñón Rojo Gigante", 32, 48, ClaveHoja.ARBOLES_32x48, 15, true, true, true, 10, 32, 12, 14),
	CHAMPINON_PURPURA(17, "Champiñón Púrpura Gigante", 32, 48, ClaveHoja.ARBOLES_32x48, 16, true, true, true, 10, 32,
			12, 14),
	ARBOL_JUNGLA(18, "Árbol de Jungla", 32, 48, ClaveHoja.ARBOLES_32x48, 17, true, true, true, 10, 32, 12, 14),
	ARBOL_CORRUPTO(19, "Árbol Volcánico de Lava", 32, 48, ClaveHoja.ARBOLES_32x48, 18, true, true, true, 10, 32, 12,
			14),

	// =========================================================================
	// 20. ESTRUCTURA RÍGIDA
	// =========================================================================
	CASA_1(20, "Casa Grande", 64, 64, ClaveHoja.CASA_1, 0, true, true, false, 5, 43, 6, 0),

	// =========================================================================
	// 21 AL 45: OBJETOS DECORATIVOS 16x16 (ClaveHoja.OBJETOS_X16)
	// =========================================================================
	// Fila 0 (Estructuras y Utilería)
	LAPIDA(21, "Lápida de Tumba", 16, 16, ClaveHoja.OBJETOS_X16, 0, true, false, false, 2, 4, 12, 10),
	VALLA_MADERA(22, "Valla de Madera", 16, 16, ClaveHoja.OBJETOS_X16, 1, true, false, false, 0, 4, 16, 10),
	POSTE_FAROL(23, "Farol de Poste", 16, 16, ClaveHoja.OBJETOS_X16, 2, true, false, false, 4, 6, 8, 10),
	CARRETILLA(24, "Carretilla de Madera", 16, 16, ClaveHoja.OBJETOS_X16, 3, true, false, false, 1, 4, 14, 11),
	BANCO_MADERA(25, "Banco de Madera", 16, 16, ClaveHoja.OBJETOS_X16, 4, true, false, false, 1, 6, 14, 9),
	CARTEL_MADERA(26, "Cartel Indicador", 16, 16, ClaveHoja.OBJETOS_X16, 5, true, false, false, 3, 6, 10, 10),
	LETRERO_BAYAS(27, "Cajón de Bayas", 16, 16, ClaveHoja.OBJETOS_X16, 6, true, false, false, 2, 5, 12, 10),
	SACO_PROVISIONES(28, "Saco de Provisiones", 16, 16, ClaveHoja.OBJETOS_X16, 7, true, false, false, 2, 4, 12, 11),
	POZO_PIEDRA(29, "Pozo de Piedra", 16, 16, ClaveHoja.OBJETOS_X16, 8, true, false, false, 1, 2, 14, 13),
	MACETA_PLANTA(30, "Maceta con Planta", 16, 16, ClaveHoja.OBJETOS_X16, 9, true, false, false, 2, 4, 12, 11),

	// Fila 1 (Flora silvestre y suelo)
	HIERBA_SUELO(31, "Brotes de Hierba", 16, 16, ClaveHoja.OBJETOS_X16, 10, false, false, true, 0, 0, 0, 0),
	JUNCOS_AGUA(32, "Juncos de Agua", 16, 16, ClaveHoja.OBJETOS_X16, 11, false, false, true, 0, 0, 0, 0),
	FLORES_SILVESTRES(33, "Flores Silvestres", 16, 16, ClaveHoja.OBJETOS_X16, 12, false, false, true, 0, 0, 0, 0),
	CORONA_ARBUSTO(34, "Corona Silvestre", 16, 16, ClaveHoja.OBJETOS_X16, 13, false, false, true, 0, 0, 0, 0),
	TRONCO_CORTO(35, "Tronco Cortado", 16, 16, ClaveHoja.OBJETOS_X16, 14, true, false, false, 2, 4, 12, 11),
	VASIJA_BARRO(36, "Vasija de Barro", 16, 16, ClaveHoja.OBJETOS_X16, 15, true, false, false, 3, 4, 10, 11),
	HONGO_ROJO_PEQUENO(37, "Hongo Rojo Silvestre", 16, 16, ClaveHoja.OBJETOS_X16, 16, false, false, true, 0, 0, 0, 0),
	HONGO_AZUL_PEQUENO(38, "Hongo Azul Místico", 16, 16, ClaveHoja.OBJETOS_X16, 17, false, false, true, 0, 0, 0, 0),
	CALAVERA_SUELO(39, "Calavera en Suelo", 16, 16, ClaveHoja.OBJETOS_X16, 18, false, false, false, 0, 0, 0, 0),
	LINGOTES_ORO(40, "Pila de Oro Decorativa", 16, 16, ClaveHoja.OBJETOS_X16, 19, true, false, false, 2, 6, 12, 9),

	// Fila 2 (Herramientas, Fuego y Campo)
	YUNQUE_DECORATIVO(41, "Yunque de Herrero", 16, 16, ClaveHoja.OBJETOS_X16, 20, true, false, false, 1, 4, 14, 11),
	VELA_BASE(42, "Vela de Campamento", 16, 16, ClaveHoja.OBJETOS_X16, 21, true, false, false, 2, 4, 12, 11),
	ANTORCHA_POSTE(43, "Antorcha de Poste", 16, 16, ClaveHoja.OBJETOS_X16, 22, true, false, false, 4, 4, 8, 11),
	CAJON_MADERA(44, "Cajón de Suministros", 16, 16, ClaveHoja.OBJETOS_X16, 23, true, false, false, 2, 2, 12, 13),
	ESPANTAPAJAROS(45, "Espantapájaros de Campo", 16, 16, ClaveHoja.OBJETOS_X16, 24, true, false, false, 3, 4, 10, 12);

	private final int id;
	private final String nombre;
	private final int ancho;
	private final int alto;
	private final ClaveHoja claveHoja;
	private final int spriteIndex;
	private final boolean solido;
	private final boolean contieneZonaNoSolida;
	private final boolean esVegetacion;

	private final int colX;
	private final int colY;
	private final int colW;
	private final int colH;
	private final Rectangle margenColision;

	private static final TipoModeloComplemento[] CAT_POR_ID;

	static {
		final TipoModeloComplemento[] valores = values();
		int maxId = 0;
		for (int i = 0; i < valores.length; i++) {
			if (valores[i].id > maxId) {
				maxId = valores[i].id;
			}
		}

		CAT_POR_ID = new TipoModeloComplemento[maxId + 1];
		for (int i = 0; i < valores.length; i++) {
			CAT_POR_ID[valores[i].id] = valores[i];
		}
	}

	TipoModeloComplemento(final int id, final String nombre, final int ancho, final int alto, final ClaveHoja claveHoja,
			final int spriteIndex, final boolean solido, final boolean contieneZonaNoSolida, final boolean esVegetacion,
			final int colX, final int colY, final int colW, final int colH) {
		this.id = id;
		this.nombre = nombre;
		this.ancho = ancho;
		this.alto = alto;
		this.claveHoja = claveHoja;
		this.spriteIndex = spriteIndex;
		this.solido = solido;
		this.contieneZonaNoSolida = solido && contieneZonaNoSolida;
		this.esVegetacion = esVegetacion;
		this.colX = colX;
		this.colY = colY;
		this.colW = colW;
		this.colH = colH;
		this.margenColision = new Rectangle(colX, colY, colW, colH);
	}

	public static TipoModeloComplemento desdeId(final int id) {
		if ((id >= 0) && (id < CAT_POR_ID.length) && (CAT_POR_ID[id] != null)) {
			return CAT_POR_ID[id];
		}
		return ARBOL_ROBLE;
	}

	public BufferedImage getTextura() {
		if ((this.claveHoja != null) && (Globales.GESTOR_TEXTURAS != null)) {
			final HojaSprite hoja = Globales.GESTOR_TEXTURAS.getHoja(this.claveHoja);
			if (hoja != null) {
				return hoja.getSprite(this.spriteIndex);
			}
		}
		return (Globales.GESTOR_TEXTURAS != null) ? Globales.GESTOR_TEXTURAS.getTexturaTransparente() : null;
	}

	public boolean intersecta(final Shape area, final Complemento cPropietario) {
		if (!this.solido || (area == null) || (cPropietario == null)) {
			return false;
		}
		final int x = cPropietario.getPosicionXInt() + this.colX;
		final int y = cPropietario.getPosicionYInt() + this.colY;
		final int w = this.ancho - this.colW - this.colX;
		final int h = this.alto - this.colH - this.colY;

		return area.intersects(x, y, w, h);
	}

	public int getId() {
		return this.id;
	}

	public String getNombre() {
		return this.nombre;
	}

	public int getAncho() {
		return this.ancho;
	}

	public int getAlto() {
		return this.alto;
	}

	public boolean esSolido() {
		return this.solido;
	}

	public boolean contieneZonaNoSolida() {
		return this.contieneZonaNoSolida;
	}

	public boolean esVegetacion() {
		return this.esVegetacion;
	}

	public Rectangle getMargenesInterseccion() {
		return this.margenColision;
	}
}