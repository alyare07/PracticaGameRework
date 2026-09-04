package principal.maquinaestado.estados.editor.historial;

import principal.mapa.Terreno;
import principal.mapa.Tile;
import principal.recursos.TipoTerreno;

/**
 * Registra una modificación en bloque sobre el terreno (trazos de pincel,
 * flood fill, rectángulos o reemplazo) permitiendo revertirla o rehacerla en O(K).
 * 
 * @version 1.0 (Vanilla Java 8 - Zero-GC Delta)
 */
public class AccionHistorialTerreno implements AccionHistorial {

	private final Terreno terreno;
	private final int[] indicesTiles;
	private final TipoTerreno[] tiposPrevios;
	private final TipoTerreno[] tiposNuevos;

	public AccionHistorialTerreno(final Terreno terreno, final int[] indicesTiles,
			final TipoTerreno[] tiposPrevios, final TipoTerreno[] tiposNuevos) {
		this.terreno = terreno;
		this.indicesTiles = indicesTiles;
		this.tiposPrevios = tiposPrevios;
		this.tiposNuevos = tiposNuevos;
	}

	@Override
	public void deshacer() {
		if (this.terreno == null || this.indicesTiles == null) {
			return;
		}

		final int cantX = this.terreno.getAncho() / this.terreno.ladoTile();
		final int lado = this.terreno.ladoTile();

		for (int i = 0; i < this.indicesTiles.length; i++) {
			final int idx = this.indicesTiles[i];
			final int tx = idx % cantX;
			final int ty = idx / cantX;
			this.terreno.establecerTileReferenciado(tx * lado, ty * lado,
					new Tile(tx * lado, ty * lado, lado, this.tiposPrevios[i]));
		}
		this.terreno.calcularAutotiles();
	}

	@Override
	public void rehacer() {
		if (this.terreno == null || this.indicesTiles == null) {
			return;
		}

		final int cantX = this.terreno.getAncho() / this.terreno.ladoTile();
		final int lado = this.terreno.ladoTile();

		for (int i = 0; i < this.indicesTiles.length; i++) {
			final int idx = this.indicesTiles[i];
			final int tx = idx % cantX;
			final int ty = idx / cantX;
			this.terreno.establecerTileReferenciado(tx * lado, ty * lado,
					new Tile(tx * lado, ty * lado, lado, this.tiposNuevos[i]));
		}
		this.terreno.calcularAutotiles();
	}

	@Override
	public String getDescripcion() {
		return "Modificar " + (this.indicesTiles != null ? this.indicesTiles.length : 0) + " Tiles";
	}
}