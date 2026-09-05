package principal.mapa.escenario.tps;

import java.awt.Rectangle;

import principal.entes.criaturas.Criatura;

public class PuertaArea extends PuertaTP {
	private final Rectangle AREA_DESTINO;

	public PuertaArea(final Rectangle area) {
		this.AREA_DESTINO = area;
	}

	@Override
	public void teletransportar(final Criatura c) {
		c.setPosicionX((this.AREA_DESTINO.x + (this.AREA_DESTINO.width / 2)) - (c.getArea().width / 2));
		c.setPosicionY((this.AREA_DESTINO.y + (this.AREA_DESTINO.height / 2)) - (c.getArea().height / 2));
	}

	public int getXDestino() {
		return this.AREA_DESTINO.x;
	}

	public int getYDestino() {
		return this.AREA_DESTINO.y;
	}

	public int getWDestino() {
		return this.AREA_DESTINO.width;
	}

	public int getHDestino() {
		return this.AREA_DESTINO.height;
	}

}
