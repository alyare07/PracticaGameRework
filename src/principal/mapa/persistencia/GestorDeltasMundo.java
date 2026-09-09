package principal.mapa.persistencia;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import principal.construccion.EstructuraConstruible;
import principal.construccion.TipoEstructura;
import principal.entes.Ente;
import principal.entes.objetos.Fogata;
import principal.entes.objetos.items.Item;
import principal.entes.objetos.recursos.RecursoCosechable;
import principal.inventario.Contenedor;
import principal.mapa.Mundo;
import principal.utilidades.Globales;

/**
 * Gestor centralizado del subsistema de persistencia diferencial por mapa.
 * Captura y aplica deltas de estado (destrucción de recursos, construcciones,
 * cofres e ítems en el suelo) con soporte de expiración por calendario in-game.
 * 
 * @version 2.0 (Vanilla Java 8 - Fogata & Structure Persistence)
 */
public class GestorDeltasMundo {

	private final Map<String, DeltaMundo> deltasPorMundo = new HashMap<String, DeltaMundo>();

	public GestorDeltasMundo() {
	}

	public DeltaMundo obtenerOCrearDelta(final String claveMundo, final int diasParaRegenerar) {
		DeltaMundo delta = this.deltasPorMundo.get(claveMundo);
		if (delta == null) {
			delta = new DeltaMundo(claveMundo, diasParaRegenerar);
			this.deltasPorMundo.put(claveMundo, delta);
		}
		return delta;
	}

	public void registrarDestruccion(final Mundo mundo, final int x, final int y) {
		if (mundo == null) {
			return;
		}
		final DeltaMundo delta = this.obtenerOCrearDelta(mundo.getNombreMundo(), 0);
		delta.registrarDestruccion(x, y);
	}

	@SuppressWarnings("unchecked")
	public void capturarDelta(final Mundo mundo, final int diasParaRegenerar) {
		if (mundo == null) {
			return;
		}

		final String claveMundo = mundo.getNombreMundo();
		final DeltaMundo delta = this.obtenerOCrearDelta(claveMundo, diasParaRegenerar);

		final int diaActual = ((Globales.GESTOR_LUZ != null) && (Globales.GESTOR_LUZ.getCiclo() != null))
				? Globales.GESTOR_LUZ.getCiclo().getDiaActual()
				: 1;

		delta.setDiaGuardado(diaActual);
		delta.getEstructurasConstruidas().clear();
		delta.getCofresModificados().clear();
		delta.getItemsEnSuelo().clear();

		for (final Ente e : mundo.getEntes()) {
			if (e.estaEliminado()) {
				continue;
			}

			// 1. Muros y defensas de construcción
			if (e instanceof EstructuraConstruible) {
				final EstructuraConstruible est = (EstructuraConstruible) e;
				final JSONObject jsonEst = new JSONObject();
				jsonEst.put("x", Integer.valueOf(est.getPosicionXInt()));
				jsonEst.put("y", Integer.valueOf(est.getPosicionYInt()));
				jsonEst.put("tipo", est.getTipo().name());
				jsonEst.put("hp", Double.valueOf(est.getVida()));
				delta.getEstructurasConstruidas().add(jsonEst);
			}
			// 2. Fogatas desplegadas o modificadas por el jugador
			else if (e instanceof Fogata) {
				final Fogata f = (Fogata) e;
				final JSONObject jsonFog = f.exportarParaJSON();
				jsonFog.put("tipo", "Fogata");
				delta.getEstructurasConstruidas().add(jsonFog);
			}
			// 3. Inventarios de cualquier contenedor (Cofres, Alijos, etc.)
			else if (e instanceof Contenedor) {
				final Contenedor c = (Contenedor) e;
				final Ente propietario = c.getEntePropietario();
				if (propietario != null) {
					final String clave = IdentificadorEspacial.generarClave(propietario.getPosicionXInt(),
							propietario.getPosicionYInt());
					final JSONArray itemsJson = new JSONArray();
					for (final Item item : c.getInventario().getItems()) {
						itemsJson.add(item.getJsonItem());
					}
					delta.getCofresModificados().put(clave, itemsJson);
				}
			}
			// 4. Ítems actualmente tirados en el suelo
			else if (e instanceof Item) {
				final Item item = (Item) e;
				delta.getItemsEnSuelo().add(item.getJsonItem());
			}
		}
	}

	public void aplicarDelta(final Mundo mundo) {
		if (mundo == null) {
			return;
		}

		final String claveMundo = mundo.getNombreMundo();
		final DeltaMundo delta = this.deltasPorMundo.get(claveMundo);
		if (delta == null) {
			return;
		}

		final int diaActual = ((Globales.GESTOR_LUZ != null) && (Globales.GESTOR_LUZ.getCiclo() != null))
				? Globales.GESTOR_LUZ.getCiclo().getDiaActual()
				: 1;

		// Si el delta expiró (ej: mazmorra regenerable), se limpia y carga fresca
		if (delta.haExpirado(diaActual)) {
			delta.limpiar();
			this.deltasPorMundo.remove(claveMundo);
			return;
		}

		// 1. Purga recursos cosechables y fogatas destruidas del mapa base
		final Iterator<Ente> it = mundo.getEntes().iterator();
		while (it.hasNext()) {
			final Ente e = it.next();
			if ((e instanceof RecursoCosechable) || (e instanceof Fogata)) {
				if (delta.isEntidadDestruida(e.getPosicionXInt(), e.getPosicionYInt())) {
					e.eliminar();
					it.remove();
				}
			} else if (e instanceof Item) {
				// Elimina los ítems plantilla para restaurar el estado exacto del delta
				e.eliminar();
				it.remove();
			}
		}

		// 2. Re-instancia las estructuras y fogatas construidas por el jugador
		for (int i = 0; i < delta.getEstructurasConstruidas().size(); i++) {
			final JSONObject jEst = delta.getEstructurasConstruidas().get(i);
			final String tipoStr = (jEst.get("tipo") != null) ? jEst.get("tipo").toString() : "";

			if (tipoStr.equals("Fogata")) {
				final Fogata f = Fogata.crearDesdeJson(jEst);
				if (f != null) {
					mundo.meterEntidad(f);
				}
			} else {
				try {
					final TipoEstructura tipo = TipoEstructura.valueOf(tipoStr);
					final int x = ((Number) jEst.get("x")).intValue();
					final int y = ((Number) jEst.get("y")).intValue();
					final EstructuraConstruible est = new EstructuraConstruible(x, y, tipo);
					mundo.meterEntidad(est);
				} catch (final Exception ignored) {
				}
			}
		}

		// 3. Restaura contenidos modificados de contenedores
		for (final Ente e : mundo.getEntes()) {
			if (e instanceof Contenedor) {
				final Contenedor c = (Contenedor) e;
				final Ente propietario = c.getEntePropietario();
				if (propietario != null) {
					final String clave = IdentificadorEspacial.generarClave(propietario.getPosicionXInt(),
							propietario.getPosicionYInt());
					final JSONArray items = delta.getCofresModificados().get(clave);

					if (items != null) {
						c.getInventario().vaciar();
						for (final Object objItem : items) {
							if (objItem instanceof JSONObject) {
								final Item item = Item.crearItemDesdeJson((JSONObject) objItem);
								if (item != null) {
									c.getInventario().agregarItem(item);
								}
							}
						}
					}
				}
			}
		}

		// 4. Re-instancia los ítems tirados en el suelo capturados en el delta
		for (int i = 0; i < delta.getItemsEnSuelo().size(); i++) {
			final JSONObject jItem = delta.getItemsEnSuelo().get(i);
			final Item item = Item.crearItemDesdeJson(jItem);
			if (item != null) {
				mundo.meterEntidad(item);
			}
		}
	}

	public void limpiarTodosLosDeltas() {
		this.deltasPorMundo.clear();
	}
}