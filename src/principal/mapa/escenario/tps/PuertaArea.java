package principal.mapa.escenario.tps;

import java.awt.Rectangle;

import principal.entes.criaturas.Criatura;

public class PuertaArea extends PuertaTP {
	private final Rectangle AREA_DESTINO;
	
	public PuertaArea(final Rectangle area) {
		this.AREA_DESTINO = area;
	}

	@Override
	public void teletransportar(Criatura c) {
		c.setPosicionX(AREA_DESTINO.x+AREA_DESTINO.width/2 - c.getArea().width/2);
		c.setPosicionY(AREA_DESTINO.y+AREA_DESTINO.height/2 - c.getArea().height/2);
	}

}
