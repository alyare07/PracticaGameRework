package principal.construccion;

import principal.entes.modelos.item.ListaModelosItem;
import principal.utilidades.Textura;

public enum TipoEstructura {

	MURO_MADERA("Muro de Madera", 16, 16, 150.0, Textura.TEXTURA_x16_MURO_PIEDRA_NEGRA,
			ListaModelosItem.COD_RECURSO_MADERA, 4),
	MURO_PIEDRA("Muro de Piedra", 16, 16, 350.0, Textura.TEXTURA_x16_MURO_PIEDRA_NEGRA,
			ListaModelosItem.COD_RECURSO_PIEDRA, 4),
	VALLA_MADERA("Valla de Madera", 16, 16, 80.0, Textura.TEXTURA_x16_MURO_PIEDRA_NEGRA,
			ListaModelosItem.COD_RECURSO_MADERA, 2);

	private final String nombre;
	private final int ancho;
	private final int alto;
	private final double durabilidadMaxima;
	private final int codTextura;
	private final String codMaterialRequerido;
	private final int cantidadMaterialRequerido;

	TipoEstructura(final String nombre, final int ancho, final int alto, final double durabilidadMaxima,
			final int codTextura, final String codMaterialRequerido, final int cantidadMaterialRequerido) {
		this.nombre = nombre;
		this.ancho = ancho;
		this.alto = alto;
		this.durabilidadMaxima = durabilidadMaxima;
		this.codTextura = codTextura;
		this.codMaterialRequerido = codMaterialRequerido;
		this.cantidadMaterialRequerido = cantidadMaterialRequerido;
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

	public int getCodTextura() {
		return this.codTextura;
	}

	public String getCodMaterialRequerido() {
		return this.codMaterialRequerido;
	}

	public int getCantidadMaterialRequerido() {
		return this.cantidadMaterialRequerido;
	}
}