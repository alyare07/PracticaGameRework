package principal.mapa;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.VolatileImage;

import principal.entes.modelos.tile.ListaModeloTile;
import principal.entes.modelos.tile.ModeloTile;
import principal.utilidades.Constantes;
import principal.utilidades.Render2D;
import principal.utilidades.Textura;

/**
 * Macro-bloque de terreno pre-horneado en VRAM (Zero-GC / O(1)).
 * Reduce drásticamente las llamadas de dibujo del suelo al consolidar
 * cuadrículas de 16x16 tiles en una única VolatileImage.
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class ChunkTerreno {

	private final int chunkX;
	private final int chunkY;
	private final int mundoX;
	private final int mundoY;
	private final int ladoPixeles;
	private final Rectangle area;

	private VolatileImage bufferVRAM;
	private boolean sucio = true;
	private boolean contieneAnimacion = false;
	private int lastFrameAnimacion = -1;

	public ChunkTerreno(final int chunkX, final int chunkY, final int ladoPixeles) {
		this.chunkX = chunkX;
		this.chunkY = chunkY;
		this.ladoPixeles = ladoPixeles;
		this.mundoX = chunkX * ladoPixeles;
		this.mundoY = chunkY * ladoPixeles;
		this.area = new Rectangle(this.mundoX, this.mundoY, ladoPixeles, ladoPixeles);
	}

	/**
	 * Hornea todos los tiles pertenecientes a este chunk en la VolatileImage local.
	 */
	public void bake(final Terreno terreno) {
		if (terreno == null) {
			return;
		}

		final GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice().getDefaultConfiguration();

		if ((this.bufferVRAM == null) || (this.bufferVRAM.validate(gc) == VolatileImage.IMAGE_INCOMPATIBLE)) {
			if (this.bufferVRAM != null) {
				this.bufferVRAM.flush();
			}
			this.bufferVRAM = gc.createCompatibleVolatileImage(this.ladoPixeles, this.ladoPixeles, Transparency.OPAQUE);
		}

		final Graphics2D gChunk = this.bufferVRAM.createGraphics();
		try {
			gChunk.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			gChunk.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

			final int ladoTile = terreno.ladoTile();
			final int tilesPorFila = this.ladoPixeles / ladoTile;

			final int startTileX = this.chunkX * tilesPorFila;
			final int startTileY = this.chunkY * tilesPorFila;

			this.contieneAnimacion = false;

			for (int ty = 0; ty < tilesPorFila; ty++) {
				for (int tx = 0; tx < tilesPorFila; tx++) {
					final int worldTileX = startTileX + tx;
					final int worldTileY = startTileY + ty;

					final Tile tile = terreno.getTileGrid(worldTileX, worldTileY);
					if (tile == null) {
						continue;
					}

					final int localX = tx * ladoTile;
					final int localY = ty * ladoTile;

					// 1. Dibujar capa de fondo si existe (ej. costas o transiciones)
					if (tile.getCodigoModeloFondo() != 0) {
						final ModeloTile modeloFondo = ListaModeloTile.getModelo(tile.getCodigoModeloFondo());
						if (modeloFondo != null) {
							final int texFondo = modeloFondo.getCodTextura(tile.getMascaraBit(), tile.getVariacionPropia());
							gChunk.drawImage(Textura.getTextura(texFondo), localX, localY, null);
							if (modeloFondo.getCantFramesAnimacion() > 1) {
								this.contieneAnimacion = true;
							}
						}
					}

					// 2. Dibujar capa de terreno principal
					final ModeloTile modelo = ListaModeloTile.getModelo(tile.getCodModelo());
					if (modelo != null) {
						final int texFinal = modelo.getCodTextura(tile.getMascaraBit(), tile.getVariacionPropia());
						gChunk.drawImage(Textura.getTextura(texFinal), localX, localY, null);
						if (modelo.getCantFramesAnimacion() > 1) {
							this.contieneAnimacion = true;
						}
					}
				}
			}

			// Registra el frame en el que fue horneado
			final ModeloTile modeloAgua = ListaModeloTile.getModelo(ListaModeloTile.COD_AGUA);
			if ((modeloAgua != null) && modeloAgua.contieneAnimacion()) {
				this.lastFrameAnimacion = modeloAgua.getAnimacion().getSpritePosicion();
			}

			this.sucio = false;

		} finally {
			gChunk.dispose();
		}
	}

	/**
	 * Renderiza el chunk completo con 1 sola llamada a drawImage().
	 */
	public void pintar(final Graphics2D g, final Terreno terreno) {
		// 1. Si contiene agua animada, verificar si cambió el fotograma global
		if (this.contieneAnimacion) {
			final ModeloTile modeloAgua = ListaModeloTile.getModelo(ListaModeloTile.COD_AGUA);
			if ((modeloAgua != null) && modeloAgua.contieneAnimacion()) {
				final int currentFrame = modeloAgua.getAnimacion().getSpritePosicion();
				if (currentFrame != this.lastFrameAnimacion) {
					this.sucio = true;
				}
			}
		}

		final GraphicsConfiguration gc = g.getDeviceConfiguration();

		// 2. Validar pérdida de contexto en VRAM o estado sucio
		if (this.sucio || (this.bufferVRAM == null) || (this.bufferVRAM.validate(gc) != VolatileImage.IMAGE_OK)) {
			this.bake(terreno);
		}

		// 3. Dibujar la imagen completa del chunk proyectada con la cámara
		Render2D.dibujarImagenRefCamara(g, this.bufferVRAM, this.mundoX, this.mundoY);
	}

	public void marcarSucio() {
		this.sucio = true;
	}

	public Rectangle getArea() {
		return this.area;
	}

	public int getMundoX() {
		return this.mundoX;
	}

	public int getMundoY() {
		return this.mundoY;
	}
}