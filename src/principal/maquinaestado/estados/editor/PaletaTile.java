package principal.maquinaestado.estados.editor;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import principal.entes.modelos.tile.ListaModeloTile;
import principal.mapa.Tile;
import principal.utilidades.Constantes;
import principal.utilidades.Render2D;

public class PaletaTile extends Paleta {

	private final ArrayList<Integer> CODIGOS_TILES = new ArrayList<Integer>();

	public PaletaTile(final int x, final int y, final int ancho, final int alto, final int ladoSlot) {
		super(x, y, ancho, alto, ladoSlot);
		this.cargarTiles();
	}

	private void cargarTiles() {
		this.CODIGOS_TILES.add(ListaModeloTile.COD_CESPED);
		this.CODIGOS_TILES.add(ListaModeloTile.COD_TIERRA);
		this.CODIGOS_TILES.add(ListaModeloTile.COD_TIERRA_2);
		this.CODIGOS_TILES.add(ListaModeloTile.COD_ARENA);
		this.CODIGOS_TILES.add(ListaModeloTile.COD_ASFALTO);
		this.CODIGOS_TILES.add(ListaModeloTile.COD_PIEDRA);
		this.CODIGOS_TILES.add(ListaModeloTile.COD_AGUA);
		this.CODIGOS_TILES.add(ListaModeloTile.COD_CESPED_2);
		this.CODIGOS_TILES.add(ListaModeloTile.COD_CESPED_3);
		this.CODIGOS_TILES.add(ListaModeloTile.COD_CESPED_3_NEVADO);
		this.CODIGOS_TILES.add(ListaModeloTile.COD_VACIO);
	}

	@Override
	public int getCantidadTotalElementos() {
		return this.CODIGOS_TILES.size();
	}

	@Override
	protected void pintarElementoEnSlot(final Graphics2D g, final int index, final int slotX, final int slotY) {
		final int codModelo = this.CODIGOS_TILES.get(index);
		if (ListaModeloTile.getModelo(codModelo) != null) {
			final BufferedImage img = ListaModeloTile.getModelo(codModelo).getTextura();
			if (img != null) {
				Render2D.dibujarImagen(g, img, slotX, slotY);
			}
		}
	}

	public Tile getTileSeleccionado() {
		if ((this.indiceSeleccionado >= 0) && (this.indiceSeleccionado < this.CODIGOS_TILES.size())) {
			final int cod = this.CODIGOS_TILES.get(this.indiceSeleccionado);
			return new Tile(0, 0, Constantes.LADO_TILE, cod);
		}
		return null;
	}

	@Override
	public String getNombreElemento(final int index) {
		return "Tile #" + this.CODIGOS_TILES.get(index);
	}

	@Override
	public boolean valoresYaEstablecidosPreviamente(final Tile tileEvaluar) {
		final Tile seleccionado = this.getTileSeleccionado();
		return (seleccionado != null) && (tileEvaluar != null)
				&& (seleccionado.getCodModelo() == tileEvaluar.getCodModelo());
	}
}