package principal.entes.objetos.items.monedas;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.json.simple.JSONObject;

import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.Item;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

/**
 * Entidad física de moneda arrojada en el suelo del mundo (Zero-GC).
 * Al contacto con el jugador se auto-absorbe sin ocupar slots de inventario.
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class ItemMoneda extends Item {

	private static final long serialVersionUID = 1L;

	private final TipoMoneda tipo;
	private final long valorPlata;

	public ItemMoneda(final int x, final int y, final TipoMoneda tipo, final long cantidadNominal) {
		super(x, y);
		this.tipo = (tipo != null) ? tipo : TipoMoneda.PLATA;
		final long cantidad = Math.max(1L, cantidadNominal);
		this.valorPlata = cantidad * this.tipo.getEquivalenciaPlata();
		this.rellenarInfo(this.LISTA_INFO);
	}

	public static ItemMoneda crearPlata(final int x, final int y, final long cantidad) {
		return new ItemMoneda(x, y, TipoMoneda.PLATA, cantidad);
	}

	public static ItemMoneda crearOro(final int x, final int y, final long cantidad) {
		return new ItemMoneda(x, y, TipoMoneda.ORO, cantidad);
	}

	public TipoMoneda getTipo() {
		return this.tipo;
	}

	public long getValorPlata() {
		return this.valorPlata;
	}

	@Override
	public BufferedImage getTexturaInventario() {
		return Globales.GESTOR_TEXTURAS.get(this.tipo.getTexturaInv());
	}

	@Override
	public void pintarInventario(final Graphics2D g, final int x, final int y) {
		Render2D.dibujarImagen(g, this.getTexturaInventario(), x, y);
	}

	@Override
	public int getTipoItem() {
		return Item.COD_ITEM_MONEDA;
	}

	@Override
	public String getNombre() {
		return this.tipo.getNombre();
	}

	@Override
	public BufferedImage getTextura() {
		return Globales.GESTOR_TEXTURAS.get(this.tipo.getTexturaMapa());
	}

	@Override
	public int getAncho() {
		return 10;
	}

	@Override
	public int getAlto() {
		return 10;
	}

	@Override
	public boolean esSolido() {
		return false;
	}

	@Override
	public Objeto copiar() {
		final long cantidadNominal = this.valorPlata / this.tipo.getEquivalenciaPlata();
		return new ItemMoneda(this.getPosicionXInt(), this.getPosicionYInt(), this.tipo, cantidadNominal);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JSONObject exportarParaJSON() {
		final JSONObject json = new JSONObject();
		json.put("x", Integer.valueOf(this.getPosicionXInt()));
		json.put("y", Integer.valueOf(this.getPosicionYInt()));
		json.put("tipoMoneda", this.tipo.name());
		json.put("valorPlata", Long.valueOf(this.valorPlata));
		return json;
	}

	public static ItemMoneda crearDesdeJson(final JSONObject json) {
		if (json == null) {
			return new ItemMoneda(0, 0, TipoMoneda.PLATA, 1L);
		}

		final int x = (json.get("x") != null) ? ((Number) json.get("x")).intValue() : 0;
		final int y = (json.get("y") != null) ? ((Number) json.get("y")).intValue() : 0;

		TipoMoneda tipo = TipoMoneda.PLATA;
		if (json.get("tipoMoneda") != null) {
			try {
				tipo = TipoMoneda.valueOf(json.get("tipoMoneda").toString());
			} catch (final Exception ignored) {
			}
		}

		final long valorPlata = (json.get("valorPlata") != null) ? ((Number) json.get("valorPlata")).longValue()
				: tipo.getEquivalenciaPlata();

		final long cantidadNominal = Math.max(1L, valorPlata / tipo.getEquivalenciaPlata());
		return new ItemMoneda(x, y, tipo, cantidadNominal);
	}

	@Override
	public String exportarTipoItem() {
		return "ItemMoneda";
	}

	@Override
	protected void rellenarInfo(final ArrayList<String> listaInfo) {
		listaInfo.clear();
		listaInfo.add("Moneda oficial del reino.");
		listaInfo.add("Valor: " + this.valorPlata + " de Plata.");
	}
}