package principal.entes.objetos.items;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.json.simple.JSONObject;

import principal.entes.modelos.item.ListaModelosItem;
import principal.utilidades.DibujoDebug;

public abstract class Portable extends Item {
	private static final long serialVersionUID = 4861089138825196600L;
	protected final String CODIGO_MODELO;

	public Portable(int x, int y, final String codModelo) {
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
		DibujoDebug.dibujarImagenRefCamara(g, getTextura(), this.x, this.y);
	}

	@Override
	public BufferedImage getTexturaInventario() {
		return ListaModelosItem.getModeloPortable(this.CODIGO_MODELO).getTexturaInventario();
	}

	@Override
	public void pintarInventario(Graphics2D g, int x, int y) {
		DibujoDebug.dibujarImagen(g, getTexturaInventario(), x, y);
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
