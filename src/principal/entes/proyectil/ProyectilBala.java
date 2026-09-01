package principal.entes.proyectil;

import java.awt.Color;
import java.awt.Graphics2D;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura.Direccion;
import principal.mapa.Mundo;
import principal.utilidades.Render2D;

public class ProyectilBala extends ProyectilGeneral {

	private static final long serialVersionUID = 1778501621520121117L;

	private static final Color COLOR_BALA = new Color(255, 215, 0);

	// Constructor Cardinal (N, S, E, O)
	public ProyectilBala(final double damage, final double velocidad, final boolean penetrante, final double alcance,
			final Mundo mundo, final double x, final double y, final int ancho, final int alto,
			final Direccion direccion, final Ente causante) {
		super(damage, velocidad, penetrante, alcance, mundo, x, y, ancho, alto, direccion, causante);
	}

	// Constructor Vectorial 360°
	public ProyectilBala(final double damage, final double velocidad, final boolean penetrante, final double alcance,
			final Mundo mundo, final double xOrigen, final double yOrigen, final double xDestino, final double yDestino,
			final int ancho, final int alto, final Ente causante) {
		super(damage, velocidad, penetrante, alcance, mundo, xOrigen, yOrigen, xDestino, yDestino, ancho, alto,
				causante);
	}

	@Override
	public void pintar(final Graphics2D g) {
		Render2D.dibujarRectanguloRellenoRefCamara(g, this.getPosicionXInt(), this.getPosicionYInt(), this.ancho,
				this.alto, COLOR_BALA);
		super.pintar(g);
	}
}