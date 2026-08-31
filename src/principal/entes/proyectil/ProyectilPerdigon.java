package principal.entes.proyectil;

import java.awt.Color;
import java.awt.Graphics2D;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Criatura.Direccion;
import principal.mapa.Mundo;
import principal.utilidades.Render2D;

/**
 * Representa un perdigón individual disparado por un cartucho de escopeta.
 * Diseñado con tamaño compacto y cálculo vectorial en 360 grados (Zero-GC).
 * 
 * @version 1.0 (Java 8 Compatible - Zero-GC Architecture)
 */
public class ProyectilPerdigon extends ProyectilBala {

	private static final long serialVersionUID = 771829304192830192L;

	/** Color plomo incandescente característico de perdigones de posta. */
	private static final Color COLOR_PERDIGON = new Color(255, 160, 40);

	// =========================================================================
	// === CONSTRUCTORES EN 360°
	// =========================================================================

	public ProyectilPerdigon(final double damage, final double velocidad, final boolean penetrante,
			final double alcance, final Mundo mundo, final double xOrigen, final double yOrigen,
			final double xDestino, final double yDestino, final Criatura causante) {
		super(damage, velocidad, penetrante, alcance, mundo, xOrigen, yOrigen, xDestino, yDestino, 2, 2, causante);
	}

	public ProyectilPerdigon(final double damage, final double velocidad, final boolean penetrante,
			final double alcance, final Mundo mundo, final double xOrigen, final double yOrigen,
			final double xDestino, final double yDestino, final Criatura causante, final boolean soloContraJugador) {
		super(damage, velocidad, penetrante, alcance, mundo, xOrigen, yOrigen, xDestino, yDestino, 2, 2, causante,
				soloContraJugador);
	}

	// =========================================================================
	// === CONSTRUCTORES CARDINALES (COMPATIBILIDAD)
	// =========================================================================

	public ProyectilPerdigon(final double damage, final double velocidad, final boolean penetrante,
			final double alcance, final Mundo mundo, final double x, final double y, final Direccion direccion,
			final Criatura causante, final boolean soloContraJugador) {
		super(damage, velocidad, penetrante, alcance, mundo, x, y, 2, 2, direccion, causante, soloContraJugador);
	}

	@Override
	public void pintar(final Graphics2D g) {
		Render2D.dibujarRectanguloRellenoRefCamara(g, this.getPosicionXInt(), this.getPosicionYInt(), this.ancho,
				this.alto, COLOR_PERDIGON);
		super.pintar(g);
	}
}