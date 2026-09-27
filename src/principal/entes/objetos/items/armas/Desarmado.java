package principal.entes.objetos.items.armas;

import java.util.ArrayList;

import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.armas.distancia.fuego.municiones.Municion;

public class Desarmado extends Arma {
	private static final long serialVersionUID = 3731254166805799834L;

	/** Instancia única compartida para Zero-GC en consultas en caliente */
	public static final Desarmado INSTANCIA = new Desarmado();

	private Desarmado() {
		super("", 0, 0, false);
	}

	@Override
	public Municion getMunicion() {
		return null;
	}

	@Override
	protected void rellenarInfo(final ArrayList<String> listaInfo) {

	}

	@Override
	public Objeto copiar() {
		return INSTANCIA;
	}

}
