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
 * Gestor y motor universal de renderizado de Tooltips flotantes con separación
 * explícita de rutas rápidas (línea simple), soporte dinámico de saltos de
 * línea '\n', desplazamiento por rueda (Scroll) y Zero-GC.
 * 
 * @version 7.0 (Vanilla Java 8 - Dedicated Fast Path & Newline Splitter)
 */
public final class GeneradorTooltip {

	private static final int MARGEN_CURSOR = 8;
	private static final int PADDING_INTERNO = 5;
	private static final int ESPACIADO_LINEAS = 1;

	private static final int MAX_ALTO_TOOLTIP = 115;
	private static final int ANCHO_SCROLLBAR = 3;
	private static final int PASO_SCROLL = 8;

	// Estandarizado a la matriz nativa de m5x7 (16f)
	private static final float TAMANIO_NATIVO = 16.0f;

	private static final Color COLOR_FONDO_DEFECTO = new Color(15, 15, 20, 240);
	private static final Color COLOR_BORDE_DEFECTO = new Color(75, 80, 100, 255);
	private static final Color COLOR_TITULO_DEFECTO = new Color(240, 200, 80);
	private static final Color COLOR_INFO_DEFECTO = new Color(200, 205, 215);

	private static final Color COLOR_TRACK_SCROLL = new Color(25, 30, 40, 200);
	private static final Color COLOR_THUMB_SCROLL = new Color(220, 180, 50, 240); // Oro

	// --- Estado del Motor Universal de Scroll (Zero-GC / O(1) puro) ---
	private int scrollY = 0;
	private int ultimoIdContenido = 0;

	public GeneradorTooltip() {
	}

	// =========================================================================
	// === 1. TOOLTIP DE UNA SOLA LÍNEA (Ruta Rápida / Fast Path - 0 Escaneo)
	// =========================================================================

	public void dibujarTooltip(final Graphics2D g, final String texto, final Color colorLetra, final Color colorFondo) {
		if ((g == null) || (texto == null) || texto.isEmpty()) {
			return;
		}

		final Font fuenteOriginal = g.getFont();
		try {
			final Font fuenteTooltip = Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, TAMANIO_NATIVO);
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
			Render2D.dibujarStringConSombra(g, texto, boxX + PADDING_INTERNO, (boxY + PADDING_INTERNO + altoTexto) - 2,
					letraFinal, Color.BLACK);

		} finally {
			g.setFont(fuenteOriginal);
		}
	}

	// =========================================================================
	// === 2. TOOLTIP MULTILÍNEA CON SALTO DE LÍNEA DINÁMICO '\n' (Zero-GC)
	// =========================================================================

	/**
	 * Dibuja un cuadro de información dividiendo el texto automáticamente por cada
	 * '\n' que contenga, sin instanciar arreglos en memoria (0 B/frame).
	 */
	public void dibujarTooltipConSaltoDeLinea(final Graphics2D g, final String textoConSaltos, final Color colorLetra,
			final Color colorFondo) {
		if ((g == null) || (textoConSaltos == null) || textoConSaltos.isEmpty()) {
			return;
		}

		final Font fuenteOriginal = g.getFont();
		try {
			final Font fuenteTooltip = Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, TAMANIO_NATIVO);
			g.setFont(fuenteTooltip);

			final int altoLinea = Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, "A");
			int maxAnchoTexto = 0;
			int cantidadLineas = 0;

			int inicio = 0;
			int fin;
			final int len = textoConSaltos.length();

			// 1. Medición de dimensiones por índices sin substring ni split
			while (inicio < len) {
				fin = textoConSaltos.indexOf('\n', inicio);
				if (fin == -1) {
					fin = len;
				}

				final int anchoLinea = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g,
						textoConSaltos.substring(inicio, fin));
				maxAnchoTexto = Math.max(maxAnchoTexto, anchoLinea);
				cantidadLineas++;

				inicio = fin + 1;
			}

			final int altoTotalContenido = (cantidadLineas * altoLinea) + ((cantidadLineas - 1) * ESPACIADO_LINEAS)
					+ (PADDING_INTERNO * 2);

			final boolean tieneScroll = altoTotalContenido > MAX_ALTO_TOOLTIP;
			final int maxScrollY = tieneScroll ? (altoTotalContenido - MAX_ALTO_TOOLTIP) : 0;

			// Gestión de Identificador y Rueda del Ratón
			final int id = textoConSaltos.hashCode();
			this.actualizarIdentificadorYScroll(id, maxScrollY);

			final int anchoTotalBox = maxAnchoTexto + (PADDING_INTERNO * 2) + (tieneScroll ? (ANCHO_SCROLLBAR + 3) : 0);
			final int altoTotalBox = tieneScroll ? MAX_ALTO_TOOLTIP : altoTotalContenido;

			final Point raton = SuperficieDibujo.obtenerSuperficieDibujo().RATON.getPuntoPosicionEscalado();
			final int boxX = this.calcularCoordenadaX(raton.x, anchoTotalBox);
			final int boxY = this.calcularCoordenadaY(raton.y, altoTotalBox);

			final Color fondoFinal = (colorFondo != null) ? colorFondo : COLOR_FONDO_DEFECTO;
			Render2D.dibujarRectanguloRelleno(g, boxX, boxY, anchoTotalBox, altoTotalBox, fondoFinal);
			Render2D.dibujarRectanguloContorno(g, boxX, boxY, anchoTotalBox, altoTotalBox, COLOR_BORDE_DEFECTO);

			// 2. Renderizado recortado
			final Graphics2D gClip = (Graphics2D) g.create();
			try {
				gClip.setClip(boxX + 2, boxY + 2, anchoTotalBox - 4 - (tieneScroll ? (ANCHO_SCROLLBAR + 2) : 0),
						altoTotalBox - 4);

				int yCursor = (boxY + PADDING_INTERNO + altoLinea) - 2 - this.scrollY;
				inicio = 0;
				final Color letraFinal = (colorLetra != null) ? colorLetra : Color.WHITE;

				while (inicio < len) {
					fin = textoConSaltos.indexOf('\n', inicio);
					if (fin == -1) {
						fin = len;
					}

					final String linea = textoConSaltos.substring(inicio, fin);
					Render2D.dibujarStringConSombra(gClip, linea, boxX + PADDING_INTERNO, yCursor, letraFinal,
							Color.BLACK);

					yCursor += altoLinea + ESPACIADO_LINEAS;
					inicio = fin + 1;
				}

			} finally {
				gClip.dispose();
			}

			// 3. Scrollbar
			if (tieneScroll && (maxScrollY > 0)) {
				this.dibujarScrollbar(g, boxX, boxY, anchoTotalBox, altoTotalBox, altoTotalContenido, maxScrollY);
			}

		} finally {
			g.setFont(fuenteOriginal);
		}
	}

	// =========================================================================
	// === 3. TOOLTIP CON CABECERA + DESCRIPCIÓN CONTINUA (Stats / Fisiología)
	// =========================================================================

	public void dibujarTooltipConCabecera(final Graphics2D g, final String titulo, final String descripcion,
			final Color colorTitulo, final Color colorDesc, final Color colorFondo) {
		if ((g == null) || (titulo == null)) {
			return;
		}

		final Font fuenteOriginal = g.getFont();
		try {
			final Font fuenteBold = Globales.GESTOR_FUENTES.getFuente(Font.BOLD, TAMANIO_NATIVO);
			final Font fuentePlain = Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, TAMANIO_NATIVO);

			g.setFont(fuenteBold);
			final int anchoTitulo = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, titulo);
			final int altoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, titulo);

			g.setFont(fuentePlain);
			final int anchoDesc = (descripcion != null)
					? Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, descripcion)
					: 0;

			final int anchoTotalContenido = anchoTitulo + anchoDesc;
			final int altoTotalContenido = altoTexto + (PADDING_INTERNO * 2);

			final boolean tieneScroll = altoTotalContenido > MAX_ALTO_TOOLTIP;
			final int maxScrollY = tieneScroll ? (altoTotalContenido - MAX_ALTO_TOOLTIP) : 0;

			final int id = titulo.hashCode() ^ ((descripcion != null) ? descripcion.hashCode() : 0);
			this.actualizarIdentificadorYScroll(id, maxScrollY);

			final int anchoTotalBox = anchoTotalContenido + (PADDING_INTERNO * 2)
					+ (tieneScroll ? (ANCHO_SCROLLBAR + 3) : 0);
			final int altoTotalBox = tieneScroll ? MAX_ALTO_TOOLTIP : altoTotalContenido;

			final Point raton = SuperficieDibujo.obtenerSuperficieDibujo().RATON.getPuntoPosicionEscalado();
			final int boxX = this.calcularCoordenadaX(raton.x, anchoTotalBox);
			final int boxY = this.calcularCoordenadaY(raton.y, altoTotalBox);

			final Color fondoFinal = (colorFondo != null) ? colorFondo : COLOR_FONDO_DEFECTO;
			Render2D.dibujarRectanguloRelleno(g, boxX, boxY, anchoTotalBox, altoTotalBox, fondoFinal);
			Render2D.dibujarRectanguloContorno(g, boxX, boxY, anchoTotalBox, altoTotalBox, COLOR_BORDE_DEFECTO);

			final Graphics2D gClip = (Graphics2D) g.create();
			try {
				gClip.setClip(boxX + 2, boxY + 2, anchoTotalBox - 4 - (tieneScroll ? (ANCHO_SCROLLBAR + 2) : 0),
						altoTotalBox - 4);

				final int yLinea = (boxY + PADDING_INTERNO + altoTexto) - 2 - this.scrollY;

				gClip.setFont(fuenteBold);
				final Color cTit = (colorTitulo != null) ? colorTitulo : COLOR_TITULO_DEFECTO;
				Render2D.dibujarStringConSombra(gClip, titulo, boxX + PADDING_INTERNO, yLinea, cTit, Color.BLACK);

				if ((descripcion != null) && !descripcion.isEmpty()) {
					gClip.setFont(fuentePlain);
					final Color cDesc = (colorDesc != null) ? colorDesc : Color.WHITE;
					Render2D.dibujarStringConSombra(gClip, descripcion, boxX + PADDING_INTERNO + anchoTitulo, yLinea,
							cDesc, Color.BLACK);
				}

			} finally {
				gClip.dispose();
			}

			if (tieneScroll && (maxScrollY > 0)) {
				this.dibujarScrollbar(g, boxX, boxY, anchoTotalBox, altoTotalBox, altoTotalContenido, maxScrollY);
			}

		} finally {
			g.setFont(fuenteOriginal);
		}
	}

	// =========================================================================
	// === 4. TOOLTIP ESTRUCTURADO DE ÍTEMS Y OBJETOS (Por Arreglo / JSON)
	// =========================================================================

	public void dibujarTooltipItem(final Graphics2D g, final Item item) {
		this.dibujarTooltipItemConPrecio(g, item, null, null);
	}

	public void dibujarTooltipItemConPrecio(final Graphics2D g, final Item item, final String textoPrecio,
			final Color colorPrecio) {
		if ((g == null) || (item == null)) {
			return;
		}

		final String nombre = item.getNombre();
		final ArrayList<String> infoLines = item.getInfo();

		this.dibujarTooltipEstructurado(g, nombre, infoLines, textoPrecio, colorPrecio);
	}

	public void dibujarTooltipEstructurado(final Graphics2D g, final String titulo, final ArrayList<String> lineas,
			final String textoPie, final Color colorPie) {
		if ((g == null) || (titulo == null)) {
			return;
		}

		final Font fuenteOriginal = g.getFont();

		try {
			final Font fuenteTitulo = Globales.GESTOR_FUENTES.getFuente(Font.BOLD, TAMANIO_NATIVO);
			final Font fuenteInfo = Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, TAMANIO_NATIVO);
			final Font fuentePie = Globales.GESTOR_FUENTES.getFuente(Font.BOLD, TAMANIO_NATIVO);

			g.setFont(fuenteTitulo);
			final int anchoTitulo = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, titulo);
			final int altoTitulo = Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, titulo);

			g.setFont(fuenteInfo);
			int maxAnchoInfo = 0;
			int altoTotalInfo = 0;

			if ((lineas != null) && !lineas.isEmpty()) {
				for (int i = 0; i < lineas.size(); i++) {
					final String linea = lineas.get(i);
					if ((linea != null) && !linea.isEmpty()) {
						final int anchoLinea = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, linea);
						final int altoLinea = Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, linea);

						maxAnchoInfo = Math.max(maxAnchoInfo, anchoLinea);
						altoTotalInfo += altoLinea + ESPACIADO_LINEAS;
					}
				}
			}

			g.setFont(fuentePie);
			final int anchoPie = (textoPie != null) ? Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, textoPie)
					: 0;
			final int altoPie = (textoPie != null) ? Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, textoPie)
					: 0;

			final int anchoContenido = Math.max(anchoTitulo, Math.max(maxAnchoInfo, anchoPie));
			final int altoTotalContenido = altoTitulo + ESPACIADO_LINEAS + altoTotalInfo
					+ (altoPie > 0 ? (altoPie + ESPACIADO_LINEAS + 4) : 0) + (PADDING_INTERNO * 2);

			final boolean tieneScroll = altoTotalContenido > MAX_ALTO_TOOLTIP;
			final int maxScrollY = tieneScroll ? (altoTotalContenido - MAX_ALTO_TOOLTIP) : 0;

			final int id = titulo.hashCode();
			this.actualizarIdentificadorYScroll(id, maxScrollY);

			final int anchoTotalBox = anchoContenido + (PADDING_INTERNO * 2)
					+ (tieneScroll ? (ANCHO_SCROLLBAR + 3) : 0);
			final int altoTotalBox = tieneScroll ? MAX_ALTO_TOOLTIP : altoTotalContenido;

			final Point raton = SuperficieDibujo.obtenerSuperficieDibujo().RATON.getPuntoPosicionEscalado();
			final int boxX = this.calcularCoordenadaX(raton.x, anchoTotalBox);
			final int boxY = this.calcularCoordenadaY(raton.y, altoTotalBox);

			Render2D.dibujarRectanguloRelleno(g, boxX, boxY, anchoTotalBox, altoTotalBox, COLOR_FONDO_DEFECTO);
			Render2D.dibujarRectanguloContorno(g, boxX, boxY, anchoTotalBox, altoTotalBox, COLOR_BORDE_DEFECTO);

			final Graphics2D gClip = (Graphics2D) g.create();
			try {
				gClip.setClip(boxX + 2, boxY + 2, anchoTotalBox - 4 - (tieneScroll ? (ANCHO_SCROLLBAR + 2) : 0),
						altoTotalBox - 4);

				int yCursor = (boxY + PADDING_INTERNO + altoTitulo) - 2 - this.scrollY;

				gClip.setFont(fuenteTitulo);
				Render2D.dibujarStringConSombra(gClip, titulo, boxX + PADDING_INTERNO, yCursor, COLOR_TITULO_DEFECTO,
						Color.BLACK);

				if ((lineas != null) && !lineas.isEmpty()) {
					gClip.setFont(fuenteInfo);
					for (int i = 0; i < lineas.size(); i++) {
						final String linea = lineas.get(i);
						if ((linea != null) && !linea.isEmpty()) {
							final int altoLinea = Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(gClip, linea);
							yCursor += altoLinea + ESPACIADO_LINEAS;
							Render2D.dibujarStringConSombra(gClip, linea, boxX + PADDING_INTERNO, yCursor - 2,
									COLOR_INFO_DEFECTO, Color.BLACK);
						}
					}
				}

				if ((textoPie != null) && !textoPie.isEmpty()) {
					gClip.setFont(fuentePie);
					yCursor += altoPie + ESPACIADO_LINEAS + 4;
					final Color cPie = (colorPie != null) ? colorPie : new Color(255, 215, 50);
					Render2D.dibujarStringConSombra(gClip, textoPie, boxX + PADDING_INTERNO, yCursor - 2, cPie,
							Color.BLACK);
				}

			} finally {
				gClip.dispose();
			}

			if (tieneScroll && (maxScrollY > 0)) {
				this.dibujarScrollbar(g, boxX, boxY, anchoTotalBox, altoTotalBox, altoTotalContenido, maxScrollY);
			}

		} finally {
			g.setFont(fuenteOriginal);
		}
	}

	// =========================================================================
	// === MÉTODOS AUXILIARES (ZERO-GC / O(1))
	// =========================================================================

	private void actualizarIdentificadorYScroll(final int idContenido, final int maxScrollY) {
		if (idContenido != this.ultimoIdContenido) {
			this.ultimoIdContenido = idContenido;
			this.scrollY = 0;
		}

		if (maxScrollY > 0) {
			final int rueda = SuperficieDibujo.obtenerSuperficieDibujo().RATON.getRotacionRueda();
			if (rueda != 0) {
				this.scrollY = Math.max(0, Math.min(maxScrollY, this.scrollY + (rueda * PASO_SCROLL)));
			}
		} else {
			this.scrollY = 0;
		}
	}

	private void dibujarScrollbar(final Graphics2D g, final int boxX, final int boxY, final int anchoTotalBox,
			final int altoTotalBox, final int altoTotalContenido, final int maxScrollY) {
		final int trackX = (boxX + anchoTotalBox) - ANCHO_SCROLLBAR - 3;
		final int trackY = boxY + 4;
		final int trackH = altoTotalBox - 8;
		Render2D.dibujarRectanguloRelleno(g, trackX, trackY, ANCHO_SCROLLBAR, trackH, COLOR_TRACK_SCROLL);

		final double ratio = (double) this.scrollY / maxScrollY;
		final int thumbH = Math.max(10, (int) (((double) altoTotalBox / altoTotalContenido) * trackH));
		final int thumbY = trackY + (int) (ratio * (trackH - thumbH));

		Render2D.dibujarRectanguloRelleno(g, trackX, thumbY, ANCHO_SCROLLBAR, thumbH, COLOR_THUMB_SCROLL);
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
