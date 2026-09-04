package principal.maquinaestado.estados.editor.historial;

import principal.iluminacion.FuenteLuz;
import principal.iluminacion.ZonaAmbiente;
import principal.mapa.escenario.tps.ZonaTP;
import principal.maquinaestado.estados.editor.MundoEditor;

/**
 * Registra la colocación o eliminación de Triggers (ZonaTP), Zonas de Ambiente
 * y Fuentes de Luz Estáticas en el historial de Deshacer/Rehacer.
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class AccionHistorialTrigger implements AccionHistorial {

	private final MundoEditor mundo;
	private final Object elemento; // ZonaTP, ZonaAmbiente o FuenteLuz
	private final boolean fueAgregado;

	public AccionHistorialTrigger(final MundoEditor mundo, final Object elemento, final boolean fueAgregado) {
		this.mundo = mundo;
		this.elemento = elemento;
		this.fueAgregado = fueAgregado;
	}

	@Override
	public void deshacer() {
		if ((this.mundo == null) || (this.elemento == null)) {
			return;
		}

		if (this.fueAgregado) {
			this.removerElemento();
		} else {
			this.insertarElemento();
		}
	}

	@Override
	public void rehacer() {
		if ((this.mundo == null) || (this.elemento == null)) {
			return;
		}

		if (this.fueAgregado) {
			this.insertarElemento();
		} else {
			this.removerElemento();
		}
	}

	private void insertarElemento() {
		if (this.elemento instanceof ZonaTP) {
			final ZonaTP tp = (ZonaTP) this.elemento;
			tp.restaurar();
			this.mundo.agregarTrigger(tp);
		} else if (this.elemento instanceof ZonaAmbiente) {
			this.mundo.agregarZonaAmbiente((ZonaAmbiente) this.elemento);
		} else if (this.elemento instanceof FuenteLuz) {
			this.mundo.agregarLuzEstatica((FuenteLuz) this.elemento);
		}
	}

	private void removerElemento() {
		if (this.elemento instanceof ZonaTP) {
			this.mundo.eliminarTrigger((ZonaTP) this.elemento);
		} else if (this.elemento instanceof ZonaAmbiente) {
			this.mundo.eliminarZonaAmbiente((ZonaAmbiente) this.elemento);
		} else if (this.elemento instanceof FuenteLuz) {
			this.mundo.eliminarLuzEstatica((FuenteLuz) this.elemento);
		}
	}

	@Override
	public String getDescripcion() {
		final String tipo = (this.elemento != null) ? this.elemento.getClass().getSimpleName() : "Trigger";
		return (this.fueAgregado ? "Colocar " : "Borrar ") + tipo;
	}
}