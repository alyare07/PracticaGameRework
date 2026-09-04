package principal.maquinaestado.estados.editor.historial;

import principal.entes.Ente;
import principal.maquinaestado.estados.editor.MundoEditor;

/**
 * Registra la colocación o borrado de una criatura u objeto en el mapa. Llama a
 * entidad.restaurar() para reactivar entidades revividas por Undo.
 * 
 * @version 2.0 (Vanilla Java 8 - Restoration Safe)
 */
public class AccionHistorialEntidad implements AccionHistorial {

	private final MundoEditor mundo;
	private final Ente entidad;
	private final boolean fueAgregada; // true = colocada, false = borrada

	public AccionHistorialEntidad(final MundoEditor mundo, final Ente entidad, final boolean fueAgregada) {
		this.mundo = mundo;
		this.entidad = entidad;
		this.fueAgregada = fueAgregada;
	}

	@Override
	public void deshacer() {
		if ((this.mundo == null) || (this.entidad == null)) {
			return;
		}
		if (this.fueAgregada) {
			this.mundo.eliminarEntidad(this.entidad);
		} else {
			this.entidad.restaurar(); // Reactiva el flag eliminado = false
			this.mundo.meterEntidad(this.entidad);
		}
	}

	@Override
	public void rehacer() {
		if ((this.mundo == null) || (this.entidad == null)) {
			return;
		}
		if (this.fueAgregada) {
			this.entidad.restaurar();
			this.mundo.meterEntidad(this.entidad);
		} else {
			this.mundo.eliminarEntidad(this.entidad);
		}
	}

	@Override
	public String getDescripcion() {
		final String nombre = (this.entidad != null) ? this.entidad.getClass().getSimpleName() : "Entidad";
		return (this.fueAgregada ? "Colocar " : "Borrar ") + nombre;
	}
}