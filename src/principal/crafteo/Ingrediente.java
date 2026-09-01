package principal.crafteo;

public class Ingrediente {

	private final String codModeloItem;
	private final int cantidad;

	public Ingrediente(final String codModeloItem, final int cantidad) {
		this.codModeloItem = codModeloItem;
		this.cantidad = Math.max(1, cantidad);
	}

	public String getCodModeloItem() {
		return this.codModeloItem;
	}

	public int getCantidad() {
		return this.cantidad;
	}
}