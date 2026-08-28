package principal.entes.objetos.cofres;

import java.awt.image.BufferedImage;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.Item;
import principal.inventario.vault.InventarioVault.EstadoInventario;
import principal.utilidades.Globales;

public class CofrePequeño extends Cofre {

	private static final long serialVersionUID = 4592661837024054777L;
	private final int ANCHO = 16;
	private final int ALTO = 16;

	public CofrePequeño(final int x, final int y) {
		super(x, y, 3, 3, "Cofre Pequeño");
	}

	@Override
	public BufferedImage getTextura() {
		if (this.getInventario().getEstadoInventario() == EstadoInventario.CERRADO) {
			return Globales.LISTA_HOJAS_SPRITES.COFRES.getCofreCerrado();
		}
		return Globales.LISTA_HOJAS_SPRITES.COFRES.getCofreAbierto();
	}

	@Override
	public int getAncho() {
		return this.ANCHO;
	}

	@Override
	public int getAlto() {
		return this.ALTO;
	}

	@Override
	public Objeto copiar() {
		return new CofrePequeño(this.getPosicionXInt(), this.getPosicionYInt()); // COPIAR TAMBIEN EL INVENTARIO
	}

	@Override
	public boolean esSolido() {
		return true;
	}

	@Override
	protected String getTipoCofre() {
		return Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(CofrePequeño.class);
	}

	public static CofrePequeño crearDesdeJson(final JSONObject json) {
		final int x = Integer.parseInt(json.get("x").toString());
		final int y = Integer.parseInt(json.get("y").toString());
		final JSONParser parse = new JSONParser();
		JSONArray listaItemsJson = null;
		try {
			listaItemsJson = (JSONArray) parse
					.parse(json.get(Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Item.class)).toString());
		} catch (final ParseException e) {
			listaItemsJson = new JSONArray();
		}
		final CofrePequeño cofre = new CofrePequeño(x, y);
		Item i = null;
		for (final Object obj : listaItemsJson) {
			if (obj instanceof JSONObject) {
				i = Item.crearItemDesdeJson((JSONObject) obj);
				if (i == null) {
					continue;
				}
				cofre.meterItem(i);
			}
		}
		return cofre;
	}

}
