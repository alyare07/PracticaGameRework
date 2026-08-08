package principal.entes.objetos.items.armas.distancia.fuego.municiones;

public class Municion{
    private final int limite;
    private int cantidad;

    public Municion(final int limite) {
	this.limite = limite;
	this.cantidad = limite;
    }

    public Municion(final int limite, final int cantidad) {
	this.limite = limite;
	this.cantidad = cantidad;
    }

    public boolean utilizarMunicion() {
	if (this.cantidad > 0) {
	    this.cantidad--;
	    return true;
	} else {
	    return false;
	}
    }

    public int getLimite() {
	return this.limite;
    }

    public int getCantidad() {
	return this.cantidad;
    }

    public void restablecer() {
	this.cantidad = this.limite;
    }

    @Override
    public String toString() {
	return String.valueOf(this.cantidad) + "/" + String.valueOf(this.limite);
    }

}
