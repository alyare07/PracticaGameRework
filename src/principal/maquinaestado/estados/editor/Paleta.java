package principal.maquinaestado.estados.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import principal.controles.Raton;
import principal.mapa.Tile;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

public abstract class Paleta {

	protected final Rectangle AREA;
	protected final int LADO_SLOT;
	protected final int MARGEN = 4;
	protected final int COLUMNAS;
	protected final int FILAS;
	protected final int ELEMENTOS_POR_PAGINA;

	protected int paginaActual = 0;
	protected int indiceSeleccionado = 0;

	protected final Rectangle botonPaginaAnterior;
	protected final Rectangle botonPaginaSiguiente;
	private static final Font FUENTE_PAGINACION = new Font(Font.SANS_SERIF, Font.PLAIN, 6);

	public Paleta(final int x, final int y, final int ancho, final int alto, final int ladoSlot) {
		this.AREA = new Rectangle(x, y, ancho, alto);
		this.LADO_SLOT = Math.max(16, ladoSlot);

		final int espacioUtilAncho = ancho - (this.MARGEN * 2);
		final int espacioUtilAlto = alto - (this.MARGEN * 2) - 18;

		this.COLUMNAS = Math.max(1, espacioUtilAncho / (this.LADO_SLOT + this.MARGEN));
		this.FILAS = Math.max(1, espacioUtilAlto / (this.LADO_SLOT + this.MARGEN));
		this.ELEMENTOS_POR_PAGINA = this.COLUMNAS * this.FILAS;

		final int yBotonera = (this.AREA.y + this.AREA.height) - 16;
		this.botonPaginaAnterior = new Rectangle(this.AREA.x + this.MARGEN, yBotonera, 16, 12);
		this.botonPaginaSiguiente = new Rectangle((this.AREA.x + this.AREA.width) - this.MARGEN - 16, yBotonera, 16,
				12);
	}

	public void actualizar(final Raton raton) {
		if ((raton == null) || !raton.presionadoClickIzq()) {
			return;
		}

		final Rectangle pClick = raton.getPuntoPresionado();

		// 1. Navegación de páginas
		if (pClick.intersects(this.botonPaginaAnterior)) {
			this.anteriorPagina();
			return;
		}
		if (pClick.intersects(this.botonPaginaSiguiente)) {
			this.siguientePagina();
			return;
		}

		// 2. Selección de elemento en O(1)
		if (pClick.intersects(this.AREA)) {
			final int relX = pClick.x - (this.AREA.x + this.MARGEN);
			final int relY = pClick.y - (this.AREA.y + this.MARGEN);

			if ((relX < 0) || (relY < 0)) {
				return;
			}

			final int paso = this.LADO_SLOT + this.MARGEN;
			final int col = relX / paso;
			final int fila = relY / paso;

			if ((col >= 0) && (col < this.COLUMNAS) && (fila >= 0) && (fila < this.FILAS)) {
				final int xEnSlot = relX % paso;
				final int yEnSlot = relY % paso;

				if ((xEnSlot <= this.LADO_SLOT) && (yEnSlot <= this.LADO_SLOT)) {
					final int indiceEnPagina = (fila * this.COLUMNAS) + col;
					final int indiceGlobal = (this.paginaActual * this.ELEMENTOS_POR_PAGINA) + indiceEnPagina;

					if (indiceGlobal < this.getCantidadTotalElementos()) {
						this.indiceSeleccionado = indiceGlobal;
					}
				}
			}
		}
	}

	public void pintar(final Graphics2D g) {
		Render2D.dibujarRectanguloRelleno(g, this.AREA, new Color(45, 45, 50));
		Render2D.dibujarRectanguloContorno(g, this.AREA, Color.BLACK);

		final int total = this.getCantidadTotalElementos();
		final int inicio = this.paginaActual * this.ELEMENTOS_POR_PAGINA;
		final int fin = Math.min(total, inicio + this.ELEMENTOS_POR_PAGINA);

		final int paso = this.LADO_SLOT + this.MARGEN;

		for (int i = inicio; i < fin; i++) {
			final int indiceEnPagina = i - inicio;
			final int col = indiceEnPagina % this.COLUMNAS;
			final int fila = indiceEnPagina / this.COLUMNAS;

			final int slotX = this.AREA.x + this.MARGEN + (col * paso);
			final int slotY = this.AREA.y + this.MARGEN + (fila * paso);

			Render2D.dibujarRectanguloRelleno(g, slotX, slotY, this.LADO_SLOT, this.LADO_SLOT, new Color(30, 30, 35));
			Render2D.dibujarRectanguloContorno(g, slotX, slotY, this.LADO_SLOT, this.LADO_SLOT, Color.DARK_GRAY);

			this.pintarElementoEnSlot(g, i, slotX, slotY);

			if (i == this.indiceSeleccionado) {
				Render2D.dibujarRectanguloContorno(g, slotX, slotY, this.LADO_SLOT, this.LADO_SLOT, Color.YELLOW);
				Render2D.dibujarRectanguloContorno(g, slotX - 1, slotY - 1, this.LADO_SLOT + 2, this.LADO_SLOT + 2,
						Color.WHITE);
			}
		}

		this.pintarControlesPaginacion(g);
	}

	/**
	 * Dibuja un icono centrando y escalando proporcionalmente cualquier sprite que
	 * supere las dimensiones del slot (ej: Casa 64x64 en slot de 32x32) sin
	 * desbordar.
	 */
	protected void dibujarIconoAjustadoAlSlot(final Graphics2D g, final BufferedImage img, final int slotX,
			final int slotY) {
		if (img == null) {
			return;
		}

		final int imgW = img.getWidth();
		final int imgH = img.getHeight();

		// Si entra sin desbordar, se centra
		if ((imgW <= this.LADO_SLOT) && (imgH <= this.LADO_SLOT)) {
			final int x = slotX + ((this.LADO_SLOT - imgW) / 2);
			final int y = slotY + ((this.LADO_SLOT - imgH) / 2);
			Render2D.dibujarImagen(g, img, x, y);
		} else {
			// Si es más grande, se escala manteniendo el Aspect Ratio
			final double factor = Math.min((double) this.LADO_SLOT / imgW, (double) this.LADO_SLOT / imgH);
			final int drawW = Math.max(1, (int) Math.round(imgW * factor));
			final int drawH = Math.max(1, (int) Math.round(imgH * factor));

			final int drawX = slotX + ((this.LADO_SLOT - drawW) / 2);
			final int drawY = slotY + ((this.LADO_SLOT - drawH) / 2);

			g.drawImage(img, drawX, drawY, drawW, drawH, null);
			Render2D.registrarLlamadas(1);
		}
	}

	private void pintarControlesPaginacion(final Graphics2D g) {
		final Font fuentePrevia = g.getFont();
		g.setFont(FUENTE_PAGINACION);

		// Botón Anterior
		Render2D.dibujarRectanguloRelleno(g, this.botonPaginaAnterior,
				(this.paginaActual > 0) ? Color.GRAY : Color.DARK_GRAY);
		Render2D.dibujarRectanguloContorno(g, this.botonPaginaAnterior, Color.BLACK);
		Render2D.dibujarString(g, "<", this.botonPaginaAnterior.x + 6, this.botonPaginaAnterior.y + 9, Color.WHITE);

		// Botón Siguiente
		final int totalPaginas = this.getTotalPaginas();
		Render2D.dibujarRectanguloRelleno(g, this.botonPaginaSiguiente,
				(this.paginaActual < (totalPaginas - 1)) ? Color.GRAY : Color.DARK_GRAY);
		Render2D.dibujarRectanguloContorno(g, this.botonPaginaSiguiente, Color.BLACK);
		Render2D.dibujarString(g, ">", this.botonPaginaSiguiente.x + 6, this.botonPaginaSiguiente.y + 9, Color.WHITE);

		// Texto de página actual
		final String textoPag = "Pag " + (this.paginaActual + 1) + "/" + totalPaginas;
		final int anchoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, textoPag);
		final int centroX = (this.AREA.x + (this.AREA.width / 2)) - (anchoTexto / 2);
		Render2D.dibujarString(g, textoPag, centroX, this.botonPaginaAnterior.y + 9, Color.WHITE);

		g.setFont(fuentePrevia);
	}

	public void siguientePagina() {
		if (this.paginaActual < (this.getTotalPaginas() - 1)) {
			this.paginaActual++;
		}
	}

	public void anteriorPagina() {
		if (this.paginaActual > 0) {
			this.paginaActual--;
		}
	}

	public int getTotalPaginas() {
		return Math.max(1, (int) Math.ceil((double) this.getCantidadTotalElementos() / this.ELEMENTOS_POR_PAGINA));
	}

	public int getIndiceSeleccionado() {
		return this.indiceSeleccionado;
	}

	public void setIndiceSeleccionado(final int indice) {
		if ((indice >= 0) && (indice < this.getCantidadTotalElementos())) {
			this.indiceSeleccionado = indice;
		}
	}

	public abstract int getCantidadTotalElementos();

	protected abstract void pintarElementoEnSlot(final Graphics2D g, final int index, final int slotX, final int slotY);

	public abstract String getNombreElemento(final int index);

	public abstract boolean valoresYaEstablecidosPreviamente(final Tile tileEvaluar);

	public int getLado() {
		return this.LADO_SLOT;
	}

	public int getPosicionX() {
		return this.AREA.x;
	}

	public int getPosicionY() {
		return this.AREA.y;
	}

	public int getAncho() {
		return this.AREA.width;
	}

	public int getAlto() {
		return this.AREA.height;
	}
}