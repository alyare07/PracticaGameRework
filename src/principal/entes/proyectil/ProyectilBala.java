package principal.entes.proyectil;

import java.awt.Color;
import java.awt.Graphics2D;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Criatura.Direccion;
import principal.mapa.Mundo;
import principal.utilidades.Render2D;

/**
 * Representa un proyectil balístico de arma de fuego (bala). Soporta
 * trayectorias continuas en 360 grados y colisión en sub-píxeles.
 * 
 * @version 2.5 (Java 8 Compatible - Zero-GC Architecture)
 */
public class ProyectilBala extends ProyectilGeneral {

	private static final long serialVersionUID = 1778501621520121117L;

	private static final Color COLOR_BALA = new Color(255, 215, 0); // Amarillo dorado balístico

	// =========================================================================
	// === CONSTRUCTORES CARDINALES (COMPATIBILIDAD)
	// =========================================================================

	public ProyectilBala(final double damage, final double velocidad, final boolean penetrante, final double alcance,
			final Mundo mundo, final double x, final double y, final int ancho, final int alto,
			final Direccion direccion, final Criatura causante) {
		super(damage, velocidad, penetrante, alcance, mundo, x, y, ancho, alto, direccion, causante);
	}

	public ProyectilBala(final double damage, final double velocidad, final boolean penetrante, final double alcance,
			final Mundo mundo, final double x, final double y, final int ancho, final int alto,
			final Direccion direccion, final Criatura causante, final boolean soloContraJugador) {
		super(damage, velocidad, penetrante, alcance, mundo, x, y, ancho, alto, direccion, causante, soloContraJugador);
	}

	// =========================================================================
	// === CONSTRUCTORES VECTORIALES EN 360°
	// =========================================================================

	public ProyectilBala(final double damage, final double velocidad, final boolean penetrante, final double alcance,
			final Mundo mundo, final double xOrigen, final double yOrigen, final double xDestino, final double yDestino,
			final int ancho, final int alto, final Criatura causante) {
		super(damage, velocidad, penetrante, alcance, mundo, xOrigen, yOrigen, xDestino, yDestino, ancho, alto,
				causante);
	}

	public ProyectilBala(final double damage, final double velocidad, final boolean penetrante, final double alcance,
			final Mundo mundo, final double xOrigen, final double yOrigen, final double xDestino, final double yDestino,
			final int ancho, final int alto, final Criatura causante, final boolean soloContraJugador) {
		super(damage, velocidad, penetrante, alcance, mundo, xOrigen, yOrigen, xDestino, yDestino, ancho, alto,
				causante, soloContraJugador);
	}

	@Override
	public void pintar(final Graphics2D g) {
		Render2D.dibujarRectanguloRellenoRefCamara(g, this.getPosicionXInt(), this.getPosicionYInt(), this.ancho,
				this.alto, COLOR_BALA);
		super.pintar(g);
	}
}