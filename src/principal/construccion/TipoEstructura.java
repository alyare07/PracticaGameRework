package principal.construccion;

import java.awt.image.BufferedImage;

import principal.entes.objetos.items.materiales.RecursoMaterial;
import principal.recursos.ClaveHoja;
import principal.utilidades.Globales;
import principal.utilidades.HojaSprite;

public enum TipoEstructura {

	MURO_MADERA("Muro de Madera", 16, 16, 150.0, ClaveHoja.DUNGEON_16, 813, RecursoMaterial.COD_MADERA, 4),
	MURO_PIEDRA("Muro de Piedra", 16, 16, 350.0, ClaveHoja.DUNGEON_16, 813, RecursoMaterial.COD_PIEDRA, 4),
	VALLA_MADERA("Valla de Madera", 16, 16, 80.0, ClaveHoja.DUNGEON_16, 813, RecursoMaterial.COD_MADERA, 2);

	private final String nombre;
	private final int ancho;
	private final int alto;
	private final double durabilidadMaxima;
	private final ClaveHoja hojaTextura;
	private final int spriteIndex;
	private final String codMaterialRequerido;
	private final int cantidadMaterialRequerido;

	TipoEstructura(final String nombre, final int ancho, final int alto, final double durabilidadMaxima,
			final ClaveHoja hojaTextura, final int spriteIndex, final String codMaterialRequerido,
			final int cantidadMaterialRequerido) {
		this.nombre = nombre;
		this.ancho = ancho;
		this.alto = alto;
		this.durabilidadMaxima = durabilidadMaxima;
		this.hojaTextura = hojaTextura;
		this.spriteIndex = spriteIndex;
		this.codMaterialRequerido = codMaterialRequerido;
		this.cantidadMaterialRequerido = cantidadMaterialRequerido;
	}

	public BufferedImage getTextura() {
		final HojaSprite hoja = Globales.GESTOR_TEXTURAS.getHoja(this.hojaTextura);
		return (hoja != null) ? hoja.getSprite(this.spriteIndex) : Globales.GESTOR_TEXTURAS.getTexturaError();
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

	public double getDurabilidadMaxima() {
		return this.durabilidadMaxima;
	}

	public String getCodMaterialRequerido() {
		return this.codMaterialRequerido;
	}

	public int getCantidadMaterialRequerido() {
		return this.cantidadMaterialRequerido;
	}

	@Deprecated
	public int getCodTextura() {
		return this.spriteIndex;
	}
}