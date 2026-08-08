package principal.inventario;


public class Info {
	private final String label;
	private String valor;
	public Info(final String label, final String valor) {
		this.label = label;
		this.valor = valor;
	}
	
	public void establecerValor(final String valor) {
		this.valor = valor;
	}
	
	
	public String getValor() {
		return this.valor;
	}
	
	public String getTexto() {
		return this.label;
	}
	
	
	
	
	
	
}
