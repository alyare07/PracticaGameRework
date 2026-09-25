package principal.maquinaestado.estados.editor.modal;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import principal.controles.Raton;
import principal.maquinaestado.estados.menu.herramientas.BotonPixel;
import principal.maquinaestado.estados.menu.herramientas.ComponenteMenu;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Cheatsheet interactivo de atajos de teclado y herramientas del Studio Layout.
 * Diseñado con tipografía m5x7 y m3x6 nativas en doble columna (Zero-GC).
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class VentanaModalAyuda extends ComponenteMenu {

	private static final int ANCHO_MODAL = 480;
	private static final int ALTO_MODAL = 270;

	private static final Color COLOR_FONDO = new Color(16, 20, 28, 250);
	private static final Color COLOR_BORDE = new Color(220, 180, 50); // Oro
	private static final Color COLOR_BORDE_SOMBRA = new Color(8, 10, 14);
	private static final Color COLOR_SECCION = new Color(255, 200, 60);
	private static final Color COLOR_TECLA = new Color(130, 220, 255);
	private static final Color COLOR_DESC = new Color(210, 215, 225);

	private boolean abierta = false;
	private BotonPixel btnEntendido;

	public VentanaModalAyuda() {
		super(new Rectangle(Constantes.CENTROX - (ANCHO_MODAL / 2), Constantes.CENTROY - (ALTO_MODAL / 2), ANCHO_MODAL,
				ALTO_MODAL));
		this.inicializarComponentes();
	}

	private void inicializarComponentes() {
		final int x = this.area.x;
		final int y = this.area.y;

		this.btnEntendido = new BotonPixel("Entendido (Esc)", new Rectangle(x + (ANCHO_MODAL / 2) - 60, y + ALTO_MODAL - 26, 120, 18), () -> {
			this.cerrar();
		});
	}

	public void abrir() {
		this.abierta = true;
		this.visible = true;
		GestorSonido.reproducir(IDSonido.SELECT_MENU);
	}

	public void cerrar() {
		this.abierta = false;
		this.visible = false;
	}

	public void conmutar() {
		if (this.abierta) {
			this.cerrar();
		} else {
			this.abrir();
		}
	}

	@Override
	public void actualizar(final Raton raton) {
		if (!this.abierta || (raton == null)) {
			return;
		}
		this.btnEntendido.actualizar(raton);
	}

	@Override
	public void pintar(final Graphics2D g) {
		if (!this.abierta) {
			return;
		}

		final int x = this.area.x;
		final int y = this.area.y;
		final int w = this.area.width;
		final int h = this.area.height;

		// 1. Overlay oscuro de fondo y caja modal
		Render2D.dibujarRectanguloRelleno(g, 0, 0, Constantes.ANCHO_JUEGO, Constantes.ALTO_JUEGO, new Color(0, 0, 0, 190));
		Render2D.dibujarRectanguloRelleno(g, x, y, w, h, COLOR_FONDO);
		Render2D.dibujarRectanguloContorno(g, x - 1, y - 1, w + 2, h + 2, COLOR_BORDE_SOMBRA);
		Render2D.dibujarRectanguloContorno(g, x, y, w, h, COLOR_BORDE);

		// 2. Cabecera
		final Font fontPrevia = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 16f));
		final String titulo = "GUIA DE ATAJOS Y PRODUCTIVIDAD (STUDIO PRO)";
		final int anchoTit = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, titulo);
		Render2D.dibujarStringConSombra(g, titulo, x + ((w - anchoTit) / 2), y + 20, COLOR_SECCION, Color.BLACK);

		// 3. Contenido en dos columnas
		final int col1X = x + 20;
		final int col2X = x + 250;
		int cursorY = y + 42;

		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, 14f));

		// COLUMNA 1: EDICIÓN Y CAPAS
		this.dibujarSeccion(g, "EDICION Y HERRAMIENTAS", col1X, cursorY);
		cursorY += 16;
		cursorY = this.dibujarAtajo(g, "Q", "Pipeta / Cuentagotas inteligente", col1X, cursorY);
		cursorY = this.dibujarAtajo(g, "V", "Variacion manual de tile", col1X, cursorY);
		cursorY = this.dibujarAtajo(g, "T", "Alternar Snap a grilla 16x16", col1X, cursorY);
		cursorY = this.dibujarAtajo(g, "R", "Regla de medicion euclidiana", col1X, cursorY);
		cursorY = this.dibujarAtajo(g, "Shift+Drag", "Trasladar entidad/trigger", col1X, cursorY);
		cursorY = this.dibujarAtajo(g, "1..4 / C", "Tamano y forma de pincel", col1X, cursorY);
		cursorY = this.dibujarAtajo(g, "E", "Inspeccionar elemento / cofre", col1X, cursorY);
		cursorY = this.dibujarAtajo(g, "Click Der", "Borrar elemento / deseleccionar", col1X, cursorY);

		// COLUMNA 2: NAVEGACIÓN Y VISTAS
		cursorY = y + 42;
		this.dibujarSeccion(g, "VISUALIZACION Y NAVEGACION", col2X, cursorY);
		cursorY += 16;
		cursorY = this.dibujarAtajo(g, "G", "Rejilla (Off / Blanca / Magenta)", col2X, cursorY);
		cursorY = this.dibujarAtajo(g, "M", "Alternar Minimapa / Fast-Travel", col2X, cursorY);
		cursorY = this.dibujarAtajo(g, "F1", "Abrir / Cerrar esta guia", col2X, cursorY);
		cursorY = this.dibujarAtajo(g, "F7", "Heatmap navegacion IA accesible", col2X, cursorY);
		cursorY = this.dibujarAtajo(g, "F8..F10", "Capas (Terreno, Entes, Triggers)", col2X, cursorY);
		cursorY = this.dibujarAtajo(g, "L", "Modo previsualizacion de luces", col2X, cursorY);
		cursorY = this.dibujarAtajo(g, "Ctrl+Z/Y", "Deshacer y Rehacer historial", col2X, cursorY);
		cursorY = this.dibujarAtajo(g, "Ctrl+S", "Guardado rapido en disco (.mp)", col2X, cursorY);

		// 4. Botón inferior
		this.btnEntendido.pintar(g);

		g.setFont(fontPrevia);
	}

	private void dibujarSeccion(final Graphics2D g, final String titulo, final int x, final int y) {
		final Font fPrev = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 14f));
		Render2D.dibujarStringConSombra(g, titulo, x, y, COLOR_SECCION, Color.BLACK);
		Render2D.dibujarLinea(g, x, y + 2, x + 200, y + 2, new Color(75, 80, 95));
		g.setFont(fPrev);
	}

	private int dibujarAtajo(final Graphics2D g, final String tecla, final String desc, final int x, final int y) {
		Render2D.dibujarStringConSombra(g, "[" + tecla + "]", x, y, COLOR_TECLA, Color.BLACK);
		Render2D.dibujarStringConSombra(g, desc, x + 65, y, COLOR_DESC, Color.BLACK);
		return y + 14;
	}

	public boolean isAbierta() {
		return this.abierta;
	}
}