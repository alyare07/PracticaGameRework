package principal.mapa.escenario.tps;

import java.io.File;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Jugador;
import principal.mapa.mapas.MapaManager;
import principal.maquinaestado.estados.GestorPartida;
import principal.utilidades.Globales;

public class PuertaMapa extends PuertaTP {

	private final File ARCHIVO_MAPA;
	private final String NOMBRE_MUNDO;
	private final String NOMBRE_SPAWN;
	private final GestorPartida GP;

	public PuertaMapa(final String rutaMapa, final String nombreMundo, final String nombreSpawn, final boolean temp,
			final GestorPartida gp) {
		this.ARCHIVO_MAPA = new File(rutaMapa);
		this.NOMBRE_MUNDO = (nombreMundo != null) ? nombreMundo : "exterior";
		this.NOMBRE_SPAWN = (nombreSpawn != null) ? nombreSpawn : "Comienzo";
		this.GP = gp;
	}

	@Override
	public void teletransportar(final Criatura c) {
		if (!(c instanceof Jugador)) {
			return;
		}

		GestorPartida gestorPartida = this.GP;
		if ((gestorPartida == null) && Globales.isEstadoJuego()) {
			gestorPartida = MapaManager.getGestorPartida();
		}

		if (gestorPartida != null) {
			if ((gestorPartida.getGestorJuego() != null) && (gestorPartida.getGestorJuego().getMapa() != null)) {
				MapaManager.guardarMapaEnTemp(gestorPartida.getGestorJuego().getMapa());
			}
			gestorPartida.cambiarMundo(this.ARCHIVO_MAPA.getPath(), this.NOMBRE_MUNDO, this.NOMBRE_SPAWN);
		}
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