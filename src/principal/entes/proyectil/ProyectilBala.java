package principal.entes.proyectil;

import java.awt.Color;
import java.awt.Graphics2D;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Criatura.Direccion;
import principal.mapa.Mundo;
import principal.utilidades.Render2D;

public class ProyectilBala extends ProyectilGeneral{

    private static final long serialVersionUID = 1778501621520121117L;

    public ProyectilBala(final int damage, final double velocidad, final boolean penetrante, final double alcance, final Mundo mundo, final double x, final double y, final int ancho, final int alto,
	    final Direccion direccion, final Criatura causante) {
	super(damage, velocidad, penetrante, alcance, mundo, x, y, ancho, alto, direccion, causante);
    }

    public ProyectilBala(final int damage, final double velocidad, final boolean penetrante, final double alcance, final Mundo mundo, final double x, final double y, final int ancho, final int alto,
	    final Direccion direccion, final Criatura causante, final boolean soloContraJugador) {
	super(damage, velocidad, penetrante, alcance, mundo, x, y, ancho, alto, direccion, causante, soloContraJugador);

    }

    @Override
    public void pintar(final Graphics2D g) {
	Render2D.dibujarRectanguloRellenoRefCamara(g, this.getPosicionXInt(), this.getPosicionYInt(), this.ancho, this.alto, Color.blue);
	super.pintar(g);
    }

}
