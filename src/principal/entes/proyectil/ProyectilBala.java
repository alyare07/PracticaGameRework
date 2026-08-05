package principal.entes.proyectil;

import java.awt.Color;
import java.awt.Graphics2D;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Criatura.Direccion;
import principal.mapa.Mundo;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;

public class ProyectilBala extends ProyectilGeneral{

	private static final long serialVersionUID = 1778501621520121117L;

	public ProyectilBala(int damage, double velocidad, boolean penetrante, double alcance, Mundo mundo, double x,
			double y, int ancho, int alto, Direccion direccion, final Criatura causante) {
		super(damage, velocidad, penetrante, alcance, mundo, x, y, ancho, alto, direccion,causante);
	}

	@Override
	public void pintar(Graphics2D g) {
		DibujoDebug.dibujarRectanguloRellenoRefCamara(g, this.getPosicionXInt(), this.getPosicionYInt(), ancho, alto, Color.blue);
		super.pintar(g);
	}
	
	

}
