package principal.entes.proyectil;

import java.awt.Color;
import java.awt.Graphics2D;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura.Direccion;
import principal.mapa.Mundo;
import principal.utilidades.Render2D;

public class ProyectilPerdigon extends ProyectilBala {

	private static final long serialVersionUID = 771829304192830192L;

	private static final Color COLOR_PERDIGON = new Color(255, 160, 40);

	// Constructor Vectorial 360°
	public ProyectilPerdigon(final double damage, final double velocidad, final boolean penetrante,
			final double alcance, final Mundo mundo, final double xOrigen, final double yOrigen, final double xDestino,
			final double yDestino, final Ente causante) {
		super(damage, velocidad, penetrante, alcance, mundo, xOrigen, yOrigen, xDestino, yDestino, 2, 2, causante);
	}

	// Constructor Cardinal
	public ProyectilPerdigon(final double damage, final double velocidad, final boolean penetrante,
			final double alcance, final Mundo mundo, final double x, final double y, final Direccion direccion,
			final Ente causante) {
		super(damage, velocidad, penetrante, alcance, mundo, x, y, 2, 2, direccion, causante);
	}

	@Override
	public void pintar(final Graphics2D g) {
		Render2D.dibujarRectanguloRellenoRefCamara(g, this.getPosicionXInt(), this.getPosicionYInt(), this.ancho,
				this.alto, COLOR_PERDIGON);
		super.pintar(g);
	}
}