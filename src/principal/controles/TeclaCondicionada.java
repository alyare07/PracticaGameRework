package principal.controles;

public abstract class TeclaCondicionada extends Tecla {
	
	public TeclaCondicionada(int codigo,final String nombre) {
		super(codigo,nombre);
	}

	public TeclaCondicionada(int codigo, boolean accionarInvertible,final String nombre) {
		super(codigo, accionarInvertible,nombre);
	}
	
	@Override
	public void presionar() {
		if(this.condicion()) {
			super.presionar();
		}
	}
	
	public abstract boolean condicion();

	

}
