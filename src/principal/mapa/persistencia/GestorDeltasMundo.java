package principal.mapa.persistencia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import principal.construccion.EstructuraConstruible;
import principal.construccion.TipoEstructura;
import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Jugador;
import principal.entes.criaturas.mascotas.Mascota;
import principal.entes.objetos.Fogata;
import principal.entes.objetos.items.Item;
import principal.entes.objetos.recursos.RecursoCosechable;
import principal.inventario.Contenedor;
import principal.mapa.Mundo;
import principal.utilidades.Globales;

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
		delta.getCriaturasModificadas().clear();
		delta.getCriaturasDinamicas().clear();

		for (final Ente e : mundo.getEntes()) {
			if (e.estaEliminado()) {
				continue;
			}

			// 1. Recursos Cosechables (Árboles tocones, rocas con daño parcial)
			if (e instanceof RecursoCosechable) {
				final RecursoCosechable rc = (RecursoCosechable) e;
				if (rc.getDurabilidad() < rc.getDurabilidadMaxima()) {
					final String clave = IdentificadorEspacial.generarClave(rc.getPosicionXInt(), rc.getPosicionYInt());
					delta.getCriaturasModificadas().put(clave, rc.exportarEstadoRecursoJSON());
				}
			}

			// 2. Estructuras construibles y Fogatas
			if (e instanceof EstructuraConstruible) {
				final EstructuraConstruible est = (EstructuraConstruible) e;
				final JSONObject jsonEst = new JSONObject();
				jsonEst.put("x", Integer.valueOf(est.getPosicionXInt()));
				jsonEst.put("y", Integer.valueOf(est.getPosicionYInt()));
				jsonEst.put("tipo", est.getTipo().name());
				jsonEst.put("hp", Double.valueOf(est.getVida()));
				delta.getEstructurasConstruidas().add(jsonEst);
			} else if (e instanceof Fogata) {
				final Fogata f = (Fogata) e;
				final JSONObject jsonFog = f.exportarParaJSON();
				jsonFog.put("tipo", "Fogata");
				jsonFog.put("vida", Double.valueOf(f.getVida()));
				jsonFog.put("tiempoCombustible", Double.valueOf(f.getTiempoCombustibleRestante()));
				delta.getEstructurasConstruidas().add(jsonFog);
			}

			// 3. Contenedores y Cofres (Si es criatura-comerciante, su inventario se guarda
			// aquí)
			if (e instanceof Contenedor) {
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

			// 4. Ítems en el suelo
			if ((e instanceof Item) && !(e instanceof Contenedor)) {
				final Item item = (Item) e;
				if (item.getTipoItem() != Item.COD_ITEM_MONEDA) {
					delta.getItemsEnSuelo().add(item.getJsonItem());
				}
			}

			// 5. Criaturas (Comerciantes, Mascotas estacionadas, Enemigos, Jefes)
			if ((e instanceof Criatura) && !(e instanceof Jugador)) {
				final Criatura c = (Criatura) e;

				if (c instanceof Mascota) {
					final boolean enEscoltaActiva = (Globales.GESTOR_GRUPO != null)
							&& (Globales.GESTOR_GRUPO.estaEnEscoltaActiva(c) || c.getBlackboard().isSiguiendoLider());

					// Solo se guarda en el delta si quedó esperando en este mapa
					if (!enEscoltaActiva) {
						delta.getCriaturasDinamicas().add(c.getJsonCriatura());
					}
				} else {
					// Guarda genéricamente vida, posición, dirección, efectos y memoria IA
					final String clave = IdentificadorEspacial.generarClave(c.getPosicionXInicial(),
							c.getPosicionYInicial());
					final JSONObject jCriat = c.getJsonCriatura();
					delta.getCriaturasModificadas().put(clave, jCriat);
				}
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

		if (delta.haExpirado(diaActual)) {
			delta.limpiar();
			this.deltasPorMundo.remove(claveMundo);
			return;
		}

		// FASE 1: Purga de destruidos
		final ArrayList<Ente> aEliminar = new ArrayList<Ente>();
		for (final Ente e : mundo.getEntes()) {
			if ((e instanceof RecursoCosechable) || (e instanceof Fogata)) {
				if (delta.isEntidadDestruida(e.getPosicionXInt(), e.getPosicionYInt())) {
					aEliminar.add(e);
				}
			} else if ((e instanceof Criatura) && !(e instanceof Jugador)) {
				final Criatura c = (Criatura) e;
				if (delta.isEntidadDestruida(c.getPosicionXInicial(), c.getPosicionYInicial())
						|| delta.isEntidadDestruida(c.getPosicionXInt(), c.getPosicionYInt())) {
					aEliminar.add(c);
				} else if ((c instanceof Mascota) && (Globales.GESTOR_GRUPO != null)) {
					for (final Criatura escolta : Globales.GESTOR_GRUPO.getEscoltaActiva()) {
						if ((escolta != null) && escolta.getNombre().equalsIgnoreCase(c.getNombre())
								&& (escolta != c)) {
							aEliminar.add(c);
							break;
						}
					}
				}
			} else if (e instanceof Item) {
				aEliminar.add(e);
			}
		}

		for (int i = 0; i < aEliminar.size(); i++) {
			final Ente e = aEliminar.get(i);
			mundo.eliminarEntidadRegistro(e);
			e.desvincularDeZonas();
			if (e.getLuzAsignada() != null) {
				e.desvincularLuz();
			}
		}

		// FASE 2: Restaurar Estado de Criaturas y Recursos Sobrevivientes
		for (final Ente e : mundo.getEntes()) {
			if (e instanceof RecursoCosechable) {
				final RecursoCosechable rc = (RecursoCosechable) e;
				final String clave = IdentificadorEspacial.generarClave(rc.getPosicionXInt(), rc.getPosicionYInt());
				final JSONObject jRec = delta.getCriaturasModificadas().get(clave);
				if (jRec != null) {
					rc.importarEstadoRecursoJSON(jRec);
				}
			} else if ((e instanceof Criatura) && !(e instanceof Jugador) && !(e instanceof Mascota)) {
				final Criatura c = (Criatura) e;
				final String clave = IdentificadorEspacial.generarClave(c.getPosicionXInicial(),
						c.getPosicionYInicial());
				final JSONObject jCriat = delta.getCriaturasModificadas().get(clave);

				if (jCriat != null) {
					final JSONObject entiti = (jCriat.get("entiti") instanceof JSONObject)
							? (JSONObject) jCriat.get("entiti")
							: jCriat;
					c.importarDatosCriaturaBase(entiti);
				}
			}
		}

		// FASE 3: Restaurar Mascotas Estacionadas
		for (int i = 0; i < delta.getCriaturasDinamicas().size(); i++) {
			final JSONObject jObj = delta.getCriaturasDinamicas().get(i);
			final JSONObject entiti = (jObj.get("entiti") instanceof JSONObject) ? (JSONObject) jObj.get("entiti")
					: jObj;

			final String nombre = (entiti.get("nombre") != null) ? entiti.get("nombre").toString() : "";
			boolean yaExiste = false;
			for (final Ente ent : mundo.getEntes()) {
				if ((ent instanceof Criatura) && ((Criatura) ent).getNombre().equalsIgnoreCase(nombre)) {
					yaExiste = true;
					break;
				}
			}

			if (!yaExiste) {
				final Mascota mascota = Mascota.crearDesdeJSON(entiti);
				if (mascota != null) {
					mundo.meterEntidad(mascota);
					if (Globales.GESTOR_GRUPO != null) {
						Globales.GESTOR_GRUPO.registrarEnRoster(mascota);
					}
				}
			}
		}

		// FASE 4: Restaurar Estructuras Construidas
		for (int i = 0; i < delta.getEstructurasConstruidas().size(); i++) {
			final JSONObject jEst = delta.getEstructurasConstruidas().get(i);
			final String tipoStr = (jEst.get("tipo") != null) ? jEst.get("tipo").toString() : "";
			final int x = ((Number) jEst.get("x")).intValue();
			final int y = ((Number) jEst.get("y")).intValue();

			boolean yaExiste = false;
			for (final Ente ent : mundo.getEntes()) {
				if ((ent.getPosicionXInt() == x) && (ent.getPosicionYInt() == y)) {
					yaExiste = true;
					break;
				}
			}

			if (!yaExiste) {
				if (tipoStr.equals("Fogata")) {
					final Fogata f = Fogata.crearDesdeJson(jEst);
					if (f != null) {
						mundo.meterEntidad(f);
					}
				} else {
					try {
						final TipoEstructura tipo = TipoEstructura.valueOf(tipoStr);
						final EstructuraConstruible est = new EstructuraConstruible(x, y, tipo);
						mundo.meterEntidad(est);
					} catch (final Exception ignored) {
					}
				}
			}
		}

		// FASE 5: Restaurar Contenedores/Cofres
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

		// FASE 6: Restaurar Ítems en el suelo
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

	@SuppressWarnings("unchecked")
	public JSONObject exportarJSON() {
		final JSONObject json = new JSONObject();
		for (final Map.Entry<String, DeltaMundo> entry : this.deltasPorMundo.entrySet()) {
			json.put(entry.getKey(), entry.getValue().exportarJSON());
		}
		return json;
	}

	public void importarJSON(final JSONObject json) {
		this.limpiarTodosLosDeltas();
		if (json == null) {
			return;
		}
		for (final Object key : json.keySet()) {
			final String claveMundo = key.toString();
			final Object val = json.get(key);
			if (val instanceof JSONObject) {
				final DeltaMundo delta = this.obtenerOCrearDelta(claveMundo, 0);
				delta.importarJSON((JSONObject) val);
			}
		}
	}
}