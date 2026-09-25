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
import principal.entes.criaturas.jugador.Jugador;
import principal.entes.criaturas.mascotas.Mascota;
import principal.entes.objetos.items.Item;
import principal.entes.objetos.recursos.RecursoCosechable;
import principal.inventario.Contenedor;
import principal.mapa.Mundo;
import principal.persistencia.json.LectorJSON;
import principal.persistencia.json.RegistroEntidades;
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

		final int diaActual = (Globales.GESTOR_ASTRONOMICO != null) ? Globales.GESTOR_ASTRONOMICO.getDiaActual() : 1;

		delta.setDiaGuardado(diaActual);
		delta.getEstructurasConstruidas().clear();
		delta.getCofresModificados().clear();
		delta.getItemsEnSuelo().clear();
		delta.getCriaturasModificadas().clear();
		delta.getCriaturasDinamicas().clear();

		if (mundo.getEstadoClima() != null) {
			delta.setClimaModificado(mundo.getEstadoClima().exportarJSON());
		}

		for (final Ente e : mundo.getEntes()) {
			if ((e == null) || e.estaEliminado()) {
				continue;
			}

			// 1. Recursos Cosechables
			if (e instanceof RecursoCosechable) {
				final RecursoCosechable rc = (RecursoCosechable) e;
				if (rc.estaModificado()) {
					final String clave = IdentificadorEspacial.generarClave(rc.getPosicionXInt(), rc.getPosicionYInt());
					delta.getCriaturasModificadas().put(clave, rc.exportarEstadoRecursoJSON());
				}
				continue;
			}

			// 2. Estructuras Construibles (Muros de jugador)
			if (e instanceof EstructuraConstruible) {
				final EstructuraConstruible est = (EstructuraConstruible) e;
				final JSONObject jsonEst = new JSONObject();
				jsonEst.put("x", Integer.valueOf(est.getPosicionXInt()));
				jsonEst.put("y", Integer.valueOf(est.getPosicionYInt()));
				jsonEst.put("tipo", est.getTipo().name());
				jsonEst.put("hp", Double.valueOf(est.getVida()));
				delta.getEstructurasConstruidas().add(jsonEst);
				continue;
			}

			// 3. Contenedores y Cofres
			if (e instanceof Contenedor) {
				final Contenedor c = (Contenedor) e;
				final Ente propietario = c.getEntePropietario();
				if (propietario != null) {
					final String clave = IdentificadorEspacial.generarClave(propietario.getPosicionXInt(),
							propietario.getPosicionYInt());
					final JSONArray itemsJson = new JSONArray();
					for (final Item item : c.getInventario().getItems()) {
						final JSONObject sobre = RegistroEntidades.exportar(item);
						if (sobre != null) {
							itemsJson.add(sobre);
						}
					}
					delta.getCofresModificados().put(clave, itemsJson);
				}
				continue;
			}

			// 4. Ítems en el suelo
			if (e instanceof Item) {
				final Item item = (Item) e;
				if (item.getTipoItem() != Item.COD_ITEM_MONEDA) {
					final JSONObject sobre = RegistroEntidades.exportar(item);
					if (sobre != null) {
						delta.getItemsEnSuelo().add(sobre);
					}
				}
				continue;
			}

			// 5. Criaturas
			if ((e instanceof Criatura) && !(e instanceof Jugador)) {
				final Criatura c = (Criatura) e;

				if (c instanceof Mascota) {
					final boolean enEscoltaActiva = (Globales.GESTOR_GRUPO != null)
							&& (Globales.GESTOR_GRUPO.estaEnEscoltaActiva(c) || c.getBlackboard().isSiguiendoLider());

					if (!enEscoltaActiva) {
						final JSONObject sobre = RegistroEntidades.exportar(c);
						if (sobre != null) {
							delta.getCriaturasDinamicas().add(sobre);
						}
					}
				} else {
					final String clave = IdentificadorEspacial.generarClave(c.getPosicionXInicial(),
							c.getPosicionYInicial());
					final JSONObject sobre = RegistroEntidades.exportar(c);
					if (sobre != null) {
						delta.getCriaturasModificadas().put(clave, sobre);
					}
				}
				continue;
			}

			// 6. Objetos Fabricables y Entidades Dinámicas registradas
			if (RegistroEntidades.soporta(e)) {
				final JSONObject sobre = RegistroEntidades.exportar(e);
				if (sobre != null) {
					delta.getEstructurasConstruidas().add(sobre);
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

		final int diaActual = (Globales.GESTOR_ASTRONOMICO != null) ? Globales.GESTOR_ASTRONOMICO.getDiaActual() : 1;

		if (delta.haExpirado(diaActual)) {
			delta.limpiar();
			this.deltasPorMundo.remove(claveMundo);
			return;
		}

		if ((delta.getClimaModificado() != null) && (mundo.getEstadoClima() != null)) {
			mundo.getEstadoClima().importarJSON(delta.getClimaModificado());
		}

		// FASE 1: Purga de destruidos
		final ArrayList<Ente> aEliminar = new ArrayList<Ente>();
		for (final Ente e : mundo.getEntes()) {
			if (e instanceof Jugador) {
				continue;
			}

			if (e instanceof Criatura) {
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
			} else if (delta.isEntidadDestruida(e.getPosicionXInt(), e.getPosicionYInt())) {
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
					final JSONObject datos = LectorJSON.getObjeto(jCriat, RegistroEntidades.CLAVE_DATOS);
					c.importarDatosCriaturaBase(datos != null ? datos : jCriat);
				}
			}
		}

		// FASE 3: Restaurar Mascotas Estacionadas
		for (int i = 0; i < delta.getCriaturasDinamicas().size(); i++) {
			final JSONObject jSobre = delta.getCriaturasDinamicas().get(i);
			final Ente mascotaInst = RegistroEntidades.importar(jSobre, mundo);
			if (mascotaInst instanceof Mascota) {
				final Mascota mascota = (Mascota) mascotaInst;
				boolean yaExiste = false;
				for (final Ente ent : mundo.getEntes()) {
					if ((ent instanceof Criatura)
							&& ((Criatura) ent).getNombre().equalsIgnoreCase(mascota.getNombre())) {
						yaExiste = true;
						break;
					}
				}
				if (!yaExiste) {
					mundo.meterEntidad(mascota);
					if (Globales.GESTOR_GRUPO != null) {
						Globales.GESTOR_GRUPO.registrarEnRoster(mascota);
					}
				}
			}
		}

		// FASE 4: Restaurar Estructuras Construidas mediante RegistroEntidades
		for (int i = 0; i < delta.getEstructurasConstruidas().size(); i++) {
			final JSONObject jSobre = delta.getEstructurasConstruidas().get(i);

			// Caso especial: EstructuraConstruible (Muros de la grilla)
			if (jSobre.containsKey("hp") && jSobre.containsKey("tipo")) {
				final int x = LectorJSON.getInt(jSobre, "x", 0);
				final int y = LectorJSON.getInt(jSobre, "y", 0);
				final TipoEstructura tipo = LectorJSON.getEnum(jSobre, "tipo", null, TipoEstructura.class);
				if (tipo != null) {
					final EstructuraConstruible est = new EstructuraConstruible(x, y, tipo);
					est.setVida(LectorJSON.getDouble(jSobre, "hp", est.getVida()));
					mundo.meterEntidad(est);
				}
				continue;
			}

			// Caso Universal: Fabricables, Camas, Carpas, Fogatas, etc.
			final Ente instanciado = RegistroEntidades.importar(jSobre, mundo);
			if (instanciado != null) {
				boolean yaExiste = false;
				for (final Ente ent : mundo.getEntes()) {
					if ((ent.getPosicionXInt() == instanciado.getPosicionXInt())
							&& (ent.getPosicionYInt() == instanciado.getPosicionYInt())
							&& (ent.getClass() == instanciado.getClass())) {
						yaExiste = true;
						break;
					}
				}
				if (!yaExiste) {
					mundo.meterEntidad(instanciado);
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
								final Item item = (Item) RegistroEntidades.importar((JSONObject) objItem, null);
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
			final JSONObject jSobre = delta.getItemsEnSuelo().get(i);
			final Ente itemInst = RegistroEntidades.importar(jSobre, mundo);
			if (itemInst instanceof Item) {
				mundo.meterEntidad(itemInst);
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