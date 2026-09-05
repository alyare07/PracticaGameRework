package principal.mapa.escenario.tps;

import java.io.File;

import principal.entes.criaturas.Criatura;
import principal.mapa.mapas.MapaManager;
import principal.maquinaestado.estados.GestorPartida;

public class PuertaMapa extends PuertaTP {
	private final File ARCHIVO_MAPA;
	private final GestorPartida GP;
	final String NOMBRE_MUNDO;
	final String NOMBRE_SPAWN;

	public PuertaMapa(final String rutaMapa, final String nombreMundo, final String nombreSpawn, final boolean temp,
			final GestorPartida gp) {
		this.ARCHIVO_MAPA = new File(rutaMapa);
		this.NOMBRE_MUNDO = nombreMundo;
		this.NOMBRE_SPAWN = nombreSpawn;
		this.GP = gp;
	}

	@Override
	public void teletransportar(final Criatura c) {
		MapaManager.guardarMapaEnTemp(this.GP.getGestorJuego().getMapa());
		this.GP.cambiarMundo(this.ARCHIVO_MAPA.getPath(), this.NOMBRE_MUNDO, this.NOMBRE_SPAWN);
	}

	public String getNombreMundoDestino() {
		return this.NOMBRE_MUNDO;
	}

	public String getRutaMapaDestino() {
		return this.ARCHIVO_MAPA.getPath();
	}

	public String getNombreSpawnDelMundoDestino() {
		return this.NOMBRE_SPAWN;
	}

}
