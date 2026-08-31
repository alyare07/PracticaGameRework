package principal.entes.objetos.items;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.json.simple.JSONObject;

import principal.entes.modelos.item.ListaModelosItem;
import principal.utilidades.Render2D;

public abstract class Portable extends Item {
	private static final long serialVersionUID = 4861089138825196600L;
	protected final String CODIGO_MODELO;

	public Portable(final int x, final int y, final String codModelo) {
		super(x, y);
		this.CODIGO_MODELO = codModelo;

	}

	public Portable(final String codModelo) {
		super();
		this.CODIGO_MODELO = codModelo;
	}

	public String getCodigoModelo() {
		return this.CODIGO_MODELO;
	}

	@Override
	public void pintar(final Graphics2D g) {
		super.pintar(g);
	}

	@Override
	public BufferedImage getTexturaInventario() {
		return ListaModelosItem.getModeloPortable(this.CODIGO_MODELO).getTexturaInventario();
	}

	@Override
	public void pintarInventario(final Graphics2D g, final int x, final int y) {
		Render2D.dibujarImagen(g, this.getTexturaInventario(), x, y);
	}

	@Override
	public int getTipoItem() {
		return ListaModelosItem.getModeloPortable(this.CODIGO_MODELO).getTipoItem();
	}

	@Override
	public int getAncho() {
		return ListaModelosItem.getModeloPortable(this.CODIGO_MODELO).getAncho();
	}

	@Override
	public int getAlto() {
		return ListaModelosItem.getModeloPortable(this.CODIGO_MODELO).getAlto();
	}

	@Override
	public BufferedImage getTextura() {
		return ListaModelosItem.getModeloPortable(this.CODIGO_MODELO).getTexturaMapa();
	}

	@Override
	public boolean esSolido() {
		return ListaModelosItem.getModeloPortable(this.CODIGO_MODELO).esSolido();
	}

	@Override
	public String getNombre() {
		return ListaModelosItem.getModeloPortable(this.CODIGO_MODELO).getNombre();
	}

	@Override
	protected JSONObject exportarParaJSON() {
		return null;
	}

	@Override
	public String exportarTipoItem() {
		return "Portable";
	}

}
