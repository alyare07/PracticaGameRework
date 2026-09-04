package principal.entes.objetos;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import principal.entes.Ente;
import principal.entes.objetos.items.Item;
import principal.inventario.Contenedor;
import principal.inventario.vault.InventarioVault;
import principal.recursos.ClaveHoja;
import principal.utilidades.Globales;
import principal.utilidades.HojaSprite;
import principal.utilidades.Render2D;

public class ArbolCofre extends Objeto implements Contenedor {

	private static final long serialVersionUID = 651599209121613328L;
	private final InventarioVault INVENTARIO;

	public ArbolCofre(final int x, final int y) {
		super(x, y);
		this.INVENTARIO = new InventarioVault(this, 20, 5, "Inventario arbol secreto");
	}

	@Override
	public void actualizar() {
		super.actualizar();
		this.INVENTARIO.actualizarEstadoCofre();
	}

	@Override
	public void pintar(final Graphics2D g) {
		Render2D.dibujarImagenRefCamara(g, this.getTextura(), this.getPosicionXInt() - 14, this.getPosicionYInt() - 18);
		if (Globales.TECLADO.TECLA_VER_COLISIONES.presionado() && Globales.estadoJuego) {
			Render2D.dibujarRectanguloContornoRefCamara(g, this.getArea(), Color.ORANGE);
		}
	}

	@Override
	public String getNombreContenedor() {
		return this.INVENTARIO.getNombre();
	}

	@Override
	public InventarioVault getInventario() {
		return this.INVENTARIO;
	}

	@Override
	public Ente getEntePropietario() {
		return this;
	}

	@Override
	public int getAncho() {
		return 5;
	}

	@Override
	public int getAlto() {
		return 12;
	}

	@Override
	public BufferedImage getTextura() {
		final HojaSprite h = Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.ARBOLES_32);
		return (h != null) ? h.getSprite(1) : Globales.GESTOR_TEXTURAS.getTexturaError();
	}

	@Override
	public boolean esSolido() {
		return true;
	}

	@Override
	public Objeto copiar() {
		return new ArbolCofre(this.getPosicionXInt(), this.getPosicionYInt());
	}

	@Override
	public void eliminar() {
		this.eliminado = true;
	}

	@Override
	public double getPosicionX() {
		return this.getPosicionXInt();
	}

	@Override
	public double getPosicionY() {
		return this.getPosicionYInt();
	}

	@Override
	public boolean estaEliminado() {
		return this.eliminado;
	}

	@SuppressWarnings("unchecked")
	public JSONObject exportarParaJson() {
		final JSONObject json = new JSONObject();
		json.put("x", Integer.valueOf(this.getPosicionXInt()));
		json.put("y", Integer.valueOf(this.getPosicionYInt()));

		final JSONArray lista = new JSONArray();
		for (final Item i : this.INVENTARIO.getItems()) {
			lista.add(i.getJsonItem());
		}
		json.put(Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Item.class), lista);

		final JSONObject jsonPrincipal = new JSONObject();
		jsonPrincipal.put("tipoObjeto", "ArbolCofre");
		jsonPrincipal.put("entiti", json);
		return jsonPrincipal;
	}

	public static ArbolCofre crearDesdeJson(final JSONObject json) {
		if (json == null) {
			return new ArbolCofre(0, 0);
		}

		final int x = (json.get("x") != null) ? ((Number) json.get("x")).intValue() : 0;
		final int y = (json.get("y") != null) ? ((Number) json.get("y")).intValue() : 0;
		final ArbolCofre arbolCofre = new ArbolCofre(x, y);

		final String claveItems = Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Item.class);
		final Object itemsObj = json.get(claveItems);

		if (itemsObj instanceof JSONArray) {
			for (final Object obj : (JSONArray) itemsObj) {
				if (obj instanceof JSONObject) {
					final Item i = Item.crearItemDesdeJson((JSONObject) obj);
					if (i != null) {
						arbolCofre.getInventario().agregarItem(i);
					}
				}
			}
		}

		return arbolCofre;
	}
}