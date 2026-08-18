package principal.utilidades.funciones;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;

import principal.entes.objetos.items.Item;
import principal.graficos.SuperficieDibujo;
import principal.utilidades.DibujoDebug;
import principal.utilidades.Globales;

/**
 * Clase de utilidad para la generación y renderizado dinámico de Tooltips
 * (cuadros informativos) flotantes cerca del puntero del ratón, adaptándose
 * automáticamente a los bordes de la pantalla.
 */
public final class GeneradorTooltip {

	private static final int MARGEN_CURSOR = 8;
	private static final int PADDING_INTERNO = 6;
	private static final int ESPACIADO_LINEAS = 2;

	// Tamaños explícitos de fuentes (Título Grande, Información Pequeña)
	private static final float TAMANIO_TITULO = 8.0f;
	private static final float TAMANIO_INFO = 6.5f;

	private static final Color COLOR_FONDO_DEFECTO = new Color(15, 15, 20, 235); // Negro translúcido
	private static final Color COLOR_BORDE_DEFECTO = new Color(80, 80, 100, 255);
	private static final Color COLOR_TITULO_DEFECTO = new Color(240, 200, 80); // Dorado
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
			final Font fuenteTooltip = fuenteOriginal.deriveFont(Font.PLAIN, TAMANIO_INFO);
			g.setFont(fuenteTooltip);

			final Point raton = SuperficieDibujo.obtenerSuperficieDibujo().RATON.getPuntoPosicionEscalado();

			final int anchoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, texto);
			final int altoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, texto);

			final int anchoTotal = anchoTexto + (PADDING_INTERNO * 2);
			final int altoTotal = altoTexto + (PADDING_INTERNO * 2);

			final int boxX = this.calcularCoordenadaX(raton.x, anchoTotal);
			final int boxY = this.calcularCoordenadaY(raton.y, altoTotal);

			final Color fondoFinal = (colorFondo != null) ? colorFondo : COLOR_FONDO_DEFECTO;
			DibujoDebug.dibujarRectanguloRelleno(g, boxX, boxY, anchoTotal, altoTotal, fondoFinal);
			DibujoDebug.dibujarRectanguloContorno(g, boxX, boxY, anchoTotal, altoTotal, COLOR_BORDE_DEFECTO);

			final Color letraFinal = (colorLetra != null) ? colorLetra : Color.WHITE;
			DibujoDebug.dibujarString(g, texto, boxX + PADDING_INTERNO, (boxY + PADDING_INTERNO + altoTexto) - 2,
					letraFinal);

		} finally {
			g.setFont(fuenteOriginal);
		}
	}

	/**
	 * Dibuja un tooltip detallado para un {@link Item}, con el Nombre GRANDE en
	 * dorado y la Información PEQUEÑA formateada debajo.
	 */
	public void dibujarTooltipItem(final Graphics2D g, final Item item) {
		if ((g == null) || (item == null)) {
			return;
		}

		final Font fuenteOriginal = g.getFont();

		try {
			final String nombre = item.getNombre();
			final ArrayList<String> infoLines = item.getInfo();

			// Configurar Fuentes Explícitas: Título Grande y BOLD, Info Pequeña y PLAIN
			final Font fuenteTitulo = fuenteOriginal.deriveFont(Font.BOLD, TAMANIO_TITULO);
			final Font fuenteInfo = fuenteOriginal.deriveFont(Font.PLAIN, TAMANIO_INFO);

			// 1. Medir Título (Fuente Grande)
			g.setFont(fuenteTitulo);
			final int anchoNombre = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, nombre);
			final int altoNombre = Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, nombre);

			// 2. Medir Líneas de Descripción (Fuente Pequeña)
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

			// 3. Dimensiones Totales del Recuadro Único
			final int anchoContenido = Math.max(anchoNombre, maxAnchoInfo);
			final int anchoTotal = anchoContenido + (PADDING_INTERNO * 2);
			final int altoTotal = altoNombre + ESPACIADO_LINEAS + altoTotalInfo + (PADDING_INTERNO * 2);

			// 4. Posición Adaptativa cerca del Cursor
			final Point raton = SuperficieDibujo.obtenerSuperficieDibujo().RATON.getPuntoPosicionEscalado();
			final int boxX = this.calcularCoordenadaX(raton.x, anchoTotal);
			final int boxY = this.calcularCoordenadaY(raton.y, altoTotal);

			// 5. Renderizar Fondo Único y Borde
			DibujoDebug.dibujarRectanguloRelleno(g, boxX, boxY, anchoTotal, altoTotal, COLOR_FONDO_DEFECTO);
			DibujoDebug.dibujarRectanguloContorno(g, boxX, boxY, anchoTotal, altoTotal, COLOR_BORDE_DEFECTO);

			// 6. Renderizar Nombre (GRANDE Y DORADO)
			g.setFont(fuenteTitulo);
			int yCursor = (boxY + PADDING_INTERNO + altoNombre) - 2;
			DibujoDebug.dibujarString(g, nombre, boxX + PADDING_INTERNO, yCursor, COLOR_TITULO_DEFECTO);

			// 7. Renderizar Información (PEQUEÑA Y GRIS)
			if ((infoLines != null) && !infoLines.isEmpty()) {
				g.setFont(fuenteInfo);
				yCursor += ESPACIADO_LINEAS;

				for (final String linea : infoLines) {
					if ((linea != null) && !linea.isEmpty()) {
						final int altoLinea = Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, linea);
						yCursor += altoLinea + ESPACIADO_LINEAS;
						DibujoDebug.dibujarString(g, linea, boxX + PADDING_INTERNO, yCursor - 2, COLOR_INFO_DEFECTO);
					}
				}
			}

		} finally {
			// Restaurar siempre la fuente original de Graphics2D
			g.setFont(fuenteOriginal);
		}
	}

	// -----------------------------------------------------------------------
	// CÁLCULOS MATEMÁTICOS DE POSICIONAMIENTO
	// -----------------------------------------------------------------------

	private int calcularCoordenadaX(final int mouseX, final int anchoTotal) {
		final int x = (mouseX <= Globales.CONSTANTES.CENTROX) ? mouseX + MARGEN_CURSOR
				: mouseX - anchoTotal - MARGEN_CURSOR;

		return Math.max(2, Math.min(x, Globales.CONSTANTES.ANCHO_JUEGO - anchoTotal - 2));
	}

	private int calcularCoordenadaY(final int mouseY, final int altoTotal) {
		final int y = (mouseY <= Globales.CONSTANTES.CENTROY) ? mouseY + MARGEN_CURSOR
				: mouseY - altoTotal - MARGEN_CURSOR;

		return Math.max(2, Math.min(y, Globales.CONSTANTES.ALTO_JUEGO - altoTotal - 2));
	}
}