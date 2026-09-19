package principal.maquinaestado.estados.editor.metadatos;

import java.awt.Color;

/**
 * Catálogo de atmósferas e iluminación ambiental uniforme para interiores
 * (Hogares, Casas, Tabernas, Tiendas). Ilumina todo el mapa sin necesidad de
 * anclar fuentes de luz puntuales a entidades.
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public enum TipoIluminacionInterior {

	HOGARENA("Hogareña (Cálida)", new Color(255, 215, 140, 40)),
	TABERNA("Taberna (Ámbar / Acogedora)", new Color(255, 160, 60, 65)),
	CLARA("Luz Clara (Plena Iluminación)", new Color(240, 245, 255, 10)),
	TENUE("Penumbra Misteriosa", new Color(30, 20, 50, 130)),
	PERSONALIZADA("Personalizada (RGB)", null);

	private final String nombreVisible;
	private final Color colorAmbiente;

	TipoIluminacionInterior(final String nombreVisible, final Color colorAmbiente) {
		this.nombreVisible = nombreVisible;
		this.colorAmbiente = colorAmbiente;
	}

	public String getNombreVisible() {
		return this.nombreVisible;
	}

	public Color getColorAmbiente() {
		return this.colorAmbiente;
	}
}