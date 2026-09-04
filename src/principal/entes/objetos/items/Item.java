package principal.entes.objetos.items;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.json.simple.JSONObject;

import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.armas.distancia.fuego.Pistola;
import principal.entes.objetos.items.armas.distancia.fuego.automaticas.AmetralladoraPesada;
import principal.entes.objetos.items.armas.distancia.fuego.automaticas.RifleAsalto;
import principal.entes.objetos.items.armas.distancia.fuego.automaticas.SubfusilLigero;
import principal.entes.objetos.items.armas.distancia.fuego.escopetas.EscopetaAutomatica;
import principal.entes.objetos.items.armas.distancia.fuego.escopetas.EscopetaRecortada;
import principal.entes.objetos.items.armas.distancia.fuego.escopetas.EscopetaTactica;
import principal.entes.objetos.items.arrojadizos.granadas.GranadaT1;
import principal.entes.objetos.items.herramientas.Herramienta;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

public abstract class Item extends Objeto {

	private static final long serialVersionUID = -451309412394893821L;

	public static final int COD_ITEM_PORTABLE = 1;
	public static final int COD_ITEM_CONSUMIBLE = 2;

	private static final Color COLOR_SOMBRA_SUELO = new Color(0, 0, 0, 75);

	protected final ArrayList<String> LISTA_INFO;

	public Item(final int x, final int y) {
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

	public ArrayList<String> getInfo() {
		return this.LISTA_INFO;
	}

	protected void rellenarInfo(final ArrayList<String> listaInfo) {
	}

	@Override
	public void pintar(final Graphics2D g) {
		final int ancho = this.getAncho();
		final int alto = this.getAlto();

		final double faseUnica = (this.getPosicionX() * 0.05) + (this.getPosicionY() * 0.05);
		final int offsetFlotacion = (int) Math.round(Math.sin((Globales.animacion * 0.12) + faseUnica) * 1.5);

		final int sombraAncho = Math.max(4, ancho - 4);
		final int sombraAlto = Math.max(2, alto / 4);
		final int sombraX = this.getPosicionXInt() + ((ancho - sombraAncho) / 2);
		final int sombraY = (this.getPosicionYInt() + alto) - (sombraAlto / 2);

		Render2D.dibujarFiguraEllipseRefCamara(g, sombraX, sombraY, sombraAncho, sombraAlto, COLOR_SOMBRA_SUELO);

		if (Globales.TECLADO.TECLA_VER_COLISIONES.presionado() && Globales.isEstadoJuego()) {
			Render2D.dibujarRectanguloContornoRefCamara(g, this.getArea(), Color.ORANGE);
		}

		Render2D.dibujarImagenRefCamara(g, this.getTextura(), this.getPosicionXInt(),
				this.getPosicionYInt() + offsetFlotacion);
	}

	@Override
	public Rectangle getArea() {
		this.AREA_ENTE_RETORNO.setBounds(this.getPosicionXInt(), this.getPosicionYInt(), this.getAncho(),
				this.getAlto());
		return this.AREA_ENTE_RETORNO;
	}

	protected abstract JSONObject exportarParaJSON();

	public abstract String exportarTipoItem();

	@SuppressWarnings("unchecked")
	public JSONObject getJsonItem() {
		final JSONObject datosItem = this.exportarParaJSON();
		final JSONObject item = new JSONObject();
		item.put("tipo", this.exportarTipoItem());
		item.put("entiti", datosItem);
		return item;
	}

	public static Item crearItemDesdeJson(final JSONObject json) {
		if (json == null) {
			return null;
		}

		final Object tipoObj = json.get("tipo");
		final String tipoStr = (tipoObj != null) ? tipoObj.toString() : "";

		JSONObject entiti = null;
		if (json.get("entiti") instanceof JSONObject) {
			entiti = (JSONObject) json.get("entiti");
		} else {
			entiti = json;
		}

		// 1. Armas de fuego
		if (tipoStr.equals("Pistola")) {
			return Pistola.crearDesdeJson(entiti);
		}
		if (tipoStr.equals("EscopetaRecortada")) {
			return EscopetaRecortada.crearDesdeJson(entiti);
		}
		if (tipoStr.equals("EscopetaTactica")) {
			return EscopetaTactica.crearDesdeJson(entiti);
		}
		if (tipoStr.equals("EscopetaAutomatica")) {
			return EscopetaAutomatica.crearDesdeJson(entiti);
		}
		if (tipoStr.equals("SubfusilLigero")) {
			return SubfusilLigero.crearDesdeJson(entiti);
		}
		if (tipoStr.equals("RifleAsalto")) {
			return RifleAsalto.crearDesdeJson(entiti);
		}
		if (tipoStr.equals("AmetralladoraPesada")) {
			return AmetralladoraPesada.crearDesdeJson(entiti);
		}

		// 2. Equipamiento (Cascos, Armaduras, Botas, Anillos)
		if (tipoStr.equals("PiezaEquipo")) {
			return principal.entes.objetos.items.equipamiento.PiezaEquipo.crearDesdeJson(entiti);
		}

		// 3. Arrojadizos / Granadas
		if (tipoStr.equals("GranadaT1") || tipoStr.equals("Granada")) {
			return GranadaT1.crearDesdeJson(entiti);
		}

		// 4. Herramientas (Hachas / Picos)
		if (tipoStr.equals("Herramienta")) {
			return Herramienta.crearDesdeJson(entiti);
		}

		// 5. Consumibles, Pociones, Materiales y Cajas de Munición
		if (tipoStr.equals("Consumible") || tipoStr.equals("RecursoMaterial") || tipoStr.equals("CajaMunicion")
				|| tipoStr.equals("PocionVidaMenor")) {
			return Consumible.crearConsumible(entiti);
		}

		return Consumible.crearConsumible(entiti);
	}
}