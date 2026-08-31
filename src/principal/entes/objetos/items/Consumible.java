package principal.entes.objetos.items;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.json.simple.JSONObject;

import principal.entes.criaturas.Criatura;
import principal.entes.modelos.item.ListaModelosItem;
import principal.entes.objetos.items.municiones.CajaMunicion;
import principal.entes.objetos.items.pociones.PocionVidaMenor;
import principal.utilidades.Render2D;

/**
 * Clase base abstracta para todos los ítems consumibles y acumulables
 * (Stackable Items). Gestiona cantidades apilables, límites de inventario y
 * deserialización polimórfica.
 * 
 * @version 2.0 (Java 8 Compatible - Zero-GC Architecture)
 */
public abstract class Consumible extends Item {

	private static final long serialVersionUID = 504856170135227071L;

	private final String CODIGO_MODELO;
	private int cantidad;

	public Consumible(final int x, final int y, final int cantidad, final String codModelo) {
		super(x, y);
		this.CODIGO_MODELO = codModelo;
		this.establecerCantidad(cantidad);
	}

	public Consumible(final int cantidad, final String codModelo) {
		super(0, 0);
		this.CODIGO_MODELO = codModelo;
		this.establecerCantidad(cantidad);
	}

	public void establecerCantidad(final int cantidad) {
		if (cantidad > this.getLimite()) {
			this.cantidad = this.getLimite();
		} else {
			if (cantidad < 0) {
				this.cantidad = 0;
			} else {
				this.cantidad = cantidad;
			}
			if (this.cantidad == 0) {
				this.eliminar();
			}
		}
	}

	public int agregarCantidad(final int cantidad) {
		int resto = 0;
		if ((this.cantidad + cantidad) > this.getLimite()) {
			resto = (this.cantidad + cantidad) - this.getLimite();
			this.cantidad = this.getLimite();
		} else {
			this.cantidad += cantidad;
		}
		return resto;
	}

	public void reducirCantidad(final int cantidad) {
		if ((this.cantidad - cantidad) < 0) {
			this.cantidad = 0;
		} else {
			this.cantidad -= cantidad;
		}
		if (this.cantidad == 0) {
			this.eliminar();
		}
	}

	public abstract void consumir(final Criatura c);

	@Override
	public void pintar(final Graphics2D g) {
		super.pintar(g);
	}

	@Override
	public void pintarInventario(final Graphics2D g, final int x, final int y) {
		Render2D.dibujarImagen(g, this.getTexturaInventario(), x, y);
	}

	public String getCodigoModelo() {
		return this.CODIGO_MODELO;
	}

	public int getCantidad() {
		return this.cantidad;
	}

	@Override
	public BufferedImage getTexturaInventario() {
		return ListaModelosItem.getModeloConsumible(this.CODIGO_MODELO).getTexturaInventario();
	}

	@Override
	public BufferedImage getTextura() {
		return ListaModelosItem.getModeloConsumible(this.CODIGO_MODELO).getTexturaMapa();
	}

	public int getLimite() {
		return ListaModelosItem.getModeloConsumible(this.CODIGO_MODELO).getLimite();
	}

	@Override
	public boolean esSolido() {
		return ListaModelosItem.getModeloConsumible(this.CODIGO_MODELO).esSolido();
	}

	@Override
	public int getTipoItem() {
		return ListaModelosItem.getModeloConsumible(this.CODIGO_MODELO).getTipoItem();
	}

	@Override
	public int getAncho() {
		return ListaModelosItem.getModeloConsumible(this.CODIGO_MODELO).getAncho();
	}

	@Override
	public int getAlto() {
		return ListaModelosItem.getModeloConsumible(this.CODIGO_MODELO).getAlto();
	}

	@Override
	public String getNombre() {
		return ListaModelosItem.getModeloConsumible(this.CODIGO_MODELO).getNombre();
	}

	@Override
	public String exportarTipoItem() {
		return "Consumible";
	}

	/**
	 * Fábrica polimórfica para reconstruir cualquier consumible o caja de munición
	 * desde JSON.
	 */
	public static Consumible crearConsumible(final JSONObject json) {
		if (json == null) {
			return null;
		}

		Consumible c = null;
		final String codModelo = (json.get("codModelo") != null) ? json.get("codModelo").toString() : "";

		if (codModelo.equals(ListaModelosItem.COD_CONSUMIBLE_POCION_VIDA_MENOR)) {
			c = PocionVidaMenor.crearDesdeJson(json);
		} else if (codModelo.equals(ListaModelosItem.COD_CONSUMIBLE_MUNICION_PISTOLA)
				|| codModelo.equals(ListaModelosItem.COD_CONSUMIBLE_MUNICION_ESCOPETA)
				|| codModelo.equals(ListaModelosItem.COD_CONSUMIBLE_MUNICION_FUSIL)
				|| codModelo.equals(ListaModelosItem.COD_CONSUMIBLE_MUNICION_PESADA)) {
			c = CajaMunicion.crearDesdeJson(json);
		}

		return c;
	}
}