package principal.maquinaestado.estados.editor.historial;

import java.util.ArrayList;

import principal.entes.Ente;
import principal.maquinaestado.estados.editor.MundoEditor;

/**
 * Acción de historial compuesta para operaciones en lote (Brocha de dispersión
 * y Borrado Masivo). Permite revertir N entidades en un único paso atómico de
 * Deshacer.
 * 
 * @version 2.0 (Vanilla Java 8 - Restoration Safe Batch Transaction)
 */
public class AccionHistorialEntidadLote implements AccionHistorial {

	private final MundoEditor mundo;
	private final ArrayList<Ente> entes;
	private final boolean esCreacion;

	public AccionHistorialEntidadLote(final MundoEditor mundo, final ArrayList<Ente> entes, final boolean esCreacion) {
		this.mundo = mundo;
		this.entes = (entes != null) ? new ArrayList<Ente>(entes) : new ArrayList<Ente>(0);
		this.esCreacion = esCreacion;
	}

	@Override
	public void deshacer() {
		if (this.mundo == null) {
			return;
		}

		for (int i = 0; i < this.entes.size(); i++) {
			final Ente e = this.entes.get(i);
			if (e == null) {
				continue;
			}

			if (this.esCreacion) {
				this.mundo.eliminarEntidad(e);
			} else {
				e.restaurar(); // Reactiva el flag eliminado = false
				this.mundo.meterEntidad(e);
			}
		}
	}

	@Override
	public void rehacer() {
		if (this.mundo == null) {
			return;
		}

		for (int i = 0; i < this.entes.size(); i++) {
			final Ente e = this.entes.get(i);
			if (e == null) {
				continue;
			}

			if (this.esCreacion) {
				e.restaurar(); // Reactiva el flag eliminado = false
				this.mundo.meterEntidad(e);
			} else {
				this.mundo.eliminarEntidad(e);
			}
		}
	}

	@Override
	public String getDescripcion() {
		final String accion = this.esCreacion ? "Dispersar " : "Borrar lote de ";
		final String tipo = (this.entes.isEmpty()) ? "Entidades" : this.entes.get(0).getClass().getSimpleName();
		return accion + this.entes.size() + " " + tipo;
	}
}