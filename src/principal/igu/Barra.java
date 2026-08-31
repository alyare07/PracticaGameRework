package principal.igu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.text.DecimalFormat;

import principal.entes.Ente;
import principal.utilidades.Render2D;
import principal.utilidades.Globales;

/**
 * Clase base abstracta para todas las barras de estado de la interfaz gráfica
 * fija (HUD).
 * <p>
 * <b>Optimizaciones y Mejoras de IGU:</b>
 * <ul>
 * <li><b>Soporte Nativo de Barra Fantasma (Lag Bar):</b> Permite que barras
 * como la de salud dibujen un rastro amarillo amortiguado tras recibir daño
 * mediante {@link #getCantidadLag()}.</li>
 * <li><b>Zero-GC en Renderizado de Texto:</b> Utiliza una fuente fija
 * pre-asignada {@link #FUENTE_TEXTO_BARRA}, eliminando llamadas continuas a
 * {@code deriveFont()} en el Heap.</li>
 * <li><b>Zero-GC en Consultas de Área:</b> Reutiliza {@link #AREA_ENTE_RETORNO}
 * de {@link Ente}.</li>
 * </ul>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.5
 */
public abstract class Barra extends Ente {

	/**
	 * Fuente pre-instanciada estática para el texto numérico de la barra. Evita
	 * instanciar objetos 'Font' en cada fotograma del bucle de IGU.
	 */
	private static final Font FUENTE_TEXTO_BARRA = new Font(Font.SANS_SERIF, Font.BOLD, 6);

	/** Color amarillo dorado predeterminado para el rastro de daño (Lag). */
	private static final Color COLOR_DEFECTO_LAG = new Color(255, 205, 40);

	private final Rectangle AREA;
	private final Color COLOR_BORDES;
	private final Color COLOR_FONDO;
	private final Color COLOR_RELLENO;
	private final Color COLOR_LAG;
	private final Color COLOR_TEXTO;
	private final DecimalFormat DF;

	/** Ancho en píxeles de la barra principal (valor actual). */
	private int anchoActual;

	/** Ancho en píxeles de la barra fantasma amortiguada (valor lag). */
	private int anchoLag;

	public Barra(final Rectangle area, final Color colorBordes, final Color colorFondo, final Color colorRelleno,
			final Color colorTexto) {
		this(area, colorBordes, colorFondo, colorRelleno, COLOR_DEFECTO_LAG, colorTexto);
	}

	public Barra(final Rectangle area, final Color colorBordes, final Color colorFondo, final Color colorRelleno,
			final Color colorLag, final Color colorTexto) {
		this.AREA = area;
		this.COLOR_BORDES = colorBordes;
		this.COLOR_FONDO = colorFondo;
		this.COLOR_RELLENO = colorRelleno;
		this.COLOR_LAG = colorLag;
		this.COLOR_TEXTO = colorTexto;
		this.DF = new DecimalFormat("0.00");
	}

	// =========================================================================
	// === ACTUALIZACIÓN LÓGICA (60 APS)
	// =========================================================================

	/**
	 * Calcula los anchos en píxeles de la barra principal y de la barra fantasma.
	 */
	@Override
	public void actualizar() {
		final double limite = Math.max(0.001, this.getLimite());

		// 1. Ancho proporcional de la barra principal
		final double ratioActual = Math.max(0.0, Math.min(1.0, this.getCantidadActual() / limite));
		this.anchoActual = (int) Math.round(ratioActual * this.AREA.width);

		// 2. Ancho proporcional de la barra fantasma (Lag)
		final double ratioLag = Math.max(0.0, Math.min(1.0, this.getCantidadLag() / limite));
		this.anchoLag = (int) Math.round(ratioLag * this.AREA.width);
	}

	// =========================================================================
	// === RENDERIZADO EN EL HUD (CAPA FIJA 1:1)
	// =========================================================================

	/**
	 * Dibuja la barra en 4 pasadas ordenadas:
	 * <ol>
	 * <li>Fondo de la barra.</li>
	 * <li>Barra fantasma (amarilla) si existe daño pendiente.</li>
	 * <li>Barra principal (roja/azul/verde) con el valor actual.</li>
	 * <li>Borde exterior y texto numérico centrado.</li>
	 * </ol>
	 *
	 * @param g Contexto gráfico {@link Graphics2D}.
	 */
	@Override
	public void pintar(final Graphics2D g) {
		// 1. Fondo base de la barra
		Render2D.dibujarRectanguloRelleno(g, this.AREA.x, this.AREA.y, this.AREA.width, this.AREA.height,
				this.COLOR_FONDO);

		// 2. Barra fantasma de daño (Amarilla / Lag)
		if (this.anchoLag > this.anchoActual) {
			Render2D.dibujarRectanguloRelleno(g, this.AREA.x, this.AREA.y, this.anchoLag, this.AREA.height,
					this.COLOR_LAG);
		}

		// 3. Barra principal frontal de valor actual (Roja / Relleno)
		if (this.anchoActual > 0) {
			Render2D.dibujarRectanguloRelleno(g, this.AREA.x, this.AREA.y, this.anchoActual, this.AREA.height,
					this.COLOR_RELLENO);
		}

		// 4. Borde exterior delimitador
		Render2D.dibujarRectanguloContorno(g, this.AREA, this.COLOR_BORDES);

		// 5. Texto numérico centrado con métricas
		this.pintarInfo(g);
	}

	/**
	 * Renderiza el texto de valores numéricos (ej: "100.00 / 100.00") centrado en
	 * la barra.
	 */
	private void pintarInfo(final Graphics2D g) {
		final String info = this.DF.format(this.getCantidadActual()) + " / " + this.DF.format(this.getLimite());

		final Font fontPrevia = g.getFont();
		g.setFont(FUENTE_TEXTO_BARRA);

		final int anchoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, info);
		final int altoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, info);

		final int x = this.AREA.x + ((this.AREA.width - anchoTexto) / 2);
		final int y = (this.AREA.y + this.AREA.height) - ((this.AREA.height - altoTexto) / 2) - 2;

		Render2D.dibujarString(g, info, x, y, this.COLOR_TEXTO);

		g.setFont(fontPrevia);
	}

	// =========================================================================
	// === MÉTODOS DE CONTRATO Y GANCHOS POLIMÓRFICOS
	// =========================================================================

	protected abstract double getLimite();

	protected abstract double getCantidadActual();

	/**
	 * Retorna el valor de la barra fantasma atrasada (Lag). Por defecto devuelve
	 * {@link #getCantidadActual()} (sin efecto lag). Las subclases como
	 * {@link BarraVida} sobreescriben este método.
	 *
	 * @return Valor numérico del rastro amortiguado.
	 */
	protected double getCantidadLag() {
		return this.getCantidadActual();
	}

	// =========================================================================
	// === GESTIÓN DE ENTE Y ÁREA (ZERO-GC)
	// =========================================================================

	@Override
	public Rectangle getArea() {
		this.AREA_ENTE_RETORNO.setBounds(this.getPosicionXInt(), this.getPosicionYInt(), this.getAncho(),
				this.getAlto());
		return this.AREA_ENTE_RETORNO;
	}

	@Override
	public void eliminar() {
		this.eliminado = true;
	}

	@Override
	public int getPosicionXInt() {
		return this.AREA.x;
	}

	@Override
	public int getPosicionYInt() {
		return this.AREA.y;
	}

	@Override
	public double getPosicionX() {
		return this.AREA.x;
	}

	@Override
	public double getPosicionY() {
		return this.AREA.y;
	}

	@Override
	public int getAncho() {
		return this.AREA.width;
	}

	@Override
	public int getAlto() {
		return this.AREA.height;
	}

	@Override
	public void modificarPosicionX(final double desplazamientoX) {
	}

	@Override
	public void modificarPosicionY(final double desplazamientoY) {
	}

	@Override
	public boolean estaEliminado() {
		return this.eliminado;
	}

	@Override
	public void setPosicion(final double x, final double y) {
	}

	public void restaurar() {
		this.eliminado = false;
	}
}