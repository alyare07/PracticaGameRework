package principal.igu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import principal.entes.efectos.EfectoEstado;
import principal.entes.efectos.TipoEfectoEstado;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

/**
 * Componente visual del HUD que renderiza la fila de iconos de efectos de
 * estado activos (Buffs / Debuffs) sobre la barra de vida del jugador con
 * temporizador, stacks y tooltips enriquecidos (Zero-GC / O(1)).
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class EfectosEstadoIGU {

	private static final int LADO_ICONO = 14;
	private static final int MARGEN = 3;
	private static final int POS_X_BASE = 6;
	private static final int POS_Y_BASE = 319; // Justo arriba de la barra de vida (Y=336)

	private static final Color COLOR_FONDO = new Color(15, 18, 24, 230);
	private static final Color COLOR_BORDE_BUFF = new Color(70, 240, 120);
	private static final Color COLOR_BORDE_DEBUFF = new Color(240, 70, 70);
	private static final Color COLOR_BARRA_TIEMPO = new Color(255, 215, 80);

	private final Rectangle[] areasIconos = new Rectangle[TipoEfectoEstado.values().length];

	public EfectosEstadoIGU() {
		for (int i = 0; i < this.areasIconos.length; i++) {
			this.areasIconos[i] = new Rectangle(0, 0, LADO_ICONO, LADO_ICONO);
		}
	}

	public void actualizar() {
		// La lógica se ejecuta en Criatura.actualizar()
	}

	public void pintar(final Graphics2D g) {
		if ((Globales.JUGADOR == null) || Globales.JUGADOR.estaEliminado()) {
			return;
		}

		final EfectoEstado[] efectos = Globales.JUGADOR.getEfectos();
		int slotIndex = 0;

		for (int i = 0; i < efectos.length; i++) {
			final EfectoEstado ef = efectos[i];
			if (!ef.isActivo()) {
				continue;
			}

			final int x = POS_X_BASE + (slotIndex * (LADO_ICONO + MARGEN));
			final int y = POS_Y_BASE;
			this.areasIconos[slotIndex].setBounds(x, y, LADO_ICONO, LADO_ICONO);

			// 1. Fondo de la casilla
			Render2D.dibujarRectanguloRelleno(g, x, y, LADO_ICONO, LADO_ICONO, COLOR_FONDO);

			// 2. Borde temático (Verde para Buff / Rojo para Debuff)
			final Color colorBorde = ef.getTipo().isBuff() ? COLOR_BORDE_BUFF : COLOR_BORDE_DEBUFF;
			Render2D.dibujarRectanguloContorno(g, x, y, LADO_ICONO, LADO_ICONO, colorBorde);

			// 3. Letra / Icono distintivo del efecto
			final Font fontPrevia = g.getFont();
			g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 8f));
			final String letra = ef.getTipo().getNombre().substring(0, 1);
			Render2D.dibujarStringConSombra(g, letra, x + 4, y + 9, ef.getTipo().getColorIdentificativo(), Color.BLACK);

			// 4. Stacks acumulados (ej: x2, x3)
			if (ef.getStacks() > 1) {
				g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 6f));
				Render2D.dibujarStringConSombra(g, String.valueOf(ef.getStacks()), (x + LADO_ICONO) - 5, y + 6,
						Color.YELLOW, Color.BLACK);
			}

			// 5. Micro-barra de tiempo restante (o símbolo infinito ∞)
			if (ef.isInfinito()) {
				g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 7f));
				Render2D.dibujarStringConSombra(g, "oo", x + 3, (y + LADO_ICONO) - 1, new Color(180, 230, 255),
						Color.BLACK);
			} else {
				final double ratio = ef.getProgresoNormalizado();
				final int anchoBarra = (int) Math.round(ratio * (LADO_ICONO - 2));
				if (anchoBarra > 0) {
					Render2D.dibujarRectanguloRelleno(g, x + 1, (y + LADO_ICONO) - 2, anchoBarra, 1,
							COLOR_BARRA_TIEMPO);
				}
			}

			g.setFont(fontPrevia);
			slotIndex++;
		}
	}

	public void pintarTooltips(final Graphics2D g) {
		if ((Globales.JUGADOR == null) || (Globales.RATON == null)) {
			return;
		}

		final Point pMouse = Globales.RATON.getPuntoPosicionEscalado();
		final EfectoEstado[] efectos = Globales.JUGADOR.getEfectos();
		int slotIndex = 0;

		for (int i = 0; i < efectos.length; i++) {
			final EfectoEstado ef = efectos[i];
			if (!ef.isActivo()) {
				continue;
			}

			if (this.areasIconos[slotIndex].contains(pMouse)) {
				final String titulo = ef.getTipo().getNombre() + (ef.getStacks() > 1 ? " x" + ef.getStacks() : "");
				final String dur = ef.isInfinito() ? " [Continuo]" : String.format(" [%.1fs]", ef.getTiempoRestante());
				final String desc = ef.getTipo().getDescripcion();

				Globales.FUNCIONES.GENERADOR_TOOLTIP.dibujarTooltipConCabecera(g, titulo + dur + ": ", desc,
						ef.getTipo().getColorIdentificativo(), Color.WHITE, COLOR_FONDO);
				break;
			}
			slotIndex++;
		}
	}
}