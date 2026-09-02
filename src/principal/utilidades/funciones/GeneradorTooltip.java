package principal.utilidades.funciones;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;

import principal.entes.objetos.items.Item;
import principal.graficos.SuperficieDibujo;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

/**
 * Clase de utilidad para la generación y renderizado dinámico de Tooltips
 * (cuadros informativos) flotantes con soporte de texto mixto (Bold + Plain) y
 * Zero-GC.
 * 
 * @version 2.0 (Vanilla Java 8)
 */
public final class GeneradorTooltip {

	private static final int MARGEN_CURSOR = 8;
	private static final int PADDING_INTERNO = 6;
	private static final int ESPACIADO_LINEAS = 2;

	private static final float TAMANIO_TITULO = 8.0f;
	private static final float TAMANIO_INFO = 6.0f;

	private static final Color COLOR_FONDO_DEFECTO = new Color(15, 15, 20, 235);
	private static final Color COLOR_BORDE_DEFECTO = new Color(80, 80, 100, 255);
	private static final Color COLOR_TITULO_DEFECTO = new Color(240, 200, 80);
	private static final Color COLOR_INFO_DEFECTO = Color.LIGHT_GRAY;

	public GeneradorTooltip() {
	}

	/**
	 * Dibuja un tooltip de una sola línea de texto en la posición del puntero del
	 * ratón.
	 */
	public void dibujarTooltip(final Graphics2D g, final String texto, final Color colorLetra, final Color colorFondo) {
		if ((g == null) || (texto == null) || texto.isEmpty()) {
			return;
		}

		final Font fuenteOriginal = g.getFont();
		try {
			final Font fuenteTooltip = Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, TAMANIO_INFO);
			g.setFont(fuenteTooltip);

			final Point raton = SuperficieDibujo.obtenerSuperficieDibujo().RATON.getPuntoPosicionEscalado();

			final int anchoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, texto);
			final int altoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, texto);

			final int anchoTotal = anchoTexto + (PADDING_INTERNO * 2);
			final int altoTotal = altoTexto + (PADDING_INTERNO * 2);

			final int boxX = this.calcularCoordenadaX(raton.x, anchoTotal);
			final int boxY = this.calcularCoordenadaY(raton.y, altoTotal);

			final Color fondoFinal = (colorFondo != null) ? colorFondo : COLOR_FONDO_DEFECTO;
			Render2D.dibujarRectanguloRelleno(g, boxX, boxY, anchoTotal, altoTotal, fondoFinal);
			Render2D.dibujarRectanguloContorno(g, boxX, boxY, anchoTotal, altoTotal, COLOR_BORDE_DEFECTO);

			final Color letraFinal = (colorLetra != null) ? colorLetra : Color.WHITE;
			Render2D.dibujarString(g, texto, boxX + PADDING_INTERNO, (boxY + PADDING_INTERNO + altoTexto) - 2,
					letraFinal);

		} finally {
			g.setFont(fuenteOriginal);
		}
	}

	/**
	 * Dibuja un tooltip horizontal continuo donde el TÍTULO va en BOLD (y su color)
	 * y la DESCRIPCIÓN en PLAIN (blanco/gris suave).
	 */
	public void dibujarTooltipConCabecera(final Graphics2D g, final String titulo, final String descripcion,
			final Color colorTitulo, final Color colorDesc, final Color colorFondo) {
		if ((g == null) || (titulo == null)) {
			return;
		}

		final Font fuenteOriginal = g.getFont();
		try {
			final Font fuenteBold = Globales.GESTOR_FUENTES.getFuente(Font.BOLD, TAMANIO_INFO);
			final Font fuentePlain = Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, TAMANIO_INFO);

			// 1. Medir ancho del título en BOLD
			g.setFont(fuenteBold);
			final int anchoTitulo = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, titulo);
			final int altoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, titulo);

			// 2. Medir ancho de la descripción en PLAIN
			g.setFont(fuentePlain);
			final int anchoDesc = (descripcion != null)
					? Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, descripcion)
					: 0;

			final int anchoTotal = anchoTitulo + anchoDesc + (PADDING_INTERNO * 2);
			final int altoTotal = altoTexto + (PADDING_INTERNO * 2);

			final Point raton = SuperficieDibujo.obtenerSuperficieDibujo().RATON.getPuntoPosicionEscalado();
			final int boxX = this.calcularCoordenadaX(raton.x, anchoTotal);
			final int boxY = this.calcularCoordenadaY(raton.y, altoTotal);

			// 3. Renderizar marco y fondo
			final Color fondoFinal = (colorFondo != null) ? colorFondo : COLOR_FONDO_DEFECTO;
			Render2D.dibujarRectanguloRelleno(g, boxX, boxY, anchoTotal, altoTotal, fondoFinal);
			Render2D.dibujarRectanguloContorno(g, boxX, boxY, anchoTotal, altoTotal, COLOR_BORDE_DEFECTO);

			final int yLinea = (boxY + PADDING_INTERNO + altoTexto) - 2;

			// 4. Dibujar Título en BOLD con su color temático
			g.setFont(fuenteBold);
			final Color cTit = (colorTitulo != null) ? colorTitulo : COLOR_TITULO_DEFECTO;
			Render2D.dibujarString(g, titulo, boxX + PADDING_INTERNO, yLinea, cTit);

			// 5. Dibujar Descripción en PLAIN a continuación del título
			if ((descripcion != null) && !descripcion.isEmpty()) {
				g.setFont(fuentePlain);
				final Color cDesc = (colorDesc != null) ? colorDesc : Color.WHITE;
				Render2D.dibujarString(g, descripcion, boxX + PADDING_INTERNO + anchoTitulo, yLinea, cDesc);
			}

		} finally {
			g.setFont(fuenteOriginal);
		}
	}

	/**
	 * Dibuja un tooltip detallado para un {@link Item}.
	 */
	public void dibujarTooltipItem(final Graphics2D g, final Item item) {
		if ((g == null) || (item == null)) {
			return;
		}

		final Font fuenteOriginal = g.getFont();

		try {
			final String nombre = item.getNombre();
			final ArrayList<String> infoLines = item.getInfo();

			final Font fuenteTitulo = Globales.GESTOR_FUENTES.getFuente(Font.BOLD, TAMANIO_TITULO);
			final Font fuenteInfo = Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, TAMANIO_INFO);

			g.setFont(fuenteTitulo);
			final int anchoNombre = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, nombre);
			final int altoNombre = Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, nombre);

			g.setFont(fuenteInfo);
			int maxAnchoInfo = 0;
			int altoTotalInfo = 0;

			if ((infoLines != null) && !infoLines.isEmpty()) {
				for (final String linea : infoLines) {
					if ((linea != null) && !linea.isEmpty()) {
						final int anchoLinea = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, linea);
						final int altoLinea = Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, linea);

						maxAnchoInfo = Math.max(maxAnchoInfo, anchoLinea);
						altoTotalInfo += altoLinea + ESPACIADO_LINEAS;
					}
				}
			}

			final int anchoContenido = Math.max(anchoNombre, maxAnchoInfo);
			final int anchoTotal = anchoContenido + (PADDING_INTERNO * 2);
			final int altoTotal = altoNombre + ESPACIADO_LINEAS + altoTotalInfo + (PADDING_INTERNO * 2);

			final Point raton = SuperficieDibujo.obtenerSuperficieDibujo().RATON.getPuntoPosicionEscalado();
			final int boxX = this.calcularCoordenadaX(raton.x, anchoTotal);
			final int boxY = this.calcularCoordenadaY(raton.y, altoTotal);

			Render2D.dibujarRectanguloRelleno(g, boxX, boxY, anchoTotal, altoTotal, COLOR_FONDO_DEFECTO);
			Render2D.dibujarRectanguloContorno(g, boxX, boxY, anchoTotal, altoTotal, COLOR_BORDE_DEFECTO);

			g.setFont(fuenteTitulo);
			int yCursor = (boxY + PADDING_INTERNO + altoNombre) - 2;
			Render2D.dibujarString(g, nombre, boxX + PADDING_INTERNO, yCursor, COLOR_TITULO_DEFECTO);

			if ((infoLines != null) && !infoLines.isEmpty()) {
				g.setFont(fuenteInfo);
				yCursor += ESPACIADO_LINEAS;

				for (final String linea : infoLines) {
					if ((linea != null) && !linea.isEmpty()) {
						final int altoLinea = Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, linea);
						yCursor += altoLinea + ESPACIADO_LINEAS;
						Render2D.dibujarString(g, linea, boxX + PADDING_INTERNO, yCursor - 2, COLOR_INFO_DEFECTO);
					}
				}
			}

		} finally {
			g.setFont(fuenteOriginal);
		}
	}

	private int calcularCoordenadaX(final int mouseX, final int anchoTotal) {
		final int x = (mouseX <= Constantes.CENTROX) ? mouseX + MARGEN_CURSOR : mouseX - anchoTotal - MARGEN_CURSOR;
		return Math.max(2, Math.min(x, Constantes.ANCHO_JUEGO - anchoTotal - 2));
	}

	private int calcularCoordenadaY(final int mouseY, final int altoTotal) {
		final int y = (mouseY <= Constantes.CENTROY) ? mouseY + MARGEN_CURSOR : mouseY - altoTotal - MARGEN_CURSOR;
		return Math.max(2, Math.min(y, Constantes.ALTO_JUEGO - altoTotal - 2));
	}
}