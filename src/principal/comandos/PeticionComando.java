package principal.comandos;

/**
 * Estructura de transferencia liviana para encolar comandos de forma segura entre hilos.
 */
public class PeticionComando {

	private final String textoComando;
	private final EmisorRespuesta emisor;

	public PeticionComando(final String textoComando, final EmisorRespuesta emisor) {
		this.textoComando = textoComando;
		this.emisor = emisor;
	}

	public String getTextoComando() {
		return this.textoComando;
	}

	public EmisorRespuesta getEmisor() {
		return this.emisor;
	}
}