package principal.entes.objetos.cofres;

import java.awt.Graphics2D;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import principal.entes.Ente;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.Item;
import principal.inventario.Contenedor;
import principal.inventario.vault.InventarioVault;
import principal.utilidades.Globales;

public abstract class Cofre extends Objeto implements Contenedor {

	private static final long serialVersionUID = 2158894619671109923L;

	private final InventarioVault INVENTARIO;
	private final String NOMBRE;

	public Cofre(final int x, final int y, final int cantSlot, final int cantMaxSlotH, final String nombre) {
		super(x, y);
		this.NOMBRE = nombre;
		this.INVENTARIO = new InventarioVault(this, cantSlot, cantMaxSlotH, this.NOMBRE);
	}

	@Override
	public void pintar(final Graphics2D g) {
		super.pintar(g);
	}

	@Override
	public void actualizar() {

	}

	public boolean meterItem(final Item i) {
		return this.INVENTARIO.agregarItem(i);
	}

	@Override
	public InventarioVault getInventario() {
		return this.INVENTARIO;
	}

	@SuppressWarnings("unchecked")
	public JSONObject exportarParaJson() {
		final JSONObject json = new JSONObject();
		json.put("tipo", this.getTipoCofre());
		json.put("x", this.getPosicionXInt());
		json.put("y", this.getPosicionYInt());
		json.put(Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Item.class), this.getListaJsonItems());

		final JSONObject jsonPrincipal = new JSONObject();
		jsonPrincipal.put("tipoObjeto", Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Cofre.class));
		jsonPrincipal.put("entiti", json);
		return jsonPrincipal;
	}

	@SuppressWarnings("unchecked")
	protected JSONArray getListaJsonItems() {
		final JSONArray lista = new JSONArray();
		for (final Item i : this.INVENTARIO.getItems()) {
			lista.add(i.getJsonItem());
		}
		return lista;
	}

	public static Cofre crearDesdeJSON(final JSONObject json) {
		Cofre c = null;
		final String tipo = json.get("tipo").toString();

		if (tipo.equals(Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(CofrePequeño.class))) {
			c = CofrePequeño.crearDesdeJson(json);
		} else if (tipo.equals(Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(CofreMediano.class))) {
			c = CofreMediano.crearDesdeJson(json);
		}
		return c;
	}

	protected abstract String getTipoCofre();

	@Override
	public Ente getEntePropietario() {
		return this;
	}

	@Override
	public void eliminar() {
		this.eliminado = true;
	}

	@Override
	public double getPosicionX() {
		return this.x;
	}

	@Override
	public double getPosicionY() {
		return this.y;
	}

	@Override
	public void modificarPosicionX(final double desplazamientoX) {
		this.x = (int) desplazamientoX;
	}

	@Override
	public void modificarPosicionY(final double desplazamientoY) {
		this.y = (int) desplazamientoY;
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
		return "Cofre [X=" + this.x + ", Y=" + this.y + ", estado=" + this.INVENTARIO.getEstadoInventario() + "]";
	}
}