package principal.maquinaestado.estados.editor;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import principal.mapa.Tile;
import principal.recursos.SetTerreno;
import principal.recursos.TipoTerreno;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

public class PaletaTile extends Paleta {

	private final TipoTerreno[] TIPOS_TERRENO = TipoTerreno.values();

	public PaletaTile(final int x, final int y, final int ancho, final int alto, final int ladoSlot) {
		super(x, y, ancho, alto, ladoSlot);
	}

	@Override
	public int getCantidadTotalElementos() {
		return this.TIPOS_TERRENO.length;
	}

	@Override
	protected void pintarElementoEnSlot(final Graphics2D g, final int index, final int slotX, final int slotY) {
		final TipoTerreno tipo = this.TIPOS_TERRENO[index];
		final SetTerreno set = Globales.GESTOR_TEXTURAS.getSetTerreno(tipo);
		if (set != null) {
			final BufferedImage img = set.getSpriteBase();
			if (img != null) {
				Render2D.dibujarImagen(g, img, slotX, slotY);
			}
		}
	}

	public Tile getTileSeleccionado() {
		if ((this.indiceSeleccionado >= 0) && (this.indiceSeleccionado < this.TIPOS_TERRENO.length)) {
			final TipoTerreno tipo = this.TIPOS_TERRENO[this.indiceSeleccionado];
			return new Tile(0, 0, Constantes.LADO_TILE, tipo);
		}
		return null;
	}

	@Override
	public String getNombreElemento(final int index) {
		return ((index >= 0) && (index < this.TIPOS_TERRENO.length)) ? this.TIPOS_TERRENO[index].getNombre() : "";
	}

	@Override
	public boolean valoresYaEstablecidosPreviamente(final Tile tileEvaluar) {
		final Tile seleccionado = this.getTileSeleccionado();
		return (seleccionado != null) && (tileEvaluar != null)
				&& (seleccionado.getTipoTerreno() == tileEvaluar.getTipoTerreno());
	}
}