package principal.maquinaestado.estados.editor.herramientas;

/**
 * Modos y herramientas de trazado geométrico disponibles en la paleta de suelos.
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public enum TipoHerramientaDibujo {

	PINCEL("Lápiz / Pincel"),
	BOTE_RELLENO("Bote de Pintura (Flood Fill)"),
	RECTANGULO_HUECO("Rectángulo Contorno"),
	RECTANGULO_RELLENO("Rectángulo Relleno"),
	REEMPLAZAR_GLOBAL("Reemplazar Tipo de Terreno");

	private final String nombreVisible;

	TipoHerramientaDibujo(final String nombreVisible) {
		this.nombreVisible = nombreVisible;
	}

	public String getNombreVisible() {
		return this.nombreVisible;
	}
}