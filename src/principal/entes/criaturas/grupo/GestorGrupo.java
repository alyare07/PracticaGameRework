package principal.entes.criaturas.grupo;

import java.util.ArrayList;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Jugador;
import principal.entes.criaturas.mascotas.Mascota;
import principal.igu.textos.TipoTextoFlotante;
import principal.mapa.Mundo;
import principal.mapa.escenario.tps.PuertaTP;
import principal.utilidades.Globales;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Gestor central de Vínculos y Séquito Activo del Jugador. Administra el Roster
 * general y la Escolta Activa coordinando migraciones en puertas (Zero-GC /
 * O(1)).
 * 
 * @version 1.3 (Vanilla Java 8 - Pre-Teleport Proximity Measurement)
 */
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

	public int contarPorVinculo(final TipoVinculo tipo) {
		if (tipo == null) {
			return 0;
		}
		int cuenta = 0;
		for (int i = 0; i < this.rosterGeneral.size(); i++) {
			if (this.rosterGeneral.get(i).getVinculo() == tipo) {
				cuenta++;
			}
		}
		return cuenta;
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

	/**
	 * Migra seguidores a través de puertas locales en el mismo mundo. La distancia
	 * se evalúa con respecto a las coordenadas del jugador ANTES de cruzar la
	 * puerta.
	 */
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

			// Si tiene orden de esperar en el sitio, se queda esperando
			if (!seguidor.getBlackboard().isSiguiendoLider()) {
				continue;
			}

			final double dx = seguidor.getCentroX() - liderOrigenX;
			final double dy = seguidor.getCentroY() - liderOrigenY;
			final double distSq = (dx * dx) + (dy * dy);
			final boolean cerca = distSq <= radioAcopleSq;

			final boolean puedeTp = (seguidor instanceof Mascota) ? ((Mascota) seguidor).isPuedeTparseAlLider()
					: seguidor.getBlackboard().isPuedeTparseAlLider();

			// Si está cerca de la puerta con el jugador SIEMPRE cruza; si está lejos solo
			// cruza si tiene TP activo
			final boolean acompana = cerca || puedeTp;

			if (acompana) {
				puerta.teletransportar(seguidor);
				seguidor.modificarPosicionX((i + 1) * 8.0);
			} else {
				GestorSonido.reproducir(IDSonido.SIN_MUNICION);
				Globales.GESTOR_TEXTOS.agregarTexto("¡" + seguidor.getNombre() + " se quedó atrás!", (int) liderOrigenX,
						(int) liderOrigenY - (10 + (i * 12)), TipoTextoFlotante.ESTADO);
			}
		}
	}

	/**
	 * Migra atómicamente a los seguidores que califican hacia el nuevo submundo. La
	 * cercanía se mide con la posición que tenía el jugador en el mundo origen al
	 * pisar la puerta.
	 */
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

			// Si tiene orden de esperar en el mapa anterior, se queda
			if (!seguidor.getBlackboard().isSiguiendoLider()) {
				continue;
			}

			// Distancia con el jugador ANTES de que el jugador cruzara
			final double dx = seguidor.getCentroX() - liderOrigenX;
			final double dy = seguidor.getCentroY() - liderOrigenY;
			final double distSq = (dx * dx) + (dy * dy);
			final boolean cerca = distSq <= radioAcopleSq;

			final boolean puedeTp = (seguidor instanceof Mascota) ? ((Mascota) seguidor).isPuedeTparseAlLider()
					: seguidor.getBlackboard().isPuedeTparseAlLider();

			// Si está cerca en la puerta SIEMPRE cruza; si está lejos depende del modo de
			// rescate
			final boolean acompana = cerca || puedeTp;

			if (acompana) {
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
}