package principal.mapa.escenario.tps;

import java.io.File;

import principal.entes.criaturas.Criatura;
import principal.maquinaestado.estados.GestorPartida;

public class PuertaMapa extends PuertaTP {
	private final File ARCHIVO_MAPA;
	private final GestorPartida GP;
	
	public PuertaMapa(final String rutaMapa, final GestorPartida gp) {
		this.ARCHIVO_MAPA = new File(rutaMapa);
		this.GP = gp;
	}

	@Override
	public void teletransportar(Criatura c) {
		this.GP.cambiarMundo(this.ARCHIVO_MAPA.getPath());
	}

}
