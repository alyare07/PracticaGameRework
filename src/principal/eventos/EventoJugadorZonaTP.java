package principal.eventos;

import principal.mapa.escenario.tps.ZonaTP;
import principal.maquinaestado.estados.GestorJuego;
import principal.utilidades.Constantes;

public class EventoJugadorZonaTP extends EventoEnte{
    private final ZonaTP ZONA_TP;

    public EventoJugadorZonaTP(final ZonaTP zonaTP, final GestorJuego gj) {
	super(Constantes.JUGADOR, gj);
	this.ZONA_TP = zonaTP;
    }

    public EventoJugadorZonaTP(final ZonaTP zonaTP, final GestorJuego gj, final boolean repetir) {
	super(Constantes.JUGADOR, gj);
	this.ZONA_TP = zonaTP;
	this.repetir = repetir;
    }

    @Override
    protected EjecucionEvento getEjecucionEvento() {
	return () -> {
	    this.ZONA_TP.teletransportar(Constantes.JUGADOR);
	};
    }

    @Override
    protected CondicionEvento getCondicionEvento() {

	return () -> (this.ZONA_TP.disponibleParaTP(Constantes.JUGADOR) && Constantes.JUGADOR.getAreaInterseccionMovimiento().intersects(this.ZONA_TP.getArea()));
    }

}
