package principal.mapa.escenario.tps;

import java.awt.Rectangle;

import principal.entes.criaturas.Criatura;

public class PuertaArea extends PuertaTP {

	private final Rectangle AREA_DESTINO;

	public PuertaArea(final Rectangle area) {
		this.AREA_DESTINO = (area != null) ? area : new Rectangle();
	}

	@Override
	public void teletransportar(final Criatura c) {
		if (c == null) {
			return;
		}
		final double nuevoX = (this.AREA_DESTINO.x + (this.AREA_DESTINO.width / 2.0)) - (c.getAncho() / 2.0);
		final double nuevoY = (this.AREA_DESTINO.y + (this.AREA_DESTINO.height / 2.0)) - (c.getAlto() / 2.0);
		c.setPosicion(nuevoX, nuevoY);
		c.verificarZoneBox();
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