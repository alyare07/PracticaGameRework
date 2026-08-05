package principal.entes.objetos.items;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.json.simple.JSONObject;

import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.armas.distancia.fuego.Pistola;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.funciones.GeneradorTooltip;

public abstract class Item extends Objeto {
	private static final long serialVersionUID = -451309412394893821L;
	public static final int COD_ITEM_PORTABLE = 1;
	public static final int COD_ITEM_CONSUMIBLE = 2;
	protected final ArrayList<String> LISTA_INFO;

	public Item(int x, int y) {
		super(x, y);
		this.LISTA_INFO = new ArrayList<String>();
	}

	public Item() {
		super(0, 0);
		this.LISTA_INFO = new ArrayList<String>();
	}

	public abstract BufferedImage getTexturaInventario();

	public abstract void pintarInventario(final Graphics2D g, final int x, final int y);

	public abstract int getTipoItem();

	public abstract String getNombre();
	
	
	public  ArrayList<String> getInfo(){
		return this.LISTA_INFO;
	}
	
	public Rectangle getArea() {
		return new Rectangle(getPosicionXInt(), getPosicionYInt(), getAncho(), getAlto());
	}
	
	protected void rellenarInfo(final ArrayList<String> listaInfo) {
	}

	@Override
	public void pintar(final Graphics2D g) {
		if (Constantes.TECLADO.TECLA_VER_COLISIONES.presionado() && Constantes.isEstadoJuego()) {
			final Rectangle area = getArea();
			DibujoDebug.dibujarRectanguloContornoRefCamara(g,area, Color.ORANGE);
		}
	}
	
	protected abstract  JSONObject exportarParaJSON();
	
	public abstract String exportarTipoItem();
	
	@SuppressWarnings("unchecked")
	public JSONObject getJsonItem() {
		JSONObject datosItem = exportarParaJSON();
		JSONObject item = new JSONObject();
		item.put("tipo", exportarTipoItem());
		item.put("entiti", datosItem);
		return item;
	}
	
	public static Item crearItemDesdeJson(final JSONObject json) {
		Item i = null;
		if(json.get("tipo").toString().equals(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Pistola.class))) {
			i = Pistola.crearDesdeJson((JSONObject)json.get("entiti"));
		}else if(json.get("tipo").toString().equals(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Consumible.class))) {
			i = Consumible.crearConsumible((JSONObject)json.get("entiti"));
		}
		return i;
	}

}
