package principal.entes.criaturas.jugador;

import java.awt.Point;

import org.json.simple.JSONObject;

import principal.configuracion.Dificultad;
import principal.entes.objetos.fabricables.Cama;
import principal.entes.objetos.fabricables.Carpa;
import principal.mapa.Mundo;
import principal.mapa.mapas.Mapa;
import principal.mapa.mapas.MapaManager;
import principal.mapa.mapas.Spawn;
import principal.mapa.persistencia.DeltaMundo;
import principal.maquinaestado.estados.GestorJuego;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Gestor maestro de resurrección con spawn canónico dinámico (mapa1 -> exterior
 * -> Comienzo) y anclaje persistente a camas/carpas (Zero-GC / O(1)).
 * 
 * @version 2.2 (Vanilla Java 8 - Dynamic Project Manifest Resolution)
 */
public class GestorResurreccion {

	public static final String NOMBRE_SPAWN_RESPAWN = "spawn_respawn_jugador";
	private static final String MUNDO_EXTERIOR_DEFECTO = "exterior";

	// Estado del Anclaje Personalizado
	private boolean tieneLechoRegistrado = false;
	private String nombreMapaAncla = MapaManager.MAPA_1;
	private String nombreMundoAncla = MUNDO_EXTERIOR_DEFECTO;
	private int respawnX = 0;
	private int respawnY = 0;
	private boolean anclaEsCamaSolida = false;
	private int idEntidadX = -1;
	private int idEntidadY = -1;

	public GestorResurreccion() {
	}

	// =========================================================================
	// ASIGNACIÓN DE ANCLAJE AL DORMIR
	// =========================================================================

	public void registrarLecho(final Cama cama) {
		if ((cama == null) || (cama.getMundo() == null)) {
			return;
		}

		final Mundo mundo = cama.getMundo();
		this.nombreMapaAncla = this.resolverIdMapa(mundo);
		this.nombreMundoAncla = mundo.getNombreMundo();
		this.respawnX = cama.getCentroX();
		this.respawnY = cama.getPosicionYInt() + cama.getAlto() + 6;
		this.anclaEsCamaSolida = true;
		this.idEntidadX = cama.getPosicionXInt();
		this.idEntidadY = cama.getPosicionYInt();
		this.tieneLechoRegistrado = true;

		Globales.GESTOR_TEXTOS.agregarTexto("Punto de reaparición guardado", cama.getCentroX(),
				cama.getPosicionYInt() - 10, principal.igu.textos.TipoTextoFlotante.ORO_EXP);
	}

	public void registrarLecho(final Carpa carpa) {
		if ((carpa == null) || (carpa.getMundo() == null)) {
			return;
		}

		// En Difícil o Hardcore, las carpas NO guardan punto de reaparición
		final boolean dificultadEstricta = (Globales.dificultad == Dificultad.DIFICIL)
				|| ((Globales.dificultad != null) && Globales.dificultad.esHardcore());

		if (dificultadEstricta) {
			Globales.GESTOR_TEXTOS.agregarTexto("Carpa: Refugio temporal (Spawn no guardado)", carpa.getCentroX(),
					carpa.getPosicionYInt() - 10, principal.igu.textos.TipoTextoFlotante.DANIO_NORMAL);
			return;
		}

		final Mundo mundo = carpa.getMundo();
		this.nombreMapaAncla = this.resolverIdMapa(mundo);
		this.nombreMundoAncla = mundo.getNombreMundo();
		this.respawnX = carpa.getCentroX();
		this.respawnY = carpa.getPosicionYInt() + carpa.getAlto() + 6;
		this.anclaEsCamaSolida = false;
		this.idEntidadX = carpa.getPosicionXInt();
		this.idEntidadY = carpa.getPosicionYInt();
		this.tieneLechoRegistrado = true;

		Globales.GESTOR_TEXTOS.agregarTexto("Punto de reaparición guardado", carpa.getCentroX(),
				carpa.getPosicionYInt() - 10, principal.igu.textos.TipoTextoFlotante.ORO_EXP);
	}

	private String resolverIdMapa(final Mundo mundo) {
		if ((mundo != null) && (mundo.getMapa() != null) && (mundo.getMapa().getManifiesto() != null)) {
			return mundo.getMapa().getManifiesto().getIdMapa();
		}
		return MapaManager.MAPA_1;
	}

	// =========================================================================
	// PIPELINE DE RESURRECCIÓN SEGURO HÍBRIDO (INTRA E INTER-MAPA)
	// =========================================================================

	public void ejecutarReaparicion(final GestorJuego gestorJuego) {
		if ((gestorJuego == null) || (gestorJuego.getMapa() == null)) {
			return;
		}

		final Mapa mapaActual = gestorJuego.getMapa();
		final Jugador jugador = Globales.JUGADOR;

		String targetMapa = this.nombreMapaAncla;
		String targetMundo = this.nombreMundoAncla;
		final int targetX = this.respawnX;
		final int targetY = this.respawnY;

		boolean usarSpawnComienzo = !this.tieneLechoRegistrado;

		// 1. Si tenía un lecho, validar si fue destruido en DeltaMundo
		if (this.tieneLechoRegistrado && (this.idEntidadX >= 0)) {
			if (Globales.GESTOR_DELTAS != null) {
				final DeltaMundo delta = Globales.GESTOR_DELTAS.obtenerOCrearDelta(targetMundo, 0);
				if (delta.isEntidadDestruida(this.idEntidadX, this.idEntidadY)) {
					usarSpawnComienzo = true;
					this.tieneLechoRegistrado = false;
					Globales.GESTOR_TEXTOS.agregarTextoFijo(
							"Tu refugio fue destruido. Reapareces en el campamento base.", Constantes.CENTROX - 120, 60,
							principal.igu.textos.TipoTextoFlotante.DANIO_NORMAL);
				}
			}
		}

		// 2. Fallback Canónico Obligatorio: mapa1 -> exterior -> comienzo
		if (usarSpawnComienzo) {
			targetMapa = MapaManager.MAPA_1;
			targetMundo = MUNDO_EXTERIOR_DEFECTO;
		}

		final String idMapaActual = (mapaActual.getManifiesto() != null) ? mapaActual.getManifiesto().getIdMapa() : "";
		final boolean esMismoMapa = idMapaActual.equalsIgnoreCase(targetMapa);

		if (esMismoMapa) {
			// =================================================================
			// RUTA A: INTRA-MAPA (Mismo proyecto de mapa, swap en memoria)
			// =================================================================
			Mundo mundoDestino = mapaActual.getMundo(targetMundo);
			if (mundoDestino == null) {
				targetMundo = MUNDO_EXTERIOR_DEFECTO;
				mundoDestino = mapaActual.getMundo(targetMundo);
				usarSpawnComienzo = true;
			}

			if (mundoDestino != null) {
				if (usarSpawnComienzo) {
					mapaActual.cambiarMundoInterno(targetMundo, Mundo.CLAVE_PUNTO_SPAWN_COMIENZO);
					jugador.reaparecer(mundoDestino);
				} else {
					mundoDestino.agregarSpawn(new Spawn(new Point(targetX, targetY), NOMBRE_SPAWN_RESPAWN));
					mapaActual.cambiarMundoInterno(targetMundo, NOMBRE_SPAWN_RESPAWN);
					jugador.reaparecer(mundoDestino);
					jugador.setPosicion(targetX, targetY);
				}
			}

		} else {
			// =================================================================
			// RUTA B: INTER-MAPA (Viaje entre proyectos de mapa distintos)
			// =================================================================
			MapaManager.guardarMapaEnTemp(mapaActual);

			final String spawnDestino = usarSpawnComienzo ? Mundo.CLAVE_PUNTO_SPAWN_COMIENZO : NOMBRE_SPAWN_RESPAWN;
			gestorJuego.cargarMapa(null, targetMapa, targetMundo, spawnDestino, false);

			final Mundo mundoNuevo = gestorJuego.getMapa().getMundoActual();
			if (mundoNuevo != null) {
				if (!usarSpawnComienzo) {
					mundoNuevo.agregarSpawn(new Spawn(new Point(targetX, targetY), NOMBRE_SPAWN_RESPAWN));
					mundoNuevo.teletransportarJugadorASpawn(NOMBRE_SPAWN_RESPAWN);
					jugador.reaparecer(mundoNuevo);
					jugador.setPosicion(targetX, targetY);
				} else {
					mundoNuevo.teletransportarJugadorASpawn(Mundo.CLAVE_PUNTO_SPAWN_COMIENZO);
					jugador.reaparecer(mundoNuevo);
				}
			}
		}

		// 3. Estabilización de cámara y controles
		Globales.CAMARA.setEntidadEnfocada(jugador);
		Globales.CAMARA.habilitarGestorLimite();
		Globales.CAMARA.getGestorEfectos().detenerTodosLosEfectos();
		GestorSonido.reproducir(IDSonido.SELECT);
	}

	// =========================================================================
	// PERSISTENCIA JSON
	// =========================================================================

	@SuppressWarnings("unchecked")
	public JSONObject exportarJSON() {
		final JSONObject json = new JSONObject();
		json.put("tieneLecho", Boolean.valueOf(this.tieneLechoRegistrado));
		json.put("mapa", this.nombreMapaAncla);
		json.put("mundo", this.nombreMundoAncla);
		json.put("x", Integer.valueOf(this.respawnX));
		json.put("y", Integer.valueOf(this.respawnY));
		json.put("esCama", Boolean.valueOf(this.anclaEsCamaSolida));
		json.put("idX", Integer.valueOf(this.idEntidadX));
		json.put("idY", Integer.valueOf(this.idEntidadY));
		return json;
	}

	public void importarJSON(final JSONObject json) {
		if (json == null) {
			return;
		}
		if (json.get("tieneLecho") != null) {
			this.tieneLechoRegistrado = Boolean.parseBoolean(json.get("tieneLecho").toString());
		}
		if (json.get("mapa") != null) {
			this.nombreMapaAncla = json.get("mapa").toString();
		}
		if (json.get("mundo") != null) {
			this.nombreMundoAncla = json.get("mundo").toString();
		}
		if (json.get("x") != null) {
			this.respawnX = ((Number) json.get("x")).intValue();
		}
		if (json.get("y") != null) {
			this.respawnY = ((Number) json.get("y")).intValue();
		}
		if (json.get("esCama") != null) {
			this.anclaEsCamaSolida = Boolean.parseBoolean(json.get("esCama").toString());
		}
		if (json.get("idX") != null) {
			this.idEntidadX = ((Number) json.get("idX")).intValue();
		}
		if (json.get("idY") != null) {
			this.idEntidadY = ((Number) json.get("idY")).intValue();
		}
	}
}