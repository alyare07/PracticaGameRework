package principal.entes.modelos.complemento;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;

import principal.entes.objetos.Complemento;
import principal.recursos.ClaveHoja;
import principal.utilidades.Globales;
import principal.utilidades.HojaSprite;

public enum TipoModeloComplemento {

	// 0. Barrera Invisible
	BARRERA_INVISIBLE(0, "Barrera Invisible", 32, 32, null, 0, true, false, false, 0, 0, 0, 0),

	// 1 AL 19: CATÁLOGO COMPLETO DE ÁRBOLES DECORATIVOS (32x48)
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

	// 20. Estructura rígida
	CASA_1(20, "Casa Grande", 64, 64, ClaveHoja.CASA_1, 0, true, true, false, 5, 43, 6, 0);

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