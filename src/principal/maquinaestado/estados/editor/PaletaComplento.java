package principal.maquinaestado.estados.editor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;

import principal.controles.Raton;
import principal.entes.objetos.Complemento;
import principal.mapa.Tile;
import principal.utilidades.DibujoDebug;

public class PaletaComplento extends Paleta {
	public static final int POSICIONAMIENTO_CENTRO = 0;
	public static final int POSICIONAMIENTO_LIBRE = 1;

	private final HashMap<Point, Complemento> COMPLEMENTOS = new HashMap<Point, Complemento>();
	public final int LADO;
	public final int X;
	public final int Y;
	public final int ANCHO;
	public final int ALTO;
	private Complemento complementoSeleccionado;
	private int posicionamiento = POSICIONAMIENTO_CENTRO;

	public PaletaComplento(int x, final int y, final int ancho, final int alto, final int ladoTile) {
		this.X = x;
		this.Y = y;
		this.ANCHO = ancho;
		this.ALTO = alto;
		this.LADO = ladoTile;
	}

	public boolean contienePuntoComplemento(final int x, final int y) {
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

	public Point intersectaComplemento(final Rectangle area) {
		for (int ejeY = area.y - (2 * LADO); ejeY < area.y + (2 * LADO);) {
			if (ejeY % LADO == 0) {
				for (int ejeX = area.x - (2 * LADO); ejeX < area.x + (2 * LADO);) {
					if (ejeX % LADO == 0) {
						if (contienePuntoComplemento(ejeX, ejeY)) {
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

	public Complemento seleccionarComplemento(Rectangle area) {
//		Rectangle r = null;
		for (Complemento c : COMPLEMENTOS.values()) {
//			r = new Rectangle(c.getPosicionXInt(), c.getPosicionYInt(), this.LADO, this.LADO);
			if (c.getArea().intersects(area)) {
				return c;
			}
		}
		return null;
	}

	public boolean agregarComplemento(final int codModeloComplemento) {
		for (int y = Y; y <= Y + ALTO - LADO; y += LADO) {
			for (int x = X; x <= X + ANCHO - LADO; x += LADO) {
				if (this.COMPLEMENTOS.get(new Point(x, y)) == null) {
					this.COMPLEMENTOS.put(new Point(x, y), new Complemento(x, y, codModeloComplemento));
					return true;
				}
			}
		}
		return false;
	}

	public boolean agregarComplemento(final Complemento c) {
		for (int y = Y; y <= Y + ALTO - LADO; y += LADO) {
			for (int x = X; x <= X + ANCHO - LADO; x += LADO) {
				if (this.COMPLEMENTOS.get(new Point(x, y)) == null) {
					final Complemento complemento = (Complemento) c.copiar();
					complemento.establecerPosicionX(x);
					complemento.establecerPosicionY(y);
					this.COMPLEMENTOS.put(new Point(x, y), complemento);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void pintar(final Graphics2D g) {
		for (Complemento c : COMPLEMENTOS.values()) {
			c.pintarFijo(g);
		}
		DibujoDebug.dibujarRectanguloContorno(g, this.X, this.Y, this.ANCHO, this.ALTO, Color.yellow);
		if (this.complementoSeleccionado != null) {
			final Rectangle area = this.complementoSeleccionado.getArea();
			DibujoDebug.dibujarRectanguloContorno(g, area, Color.WHITE);
			DibujoDebug.dibujarRectanguloContorno(g, area.x - 1, area.y - 1, area.width + 2, area.height + 2, Color.BLACK);
		}
	}

	@Override
	public void actualizar(final Raton raton) {
		if (raton.presionadoClickIzq()) {
			final Complemento c = seleccionarComplemento(raton.getPuntoPresionado());
			if (c != null) {
				this.complementoSeleccionado = c;
			}
		}

	}

	public int getPosicionamientoActual() {
		return posicionamiento;
	}

	@Override
	public boolean valoresYaEstalecidosPreviamente(Tile tileEvaluar) {
		Complemento aux = null;
		if (this.complementoSeleccionado == null) {
			return false;
		}
//		for (Objeto objeto : tileEvaluar.LISTA_OBJETOS) {
//			if (!(objeto instanceof Complemento)) {
//				continue;
//			}
//
//			aux = (Complemento) objeto.copiar();
//			if (!this.complementoSeleccionado.compararModelos(aux)) {
//				continue;
//			} else {
//				final Point puntoZona = tileEvaluar.getPosicionSegunZonaYArea(posicionamiento, this.complementoSeleccionado);
//				if (aux.getPosicionXInt() == puntoZona.x && aux.getPosicionYInt() == puntoZona.y) {
//					return true;
//				} else {
//					continue;
//				}
//			}
//
//		}
		return false;
	}

	public Complemento getComplementoSeleccionado() {
		return complementoSeleccionado;
	}

	public void establecerPosicion(final int p) {
		this.posicionamiento = p;
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
