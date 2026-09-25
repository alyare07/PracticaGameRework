package principal.entes.objetos.recursos.arboles;

import principal.entes.objetos.items.comidas.BayaSilvestre;
import principal.entes.objetos.items.materiales.RecursoMaterial;
import principal.recursos.ClaveHoja;
import principal.utilidades.Globales;

/**
 * Catálogo maestro de especies arbóreas ($32 \times 48\text{ px}$) correspondientes
 * a la hoja {@link ClaveHoja#ARBOLES_32x48} (Índices 0 al 18, tocón universal en índice 19).
 */
public enum TipoArbol {

	// Fila 0
	ROBLE(0, "Roble Común", 100.0, 4, 2),
	PINO(1, "Pino Conífera", 110.0, 5, 2),
	ABEDUL(2, "Abedul Blanco", 90.0, 4, 1),
	FRUTAL_MANZANO(3, "Manzano Silvestre", 100.0, 3, 2),
	OTONO(4, "Árbol Dorado de Otoño", 95.0, 4, 2),

	// Fila 1
	ROJO(5, "Árbol Carmesí", 105.0, 4, 2),
	SAUCE_LLORON(6, "Sauce con Vides", 120.0, 5, 2),
	AZULINO(7, "Árbol Azul Místico", 115.0, 5, 2),
	PINO_NEVADO(8, "Pino Nevado", 110.0, 5, 2),
	SECO(9, "Árbol Seco", 70.0, 3, 1),

	// Fila 2
	TENEBROSO(10, "Árbol Tenebroso", 130.0, 4, 2),
	PALMERA(11, "Palmera Costera", 85.0, 3, 2),
	PALMERA_SECA(12, "Palmera Seca", 65.0, 2, 1),
	CACTUS(13, "Cactus del Desierto", 60.0, 2, 1),
	SAKURA(14, "Cerezo Sakura", 95.0, 4, 2),

	// Fila 3
	CHAMPINON_ROJO(15, "Champiñón Gigante Rojo", 80.0, 3, 1),
	CHAMPINON_PURPURA(16, "Champiñón Gigante Púrpura", 85.0, 3, 1),
	JUNGLA(17, "Árbol Tropical de Jungla", 140.0, 6, 3),
	CORRUPTO_LAVA(18, "Árbol Volcánico de Lava", 150.0, 4, 2);

	public static final int SPRITE_TOCON = 19;

	private final int spriteIndex;
	private final String nombre;
	private final double durabilidadBase;
	private final int maderaCopa;
	private final int maderaTocon;

	// Coordenadas relativas del pie del tronco (32x48)
	private final int colX = 10;
	private final int colY = 32;
	private final int colAncho = 12;
	private final int colAlto = 14;

	TipoArbol(final int spriteIndex, final String nombre, final double durabilidadBase,
			final int maderaCopa, final int maderaTocon) {
		this.spriteIndex = spriteIndex;
		this.nombre = nombre;
		this.durabilidadBase = durabilidadBase;
		this.maderaCopa = maderaCopa;
		this.maderaTocon = maderaTocon;
	}

	/**
	 * Entrega el botín estándar según la especie biológica.
	 */
	public DispensadorBotinArbol getDispensadorPorDefecto() {
		return (arbol, mundo, esTocon) -> {
			final int dropX = arbol.getCentroX() - 4;
			final int dropY = arbol.getPosicionYInt() + 24;

			if (esTocon) {
				mundo.meterEntidad(RecursoMaterial.crearMadera(dropX, dropY, this.maderaTocon));
			} else {
				mundo.meterEntidad(RecursoMaterial.crearMadera(dropX, dropY, this.maderaCopa));

				// Botines especiales adicionales
				if (this == FRUTAL_MANZANO) {
					mundo.meterEntidad(new BayaSilvestre(dropX + 6, dropY, 3));
				} else if (this == CORRUPTO_LAVA) {
					mundo.meterEntidad(new RecursoMaterial(dropX + 6, dropY, 2, RecursoMaterial.COD_CARBON));
				}
			}
			Globales.GESTOR_PARTICULAS.emitirPolvoPaso(arbol.getCentroX(), arbol.getCentroY(), esTocon ? 10 : 18);
		};
	}

	public int getSpriteIndex() {
		return this.spriteIndex;
	}

	public String getNombre() {
		return this.nombre;
	}

	public double getDurabilidadBase() {
		return this.durabilidadBase;
	}

	public int getColX() {
		return this.colX;
	}

	public int getColY() {
		return this.colY;
	}

	public int getColAncho() {
		return this.colAncho;
	}

	public int getColAlto() {
		return this.colAlto;
	}
}