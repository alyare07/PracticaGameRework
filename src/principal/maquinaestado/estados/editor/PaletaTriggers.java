package principal.maquinaestado.estados.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import principal.mapa.Tile;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

/**
 * Paleta del editor limpia y simplificada para colocar sellos de Triggers,
 * Volúmenes de Ambiente y Luces en el mapa. La configuración detallada se
 * realiza directamente con la tecla 'E' sobre el trigger en el mapa.
 * 
 * @version 2.0 (Vanilla Java 8 - Clean Stamp Palette)
 */
public class PaletaTriggers extends Paleta {

	public enum CategoriaTrigger {
		TELEPORT_PUERTA("Zona TP / Puerta"), ZONA_AMBIENTE_BIOMA("Bioma / Niebla"),
		ZONA_AMBIENTE_CUEVA("Cueva / Interior"), LUZ_ANTORCHA("Luz Antorcha (80px)"), LUZ_FOGATA("Luz Fogata (140px)");

		private final String nombre;

		CategoriaTrigger(final String nombre) {
			this.nombre = nombre;
		}

		public String getNombre() {
			return this.nombre;
		}
	}

	public static class EntradaTrigger {
		public final String nombre;
		public final CategoriaTrigger categoria;
		public final BufferedImage icono;
		public final Color colorDistintivo;

		public EntradaTrigger(final String nombre, final CategoriaTrigger categoria, final BufferedImage icono,
				final Color colorDistintivo) {
			this.nombre = nombre;
			this.categoria = categoria;
			this.icono = icono;
			this.colorDistintivo = colorDistintivo;
		}
	}

	private final ArrayList<EntradaTrigger> ENTRADAS = new ArrayList<EntradaTrigger>();
	private static final Font FUENTE_BADGE = new Font(Font.SANS_SERIF, Font.BOLD, 5);

	public PaletaTriggers(final int x, final int y, final int ancho, final int alto, final int ladoSlot) {
		super(x, y, ancho, alto, ladoSlot);
		this.cargarPresets();
	}

	private void cargarPresets() {
		this.ENTRADAS.add(new EntradaTrigger("Zona TP", CategoriaTrigger.TELEPORT_PUERTA,
				this.crearIconoTexto("TP", new Color(255, 60, 60)), new Color(255, 60, 60)));

		this.ENTRADAS.add(new EntradaTrigger("Zona Bioma", CategoriaTrigger.ZONA_AMBIENTE_BIOMA,
				this.crearIconoTexto("BIO", new Color(60, 220, 120)), new Color(60, 220, 120)));

		this.ENTRADAS.add(new EntradaTrigger("Zona Cueva", CategoriaTrigger.ZONA_AMBIENTE_CUEVA,
				this.crearIconoTexto("INT", new Color(160, 170, 200)), new Color(160, 170, 200)));

		this.ENTRADAS.add(new EntradaTrigger("Luz Antorcha", CategoriaTrigger.LUZ_ANTORCHA,
				this.crearIconoTexto("LUZ", new Color(255, 160, 40)), new Color(255, 160, 40)));

		this.ENTRADAS.add(new EntradaTrigger("Luz Fogata", CategoriaTrigger.LUZ_FOGATA,
				this.crearIconoTexto("FOG", new Color(255, 100, 20)), new Color(255, 100, 20)));
	}

	private BufferedImage crearIconoTexto(final String texto, final Color color) {
		final int lado = this.LADO_SLOT;
		final BufferedImage img = new BufferedImage(lado, lado, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = img.createGraphics();

		g.setColor(new Color(20, 24, 30));
		g.fillRect(0, 0, lado, lado);
		g.setColor(color);
		g.drawRect(1, 1, lado - 3, lado - 3);

		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 8f));
		final int ancho = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, texto);
		final int alto = Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, texto);
		final int tx = (lado - ancho) / 2;
		final int ty = (lado + (alto / 2)) / 2;

		Render2D.dibujarStringConSombra(g, texto, tx, ty, color, Color.BLACK);
		g.dispose();
		return img;
	}

	@Override
	protected void pintarElementoEnSlot(final Graphics2D g, final int index, final int slotX, final int slotY) {
		final EntradaTrigger entrada = this.ENTRADAS.get(index);
		if (entrada.icono != null) {
			this.dibujarIconoAjustadoAlSlot(g, entrada.icono, slotX, slotY);
		}

		final Font fontPrevia = g.getFont();
		g.setFont(FUENTE_BADGE);
		Render2D.dibujarRectanguloRelleno(g, slotX + 1, slotY + 1, 6, 6, Color.BLACK);
		Render2D.dibujarString(g, "T", slotX + 2, slotY + 6, entrada.colorDistintivo);
		g.setFont(fontPrevia);
	}

	public EntradaTrigger getEntradaSeleccionada() {
		if ((this.indiceSeleccionado >= 0) && (this.indiceSeleccionado < this.ENTRADAS.size())) {
			return this.ENTRADAS.get(this.indiceSeleccionado);
		}
		return null;
	}

	@Override
	public int getCantidadTotalElementos() {
		return this.ENTRADAS.size();
	}

	@Override
	public String getNombreElemento(final int index) {
		return ((index >= 0) && (index < this.ENTRADAS.size())) ? this.ENTRADAS.get(index).nombre : "";
	}

	@Override
	public boolean valoresYaEstablecidosPreviamente(final Tile tileEvaluar) {
		return false;
	}
}