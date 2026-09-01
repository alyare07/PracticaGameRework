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

	@Override
	public int getCantidadTotalElementos() {
		return this.ENTRADAS.size();
	}

	@Override
	protected void pintarElementoEnSlot(final Graphics2D g, final int index, final int slotX, final int slotY) {
		final EntradaPaleta entrada = this.ENTRADAS.get(index);
		if (entrada.icono != null) {
			Render2D.dibujarImagen(g, entrada.icono, slotX, slotY);
		}

		// Insignia: [T] Verde para Cosechables/Talables, [E] Azul para Estáticos/Cofres
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