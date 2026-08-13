package principal.eventos;

import principal.entes.Ente;
import principal.mapa.Mundo;
import principal.maquinaestado.estados.GestorJuego;

public abstract class EventoEnte extends Evento {

	protected final Ente ENTE;

	public EventoEnte(final Ente e, final GestorJuego gj) {
		super(gj);
		this.ENTE = e;
	}

	@Override
	public void actualizar() {
		// Validar si la entidad asociada fue destruida ANTES de procesar la lógica del
		// evento
		if ((this.ENTE == null) || this.ENTE.estaEliminado()) {
			this.eliminar();
			return;
		}
		super.actualizar();
	}

	public Ente getEnte() {
		return this.ENTE;
	}

	@Override
	protected Mundo getMundo() {
		return (this.ENTE != null) ? this.ENTE.getMundo() : null;
	}
}