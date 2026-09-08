package principal.maquinaestado.estados.editor.historial;

import principal.mapa.Mundo;
import principal.mapa.mapas.Spawn;

/**
 * Registra la colocación, eliminación o renombramiento de puntos de Spawn en el
 * historial de Deshacer/Rehacer del editor (Zero-GC).
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class AccionHistorialSpawn implements AccionHistorial {

	private final Mundo mundo;
	private final Spawn spawn;
	private final boolean fueAgregado; // true = creado, false = eliminado

	public AccionHistorialSpawn(final Mundo mundo, final Spawn spawn, final boolean fueAgregado) {
		this.mundo = mundo;
		this.spawn = spawn;
		this.fueAgregado = fueAgregado;
	}

	@Override
	public void deshacer() {
		if ((this.mundo == null) || (this.spawn == null)) {
			return;
		}
		if (this.fueAgregado) {
			this.mundo.eliminarSpawn(this.spawn.getNombre());
		} else {
			this.mundo.agregarSpawn(this.spawn);
		}
	}

	@Override
	public void rehacer() {
		if ((this.mundo == null) || (this.spawn == null)) {
			return;
		}
		if (this.fueAgregado) {
			this.mundo.agregarSpawn(this.spawn);
		} else {
			this.mundo.eliminarSpawn(this.spawn.getNombre());
		}
	}

	@Override
	public String getDescripcion() {
		final String nombre = (this.spawn != null) ? this.spawn.getNombre() : "Spawn";
		return (this.fueAgregado ? "Colocar Spawn [" : "Borrar Spawn [") + nombre + "]";
	}
}