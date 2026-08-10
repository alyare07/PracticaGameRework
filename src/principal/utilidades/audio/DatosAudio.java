package principal.utilidades.audio;

public class DatosAudio {

	private final String ruta;
	private final double volumen;

	public DatosAudio(final String ruta, final double volumen) {
		this.ruta = ruta;
		this.volumen = volumen;
	}

	public String getRuta() {
		return this.ruta;
	}

	public double getVolumen() {
		return this.volumen;
	}
}