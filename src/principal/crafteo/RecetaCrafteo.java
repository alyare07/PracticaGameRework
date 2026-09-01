package principal.crafteo;

import principal.entes.objetos.items.Item;
import principal.inventario.Inventario;

public class RecetaCrafteo {

	private final String idReceta;
	private final String nombreVisible;
	private final EstacionCrafteo estacionRequerida;
	private final Ingrediente[] ingredientes;
	private final Item itemResultado;

	public RecetaCrafteo(final String idReceta, final String nombreVisible, final EstacionCrafteo estacionRequerida,
			final Ingrediente[] ingredientes, final Item itemResultado) {
		this.idReceta = idReceta;
		this.nombreVisible = nombreVisible;
		this.estacionRequerida = (estacionRequerida != null) ? estacionRequerida : EstacionCrafteo.MANUAL;
		this.ingredientes = (ingredientes != null) ? ingredientes : new Ingrediente[0];
		this.itemResultado = itemResultado;
	}

	public boolean puedeCraftear(final Inventario inventario) {
		if ((inventario == null) || (this.itemResultado == null)) {
			return false;
		}

		for (int i = 0; i < this.ingredientes.length; i++) {
			final Ingrediente ing = this.ingredientes[i];
			final int disponible = inventario.contarMunicionTotal(ing.getCodModeloItem());
			if (disponible < ing.getCantidad()) {
				return false;
			}
		}

		return true;
	}

	public boolean craftear(final Inventario inventario) {
		if (!this.puedeCraftear(inventario)) {
			return false;
		}

		// 1. Descuenta los materiales necesarios
		for (int i = 0; i < this.ingredientes.length; i++) {
			final Ingrediente ing = this.ingredientes[i];
			inventario.extraerMunicion(ing.getCodModeloItem(), ing.getCantidad());
		}

		// 2. Entrega el resultado clonado
		return inventario.agregarObjeto((Item) this.itemResultado.copiar());
	}

	public String getIdReceta() {
		return this.idReceta;
	}

	public String getNombreVisible() {
		return this.nombreVisible;
	}

	public EstacionCrafteo getEstacionRequerida() {
		return this.estacionRequerida;
	}

	public Ingrediente[] getIngredientes() {
		return this.ingredientes;
	}

	public Item getItemResultado() {
		return this.itemResultado;
	}
}