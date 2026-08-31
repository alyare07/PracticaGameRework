package principal.maquinaestado.estados.editor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;

import principal.controles.Raton;
import principal.mapa.Tile;
import principal.utilidades.Render2D;

public class PaletaTile extends Paleta {

	private final HashMap<Point, Tile> TILES = new HashMap<Point, Tile>();
	public final int LADO;
	public final int X;
	public final int Y;
	public final int ANCHO;
	public final int ALTO;
	private Tile tileSeleccionado;

	public PaletaTile(int x, final int y, final int ancho, final int alto, final int ladoTile) {
		this.X = x;
		this.Y = y;
		this.ANCHO = ancho;
		this.ALTO = alto;
		this.LADO = ladoTile;
	}

	public boolean contienePuntoTile(final int x, final int y) {
		boolean contiene = false;
		if (y >= 0 && y < ALTO) {
			if (y % LADO == 0) {
				if (x >= 0 && x < ANCHO) {
					if (x % LADO == 0) {
						contiene = true;
					}
				}
			}
		}
		return contiene;
	}

	public Point intersectaTile(final Rectangle area) {
		for (int ejeY = area.y - (2 * LADO); ejeY < area.y + (2 * LADO);) {
			if (ejeY % LADO == 0) {
				for (int ejeX = area.x - (2 * LADO); ejeX < area.x + (2 * LADO);) {
					if (ejeX % LADO == 0) {
						if (contienePuntoTile(ejeX, ejeY)) {
							if (new Rectangle(ejeX, ejeY, LADO, LADO).intersects(area)) {
								return new Point(ejeX, ejeY);
							}
						}
						ejeX += LADO;
					} else {
						ejeX++;
					}
				}
				ejeY += LADO;

			} else {
				ejeY++;
			}
		}
		return null;
	}

	public Tile seleccionarTile(Rectangle area) {
		for (Tile t : TILES.values()) {
			if (t.getArea().intersects(area)) {
				return t;
			}
		}
		return null;
	}

	public boolean agregarTile(final int codModeloTile) {
		for (int y = Y; y <= Y + ALTO - LADO; y += LADO) {
			for (int x = X; x <= X + ANCHO - LADO; x += LADO) {
				if (TILES.get(new Point(x, y)) == null) {
					TILES.put(new Point(x, y), new Tile(x, y, LADO, codModeloTile));
					return true;
				}
			}
		}
		return false;
	}

	public void pintar(final Graphics2D g) {
		for (Tile t : TILES.values()) {
			t.pintarPaleta(g);
		}
		Render2D.dibujarRectanguloContorno(g, this.X, this.Y, this.ANCHO, this.ALTO, Color.yellow);
		if (this.tileSeleccionado != null) {
			final Rectangle area = this.tileSeleccionado.getArea();
			Render2D.dibujarRectanguloContorno(g, area, Color.WHITE);
			Render2D.dibujarRectanguloContorno(g, area.x - 1, area.y - 1, area.width + 2, area.height + 2,
					Color.BLACK);
		}
	}

	public HashMap<Point, Tile> getTiles() {
		return this.TILES;
	}

	public void actualizar(final Raton raton) {
		if (raton.presionadoClickIzq()) {
			Tile t = seleccionarTile(raton.getPuntoPresionado());
			if (t != null) {

				this.tileSeleccionado = t;
			}
		}

	}

	public Tile getTileSeleccionado() {
		return tileSeleccionado;
	}

	public boolean valoresYaEstalecidosPreviamente(final Tile tileEvaluar) {

		if ((this.tileSeleccionado.getCodModelo() == tileEvaluar.getCodModelo())) {
			return true;
		}
		return false;
	}

	@Override
	public int getLado() {
		return this.LADO;
	}

	@Override
	public int getPosicionX() {
		return this.X;
	}

	@Override
	public int getPosicionY() {
		return this.Y;
	}

	@Override
	public int getAncho() {
		return this.ANCHO;
	}

	@Override
	public int getAlto() {
		return this.ALTO;
	}

}
