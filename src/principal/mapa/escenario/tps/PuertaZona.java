package principal.mapa.escenario.tps;

import principal.entes.criaturas.Criatura;

public class PuertaZona extends PuertaTP {
	private final ZonaTP ZONA_TP_DESTINO;

	public PuertaZona(final ZonaTP zonaTpDestino) {
		this.ZONA_TP_DESTINO = zonaTpDestino;
	}

	@Override
	public void teletransportar(final Criatura c) {
//		this.ZONA_TP_DESTINO.meterCriaturaTeletransportadoParaAca(c);
		c.setPosicionX(this.ZONA_TP_DESTINO.getCentroX(c));
		c.setPosicionY(this.ZONA_TP_DESTINO.getCentroY(c));
	}

}
