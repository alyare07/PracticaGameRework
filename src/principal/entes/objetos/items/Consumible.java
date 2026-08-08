package principal.entes.objetos.items;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.json.simple.JSONObject;
import principal.entes.criaturas.Criatura;
import principal.entes.modelos.item.ListaModelosItem;
import principal.entes.objetos.items.pociones.PocionVidaMenor;
import principal.utilidades.DibujoDebug;

public abstract class Consumible extends Item {
	private static final long serialVersionUID = 504856170135227071L;
	private final String CODIGO_MODELO;
	private int cantidad;

	public Consumible(final int x, final int y, final int cantidad, final String codModelo) {
		super(x, y);
		this.CODIGO_MODELO = codModelo;
		establecerCantidad(cantidad);
	}

	public Consumible(final int cantidad, final String codModelo) {
		super(0, 0);
		this.CODIGO_MODELO = codModelo;
		establecerCantidad(cantidad);
	}

	public void establecerCantidad(final int cantidad) {
		if (cantidad > getLimite()) {
			this.cantidad = getLimite();
		} else {
			if (cantidad < 0) {
				this.cantidad = 0;
			} else {
				this.cantidad = cantidad;
			}
			if(cantidad == 0) {
				this.eliminar();
			}
		}
	}

	public int agregarCantidad(final int cantidad) {
		int resto = 0;
		if ((this.cantidad + cantidad) > getLimite()) {
			resto = (this.cantidad + cantidad) - getLimite();
			this.cantidad = getLimite();
		} else {
			this.cantidad += cantidad;
		}
		return resto;
	}

	public void reducirCantidad(final int cantidad) {
		if (this.cantidad - cantidad < 0) {
			this.cantidad = 0;
		} else {
			this.cantidad -= cantidad;
		}
		if(this.cantidad == 0) {
			this.eliminar();
		}
	}
	
	public abstract void consumir(final Criatura c);

	@Override
	public void pintar(final Graphics2D g) {
		super.pintar(g);

		DibujoDebug.dibujarImagenRefCamara(g, getTextura(), this.x, this.y);
	}

	@Override
	public void pintarInventario(final Graphics2D g, final int x, final int y) {
		DibujoDebug.dibujarImagen(g, getTexturaInventario(), x, y);
	}

	public String getCodigoModelo() {
		return this.CODIGO_MODELO;
	}

	public int getCantidad() {
		return this.cantidad;
	}

	@Override
	public BufferedImage getTexturaInventario() {
		return ListaModelosItem.getModeloConsumible(CODIGO_MODELO).getTexturaInventario();
	}

	@Override
	public BufferedImage getTextura() {
		return ListaModelosItem.getModeloConsumible(CODIGO_MODELO).getTexturaMapa();
	}

	public int getLimite() {
		return ListaModelosItem.getModeloConsumible(CODIGO_MODELO).getLimite();
	}

	@Override
	public boolean esSolido() {
		return ListaModelosItem.getModeloConsumible(CODIGO_MODELO).esSolido();
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
	public void modificarPosicionX(double desplazamientoX) {
		this.x += desplazamientoX;
	}

	@Override
	public void modificarPosicionY(double desplazamientoY) {
		this.y += desplazamientoY;
	}

	@Override
	public boolean estaEliminado() {
		return this.eliminado;
	}

	@Override
	public int getTipoItem() {
		return ListaModelosItem.getModeloConsumible(CODIGO_MODELO).getTipoItem();
	}

	@Override
	public int getAncho() {
		return ListaModelosItem.getModeloConsumible(CODIGO_MODELO).getAncho();
	}

	@Override
	public int getAlto() {
		return ListaModelosItem.getModeloConsumible(CODIGO_MODELO).getAlto();
	}

	@Override
	public String getNombre() {
		return ListaModelosItem.getModeloConsumible(CODIGO_MODELO).getNombre();
	}


	@Override
	public String exportarTipoItem() {
		return "Consumible";
	}
	
	public static Consumible crearConsumible(final JSONObject json) {
		Consumible c = null;
		final String codModelo = json.get("codModelo").toString();
		if(codModelo == ListaModelosItem.COD_CONSUMIBLE_POCION_VIDA_MENOR) {
			c = PocionVidaMenor.crearDesdeJson(json);
		}
		
		return c;
	}

}
