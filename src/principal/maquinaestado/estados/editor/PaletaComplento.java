package principal.maquinaestado.estados.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import principal.entes.objetos.Objeto;
import principal.mapa.Tile;
import principal.utilidades.Render2D;

public class PaletaComplento extends Paleta {

	@FunctionalInterface
	public interface CreadorObjeto {
		Objeto crear(int x, int y);
	}

	public static class EntradaPaleta {
		public final String nombre;
		public final BufferedImage icono;
		public final boolean esCosechable;
		public final CreadorObjeto creador;

		public EntradaPaleta(final String nombre, final BufferedImage icono, final boolean esCosechable,
				final CreadorObjeto creador) {
			this.nombre = nombre;
			this.icono = icono;
			this.esCosechable = esCosechable;
			this.creador = creador;
		}
	}

	private final ArrayList<EntradaPaleta> ENTRADAS = new ArrayList<EntradaPaleta>();
	private static final Font FUENTE_BADGE = new Font(Font.SANS_SERIF, Font.BOLD, 5);

	public PaletaComplento(final int x, final int y, final int ancho, final int alto, final int ladoSlot) {
		super(x, y, ancho, alto, ladoSlot);
	}

	public void agregarEntrada(final String nombre, final BufferedImage icono, final boolean esCosechable,
			final CreadorObjeto creador) {
		if ((nombre != null) && (icono != null) && (creador != null)) {
			this.ENTRADAS.add(new EntradaPaleta(nombre, icono, esCosechable, creador));
		}
	}

	public boolean seleccionarPorNombre(final String nombre) {
		if ((nombre == null) || nombre.isEmpty()) {
			return false;
		}
		final String buscado = normalizar(nombre);
		for (int i = 0; i < this.ENTRADAS.size(); i++) {
			final String entradaNorm = normalizar(this.ENTRADAS.get(i).nombre);
			if (entradaNorm.equals(buscado) || entradaNorm.contains(buscado) || buscado.contains(entradaNorm)) {
				this.indiceSeleccionado = i;
				this.paginaActual = i / this.ELEMENTOS_POR_PAGINA;
				return true;
			}
		}
		return false;
	}

	private static String normalizar(final String s) {
		if (s == null) {
			return "";
		}
		return s.toLowerCase().replace(" ", "").replace("_", "").replace("á", "a").replace("é", "e").replace("í", "i")
				.replace("ó", "o").replace("ú", "u").replace("ñ", "n");
	}

	@Override
	public int getCantidadTotalElementos() {
		return this.ENTRADAS.size();
	}

	@Override
	protected void pintarElementoEnSlot(final Graphics2D g, final int index, final int slotX, final int slotY) {
		final EntradaPaleta entrada = this.ENTRADAS.get(index);
		if (entrada.icono != null) {
			this.dibujarIconoAjustadoAlSlot(g, entrada.icono, slotX, slotY);
		}

		final Font fontPrevia = g.getFont();
		g.setFont(FUENTE_BADGE);
		final String badge = entrada.esCosechable ? "T" : "E";
		final Color colorBadge = entrada.esCosechable ? new Color(60, 240, 80) : new Color(80, 180, 255);

		Render2D.dibujarRectanguloRelleno(g, slotX + 1, slotY + 1, 6, 6, Color.BLACK);
		Render2D.dibujarString(g, badge, slotX + 2, slotY + 6, colorBadge);
		g.setFont(fontPrevia);
	}

	public Objeto crearInstanciaSeleccionada(final int x, final int y) {
		if ((this.indiceSeleccionado >= 0) && (this.indiceSeleccionado < this.ENTRADAS.size())) {
			return this.ENTRADAS.get(this.indiceSeleccionado).creador.crear(x, y);
		}
		return null;
	}

	public EntradaPaleta getEntradaSeleccionada() {
		if ((this.indiceSeleccionado >= 0) && (this.indiceSeleccionado < this.ENTRADAS.size())) {
			return this.ENTRADAS.get(this.indiceSeleccionado);
		}
		return null;
	}

	@Override
	public String getNombreElemento(final int index) {
		return (index >= 0) && (index < this.ENTRADAS.size()) ? this.ENTRADAS.get(index).nombre : "";
	}

	@Override
	public boolean valoresYaEstablecidosPreviamente(final Tile tileEvaluar) {
		return false;
	}
}