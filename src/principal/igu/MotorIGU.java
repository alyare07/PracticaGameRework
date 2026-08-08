package principal.igu;

import java.awt.Graphics2D;
import java.awt.Rectangle;

public class MotorIGU {
	private final BarraVida BARRA_VIDA;
	private final BarraEstamina BARRA_ESTAMINA;
	
	public MotorIGU() {
		this.BARRA_VIDA = new BarraVida(new Rectangle(4,330,50,10));
		this.BARRA_ESTAMINA =  new BarraEstamina(new Rectangle(4,this.BARRA_VIDA.getPosicionYInt()+this.BARRA_VIDA.getAlto() +2,50,10));
	}
	
	public void actualizar() {
		this.BARRA_VIDA.actualizar();
		this.BARRA_ESTAMINA.actualizar();
	}
	
	public void pintar(final Graphics2D g) {
		this.BARRA_VIDA.pintar(g);
		this.BARRA_ESTAMINA.pintar(g);
	}


}
