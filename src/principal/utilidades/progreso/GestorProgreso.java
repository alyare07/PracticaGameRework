package principal.utilidades.progreso;

import java.util.EnumSet;

import org.json.simple.JSONArray;

import principal.utilidades.progreso.FlagProgreso;

/**
 * Gestor maestro de progreso e historia con respaldo en EnumSet (Bitmask O(1) /
 * Zero-GC).
 * 
 * @version 2.0 (Vanilla Java 8 - EnumSet Backed)
 */
public class GestorProgreso {

	// Internamente opera como un entero de 64 bits a nivel de registros de CPU
	private final EnumSet<FlagProgreso> flagsActivos = EnumSet.noneOf(FlagProgreso.class);

	public void activar(final FlagProgreso flag) {
		if (flag != null) {
			this.flagsActivos.add(flag);
		}
	}

	public void desactivar(final FlagProgreso flag) {
		if (flag != null) {
			this.flagsActivos.remove(flag);
		}
	}

	public void conmutar(final FlagProgreso flag) {
		if (flag != null) {
			if (this.flagsActivos.contains(flag)) {
				this.flagsActivos.remove(flag);
			} else {
				this.flagsActivos.add(flag);
			}
		}
	}

	public boolean isActivo(final FlagProgreso flag) {
		return (flag != null) && this.flagsActivos.contains(flag);
	}

	public void limpiar() {
		this.flagsActivos.clear();
	}

	public int getCantidadFlagsActivos() {
		return this.flagsActivos.size();
	}

	// =========================================================================
	// PERSISTENCIA JSON (GUARDAR / CARGAR PARTIDA)
	// =========================================================================

	@SuppressWarnings("unchecked")
	public JSONArray exportarJSON() {
		final JSONArray lista = new JSONArray();
		for (final FlagProgreso f : this.flagsActivos) {
			lista.add(f.name());
		}
		return lista;
	}

	public void importarJSON(final JSONArray lista) {
		this.limpiar();
		if (lista == null) {
			return;
		}
		for (final Object obj : lista) {
			if (obj != null) {
				try {
					final FlagProgreso f = FlagProgreso.valueOf(obj.toString().trim());
					this.activar(f);
				} catch (final IllegalArgumentException ignored) {
					// Ignora flags obsoletos de versiones anteriores
				}
			}
		}
	}
}