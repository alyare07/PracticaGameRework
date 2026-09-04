package principal.maquinaestado.estados.editor.herramientas;

import principal.mapa.Terreno;
import principal.mapa.Tile;
import principal.recursos.TipoTerreno;

/**
 * Algoritmo de relleno por inundación (Flood Fill) de alto rendimiento.
 * Utiliza una cola BFS plana indexada sin recursión para evitar StackOverflowError.
 * 
 * @version 1.0 (Vanilla Java 8 - Non-Recursive BFS)
 */
public class FloodFillTerreno {

	// Desplazamientos ortogonales (Norte, Este, Sur, Oeste)
	private static final int[] DX = { 0, 1, 0, -1 };
	private static final int[] DY = { -1, 0, 1, 0 };

	private int[] colaIndices;
	private boolean[] visitados;

	public FloodFillTerreno() {
	}

	private void asegurarCapacidad(final int totalTiles) {
		if (this.colaIndices == null || this.colaIndices.length < totalTiles) {
			this.colaIndices = new int[totalTiles];
			this.visitados = new boolean[totalTiles];
		}
	}

	/**
	 * Ejecuta el relleno por inundación desde una posición inicial, reemplazando
	 * todos los tiles contiguos del mismo tipo.
	 * 
	 * @param terreno Terreno a modificar.
	 * @param startTileX Coordenada X del tile origen.
	 * @param startTileY Coordenada Y del tile origen.
	 * @param nuevoTipo Tipo de terreno que se pintará.
	 * @return Cantidad de tiles modificados.
	 */
	public int ejecutar(final Terreno terreno, final int startTileX, final int startTileY, final TipoTerreno nuevoTipo) {
		if (terreno == null || nuevoTipo == null) {
			return 0;
		}

		final int cantX = terreno.getAncho() / terreno.ladoTile();
		final int cantY = terreno.getAlto() / terreno.ladoTile();
		final int totalTiles = cantX * cantY;

		if (startTileX < 0 || startTileX >= cantX || startTileY < 0 || startTileY >= cantY) {
			return 0;
		}

		final Tile tileInicial = terreno.getTileGrid(startTileX, startTileY);
		if (tileInicial == null) {
			return 0;
		}

		final TipoTerreno tipoObjetivo = tileInicial.getTipoTerreno();
		if (tipoObjetivo == nuevoTipo) {
			return 0; // Mismo tipo, no hay cambios
		}

		this.asegurarCapacidad(totalTiles);
		java.util.Arrays.fill(this.visitados, 0, totalTiles, false);

		int cabeza = 0;
		int cola = 0;

		final int indiceInicio = (startTileY * cantX) + startTileX;
		this.colaIndices[cola++] = indiceInicio;
		this.visitados[indiceInicio] = true;

		int tilesModificados = 0;
		final int ladoTile = terreno.ladoTile();

		while (cabeza < cola) {
			final int index = this.colaIndices[cabeza++];
			final int tx = index % cantX;
			final int ty = index / cantX;

			// Reemplaza el tile en el terreno
			terreno.establecerTileReferenciado(tx * ladoTile, ty * ladoTile, new Tile(tx * ladoTile, ty * ladoTile, ladoTile, nuevoTipo));
			tilesModificados++;

			// Expansión a vecinos ortogonales
			for (int d = 0; d < 4; d++) {
				final int nx = tx + DX[d];
				final int ny = ty + DY[d];

				if (nx >= 0 && nx < cantX && ny >= 0 && ny < cantY) {
					final int nIndex = (ny * cantX) + nx;
					if (!this.visitados[nIndex]) {
						this.visitados[nIndex] = true;
						final Tile tileVecino = terreno.getTileGrid(nx, ny);
						if (tileVecino != null && tileVecino.getTipoTerreno() == tipoObjetivo) {
							this.colaIndices[cola++] = nIndex;
						}
					}
				}
			}
		}

		if (tilesModificados > 0) {
			terreno.calcularAutotiles();
		}

		return tilesModificados;
	}
}