package principal.igu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import principal.entes.criaturas.Criatura;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

/**
 * Barra de vida cinemática para Jefes y Grandes Encuentros (HP >= 1000).
 * Renderizada de forma fija en la parte central superior del HUD (Fase 3 - 1:1)
 * con tipografía nativa pixel-art 'm5x7', barra fantasma (Lag) y marco ornamental.
 * 
 * @version 1.0 (Vanilla Java 8 - Zero-GC)
 */
public class BarraJefe {

	// =========================================================================
	// === 1. DIMENSIONES Y PALETA DE COLOR (ESTÉTICA BOSS FIGHT)
	// =========================================================================

	private static final int ANCHO_BARRA = 200;
	private static final int ALTO_BARRA = 8;
	private static final int POS_Y_BARRA = 14;

	private static final Color COLOR_FONDO = new Color(15, 18, 24, 240);
	private static final Color COLOR_BORDE_ORO = new Color(220, 180, 50);
	private static final Color COLOR_BORDE_SOMBRA = Color.BLACK;
	private static final Color COLOR_BARRA_VIDA = new Color(225, 30, 30);
	private static final Color COLOR_BARRA_LAG = new Color(255, 205, 40);
	private static final Color COLOR_TEXTO_NOMBRE = new Color(255, 235, 180);

	private final Rectangle areaBarra;
	private Criatura jefeAsignado;
	private boolean activa;

	private int anchoActual;
	private int anchoLag;

	public BarraJefe() {
		final int posX = Constantes.CENTROX - (ANCHO_BARRA / 2);
		this.areaBarra = new Rectangle(posX, POS_Y_BARRA, ANCHO_BARRA, ALTO_BARRA);
		this.activa = false;
	}

	// =========================================================================
	// === 2. ACTUALIZACIÓN LÓGICA (60 APS)
	// =========================================================================

	public void actualizar() {
		if (!this.activa || (this.jefeAsignado == null)) {
			return;
		}

		// Si el jefe murió o fue eliminado del mapa, desvinculamos la barra
		if (this.jefeAsignado.estaEliminado() || (this.jefeAsignado.getVida() <= 0.0)) {
			this.desvincularJefe();
			return;
		}

		final double vidaMax = Math.max(1.0, this.jefeAsignado.getVidaMaxima());
		final double vidaAct = Math.max(0.0, this.jefeAsignado.getVida());
		final double vidaLag = Math.max(0.0, this.jefeAsignado.getVidaLag());

		// 1. Ancho de barra principal
		final double ratioActual = Math.min(1.0, vidaAct / vidaMax);
		this.anchoActual = (int) Math.round(ratioActual * (this.areaBarra.width - 2));

		// 2. Ancho de barra fantasma de daño amortiguado (Lag)
		final double ratioLag = Math.min(1.0, vidaLag / vidaMax);
		this.anchoLag = (int) Math.round(ratioLag * (this.areaBarra.width - 2));
	}

	// =========================================================================
	// === 3. RENDERIZADO 1:1 EN CAPA DE HUD (FASE 3)
	// =========================================================================

	public void pintar(final Graphics2D g) {
		if (!this.activa || (this.jefeAsignado == null)) {
			return;
		}

		final int bx = this.areaBarra.x;
		final int by = this.areaBarra.y;
		final int bw = this.areaBarra.width;
		final int bh = this.areaBarra.height;

		// 1. Dibujar Nombre del Jefe centrado arriba de la barra (Fuente nativa m5x7 a 16f)
		final String nombre = this.jefeAsignado.getNombre();
		if ((nombre != null) && !nombre.isEmpty()) {
			final Font fontPrevia = g.getFont();
			g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 16f));

			final int anchoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, nombre);
			final int xNombre = Constantes.CENTROX - (anchoTexto / 2);
			final int yNombre = by - 3;

			Render2D.dibujarStringConSombra(g, nombre, xNombre, yNombre, COLOR_TEXTO_NOMBRE, COLOR_BORDE_SOMBRA);
			g.setFont(fontPrevia);
		}

		// 2. Fondo oscuro
		Render2D.dibujarRectanguloRelleno(g, bx, by, bw, bh, COLOR_FONDO);

		// 3. Barra fantasma de daño reciente (Amarilla / Lag)
		if (this.anchoLag > this.anchoActual) {
			Render2D.dibujarRectanguloRelleno(g, bx + 1, by + 1, this.anchoLag, bh - 2, COLOR_BARRA_LAG);
		}

		// 4. Barra frontal de salud actual (Roja)
		if (this.anchoActual > 0) {
			Render2D.dibujarRectanguloRelleno(g, bx + 1, by + 1, this.anchoActual, bh - 2, COLOR_BARRA_VIDA);
		}

		// 5. Marco ornamental exterior dorado y bisel negro
		Render2D.dibujarRectanguloContorno(g, bx - 1, by - 1, bw + 2, bh + 2, COLOR_BORDE_SOMBRA);
		Render2D.dibujarRectanguloContorno(g, bx, by, bw, bh, COLOR_BORDE_ORO);
	}

	// =========================================================================
	// === 4. ASIGNACIÓN Y CONTROL
	// =========================================================================

	public void asignarJefe(final Criatura jefe) {
		if ((jefe != null) && !jefe.estaEliminado()) {
			this.jefeAsignado = jefe;
			this.activa = true;
		}
	}

	public void desvincularJefe() {
		this.jefeAsignado = null;
		this.activa = false;
		this.anchoActual = 0;
		this.anchoLag = 0;
	}

	public boolean isActiva() {
		return this.activa;
	}

	public Criatura getJefeAsignado() {
		return this.jefeAsignado;
	}
}