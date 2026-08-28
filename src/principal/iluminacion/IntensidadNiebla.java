package principal.iluminacion;

/**
 * Define los niveles de densidad de neblina en el mapa.
 * 
 * @version 1.0
 */
public enum IntensidadNiebla {

	DESACTIVADA(0.0f), LEVE(0.18f), MODERADA(0.40f), INTENSA(0.68f);

	private final float opacidad;

	IntensidadNiebla(final float opacidad) {
		this.opacidad = opacidad;
	}

	public float getOpacidad() {
		return this.opacidad;
	}
}