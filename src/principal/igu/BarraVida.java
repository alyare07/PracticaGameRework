package principal.igu;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import principal.entes.criaturas.Jugador;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

/**
 * Barra de vida principal del jugador con soporte de capas de color adaptativas
 * cada 50 HP (Zero-GC / O(1)).
 * 
 * @version 4.0 (Vanilla Java 8 - Adaptive Layer Cap)
 */
public class BarraVida extends Barra {

	private static final double HP_POR_CAPA = 50.0;

	private static final Color[] COLORES_CAPAS_VIDA = { new Color(235, 30, 30), // Capa 1 (0-50 HP): Rojo clásico
			// (Peligro crítico)
			new Color(255, 120, 0), // Capa 2 (51-100 HP): Naranja fuego (Advertencia)
			new Color(40, 235, 100), // Capa 3 (101-150 HP): Verde esmeralda (Salud base normal)
			new Color(16, 109, 54), // Capa 4 (151-200 HP): Cian mágico (Salud extra / Escudo)
			new Color(150, 20, 200), // Capa 5 (201-250 HP): Púrpura arcano (Nivel Épico)
			new Color(255, 200, 40) // Capa 6+ (>250 HP): Dorado solar (Nivel Máximo / Divino)
	};

	private final Jugador JUGADOR;

	private int capaActual;
	private double progresoActual;
	private double progresoLag;

	public BarraVida(final Rectangle area) {
		super(area, Color.BLACK, new Color(15, 18, 24), Color.RED, Color.WHITE);
		this.JUGADOR = Globales.JUGADOR;
	}

	@Override
	public void actualizar() {
		final double vida = Math.max(0.0, this.getCantidadActual());
		final double vidaLag = Math.max(0.0, this.getCantidadLag());
		final double vidaMax = Math.max(1.0, this.getLimite());

		// Capa más alta posible según la vida máxima
		final int capaMaxima = this.obtenerIndiceCapa(vidaMax);

		this.capaActual = Math.min(capaMaxima, this.obtenerIndiceCapa(vida));
		this.progresoActual = this.obtenerProgresoCapa(vida, this.capaActual, capaMaxima, vidaMax);

		final int capaLag = Math.min(capaMaxima, this.obtenerIndiceCapa(vidaLag));
		this.progresoLag = (capaLag > this.capaActual) ? 1.0
				: this.obtenerProgresoCapa(vidaLag, this.capaActual, capaMaxima, vidaMax);

		final int anchoUtil = this.AREA.width - 2;
		this.anchoActual = (int) Math.round(this.progresoActual * anchoUtil);
		this.anchoLag = (int) Math.round(this.progresoLag * anchoUtil);
	}

	@Override
	public void pintar(final Graphics2D g) {
		final int bx = this.AREA.x;
		final int by = this.AREA.y;
		final int bw = this.AREA.width;
		final int bh = this.AREA.height;

		// 1. Fondo oscuro
		Render2D.dibujarRectanguloRelleno(g, bx, by, bw, bh, this.COLOR_FONDO);

		// 2. Capa inferior de fondo si existe (Color de la capa anterior completa)
		if (this.capaActual > 0) {
			final Color colorFondoCapa = COLORES_CAPAS_VIDA[this.capaActual - 1];
			Render2D.dibujarRectanguloRelleno(g, bx + 1, by + 1, bw - 2, bh - 2, colorFondoCapa);
		}

		// 3. Barra fantasma de daño amarillo (Lag)
		if (this.anchoLag > this.anchoActual) {
			Render2D.dibujarRectanguloRelleno(g, bx + 1, by + 1, this.anchoLag, bh - 2, this.COLOR_LAG);
		}

		// 4. Barra frontal activa de la capa actual
		if (this.anchoActual > 0) {
			final Color colorCapaActual = COLORES_CAPAS_VIDA[this.capaActual];
			Render2D.dibujarRectanguloRelleno(g, bx + 1, by + 1, this.anchoActual, bh - 2, colorCapaActual);
		}

		// 5. Marco exterior negro
		Render2D.dibujarRectanguloContorno(g, this.AREA, this.COLOR_BORDES);

		// 6. Texto de valores numéricos enteros
		this.pintarInfo(g);
	}

	private int obtenerIndiceCapa(final double hp) {
		if (hp <= 0.0) {
			return 0;
		}
		int capa = (int) (hp / HP_POR_CAPA);
		if ((hp % HP_POR_CAPA) == 0.0) {
			capa--;
		}
		return Math.max(0, Math.min(COLORES_CAPAS_VIDA.length - 1, capa));
	}

	private double obtenerProgresoCapa(final double hp, final int capa, final int capaMaxima, final double hpMax) {
		if (hp <= 0.0) {
			return 0.0;
		}
		final double hpBaseCapa = capa * HP_POR_CAPA;
		final double hpEnEstaCapa = hp - hpBaseCapa;

		// Si es la capa superior máxima, la capacidad es el remanente de vidaMaxima
		final double capacidadEstaCapa = (capa == capaMaxima) ? Math.max(1.0, hpMax - hpBaseCapa) : HP_POR_CAPA;

		return Math.max(0.0, Math.min(1.0, hpEnEstaCapa / capacidadEstaCapa));
	}

	@Override
	protected double getLimite() {
		return (this.JUGADOR != null) ? this.JUGADOR.getVidaMaxima() : 50.0;
	}

	@Override
	protected double getCantidadActual() {
		return (this.JUGADOR != null) ? this.JUGADOR.getVida() : 50.0;
	}

	@Override
	protected double getCantidadLag() {
		return (this.JUGADOR != null) ? this.JUGADOR.getVidaLag() : 50.0;
	}
}