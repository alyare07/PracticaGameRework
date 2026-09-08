package principal.maquinaestado.estados.editor.historial;

import principal.entes.Ente;
import principal.iluminacion.FuenteLuz;
import principal.iluminacion.ZonaAmbiente;
import principal.mapa.mapas.Spawn;

/**
 * Registra el traslado espacial de cualquier entidad, trigger, luz o spawn en
 * el historial de Undo/Redo sincronizando celdas ZoneBox (Zero-GC).
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class AccionHistorialMover implements AccionHistorial {

	private final Object elemento;
	private final int origenX;
	private final int origenY;
	private final int destinoX;
	private final int destinoY;

	public AccionHistorialMover(final Object elemento, final int origenX, final int origenY, final int destinoX,
			final int destinoY) {
		this.elemento = elemento;
		this.origenX = origenX;
		this.origenY = origenY;
		this.destinoX = destinoX;
		this.destinoY = destinoY;
	}

	@Override
	public void deshacer() {
		this.aplicarPosicion(this.origenX, this.origenY);
	}

	@Override
	public void rehacer() {
		this.aplicarPosicion(this.destinoX, this.destinoY);
	}

	private void aplicarPosicion(final int x, final int y) {
		if (this.elemento == null) {
			return;
		}

		if (this.elemento instanceof Ente) {
			final Ente ente = (Ente) this.elemento;
			ente.setPosicion(x, y);
			ente.verificarZoneBox();
		} else if (this.elemento instanceof Spawn) {
			final Spawn spawn = (Spawn) this.elemento;
			spawn.setPosicion(x, y);
		} else if (this.elemento instanceof FuenteLuz) {
			final FuenteLuz luz = (FuenteLuz) this.elemento;
			luz.setPosicion(x, y);
		} else if (this.elemento instanceof ZonaAmbiente) {
			final ZonaAmbiente zona = (ZonaAmbiente) this.elemento;
			zona.getLimites().setLocation(x, y);
		}
	}

	@Override
	public String getDescripcion() {
		final String nombre = (this.elemento != null) ? this.elemento.getClass().getSimpleName() : "Elemento";
		return "Mover " + nombre + " a (" + this.destinoX + ", " + this.destinoY + ")";
	}
}