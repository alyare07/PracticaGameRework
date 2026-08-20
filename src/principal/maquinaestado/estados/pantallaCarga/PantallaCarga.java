package principal.maquinaestado.estados.pantallaCarga;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import principal.maquinaestado.estados.EstadoJuego;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.Globales;

/**
 * Estado gráfico de pantalla de carga que muestra el avance textual y
 * porcentaje del {@link GestorCarga}.
 */
public class PantallaCarga implements EstadoJuego {

	protected final GestorCarga GC;
	protected final BufferedImage FONDO;

	protected final Dimension DIMENSION = new Dimension(Constantes.ANCHO_JUEGO, Constantes.ALTO_JUEGO);
	protected final int MARGEN_MARCO = 50;

	protected final Rectangle MARCO_SUPERIOR = new Rectangle(0, 0, this.DIMENSION.width, this.MARGEN_MARCO);
	protected final Rectangle MARCO_INFERIOR = new Rectangle(0, this.DIMENSION.height - this.MARGEN_MARCO,
			this.DIMENSION.width, this.MARGEN_MARCO);
	protected final Rectangle MARCO_IZQUIERDA = new Rectangle(0, this.MARCO_SUPERIOR.y + this.MARCO_SUPERIOR.height,
			this.MARGEN_MARCO, this.DIMENSION.height - this.MARCO_INFERIOR.height - this.MARCO_SUPERIOR.height);
	protected final Rectangle MARCO_DERECHA = new Rectangle(this.DIMENSION.width - this.MARGEN_MARCO,
			this.MARCO_SUPERIOR.y + this.MARCO_SUPERIOR.height, this.MARGEN_MARCO,
			this.DIMENSION.height - this.MARCO_INFERIOR.height - this.MARCO_SUPERIOR.height);

	public PantallaCarga(final GestorCarga gc) {
		this(gc, new Color(20, 25, 30));
	}

	public PantallaCarga(final GestorCarga gc, final BufferedImage fondo) {
		this.GC = gc;
		this.FONDO = (fondo != null) ? Globales.FUNCIONES.TEXTURAS_TOOLS.redimensionar(fondo,
				Constantes.ANCHO_JUEGO - (2 * this.MARGEN_MARCO), Constantes.ALTO_JUEGO - (2 * this.MARGEN_MARCO))
				: Globales.FUNCIONES.TEXTURAS_TOOLS.crearTextura(new Color(20, 25, 30), this.DIMENSION.width,
						this.DIMENSION.height);
	}

	public PantallaCarga(final GestorCarga gc, final Color fondo) {
		this.GC = gc;
		this.FONDO = Globales.FUNCIONES.TEXTURAS_TOOLS.crearTextura((fondo != null) ? fondo : Color.BLACK,
				this.DIMENSION.width, this.DIMENSION.height);
	}

	@Override
	public void actualizar() {
		// La pantalla de carga se actualiza pasivamente según el estado del GestorCarga
	}

	@Override
	public void pintar(final Graphics2D g) {
		DibujoDebug.dibujarImagen(g, this.FONDO, 0, 0);
		this.pintarMarcos(g);
		this.pintarTextoCarga(g);
	}

	public void disposeMundo() {
		// Limpieza de recursos si fuera necesario
	}

	private void pintarTextoCarga(final Graphics2D g) {
		if (this.GC == null) {
			return;
		}

		final int porcentajeCarga = this.GC.getPorcentaje();
		final String texto = this.GC.getDetalleCarga() + "... " + porcentajeCarga + "%";

		final int anchoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, texto);
		final int altoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, texto);

		final int x = (this.DIMENSION.width / 2) - (anchoTexto / 2);
		final int y = (int) (this.DIMENSION.height * 0.85) + altoTexto;

		// Sombra de texto
		DibujoDebug.dibujarString(g, texto, x + 1, y + 1, Color.BLACK);
		// Texto principal en verde claro
		DibujoDebug.dibujarString(g, texto, x, y, new Color(100, 240, 120));
	}

	private void pintarMarcos(final Graphics2D g) {
		DibujoDebug.dibujarRectanguloRelleno(g, this.MARCO_SUPERIOR, Color.BLACK);
		DibujoDebug.dibujarRectanguloRelleno(g, this.MARCO_INFERIOR, Color.BLACK);
		DibujoDebug.dibujarRectanguloRelleno(g, this.MARCO_IZQUIERDA, Color.BLACK);
		DibujoDebug.dibujarRectanguloRelleno(g, this.MARCO_DERECHA, Color.BLACK);
	}
}