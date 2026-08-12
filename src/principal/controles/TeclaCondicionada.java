package principal.controles;

/**
 * Tecla que solo procesa la entrada si se cumple una condición previa.
 */
public abstract class TeclaCondicionada extends Tecla {

	public TeclaCondicionada(final int codigo, final String nombre) {
		super(codigo, nombre);
	}

	public TeclaCondicionada(final int codigo, final boolean accionarInvertible, final String nombre) {
		super(codigo, accionarInvertible, nombre);
	}

	@Override
	public void presionar() {
		if (this.condicion()) {
			super.presionar();
		}
	}

	public abstract boolean condicion();
}