package principal.controles;

public abstract class TeclaAccionCondicionada extends TeclaCondicionada {

	public TeclaAccionCondicionada(int codigo,final String nombre) {
		super(codigo,nombre);
	}
	
	public TeclaAccionCondicionada(int codigo, boolean accionarInvertible,final String nombre) {
		super(codigo, accionarInvertible,nombre);
	}
	
	@Override
	public void presionar() {
		if(this.condicion()) {
			if(this.accionarInvertible) {
				this.presionada = !this.presionada;
			}else if(!presionada) {
				this.presionada = true;
			}
			this.accionar();
		}
	}

	
	public abstract void accionar();

}
