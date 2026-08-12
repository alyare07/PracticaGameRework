package principal.controles;

/**
 * Tecla condicionada que ejecuta un método {@link #accionar()} una única vez al
 * presionarse.
 */
public abstract class TeclaAccionCondicionada extends TeclaCondicionada {

	public TeclaAccionCondicionada(final int codigo, final String nombre) {
		super(codigo, nombre);
	}

	public TeclaAccionCondicionada(final int codigo, final boolean accionarInvertible, final String nombre) {
		super(codigo, accionarInvertible, nombre);
	}

	@Override
	public void presionar() {
		if (this.condicion()) {
			final boolean estabaPresionada = this.teclaFisicaPresionada;
			super.presionar();

			// Se ejecuta la acción SOLO en la pulsación inicial, no en el auto-repeat del
			// SO
			if (!estabaPresionada) {
				this.accionar();
			}
		}
	}

	public abstract void accionar();
}