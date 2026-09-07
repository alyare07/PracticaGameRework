package principal.dialogos;

public class OpcionDialogo {

	private final String texto;
	private final Runnable alSeleccionar;

	public OpcionDialogo(final String texto, final Runnable alSeleccionar) {
		this.texto = (texto != null) ? texto : "";
		this.alSeleccionar = alSeleccionar;
	}

	public String getTexto() {
		return this.texto;
	}

	public void seleccionar() {
		if (this.alSeleccionar != null) {
			this.alSeleccionar.run();
		}
	}
}