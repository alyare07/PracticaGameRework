package principal.entes.objetos.cofres;

import java.awt.Graphics2D;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import principal.entes.Ente;
import principal.entes.criaturas.jugador.Jugador;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.Item;
import principal.interaccion.Interactuable;
import principal.inventario.Contenedor;
import principal.inventario.vault.InventarioVault;
import principal.inventario.vault.InventarioVault.EstadoInventario;
import principal.persistencia.json.RegistroEntidades;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

public abstract class Cofre extends Objeto implements Contenedor, Interactuable {

	private static final long serialVersionUID = 2158894619671109923L;

	private final InventarioVault INVENTARIO;
	private final String NOMBRE;

	public Cofre(final int x, final int y, final int cantSlot, final int cantMaxSlotH, final String nombre) {
		super(x, y);
		this.NOMBRE = (nombre != null) ? nombre : "Cofre";
		this.INVENTARIO = new InventarioVault(this, cantSlot, cantMaxSlotH, this.NOMBRE);
	}

	@Override
	public void pintar(final Graphics2D g) {
		super.pintar(g);
	}

	@Override
	public void actualizar() {
		this.INVENTARIO.actualizarEstadoCofre();
	}

	@Override
	public String getTextoPrompt() {
		return "Abrir " + this.NOMBRE;
	}

	@Override
	public void interactuar(final Jugador jugador) {
		if (this.INVENTARIO.getEstadoInventario() == EstadoInventario.CERRADO) {
			this.INVENTARIO.abrir();
			GestorSonido.reproducir(IDSonido.SELECT);
		}
	}

	@Override
	public boolean puedeInteractuar(final Jugador jugador) {
		return !this.estaEliminado() && (this.INVENTARIO.getEstadoInventario() == EstadoInventario.CERRADO);
	}

	public boolean meterItem(final Item i) {
		return this.INVENTARIO.agregarItem(i);
	}

	@Override
	public InventarioVault getInventario() {
		return this.INVENTARIO;
	}

	@SuppressWarnings("unchecked")
	public JSONArray getListaJsonItems() {
		final JSONArray lista = new JSONArray();
		for (final Item i : this.INVENTARIO.getItems()) {
			final JSONObject sobre = RegistroEntidades.exportar(i);
			if (sobre != null) {
				lista.add(sobre);
			}
		}
		return lista;
	}

	@Override
	public Ente getEntePropietario() {
		return this;
	}

	@Override
	public void eliminar() {
		this.eliminado = true;
	}

	@Override
	public boolean estaEliminado() {
		return this.eliminado;
	}

	@Override
	public String getNombreContenedor() {
		return this.NOMBRE;
	}

	@Override
	public String toString() {
		return "Cofre [X=" + this.getPosicionXInt() + ", Y=" + this.getPosicionYInt() + ", estado="
				+ this.INVENTARIO.getEstadoInventario() + "]";
	}
}