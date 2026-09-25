package principal.entes.objetos.items;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.json.simple.JSONObject;

import principal.entes.objetos.Objeto;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

public abstract class Item extends Objeto {

	private static final long serialVersionUID = -451309412394893821L;

	public static final int COD_ITEM_PORTABLE = 1;
	public static final int COD_ITEM_CONSUMIBLE = 2;
	public static final int COD_ITEM_MONEDA = 3;

	private static final Color COLOR_SOMBRA_SUELO = new Color(0, 0, 0, 75);

	protected final ArrayList<String> LISTA_INFO;
	protected long precioBasePlata = 10L;

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

	// =========================================================================
	// SISTEMA DE VALORACIÓN COMERCIAL (1 Oro = 100 Plata)
	// =========================================================================

	public long getPrecioBasePlata() {
		return this.precioBasePlata;
	}

	public void setPrecioBasePlata(final long precioPlata) {
		this.precioBasePlata = Math.max(1L, precioPlata);
	}

	public long getPrecioVentaPlata() {
		// Tasa de reventa estándar de RPG: 50% del precio base (mínimo 1 de Plata)
		return Math.max(1L, this.precioBasePlata / 2L);
	}

	public static String formatearMoneda(final long totalPlata) {
		final long oro = totalPlata / 100L;
		final long plata = totalPlata % 100L;

		if ((oro > 0L) && (plata > 0L)) {
			return oro + " Oro " + plata + " Plata";
		}
		if (oro > 0L) {
			return oro + " Oro";
		}
		return plata + " Plata";
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

	// =========================================================================
	// PERSISTENCIA MEDIANTE REGISTRO DE ENTIDADES (ZERO-HARDCODE)
	// =========================================================================

	public JSONObject getJsonItem() {
		return principal.persistencia.json.RegistroEntidades.exportar(this);
	}

	public static Item crearItemDesdeJson(final JSONObject sobre) {
		final principal.entes.Ente e = principal.persistencia.json.RegistroEntidades.importar(sobre, null);
		return (e instanceof Item) ? (Item) e : null;
	}

	protected JSONObject exportarParaJSON() {
		return null; // Delegado en AdaptadorEntidad
	}

	public String exportarTipoItem() {
		return this.getClass().getSimpleName();
	}

}