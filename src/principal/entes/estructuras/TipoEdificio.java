package principal.entes.estructuras;

import java.awt.image.BufferedImage;

import principal.recursos.ClaveHoja;
import principal.utilidades.Globales;
import principal.utilidades.HojaSprite;

/**
 * Catálogo maestro de edificios del juego ($64 \times 64\text{ px}$) correspondientes
 * a la hoja {@link ClaveHoja#CASAS_64}. Define su función de rol, lore ambiental y geometría de acceso.
 */
public enum TipoEdificio {

	// Fila 0
	CASA_CAMPO(0, "Cottage de Campo", "Vivienda rústica con chimenea y flores en la ventana.", "interior_casa_campo"),
	TABERNA_MADERA(1, "Taberna del Viajero", "Posada de troncos con farol en la entrada. Huele a cerveza y asado.", "interior_taberna"),
	HERRERIA(2, "Herrería y Forja", "Taller de herrería con yunque exterior y un horno de fundición encendido.", "interior_herreria"),
	CABANA_INVERNAL(3, "Cabaña Invernal", "Refugio de troncos reforzados con nieve acumulada en el techo.", "interior_cabana_invernal"),
	MOLINO_VIENTO(4, "Molino de Viento", "Estructura de mampostería con aspas giratorias para molienda de grano.", "interior_molino"),

	// Fila 1
	TIENDA_MAGIA(5, "Boticario Arcano", "Emporio místico de techo púrpura. Emana vapores arcanos e incienso.", "interior_magia"),
	IGLESIA_TEMPLO(6, "Santuario de Fe", "Capilla de piedra coronada con un campanario y altar sagrado.", "interior_iglesia"),
	CABANA_PESCADOR(7, "Palafito Costero", "Cabaña de pescador elevada sobre pilotes de madera para el agua.", "interior_pescador"),
	MERCADO_TIENDA(8, "Puesto Comercial", "Tienda general de abarrotes con marquesina a rayas y mostrador exterior.", "interior_tienda"),
	TORREON_CASTILLO(9, "Torreón de Guardia", "Bastión fortificado de sillares de piedra con almenas defensivas y antorchas.", "interior_torreon");

	public static final int LADO = 64;

	private final int spriteIndex;
	private final String nombre;
	private final String descripcionLore;
	private final String mundoInteriorPorDefecto;

	TipoEdificio(final int spriteIndex, final String nombre, final String descripcionLore,
			final String mundoInteriorPorDefecto) {
		this.spriteIndex = spriteIndex;
		this.nombre = nombre;
		this.descripcionLore = descripcionLore;
		this.mundoInteriorPorDefecto = mundoInteriorPorDefecto;
	}

	public BufferedImage getTextura() {
		if (Globales.GESTOR_TEXTURAS == null) {
			return null;
		}
		final HojaSprite hoja = Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.CASAS_64);
		return (hoja != null) ? hoja.getSprite(this.spriteIndex) : Globales.GESTOR_TEXTURAS.getTexturaError();
	}

	public int getSpriteIndex() {
		return this.spriteIndex;
	}

	public String getNombre() {
		return this.nombre;
	}

	public String getDescripcionLore() {
		return this.descripcionLore;
	}

	public String getMundoInteriorPorDefecto() {
		return this.mundoInteriorPorDefecto;
	}
}