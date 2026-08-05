package principal.eventos;

import principal.mapa.escenario.tps.ZonaTP;
import principal.maquinaestado.estados.GestorJuego;
import principal.utilidades.Constantes;

public class EventoJugadorZonaTP extends EventoEnte {
	private final ZonaTP ZONA_TP;
	
	public EventoJugadorZonaTP(final ZonaTP zonaTP, GestorJuego gj) {
		super(Constantes.JUGADOR, gj);
		this.ZONA_TP = zonaTP;
	}
	
	public EventoJugadorZonaTP(final ZonaTP zonaTP, GestorJuego gj, final boolean repetir) {
		super(Constantes.JUGADOR, gj);
		this.ZONA_TP = zonaTP;
		this.repetir = repetir;
	}

	@Override
	protected EjecucionEvento getEjecucionEvento() {
		return () -> {
			ZONA_TP.teletransportar(Constantes.JUGADOR);
		};
	}

	@Override
	protected CondicionEvento getCondicionEvento() {
		
		return () -> {
			return ZONA_TP.disponibleParaTP(Constantes.JUGADOR) && Constantes.JUGADOR.getRectanguloInterseccionGeneral().intersects(ZONA_TP.getArea());
		};
	}

}
