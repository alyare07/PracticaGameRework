package principal.entes.criaturas.grupo;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Jugador;
import principal.entes.criaturas.mascotas.Mascota;
import principal.igu.textos.TipoTextoFlotante;
import principal.mapa.Mundo;
import principal.mapa.escenario.tps.PuertaTP;
import principal.utilidades.Globales;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

public class GestorGrupo {

	private final Criatura[] escoltaActiva = new Criatura[Jugador.LIMITE_MAXIMO_SEGUIDORES];
	private int cantidadSeguidores = 0;
	private final ArrayList<Criatura> rosterGeneral = new ArrayList<Criatura>(64);

	public GestorGrupo() {
	}

	public void registrarEnRoster(final Criatura c) {
		if ((c != null) && !this.rosterGeneral.contains(c)) {
			this.rosterGeneral.add(c);
		}
	}

	public void desvincularDeRoster(final Criatura c) {
		if (c != null) {
			this.removerSeguidor(c);
			this.rosterGeneral.remove(c);
			c.setVinculo(TipoVinculo.NINGUNO);
		}
	}

	public int getCapacidadMaximaActual() {
		return (Globales.JUGADOR != null) ? Globales.JUGADOR.getCapacidadLiderazgo() : 0;
	}

	public boolean estaLlenoElGrupo() {
		return this.cantidadSeguidores >= this.getCapacidadMaximaActual();
	}

	public boolean agregarSeguidor(final Criatura c) {
		if ((c == null) || c.estaEliminado()) {
			return false;
		}

		this.registrarEnRoster(c);

		if (this.obtenerIndiceEscolta(c) >= 0) {
			return true;
		}

		if (this.estaLlenoElGrupo()) {
			return false;
		}

		this.escoltaActiva[this.cantidadSeguidores] = c;
		this.cantidadSeguidores++;

		c.getBlackboard().setSiguiendoLider(true);
		return true;
	}

	public boolean removerSeguidor(final Criatura c) {
		if (c == null) {
			return false;
		}

		for (int i = 0; i < this.cantidadSeguidores; i++) {
			if (this.escoltaActiva[i] == c) {
				for (int j = i; j < (this.cantidadSeguidores - 1); j++) {
					this.escoltaActiva[j] = this.escoltaActiva[j + 1];
				}
				this.escoltaActiva[this.cantidadSeguidores - 1] = null;
				this.cantidadSeguidores--;

				c.getBlackboard().setSiguiendoLider(false);
				c.detenerMovimiento();
				c.setEstadoEstandar();
				return true;
			}
		}
		return false;
	}

	public int obtenerIndiceEscolta(final Criatura c) {
		for (int i = 0; i < this.cantidadSeguidores; i++) {
			if (this.escoltaActiva[i] == c) {
				return i;
			}
		}
		return -1;
	}

	public boolean estaEnEscoltaActiva(final Criatura c) {
		return this.obtenerIndiceEscolta(c) >= 0;
	}

	public int getCantidadSeguidoresActivos() {
		return this.cantidadSeguidores;
	}

	public Criatura[] getEscoltaActiva() {
		return this.escoltaActiva;
	}

	public ArrayList<Criatura> getRosterGeneral() {
		return this.rosterGeneral;
	}

	public void vaciar() {
		for (int i = 0; i < this.cantidadSeguidores; i++) {
			this.escoltaActiva[i] = null;
		}
		this.cantidadSeguidores = 0;
		this.rosterGeneral.clear();
	}

	public void migrarEscoltaLocal(final PuertaTP puerta, final double liderOrigenX, final double liderOrigenY) {
		if (puerta == null) {
			return;
		}

		final double radioAcopleSq = 64.0 * 64.0;

		for (int i = 0; i < this.cantidadSeguidores; i++) {
			final Criatura seguidor = this.escoltaActiva[i];
			if ((seguidor == null) || seguidor.estaEliminado()) {
				continue;
			}

			if (!seguidor.getBlackboard().isSiguiendoLider()) {
				continue;
			}

			final double dx = seguidor.getCentroX() - liderOrigenX;
			final double dy = seguidor.getCentroY() - liderOrigenY;
			final double distSq = (dx * dx) + (dy * dy);
			final boolean cerca = distSq <= radioAcopleSq;

			final boolean puedeTp = (seguidor instanceof Mascota) ? ((Mascota) seguidor).isPuedeTparseAlLider()
					: seguidor.getBlackboard().isPuedeTparseAlLider();

			if (cerca || puedeTp) {
				puerta.teletransportar(seguidor);
				seguidor.modificarPosicionX((i + 1) * 8.0);
				seguidor.verificarZoneBox();
			} else {
				GestorSonido.reproducir(IDSonido.SIN_MUNICION);
				Globales.GESTOR_TEXTOS.agregarTexto("¡" + seguidor.getNombre() + " se quedó atrás!", (int) liderOrigenX,
						(int) liderOrigenY - (10 + (i * 12)), TipoTextoFlotante.ESTADO);
			}
		}
	}

	public void migrarEscoltaSubmundo(final Mundo mundoOrigen, final Mundo mundoDestino, final Criatura lider,
			final double liderOrigenX, final double liderOrigenY, final double radioAcople) {
		if ((mundoOrigen == null) || (mundoDestino == null) || (lider == null)) {
			return;
		}

		final double radioAcopleSq = radioAcople * radioAcople;

		for (int i = 0; i < this.cantidadSeguidores; i++) {
			final Criatura seguidor = this.escoltaActiva[i];
			if ((seguidor == null) || seguidor.estaEliminado()) {
				continue;
			}

			if (seguidor.getMundo() != mundoOrigen) {
				continue;
			}

			if (!seguidor.getBlackboard().isSiguiendoLider()) {
				continue;
			}

			final double dx = seguidor.getCentroX() - liderOrigenX;
			final double dy = seguidor.getCentroY() - liderOrigenY;
			final double distSq = (dx * dx) + (dy * dy);
			final boolean cerca = distSq <= radioAcopleSq;

			final boolean puedeTp = (seguidor instanceof Mascota) ? ((Mascota) seguidor).isPuedeTparseAlLider()
					: seguidor.getBlackboard().isPuedeTparseAlLider();

			if (cerca || puedeTp) {
				mundoOrigen.eliminarEntidadRegistro(seguidor);
				seguidor.desvincularDeZonas();

				final double offsetX = (((i % 2) == 0) ? 1 : -1) * (((i / 2) + 1) * 10.0);
				final double offsetY = 12.0;

				seguidor.setPosicion(lider.getPosicionX() + offsetX, lider.getPosicionY() + offsetY);
				seguidor.reiniciarRecorridoAEstrella();
				seguidor.detenerMovimiento();
				seguidor.setEstadoEstandar();

				mundoDestino.meterEntidad(seguidor);
			} else {
				GestorSonido.reproducir(IDSonido.SIN_MUNICION);
				Globales.GESTOR_TEXTOS.agregarTexto("¡" + seguidor.getNombre() + " se quedó atrás!", (int) liderOrigenX,
						(int) liderOrigenY - (10 + (i * 12)), TipoTextoFlotante.ESTADO);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public JSONObject exportarJSON() {
		final JSONObject json = new JSONObject();
		final JSONArray arrRoster = new JSONArray();

		for (int i = 0; i < this.rosterGeneral.size(); i++) {
			final Criatura c = this.rosterGeneral.get(i);
			if ((c != null) && !c.estaEliminado()) {
				final JSONObject jCriat = c.getJsonCriatura();
				final boolean enEscolta = this.estaEnEscoltaActiva(c);
				jCriat.put("enEscolta", Boolean.valueOf(enEscolta));
				jCriat.put("mundoResidencia", (c.getMundo() != null) ? c.getMundo().getNombreMundo() : "exterior");
				arrRoster.add(jCriat);
			}
		}
		json.put("roster", arrRoster);
		return json;
	}

	public void importarJSON(final JSONObject json, final Mundo mundoActivo) {
		if (json == null) {
			return;
		}

		this.vaciar();

		final Object rosterObj = json.get("roster");
		if (rosterObj instanceof JSONArray) {
			final JSONArray arr = (JSONArray) rosterObj;
			for (final Object obj : arr) {
				if (obj instanceof JSONObject) {
					final JSONObject jEntry = (JSONObject) obj;
					final String tipo = (jEntry.get("tipo") != null) ? jEntry.get("tipo").toString() : "";
					final JSONObject entiti = (jEntry.get("entiti") instanceof JSONObject)
							? (JSONObject) jEntry.get("entiti")
							: jEntry;

					if (tipo.equals("Mascota") || entiti.containsKey("vinculo")) {
						final Mascota m = Mascota.crearDesdeJSON(entiti);
						if (m != null) {
							this.registrarEnRoster(m);

							final boolean enEscolta = (jEntry.get("enEscolta") != null)
									? Boolean.parseBoolean(jEntry.get("enEscolta").toString())
									: m.isSiguiendo();

							final String mundoResidencia = (jEntry.get("mundoResidencia") != null)
									? jEntry.get("mundoResidencia").toString()
									: "exterior";

							// Si estaba en la escolta activa, aparece en el mundo donde está el jugador
							if (enEscolta) {
								if (mundoActivo != null) {
									mundoActivo.meterEntidad(m);
								}
								this.agregarSeguidor(m);
							} else // Si estaba esperando en otro mapa/mundo, solo se agrega si estamos en ese
									// mundo
							if ((mundoActivo != null)
									&& mundoActivo.getNombreMundo().equalsIgnoreCase(mundoResidencia)) {
								mundoActivo.meterEntidad(m);
							}
						}
					}
				}
			}
		}
	}
}